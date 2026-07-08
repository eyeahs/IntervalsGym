import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.gradle.play.publisher)
}

val oauthProperties = Properties()
val oauthPropertiesFile = file(
    providers.gradleProperty("intervalsgym.oauth.properties").orNull
        ?: "/Users/hyunwoo.pr/Dev/private_settings/intervalsgym_oauth.properties"
)
if (oauthPropertiesFile.isFile) {
    oauthPropertiesFile.inputStream().use { input -> oauthProperties.load(input) }
}

fun oauthProperty(name: String, fallback: String = ""): String {
    return providers.gradleProperty("intervalsgym.oauth.$name").orNull
        ?: oauthProperties.getProperty(name).orEmpty().ifBlank { fallback }
}

fun buildConfigString(value: String): String {
    return "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""
}

val nativeAlphaValue = oauthProperty("intervals.clientId")
val nativeBetaValue = oauthProperty("intervals.clientSecret")
val intervalsOAuthRedirectScheme = oauthProperty("intervals.redirectScheme", "intervalsgym")
val intervalsOAuthRedirectHost = oauthProperty("intervals.redirectHost", "intervals-oauth")
val intervalsOAuthRedirectUri = "$intervalsOAuthRedirectScheme://$intervalsOAuthRedirectHost"
val generatedNativeBridgeDir = layout.buildDirectory.dir("generated/nativeBridge/cpp")
val generatedNativeBridgeCMakeLists = generatedNativeBridgeDir.map { it.file("CMakeLists.txt") }
val generatedGridFrameSource = generatedNativeBridgeDir.map { it.file("grid_frame.cpp") }
val generatedPanelMeshSource = generatedNativeBridgeDir.map { it.file("panel_mesh.cpp") }

fun nativeBridgeShare(index: Int): Int {
    return (0xC3 + index * 73 + (index % 5) * 41) and 0xFF
}

fun generatedCMakeListsText(): String {
    return """
        cmake_minimum_required(VERSION 3.22.1)

        project(app_bridge)

        add_library(
            panel_mesh
            SHARED
            panel_mesh.cpp
        )

        add_library(
            grid_frame
            SHARED
            grid_frame.cpp
        )

        target_compile_features(panel_mesh PRIVATE cxx_std_17)
        target_compile_features(grid_frame PRIVATE cxx_std_17)
        target_link_libraries(grid_frame PRIVATE panel_mesh)
    """.trimIndent()
}

fun List<Int>.toCppBytes(): String {
    return map { value -> "static_cast<unsigned char>($value)" }
        .ifEmpty { listOf("0") }
        .joinToString(", ")
}

fun generatedPanelMeshCpp(
    alphaShare: List<Int>,
    betaShare: List<Int>,
): String {
    return """
        extern "C" const unsigned char* panel_mesh_pull(int selector, int* length) {
            switch (selector) {
                case 0: {
                    static const unsigned char share[] = { ${alphaShare.toCppBytes()} };
                    *length = ${alphaShare.size};
                    return share;
                }
                case 1: {
                    static const unsigned char share[] = { ${betaShare.toCppBytes()} };
                    *length = ${betaShare.size};
                    return share;
                }
                default:
                    *length = 0;
                    return nullptr;
            }
        }
    """.trimIndent()
}

fun generatedGridFrameCpp(
    alphaShare: List<Int>,
    betaShare: List<Int>,
): String {
    fun localShareCase(selector: Int, shareBytes: List<Int>): String {
        return """
                case $selector: {
                    static const unsigned char share[] = { ${shareBytes.toCppBytes()} };
                    *length = ${shareBytes.size};
                    return share;
                }
        """.trimIndent()
    }

    return """
        #include <jni.h>
        #include <string>

        extern "C" const unsigned char* panel_mesh_pull(int selector, int* length);

        static const unsigned char* grid_frame_pull(int selector, int* length) {
            switch (selector) {
        ${localShareCase(0, alphaShare).prependIndent("        ")}
        ${localShareCase(1, betaShare).prependIndent("        ")}
                default:
                    *length = 0;
                    return nullptr;
            }
        }

        static std::string fold_value(int selector) {
            int leftLength = 0;
            int rightLength = 0;
            const unsigned char* left = grid_frame_pull(selector, &leftLength);
            const unsigned char* right = panel_mesh_pull(selector, &rightLength);
            if (left == nullptr || right == nullptr || leftLength != rightLength) {
                return "";
            }
            std::string value;
            value.resize(leftLength);
            for (int index = 0; index < leftLength; ++index) {
                value[index] = static_cast<char>(left[index] ^ right[index]);
            }
            return value;
        }

        static jstring emit_value(JNIEnv* env, int selector) {
            const std::string value = fold_value(selector);
            return env->NewStringUTF(value.c_str());
        }

        extern "C" JNIEXPORT jstring JNICALL
        Java_com_lighthousepark_intervalsgym_data_NativeAppBridge_readAlpha(
                JNIEnv* env,
                jobject /* this */) {
            return emit_value(env, 0);
        }

        extern "C" JNIEXPORT jstring JNICALL
        Java_com_lighthousepark_intervalsgym_data_NativeAppBridge_readBeta(
                JNIEnv* env,
                jobject /* this */) {
            return emit_value(env, 1);
        }
    """.trimIndent()
}

fun splitNativeValue(value: String): Pair<List<Int>, List<Int>> {
    val valueBytes = value
        .toByteArray(Charsets.UTF_8)
        .map { byte -> byte.toInt() and 0xFF }
    val shareA = valueBytes.mapIndexed { index, _ -> nativeBridgeShare(index) }
    val shareB = valueBytes.mapIndexed { index, byte -> byte xor shareA[index] }
    return shareA to shareB
}

fun writeNativeBridgeSources(
    alphaValue: String,
    betaValue: String,
) {
    val (alphaA, alphaB) = splitNativeValue(alphaValue)
    val (betaA, betaB) = splitNativeValue(betaValue)
    generatedNativeBridgeCMakeLists.get().asFile.writeText(generatedCMakeListsText())
    generatedGridFrameSource.get().asFile.writeText(
        generatedGridFrameCpp(
            alphaShare = alphaA,
            betaShare = betaA
        )
    )
    generatedPanelMeshSource.get().asFile.writeText(
        generatedPanelMeshCpp(
            alphaShare = alphaB,
            betaShare = betaB
        )
    )
}

val generateNativeBridge by tasks.registering {
    outputs.file(generatedNativeBridgeCMakeLists)
    outputs.file(generatedGridFrameSource)
    outputs.file(generatedPanelMeshSource)
    outputs.upToDateWhen { false }
    doLast {
        generatedNativeBridgeDir.get().asFile.mkdirs()
        writeNativeBridgeSources(
            alphaValue = nativeAlphaValue,
            betaValue = nativeBetaValue
        )
    }
}

val generatedNativeBootstrapDir = generatedNativeBridgeDir.get().asFile
generatedNativeBootstrapDir.mkdirs()
if (!generatedNativeBridgeCMakeLists.get().asFile.isFile) {
    writeNativeBridgeSources(alphaValue = "", betaValue = "")
}

android {
    namespace = "com.lighthousepark.intervalsgym"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.lighthousepark.intervalsgym"
        minSdk = 33
        targetSdk = 36
        versionCode = 17
        versionName = "1.3.13"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["intervalsOAuthRedirectScheme"] = intervalsOAuthRedirectScheme
        manifestPlaceholders["intervalsOAuthRedirectHost"] = intervalsOAuthRedirectHost
        buildConfigField("String", "INTERVALS_OAUTH_REDIRECT_SCHEME", buildConfigString(intervalsOAuthRedirectScheme))
        buildConfigField("String", "INTERVALS_OAUTH_REDIRECT_HOST", buildConfigString(intervalsOAuthRedirectHost))
        buildConfigField("String", "INTERVALS_OAUTH_REDIRECT_URI", buildConfigString(intervalsOAuthRedirectUri))
    }

    signingConfigs {
        create("release") {
            val signingStoreFile = providers.gradleProperty("intervalsgym.signing.storeFile").orNull
            if (!signingStoreFile.isNullOrBlank()) {
                storeFile = file(signingStoreFile)
                storePassword = providers.gradleProperty("intervalsgym.signing.storePassword").orNull
                keyAlias = providers.gradleProperty("intervalsgym.signing.keyAlias").orNull
                keyPassword = providers.gradleProperty("intervalsgym.signing.keyPassword").orNull
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    ndkVersion = "26.1.10909125"
    externalNativeBuild {
        cmake {
            path = generatedNativeBridgeCMakeLists.get().asFile
            version = "3.22.1"
        }
    }
}

tasks.configureEach {
    if (name.contains("CMake")) {
        dependsOn(generateNativeBridge)
    }
}

play {
    serviceAccountCredentials.set(
        providers.gradleProperty("intervalsgym.play.serviceAccountJson")
            .map { layout.projectDirectory.file(it) },
    )
    track.set("internal")
    releaseStatus.set(com.github.triplet.gradle.androidpublisher.ReleaseStatus.COMPLETED)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
    testImplementation(libs.json)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

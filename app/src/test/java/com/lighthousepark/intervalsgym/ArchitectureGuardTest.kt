package com.lighthousepark.intervalsgym

import java.nio.file.Files
import kotlin.streams.toList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchitectureGuardTest {
    private val projectRoot = ArchitectureGuardProject.projectRoot
    private val mainSourceRoot = ArchitectureGuardProject.mainSourceRoot
    private val testSourceRoot = ArchitectureGuardProject.testSourceRoot
    private val androidTestSourceRoot = ArchitectureGuardProject.androidTestSourceRoot

    @Test
    fun kotlinSourcesDoNotUseWildcardImports() {
        val violations = listOf(mainSourceRoot, testSourceRoot, androidTestSourceRoot)
            .flatMap(::kotlinFiles)
            .flatMap { path ->
                Files.readAllLines(path)
                    .mapIndexedNotNull { index, line ->
                        "${path.relativeToProject()}:${index + 1}".takeIf {
                            Regex("""^import .*\.\*""").containsMatchIn(line)
                        }
                    }
            }

        assertEquals(emptyList<String>(), violations)
    }

    @Test
    fun dateTimeFormatterPatternsStayCentralized() {
        val violations = kotlinFiles(mainSourceRoot)
            .filter { path ->
                Files.readString(path).contains("DateTimeFormatter.ofPattern") &&
                    path.fileName.toString() != "AppDateTimeFormatters.kt"
            }
            .map { it.relativeToProject() }

        assertEquals(emptyList<String>(), violations)
    }

    @Test
    fun generatedNativeBridgeLibrariesUse16KbElfAlignment() {
        val appBuildScript = Files.readString(projectRoot.resolve("app/build.gradle.kts"))
        val sixteenKbLinkOptions = { target: String ->
            Regex(
                """target_link_options\(\s*$target\s*PRIVATE\s*"-Wl,-z,max-page-size=16384"\s*"-Wl,-z,common-page-size=16384"\s*\)"""
            )
        }

        assertTrue(sixteenKbLinkOptions("panel_mesh").containsMatchIn(appBuildScript))
        assertTrue(sixteenKbLinkOptions("grid_frame").containsMatchIn(appBuildScript))
    }

    @Test
    fun privateBuildConfigurationDefaultsDoNotDependOnOneDeveloperHomeDirectory() {
        val appBuildScript = Files.readString(projectRoot.resolve("app/build.gradle.kts"))
        val publishScript = Files.readString(projectRoot.resolve("scripts/publish_internal_test.sh"))

        assertFalse(appBuildScript.contains("/Users/"))
        assertFalse(publishScript.contains("/Users/"))
        assertTrue(appBuildScript.contains("private_settings/intervalsgym_oauth.properties"))
        assertTrue(publishScript.contains("../private_settings/intervalsgym_publish_config.json"))
    }

    @Test
    fun appThemeUsesDarkSurfacesWithTheSelectedHighlight() {
        val palette = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/core/AppColorPalette.kt")
        )
        val colors = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/ui/theme/Color.kt")
        )
        val theme = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/ui/theme/Theme.kt")
        )
        val androidTheme = Files.readString(
            projectRoot.resolve("app/src/main/res/values/themes.xml")
        )

        listOf(
            "AppHighlight = Color(AppColorPalette.HIGHLIGHT)",
            "AppBackground = Color(AppColorPalette.BACKGROUND)",
            "AppSurface = Color(AppColorPalette.SURFACE)",
            "AppSurfaceHigh = Color(AppColorPalette.SURFACE_HIGH)",
            "AppSurfaceBright = Color(AppColorPalette.SURFACE_BRIGHT)",
            "AppSurfaceContainerLowest = Color(AppColorPalette.SURFACE_CONTAINER_LOWEST)",
            "AppSurfaceContainerLow = Color(AppColorPalette.SURFACE_CONTAINER_LOW)",
            "AppSurfaceContainer = Color(AppColorPalette.SURFACE_CONTAINER)",
            "AppSurfaceContainerHigh = Color(AppColorPalette.SURFACE_CONTAINER_HIGH)",
            "AppSurfaceContainerHighest = Color(AppColorPalette.SURFACE_CONTAINER_HIGHEST)",
            "AppSurfaceDim = AppBackground",
            "AppHighlightContainer = Color(AppColorPalette.HIGHLIGHT_CONTAINER)",
            "AppOnHighlightContainer = Color(AppColorPalette.ON_HIGHLIGHT_CONTAINER)",
            "AppCoolAccent = Color(AppColorPalette.COOL_ACCENT)",
            "AppCoolAccentMuted = Color(AppColorPalette.COOL_ACCENT_MUTED)",
            "AppCoolContainer = Color(AppColorPalette.COOL_CONTAINER)",
            "AppOnCoolContainer = Color(AppColorPalette.ON_COOL_CONTAINER)",
            "AppText = Color(AppColorPalette.TEXT)",
            "AppTextMuted = Color(AppColorPalette.TEXT_MUTED)",
            "AppOutline = Color(AppColorPalette.OUTLINE)",
            "AppOutlineSoft = Color(AppColorPalette.OUTLINE_SOFT)",
            "AppDanger = Color(AppColorPalette.DANGER)",
            "AppDangerContainer = Color(AppColorPalette.DANGER_CONTAINER)",
            "AppGraphOrange1 = Color(AppColorPalette.GRAPH_ORANGE_1)",
            "AppGraphOrange7 = Color(AppColorPalette.GRAPH_ORANGE_7)",
            "AppGraphHeartRate = Color(AppColorPalette.GRAPH_HEART_RATE)",
            "AppGraphRouteBackground = Color(AppColorPalette.GRAPH_ROUTE_BACKGROUND)"
        ).forEach { token ->
            assertTrue("Missing theme color: $token", colors.contains(token))
        }
        listOf(
            "const val BACKGROUND = 0xFF08090BL",
            "const val HIGHLIGHT = 0xFFFF4E01L",
            "const val GRAPH_ORANGE_1 = 0xFFFFD1B8L",
            "const val GRAPH_ORANGE_7 = 0xFFD83A00L",
            "const val GRAPH_HEART_RATE = 0xFFFF6424L",
            "const val OVERLAY_ACTION_BACKGROUND = HIGHLIGHT"
        ).forEach { token ->
            assertTrue("Missing global palette color: $token", palette.contains(token))
        }
        assertTrue(theme.contains("primary = AppHighlight"))
        assertTrue(theme.contains("private val DarkColorScheme = darkColorScheme("))
        assertTrue(theme.contains("colorScheme = DarkColorScheme"))
        assertTrue(theme.contains("onPrimaryContainer = AppOnHighlightContainer"))
        assertTrue(theme.contains("secondary = AppCoolAccent"))
        assertTrue(theme.contains("tertiary = AppCoolAccentMuted"))
        assertTrue(theme.contains("tertiaryContainer = AppSurfaceHigh"))
        assertTrue(theme.contains("onTertiaryContainer = AppOnCoolContainer"))
        assertTrue(theme.contains("errorContainer = AppDangerContainer"))
        assertTrue(theme.contains("onError = AppBackground"))
        assertTrue(theme.contains("onErrorContainer = AppOnDangerContainer"))
        listOf(
            "surfaceBright = AppSurfaceBright",
            "surfaceContainerLowest = AppSurfaceContainerLowest",
            "surfaceContainerLow = AppSurfaceContainerLow",
            "surfaceContainer = AppSurfaceContainer",
            "surfaceContainerHigh = AppSurfaceContainerHigh",
            "surfaceContainerHighest = AppSurfaceContainerHighest",
            "surfaceDim = AppSurfaceDim"
        ).forEach { token ->
            assertTrue("Missing Material surface role: $token", theme.contains(token))
        }
        assertTrue(androidTheme.contains("parent=\"android:Theme.Material.NoActionBar\""))
        assertTrue(androidTheme.contains("<item name=\"android:windowBackground\">@color/app_background</item>"))
        assertTrue(androidTheme.contains("<item name=\"android:windowLightStatusBar\">false</item>"))
        assertTrue(androidTheme.contains("<item name=\"android:windowLightNavigationBar\">false</item>"))
    }

    @Test
    fun kotlinArgbLiteralsStayInGlobalAppColorPalette() {
        val palettePath = mainSourceRoot.resolve(
            "com/lighthousepark/intervalsgym/core/AppColorPalette.kt"
        )
        val argbLiteral = Regex("""0x[0-9A-Fa-f]{8}L?""")
        val violations = kotlinFiles(mainSourceRoot)
            .filterNot { it == palettePath }
            .filter { path -> argbLiteral.containsMatchIn(Files.readString(path)) }
            .map { it.relativeToProject() }

        assertEquals(emptyList<String>(), violations)
    }

    @Test
    fun directNamedColorsStayInGlobalAppColorPalette() {
        val directNamedColor = Regex(
            """(?:android\.graphics\.)?Color\.(?:Black|White|Red|Green|Blue|Yellow|Gray|DarkGray|LightGray|Cyan|Magenta|Transparent|BLACK|WHITE|RED|GREEN|BLUE|YELLOW|GRAY|DKGRAY|LTGRAY|CYAN|MAGENTA|TRANSPARENT)\b"""
        )
        val violations = kotlinFiles(mainSourceRoot)
            .filter { path -> directNamedColor.containsMatchIn(Files.readString(path)) }
            .map { it.relativeToProject() }

        assertEquals(emptyList<String>(), violations)
    }

    @Test
    fun xmlColorLiteralsStayInGlobalColorResources() {
        val resourceRoot = projectRoot.resolve("app/src/main/res")
        val palettePath = resourceRoot.resolve("values/colors.xml")
        val colorLiteral = Regex("""#[0-9A-Fa-f]{3,8}""")
        val violations = Files.walk(resourceRoot).use { paths ->
            paths
                .filter { path -> Files.isRegularFile(path) && path.toString().endsWith(".xml") }
                .filter { path -> path != palettePath }
                .filter { path -> colorLiteral.containsMatchIn(Files.readString(path)) }
                .map { it.relativeToProject() }
                .toList()
        }

        assertEquals(emptyList<String>(), violations)
    }

    @Test
    fun appRootDoesNotOwnRoutePayloadOrGenericModifierHelpers() {
        val text = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/app/AppRoot.kt")
        )

        assertFalse(text.contains("fun TrainingItem?.toRouteJson"))
        assertFalse(text.contains("fun Modifier.throttleRapidTaps"))
    }

    @Test
    fun appRouteRegistryStaysOutOfAppRoot() {
        val appRoot = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/app/AppRoot.kt")
        )
        val appNavGraph = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/app/AppNavGraph.kt")
        )
        val routeRegistryTokens = listOf(
            "internal fun AppNavGraph",
            "NavHost(",
            "composable("
        )

        routeRegistryTokens.forEach { token ->
            assertFalse("$token belongs in AppNavGraph.kt", appRoot.contains(token))
            assertTrue("$token missing from AppNavGraph.kt", appNavGraph.contains(token))
        }
        assertFalse(
            "AppRoot.kt should not import destination UI screens.",
            Regex("""import com\.lighthousepark\.intervalsgym\..*\.ui\.""").containsMatchIn(appRoot)
        )
    }

    @Test
    fun appNavGraphDoesNotReadStorageOrBuildDataQueries() {
        val appNavGraph = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/app/AppNavGraph.kt")
        )
        val forbiddenTokens = listOf(
            "LocalContext.current",
            "getSharedPreferences(",
            "PREFS_NAME",
            "SessionHistoryQueryUseCase("
        )

        forbiddenTokens.forEach { token ->
            assertFalse("$token belongs in AppRoot or a route owner, not AppNavGraph.kt", appNavGraph.contains(token))
        }
        assertTrue(appNavGraph.contains("completedStrengthHistory: List<CompletedStrengthSession>"))
        assertTrue(appNavGraph.contains("history = completedStrengthHistory"))
    }

    @Test
    fun rapidTapThrottleIsScopedToTransitionsAndPopupActions() {
        val appNavGraph = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/app/AppNavGraph.kt")
        )
        val ongoingRows = Files.readString(
            mainSourceRoot.resolve(
                "com/lighthousepark/intervalsgym/strength/ui/StrengthSessionOngoingRoutineRows.kt"
            )
        )
        val sessionChrome = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionChrome.kt")
        )

        assertFalse(appNavGraph.contains(".throttleRapidTaps()"))
        assertTrue(appNavGraph.contains("RapidActionThrottle()"))
        assertTrue(ongoingRows.contains("throttleRapidTaps(enabled = !isSupersetSelectionMode)"))
        assertTrue(sessionChrome.contains(".throttleRapidTaps()"))
    }

    @Test
    fun appNavigationAvoidsFullScreenSlideTransitions() {
        val appNavGraph = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/app/AppNavGraph.kt")
        )

        assertFalse(appNavGraph.contains("slideIntoContainer("))
        assertFalse(appNavGraph.contains("slideOutOfContainer("))
        assertTrue(appNavGraph.contains("fadeIn(animationSpec = tween(ROUTE_FADE_IN_MILLIS))"))
        assertTrue(appNavGraph.contains("fadeOut(animationSpec = tween(ROUTE_FADE_OUT_MILLIS))"))
        assertTrue(appNavGraph.contains("ROUTE_FADE_IN_MILLIS = 120"))
        assertTrue(appNavGraph.contains("ROUTE_FADE_OUT_MILLIS = 90"))
    }

    @Test
    fun appRootUsesStrengthRouteStateHelpers() {
        val appRoot = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/app/AppRoot.kt")
        )
        val appNavGraph = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/app/AppNavGraph.kt")
        )
        val strengthRouteState = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/app/AppStrengthRouteState.kt")
        )
        val helperDefinitions = listOf(
            "internal data class AppStrengthRoutineSaveResult",
            "internal fun strengthSessionRoutine",
            "internal fun List<String>.withDeletedCalendarRoutineIds",
            "internal fun List<StrengthWorkoutRoutine>.withWorkoutResultApplied",
            "internal fun CompletedStrengthSession.toRouteStrengthRoutineOverride",
            "internal fun appStrengthRoutineSaveResult",
            "internal fun List<StrengthWorkoutRoutine>.withoutStrengthRoutine",
            "internal fun Int?.withoutDeletedStrengthRoutine",
            "internal fun ActiveStrengthSession?.isForRoutine"
        )

        helperDefinitions.forEach { definition ->
            assertFalse("$definition belongs in AppStrengthRouteState.kt", appRoot.contains(definition))
            assertFalse("$definition belongs in AppStrengthRouteState.kt", appNavGraph.contains(definition))
            assertTrue("$definition missing from AppStrengthRouteState.kt", strengthRouteState.contains(definition))
        }
        listOf(
            "copyForWorkout(",
            "workout.entries.map",
            "routine.copy(id =",
            "filterNot { it.id == routine.id }",
            "deletedCalendarRoutineIdList + routine.id"
        ).forEach { token ->
            assertFalse("$token belongs behind AppStrengthRouteState.kt", appRoot.contains(token))
        }
        assertTrue(appRoot.contains("appStrengthRoutineSaveResult("))
        assertTrue(appRoot.contains("withWorkoutResultApplied("))
        assertTrue(appRoot.contains("toRouteStrengthRoutineOverride("))
        assertTrue(appNavGraph.contains("strengthSessionRoutine("))
    }

    @Test
    fun appConfigFilesDoNotUseProjectWildcardImports() {
        val appConfigFiles = listOf(
            "AppPreferences.kt",
            "AppRoutes.kt",
            "AppRoutePayloads.kt",
            "AppNavGraph.kt",
            "AppStrengthRouteState.kt"
        ).map { fileName -> mainSourceRoot.resolve("com/lighthousepark/intervalsgym/app/$fileName") }
        val violations = appConfigFiles
            .filter { path ->
                Regex("""import com\.lighthousepark\.intervalsgym\..*\.\*""")
                    .containsMatchIn(Files.readString(path))
            }
            .map { it.relativeToProject() }

        assertEquals(emptyList<String>(), violations)
    }

    @Test
    fun appRootDoesNotUseProjectWildcardImports() {
        val routeOwner = mainSourceRoot.resolve("com/lighthousepark/intervalsgym/app/AppRoot.kt")
        val text = Files.readString(routeOwner)

        assertFalse(
            "AppRoot.kt should keep top-level route owner dependencies explicit.",
            Regex("""import com\.lighthousepark\.intervalsgym\..*\.\*""").containsMatchIn(text)
        )
    }

    @Test
    fun coreTestsDoNotUseProjectWildcardImports() {
        val coreTestRoot = testSourceRoot.resolve("com/lighthousepark/intervalsgym/core")
        val violations = kotlinFiles(coreTestRoot)
            .filter { path ->
                Regex("""import com\.lighthousepark\.intervalsgym\..*\.\*""")
                    .containsMatchIn(Files.readString(path))
            }
            .map { it.relativeToProject() }

        assertEquals(emptyList<String>(), violations)
    }

    @Test
    fun appRootUsesIntervalsOAuthSessionStorageForOAuthPrefs() {
        val appRoot = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/app/AppRoot.kt")
        )
        val forbiddenCalls = listOf(
            "LEGACY_INTERVALS_CREDENTIAL_PREF",
            "INTERVALS_LOGIN_PROMPT_SEEN_PREF",
            "INTERVALS_OAUTH_TOKEN_PREF",
            "INTERVALS_OAUTH_STATE_PREF",
            "toIntervalsOAuthToken(",
            "token.toJsonString()",
            "prefs.edit()"
        )

        forbiddenCalls.forEach { call ->
            assertFalse("$call belongs behind IntervalsOAuthSessionStorage", appRoot.contains(call))
        }
        assertTrue(appRoot.contains("IntervalsOAuthSessionStorage("))
        assertTrue(appRoot.contains("intervalsOAuthSessionStorage.loadToken("))
        assertTrue(appRoot.contains("intervalsOAuthSessionStorage.savePendingState("))
        assertTrue(appRoot.contains("intervalsOAuthSessionStorage.loadPendingState("))
        assertTrue(appRoot.contains("intervalsOAuthSessionStorage.saveConnectedToken("))
        assertTrue(appRoot.contains("intervalsOAuthSessionStorage.clearConnectedToken("))
        assertTrue(appRoot.contains("intervalsOAuthSessionStorage.markLoginPromptSeen("))
    }

    @Test
    fun overlayFilesDoNotUseProjectWildcardImports() {
        val overlayRoot = mainSourceRoot.resolve("com/lighthousepark/intervalsgym/overlay")
        val violations = kotlinFiles(overlayRoot)
            .filter { path ->
                Regex("""import com\.lighthousepark\.intervalsgym\..*\.\*""")
                    .containsMatchIn(Files.readString(path))
            }
            .map { it.relativeToProject() }

        assertEquals(emptyList<String>(), violations)
    }

}

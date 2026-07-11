package com.lighthousepark.intervalsgym

import java.nio.file.Files
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
    fun appThemeUsesTheSelectedHighlightAndSupportingPalettes() {
        val colors = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/ui/theme/Color.kt")
        )
        val theme = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/ui/theme/Theme.kt")
        )

        listOf(
            "AppHighlight = Color(0xFFFF4E01)",
            "AppBackground = Color(0xFFE3F0FF)",
            "AppSurface = Color(0xFFD5E6FF)",
            "AppSurfaceHigh = Color(0xFF89ABF2)",
            "AppSurfaceBright = Color(0xFFF1F7FF)",
            "AppSurfaceContainer = Color(0xFFC5DAFA)",
            "AppSurfaceContainerHigh = Color(0xFFABC7F5)",
            "AppSurfaceDim = Color(0xFFB6CEF4)",
            "AppHighlightContainer = Color(0xFF102347)",
            "AppCoolAccent = Color(0xFF89ABF2)",
            "AppCoolAccentMuted = Color(0xFF95ABE0)",
            "AppCoolContainer = AppSurfaceContainer",
            "AppOnCoolContainer = AppHighlightContainer",
            "AppText = AppHighlightContainer",
            "AppTextMuted = Color(0xFF465A86)",
            "AppOutline = Color(0xFF6A7798)",
            "AppOutlineSoft = AppCoolContainer",
            "AppSuccess = Color(0xFF00A600)",
            "AppSuccessSoft = Color(0xFFA3AF9F)",
            "AppDanger = AppHighlight",
            "AppDangerContainer = AppSurfaceContainerHigh"
        ).forEach { token ->
            assertTrue("Missing theme color: $token", colors.contains(token))
        }
        assertTrue(theme.contains("primary = AppHighlight"))
        assertTrue(theme.contains("private val LightColorScheme = lightColorScheme("))
        assertTrue(theme.contains("colorScheme = LightColorScheme"))
        assertTrue(theme.contains("onPrimaryContainer = AppHighlight"))
        assertTrue(theme.contains("secondary = AppCoolAccent"))
        assertTrue(theme.contains("tertiary = AppCoolAccentMuted"))
        assertTrue(theme.contains("tertiaryContainer = AppCoolContainer"))
        assertTrue(theme.contains("onTertiaryContainer = AppOnCoolContainer"))
        assertTrue(theme.contains("errorContainer = AppDangerContainer"))
        assertTrue(theme.contains("onError = AppHighlightContainer"))
        assertTrue(theme.contains("onErrorContainer = AppText"))
        listOf(
            "surfaceBright = AppSurfaceBright",
            "surfaceContainerLowest = AppSurfaceBright",
            "surfaceContainerLow = AppSurface",
            "surfaceContainer = AppSurfaceContainer",
            "surfaceContainerHigh = AppSurfaceContainerHigh",
            "surfaceContainerHighest = AppCoolAccentMuted",
            "surfaceDim = AppSurfaceDim"
        ).forEach { token ->
            assertTrue("Missing Material surface role: $token", theme.contains(token))
        }
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
        val strengthRouteState = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/app/AppStrengthRouteState.kt")
        )
        val helperDefinitions = listOf(
            "internal data class AppStrengthRoutineSaveResult",
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

package com.lighthousepark.intervalsgym

import java.nio.file.Files
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningSessionUiArchitectureGuardTest {
    private val mainSourceRoot = ArchitectureGuardProject.mainSourceRoot

    @Test
    fun runningSessionComponentsStayOutOfSessionScreen() {
        val sessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionScreen.kt")
        )
        val oldComponentsBucket = mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionComponents.kt")
        val chrome = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionChrome.kt")
        )
        val choiceDialogs = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionChoiceDialogs.kt")
        )
        val statusPanels = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionStatusPanels.kt")
        )
        val renderComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionRenderComponents.kt")
        )
        val blockComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionBlockComponents.kt")
        )
        val combinedFocusedComponents = listOf(
            chrome,
            choiceDialogs,
            statusPanels,
            blockComponents,
            renderComponents
        ).joinToString("\n")
        val movedDefinitionsByOwner = mapOf(
            "RunningSessionChrome.kt" to Pair(
                chrome,
                listOf(
                    "internal fun RunningSessionActionBar",
                    "internal fun RunningSessionTopBar"
                )
            ),
            "RunningSessionChoiceDialogs.kt" to Pair(
                choiceDialogs,
                listOf(
                    "internal fun RunningStopSaveDialog"
                )
            ),
            "RunningSessionStatusPanels.kt" to Pair(
                statusPanels,
                listOf(
                    "internal fun RunningWarmupPanel",
                    "internal fun RunningFinishedPanel"
                )
            )
        )

        assertFalse(
            "Running session components should stay split into chrome, choice dialogs, status panels, block, render, and heart-rate files.",
            Files.exists(oldComponentsBucket)
        )
        movedDefinitionsByOwner.forEach { (ownerName, ownerAndDefinitions) ->
            val owner = ownerAndDefinitions.first
            val definitions = ownerAndDefinitions.second
            definitions.forEach { definition ->
                assertFalse("$definition belongs in a focused running session component file", sessionScreen.contains(definition))
                assertTrue("$definition missing from its focused running session component file", owner.contains(definition))
                movedDefinitionsByOwner
                    .filterKeys { it != ownerName }
                    .forEach { (otherOwnerName, otherOwnerAndDefinitions) ->
                        assertFalse(
                            "$definition belongs in $ownerName, not $otherOwnerName",
                            otherOwnerAndDefinitions.first.contains(definition)
                        )
                    }
            }
        }
        listOf(
            "internal fun RunningBlockMetric"
        ).forEach { deletedDefinition ->
            assertFalse("$deletedDefinition was unused; do not recreate dead running UI helpers.", combinedFocusedComponents.contains(deletedDefinition))
        }
    }

    @Test
    fun runningSessionChromeDialogsAndPanelsStayInFocusedFiles() {
        val sessionComponentsPath = mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionComponents.kt")
        assertFalse(Files.exists(sessionComponentsPath))
        val owners = mapOf(
            "RunningSessionChrome.kt" to listOf(
                "RunningSessionActionBar(",
                "RunningSessionTopBar("
            ),
            "RunningSessionChoiceDialogs.kt" to listOf(
                "RunningStopSaveDialog("
            ),
            "RunningSessionStatusPanels.kt" to listOf(
                "RunningWarmupPanel(",
                "RunningFinishedPanel("
            )
        )

        owners.forEach { (fileName, calls) ->
            val owner = Files.readString(mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/$fileName"))
            calls.forEach { call ->
                assertTrue("$call should stay in $fileName", owner.contains(call))
            }
        }
    }

    @Test
    fun runningSessionRenderComponentsStayOutOfSessionScreen() {
        val sessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionScreen.kt")
        )
        val renderComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionRenderComponents.kt")
        )
        val movedDefinitions = listOf(
            "internal fun RunningSessionDialogs",
            "internal fun RunningSessionScaffold"
        )
        val renderOnlyCalls = listOf(
            Regex("""(?m)^\s*Scaffold\(""") to "Scaffold(",
            Regex("""RoutineWorkoutGraphCanvas\(""") to "RoutineWorkoutGraphCanvas(",
            Regex("""HeartRateGraph\(""") to "HeartRateGraph(",
            Regex("""RunningBlockPanel\(""") to "RunningBlockPanel(",
            Regex("""RunningStopSaveDialog\(""") to "RunningStopSaveDialog("
        )

        movedDefinitions.forEach { definition ->
            assertFalse("$definition belongs in RunningSessionRenderComponents.kt", sessionScreen.contains(definition))
            assertTrue("$definition missing from RunningSessionRenderComponents.kt", renderComponents.contains(definition))
        }
        renderOnlyCalls.forEach { (pattern, call) ->
            assertFalse("$call belongs in RunningSessionRenderComponents.kt", pattern.containsMatchIn(sessionScreen))
            assertTrue("$call missing from RunningSessionRenderComponents.kt", pattern.containsMatchIn(renderComponents))
        }
        assertTrue(sessionScreen.contains("RunningSessionDialogs("))
        assertTrue(sessionScreen.contains("RunningSessionScaffold("))
    }

    @Test
    fun runningBlockComponentsStayOutOfGeneralSessionComponents() {
        val sessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionScreen.kt")
        )
        val chrome = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionChrome.kt")
        )
        val choiceDialogs = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionChoiceDialogs.kt")
        )
        val statusPanels = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionStatusPanels.kt")
        )
        val blockComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionBlockComponents.kt")
        )
        val generalFocusedComponents = listOf(chrome, choiceDialogs, statusPanels).joinToString("\n")
        val movedDefinitions = listOf(
            "internal fun RunningBlockPanel",
            "internal fun RunningTargetStepper",
            "private fun RunningTargetStepButton",
            "internal fun RunningTimerText"
        )

        movedDefinitions.forEach { definition ->
            assertFalse("$definition belongs in RunningSessionBlockComponents.kt", sessionScreen.contains(definition))
            assertFalse("$definition belongs in RunningSessionBlockComponents.kt", generalFocusedComponents.contains(definition))
            assertTrue("$definition missing from RunningSessionBlockComponents.kt", blockComponents.contains(definition))
        }
        assertTrue(blockComponents.contains("Handler(Looper.getMainLooper())"))
    }

    @Test
    fun runningHeartRateComponentsStayOutOfGeneralSessionComponents() {
        val sessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionScreen.kt")
        )
        val chrome = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionChrome.kt")
        )
        val choiceDialogs = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionChoiceDialogs.kt")
        )
        val statusPanels = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionStatusPanels.kt")
        )
        val heartRateComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningHeartRateComponents.kt")
        )
        val generalFocusedComponents = listOf(chrome, choiceDialogs, statusPanels).joinToString("\n")

        assertFalse("HeartRateGraph belongs in RunningHeartRateComponents.kt", sessionScreen.contains("internal fun HeartRateGraph"))
        assertFalse("HeartRateGraph belongs in RunningHeartRateComponents.kt", generalFocusedComponents.contains("internal fun HeartRateGraph"))
        assertTrue(heartRateComponents.contains("internal fun HeartRateGraph"))
        assertTrue(heartRateComponents.contains("HEART_RATE_GRAPH_WINDOW_MILLIS"))
    }

    @Test
    fun runningSessionRuntimeEffectsStayOutOfSessionScreen() {
        val sessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionScreen.kt")
        )
        val oldEffectsBucket = mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionEffects.kt")
        val lifecycleEffects = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionLifecycleEffects.kt")
        )
        val progressEffects = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionProgressEffects.kt")
        )
        val overlayEffects = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionOverlayEffects.kt")
        )
        val combinedEffects = listOf(lifecycleEffects, progressEffects, overlayEffects).joinToString("\n")
        val movedDefinitionsByOwner = mapOf(
            lifecycleEffects to listOf(
                "internal typealias RunningSessionEventLogger",
                "internal fun RunningSessionStartupEffect",
                "internal fun RunningSessionBackHandler"
            ),
            progressEffects to listOf(
                "internal fun RunningTargetOverridesSizeEffect",
                "internal fun RunningWorkoutHeartRateSamplesEffect",
                "internal fun RunningSessionAutoUploadEffect",
                "internal fun RunningWarmupTickerEffect",
                "internal fun RunningBlockProgressEffect",
                "internal fun RunningUrgentBlinkEffect",
                "internal fun RunningLastBlockAutoSaveEffect"
            ),
            overlayEffects to listOf(
                "internal fun RunningWorkoutStatusEffect",
                "internal fun RunningOverlayLifecycleEffect",
                "internal fun RunningOverlayActionEffect",
                "internal fun stopRunningSessionRuntime"
            )
        )
        val movedRuntimeCalls = listOf(
            Regex("""(?m)^\s*LaunchedEffect\(""") to "LaunchedEffect(",
            Regex("""(?m)^\s*DisposableEffect\(""") to "DisposableEffect(",
            Regex("""(?m)^\s*BackHandler\(""") to "BackHandler(",
            Regex("""RunningOverlayRequests\.actionRequest""") to "RunningOverlayRequests.actionRequest",
            Regex("""requestOverlayPermissionIfNeeded\(""") to "requestOverlayPermissionIfNeeded(",
            Regex("""startRunningOverlay\(""") to "startRunningOverlay(",
            Regex("""startWorkoutStatusService\(""") to "startWorkoutStatusService(",
            Regex("""stopRunningOverlay\(""") to "stopRunningOverlay(",
            Regex("""stopWorkoutStatusService\(""") to "stopWorkoutStatusService(",
            Regex("""runningAutoLocalSaveDelayMillis\(""") to "runningAutoLocalSaveDelayMillis("
        )

        assertFalse(
            "Running session effects should stay split into lifecycle, progress, and overlay effect files.",
            Files.exists(oldEffectsBucket)
        )
        movedDefinitionsByOwner.forEach { (owner, definitions) ->
            definitions.forEach { definition ->
                assertFalse("$definition belongs in a focused running session effect file", sessionScreen.contains(definition))
                assertTrue("$definition missing from its focused running session effect file", owner.contains(definition))
            }
        }
        movedRuntimeCalls.forEach { (pattern, call) ->
            assertFalse("$call belongs in a focused running session effect file", pattern.containsMatchIn(sessionScreen))
            assertTrue("$call missing from focused running session effect files", pattern.containsMatchIn(combinedEffects))
        }
        assertFalse(
            "RunningSessionScreen.kt should not import overlay APIs directly.",
            sessionScreen.contains("import com.lighthousepark.intervalsgym.overlay.")
        )
        assertTrue(sessionScreen.contains("RunningOverlayLifecycleEffect("))
        assertTrue(sessionScreen.contains("RunningWorkoutStatusEffect("))
    }

    @Test
    fun runningSessionDiagnosticsStayOutOfSessionScreen() {
        val sessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionScreen.kt")
        )
        val diagnostics = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionDiagnostics.kt")
        )
        val movedDefinitions = listOf(
            "internal data class RunningSessionDiagnosticSnapshot",
            "internal fun logRunningSessionDiagnosticEvent",
            "internal fun runningRecordedBlockDiagnosticDetails",
            "internal fun runningFinishedLocalSessionDiagnosticDetails",
            "internal fun runningCatchUpDiagnosticDetails",
            "internal fun runningBlockStartedDiagnosticDetails",
            "internal fun runningTargetOverrideDiagnosticDetails",
            "internal fun runningUploadStartedDiagnosticDetails"
        )
        val diagnosticOnlyCalls = listOf(
            "DiagnosticsLogger.log(",
            "runningBlockDiagnosticText(",
            "runningBlocksDiagnosticText("
        )

        movedDefinitions.forEach { definition ->
            assertFalse("$definition belongs in RunningSessionDiagnostics.kt", sessionScreen.contains(definition))
            assertTrue("$definition missing from RunningSessionDiagnostics.kt", diagnostics.contains(definition))
        }
        diagnosticOnlyCalls.forEach { call ->
            assertFalse("$call belongs in RunningSessionDiagnostics.kt", sessionScreen.contains(call))
            assertTrue("$call missing from RunningSessionDiagnostics.kt", diagnostics.contains(call))
        }
        assertTrue(sessionScreen.contains("RunningSessionDiagnosticSnapshot("))
        assertTrue(sessionScreen.contains("logRunningSessionDiagnosticEvent("))
    }

    @Test
    fun runningSessionRouteOwnerDoesNotUseProjectWildcardImports() {
        val routeOwner = mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningSessionScreen.kt")
        val text = Files.readString(routeOwner)

        assertFalse(
            "RunningSessionScreen.kt should keep app/data/overlay/running/training dependencies explicit.",
            Regex("""import com\.lighthousepark\.intervalsgym\..*\.\*""").containsMatchIn(text)
        )
    }
}

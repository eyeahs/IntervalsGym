package com.lighthousepark.intervalsgym

import java.nio.file.Files
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthSessionUiArchitectureGuardTest {
    private val mainSourceRoot = ArchitectureGuardProject.mainSourceRoot

    @Test
    fun strengthSessionRouteOwnerDoesNotUseProjectWildcardImports() {
        val routeOwner = mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionScreen.kt")
        val text = Files.readString(routeOwner)

        assertFalse(
            "StrengthSessionScreen.kt should keep data sync, overlay, strength-domain, training, and shared visual dependencies explicit.",
            Regex("""import com\.lighthousepark\.intervalsgym\..*\.\*""").containsMatchIn(text)
        )
    }

    @Test
    fun legacyManualStrengthSessionStaysOutOfRoutedSessionScreen() {
        val routedSessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionScreen.kt")
        )
        val manualSessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthManualSessionScreen.kt")
        )

        val legacyManualSignature = "internal fun StrengthSessionScreen(\n    apiKey: String,\n    onBack: () -> Unit"

        assertFalse(routedSessionScreen.contains(legacyManualSignature))
        assertFalse(manualSessionScreen.contains(legacyManualSignature))
        assertTrue(manualSessionScreen.contains("internal fun StrengthManualSessionScreen("))
    }

    @Test
    fun strengthSessionChromeStaysOutOfRoutedSessionScreen() {
        val routedSessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionScreen.kt")
        )
        val chrome = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionChrome.kt")
        )
        val restChrome = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionRestChrome.kt")
        )
        val finishChrome = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionFinishChrome.kt")
        )
        val dialogHost = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionDialogs.kt")
        )
        val barFunctions = listOf(
            "internal fun StrengthSessionTopBar",
            "internal fun StrengthSessionTopBarTitle",
            "internal fun StrengthSetBottomBar",
            "internal fun StrengthSessionOngoingBottomBar"
        )
        val restFunctions = listOf(
            "internal fun RestTimerBottomSheet",
            "internal fun RestTimeControls",
            "internal fun RestTimerFloatingChip"
        )
        val finishFunctions = listOf(
            "internal fun StrengthUploadPanel",
            "internal fun StrengthFinishChoiceDialog",
            "internal fun StrengthCalendarRoutineDeleteConfirmDialog"
        )

        barFunctions.forEach { functionName ->
            assertFalse("$functionName belongs in StrengthSessionChrome.kt", routedSessionScreen.contains(functionName))
            assertTrue("$functionName missing from StrengthSessionChrome.kt", chrome.contains(functionName))
        }
        restFunctions.forEach { functionName ->
            assertFalse("$functionName belongs in StrengthSessionRestChrome.kt", routedSessionScreen.contains(functionName))
            assertFalse("$functionName belongs in StrengthSessionRestChrome.kt", chrome.contains(functionName))
            assertTrue("$functionName missing from StrengthSessionRestChrome.kt", restChrome.contains(functionName))
        }
        finishFunctions.forEach { functionName ->
            assertFalse("$functionName belongs in StrengthSessionFinishChrome.kt", routedSessionScreen.contains(functionName))
            assertFalse("$functionName belongs in StrengthSessionFinishChrome.kt", chrome.contains(functionName))
            assertTrue("$functionName missing from StrengthSessionFinishChrome.kt", finishChrome.contains(functionName))
        }
        listOf(
            "RestTimerBottomSheet(",
            "StrengthExerciseTypeDialog(",
            "StrengthExerciseConfigDialog(",
            "CustomStrengthExerciseDialog(",
            "StrengthFinishChoiceDialog(",
            "StrengthCalendarRoutineDeleteConfirmDialog("
        ).forEach { call ->
            assertFalse("$call belongs behind StrengthSessionDialogs", routedSessionScreen.contains(call))
            assertTrue("$call missing from StrengthSessionDialogs.kt", dialogHost.contains(call))
        }
        assertTrue(routedSessionScreen.contains("StrengthSessionDialogs("))
    }

    @Test
    fun strengthSessionRenderScaffoldStaysOutOfRoutedSessionScreen() {
        val routedSessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionScreen.kt")
        )
        val renderComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionRenderComponents.kt")
        )
        val movedDefinitions = listOf(
            "internal fun StrengthSessionScaffold",
            "internal fun StrengthSessionContentHost"
        )
        val renderOnlyCalls = listOf(
            Regex("""(?m)^\s*Scaffold\(""") to "Scaffold(",
            Regex("""StrengthSessionTopBar\(""") to "StrengthSessionTopBar(",
            Regex("""RestTimerFloatingChip\(""") to "RestTimerFloatingChip(",
            Regex("""StrengthSetBottomBar\(""") to "StrengthSetBottomBar(",
            Regex("""StrengthSessionOngoingBottomBar\(""") to "StrengthSessionOngoingBottomBar(",
            Regex("""StrengthSessionReadyScreen\(""") to "StrengthSessionReadyScreen(",
            Regex("""StrengthExerciseListScreen\(""") to "StrengthExerciseListScreen(",
            Regex("""StrengthSetExecutionScreen\(""") to "StrengthSetExecutionScreen(",
            Regex("""StrengthSessionOngoingRoutineScreen\(""") to "StrengthSessionOngoingRoutineScreen(",
            Regex("""(?m)^\s*EmptyView\(""") to "EmptyView(",
            Regex("""Modifier\.padding\(""") to "Modifier.padding("
        )

        movedDefinitions.forEach { definition ->
            assertFalse("$definition belongs in StrengthSessionRenderComponents.kt", routedSessionScreen.contains(definition))
            assertTrue("$definition missing from StrengthSessionRenderComponents.kt", renderComponents.contains(definition))
        }
        renderOnlyCalls.forEach { (pattern, call) ->
            assertFalse("$call belongs in StrengthSessionRenderComponents.kt", pattern.containsMatchIn(routedSessionScreen))
            assertTrue("$call missing from StrengthSessionRenderComponents.kt", pattern.containsMatchIn(renderComponents))
        }
        assertTrue(routedSessionScreen.contains("StrengthSessionScaffold("))
        assertTrue(routedSessionScreen.contains("StrengthSessionContentHost("))
    }

    @Test
    fun strengthSessionReadyComponentsStayOutOfRoutedSessionScreen() {
        val routedSessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionScreen.kt")
        )
        val readyComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionReadyComponents.kt")
        )
        val routineComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionRoutineComponents.kt")
        )
        val movedDefinitions = listOf(
            "internal fun StrengthSessionReadyScreen",
            "internal fun StrengthReadySetRow"
        )

        movedDefinitions.forEach { definition ->
            assertFalse("$definition belongs in StrengthSessionReadyComponents.kt", routedSessionScreen.contains(definition))
            assertFalse("$definition belongs in StrengthSessionReadyComponents.kt", routineComponents.contains(definition))
            assertTrue("$definition missing from StrengthSessionReadyComponents.kt", readyComponents.contains(definition))
        }
    }

    @Test
    fun strengthSessionOngoingRoutineComponentsStayOutOfRoutedSessionScreen() {
        val routedSessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionScreen.kt")
        )
        val readyComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionReadyComponents.kt")
        )
        val routineComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionRoutineComponents.kt")
        )
        val rowComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionOngoingRoutineRows.kt")
        )

        assertFalse(
            "StrengthSessionOngoingRoutineScreen belongs in StrengthSessionRoutineComponents.kt",
            routedSessionScreen.contains("internal fun StrengthSessionOngoingRoutineScreen")
        )
        assertFalse(
            "StrengthSessionOngoingRoutineScreen belongs in StrengthSessionRoutineComponents.kt",
            readyComponents.contains("internal fun StrengthSessionOngoingRoutineScreen")
        )
        assertTrue(routineComponents.contains("internal fun StrengthSessionOngoingRoutineScreen"))

        assertFalse(
            "StrengthOngoingExerciseRow belongs in StrengthSessionOngoingRoutineRows.kt",
            routedSessionScreen.contains("internal fun StrengthOngoingExerciseRow")
        )
        assertFalse(
            "StrengthOngoingExerciseRow belongs in StrengthSessionOngoingRoutineRows.kt",
            readyComponents.contains("internal fun StrengthOngoingExerciseRow")
        )
        assertFalse(
            "StrengthOngoingExerciseRow belongs in StrengthSessionOngoingRoutineRows.kt",
            routineComponents.contains("internal fun StrengthOngoingExerciseRow")
        )
        assertTrue(rowComponents.contains("internal fun StrengthOngoingExerciseRow"))
    }

    @Test
    fun strengthSessionEffectsStayOutOfRoutedSessionScreen() {
        val routedSessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionScreen.kt")
        )
        val oldEffectsBucket = mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionEffects.kt")
        val lifecycleEffects = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionLifecycleEffects.kt")
        )
        val persistenceEffects = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionPersistenceEffects.kt")
        )
        val overlayEffects = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionOverlayEffects.kt")
        )
        val combinedEffects = listOf(lifecycleEffects, persistenceEffects, overlayEffects).joinToString("\n")
        val movedDefinitionsByOwner = mapOf(
            lifecycleEffects to listOf(
                "internal fun StrengthStartImmediatelyEffect",
                "internal fun StrengthReadyRoutineEntriesEffect",
                "internal fun StrengthExerciseChangeFocusEffect",
                "internal fun StrengthSessionBackHandler",
                "internal fun StrengthSessionElapsedTickerEffect"
            ),
            persistenceEffects to listOf(
                "internal fun StrengthLiveResultPersistenceEffect",
                "internal fun StrengthActiveSessionPersistenceEffect"
            ),
            overlayEffects to listOf(
                "internal fun StrengthWorkoutStatusServiceEffect",
                "internal fun StrengthRestCountdownEffect",
                "internal enum class StrengthFloatingOverlayMode",
                "internal fun rememberStrengthSessionAppVisibility",
                "internal data class StrengthSessionAppVisibility",
                "internal fun StrengthFloatingOverlayEffect",
                "internal fun strengthFloatingOverlayMode",
                "internal fun StrengthShowRestSheetOverlayRequestEffect",
                "internal fun StrengthSetCompleteOverlayRequestEffect",
                "internal fun strengthSetCompleteOverlayTitle",
                "internal fun requestStrengthSessionOverlayPermission",
                "internal fun startStrengthRestOverlay",
                "internal fun stopStrengthRestOverlay",
                "internal fun stopStrengthSessionRuntime"
            )
        )
        val movedOverlayCalls = listOf(
            "RestOverlayRequests",
            "WorkoutStatusForegroundService.TYPE_STRENGTH",
            "requestOverlayPermissionIfNeeded(",
            "notifyRestFinished(",
            "startRestOverlay(",
            "stopRestOverlay(",
            "stopWorkoutStatusService(",
            "startStrengthSetCompleteOverlay(",
            "startWorkoutStatusService("
        )
        val movedRuntimeCalls = listOf(
            Regex("""(?m)^\s*LaunchedEffect\(""") to "LaunchedEffect(",
            Regex("""(?m)^\s*DisposableEffect\(""") to "DisposableEffect(",
            Regex("""(?m)^\s*BackHandler\(""") to "BackHandler(",
            Regex("""delay\(""") to "delay("
        )

        assertFalse(
            "Strength session effects should stay split into lifecycle, persistence, and overlay effect files.",
            Files.exists(oldEffectsBucket)
        )
        movedDefinitionsByOwner.forEach { (owner, definitions) ->
            definitions.forEach { definition ->
                assertFalse("$definition belongs in a focused strength session effect file", routedSessionScreen.contains(definition))
                assertTrue("$definition missing from its focused strength session effect file", owner.contains(definition))
            }
        }
        movedOverlayCalls.forEach { call ->
            assertFalse("$call belongs in a focused strength session effect file", routedSessionScreen.contains(call))
            assertTrue("$call missing from focused strength session effect files", combinedEffects.contains(call))
        }
        movedRuntimeCalls.forEach { (pattern, call) ->
            assertFalse("$call belongs in a focused strength session effect file", pattern.containsMatchIn(routedSessionScreen))
            assertTrue("$call missing from focused strength session effect files", pattern.containsMatchIn(combinedEffects))
        }
        assertFalse(
            "StrengthSessionScreen.kt should not import overlay APIs directly.",
            routedSessionScreen.contains("import com.lighthousepark.intervalsgym.overlay.")
        )
    }

    @Test
    fun strengthSetExecutionComponentsStayOutOfSessionScreen() {
        val sessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionScreen.kt")
        )
        val setExecutionComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionSetExecutionComponents.kt")
        )
        val movedDefinitions = listOf(
            "internal fun StrengthExerciseSetDialog",
            "internal fun StrengthSetExecutionScreen",
            "private fun StrengthExerciseRecentHistorySection",
            "private fun StrengthExerciseHistoryItem"
        )

        movedDefinitions.forEach { definition ->
            assertFalse("$definition belongs in StrengthSessionSetExecutionComponents.kt", sessionScreen.contains(definition))
            assertTrue("$definition missing from StrengthSessionSetExecutionComponents.kt", setExecutionComponents.contains(definition))
        }
    }

    @Test
    fun restOverlayStopCannotTerminateANewerStartRequest() {
        val overlayService = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/overlay/RestTimerOverlayService.kt")
        )

        assertTrue(overlayService.contains("ACTION_STOP -> stopSelf(startId)"))
        assertFalse(overlayService.contains("ACTION_STOP -> stopSelf()"))
    }
}

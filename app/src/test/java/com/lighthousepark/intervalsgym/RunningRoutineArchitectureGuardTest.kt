package com.lighthousepark.intervalsgym

import java.nio.file.Files
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningRoutineArchitectureGuardTest {
    private val mainSourceRoot = ArchitectureGuardProject.mainSourceRoot

    @Test
    fun runningRoutineComponentsChromeAndEffectsStayOutOfRouteOwners() {
        val routeOwners = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningRoutineScreens.kt")
        )
        val components = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningRoutineComponents.kt")
        )
        val chrome = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningRoutineChrome.kt")
        )
        val effects = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/RunningRoutineEffects.kt")
        )
        val componentDefinitions = listOf(
            "internal fun RunningRoutineListContent",
            "private fun RunningRoutineRow",
            "internal fun RunningRoutineDetailContent",
            "private fun SavedRunningWorkoutRoutine.savedAtLabel"
        )
        val chromeDefinitions = listOf(
            "internal fun RunningRoutineListTopBar",
            "internal fun RunningRoutineManagementTopBar",
            "internal fun RunningRoutineDeleteDialog"
        )
        val effectDefinitions = listOf(
            "internal fun RefreshRunningRoutinesOnResume"
        )
        val componentOnlyCalls = listOf(
            Regex("""(?m)^\s*LazyColumn\(""") to "LazyColumn(",
            Regex("""RoutineWorkoutGraphCanvas\(""") to "RoutineWorkoutGraphCanvas(",
            Regex("""TestContentDescriptions\.runningSavedRoutine""") to "TestContentDescriptions.runningSavedRoutine",
            Regex("""formatShortMonthDayTime\(""") to "formatShortMonthDayTime("
        )
        val chromeOnlyCalls = listOf(
            Regex("""(?m)^\s*TopAppBar\(""") to "TopAppBar(",
            Regex("""(?m)^\s*AlertDialog\(""") to "AlertDialog(",
            Regex("""Icons\.AutoMirrored\.Outlined\.ArrowBack""") to "Icons.AutoMirrored.Outlined.ArrowBack",
            Regex("""TestContentDescriptions\.RunningRoutineConfirmDelete""") to "TestContentDescriptions.RunningRoutineConfirmDelete"
        )
        val effectOnlyCalls = listOf(
            Regex("""(?m)^\s*DisposableEffect\(""") to "DisposableEffect(",
            Regex("""LifecycleEventObserver""") to "LifecycleEventObserver"
        )

        componentDefinitions.forEach { definition ->
            assertFalse("$definition belongs in RunningRoutineComponents.kt", routeOwners.contains(definition))
            assertTrue("$definition missing from RunningRoutineComponents.kt", components.contains(definition))
        }
        chromeDefinitions.forEach { definition ->
            assertFalse("$definition belongs in RunningRoutineChrome.kt", routeOwners.contains(definition))
            assertTrue("$definition missing from RunningRoutineChrome.kt", chrome.contains(definition))
        }
        effectDefinitions.forEach { definition ->
            assertFalse("$definition belongs in RunningRoutineEffects.kt", routeOwners.contains(definition))
            assertTrue("$definition missing from RunningRoutineEffects.kt", effects.contains(definition))
        }
        componentOnlyCalls.forEach { (pattern, call) ->
            assertFalse("$call belongs in RunningRoutineComponents.kt", pattern.containsMatchIn(routeOwners))
            assertTrue("$call missing from RunningRoutineComponents.kt", pattern.containsMatchIn(components))
        }
        chromeOnlyCalls.forEach { (pattern, call) ->
            assertFalse("$call belongs in RunningRoutineChrome.kt", pattern.containsMatchIn(routeOwners))
            assertTrue("$call missing from RunningRoutineChrome.kt", pattern.containsMatchIn(chrome))
        }
        effectOnlyCalls.forEach { (pattern, call) ->
            assertFalse("$call belongs in RunningRoutineEffects.kt", pattern.containsMatchIn(routeOwners))
            assertTrue("$call missing from RunningRoutineEffects.kt", pattern.containsMatchIn(effects))
        }
        assertTrue(routeOwners.contains("RunningRoutineListTopBar("))
        assertTrue(routeOwners.contains("RunningRoutineManagementTopBar("))
        assertTrue(routeOwners.contains("RunningRoutineDeleteDialog("))
        assertTrue(routeOwners.contains("RunningRoutineListContent("))
        assertTrue(routeOwners.contains("RunningRoutineDetailContent("))
        assertTrue(routeOwners.contains("RefreshRunningRoutinesOnResume"))
    }
}

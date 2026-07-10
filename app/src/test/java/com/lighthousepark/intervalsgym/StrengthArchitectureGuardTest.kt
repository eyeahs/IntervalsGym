package com.lighthousepark.intervalsgym

import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthArchitectureGuardTest {
    private val mainSourceRoot = ArchitectureGuardProject.mainSourceRoot
    private val testSourceRoot = ArchitectureGuardProject.testSourceRoot

    @Test
    fun strengthArchitectureGuardsStaySplitByConcern() {
        val guardRoot = testSourceRoot.resolve("com/lighthousepark/intervalsgym")
        val coreGuard = Files.readString(guardRoot.resolve("StrengthArchitectureGuardTest.kt"))
        val focusedGuards = listOf(
            "StrengthSessionUiArchitectureGuardTest.kt",
            "StrengthSessionStateArchitectureGuardTest.kt",
            "StrengthRoutineEditArchitectureGuardTest.kt",
            "StrengthDomainArchitectureGuardTest.kt"
        )

        focusedGuards.forEach { fileName ->
            assertTrue("$fileName should own its focused strength architecture rules.", Files.exists(guardRoot.resolve(fileName)))
        }
        listOf(
            "strengthSessionChromeStaysOutOfRoutedSessionScreen",
            "routedStrengthSessionScreenDoesNotBuildCompletedResultsDirectly",
            "strengthRoutineEditActionsOwnEntryEditRules",
            "strengthSessionProgressionRulesStayOutOfGenericStrengthDomain"
        ).forEach { movedRuleName ->
            assertFalse(
                "$movedRuleName belongs in a focused strength guard file.",
                Regex("""fun\s+$movedRuleName\(""").containsMatchIn(coreGuard)
            )
        }
    }

    @Test
    fun strengthSetEditorAndSwipeComponentsStayShared() {
        val sessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionScreen.kt")
        )
        val editScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthRoutineEditScreen.kt")
        )
        val setEditorComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSetEditorComponents.kt")
        )
        val setRecordComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSetRecordComponents.kt")
        )
        val setMetricFields = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSetMetricFields.kt")
        )
        val completedSetResetSwipe = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthCompletedSetResetSwipeContainer.kt")
        )
        val swipeComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSwipeContainers.kt")
        )
        val setRecordDefinitionsByOwner = mapOf(
            setRecordComponents to listOf(
                "internal fun StrengthSetRecordRow",
                "private fun StrengthActualSetRecordCell"
            ),
            setMetricFields to listOf(
                "internal fun UnilateralSetSideRow",
                "internal fun SetMetricField"
            ),
            completedSetResetSwipe to listOf("internal fun CompletedSetResetSwipeContainer")
        )
        val editorDefinitions = listOf(
            "internal fun ExerciseSearchRow",
            "internal fun ChoiceGrid",
            "internal fun NumberField",
            "internal fun StrengthRoutineEntryCard"
        )

        setRecordDefinitionsByOwner.forEach { (owner, definitions) ->
            definitions.forEach { definition ->
                assertFalse("$definition belongs in a focused set-record component file", sessionScreen.contains(definition))
                assertFalse("$definition belongs in a focused set-record component file", editScreen.contains(definition))
                assertFalse("$definition belongs in a focused set-record component file", setEditorComponents.contains(definition))
                assertTrue("$definition missing from its focused set-record component file", owner.contains(definition))
            }
        }
        editorDefinitions.forEach { definition ->
            assertFalse("$definition belongs in StrengthSetEditorComponents.kt", sessionScreen.contains(definition))
            assertFalse("$definition belongs in StrengthSetEditorComponents.kt", editScreen.contains(definition))
            assertFalse("$definition belongs in StrengthSetEditorComponents.kt", setRecordComponents.contains(definition))
            assertTrue("$definition missing from StrengthSetEditorComponents.kt", setEditorComponents.contains(definition))
        }
        assertFalse("Metric field rendering belongs in StrengthSetMetricFields.kt", setRecordComponents.contains("internal fun SetMetricField"))
        assertFalse(
            "Completed-set reset swipe belongs in StrengthCompletedSetResetSwipeContainer.kt",
            setRecordComponents.contains("internal fun CompletedSetResetSwipeContainer")
        )
        assertFalse(sessionScreen.contains("internal fun PendingSwipeDeleteContainer"))
        assertFalse(editScreen.contains("internal fun PendingSwipeDeleteContainer"))
        assertTrue(swipeComponents.contains("internal fun PendingSwipeDeleteContainer"))
    }

    @Test
    fun strengthSupersetSelectionUiStaysSharedAcrossRoutineSurfaces() {
        val uiRoot = mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui")
        val sharedComponents = Files.readString(uiRoot.resolve("StrengthSupersetSelectionComponents.kt"))
        val routineEditComponents = Files.readString(uiRoot.resolve("StrengthRoutineEditComponents.kt"))
        val routineEditList = Files.readString(uiRoot.resolve("StrengthRoutineEditListComponents.kt"))
        val sessionChrome = Files.readString(uiRoot.resolve("StrengthSessionChrome.kt"))
        val sessionRender = Files.readString(uiRoot.resolve("StrengthSessionRenderComponents.kt"))
        val sessionRoutine = Files.readString(uiRoot.resolve("StrengthSessionRoutineComponents.kt"))
        val ongoingRows = Files.readString(uiRoot.resolve("StrengthSessionOngoingRoutineRows.kt"))
        val surfaceFiles = listOf(
            routineEditComponents,
            routineEditList,
            sessionChrome,
            sessionRender,
            sessionRoutine,
            ongoingRows
        )
        val sharedDefinitions = listOf(
            "internal class StrengthSupersetSelectionUiState",
            "internal fun rememberStrengthSupersetSelectionUiState",
            "internal fun StrengthSupersetSelectionMarker",
            "internal fun StrengthSupersetSelectionBottomBar",
            "internal fun strengthSupersetSelectionContainerColor"
        )

        sharedDefinitions.forEach { definition ->
            assertTrue("$definition missing from the shared superset UI owner", sharedComponents.contains(definition))
            surfaceFiles.forEach { surface ->
                assertFalse("$definition must not be redefined by a routine surface", surface.contains(definition))
            }
        }
        listOf(
            "Text(\"선택 묶기\"",
            "TestContentDescriptions.StrengthConfirmSuperset",
            "TestContentDescriptions.strengthSupersetEntryLabel"
        ).forEach { contractToken ->
            assertTrue("$contractToken missing from the shared superset UI owner", sharedComponents.contains(contractToken))
            surfaceFiles.forEach { surface ->
                assertFalse("$contractToken must not be duplicated by a routine surface", surface.contains(contractToken))
            }
        }
        assertTrue(routineEditList.contains("StrengthSupersetSelectionBottomBar("))
        assertTrue(sessionRender.contains("StrengthSupersetSelectionBottomBar("))
        assertTrue(routineEditComponents.contains("StrengthSupersetSelectionMarker("))
        assertTrue(ongoingRows.contains("StrengthSupersetSelectionMarker("))
        assertTrue(routineEditComponents.contains("strengthSupersetSelectionContainerColor("))
        assertTrue(ongoingRows.contains("strengthSupersetSelectionContainerColor("))
    }

    @Test
    fun simpleStrengthRouteScreensDoNotUseProjectWildcardImports() {
        val screenFiles = listOf(
            "StrengthRoutineListScreen.kt",
            "StrengthRoutineHistoryScreen.kt",
            "StrengthRoutineManagementScreen.kt"
        ).map { fileName -> mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/$fileName") }
        val violations = screenFiles
            .filter { path ->
                Regex("""import com\.lighthousepark\.intervalsgym\..*\.\*""")
                    .containsMatchIn(Files.readString(path))
            }
            .map { it.relativeToProject() }

        assertEquals(emptyList<String>(), violations)
    }

    @Test
    fun appRootUsesStrengthAppStateStorageUseCaseForStrengthState() {
        val appRoot = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/app/AppRoot.kt")
        )
        val forbiddenCalls = listOf(
            "loadStrengthRoutines(",
            "loadActiveStrengthSession(",
            "loadScheduledStrengthRoutines(",
            "nextStrengthWorkoutRoutineId(",
            "saveStrengthRoutineLibrary(",
            "saveActiveStrengthSession(",
            "STRENGTH_ROUTINES_PREF",
            "ACTIVE_STRENGTH_SESSION_PREF"
        )

        forbiddenCalls.forEach { call ->
            assertFalse("$call belongs behind StrengthAppStateStorageUseCase", appRoot.contains(call))
        }
        assertTrue(appRoot.contains("StrengthAppStateStorageUseCase("))
        assertTrue(appRoot.contains("strengthAppStateStorage.loadSnapshot("))
        assertTrue(appRoot.contains("strengthAppStateStorage.saveStrengthRoutines("))
        assertTrue(appRoot.contains("strengthAppStateStorage.saveActiveSession("))
        assertTrue(appRoot.contains("strengthAppStateStorage.nextStrengthRoutineId("))
    }

    @Test
    fun strengthDomainFilesDoNotUseProjectWildcardImports() {
        val strengthRoots = listOf(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength"),
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/strength")
        )
        val violations = strengthRoots
            .flatMap { root ->
                kotlinFiles(root)
                    .filter { path -> path.parent == root }
            }
            .filter { path ->
                Regex("""import com\.lighthousepark\.intervalsgym\..*\.\*""")
                    .containsMatchIn(Files.readString(path))
            }
            .map { it.relativeToProject() }

        assertEquals(emptyList<String>(), violations)
    }
}

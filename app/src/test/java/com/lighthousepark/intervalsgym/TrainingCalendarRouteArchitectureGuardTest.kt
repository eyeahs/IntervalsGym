package com.lighthousepark.intervalsgym

import java.nio.file.Files
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingCalendarRouteArchitectureGuardTest {
    private val mainSourceRoot = ArchitectureGuardProject.mainSourceRoot
    private val testSourceRoot = ArchitectureGuardProject.testSourceRoot

    @Test
    fun trainingCalendarScreenUsesDataUseCaseForWeekLoading() {
        val calendarScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarScreen.kt")
        )
        val pagerContent = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarPagerContent.kt")
        )
        val forbiddenCalls = listOf(
            "loadCompletedStrengthSessionHistory(",
            "loadCompletedRunningSessionHistory(",
            "loadScheduledStrengthRoutines(",
            "loadIntervalsWeekCache(",
            "saveIntervalsWeekCache(",
            "repository.loadWeek("
        )

        forbiddenCalls.forEach { call ->
            assertFalse("$call belongs behind TrainingCalendarDataUseCase", calendarScreen.contains(call))
            assertFalse("$call belongs behind TrainingCalendarDataUseCase", pagerContent.contains(call))
        }
        assertTrue(calendarScreen.contains("intervalsUseCaseFactory.trainingCalendarData("))
        assertTrue(calendarScreen.contains("calendarDataUseCase.initialLoad("))
        assertTrue(pagerContent.contains("calendarDataUseCase.loadCachedRemoteWeek("))
        assertTrue(calendarScreen.contains("fetchRemoteWeek("))
    }

    @Test
    fun trainingCalendarRouteOwnerDoesNotUseProjectWildcardImports() {
        val routeOwner = mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarScreen.kt")
        val text = Files.readString(routeOwner)

        assertFalse(
            "TrainingCalendarScreen.kt should keep app/core/data/strength/training/workout dependencies explicit.",
            Regex("""import com\.lighthousepark\.intervalsgym\..*\.\*""").containsMatchIn(text)
        )
    }

    @Test
    fun trainingCalendarPagerContentUsesPageRenderDataBuilder() {
        val calendarScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarScreen.kt")
        )
        val pagerContent = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarPagerContent.kt")
        )
        val trainingModelsTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/training/TrainingModelsTest.kt")
        )
        val pageRenderDataTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/training/TrainingCalendarPageRenderDataTest.kt")
        )
        val forbiddenCalls = listOf(
            "withLocalStrengthResults(",
            "withLocalRunningResults(",
            "withLocalStrengthRoutines(",
            "withPendingCalendarRoutineMoves(",
            "mergeTrainingRoutinesAndResults(",
            "hasCalendarIdentityIn("
        )

        forbiddenCalls.forEach { call ->
            assertFalse("$call belongs behind buildTrainingCalendarPageRenderData", calendarScreen.contains(call))
            assertFalse("$call belongs behind buildTrainingCalendarPageRenderData", pagerContent.contains(call))
        }
        assertFalse(calendarScreen.contains("buildTrainingCalendarPageRenderData("))
        assertTrue(pagerContent.contains("buildTrainingCalendarPageRenderData("))
        assertTrue(calendarScreen.contains("TrainingCalendarPagerContent("))
        assertFalse(
            "Page render data tests belong in TrainingCalendarPageRenderDataTest.kt",
            trainingModelsTest.contains("trainingCalendarPageRenderData_filtersDeletedRendersPendingMovesAndSortsItems")
        )
        assertTrue(
            pageRenderDataTest.contains("trainingCalendarPageRenderData_filtersDeletedRendersPendingMovesAndSortsItems")
        )
    }

    @Test
    fun trainingCalendarScreenUsesWeekUiStateRefreshHelpers() {
        val calendarScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarScreen.kt")
        )
        val pagerContent = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarPagerContent.kt")
        )
        val uiState = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarUiState.kt")
        )
        val helperDefinitions = listOf(
            "internal data class TrainingCalendarRemotePageUiState",
            "internal fun WeekUiState.isLoadedRange",
            "internal fun WeekUiState.remotePageUiState",
            "internal fun WeekUiState.withTrainingCalendarInitialLoad",
            "internal fun WeekUiState.withFetchedRemoteData",
            "internal fun WeekUiState.withRemoteFetchFailed"
        )
        val screenHelperCalls = listOf(
            "state.withTrainingCalendarInitialLoad(",
            "state.withFetchedRemoteData(",
            "state.withRemoteFetchFailed("
        )
        val pagerHelperCalls = listOf(
            "weekUiState.isLoadedRange(",
            "weekUiState.remotePageUiState("
        )
        val forbiddenStateAssembly = listOf(
            "activities = cachedRemoteData.activities",
            "routines = cachedRemoteData.routines",
            "activities = data.activities",
            "routines = data.routines",
            "cachedPageData == null && (!isLoadedPage || state.isLoading)",
            "cachedPageData == null && state.error != null",
            "error = error.message ?: \"데이터를 불러오지 못했습니다.\""
        )

        helperDefinitions.forEach { definition ->
            assertFalse("$definition belongs in TrainingCalendarUiState.kt", calendarScreen.contains(definition))
            assertTrue("$definition missing from TrainingCalendarUiState.kt", uiState.contains(definition))
        }
        screenHelperCalls.forEach { call ->
            assertTrue("$call missing from TrainingCalendarScreen.kt", calendarScreen.contains(call))
        }
        pagerHelperCalls.forEach { call ->
            assertTrue("$call missing from TrainingCalendarPagerContent.kt", pagerContent.contains(call))
        }
        forbiddenStateAssembly.forEach { snippet ->
            assertFalse("$snippet belongs behind WeekUiState helpers", calendarScreen.contains(snippet))
            assertFalse("$snippet belongs behind WeekUiState helpers", pagerContent.contains(snippet))
        }
    }

    @Test
    fun trainingCalendarScreenUsesCalendarRoutineMoveHelpers() {
        val calendarScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarScreen.kt")
        )
        val calendarRoutineMoves = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/CalendarRoutineMoves.kt")
        )
        val routineActions = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarRoutineActions.kt")
        )
        val actionDefinitions = listOf(
            "internal data class TrainingCalendarRoutineSavePlan",
            "internal fun planTrainingCalendarRoutineSave(",
            "internal sealed interface TrainingCalendarRoutineMoveDecision",
            "internal data class TrainingCalendarRoutineMovePlan",
            "internal fun planTrainingCalendarRoutineMove(",
            "internal sealed interface TrainingCalendarRoutineDeleteDecision",
            "internal data class TrainingCalendarRoutineDeletePlan",
            "internal fun planTrainingCalendarRoutineDelete("
        )
        val helperCalls = listOf(
            "hasPendingCalendarRoutineMoveFor(",
            "withPendingCalendarRoutineMove(",
            "withoutCalendarRoutineMove(",
            "calendarRoutineForMove(",
            "calendarIdentityKeys(",
            "withoutCalendarRoutineMoveIdentities("
        )
        val directSyncCalls = listOf(
            "calendarRoutineSync.saveStrengthRoutineLocally(",
            "calendarRoutineSync.uploadSavedStrengthRoutine(",
            "calendarRoutineSync.moveStrengthRoutineLocally(",
            "calendarRoutineSync.syncMovedRoutine(",
            "calendarRoutineSync.deleteRoutine("
        )

        actionDefinitions.forEach { definition ->
            assertFalse("$definition belongs in TrainingCalendarRoutineActions.kt", calendarScreen.contains(definition))
            assertTrue("$definition missing from TrainingCalendarRoutineActions.kt", routineActions.contains(definition))
        }
        helperCalls.forEach { call ->
            assertTrue("$call missing from CalendarRoutineMoves.kt", calendarRoutineMoves.contains(call))
            assertFalse("$call belongs behind TrainingCalendarRoutineActions.kt", calendarScreen.contains(call))
            assertTrue("$call missing from TrainingCalendarRoutineActions.kt", routineActions.contains(call))
        }
        listOf(
            "planTrainingCalendarRoutineSave(",
            "planTrainingCalendarRoutineMove(",
            "planTrainingCalendarRoutineDelete("
        ).forEach { call ->
            assertTrue("$call missing from TrainingCalendarScreen.kt", calendarScreen.contains(call))
            assertTrue("$call missing from TrainingCalendarRoutineActions.kt", routineActions.contains(call))
        }
        listOf(
            "savePlan.saveLocally(",
            "savePlan.upload(",
            "movePlan.moveLocally(",
            "movePlan.syncRemote(",
            "deletePlan.delete("
        ).forEach { call ->
            assertTrue("$call missing from TrainingCalendarScreen.kt", calendarScreen.contains(call))
        }
        directSyncCalls.forEach { call ->
            assertFalse("$call belongs behind TrainingCalendarRoutineActions.kt", calendarScreen.contains(call))
        }
        assertFalse(
            "TrainingCalendarScreen.kt should not hand-filter pending move identities.",
            calendarScreen.contains("move.identityKeys().none")
        )
    }

    @Test
    fun trainingCalendarDragUiStaysOutOfMainCalendarScreen() {
        val calendarScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarScreen.kt")
        )
        val pagerContent = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarPagerContent.kt")
        )
        val dragUi = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarDragUi.kt")
        )
        val movedDefinitions = listOf(
            "internal data class CalendarRoutineDragOverlayState",
            "internal enum class CalendarRoutineDragAction",
            "internal data class TrainingCalendarDragUiState",
            "internal fun CalendarRoutineDragOverlay",
            "internal fun CalendarRoutineDragActionButtons",
            "internal fun BoxScope.CalendarRoutineExternalDragOverlayHost"
        )
        val forbiddenStateDeclarations = listOf(
            "var isCalendarRoutineDragging",
            "var calendarDragDropTargetDate",
            "var calendarDragPointerRootPosition",
            "var calendarDragOverlayState",
            "var calendarDragActionBounds",
            "var calendarContentRootPosition",
            "var calendarContentRootSize"
        )

        movedDefinitions.forEach { definition ->
            assertFalse("$definition belongs in TrainingCalendarDragUi.kt", calendarScreen.contains(definition))
            assertTrue("$definition missing from TrainingCalendarDragUi.kt", dragUi.contains(definition))
        }
        forbiddenStateDeclarations.forEach { declaration ->
            assertFalse("$declaration belongs in TrainingCalendarDragUiState", calendarScreen.contains(declaration))
        }
        assertFalse(calendarScreen.contains("shadowElevation = 18f"))
        assertFalse(calendarScreen.contains("AnimatedVisibility("))
        assertFalse(calendarScreen.contains("CalendarRoutineDragActionButtons("))
        assertFalse(calendarScreen.contains("CalendarRoutineDragOverlay("))
        assertTrue(calendarScreen.contains("var calendarDragUiState"))
        assertFalse(calendarScreen.contains("CalendarRoutineExternalDragOverlayHost("))
        assertTrue(pagerContent.contains("CalendarRoutineExternalDragOverlayHost("))
    }

    @Test
    fun trainingCalendarPagerContentStaysOutOfMainCalendarScreen() {
        val calendarScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarScreen.kt")
        )
        val pagerContent = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarPagerContent.kt")
        )
        val movedDefinitions = listOf(
            "internal fun TrainingCalendarPagerContent"
        )
        val pagerRenderCalls = listOf(
            Regex("""(?m)^\s*Box\(""") to "Box(",
            Regex("""(?m)^\s*HorizontalPager\(""") to "HorizontalPager(",
            Regex("""(?m)^\s*Column\(""") to "Column(",
            Regex("""WeekSummary\(""") to "WeekSummary(",
            Regex("""LoadingView\(""") to "LoadingView(",
            Regex("""ErrorView\(""") to "ErrorView(",
            Regex("""MonthlyTrainingCalendar\(""") to "MonthlyTrainingCalendar(",
            Regex("""TrainingList\(""") to "TrainingList(",
            Regex("""CalendarRoutineExternalDragOverlayHost\(""") to "CalendarRoutineExternalDragOverlayHost(",
            Regex("""onGloballyPositioned""") to "onGloballyPositioned"
        )

        movedDefinitions.forEach { definition ->
            assertFalse("$definition belongs in TrainingCalendarPagerContent.kt", calendarScreen.contains(definition))
            assertTrue("$definition missing from TrainingCalendarPagerContent.kt", pagerContent.contains(definition))
        }
        pagerRenderCalls.forEach { (pattern, call) ->
            assertFalse("$call belongs in TrainingCalendarPagerContent.kt", pattern.containsMatchIn(calendarScreen))
            assertTrue("$call missing from TrainingCalendarPagerContent.kt", pattern.containsMatchIn(pagerContent))
        }
        assertTrue(calendarScreen.contains("TrainingCalendarPagerContent("))
    }

    @Test
    fun trainingRoutineSaveStateStaysOutOfMainCalendarScreen() {
        val calendarScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarScreen.kt")
        )
        val routineSaveState = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingRoutineSaveUiState.kt")
        )
        val forbiddenStateDeclarations = listOf(
            "var showRoutineSaveSheet",
            "var routineSaveMessage",
            "var routineSaveError",
            "var savingRoutineId",
            "var routineSaveDateText",
            "var routineSaveTimeText"
        )

        forbiddenStateDeclarations.forEach { declaration ->
            assertFalse("$declaration belongs in TrainingRoutineSaveUiState", calendarScreen.contains(declaration))
        }
        assertTrue(routineSaveState.contains("internal data class TrainingRoutineSaveUiState"))
        assertTrue(routineSaveState.contains("trainingRoutineSaveUiStateSaver"))
        assertTrue(calendarScreen.contains("var routineSaveUiState"))
        assertTrue(calendarScreen.contains("routineSaveUiState.withUploadStarted("))
        assertTrue(calendarScreen.contains("routineSaveUiState.withUploadFailed("))
    }

    @Test
    fun trainingCalendarChromeStaysOutOfMainCalendarScreen() {
        val calendarScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarScreen.kt")
        )
        val chrome = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarChrome.kt")
        )
        val movedDefinitions = listOf(
            "internal fun TrainingCalendarTopBar",
            "internal fun TrainingCalendarDatePickerDialog"
        )
        val movedChromeCalls = listOf(
            "TopAppBar(",
            "DropdownMenu(",
            "rememberDatePickerState(",
            "painterResource(id = R.drawable.ic_today_word)"
        )

        movedDefinitions.forEach { definition ->
            assertFalse("$definition belongs in TrainingCalendarChrome.kt", calendarScreen.contains(definition))
            assertTrue("$definition missing from TrainingCalendarChrome.kt", chrome.contains(definition))
        }
        movedChromeCalls.forEach { call ->
            assertFalse("$call belongs in TrainingCalendarChrome.kt", calendarScreen.contains(call))
            assertTrue("$call missing from TrainingCalendarChrome.kt", chrome.contains(call))
        }
        assertFalse(
            "DatePickerDialog should only be called directly from TrainingCalendarChrome.kt",
            Regex("""(?m)^\s*DatePickerDialog\(""").containsMatchIn(calendarScreen)
        )
        assertTrue(
            "TrainingCalendarChrome.kt should own the direct DatePickerDialog call",
            Regex("""(?m)^\s*DatePickerDialog\(""").containsMatchIn(chrome)
        )
        assertTrue(calendarScreen.contains("TrainingCalendarTopBar("))
        assertTrue(calendarScreen.contains("TrainingCalendarDatePickerDialog("))
    }
}

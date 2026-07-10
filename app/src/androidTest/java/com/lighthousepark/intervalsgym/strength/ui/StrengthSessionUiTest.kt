package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.overlay.RestOverlayRequests
import com.lighthousepark.intervalsgym.strength.ActiveStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthSetRecord
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import com.lighthousepark.intervalsgym.strength.strengthExerciseCatalog
import com.lighthousepark.intervalsgym.ui.theme.IntervalsGymTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StrengthSessionUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun readyScreen_startButtonInvokesStart() {
        val routine = defaultStrengthRoutines().first()
        var started = false

        composeRule.setThemedContent {
            StrengthSessionReadyScreen(
                routine = routine,
                entries = routine.entries,
                onStart = { started = true },
                onEditRoutine = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthStartWorkout)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(started)
        }
    }

    @Test
    fun readyScreen_editButtonInvokesEditRoutine() {
        val routine = defaultStrengthRoutines().first()
        var editClicked = false

        composeRule.setThemedContent {
            StrengthSessionReadyScreen(
                routine = routine,
                entries = routine.entries,
                onStart = {},
                onEditRoutine = { editClicked = true }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthEditWorkoutRoutine)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(editClicked)
        }
    }

    @Test
    fun readyScreen_entryRowTogglesSetDetails() {
        val routine = defaultStrengthRoutines().first()
        val entry = routine.entries.first()

        composeRule.setThemedContent {
            StrengthSessionReadyScreen(
                routine = routine,
                entries = routine.entries,
                onStart = {},
                onEditRoutine = {}
            )
        }

        composeRule.onNodeWithText("Set 1").assertDoesNotExist()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthReadyEntry(entry.id))
            .performClick()
        composeRule.onNodeWithText("Set 1").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthReadyEntry(entry.id))
            .performClick()
        composeRule.onNodeWithText("Set 1").assertDoesNotExist()
    }

    @Test
    fun strengthSessionTopBar_readyActionsInvokeCallbacks() {
        var backClicked = false
        var deleteClicked = false
        var historyClicked = false

        composeRule.setThemedContent {
            StrengthSessionTopBar(
                title = "상체",
                isWorkoutActive = false,
                elapsedSeconds = 0,
                showTimerBadgeAsNavigation = false,
                showReadyActions = true,
                showCalendarRoutineDelete = true,
                isDeletingCalendarRoutine = false,
                onBack = { backClicked = true },
                onCalendarRoutineDelete = { deleteClicked = true },
                onHistoryClick = { historyClicked = true }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthSessionBack)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthSessionCalendarRoutineDelete)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthSessionHistory)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(backClicked)
            assertTrue(deleteClicked)
            assertTrue(historyClicked)
        }
    }

    @Test
    fun strengthSessionTopBar_ongoingListShowsTimerInsteadOfBackAndHidesReadyActions() {
        composeRule.setThemedContent {
            StrengthSessionTopBar(
                title = "상체",
                isWorkoutActive = false,
                elapsedSeconds = 75,
                showTimerBadgeAsNavigation = true,
                showReadyActions = false,
                showCalendarRoutineDelete = true,
                isDeletingCalendarRoutine = false,
                onBack = {},
                onCalendarRoutineDelete = {},
                onHistoryClick = {}
            )
        }

        composeRule.onNodeWithText("01:15").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthSessionBack)
            .assertDoesNotExist()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthSessionCalendarRoutineDelete)
            .assertDoesNotExist()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthSessionHistory)
            .assertDoesNotExist()
    }

    @Test
    fun calendarRoutineDeleteConfirmDialog_invokesConfirmAndCancelCallbacks() {
        var confirmed = false
        var canceled = false

        composeRule.setThemedContent {
            StrengthCalendarRoutineDeleteConfirmDialog(
                message = "7월 1일의 routine을 삭제할까요?",
                isDeleting = false,
                onConfirm = { confirmed = true },
                onCancel = { canceled = true }
            )
        }

        composeRule.onNodeWithText("7월 1일의 routine을 삭제할까요?").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthSessionCalendarRoutineConfirmDelete)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthSessionCalendarRoutineCancelDelete)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(confirmed)
            assertTrue(canceled)
        }
    }

    @Test
    fun calendarRoutineDeleteConfirmDialog_disablesActionsWhileDeleting() {
        composeRule.setThemedContent {
            StrengthCalendarRoutineDeleteConfirmDialog(
                message = "삭제 중",
                isDeleting = true,
                onConfirm = {},
                onCancel = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthSessionCalendarRoutineConfirmDelete)
            .assertIsNotEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthSessionCalendarRoutineCancelDelete)
            .assertIsNotEnabled()
    }

    @Test
    fun finishChoiceDialog_invokesSaveDiscardAndApplyCallbacks() {
        var saved = false
        var discarded = false
        var applyToRoutine by mutableStateOf(false)

        composeRule.setThemedContent {
            StrengthFinishChoiceDialog(
                apiKey = "",
                entries = strengthTestEntries(),
                finishRpe = 7,
                applyWorkoutResultToRoutine = applyToRoutine,
                isUploading = false,
                onApplyWorkoutResultToRoutineChange = { applyToRoutine = it },
                onFinishRpeChange = {},
                onDismiss = {},
                onSave = { saved = true },
                onDiscard = { discarded = true }
            )
        }

        composeRule.onNodeWithText("운동 기록을 로컬에 저장하거나 삭제할 수 있습니다.").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishApplyToRoutine)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishSave)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishDiscard)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(applyToRoutine)
            assertTrue(saved)
            assertTrue(discarded)
        }
    }

    @Test
    fun finishChoiceDialog_disablesSaveAndDiscardWhileUploading() {
        composeRule.setThemedContent {
            StrengthFinishChoiceDialog(
                apiKey = "api-key",
                entries = strengthTestEntries(),
                finishRpe = 8,
                applyWorkoutResultToRoutine = true,
                isUploading = true,
                onApplyWorkoutResultToRoutineChange = {},
                onFinishRpeChange = {},
                onDismiss = {},
                onSave = {},
                onDiscard = {}
            )
        }

        composeRule.onNodeWithText("운동 기록을 저장하면 로컬 기록에 남기고 Intervals.icu 업로드를 시도합니다.").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishSave)
            .assertIsNotEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishDiscard)
            .assertIsNotEnabled()
    }

    @Test
    fun finishChoiceDialog_doesNotCountIncompletePlannedSetsInLoad() {
        composeRule.setThemedContent {
            StrengthFinishChoiceDialog(
                apiKey = "",
                entries = strengthTestEntries(),
                finishRpe = 7,
                applyWorkoutResultToRoutine = true,
                isUploading = false,
                onApplyWorkoutResultToRoutineChange = {},
                onFinishRpeChange = {},
                onDismiss = {},
                onSave = {},
                onDiscard = {}
            )
        }

        composeRule.onNodeWithText("Strength Load 1").assertExists()
    }

    @Test
    fun setCompleteOverlayRequestStartsHiddenRestOverlayState() {
        val state = StrengthSessionInteractionState(
            entries = listOf(defaultStrengthRoutines().first().entries.first()),
            setEvents = emptyList(),
            restEvents = emptyList(),
            restUiState = StrengthRestUiState.inactive(),
            navigationUiState = StrengthSessionNavigationUiState(
                isSetScreenVisible = true,
                currentExerciseIndex = 0,
                currentSetIndex = 0,
                pendingExerciseIndex = null,
                pendingSetIndex = null
            )
        )
        var transition by mutableStateOf<StrengthSessionStateTransition?>(null)

        composeRule.setThemedContent {
            StrengthSetCompleteOverlayRequestEffect(canCompleteSet = true) {
                transition = state.withCompletedCurrentSetFromOverlay(completedAtMillis = 10_000L)
            }
        }
        composeRule.runOnIdle {
            RestOverlayRequests.requestCompleteSet()
        }
        composeRule.waitUntil(timeoutMillis = 5_000L) { transition != null }

        composeRule.runOnIdle {
            val result = requireNotNull(transition)
            assertFalse(result.state.restUiState.isSheetVisible)
            assertEquals(StrengthRestOverlayCommand.START, result.restOverlayCommand)
        }
    }

    @Test
    fun restCountdownFinishesImmediatelyWhenWallClockDeadlinePassed() {
        var finished by mutableStateOf(false)

        composeRule.setThemedContent {
            StrengthRestCountdownEffect(
                context = LocalContext.current,
                remainingSeconds = 60,
                endAtMillis = System.currentTimeMillis() - 1L,
                onRemainingSecondsChange = {},
                onRestFinished = { finished = true }
            )
        }

        composeRule.waitUntil(timeoutMillis = 5_000L) { finished }
        composeRule.runOnIdle { assertTrue(finished) }
    }

    @Test
    fun ongoingRoutine_addExerciseButtonInvokesCallback() {
        val routine = defaultStrengthRoutines().first()
        var addExerciseClicked = false

        composeRule.setThemedContent {
            StrengthSessionOngoingRoutineScreen(
                routine = routine,
                entries = routine.entries,
                currentExerciseIndex = 0,
                uploadMessage = null,
                uploadError = null,
                onExerciseClick = {},
                onAddExercise = { addExerciseClicked = true },
                onEntriesChange = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthAddExercise)
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(addExerciseClicked)
        }
    }

    @Test
    fun ongoingRoutine_supersetSelectionGroupsRowsAndMovesSecondBelowTop() {
        val routine = defaultStrengthRoutines().first().copy(entries = strengthTestEntries())
        var changedEntries: List<StrengthRoutineEntry>? = null

        composeRule.setThemedContent {
            StrengthSessionOngoingRoutineScreen(
                routine = routine,
                entries = routine.entries,
                currentExerciseIndex = 0,
                uploadMessage = null,
                uploadError = null,
                onExerciseClick = {},
                onAddExercise = {},
                onEntriesChange = { changedEntries = it }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthGroupSuperset)
            .performScrollTo()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthOngoingEntry(1))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthOngoingEntry(3))
            .performScrollTo()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthConfirmSuperset)
            .performClick()

        composeRule.runOnIdle {
            val result = requireNotNull(changedEntries)
            assertEquals(listOf(1, 3, 2), result.map { it.id })
            assertNotNull(result[0].supersetGroupId)
            assertEquals(result[0].supersetGroupId, result[1].supersetGroupId)
        }
    }

    @Test
    fun setBottomBar_completeButtonInvokesCallback() {
        var completed = false

        composeRule.setThemedContent {
            StrengthSetBottomBar(
                allDone = false,
                currentLabel = "Set 1 · 스쿼트",
                isUploading = false,
                onCompleteSet = { completed = true }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthCompleteSet)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(completed)
        }
    }

    @Test
    fun setRecordRow_swipeLeftOnCompletedSetMarksIncomplete() {
        var record by mutableStateOf(
            StrengthSetRecord(
                id = 10,
                weightKg = "60",
                reps = "8",
                durationSeconds = "",
                restSeconds = "0",
                completed = true
            )
        )

        composeRule.setThemedContent {
            StrengthSetRecordRow(
                index = 0,
                record = record,
                onRecordChange = { record = it }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthSetRecordRow(10))
            .performTouchInput { swipeLeft() }

        composeRule.waitForIdle()
        composeRule.runOnIdle {
            assertFalse(record.completed)
        }
    }

    @Test
    fun strengthSessionScreen_addExerciseChangesNewEntryNotExistingEntry() {
        val baseRoutine = defaultStrengthRoutines().first()
        var latestSession: ActiveStrengthSession? = null

        composeRule.setThemedContent {
            StrengthSessionScreen(
                apiKey = "",
                routine = baseRoutine,
                calendarRoutineItem = null,
                isRoutineEditable = true,
                activeSession = null,
                startImmediately = true,
                onImmediateStartConsumed = {},
                onSessionChange = { latestSession = it },
                onSessionFinished = { _, _ -> },
                onHistoryClick = {},
                onEditRoutine = {},
                onCalendarRoutineDeleted = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthSessionBack)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthAddExercise)
            .performScrollTo()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseSearch)
            .performTextInput("레그컬")
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthExerciseSearchResult("leg_curl"))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseConfigDone)
            .performClick()

        composeRule.waitForIdle()
        composeRule.runOnIdle {
            val entries = requireNotNull(latestSession).entries
            assertEquals(baseRoutine.entries.size + 1, entries.size)
            assertEquals(baseRoutine.entries.first().exercise.id, entries.first().exercise.id)
            assertEquals("leg_curl", entries.last().exercise.id)
        }
    }

    @Test
    fun strengthSessionScreen_threeExerciseSupersetAdvancesToFirstExerciseNextSet() {
        val routine = defaultStrengthRoutines().first().copy(
            entries = strengthTestEntries().map { entry ->
                entry.copy(
                    supersetGroupId = 7,
                    restSeconds = 0,
                    records = entry.records.map { record -> record.copy(restSeconds = "0") }
                )
            }
        )
        var latestSession: ActiveStrengthSession? = null

        composeRule.setThemedContent {
            StrengthSessionScreen(
                apiKey = "",
                routine = routine,
                calendarRoutineItem = null,
                isRoutineEditable = true,
                activeSession = null,
                startImmediately = true,
                onImmediateStartConsumed = {},
                onSessionChange = { latestSession = it },
                onSessionFinished = { _, _ -> },
                onHistoryClick = {},
                onEditRoutine = {},
                onCalendarRoutineDeleted = {},
                onBack = {}
            )
        }

        repeat(3) {
            composeRule
                .onNodeWithContentDescription(TestContentDescriptions.StrengthCompleteSet)
                .performClick()
            composeRule.waitForIdle()
        }

        composeRule.onNodeWithText("Set 2 · ${routine.entries.first().title}").assertExists()
        composeRule.runOnIdle {
            val session = requireNotNull(latestSession)
            assertEquals(0, session.currentExerciseIndex)
            assertEquals(1, session.currentSetIndex)
        }
    }

    @Test
    fun restTimeControls_invokeAdjustAndSetCallbacks() {
        val adjustments = mutableListOf<Int>()
        val setValues = mutableListOf<Int>()

        composeRule.setThemedContent {
            RestTimeControls(
                onAdjustSeconds = { adjustments += it },
                onSetSeconds = { setValues += it }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthRestTimeControl("-10초"))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthRestTimeControl("+10초"))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthRestTimeControl("30초"))
            .performClick()

        composeRule.runOnIdle {
            assertEquals(listOf(-10, 10), adjustments)
            assertEquals(listOf(30), setValues)
        }
    }

    @Test
    fun restTimerBottomSheet_stopButtonInvokesCallback() {
        var stopped = false

        composeRule.setThemedContent {
            RestTimerBottomSheet(
                title = "스쿼트 휴식",
                remainingSeconds = 75,
                onAdjustSeconds = {},
                onSetSeconds = {},
                onDismiss = {},
                onStop = { stopped = true }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRestStop)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(stopped)
        }
    }

    @Test
    fun restTimerFloatingChip_displaysRemainingTimeAndInvokesClick() {
        var clicked = false

        composeRule.setThemedContent {
            RestTimerFloatingChip(
                title = "스쿼트 휴식",
                remainingSeconds = 75,
                onClick = { clicked = true }
            )
        }

        composeRule.onNodeWithText("스쿼트 휴식 01:15").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRestFloatingChip)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(clicked)
        }
    }

    @Test
    fun finishBar_invokesFinishWhenNotUploadingAndDisablesWhileUploading() {
        var finished = false
        var isUploading by mutableStateOf(false)

        composeRule.setThemedContent {
            StrengthSessionFinishBar(
                isUploading = isUploading,
                onFinish = { finished = true }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishWorkout)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(finished)
            isUploading = true
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishWorkout)
            .assertIsNotEnabled()
    }

    @Test
    fun uploadPanel_invokesUploadOnlyWhenEntriesAreAvailable() {
        var uploaded = false
        var entries by mutableStateOf(strengthTestEntries())

        composeRule.setThemedContent {
            StrengthUploadPanel(
                apiKey = "api-key",
                routineName = "테스트 웨이트",
                entries = entries,
                isUploading = false,
                uploadMessage = null,
                uploadError = null,
                onUpload = { uploaded = true }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthUploadWorkout)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(uploaded)
            entries = emptyList()
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthUploadWorkout)
            .assertIsNotEnabled()
    }

    @Test
    fun uploadPanel_displaysSyncMessagesAndDisablesWhileUploading() {
        composeRule.setThemedContent {
            StrengthUploadPanel(
                apiKey = "",
                routineName = "테스트 웨이트",
                entries = strengthTestEntries(),
                isUploading = true,
                uploadMessage = "업로드 완료",
                uploadError = "업로드 실패",
                onUpload = {}
            )
        }

        composeRule.onNodeWithText("Intervals.icu 업데이트는 로그인 후 사용할 수 있습니다.").assertExists()
        composeRule.onNodeWithText("업로드 완료").assertExists()
        composeRule.onNodeWithText("업로드 실패").assertExists()
        composeRule.onNodeWithText("업로드 중").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthUploadWorkout)
            .assertIsNotEnabled()
    }

    @Test
    fun setExecutionScreen_invokesExerciseChangeAndAddSetCallbacks() {
        val entry = strengthTestEntries().first()
        var exerciseClicked = false
        var addSetClicked = false

        composeRule.setThemedContent {
            StrengthSetExecutionScreen(
                entry = entry,
                onExerciseClick = { exerciseClicked = true },
                onEntryChange = {},
                onAddSet = { addSetClicked = true }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthSetExecutionExercise)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthSetExecutionAddSet)
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(exerciseClicked)
            assertTrue(addSetClicked)
        }
    }
}

private fun strengthTestEntries(): List<StrengthRoutineEntry> {
    val squat = strengthExerciseCatalog.first { it.id == "squat" }
    val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
    val row = strengthExerciseCatalog.first { it.id == "row" }
    return listOf(
        defaultStrengthRoutineEntry(id = 1, exercise = squat),
        defaultStrengthRoutineEntry(id = 2, exercise = bench),
        defaultStrengthRoutineEntry(id = 3, exercise = row)
    )
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.setThemedContent(
    content: @Composable () -> Unit,
) {
    setContent {
        IntervalsGymTheme(content = content)
    }
}

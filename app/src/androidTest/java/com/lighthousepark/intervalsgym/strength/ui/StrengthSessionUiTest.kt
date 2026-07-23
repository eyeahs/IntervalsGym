package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.click
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.overlay.RestOverlayRequests
import com.lighthousepark.intervalsgym.strength.ActiveStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthRoutineUpdateSelection
import com.lighthousepark.intervalsgym.strength.StrengthSetMetricType
import com.lighthousepark.intervalsgym.strength.StrengthSetRecord
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
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
    fun finishChoiceDialog_invokesSaveDiscardAndRoutineSelectionCallbacks() {
        var saved = false
        var discarded = false
        val availability = StrengthRoutineUpdateSelection(
            order = true,
            supersets = true
        )
        var selection by mutableStateOf(availability)

        composeRule.setThemedContent {
            StrengthFinishChoiceDialog(
                apiKey = "",
                entries = strengthTestEntries(),
                finishRpe = 7,
                routineUpdateAvailability = availability,
                routineUpdateSelection = selection,
                isUploading = false,
                onRoutineUpdateSelectionChange = { selection = it },
                onFinishRpeChange = {},
                onDismiss = {},
                onSave = { saved = true },
                onDiscard = { discarded = true }
            )
        }

        composeRule.onNodeWithText("운동 기록을 로컬에 저장하거나 삭제할 수 있습니다.").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishUpdateOrder)
            .assertIsOn()
            .performClick()
            .assertIsOff()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishUpdateSupersets)
            .assertIsOn()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishUpdateExerciseDetails)
            .assertIsNotEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishSave)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishDiscard)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertFalse(selection.order)
            assertTrue(selection.supersets)
            assertTrue(saved)
            assertTrue(discarded)
        }
    }

    @Test
    fun restoredSessionFinishChoice_keepsAllRoutineUpdatesClickable() {
        val routine = defaultStrengthRoutines().first().copy(entries = strengthTestEntries())
        val changedEntries = listOf(
            routine.entries[1].copy(
                exercise = strengthExerciseCatalog.last(),
                supersetGroupId = 7,
                note = "운동 중 변경"
            ),
            routine.entries[0].copy(supersetGroupId = 7),
            routine.entries[2]
        )
        val activeSession = ActiveStrengthSession(
            routineId = routine.id,
            routineName = routine.name,
            entries = changedEntries,
            hasStarted = true,
            sessionStartedAtMillis = System.currentTimeMillis() - 60_000L,
            isSetScreenVisible = false,
            currentExerciseIndex = 0,
            currentSetIndex = 0,
            pendingExerciseIndex = null,
            pendingSetIndex = null,
            restEndAtMillis = 0L,
            isRestSheetVisible = false,
            restTitle = "",
            setEvents = emptyList(),
            restEvents = emptyList(),
            activeRestEventId = null,
            routineBaselineEntries = routine.entries
        )

        composeRule.setThemedContent {
            StrengthSessionScreen(
                apiKey = "",
                routine = activeSession.toWorkoutRoutine(),
                calendarRoutineItem = null,
                isRoutineEditable = false,
                activeSession = activeSession,
                startImmediately = false,
                onImmediateStartConsumed = {},
                onSessionChange = {},
                onSessionFinished = { _, _ -> },
                onHistoryClick = {},
                onEditRoutine = {},
                onCalendarRoutineDeleted = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishWorkout)
            .performClick()

        listOf(
            TestContentDescriptions.StrengthFinishUpdateOrder,
            TestContentDescriptions.StrengthFinishUpdateSupersets,
            TestContentDescriptions.StrengthFinishUpdateExerciseTypes,
            TestContentDescriptions.StrengthFinishUpdateExerciseDetails
        ).forEach { contentDescription ->
            composeRule
                .onNodeWithContentDescription(contentDescription)
                .assertIsEnabled()
                .assertIsOn()
                .performClick()
                .assertIsOff()
        }
    }

    @Test
    fun strengthSessionScreen_startButtonOpensFirstExerciseWhenRoutineStartsWithSuperset() {
        val routine = defaultStrengthRoutines().first().let { source ->
            source.copy(
                entries = source.entries.take(2).map { entry ->
                    entry.copy(supersetGroupId = 7)
                }
            )
        }

        composeRule.setThemedContent {
            StrengthSessionScreen(
                apiKey = "",
                routine = routine,
                calendarRoutineItem = null,
                isRoutineEditable = true,
                activeSession = null,
                startImmediately = false,
                onImmediateStartConsumed = {},
                onSessionChange = {},
                onSessionFinished = { _, _ -> },
                onHistoryClick = {},
                onEditRoutine = {},
                onCalendarRoutineDeleted = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthStartWorkout)
            .performClick()

        composeRule.onNodeWithText("Set 1 · ${routine.entries.first().title}").assertExists()
        composeRule.onNodeWithText("Set 1 · ${routine.entries.last().title}").assertDoesNotExist()
    }

    @Test
    fun finishChoiceDialog_disablesSaveAndDiscardWhileUploading() {
        composeRule.setThemedContent {
            StrengthFinishChoiceDialog(
                apiKey = "api-key",
                entries = strengthTestEntries(),
                finishRpe = 8,
                routineUpdateAvailability = StrengthRoutineUpdateSelection(order = true),
                routineUpdateSelection = StrengthRoutineUpdateSelection(order = true),
                isUploading = true,
                onRoutineUpdateSelectionChange = {},
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
                routineUpdateAvailability = StrengthRoutineUpdateSelection(),
                routineUpdateSelection = StrengthRoutineUpdateSelection(),
                isUploading = false,
                onRoutineUpdateSelectionChange = {},
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
    fun showRestSheetOverlayRequestWaitsForForegroundAndConsumesOnlyOnce() {
        var isAppInForeground by mutableStateOf(false)
        var showSheetCount by mutableStateOf(0)

        composeRule.setThemedContent {
            StrengthShowRestSheetOverlayRequestEffect(
                isAppInteractive = isAppInForeground,
                isRestTimerActive = true,
                onShowRestSheet = { showSheetCount += 1 }
            )
        }

        composeRule.runOnIdle {
            RestOverlayRequests.requestShowSheet()
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle { assertEquals(0, showSheetCount) }

        composeRule.runOnIdle { isAppInForeground = true }
        composeRule.waitUntil(timeoutMillis = 5_000L) { showSheetCount == 1 }
        composeRule.runOnIdle { isAppInForeground = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isAppInForeground = true }
        composeRule.waitForIdle()

        composeRule.runOnIdle { assertEquals(1, showSheetCount) }
    }

    @Test
    fun inactiveRestConsumesOverlayRequestWithoutOpeningNextRestSheet() {
        var isRestTimerActive by mutableStateOf(false)
        var showSheetCount by mutableStateOf(0)

        composeRule.setThemedContent {
            StrengthShowRestSheetOverlayRequestEffect(
                isAppInteractive = true,
                isRestTimerActive = isRestTimerActive,
                onShowRestSheet = { showSheetCount += 1 }
            )
        }

        composeRule.runOnIdle {
            RestOverlayRequests.requestShowSheet()
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isRestTimerActive = true }
        composeRule.waitForIdle()

        composeRule.runOnIdle { assertEquals(0, showSheetCount) }
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
        val supersetSelectionUiState = StrengthSupersetSelectionUiState()
        var addExerciseClicked = false

        composeRule.setThemedContent {
            StrengthSessionOngoingRoutineScreen(
                routine = routine,
                entries = routine.entries,
                currentExerciseIndex = 0,
                uploadMessage = null,
                uploadError = null,
                supersetSelectionUiState = supersetSelectionUiState,
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
    fun ongoingRoutine_supersetModeReplacesBottomActionsWithoutInlinePanel() {
        val routine = defaultStrengthRoutines().first().copy(entries = strengthTestEntries())
        val supersetSelectionUiState = StrengthSupersetSelectionUiState()

        composeRule.setThemedContent {
            OngoingRoutineSupersetTestHost(
                routine = routine,
                supersetSelectionUiState = supersetSelectionUiState,
                onEntriesChange = {}
            )
        }

        composeRule.onNodeWithText("슈퍼세트").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthGroupSuperset)
            .performScrollTo()
            .performClick()

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthConfirmSuperset)
            .assertIsNotEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthClearSuperset)
            .assertIsNotEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthCancelSuperset)
            .assertExists()
        composeRule.onNodeWithText("선택 묶기").assertExists()
        composeRule.onNodeWithText("묶기 해제").assertExists()
        composeRule.onNodeWithText("취소").assertExists()
        composeRule.onNodeWithText("슈퍼세트로 묶을 운동을 선택하세요.").assertDoesNotExist()
        composeRule.onNodeWithText("0개 선택됨").assertDoesNotExist()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthGroupSuperset)
            .assertDoesNotExist()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthAddExercise)
            .assertDoesNotExist()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthResumeWorkoutExercise)
            .assertDoesNotExist()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishWorkout)
            .assertDoesNotExist()

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthOngoingEntry(1))
            .performScrollTo()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthConfirmSuperset)
            .assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthCancelSuperset)
            .assertExists()

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthCancelSuperset)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthGroupSuperset)
            .assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthAddExercise)
            .assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthResumeWorkoutExercise)
            .assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishWorkout)
            .assertExists()
    }

    @Test
    fun ongoingRoutine_supersetSelectionGroupsRowsAndMovesSecondBelowTop() {
        val routine = defaultStrengthRoutines().first().copy(entries = strengthTestEntries())
        val supersetSelectionUiState = StrengthSupersetSelectionUiState()
        var changedEntries: List<StrengthRoutineEntry>? = null

        composeRule.setThemedContent {
            OngoingRoutineSupersetTestHost(
                routine = routine,
                supersetSelectionUiState = supersetSelectionUiState,
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
    fun ongoingRoutine_supersetSelectionUsesGroupLabelInsteadOfCheckboxAndCanClearGroup() {
        val groupedEntries = strengthTestEntries().mapIndexed { index, entry ->
            entry.copy(supersetGroupId = if (index < 2) 9 else null)
        }
        val routine = defaultStrengthRoutines().first().copy(entries = groupedEntries)
        val supersetSelectionUiState = StrengthSupersetSelectionUiState()
        var changedEntries: List<StrengthRoutineEntry>? = null

        composeRule.setThemedContent {
            OngoingRoutineSupersetTestHost(
                routine = routine,
                supersetSelectionUiState = supersetSelectionUiState,
                onEntriesChange = { changedEntries = it }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthGroupSuperset)
            .performScrollTo()
            .performClick()
        composeRule.assertStrengthSupersetSelectionContract(
            existingGroupEntryIds = listOf(1, 2),
            looseEntryId = 3,
            hiddenActionContentDescriptions = listOf(
                TestContentDescriptions.StrengthGroupSuperset,
                TestContentDescriptions.StrengthAddExercise,
                TestContentDescriptions.StrengthResumeWorkoutExercise,
                TestContentDescriptions.StrengthFinishWorkout
            )
        )
        composeRule
            .onNodeWithContentDescription("길게 눌러 순서 변경")
            .assertDoesNotExist()

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthOngoingEntry(1))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthClearSuperset)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(requireNotNull(changedEntries).all { it.supersetGroupId == null })
        }
    }

    @Test
    fun ongoingRoutine_supersetSelectionAddsCheckedExerciseToSelectedExistingGroup() {
        val groupedEntries = strengthTestEntries().mapIndexed { index, entry ->
            entry.copy(supersetGroupId = if (index > 0) 9 else null)
        }
        val routine = defaultStrengthRoutines().first().copy(entries = groupedEntries)
        val supersetSelectionUiState = StrengthSupersetSelectionUiState()
        var changedEntries: List<StrengthRoutineEntry>? = null

        composeRule.setThemedContent {
            OngoingRoutineSupersetTestHost(
                routine = routine,
                supersetSelectionUiState = supersetSelectionUiState,
                onEntriesChange = { changedEntries = it }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthGroupSuperset)
            .performScrollTo()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthOngoingEntry(2))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthConfirmSuperset)
            .assertIsNotEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthOngoingEntry(1))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthConfirmSuperset)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            val result = requireNotNull(changedEntries)
            assertEquals(listOf(2, 3, 1), result.map { it.id })
            assertEquals(listOf(9, 9, 9), result.map { it.supersetGroupId })
        }
    }

    @Test
    fun ongoingRoutine_supersetSelectionAllowsRapidToggleTaps() {
        val routine = defaultStrengthRoutines().first().copy(entries = strengthTestEntries())
        val supersetSelectionUiState = StrengthSupersetSelectionUiState()

        composeRule.setThemedContent {
            OngoingRoutineSupersetTestHost(
                routine = routine,
                supersetSelectionUiState = supersetSelectionUiState,
                onEntriesChange = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthGroupSuperset)
            .performScrollTo()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthOngoingEntry(1))
            .performClick()
            .performClick()

        composeRule.runOnIdle {
            assertFalse(1 in supersetSelectionUiState.selectedEntryIds)
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
    fun durationSetRecordRow_editsActualSecondsWithoutChangingPlan() {
        var record by mutableStateOf(
            StrengthSetRecord(
                id = 11,
                weightKg = "",
                reps = "",
                durationSeconds = "45",
                restSeconds = "30",
                completed = false
            )
        )

        composeRule.setThemedContent {
            StrengthSetRecordRow(
                index = 0,
                record = record,
                setMetricType = StrengthSetMetricType.DURATION,
                showCompletion = false,
                showActualInput = true,
                onActualRecordChange = { record = it },
                onRecordChange = { record = it }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthPlannedSetDuration(record.id))
            .assertTextContains("45")
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthActualSetDuration(record.id))
            .performTextReplacement("38")

        composeRule.runOnIdle {
            assertEquals("45", record.durationSeconds)
            assertEquals("38", record.actualDurationSeconds)
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
    fun strengthSessionScreen_resumeButtonOpensCurrentExerciseSet() {
        val routine = defaultStrengthRoutines().first().copy(entries = strengthTestEntries())
        val currentExerciseIndex = 1
        val activeSession = ActiveStrengthSession(
            routineId = routine.id,
            routineName = routine.name,
            entries = routine.entries,
            hasStarted = true,
            sessionStartedAtMillis = System.currentTimeMillis() - 60_000L,
            isSetScreenVisible = false,
            currentExerciseIndex = currentExerciseIndex,
            currentSetIndex = 0,
            pendingExerciseIndex = null,
            pendingSetIndex = null,
            restEndAtMillis = 0L,
            isRestSheetVisible = false,
            restTitle = "",
            setEvents = emptyList(),
            restEvents = emptyList(),
            activeRestEventId = null
        )

        composeRule.setThemedContent {
            StrengthSessionScreen(
                apiKey = "",
                routine = routine,
                calendarRoutineItem = null,
                isRoutineEditable = true,
                activeSession = activeSession,
                startImmediately = false,
                onImmediateStartConsumed = {},
                onSessionChange = {},
                onSessionFinished = { _, _ -> },
                onHistoryClick = {},
                onEditRoutine = {},
                onCalendarRoutineDeleted = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthResumeWorkoutExercise)
            .assertTextContains(routine.entries[currentExerciseIndex].title)
            .performClick()

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthCompleteSet)
            .assertExists()
        composeRule
            .onNodeWithText("Set 1 · ${routine.entries[currentExerciseIndex].title}")
            .assertExists()
    }

    @Test
    fun strengthSessionScreen_groupedUnevenSupersetAdvancesToNextExercise() {
        val baseEntries = strengthTestEntries()
        val entries = listOf(
            baseEntries[0].copy(
                records = baseEntries[0].records.mapIndexed { index, record ->
                    record.copy(completed = index == 0)
                }
            ),
            baseEntries[1].copy(records = baseEntries[1].records.take(1)),
            baseEntries[2].copy(records = baseEntries[2].records.take(1))
        )
        val routine = defaultStrengthRoutines().first().copy(entries = entries)
        val activeSession = ActiveStrengthSession(
            routineId = routine.id,
            routineName = routine.name,
            entries = entries,
            hasStarted = true,
            sessionStartedAtMillis = System.currentTimeMillis() - 60_000L,
            isSetScreenVisible = false,
            currentExerciseIndex = 0,
            currentSetIndex = 1,
            pendingExerciseIndex = null,
            pendingSetIndex = null,
            restEndAtMillis = 0L,
            isRestSheetVisible = false,
            restTitle = "",
            setEvents = emptyList(),
            restEvents = emptyList(),
            activeRestEventId = null
        )
        var latestSession: ActiveStrengthSession? = null

        composeRule.setThemedContent {
            StrengthSessionScreen(
                apiKey = "",
                routine = routine,
                calendarRoutineItem = null,
                isRoutineEditable = true,
                activeSession = activeSession,
                startImmediately = false,
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
            .onNodeWithContentDescription(TestContentDescriptions.StrengthGroupSuperset)
            .performScrollTo()
            .performClick()
        entries.forEach { entry ->
            composeRule
                .onNodeWithContentDescription(TestContentDescriptions.strengthOngoingEntry(entry.id))
                .performScrollTo()
                .performClick()
        }
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthConfirmSuperset)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthOngoingEntry(entries[0].id))
            .performScrollTo()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthCompleteSet)
            .performClick()

        composeRule.onNodeWithText("Set 1 · ${entries[1].title}").assertExists()
        composeRule.runOnIdle {
            val session = requireNotNull(latestSession)
            assertEquals(1, session.currentExerciseIndex)
            assertEquals(0, session.currentSetIndex)
            assertFalse(session.isRestSheetVisible)
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
    fun restTimerBottomSheet_showsTimerWithoutExerciseLabelAndInvokesStop() {
        var stopped = false

        composeRule.setThemedContent {
            RestTimerBottomSheet(
                remainingSeconds = 75,
                onAdjustSeconds = {},
                onSetSeconds = {},
                onDismiss = {},
                onStop = { stopped = true }
            )
        }

        composeRule.onNodeWithText("01:15").assertExists()
        composeRule.onNodeWithText("스쿼트 휴식").assertDoesNotExist()
        composeRule.onNodeWithText("세트 휴식").assertDoesNotExist()
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
                remainingSeconds = 75,
                onClick = { clicked = true }
            )
        }

        composeRule.onNodeWithText("01:15").assertExists()
        composeRule.onNodeWithText("스쿼트 휴식").assertDoesNotExist()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRestFloatingChip)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(clicked)
        }
    }

    @Test
    fun restTimerFloatingChip_tracksRestBottomSheetVisibility() {
        var restUiState by mutableStateOf(
            StrengthRestUiState(
                activeRestEventId = 1,
                remainingSeconds = 75,
                endAtMillis = System.currentTimeMillis() + 75_000L,
                isSheetVisible = true,
                title = "스쿼트 휴식"
            )
        )

        composeRule.setThemedContent {
            Box(modifier = Modifier.fillMaxSize()) {
                Text("휴식 UI 테스트")
                if (restUiState.isSheetVisible) {
                    Text("휴식 bottom sheet 표시 중")
                }
                if (
                    restUiState.shouldShowFloatingChip(
                        hasStarted = true,
                        isChangingCurrentExercise = false,
                        canDrawSystemOverlay = false
                    )
                ) {
                    RestTimerFloatingChip(
                        remainingSeconds = restUiState.remainingSeconds ?: 0,
                        onClick = { restUiState = restUiState.withSheetVisible(true) }
                    )
                }
            }
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRestFloatingChip)
            .assertDoesNotExist()

        composeRule.runOnIdle {
            restUiState = restUiState.withSheetVisible(false)
        }
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRestFloatingChip)
            .assertExists()

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRestFloatingChip)
            .performClick()
        composeRule.onNodeWithText("휴식 bottom sheet 표시 중").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRestFloatingChip)
            .assertDoesNotExist()
    }

    @Test
    fun ongoingBottomBar_resumesActiveExerciseAndFinishesWorkout() {
        var resumed = false
        var finished = false
        var isUploading by mutableStateOf(false)

        composeRule.setThemedContent {
            StrengthSessionOngoingBottomBar(
                activeExerciseLabel = "스쿼트",
                isUploading = isUploading,
                onResumeExercise = { resumed = true },
                onFinish = { finished = true }
            )
        }

        composeRule.onNodeWithText("스쿼트").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthResumeWorkoutExercise)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishWorkout)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(resumed)
            assertTrue(finished)
            isUploading = true
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthResumeWorkoutExercise)
            .assertIsNotEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishWorkout)
            .assertIsNotEnabled()
    }

    @Test
    fun ongoingBottomBar_blocksRapidDuplicateFinishTap() {
        var finishCount = 0

        composeRule.setThemedContent {
            StrengthSessionOngoingBottomBar(
                activeExerciseLabel = "스쿼트",
                isUploading = false,
                onResumeExercise = {},
                onFinish = { finishCount += 1 }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthFinishWorkout)
            .performClick()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, finishCount)
        }
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

    @Test
    fun setExecutionScreen_currentSetRecordsActualValuesWithoutChangingPlan() {
        var entry by mutableStateOf(
            strengthTestEntries().first().let { source ->
                source.copy(
                    records = source.records.mapIndexed { index, record ->
                        record.copy(completed = index == 0)
                    }
                )
            }
        )
        val activeRecord = entry.records[1]
        val plannedWeights = entry.records.map { it.weightKg }
        val plannedReps = entry.records.map { it.reps }

        composeRule.setThemedContent {
            StrengthSetExecutionScreen(
                entry = entry,
                currentSetIndex = 1,
                onExerciseClick = {},
                onEntryChange = { entry = it },
                onAddSet = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthActualSetRecord(activeRecord.id))
            .assertExists()
        composeRule.onNodeWithText("결과").assertExists()
        composeRule.onNodeWithText("실제").assertDoesNotExist()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthActualSetRecord(entry.records[0].id))
            .assertDoesNotExist()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthActualSetWeight(activeRecord.id))
            .performTextReplacement("72.5")
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthActualSetReps(activeRecord.id))
            .performTextReplacement("6")

        composeRule.runOnIdle {
            val updatedRecord = entry.records[1]
            assertEquals(plannedWeights, entry.records.map { it.weightKg })
            assertEquals(plannedReps, entry.records.map { it.reps })
            assertEquals("72.5", updatedRecord.actualWeightKg)
            assertEquals("6", updatedRecord.actualReps)
            assertEquals("72.5", entry.records[2].actualWeightKg)
            assertEquals("6", entry.records[2].actualReps)
        }
    }

    @Test
    fun completedSetWithDifferentResultShowsPlanAndResultOnSeparateRows() {
        val record = strengthTestEntries().first().records.first().copy(
            weightKg = "10",
            reps = "8",
            actualWeightKg = "12.5",
            actualReps = "6",
            completed = true
        )

        composeRule.setThemedContent {
            StrengthSetRecordRow(
                index = 0,
                record = record,
                showCompletion = false,
                canResetCompleted = false,
                onRecordChange = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthPlannedSetWeight(record.id))
            .assertTextContains("10")
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthActualSetWeight(record.id))
            .assertTextContains("12.5")
        val plannedBounds = composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthPlannedSetRecord(record.id))
            .fetchSemanticsNode()
            .boundsInRoot
        val resultBounds = composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthActualSetRecord(record.id))
            .fetchSemanticsNode()
            .boundsInRoot
        assertTrue(resultBounds.center.y > plannedBounds.center.y)
    }

    @Test
    fun completedSetWithMatchingResultKeepsSingleRow() {
        val record = strengthTestEntries().first().records.first().copy(
            weightKg = "10",
            reps = "8",
            actualWeightKg = "10",
            actualReps = "8",
            completed = true
        )

        composeRule.setThemedContent {
            StrengthSetRecordRow(
                index = 0,
                record = record,
                showCompletion = false,
                canResetCompleted = false,
                onRecordChange = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthActualSetRecord(record.id))
            .assertDoesNotExist()
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun setMetricField_backspacePersistsEditedValue() {
        var value by mutableStateOf("10")

        composeRule.setThemedContent {
            SetMetricField(
                value = value,
                unit = "회",
                testContentDescription = "test-set-metric-backspace",
                onValueChange = { value = it }
            )
        }

        composeRule
            .onNodeWithContentDescription("test-set-metric-backspace")
            .performClick()
            .performKeyInput { pressKey(Key.Backspace) }
            .assertTextContains("1")
        composeRule.runOnIdle {
            assertEquals("1", value)
        }
    }

    @Test
    fun setMetricField_keyboardDismissDoesNotMutateValue() {
        var value by mutableStateOf("10")
        val changedValues = mutableListOf<String>()

        composeRule.setThemedContent {
            SetMetricField(
                value = value,
                unit = "회",
                testContentDescription = "test-set-metric-dismiss",
                onValueChange = {
                    changedValues += it
                    value = it
                }
            )
        }

        composeRule
            .onNodeWithContentDescription("test-set-metric-dismiss")
            .performClick()
        Espresso.pressBack()

        composeRule
            .onNodeWithContentDescription("test-set-metric-dismiss")
            .assertTextContains("10")
        composeRule.runOnIdle {
            assertEquals(emptyList<String>(), changedValues)
            assertEquals("10", value)
        }
    }

    @Test
    fun setExecutionScreen_plannedAndActualCellsMatchWidthAndMetricTouchTargetsAreExpanded() {
        var entry by mutableStateOf(
            strengthTestEntries().first().let { source ->
                source.copy(
                    records = source.records.mapIndexed { index, record ->
                        record.copy(completed = index == 0)
                    }
                )
            }
        )
        val activeRecord = entry.records[1]

        composeRule.setThemedContent {
            StrengthSetExecutionScreen(
                entry = entry,
                currentSetIndex = 1,
                onExerciseClick = {},
                onEntryChange = { entry = it },
                onAddSet = {}
            )
        }

        val plannedCellBounds = composeRule
            .onNodeWithContentDescription(
                TestContentDescriptions.strengthPlannedSetRecord(activeRecord.id)
            )
            .fetchSemanticsNode()
            .boundsInRoot
        val actualCellBounds = composeRule
            .onNodeWithContentDescription(
                TestContentDescriptions.strengthActualSetRecord(activeRecord.id)
            )
            .fetchSemanticsNode()
            .boundsInRoot
        assertEquals(plannedCellBounds.width, actualCellBounds.width, 0.5f)

        val minimumTouchTargetPx = with(composeRule.density) { 48.dp.toPx() }
        val metricDescriptions = listOf(
            TestContentDescriptions.strengthPlannedSetWeight(activeRecord.id),
            TestContentDescriptions.strengthPlannedSetReps(activeRecord.id),
            TestContentDescriptions.strengthActualSetWeight(activeRecord.id),
            TestContentDescriptions.strengthActualSetReps(activeRecord.id)
        )
        metricDescriptions.forEach { description ->
            val metricField = composeRule.onNodeWithContentDescription(description)
            val bounds = metricField.fetchSemanticsNode().boundsInRoot
            assertTrue("$description touch width", bounds.width >= minimumTouchTargetPx)
            assertTrue("$description touch height", bounds.height >= minimumTouchTargetPx)
            metricField
                .performTouchInput {
                    click(Offset(width * 0.8f, height / 2f))
                }
                .assertIsFocused()
        }
    }

    @Test
    fun strengthSessionScreen_onlyLatestCompletedSetCanSwipeToCancel() {
        val baseRoutine = defaultStrengthRoutines().first()
        val entry = baseRoutine.entries.first().copy(
            records = baseRoutine.entries.first().records.take(3).map { record ->
                record.copy(restSeconds = "0")
            }
        )
        val routine = baseRoutine.copy(entries = listOf(entry))
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

        repeat(2) {
            composeRule
                .onNodeWithContentDescription(TestContentDescriptions.StrengthCompleteSet)
                .performClick()
        }
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            latestSession?.setEvents?.size == 2
        }

        composeRule
            .onNodeWithContentDescription(
                TestContentDescriptions.strengthSetRecordRow(entry.records[0].id)
            )
            .performScrollTo()
            .performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            val session = requireNotNull(latestSession)
            assertTrue(session.entries.single().records[0].completed)
            assertEquals(2, session.setEvents.size)
        }

        composeRule
            .onNodeWithContentDescription(
                TestContentDescriptions.strengthSetRecordRow(entry.records[1].id)
            )
            .performScrollTo()
            .performTouchInput { swipeLeft() }
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            latestSession?.let { session ->
                session.setEvents.size == 1 &&
                    !session.entries.single().records[1].completed
            } == true
        }

        composeRule.runOnIdle {
            val session = requireNotNull(latestSession)
            assertTrue(session.entries.single().records[0].completed)
            assertFalse(session.entries.single().records[1].completed)
            assertEquals(entry.records[0].id, session.setEvents.single().setRecordId)
        }
        composeRule
            .onNodeWithContentDescription(
                TestContentDescriptions.strengthActualSetRecord(entry.records[1].id)
            )
            .assertExists()
    }

    @Test
    fun completedSetResetSwipe_hidesCancelActionUntilContentIsSwiped() {
        composeRule.setThemedContent {
            CompletedSetResetSwipeContainer(
                key = 1,
                enabled = true,
                onResetRequested = {}
            ) { contentModifier ->
                Box(modifier = contentModifier.height(64.dp))
            }
        }

        composeRule.onNodeWithText("완료 취소").assertDoesNotExist()
    }
}

@Composable
private fun OngoingRoutineSupersetTestHost(
    routine: StrengthWorkoutRoutine,
    supersetSelectionUiState: StrengthSupersetSelectionUiState,
    onEntriesChange: (List<StrengthRoutineEntry>) -> Unit,
) {
    StrengthSessionScaffold(
        routineName = routine.name,
        hasStarted = true,
        isChangingCurrentExercise = false,
        isSetScreenVisible = false,
        sessionElapsedSeconds = 60,
        showCalendarRoutineDelete = false,
        isDeletingCalendarRoutine = false,
        showRestTimerFloatingChip = false,
        restRemainingSeconds = 0,
        entries = routine.entries,
        currentExerciseIndex = 0,
        currentSetIndex = 0,
        supersetSelectionUiState = supersetSelectionUiState,
        isUploading = false,
        onBack = {},
        onCalendarRoutineDelete = {},
        onHistoryClick = {},
        onShowRestTimer = {},
        onCompleteSet = {},
        onResumeCurrentExercise = {},
        onFinish = {},
        onGroupSelectedSuperset = {
            supersetSelectionUiState.groupedEntries(routine.entries)?.let(onEntriesChange)
        },
        onClearSelectedSuperset = {
            supersetSelectionUiState.clearedEntries(routine.entries)?.let(onEntriesChange)
        }
    ) { innerPadding ->
        StrengthSessionOngoingRoutineScreen(
            routine = routine,
            entries = routine.entries,
            currentExerciseIndex = 0,
            uploadMessage = null,
            uploadError = null,
            supersetSelectionUiState = supersetSelectionUiState,
            modifier = Modifier.padding(innerPadding),
            onExerciseClick = {},
            onAddExercise = {},
            onEntriesChange = onEntriesChange
        )
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

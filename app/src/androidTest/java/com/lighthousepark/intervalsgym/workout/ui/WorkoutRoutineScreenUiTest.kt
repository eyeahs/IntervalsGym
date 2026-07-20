package com.lighthousepark.intervalsgym.workout.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lighthousepark.intervalsgym.app.PREFS_NAME
import com.lighthousepark.intervalsgym.app.RUNNING_SESSION_HISTORY_PREF
import com.lighthousepark.intervalsgym.app.SAVED_RUNNING_ROUTINES_PREF
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.data.RunningActivityMergeActions
import com.lighthousepark.intervalsgym.data.RunningActivityMergeResult
import com.lighthousepark.intervalsgym.data.appendRunningSessionHistory
import com.lighthousepark.intervalsgym.data.loadCompletedRunningSessionHistory
import com.lighthousepark.intervalsgym.data.loadSavedRunningWorkoutRoutines
import com.lighthousepark.intervalsgym.running.CompletedRunningSession
import com.lighthousepark.intervalsgym.running.HeartRateSensorState
import com.lighthousepark.intervalsgym.running.INTERVALS_GARMIN_ACTIVITY_SOURCE
import com.lighthousepark.intervalsgym.running.RunningActivityMergeCandidate
import com.lighthousepark.intervalsgym.running.RunningActivityMergeMatchMethod
import com.lighthousepark.intervalsgym.running.RunningRemoteActivity
import com.lighthousepark.intervalsgym.running.RunningRoutePoint
import com.lighthousepark.intervalsgym.running.ui.HeartRateConnectionAutoDismissEffect
import com.lighthousepark.intervalsgym.running.ui.HeartRateDevicePickerDialog
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.ui.theme.IntervalsGymTheme
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutRoutineScreenUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val prefs by lazy {
        InstrumentationRegistry.getInstrumentation()
            .targetContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    @Before
    fun clearSavedRoutines() {
        prefs.edit()
            .remove(SAVED_RUNNING_ROUTINES_PREF)
            .remove(RUNNING_SESSION_HISTORY_PREF)
            .commit()
    }

    @Test
    fun strengthRoutineDetail_startWorkoutInvokesStrengthStartCallback() {
        val strengthRoutine = defaultStrengthRoutines().first()
        var startedRoutine: StrengthWorkoutRoutine? = null

        composeRule.setThemedContent {
            WorkoutRoutineScreen(
                apiKey = "",
                routine = strengthTrainingItem(strengthRoutine),
                onStartStrengthRoutine = { startedRoutine = it },
                onStrengthSessionUploaded = {},
                onRoutineDeleted = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutRoutineStartWorkout)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(strengthRoutine.id, startedRoutine?.id)
        }
    }

    @Test
    fun intervalsStrengthPlan_canBeSavedToLocalRoutineLibrary() {
        val strengthRoutine = defaultStrengthRoutines().first().copy(id = 81)
        var savedRoutine: StrengthWorkoutRoutine? = null

        composeRule.setThemedContent {
            WorkoutRoutineScreen(
                apiKey = "",
                routine = strengthTrainingItem(strengthRoutine),
                onStartStrengthRoutine = {},
                onStrengthSessionUploaded = {},
                onRoutineDeleted = {},
                localStrengthRoutines = emptyList(),
                onSaveStrengthRoutineLocally = { savedRoutine = it },
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutRoutineSaveStrength)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(strengthRoutine, savedRoutine)
        }
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutRoutineSaveStrength)
            .assertDoesNotExist()
    }

    @Test
    fun intervalsStrengthPlan_hidesLocalSaveWhenSameRoutineAlreadyExists() {
        val strengthRoutine = defaultStrengthRoutines().first().copy(id = 82)

        composeRule.setThemedContent {
            WorkoutRoutineScreen(
                apiKey = "",
                routine = strengthTrainingItem(strengthRoutine),
                onStartStrengthRoutine = {},
                onStrengthSessionUploaded = {},
                onRoutineDeleted = {},
                localStrengthRoutines = listOf(strengthRoutine),
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutRoutineSaveStrength)
            .assertDoesNotExist()
    }

    @Test
    fun runningRoutineDetail_saveButtonPersistsExecutableRunningRoutine() {
        val item = runningTrainingItem()

        composeRule.setThemedContent {
            WorkoutRoutineScreen(
                apiKey = "",
                routine = item,
                onStartStrengthRoutine = {},
                onStrengthSessionUploaded = {},
                onRoutineDeleted = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutRoutineSaveRunning)
            .performClick()

        composeRule.runOnIdle {
            val savedRoutines = loadSavedRunningWorkoutRoutines(prefs)
            assertEquals(1, savedRoutines.size)
            assertEquals(item.name, savedRoutines.single().name)
            assertTrue(savedRoutines.single().blocks.isNotEmpty())
        }
    }

    @Test
    fun runningRoutineDetail_heartRateButtonIsAccessible() {
        composeRule.setThemedContent {
            WorkoutRoutineScreen(
                apiKey = "",
                routine = runningTrainingItem(),
                onStartStrengthRoutine = {},
                onStrengthSessionUploaded = {},
                onRoutineDeleted = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutRoutineHeartRate)
            .assertIsEnabled()
        composeRule.onNodeWithText("심박계").assertExists()
    }

    @Test
    fun routineDetail_backButtonInvokesBackCallback() {
        var backClicks = 0

        composeRule.setThemedContent {
            WorkoutRoutineScreen(
                apiKey = "",
                routine = runningTrainingItem(),
                onStartStrengthRoutine = {},
                onStrengthSessionUploaded = {},
                onRoutineDeleted = {},
                onBack = { backClicks += 1 }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutRoutineBack)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, backClicks)
        }
    }

    @Test
    fun routineDetail_confirmDeleteInvokesRoutineDeletedCallback() {
        val item = runningTrainingItem()
        var deletedRoutine: TrainingItem? = null

        composeRule.setThemedContent {
            WorkoutRoutineScreen(
                apiKey = "",
                routine = item,
                onStartStrengthRoutine = {},
                onStrengthSessionUploaded = {},
                onRoutineDeleted = { deletedRoutine = it },
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutRoutineDelete)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutRoutineConfirmDelete)
            .performClick()

        composeRule.waitUntil(5_000) {
            deletedRoutine != null
        }
        composeRule.runOnIdle {
            assertEquals(item.id, deletedRoutine?.id)
        }
    }

    @Test
    fun routineDetail_cancelDeleteDoesNotInvokeRoutineDeletedCallback() {
        val item = runningTrainingItem()
        var deletedRoutine: TrainingItem? = null

        composeRule.setThemedContent {
            WorkoutRoutineScreen(
                apiKey = "",
                routine = item,
                onStartStrengthRoutine = {},
                onStrengthSessionUploaded = {},
                onRoutineDeleted = { deletedRoutine = it },
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutRoutineDelete)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutRoutineCancelDelete)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(null, deletedRoutine)
        }
    }

    @Test
    fun localStrengthSessionDetail_exposesUploadActionWhenApiKeyExists() {
        val localResult = localStrengthResultItem()

        composeRule.setThemedContent {
            WorkoutRoutineScreen(
                apiKey = "api-key",
                routine = localResult,
                onStartStrengthRoutine = {},
                onStrengthSessionUploaded = {},
                onRoutineDeleted = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutRoutineUploadLocalWorkout)
            .assertIsEnabled()
    }

    @Test
    fun localStrengthSessionDetail_hidesUploadActionWhenApiKeyIsBlank() {
        val localResult = localStrengthResultItem()

        composeRule.setThemedContent {
            WorkoutRoutineScreen(
                apiKey = "",
                routine = localResult,
                onStartStrengthRoutine = {},
                onStrengthSessionUploaded = {},
                onRoutineDeleted = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutRoutineUploadLocalWorkout)
            .assertDoesNotExist()
    }

    @Test
    fun localRunningSessionDetail_deleteRemovesHistoryAndNavigatesBack() {
        val workout = completedRunningSessionForScreen()
        appendRunningSessionHistory(prefs, workout)
        val localResult = localRunningResultItem(workout)
        var backClicks = 0

        composeRule.setThemedContent {
            WorkoutRoutineScreen(
                apiKey = "",
                routine = localResult,
                onStartStrengthRoutine = {},
                onStrengthSessionUploaded = {},
                onRoutineDeleted = {},
                onBack = { backClicks += 1 }
            )
        }

        composeRule.runOnIdle {
            assertEquals(listOf(workout.id), loadCompletedRunningSessionHistory(prefs).map { it.id })
        }
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.LocalRunningSessionDelete)
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, backClicks)
            assertTrue(loadCompletedRunningSessionHistory(prefs).isEmpty())
        }
    }

    @Test
    fun localRunningSessionDetail_mergesSelectedGarminActivityAfterConfirmation() {
        val workout = completedRunningSessionForScreen()
        appendRunningSessionHistory(prefs, workout)
        val mergeActions = FakeRunningActivityMergeActions(
            candidate = runningMergeCandidate()
        )

        composeRule.setThemedContent {
            WorkoutRoutineScreen(
                apiKey = "api-key",
                routine = localRunningResultItem(workout),
                onStartStrengthRoutine = {},
                onStrengthSessionUploaded = {},
                onRoutineDeleted = {},
                runningActivityMergeActionsOverride = mergeActions,
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningMergeGarmin)
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningMergeConfirm)
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(5_000) { mergeActions.mergeCalls == 1 }
        composeRule.onNodeWithText("Garmin 기록에 IntervalsGym 수행 정보를 병합했습니다.").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningMergeGarmin)
            .assertDoesNotExist()
    }

    @Test
    fun localRunningSessionDetail_hidesGarminMergeWhenIntervalsLoginIsMissing() {
        val workout = completedRunningSessionForScreen()
        appendRunningSessionHistory(prefs, workout)

        composeRule.setThemedContent {
            WorkoutRoutineScreen(
                apiKey = "",
                routine = localRunningResultItem(workout),
                onStartStrengthRoutine = {},
                onStrengthSessionUploaded = {},
                onRoutineDeleted = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningMergeGarmin)
            .assertDoesNotExist()
    }

    @Test
    fun runningMergeDialog_allowsChoosingGarminCandidateBeforeConfirmation() {
        val first = runningMergeCandidate(id = "i-garmin-first", name = "아침 러닝")
        val second = runningMergeCandidate(id = "i-garmin-second", name = "저녁 러닝")
        var selectedId by mutableStateOf(first.activity.id)
        var confirmed = false

        composeRule.setThemedContent {
            WorkoutRunningMergeConfirmDialog(
                candidates = listOf(first, second),
                selectedCandidateId = selectedId,
                isMerging = false,
                onCandidateSelected = { selectedId = it },
                onDismiss = {},
                onConfirm = { confirmed = true }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.runningMergeCandidate(second.activity.id))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningMergeConfirm)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(second.activity.id, selectedId)
            assertTrue(confirmed)
        }
    }

    @Test
    fun heartRateDevicePicker_emptyStateInvokesRescanAndDismissCallbacks() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val state = HeartRateSensorState(context)
        var rescanClicked = false
        var dismissClicked = false

        composeRule.setThemedContent {
            HeartRateDevicePickerDialog(
                state = state,
                onDismiss = { dismissClicked = true },
                onDeviceSelected = {},
                onRescan = { rescanClicked = true },
                onDisconnect = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.HeartRatePickerRescan)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.HeartRatePickerDismiss)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(rescanClicked)
            assertTrue(dismissClicked)
        }
    }

    @Test
    fun heartRateDevicePicker_emptyStateHidesDisconnectAction() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val state = HeartRateSensorState(context)

        composeRule.setThemedContent {
            HeartRateDevicePickerDialog(
                state = state,
                onDismiss = {},
                onDeviceSelected = {},
                onRescan = {},
                onDisconnect = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.HeartRatePickerDisconnect)
            .assertDoesNotExist()
    }

    @Test
    fun heartRateConnectionAutoDismissEffect_dismissesOnlyAfterDisconnectedStateConnects() {
        var isConnected by mutableStateOf(false)
        var dismissCalls = 0

        composeRule.setThemedContent {
            HeartRateConnectionAutoDismissEffect(
                isConnected = isConnected,
                onDismiss = { dismissCalls += 1 }
            )
        }

        composeRule.runOnIdle {
            assertEquals(0, dismissCalls)
            isConnected = true
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            assertEquals(1, dismissCalls)
        }
    }
}

private class FakeRunningActivityMergeActions(
    private val candidate: RunningActivityMergeCandidate,
) : RunningActivityMergeActions {
    var mergeCalls: Int = 0

    override suspend fun findCandidates(session: CompletedRunningSession): List<RunningActivityMergeCandidate> {
        return listOf(candidate)
    }

    override suspend fun merge(
        session: CompletedRunningSession,
        candidate: RunningActivityMergeCandidate,
    ): RunningActivityMergeResult {
        mergeCalls += 1
        return RunningActivityMergeResult(
            session = session.copy(
                mergedIntervalsActivityId = candidate.activity.id,
                mergeOffsetSeconds = candidate.offsetSeconds,
                mergeCorrelation = candidate.heartRateCorrelation
            ),
            deletedDuplicateActivity = false
        )
    }
}

private fun runningMergeCandidate(
    id: String = "i-garmin-ui",
    name: String = "Garmin Run",
): RunningActivityMergeCandidate {
    return RunningActivityMergeCandidate(
        activity = RunningRemoteActivity(
            id = id,
            name = name,
            type = "Run",
            source = INTERVALS_GARMIN_ACTIVITY_SOURCE,
            externalId = null,
            startedAtMillis = 1_000L,
            durationSeconds = 60,
            description = null
        ),
        matchMethod = RunningActivityMergeMatchMethod.HEART_RATE,
        offsetSeconds = 2,
        heartRateCorrelation = 0.93,
        comparedHeartRateSamples = 60,
        startDifferenceSeconds = 2,
        durationDifferenceSeconds = 0,
        duplicateActivityId = "i-app-ui"
    )
}

private fun strengthTrainingItem(strengthRoutine: StrengthWorkoutRoutine): TrainingItem {
    return TrainingItem(
        id = "strength-routine-ui-test",
        remoteId = "strength-routine-ui-test",
        externalId = null,
        name = strengthRoutine.name,
        type = "Weight Training",
        date = LocalDate.of(2026, 7, 1),
        startedAt = LocalDate.of(2026, 7, 1).atStartOfDay(),
        timeLabel = "Routine",
        durationSeconds = 3600,
        distanceMeters = null,
        weightLiftedKg = null,
        load = null,
        fitness = null,
        fatigue = null,
        form = null,
        description = null,
        blocks = emptyList(),
        isRoutine = true,
        matchedStrengthRoutine = strengthRoutine
    )
}

private fun localStrengthResultItem(): TrainingItem {
    val routine = defaultStrengthRoutines().first()
    val startedAt = LocalDate.of(2026, 7, 1).atStartOfDay()
    val workout = CompletedStrengthSession(
        id = "local-strength-result-ui-test",
        routineId = routine.id,
        routineName = routine.name,
        startedAtMillis = 1_000L,
        endedAtMillis = 61_000L,
        durationSeconds = 60,
        intervalsExternalId = "strength-local-strength-result-ui-test",
        entries = routine.entries,
        setEvents = emptyList(),
        restEvents = emptyList(),
        rpe = 7,
        trainingLoad = 70,
        uploadedToIntervals = false
    )
    return TrainingItem(
        id = "local-strength-result-ui-test",
        remoteId = workout.id,
        externalId = workout.intervalsExternalId,
        name = routine.name,
        type = "Weight Training",
        date = startedAt.toLocalDate(),
        startedAt = startedAt,
        timeLabel = "08:00",
        durationSeconds = workout.durationSeconds,
        distanceMeters = null,
        weightLiftedKg = null,
        load = workout.trainingLoad,
        fitness = null,
        fatigue = null,
        form = null,
        description = null,
        blocks = emptyList(),
        isRoutine = false,
        matchedStrengthSession = workout,
        isLocalOnlyStrengthResult = true
    )
}

private fun completedRunningSessionForScreen(): CompletedRunningSession {
    return CompletedRunningSession(
        id = "local-running-result-ui-test",
        name = "로컬 러닝 결과",
        startedAtMillis = 1_000L,
        endedAtMillis = 61_000L,
        durationSeconds = 60,
        warmupSeconds = 0,
        estimatedDistanceMeters = 100.0,
        blocks = listOf(runningResultBlock()),
        actualBlocks = listOf(runningResultBlock()),
        uploadedToIntervals = false,
        routePoints = listOf(
            RunningRoutePoint(elapsedSeconds = 0, latitude = 37.241, longitude = 131.867),
            RunningRoutePoint(elapsedSeconds = 60, latitude = 37.242, longitude = 131.868)
        )
    )
}

private fun localRunningResultItem(workout: CompletedRunningSession): TrainingItem {
    val startedAt = LocalDate.of(2026, 7, 1).atStartOfDay()
    return TrainingItem(
        id = "local-${workout.id}",
        remoteId = workout.id,
        externalId = workout.id,
        name = workout.name,
        type = "Run",
        date = startedAt.toLocalDate(),
        startedAt = startedAt,
        timeLabel = "08:00",
        durationSeconds = workout.durationSeconds,
        distanceMeters = workout.estimatedDistanceMeters,
        weightLiftedKg = null,
        load = null,
        fitness = null,
        fatigue = null,
        form = null,
        description = "로컬 러닝 기록",
        blocks = workout.blocks,
        isRoutine = false,
        isLocalOnlyRunningResult = true,
        actualRunningBlocks = workout.actualBlocks,
        actualRunningRoutePoints = workout.routePoints
    )
}

private fun runningResultBlock(): RoutineBlock {
    return RoutineBlock(
        index = 0,
        title = "Block 1",
        kind = "work",
        targetText = "6km/h · 1%",
        durationSeconds = 60,
        startSecond = 0,
        endSecond = 60,
        isRecovery = false
    )
}

private fun runningTrainingItem(): TrainingItem {
    return TrainingItem(
        id = "running-routine-ui-test",
        remoteId = "running-routine-ui-test",
        externalId = "running-routine-ui-test",
        name = "UI 러닝 Routine",
        type = "Run",
        date = LocalDate.of(2026, 7, 1),
        startedAt = LocalDate.of(2026, 7, 1).atStartOfDay(),
        timeLabel = "Routine",
        durationSeconds = 60,
        distanceMeters = null,
        weightLiftedKg = null,
        load = null,
        fitness = null,
        fatigue = null,
        form = null,
        description = "1m 10:00 pace [6km/h 1%]",
        blocks = listOf(
            RoutineBlock(
                index = 0,
                title = "Block 1",
                kind = "work",
                targetText = "10:00 pace",
                durationSeconds = 60,
                startSecond = 0,
                endSecond = 60,
                isRecovery = false
            )
        ),
        isRoutine = true
    )
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.setThemedContent(
    content: @Composable () -> Unit,
) {
    setContent {
        IntervalsGymTheme(content = content)
    }
}

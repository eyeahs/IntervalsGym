package com.lighthousepark.intervalsgym.workout.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lighthousepark.intervalsgym.app.PREFS_NAME
import com.lighthousepark.intervalsgym.app.RUNNING_WORKOUT_HISTORY_PREF
import com.lighthousepark.intervalsgym.app.SAVED_RUNNING_PLANS_PREF
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.data.appendRunningWorkoutHistory
import com.lighthousepark.intervalsgym.data.loadCompletedRunningWorkoutHistory
import com.lighthousepark.intervalsgym.data.loadSavedRunningWorkoutPlans
import com.lighthousepark.intervalsgym.running.CompletedRunningWorkout
import com.lighthousepark.intervalsgym.running.HeartRateSensorState
import com.lighthousepark.intervalsgym.running.RunningRoutePoint
import com.lighthousepark.intervalsgym.strength.CompletedStrengthWorkout
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutPlan
import com.lighthousepark.intervalsgym.strength.defaultStrengthPlans
import com.lighthousepark.intervalsgym.training.PlanBlock
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
class WorkoutPlanScreenUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val prefs by lazy {
        InstrumentationRegistry.getInstrumentation()
            .targetContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    @Before
    fun clearSavedPlans() {
        prefs.edit()
            .remove(SAVED_RUNNING_PLANS_PREF)
            .remove(RUNNING_WORKOUT_HISTORY_PREF)
            .commit()
    }

    @Test
    fun strengthPlanDetail_startWorkoutInvokesStrengthStartCallback() {
        val strengthPlan = defaultStrengthPlans().first()
        var startedPlan: StrengthWorkoutPlan? = null

        composeRule.setThemedContent {
            WorkoutPlanScreen(
                apiKey = "",
                plan = strengthTrainingItem(strengthPlan),
                onStartStrengthPlan = { startedPlan = it },
                onStrengthWorkoutUploaded = {},
                onPlanDeleted = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutPlanStartWorkout)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(strengthPlan.id, startedPlan?.id)
        }
    }

    @Test
    fun runningPlanDetail_saveButtonPersistsExecutableRunningPlan() {
        val item = runningTrainingItem()

        composeRule.setThemedContent {
            WorkoutPlanScreen(
                apiKey = "",
                plan = item,
                onStartStrengthPlan = {},
                onStrengthWorkoutUploaded = {},
                onPlanDeleted = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutPlanSaveRunning)
            .performClick()

        composeRule.runOnIdle {
            val savedPlans = loadSavedRunningWorkoutPlans(prefs)
            assertEquals(1, savedPlans.size)
            assertEquals(item.name, savedPlans.single().name)
            assertTrue(savedPlans.single().blocks.isNotEmpty())
        }
    }

    @Test
    fun runningPlanDetail_heartRateButtonIsAccessible() {
        composeRule.setThemedContent {
            WorkoutPlanScreen(
                apiKey = "",
                plan = runningTrainingItem(),
                onStartStrengthPlan = {},
                onStrengthWorkoutUploaded = {},
                onPlanDeleted = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutPlanHeartRate)
            .assertIsEnabled()
        composeRule.onNodeWithText("심박계").assertExists()
    }

    @Test
    fun planDetail_backButtonInvokesBackCallback() {
        var backClicks = 0

        composeRule.setThemedContent {
            WorkoutPlanScreen(
                apiKey = "",
                plan = runningTrainingItem(),
                onStartStrengthPlan = {},
                onStrengthWorkoutUploaded = {},
                onPlanDeleted = {},
                onBack = { backClicks += 1 }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutPlanBack)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, backClicks)
        }
    }

    @Test
    fun planDetail_confirmDeleteInvokesPlanDeletedCallback() {
        val item = runningTrainingItem()
        var deletedPlan: TrainingItem? = null

        composeRule.setThemedContent {
            WorkoutPlanScreen(
                apiKey = "",
                plan = item,
                onStartStrengthPlan = {},
                onStrengthWorkoutUploaded = {},
                onPlanDeleted = { deletedPlan = it },
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutPlanDelete)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutPlanConfirmDelete)
            .performClick()

        composeRule.waitUntil(5_000) {
            deletedPlan != null
        }
        composeRule.runOnIdle {
            assertEquals(item.id, deletedPlan?.id)
        }
    }

    @Test
    fun planDetail_cancelDeleteDoesNotInvokePlanDeletedCallback() {
        val item = runningTrainingItem()
        var deletedPlan: TrainingItem? = null

        composeRule.setThemedContent {
            WorkoutPlanScreen(
                apiKey = "",
                plan = item,
                onStartStrengthPlan = {},
                onStrengthWorkoutUploaded = {},
                onPlanDeleted = { deletedPlan = it },
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutPlanDelete)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutPlanCancelDelete)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(null, deletedPlan)
        }
    }

    @Test
    fun localStrengthWorkoutDetail_exposesUploadActionWhenApiKeyExists() {
        val localResult = localStrengthResultItem()

        composeRule.setThemedContent {
            WorkoutPlanScreen(
                apiKey = "api-key",
                plan = localResult,
                onStartStrengthPlan = {},
                onStrengthWorkoutUploaded = {},
                onPlanDeleted = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutPlanUploadLocalWorkout)
            .assertIsEnabled()
    }

    @Test
    fun localStrengthWorkoutDetail_hidesUploadActionWhenApiKeyIsBlank() {
        val localResult = localStrengthResultItem()

        composeRule.setThemedContent {
            WorkoutPlanScreen(
                apiKey = "",
                plan = localResult,
                onStartStrengthPlan = {},
                onStrengthWorkoutUploaded = {},
                onPlanDeleted = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutPlanUploadLocalWorkout)
            .assertDoesNotExist()
    }

    @Test
    fun localRunningWorkoutDetail_deleteRemovesHistoryAndNavigatesBack() {
        val workout = completedRunningWorkoutForScreen()
        appendRunningWorkoutHistory(prefs, workout)
        val localResult = localRunningResultItem(workout)
        var backClicks = 0

        composeRule.setThemedContent {
            WorkoutPlanScreen(
                apiKey = "",
                plan = localResult,
                onStartStrengthPlan = {},
                onStrengthWorkoutUploaded = {},
                onPlanDeleted = {},
                onBack = { backClicks += 1 }
            )
        }

        composeRule.runOnIdle {
            assertEquals(listOf(workout.id), loadCompletedRunningWorkoutHistory(prefs).map { it.id })
        }
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.LocalRunningWorkoutDelete)
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, backClicks)
            assertTrue(loadCompletedRunningWorkoutHistory(prefs).isEmpty())
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
}

private fun strengthTrainingItem(strengthPlan: StrengthWorkoutPlan): TrainingItem {
    return TrainingItem(
        id = "strength-plan-ui-test",
        remoteId = "strength-plan-ui-test",
        externalId = null,
        name = strengthPlan.name,
        type = "Weight Training",
        date = LocalDate.of(2026, 7, 1),
        startedAt = LocalDate.of(2026, 7, 1).atStartOfDay(),
        timeLabel = "Plan",
        durationSeconds = 3600,
        distanceMeters = null,
        weightLiftedKg = null,
        load = null,
        fitness = null,
        fatigue = null,
        form = null,
        description = null,
        blocks = emptyList(),
        isPlan = true,
        matchedStrengthPlan = strengthPlan
    )
}

private fun localStrengthResultItem(): TrainingItem {
    val plan = defaultStrengthPlans().first()
    val startedAt = LocalDate.of(2026, 7, 1).atStartOfDay()
    val workout = CompletedStrengthWorkout(
        id = "local-strength-result-ui-test",
        planId = plan.id,
        planName = plan.name,
        startedAtMillis = 1_000L,
        endedAtMillis = 61_000L,
        durationSeconds = 60,
        intervalsExternalId = "strength-local-strength-result-ui-test",
        entries = plan.entries,
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
        name = plan.name,
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
        isPlan = false,
        matchedStrengthWorkout = workout,
        isLocalOnlyStrengthResult = true
    )
}

private fun completedRunningWorkoutForScreen(): CompletedRunningWorkout {
    return CompletedRunningWorkout(
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

private fun localRunningResultItem(workout: CompletedRunningWorkout): TrainingItem {
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
        isPlan = false,
        isLocalOnlyRunningResult = true,
        actualRunningBlocks = workout.actualBlocks,
        actualRunningRoutePoints = workout.routePoints
    )
}

private fun runningResultBlock(): PlanBlock {
    return PlanBlock(
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
        id = "running-plan-ui-test",
        remoteId = "running-plan-ui-test",
        externalId = "running-plan-ui-test",
        name = "UI 러닝 Plan",
        type = "Run",
        date = LocalDate.of(2026, 7, 1),
        startedAt = LocalDate.of(2026, 7, 1).atStartOfDay(),
        timeLabel = "Plan",
        durationSeconds = 60,
        distanceMeters = null,
        weightLiftedKg = null,
        load = null,
        fitness = null,
        fatigue = null,
        form = null,
        description = "1m 10:00 pace [6km/h 1%]",
        blocks = listOf(
            PlanBlock(
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
        isPlan = true
    )
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.setThemedContent(
    content: @Composable () -> Unit,
) {
    setContent {
        IntervalsGymTheme(content = content)
    }
}

package com.lighthousepark.intervalsgym.workout.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.data.buildCompletedStrengthSession
import com.lighthousepark.intervalsgym.running.RunningRoutePoint
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthRestEvent
import com.lighthousepark.intervalsgym.strength.StrengthSetCompletionEvent
import com.lighthousepark.intervalsgym.strength.StrengthSetRecord
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.strengthExerciseCatalog
import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.ui.theme.IntervalsGymTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutRoutineVisualsUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun localStrengthSessionDetailSection_rendersCompletedSetWithActualRest() {
        val workout = completedStrengthSessionForDetail()
        val entry = workout.entries.single()
        val record = entry.records.single()

        composeRule.setThemedContent {
            LocalStrengthSessionDetailSection(workout = workout)
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthSessionSetDetail(entry.id, record.id))
            .assertExists()
        composeRule.onNodeWithText("Set 1").assertExists()
        composeRule.onNodeWithText("25kg x 10회 · 휴식 120초 · 실제 01:30").assertExists()
        composeRule.onNodeWithText("완료").assertExists()
    }

    @Test
    fun localRunningSessionGraphSection_invokesDeleteCallback() {
        var deleteClicked = false

        composeRule.setThemedContent {
            LocalRunningSessionGraphSection(
                blocks = listOf(runningBlockForGraph()),
                totalSeconds = 60,
                routePoints = listOf(
                    RunningRoutePoint(elapsedSeconds = 0, latitude = 37.241, longitude = 131.867),
                    RunningRoutePoint(elapsedSeconds = 60, latitude = 37.242, longitude = 131.868)
                ),
                onDelete = { deleteClicked = true }
            )
        }

        composeRule.onNodeWithText("로컬 러닝 기록 그래프").assertExists()
        composeRule.onNodeWithText("2 points").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.LocalRunningSessionDelete)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(deleteClicked)
        }
    }

    @Test
    fun runningTimerPanel_invokesToggleAndResetCallbacks() {
        var toggleClicked = false
        var resetClicked = false

        composeRule.setThemedContent {
            RunningTimerPanel(
                elapsedSeconds = 15,
                totalSeconds = 60,
                currentBlock = runningBlockForGraph(),
                blockRemaining = 45,
                remainingTotal = 45,
                isRunning = false,
                onToggle = { toggleClicked = true },
                onReset = { resetClicked = true }
            )
        }

        composeRule.onNodeWithText("00:15 / 01:00").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningTimerToggle)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningTimerReset)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(toggleClicked)
            assertTrue(resetClicked)
        }
    }

    @Test
    fun runningTimerPanel_disablesToggleWhenNoDuration() {
        composeRule.setThemedContent {
            RunningTimerPanel(
                elapsedSeconds = 0,
                totalSeconds = 0,
                currentBlock = null,
                blockRemaining = 0,
                remainingTotal = 0,
                isRunning = false,
                onToggle = {},
                onReset = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningTimerToggle)
            .assertIsNotEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningTimerReset)
            .assertIsEnabled()
    }

    @Test
    fun errorView_retryButtonInvokesCallback() {
        var retryClicked = false

        composeRule.setThemedContent {
            ErrorView(
                message = "동기화 실패",
                onRetry = { retryClicked = true }
            )
        }

        composeRule.onNodeWithText("동기화 실패").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.WorkoutErrorRetry)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(retryClicked)
        }
    }
}

private fun runningBlockForGraph(): RoutineBlock {
    return RoutineBlock(
        index = 0,
        title = "Block 1",
        kind = "work",
        targetText = "10km/h",
        durationSeconds = 60,
        startSecond = 0,
        endSecond = 60,
        isRecovery = false
    )
}

private fun completedStrengthSessionForDetail(): CompletedStrengthSession {
    val exercise = strengthExerciseCatalog.first { it.id == "bench_press" }
    val record = StrengthSetRecord(
        id = 11,
        weightKg = "20",
        reps = "8",
        durationSeconds = "",
        restSeconds = "120",
        completed = false
    )
    val entry = defaultStrengthRoutineEntry(
        id = 3,
        exercise = exercise,
        weightKg = "20",
        reps = "8",
        restSeconds = "120"
    ).copy(
        records = listOf(record),
        targetSets = 1
    )
    val routine = StrengthWorkoutRoutine(
        id = 77,
        name = "상세 표시 테스트",
        entries = listOf(entry)
    )
    val setEvent = StrengthSetCompletionEvent(
        sequence = 1,
        exerciseEntryId = entry.id,
        exerciseTitle = entry.title,
        exerciseGroup = entry.exercise.group,
        exerciseId = entry.exercise.id,
        equipment = entry.equipment,
        variation = entry.variation,
        setRecordId = record.id,
        setIndex = 0,
        weightKg = "25",
        reps = "10",
        targetRestSeconds = 120,
        completedAtMillis = 1_000_000L
    )
    val restEvent = StrengthRestEvent(
        id = 1,
        afterSetSequence = setEvent.sequence,
        exerciseEntryId = entry.id,
        exerciseTitle = entry.title,
        setRecordId = record.id,
        setIndex = 0,
        startedAtMillis = 1_000_000L,
        plannedSeconds = 120,
        targetEndAtMillis = 1_120_000L,
        endedAtMillis = 1_090_000L,
        endReason = "finished"
    )
    return buildCompletedStrengthSession(
        routine = routine,
        entries = listOf(entry),
        setEvents = listOf(setEvent),
        restEvents = listOf(restEvent),
        startedAtMillis = 900_000L,
        endedAtMillis = 1_200_000L,
        rpe = 7,
        trainingLoad = 35,
        uploadedToIntervals = false
    )
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.setThemedContent(
    content: @Composable () -> Unit,
) {
    setContent {
        IntervalsGymTheme(content = content)
    }
}

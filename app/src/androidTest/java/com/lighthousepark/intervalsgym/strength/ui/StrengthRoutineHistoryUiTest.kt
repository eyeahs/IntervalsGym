package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthSetCompletionEvent
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import com.lighthousepark.intervalsgym.ui.theme.IntervalsGymTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StrengthRoutineHistoryUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun historyScreen_filtersByRoutineAndSelectsMatchingWorkout() {
        val routine = defaultStrengthRoutines().first().copy(id = 101, name = "상체")
        val otherRoutine = defaultStrengthRoutines().last().copy(id = 202, name = "하체")
        val olderWorkout = completedWorkout(routine, id = "older-history", startedAtMillis = 1_000_000L)
        val newerWorkout = completedWorkout(routine, id = "newer-history", startedAtMillis = 2_000_000L)
        val otherWorkout = completedWorkout(otherRoutine, id = "other-history", startedAtMillis = 3_000_000L)
        var selectedWorkout: CompletedStrengthSession? = null

        composeRule.setThemedContent {
            StrengthRoutineHistoryScreen(
                routine = routine,
                history = listOf(olderWorkout, otherWorkout, newerWorkout),
                onHistorySelected = { selectedWorkout = it },
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthHistoryRow(newerWorkout.id))
            .assertExists()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthHistoryRow(olderWorkout.id))
            .assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthHistoryRow(otherWorkout.id))
            .assertDoesNotExist()

        composeRule.runOnIdle {
            assertEquals(newerWorkout.id, selectedWorkout?.id)
        }
    }

    @Test
    fun historyScreen_showsEmptyStateWhenNoMatchingHistoryExists() {
        val routine = defaultStrengthRoutines().first().copy(id = 303, name = "빈 routine")
        val otherRoutine = defaultStrengthRoutines().last().copy(id = 404, name = "다른 routine")

        composeRule.setThemedContent {
            StrengthRoutineHistoryScreen(
                routine = routine,
                history = listOf(completedWorkout(otherRoutine, id = "other-only", startedAtMillis = 1_000_000L)),
                onHistorySelected = {},
                onBack = {}
            )
        }

        composeRule.onNodeWithText("저장된 history가 없습니다.").assertExists()
    }

    @Test
    fun historyScreen_backButtonInvokesBackCallback() {
        val routine = defaultStrengthRoutines().first().copy(id = 505, name = "back routine")
        var backClicks = 0

        composeRule.setThemedContent {
            StrengthRoutineHistoryScreen(
                routine = routine,
                history = emptyList(),
                onHistorySelected = {},
                onBack = { backClicks += 1 }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthHistoryBack)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, backClicks)
        }
    }
}

private fun completedWorkout(
    routine: StrengthWorkoutRoutine,
    id: String,
    startedAtMillis: Long,
): CompletedStrengthSession {
    val entry = routine.entries.first()
    val record = entry.records.first()
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
        weightKg = record.weightKg,
        reps = record.reps,
        targetRestSeconds = record.restSeconds.toIntOrNull() ?: entry.restSeconds,
        completedAtMillis = startedAtMillis + 60_000L
    )
    return CompletedStrengthSession(
        id = id,
        routineId = routine.id,
        routineName = routine.name,
        startedAtMillis = startedAtMillis,
        endedAtMillis = startedAtMillis + 600_000L,
        durationSeconds = 600,
        intervalsExternalId = "strength-$id",
        entries = routine.entries,
        setEvents = listOf(setEvent),
        restEvents = emptyList(),
        rpe = 7,
        trainingLoad = 70,
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

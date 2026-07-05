package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.strength.CompletedStrengthWorkout
import com.lighthousepark.intervalsgym.strength.StrengthSetCompletionEvent
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutPlan
import com.lighthousepark.intervalsgym.strength.defaultStrengthPlans
import com.lighthousepark.intervalsgym.ui.theme.IntervalsGymTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StrengthPlanHistoryUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun historyScreen_filtersByPlanAndSelectsMatchingWorkout() {
        val plan = defaultStrengthPlans().first().copy(id = 101, name = "상체")
        val otherPlan = defaultStrengthPlans().last().copy(id = 202, name = "하체")
        val olderWorkout = completedWorkout(plan, id = "older-history", startedAtMillis = 1_000_000L)
        val newerWorkout = completedWorkout(plan, id = "newer-history", startedAtMillis = 2_000_000L)
        val otherWorkout = completedWorkout(otherPlan, id = "other-history", startedAtMillis = 3_000_000L)
        var selectedWorkout: CompletedStrengthWorkout? = null

        composeRule.setThemedContent {
            StrengthPlanHistoryScreen(
                plan = plan,
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
        val plan = defaultStrengthPlans().first().copy(id = 303, name = "빈 plan")
        val otherPlan = defaultStrengthPlans().last().copy(id = 404, name = "다른 plan")

        composeRule.setThemedContent {
            StrengthPlanHistoryScreen(
                plan = plan,
                history = listOf(completedWorkout(otherPlan, id = "other-only", startedAtMillis = 1_000_000L)),
                onHistorySelected = {},
                onBack = {}
            )
        }

        composeRule.onNodeWithText("저장된 history가 없습니다.").assertExists()
    }

    @Test
    fun historyScreen_backButtonInvokesBackCallback() {
        val plan = defaultStrengthPlans().first().copy(id = 505, name = "back plan")
        var backClicks = 0

        composeRule.setThemedContent {
            StrengthPlanHistoryScreen(
                plan = plan,
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
    plan: StrengthWorkoutPlan,
    id: String,
    startedAtMillis: Long,
): CompletedStrengthWorkout {
    val entry = plan.entries.first()
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
    return CompletedStrengthWorkout(
        id = id,
        planId = plan.id,
        planName = plan.name,
        startedAtMillis = startedAtMillis,
        endedAtMillis = startedAtMillis + 600_000L,
        durationSeconds = 600,
        intervalsExternalId = "strength-$id",
        entries = plan.entries,
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

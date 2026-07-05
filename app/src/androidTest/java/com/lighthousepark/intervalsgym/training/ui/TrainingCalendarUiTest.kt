package com.lighthousepark.intervalsgym.training.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutPlan
import com.lighthousepark.intervalsgym.strength.defaultStrengthPlans
import com.lighthousepark.intervalsgym.training.PlanBlock
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.ui.theme.IntervalsGymTheme
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrainingCalendarUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun weeklyFabMenu_invokesExpandedActionCallbacks() {
        var expandedChangedTo: Boolean? = null
        var workoutClicked = false
        var planClicked = false

        composeRule.setThemedContent {
            Box {
                WeeklyTrainingFabMenu(
                    expanded = true,
                    onExpandedChange = { expandedChangedTo = it },
                    onWorkoutClick = { workoutClicked = true },
                    onPlanSaveClick = { planClicked = true }
                )
            }
        }

        composeRule.waitForIdle()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.trainingCalendarFabAction("운동 실행"))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.trainingCalendarFabAction("plan 관리"))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.TrainingCalendarFabMenu)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(workoutClicked)
            assertTrue(planClicked)
            assertEquals(false, expandedChangedTo)
        }
    }

    @Test
    fun workoutActionBottomSheet_invokesRunningAndStrengthCallbacks() {
        var runningClicked = false
        var strengthClicked = false

        composeRule.setThemedContent {
            WorkoutActionBottomSheet(
                onDismiss = {},
                onRunningClick = { runningClicked = true },
                onStrengthClick = { strengthClicked = true }
            )
        }

        composeRule.waitForIdle()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.TrainingActionRunning)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.TrainingActionStrength)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(runningClicked)
            assertTrue(strengthClicked)
        }
    }

    @Test
    fun weeklyTrainingScreen_settingsLoginActionInvokesLoginWhenConfigured() {
        var loginClicked = false
        var logoutClicked = false

        composeRule.setThemedContent {
            WeeklyTrainingScreen(
                apiKey = "",
                strengthPlans = emptyList(),
                deletedCalendarPlanIds = emptySet(),
                initialDate = LocalDate.of(2026, 7, 1),
                showCalendarModeButton = false,
                onPlanSelected = {},
                onIntervalStrengthPlanSelected = { _, _ -> },
                onManagePlans = {},
                onStrengthWorkout = {},
                onRunningWorkout = {},
                onLoginClick = { loginClicked = true },
                onLogout = { logoutClicked = true },
                isIntervalsOAuthConfigured = true
            )
        }

        composeRule.waitForIdle()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.TrainingCalendarSettings)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.TrainingCalendarIntervalsAuth)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(loginClicked)
            assertFalse(logoutClicked)
        }
    }

    @Test
    fun weeklyTrainingScreen_settingsAuthActionInvokesLogoutWhenConnected() {
        var loginClicked = false
        var logoutClicked = false

        composeRule.setThemedContent {
            WeeklyTrainingScreen(
                apiKey = "bearer-token",
                strengthPlans = emptyList(),
                deletedCalendarPlanIds = emptySet(),
                initialDate = LocalDate.of(2026, 7, 1),
                showCalendarModeButton = false,
                onPlanSelected = {},
                onIntervalStrengthPlanSelected = { _, _ -> },
                onManagePlans = {},
                onStrengthWorkout = {},
                onRunningWorkout = {},
                onLoginClick = { loginClicked = true },
                onLogout = { logoutClicked = true },
                isIntervalsOAuthConfigured = true,
                intervalsOAuthConnectedLabel = "hyunwoo"
            )
        }

        composeRule.waitForIdle()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.TrainingCalendarSettings)
            .performClick()
        composeRule.onNodeWithText("Intervals 로그아웃 · hyunwoo").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.TrainingCalendarIntervalsAuth)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertFalse(loginClicked)
            assertTrue(logoutClicked)
        }
    }

    @Test
    fun weeklyTrainingScreen_settingsRefreshActionClosesMenu() {
        composeRule.setThemedContent {
            WeeklyTrainingScreen(
                apiKey = "",
                strengthPlans = emptyList(),
                deletedCalendarPlanIds = emptySet(),
                initialDate = LocalDate.of(2026, 7, 1),
                showCalendarModeButton = false,
                onPlanSelected = {},
                onIntervalStrengthPlanSelected = { _, _ -> },
                onManagePlans = {},
                onStrengthWorkout = {},
                onRunningWorkout = {},
                onLoginClick = {},
                onLogout = {},
                isIntervalsOAuthConfigured = true
            )
        }

        composeRule.waitForIdle()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.TrainingCalendarSettings)
            .performClick()
        composeRule.onNodeWithText("새로고침").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.TrainingCalendarRefresh)
            .assertIsEnabled()
            .performClick()

        composeRule.onNodeWithText("새로고침").assertDoesNotExist()
    }

    @Test
    fun weeklyTrainingScreen_settingsAuthActionDisabledWhenOAuthIsUnavailable() {
        composeRule.setThemedContent {
            WeeklyTrainingScreen(
                apiKey = "",
                strengthPlans = emptyList(),
                deletedCalendarPlanIds = emptySet(),
                initialDate = LocalDate.of(2026, 7, 1),
                showCalendarModeButton = false,
                onPlanSelected = {},
                onIntervalStrengthPlanSelected = { _, _ -> },
                onManagePlans = {},
                onStrengthWorkout = {},
                onRunningWorkout = {},
                onLoginClick = {},
                onLogout = {},
                isIntervalsOAuthConfigured = false
            )
        }

        composeRule.waitForIdle()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.TrainingCalendarSettings)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.TrainingCalendarIntervalsAuth)
            .assertIsNotEnabled()
    }

    @Test
    fun weeklyTrainingScreen_backButtonInvokesBackCallback() {
        var backClicks = 0

        composeRule.setThemedContent {
            WeeklyTrainingScreen(
                apiKey = "",
                strengthPlans = emptyList(),
                deletedCalendarPlanIds = emptySet(),
                initialDate = LocalDate.of(2026, 7, 1),
                showBackButton = true,
                showCalendarModeButton = false,
                onPlanSelected = {},
                onIntervalStrengthPlanSelected = { _, _ -> },
                onManagePlans = {},
                onStrengthWorkout = {},
                onRunningWorkout = {},
                onLoginClick = {},
                onLogout = {},
                onBack = { backClicks += 1 }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.TrainingCalendarBack)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, backClicks)
        }
    }

    @Test
    fun weeklyTrainingScreen_calendarModeButtonCyclesTitle() {
        composeRule.setThemedContent {
            WeeklyTrainingScreen(
                apiKey = "",
                strengthPlans = emptyList(),
                deletedCalendarPlanIds = emptySet(),
                initialDate = LocalDate.of(2026, 7, 1),
                showCalendarModeButton = true,
                onPlanSelected = {},
                onIntervalStrengthPlanSelected = { _, _ -> },
                onManagePlans = {},
                onStrengthWorkout = {},
                onRunningWorkout = {},
                onLoginClick = {},
                onLogout = {}
            )
        }

        composeRule.onNodeWithText("주간 훈련").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.TrainingCalendarMode)
            .performClick()

        composeRule.onNodeWithText("월간 훈련").assertExists()
    }

    @Test
    fun monthlyCalendarDayCell_selectsEmptyDay() {
        val day = LocalDate.of(2026, 7, 1)
        var selectedDay: LocalDate? = null

        composeRule.setThemedContent {
            MonthlyCalendarDayCell(
                day = day,
                isInCurrentMonth = true,
                items = emptyList(),
                visibleItemCount = 2,
                modifier = Modifier.size(width = 120.dp, height = 92.dp),
                onPlanSelected = {},
                onIntervalStrengthPlanSelected = { _, _ -> },
                onDaySelected = { selectedDay = it }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.monthlyCalendarDay(day))
            .performClick()

        composeRule.runOnIdle {
            assertEquals(day, selectedDay)
        }
    }

    @Test
    fun monthlyCalendarDayCell_selectsResultItem() {
        val day = LocalDate.of(2026, 7, 1)
        val item = trainingCalendarItem(id = "run-1", type = "Run", date = day, isPlan = false)
        var selectedItem: TrainingItem? = null
        var strengthPlanSelected = false

        composeRule.setThemedContent {
            MonthlyCalendarDayCell(
                day = day,
                isInCurrentMonth = true,
                items = listOf(item),
                visibleItemCount = 2,
                modifier = Modifier.size(width = 120.dp, height = 92.dp),
                onPlanSelected = { selectedItem = it },
                onIntervalStrengthPlanSelected = { _, _ -> strengthPlanSelected = true },
                onDaySelected = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.monthlyCalendarItem(item.id))
            .performClick()

        composeRule.runOnIdle {
            assertSame(item, selectedItem)
            assertFalse(strengthPlanSelected)
        }
    }

    @Test
    fun monthlyCalendarDayCell_routesStrengthPlanChipToStrengthCallback() {
        val day = LocalDate.of(2026, 7, 2)
        val plan = StrengthWorkoutPlan(id = 7, name = "Upper", entries = emptyList())
        val item = trainingCalendarItem(
            id = "strength-plan-1",
            type = "WeightTraining",
            date = day,
            isPlan = true
        ).copy(matchedStrengthPlan = plan)
        var selectedItem: TrainingItem? = null
        var selectedPlan: StrengthWorkoutPlan? = null
        var genericPlanSelected = false

        composeRule.setThemedContent {
            MonthlyCalendarDayCell(
                day = day,
                isInCurrentMonth = true,
                items = listOf(item),
                visibleItemCount = 2,
                modifier = Modifier.size(width = 120.dp, height = 92.dp),
                onPlanSelected = { genericPlanSelected = true },
                onIntervalStrengthPlanSelected = { calendarItem, strengthPlan ->
                    selectedItem = calendarItem
                    selectedPlan = strengthPlan
                },
                onDaySelected = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.monthlyCalendarItem(item.id))
            .performClick()

        composeRule.runOnIdle {
            assertFalse(genericPlanSelected)
            assertSame(item, selectedItem)
            assertSame(plan, selectedPlan)
        }
    }

    @Test
    fun strengthPlanSaveRow_invokesPlanSelection() {
        val plan = defaultStrengthPlans().first().copy(id = 44, name = "Save Row Plan")
        var selectedPlan: StrengthWorkoutPlan? = null

        composeRule.setThemedContent {
            StrengthPlanSaveRow(
                plan = plan,
                isSaving = false,
                enabled = true,
                onClick = { selectedPlan = plan }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthPlanSaveRow(plan.id))
            .performClick()

        composeRule.runOnIdle {
            assertSame(plan, selectedPlan)
        }
    }

    @Test
    fun strengthPlanSaveBottomSheet_dateButtonOpensDatePicker() {
        val plan = defaultStrengthPlans().first().copy(id = 45, name = "Date Picker Plan")

        composeRule.setThemedContent {
            StrengthPlanSaveBottomSheet(
                plans = listOf(plan),
                selectedDate = LocalDate.of(2026, 7, 1),
                savingPlanId = null,
                message = null,
                error = null,
                onDismiss = {},
                onDateSelected = {},
                onPlanSelected = {}
            )
        }

        composeRule.waitForIdle()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanSaveDate)
            .assertIsEnabled()
            .performClick()

        composeRule.onNodeWithText("변경").assertExists()
        composeRule.onNodeWithText("취소").assertExists()
    }

    @Test
    fun strengthPlanSaveBottomSheet_disablesDateAndRowsWhileSaving() {
        val savingPlan = defaultStrengthPlans().first().copy(id = 46, name = "Saving Plan")
        val waitingPlan = defaultStrengthPlans().last().copy(id = 47, name = "Waiting Plan")
        var selectedPlan: StrengthWorkoutPlan? = null

        composeRule.setThemedContent {
            StrengthPlanSaveBottomSheet(
                plans = listOf(savingPlan, waitingPlan),
                selectedDate = LocalDate.of(2026, 7, 1),
                savingPlanId = savingPlan.id,
                message = "저장 중",
                error = null,
                onDismiss = {},
                onDateSelected = {},
                onPlanSelected = { selectedPlan = it }
            )
        }

        composeRule.waitForIdle()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanSaveDate)
            .assertIsNotEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthPlanSaveRow(waitingPlan.id))
            .assertIsNotEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(null, selectedPlan)
        }
    }
}

private fun trainingCalendarItem(
    id: String,
    type: String,
    date: LocalDate,
    isPlan: Boolean,
): TrainingItem {
    return TrainingItem(
        id = id,
        remoteId = id,
        externalId = null,
        name = type,
        type = type,
        date = date,
        startedAt = null,
        timeLabel = if (isPlan) "Plan" else "08:00",
        durationSeconds = null,
        distanceMeters = null,
        weightLiftedKg = null,
        load = null,
        fitness = null,
        fatigue = null,
        form = null,
        description = null,
        blocks = emptyList<PlanBlock>(),
        isPlan = isPlan
    )
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.setThemedContent(
    content: @Composable () -> Unit,
) {
    setContent {
        IntervalsGymTheme(content = content)
    }
}

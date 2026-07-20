package com.lighthousepark.intervalsgym.training.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import com.lighthousepark.intervalsgym.training.RoutineBlock
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
    fun weeklySummary_attachesToToolbarAndUsesFullWidth() {
        composeRule.setThemedContent {
            Box(modifier = Modifier.fillMaxSize()) {
                TrainingCalendarFloatingHeader(
                    headerOffsetPx = 0f,
                    onHeaderHeightChanged = {},
                    header = {
                        WeekSummary(
                            activities = emptyList(),
                            routines = emptyList(),
                            attachedToToolbar = true
                        )
                    }
                )
            }
        }

        val rootBounds = composeRule.onRoot().fetchSemanticsNode().boundsInRoot
        val summaryBounds = composeRule
            .onNodeWithContentDescription(TestContentDescriptions.TrainingCalendarWeekSummary)
            .fetchSemanticsNode()
            .boundsInRoot

        assertEquals(rootBounds.left, summaryBounds.left, 0.5f)
        assertEquals(rootBounds.top, summaryBounds.top, 0.5f)
        assertEquals(rootBounds.right, summaryBounds.right, 0.5f)
    }

    @Test
    fun weeklyFabMenu_invokesExpandedActionCallbacks() {
        var expandedChangedTo: Boolean? = null
        var workoutClicked = false
        var planClicked = false
        var routineClicked = false

        composeRule.setThemedContent {
            Box {
                WeeklyTrainingFabMenu(
                    expanded = true,
                    onExpandedChange = { expandedChangedTo = it },
                    onWorkoutClick = { workoutClicked = true },
                    onPlanAddClick = { planClicked = true },
                    onRoutineSaveClick = { routineClicked = true }
                )
            }
        }

        composeRule.waitForIdle()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.trainingCalendarFabAction("운동 실행"))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.trainingCalendarFabAction("계획 추가"))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.trainingCalendarFabAction("Routine 관리"))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.TrainingCalendarFabMenu)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(workoutClicked)
            assertTrue(planClicked)
            assertTrue(routineClicked)
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
    fun weeklyTrainingScreen_planAddFabActionOpensRoutineAddSheet() {
        val routine = defaultStrengthRoutines().first().copy(id = 66, name = "저녁 웨이트")

        composeRule.setThemedContent {
            WeeklyTrainingScreen(
                apiKey = "",
                strengthRoutines = listOf(routine),
                deletedCalendarRoutineIds = emptySet(),
                initialDate = LocalDate.of(2026, 7, 8),
                showCalendarModeButton = false,
                onRoutineSelected = {},
                onIntervalStrengthRoutineSelected = { _, _ -> },
                onManageRoutines = {},
                onStrengthSession = {},
                onRunningSession = {},
                onLoginClick = {},
                onLogout = {}
            )
        }

        composeRule.waitForIdle()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.TrainingCalendarFabMenu)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.trainingCalendarFabAction("계획 추가"))
            .performClick()

        composeRule.onNodeWithText("Routine 추가").assertExists()
        composeRule.onNodeWithText("저녁 웨이트").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineSaveTime)
            .assertDoesNotExist()
    }

    @Test
    fun weeklyTrainingScreen_settingsLoginActionInvokesLoginWhenConfigured() {
        var loginClicked = false
        var logoutClicked = false

        composeRule.setThemedContent {
            WeeklyTrainingScreen(
                apiKey = "",
                strengthRoutines = emptyList(),
                deletedCalendarRoutineIds = emptySet(),
                initialDate = LocalDate.of(2026, 7, 1),
                showCalendarModeButton = false,
                onRoutineSelected = {},
                onIntervalStrengthRoutineSelected = { _, _ -> },
                onManageRoutines = {},
                onStrengthSession = {},
                onRunningSession = {},
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
                strengthRoutines = emptyList(),
                deletedCalendarRoutineIds = emptySet(),
                initialDate = LocalDate.of(2026, 7, 1),
                showCalendarModeButton = false,
                onRoutineSelected = {},
                onIntervalStrengthRoutineSelected = { _, _ -> },
                onManageRoutines = {},
                onStrengthSession = {},
                onRunningSession = {},
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
                strengthRoutines = emptyList(),
                deletedCalendarRoutineIds = emptySet(),
                initialDate = LocalDate.of(2026, 7, 1),
                showCalendarModeButton = false,
                onRoutineSelected = {},
                onIntervalStrengthRoutineSelected = { _, _ -> },
                onManageRoutines = {},
                onStrengthSession = {},
                onRunningSession = {},
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
                strengthRoutines = emptyList(),
                deletedCalendarRoutineIds = emptySet(),
                initialDate = LocalDate.of(2026, 7, 1),
                showCalendarModeButton = false,
                onRoutineSelected = {},
                onIntervalStrengthRoutineSelected = { _, _ -> },
                onManageRoutines = {},
                onStrengthSession = {},
                onRunningSession = {},
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
                strengthRoutines = emptyList(),
                deletedCalendarRoutineIds = emptySet(),
                initialDate = LocalDate.of(2026, 7, 1),
                showBackButton = true,
                showCalendarModeButton = false,
                onRoutineSelected = {},
                onIntervalStrengthRoutineSelected = { _, _ -> },
                onManageRoutines = {},
                onStrengthSession = {},
                onRunningSession = {},
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
                strengthRoutines = emptyList(),
                deletedCalendarRoutineIds = emptySet(),
                initialDate = LocalDate.of(2026, 7, 1),
                showCalendarModeButton = true,
                onRoutineSelected = {},
                onIntervalStrengthRoutineSelected = { _, _ -> },
                onManageRoutines = {},
                onStrengthSession = {},
                onRunningSession = {},
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
                onRoutineSelected = {},
                onIntervalStrengthRoutineSelected = { _, _ -> },
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
        val item = trainingCalendarItem(id = "run-1", type = "Run", date = day, isRoutine = false)
        var selectedItem: TrainingItem? = null
        var strengthRoutineSelected = false

        composeRule.setThemedContent {
            MonthlyCalendarDayCell(
                day = day,
                isInCurrentMonth = true,
                items = listOf(item),
                visibleItemCount = 2,
                modifier = Modifier.size(width = 120.dp, height = 92.dp),
                onRoutineSelected = { selectedItem = it },
                onIntervalStrengthRoutineSelected = { _, _ -> strengthRoutineSelected = true },
                onDaySelected = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.monthlyCalendarItem(item.id))
            .performClick()

        composeRule.runOnIdle {
            assertSame(item, selectedItem)
            assertFalse(strengthRoutineSelected)
        }
    }

    @Test
    fun monthlyCalendarDayCell_routesStrengthRoutineChipToStrengthCallback() {
        val day = LocalDate.of(2026, 7, 2)
        val routine = StrengthWorkoutRoutine(id = 7, name = "Upper", entries = emptyList())
        val item = trainingCalendarItem(
            id = "strength-routine-1",
            type = "WeightTraining",
            date = day,
            isRoutine = true
        ).copy(matchedStrengthRoutine = routine)
        var selectedItem: TrainingItem? = null
        var selectedRoutine: StrengthWorkoutRoutine? = null
        var genericRoutineSelected = false

        composeRule.setThemedContent {
            MonthlyCalendarDayCell(
                day = day,
                isInCurrentMonth = true,
                items = listOf(item),
                visibleItemCount = 2,
                modifier = Modifier.size(width = 120.dp, height = 92.dp),
                onRoutineSelected = { genericRoutineSelected = true },
                onIntervalStrengthRoutineSelected = { calendarItem, strengthRoutine ->
                    selectedItem = calendarItem
                    selectedRoutine = strengthRoutine
                },
                onDaySelected = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.monthlyCalendarItem(item.id))
            .performClick()

        composeRule.runOnIdle {
            assertFalse(genericRoutineSelected)
            assertSame(item, selectedItem)
            assertSame(routine, selectedRoutine)
        }
    }

    @Test
    fun strengthRoutineSaveRow_invokesRoutineSelection() {
        val routine = defaultStrengthRoutines().first().copy(id = 44, name = "Save Row Routine")
        var selectedRoutine: StrengthWorkoutRoutine? = null

        composeRule.setThemedContent {
            StrengthRoutineSaveRow(
                routine = routine,
                isSaving = false,
                enabled = true,
                onClick = { selectedRoutine = routine }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthRoutineSaveRow(routine.id))
            .performClick()

        composeRule.runOnIdle {
            assertSame(routine, selectedRoutine)
        }
    }

    @Test
    fun strengthRoutineSaveBottomSheet_dateButtonOpensDatePicker() {
        val routine = defaultStrengthRoutines().first().copy(id = 45, name = "Date Picker Routine")

        composeRule.setThemedContent {
            StrengthRoutineSaveBottomSheet(
                routines = listOf(routine),
                selectedDate = LocalDate.of(2026, 7, 1),
                savingRoutineId = null,
                message = null,
                error = null,
                onDismiss = {},
                onDateSelected = {},
                onRoutineSelected = {}
            )
        }

        composeRule.waitForIdle()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineSaveDate)
            .assertIsEnabled()
            .performClick()

        composeRule.onNodeWithText("변경").assertExists()
        composeRule.onNodeWithText("취소").assertExists()
    }

    @Test
    fun strengthRoutineSaveBottomSheet_omitsTimeAndKeepsRoutineSelectable() {
        val routine = defaultStrengthRoutines().first().copy(id = 48, name = "Routine")
        var selectedRoutine: StrengthWorkoutRoutine? = null

        composeRule.setThemedContent {
            StrengthRoutineSaveBottomSheet(
                routines = listOf(routine),
                selectedDate = LocalDate.of(2026, 7, 1),
                savingRoutineId = null,
                message = null,
                error = null,
                onDismiss = {},
                onDateSelected = {},
                onRoutineSelected = { selectedRoutine = it }
            )
        }

        composeRule.waitForIdle()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineSaveTime)
            .assertDoesNotExist()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthRoutineSaveRow(routine.id))
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertSame(routine, selectedRoutine)
        }
    }

    @Test
    fun strengthRoutineSaveBottomSheet_disablesDateAndRowsWhileSaving() {
        val savingRoutine = defaultStrengthRoutines().first().copy(id = 46, name = "Saving Routine")
        val waitingRoutine = defaultStrengthRoutines().last().copy(id = 47, name = "Waiting Routine")
        var selectedRoutine: StrengthWorkoutRoutine? = null

        composeRule.setThemedContent {
            StrengthRoutineSaveBottomSheet(
                routines = listOf(savingRoutine, waitingRoutine),
                selectedDate = LocalDate.of(2026, 7, 1),
                savingRoutineId = savingRoutine.id,
                message = "저장 중",
                error = null,
                onDismiss = {},
                onDateSelected = {},
                onRoutineSelected = { selectedRoutine = it }
            )
        }

        composeRule.waitForIdle()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineSaveDate)
            .assertIsNotEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthRoutineSaveRow(waitingRoutine.id))
            .assertIsNotEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(null, selectedRoutine)
        }
    }
}

private fun trainingCalendarItem(
    id: String,
    type: String,
    date: LocalDate,
    isRoutine: Boolean,
): TrainingItem {
    return TrainingItem(
        id = id,
        remoteId = id,
        externalId = null,
        name = type,
        type = type,
        date = date,
        startedAt = null,
        timeLabel = if (isRoutine) "Routine" else "08:00",
        durationSeconds = null,
        distanceMeters = null,
        weightLiftedKg = null,
        load = null,
        fitness = null,
        fatigue = null,
        form = null,
        description = null,
        blocks = emptyList<RoutineBlock>(),
        isRoutine = isRoutine
    )
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.setThemedContent(
    content: @Composable () -> Unit,
) {
    setContent {
        IntervalsGymTheme(content = content)
    }
}

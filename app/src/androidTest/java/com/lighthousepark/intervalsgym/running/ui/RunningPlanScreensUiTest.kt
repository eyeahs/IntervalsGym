package com.lighthousepark.intervalsgym.running.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lighthousepark.intervalsgym.app.PREFS_NAME
import com.lighthousepark.intervalsgym.app.SAVED_RUNNING_PLANS_PREF
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.data.loadSavedRunningWorkoutPlans
import com.lighthousepark.intervalsgym.data.upsertSavedRunningWorkoutPlan
import com.lighthousepark.intervalsgym.running.SavedRunningWorkoutPlan
import com.lighthousepark.intervalsgym.training.PlanBlock
import com.lighthousepark.intervalsgym.ui.theme.IntervalsGymTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RunningPlanScreensUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val prefs by lazy {
        InstrumentationRegistry.getInstrumentation()
            .targetContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    @Before
    fun clearSavedPlans() {
        prefs.edit().remove(SAVED_RUNNING_PLANS_PREF).commit()
    }

    @Test
    fun planList_selectsSavedPlanAndOpensManagement() {
        val plan = savedRunningPlan()
        upsertSavedRunningWorkoutPlan(prefs, plan)
        var selectedPlan: SavedRunningWorkoutPlan? = null
        var manageClicked = false

        composeRule.setThemedContent {
            RunningPlanListScreen(
                onPlanSelected = { selectedPlan = it },
                onManagePlans = { manageClicked = true },
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.runningSavedPlan(plan.id))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningPlanListManage)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(plan.id, selectedPlan?.id)
            assertTrue(manageClicked)
        }
    }

    @Test
    fun planList_emptyStateExposesManageAction() {
        var manageClicked = false

        composeRule.setThemedContent {
            RunningPlanListScreen(
                onPlanSelected = {},
                onManagePlans = { manageClicked = true },
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningPlanListEmpty)
            .assertExists()
        composeRule
            .onNodeWithText("저장된 러닝 Plan이 없습니다. Intervals.icu plan 상세에서 먼저 저장하세요.")
            .assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningPlanListManage)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(manageClicked)
        }
    }

    @Test
    fun planList_backButtonInvokesBackCallback() {
        var backClicks = 0

        composeRule.setThemedContent {
            RunningPlanListScreen(
                onPlanSelected = {},
                onManagePlans = {},
                onBack = { backClicks += 1 }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningPlanListBack)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, backClicks)
        }
    }

    @Test
    fun planManagement_emptyStateIsAccessible() {
        composeRule.setThemedContent {
            RunningPlanManagementScreen(onBack = {})
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningPlanManagementEmpty)
            .assertExists()
        composeRule
            .onNodeWithText("저장된 러닝 Plan이 없습니다.")
            .assertExists()
    }

    @Test
    fun planManagement_deletesSavedPlanAfterConfirmation() {
        val plan = savedRunningPlan()
        upsertSavedRunningWorkoutPlan(prefs, plan)

        composeRule.setThemedContent {
            RunningPlanManagementScreen(onBack = {})
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.runningSavedPlan(plan.id))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningPlanDelete)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningPlanConfirmDelete)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(loadSavedRunningWorkoutPlans(prefs).isEmpty())
        }
    }

    @Test
    fun planManagement_cancelDeleteKeepsSavedPlan() {
        val plan = savedRunningPlan()
        upsertSavedRunningWorkoutPlan(prefs, plan)

        composeRule.setThemedContent {
            RunningPlanManagementScreen(onBack = {})
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.runningSavedPlan(plan.id))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningPlanDelete)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningPlanCancelDelete)
            .performClick()

        composeRule.runOnIdle {
            val plans = loadSavedRunningWorkoutPlans(prefs)
            assertEquals(1, plans.size)
            assertEquals(plan.id, plans.single().id)
        }
    }

    @Test
    fun planManagement_backFromDetailReturnsToListThenInvokesBack() {
        val plan = savedRunningPlan()
        upsertSavedRunningWorkoutPlan(prefs, plan)
        var backClicks = 0

        composeRule.setThemedContent {
            RunningPlanManagementScreen(onBack = { backClicks += 1 })
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.runningSavedPlan(plan.id))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningPlanDelete)
            .assertExists()

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningPlanManagementBack)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningPlanDelete)
            .assertDoesNotExist()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.runningSavedPlan(plan.id))
            .assertExists()

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningPlanManagementBack)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, backClicks)
        }
    }
}

private fun savedRunningPlan(): SavedRunningWorkoutPlan {
    return SavedRunningWorkoutPlan(
        id = "saved-running-ui-test",
        name = "UI 러닝 Plan",
        description = "1m 10:00 pace [6km/h 1%]",
        durationSeconds = 60,
        blocks = listOf(
            PlanBlock(
                index = 0,
                title = "Block 1",
                kind = "work",
                targetText = "6km/h · 1%",
                durationSeconds = 60,
                startSecond = 0,
                endSecond = 60,
                isRecovery = false
            )
        ),
        workoutDocJson = null,
        savedAtMillis = 1_800_000L
    )
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.setThemedContent(
    content: @Composable () -> Unit,
) {
    setContent {
        IntervalsGymTheme(content = content)
    }
}

package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutPlan
import com.lighthousepark.intervalsgym.strength.defaultStrengthPlans
import com.lighthousepark.intervalsgym.ui.theme.IntervalsGymTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StrengthPlanScreensUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun planList_exposesSelectStartAndManageActions() {
        val plan = defaultStrengthPlans().first()
        var selectedPlan: StrengthWorkoutPlan? = null
        var startedPlan: StrengthWorkoutPlan? = null
        var manageClicked = false

        composeRule.setThemedContent {
            StrengthPlanListScreen(
                plans = listOf(plan),
                onPlanSelected = { selectedPlan = it },
                onStartPlan = { startedPlan = it },
                onManagePlans = { manageClicked = true },
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthPlanListRow(plan.id))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthPlanListStart(plan.id))
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanListManage)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(plan.id, selectedPlan?.id)
            assertEquals(plan.id, startedPlan?.id)
            assertTrue(manageClicked)
        }
    }

    @Test
    fun planList_emptyStateStillAllowsManagement() {
        var manageClicked = false

        composeRule.setThemedContent {
            StrengthPlanListScreen(
                plans = emptyList(),
                onPlanSelected = {},
                onStartPlan = {},
                onManagePlans = { manageClicked = true },
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanListEmpty)
            .assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanListManage)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(manageClicked)
        }
    }

    @Test
    fun planList_backButtonInvokesBackCallback() {
        var backClicks = 0

        composeRule.setThemedContent {
            StrengthPlanListScreen(
                plans = emptyList(),
                onPlanSelected = {},
                onStartPlan = {},
                onManagePlans = {},
                onBack = { backClicks += 1 }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanListBack)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, backClicks)
        }
    }

    @Test
    fun planManagement_exposesAddAndEditActions() {
        val plan = defaultStrengthPlans().first()
        var addClicked = false
        var editedPlan: StrengthWorkoutPlan? = null

        composeRule.setThemedContent {
            StrengthPlanManagementScreen(
                plans = listOf(plan),
                onAddPlan = { addClicked = true },
                onEditPlan = { editedPlan = it },
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanManagementAdd)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthPlanManagementEdit(plan.id))
            .performClick()

        composeRule.runOnIdle {
            assertTrue(addClicked)
            assertEquals(plan.id, editedPlan?.id)
        }
    }

    @Test
    fun planManagement_emptyStateStillAllowsAddPlan() {
        var addClicked = false

        composeRule.setThemedContent {
            StrengthPlanManagementScreen(
                plans = emptyList(),
                onAddPlan = { addClicked = true },
                onEditPlan = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanManagementEmpty)
            .assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanManagementAdd)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(addClicked)
        }
    }

    @Test
    fun planManagement_backButtonInvokesBackCallback() {
        var backClicks = 0

        composeRule.setThemedContent {
            StrengthPlanManagementScreen(
                plans = emptyList(),
                onAddPlan = {},
                onEditPlan = {},
                onBack = { backClicks += 1 }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanManagementBack)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, backClicks)
        }
    }
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.setThemedContent(
    content: @Composable () -> Unit,
) {
    setContent {
        IntervalsGymTheme(content = content)
    }
}

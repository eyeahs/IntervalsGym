package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import com.lighthousepark.intervalsgym.ui.theme.IntervalsGymTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StrengthRoutineScreensUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun routineList_exposesSelectStartAndManageActions() {
        val routine = defaultStrengthRoutines().first()
        var selectedRoutine: StrengthWorkoutRoutine? = null
        var startedRoutine: StrengthWorkoutRoutine? = null
        var manageClicked = false

        composeRule.setThemedContent {
            StrengthRoutineListScreen(
                routines = listOf(routine),
                onRoutineSelected = { selectedRoutine = it },
                onStartRoutine = { startedRoutine = it },
                onManageRoutines = { manageClicked = true },
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthRoutineListRow(routine.id))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthRoutineListStart(routine.id))
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineListManage)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(routine.id, selectedRoutine?.id)
            assertEquals(routine.id, startedRoutine?.id)
            assertTrue(manageClicked)
        }
    }

    @Test
    fun routineList_emptyStateStillAllowsManagement() {
        var manageClicked = false

        composeRule.setThemedContent {
            StrengthRoutineListScreen(
                routines = emptyList(),
                onRoutineSelected = {},
                onStartRoutine = {},
                onManageRoutines = { manageClicked = true },
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineListEmpty)
            .assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineListManage)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(manageClicked)
        }
    }

    @Test
    fun routineList_backButtonInvokesBackCallback() {
        var backClicks = 0

        composeRule.setThemedContent {
            StrengthRoutineListScreen(
                routines = emptyList(),
                onRoutineSelected = {},
                onStartRoutine = {},
                onManageRoutines = {},
                onBack = { backClicks += 1 }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineListBack)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, backClicks)
        }
    }

    @Test
    fun routineManagement_exposesAddAndEditActions() {
        val routine = defaultStrengthRoutines().first()
        var addClicked = false
        var editedRoutine: StrengthWorkoutRoutine? = null
        var clonedRoutine: StrengthWorkoutRoutine? = null

        composeRule.setThemedContent {
            StrengthRoutineManagementScreen(
                routines = listOf(routine),
                onAddRoutine = { addClicked = true },
                onEditRoutine = { editedRoutine = it },
                onCloneRoutine = { clonedRoutine = it },
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineManagementAdd)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthRoutineManagementEdit(routine.id))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthRoutineManagementClone(routine.id))
            .performClick()

        composeRule.runOnIdle {
            assertTrue(addClicked)
            assertEquals(routine.id, editedRoutine?.id)
            assertEquals(routine.id, clonedRoutine?.id)
        }
    }

    @Test
    fun routineManagement_emptyStateStillAllowsAddRoutine() {
        var addClicked = false

        composeRule.setThemedContent {
            StrengthRoutineManagementScreen(
                routines = emptyList(),
                onAddRoutine = { addClicked = true },
                onEditRoutine = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineManagementEmpty)
            .assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineManagementAdd)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(addClicked)
        }
    }

    @Test
    fun routineManagement_backButtonInvokesBackCallback() {
        var backClicks = 0

        composeRule.setThemedContent {
            StrengthRoutineManagementScreen(
                routines = emptyList(),
                onAddRoutine = {},
                onEditRoutine = {},
                onBack = { backClicks += 1 }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineManagementBack)
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

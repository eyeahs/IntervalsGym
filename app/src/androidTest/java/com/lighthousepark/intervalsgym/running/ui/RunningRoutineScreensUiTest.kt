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
import com.lighthousepark.intervalsgym.app.SAVED_RUNNING_ROUTINES_PREF
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.data.loadSavedRunningWorkoutRoutines
import com.lighthousepark.intervalsgym.data.upsertSavedRunningWorkoutRoutine
import com.lighthousepark.intervalsgym.running.SavedRunningWorkoutRoutine
import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.ui.theme.IntervalsGymTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RunningRoutineScreensUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val prefs by lazy {
        InstrumentationRegistry.getInstrumentation()
            .targetContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    @Before
    fun clearSavedRoutines() {
        prefs.edit().remove(SAVED_RUNNING_ROUTINES_PREF).commit()
    }

    @Test
    fun routineList_selectsSavedRoutineAndOpensManagement() {
        val routine = savedRunningRoutine()
        upsertSavedRunningWorkoutRoutine(prefs, routine)
        var selectedRoutine: SavedRunningWorkoutRoutine? = null
        var manageClicked = false

        composeRule.setThemedContent {
            RunningRoutineListScreen(
                onRoutineSelected = { selectedRoutine = it },
                onManageRoutines = { manageClicked = true },
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.runningSavedRoutine(routine.id))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningRoutineListManage)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(routine.id, selectedRoutine?.id)
            assertTrue(manageClicked)
        }
    }

    @Test
    fun routineList_emptyStateExposesManageAction() {
        var manageClicked = false

        composeRule.setThemedContent {
            RunningRoutineListScreen(
                onRoutineSelected = {},
                onManageRoutines = { manageClicked = true },
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningRoutineListEmpty)
            .assertExists()
        composeRule
            .onNodeWithText("저장된 러닝 Routine이 없습니다. Intervals.icu Routine 상세에서 먼저 저장하세요.")
            .assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningRoutineListManage)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(manageClicked)
        }
    }

    @Test
    fun routineList_backButtonInvokesBackCallback() {
        var backClicks = 0

        composeRule.setThemedContent {
            RunningRoutineListScreen(
                onRoutineSelected = {},
                onManageRoutines = {},
                onBack = { backClicks += 1 }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningRoutineListBack)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, backClicks)
        }
    }

    @Test
    fun routineManagement_emptyStateIsAccessible() {
        composeRule.setThemedContent {
            RunningRoutineManagementScreen(onBack = {})
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningRoutineManagementEmpty)
            .assertExists()
        composeRule
            .onNodeWithText("저장된 러닝 Routine이 없습니다.")
            .assertExists()
    }

    @Test
    fun routineManagement_deletesSavedRoutineAfterConfirmation() {
        val routine = savedRunningRoutine()
        upsertSavedRunningWorkoutRoutine(prefs, routine)

        composeRule.setThemedContent {
            RunningRoutineManagementScreen(onBack = {})
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.runningSavedRoutine(routine.id))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningRoutineDelete)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningRoutineConfirmDelete)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(loadSavedRunningWorkoutRoutines(prefs).isEmpty())
        }
    }

    @Test
    fun routineManagement_cancelDeleteKeepsSavedRoutine() {
        val routine = savedRunningRoutine()
        upsertSavedRunningWorkoutRoutine(prefs, routine)

        composeRule.setThemedContent {
            RunningRoutineManagementScreen(onBack = {})
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.runningSavedRoutine(routine.id))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningRoutineDelete)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningRoutineCancelDelete)
            .performClick()

        composeRule.runOnIdle {
            val routines = loadSavedRunningWorkoutRoutines(prefs)
            assertEquals(1, routines.size)
            assertEquals(routine.id, routines.single().id)
        }
    }

    @Test
    fun routineManagement_backFromDetailReturnsToListThenInvokesBack() {
        val routine = savedRunningRoutine()
        upsertSavedRunningWorkoutRoutine(prefs, routine)
        var backClicks = 0

        composeRule.setThemedContent {
            RunningRoutineManagementScreen(onBack = { backClicks += 1 })
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.runningSavedRoutine(routine.id))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningRoutineDelete)
            .assertExists()

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningRoutineManagementBack)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningRoutineDelete)
            .assertDoesNotExist()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.runningSavedRoutine(routine.id))
            .assertExists()

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.RunningRoutineManagementBack)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, backClicks)
        }
    }
}

private fun savedRunningRoutine(): SavedRunningWorkoutRoutine {
    return SavedRunningWorkoutRoutine(
        id = "saved-running-ui-test",
        name = "UI 러닝 Routine",
        description = "1m 10:00 pace [6km/h 1%]",
        durationSeconds = 60,
        blocks = listOf(
            RoutineBlock(
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

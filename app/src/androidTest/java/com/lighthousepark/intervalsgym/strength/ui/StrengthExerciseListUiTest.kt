package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.strength.StrengthExercise
import com.lighthousepark.intervalsgym.ui.theme.IntervalsGymTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StrengthExerciseListUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun exerciseList_searchShowsMatchingExercisesWithoutSetEmptyView() {
        var selectedExercise: StrengthExercise? = null

        composeRule.setThemedContent {
            StrengthExerciseListScreen(
                onAddCustomExercise = {},
                onExerciseSelected = { exercise, _ -> selectedExercise = exercise }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseSearch)
            .performTextInput("레그컬")

        composeRule.onNodeWithText("수행할 세트가 없습니다.").assertDoesNotExist()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthExerciseSearchResult("leg_curl"))
            .assertExists()
            .performClick()

        composeRule.runOnIdle {
            assertEquals("leg_curl", selectedExercise?.id)
        }
    }

    @Test
    fun exerciseList_createExerciseButtonInvokesCallback() {
        var createClicked = false

        composeRule.setThemedContent {
            StrengthExerciseListScreen(
                onAddCustomExercise = { createClicked = true },
                onExerciseSelected = { _, _ -> }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthCreateExercise)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(createClicked)
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

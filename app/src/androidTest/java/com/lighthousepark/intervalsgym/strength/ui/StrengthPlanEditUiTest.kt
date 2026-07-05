package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.strength.StrengthPlanEntry
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutPlan
import com.lighthousepark.intervalsgym.strength.defaultStrengthPlanEntry
import com.lighthousepark.intervalsgym.strength.strengthExerciseCatalog
import com.lighthousepark.intervalsgym.ui.theme.IntervalsGymTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StrengthPlanEditUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun editBottomBar_exposesAllPrimaryActions() {
        var groupClicked = false
        var addClicked = false
        var saveClicked = false
        var deleteClicked = false

        composeRule.setThemedContent {
            StrengthPlanEditBottomBar(
                canGroupSuperset = true,
                canSave = true,
                showDelete = true,
                onGroupSuperset = { groupClicked = true },
                onAddExercise = { addClicked = true },
                onSave = { saveClicked = true },
                onDelete = { deleteClicked = true }
            )
        }

        composeRule.onNodeWithContentDescription(TestContentDescriptions.StrengthPlanEditGroupSuperset)
            .assertIsEnabled()
            .performClick()
        composeRule.onNodeWithContentDescription(TestContentDescriptions.StrengthPlanEditAddExercise)
            .performClick()
        composeRule.onNodeWithContentDescription(TestContentDescriptions.StrengthPlanEditSave)
            .assertIsEnabled()
            .performClick()
        composeRule.onNodeWithContentDescription(TestContentDescriptions.StrengthPlanEditDelete)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(groupClicked)
            assertTrue(addClicked)
            assertTrue(saveClicked)
            assertTrue(deleteClicked)
        }
    }

    @Test
    fun planDeleteDialog_confirmInvokesDeleteCallback() {
        val plan = editTestPlan()
        var deletedPlan: StrengthWorkoutPlan? = null
        var backClicked = false

        composeRule.setThemedContent {
            StrengthPlanEditScreen(
                plan = plan,
                onSave = {},
                onDelete = { deletedPlan = it },
                onBack = { backClicked = true }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanEditDelete)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanEditCancelDelete)
            .assertIsEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanEditConfirmDelete)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanEditConfirmDelete)
            .assertDoesNotExist()

        composeRule.runOnIdle {
            assertEquals(plan, deletedPlan)
            assertTrue(!backClicked)
        }
    }

    @Test
    fun planDeleteDialog_cancelKeepsPlan() {
        val plan = editTestPlan()
        var deleteCount = 0

        composeRule.setThemedContent {
            StrengthPlanEditScreen(
                plan = plan,
                onSave = {},
                onDelete = { deleteCount += 1 },
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanEditDelete)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanEditCancelDelete)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanEditConfirmDelete)
            .assertDoesNotExist()

        composeRule.runOnIdle {
            assertEquals(0, deleteCount)
        }
    }

    @Test
    fun unsavedBackDialog_cancelsSavesAndDiscardsChanges() {
        val plan = editTestPlan()
        var savedPlan: StrengthWorkoutPlan? = null
        var backCount = 0

        composeRule.setThemedContent {
            StrengthPlanEditScreen(
                plan = plan,
                onSave = { savedPlan = it },
                onDelete = {},
                onBack = { backCount += 1 }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanEditName)
            .performTextClearance()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanEditName)
            .performTextInput("Updated Plan")
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanEditBack)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanEditCancelUnsaved)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanEditSaveUnsaved)
            .assertDoesNotExist()

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanEditBack)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanEditSaveUnsaved)
            .assertIsEnabled()
            .performClick()

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanEditBack)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthPlanEditDiscardUnsaved)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertEquals("Updated Plan", savedPlan?.name)
            assertEquals(1, backCount)
        }
    }

    @Test
    fun supersetEditPanel_exposesConfirmClearAndCancelActions() {
        var grouped = false
        var cleared = false
        var cancelled = false

        composeRule.setThemedContent {
            SupersetEditPanel(
                isSelectionMode = true,
                selectedCount = 2,
                canClearSelectedGroups = true,
                onGroupSelected = { grouped = true },
                onClearSelectedGroups = { cleared = true },
                onCancel = { cancelled = true }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthConfirmSuperset)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthClearSuperset)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthCancelSuperset)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertTrue(grouped)
            assertTrue(cleared)
            assertTrue(cancelled)
        }
    }

    @Test
    fun supersetEditPanel_disablesUnavailableActions() {
        composeRule.setThemedContent {
            SupersetEditPanel(
                isSelectionMode = true,
                selectedCount = 1,
                canClearSelectedGroups = false,
                onGroupSelected = {},
                onClearSelectedGroups = {},
                onCancel = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthConfirmSuperset)
            .assertIsNotEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthClearSuperset)
            .assertIsNotEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthCancelSuperset)
            .assertIsEnabled()
    }

    @Test
    fun exerciseDetailEditor_addsSetAndOpensExercisePicker() {
        var entryState by mutableStateOf(editTestEntry())
        var changingExercise = false
        var addExerciseClicked = false
        var deleteClicked = false

        composeRule.setThemedContent {
            StrengthExerciseDetailEditor(
                entry = entryState,
                isChangingExercise = changingExercise,
                onEntryChange = { entryState = it },
                onChangingExerciseChange = { changingExercise = it },
                onAddExercise = { addExerciseClicked = true },
                onDelete = { deleteClicked = true }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseDetailAddSet)
            .performScrollTo()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseDetailAddExercise)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseDetailDeleteExercise)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseDetailChangeExercise)
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(4, entryState.records.size)
            assertTrue(changingExercise)
            assertTrue(addExerciseClicked)
            assertTrue(deleteClicked)
        }
    }

    @Test
    fun exerciseDetailEditor_changeTypeCancelKeepsEntryUnchanged() {
        val originalEntry = editTestEntry()
        var entryState by mutableStateOf(originalEntry)

        composeRule.setThemedContent {
            StrengthExerciseDetailEditor(
                entry = entryState,
                isChangingExercise = false,
                onEntryChange = { entryState = it },
                onChangingExerciseChange = {},
                onAddExercise = {},
                onDelete = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseDetailChangeType)
            .performScrollTo()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseConfigCancel)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseConfigCancel)
            .assertDoesNotExist()

        composeRule.runOnIdle {
            assertEquals(originalEntry.equipment, entryState.equipment)
            assertEquals(originalEntry.variation, entryState.variation)
        }
    }

    @Test
    fun exerciseTypeDialog_completesSelectedEquipmentVariationAndUnilateral() {
        val entry = editTestEntry()
        var result: Pair<String, String>? = null

        composeRule.setThemedContent {
            StrengthExerciseTypeDialog(
                entry = entry,
                exercise = entry.exercise,
                initialEquipment = "바벨",
                initialVariation = "백 스쿼트",
                onDismiss = {},
                onDone = { equipment, variation -> result = equipment to variation }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthChoiceOption("기구", "스미스"))
            .performScrollTo()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthChoiceOption("세부 타입", "프론트 스쿼트"))
            .performScrollTo()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthChoiceOption("좌우 방식", "한쪽"))
            .performScrollTo()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseConfigDone)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertEquals("스미스" to "한쪽 프론트 스쿼트", result)
        }
    }

    @Test
    fun exerciseConfigDialog_completesWithInferredSearchDefaults() {
        val chestFly = strengthExerciseCatalog.first { it.id == "chest_fly" }
        var result: Pair<String, String>? = null

        composeRule.setThemedContent {
            StrengthExerciseConfigDialog(
                exercise = chestFly,
                initialSearchQuery = "펙덱플라이 싱글",
                onDismiss = {},
                onDone = { equipment, variation -> result = equipment to variation }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseConfigDone)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertEquals("팩 덱 머신" to "한쪽", result)
        }
    }

    @Test
    fun customExerciseDialog_addsTrimmedNameAfterInput() {
        var addedName: String? = null

        composeRule.setThemedContent {
            CustomStrengthExerciseDialog(
                onDismiss = {},
                onAdd = { addedName = it }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthCustomExerciseAdd)
            .assertIsNotEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthCustomExerciseName)
            .performTextInput(" 케이블 풀오버 ")
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthCustomExerciseAdd)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertEquals("케이블 풀오버", addedName)
        }
    }

    @Test
    fun customExerciseDialog_cancelInvokesDismiss() {
        var dismissed = false

        composeRule.setThemedContent {
            CustomStrengthExerciseDialog(
                onDismiss = { dismissed = true },
                onAdd = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthCustomExerciseCancel)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(dismissed)
        }
    }

    @Test
    fun exerciseRow_clicksNormalCallback() {
        val entry = editTestEntry()
        var normalClicked = false

        composeRule.setThemedContent {
            StrengthPlanExerciseRow(
                entry = entry,
                supersetLabel = null,
                isSupersetSelectionMode = false,
                isSupersetSelected = false,
                isPendingDelete = false,
                isDragging = false,
                dragHandleModifier = androidx.compose.ui.Modifier,
                onClick = { normalClicked = true },
                onSupersetToggle = {},
                onDelete = {},
                onCommitDelete = {},
                onRestore = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthPlanExerciseRow(entry.id))
            .performClick()

        composeRule.runOnIdle {
            assertTrue(normalClicked)
        }
    }

    @Test
    fun exerciseRow_clicksSupersetSelectionCallback() {
        val entry = editTestEntry()
        var supersetToggled = false

        composeRule.setThemedContent {
            StrengthPlanExerciseRow(
                entry = entry,
                supersetLabel = null,
                isSupersetSelectionMode = true,
                isSupersetSelected = false,
                isPendingDelete = false,
                isDragging = false,
                dragHandleModifier = androidx.compose.ui.Modifier,
                onClick = {},
                onSupersetToggle = { supersetToggled = true },
                onDelete = {},
                onCommitDelete = {},
                onRestore = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthPlanExerciseRow(entry.id))
            .performClick()

        composeRule.runOnIdle {
            assertTrue(supersetToggled)
        }
    }

    @Test
    fun exerciseRow_pendingDeleteRestoresFromButtonAndRowClick() {
        val entry = editTestEntry()
        var restoreCount = 0
        var normalClicked = false

        composeRule.setThemedContent {
            StrengthPlanExerciseRow(
                entry = entry,
                supersetLabel = null,
                isSupersetSelectionMode = false,
                isSupersetSelected = false,
                isPendingDelete = true,
                isDragging = false,
                dragHandleModifier = androidx.compose.ui.Modifier,
                onClick = { normalClicked = true },
                onSupersetToggle = {},
                onDelete = {},
                onCommitDelete = {},
                onRestore = { restoreCount += 1 }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthPlanExerciseRestore(entry.id))
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthPlanExerciseRow(entry.id))
            .performClick()

        composeRule.runOnIdle {
            assertEquals(2, restoreCount)
            assertTrue(!normalClicked)
        }
    }
}

private fun editTestEntry(): StrengthPlanEntry {
    return defaultStrengthPlanEntry(
        id = 1,
        exercise = strengthExerciseCatalog.first { it.id == "squat" }
    )
}

private fun editTestPlan(): StrengthWorkoutPlan {
    return StrengthWorkoutPlan(
        id = 7,
        name = "Original Plan",
        entries = listOf(editTestEntry())
    )
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.setThemedContent(
    content: @Composable () -> Unit,
) {
    setContent {
        IntervalsGymTheme(content = content)
    }
}

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
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.strengthExerciseCatalog
import com.lighthousepark.intervalsgym.ui.theme.IntervalsGymTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StrengthRoutineEditUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun editBottomBar_exposesAllPrimaryActions() {
        var groupClicked = false
        var addClicked = false
        var saveClicked = false
        var deleteClicked = false

        composeRule.setThemedContent {
            StrengthRoutineEditBottomBar(
                canGroupSuperset = true,
                canSave = true,
                showDelete = true,
                onGroupSuperset = { groupClicked = true },
                onAddExercise = { addClicked = true },
                onSave = { saveClicked = true },
                onDelete = { deleteClicked = true }
            )
        }

        composeRule.onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditGroupSuperset)
            .assertIsEnabled()
            .performClick()
        composeRule.onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditAddExercise)
            .performClick()
        composeRule.onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditSave)
            .assertIsEnabled()
            .performClick()
        composeRule.onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditDelete)
            .performClick()

        composeRule.runOnIdle {
            assertTrue(groupClicked)
            assertTrue(addClicked)
            assertTrue(saveClicked)
            assertTrue(deleteClicked)
        }
    }

    @Test
    fun routineDeleteDialog_confirmInvokesDeleteCallback() {
        val routine = editTestRoutine()
        var deletedRoutine: StrengthWorkoutRoutine? = null
        var backClicked = false

        composeRule.setThemedContent {
            StrengthRoutineEditScreen(
                routine = routine,
                onSave = {},
                onDelete = { deletedRoutine = it },
                onBack = { backClicked = true }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditDelete)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditCancelDelete)
            .assertIsEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditConfirmDelete)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditConfirmDelete)
            .assertDoesNotExist()

        composeRule.runOnIdle {
            assertEquals(routine, deletedRoutine)
            assertTrue(!backClicked)
        }
    }

    @Test
    fun routineDeleteDialog_cancelKeepsRoutine() {
        val routine = editTestRoutine()
        var deleteCount = 0

        composeRule.setThemedContent {
            StrengthRoutineEditScreen(
                routine = routine,
                onSave = {},
                onDelete = { deleteCount += 1 },
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditDelete)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditCancelDelete)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditConfirmDelete)
            .assertDoesNotExist()

        composeRule.runOnIdle {
            assertEquals(0, deleteCount)
        }
    }

    @Test
    fun unsavedBackDialog_cancelsSavesAndDiscardsChanges() {
        val routine = editTestRoutine()
        var savedRoutine: StrengthWorkoutRoutine? = null
        var backCount = 0

        composeRule.setThemedContent {
            StrengthRoutineEditScreen(
                routine = routine,
                onSave = { savedRoutine = it },
                onDelete = {},
                onBack = { backCount += 1 }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditName)
            .performTextClearance()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditName)
            .performTextInput("Updated Routine")
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditBack)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditCancelUnsaved)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditSaveUnsaved)
            .assertDoesNotExist()

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditBack)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditSaveUnsaved)
            .assertIsEnabled()
            .performClick()

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditBack)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditDiscardUnsaved)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertEquals("Updated Routine", savedRoutine?.name)
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
            StrengthRoutineExerciseRow(
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
            .onNodeWithContentDescription(TestContentDescriptions.strengthRoutineExerciseRow(entry.id))
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
            StrengthRoutineExerciseRow(
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
            .onNodeWithContentDescription(TestContentDescriptions.strengthRoutineExerciseRow(entry.id))
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
            StrengthRoutineExerciseRow(
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
            .onNodeWithContentDescription(TestContentDescriptions.strengthRoutineExerciseRestore(entry.id))
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthRoutineExerciseRow(entry.id))
            .performClick()

        composeRule.runOnIdle {
            assertEquals(2, restoreCount)
            assertTrue(!normalClicked)
        }
    }
}

private fun editTestEntry(): StrengthRoutineEntry {
    return defaultStrengthRoutineEntry(
        id = 1,
        exercise = strengthExerciseCatalog.first { it.id == "squat" }
    )
}

private fun editTestRoutine(): StrengthWorkoutRoutine {
    return StrengthWorkoutRoutine(
        id = 7,
        name = "Original Routine",
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

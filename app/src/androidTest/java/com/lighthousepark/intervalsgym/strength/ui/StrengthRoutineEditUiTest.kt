package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.StrengthSetGroupType
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
    fun existingRoutine_historyButtonOpensMatchingHistory() {
        val routine = editTestRoutine()
        var historyRoutine: StrengthWorkoutRoutine? = null

        composeRule.setThemedContent {
            StrengthRoutineEditScreen(
                routine = routine,
                onSave = {},
                onDelete = {},
                onBack = {},
                onHistory = { historyRoutine = it }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditHistory)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(routine, historyRoutine)
        }
    }

    @Test
    fun newRoutine_doesNotShowHistoryButton() {
        composeRule.setThemedContent {
            StrengthRoutineEditScreen(
                routine = null,
                onSave = {},
                onDelete = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditHistory)
            .assertDoesNotExist()
    }

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

        composeRule.onNodeWithText("세트 관리").assertExists()
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
    fun existingRoutine_addsAndSavesChangedLocation() {
        val routine = editTestRoutine().copy(location = "기존 헬스장")
        var savedRoutine: StrengthWorkoutRoutine? = null

        composeRule.setThemedContent {
            StrengthRoutineEditScreen(
                routine = routine,
                onSave = { savedRoutine = it },
                onDelete = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditLocation)
            .performScrollTo()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditLocationPicker)
            .assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditAddLocation)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditLocationName)
            .performTextInput("UI 테스트 헬스장")
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditConfirmLocation)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditLocation)
            .assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditSave)
            .performClick()

        composeRule.runOnIdle {
            assertEquals("UI 테스트 헬스장", savedRoutine?.location)
        }
    }

    @Test
    fun existingRoutine_removesCurrentLocationFromPickerAndSavesUnspecified() {
        val location = "제거할 UI 테스트 헬스장"
        val routine = editTestRoutine().copy(location = location)
        var savedRoutine: StrengthWorkoutRoutine? = null

        composeRule.setThemedContent {
            StrengthRoutineEditScreen(
                routine = routine,
                onSave = { savedRoutine = it },
                onDelete = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditLocation)
            .performScrollTo()
            .performClick()
        composeRule
            .onNodeWithContentDescription(
                TestContentDescriptions.strengthRoutineEditRemoveLocation(location)
            )
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditLocationPicker)
            .assertExists()
        composeRule.onNodeWithText(location).assertDoesNotExist()
        composeRule.onNodeWithText("닫기").performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditSave)
            .performClick()

        composeRule.runOnIdle {
            assertEquals("", savedRoutine?.location)
        }
    }

    @Test
    fun newExerciseDetail_cancelAndSystemBackDiscardPendingExercise() {
        val routine = editTestRoutine()
        var savedRoutine: StrengthWorkoutRoutine? = null

        composeRule.setThemedContent {
            StrengthRoutineEditScreen(
                routine = routine,
                onSave = { savedRoutine = it },
                onDelete = {},
                onBack = {}
            )
        }

        composeRule.openNewExerciseDetail()
        composeRule.onNodeWithText("운동 추가").assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseDetailCancel)
            .assertIsEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseDetailSave)
            .assertIsEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseDetailDeleteExercise)
            .assertDoesNotExist()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseDetailCancel)
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditSave)
            .performClick()
        composeRule.runOnIdle {
            assertEquals(routine.entries, savedRoutine?.entries)
            savedRoutine = null
        }

        composeRule.openNewExerciseDetail()
        Espresso.pressBack()
        composeRule.waitForIdle()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseDetailSave)
            .assertDoesNotExist()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditSave)
            .performClick()
        composeRule.runOnIdle {
            assertEquals(routine.entries, savedRoutine?.entries)
        }
    }

    @Test
    fun newExerciseDetail_saveCommitsEditedExerciseToRoutine() {
        val routine = editTestRoutine()
        var savedRoutine: StrengthWorkoutRoutine? = null

        composeRule.setThemedContent {
            StrengthRoutineEditScreen(
                routine = routine,
                onSave = { savedRoutine = it },
                onDelete = {},
                onBack = {}
            )
        }

        composeRule.openNewExerciseDetail()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseDetailAddSet)
            .performScrollTo()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseDetailSave)
            .performClick()
        val addedEntryId = routine.entries.maxOf { it.id } + 1
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthRoutineExerciseRow(addedEntryId))
            .assertExists()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditSave)
            .performClick()

        composeRule.runOnIdle {
            val result = requireNotNull(savedRoutine)
            assertEquals(routine.entries.size + 1, result.entries.size)
            assertEquals("leg_curl", result.entries.last().exercise.id)
            assertEquals(4, result.entries.last().records.size)
        }
    }

    @Test
    fun supersetSelectionBottomBar_exposesConfirmClearAndCancelActions() {
        var grouped = false
        var cleared = false
        var cancelled = false

        composeRule.setThemedContent {
            StrengthSupersetSelectionBottomBar(
                canGroup = true,
                canClear = true,
                onGroup = { grouped = true },
                onClear = { cleared = true },
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
    fun supersetSelectionBottomBar_disablesUnavailableActions() {
        composeRule.setThemedContent {
            StrengthSupersetSelectionBottomBar(
                canGroup = false,
                canClear = false,
                onGroup = {},
                onClear = {},
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
    fun routineEdit_supersetSelectionUsesStickyFooterLabelsAndAddsToExistingGroup() {
        val routine = editSupersetTestRoutine()
        var savedRoutine: StrengthWorkoutRoutine? = null

        composeRule.setThemedContent {
            StrengthRoutineEditScreen(
                routine = routine,
                onSave = { savedRoutine = it },
                onDelete = {},
                onBack = {}
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditGroupSuperset)
            .performClick()

        composeRule.assertStrengthSupersetSelectionContract(
            existingGroupEntryIds = listOf(1, 2),
            looseEntryId = 3,
            hiddenActionContentDescriptions = listOf(
                TestContentDescriptions.StrengthRoutineEditAddExercise,
                TestContentDescriptions.StrengthRoutineEditSave,
                TestContentDescriptions.StrengthRoutineEditDelete
            )
        )

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthRoutineExerciseRow(1))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthConfirmSuperset)
            .assertIsEnabled()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.strengthRoutineExerciseRow(3))
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthConfirmSuperset)
            .assertIsEnabled()
            .performClick()
        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthGroupAsPairedSet)
            .assertIsEnabled()
            .performClick()

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditSave)
            .assertExists()
            .performClick()
        composeRule.runOnIdle {
            val result = requireNotNull(savedRoutine)
            assertEquals(listOf(1, 2, 3), result.entries.map { it.id })
            assertEquals(listOf(9, 9, 9), result.entries.map { it.supersetGroupId })
            assertEquals(
                listOf(
                    StrengthSetGroupType.PAIRED_SET,
                    StrengthSetGroupType.PAIRED_SET,
                    StrengthSetGroupType.PAIRED_SET
                ),
                result.entries.map { it.setGroupType }
            )
        }
    }

    @Test
    fun exerciseDetailEditor_addsSetAndHidesAddExerciseAction() {
        var entryState by mutableStateOf(editTestEntry())
        var changingExercise = false
        var deleteClicked = false

        composeRule.setThemedContent {
            StrengthExerciseDetailEditor(
                entry = entryState,
                isChangingExercise = changingExercise,
                onEntryChange = { entryState = it },
                onChangingExerciseChange = { changingExercise = it },
                onDelete = { deleteClicked = true }
            )
        }

        composeRule
            .onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseDetailAddSet)
            .performScrollTo()
            .performClick()
        composeRule
            .onAllNodesWithContentDescription(TestContentDescriptions.StrengthExerciseDetailAddExercise)
            .assertCountEquals(0)
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

private fun editSupersetTestRoutine(): StrengthWorkoutRoutine {
    val squat = strengthExerciseCatalog.first { it.id == "squat" }
    val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
    val row = strengthExerciseCatalog.first { it.id == "row" }
    return StrengthWorkoutRoutine(
        id = 8,
        name = "Superset Routine",
        entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat).copy(supersetGroupId = 9),
            defaultStrengthRoutineEntry(id = 2, exercise = bench).copy(supersetGroupId = 9),
            defaultStrengthRoutineEntry(id = 3, exercise = row)
        )
    )
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.setThemedContent(
    content: @Composable () -> Unit,
) {
    setContent {
        IntervalsGymTheme(content = content)
    }
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.openNewExerciseDetail() {
    onNodeWithContentDescription(TestContentDescriptions.StrengthRoutineEditAddExercise)
        .performClick()
    onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseSearch)
        .performTextInput("레그컬")
    onNodeWithContentDescription(TestContentDescriptions.strengthExerciseSearchResult("leg_curl"))
        .performClick()
    onNodeWithContentDescription(TestContentDescriptions.StrengthExerciseConfigDone)
        .performClick()
}

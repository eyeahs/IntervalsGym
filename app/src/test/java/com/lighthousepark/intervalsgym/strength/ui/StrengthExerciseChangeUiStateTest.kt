package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.StrengthExercise
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthExerciseChangeUiStateTest {
    @Test
    fun inactive_hasNoOpenChangeFlowOrDialogs() {
        val state = StrengthExerciseChangeUiState.inactive()

        assertFalse(state.isChangingCurrentExercise)
        assertFalse(state.isCurrentExerciseTypeDialogVisible)
        assertFalse(state.shouldReturnToOngoingAfterExerciseChange)
        assertFalse(state.isCustomExerciseDialogVisible)
        assertNull(state.pendingAddedExerciseEntryId)
        assertNull(state.exerciseToConfigure)
        assertEquals("", state.exerciseToConfigureSearchQuery)
    }

    @Test
    fun beginAddedExercise_tracksPendingEntryAndReturnTargetTogether() {
        val state = StrengthExerciseChangeUiState.inactive()
            .beginAddedExercise(entryId = 42)

        assertTrue(state.isChangingCurrentExercise)
        assertEquals(42, state.pendingAddedExerciseEntryId)
        assertTrue(state.shouldReturnToOngoingAfterExerciseChange)
        assertFalse(state.isCurrentExerciseTypeDialogVisible)
        assertFalse(state.isCustomExerciseDialogVisible)
    }

    @Test
    fun beginExistingExerciseChange_clearsAddedEntryState() {
        val state = StrengthExerciseChangeUiState.inactive()
            .beginAddedExercise(entryId = 42)
            .beginExistingExerciseChange()

        assertTrue(state.isChangingCurrentExercise)
        assertNull(state.pendingAddedExerciseEntryId)
        assertFalse(state.shouldReturnToOngoingAfterExerciseChange)
        assertFalse(state.isCurrentExerciseTypeDialogVisible)
    }

    @Test
    fun selectingAndDismissingExerciseConfig_updatesExerciseTargetOnly() {
        val exercise = exercise("row")
        val selected = StrengthExerciseChangeUiState.inactive()
            .selectExerciseToConfigure(exercise = exercise, searchQuery = "row")

        assertEquals(exercise, selected.exerciseToConfigure)
        assertEquals("row", selected.exerciseToConfigureSearchQuery)
        assertNull(selected.dismissExerciseConfig().exerciseToConfigure)
    }

    @Test
    fun customExerciseSelectionClosesDialogAndBecomesExerciseToConfigure() {
        val exercise = exercise("custom")
        val state = StrengthExerciseChangeUiState.inactive()
            .showCustomExerciseDialog()
            .addCustomExercise(exercise)

        assertFalse(state.isCustomExerciseDialogVisible)
        assertEquals(exercise, state.exerciseToConfigure)
        assertEquals("", state.exerciseToConfigureSearchQuery)
    }

    private fun exercise(id: String): StrengthExercise {
        return StrengthExercise(
            id = id,
            nameKo = id,
            nameEn = id,
            group = "test",
            equipmentOptions = listOf("덤벨"),
            variationOptions = listOf("기본")
        )
    }
}

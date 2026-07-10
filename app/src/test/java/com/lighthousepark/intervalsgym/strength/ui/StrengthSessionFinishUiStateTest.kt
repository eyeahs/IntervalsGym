package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.StrengthRoutineUpdateSelection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthSessionFinishUiStateTest {
    @Test
    fun uploadStartedClearsPreviousMessageAndError() {
        val state = StrengthSessionFinishUiState(
            uploadMessage = "done",
            uploadError = "old"
        )

        val uploading = state.withUploadStarted()

        assertTrue(uploading.isUploading)
        assertNull(uploading.uploadMessage)
        assertNull(uploading.uploadError)
    }

    @Test
    fun uploadFailureStopsUploadingAndKeepsErrorVisible() {
        val state = StrengthSessionFinishUiState().withUploadStarted()

        val failed = state.withUploadFailed("network")

        assertFalse(failed.isUploading)
        assertEquals("network", failed.uploadError)
    }

    @Test
    fun deleteFailureStopsDeletingAndKeepsErrorVisible() {
        val state = StrengthSessionFinishUiState()
            .showCalendarRoutineDeleteConfirm()
            .withCalendarRoutineDeleteStarted()

        val failed = state.withCalendarRoutineDeleteFailed(null)

        assertFalse(failed.isDeletingCalendarRoutine)
        assertTrue(failed.isCalendarRoutineDeleteConfirmVisible)
        assertEquals("Routine을 삭제하지 못했습니다.", failed.uploadError)
    }

    @Test
    fun finishPreferencesMoveTogether() {
        val availability = StrengthRoutineUpdateSelection(
            order = true,
            supersets = true,
            exerciseDetails = true
        )
        val state = StrengthSessionFinishUiState()
            .showFinishChoiceDialog(availability)
            .withFinishRpe(9)
            .withRoutineUpdateSelection(availability.copy(supersets = false))

        assertTrue(state.isFinishChoiceDialogVisible)
        assertEquals(9, state.finishRpe)
        assertTrue(state.routineUpdateSelection.order)
        assertFalse(state.routineUpdateSelection.supersets)
        assertTrue(state.routineUpdateSelection.exerciseDetails)

        val dismissed = state.dismissFinishChoiceDialog()

        assertFalse(dismissed.isFinishChoiceDialogVisible)
        assertEquals(9, dismissed.finishRpe)
        assertEquals(state.routineUpdateSelection, dismissed.routineUpdateSelection)
    }

    @Test
    fun openingFinishDialogChecksOnlyChangedRoutineCategoriesByDefault() {
        val availability = StrengthRoutineUpdateSelection(
            supersets = true,
            exerciseTypes = true
        )

        val state = StrengthSessionFinishUiState().showFinishChoiceDialog(availability)

        assertEquals(availability, state.routineUpdateSelection)
    }
}

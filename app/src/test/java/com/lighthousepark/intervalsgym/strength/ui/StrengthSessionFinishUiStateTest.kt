package com.lighthousepark.intervalsgym.strength.ui

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
        val state = StrengthSessionFinishUiState()
            .showFinishChoiceDialog()
            .withFinishRpe(9)
            .withApplyWorkoutResultToRoutine(false)

        assertTrue(state.isFinishChoiceDialogVisible)
        assertEquals(9, state.finishRpe)
        assertFalse(state.applyWorkoutResultToRoutine)

        val dismissed = state.dismissFinishChoiceDialog()

        assertFalse(dismissed.isFinishChoiceDialogVisible)
        assertEquals(9, dismissed.finishRpe)
        assertFalse(dismissed.applyWorkoutResultToRoutine)
    }
}

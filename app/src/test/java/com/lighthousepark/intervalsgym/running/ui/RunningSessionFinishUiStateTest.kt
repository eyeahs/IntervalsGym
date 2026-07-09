package com.lighthousepark.intervalsgym.running.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningSessionFinishUiStateTest {
    @Test
    fun finishedLocalSessionShowsUploadChoiceAndClearsStopDialogAndError() {
        val state = RunningSessionFinishUiState(
            isStopSaveDialogVisible = true,
            isUploading = true,
            error = "old"
        )

        val finished = state.withFinishedLocalSession(
            endedAtMillis = 10_000L,
            localSessionId = "running-1"
        )

        assertTrue(finished.isFinished)
        assertTrue(finished.isFinishDialogVisible)
        assertFalse(finished.isStopSaveDialogVisible)
        assertFalse(finished.isUploading)
        assertNull(finished.error)
        assertEquals("running-1", finished.localSessionId)
    }

    @Test
    fun uploadStartedAndFailedKeepDialogStateStable() {
        val state = RunningSessionFinishUiState(
            finishedAtMillis = 10_000L,
            isFinishDialogVisible = true,
            localSessionId = "local"
        )

        val uploading = state.withUploadStarted()
        val failed = uploading.withUploadFailed(null)

        assertTrue(uploading.isUploading)
        assertNull(uploading.error)
        assertTrue(uploading.isFinishDialogVisible)
        assertFalse(failed.isUploading)
        assertEquals("업로드하지 못했습니다.", failed.error)
        assertTrue(failed.isFinishDialogVisible)
    }

    @Test
    fun uploadSuccessStoresUploadedSessionId() {
        val state = RunningSessionFinishUiState(
            finishedAtMillis = 10_000L,
            isFinishDialogVisible = true,
            isUploading = true,
            localSessionId = "local"
        )

        val succeeded = state.withUploadSucceeded("uploaded")

        assertFalse(succeeded.isUploading)
        assertNull(succeeded.error)
        assertEquals("uploaded", succeeded.localSessionId)
    }

    @Test
    fun exitBackHandlerDisablesWhileDialogsAreVisible() {
        assertFalse(RunningSessionFinishUiState(isStopSaveDialogVisible = true).isExitBackHandlerEnabled)
        assertFalse(RunningSessionFinishUiState(isFinishDialogVisible = true).isExitBackHandlerEnabled)
        assertTrue(RunningSessionFinishUiState().isExitBackHandlerEnabled)
    }
}

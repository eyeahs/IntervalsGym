package com.lighthousepark.intervalsgym.running.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningSessionFinishUiStateTest {
    @Test
    fun finishedLocalSessionStartsWithoutUploadChoiceAndClearsStopDialogAndError() {
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
        assertFalse(finished.isStopSaveDialogVisible)
        assertFalse(finished.isUploading)
        assertNull(finished.error)
        assertEquals("running-1", finished.localSessionId)
    }

    @Test
    fun uploadStartedAndFailedKeepFinishedStateStableWithoutDialog() {
        val state = RunningSessionFinishUiState(
            finishedAtMillis = 10_000L,
            localSessionId = "local"
        )

        val uploading = state.withUploadStarted()
        val failed = uploading.withUploadFailed(null)

        assertTrue(uploading.isUploading)
        assertNull(uploading.error)
        assertFalse(failed.isUploading)
        assertEquals("업로드하지 못했습니다.", failed.error)
    }

    @Test
    fun uploadSuccessStoresUploadedSessionId() {
        val state = RunningSessionFinishUiState(
            finishedAtMillis = 10_000L,
            isUploading = true,
            localSessionId = "local"
        )

        val succeeded = state.withUploadSucceeded("uploaded")

        assertFalse(succeeded.isUploading)
        assertNull(succeeded.error)
        assertEquals("uploaded", succeeded.localSessionId)
    }

    @Test
    fun exitStateKeepsBackHandlerButBlocksExitWhileUploadIsActive() {
        assertFalse(RunningSessionFinishUiState(isStopSaveDialogVisible = true).isExitBackHandlerEnabled)
        assertTrue(RunningSessionFinishUiState(isUploading = true).isExitBackHandlerEnabled)
        assertFalse(RunningSessionFinishUiState(isUploading = true).canExitSession)
        assertTrue(RunningSessionFinishUiState().isExitBackHandlerEnabled)
        assertTrue(RunningSessionFinishUiState().canExitSession)
    }
}

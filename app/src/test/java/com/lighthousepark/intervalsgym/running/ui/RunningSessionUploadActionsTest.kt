package com.lighthousepark.intervalsgym.running.ui

import com.lighthousepark.intervalsgym.data.MemorySharedPreferences
import com.lighthousepark.intervalsgym.data.RecordingRunningSessionRemoteDataSource
import com.lighthousepark.intervalsgym.data.RunningSessionSyncUseCase
import com.lighthousepark.intervalsgym.running.HeartRateSample
import com.lighthousepark.intervalsgym.running.routineBlock
import com.lighthousepark.intervalsgym.running.toCompletedRunningSession
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningSessionUploadActionsTest {
    @Test
    fun planRunningSessionUpload_blocksBlankApiKey() {
        val action = runningResultSnapshot().planRunningSessionUpload(
            apiKey = "",
            finishUiState = RunningSessionFinishUiState(finishedAtMillis = 181_000L),
            nowMillis = 200_000L
        )

        assertEquals(RunningSessionUploadLoginRequired, action)
    }

    @Test
    fun planRunningSessionUpload_usesFinishedTimeAndUploadsSameSnapshot() = runBlocking {
        val prefs = MemorySharedPreferences()
        val remote = RecordingRunningSessionRemoteDataSource()
        val syncUseCase = RunningSessionSyncUseCase(
            prefs = prefs,
            remoteDataSource = remote
        )
        val snapshot = runningResultSnapshot()
        val action = snapshot.planRunningSessionUpload(
            apiKey = "token",
            finishUiState = RunningSessionFinishUiState(finishedAtMillis = 181_000L),
            nowMillis = 200_000L
        )

        require(action is RunningSessionUploadReady)
        val uploaded = action.uploadResult(syncUseCase)

        assertEquals(181_000L, action.endedAtMillis)
        assertTrue(action.startedDiagnosticDetails.contains("endedAtMillis=181000"))
        assertEquals(listOf(snapshot.toRunningSession(181_000L)), remote.uploads)
        assertEquals(uploaded.id, remote.uploads.single().toCompletedRunningSession(uploadedToIntervals = true).id)
    }

    private fun runningResultSnapshot(): RunningSessionResultSnapshot {
        return RunningSessionResultSnapshot(
            routineName = "언덕 러닝",
            startedAtMillis = 1_000L,
            blocks = listOf(
                routineBlock(index = 0, durationSeconds = 60, targetText = "6km/h"),
                routineBlock(index = 1, durationSeconds = 60, targetText = "8km/h")
            ),
            actualBlocks = listOf(routineBlock(index = 0, durationSeconds = 45, targetText = "6km/h")),
            heartRateSamples = listOf(HeartRateSample(timestampMillis = 30_000L, bpm = 142))
        )
    }
}

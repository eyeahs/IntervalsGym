package com.lighthousepark.intervalsgym.running.ui

import com.lighthousepark.intervalsgym.data.MemorySharedPreferences
import com.lighthousepark.intervalsgym.data.RecordingRunningSessionRemoteDataSource
import com.lighthousepark.intervalsgym.data.RunningSessionSyncUseCase
import com.lighthousepark.intervalsgym.data.loadCompletedRunningSessionHistory
import com.lighthousepark.intervalsgym.running.HeartRateSample
import com.lighthousepark.intervalsgym.running.routineBlock
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningSessionResultSnapshotsTest {
    @Test
    fun resultSnapshotBuildsRunningSessionWithActualBlocksAndHeartRateSamples() {
        val snapshot = runningResultSnapshot()

        val session = snapshot.toRunningSession(endedAtMillis = 181_000L)

        assertEquals("언덕 러닝", session.name)
        assertEquals(135, session.warmupSeconds)
        assertEquals(listOf(45), session.actualBlocks.map { it.durationSeconds })
        assertEquals(listOf(0), session.actualBlocks.map { it.startSecond })
        assertEquals(listOf(45), session.actualBlocks.map { it.endSecond })
        assertEquals(listOf(HeartRateSample(timestampMillis = 30_000L, bpm = 142)), session.heartRateSamples)
    }

    @Test
    fun resultSnapshotSaveAndUploadUseSameSessionShape() = runBlocking {
        val prefs = MemorySharedPreferences()
        val remote = RecordingRunningSessionRemoteDataSource()
        val syncUseCase = RunningSessionSyncUseCase(
            prefs = prefs,
            remoteDataSource = remote
        )
        val snapshot = runningResultSnapshot()

        val localSession = snapshot.saveLocalResult(
            syncUseCase = syncUseCase,
            endedAtMillis = 181_000L
        )
        val uploadedSession = snapshot.uploadResult(
            syncUseCase = syncUseCase,
            endedAtMillis = 181_000L
        )

        assertEquals(localSession.id, uploadedSession.id)
        assertFalse(localSession.uploadedToIntervals)
        assertTrue(uploadedSession.uploadedToIntervals)
        assertEquals(listOf(snapshot.toRunningSession(181_000L)), remote.uploads)
        assertEquals(listOf(uploadedSession.id), loadCompletedRunningSessionHistory(prefs).map { it.id })
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

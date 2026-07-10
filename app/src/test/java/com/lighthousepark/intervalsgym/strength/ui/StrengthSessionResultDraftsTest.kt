package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.data.MemorySharedPreferences
import com.lighthousepark.intervalsgym.data.RecordingStrengthSessionRemoteDataSource
import com.lighthousepark.intervalsgym.data.StrengthSessionSyncUseCase
import com.lighthousepark.intervalsgym.data.loadCompletedStrengthSessionHistory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthSessionResultDraftsTest {
    @Test
    fun finishedResultUsesSnapshotActionsAndDefaultFinishReason() {
        val syncUseCase = StrengthSessionSyncUseCase(
            prefs = MemorySharedPreferences(),
            remoteDataSource = RecordingStrengthSessionRemoteDataSource()
        )
        val snapshot = strengthResultSnapshotForTest()

        val result = requireNotNull(
            snapshot.buildFinishedResult(
                syncUseCase = syncUseCase,
                endedAtMillis = 20_000L,
                uploadedToIntervals = true
            )
        )

        assertEquals("85", result.setEvents.single().weightKg)
        assertEquals("4", result.setEvents.single().reps)
        assertEquals(45, result.setEvents.single().targetRestSeconds)
        assertEquals(45, result.restEvents.single().plannedSeconds)
        assertEquals(20_000L, result.restEvents.single().endedAtMillis)
        assertEquals(STRENGTH_RESULT_END_REASON_WORKOUT_FINISHED, result.restEvents.single().endReason)
        assertTrue(result.appliedToRoutine)
        assertEquals("85", requireNotNull(result.routineUpdateEntries).first().records.first().weightKg)
    }

    @Test
    fun liveResultActionsSaveAndDeleteTheSameSnapshot() {
        val prefs = MemorySharedPreferences()
        val syncUseCase = StrengthSessionSyncUseCase(
            prefs = prefs,
            remoteDataSource = RecordingStrengthSessionRemoteDataSource()
        )
        val snapshot = strengthResultSnapshotForTest()

        val liveResult = requireNotNull(snapshot.saveLiveResult(
            syncUseCase = syncUseCase,
            endedAtMillis = 12_000L
        ))
        assertFalse(liveResult.appliedToRoutine)
        assertNull(liveResult.routineUpdateEntries)
        assertEquals(1, loadCompletedStrengthSessionHistory(prefs).size)

        snapshot.deleteLiveResult(
            syncUseCase = syncUseCase,
            endedAtMillis = 12_000L
        )

        assertEquals(0, loadCompletedStrengthSessionHistory(prefs).size)
    }
}

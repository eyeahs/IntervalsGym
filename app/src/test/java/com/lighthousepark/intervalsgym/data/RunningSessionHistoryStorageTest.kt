package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.app.RUNNING_SESSION_HISTORY_PREF
import com.lighthousepark.intervalsgym.running.HeartRateSample
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningSessionHistoryStorageTest {
    @Test
    fun appendRunningSessionHistory_deduplicatesExistingSessionId() {
        val prefs = MemorySharedPreferences()
        val original = completedRunningSessionForStorage(
            id = "running-same",
            name = "before",
            startedAtMillis = 1_000L,
            endedAtMillis = 61_000L
        )
        val replacement = original.copy(name = "after", uploadedToIntervals = true)

        appendRunningSessionHistory(prefs, original)
        appendRunningSessionHistory(prefs, replacement)

        val history = loadCompletedRunningSessionHistory(prefs)
        assertEquals(1, history.size)
        assertEquals("after", history.single().name)
        assertTrue(history.single().uploadedToIntervals)
    }

    @Test
    fun runningSessionHistory_roundTripsHeartRateAndMergeMetadata() {
        val prefs = MemorySharedPreferences()
        val workout = completedRunningSessionForStorage(
            id = "running-merge",
            name = "interval run",
            startedAtMillis = 1_000L,
            endedAtMillis = 61_000L
        ).copy(
            heartRateSamples = listOf(HeartRateSample(timestampMillis = 2_000L, bpm = 142)),
            mergedIntervalsActivityId = "i-garmin-1",
            mergeOffsetSeconds = 7,
            mergeCorrelation = 0.91
        )

        appendRunningSessionHistory(prefs, workout)

        val restored = loadCompletedRunningSessionHistory(prefs).single()
        assertEquals(workout.heartRateSamples, restored.heartRateSamples)
        assertEquals("i-garmin-1", restored.mergedIntervalsActivityId)
        assertEquals(7, restored.mergeOffsetSeconds)
        assertEquals(0.91, restored.mergeCorrelation ?: 0.0, 0.0001)
    }

    @Test
    fun runningSessionHistory_loadsLegacyShapeWithoutMergeFields() {
        val prefs = MemorySharedPreferences()
        prefs.edit().putString(
            RUNNING_SESSION_HISTORY_PREF,
            """[{"id":"legacy","name":"run","startedAtMillis":1000,"endedAtMillis":61000,"durationSeconds":60,"blocks":[],"actualBlocks":[]}]"""
        ).apply()

        val restored = loadCompletedRunningSessionHistory(prefs).single()

        assertTrue(restored.heartRateSamples.isEmpty())
        assertNull(restored.mergedIntervalsActivityId)
        assertNull(restored.mergeOffsetSeconds)
        assertNull(restored.mergeCorrelation)
    }
}

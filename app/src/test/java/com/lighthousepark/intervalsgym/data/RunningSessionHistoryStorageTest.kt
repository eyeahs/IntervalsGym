package com.lighthousepark.intervalsgym.data

import org.junit.Assert.assertEquals
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
}

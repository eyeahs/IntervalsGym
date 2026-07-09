package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.strength.StrengthRestEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class StrengthSessionEventJsonTest {
    @Test
    fun finalizeRestEvents_closesOnlyActiveOpenRest() {
        val events = listOf(
            StrengthRestEvent(
                id = 1,
                afterSetSequence = 1,
                exerciseEntryId = 1,
                exerciseTitle = "스쿼트",
                setRecordId = 1,
                setIndex = 0,
                startedAtMillis = 1000L,
                plannedSeconds = 60,
                targetEndAtMillis = 61000L,
                endedAtMillis = null,
                endReason = null
            ),
            StrengthRestEvent(
                id = 2,
                afterSetSequence = 2,
                exerciseEntryId = 1,
                exerciseTitle = "스쿼트",
                setRecordId = 2,
                setIndex = 1,
                startedAtMillis = 2000L,
                plannedSeconds = 60,
                targetEndAtMillis = 62000L,
                endedAtMillis = null,
                endReason = null
            )
        )

        val finalized = finalizeRestEvents(events, activeRestEventId = 2, endedAtMillis = 5000L, reason = "stopped")

        assertEquals(null, finalized[0].endedAtMillis)
        assertEquals(5000L, finalized[1].endedAtMillis)
        assertEquals("stopped", finalized[1].endReason)
    }
}

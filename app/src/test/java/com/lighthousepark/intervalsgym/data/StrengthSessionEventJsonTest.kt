package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.strength.StrengthRestEvent
import com.lighthousepark.intervalsgym.strength.StrengthSetCompletionEvent
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Test

class StrengthSessionEventJsonTest {
    @Test
    fun setEventDuration_roundTripsAndLegacyEventDefaultsToBlank() {
        val event = StrengthSetCompletionEvent(
            sequence = 1,
            exerciseEntryId = 2,
            exerciseTitle = "플랭크",
            exerciseGroup = "코어",
            exerciseId = "plank",
            equipment = "맨몸",
            variation = "기본",
            setRecordId = 3,
            setIndex = 0,
            weightKg = "",
            reps = "",
            targetRestSeconds = 30,
            completedAtMillis = 1_000L,
            durationSeconds = "45"
        )
        val encoded = listOf(event).toSetEventsJsonArray()

        assertEquals("45", encoded.toStrengthSetCompletionEvents().single().durationSeconds)

        val legacy = JSONArray(encoded.toString()).apply {
            getJSONObject(0).remove("durationSeconds")
        }
        assertEquals("", legacy.toStrengthSetCompletionEvents().single().durationSeconds)
    }

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

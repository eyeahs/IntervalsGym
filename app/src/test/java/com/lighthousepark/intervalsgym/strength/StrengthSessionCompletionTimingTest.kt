package com.lighthousepark.intervalsgym.strength

import com.lighthousepark.intervalsgym.core.SESSION_AUTO_LOCAL_SAVE_DELAY_MILLIS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthSessionCompletionTimingTest {
    @Test
    fun completedStrengthSessionAutoLocalSaveAtMillis_usesLastCompletedSetWhenAllSetsAreDone() {
        val entry = defaultStrengthRoutineEntry(
            id = 1,
            exercise = strengthExerciseCatalog.first { it.id == "squat" }
        ).withCompletedRecords(0, 1, 2)
        val setEvents = listOf(
            entry.toSetEvent(sequence = 1, setIndex = 0).copy(completedAtMillis = 1_000L),
            entry.toSetEvent(sequence = 2, setIndex = 1).copy(completedAtMillis = 2_000L),
            entry.toSetEvent(sequence = 3, setIndex = 2).copy(completedAtMillis = 3_000L)
        )

        assertEquals(3_000L, completedStrengthSessionFinishedAtMillis(listOf(entry), setEvents))
        assertEquals(
            3_000L + SESSION_AUTO_LOCAL_SAVE_DELAY_MILLIS,
            completedStrengthSessionAutoLocalSaveAtMillis(listOf(entry), setEvents)
        )
        assertFalse(
            shouldAutoLocalSaveCompletedStrengthSession(
                entries = listOf(entry),
                setEvents = setEvents,
                nowMillis = 3_000L + SESSION_AUTO_LOCAL_SAVE_DELAY_MILLIS - 1L
            )
        )
        assertTrue(
            shouldAutoLocalSaveCompletedStrengthSession(
                entries = listOf(entry),
                setEvents = setEvents,
                nowMillis = 3_000L + SESSION_AUTO_LOCAL_SAVE_DELAY_MILLIS
            )
        )
    }

    @Test
    fun completedStrengthSessionFinishedAtMillis_waitsUntilEverySetIsDone() {
        val entry = defaultStrengthRoutineEntry(
            id = 1,
            exercise = strengthExerciseCatalog.first { it.id == "squat" }
        ).withCompletedRecords(0, 1)
        val setEvents = listOf(
            entry.toSetEvent(sequence = 1, setIndex = 0),
            entry.toSetEvent(sequence = 2, setIndex = 1)
        )

        assertEquals(null, completedStrengthSessionFinishedAtMillis(listOf(entry), setEvents))
        assertEquals(null, completedStrengthSessionAutoLocalSaveAtMillis(listOf(entry), setEvents))
        assertFalse(
            shouldAutoLocalSaveCompletedStrengthSession(
                entries = listOf(entry),
                setEvents = setEvents,
                nowMillis = Long.MAX_VALUE
            )
        )
    }
}

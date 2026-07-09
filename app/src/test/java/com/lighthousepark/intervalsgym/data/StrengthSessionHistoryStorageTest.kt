package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.strength.StrengthRestEvent
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthSessionHistoryStorageTest {
    @Test
    fun appendStrengthSessionHistory_deduplicatesExistingSessionId() {
        val prefs = MemorySharedPreferences()
        val original = completedStrengthSessionForStorage(
            id = "strength-same",
            routineName = "before",
            startedAtMillis = 1_000L,
            endedAtMillis = 61_000L
        )
        val replacement = original.copy(routineName = "after", uploadedToIntervals = true)

        appendStrengthSessionHistory(prefs, original)
        appendStrengthSessionHistory(prefs, replacement)

        val history = loadCompletedStrengthSessionHistory(prefs)
        assertEquals(1, history.size)
        assertEquals("after", history.single().routineName)
        assertTrue(history.single().uploadedToIntervals)
    }

    @Test
    fun buildCompletedStrengthSession_keepsStableIdAcrossResultUpdates() {
        val routine = defaultStrengthRoutines().first()
        val first = buildCompletedStrengthSession(
            routine = routine,
            entries = routine.entries,
            setEvents = emptyList(),
            restEvents = emptyList(),
            startedAtMillis = 1_000L,
            endedAtMillis = 61_000L,
            rpe = 7,
            trainingLoad = 1,
            uploadedToIntervals = false
        )
        val updated = buildCompletedStrengthSession(
            routine = routine,
            entries = routine.entries,
            setEvents = emptyList(),
            restEvents = emptyList(),
            startedAtMillis = 1_000L,
            endedAtMillis = 91_000L,
            rpe = 8,
            trainingLoad = 2,
            uploadedToIntervals = false
        )

        assertEquals(strengthSessionResultId(routine.id, 1_000L), first.id)
        assertEquals(first.id, updated.id)
    }

    @Test
    fun toStrengthSession_keepsCompletedSetAndRestEventsForUpload() {
        val routine = defaultStrengthRoutines().first()
        val completedEntry = routine.entries.first().copy(
            records = routine.entries.first().records.mapIndexed { index, record ->
                if (index == 0) record.copy(weightKg = "85", reps = "4", restSeconds = "45", completed = true) else record
            }
        )
        val setEvent = strengthSetEventForStorage(completedEntry)
        val restEvent = StrengthRestEvent(
            id = 1,
            afterSetSequence = setEvent.sequence,
            exerciseEntryId = completedEntry.id,
            exerciseTitle = completedEntry.title,
            setRecordId = completedEntry.records.first().id,
            setIndex = 0,
            startedAtMillis = setEvent.completedAtMillis,
            plannedSeconds = 45,
            targetEndAtMillis = setEvent.completedAtMillis + 45_000L,
            endedAtMillis = setEvent.completedAtMillis + 42_000L,
            endReason = "finished"
        )
        val completedSession = buildCompletedStrengthSession(
            routine = routine,
            entries = listOf(completedEntry),
            setEvents = listOf(setEvent),
            restEvents = listOf(restEvent),
            startedAtMillis = 1_000L,
            endedAtMillis = 91_000L,
            rpe = 8,
            trainingLoad = 2,
            uploadedToIntervals = false
        )

        val uploadSession = completedSession.toStrengthSession()

        assertEquals(90, uploadSession.durationSeconds)
        assertEquals(listOf(setEvent), uploadSession.setEvents)
        assertEquals(listOf(restEvent), uploadSession.restEvents)
    }

    @Test
    fun appendStrengthSessionHistory_replacesSameStartedRoutineEvenWhenLegacyIdDiffers() {
        val prefs = MemorySharedPreferences()
        val routine = defaultStrengthRoutines().first()
        val legacy = completedStrengthSessionForStorage(
            id = "strength-1000-61000",
            routineName = "before",
            startedAtMillis = 1_000L,
            endedAtMillis = 61_000L
        )
        val updated = buildCompletedStrengthSession(
            routine = routine,
            entries = routine.entries,
            setEvents = emptyList(),
            restEvents = emptyList(),
            startedAtMillis = 1_000L,
            endedAtMillis = 91_000L,
            rpe = 8,
            trainingLoad = 2,
            uploadedToIntervals = false
        ).copy(routineName = "after")

        appendStrengthSessionHistory(prefs, legacy)
        appendStrengthSessionHistory(prefs, updated)

        val history = loadCompletedStrengthSessionHistory(prefs)
        assertEquals(1, history.size)
        assertEquals(updated.id, history.single().id)
        assertEquals("after", history.single().routineName)
        assertEquals(91_000L, history.single().endedAtMillis)
    }
}

package com.lighthousepark.intervalsgym.strength

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthSessionEventSyncTest {
    @Test
    fun strengthSetAndRestEvents_followCompletedSetEdits() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val originalEntry = defaultStrengthRoutineEntry(id = 1, exercise = squat)
            .withCompletedRecord(0)
        val originalSetEvent = originalEntry.toSetEvent(sequence = 1, setIndex = 0)
        val updatedEntry = originalEntry.copy(
            exercise = bench,
            equipment = "덤벨",
            variation = "인클라인",
            records = originalEntry.records.mapIndexed { index, record ->
                if (index == 0) {
                    record.copy(weightKg = "42.5", reps = "7", restSeconds = "45", completed = true)
                } else {
                    record
                }
            }
        )
        val originalRestEvent = strengthRestEvent(
            id = 1,
            targetEndAtMillis = originalSetEvent.completedAtMillis + originalSetEvent.targetRestSeconds * 1000L
        ).copy(
            afterSetSequence = originalSetEvent.sequence,
            setRecordId = originalSetEvent.setRecordId,
            setIndex = originalSetEvent.setIndex,
            plannedSeconds = originalSetEvent.targetRestSeconds
        )

        val syncedSetEvents = listOf(originalSetEvent).withCurrentStrengthSetDetails(listOf(updatedEntry))
        val syncedRestEvents = listOf(originalRestEvent).withCurrentStrengthRestDetails(syncedSetEvents)

        assertEquals("42.5", syncedSetEvents.single().weightKg)
        assertEquals("7", syncedSetEvents.single().reps)
        assertEquals(bench.id, syncedSetEvents.single().exerciseId)
        assertEquals("덤벨", syncedSetEvents.single().equipment)
        assertEquals("인클라인", syncedSetEvents.single().variation)
        assertEquals(45, syncedSetEvents.single().targetRestSeconds)
        assertEquals(syncedSetEvents.single().exerciseTitle, syncedRestEvents.single().exerciseTitle)
        assertEquals(45, syncedRestEvents.single().plannedSeconds)
        assertEquals(46_000L, syncedRestEvents.single().targetEndAtMillis)
    }

    @Test
    fun strengthSetEvents_dropUncheckedCompletedRecords() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val completedEntry = defaultStrengthRoutineEntry(id = 1, exercise = squat)
            .withCompletedRecord(0)
        val uncheckedEntry = completedEntry.copy(
            records = completedEntry.records.mapIndexed { index, record ->
                if (index == 0) record.copy(completed = false) else record
            }
        )

        val syncedSetEvents = listOf(completedEntry.toSetEvent(sequence = 1, setIndex = 0))
            .withCurrentStrengthSetDetails(listOf(uncheckedEntry))

        assertTrue(syncedSetEvents.isEmpty())
    }
}

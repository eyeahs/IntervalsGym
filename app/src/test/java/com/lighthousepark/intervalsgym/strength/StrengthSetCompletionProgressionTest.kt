package com.lighthousepark.intervalsgym.strength

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthSetCompletionProgressionTest {
    @Test
    fun completeStrengthSet_createsSetEventAndRestBeforeNextNonSupersetSet() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat).copy(restSeconds = 90),
            defaultStrengthRoutineEntry(id = 2, exercise = bench)
        )

        val result = requireNotNull(
            completeStrengthSet(
                entries = entries,
                currentExerciseIndex = 0,
                currentSetIndex = 0,
                nextSetEventSequence = 3,
                nextRestEventId = 5,
                completedAtMillis = 10_000L
            )
        )

        assertTrue(result.entries[0].records[0].completed)
        assertEquals(3, result.setEvent?.sequence)
        assertEquals(0, result.currentExerciseIndex)
        assertEquals(0, result.currentSetIndex)
        assertEquals(0, result.pendingExerciseIndex)
        assertEquals(1, result.pendingSetIndex)
        assertEquals(StrengthSetCompletionFollowUp.START_REST, result.followUp)
        assertEquals(5, result.restEvent?.id)
        assertEquals(result.setEvent?.targetRestSeconds, result.restEvent?.plannedSeconds)
    }

    @Test
    fun completeStrengthSet_skipsRestForImmediateSupersetTransition() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat).copy(supersetGroupId = 7, restSeconds = 90),
            defaultStrengthRoutineEntry(id = 2, exercise = bench).copy(supersetGroupId = 7, restSeconds = 90)
        )

        val result = requireNotNull(
            completeStrengthSet(
                entries = entries,
                currentExerciseIndex = 0,
                currentSetIndex = 0,
                nextSetEventSequence = 1,
                nextRestEventId = 1,
                completedAtMillis = 10_000L
            )
        )

        assertEquals(1, result.pendingExerciseIndex)
        assertEquals(0, result.pendingSetIndex)
        assertEquals(null, result.restEvent)
        assertEquals(StrengthSetCompletionFollowUp.MOVE_TO_PENDING_SET, result.followUp)
    }

    @Test
    fun completeStrengthSet_finishesAllSetsAfterLastSet() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val entry = defaultStrengthRoutineEntry(id = 1, exercise = squat)
            .withCompletedRecords(0, 1)
            .copy(restSeconds = 90)

        val result = requireNotNull(
            completeStrengthSet(
                entries = listOf(entry),
                currentExerciseIndex = 0,
                currentSetIndex = 2,
                nextSetEventSequence = 3,
                nextRestEventId = 1,
                completedAtMillis = 10_000L
            )
        )

        assertTrue(result.entries.single().records.all { it.completed })
        assertEquals(null, result.pendingExerciseIndex)
        assertEquals(null, result.pendingSetIndex)
        assertEquals(null, result.restEvent)
        assertEquals(StrengthSetCompletionFollowUp.FINISH_ALL_SETS, result.followUp)
    }
}

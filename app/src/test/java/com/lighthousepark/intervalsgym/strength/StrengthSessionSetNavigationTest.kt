package com.lighthousepark.intervalsgym.strength

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthSessionSetNavigationTest {
    @Test
    fun nextIncompleteSet_prefersNextSupersetExerciseInSameSetRound() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val row = strengthExerciseCatalog.first { it.id == "row" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat).copy(supersetGroupId = 7).withCompletedRecord(0),
            defaultStrengthRoutineEntry(id = 2, exercise = bench).copy(supersetGroupId = 7),
            defaultStrengthRoutineEntry(id = 3, exercise = row)
        )

        val next = nextIncompleteSet(entries, fromExerciseIndex = 0, fromSetIndex = 0)

        assertEquals(1 to 0, next)
        assertTrue(isImmediateSupersetTransition(entries, fromExerciseIndex = 0, fromSetIndex = 0, toSet = next))
    }

    @Test
    fun nextIncompleteSet_startsAtFirstExerciseWhenCurrentIndexIsNegative() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat).withCompletedRecord(0),
            defaultStrengthRoutineEntry(id = 2, exercise = bench)
        )

        val next = nextIncompleteSet(entries, fromExerciseIndex = -1, fromSetIndex = 0)

        assertEquals(0 to 1, next)
    }

    @Test
    fun nextIncompleteSet_returnsNullWhenEverySetIsCompleted() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat).withCompletedRecords(0, 1, 2),
            defaultStrengthRoutineEntry(id = 2, exercise = bench).withCompletedRecords(0, 1, 2)
        )

        val next = nextIncompleteSet(entries, fromExerciseIndex = 0, fromSetIndex = 2)

        assertEquals(null, next)
    }

    @Test
    fun nextIncompleteSet_returnsNextSupersetRoundAfterLastSupersetExercise() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat).copy(supersetGroupId = 7).withCompletedRecord(0),
            defaultStrengthRoutineEntry(id = 2, exercise = bench).copy(supersetGroupId = 7).withCompletedRecord(0)
        )

        val next = nextIncompleteSet(entries, fromExerciseIndex = 1, fromSetIndex = 0)

        assertEquals(0 to 1, next)
        assertEquals(false, isImmediateSupersetTransition(entries, fromExerciseIndex = 1, fromSetIndex = 0, toSet = next))
    }

    @Test
    fun nextIncompleteSet_returnsNextSupersetRoundAfterThirdSupersetExercise() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val row = strengthExerciseCatalog.first { it.id == "row" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat).copy(supersetGroupId = 7).withCompletedRecord(0),
            defaultStrengthRoutineEntry(id = 2, exercise = bench).copy(supersetGroupId = 7).withCompletedRecord(0),
            defaultStrengthRoutineEntry(id = 3, exercise = row).copy(supersetGroupId = 7).withCompletedRecord(0)
        )

        val next = nextIncompleteSet(entries, fromExerciseIndex = 2, fromSetIndex = 0)

        assertEquals(0 to 1, next)
        assertEquals(false, isImmediateSupersetTransition(entries, fromExerciseIndex = 2, fromSetIndex = 0, toSet = next))
    }

    @Test
    fun shouldAdvanceCurrentExerciseAfterCompletedExercise_movesAfterLastSet() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat).withCompletedRecords(0, 1, 2),
            defaultStrengthRoutineEntry(id = 2, exercise = bench)
        )
        val next = nextIncompleteSet(entries, fromExerciseIndex = 0, fromSetIndex = 2)

        assertEquals(1 to 0, next)
        assertTrue(
            shouldAdvanceCurrentExerciseAfterCompletedExercise(
                entries = entries,
                fromExerciseIndex = 0,
                toSet = next
            )
        )
    }

    @Test
    fun shouldAdvanceCurrentExerciseAfterCompletedExercise_staysWhenSameExerciseHasMoreSets() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat).withCompletedRecord(0),
            defaultStrengthRoutineEntry(id = 2, exercise = bench)
        )
        val next = nextIncompleteSet(entries, fromExerciseIndex = 0, fromSetIndex = 0)

        assertEquals(0 to 1, next)
        assertFalse(
            shouldAdvanceCurrentExerciseAfterCompletedExercise(
                entries = entries,
                fromExerciseIndex = 0,
                toSet = next
            )
        )
    }

    @Test
    fun exerciseChangeFocusIndex_prefersPendingAddedEntryOverStaleCurrentIndex() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat),
            defaultStrengthRoutineEntry(id = 9, exercise = bench)
        )

        val focusIndex = entries.exerciseChangeFocusIndex(
            currentExerciseIndex = 7,
            pendingAddedEntryId = 9
        )

        assertEquals(1, focusIndex)
    }

    @Test
    fun exerciseChangeFocusIndex_clampsWhenPendingEntryIsMissing() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val entries = listOf(defaultStrengthRoutineEntry(id = 1, exercise = squat))

        val focusIndex = entries.exerciseChangeFocusIndex(
            currentExerciseIndex = 7,
            pendingAddedEntryId = 99
        )

        assertEquals(0, focusIndex)
    }
}

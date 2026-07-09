package com.lighthousepark.intervalsgym.strength

import org.junit.Assert.assertEquals
import org.junit.Test

class StrengthSupersetGroupsTest {
    @Test
    fun groupSelectedEntriesAsSuperset_movesSelectedEntriesBelowTopSelectedEntry() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val row = strengthExerciseCatalog.first { it.id == "row" }
        val deadlift = strengthExerciseCatalog.first { it.id == "deadlift" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat),
            defaultStrengthRoutineEntry(id = 2, exercise = bench),
            defaultStrengthRoutineEntry(id = 3, exercise = row),
            defaultStrengthRoutineEntry(id = 4, exercise = deadlift)
        )

        val grouped = entries.groupSelectedEntriesAsSuperset(
            selectedEntryIds = setOf(1, 3),
            supersetGroupId = 7
        )

        assertEquals(listOf(1, 3, 2, 4), grouped.map { it.id })
        assertEquals(listOf(7, 7), grouped.take(2).map { it.supersetGroupId })
        assertEquals(null, grouped[2].supersetGroupId)
        assertEquals(null, grouped[3].supersetGroupId)
    }

    @Test
    fun groupSelectedEntriesAsSuperset_keepsAlreadyAdjacentEntriesInPlace() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val row = strengthExerciseCatalog.first { it.id == "row" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat),
            defaultStrengthRoutineEntry(id = 2, exercise = bench),
            defaultStrengthRoutineEntry(id = 3, exercise = row)
        )

        val grouped = entries.groupSelectedEntriesAsSuperset(
            selectedEntryIds = setOf(2, 3),
            supersetGroupId = 8
        )

        assertEquals(listOf(1, 2, 3), grouped.map { it.id })
        assertEquals(listOf(null, 8, 8), grouped.map { it.supersetGroupId })
    }

    @Test
    fun normalizeSupersetGroups_clearsGroupsWithSingleRemainingEntry() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val row = strengthExerciseCatalog.first { it.id == "row" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat).copy(supersetGroupId = 7),
            defaultStrengthRoutineEntry(id = 2, exercise = bench).copy(supersetGroupId = 8),
            defaultStrengthRoutineEntry(id = 3, exercise = row).copy(supersetGroupId = 8)
        )

        val normalized = entries.normalizeSupersetGroups()

        assertEquals(listOf(null, 8, 8), normalized.map { it.supersetGroupId })
    }
}

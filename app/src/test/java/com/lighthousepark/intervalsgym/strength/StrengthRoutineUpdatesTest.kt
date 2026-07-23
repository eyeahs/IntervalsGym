package com.lighthousepark.intervalsgym.strength

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthRoutineUpdatesTest {
    @Test
    fun availabilityIgnoresCompletionAndActualValuesButDetectsFourRoutineConcerns() {
        val routineEntries = defaultStrengthRoutines().first().entries
        val workoutEntries = listOf(
            routineEntries[1].copy(
                supersetGroupId = 7,
                records = routineEntries[1].records.mapIndexed { index, record ->
                    if (index == 0) {
                        record.copy(actualWeightKg = "80", actualReps = "6", completed = true)
                    } else {
                        record
                    }
                }
            ),
            routineEntries[0].copy(
                supersetGroupId = 7,
                equipment = "덤벨",
                note = "천천히",
                records = routineEntries[0].records.map { it.copy(weightKg = "65") }
            ),
            routineEntries[2]
        )

        val changes = strengthRoutineUpdateAvailability(routineEntries, workoutEntries)

        assertTrue(changes.order)
        assertTrue(changes.supersets)
        assertTrue(changes.exerciseTypes)
        assertTrue(changes.exerciseDetails)
    }

    @Test
    fun availabilityDoesNotTreatActualPerformanceAsRoutineDetailChange() {
        val routineEntries = defaultStrengthRoutines().first().entries
        val workoutEntries = routineEntries.mapIndexed { entryIndex, entry ->
            if (entryIndex == 0) {
                entry.copy(
                    records = entry.records.mapIndexed { recordIndex, record ->
                        if (recordIndex == 0) {
                            record.copy(actualWeightKg = "80", actualReps = "6", completed = true)
                        } else {
                            record
                        }
                    }
                )
            } else {
                entry
            }
        }

        val changes = strengthRoutineUpdateAvailability(routineEntries, workoutEntries)

        assertFalse(changes.hasSelection)
    }

    @Test
    fun measurementTypeChangeIsAppliedWithExerciseTypesSelection() {
        val routineEntries = defaultStrengthRoutines().first().entries
        val workoutEntries = routineEntries.mapIndexed { index, entry ->
            if (index == 0) entry.copy(setMetricType = StrengthSetMetricType.DURATION) else entry
        }

        val availability = strengthRoutineUpdateAvailability(routineEntries, workoutEntries)
        val merged = requireNotNull(
            mergeStrengthRoutineUpdates(
                routineEntries = routineEntries,
                workoutEntries = workoutEntries,
                selection = StrengthRoutineUpdateSelection(exerciseTypes = true)
            )
        )

        assertTrue(availability.exerciseTypes)
        assertEquals(StrengthSetMetricType.DURATION, merged.first().setMetricType)
    }

    @Test
    fun availabilityDetectsNewExerciseInsertedBetweenExistingEntriesAsOrderChange() {
        val routineEntries = defaultStrengthRoutines().first().entries
        val addedEntry = defaultStrengthRoutineEntry(
            id = 99,
            exercise = strengthExerciseCatalog.last()
        )
        val workoutEntries = listOf(routineEntries[0], addedEntry) + routineEntries.drop(1)

        val changes = strengthRoutineUpdateAvailability(routineEntries, workoutEntries)

        assertTrue(changes.order)
        assertTrue(changes.exerciseTypes)
    }

    @Test
    fun mergeAppliesOnlySelectedRoutineChangesAndClearsRuntimeValues() {
        val routineEntries = defaultStrengthRoutines().first().entries
        val workoutEntries = listOf(
            routineEntries[1].copy(supersetGroupId = 9),
            routineEntries[0].copy(
                supersetGroupId = 9,
                note = "운동 중 변경",
                records = routineEntries[0].records.map { record ->
                    record.copy(
                        weightKg = "70",
                        actualWeightKg = "80",
                        actualReps = "6",
                        completed = true
                    )
                }
            ),
            routineEntries[2]
        )

        val merged = requireNotNull(
            mergeStrengthRoutineUpdates(
                routineEntries = routineEntries,
                workoutEntries = workoutEntries,
                selection = StrengthRoutineUpdateSelection(order = true, exerciseDetails = true)
            )
        )

        assertEquals(listOf(2, 1, 3), merged.map { it.id })
        assertEquals(routineEntries[1].supersetGroupId, merged[0].supersetGroupId)
        assertEquals("운동 중 변경", merged[1].note)
        assertEquals("70", merged[1].records.first().weightKg)
        assertEquals("", merged[1].records.first().actualWeightKg)
        assertFalse(merged[1].records.first().completed)
    }

    @Test
    fun mergeReturnsNullWhenNothingWasSelected() {
        assertNull(
            mergeStrengthRoutineUpdates(
                routineEntries = defaultStrengthRoutines().first().entries,
                workoutEntries = defaultStrengthRoutines().first().entries,
                selection = StrengthRoutineUpdateSelection()
            )
        )
    }

    @Test
    fun mergeSupersetsOnlyPreservesRoutineOrderAndExerciseDetails() {
        val routineEntries = defaultStrengthRoutines().first().entries
        val workoutEntries = listOf(
            routineEntries[1].copy(supersetGroupId = 10),
            routineEntries[0].copy(
                supersetGroupId = 10,
                equipment = "덤벨",
                note = "선택하지 않은 상세"
            ),
            routineEntries[2]
        )

        val merged = requireNotNull(
            mergeStrengthRoutineUpdates(
                routineEntries = routineEntries,
                workoutEntries = workoutEntries,
                selection = StrengthRoutineUpdateSelection(supersets = true)
            )
        )

        assertEquals(routineEntries.map { it.id }, merged.map { it.id })
        assertEquals(10, merged[0].supersetGroupId)
        assertEquals(10, merged[1].supersetGroupId)
        assertEquals(routineEntries[0].equipment, merged[0].equipment)
        assertEquals(routineEntries[0].note, merged[0].note)
    }

    @Test
    fun mergeSetGroupsAppliesPairedSetTypeChange() {
        val routineEntries = defaultStrengthRoutines().first().entries.mapIndexed { index, entry ->
            if (index < 2) entry.copy(supersetGroupId = 10, setGroupType = StrengthSetGroupType.SUPERSET) else entry
        }
        val workoutEntries = routineEntries.map { entry ->
            if (entry.supersetGroupId == 10) entry.copy(setGroupType = StrengthSetGroupType.PAIRED_SET) else entry
        }

        val availability = strengthRoutineUpdateAvailability(routineEntries, workoutEntries)
        val merged = requireNotNull(
            mergeStrengthRoutineUpdates(
                routineEntries = routineEntries,
                workoutEntries = workoutEntries,
                selection = StrengthRoutineUpdateSelection(supersets = true)
            )
        )

        assertTrue(availability.supersets)
        assertEquals(StrengthSetGroupType.PAIRED_SET, merged[0].setGroupType)
        assertEquals(StrengthSetGroupType.PAIRED_SET, merged[1].setGroupType)
    }

    @Test
    fun mergeExerciseTypesOnlyAddsAndDeletesEntriesWithoutReorderingCommonEntries() {
        val routineEntries = defaultStrengthRoutines().first().entries
        val replacement = defaultStrengthRoutineEntry(
            id = 99,
            exercise = strengthExerciseCatalog.last()
        ).copy(note = "새 운동 기본 상세")
        val workoutEntries = listOf(
            routineEntries[1].copy(equipment = "덤벨", note = "선택하지 않은 상세"),
            routineEntries[0],
            replacement
        )

        val merged = requireNotNull(
            mergeStrengthRoutineUpdates(
                routineEntries = routineEntries,
                workoutEntries = workoutEntries,
                selection = StrengthRoutineUpdateSelection(exerciseTypes = true)
            )
        )

        assertEquals(listOf(routineEntries[0].id, routineEntries[1].id, replacement.id), merged.map { it.id })
        assertEquals("덤벨", merged[1].equipment)
        assertEquals(routineEntries[1].note, merged[1].note)
        assertEquals("새 운동 기본 상세", merged[2].note)
        assertFalse(merged.any { it.id == routineEntries[2].id })
    }

    @Test
    fun mergeIgnoresStaleSelectionWhenCategoryNoLongerChanged() {
        val routineEntries = defaultStrengthRoutines().first().entries

        assertNull(
            mergeStrengthRoutineUpdates(
                routineEntries = routineEntries,
                workoutEntries = routineEntries,
                selection = StrengthRoutineUpdateSelection(order = true)
            )
        )
    }

    @Test
    fun mergeOrderOnlyDoesNotClaimUpdateForUnselectedAddedExercise() {
        val routineEntries = defaultStrengthRoutines().first().entries
        val addedEntry = defaultStrengthRoutineEntry(
            id = 99,
            exercise = strengthExerciseCatalog.last()
        )
        val workoutEntries = listOf(routineEntries[0], addedEntry) + routineEntries.drop(1)

        assertNull(
            mergeStrengthRoutineUpdates(
                routineEntries = routineEntries,
                workoutEntries = workoutEntries,
                selection = StrengthRoutineUpdateSelection(order = true)
            )
        )
    }
}

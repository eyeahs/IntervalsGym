package com.lighthousepark.intervalsgym.strength

import org.junit.Assert.assertEquals
import org.junit.Test

class StrengthRoutineRecordsTest {
    @Test
    fun setRecordChange_propagatesOnlyToFollowingSets() {
        val entry = defaultStrengthRoutineEntry(
            id = 1,
            exercise = strengthExerciseCatalog.first { it.id == "squat" },
            weightKg = "60",
            reps = "8",
            restSeconds = "90"
        )
        val changed = entry.records[1].copy(weightKg = "70", reps = "6", restSeconds = "120")

        val next = entry.withPropagatedRecordChange(1, changed)

        assertEquals("60", next.records[0].weightKg)
        assertEquals("8", next.records[0].reps)
        assertEquals("70", next.records[1].weightKg)
        assertEquals("6", next.records[1].reps)
        assertEquals("70", next.records[2].weightKg)
        assertEquals("6", next.records[2].reps)
        assertEquals("120", next.records[2].restSeconds)
    }

    @Test
    fun workoutCopiesResetActualValuesAndAppliedResultsPromoteOnlyCompletedActuals() {
        val entry = defaultStrengthRoutineEntry(
            id = 1,
            exercise = strengthExerciseCatalog.first { it.id == "squat" },
            weightKg = "60",
            reps = "8"
        ).let { source ->
            source.copy(
                records = source.records.mapIndexed { index, record ->
                    when (index) {
                        0 -> record.copy(
                            actualWeightKg = "67.5",
                            actualReps = "6",
                            completed = true
                        )
                        1 -> record.copy(actualWeightKg = "70", actualReps = "5")
                        else -> record
                    }
                }
            )
        }

        val nextWorkout = entry.copyForWorkout()
        val appliedRoutine = entry.copyWorkoutResultToRoutine()

        assertEquals(listOf("", "", ""), nextWorkout.records.map { it.actualWeightKg })
        assertEquals(listOf(false, false, false), nextWorkout.records.map { it.completed })
        assertEquals("67.5", appliedRoutine.records[0].weightKg)
        assertEquals("6", appliedRoutine.records[0].reps)
        assertEquals("60", appliedRoutine.records[1].weightKg)
        assertEquals("8", appliedRoutine.records[1].reps)
        assertEquals(listOf("", "", ""), appliedRoutine.records.map { it.actualWeightKg })
    }

    @Test
    fun actualRecordReplacementDoesNotChangeOtherPlannedSets() {
        val entry = defaultStrengthRoutineEntry(
            id = 1,
            exercise = strengthExerciseCatalog.first { it.id == "squat" },
            weightKg = "60",
            reps = "8"
        ).let { source ->
            source.copy(
                records = source.records.mapIndexed { index, record ->
                    if (index == 2) record.copy(weightKg = "70", reps = "5") else record
                }
            )
        }

        val changed = entry.records[1].copy(actualWeightKg = "65", actualReps = "6")
        val updated = entry.withRecordReplaced(recordIndex = 1, record = changed)

        assertEquals(listOf("60", "60", "70"), updated.records.map { it.weightKg })
        assertEquals(listOf("8", "8", "5"), updated.records.map { it.reps })
        assertEquals("65", updated.records[1].actualWeightKg)
        assertEquals("6", updated.records[1].actualReps)
    }
}

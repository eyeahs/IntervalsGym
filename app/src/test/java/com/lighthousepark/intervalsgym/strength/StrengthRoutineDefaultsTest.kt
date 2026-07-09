package com.lighthousepark.intervalsgym.strength

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class StrengthRoutineDefaultsTest {
    @Test
    fun defaultStrengthEntry_usesTenKgExceptBodyweight() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val pushUp = strengthExerciseCatalog.first { it.id == "push_up" }

        val weightedEntry = defaultStrengthRoutineEntry(id = 1, exercise = squat)
        val bodyweightEntry = defaultStrengthRoutineEntry(id = 2, exercise = pushUp)

        assertEquals("10", weightedEntry.targetWeightKg)
        assertEquals(listOf("10", "10", "10"), weightedEntry.records.map { it.weightKg })
        assertEquals("", bodyweightEntry.targetWeightKg)
        assertEquals(listOf("", "", ""), bodyweightEntry.records.map { it.weightKg })
    }

    @Test
    fun nextStrengthWorkoutRoutineId_doesNotReuseDeletedRoutineIdsStillReferencedByHistory() {
        val existingRoutine = defaultStrengthRoutines().first().copy(id = 1)
        val deletedRoutineHistory = completedStrengthSession(
            id = "deleted-routine-workout",
            routineId = 2,
            startedAtMillis = 1_000L,
            entries = existingRoutine.entries,
            setEvents = emptyList()
        )

        val nextId = nextStrengthWorkoutRoutineId(
            routines = listOf(existingRoutine),
            history = listOf(deletedRoutineHistory)
        )

        assertEquals(3, nextId)
    }

    @Test
    fun nextStrengthWorkoutRoutineId_reservesScheduledAndActiveRoutineIds() {
        val existingRoutine = defaultStrengthRoutines().first().copy(id = 1)
        val scheduledRoutine = ScheduledStrengthRoutine(
            id = "scheduled",
            date = LocalDate.of(2026, 7, 1),
            routine = existingRoutine.copy(id = 4),
            uploadedToIntervals = false,
            externalId = "scheduled-external"
        )
        val activeSession = ActiveStrengthSession(
            routineId = 5,
            routineName = "active",
            entries = emptyList(),
            hasStarted = false,
            sessionStartedAtMillis = 0L,
            isSetScreenVisible = false,
            currentExerciseIndex = 0,
            currentSetIndex = 0,
            pendingExerciseIndex = null,
            pendingSetIndex = null,
            restEndAtMillis = 0L,
            isRestSheetVisible = false,
            restTitle = "",
            setEvents = emptyList(),
            restEvents = emptyList(),
            activeRestEventId = null
        )

        val nextId = nextStrengthWorkoutRoutineId(
            routines = listOf(existingRoutine),
            scheduledRoutines = listOf(scheduledRoutine),
            activeSession = activeSession,
            reservedIds = listOf(6)
        )

        assertEquals(7, nextId)
    }
}

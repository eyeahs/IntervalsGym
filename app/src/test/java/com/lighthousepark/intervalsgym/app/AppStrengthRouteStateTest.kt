package com.lighthousepark.intervalsgym.app

import com.lighthousepark.intervalsgym.strength.completedStrengthSession
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import com.lighthousepark.intervalsgym.strength.withCompletedRecord
import com.lighthousepark.intervalsgym.training.trainingItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppStrengthRouteStateTest {
    @Test
    fun workoutResultAppliedToRoutineResetsCompletedSetFlagsForNextWorkout() {
        val routine = defaultStrengthRoutines().first()
        val completedEntry = routine.entries.first().withCompletedRecord(setIndex = 0)
        val workout = completedStrengthSession(
            id = "workout",
            routineId = routine.id,
            startedAtMillis = 1_000L,
            entries = listOf(completedEntry),
            setEvents = emptyList()
        )

        val updatedRoutine = listOf(routine).withWorkoutResultApplied(workout).single()

        assertFalse(updatedRoutine.entries.single().records.first().completed)
    }

    @Test
    fun historyWorkoutRouteOverrideUsesWorkoutEntriesButClearsCompletion() {
        val routine = defaultStrengthRoutines().first()
        val completedEntry = routine.entries.first().withCompletedRecord(setIndex = 0)
        val workout = completedStrengthSession(
            id = "history",
            routineId = 44,
            startedAtMillis = 1_000L,
            entries = listOf(completedEntry),
            setEvents = emptyList()
        ).copy(routineName = "지난 운동")

        val override = workout.toRouteStrengthRoutineOverride()

        assertEquals(44, override.id)
        assertEquals("지난 운동", override.name)
        assertFalse(override.entries.single().records.first().completed)
    }

    @Test
    fun saveResultAssignsNewRoutineIdAndUpdatesRouteSelectionsTogether() {
        val draft = defaultStrengthRoutines().first().copy(id = 0, name = "새 루틴")

        val result = appStrengthRoutineSaveResult(
            routine = draft,
            newRoutineId = 77,
            currentRoutines = emptyList(),
            selectedStrengthRoutineId = 0,
            selectedStrengthRoutineOverride = draft,
            editingStrengthRoutineId = 0
        )

        assertEquals(77, result.savedRoutine.id)
        assertEquals(listOf(result.savedRoutine), result.routines)
        assertEquals(77, result.selectedStrengthRoutineId)
        assertEquals(77, result.selectedStrengthRoutineOverride?.id)
        assertEquals(77, result.editingStrengthRoutineId)
    }

    @Test
    fun deletedCalendarRoutineIdsAreDistinctAcrossLocalAndRemoteIds() {
        val routine = trainingItem(id = "same", remoteId = "same")

        val result = listOf("same").withDeletedCalendarRoutineIds(routine)

        assertEquals(listOf("same"), result)
    }

    @Test
    fun deletedStrengthRoutineClearsMatchingSelectionAndActiveSessionOnly() {
        val routine = defaultStrengthRoutines().first()
        val activeSession = com.lighthousepark.intervalsgym.strength.ActiveStrengthSession(
            routineId = routine.id,
            routineName = routine.name,
            entries = routine.entries,
            hasStarted = true,
            sessionStartedAtMillis = 1_000L,
            isSetScreenVisible = true,
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

        assertEquals(null, routine.id.withoutDeletedStrengthRoutine(routine))
        assertTrue(activeSession.isForRoutine(routine))
    }
}

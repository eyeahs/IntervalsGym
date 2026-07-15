package com.lighthousepark.intervalsgym.app

import com.lighthousepark.intervalsgym.strength.ActiveStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
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
    fun activeSessionRouteKeepsStoredRoutineAsUpdateBaseline() {
        val routine = defaultStrengthRoutines().first()
        val activeSession = activeStrengthSessionForRouteTest(
            routine = routine,
            activeEntries = routine.entries.reversed()
        )

        val selected = strengthSessionRoutine(
            activeSession = activeSession,
            selectedRoutineOverride = null,
            routines = listOf(routine),
            selectedRoutineId = routine.id
        )

        assertEquals(routine, selected)
        assertEquals(routine.entries.reversed(), activeSession.entries)
    }

    @Test
    fun activeSessionRouteFallsBackToPersistedRoutineBaseline() {
        val routine = defaultStrengthRoutines().first()
        val activeSession = activeStrengthSessionForRouteTest(
            routine = routine,
            activeEntries = routine.entries.reversed()
        )

        val selected = strengthSessionRoutine(
            activeSession = activeSession,
            selectedRoutineOverride = null,
            routines = emptyList(),
            selectedRoutineId = routine.id
        )

        assertEquals(routine, selected)
    }

    @Test
    fun workoutResultAppliedToRoutineResetsCompletedSetFlagsForNextWorkout() {
        val routine = defaultStrengthRoutines().first()
        val completedEntry = routine.entries.first().withCompletedRecord(setIndex = 0).let { entry ->
            entry.copy(
                records = entry.records.mapIndexed { index, record ->
                    if (index == 0) {
                        record.copy(actualWeightKg = "72.5", actualReps = "6")
                    } else {
                        record
                    }
                }
            )
        }
        val workout = completedStrengthSession(
            id = "workout",
            routineId = routine.id,
            startedAtMillis = 1_000L,
            entries = listOf(completedEntry),
            setEvents = emptyList()
        )

        val updatedRoutine = listOf(routine).withWorkoutResultApplied(workout).single()

        assertFalse(updatedRoutine.entries.single().records.first().completed)
        assertEquals("72.5", updatedRoutine.entries.single().records.first().weightKg)
        assertEquals("6", updatedRoutine.entries.single().records.first().reps)
        assertEquals("", updatedRoutine.entries.single().records.first().actualWeightKg)
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
    fun workoutResultAppliesSelectiveRoutineSnapshotInsteadOfPerformedEntries() {
        val routine = defaultStrengthRoutines().first()
        val performedEntry = routine.entries.first().withCompletedRecord(setIndex = 0).let { entry ->
            entry.copy(
                records = entry.records.mapIndexed { index, record ->
                    if (index == 0) {
                        record.copy(actualWeightKg = "99", actualReps = "1")
                    } else {
                        record
                    }
                }
            )
        }
        val selectedRoutineSnapshot = routine.entries.reversed().mapIndexed { index, entry ->
            if (index == 0) entry.copy(note = "선택한 변경") else entry
        }
        val workout = completedStrengthSession(
            id = "selective-update",
            routineId = routine.id,
            startedAtMillis = 1_000L,
            entries = listOf(performedEntry),
            setEvents = emptyList()
        ).copy(routineUpdateEntries = selectedRoutineSnapshot)

        val updatedRoutine = listOf(routine).withWorkoutResultApplied(workout).single()

        assertEquals(selectedRoutineSnapshot.map { it.id }, updatedRoutine.entries.map { it.id })
        assertEquals("선택한 변경", updatedRoutine.entries.first().note)
        assertTrue(updatedRoutine.entries.all { entry -> entry.records.none { it.completed } })
        assertTrue(updatedRoutine.entries.all { entry -> entry.records.none { it.actualWeightKg.isNotBlank() } })
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
        val activeSession = ActiveStrengthSession(
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

    private fun activeStrengthSessionForRouteTest(
        routine: StrengthWorkoutRoutine,
        activeEntries: List<StrengthRoutineEntry>,
    ): ActiveStrengthSession {
        return ActiveStrengthSession(
            routineId = routine.id,
            routineName = routine.name,
            entries = activeEntries,
            hasStarted = true,
            sessionStartedAtMillis = 1_000L,
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
            activeRestEventId = null,
            routineBaselineEntries = routine.entries
        )
    }
}

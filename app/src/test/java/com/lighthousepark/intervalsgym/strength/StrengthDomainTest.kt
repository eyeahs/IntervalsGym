package com.lighthousepark.intervalsgym.strength

import org.junit.Assert.assertEquals
import org.junit.Test

class StrengthDomainTest {
    @Test
    fun activeSessionToWorkoutRoutine_keepsOriginalRoutineBaseline() {
        val routine = defaultStrengthRoutines().first()
        val activeEntries = routine.entries.reversed()
        val session = ActiveStrengthSession(
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

        assertEquals(routine, session.toWorkoutRoutine())
        assertEquals(activeEntries, session.entries)
    }
}

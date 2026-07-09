package com.lighthousepark.intervalsgym.strength

import org.junit.Assert.assertEquals
import org.junit.Test

class StrengthDomainTest {
    @Test
    fun activeSessionToWorkoutRoutine_keepsRoutineIdentityAndEntries() {
        val routine = defaultStrengthRoutines().first()
        val session = ActiveStrengthSession(
            routineId = routine.id,
            routineName = routine.name,
            entries = routine.entries,
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
            activeRestEventId = null
        )

        assertEquals(routine, session.toWorkoutRoutine())
    }
}

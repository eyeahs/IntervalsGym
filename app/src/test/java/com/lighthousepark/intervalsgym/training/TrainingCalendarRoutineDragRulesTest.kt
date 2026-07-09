package com.lighthousepark.intervalsgym.training

import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingCalendarRoutineDragRulesTest {
    @Test
    fun canDragCalendarRoutine_allowsRemoteRoutineAndPairedRoutineWhenLoggedIn() {
        val remoteRoutine = trainingItem(
            id = "routine-remote-1",
            type = "Run",
            isRoutine = true
        )
        val resultWithRoutine = trainingItem(
            id = "activity-1",
            type = "Run",
            isRoutine = false
        ).copy(pairedRoutine = remoteRoutine)

        assertTrue(remoteRoutine.canDragCalendarRoutine(emptySet(), canMoveRemoteRoutines = true))
        assertTrue(resultWithRoutine.canDragCalendarRoutine(emptySet(), canMoveRemoteRoutines = true))
        assertSame(remoteRoutine, resultWithRoutine.calendarRoutineForMove())
    }

    @Test
    fun canDragCalendarRoutine_blocksUnmatchedRemoteRoutineWhenLoggedOut() {
        val remoteRoutine = trainingItem(
            id = "routine-remote-1",
            type = "Ride",
            isRoutine = true
        )

        assertFalse(remoteRoutine.canDragCalendarRoutine(emptySet(), canMoveRemoteRoutines = false))
    }
}

package com.lighthousepark.intervalsgym.training.ui

import com.lighthousepark.intervalsgym.training.trainingItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingCalendarListItemDragStateTest {
    @Test
    fun localRoutineCanDragWhenItsKeyIsMovable() {
        val routine = trainingItem(
            id = "local-42",
            remoteId = "local-42",
            isRoutine = true
        )

        val state = routine.trainingCalendarItemDragState(
            pendingApiMoveRoutineKeys = emptySet(),
            movableRoutineKeys = setOf("42"),
            canMoveRemoteRoutines = false,
            draggingRoutineId = null
        )

        assertEquals(routine, state.movableRoutine)
        assertFalse(state.isApiPendingMove)
        assertTrue(state.canDragRoutine)
        assertFalse(state.isDragging)
        assertEquals(1f, state.alpha, FLOAT_TOLERANCE)
    }

    @Test
    fun pendingMoveRoutineCannotDragAndIsDimmed() {
        val routine = trainingItem(
            id = "local-42",
            remoteId = "local-42",
            isRoutine = true
        )

        val state = routine.trainingCalendarItemDragState(
            pendingApiMoveRoutineKeys = setOf("local-42"),
            movableRoutineKeys = setOf("42"),
            canMoveRemoteRoutines = false,
            draggingRoutineId = null
        )

        assertTrue(state.isApiPendingMove)
        assertFalse(state.canDragRoutine)
        assertEquals(0.5f, state.alpha, FLOAT_TOLERANCE)
    }

    @Test
    fun draggingRoutineUsesDragAlpha() {
        val routine = trainingItem(
            id = "routine-1",
            remoteId = "remote-1",
            isRoutine = true
        )

        val state = routine.trainingCalendarItemDragState(
            pendingApiMoveRoutineKeys = emptySet(),
            movableRoutineKeys = emptySet(),
            canMoveRemoteRoutines = true,
            draggingRoutineId = routine.id
        )

        assertTrue(state.canDragRoutine)
        assertTrue(state.isDragging)
        assertEquals(0.2f, state.alpha, FLOAT_TOLERANCE)
    }

    private companion object {
        const val FLOAT_TOLERANCE = 0.0001f
    }
}

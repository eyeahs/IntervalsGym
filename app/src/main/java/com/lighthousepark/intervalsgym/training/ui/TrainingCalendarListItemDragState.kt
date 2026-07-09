package com.lighthousepark.intervalsgym.training.ui

import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.calendarRoutineForMove
import com.lighthousepark.intervalsgym.training.canDragCalendarRoutine
import com.lighthousepark.intervalsgym.training.isApiPendingMove

internal data class TrainingCalendarItemDragState(
    val movableRoutine: TrainingItem,
    val isApiPendingMove: Boolean,
    val canDragRoutine: Boolean,
    val isDragging: Boolean,
    val alpha: Float,
)

internal fun TrainingItem.trainingCalendarItemDragState(
    pendingApiMoveRoutineKeys: Set<String>,
    movableRoutineKeys: Set<String>,
    canMoveRemoteRoutines: Boolean,
    draggingRoutineId: String?,
): TrainingCalendarItemDragState {
    val movableRoutine = calendarRoutineForMove() ?: this
    val isPendingMove = isApiPendingMove(pendingApiMoveRoutineKeys)
    val canDrag = !isPendingMove && canDragCalendarRoutine(
        movableLocalRoutineKeys = movableRoutineKeys,
        canMoveRemoteRoutines = canMoveRemoteRoutines
    )
    val isDragging = draggingRoutineId == movableRoutine.id
    return TrainingCalendarItemDragState(
        movableRoutine = movableRoutine,
        isApiPendingMove = isPendingMove,
        canDragRoutine = canDrag,
        isDragging = isDragging,
        alpha = trainingCalendarItemRowAlpha(
            isDragging = isDragging,
            isApiPendingMove = isPendingMove
        )
    )
}

internal fun trainingCalendarItemRowAlpha(
    isDragging: Boolean,
    isApiPendingMove: Boolean,
): Float {
    return when {
        isDragging -> 0.2f
        isApiPendingMove -> 0.5f
        else -> 1f
    }
}

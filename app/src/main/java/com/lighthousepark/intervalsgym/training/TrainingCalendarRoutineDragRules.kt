package com.lighthousepark.intervalsgym.training

internal fun TrainingItem.calendarRoutineForMove(): TrainingItem? {
    return when {
        isRoutine -> this
        pairedRoutine?.isRoutine == true -> pairedRoutine
        else -> null
    }
}

internal fun TrainingItem.canDragCalendarRoutine(
    movableLocalRoutineKeys: Set<String>,
    canMoveRemoteRoutines: Boolean,
): Boolean {
    val routine = calendarRoutineForMove() ?: return false
    val isMovableLocalStrengthRoutine = listOfNotNull(
        routine.id,
        routine.id.removePrefix("local-"),
        routine.remoteId,
        routine.externalId
    ).any { key -> key in movableLocalRoutineKeys }
    val isMovableRemoteRoutine = canMoveRemoteRoutines &&
        routine.id.startsWith("routine-") &&
        routine.remoteId.isNotBlank()
    return isMovableLocalStrengthRoutine || isMovableRemoteRoutine
}

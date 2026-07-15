package com.lighthousepark.intervalsgym.app

import com.lighthousepark.intervalsgym.strength.ActiveStrengthSession
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.appliedRoutineEntries
import com.lighthousepark.intervalsgym.training.TrainingItem

internal data class AppStrengthRoutineSaveResult(
    val savedRoutine: StrengthWorkoutRoutine,
    val routines: List<StrengthWorkoutRoutine>,
    val selectedStrengthRoutineId: Int?,
    val selectedStrengthRoutineOverride: StrengthWorkoutRoutine?,
    val editingStrengthRoutineId: Int?,
)

internal fun strengthSessionRoutine(
    activeSession: ActiveStrengthSession?,
    selectedRoutineOverride: StrengthWorkoutRoutine?,
    routines: List<StrengthWorkoutRoutine>,
    selectedRoutineId: Int?,
): StrengthWorkoutRoutine? {
    if (activeSession != null) {
        return selectedRoutineOverride?.takeIf { it.id == activeSession.routineId }
            ?: routines.firstOrNull { it.id == activeSession.routineId }
            ?: activeSession.toWorkoutRoutine()
    }
    return selectedRoutineOverride
        ?: routines.firstOrNull { it.id == selectedRoutineId }
}

internal fun List<String>.withDeletedCalendarRoutineIds(
    routine: TrainingItem,
): List<String> {
    return (this + routine.id + routine.remoteId).distinct()
}

internal fun List<StrengthWorkoutRoutine>.withWorkoutResultApplied(
    workout: CompletedStrengthSession,
): List<StrengthWorkoutRoutine> {
    if (workout.routineId == 0) return this
    val nextEntries = workout.appliedRoutineEntries()
    return map { routine ->
        if (routine.id == workout.routineId) {
            routine.copy(entries = nextEntries)
        } else {
            routine
        }
    }
}

internal fun CompletedStrengthSession.toRouteStrengthRoutineOverride(): StrengthWorkoutRoutine {
    return StrengthWorkoutRoutine(
        id = routineId,
        name = routineName,
        entries = appliedRoutineEntries()
    )
}

internal fun appStrengthRoutineSaveResult(
    routine: StrengthWorkoutRoutine,
    newRoutineId: Int,
    currentRoutines: List<StrengthWorkoutRoutine>,
    selectedStrengthRoutineId: Int?,
    selectedStrengthRoutineOverride: StrengthWorkoutRoutine?,
    editingStrengthRoutineId: Int?,
): AppStrengthRoutineSaveResult {
    val savedRoutine = if (routine.id == 0) {
        routine.copy(id = newRoutineId)
    } else {
        routine
    }
    val nextRoutines = when {
        routine.id == 0 -> currentRoutines + savedRoutine
        currentRoutines.any { it.id == routine.id } ->
            currentRoutines.map { if (it.id == routine.id) savedRoutine else it }
        else -> currentRoutines + savedRoutine
    }
    return AppStrengthRoutineSaveResult(
        savedRoutine = savedRoutine,
        routines = nextRoutines,
        selectedStrengthRoutineId = if (selectedStrengthRoutineId == routine.id) {
            savedRoutine.id
        } else {
            selectedStrengthRoutineId
        },
        selectedStrengthRoutineOverride = if (selectedStrengthRoutineOverride?.id == routine.id) {
            savedRoutine
        } else {
            selectedStrengthRoutineOverride
        },
        editingStrengthRoutineId = if (editingStrengthRoutineId == routine.id) {
            savedRoutine.id
        } else {
            editingStrengthRoutineId
        }
    )
}

internal fun List<StrengthWorkoutRoutine>.withoutStrengthRoutine(
    routine: StrengthWorkoutRoutine,
): List<StrengthWorkoutRoutine> {
    return filterNot { it.id == routine.id }
}

internal fun Int?.withoutDeletedStrengthRoutine(
    routine: StrengthWorkoutRoutine,
): Int? {
    return if (this == routine.id) null else this
}

internal fun ActiveStrengthSession?.isForRoutine(
    routine: StrengthWorkoutRoutine,
): Boolean {
    return this?.routineId == routine.id
}

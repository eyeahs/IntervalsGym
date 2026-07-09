package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.strength.ActiveStrengthSession
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.copyForWorkout

internal fun List<StrengthWorkoutRoutine>.withLatestCompletedSession(
    history: List<CompletedStrengthSession>,
): List<StrengthWorkoutRoutine> {
    if (isEmpty() || history.isEmpty()) return this
    val latestByRoutineId = history
        .filter { it.appliedToRoutine && it.routineId != 0 && it.entries.isNotEmpty() }
        .groupBy { it.routineId }
        .mapValues { (_, workouts) -> workouts.maxByOrNull { it.startedAtMillis } }

    return map { routine ->
        val latestWorkout = latestByRoutineId[routine.id] ?: return@map routine
        routine.copy(entries = latestWorkout.entries.map { it.copyForWorkout() })
    }
}

internal fun ActiveStrengthSession.withLatestCompletedSession(
    history: List<CompletedStrengthSession>,
): ActiveStrengthSession {
    if (hasStarted || history.isEmpty()) return this
    val latestWorkout = history
        .filter { it.appliedToRoutine && it.routineId == routineId && it.entries.isNotEmpty() }
        .maxByOrNull { it.startedAtMillis }
        ?: return this
    return copy(entries = latestWorkout.entries.map { it.copyForWorkout() })
}

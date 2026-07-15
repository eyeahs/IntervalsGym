package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.ActiveStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthRestEvent
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthSetCompletionEvent
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine

/**
 * Snapshot of every mutable screen field needed to persist and restore an active strength session.
 * Keep this field list in sync with live result snapshots so set/rest event state survives process death.
 */
internal data class StrengthActiveSessionSnapshot(
    val routine: StrengthWorkoutRoutine?,
    val entries: List<StrengthRoutineEntry>,
    val hasStarted: Boolean,
    val sessionStartedAtMillis: Long,
    val isSetScreenVisible: Boolean,
    val currentExerciseIndex: Int,
    val currentSetIndex: Int,
    val pendingExerciseIndex: Int?,
    val pendingSetIndex: Int?,
    val restEndAtMillis: Long,
    val isRestSheetVisible: Boolean,
    val restTitle: String,
    val setEvents: List<StrengthSetCompletionEvent>,
    val restEvents: List<StrengthRestEvent>,
    val activeRestEventId: Int?,
) {
    fun toActiveSession(): ActiveStrengthSession? {
        val workoutRoutine = routine ?: return null
        if (!hasStarted) return null
        return ActiveStrengthSession(
            routineId = workoutRoutine.id,
            routineName = workoutRoutine.name,
            entries = entries,
            hasStarted = hasStarted,
            sessionStartedAtMillis = sessionStartedAtMillis,
            isSetScreenVisible = isSetScreenVisible,
            currentExerciseIndex = currentExerciseIndex,
            currentSetIndex = currentSetIndex,
            pendingExerciseIndex = pendingExerciseIndex,
            pendingSetIndex = pendingSetIndex,
            restEndAtMillis = restEndAtMillis,
            isRestSheetVisible = isRestSheetVisible,
            restTitle = restTitle,
            setEvents = setEvents,
            restEvents = restEvents,
            activeRestEventId = activeRestEventId,
            routineBaselineEntries = workoutRoutine.entries
        )
    }
}

package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.StrengthRestEvent
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthSetCompletionEvent
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine

/**
 * One read-only view of the routed screen fields that feed persistence, live
 * result saving, finish saving, and set/rest transition helpers.
 */
internal data class StrengthSessionRuntimeSnapshot(
    val routine: StrengthWorkoutRoutine?,
    val entries: List<StrengthRoutineEntry>,
    val hasStarted: Boolean,
    val sessionStartedAtMillis: Long,
    val navigationUiState: StrengthSessionNavigationUiState,
    val restUiState: StrengthRestUiState,
    val setEvents: List<StrengthSetCompletionEvent>,
    val restEvents: List<StrengthRestEvent>,
    val finishUiState: StrengthSessionFinishUiState,
) {
    fun toInteractionState(): StrengthSessionInteractionState {
        return StrengthSessionInteractionState(
            entries = entries,
            setEvents = setEvents,
            restEvents = restEvents,
            restUiState = restUiState,
            navigationUiState = navigationUiState
        )
    }

    fun toResultSnapshot(): StrengthSessionResultSnapshot {
        return StrengthSessionResultSnapshot(
            routine = routine,
            entries = entries,
            setEvents = setEvents,
            restEvents = restEvents,
            activeRestEventId = restUiState.activeRestEventId,
            sessionStartedAtMillis = sessionStartedAtMillis,
            finishRpe = finishUiState.finishRpe,
            applyWorkoutResultToRoutine = finishUiState.applyWorkoutResultToRoutine
        )
    }

    fun toActiveSessionSnapshot(): StrengthActiveSessionSnapshot {
        return StrengthActiveSessionSnapshot(
            routine = routine,
            entries = entries,
            hasStarted = hasStarted,
            sessionStartedAtMillis = sessionStartedAtMillis,
            isSetScreenVisible = navigationUiState.isSetScreenVisible,
            currentExerciseIndex = navigationUiState.currentExerciseIndex,
            currentSetIndex = navigationUiState.currentSetIndex,
            pendingExerciseIndex = navigationUiState.pendingExerciseIndex,
            pendingSetIndex = navigationUiState.pendingSetIndex,
            restEndAtMillis = restUiState.endAtMillis,
            isRestSheetVisible = restUiState.isSheetVisible,
            restTitle = restUiState.title,
            setEvents = setEvents,
            restEvents = restEvents,
            activeRestEventId = restUiState.activeRestEventId
        )
    }
}

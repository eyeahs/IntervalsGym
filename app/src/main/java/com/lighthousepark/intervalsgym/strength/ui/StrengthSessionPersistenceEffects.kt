package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.lighthousepark.intervalsgym.strength.StrengthRestEvent
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthSetCompletionEvent

@Composable
internal fun StrengthLiveResultPersistenceEffect(
    hasStarted: Boolean,
    routineId: Int?,
    entries: List<StrengthRoutineEntry>,
    setEvents: List<StrengthSetCompletionEvent>,
    restEvents: List<StrengthRestEvent>,
    activeRestEventId: Int?,
    sessionStartedAtMillis: Long,
    finishRpe: Int,
    applyWorkoutResultToRoutine: Boolean,
    onPersistLiveResult: () -> Unit,
) {
    val currentOnPersistLiveResult by rememberUpdatedState(onPersistLiveResult)
    LaunchedEffect(
        hasStarted,
        routineId,
        entries,
        setEvents,
        restEvents,
        activeRestEventId,
        sessionStartedAtMillis,
        finishRpe,
        applyWorkoutResultToRoutine
    ) {
        if (!hasStarted || routineId == null || sessionStartedAtMillis <= 0L) return@LaunchedEffect
        currentOnPersistLiveResult()
    }
}

@Composable
internal fun StrengthActiveSessionPersistenceEffect(
    routineId: Int?,
    routineName: String?,
    hasStarted: Boolean,
    sessionStartedAtMillis: Long,
    isSetScreenVisible: Boolean,
    entries: List<StrengthRoutineEntry>,
    currentExerciseIndex: Int,
    currentSetIndex: Int,
    pendingExerciseIndex: Int?,
    pendingSetIndex: Int?,
    restUiState: StrengthRestUiState,
    setEvents: List<StrengthSetCompletionEvent>,
    restEvents: List<StrengthRestEvent>,
    onPersistActiveSession: () -> Unit,
) {
    val currentOnPersistActiveSession by rememberUpdatedState(onPersistActiveSession)
    LaunchedEffect(
        routineId,
        routineName,
        hasStarted,
        sessionStartedAtMillis,
        isSetScreenVisible,
        entries,
        currentExerciseIndex,
        currentSetIndex,
        pendingExerciseIndex,
        pendingSetIndex,
        restUiState.endAtMillis,
        restUiState.isSheetVisible,
        restUiState.title,
        setEvents,
        restEvents,
        restUiState.activeRestEventId
    ) {
        currentOnPersistActiveSession()
    }
}

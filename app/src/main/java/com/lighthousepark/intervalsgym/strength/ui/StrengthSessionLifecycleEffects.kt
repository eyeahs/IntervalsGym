package com.lighthousepark.intervalsgym.strength.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.copyForWorkout
import kotlinx.coroutines.delay

@Composable
internal fun StrengthStartImmediatelyEffect(
    shouldStartImmediately: Boolean,
    onImmediateStartConsumed: () -> Unit,
) {
    val currentOnImmediateStartConsumed by rememberUpdatedState(onImmediateStartConsumed)
    LaunchedEffect(shouldStartImmediately) {
        if (shouldStartImmediately) {
            currentOnImmediateStartConsumed()
        }
    }
}

@Composable
internal fun StrengthReadyRoutineEntriesEffect(
    routineEntries: List<StrengthRoutineEntry>,
    hasStarted: Boolean,
    activeSessionRoutineId: Int?,
    onEntriesChange: (List<StrengthRoutineEntry>) -> Unit,
) {
    val currentOnEntriesChange by rememberUpdatedState(onEntriesChange)
    LaunchedEffect(routineEntries, hasStarted, activeSessionRoutineId) {
        if (!hasStarted && activeSessionRoutineId == null) {
            currentOnEntriesChange(routineEntries.map { it.copyForWorkout() })
        }
    }
}

@Composable
internal fun StrengthExerciseChangeFocusEffect(
    isChangingCurrentExercise: Boolean,
    pendingAddedEntryId: Int?,
    entries: List<StrengthRoutineEntry>,
    navigationUiState: StrengthSessionNavigationUiState,
    onNavigationUiStateChange: (StrengthSessionNavigationUiState) -> Unit,
) {
    val currentOnNavigationUiStateChange by rememberUpdatedState(onNavigationUiStateChange)
    LaunchedEffect(isChangingCurrentExercise, pendingAddedEntryId, entries) {
        if (!isChangingCurrentExercise) return@LaunchedEffect
        currentOnNavigationUiStateChange(
            navigationUiState.focusExerciseChange(
                entries = entries,
                pendingAddedEntryId = pendingAddedEntryId
            )
        )
    }
}

@Composable
internal fun StrengthSessionBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    BackHandler(enabled = enabled, onBack = onBack)
}

@Composable
internal fun StrengthSessionElapsedTickerEffect(
    hasStarted: Boolean,
    sessionStartedAtMillis: Long,
    onElapsedSecondsChange: (Int) -> Unit,
) {
    val currentOnElapsedSecondsChange by rememberUpdatedState(onElapsedSecondsChange)
    LaunchedEffect(hasStarted, sessionStartedAtMillis) {
        while (hasStarted && sessionStartedAtMillis > 0L) {
            currentOnElapsedSecondsChange(
                ((System.currentTimeMillis() - sessionStartedAtMillis) / 1000L)
                    .toInt()
                    .coerceAtLeast(0)
            )
            delay(1_000)
        }
    }
}

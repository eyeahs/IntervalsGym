package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.ActiveStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthSetCompletionResult
import com.lighthousepark.intervalsgym.strength.StrengthSetMetricType
import com.lighthousepark.intervalsgym.strength.exerciseChangeFocusIndex

internal data class StrengthSessionNavigationUiState(
    val isSetScreenVisible: Boolean,
    val currentExerciseIndex: Int,
    val currentSetIndex: Int,
    val pendingExerciseIndex: Int?,
    val pendingSetIndex: Int?,
) {
    fun withSetScreenVisible(visible: Boolean): StrengthSessionNavigationUiState {
        return copy(isSetScreenVisible = visible)
    }

    fun openSet(exerciseIndex: Int, setIndex: Int): StrengthSessionNavigationUiState {
        return copy(
            isSetScreenVisible = true,
            currentExerciseIndex = exerciseIndex.coerceAtLeast(0),
            currentSetIndex = setIndex.coerceAtLeast(0),
            pendingExerciseIndex = null,
            pendingSetIndex = null
        )
    }

    fun openExerciseSet(
        entries: List<StrengthRoutineEntry>,
        exerciseIndex: Int,
    ): StrengthSessionNavigationUiState {
        val safeIndex = exerciseIndex.coerceIn(0, (entries.size - 1).coerceAtLeast(0))
        val entry = entries.getOrNull(safeIndex)
        val firstIncomplete = entry?.records?.indexOfFirst { !it.completed } ?: -1
        val targetSetIndex = when {
            firstIncomplete >= 0 -> firstIncomplete
            entry != null && entry.records.isNotEmpty() -> entry.records.lastIndex
            else -> 0
        }
        return openSet(safeIndex, targetSetIndex)
    }

    fun withCurrentExerciseIndex(exerciseIndex: Int): StrengthSessionNavigationUiState {
        return copy(currentExerciseIndex = exerciseIndex.coerceAtLeast(0))
    }

    fun withCurrentSetIndex(setIndex: Int): StrengthSessionNavigationUiState {
        return copy(currentSetIndex = setIndex.coerceAtLeast(0))
    }

    fun clampCurrentSetForEntry(entry: StrengthRoutineEntry): StrengthSessionNavigationUiState {
        if (currentSetIndex < entry.records.size) return this
        return withCurrentSetIndex((entry.records.size - 1).coerceAtLeast(0))
    }

    fun focusExerciseChange(
        entries: List<StrengthRoutineEntry>,
        pendingAddedEntryId: Int?,
    ): StrengthSessionNavigationUiState {
        val focusIndex = entries.exerciseChangeFocusIndex(
            currentExerciseIndex = currentExerciseIndex,
            pendingAddedEntryId = pendingAddedEntryId
        )
        val focusEntry = entries.getOrNull(focusIndex) ?: return this
        val targetSetIndex = if (currentSetIndex in focusEntry.records.indices) currentSetIndex else 0
        return copy(
            currentExerciseIndex = focusIndex,
            currentSetIndex = targetSetIndex
        )
    }

    fun applyCompletedSetResult(result: StrengthSetCompletionResult): StrengthSessionNavigationUiState {
        return copy(
            currentExerciseIndex = result.currentExerciseIndex,
            currentSetIndex = result.currentSetIndex,
            pendingExerciseIndex = result.pendingExerciseIndex,
            pendingSetIndex = result.pendingSetIndex
        )
    }

    fun moveToPendingSet(): StrengthSessionNavigationUiState {
        return copy(
            currentExerciseIndex = pendingExerciseIndex ?: currentExerciseIndex,
            currentSetIndex = pendingSetIndex ?: currentSetIndex,
            pendingExerciseIndex = null,
            pendingSetIndex = null
        )
    }

    fun pendingTimedSetDurationSeconds(
        entries: List<StrengthRoutineEntry>,
    ): Int? {
        val exerciseIndex = pendingExerciseIndex ?: return null
        val setIndex = pendingSetIndex ?: return null
        val entry = entries.getOrNull(exerciseIndex)
            ?.takeIf { it.setMetricType == StrengthSetMetricType.DURATION }
            ?: return null
        return entry.records
            .getOrNull(setIndex)
            ?.durationSeconds
            ?.toIntOrNull()
            ?.takeIf { it > 0 }
    }

    fun finishAllSets(): StrengthSessionNavigationUiState {
        return copy(
            isSetScreenVisible = false,
            pendingExerciseIndex = null,
            pendingSetIndex = null
        )
    }

    fun keepEntrySelectionAfterReorder(
        previousEntries: List<StrengthRoutineEntry>,
        normalizedEntries: List<StrengthRoutineEntry>,
    ): StrengthSessionNavigationUiState {
        val currentEntryId = previousEntries.getOrNull(currentExerciseIndex)?.id
        val pendingEntryId = pendingExerciseIndex?.let { previousEntries.getOrNull(it)?.id }
        val nextCurrentExerciseIndex = currentEntryId
            ?.let { id -> normalizedEntries.indexOfFirst { it.id == id }.takeIf { it >= 0 } }
            ?: currentExerciseIndex
        val nextPendingExerciseIndex = pendingEntryId
            ?.let { id -> normalizedEntries.indexOfFirst { it.id == id }.takeIf { it >= 0 } }
        return copy(
            currentExerciseIndex = nextCurrentExerciseIndex,
            pendingExerciseIndex = nextPendingExerciseIndex
        )
    }

    companion object {
        fun restored(
            activeSession: ActiveStrengthSession?,
            shouldStartImmediately: Boolean,
            nowMillis: Long,
            isRestActive: Boolean,
        ): StrengthSessionNavigationUiState {
            val isExpiredRest = activeSession != null &&
                activeSession.restEndAtMillis > 0L &&
                activeSession.restEndAtMillis <= nowMillis
            return StrengthSessionNavigationUiState(
                isSetScreenVisible = activeSession?.isSetScreenVisible ?: shouldStartImmediately,
                currentExerciseIndex = if (isExpiredRest) {
                    activeSession?.pendingExerciseIndex ?: activeSession?.currentExerciseIndex ?: 0
                } else {
                    activeSession?.currentExerciseIndex ?: 0
                },
                currentSetIndex = if (isExpiredRest) {
                    activeSession?.pendingSetIndex ?: activeSession?.currentSetIndex ?: 0
                } else {
                    activeSession?.currentSetIndex ?: 0
                },
                pendingExerciseIndex = if (isRestActive) activeSession?.pendingExerciseIndex else null,
                pendingSetIndex = if (isRestActive) activeSession?.pendingSetIndex else null
            )
        }
    }
}

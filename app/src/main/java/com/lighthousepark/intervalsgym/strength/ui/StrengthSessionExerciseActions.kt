package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthExercise
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.copyAsNewRoutineEntry
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.latestMatchingStrengthEntry
import com.lighthousepark.intervalsgym.strength.normalizeSupersetGroups
import com.lighthousepark.intervalsgym.strength.strengthExerciseCatalog

internal data class StrengthSessionExerciseActionResult(
    val transition: StrengthSessionStateTransition? = null,
    val navigationUiState: StrengthSessionNavigationUiState? = null,
    val exerciseChangeUiState: StrengthExerciseChangeUiState? = null,
)

internal fun StrengthSessionInteractionState.withOpenedExerciseSet(
    exerciseChangeUiState: StrengthExerciseChangeUiState,
    exerciseIndex: Int,
): StrengthSessionExerciseActionResult {
    return StrengthSessionExerciseActionResult(
        navigationUiState = navigationUiState.openExerciseSet(entries, exerciseIndex),
        exerciseChangeUiState = exerciseChangeUiState.clearPendingSelectionForOpenedSet()
    )
}

internal fun StrengthSessionInteractionState.withAddedExercise(
    exerciseChangeUiState: StrengthExerciseChangeUiState,
    nowMillis: Long,
): StrengthSessionExerciseActionResult {
    val nextId = (entries.maxOfOrNull { it.id } ?: 0) + 1
    val entry = defaultStrengthRoutineEntry(nextId, strengthExerciseCatalog.first())
    val nextEntries = entries + entry
    val transition = withEntriesReplaced(
        nextEntries = nextEntries,
        nowMillis = nowMillis
    )
    return StrengthSessionExerciseActionResult(
        transition = transition.copy(
            state = transition.state.copy(
                navigationUiState = transition.state.navigationUiState.openSet(nextEntries.lastIndex, 0)
            )
        ),
        exerciseChangeUiState = exerciseChangeUiState.beginAddedExercise(nextId)
    )
}

internal fun StrengthSessionInteractionState.withConfiguredExercise(
    exerciseChangeUiState: StrengthExerciseChangeUiState,
    completedStrengthHistory: List<CompletedStrengthSession>,
    exercise: StrengthExercise,
    equipment: String,
    variation: String,
    nowMillis: Long,
): StrengthSessionExerciseActionResult? {
    val targetEntryId = exerciseChangeUiState.pendingAddedExerciseEntryId
        ?: entries.getOrNull(navigationUiState.currentExerciseIndex)?.id
        ?: return null
    val targetExerciseIndex = entries.indexOfFirst { it.id == targetEntryId }.takeIf { it >= 0 } ?: return null
    val entry = entries.getOrNull(targetExerciseIndex) ?: return null
    val restoredEntry = if (entry.id == exerciseChangeUiState.pendingAddedExerciseEntryId) {
        completedStrengthHistory
            .latestMatchingStrengthEntry(exercise, equipment, variation)
            ?.copyAsNewRoutineEntry(
                id = entry.id,
                exercise = exercise,
                equipment = equipment,
                variation = variation
            )
    } else {
        null
    }
    val updatedEntry = restoredEntry ?: entry.copy(
        exercise = exercise,
        equipment = equipment,
        variation = variation
    )
    val nextEntries = entries.map { if (it.id == entry.id) updatedEntry else it }
    val baseState = copy(
        navigationUiState = navigationUiState.withCurrentExerciseIndex(targetExerciseIndex)
    )
    val transition = baseState.withEntriesReplaced(
        nextEntries = nextEntries,
        nowMillis = nowMillis
    )
    val currentEntry = transition.state.entries.getOrNull(transition.state.navigationUiState.currentExerciseIndex)
    val clampedNavigation = if (
        currentEntry?.id == updatedEntry.id &&
        transition.state.navigationUiState.currentSetIndex >= updatedEntry.records.size
    ) {
        transition.state.navigationUiState.clampCurrentSetForEntry(updatedEntry)
    } else {
        transition.state.navigationUiState
    }
    val nextNavigation = if (exerciseChangeUiState.shouldReturnToOngoingAfterExerciseChange) {
        clampedNavigation.withSetScreenVisible(false)
    } else {
        clampedNavigation
    }

    return StrengthSessionExerciseActionResult(
        transition = transition.copy(
            state = transition.state.copy(navigationUiState = nextNavigation)
        ),
        exerciseChangeUiState = exerciseChangeUiState.finish()
    )
}

internal fun StrengthSessionInteractionState.withCanceledExerciseChange(
    exerciseChangeUiState: StrengthExerciseChangeUiState,
    nowMillis: Long,
): StrengthSessionExerciseActionResult {
    val pendingAddedEntryId = exerciseChangeUiState.pendingAddedExerciseEntryId
    val nextEntries = pendingAddedEntryId?.let { addedEntryId ->
        entries.filterNot { it.id == addedEntryId }
    } ?: entries
    val transition = if (nextEntries != entries) {
        withEntriesReplaced(
            nextEntries = nextEntries,
            nowMillis = nowMillis
        )
    } else {
        null
    }
    val navigationSource = transition?.state?.navigationUiState ?: navigationUiState
    val safeExerciseIndex = navigationSource.currentExerciseIndex.coerceIn(
        minimumValue = 0,
        maximumValue = (nextEntries.size - 1).coerceAtLeast(0)
    )
    val nextNavigation = navigationSource
        .withCurrentExerciseIndex(safeExerciseIndex)
        .withCurrentSetIndex(navigationSource.currentSetIndex.coerceAtLeast(0))
        .let { state ->
            if (exerciseChangeUiState.shouldReturnToOngoingAfterExerciseChange) {
                state.withSetScreenVisible(false)
            } else {
                state
            }
        }
    return StrengthSessionExerciseActionResult(
        transition = transition,
        navigationUiState = nextNavigation,
        exerciseChangeUiState = exerciseChangeUiState.finish()
    )
}

internal fun StrengthSessionInteractionState.withReorderedExercises(
    nextEntries: List<StrengthRoutineEntry>,
    nowMillis: Long,
): StrengthSessionStateTransition? {
    if (nextEntries == entries) return null
    val previousEntries = entries
    val normalizedEntries = nextEntries.normalizeSupersetGroups()
    val transition = withEntriesReplaced(
        nextEntries = normalizedEntries,
        nowMillis = nowMillis
    )
    return transition.copy(
        state = transition.state.copy(
            navigationUiState = transition.state.navigationUiState.keepEntrySelectionAfterReorder(
                previousEntries = previousEntries,
                normalizedEntries = normalizedEntries
            )
        )
    )
}

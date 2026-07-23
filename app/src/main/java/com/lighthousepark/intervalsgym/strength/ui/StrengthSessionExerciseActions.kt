package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthExercise
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthSetMetricType
import com.lighthousepark.intervalsgym.strength.copyAsNewRoutineEntry
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.defaultStrengthWeightForEquipment
import com.lighthousepark.intervalsgym.strength.latestMatchingStrengthEntry
import com.lighthousepark.intervalsgym.strength.normalizeSupersetGroups

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
): StrengthSessionExerciseActionResult {
    val nextId = (entries.maxOfOrNull { it.id } ?: 0) + 1
    return StrengthSessionExerciseActionResult(
        exerciseChangeUiState = exerciseChangeUiState.beginAddedExercise(nextId)
    )
}

internal fun StrengthSessionInteractionState.withConfiguredExercise(
    exerciseChangeUiState: StrengthExerciseChangeUiState,
    completedStrengthHistory: List<CompletedStrengthSession>,
    exercise: StrengthExercise,
    equipment: String,
    variation: String,
    setMetricType: StrengthSetMetricType = StrengthSetMetricType.REPS,
    nowMillis: Long,
    routineLocation: String = "",
): StrengthSessionExerciseActionResult? {
    val pendingAddedEntryId = exerciseChangeUiState.pendingAddedExerciseEntryId
    val existingTargetIndex = if (pendingAddedEntryId != null) {
        entries.indexOfFirst { it.id == pendingAddedEntryId }.takeIf { it >= 0 }
    } else {
        navigationUiState.currentExerciseIndex.takeIf { it in entries.indices }
    }
    val targetEntryId = pendingAddedEntryId
        ?: existingTargetIndex?.let { entries[it].id }
        ?: return null
    val updatedEntry = if (pendingAddedEntryId != null) {
        configuredAddedStrengthEntry(
            id = targetEntryId,
            completedStrengthHistory = completedStrengthHistory,
            exercise = exercise,
            equipment = equipment,
            variation = variation,
            setMetricType = setMetricType,
            location = routineLocation
        )
    } else {
        val existingEntry = existingTargetIndex?.let { entries[it] } ?: return null
        existingEntry.copy(
            exercise = exercise,
            equipment = equipment,
            variation = variation,
            setMetricType = setMetricType
        )
    }
    val nextEntries = if (existingTargetIndex != null) {
        entries.map { if (it.id == targetEntryId) updatedEntry else it }
    } else {
        entries + updatedEntry
    }
    val targetExerciseIndex = nextEntries.indexOfFirst { it.id == targetEntryId }
        .takeIf { it >= 0 }
        ?: return null
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

private fun configuredAddedStrengthEntry(
    id: Int,
    completedStrengthHistory: List<CompletedStrengthSession>,
    exercise: StrengthExercise,
    equipment: String,
    variation: String,
    setMetricType: StrengthSetMetricType,
    location: String,
): StrengthRoutineEntry {
    return completedStrengthHistory
        .latestMatchingStrengthEntry(exercise, equipment, variation, location)
        ?.copyAsNewRoutineEntry(
            id = id,
            exercise = exercise,
            equipment = equipment,
            variation = variation
        )
        ?.copy(setMetricType = setMetricType)
        ?: defaultStrengthRoutineEntry(
            id = id,
            exercise = exercise,
            weightKg = defaultStrengthWeightForEquipment(equipment)
        ).copy(
            equipment = equipment,
            variation = variation,
            setMetricType = setMetricType
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

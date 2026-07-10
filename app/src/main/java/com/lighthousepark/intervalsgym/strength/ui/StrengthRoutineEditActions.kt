package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthExercise
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.addSelectedEntriesToSupersetGroup
import com.lighthousepark.intervalsgym.strength.copyAsNewRoutineEntry
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.defaultStrengthWeightForEquipment
import com.lighthousepark.intervalsgym.strength.groupSelectedEntriesAsSuperset
import com.lighthousepark.intervalsgym.strength.latestMatchingStrengthEntry
import com.lighthousepark.intervalsgym.strength.normalizeSupersetGroups

internal data class StrengthRoutineEntryDeleteState(
    val entries: List<StrengthRoutineEntry>,
    val pendingDeleteEntryIds: Set<Int>,
    val selectedSupersetEntryIds: Set<Int>,
    val selectedEntryId: Int?,
) {
    fun withDeleteRequested(entryId: Int): StrengthRoutineEntryDeleteState {
        return copy(
            pendingDeleteEntryIds = pendingDeleteEntryIds + entryId,
            selectedSupersetEntryIds = selectedSupersetEntryIds - entryId
        )
    }

    fun withDeleteRestored(entryId: Int): StrengthRoutineEntryDeleteState {
        return copy(pendingDeleteEntryIds = pendingDeleteEntryIds - entryId)
    }

    fun withDeleteCommitted(entryId: Int): StrengthRoutineEntryDeleteState {
        if (entryId !in pendingDeleteEntryIds) return this
        return copy(
            entries = entries
                .filterNot { it.id == entryId }
                .normalizeSupersetGroups(),
            pendingDeleteEntryIds = pendingDeleteEntryIds - entryId,
            selectedSupersetEntryIds = selectedSupersetEntryIds - entryId,
            selectedEntryId = selectedEntryId.takeUnless { it == entryId }
        )
    }
}

internal fun editableStrengthRoutine(
    routine: StrengthWorkoutRoutine?,
    routineName: String,
    entries: List<StrengthRoutineEntry>,
    pendingDeleteEntryIds: Set<Int>,
): StrengthWorkoutRoutine {
    return StrengthWorkoutRoutine(
        id = routine?.id ?: 0,
        name = routineName.trim(),
        entries = entries
            .filterNot { it.id in pendingDeleteEntryIds }
            .normalizeSupersetGroups()
    )
}

internal fun originalStrengthRoutineEditSnapshot(routine: StrengthWorkoutRoutine?): StrengthWorkoutRoutine {
    return StrengthWorkoutRoutine(
        id = routine?.id ?: 0,
        name = routine?.name.orEmpty().trim(),
        entries = routine?.entries.orEmpty().normalizeSupersetGroups()
    )
}

internal fun List<StrengthRoutineEntry>.withoutRoutineEntry(entryId: Int): List<StrengthRoutineEntry> {
    return filterNot { it.id == entryId }.normalizeSupersetGroups()
}

internal fun List<StrengthRoutineEntry>.withSelectedEntriesGroupedAsSuperset(
    selectedEntryIds: Set<Int>,
): List<StrengthRoutineEntry> {
    if (selectedEntryIds.size < 2) return this
    val nextGroupId = (mapNotNull { it.supersetGroupId }.maxOrNull() ?: 0) + 1
    return groupSelectedEntriesAsSuperset(
        selectedEntryIds = selectedEntryIds,
        supersetGroupId = nextGroupId
    ).normalizeSupersetGroups()
}

internal fun List<StrengthRoutineEntry>.withSelectedEntriesAddedToSupersetGroup(
    selectedEntryIds: Set<Int>,
    supersetGroupId: Int,
): List<StrengthRoutineEntry> {
    return addSelectedEntriesToSupersetGroup(
        selectedEntryIds = selectedEntryIds,
        supersetGroupId = supersetGroupId
    ).normalizeSupersetGroups()
}

internal fun List<StrengthRoutineEntry>.withSelectedSupersetGroupsCleared(
    selectedEntryIds: Set<Int>,
): List<StrengthRoutineEntry> {
    val selectedGroupIds = filter { it.id in selectedEntryIds }
        .mapNotNull { it.supersetGroupId }
        .toSet()
    if (selectedGroupIds.isEmpty()) return this
    return map { entry ->
        if (entry.supersetGroupId in selectedGroupIds) {
            entry.copy(supersetGroupId = null)
        } else {
            entry
        }
    }.normalizeSupersetGroups()
}

internal fun addedStrengthRoutineEntry(
    entries: List<StrengthRoutineEntry>,
    completedStrengthHistory: List<CompletedStrengthSession>,
    exercise: StrengthExercise,
    equipment: String,
    variation: String,
): StrengthRoutineEntry {
    val nextId = (entries.maxOfOrNull { it.id } ?: 0) + 1
    return completedStrengthHistory
        .latestMatchingStrengthEntry(exercise, equipment, variation)
        ?.copyAsNewRoutineEntry(
            id = nextId,
            exercise = exercise,
            equipment = equipment,
            variation = variation
        )
        ?: defaultStrengthRoutineEntry(
            id = nextId,
            exercise = exercise,
            weightKg = defaultStrengthWeightForEquipment(equipment)
        ).copy(
            equipment = equipment,
            variation = variation
        )
}

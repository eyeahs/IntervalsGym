package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthExercise
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthSetMetricType
import com.lighthousepark.intervalsgym.strength.StrengthSetGroupType
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
    routineLocation: String,
    entries: List<StrengthRoutineEntry>,
    pendingDeleteEntryIds: Set<Int>,
): StrengthWorkoutRoutine {
    return StrengthWorkoutRoutine(
        id = routine?.id ?: 0,
        name = routineName.trim(),
        entries = entries
            .filterNot { it.id in pendingDeleteEntryIds }
            .normalizeSupersetGroups(),
        location = routineLocation.trim()
    )
}

internal fun originalStrengthRoutineEditSnapshot(routine: StrengthWorkoutRoutine?): StrengthWorkoutRoutine {
    return StrengthWorkoutRoutine(
        id = routine?.id ?: 0,
        name = routine?.name.orEmpty().trim(),
        entries = routine?.entries.orEmpty().normalizeSupersetGroups(),
        location = routine?.location.orEmpty().trim()
    )
}

internal fun List<StrengthRoutineEntry>.withoutRoutineEntry(entryId: Int): List<StrengthRoutineEntry> {
    return filterNot { it.id == entryId }.normalizeSupersetGroups()
}

internal fun List<StrengthRoutineEntry>.withSelectedEntriesGroupedAsSuperset(
    selectedEntryIds: Set<Int>,
    setGroupType: StrengthSetGroupType = StrengthSetGroupType.SUPERSET,
): List<StrengthRoutineEntry> {
    if (selectedEntryIds.size < 2) return this
    val nextGroupId = (mapNotNull { it.supersetGroupId }.maxOrNull() ?: 0) + 1
    return groupSelectedEntriesAsSuperset(
        selectedEntryIds = selectedEntryIds,
        supersetGroupId = nextGroupId,
        setGroupType = setGroupType
    ).normalizeSupersetGroups()
}

internal fun List<StrengthRoutineEntry>.withSelectedEntriesAddedToSupersetGroup(
    selectedEntryIds: Set<Int>,
    supersetGroupId: Int,
    setGroupType: StrengthSetGroupType = StrengthSetGroupType.SUPERSET,
): List<StrengthRoutineEntry> {
    return addSelectedEntriesToSupersetGroup(
        selectedEntryIds = selectedEntryIds,
        supersetGroupId = supersetGroupId,
        setGroupType = setGroupType
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
            entry.copy(supersetGroupId = null, setGroupType = null)
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
    setMetricType: StrengthSetMetricType = StrengthSetMetricType.REPS,
    location: String = "",
): StrengthRoutineEntry {
    val nextId = (entries.maxOfOrNull { it.id } ?: 0) + 1
    return completedStrengthHistory
        .latestMatchingStrengthEntry(exercise, equipment, variation, location)
        ?.copyAsNewRoutineEntry(
            id = nextId,
            exercise = exercise,
            equipment = equipment,
            variation = variation
        )
        ?.copy(setMetricType = setMetricType)
        ?: defaultStrengthRoutineEntry(
            id = nextId,
            exercise = exercise,
            weightKg = defaultStrengthWeightForEquipment(equipment)
        ).copy(
            equipment = equipment,
            variation = variation,
            setMetricType = setMetricType
        )
}

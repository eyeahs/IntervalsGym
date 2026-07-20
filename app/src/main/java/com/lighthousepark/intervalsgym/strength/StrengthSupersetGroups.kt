package com.lighthousepark.intervalsgym.strength

internal fun List<StrengthRoutineEntry>.supersetGroupLabels(): Map<Int, String> {
    return mapNotNull { it.supersetGroupId }
        .distinct()
        .mapIndexed { index, groupId ->
            val groupType = first { entry -> entry.supersetGroupId == groupId }.effectiveSetGroupType()
                ?: StrengthSetGroupType.SUPERSET
            groupId to "${groupType.displayName()} ${supersetGroupName(index)}"
        }
        .toMap()
}

internal fun StrengthRoutineEntry.effectiveSetGroupType(): StrengthSetGroupType? {
    if (supersetGroupId == null) return null
    return setGroupType ?: StrengthSetGroupType.SUPERSET
}

internal fun StrengthSetGroupType.displayName(): String {
    return when (this) {
        StrengthSetGroupType.PAIRED_SET -> "페어 세트"
        StrengthSetGroupType.SUPERSET -> "슈퍼 세트"
    }
}

internal fun <T> List<T>.moveItem(fromIndex: Int, toIndex: Int): List<T> {
    if (fromIndex !in indices || toIndex !in indices || fromIndex == toIndex) return this
    return toMutableList().apply {
        add(toIndex, removeAt(fromIndex))
    }
}

internal fun List<StrengthRoutineEntry>.groupSelectedEntriesAsSuperset(
    selectedEntryIds: Set<Int>,
    supersetGroupId: Int,
    setGroupType: StrengthSetGroupType = StrengthSetGroupType.SUPERSET,
): List<StrengthRoutineEntry> {
    if (selectedEntryIds.size < 2) return this
    val selectedIdsInOrder = map { it.id }.filter { it in selectedEntryIds }
    if (selectedIdsInOrder.size < 2) return this

    val anchorId = selectedIdsInOrder.first()
    val selectedTailIds = selectedIdsInOrder.drop(1).toSet()
    val groupedEntries = map { entry ->
        if (entry.id in selectedEntryIds) {
            entry.copy(
                supersetGroupId = supersetGroupId,
                setGroupType = setGroupType
            )
        } else {
            entry
        }
    }
    val selectedTailEntries = groupedEntries.filter { it.id in selectedTailIds }
    val anchoredEntries = groupedEntries.filterNot { it.id in selectedTailIds }
    val anchorIndex = anchoredEntries.indexOfFirst { it.id == anchorId }
    if (anchorIndex < 0) return groupedEntries

    return anchoredEntries.take(anchorIndex + 1) +
        selectedTailEntries +
        anchoredEntries.drop(anchorIndex + 1)
}

internal fun List<StrengthRoutineEntry>.addSelectedEntriesToSupersetGroup(
    selectedEntryIds: Set<Int>,
    supersetGroupId: Int,
    setGroupType: StrengthSetGroupType = StrengthSetGroupType.SUPERSET,
): List<StrengthRoutineEntry> {
    val existingGroupEntries = filter { it.supersetGroupId == supersetGroupId }
    if (existingGroupEntries.size < 2) return this

    val entriesWithUpdatedGroupType = map { entry ->
        if (entry.supersetGroupId == supersetGroupId) {
            entry.copy(setGroupType = setGroupType)
        } else {
            entry
        }
    }
    val addedEntries = entriesWithUpdatedGroupType.filter { entry ->
        entry.id in selectedEntryIds && entry.supersetGroupId == null
    }
    if (addedEntries.isEmpty()) return entriesWithUpdatedGroupType

    val addedEntryIds = addedEntries.map { it.id }.toSet()
    val entriesWithoutAdditions = entriesWithUpdatedGroupType.filterNot { it.id in addedEntryIds }
    val groupEndIndex = entriesWithoutAdditions.indexOfLast { it.supersetGroupId == supersetGroupId }
    if (groupEndIndex < 0) return this

    return entriesWithoutAdditions.take(groupEndIndex + 1) +
        addedEntries.map {
            it.copy(
                supersetGroupId = supersetGroupId,
                setGroupType = setGroupType
            )
        } +
        entriesWithoutAdditions.drop(groupEndIndex + 1)
}

internal fun List<StrengthRoutineEntry>.normalizeSupersetGroups(): List<StrengthRoutineEntry> {
    val validGroupIds = mapNotNull { it.supersetGroupId }
        .groupingBy { it }
        .eachCount()
        .filterValues { it >= 2 }
        .keys
    return map { entry ->
        when {
            entry.supersetGroupId == null && entry.setGroupType != null -> {
                entry.copy(setGroupType = null)
            }
            entry.supersetGroupId != null && entry.supersetGroupId !in validGroupIds -> {
                entry.copy(supersetGroupId = null, setGroupType = null)
            }
            else -> entry
        }
    }
}

private fun supersetGroupName(index: Int): String {
    return if (index in 0 until 26) {
        ('A'.code + index).toChar().toString()
    } else {
        (index + 1).toString()
    }
}

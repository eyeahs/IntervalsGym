package com.lighthousepark.intervalsgym.strength

internal fun List<StrengthRoutineEntry>.supersetGroupLabels(): Map<Int, String> {
    return mapNotNull { it.supersetGroupId }
        .distinct()
        .mapIndexed { index, groupId -> groupId to "슈퍼세트 ${supersetGroupName(index)}" }
        .toMap()
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
): List<StrengthRoutineEntry> {
    if (selectedEntryIds.size < 2) return this
    val selectedIdsInOrder = map { it.id }.filter { it in selectedEntryIds }
    if (selectedIdsInOrder.size < 2) return this

    val anchorId = selectedIdsInOrder.first()
    val selectedTailIds = selectedIdsInOrder.drop(1).toSet()
    val groupedEntries = map { entry ->
        if (entry.id in selectedEntryIds) {
            entry.copy(supersetGroupId = supersetGroupId)
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

internal fun List<StrengthRoutineEntry>.normalizeSupersetGroups(): List<StrengthRoutineEntry> {
    val validGroupIds = mapNotNull { it.supersetGroupId }
        .groupingBy { it }
        .eachCount()
        .filterValues { it >= 2 }
        .keys
    return map { entry ->
        if (entry.supersetGroupId != null && entry.supersetGroupId !in validGroupIds) {
            entry.copy(supersetGroupId = null)
        } else {
            entry
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

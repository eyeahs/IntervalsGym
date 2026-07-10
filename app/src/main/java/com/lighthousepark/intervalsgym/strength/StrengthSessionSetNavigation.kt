package com.lighthousepark.intervalsgym.strength

internal fun List<StrengthRoutineEntry>.exerciseChangeFocusIndex(
    currentExerciseIndex: Int,
    pendingAddedEntryId: Int?,
): Int {
    pendingAddedEntryId
        ?.let { pendingId -> indexOfFirst { it.id == pendingId } }
        ?.takeIf { it >= 0 }
        ?.let { return it }
    return currentExerciseIndex.coerceIn(0, (size - 1).coerceAtLeast(0))
}

internal fun nextIncompleteSet(
    entries: List<StrengthRoutineEntry>,
    fromExerciseIndex: Int,
    fromSetIndex: Int,
): Pair<Int, Int>? {
    val hasValidFromExercise = fromExerciseIndex in entries.indices
    val searchStartExerciseIndex = when {
        hasValidFromExercise -> fromExerciseIndex
        fromExerciseIndex >= entries.size -> entries.size
        else -> 0
    }
    if (hasValidFromExercise) {
        nextSupersetIncompleteSet(entries, fromExerciseIndex)?.let { return it }
    }
    for (exerciseIndex in searchStartExerciseIndex until entries.size) {
        val entry = entries[exerciseIndex]
        val setStart = if (hasValidFromExercise && exerciseIndex == fromExerciseIndex) fromSetIndex + 1 else 0
        for (setIndex in setStart until entry.records.size) {
            if (!entry.records[setIndex].completed) return exerciseIndex to setIndex
        }
    }
    for (exerciseIndex in 0 until searchStartExerciseIndex.coerceAtMost(entries.size)) {
        val entry = entries[exerciseIndex]
        for (setIndex in entry.records.indices) {
            if (!entry.records[setIndex].completed) return exerciseIndex to setIndex
        }
    }
    return null
}

internal fun isImmediateSupersetTransition(
    entries: List<StrengthRoutineEntry>,
    fromExerciseIndex: Int,
    toSet: Pair<Int, Int>?,
): Boolean {
    val target = toSet ?: return false
    val fromEntry = entries.getOrNull(fromExerciseIndex) ?: return false
    val toEntry = entries.getOrNull(target.first) ?: return false
    val groupId = fromEntry.supersetGroupId ?: return false
    return toEntry.supersetGroupId == groupId &&
        target.first > fromExerciseIndex
}

internal fun shouldAdvanceCurrentExerciseAfterCompletedExercise(
    entries: List<StrengthRoutineEntry>,
    fromExerciseIndex: Int,
    toSet: Pair<Int, Int>?,
): Boolean {
    val target = toSet ?: return false
    if (target.first == fromExerciseIndex) return false
    val entry = entries.getOrNull(fromExerciseIndex) ?: return false
    val targetEntry = entries.getOrNull(target.first)
    val isSupersetRoundWrap = entry.supersetGroupId != null &&
        targetEntry?.supersetGroupId == entry.supersetGroupId &&
        target.first < fromExerciseIndex
    return isSupersetRoundWrap ||
        (entry.records.isNotEmpty() && entry.records.all { it.completed })
}

private fun nextSupersetIncompleteSet(
    entries: List<StrengthRoutineEntry>,
    fromExerciseIndex: Int,
): Pair<Int, Int>? {
    val groupId = entries.getOrNull(fromExerciseIndex)?.supersetGroupId ?: return null
    val groupIndices = entries.indices.filter { index -> entries[index].supersetGroupId == groupId }
    val groupPosition = groupIndices.indexOf(fromExerciseIndex)
    if (groupPosition < 0) return null

    // Keep moving forward through the group before wrapping, while catching up
    // the least-advanced exercise when set counts or completion progress differ.
    fun firstIncompleteSet(indices: List<Int>): Pair<Int, Int>? {
        return indices.mapNotNull { exerciseIndex ->
            entries[exerciseIndex].records
                .indexOfFirst { !it.completed }
                .takeIf { it >= 0 }
                ?.let { setIndex -> exerciseIndex to setIndex }
        }.minWithOrNull(
            compareBy<Pair<Int, Int>> { it.second }
                .thenBy { groupIndices.indexOf(it.first) }
        )
    }

    return firstIncompleteSet(groupIndices.drop(groupPosition + 1))
        ?: firstIncompleteSet(groupIndices.take(groupPosition + 1))
}

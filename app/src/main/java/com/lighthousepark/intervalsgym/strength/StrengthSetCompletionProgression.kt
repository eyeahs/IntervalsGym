package com.lighthousepark.intervalsgym.strength

internal enum class StrengthSetCompletionFollowUp {
    NONE,
    HIDE_SET_SCREEN,
    START_REST,
    MOVE_TO_PENDING_SET,
    FINISH_ALL_SETS
}

internal data class StrengthSetCompletionResult(
    val entries: List<StrengthRoutineEntry>,
    val setEvent: StrengthSetCompletionEvent?,
    val currentExerciseIndex: Int,
    val currentSetIndex: Int,
    val pendingExerciseIndex: Int?,
    val pendingSetIndex: Int?,
    val restEvent: StrengthRestEvent?,
    val followUp: StrengthSetCompletionFollowUp,
)

internal fun completeStrengthSet(
    entries: List<StrengthRoutineEntry>,
    currentExerciseIndex: Int,
    currentSetIndex: Int,
    nextSetEventSequence: Int,
    nextRestEventId: Int,
    completedAtMillis: Long,
): StrengthSetCompletionResult? {
    val entry = entries.getOrNull(currentExerciseIndex) ?: return null
    val targetSetIndex = entry.records.indexOfFirst { !it.completed }
        .takeIf { it >= 0 }
        ?: currentSetIndex
    val record = entry.records.getOrNull(targetSetIndex) ?: return null

    if (record.completed) {
        return alreadyCompletedStrengthSetResult(
            entries = entries,
            currentExerciseIndex = currentExerciseIndex,
            targetSetIndex = targetSetIndex
        )
    }

    val setEvent = entry.toSetCompletionEvent(
        record = record,
        setIndex = targetSetIndex,
        sequence = nextSetEventSequence,
        completedAtMillis = completedAtMillis
    )
    val updatedEntry = entry.copy(
        records = entry.records.mapIndexed { index, old ->
            if (index == targetSetIndex) old.copy(completed = true) else old
        }
    )
    val updatedEntries = entries.map { if (it.id == entry.id) updatedEntry else it }
    val nextIncomplete = nextIncompleteSet(updatedEntries, currentExerciseIndex, targetSetIndex)
    val shouldAdvanceCurrentExercise = shouldAdvanceCurrentExerciseAfterCompletedExercise(
        entries = updatedEntries,
        fromExerciseIndex = currentExerciseIndex,
        toSet = nextIncomplete
    )
    val restSeconds = record.restSeconds.toIntOrNull() ?: entry.restSeconds
    val skipRestForSupersetTransition = isImmediateSupersetTransition(
        entries = updatedEntries,
        fromExerciseIndex = currentExerciseIndex,
        toSet = nextIncomplete
    )
    val nextCurrentSet = if (shouldAdvanceCurrentExercise && nextIncomplete != null) {
        nextIncomplete
    } else {
        currentExerciseIndex to targetSetIndex
    }
    val restEvent = if (nextIncomplete != null && restSeconds > 0 && !skipRestForSupersetTransition) {
        StrengthRestEvent(
            id = nextRestEventId,
            afterSetSequence = setEvent.sequence,
            exerciseEntryId = entry.id,
            exerciseTitle = entry.title,
            setRecordId = record.id,
            setIndex = targetSetIndex,
            startedAtMillis = completedAtMillis,
            plannedSeconds = restSeconds,
            targetEndAtMillis = completedAtMillis + restSeconds * 1000L,
            endedAtMillis = null,
            endReason = null
        )
    } else {
        null
    }
    val followUp = when {
        restEvent != null -> StrengthSetCompletionFollowUp.START_REST
        nextIncomplete != null -> StrengthSetCompletionFollowUp.MOVE_TO_PENDING_SET
        else -> StrengthSetCompletionFollowUp.FINISH_ALL_SETS
    }

    return StrengthSetCompletionResult(
        entries = updatedEntries,
        setEvent = setEvent,
        currentExerciseIndex = nextCurrentSet.first,
        currentSetIndex = nextCurrentSet.second,
        pendingExerciseIndex = nextIncomplete?.first,
        pendingSetIndex = nextIncomplete?.second,
        restEvent = restEvent,
        followUp = followUp
    )
}

private fun alreadyCompletedStrengthSetResult(
    entries: List<StrengthRoutineEntry>,
    currentExerciseIndex: Int,
    targetSetIndex: Int,
): StrengthSetCompletionResult {
    val entry = entries.getOrNull(currentExerciseIndex)
    val nextSetIndex = entry?.records?.indexOfFirst { !it.completed } ?: -1
    if (nextSetIndex >= 0) {
        return StrengthSetCompletionResult(
            entries = entries,
            setEvent = null,
            currentExerciseIndex = currentExerciseIndex,
            currentSetIndex = nextSetIndex,
            pendingExerciseIndex = null,
            pendingSetIndex = null,
            restEvent = null,
            followUp = StrengthSetCompletionFollowUp.NONE
        )
    }
    val nextIncomplete = nextIncompleteSet(entries, currentExerciseIndex, targetSetIndex)
    return StrengthSetCompletionResult(
        entries = entries,
        setEvent = null,
        currentExerciseIndex = nextIncomplete?.first ?: currentExerciseIndex,
        currentSetIndex = nextIncomplete?.second ?: targetSetIndex,
        pendingExerciseIndex = null,
        pendingSetIndex = null,
        restEvent = null,
        followUp = if (nextIncomplete == null) {
            StrengthSetCompletionFollowUp.HIDE_SET_SCREEN
        } else {
            StrengthSetCompletionFollowUp.NONE
        }
    )
}

private fun StrengthRoutineEntry.toSetCompletionEvent(
    record: StrengthSetRecord,
    setIndex: Int,
    sequence: Int,
    completedAtMillis: Long,
): StrengthSetCompletionEvent {
    return StrengthSetCompletionEvent(
        sequence = sequence,
        exerciseEntryId = id,
        exerciseTitle = title,
        exerciseGroup = exercise.group,
        exerciseId = exercise.id,
        equipment = equipment,
        variation = variation,
        setRecordId = record.id,
        setIndex = setIndex,
        weightKg = record.performedWeightKg,
        reps = if (isUnilateral()) "각 ${record.performedReps}" else record.performedReps,
        targetRestSeconds = record.restSeconds.toIntOrNull() ?: restSeconds,
        completedAtMillis = completedAtMillis
    )
}

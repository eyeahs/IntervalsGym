package com.lighthousepark.intervalsgym.strength

internal fun List<StrengthSetCompletionEvent>.withCurrentStrengthSetDetails(
    entries: List<StrengthRoutineEntry>,
): List<StrengthSetCompletionEvent> {
    val entriesById = entries.associateBy { it.id }
    return mapNotNull { event ->
        val entry = entriesById[event.exerciseEntryId] ?: return@mapNotNull null
        val record = entry.records.getOrNull(event.setIndex)
            ?.takeIf { it.id == event.setRecordId }
            ?: entry.records.firstOrNull { it.id == event.setRecordId }
            ?: return@mapNotNull null
        if (!record.completed) return@mapNotNull null
        val currentSetIndex = entry.records.indexOfFirst { it.id == record.id }
            .takeIf { it >= 0 }
            ?: event.setIndex

        event.copy(
            exerciseTitle = entry.title,
            exerciseGroup = entry.exercise.group,
            exerciseId = entry.exercise.id,
            equipment = entry.equipment,
            variation = entry.variation,
            setIndex = currentSetIndex,
            weightKg = record.performedWeightKg,
            reps = if (entry.isUnilateral()) "각 ${record.performedReps}" else record.performedReps,
            targetRestSeconds = record.restSeconds.toIntOrNull() ?: entry.restSeconds
        )
    }
}

internal fun List<StrengthRestEvent>.withCurrentStrengthRestDetails(
    setEvents: List<StrengthSetCompletionEvent>,
): List<StrengthRestEvent> {
    val setEventBySequence = setEvents.associateBy { it.sequence }
    return mapNotNull { event ->
        val setEvent = setEventBySequence[event.afterSetSequence] ?: return@mapNotNull null
        val oldPlannedTargetEndAtMillis = event.startedAtMillis + event.plannedSeconds * 1000L
        val shouldRetargetPlannedRest = event.targetEndAtMillis == oldPlannedTargetEndAtMillis
        event.copy(
            exerciseEntryId = setEvent.exerciseEntryId,
            exerciseTitle = setEvent.exerciseTitle,
            setRecordId = setEvent.setRecordId,
            setIndex = setEvent.setIndex,
            plannedSeconds = setEvent.targetRestSeconds,
            targetEndAtMillis = if (shouldRetargetPlannedRest) {
                event.startedAtMillis + setEvent.targetRestSeconds * 1000L
            } else {
                event.targetEndAtMillis
            }
        )
    }
}

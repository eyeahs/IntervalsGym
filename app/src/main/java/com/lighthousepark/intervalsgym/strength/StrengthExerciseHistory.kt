package com.lighthousepark.intervalsgym.strength

internal fun List<CompletedStrengthSession>.latestMatchingStrengthEntry(
    exercise: StrengthExercise,
    equipment: String,
    variation: String,
): StrengthRoutineEntry? {
    return sortedByDescending { it.startedAtMillis }
        .asSequence()
        .flatMap { it.entries.asSequence() }
        .firstOrNull { entry -> entry.matchesStrengthExercise(exercise, equipment, variation) }
}

internal fun List<CompletedStrengthSession>.recentMatchingStrengthExerciseHistory(
    exercise: StrengthExercise,
    equipment: String,
    variation: String,
    limit: Int = 3,
): List<CompletedStrengthExerciseHistory> {
    if (limit <= 0) return emptyList()
    return sortedByDescending { session ->
        session.endedAtMillis.takeIf { it > 0L } ?: session.startedAtMillis
    }.mapNotNull { session ->
        val matchingEntries = session.entries.filter { entry ->
            entry.matchesStrengthExercise(exercise, equipment, variation)
        }
        val matchingEvents = session.setEvents.filter { event ->
            event.matchesStrengthExercise(exercise, equipment, variation)
        }
        val entry = matchingEntries.firstOrNull { entry ->
            matchingEvents.any { event -> event.exerciseEntryId == entry.id }
        } ?: matchingEntries.firstOrNull()

        entry?.let {
            val entryEvents = matchingEvents
                .filter { event -> event.exerciseEntryId == it.id }
                .ifEmpty { matchingEvents.takeIf { matchingEntries.size == 1 }.orEmpty() }
                .sortedBy { event -> event.sequence }
            CompletedStrengthExerciseHistory(
                session = session,
                entry = it,
                setEvents = entryEvents
            )
        }
    }.take(limit)
}

private fun StrengthRoutineEntry.matchesStrengthExercise(
    exercise: StrengthExercise,
    equipment: String,
    variation: String,
): Boolean {
    return this.exercise.id == exercise.id &&
        this.equipment == equipment &&
        this.variation == variation
}

private fun StrengthSetCompletionEvent.matchesStrengthExercise(
    exercise: StrengthExercise,
    equipment: String,
    variation: String,
): Boolean {
    return exerciseId == exercise.id &&
        this.equipment == equipment &&
        this.variation == variation
}

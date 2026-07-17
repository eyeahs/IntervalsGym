package com.lighthousepark.intervalsgym.strength

internal fun List<CompletedStrengthSession>.latestMatchingStrengthEntry(
    exercise: StrengthExercise,
    equipment: String,
    variation: String,
    location: String = "",
): StrengthRoutineEntry? {
    return sortedByDescending { it.startedAtMillis }
        .asSequence()
        .filter { session -> session.matchesStrengthHistoryLocation(equipment, location) }
        .flatMap { it.entries.asSequence() }
        .firstOrNull { entry -> entry.matchesStrengthExercise(exercise, equipment, variation) }
}

internal fun List<CompletedStrengthSession>.recentMatchingStrengthExerciseHistory(
    exercise: StrengthExercise,
    equipment: String,
    variation: String,
    limit: Int = 3,
    location: String = "",
): List<CompletedStrengthExerciseHistory> {
    if (limit <= 0) return emptyList()
    return sortedByDescending { session ->
        session.endedAtMillis.takeIf { it > 0L } ?: session.startedAtMillis
    }.asSequence()
        .filter { session -> session.matchesStrengthHistoryLocation(equipment, location) }
        .mapNotNull { session ->
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
        }
        .take(limit)
        .toList()
}

internal fun String.usesLocationSpecificStrengthHistory(): Boolean {
    val normalizedEquipment = trim().lowercase()
    return listOf("머신", "machine", "스미스", "smith", "케이블", "cable")
        .any(normalizedEquipment::contains)
}

private fun CompletedStrengthSession.matchesStrengthHistoryLocation(
    equipment: String,
    location: String,
): Boolean {
    if (!equipment.usesLocationSpecificStrengthHistory()) return true
    return this.location.trim().equals(location.trim(), ignoreCase = true)
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

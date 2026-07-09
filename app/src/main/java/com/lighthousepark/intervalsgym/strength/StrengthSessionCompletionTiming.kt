package com.lighthousepark.intervalsgym.strength

import com.lighthousepark.intervalsgym.core.sessionAutoLocalSaveAtMillis

internal fun List<StrengthRoutineEntry>.allSetsCompleted(): Boolean {
    return isNotEmpty() && all { entry -> entry.records.isNotEmpty() && entry.records.all { it.completed } }
}

internal fun completedStrengthSessionFinishedAtMillis(
    entries: List<StrengthRoutineEntry>,
    setEvents: List<StrengthSetCompletionEvent>,
): Long? {
    if (!entries.allSetsCompleted()) return null
    return setEvents.maxOfOrNull { it.completedAtMillis }?.takeIf { it > 0L }
}

internal fun completedStrengthSessionAutoLocalSaveAtMillis(
    entries: List<StrengthRoutineEntry>,
    setEvents: List<StrengthSetCompletionEvent>,
): Long? {
    return completedStrengthSessionFinishedAtMillis(entries, setEvents)
        ?.let(::sessionAutoLocalSaveAtMillis)
}

internal fun shouldAutoLocalSaveCompletedStrengthSession(
    entries: List<StrengthRoutineEntry>,
    setEvents: List<StrengthSetCompletionEvent>,
    nowMillis: Long,
): Boolean {
    val finishedAtMillis = completedStrengthSessionFinishedAtMillis(entries, setEvents) ?: return false
    return nowMillis >= sessionAutoLocalSaveAtMillis(finishedAtMillis)
}

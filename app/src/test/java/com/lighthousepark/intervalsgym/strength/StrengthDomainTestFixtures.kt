package com.lighthousepark.intervalsgym.strength

internal fun completedStrengthSession(
    id: String,
    routineId: Int = 1,
    startedAtMillis: Long,
    entries: List<StrengthRoutineEntry>,
    setEvents: List<StrengthSetCompletionEvent>,
    location: String = "",
): CompletedStrengthSession {
    return CompletedStrengthSession(
        id = id,
        routineId = routineId,
        routineName = "history",
        startedAtMillis = startedAtMillis,
        endedAtMillis = startedAtMillis + 600_000L,
        durationSeconds = 600,
        intervalsExternalId = id,
        entries = entries,
        setEvents = setEvents,
        restEvents = emptyList(),
        rpe = 7,
        trainingLoad = 1,
        uploadedToIntervals = true,
        location = location
    )
}

internal fun StrengthRoutineEntry.withCompletedRecord(setIndex: Int): StrengthRoutineEntry {
    return copy(
        records = records.mapIndexed { index, record ->
            if (index == setIndex) record.copy(completed = true) else record
        }
    )
}

internal fun StrengthRoutineEntry.withCompletedRecords(vararg setIndices: Int): StrengthRoutineEntry {
    val completedSetIndices = setIndices.toSet()
    return copy(
        records = records.mapIndexed { index, record ->
            if (index in completedSetIndices) record.copy(completed = true) else record
        }
    )
}

internal fun StrengthRoutineEntry.toSetEvent(
    sequence: Int,
    setIndex: Int,
): StrengthSetCompletionEvent {
    val record = records[setIndex]
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
        weightKg = record.weightKg,
        reps = record.reps,
        targetRestSeconds = record.restSeconds.toIntOrNull() ?: restSeconds,
        completedAtMillis = sequence * 1_000L
    )
}

internal fun strengthRestEvent(
    id: Int,
    targetEndAtMillis: Long = 60_000L,
    endedAtMillis: Long? = null,
): StrengthRestEvent {
    return StrengthRestEvent(
        id = id,
        afterSetSequence = id,
        exerciseEntryId = 1,
        exerciseTitle = "스쿼트",
        setRecordId = id,
        setIndex = id - 1,
        startedAtMillis = 1_000L,
        plannedSeconds = 60,
        targetEndAtMillis = targetEndAtMillis,
        endedAtMillis = endedAtMillis,
        endReason = null
    )
}

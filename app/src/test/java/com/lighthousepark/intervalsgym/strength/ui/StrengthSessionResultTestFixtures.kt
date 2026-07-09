package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.data.strengthSetEventForStorage
import com.lighthousepark.intervalsgym.strength.StrengthRestEvent
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines

internal fun strengthResultSnapshotForTest(): StrengthSessionResultSnapshot {
    val routine = defaultStrengthRoutines().first()
    val entry = routine.entries.first().copy(
        records = routine.entries.first().records.mapIndexed { index, record ->
            if (index == 0) {
                record.copy(weightKg = "85", reps = "4", restSeconds = "45", completed = true)
            } else {
                record
            }
        }
    )
    val staleSetEvent = strengthSetEventForStorage(
        routine.entries.first().copy(
            records = routine.entries.first().records.mapIndexed { index, record ->
                if (index == 0) {
                    record.copy(weightKg = "80", reps = "5", restSeconds = "30", completed = true)
                } else {
                    record
                }
            }
        )
    )
    val restEvent = StrengthRestEvent(
        id = 1,
        afterSetSequence = staleSetEvent.sequence,
        exerciseEntryId = entry.id,
        exerciseTitle = entry.title,
        setRecordId = entry.records.first().id,
        setIndex = 0,
        startedAtMillis = staleSetEvent.completedAtMillis,
        plannedSeconds = staleSetEvent.targetRestSeconds,
        targetEndAtMillis = staleSetEvent.completedAtMillis + staleSetEvent.targetRestSeconds * 1000L,
        endedAtMillis = null,
        endReason = null
    )
    return StrengthSessionResultSnapshot(
        routine = routine,
        entries = listOf(entry),
        setEvents = listOf(staleSetEvent),
        restEvents = listOf(restEvent),
        activeRestEventId = restEvent.id,
        sessionStartedAtMillis = 1_000L,
        finishRpe = 7,
        applyWorkoutResultToRoutine = true
    )
}

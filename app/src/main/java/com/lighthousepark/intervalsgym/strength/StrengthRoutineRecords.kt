package com.lighthousepark.intervalsgym.strength

internal fun StrengthRoutineEntry.withRecords(records: List<StrengthSetRecord>): StrengthRoutineEntry {
    val first = records.firstOrNull()
    return copy(
        targetSets = records.size,
        targetReps = first?.reps?.toIntOrNull() ?: targetReps,
        restSeconds = first?.restSeconds?.toIntOrNull() ?: restSeconds,
        targetWeightKg = first?.weightKg ?: targetWeightKg,
        records = records
    )
}

internal fun StrengthRoutineEntry.withPropagatedRecordChange(
    changedIndex: Int,
    changedRecord: StrengthSetRecord,
): StrengthRoutineEntry {
    val nextRecords = records.mapIndexed { index, old ->
        when {
            index < changedIndex -> old
            index == changedIndex -> changedRecord
            else -> old.copy(
                weightKg = changedRecord.weightKg,
                reps = changedRecord.reps,
                restSeconds = changedRecord.restSeconds,
                leftWeightKg = changedRecord.weightKg,
                leftReps = changedRecord.reps,
                rightWeightKg = changedRecord.weightKg,
                rightReps = changedRecord.reps
            )
        }
    }
    return withRecords(nextRecords)
}

internal fun StrengthRoutineEntry.copyForWorkout(): StrengthRoutineEntry {
    return copy(records = records.map { it.copy(completed = false) })
}

internal fun StrengthRoutineEntry.copyAsNewRoutineEntry(
    id: Int,
    exercise: StrengthExercise,
    equipment: String,
    variation: String,
): StrengthRoutineEntry {
    return copy(
        id = id,
        exercise = exercise,
        equipment = equipment,
        variation = variation,
        supersetGroupId = null,
        records = records.mapIndexed { index, record ->
            record.copy(
                id = index + 1,
                durationSeconds = "",
                completed = false
            )
        }
    )
}

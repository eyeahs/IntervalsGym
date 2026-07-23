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

internal fun StrengthRoutineEntry.withRecordReplaced(
    recordIndex: Int,
    record: StrengthSetRecord,
): StrengthRoutineEntry {
    if (recordIndex !in records.indices) return this
    return withRecords(
        records.mapIndexed { index, old -> if (index == recordIndex) record else old }
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
                durationSeconds = changedRecord.durationSeconds,
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

internal fun StrengthRoutineEntry.withPropagatedActualRecordChange(
    changedIndex: Int,
    changedRecord: StrengthSetRecord,
): StrengthRoutineEntry {
    if (changedIndex !in records.indices) return this
    val performedWeightKg = changedRecord.performedWeightKg
    val performedReps = changedRecord.performedReps
    val performedDurationSeconds = changedRecord.performedDurationSeconds
    return withRecords(
        records.mapIndexed { index, old ->
            when {
                index < changedIndex || old.completed -> old
                index == changedIndex -> changedRecord
                else -> old.copy(
                    actualWeightKg = performedWeightKg,
                    actualReps = performedReps,
                    actualDurationSeconds = performedDurationSeconds
                )
            }
        }
    )
}

internal fun StrengthRoutineEntry.copyForWorkout(): StrengthRoutineEntry {
    return copy(
        records = records.map { record ->
            record.copy(
                actualWeightKg = "",
                actualReps = "",
                actualDurationSeconds = "",
                completed = false
            )
        }
    )
}

internal fun StrengthRoutineEntry.copyWorkoutResultToRoutine(): StrengthRoutineEntry {
    val appliedRecords = records.map { record ->
        val appliedWeightKg = if (record.completed) record.performedWeightKg else record.weightKg
        val appliedReps = if (record.completed) record.performedReps else record.reps
        val appliedDurationSeconds = if (record.completed) {
            record.performedDurationSeconds
        } else {
            record.durationSeconds
        }
        record.copy(
            weightKg = appliedWeightKg,
            reps = appliedReps,
            actualWeightKg = "",
            actualReps = "",
            durationSeconds = appliedDurationSeconds,
            actualDurationSeconds = "",
            leftWeightKg = appliedWeightKg,
            leftReps = appliedReps,
            rightWeightKg = appliedWeightKg,
            rightReps = appliedReps,
            completed = false
        )
    }
    return withRecords(appliedRecords)
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
        setGroupType = null,
        records = records.mapIndexed { index, record ->
            record.copy(
                id = index + 1,
                actualWeightKg = "",
                actualReps = "",
                actualDurationSeconds = "",
                completed = false
            )
        }
    )
}

internal fun StrengthWorkoutRoutine.copyForLocalLibrary(
    id: Int,
    name: String = this.name,
): StrengthWorkoutRoutine {
    return copy(
        id = id,
        name = name,
        entries = entries.map { it.copyForWorkout() }
    )
}

internal fun StrengthWorkoutRoutine.clonedForLocalLibrary(
    id: Int,
    existingRoutines: List<StrengthWorkoutRoutine>,
): StrengthWorkoutRoutine {
    val baseName = "$name 복사본"
    val existingNames = existingRoutines.map { it.name }.toSet()
    val cloneName = generateSequence(1) { it + 1 }
        .map { copyIndex -> if (copyIndex == 1) baseName else "$baseName $copyIndex" }
        .first { it !in existingNames }
    return copyForLocalLibrary(id = id, name = cloneName)
}

internal fun List<StrengthWorkoutRoutine>.containsSameStrengthRoutine(
    routine: StrengthWorkoutRoutine,
): Boolean {
    return any { localRoutine ->
        localRoutine.id == routine.id &&
            localRoutine.name == routine.name &&
            localRoutine.location == routine.location &&
            localRoutine.entries.map { it.copyForWorkout() } == routine.entries.map { it.copyForWorkout() }
    }
}

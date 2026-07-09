package com.lighthousepark.intervalsgym.strength

internal fun defaultStrengthRoutines(): List<StrengthWorkoutRoutine> {
    val squat = strengthExerciseCatalog.first { it.id == "squat" }
    val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
    val row = strengthExerciseCatalog.first { it.id == "row" }
    return listOf(
        StrengthWorkoutRoutine(
            id = 1,
            name = "전신 기본",
            entries = listOf(
                defaultStrengthRoutineEntry(id = 1, exercise = squat, weightKg = "", reps = "8", restSeconds = "120"),
                defaultStrengthRoutineEntry(id = 2, exercise = bench, weightKg = "", reps = "8", restSeconds = "120"),
                defaultStrengthRoutineEntry(id = 3, exercise = row, weightKg = "", reps = "10", restSeconds = "90")
            )
        )
    )
}

internal fun nextStrengthWorkoutRoutineId(
    routines: List<StrengthWorkoutRoutine>,
    history: List<CompletedStrengthSession> = emptyList(),
    scheduledRoutines: List<ScheduledStrengthRoutine> = emptyList(),
    activeSession: ActiveStrengthSession? = null,
    reservedIds: List<Int> = emptyList(),
): Int {
    val usedIds = routines.map { it.id } +
        history.map { it.routineId } +
        scheduledRoutines.map { it.routine.id } +
        listOfNotNull(activeSession?.routineId) +
        reservedIds
    return (usedIds.filter { it > 0 }.maxOrNull() ?: 0) + 1
}

internal fun defaultStrengthRoutineEntry(
    id: Int,
    exercise: StrengthExercise,
    weightKg: String = defaultStrengthWeightForEquipment(exercise.equipmentOptions.first()),
    reps: String = "8",
    restSeconds: String = "120",
): StrengthRoutineEntry {
    val records = List(3) { index ->
        StrengthSetRecord(
            id = index + 1,
            weightKg = weightKg,
            reps = reps,
            durationSeconds = "",
            restSeconds = restSeconds,
            completed = false
        )
    }
    return StrengthRoutineEntry(
        id = id,
        exercise = exercise,
        equipment = exercise.equipmentOptions.first(),
        variation = exercise.variationOptions.first(),
        supersetGroupId = null,
        targetSets = records.size,
        targetReps = reps.toIntOrNull() ?: 0,
        restSeconds = restSeconds.toIntOrNull() ?: 0,
        targetWeightKg = weightKg,
        records = records
    )
}

internal fun defaultStrengthWeightForEquipment(equipment: String): String {
    return if (equipment.trim() == "맨몸") "" else "10"
}

internal fun defaultStrengthSetRecord(entry: StrengthRoutineEntry): StrengthSetRecord {
    val last = entry.records.lastOrNull()
    val weightKg = last?.weightKg ?: entry.targetWeightKg
    val reps = last?.reps ?: entry.targetReps.takeIf { it > 0 }?.toString().orEmpty()
    return StrengthSetRecord(
        id = (entry.records.maxOfOrNull { it.id } ?: 0) + 1,
        weightKg = weightKg,
        reps = reps,
        leftWeightKg = last?.leftWeightKg ?: weightKg,
        leftReps = last?.leftReps ?: reps,
        rightWeightKg = last?.rightWeightKg ?: weightKg,
        rightReps = last?.rightReps ?: reps,
        durationSeconds = last?.durationSeconds.orEmpty(),
        restSeconds = last?.restSeconds ?: entry.restSeconds.takeIf { it > 0 }?.toString().orEmpty(),
        completed = false
    )
}

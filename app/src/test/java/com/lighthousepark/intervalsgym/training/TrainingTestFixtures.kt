package com.lighthousepark.intervalsgym.training

import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import java.time.LocalDate
import java.time.LocalDateTime

internal fun trainingItem(
    id: String = "item",
    remoteId: String = id,
    externalId: String? = null,
    type: String = "Workout",
    name: String = type,
    isRoutine: Boolean = false,
    timeLabel: String = "08:00",
    date: LocalDate = LocalDate.of(2026, 6, 23),
    startedAt: LocalDateTime? = null,
    durationSeconds: Int? = null,
    distanceMeters: Double? = null,
    fitness: Double? = null,
    description: String? = null,
    blocks: List<RoutineBlock> = emptyList(),
    matchedStrengthRoutine: StrengthWorkoutRoutine? = null,
): TrainingItem {
    return TrainingItem(
        id = id,
        remoteId = remoteId,
        externalId = externalId,
        name = name,
        type = type,
        date = date,
        startedAt = startedAt,
        timeLabel = timeLabel,
        durationSeconds = durationSeconds,
        distanceMeters = distanceMeters,
        weightLiftedKg = null,
        load = null,
        fitness = fitness,
        fatigue = null,
        form = null,
        description = description,
        blocks = blocks,
        isRoutine = isRoutine,
        matchedStrengthRoutine = matchedStrengthRoutine
    )
}

internal fun routineBlock(
    index: Int,
    targetText: String,
    durationSeconds: Int,
    startSecond: Int,
): RoutineBlock {
    return RoutineBlock(
        index = index,
        title = "Workout",
        kind = "work",
        targetText = targetText,
        durationSeconds = durationSeconds,
        startSecond = startSecond,
        endSecond = startSecond + durationSeconds,
        isRecovery = false
    )
}

internal fun sprintRunDescription(): String {
    return """
        # Warmup
        - 1m 10:00 pace [6km/h 1%]
        - 1m 7:30 pace [8km/h 1%]
        - 3m 6:40 pace [9km/h 1%]
        - 2m 6:00 pace [10km/h 1%]
        - 1m 5:00 pace [12km/h 1%]
        - 1m 10:00 pace [6km/h 1%]

        # Sprint
        6x
        - 5s 5:00 pace [12km/h 1%] Ramp time
        - 15s 3:45 pace [16km/h 1%] All Out
        - 5s Rest
        - 40s 10:00 pace [6km/h 1%]
    """.trimIndent()
}

package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.core.formatClockTime
import com.lighthousepark.intervalsgym.running.CompletedRunningSession
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.totalVolumeKg
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.TrainingSportType
import com.lighthousepark.intervalsgym.training.sportType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.abs

private fun List<TrainingItem>.withMatchedStrengthSessions(
    history: List<CompletedStrengthSession>,
): List<TrainingItem> {
    if (history.isEmpty()) return this
    return map { item ->
        if (item.isRoutine) {
            item
        } else {
            item.copy(matchedStrengthSession = item.matchStrengthSession(history))
        }
    }
}

internal fun List<TrainingItem>.withLocalStrengthResults(
    history: List<CompletedStrengthSession>,
    weekStart: LocalDate,
    weekEnd: LocalDate,
): List<TrainingItem> {
    val matched = withMatchedStrengthSessions(history)
    val matchedWorkoutIds = matched.mapNotNull { it.matchedStrengthSession?.id }.toSet()
    val localOnlyItems = history
        .filter { workout ->
            val date = workout.startedLocalDate()
            date in weekStart..weekEnd && workout.id !in matchedWorkoutIds
        }
        .map { workout -> workout.toLocalTrainingItem() }
    return matched + localOnlyItems
}

private fun CompletedStrengthSession.startedLocalDate(): LocalDate {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault()).toLocalDate()
}

private fun CompletedStrengthSession.toLocalTrainingItem(): TrainingItem {
    val startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault())
    return TrainingItem(
        id = "local-strength-$id",
        remoteId = id,
        externalId = intervalsExternalId,
        name = routineName,
        type = "Weight Training",
        date = startedAt.toLocalDate(),
        startedAt = startedAt,
        timeLabel = startedAt.toLocalTime().formatClockTime(),
        durationSeconds = durationSeconds,
        distanceMeters = null,
        weightLiftedKg = entries.totalVolumeKg(),
        load = trainingLoad,
        fitness = null,
        fatigue = null,
        form = null,
        description = if (uploadedToIntervals) {
            "로컬 웨이트 기록 · Intervals.icu에서 삭제되었을 수 있습니다."
        } else {
            "로컬 웨이트 기록 · Intervals.icu 미동기화"
        },
        blocks = emptyList(),
        isRoutine = false,
        matchedStrengthSession = this,
        isLocalOnlyStrengthResult = true
    )
}

internal fun List<TrainingItem>.withLocalRunningResults(
    history: List<CompletedRunningSession>,
    weekStart: LocalDate,
    weekEnd: LocalDate,
): List<TrainingItem> {
    if (history.isEmpty()) return this
    val localOnlyItems = history
        .filter { workout ->
            val date = workout.startedLocalDate()
            date in weekStart..weekEnd && none { item -> item.matchesRunningSession(workout) }
        }
        .map { workout -> workout.toLocalTrainingItem() }
    return this + localOnlyItems
}

private fun CompletedRunningSession.startedLocalDate(): LocalDate {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault()).toLocalDate()
}

private fun CompletedRunningSession.toLocalTrainingItem(): TrainingItem {
    val startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault())
    return TrainingItem(
        id = "local-running-$id",
        remoteId = id,
        externalId = id,
        name = name,
        type = "Run",
        date = startedAt.toLocalDate(),
        startedAt = startedAt,
        timeLabel = startedAt.toLocalTime().formatClockTime(),
        durationSeconds = durationSeconds,
        distanceMeters = estimatedDistanceMeters,
        weightLiftedKg = null,
        load = null,
        fitness = null,
        fatigue = null,
        form = null,
        description = if (uploadedToIntervals) {
            "로컬 러닝 기록 · Intervals.icu 업로드됨"
        } else {
            "로컬 러닝 기록"
        },
        blocks = blocks,
        isRoutine = false,
        isLocalOnlyRunningResult = true,
        actualRunningBlocks = actualBlocks,
        actualRunningRoutePoints = routePoints
    )
}

private fun TrainingItem.matchesRunningSession(workout: CompletedRunningSession): Boolean {
    if (isLocalOnlyRunningResult) {
        return remoteId == workout.id || id == "local-running-${workout.id}"
    }
    if (isRoutine || sportType() != TrainingSportType.RUNNING) return false
    val startedMillis = startedAt
        ?.atZone(ZoneId.systemDefault())
        ?.toInstant()
        ?.toEpochMilli()
        ?: return false
    val timeDiff = abs(startedMillis - workout.startedAtMillis)
    val durationDiff = durationSeconds?.let { abs(it - workout.durationSeconds) } ?: Int.MAX_VALUE
    return timeDiff <= 10 * 60 * 1000L || durationDiff <= 5 * 60
}

private fun TrainingItem.matchStrengthSession(
    history: List<CompletedStrengthSession>,
): CompletedStrengthSession? {
    externalId?.let { id ->
        history.firstOrNull { it.intervalsExternalId == id }?.let { return it }
    }
    val startedMillis = startedAt
        ?.atZone(ZoneId.systemDefault())
        ?.toInstant()
        ?.toEpochMilli()
        ?: return null
    val looksLikeStrength = name.contains("웨이트", ignoreCase = true) ||
        name.contains("strength", ignoreCase = true) ||
        description.orEmpty().contains("IntervalsGym 웨이트", ignoreCase = true)
    return history
        .filter { workout ->
            abs(workout.startedAtMillis - startedMillis) <= 2 * 60 * 1000L
        }
        .filter { workout ->
            looksLikeStrength ||
                workout.routineName.equals(name, ignoreCase = true) ||
                name.contains(workout.routineName, ignoreCase = true)
        }
        .minByOrNull { abs(it.startedAtMillis - startedMillis) }
}

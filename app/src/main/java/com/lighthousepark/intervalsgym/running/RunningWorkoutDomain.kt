package com.lighthousepark.intervalsgym.running

import com.lighthousepark.intervalsgym.MainActivity
import com.lighthousepark.intervalsgym.R
import com.lighthousepark.intervalsgym.app.*
import com.lighthousepark.intervalsgym.core.*
import com.lighthousepark.intervalsgym.data.*
import com.lighthousepark.intervalsgym.login.*
import com.lighthousepark.intervalsgym.overlay.*
import com.lighthousepark.intervalsgym.running.*
import com.lighthousepark.intervalsgym.running.ui.*
import com.lighthousepark.intervalsgym.strength.*
import com.lighthousepark.intervalsgym.strength.ui.*
import com.lighthousepark.intervalsgym.training.*
import com.lighthousepark.intervalsgym.training.ui.*
import com.lighthousepark.intervalsgym.workout.ui.*

import java.time.LocalDateTime
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import org.json.JSONObject

internal data class RunningWorkoutSession(
    val name: String,
    val startedAt: LocalDateTime,
    val endedAt: LocalDateTime,
    val warmupSeconds: Int,
    val blocks: List<PlanBlock>,
    val actualBlocks: List<PlanBlock>,
)

internal data class CompletedRunningWorkout(
    val id: String,
    val name: String,
    val startedAtMillis: Long,
    val endedAtMillis: Long,
    val durationSeconds: Int,
    val warmupSeconds: Int,
    val estimatedDistanceMeters: Double,
    val blocks: List<PlanBlock>,
    val actualBlocks: List<PlanBlock>,
    val uploadedToIntervals: Boolean,
)

internal data class SavedRunningWorkoutPlan(
    val id: String,
    val name: String,
    val description: String?,
    val durationSeconds: Int,
    val blocks: List<PlanBlock>,
    val workoutDocJson: String?,
    val savedAtMillis: Long,
)

internal fun TrainingItem.toSavedRunningWorkoutPlan(
    graphBlocks: List<PlanBlock>,
): SavedRunningWorkoutPlan? {
    if (sportType() != TrainingSportType.RUNNING || graphBlocks.isEmpty()) return null
    val sourceId = listOfNotNull(externalId, remoteId.takeIf { it.isNotBlank() }, id)
        .firstOrNull()
        .orEmpty()
        .ifBlank { "${name}-${System.currentTimeMillis()}" }
        .replace(Regex("""[^A-Za-z0-9_.-]"""), "-")
    return SavedRunningWorkoutPlan(
        id = "saved-running-$sourceId",
        name = name.ifBlank { "러닝 Plan" },
        description = description,
        durationSeconds = durationSeconds ?: graphBlocks.sumOf { it.durationSeconds },
        blocks = graphBlocks,
        workoutDocJson = workoutDocJson,
        savedAtMillis = System.currentTimeMillis()
    )
}

internal fun SavedRunningWorkoutPlan.toTrainingItem(): TrainingItem {
    val date = LocalDate.now()
    val startedAt = date.atStartOfDay()
    return TrainingItem(
        id = "local-$id",
        remoteId = id,
        externalId = id,
        name = name,
        type = "Run",
        date = date,
        startedAt = startedAt,
        timeLabel = startedAt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
        durationSeconds = durationSeconds.takeIf { it > 0 } ?: blocks.sumOf { it.durationSeconds },
        distanceMeters = null,
        weightLiftedKg = null,
        load = null,
        fitness = null,
        fatigue = null,
        form = null,
        description = description,
        blocks = blocks,
        isPlan = false,
        workoutDocJson = workoutDocJson
    )
}

internal fun RunningWorkoutSession.durationSeconds(): Int {
    return ChronoUnit.SECONDS.between(startedAt, endedAt).toInt().coerceAtLeast(0)
}

internal fun RunningWorkoutSession.toCompletedRunningWorkout(uploadedToIntervals: Boolean): CompletedRunningWorkout {
    val startedAtMillis = startedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val endedAtMillis = endedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    return CompletedRunningWorkout(
        id = "running-$startedAtMillis",
        name = name,
        startedAtMillis = startedAtMillis,
        endedAtMillis = endedAtMillis,
        durationSeconds = durationSeconds(),
        warmupSeconds = warmupSeconds,
        estimatedDistanceMeters = estimatedDistanceMeters(),
        blocks = blocks,
        actualBlocks = actualBlocks,
        uploadedToIntervals = uploadedToIntervals
    )
}

internal fun CompletedRunningWorkout.toJsonObject(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("name", name)
        .put("startedAtMillis", startedAtMillis)
        .put("endedAtMillis", endedAtMillis)
        .put("durationSeconds", durationSeconds)
        .put("warmupSeconds", warmupSeconds)
        .put("estimatedDistanceMeters", estimatedDistanceMeters)
        .put("blocks", blocks.toPlanBlocksJsonArray())
        .put("actualBlocks", actualBlocks.toPlanBlocksJsonArray())
        .put("uploadedToIntervals", uploadedToIntervals)
}

internal fun RunningWorkoutSession.estimatedDistanceMeters(): Double {
    return actualBlocks.estimatedRunningDistanceMeters()
}

internal fun RunningWorkoutSession.toIntervalsDescription(): String {
    val estimatedDistance = estimatedDistanceMeters()
    return buildString {
        appendLine("IntervalsGym 러닝 수행 기록")
        appendLine("Garmin 원본 기록이 있으면 Garmin 기록을 우선 사용하세요.")
        appendLine("총 수행 시간: ${formatDuration(durationSeconds())}")
        appendLine("Warmup: ${formatClock(warmupSeconds)}")
        if (estimatedDistance > 0.0) {
            appendLine("예상 거리: ${formatDistance(estimatedDistance)}")
        }
        appendLine()
        actualBlocks.forEachIndexed { index, block ->
            val speed = block.runningTargetSpeedText().ifBlank { "-" }
            val incline = block.runningInclineText().ifBlank { "-" }
            appendLine("- Block ${index + 1}: ${block.title}")
            appendLine("  실제 시간: ${formatClock(block.durationSeconds)}, 속도: $speed, 경사도: $incline")
        }
    }
}

internal fun List<PlanBlock>.toActualTimeline(): List<PlanBlock> {
    var cursor = 0
    return mapIndexedNotNull { index, block ->
        val duration = block.durationSeconds.coerceAtLeast(0)
        if (duration <= 0) return@mapIndexedNotNull null
        val start = cursor
        cursor += duration
        block.copy(
            index = index,
            durationSeconds = duration,
            startSecond = start,
            endSecond = cursor
        )
    }
}

internal fun List<PlanBlock>.normalizedRunningActualBlocks(
    planBlocks: List<PlanBlock>,
    activeDurationSeconds: Int,
): List<PlanBlock> {
    if (isEmpty()) {
        return if (activeDurationSeconds > 0 && planBlocks.isNotEmpty()) {
            planBlocks.scaledToTotalDuration(activeDurationSeconds)
        } else {
            emptyList()
        }
    }
    val planDurationSeconds = planBlocks.sumOf { it.durationSeconds.coerceAtLeast(0) }
    val actualDurationSeconds = sumOf { it.durationSeconds.coerceAtLeast(0) }
    val looksLikePlanFallback = planBlocks.isNotEmpty() &&
        actualDurationSeconds == planDurationSeconds &&
        activeDurationSeconds in 1 until planDurationSeconds &&
        sameRunningTimelineAs(planBlocks)
    return if (looksLikePlanFallback) {
        scaledToTotalDuration(activeDurationSeconds)
    } else {
        toActualTimeline()
    }
}

private fun List<PlanBlock>.sameRunningTimelineAs(other: List<PlanBlock>): Boolean {
    if (size != other.size) return false
    return zip(other).all { (left, right) ->
        left.title == right.title &&
            left.kind == right.kind &&
            left.targetText == right.targetText &&
            left.durationSeconds == right.durationSeconds
    }
}

internal fun List<PlanBlock>.scaledToTotalDuration(totalDurationSeconds: Int): List<PlanBlock> {
    val safeTotalDuration = totalDurationSeconds.coerceAtLeast(0)
    val originalTotalDuration = sumOf { it.durationSeconds.coerceAtLeast(0) }
    if (safeTotalDuration <= 0 || originalTotalDuration <= 0) return emptyList()
    var remainingDuration = safeTotalDuration
    return mapIndexedNotNull { index, block ->
        if (remainingDuration <= 0) return@mapIndexedNotNull null
        val originalDuration = block.durationSeconds.coerceAtLeast(0)
        if (originalDuration <= 0) return@mapIndexedNotNull null
        val scaledDuration = if (index == lastIndex) {
            remainingDuration
        } else {
            ((originalDuration.toDouble() / originalTotalDuration.toDouble()) * safeTotalDuration)
                .roundToInt()
                .coerceAtLeast(1)
                .coerceAtMost(remainingDuration)
        }
        remainingDuration -= scaledDuration
        block.copy(durationSeconds = scaledDuration)
    }.toActualTimeline()
}

internal fun List<PlanBlock>.estimatedRunningDistanceMeters(): Double {
    return sumOf { block ->
        val speedKmh = block.graphTargetSpeedKmh()?.toDouble() ?: return@sumOf 0.0
        speedKmh * 1000.0 * block.durationSeconds.coerceAtLeast(0).toDouble() / 3600.0
    }
}

internal fun currentBlockIndex(blocks: List<PlanBlock>, elapsedSeconds: Int): Int {
    if (blocks.isEmpty()) return -1
    if (elapsedSeconds >= blocks.last().endSecond) return -1
    return blocks.indexOfFirst { elapsedSeconds in it.startSecond until it.endSecond }
}

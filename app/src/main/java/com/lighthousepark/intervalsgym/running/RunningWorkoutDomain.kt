package com.lighthousepark.intervalsgym.running

import com.lighthousepark.intervalsgym.core.formatClock
import com.lighthousepark.intervalsgym.core.formatClockTime
import com.lighthousepark.intervalsgym.core.formatDistance
import com.lighthousepark.intervalsgym.core.formatDuration
import com.lighthousepark.intervalsgym.data.toCachedRoutineBlocks
import com.lighthousepark.intervalsgym.data.toRoutineBlocksJsonArray
import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.TrainingSportType
import com.lighthousepark.intervalsgym.training.runningInclineText
import com.lighthousepark.intervalsgym.training.runningTargetSpeedText
import com.lighthousepark.intervalsgym.training.sportType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import org.json.JSONArray
import org.json.JSONObject

internal enum class RunningSessionPhase {
    WARMUP,
    BLOCK,
    FINISHED
}

internal data class RunningSession(
    val name: String,
    val startedAt: LocalDateTime,
    val endedAt: LocalDateTime,
    val warmupSeconds: Int,
    val blocks: List<RoutineBlock>,
    val actualBlocks: List<RoutineBlock>,
    val heartRateSamples: List<HeartRateSample> = emptyList(),
)

internal data class CompletedRunningSession(
    val id: String,
    val name: String,
    val startedAtMillis: Long,
    val endedAtMillis: Long,
    val durationSeconds: Int,
    val warmupSeconds: Int,
    val estimatedDistanceMeters: Double,
    val blocks: List<RoutineBlock>,
    val actualBlocks: List<RoutineBlock>,
    val uploadedToIntervals: Boolean,
    val routePoints: List<RunningRoutePoint> = emptyList(),
)

internal data class SavedRunningWorkoutRoutine(
    val id: String,
    val name: String,
    val description: String?,
    val durationSeconds: Int,
    val blocks: List<RoutineBlock>,
    val workoutDocJson: String?,
    val savedAtMillis: Long,
)

internal fun TrainingItem.toSavedRunningWorkoutRoutine(
    graphBlocks: List<RoutineBlock>,
): SavedRunningWorkoutRoutine? {
    if (sportType() != TrainingSportType.RUNNING || graphBlocks.isEmpty()) return null
    val sourceId = listOfNotNull(externalId, remoteId.takeIf { it.isNotBlank() }, id)
        .firstOrNull()
        .orEmpty()
        .ifBlank { "${name}-${System.currentTimeMillis()}" }
        .replace(Regex("""[^A-Za-z0-9_.-]"""), "-")
    return SavedRunningWorkoutRoutine(
        id = "saved-running-$sourceId",
        name = name.ifBlank { "러닝 Routine" },
        description = description,
        durationSeconds = durationSeconds ?: graphBlocks.sumOf { it.durationSeconds },
        blocks = graphBlocks,
        workoutDocJson = workoutDocJson,
        savedAtMillis = System.currentTimeMillis()
    )
}

internal fun SavedRunningWorkoutRoutine.toTrainingItem(): TrainingItem {
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
        timeLabel = startedAt.toLocalTime().formatClockTime(),
        durationSeconds = durationSeconds.takeIf { it > 0 } ?: blocks.sumOf { it.durationSeconds },
        distanceMeters = null,
        weightLiftedKg = null,
        load = null,
        fitness = null,
        fatigue = null,
        form = null,
        description = description,
        blocks = blocks,
        isRoutine = false,
        workoutDocJson = workoutDocJson
    )
}

internal fun RunningSession.durationSeconds(): Int {
    return ChronoUnit.SECONDS.between(startedAt, endedAt).toInt().coerceAtLeast(0)
}

internal fun buildRunningSessionForFinish(
    routineName: String,
    startedAtMillis: Long,
    endedAtMillis: Long,
    blocks: List<RoutineBlock>,
    actualBlocks: List<RoutineBlock>,
    heartRateSamples: List<HeartRateSample>,
): RunningSession {
    val blockSeconds = blocks.sumOf { it.durationSeconds }
    return RunningSession(
        name = routineName,
        startedAt = startedAtMillis.toRunningLocalDateTime(),
        endedAt = endedAtMillis.toRunningLocalDateTime(),
        warmupSeconds = ((endedAtMillis - startedAtMillis) / 1000L).toInt()
            .coerceAtLeast(0)
            .let { elapsed -> (elapsed - blockSeconds).coerceAtLeast(0) },
        blocks = blocks,
        actualBlocks = actualBlocks.toActualTimeline(),
        heartRateSamples = heartRateSamples
    )
}

private fun Long.toRunningLocalDateTime(): LocalDateTime {
    return java.time.Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
}

internal fun RunningSession.toCompletedRunningSession(uploadedToIntervals: Boolean): CompletedRunningSession {
    val startedAtMillis = startedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val endedAtMillis = endedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    return CompletedRunningSession(
        id = "running-$startedAtMillis",
        name = name,
        startedAtMillis = startedAtMillis,
        endedAtMillis = endedAtMillis,
        durationSeconds = durationSeconds(),
        warmupSeconds = warmupSeconds,
        estimatedDistanceMeters = estimatedDistanceMeters(),
        blocks = blocks,
        actualBlocks = actualBlocks,
        uploadedToIntervals = uploadedToIntervals,
        routePoints = buildDokdoTrackRoutePoints()
    )
}

internal fun CompletedRunningSession.toJsonObject(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("name", name)
        .put("startedAtMillis", startedAtMillis)
        .put("endedAtMillis", endedAtMillis)
        .put("durationSeconds", durationSeconds)
        .put("warmupSeconds", warmupSeconds)
        .put("estimatedDistanceMeters", estimatedDistanceMeters)
        .put("blocks", blocks.toRoutineBlocksJsonArray())
        .put("actualBlocks", actualBlocks.toRoutineBlocksJsonArray())
        .put("uploadedToIntervals", uploadedToIntervals)
        .put("routePoints", routePoints.toRunningRoutePointsJsonArray())
}

internal fun RunningSession.estimatedDistanceMeters(): Double {
    return actualBlocks.estimatedRunningDistanceMeters()
}

internal fun runningBlocksFromJson(jsonText: String): List<RoutineBlock> {
    return runCatching { JSONArray(jsonText).toCachedRoutineBlocks() }.getOrElse { emptyList() }
}

internal fun RunningSession.toIntervalsDescription(): String {
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

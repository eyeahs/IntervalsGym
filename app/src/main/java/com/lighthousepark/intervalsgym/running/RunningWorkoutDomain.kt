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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

internal data class RunningWorkoutSession(
    val name: String,
    val startedAt: LocalDateTime,
    val endedAt: LocalDateTime,
    val warmupSeconds: Int,
    val blocks: List<PlanBlock>,
    val actualBlocks: List<PlanBlock>,
    val heartRateSamples: List<HeartRateSample> = emptyList(),
)

internal data class RunningRoutePoint(
    val elapsedSeconds: Int,
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Double = 0.0,
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
    val routePoints: List<RunningRoutePoint> = emptyList(),
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

internal data class RunningWorkoutCatchUpResult(
    val currentBlockIndex: Int,
    val blockStartedAtMillis: Long,
    val blockEndAtMillis: Long,
    val actualBlocks: List<PlanBlock>,
    val finishedAtMillis: Long? = null,
)

internal const val RUNNING_SPEED_STEP_KMH = 0.5f
internal const val RUNNING_INCLINE_STEP_PERCENT = 0.5f
internal const val MAX_RUNNING_SPEED_KMH = 30f
internal const val MAX_RUNNING_INCLINE_PERCENT = 30f
internal const val DOKDO_ROUTE_CENTER_LATITUDE = 37.241306
internal const val DOKDO_ROUTE_CENTER_LONGITUDE = 131.867361
internal const val DOKDO_TRACK_LAP_METERS = 400.0
internal const val DOKDO_TRACK_CURVE_RADIUS_METERS = 36.5
private const val DOKDO_ROUTE_SAMPLE_INTERVAL_SECONDS = 5
private const val DOKDO_ROUTE_PACE_SAWTOOTH_PERIOD_SECONDS = 20
private const val DOKDO_ROUTE_PACE_VARIATION_SECONDS = 1.0
private const val METERS_PER_LATITUDE_DEGREE = 111_320.0
private val DOKDO_TRACK_STRAIGHT_METERS =
    (DOKDO_TRACK_LAP_METERS - 2.0 * PI * DOKDO_TRACK_CURVE_RADIUS_METERS) / 2.0

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
        uploadedToIntervals = uploadedToIntervals,
        routePoints = buildDokdoTrackRoutePoints()
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
        .put("routePoints", routePoints.toRunningRoutePointsJsonArray())
}

internal fun RunningWorkoutSession.estimatedDistanceMeters(): Double {
    return actualBlocks.estimatedRunningDistanceMeters()
}

internal fun RunningWorkoutSession.buildDokdoTrackRoutePoints(): List<RunningRoutePoint> {
    return buildDokdoTrackRoutePoints(
        actualBlocks = actualBlocks,
        warmupSeconds = warmupSeconds,
        sampleIntervalSeconds = DOKDO_ROUTE_SAMPLE_INTERVAL_SECONDS
    )
}

internal fun buildDokdoTrackRoutePoints(
    actualBlocks: List<PlanBlock>,
    warmupSeconds: Int = 0,
    sampleIntervalSeconds: Int = DOKDO_ROUTE_SAMPLE_INTERVAL_SECONDS,
): List<RunningRoutePoint> {
    val safeSampleInterval = sampleIntervalSeconds.coerceAtLeast(1)
    val movingBlocks = actualBlocks.toActualTimeline()
    if (movingBlocks.none { it.durationSeconds > 0 && (it.graphTargetSpeedKmh() ?: 0f) > 0f }) return emptyList()

    val points = mutableListOf<RunningRoutePoint>()
    var cumulativeMeters = 0.0
    var elapsedSeconds = warmupSeconds.coerceAtLeast(0)
    points += dokdoTrackRoutePoint(elapsedSeconds = 0, distanceMeters = 0.0)
    if (elapsedSeconds > 0) {
        points += dokdoTrackRoutePoint(elapsedSeconds = elapsedSeconds, distanceMeters = 0.0)
    }

    movingBlocks.forEach { block ->
        val duration = block.durationSeconds.coerceAtLeast(0)
        if (duration <= 0) return@forEach
        var blockCursor = 0
        while (blockCursor < duration) {
            val nextCursor = (blockCursor + safeSampleInterval).coerceAtMost(duration)
            val midpointElapsedSeconds = elapsedSeconds + (nextCursor - blockCursor) / 2
            val metersPerSecond = block.virtualRouteMetersPerSecond(midpointElapsedSeconds)
            cumulativeMeters += metersPerSecond * (nextCursor - blockCursor).toDouble()
            elapsedSeconds += nextCursor - blockCursor
            points += dokdoTrackRoutePoint(elapsedSeconds = elapsedSeconds, distanceMeters = cumulativeMeters)
            blockCursor = nextCursor
        }
    }

    return points.distinctBy { it.elapsedSeconds }
}

private fun dokdoTrackRoutePoint(
    elapsedSeconds: Int,
    distanceMeters: Double,
): RunningRoutePoint {
    val offset = dokdoTrackOffsetMeters(distanceMeters)
    val metersPerLongitudeDegree = METERS_PER_LATITUDE_DEGREE * cos(DOKDO_ROUTE_CENTER_LATITUDE * PI / 180.0)
    return RunningRoutePoint(
        elapsedSeconds = elapsedSeconds.coerceAtLeast(0),
        latitude = DOKDO_ROUTE_CENTER_LATITUDE + offset.northMeters / METERS_PER_LATITUDE_DEGREE,
        longitude = DOKDO_ROUTE_CENTER_LONGITUDE + offset.eastMeters / metersPerLongitudeDegree,
        elevationMeters = 0.0
    )
}

internal fun dokdoTrackOffsetMeters(distanceMeters: Double): RunningTrackOffset {
    val straight = DOKDO_TRACK_STRAIGHT_METERS
    val radius = DOKDO_TRACK_CURVE_RADIUS_METERS
    val curve = PI * radius
    val lapDistance = distanceMeters.mod(DOKDO_TRACK_LAP_METERS)
    return when {
        lapDistance < straight -> {
            RunningTrackOffset(
                eastMeters = -straight / 2.0 + lapDistance,
                northMeters = -radius
            )
        }
        lapDistance < straight + curve -> {
            val angle = -PI / 2.0 + (lapDistance - straight) / radius
            RunningTrackOffset(
                eastMeters = straight / 2.0 + cos(angle) * radius,
                northMeters = sin(angle) * radius
            )
        }
        lapDistance < straight * 2.0 + curve -> {
            val straightDistance = lapDistance - straight - curve
            RunningTrackOffset(
                eastMeters = straight / 2.0 - straightDistance,
                northMeters = radius
            )
        }
        else -> {
            val angle = PI / 2.0 + (lapDistance - straight * 2.0 - curve) / radius
            RunningTrackOffset(
                eastMeters = -straight / 2.0 + cos(angle) * radius,
                northMeters = sin(angle) * radius
            )
        }
    }
}

internal data class RunningTrackOffset(
    val eastMeters: Double,
    val northMeters: Double,
)

private fun PlanBlock.virtualRouteMetersPerSecond(elapsedSeconds: Int): Double {
    val targetSpeedKmh = graphTargetSpeedKmh()?.takeIf { it > 0f } ?: return 0.0
    val paceSecondsPerKm = 3600.0 / targetSpeedKmh.toDouble()
    val variedPaceSecondsPerKm = (paceSecondsPerKm + virtualRoutePaceOffsetSeconds(elapsedSeconds))
        .coerceAtLeast(1.0)
    return 1000.0 / variedPaceSecondsPerKm
}

internal fun virtualRoutePaceOffsetSeconds(elapsedSeconds: Int): Double {
    val phaseSecond = elapsedSeconds.floorMod(DOKDO_ROUTE_PACE_SAWTOOTH_PERIOD_SECONDS)
    val phase = phaseSecond.toDouble() / (DOKDO_ROUTE_PACE_SAWTOOTH_PERIOD_SECONDS - 1).toDouble()
    return -DOKDO_ROUTE_PACE_VARIATION_SECONDS + phase * DOKDO_ROUTE_PACE_VARIATION_SECONDS * 2.0
}

private fun Int.floorMod(other: Int): Int {
    return ((this % other) + other) % other
}

internal fun List<RunningRoutePoint>.toRunningRoutePointsJsonArray(): JSONArray {
    return JSONArray().also { array ->
        forEach { point ->
            array.put(
                JSONObject()
                    .put("elapsedSeconds", point.elapsedSeconds)
                    .put("latitude", point.latitude)
                    .put("longitude", point.longitude)
                    .put("elevationMeters", point.elevationMeters)
            )
        }
    }
}

internal fun JSONArray?.toRunningRoutePoints(): List<RunningRoutePoint> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { index ->
        val json = optJSONObject(index) ?: return@mapNotNull null
        val latitude = json.optDouble("latitude", Double.NaN).takeUnless { it.isNaN() }
            ?: return@mapNotNull null
        val longitude = json.optDouble("longitude", Double.NaN).takeUnless { it.isNaN() }
            ?: return@mapNotNull null
        RunningRoutePoint(
            elapsedSeconds = json.optInt("elapsedSeconds", index * DOKDO_ROUTE_SAMPLE_INTERVAL_SECONDS),
            latitude = latitude,
            longitude = longitude,
            elevationMeters = json.optDouble("elevationMeters", 0.0)
        )
    }
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

internal fun RunningWorkoutSession.buildRunningTcx(): String {
    val routePoints = buildDokdoTrackRoutePoints()
    val durationSeconds = maxOf(
        durationSeconds().coerceAtLeast(1),
        routePoints.maxOfOrNull { it.elapsedSeconds } ?: 0
    )
    val heartRatesByElapsedSecond = heartRateSamplesByElapsedSecond(durationSeconds)
    val startInstant = startedAt.atZone(ZoneId.systemDefault()).toInstant()
    val startText = DateTimeFormatter.ISO_INSTANT.format(startInstant)
    val totalDistanceMeters = estimatedDistanceMeters().coerceAtLeast(0.0)
    val normalizedRoutePoints = routePoints.toMutableList().apply {
        if (isEmpty()) {
            add(
                RunningRoutePoint(
                    elapsedSeconds = 0,
                    latitude = DOKDO_ROUTE_CENTER_LATITUDE,
                    longitude = DOKDO_ROUTE_CENTER_LONGITUDE
                )
            )
        }
        if (first().elapsedSeconds != 0) {
            add(0, first().copy(elapsedSeconds = 0))
        }
        if (last().elapsedSeconds < durationSeconds) {
            add(last().copy(elapsedSeconds = durationSeconds))
        }
    }.sortedBy { it.elapsedSeconds }.distinctBy { it.elapsedSeconds }
    val routePointsByElapsedSecond = normalizedRoutePoints.associateBy { it.elapsedSeconds }
    val trackPoints = (normalizedRoutePoints.map { it.elapsedSeconds } + heartRatesByElapsedSecond.keys)
        .distinct()
        .sorted()
        .map { elapsedSeconds ->
            val point = routePointsByElapsedSecond[elapsedSeconds]
                ?: runningRoutePointAtElapsed(elapsedSeconds)
            RunningTcxTrackPoint(
                routePoint = point,
                heartRateBpm = heartRatesByElapsedSecond[elapsedSeconds]
            )
        }
    val heartRateValues = heartRatesByElapsedSecond.values.toList()
    val lapHeartRateXml = if (heartRateValues.isNotEmpty()) {
        """
                <AverageHeartRateBpm>
                  <Value>${heartRateValues.average().roundToInt()}</Value>
                </AverageHeartRateBpm>
                <MaximumHeartRateBpm>
                  <Value>${heartRateValues.max()}</Value>
                </MaximumHeartRateBpm>
        """.trimIndent()
    } else {
        ""
    }
    val trackXml = trackPoints.joinToString(separator = "\n") { trackPoint ->
        val point = trackPoint.routePoint
        val timeText = DateTimeFormatter.ISO_INSTANT.format(startInstant.plusSeconds(point.elapsedSeconds.toLong()))
        val distanceText = runningDistanceMetersAtElapsed(point.elapsedSeconds).formatTcxDecimal()
        buildString {
            appendLine("              <Trackpoint>")
            appendLine("                <Time>$timeText</Time>")
            appendLine("                <Position>")
            appendLine("                  <LatitudeDegrees>${point.latitude.formatTcxCoordinate()}</LatitudeDegrees>")
            appendLine("                  <LongitudeDegrees>${point.longitude.formatTcxCoordinate()}</LongitudeDegrees>")
            appendLine("                </Position>")
            appendLine("                <AltitudeMeters>${point.elevationMeters.formatTcxDecimal()}</AltitudeMeters>")
            appendLine("                <DistanceMeters>$distanceText</DistanceMeters>")
            trackPoint.heartRateBpm?.let { bpm ->
                appendLine("                <HeartRateBpm>")
                appendLine("                  <Value>$bpm</Value>")
                appendLine("                </HeartRateBpm>")
            }
            append("              </Trackpoint>")
        }
    }

    return """
        <?xml version="1.0" encoding="UTF-8"?>
        <TrainingCenterDatabase xmlns="http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2 http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd">
          <Activities>
            <Activity Sport="Running">
              <Id>$startText</Id>
              <Lap StartTime="$startText">
                <TotalTimeSeconds>$durationSeconds</TotalTimeSeconds>
                <DistanceMeters>${totalDistanceMeters.formatTcxDecimal()}</DistanceMeters>
                <Calories>0</Calories>
$lapHeartRateXml
                <Intensity>Active</Intensity>
                <TriggerMethod>Manual</TriggerMethod>
                <Track>
        $trackXml
                </Track>
              </Lap>
              <Notes>${name.xmlEscape()}</Notes>
            </Activity>
          </Activities>
        </TrainingCenterDatabase>
    """.trimIndent()
}

private data class RunningTcxTrackPoint(
    val routePoint: RunningRoutePoint,
    val heartRateBpm: Int?,
)

private fun RunningWorkoutSession.heartRateSamplesByElapsedSecond(durationSeconds: Int): Map<Int, Int> {
    if (heartRateSamples.isEmpty()) return emptyMap()
    val startMillis = startedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val endMillis = endedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    return heartRateSamples
        .asSequence()
        .filter { it.bpm > 0 }
        .filter { it.timestampMillis in startMillis..endMillis }
        .groupBy { sample ->
            ((sample.timestampMillis - startMillis) / 1000L)
                .toInt()
                .coerceIn(0, durationSeconds)
        }
        .mapValues { (_, samples) -> samples.map { it.bpm }.average().roundToInt() }
        .toSortedMap()
}

private fun RunningWorkoutSession.runningRoutePointAtElapsed(elapsedSeconds: Int): RunningRoutePoint {
    return dokdoTrackRoutePoint(
        elapsedSeconds = elapsedSeconds,
        distanceMeters = runningDistanceMetersAtElapsed(elapsedSeconds)
    )
}

private fun RunningWorkoutSession.runningDistanceMetersAtElapsed(elapsedSeconds: Int): Double {
    val activeElapsedSeconds = (elapsedSeconds - warmupSeconds).coerceAtLeast(0)
    if (activeElapsedSeconds <= 0) return 0.0
    var distanceMeters = 0.0
    actualBlocks.toActualTimeline().forEach { block ->
        val speedKmh = block.graphTargetSpeedKmh()?.takeIf { it > 0f }?.toDouble() ?: 0.0
        val metersPerSecond = speedKmh * 1000.0 / 3600.0
        val segmentSeconds = when {
            activeElapsedSeconds >= block.endSecond -> block.durationSeconds
            activeElapsedSeconds > block.startSecond -> activeElapsedSeconds - block.startSecond
            else -> 0
        }.coerceAtLeast(0)
        distanceMeters += metersPerSecond * segmentSeconds
    }
    return distanceMeters
}

private fun Double.formatTcxCoordinate(): String = String.format(Locale.US, "%.8f", this)

private fun Double.formatTcxDecimal(): String = String.format(Locale.US, "%.2f", this)

private fun String.xmlEscape(): String {
    return replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
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

internal fun PlanBlock.withRunningTargetOverride(
    speedKmh: Float,
    inclinePercent: Float,
): PlanBlock {
    return copy(targetText = runningTargetOverrideText(speedKmh, inclinePercent))
}

internal fun runningTargetOverrideText(
    speedKmh: Float,
    inclinePercent: Float,
): String {
    return listOf(
        formatKmh(speedKmh.coerceIn(0f, MAX_RUNNING_SPEED_KMH)),
        formatRunningInclinePercent(inclinePercent.coerceIn(0f, MAX_RUNNING_INCLINE_PERCENT))
    ).joinToString(" · ")
}

internal fun formatRunningInclinePercent(inclinePercent: Float): String {
    val safeIncline = inclinePercent.coerceIn(0f, MAX_RUNNING_INCLINE_PERCENT)
    return if (safeIncline % 1f == 0f) {
        "${safeIncline.roundToInt()}%"
    } else {
        String.format(java.util.Locale.US, "%.1f%%", safeIncline)
    }
}

internal fun shouldAutoLocalSaveLastRunningBlock(
    currentBlockIndex: Int,
    blockCount: Int,
    blockEndAtMillis: Long,
    nowMillis: Long,
): Boolean {
    return blockCount > 0 &&
        currentBlockIndex == blockCount - 1 &&
        blockEndAtMillis > 0L &&
        nowMillis >= workoutAutoLocalSaveAtMillis(blockEndAtMillis)
}

internal fun currentBlockIndex(blocks: List<PlanBlock>, elapsedSeconds: Int): Int {
    if (blocks.isEmpty()) return -1
    if (elapsedSeconds >= blocks.last().endSecond) return -1
    return blocks.indexOfFirst { elapsedSeconds in it.startSecond until it.endSecond }
}

internal fun catchUpRunningWorkoutBlocks(
    blocks: List<PlanBlock>,
    currentBlockIndex: Int,
    blockStartedAtMillis: Long,
    blockEndAtMillis: Long,
    actualBlocks: List<PlanBlock>,
    nowMillis: Long,
): RunningWorkoutCatchUpResult? {
    if (blocks.isEmpty() || blockStartedAtMillis <= 0L) return null
    val safeCurrentIndex = currentBlockIndex.coerceIn(0, blocks.lastIndex)
    val currentBlock = blocks[safeCurrentIndex]
    val activeBlockEndAtMillis = blockEndAtMillis.takeIf { it > 0L }
        ?: (blockStartedAtMillis + currentBlock.durationSeconds.toDurationMillis())
    val nextActualBlocks = actualBlocks.take(safeCurrentIndex).toMutableList()
    if (nextActualBlocks.size < safeCurrentIndex) {
        for (index in nextActualBlocks.size until safeCurrentIndex) {
            nextActualBlocks += blocks[index].asFullActualBlock()
        }
    }

    if (nowMillis < activeBlockEndAtMillis) {
        return if (
            safeCurrentIndex != currentBlockIndex ||
            activeBlockEndAtMillis != blockEndAtMillis ||
            nextActualBlocks != actualBlocks
        ) {
            RunningWorkoutCatchUpResult(
                currentBlockIndex = safeCurrentIndex,
                blockStartedAtMillis = blockStartedAtMillis,
                blockEndAtMillis = activeBlockEndAtMillis,
                actualBlocks = nextActualBlocks
            )
        } else {
            null
        }
    }

    nextActualBlocks += currentBlock.asFullActualBlock()
    var nextBlockStartAtMillis = activeBlockEndAtMillis
    for (index in (safeCurrentIndex + 1) until blocks.size) {
        val block = blocks[index]
        val nextBlockEndAtMillis = nextBlockStartAtMillis + block.durationSeconds.toDurationMillis()
        if (nowMillis < nextBlockEndAtMillis) {
            return RunningWorkoutCatchUpResult(
                currentBlockIndex = index,
                blockStartedAtMillis = nextBlockStartAtMillis,
                blockEndAtMillis = nextBlockEndAtMillis,
                actualBlocks = nextActualBlocks
            )
        }
        nextActualBlocks += block.asFullActualBlock()
        nextBlockStartAtMillis = nextBlockEndAtMillis
    }

    return RunningWorkoutCatchUpResult(
        currentBlockIndex = blocks.lastIndex,
        blockStartedAtMillis = nextBlockStartAtMillis,
        blockEndAtMillis = 0L,
        actualBlocks = nextActualBlocks,
        finishedAtMillis = nextBlockStartAtMillis
    )
}

private fun PlanBlock.asFullActualBlock(): PlanBlock {
    return copy(durationSeconds = durationSeconds.coerceAtLeast(0))
}

private fun Int.toDurationMillis(): Long {
    return coerceAtLeast(0).toLong() * 1000L
}

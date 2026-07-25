package com.lighthousepark.intervalsgym.running

import com.lighthousepark.intervalsgym.training.graphTargetSpeedKmh
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

internal fun RunningSession.buildRunningTcx(): String {
    val routePoints = buildDokdoTrackRoutePoints()
    val durationSeconds = maxOf(
        durationSeconds().coerceAtLeast(1),
        routePoints.maxOfOrNull { it.elapsedSeconds } ?: 0
    )
    val heartRatesByElapsedSecond = heartRateSamplesByElapsedSecond(durationSeconds)
    val startInstant = runningRecordStartedAt().atZone(ZoneId.systemDefault()).toInstant()
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

private fun RunningSession.heartRateSamplesByElapsedSecond(durationSeconds: Int): Map<Int, Int> {
    if (heartRateSamples.isEmpty()) return emptyMap()
    val startMillis = runningRecordStartedAt()
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
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

private fun RunningSession.runningRoutePointAtElapsed(elapsedSeconds: Int): RunningRoutePoint {
    return dokdoTrackRoutePoint(
        elapsedSeconds = elapsedSeconds,
        distanceMeters = runningDistanceMetersAtElapsed(elapsedSeconds),
        elevationMeters = actualBlocks.estimatedRunningClimbMetersAtElapsed(elapsedSeconds)
    )
}

private fun RunningSession.runningDistanceMetersAtElapsed(elapsedSeconds: Int): Double {
    val activeElapsedSeconds = elapsedSeconds.coerceAtLeast(0)
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

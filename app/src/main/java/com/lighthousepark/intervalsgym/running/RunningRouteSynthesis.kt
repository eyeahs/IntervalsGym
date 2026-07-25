package com.lighthousepark.intervalsgym.running

import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.training.graphTargetSpeedKmh
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import org.json.JSONArray
import org.json.JSONObject

internal data class RunningRoutePoint(
    val elapsedSeconds: Int,
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Double = 0.0,
)

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

internal fun RunningSession.buildDokdoTrackRoutePoints(): List<RunningRoutePoint> {
    return buildDokdoTrackRoutePoints(
        actualBlocks = actualBlocks,
        warmupSeconds = 0,
        sampleIntervalSeconds = DOKDO_ROUTE_SAMPLE_INTERVAL_SECONDS
    )
}

internal fun buildDokdoTrackRoutePoints(
    actualBlocks: List<RoutineBlock>,
    warmupSeconds: Int = 0,
    sampleIntervalSeconds: Int = DOKDO_ROUTE_SAMPLE_INTERVAL_SECONDS,
): List<RunningRoutePoint> {
    val safeSampleInterval = sampleIntervalSeconds.coerceAtLeast(1)
    val movingBlocks = actualBlocks.toActualTimeline()
    if (movingBlocks.none { it.durationSeconds > 0 && (it.graphTargetSpeedKmh() ?: 0f) > 0f }) return emptyList()

    val points = mutableListOf<RunningRoutePoint>()
    var cumulativeMeters = 0.0
    var cumulativeElevationMeters = 0.0
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
            val segmentSeconds = nextCursor - blockCursor
            cumulativeMeters += metersPerSecond * segmentSeconds.toDouble()
            cumulativeElevationMeters += block.estimatedRunningClimbMeters(segmentSeconds)
            elapsedSeconds += nextCursor - blockCursor
            points += dokdoTrackRoutePoint(
                elapsedSeconds = elapsedSeconds,
                distanceMeters = cumulativeMeters,
                elevationMeters = cumulativeElevationMeters
            )
            blockCursor = nextCursor
        }
    }

    return points.distinctBy { it.elapsedSeconds }
}

internal fun dokdoTrackRoutePoint(
    elapsedSeconds: Int,
    distanceMeters: Double,
    elevationMeters: Double = 0.0,
): RunningRoutePoint {
    val offset = dokdoTrackOffsetMeters(distanceMeters)
    val metersPerLongitudeDegree = METERS_PER_LATITUDE_DEGREE * cos(DOKDO_ROUTE_CENTER_LATITUDE * PI / 180.0)
    return RunningRoutePoint(
        elapsedSeconds = elapsedSeconds.coerceAtLeast(0),
        latitude = DOKDO_ROUTE_CENTER_LATITUDE + offset.northMeters / METERS_PER_LATITUDE_DEGREE,
        longitude = DOKDO_ROUTE_CENTER_LONGITUDE + offset.eastMeters / metersPerLongitudeDegree,
        elevationMeters = elevationMeters.coerceAtLeast(0.0)
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

private fun RoutineBlock.virtualRouteMetersPerSecond(elapsedSeconds: Int): Double {
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

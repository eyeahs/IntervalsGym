package com.lighthousepark.intervalsgym.running

import kotlin.math.PI
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningRouteSynthesisTest {
    @Test
    fun dokdoTrackOffsetMeters_usesStandardTrackShape() {
        val straight = (DOKDO_TRACK_LAP_METERS - 2.0 * PI * DOKDO_TRACK_CURVE_RADIUS_METERS) / 2.0

        val start = dokdoTrackOffsetMeters(0.0)
        val firstStraightEnd = dokdoTrackOffsetMeters(straight)
        val halfLap = dokdoTrackOffsetMeters(DOKDO_TRACK_LAP_METERS / 2.0)
        val lapEnd = dokdoTrackOffsetMeters(DOKDO_TRACK_LAP_METERS)

        assertEquals(-straight / 2.0, start.eastMeters, 0.01)
        assertEquals(-DOKDO_TRACK_CURVE_RADIUS_METERS, start.northMeters, 0.01)
        assertEquals(straight / 2.0, firstStraightEnd.eastMeters, 0.01)
        assertEquals(-DOKDO_TRACK_CURVE_RADIUS_METERS, firstStraightEnd.northMeters, 0.01)
        assertEquals(straight / 2.0, halfLap.eastMeters, 0.01)
        assertEquals(DOKDO_TRACK_CURVE_RADIUS_METERS, halfLap.northMeters, 0.01)
        assertEquals(start.eastMeters, lapEnd.eastMeters, 0.01)
        assertEquals(start.northMeters, lapEnd.northMeters, 0.01)
    }

    @Test
    fun virtualRoutePaceOffsetSeconds_isSmallSawtooth() {
        assertEquals(-1.0, virtualRoutePaceOffsetSeconds(0), 0.01)
        assertTrue(virtualRoutePaceOffsetSeconds(10) > 0.0)
        assertTrue(virtualRoutePaceOffsetSeconds(19) > 0.9)
        assertEquals(-1.0, virtualRoutePaceOffsetSeconds(20), 0.01)
    }

    @Test
    fun buildDokdoTrackRoutePoints_generatesVirtualTrackAroundDokdo() {
        val points = buildDokdoTrackRoutePoints(
            actualBlocks = listOf(routineBlock(index = 0, durationSeconds = 600, targetText = "10km/h")),
            warmupSeconds = 60
        )
        val latRange = points.maxOf { it.latitude } - points.minOf { it.latitude }
        val lonRange = points.maxOf { it.longitude } - points.minOf { it.longitude }

        assertTrue(points.size > 10)
        assertEquals(0, points.first().elapsedSeconds)
        assertEquals(660, points.last().elapsedSeconds)
        assertTrue(points.all { it.latitude in 37.23..37.25 })
        assertTrue(points.all { it.longitude in 131.85..131.89 })
        assertTrue(lonRange > latRange * 2.0)
        assertTrue(points.first().longitude != points.last().longitude)
    }

    @Test
    fun buildDokdoTrackRoutePoints_accumulatesElevationFromSpeedAndIncline() {
        val points = buildDokdoTrackRoutePoints(
            actualBlocks = listOf(
                routineBlock(index = 0, durationSeconds = 360, targetText = "10km/h · 5%")
            )
        )

        assertEquals(0.0, points.first().elevationMeters, 0.01)
        assertEquals(50.0, points.last().elevationMeters, 0.01)
        assertTrue(points.zipWithNext().all { (first, second) ->
            second.elevationMeters >= first.elevationMeters
        })
    }
}

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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningWorkoutDomainTest {
    @Test
    fun toActualTimeline_rebuildsStartAndEndSeconds() {
        val timeline = listOf(
            planBlock(index = 7, durationSeconds = 60),
            planBlock(index = 8, durationSeconds = 30)
        ).toActualTimeline()

        assertEquals(2, timeline.size)
        assertEquals(0, timeline[0].index)
        assertEquals(0, timeline[0].startSecond)
        assertEquals(60, timeline[0].endSecond)
        assertEquals(1, timeline[1].index)
        assertEquals(60, timeline[1].startSecond)
        assertEquals(90, timeline[1].endSecond)
    }

    @Test
    fun scaledToTotalDuration_preservesRequestedTotalAndTimeline() {
        val scaled = listOf(
            planBlock(index = 0, durationSeconds = 60),
            planBlock(index = 1, durationSeconds = 60)
        ).scaledToTotalDuration(totalDurationSeconds = 30)

        assertEquals(30, scaled.sumOf { it.durationSeconds })
        assertEquals(0, scaled.first().startSecond)
        assertEquals(30, scaled.last().endSecond)
        assertTrue(scaled.all { it.durationSeconds > 0 })
    }

    @Test
    fun estimatedRunningDistanceMeters_usesRunningSpeedTargets() {
        val distanceMeters = listOf(
            planBlock(index = 0, durationSeconds = 3600, targetText = "5km/h")
        ).estimatedRunningDistanceMeters()

        assertEquals(5000.0, distanceMeters, 0.01)
    }

    @Test
    fun withRunningTargetOverride_roundTripsSpeedAndIncline() {
        val block = planBlock(index = 0, durationSeconds = 60, targetText = "6km/h · 4%")
            .withRunningTargetOverride(speedKmh = 7.2f, inclinePercent = 5f)

        assertEquals(7.2f, block.graphTargetSpeedKmh() ?: 0f, 0.01f)
        assertEquals("8:20 (7.2km/h)", block.runningTargetSpeedText())
        assertEquals("5%", block.runningInclineText())
    }

    @Test
    fun dokdoTrackOffsetMeters_usesStandardTrackShape() {
        val straight = (DOKDO_TRACK_LAP_METERS - 2.0 * kotlin.math.PI * DOKDO_TRACK_CURVE_RADIUS_METERS) / 2.0

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
            actualBlocks = listOf(planBlock(index = 0, durationSeconds = 600, targetText = "10km/h")),
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
    fun toCompletedRunningWorkout_storesDokdoRoutePoints() {
        val startedAt = java.time.LocalDateTime.of(2026, 6, 25, 7, 0)
        val session = RunningWorkoutSession(
            name = "독도 러닝",
            startedAt = startedAt,
            endedAt = startedAt.plusMinutes(11),
            warmupSeconds = 60,
            blocks = listOf(planBlock(index = 0, durationSeconds = 600, targetText = "10km/h")),
            actualBlocks = listOf(planBlock(index = 0, durationSeconds = 600, targetText = "10km/h"))
        )

        val completed = session.toCompletedRunningWorkout(uploadedToIntervals = false)

        assertTrue(completed.routePoints.isNotEmpty())
        assertEquals(DOKDO_ROUTE_CENTER_LATITUDE, completed.routePoints.map { it.latitude }.average(), 0.01)
        assertEquals(DOKDO_ROUTE_CENTER_LONGITUDE, completed.routePoints.map { it.longitude }.average(), 0.01)
    }

    @Test
    fun currentBlockIndex_returnsActiveBlockOnly() {
        val blocks = listOf(
            planBlock(index = 0, durationSeconds = 60).copy(startSecond = 0, endSecond = 60),
            planBlock(index = 1, durationSeconds = 30).copy(startSecond = 60, endSecond = 90)
        )

        assertEquals(0, currentBlockIndex(blocks, elapsedSeconds = 30))
        assertEquals(1, currentBlockIndex(blocks, elapsedSeconds = 60))
        assertEquals(-1, currentBlockIndex(blocks, elapsedSeconds = 90))
    }

    private fun planBlock(
        index: Int,
        durationSeconds: Int,
        targetText: String = "",
    ): PlanBlock {
        return PlanBlock(
            index = index,
            title = "Block ${index + 1}",
            kind = "work",
            targetText = targetText,
            durationSeconds = durationSeconds,
            startSecond = 0,
            endSecond = 0,
            isRecovery = false
        )
    }
}

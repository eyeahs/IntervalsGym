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
    fun scaledToTotalDuration_returnsEmptyForNonPositiveInput() {
        assertTrue(emptyList<PlanBlock>().scaledToTotalDuration(totalDurationSeconds = 30).isEmpty())
        assertTrue(listOf(planBlock(index = 0, durationSeconds = 60)).scaledToTotalDuration(totalDurationSeconds = 0).isEmpty())
        assertTrue(
            listOf(
                planBlock(index = 0, durationSeconds = 0),
                planBlock(index = 1, durationSeconds = -30)
            ).scaledToTotalDuration(totalDurationSeconds = 30).isEmpty()
        )
    }

    @Test
    fun normalizedRunningActualBlocks_scalesPlanWhenActualBlocksAreMissing() {
        val planBlocks = listOf(
            planBlock(index = 0, durationSeconds = 60, targetText = "6km/h · 1%"),
            planBlock(index = 1, durationSeconds = 120, targetText = "12km/h · 3%")
        )

        val normalized = emptyList<PlanBlock>().normalizedRunningActualBlocks(
            planBlocks = planBlocks,
            activeDurationSeconds = 45
        )

        assertEquals(listOf(15, 30), normalized.map { it.durationSeconds })
        assertEquals(listOf(0, 15), normalized.map { it.startSecond })
        assertEquals(listOf(15, 45), normalized.map { it.endSecond })
        assertEquals(listOf("6km/h · 1%", "12km/h · 3%"), normalized.map { it.targetText })
    }

    @Test
    fun normalizedRunningActualBlocks_shortensFullPlanFallbackToActiveDuration() {
        val planBlocks = listOf(
            planBlock(index = 0, durationSeconds = 60, targetText = "6km/h · 1%"),
            planBlock(index = 1, durationSeconds = 60, targetText = "16km/h · 1%")
        )

        val normalized = planBlocks.normalizedRunningActualBlocks(
            planBlocks = planBlocks,
            activeDurationSeconds = 90
        )

        assertEquals(listOf(45, 45), normalized.map { it.durationSeconds })
        assertEquals(90, normalized.last().endSecond)
        assertEquals("16km/h · 1%", normalized.last().targetText)
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
    fun shouldAutoLocalSaveLastRunningBlock_requiresLastBlockAndThirtyMinuteDelay() {
        val lastBlockEndAtMillis = 1_000L
        val autoSaveAtMillis = lastBlockEndAtMillis + WORKOUT_AUTO_LOCAL_SAVE_DELAY_MILLIS

        assertEquals(
            false,
            shouldAutoLocalSaveLastRunningBlock(
                currentBlockIndex = 2,
                blockCount = 3,
                blockEndAtMillis = lastBlockEndAtMillis,
                nowMillis = autoSaveAtMillis - 1L
            )
        )
        assertEquals(
            true,
            shouldAutoLocalSaveLastRunningBlock(
                currentBlockIndex = 2,
                blockCount = 3,
                blockEndAtMillis = lastBlockEndAtMillis,
                nowMillis = autoSaveAtMillis
            )
        )
        assertEquals(
            false,
            shouldAutoLocalSaveLastRunningBlock(
                currentBlockIndex = 1,
                blockCount = 3,
                blockEndAtMillis = lastBlockEndAtMillis,
                nowMillis = autoSaveAtMillis
            )
        )
        assertEquals(
            false,
            shouldAutoLocalSaveLastRunningBlock(
                currentBlockIndex = 0,
                blockCount = 0,
                blockEndAtMillis = lastBlockEndAtMillis,
                nowMillis = autoSaveAtMillis
            )
        )
        assertEquals(
            false,
            shouldAutoLocalSaveLastRunningBlock(
                currentBlockIndex = 2,
                blockCount = 3,
                blockEndAtMillis = 0L,
                nowMillis = autoSaveAtMillis
            )
        )
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
    fun buildRunningTcx_containsTrackPositionAndDistanceData() {
        val startedAt = java.time.LocalDateTime.of(2026, 6, 25, 7, 0)
        val session = RunningWorkoutSession(
            name = "Morning & Run",
            startedAt = startedAt,
            endedAt = startedAt.plusMinutes(2),
            warmupSeconds = 60,
            blocks = listOf(planBlock(index = 0, durationSeconds = 60, targetText = "10km/h")),
            actualBlocks = listOf(planBlock(index = 0, durationSeconds = 60, targetText = "10km/h"))
        )

        val tcx = session.buildRunningTcx()
        val distanceValues = Regex("""<DistanceMeters>([0-9.]+)</DistanceMeters>""")
            .findAll(tcx)
            .mapNotNull { it.groupValues[1].toDoubleOrNull() }
            .toList()

        assertTrue(tcx.contains("""<Activity Sport="Running">"""))
        assertTrue(tcx.contains("<Trackpoint>"))
        assertTrue(tcx.contains("<Position>"))
        assertTrue(tcx.contains("<LatitudeDegrees>"))
        assertTrue(tcx.contains("<LongitudeDegrees>"))
        assertTrue(tcx.contains("<Notes>Morning &amp; Run</Notes>"))
        assertTrue(distanceValues.last() > 160.0)
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

    @Test
    fun catchUpRunningWorkoutBlocks_finishesAtScheduledEndAfterLongPause() {
        val blocks = listOf(
            planBlock(index = 0, durationSeconds = 60),
            planBlock(index = 1, durationSeconds = 30)
        )

        val result = catchUpRunningWorkoutBlocks(
            blocks = blocks,
            currentBlockIndex = 0,
            blockStartedAtMillis = 1_000L,
            blockEndAtMillis = 61_000L,
            actualBlocks = emptyList(),
            nowMillis = 600_000L
        )

        requireNotNull(result)
        assertEquals(91_000L, result.finishedAtMillis)
        assertEquals(listOf(60, 30), result.actualBlocks.map { it.durationSeconds })
    }

    @Test
    fun catchUpRunningWorkoutBlocks_advancesIntoElapsedNextBlock() {
        val blocks = listOf(
            planBlock(index = 0, durationSeconds = 60),
            planBlock(index = 1, durationSeconds = 60),
            planBlock(index = 2, durationSeconds = 60)
        )

        val result = catchUpRunningWorkoutBlocks(
            blocks = blocks,
            currentBlockIndex = 0,
            blockStartedAtMillis = 1_000L,
            blockEndAtMillis = 61_000L,
            actualBlocks = emptyList(),
            nowMillis = 90_000L
        )

        requireNotNull(result)
        assertEquals(null, result.finishedAtMillis)
        assertEquals(1, result.currentBlockIndex)
        assertEquals(61_000L, result.blockStartedAtMillis)
        assertEquals(121_000L, result.blockEndAtMillis)
        assertEquals(listOf(60), result.actualBlocks.map { it.durationSeconds })
    }

    @Test
    fun catchUpRunningWorkoutBlocks_restoresMissingPreviousBlocks() {
        val blocks = listOf(
            planBlock(index = 0, durationSeconds = 60),
            planBlock(index = 1, durationSeconds = 30)
        )

        val result = catchUpRunningWorkoutBlocks(
            blocks = blocks,
            currentBlockIndex = 1,
            blockStartedAtMillis = 61_000L,
            blockEndAtMillis = 91_000L,
            actualBlocks = emptyList(),
            nowMillis = 100_000L
        )

        requireNotNull(result)
        assertEquals(91_000L, result.finishedAtMillis)
        assertEquals(listOf(60, 30), result.actualBlocks.map { it.durationSeconds })
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

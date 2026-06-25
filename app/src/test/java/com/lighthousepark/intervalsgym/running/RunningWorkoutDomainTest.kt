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

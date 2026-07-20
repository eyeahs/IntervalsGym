package com.lighthousepark.intervalsgym.training

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutGraphRunningTargetsTest {
    @Test
    fun runningGraph_usesPaceAsSpeedAndTreatsPercentAsIncline() {
        val block = graphTestBlock(targetText = "12:00 pace 20%")

        val graphBlock = listOf(block).toWorkoutGraphBlocks(TrainingSportType.RUNNING).single()

        assertEquals(WorkoutGraphUnit.SpeedKmh, graphBlock.unit)
        assertEquals(5f, graphBlock.value, 0.01f)
        assertEquals("20%", block.runningInclineText())
        assertEquals("12:00 (5km/h)", block.runningTargetSpeedText())
    }

    @Test
    fun runningTargetDisplay_preservesEveryAuthoredPaceSecond() {
        (2 * 60..30 * 60).forEach { secondsPerKm ->
            val pace = "${secondsPerKm / 60}:${(secondsPerKm % 60).toString().padStart(2, '0')}"
            val display = graphTestBlock(targetText = "$pace pace").runningTargetDisplay()

            assertEquals(pace, requireNotNull(display).paceText)
        }
    }

    @Test
    fun runningTargetDisplay_preservesAuthoredKmhPrecisionWithoutAveragingTargets() {
        val block = graphTestBlock(targetText = "9.8km/h · 5:37 pace · 10.25 Km/h")

        val display = requireNotNull(block.runningTargetDisplay())

        assertEquals(10.25f, display.speedKmh, 0.001f)
        assertEquals("10.25", display.speedText)
        assertEquals("5:51", display.paceText)
        assertEquals("5:51 (10.25km/h)", block.runningTargetSpeedText())
    }

    @Test
    fun runningGraph_prefersExplicitDescriptionKmhOverLeadingMetersPerSecondRange() {
        val block = graphTestBlock(targetText = "1.6-1.7 · 10km/h")

        val graphBlock = listOf(block).toWorkoutGraphBlocks(TrainingSportType.RUNNING).single()

        assertEquals(WorkoutGraphUnit.SpeedKmh, graphBlock.unit)
        assertEquals(10f, graphBlock.value, 0.01f)
        assertEquals("10", requireNotNull(block.runningTargetDisplay()).speedText)
    }

    @Test
    fun runningGraph_prefersBracketedKmhOverWrittenPaceApproximation() {
        val block = graphTestBlock(
            title = "All Out",
            kind = "work",
            targetText = "3:44 pace [16km/h 1%] All Out",
            durationSeconds = 15
        )

        val graphBlock = listOf(block).toWorkoutGraphBlocks(TrainingSportType.RUNNING).single()

        assertEquals(WorkoutGraphUnit.SpeedKmh, graphBlock.unit)
        assertEquals(16f, graphBlock.value, 0.01f)
        assertEquals("3:45 (16km/h)", block.runningTargetSpeedText())
        assertEquals("1%", block.runningInclineText())
    }

    @Test
    fun runningGraph_prefersBracketedKmhWhenInclineAppearsBeforeSpeed() {
        val block = graphTestBlock(
            title = "All Out",
            kind = "work",
            targetText = "3:45 pace [1% 16km/h] All Out",
            durationSeconds = 15
        )

        val graphBlock = listOf(block).toWorkoutGraphBlocks(TrainingSportType.RUNNING).single()

        assertEquals(WorkoutGraphUnit.SpeedKmh, graphBlock.unit)
        assertEquals(16f, graphBlock.value, 0.01f)
        assertEquals("3:45 (16km/h)", block.runningTargetSpeedText())
        assertEquals("1%", block.runningInclineText())
    }

    @Test
    fun runningGraph_prefersExplicitDescriptionKmhOverWrittenPace() {
        val block = graphTestBlock(
            targetText = "5:30 pace · 12 Km/h",
            durationSeconds = 60
        )

        assertEquals(12f, block.graphTargetSpeedKmh() ?: 0f, 0.01f)
        assertEquals("5:00 (12km/h)", block.runningTargetSpeedText())
    }

    @Test
    fun runningIncline_ignoresPacePercentWhenDescriptionSpeedContextIsAppended() {
        val block = graphTestBlock(
            title = "All Out",
            kind = "work",
            targetText = "62.5% · 1% · 16km/h 1%",
            durationSeconds = 15
        )

        assertEquals(16f, block.graphTargetSpeedKmh() ?: 0f, 0.01f)
        assertEquals("1%", block.runningInclineText())
    }

    @Test
    fun runningIncline_ignoresPacePercentEvenWithoutStructuredGradeSegment() {
        val block = graphTestBlock(
            title = "All Out",
            kind = "work",
            targetText = "62.5% · 16km/h 1%",
            durationSeconds = 15
        )

        assertEquals("1%", block.runningInclineText())
    }
}

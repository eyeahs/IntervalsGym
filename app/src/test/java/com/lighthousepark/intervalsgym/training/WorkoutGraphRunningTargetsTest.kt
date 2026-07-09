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
    fun runningGraph_prefersLeadingMetersPerSecondRangeOverLaterSpeedContext() {
        val block = graphTestBlock(targetText = "1.6-1.7 · 10km/h")

        val graphBlock = listOf(block).toWorkoutGraphBlocks(TrainingSportType.RUNNING).single()

        assertEquals(WorkoutGraphUnit.SpeedKmh, graphBlock.unit)
        assertEquals(5.94f, graphBlock.value, 0.2f)
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

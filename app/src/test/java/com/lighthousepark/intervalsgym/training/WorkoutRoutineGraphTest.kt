package com.lighthousepark.intervalsgym.training

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
import org.junit.Test

class WorkoutRoutineGraphTest {
    @Test
    fun runningGraph_usesPaceAsSpeedAndTreatsPercentAsIncline() {
        val block = RoutineBlock(
            index = 0,
            title = "Block 1",
            kind = "Run",
            targetText = "12:00 pace 20%",
            durationSeconds = 60,
            startSecond = 0,
            endSecond = 60,
            isRecovery = false
        )

        val graphBlock = listOf(block).toWorkoutGraphBlocks(TrainingSportType.RUNNING).single()

        assertEquals(WorkoutGraphUnit.SpeedKmh, graphBlock.unit)
        assertEquals(5f, graphBlock.value, 0.01f)
        assertEquals("20%", block.runningInclineText())
        assertEquals("12:00 (5km/h)", block.runningTargetSpeedText())
    }

    @Test
    fun speedAxisLabelForZeroShowsOnlyZero() {
        assertEquals(listOf("0"), 0f.formatGraphAxisLabels(WorkoutGraphUnit.SpeedKmh))
    }

    @Test
    fun runningGraph_prefersLeadingMetersPerSecondRangeOverLaterSpeedContext() {
        val block = RoutineBlock(
            index = 0,
            title = "Workout",
            kind = "work",
            targetText = "1.6-1.7 · 10km/h",
            durationSeconds = 60,
            startSecond = 0,
            endSecond = 60,
            isRecovery = false
        )

        val graphBlock = listOf(block).toWorkoutGraphBlocks(TrainingSportType.RUNNING).single()

        assertEquals(WorkoutGraphUnit.SpeedKmh, graphBlock.unit)
        assertEquals(5.94f, graphBlock.value, 0.2f)
    }

    @Test
    fun runningGraph_prefersBracketedKmhOverWrittenPaceApproximation() {
        val block = RoutineBlock(
            index = 0,
            title = "All Out",
            kind = "work",
            targetText = "3:44 pace [16km/h 1%] All Out",
            durationSeconds = 15,
            startSecond = 0,
            endSecond = 15,
            isRecovery = false
        )

        val graphBlock = listOf(block).toWorkoutGraphBlocks(TrainingSportType.RUNNING).single()

        assertEquals(WorkoutGraphUnit.SpeedKmh, graphBlock.unit)
        assertEquals(16f, graphBlock.value, 0.01f)
        assertEquals("3:45 (16km/h)", block.runningTargetSpeedText())
        assertEquals("1%", block.runningInclineText())
    }

    @Test
    fun runningGraph_prefersBracketedKmhWhenInclineAppearsBeforeSpeed() {
        val block = RoutineBlock(
            index = 0,
            title = "All Out",
            kind = "work",
            targetText = "3:45 pace [1% 16km/h] All Out",
            durationSeconds = 15,
            startSecond = 0,
            endSecond = 15,
            isRecovery = false
        )

        val graphBlock = listOf(block).toWorkoutGraphBlocks(TrainingSportType.RUNNING).single()

        assertEquals(WorkoutGraphUnit.SpeedKmh, graphBlock.unit)
        assertEquals(16f, graphBlock.value, 0.01f)
        assertEquals("3:45 (16km/h)", block.runningTargetSpeedText())
        assertEquals("1%", block.runningInclineText())
    }

    @Test
    fun runningIncline_ignoresPacePercentWhenDescriptionSpeedContextIsAppended() {
        val block = RoutineBlock(
            index = 0,
            title = "All Out",
            kind = "work",
            targetText = "62.5% · 1% · 16km/h 1%",
            durationSeconds = 15,
            startSecond = 0,
            endSecond = 15,
            isRecovery = false
        )

        assertEquals(16f, block.graphTargetSpeedKmh() ?: 0f, 0.01f)
        assertEquals("1%", block.runningInclineText())
    }

    @Test
    fun runningIncline_ignoresPacePercentEvenWithoutStructuredGradeSegment() {
        val block = RoutineBlock(
            index = 0,
            title = "All Out",
            kind = "work",
            targetText = "62.5% · 16km/h 1%",
            durationSeconds = 15,
            startSecond = 0,
            endSecond = 15,
            isRecovery = false
        )

        assertEquals("1%", block.runningInclineText())
    }

    @Test
    fun cyclingGraph_usesUnitlessWattsAndFtpPercentContext() {
        val block = RoutineBlock(
            index = 0,
            title = "Z4",
            kind = "Bike",
            targetText = "240 · 80%ftp",
            durationSeconds = 300,
            startSecond = 0,
            endSecond = 300,
            isRecovery = false
        )

        val graphBlock = listOf(block).toWorkoutGraphBlocks(TrainingSportType.CYCLING).single()

        assertEquals(WorkoutGraphUnit.Watts, graphBlock.unit)
        assertEquals(240f, graphBlock.value, 0.01f)
        assertEquals(80f, graphBlock.intensityPercent ?: -1f, 0.01f)
    }
}

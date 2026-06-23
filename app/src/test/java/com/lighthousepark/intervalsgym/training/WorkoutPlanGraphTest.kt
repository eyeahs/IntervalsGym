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

class WorkoutPlanGraphTest {
    @Test
    fun runningGraph_usesPaceAsSpeedAndTreatsPercentAsIncline() {
        val block = PlanBlock(
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
    fun cyclingGraph_usesUnitlessWattsAndFtpPercentContext() {
        val block = PlanBlock(
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

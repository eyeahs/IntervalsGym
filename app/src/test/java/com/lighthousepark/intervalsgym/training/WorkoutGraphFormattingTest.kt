package com.lighthousepark.intervalsgym.training

import com.lighthousepark.intervalsgym.ui.theme.AppGraphOrange1
import com.lighthousepark.intervalsgym.ui.theme.AppGraphOrange3
import com.lighthousepark.intervalsgym.ui.theme.AppGraphOrange7
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutGraphFormattingTest {
    @Test
    fun speedAxisLabelForZeroShowsOnlyZero() {
        assertEquals(listOf("0"), 0f.formatGraphAxisLabels(WorkoutGraphUnit.SpeedKmh))
    }

    @Test
    fun graphColorsUseGlobalOrangePaletteForRunningAndCyclingIntensity() {
        val block = graphTestBlock(targetText = "10km/h")
        val speed = WorkoutGraphBlock(
            block = block,
            value = 10f,
            unit = WorkoutGraphUnit.SpeedKmh
        )
        val cyclingRecovery = WorkoutGraphBlock(
            block = block,
            value = 100f,
            unit = WorkoutGraphUnit.Watts,
            intensityPercent = 50f
        )
        val cyclingMaximum = cyclingRecovery.copy(intensityPercent = 130f)

        assertEquals(
            AppGraphOrange3,
            speed.graphColor(12f, WorkoutGraphUnit.SpeedKmh, TrainingSportType.RUNNING)
        )
        assertEquals(
            AppGraphOrange1,
            cyclingRecovery.graphColor(200f, WorkoutGraphUnit.Watts, TrainingSportType.CYCLING)
        )
        assertEquals(
            AppGraphOrange7,
            cyclingMaximum.graphColor(200f, WorkoutGraphUnit.Watts, TrainingSportType.CYCLING)
        )
    }
}

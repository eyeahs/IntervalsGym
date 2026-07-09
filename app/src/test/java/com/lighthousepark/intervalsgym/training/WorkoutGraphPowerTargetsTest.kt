package com.lighthousepark.intervalsgym.training

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutGraphPowerTargetsTest {
    @Test
    fun cyclingGraph_usesUnitlessWattsAndFtpPercentContext() {
        val block = graphTestBlock(
            title = "Z4",
            kind = "Bike",
            targetText = "240 · 80%ftp",
            durationSeconds = 300
        )

        val graphBlock = listOf(block).toWorkoutGraphBlocks(TrainingSportType.CYCLING).single()

        assertEquals(WorkoutGraphUnit.Watts, graphBlock.unit)
        assertEquals(240f, graphBlock.value, 0.01f)
        assertEquals(80f, graphBlock.intensityPercent ?: -1f, 0.01f)
    }
}

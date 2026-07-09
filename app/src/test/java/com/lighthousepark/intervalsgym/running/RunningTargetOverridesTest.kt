package com.lighthousepark.intervalsgym.running

import com.lighthousepark.intervalsgym.training.graphTargetSpeedKmh
import com.lighthousepark.intervalsgym.training.runningInclineText
import com.lighthousepark.intervalsgym.training.runningTargetSpeedText
import org.junit.Assert.assertEquals
import org.junit.Test

class RunningTargetOverridesTest {
    @Test
    fun withRunningTargetOverride_roundTripsSpeedAndIncline() {
        val block = routineBlock(index = 0, durationSeconds = 60, targetText = "6km/h · 4%")
            .withRunningTargetOverride(speedKmh = 7.2f, inclinePercent = 5f)

        assertEquals(7.2f, block.graphTargetSpeedKmh() ?: 0f, 0.01f)
        assertEquals("8:20 (7.2km/h)", block.runningTargetSpeedText())
        assertEquals("5%", block.runningInclineText())
    }

    @Test
    fun runningTargetOverrideChange_growsOverridesAndClampsTargets() {
        val blocks = listOf(
            routineBlock(index = 0, durationSeconds = 60, targetText = "6km/h · 1%"),
            routineBlock(index = 1, durationSeconds = 60, targetText = "8km/h · 2%")
        )

        val change = runningTargetOverrideChange(
            blocks = blocks,
            displayBlocks = blocks,
            targetOverrides = emptyList(),
            currentBlockIndex = 1,
            speedDeltaKmh = 99f,
            inclineDeltaPercent = -99f
        )

        requireNotNull(change)
        assertEquals(2, change.targetOverrides.size)
        assertEquals("", change.targetOverrides[0])
        assertEquals("30km/h · 0%", change.targetOverrides[1])
        assertEquals(30f, change.nextSpeedKmh, 0.01f)
        assertEquals(0f, change.nextInclinePercent, 0.01f)
    }
}

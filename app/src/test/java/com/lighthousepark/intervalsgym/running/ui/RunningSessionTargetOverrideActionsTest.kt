package com.lighthousepark.intervalsgym.running.ui

import com.lighthousepark.intervalsgym.running.routineBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningSessionTargetOverrideActionsTest {
    @Test
    fun planRunningSessionTargetOverrideAction_updatesOverridesAndBuildsDiagnostics() {
        val blocks = listOf(
            routineBlock(index = 0, targetText = "10km/h 1%", durationSeconds = 60),
            routineBlock(index = 1, targetText = "8km/h 0%", durationSeconds = 60)
        )

        val action = planRunningSessionTargetOverrideAction(
            blocks = blocks,
            displayBlocks = blocks,
            targetOverrides = emptyList(),
            currentBlockIndex = 0,
            speedDeltaKmh = 0.1f,
            inclineDeltaPercent = 1f
        )

        requireNotNull(action)
        assertEquals(2, action.targetOverrides.size)
        assertTrue(action.targetOverrides[0].contains("10.1km/h"))
        assertTrue(action.targetOverrides[0].contains("2%"))
        assertTrue(action.diagnosticDetails.contains("speedDeltaKmh=0.1"))
        assertTrue(action.diagnosticDetails.contains("inclineDeltaPercent=1.0"))
    }

    @Test
    fun planRunningSessionTargetOverrideAction_returnsNullForInvalidBlockIndex() {
        val blocks = listOf(routineBlock(index = 0, targetText = "10km/h", durationSeconds = 60))

        val action = planRunningSessionTargetOverrideAction(
            blocks = blocks,
            displayBlocks = blocks,
            targetOverrides = emptyList(),
            currentBlockIndex = 4,
            speedDeltaKmh = 0.1f,
            inclineDeltaPercent = 0f
        )

        assertNull(action)
    }
}

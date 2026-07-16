package com.lighthousepark.intervalsgym.running.ui

import com.lighthousepark.intervalsgym.running.RunningSessionPhase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningSessionOverlayActionsTest {
    @Test
    fun warmupActionUpdatesOverlayWithoutOpeningApp() {
        assertFalse(
            runningOverlayOpensAppOnPrimaryAction(
                phase = RunningSessionPhase.WARMUP,
                isLastBlock = false
            )
        )
    }

    @Test
    fun onlyLastBlockActionOpensAppForSaving() {
        assertFalse(
            runningOverlayOpensAppOnPrimaryAction(
                phase = RunningSessionPhase.BLOCK,
                isLastBlock = false
            )
        )
        assertTrue(
            runningOverlayOpensAppOnPrimaryAction(
                phase = RunningSessionPhase.BLOCK,
                isLastBlock = true
            )
        )
    }
}

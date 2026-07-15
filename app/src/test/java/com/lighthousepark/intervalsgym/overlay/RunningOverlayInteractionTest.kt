package com.lighthousepark.intervalsgym.overlay

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningOverlayInteractionTest {
    @Test
    fun blockSkipRequestsActionWithoutOpeningApp() {
        val plan = planRunningOverlayTap(
            target = RunningOverlayTapTarget.PRIMARY_ACTION,
            openAppOnPrimaryAction = false
        )

        assertTrue(plan.requestPrimaryAction)
        assertFalse(plan.requestOpen)
        assertFalse(plan.openApp)
    }

    @Test
    fun finishActionCanRequestActionAndOpenApp() {
        val plan = planRunningOverlayTap(
            target = RunningOverlayTapTarget.PRIMARY_ACTION,
            openAppOnPrimaryAction = true
        )

        assertTrue(plan.requestPrimaryAction)
        assertFalse(plan.requestOpen)
        assertTrue(plan.openApp)
    }

    @Test
    fun contentTapOnlyOpensApp() {
        val plan = planRunningOverlayTap(
            target = RunningOverlayTapTarget.CONTENT,
            openAppOnPrimaryAction = false
        )

        assertFalse(plan.requestPrimaryAction)
        assertTrue(plan.requestOpen)
        assertTrue(plan.openApp)
    }
}

package com.lighthousepark.intervalsgym.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RapidActionThrottleTest {
    @Test
    fun tryRun_blocksOnlyActionsWithinThrottleWindow() {
        var nowMillis = 1_000L
        var actionCount = 0
        val throttle = RapidActionThrottle(
            throttleMillis = 500L,
            nowMillis = { nowMillis }
        )

        assertTrue(throttle.tryRun { actionCount += 1 })
        nowMillis += 100L
        assertFalse(throttle.tryRun { actionCount += 1 })
        nowMillis += 400L
        assertTrue(throttle.tryRun { actionCount += 1 })

        assertEquals(2, actionCount)
    }
}

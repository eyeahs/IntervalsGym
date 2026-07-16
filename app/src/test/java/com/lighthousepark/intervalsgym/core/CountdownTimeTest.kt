package com.lighthousepark.intervalsgym.core

import org.junit.Assert.assertEquals
import org.junit.Test

class CountdownTimeTest {
    @Test
    fun remainingCountdownSeconds_roundsPartialSecondsUpConsistently() {
        assertEquals(3, remainingCountdownSeconds(endAtMillis = 4_000L, nowMillis = 1_001L))
        assertEquals(2, remainingCountdownSeconds(endAtMillis = 4_000L, nowMillis = 2_000L))
        assertEquals(1, remainingCountdownSeconds(endAtMillis = 4_000L, nowMillis = 3_999L))
        assertEquals(0, remainingCountdownSeconds(endAtMillis = 4_000L, nowMillis = 4_000L))
        assertEquals(0, remainingCountdownSeconds(endAtMillis = 4_000L, nowMillis = 5_000L))
    }
}

package com.lighthousepark.intervalsgym.running.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningSessionDiagnosticsTest {
    @Test
    fun diagnosticRateLimiter_dropsRapidDuplicateEventsButAllowsProgress() {
        val limiter = RunningSessionDiagnosticRateLimiter(minIntervalMillis = 250L)

        assertTrue(limiter.shouldLog(event = "catch up elapsed blocks", nowMillis = 1_000L))
        assertFalse(limiter.shouldLog(event = "catch up elapsed blocks", nowMillis = 1_001L))
        assertTrue(limiter.shouldLog(event = "block started", nowMillis = 1_001L))
        assertTrue(limiter.shouldLog(event = "catch up elapsed blocks", nowMillis = 1_250L))
    }
}

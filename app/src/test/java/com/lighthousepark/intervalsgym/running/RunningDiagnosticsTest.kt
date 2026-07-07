package com.lighthousepark.intervalsgym.running

import com.lighthousepark.intervalsgym.training.RoutineBlock
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningDiagnosticsTest {
    @Test
    fun runningBlocksDiagnosticText_includesParsedSpeedAndIncline() {
        val text = listOf(
            RoutineBlock(
                index = 7,
                title = "All Out",
                kind = "work",
                targetText = "62.5% · 16km/h 1%",
                durationSeconds = 15,
                startSecond = 420,
                endSecond = 435,
                isRecovery = false
            )
        ).runningBlocksDiagnosticText(label = "session")

        assertTrue(text.contains("session count=1"))
        assertTrue(text.contains("#7"))
        assertTrue(text.contains("target=\"62.5% · 16km/h 1%\""))
        assertTrue(text.contains("speedKmh=16.00"))
        assertTrue(text.contains("inclinePercent=1.00"))
    }
}

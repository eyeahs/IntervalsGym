package com.lighthousepark.intervalsgym.running

import com.lighthousepark.intervalsgym.training.RoutineBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningDiagnosticsTest {
    @Test
    fun runningBlocksDiagnosticText_includesRawBlockStateWithoutTargetParsing() {
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
    }

    @Test
    fun runningBlocksDiagnosticText_limitsBlockCountAndFieldLength() {
        val blocks = List(12) { index ->
            RoutineBlock(
                index = index,
                title = "x".repeat(500),
                kind = "work",
                targetText = "y".repeat(500),
                durationSeconds = 30,
                startSecond = index * 30,
                endSecond = (index + 1) * 30,
                isRecovery = false
            )
        }

        val text = blocks.runningBlocksDiagnosticText(label = "session")

        assertEquals(8, text.lineSequence().count { it.startsWith("#") })
        assertTrue(text.contains("4 more blocks omitted"))
        assertTrue(text.length < 4_000)
    }
}

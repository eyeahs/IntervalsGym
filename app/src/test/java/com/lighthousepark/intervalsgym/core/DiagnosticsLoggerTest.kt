package com.lighthousepark.intervalsgym.core

import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsLoggerTest {
    @Test
    fun appendDiagnosticLogEntry_writesTagMessageAndThrowable() {
        val dir = Files.createTempDirectory("intervals-gym-diagnostics").toFile()
        val logFile = File(dir, "diagnostics.log")

        appendDiagnosticLogEntry(
            logFile = logFile,
            tag = "RunningSession",
            message = "plan=Morning Run\nblock=7 speed=16km/h incline=1%",
            throwable = IllegalStateException("bad state"),
            timestamp = "2026-07-01T12:00:00+09:00"
        )

        val text = logFile.readText()
        assertTrue(text.contains("[RunningSession]"))
        assertTrue(text.contains("plan=Morning Run"))
        assertTrue(text.contains("block=7 speed=16km/h incline=1%"))
        assertTrue(text.contains("java.lang.IllegalStateException: bad state"))
    }

    @Test
    fun appendDiagnosticLogEntry_rotatesLargeLogFile() {
        val dir = Files.createTempDirectory("intervals-gym-diagnostics").toFile()
        val logFile = File(dir, "diagnostics.log")
        logFile.writeText("x".repeat(32))

        appendDiagnosticLogEntry(
            logFile = logFile,
            tag = "Test",
            message = "after rotation",
            timestamp = "2026-07-01T12:00:00+09:00",
            maxBytes = 8
        )

        assertTrue(File(dir, "diagnostics.log.1").exists())
        assertTrue(logFile.readText().contains("after rotation"))
    }
}

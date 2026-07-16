package com.lighthousepark.intervalsgym.core

import java.io.File
import java.nio.file.Files
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsLoggerTest {
    @Test
    fun enqueueDiagnosticLogWrite_runsFileWorkOffCallingThread() {
        val callerThread = Thread.currentThread()
        val executor = Executors.newSingleThreadExecutor()
        val completed = CountDownLatch(1)
        var writerThread: Thread? = null

        try {
            enqueueDiagnosticLogWrite(executor) {
                writerThread = Thread.currentThread()
                completed.countDown()
            }

            assertTrue(completed.await(2, TimeUnit.SECONDS))
            assertNotEquals(callerThread, writerThread)
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun diagnosticLogExecutor_boundsQueueAndKeepsLatestWork() {
        val executor = createDiagnosticLogExecutor(queueCapacity = 2)
        val writerStarted = CountDownLatch(1)
        val releaseWriter = CountDownLatch(1)
        val latestWorkCompleted = CountDownLatch(1)

        try {
            executor.execute {
                writerStarted.countDown()
                releaseWriter.await(2, TimeUnit.SECONDS)
            }
            assertTrue(writerStarted.await(2, TimeUnit.SECONDS))

            executor.execute {}
            executor.execute {}
            executor.execute { latestWorkCompleted.countDown() }

            assertEquals(2, executor.queue.size)
            releaseWriter.countDown()
            assertTrue(latestWorkCompleted.await(2, TimeUnit.SECONDS))
        } finally {
            releaseWriter.countDown()
            executor.shutdownNow()
        }
    }

    @Test
    fun limitDiagnosticMessage_truncatesOversizedMessages() {
        val limited = limitDiagnosticMessage(
            message = "x".repeat(32),
            maxChars = 8
        )

        assertTrue(limited.startsWith("x".repeat(8)))
        assertTrue(limited.contains("truncated"))
    }

    @Test
    fun appendDiagnosticLogEntry_writesTagMessageAndThrowable() {
        val dir = Files.createTempDirectory("intervals-gym-diagnostics").toFile()
        val logFile = File(dir, "diagnostics.log")

        appendDiagnosticLogEntry(
            logFile = logFile,
            tag = "RunningSession",
            message = "routine=Morning Run\nblock=7 speed=16km/h incline=1%",
            throwable = IllegalStateException("bad state"),
            timestamp = "2026-07-01T12:00:00+09:00"
        )

        val text = logFile.readText()
        assertTrue(text.contains("[RunningSession]"))
        assertTrue(text.contains("routine=Morning Run"))
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

package com.lighthousepark.intervalsgym.running

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningSessionProgressSnapshotsTest {
    @Test
    fun currentBlockIndex_returnsActiveBlockOnly() {
        val blocks = listOf(
            routineBlock(index = 0, durationSeconds = 60).copy(startSecond = 0, endSecond = 60),
            routineBlock(index = 1, durationSeconds = 30).copy(startSecond = 60, endSecond = 90)
        )

        assertEquals(0, currentBlockIndex(blocks, elapsedSeconds = 30))
        assertEquals(1, currentBlockIndex(blocks, elapsedSeconds = 60))
        assertEquals(-1, currentBlockIndex(blocks, elapsedSeconds = 90))
    }

    @Test
    fun runningRepeatProgressText_formatsOnlyValidRepeatedBlocks() {
        val block = routineBlock(index = 0, durationSeconds = 60)

        assertEquals("반복 3 / 8", block.copy(repeatIteration = 3, repeatCount = 8).runningRepeatProgressText())
        assertEquals(null, block.runningRepeatProgressText())
        assertEquals(null, block.copy(repeatIteration = 1, repeatCount = 1).runningRepeatProgressText())
        assertEquals(null, block.copy(repeatIteration = 5, repeatCount = 4).runningRepeatProgressText())
    }

    @Test
    fun runningSessionProgressSnapshot_calculatesWarmupBlockAndFinishedProgress() {
        val block = routineBlock(index = 0, durationSeconds = 60).copy(startSecond = 10, endSecond = 70)

        val warmup = runningSessionProgressSnapshot(
            phase = RunningSessionPhase.WARMUP,
            currentBlock = null,
            warmupStartedAtMillis = 1_000L,
            blockEndAtMillis = 0L,
            nowMillis = 6_500L,
            totalSeconds = 180
        )
        val active = runningSessionProgressSnapshot(
            phase = RunningSessionPhase.BLOCK,
            currentBlock = block,
            warmupStartedAtMillis = 1_000L,
            blockEndAtMillis = 61_000L,
            nowMillis = 58_200L,
            totalSeconds = 180
        )
        val finished = runningSessionProgressSnapshot(
            phase = RunningSessionPhase.FINISHED,
            currentBlock = block,
            warmupStartedAtMillis = 1_000L,
            blockEndAtMillis = 61_000L,
            nowMillis = 61_000L,
            totalSeconds = 180
        )

        assertEquals(5, warmup.warmupElapsedSeconds)
        assertEquals(null, warmup.progressSeconds)
        assertEquals(3, active.blockRemainingSeconds)
        assertEquals(57, active.blockElapsedSeconds)
        assertEquals(67, active.progressSeconds)
        assertTrue(active.isUrgent)
        assertEquals(180, finished.progressSeconds)
    }
}

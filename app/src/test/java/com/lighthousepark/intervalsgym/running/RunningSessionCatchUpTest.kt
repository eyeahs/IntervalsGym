package com.lighthousepark.intervalsgym.running

import org.junit.Assert.assertEquals
import org.junit.Test

class RunningSessionCatchUpTest {
    @Test
    fun catchUpRunningSessionBlocks_finishesAtScheduledEndAfterLongPause() {
        val blocks = listOf(
            routineBlock(index = 0, durationSeconds = 60),
            routineBlock(index = 1, durationSeconds = 30)
        )

        val result = catchUpRunningSessionBlocks(
            blocks = blocks,
            currentBlockIndex = 0,
            blockStartedAtMillis = 1_000L,
            blockEndAtMillis = 61_000L,
            actualBlocks = emptyList(),
            nowMillis = 600_000L
        )

        requireNotNull(result)
        assertEquals(91_000L, result.finishedAtMillis)
        assertEquals(listOf(60, 30), result.actualBlocks.map { it.durationSeconds })
    }

    @Test
    fun catchUpRunningSessionBlocks_advancesIntoElapsedNextBlock() {
        val blocks = listOf(
            routineBlock(index = 0, durationSeconds = 60),
            routineBlock(index = 1, durationSeconds = 60),
            routineBlock(index = 2, durationSeconds = 60)
        )

        val result = catchUpRunningSessionBlocks(
            blocks = blocks,
            currentBlockIndex = 0,
            blockStartedAtMillis = 1_000L,
            blockEndAtMillis = 61_000L,
            actualBlocks = emptyList(),
            nowMillis = 90_000L
        )

        requireNotNull(result)
        assertEquals(null, result.finishedAtMillis)
        assertEquals(1, result.currentBlockIndex)
        assertEquals(61_000L, result.blockStartedAtMillis)
        assertEquals(121_000L, result.blockEndAtMillis)
        assertEquals(listOf(60), result.actualBlocks.map { it.durationSeconds })
    }

    @Test
    fun catchUpRunningSessionBlocks_restoresMissingPreviousBlocks() {
        val blocks = listOf(
            routineBlock(index = 0, durationSeconds = 60),
            routineBlock(index = 1, durationSeconds = 30)
        )

        val result = catchUpRunningSessionBlocks(
            blocks = blocks,
            currentBlockIndex = 1,
            blockStartedAtMillis = 61_000L,
            blockEndAtMillis = 91_000L,
            actualBlocks = emptyList(),
            nowMillis = 100_000L
        )

        requireNotNull(result)
        assertEquals(91_000L, result.finishedAtMillis)
        assertEquals(listOf(60, 30), result.actualBlocks.map { it.durationSeconds })
    }
}

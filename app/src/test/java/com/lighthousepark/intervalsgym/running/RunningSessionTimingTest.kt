package com.lighthousepark.intervalsgym.running

import com.lighthousepark.intervalsgym.core.SESSION_AUTO_LOCAL_SAVE_DELAY_MILLIS
import org.junit.Assert.assertEquals
import org.junit.Test

class RunningSessionTimingTest {
    @Test
    fun shouldAutoLocalSaveLastRunningBlock_requiresLastBlockAndThirtyMinuteDelay() {
        val lastBlockEndAtMillis = 1_000L
        val autoSaveAtMillis = lastBlockEndAtMillis + SESSION_AUTO_LOCAL_SAVE_DELAY_MILLIS

        assertEquals(
            false,
            shouldAutoLocalSaveLastRunningBlock(
                currentBlockIndex = 2,
                blockCount = 3,
                blockEndAtMillis = lastBlockEndAtMillis,
                nowMillis = autoSaveAtMillis - 1L
            )
        )
        assertEquals(
            true,
            shouldAutoLocalSaveLastRunningBlock(
                currentBlockIndex = 2,
                blockCount = 3,
                blockEndAtMillis = lastBlockEndAtMillis,
                nowMillis = autoSaveAtMillis
            )
        )
        assertEquals(
            false,
            shouldAutoLocalSaveLastRunningBlock(
                currentBlockIndex = 1,
                blockCount = 3,
                blockEndAtMillis = lastBlockEndAtMillis,
                nowMillis = autoSaveAtMillis
            )
        )
        assertEquals(
            false,
            shouldAutoLocalSaveLastRunningBlock(
                currentBlockIndex = 0,
                blockCount = 0,
                blockEndAtMillis = lastBlockEndAtMillis,
                nowMillis = autoSaveAtMillis
            )
        )
        assertEquals(
            false,
            shouldAutoLocalSaveLastRunningBlock(
                currentBlockIndex = 2,
                blockCount = 3,
                blockEndAtMillis = 0L,
                nowMillis = autoSaveAtMillis
            )
        )
    }
}

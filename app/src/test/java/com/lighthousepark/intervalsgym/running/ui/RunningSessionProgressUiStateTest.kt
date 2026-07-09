package com.lighthousepark.intervalsgym.running.ui

import com.lighthousepark.intervalsgym.running.RunningSessionCatchUpResult
import com.lighthousepark.intervalsgym.running.RunningSessionPhase
import com.lighthousepark.intervalsgym.running.routineBlock
import org.junit.Assert.assertEquals
import org.junit.Test

class RunningSessionProgressUiStateTest {
    @Test
    fun initialStateStartsWarmupWithStableStartTime() {
        val state = RunningSessionProgressUiState.initial(nowMillis = 1_000L)

        assertEquals(RunningSessionPhase.WARMUP, state.phase)
        assertEquals(0, state.currentBlockIndex)
        assertEquals(1_000L, state.warmupStartedAtMillis)
        assertEquals(0L, state.blockStartedAtMillis)
        assertEquals(0L, state.blockEndAtMillis)
    }

    @Test
    fun startedBlockSetsBlockPhaseIndexAndTimingTogether() {
        val block = routineBlock(index = 2, durationSeconds = 45, targetText = "8km/h")
        val state = RunningSessionProgressUiState.initial(nowMillis = 1_000L)
            .withStartedBlock(index = 2, block = block, startedAtMillis = 5_000L)

        assertEquals(RunningSessionPhase.BLOCK, state.phase)
        assertEquals(2, state.currentBlockIndex)
        assertEquals(5_000L, state.blockStartedAtMillis)
        assertEquals(50_000L, state.blockEndAtMillis)
    }

    @Test
    fun catchUpAndFinishUpdateOnlyProgressFieldsTheyOwn() {
        val state = RunningSessionProgressUiState.initial(nowMillis = 1_000L)
            .withStartedBlock(
                index = 0,
                block = routineBlock(index = 0, durationSeconds = 60, targetText = "6km/h"),
                startedAtMillis = 5_000L
            )
        val caughtUp = state.withCatchUp(
            RunningSessionCatchUpResult(
                currentBlockIndex = 1,
                blockStartedAtMillis = 65_000L,
                blockEndAtMillis = 125_000L,
                actualBlocks = emptyList()
            )
        )
        val finished = caughtUp.withFinished()

        assertEquals(RunningSessionPhase.BLOCK, caughtUp.phase)
        assertEquals(1, caughtUp.currentBlockIndex)
        assertEquals(65_000L, caughtUp.blockStartedAtMillis)
        assertEquals(125_000L, caughtUp.blockEndAtMillis)
        assertEquals(RunningSessionPhase.FINISHED, finished.phase)
        assertEquals(0L, finished.blockEndAtMillis)
    }

    @Test
    fun recordedCurrentBlockClearsOnlyBlockStart() {
        val state = RunningSessionProgressUiState.initial(nowMillis = 1_000L)
            .withStartedBlock(
                index = 0,
                block = routineBlock(index = 0, durationSeconds = 60, targetText = "6km/h"),
                startedAtMillis = 5_000L
            )
            .withCurrentBlockRecorded()

        assertEquals(RunningSessionPhase.BLOCK, state.phase)
        assertEquals(0, state.currentBlockIndex)
        assertEquals(0L, state.blockStartedAtMillis)
        assertEquals(65_000L, state.blockEndAtMillis)
    }
}

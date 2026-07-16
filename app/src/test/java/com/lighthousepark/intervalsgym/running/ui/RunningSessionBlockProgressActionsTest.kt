package com.lighthousepark.intervalsgym.running.ui

import com.lighthousepark.intervalsgym.running.RunningSessionPhase
import com.lighthousepark.intervalsgym.running.routineBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningSessionBlockProgressActionsTest {
    @Test
    fun advanceBlockActionRecordsAndStartsNextBlockAtomically() {
        val blocks = listOf(
            routineBlock(index = 0, durationSeconds = 60, targetText = "6km/h"),
            routineBlock(index = 1, durationSeconds = 45, targetText = "8km/h")
        )
        val progress = RunningSessionProgressUiState.initial(nowMillis = 1_000L)
            .withStartedBlock(index = 0, block = blocks[0], startedAtMillis = 2_000L)

        val action = planRunningSessionAdvanceBlockAction(
            blocks = blocks,
            displayBlocks = blocks,
            actualBlocks = emptyList(),
            progressUiState = progress,
            expectedBlockIndex = 0,
            expectedBlockStartedAtMillis = 2_000L,
            advancedAtMillis = 12_000L
        )

        require(action is RunningSessionAdvanceToNextBlock)
        assertEquals(listOf(10), action.actualBlocks.map { it.durationSeconds })
        assertEquals(1, action.progressUiState.currentBlockIndex)
        assertEquals(12_000L, action.progressUiState.blockStartedAtMillis)
        assertEquals(57_000L, action.progressUiState.blockEndAtMillis)
    }

    @Test
    fun advanceBlockActionRejectsStaleDuplicateTransition() {
        val blocks = listOf(
            routineBlock(index = 0, durationSeconds = 60),
            routineBlock(index = 1, durationSeconds = 45)
        )
        val progress = RunningSessionProgressUiState.initial(nowMillis = 1_000L)
            .withStartedBlock(index = 1, block = blocks[1], startedAtMillis = 12_000L)

        val action = planRunningSessionAdvanceBlockAction(
            blocks = blocks,
            displayBlocks = blocks,
            actualBlocks = listOf(blocks[0].copy(durationSeconds = 10)),
            progressUiState = progress,
            expectedBlockIndex = 0,
            expectedBlockStartedAtMillis = 2_000L,
            advancedAtMillis = 13_000L
        )

        assertNull(action)
    }

    @Test
    fun advanceBlockActionFinishesAfterLastBlock() {
        val block = routineBlock(index = 0, durationSeconds = 60)
        val progress = RunningSessionProgressUiState.initial(nowMillis = 1_000L)
            .withStartedBlock(index = 0, block = block, startedAtMillis = 2_000L)

        val action = planRunningSessionAdvanceBlockAction(
            blocks = listOf(block),
            displayBlocks = listOf(block),
            actualBlocks = emptyList(),
            progressUiState = progress,
            expectedBlockIndex = 0,
            expectedBlockStartedAtMillis = 2_000L,
            advancedAtMillis = 12_000L
        )

        require(action is RunningSessionAdvanceToFinish)
        assertEquals(listOf(10), action.actualBlocks.map { it.durationSeconds })
        assertEquals(0L, action.progressUiState.blockStartedAtMillis)
        assertEquals(12_000L, action.endedAtMillis)
    }

    @Test
    fun recordBlockActionRecordsActualBlockAndClearsBlockStart() {
        val block = routineBlock(index = 0, durationSeconds = 60, targetText = "6km/h")
        val progress = RunningSessionProgressUiState.initial(nowMillis = 1_000L)
            .withStartedBlock(
                index = 0,
                block = block,
                startedAtMillis = 2_000L
            )

        val action = planRunningSessionRecordBlockAction(
            actualBlocks = emptyList(),
            currentBlock = block,
            progressUiState = progress,
            endMillis = 12_000L
        )

        requireNotNull(action)
        assertEquals(listOf(10), action.actualBlocks.map { it.durationSeconds })
        assertEquals(0L, action.progressUiState.blockStartedAtMillis)
        assertTrue(action.diagnosticDetails.contains("endMillis=12000"))
        assertNull(
            planRunningSessionRecordBlockAction(
                actualBlocks = action.actualBlocks,
                currentBlock = block,
                progressUiState = action.progressUiState,
                endMillis = 20_000L
            )
        )
    }

    @Test
    fun catchUpActionAdvancesProgressAndCarriesFinishedTime() {
        val blocks = listOf(
            routineBlock(index = 0, durationSeconds = 60),
            routineBlock(index = 1, durationSeconds = 30)
        )
        val progress = RunningSessionProgressUiState.initial(nowMillis = 1_000L)
            .withStartedBlock(
                index = 0,
                block = blocks[0],
                startedAtMillis = 1_000L
            )

        val action = planRunningSessionCatchUpAction(
            displayBlocks = blocks,
            progressUiState = progress,
            actualBlocks = emptyList(),
            observedAtMillis = 600_000L
        )

        requireNotNull(action)
        assertEquals(91_000L, action.finishedAtMillis)
        assertEquals(listOf(60, 30), action.actualBlocks.map { it.durationSeconds })
        assertEquals(blocks.lastIndex, action.progressUiState.currentBlockIndex)
        assertEquals(0L, action.progressUiState.blockEndAtMillis)
        assertTrue(action.diagnosticDetails.contains("observedAtMillis=600000"))
    }

    @Test
    fun catchUpActionIgnoresNonBlockPhase() {
        val block = routineBlock(index = 0, durationSeconds = 60)

        val action = planRunningSessionCatchUpAction(
            displayBlocks = listOf(block),
            progressUiState = RunningSessionProgressUiState.initial(nowMillis = 1_000L),
            actualBlocks = emptyList(),
            observedAtMillis = 600_000L
        )

        assertNull(action)
    }

    @Test
    fun previousBlockActionDropsLastActualBlockAndReturnsPreviousIndex() {
        val blocks = listOf(
            routineBlock(index = 0, durationSeconds = 60),
            routineBlock(index = 1, durationSeconds = 45),
            routineBlock(index = 2, durationSeconds = 30)
        )
        val progress = RunningSessionProgressUiState.initial(nowMillis = 1_000L)
            .withStartedBlock(
                index = 2,
                block = blocks[2],
                startedAtMillis = 90_000L
            )

        val action = planRunningSessionPreviousBlockAction(
            actualBlocks = blocks.take(2),
            progressUiState = progress
        )

        requireNotNull(action)
        assertEquals(listOf(60), action.actualBlocks.map { it.durationSeconds })
        assertEquals(1, action.previousBlockIndex)
        assertEquals(0L, action.progressUiState.blockStartedAtMillis)
        assertNull(
            planRunningSessionPreviousBlockAction(
                actualBlocks = action.actualBlocks,
                progressUiState = progress.copy(currentBlockIndex = 0)
            )
        )
        assertNull(
            planRunningSessionPreviousBlockAction(
                actualBlocks = action.actualBlocks,
                progressUiState = RunningSessionProgressUiState.initial(nowMillis = 1_000L)
            )
        )
    }

    @Test
    fun startBlockActionBuildsStartedProgressAndDiagnostics() {
        val blocks = listOf(routineBlock(index = 0, durationSeconds = 45, targetText = "6km/h"))
        val action = planRunningSessionStartBlockAction(
            blocks = blocks,
            displayBlocks = listOf(blocks[0].copy(targetText = "7km/h")),
            progressUiState = RunningSessionProgressUiState.initial(nowMillis = 1_000L),
            index = 0,
            startedAtMillis = 5_000L
        )
        val unavailable = planRunningSessionStartBlockAction(
            blocks = blocks,
            displayBlocks = blocks,
            progressUiState = RunningSessionProgressUiState.initial(nowMillis = 1_000L),
            index = 3,
            startedAtMillis = 5_000L
        )

        require(action is RunningSessionStartBlockReady)
        assertEquals(RunningSessionPhase.BLOCK, action.progressUiState.phase)
        assertEquals(0, action.progressUiState.currentBlockIndex)
        assertEquals(50_000L, action.progressUiState.blockEndAtMillis)
        assertTrue(action.diagnosticDetails.contains("requestedIndex=0"))
        assertEquals(RunningSessionStartBlockUnavailable, unavailable)
    }
}

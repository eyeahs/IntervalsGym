package com.lighthousepark.intervalsgym.running.ui

import com.lighthousepark.intervalsgym.running.RunningSessionPhase
import com.lighthousepark.intervalsgym.running.catchUpRunningSessionBlocks
import com.lighthousepark.intervalsgym.running.recordRunningCurrentBlock
import com.lighthousepark.intervalsgym.training.RoutineBlock

internal data class RunningSessionRecordBlockAction(
    val actualBlocks: List<RoutineBlock>,
    val progressUiState: RunningSessionProgressUiState,
    val diagnosticDetails: String,
)

internal sealed interface RunningSessionAdvanceBlockAction {
    val actualBlocks: List<RoutineBlock>
    val recordDiagnosticDetails: String
}

internal data class RunningSessionAdvanceToNextBlock(
    override val actualBlocks: List<RoutineBlock>,
    override val recordDiagnosticDetails: String,
    val progressUiState: RunningSessionProgressUiState,
    val nowMillis: Long,
    val startDiagnosticDetails: String,
) : RunningSessionAdvanceBlockAction

internal data class RunningSessionAdvanceToFinish(
    override val actualBlocks: List<RoutineBlock>,
    override val recordDiagnosticDetails: String,
    val progressUiState: RunningSessionProgressUiState,
    val endedAtMillis: Long,
) : RunningSessionAdvanceBlockAction

internal fun planRunningSessionAdvanceBlockAction(
    blocks: List<RoutineBlock>,
    displayBlocks: List<RoutineBlock>,
    actualBlocks: List<RoutineBlock>,
    progressUiState: RunningSessionProgressUiState,
    expectedBlockIndex: Int,
    expectedBlockStartedAtMillis: Long,
    advancedAtMillis: Long,
): RunningSessionAdvanceBlockAction? {
    if (
        progressUiState.phase != RunningSessionPhase.BLOCK ||
        progressUiState.currentBlockIndex != expectedBlockIndex ||
        progressUiState.blockStartedAtMillis != expectedBlockStartedAtMillis ||
        expectedBlockStartedAtMillis <= 0L
    ) {
        return null
    }
    val currentBlock = displayBlocks.getOrNull(expectedBlockIndex)
        ?: blocks.getOrNull(expectedBlockIndex)
        ?: return null
    val recordAction = planRunningSessionRecordBlockAction(
        actualBlocks = actualBlocks,
        currentBlock = currentBlock,
        progressUiState = progressUiState,
        endMillis = advancedAtMillis
    ) ?: return null
    val nextIndex = expectedBlockIndex + 1
    val nextBlock = blocks.getOrNull(nextIndex)
        ?: return RunningSessionAdvanceToFinish(
            actualBlocks = recordAction.actualBlocks,
            recordDiagnosticDetails = recordAction.diagnosticDetails,
            progressUiState = recordAction.progressUiState,
            endedAtMillis = advancedAtMillis
        )
    val startedProgress = recordAction.progressUiState.withStartedBlock(
        index = nextIndex,
        block = nextBlock,
        startedAtMillis = advancedAtMillis
    )
    return RunningSessionAdvanceToNextBlock(
        actualBlocks = recordAction.actualBlocks,
        recordDiagnosticDetails = recordAction.diagnosticDetails,
        progressUiState = startedProgress,
        nowMillis = advancedAtMillis,
        startDiagnosticDetails = runningBlockStartedDiagnosticDetails(
            requestedIndex = nextIndex,
            startedAtMillis = advancedAtMillis,
            scheduledEndAtMillis = startedProgress.blockEndAtMillis,
            block = displayBlocks.getOrNull(nextIndex) ?: nextBlock
        )
    )
}

internal fun planRunningSessionRecordBlockAction(
    actualBlocks: List<RoutineBlock>,
    currentBlock: RoutineBlock?,
    progressUiState: RunningSessionProgressUiState,
    endMillis: Long,
): RunningSessionRecordBlockAction? {
    val result = recordRunningCurrentBlock(
        actualBlocks = actualBlocks,
        currentBlock = currentBlock,
        blockStartedAtMillis = progressUiState.blockStartedAtMillis,
        endMillis = endMillis
    )
    val recordedBlock = result.recordedBlock ?: return null
    return RunningSessionRecordBlockAction(
        actualBlocks = result.actualBlocks,
        progressUiState = progressUiState.withCurrentBlockRecorded(),
        diagnosticDetails = runningRecordedBlockDiagnosticDetails(
            endMillis = endMillis,
            recordedBlock = recordedBlock
        )
    )
}

internal data class RunningSessionCatchUpAction(
    val actualBlocks: List<RoutineBlock>,
    val progressUiState: RunningSessionProgressUiState,
    val observedAtMillis: Long,
    val finishedAtMillis: Long?,
    val diagnosticDetails: String,
)

internal fun planRunningSessionCatchUpAction(
    displayBlocks: List<RoutineBlock>,
    progressUiState: RunningSessionProgressUiState,
    actualBlocks: List<RoutineBlock>,
    observedAtMillis: Long,
): RunningSessionCatchUpAction? {
    if (progressUiState.phase != RunningSessionPhase.BLOCK) return null
    val result = catchUpRunningSessionBlocks(
        blocks = displayBlocks,
        currentBlockIndex = progressUiState.currentBlockIndex,
        blockStartedAtMillis = progressUiState.blockStartedAtMillis,
        blockEndAtMillis = progressUiState.blockEndAtMillis,
        actualBlocks = actualBlocks,
        nowMillis = observedAtMillis
    ) ?: return null
    val nextProgressUiState = progressUiState.withCatchUp(result)
    if (
        result.finishedAtMillis == null &&
        nextProgressUiState == progressUiState &&
        result.actualBlocks == actualBlocks
    ) {
        return null
    }
    return RunningSessionCatchUpAction(
        actualBlocks = result.actualBlocks,
        progressUiState = nextProgressUiState,
        observedAtMillis = observedAtMillis,
        finishedAtMillis = result.finishedAtMillis,
        diagnosticDetails = runningCatchUpDiagnosticDetails(
            observedAtMillis = observedAtMillis,
            result = result
        )
    )
}

internal data class RunningSessionPreviousBlockAction(
    val actualBlocks: List<RoutineBlock>,
    val progressUiState: RunningSessionProgressUiState,
    val previousBlockIndex: Int,
)

internal fun planRunningSessionPreviousBlockAction(
    actualBlocks: List<RoutineBlock>,
    progressUiState: RunningSessionProgressUiState,
): RunningSessionPreviousBlockAction? {
    if (progressUiState.phase != RunningSessionPhase.BLOCK || progressUiState.currentBlockIndex <= 0) {
        return null
    }
    return RunningSessionPreviousBlockAction(
        actualBlocks = actualBlocks.dropLast(1),
        progressUiState = progressUiState.withCurrentBlockRecorded(),
        previousBlockIndex = progressUiState.currentBlockIndex - 1
    )
}

internal sealed interface RunningSessionStartBlockAction

internal data class RunningSessionStartBlockReady(
    val progressUiState: RunningSessionProgressUiState,
    val nowMillis: Long,
    val diagnosticDetails: String,
) : RunningSessionStartBlockAction

internal data object RunningSessionStartBlockUnavailable : RunningSessionStartBlockAction

internal fun planRunningSessionStartBlockAction(
    blocks: List<RoutineBlock>,
    displayBlocks: List<RoutineBlock>,
    progressUiState: RunningSessionProgressUiState,
    index: Int,
    startedAtMillis: Long,
): RunningSessionStartBlockAction {
    val block = blocks.getOrNull(index) ?: return RunningSessionStartBlockUnavailable
    val startedProgress = progressUiState.withStartedBlock(
        index = index,
        block = block,
        startedAtMillis = startedAtMillis
    )
    return RunningSessionStartBlockReady(
        progressUiState = startedProgress,
        nowMillis = startedAtMillis,
        diagnosticDetails = runningBlockStartedDiagnosticDetails(
            requestedIndex = index,
            startedAtMillis = startedAtMillis,
            scheduledEndAtMillis = startedProgress.blockEndAtMillis,
            block = displayBlocks.getOrNull(index) ?: block
        )
    )
}

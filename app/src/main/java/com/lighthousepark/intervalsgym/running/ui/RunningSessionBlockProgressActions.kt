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
    return RunningSessionCatchUpAction(
        actualBlocks = result.actualBlocks,
        progressUiState = progressUiState.withCatchUp(result),
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

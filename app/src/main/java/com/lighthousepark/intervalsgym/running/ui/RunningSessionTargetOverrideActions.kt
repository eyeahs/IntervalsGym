package com.lighthousepark.intervalsgym.running.ui

import com.lighthousepark.intervalsgym.running.runningTargetOverrideChange
import com.lighthousepark.intervalsgym.training.RoutineBlock

internal data class RunningSessionTargetOverrideAction(
    val targetOverrides: List<String>,
    val diagnosticDetails: String,
)

internal fun planRunningSessionTargetOverrideAction(
    blocks: List<RoutineBlock>,
    displayBlocks: List<RoutineBlock>,
    targetOverrides: List<String>,
    currentBlockIndex: Int,
    speedDeltaKmh: Float,
    inclineDeltaPercent: Float,
): RunningSessionTargetOverrideAction? {
    val change = runningTargetOverrideChange(
        blocks = blocks,
        displayBlocks = displayBlocks,
        targetOverrides = targetOverrides,
        currentBlockIndex = currentBlockIndex,
        speedDeltaKmh = speedDeltaKmh,
        inclineDeltaPercent = inclineDeltaPercent
    ) ?: return null
    return RunningSessionTargetOverrideAction(
        targetOverrides = change.targetOverrides,
        diagnosticDetails = runningTargetOverrideDiagnosticDetails(
            speedDeltaKmh = speedDeltaKmh,
            inclineDeltaPercent = inclineDeltaPercent,
            change = change,
            block = blocks[currentBlockIndex]
        )
    )
}

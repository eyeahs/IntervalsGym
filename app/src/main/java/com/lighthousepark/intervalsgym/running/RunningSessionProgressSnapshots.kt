package com.lighthousepark.intervalsgym.running

import com.lighthousepark.intervalsgym.core.remainingCountdownSeconds
import com.lighthousepark.intervalsgym.training.RoutineBlock

internal data class RunningSessionProgressSnapshot(
    val warmupElapsedSeconds: Int,
    val blockRemainingSeconds: Int,
    val blockElapsedSeconds: Int,
    val progressSeconds: Int?,
    val isUrgent: Boolean,
)

internal fun runningSessionProgressSnapshot(
    phase: RunningSessionPhase,
    currentBlock: RoutineBlock?,
    warmupStartedAtMillis: Long,
    blockEndAtMillis: Long,
    nowMillis: Long,
    totalSeconds: Int,
): RunningSessionProgressSnapshot {
    val warmupElapsedSeconds = if (phase == RunningSessionPhase.WARMUP) {
        ((nowMillis - warmupStartedAtMillis) / 1000L).toInt().coerceAtLeast(0)
    } else {
        0
    }
    val blockRemainingSeconds = if (phase == RunningSessionPhase.BLOCK && blockEndAtMillis > 0L) {
        remainingCountdownSeconds(
            endAtMillis = blockEndAtMillis,
            nowMillis = nowMillis
        )
    } else {
        0
    }
    val blockElapsedSeconds = currentBlock?.let { block ->
        if (phase == RunningSessionPhase.BLOCK) {
            (block.durationSeconds - blockRemainingSeconds).coerceIn(0, block.durationSeconds)
        } else {
            0
        }
    } ?: 0
    val progressSeconds = when (phase) {
        RunningSessionPhase.WARMUP -> null
        RunningSessionPhase.BLOCK -> currentBlock?.let { it.startSecond + blockElapsedSeconds }
        RunningSessionPhase.FINISHED -> totalSeconds
    }
    return RunningSessionProgressSnapshot(
        warmupElapsedSeconds = warmupElapsedSeconds,
        blockRemainingSeconds = blockRemainingSeconds,
        blockElapsedSeconds = blockElapsedSeconds,
        progressSeconds = progressSeconds,
        isUrgent = phase == RunningSessionPhase.BLOCK && blockRemainingSeconds in 1..5
    )
}

internal fun currentBlockIndex(blocks: List<RoutineBlock>, elapsedSeconds: Int): Int {
    if (blocks.isEmpty()) return -1
    if (elapsedSeconds >= blocks.last().endSecond) return -1
    return blocks.indexOfFirst { elapsedSeconds in it.startSecond until it.endSecond }
}

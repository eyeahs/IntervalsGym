package com.lighthousepark.intervalsgym.running.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.lighthousepark.intervalsgym.running.HeartRateSample
import com.lighthousepark.intervalsgym.running.RunningSessionPhase
import com.lighthousepark.intervalsgym.running.runningAutoLocalSaveAtMillis
import com.lighthousepark.intervalsgym.running.runningAutoLocalSaveDelayMillis
import com.lighthousepark.intervalsgym.running.shouldAutoLocalSaveLastRunningBlock
import kotlinx.coroutines.delay

@Composable
internal fun RunningTargetOverridesSizeEffect(
    blockCount: Int,
    targetOverrides: List<String>,
    onTargetOverridesChanged: (List<String>) -> Unit,
) {
    LaunchedEffect(blockCount) {
        if (targetOverrides.size != blockCount) {
            onTargetOverridesChanged(
                List(blockCount) { index ->
                    targetOverrides.getOrNull(index).orEmpty()
                }
            )
        }
    }
}

@Composable
internal fun RunningWorkoutHeartRateSamplesEffect(
    heartRateSamples: List<HeartRateSample>,
    warmupStartedAtMillis: Long,
    workoutHeartRateSamples: List<HeartRateSample>,
    onWorkoutHeartRateSamplesChanged: (List<HeartRateSample>) -> Unit,
) {
    val currentWorkoutHeartRateSamples by rememberUpdatedState(workoutHeartRateSamples)
    LaunchedEffect(heartRateSamples, warmupStartedAtMillis) {
        val sessionSamples = heartRateSamples.filter { it.timestampMillis >= warmupStartedAtMillis }
        if (sessionSamples.isNotEmpty()) {
            onWorkoutHeartRateSamplesChanged(
                (currentWorkoutHeartRateSamples + sessionSamples)
                    .distinctBy { it.timestampMillis }
                    .sortedBy { it.timestampMillis }
            )
        }
    }
}

@Composable
internal fun RunningWarmupTickerEffect(
    phase: RunningSessionPhase,
    warmupStartedAtMillis: Long,
    onNowMillisChanged: (Long) -> Unit,
) {
    val currentOnNowMillisChanged by rememberUpdatedState(onNowMillisChanged)
    LaunchedEffect(phase, warmupStartedAtMillis) {
        while (phase == RunningSessionPhase.WARMUP) {
            currentOnNowMillisChanged(System.currentTimeMillis())
            delay(1_000L)
        }
    }
}

@Composable
internal fun RunningBlockProgressEffect(
    phase: RunningSessionPhase,
    blockStartedAtMillis: Long,
    blockEndAtMillis: Long,
    currentBlockIndex: Int,
    currentBlockTargetText: String?,
    onNowMillisChanged: (Long) -> Unit,
    onCatchUpElapsedBlocks: (Long) -> Boolean,
    isWorkoutFinished: () -> Boolean,
    onMoveToNextBlock: () -> Unit,
) {
    val currentOnNowMillisChanged by rememberUpdatedState(onNowMillisChanged)
    val currentCatchUpElapsedBlocks by rememberUpdatedState(onCatchUpElapsedBlocks)
    val currentIsWorkoutFinished by rememberUpdatedState(isWorkoutFinished)
    val currentMoveToNextBlock by rememberUpdatedState(onMoveToNextBlock)

    LaunchedEffect(phase, blockStartedAtMillis, blockEndAtMillis, currentBlockIndex, currentBlockTargetText) {
        while (phase == RunningSessionPhase.BLOCK && blockStartedAtMillis > 0L) {
            val observedAtMillis = System.currentTimeMillis()
            currentOnNowMillisChanged(observedAtMillis)
            if (currentCatchUpElapsedBlocks(observedAtMillis)) {
                if (currentIsWorkoutFinished()) break
                continue
            }
            if (blockEndAtMillis > 0L && observedAtMillis >= blockEndAtMillis) {
                currentMoveToNextBlock()
                break
            }
            delay(250L)
        }
    }
}

@Composable
internal fun RunningUrgentBlinkEffect(
    isUrgent: Boolean,
    onBlinkChanged: (Boolean) -> Unit,
) {
    val currentOnBlinkChanged by rememberUpdatedState(onBlinkChanged)
    LaunchedEffect(isUrgent) {
        if (!isUrgent) {
            currentOnBlinkChanged(false)
            return@LaunchedEffect
        }
        var blinkOn = false
        while (true) {
            blinkOn = !blinkOn
            currentOnBlinkChanged(blinkOn)
            delay(350L)
        }
    }
}

@Composable
internal fun RunningLastBlockAutoSaveEffect(
    phase: RunningSessionPhase,
    currentBlockIndex: Int,
    blockEndAtMillis: Long,
    blockCount: Int,
    onLogRunningSessionEvent: RunningSessionEventLogger,
    onCatchUpElapsedBlocks: () -> Boolean,
) {
    val currentLogger by rememberUpdatedState(onLogRunningSessionEvent)
    val currentCatchUpElapsedBlocks by rememberUpdatedState(onCatchUpElapsedBlocks)
    LaunchedEffect(phase, currentBlockIndex, blockEndAtMillis, blockCount) {
        if (
            phase != RunningSessionPhase.BLOCK ||
            currentBlockIndex != blockCount - 1 ||
            blockEndAtMillis <= 0L
        ) {
            return@LaunchedEffect
        }
        val delayMillis = runningAutoLocalSaveDelayMillis(
            finishedAtMillis = blockEndAtMillis,
            nowMillis = System.currentTimeMillis()
        )
        if (delayMillis > 0L) {
            delay(delayMillis)
        }
        if (
            phase == RunningSessionPhase.BLOCK &&
            shouldAutoLocalSaveLastRunningBlock(
                currentBlockIndex = currentBlockIndex,
                blockCount = blockCount,
                blockEndAtMillis = blockEndAtMillis,
                nowMillis = System.currentTimeMillis()
            )
        ) {
            currentLogger(
                "auto local save last block timeout",
                buildString {
                    appendLine("lastBlockEndAtMillis=$blockEndAtMillis")
                    appendLine("autoSaveAtMillis=${runningAutoLocalSaveAtMillis(blockEndAtMillis)}")
                },
                null
            )
            currentCatchUpElapsedBlocks()
        }
    }
}

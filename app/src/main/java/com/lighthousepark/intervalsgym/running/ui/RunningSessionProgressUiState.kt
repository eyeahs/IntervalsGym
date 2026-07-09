package com.lighthousepark.intervalsgym.running.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import com.lighthousepark.intervalsgym.running.RunningSessionCatchUpResult
import com.lighthousepark.intervalsgym.running.RunningSessionPhase
import com.lighthousepark.intervalsgym.training.RoutineBlock

internal data class RunningSessionProgressUiState(
    val phase: RunningSessionPhase,
    val currentBlockIndex: Int,
    val warmupStartedAtMillis: Long,
    val blockEndAtMillis: Long,
    val blockStartedAtMillis: Long,
) {
    fun withStartedBlock(
        index: Int,
        block: RoutineBlock,
        startedAtMillis: Long,
    ): RunningSessionProgressUiState {
        return copy(
            phase = RunningSessionPhase.BLOCK,
            currentBlockIndex = index,
            blockStartedAtMillis = startedAtMillis,
            blockEndAtMillis = startedAtMillis + block.durationSeconds.coerceAtLeast(0) * 1000L
        )
    }

    fun withCurrentBlockRecorded(): RunningSessionProgressUiState {
        return copy(blockStartedAtMillis = 0L)
    }

    fun withCatchUp(result: RunningSessionCatchUpResult): RunningSessionProgressUiState {
        return copy(
            currentBlockIndex = result.currentBlockIndex,
            blockStartedAtMillis = result.blockStartedAtMillis,
            blockEndAtMillis = result.blockEndAtMillis
        )
    }

    fun withFinished(): RunningSessionProgressUiState {
        return copy(
            phase = RunningSessionPhase.FINISHED,
            blockEndAtMillis = 0L
        )
    }

    companion object {
        fun initial(nowMillis: Long = System.currentTimeMillis()): RunningSessionProgressUiState {
            return RunningSessionProgressUiState(
                phase = RunningSessionPhase.WARMUP,
                currentBlockIndex = 0,
                warmupStartedAtMillis = nowMillis,
                blockEndAtMillis = 0L,
                blockStartedAtMillis = 0L
            )
        }
    }
}

internal fun runningSessionProgressUiStateSaver(): Saver<MutableState<RunningSessionProgressUiState>, List<Any?>> {
    return Saver(
        save = { state ->
            listOf(
                state.value.phase.name,
                state.value.currentBlockIndex,
                state.value.warmupStartedAtMillis,
                state.value.blockEndAtMillis,
                state.value.blockStartedAtMillis
            )
        },
        restore = { saved ->
            mutableStateOf(
                RunningSessionProgressUiState(
                    phase = saved.getOrNull(0)
                        ?.let { value -> runCatching { RunningSessionPhase.valueOf(value as String) }.getOrNull() }
                        ?: RunningSessionPhase.WARMUP,
                    currentBlockIndex = saved.getOrNull(1) as? Int ?: 0,
                    warmupStartedAtMillis = saved.getOrNull(2) as? Long ?: System.currentTimeMillis(),
                    blockEndAtMillis = saved.getOrNull(3) as? Long ?: 0L,
                    blockStartedAtMillis = saved.getOrNull(4) as? Long ?: 0L
                )
            )
        }
    )
}

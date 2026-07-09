package com.lighthousepark.intervalsgym.running.ui

import com.lighthousepark.intervalsgym.data.RunningSessionSyncUseCase
import com.lighthousepark.intervalsgym.running.CompletedRunningSession
import com.lighthousepark.intervalsgym.running.HeartRateSample
import com.lighthousepark.intervalsgym.running.RunningSession
import com.lighthousepark.intervalsgym.running.buildRunningSessionForFinish
import com.lighthousepark.intervalsgym.training.RoutineBlock

internal data class RunningSessionResultSnapshot(
    val routineName: String,
    val startedAtMillis: Long,
    val blocks: List<RoutineBlock>,
    val actualBlocks: List<RoutineBlock>,
    val heartRateSamples: List<HeartRateSample>,
) {
    fun toRunningSession(endedAtMillis: Long): RunningSession {
        return buildRunningSessionForFinish(
            routineName = routineName,
            startedAtMillis = startedAtMillis,
            endedAtMillis = endedAtMillis,
            blocks = blocks,
            actualBlocks = actualBlocks,
            heartRateSamples = heartRateSamples
        )
    }

    fun saveLocalResult(
        syncUseCase: RunningSessionSyncUseCase,
        endedAtMillis: Long,
    ): CompletedRunningSession {
        return syncUseCase.saveRunningSessionLocally(toRunningSession(endedAtMillis))
    }

    suspend fun uploadResult(
        syncUseCase: RunningSessionSyncUseCase,
        endedAtMillis: Long,
    ): CompletedRunningSession {
        return syncUseCase.uploadRunningSession(toRunningSession(endedAtMillis))
    }
}

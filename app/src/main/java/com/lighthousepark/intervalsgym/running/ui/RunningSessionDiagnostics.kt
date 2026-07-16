package com.lighthousepark.intervalsgym.running.ui

import android.content.Context
import com.lighthousepark.intervalsgym.core.DiagnosticsLogger
import com.lighthousepark.intervalsgym.running.CompletedRunningSession
import com.lighthousepark.intervalsgym.running.RunningSession
import com.lighthousepark.intervalsgym.running.RunningSessionCatchUpResult
import com.lighthousepark.intervalsgym.running.RunningSessionPhase
import com.lighthousepark.intervalsgym.running.RunningTargetOverrideChange
import com.lighthousepark.intervalsgym.running.runningBlockDiagnosticText
import com.lighthousepark.intervalsgym.running.runningBlocksDiagnosticText
import com.lighthousepark.intervalsgym.training.RoutineBlock

internal data class RunningSessionDiagnosticSnapshot(
    val routineName: String,
    val phase: RunningSessionPhase,
    val currentBlockIndex: Int,
    val blockCount: Int,
    val totalSeconds: Int,
    val warmupStartedAtMillis: Long,
    val blockStartedAtMillis: Long,
    val blockEndAtMillis: Long,
    val finishedAtMillis: Long,
)

internal class RunningSessionDiagnosticRateLimiter(
    private val minIntervalMillis: Long = 250L,
) {
    private val lastLoggedAtMillisByEvent = mutableMapOf<String, Long>()

    @Synchronized
    fun shouldLog(event: String, nowMillis: Long): Boolean {
        val lastLoggedAtMillis = lastLoggedAtMillisByEvent[event]
        if (
            lastLoggedAtMillis != null &&
            nowMillis >= lastLoggedAtMillis &&
            nowMillis - lastLoggedAtMillis < minIntervalMillis
        ) {
            return false
        }
        lastLoggedAtMillisByEvent[event] = nowMillis
        return true
    }
}

internal fun logRunningSessionDiagnosticEvent(
    context: Context,
    snapshot: RunningSessionDiagnosticSnapshot,
    event: String,
    details: String = "",
    throwable: Throwable? = null,
) {
    DiagnosticsLogger.log(
        context = context,
        tag = "RunningSession",
        throwable = throwable,
        messageProvider = { snapshot.message(event, details) }
    )
}

internal fun runningRecordedBlockDiagnosticDetails(
    endMillis: Long,
    recordedBlock: RoutineBlock,
): String {
    return buildString {
        appendLine("endMillis=$endMillis")
        appendLine("actualSeconds=${recordedBlock.durationSeconds}")
        appendLine(recordedBlock.runningBlockDiagnosticText())
    }
}

internal fun runningFinishedLocalSessionDiagnosticDetails(
    endedAtMillis: Long,
    localSession: CompletedRunningSession,
): String {
    return buildString {
        appendLine("endedAtMillis=$endedAtMillis")
        appendLine("localSessionId=${localSession.id}")
        appendLine("durationSeconds=${localSession.durationSeconds}")
        appendLine("warmupSeconds=${localSession.warmupSeconds}")
        appendLine("estimatedDistanceMeters=${localSession.estimatedDistanceMeters}")
        appendLine(localSession.actualBlocks.runningBlocksDiagnosticText(label = "actualBlocks"))
    }
}

internal fun runningCatchUpDiagnosticDetails(
    observedAtMillis: Long,
    result: RunningSessionCatchUpResult,
): String {
    return buildString {
        appendLine("observedAtMillis=$observedAtMillis")
        appendLine("resultCurrentBlockIndex=${result.currentBlockIndex}")
        appendLine("resultBlockStartedAtMillis=${result.blockStartedAtMillis}")
        appendLine("resultBlockEndAtMillis=${result.blockEndAtMillis}")
        appendLine("resultFinishedAtMillis=${result.finishedAtMillis}")
        appendLine(result.actualBlocks.runningBlocksDiagnosticText(label = "actualBlocksAfterCatchUp"))
    }
}

internal fun runningBlockStartedDiagnosticDetails(
    requestedIndex: Int,
    startedAtMillis: Long,
    scheduledEndAtMillis: Long,
    block: RoutineBlock,
): String {
    return buildString {
        appendLine("requestedIndex=$requestedIndex")
        appendLine("startedAtMillis=$startedAtMillis")
        appendLine("scheduledEndAtMillis=$scheduledEndAtMillis")
        appendLine(block.runningBlockDiagnosticText())
    }
}

internal fun runningTargetOverrideDiagnosticDetails(
    speedDeltaKmh: Float,
    inclineDeltaPercent: Float,
    change: RunningTargetOverrideChange,
    block: RoutineBlock,
): String {
    return buildString {
        appendLine("speedDeltaKmh=$speedDeltaKmh")
        appendLine("inclineDeltaPercent=$inclineDeltaPercent")
        appendLine("nextSpeedKmh=${change.nextSpeedKmh}")
        appendLine("nextInclinePercent=${change.nextInclinePercent}")
        appendLine(block.copy(targetText = change.targetText).runningBlockDiagnosticText())
    }
}

internal fun runningUploadStartedDiagnosticDetails(
    endedAtMillis: Long,
    session: RunningSession,
): String {
    return buildString {
        appendLine("endedAtMillis=$endedAtMillis")
        appendLine("heartRateSamples=${session.heartRateSamples.size}")
        appendLine(session.actualBlocks.runningBlocksDiagnosticText(label = "uploadActualBlocks"))
    }
}

private fun RunningSessionDiagnosticSnapshot.message(
    event: String,
    details: String,
): String {
    return buildString {
        appendLine("event=$event")
        appendLine("routineName=$routineName")
        appendLine("phase=$phase")
        appendLine("currentBlockIndex=$currentBlockIndex")
        appendLine("blockCount=$blockCount")
        appendLine("totalSeconds=$totalSeconds")
        appendLine("warmupStartedAtMillis=$warmupStartedAtMillis")
        appendLine("blockStartedAtMillis=$blockStartedAtMillis")
        appendLine("blockEndAtMillis=$blockEndAtMillis")
        appendLine("finishedAtMillis=$finishedAtMillis")
        if (details.isNotBlank()) appendLine(details)
    }
}

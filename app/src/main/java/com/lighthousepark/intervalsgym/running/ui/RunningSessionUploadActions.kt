package com.lighthousepark.intervalsgym.running.ui

import com.lighthousepark.intervalsgym.data.RunningSessionSyncUseCase
import com.lighthousepark.intervalsgym.running.CompletedRunningSession

internal sealed interface RunningSessionUploadAction

internal fun shouldAutoUploadFinishedRunningSession(
    finishedAtMillis: Long,
    localSessionId: String?,
    isUploading: Boolean,
    uploadError: String?,
): Boolean {
    return finishedAtMillis > 0L &&
        localSessionId != null &&
        !isUploading &&
        uploadError == null
}

internal data object RunningSessionUploadLoginRequired : RunningSessionUploadAction

internal data class RunningSessionUploadReady(
    val endedAtMillis: Long,
    val startedDiagnosticDetails: String,
    private val resultSnapshot: RunningSessionResultSnapshot,
) : RunningSessionUploadAction {
    suspend fun uploadResult(syncUseCase: RunningSessionSyncUseCase): CompletedRunningSession {
        return resultSnapshot.uploadResult(
            syncUseCase = syncUseCase,
            endedAtMillis = endedAtMillis
        )
    }
}

internal fun RunningSessionResultSnapshot.planRunningSessionUpload(
    apiKey: String,
    finishUiState: RunningSessionFinishUiState,
    nowMillis: Long = System.currentTimeMillis(),
): RunningSessionUploadAction {
    if (apiKey.isBlank()) return RunningSessionUploadLoginRequired
    val endedAtMillis = finishUiState.finishedAtMillis.takeIf { it > 0L } ?: nowMillis
    return RunningSessionUploadReady(
        endedAtMillis = endedAtMillis,
        startedDiagnosticDetails = runningUploadStartedDiagnosticDetails(
            endedAtMillis = endedAtMillis,
            session = toRunningSession(endedAtMillis)
        ),
        resultSnapshot = this
    )
}

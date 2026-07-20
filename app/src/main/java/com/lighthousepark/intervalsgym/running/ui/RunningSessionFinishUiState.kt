package com.lighthousepark.intervalsgym.running.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver

internal data class RunningSessionFinishUiState(
    val finishedAtMillis: Long = 0L,
    val isStopSaveDialogVisible: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null,
    val localSessionId: String? = null,
) {
    val isFinished: Boolean
        get() = finishedAtMillis > 0L

    val isExitBackHandlerEnabled: Boolean
        get() = !isStopSaveDialogVisible

    val canExitSession: Boolean
        get() = !isStopSaveDialogVisible && !isUploading

    fun withStopSaveDialogVisible(visible: Boolean): RunningSessionFinishUiState {
        return copy(isStopSaveDialogVisible = visible)
    }

    fun withFinishedLocalSession(
        endedAtMillis: Long,
        localSessionId: String,
    ): RunningSessionFinishUiState {
        return copy(
            finishedAtMillis = endedAtMillis,
            isStopSaveDialogVisible = false,
            isUploading = false,
            error = null,
            localSessionId = localSessionId
        )
    }

    fun withUploadLoginRequired(): RunningSessionFinishUiState {
        return copy(error = "Intervals.icu 업로드는 로그인 후 사용할 수 있습니다.")
    }

    fun withUploadStarted(): RunningSessionFinishUiState {
        return copy(
            isUploading = true,
            error = null
        )
    }

    fun withUploadSucceeded(localSessionId: String): RunningSessionFinishUiState {
        return copy(
            isUploading = false,
            error = null,
            localSessionId = localSessionId
        )
    }

    fun withUploadFailed(errorMessage: String?): RunningSessionFinishUiState {
        return copy(
            isUploading = false,
            error = errorMessage ?: "업로드하지 못했습니다."
        )
    }

    fun discarded(): RunningSessionFinishUiState {
        return copy(isStopSaveDialogVisible = false)
    }
}

internal fun runningSessionFinishUiStateSaver(): Saver<MutableState<RunningSessionFinishUiState>, List<Any?>> {
    return Saver(
        save = { state ->
            listOf(
                state.value.finishedAtMillis,
                state.value.isStopSaveDialogVisible,
                state.value.isUploading,
                state.value.error,
                state.value.localSessionId
            )
        },
        restore = { saved ->
            val isLegacyUploadChoiceState = saved.size >= 6
            val stopDialogIndex = if (isLegacyUploadChoiceState) 2 else 1
            val uploadingIndex = if (isLegacyUploadChoiceState) 3 else 2
            val errorIndex = if (isLegacyUploadChoiceState) 4 else 3
            val localSessionIdIndex = if (isLegacyUploadChoiceState) 5 else 4
            val uploadWasInterrupted = saved.getOrNull(uploadingIndex) as? Boolean ?: false
            mutableStateOf(
                RunningSessionFinishUiState(
                    finishedAtMillis = saved.getOrNull(0) as? Long ?: 0L,
                    isStopSaveDialogVisible = saved.getOrNull(stopDialogIndex) as? Boolean ?: false,
                    isUploading = false,
                    error = if (uploadWasInterrupted) null else saved.getOrNull(errorIndex) as? String,
                    localSessionId = saved.getOrNull(localSessionIdIndex) as? String
                )
            )
        }
    )
}

package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver

internal data class StrengthSessionFinishUiState(
    val isUploading: Boolean = false,
    val uploadMessage: String? = null,
    val uploadError: String? = null,
    val isFinishChoiceDialogVisible: Boolean = false,
    val isCalendarRoutineDeleteConfirmVisible: Boolean = false,
    val isDeletingCalendarRoutine: Boolean = false,
    val finishRpe: Int = 7,
    val applyWorkoutResultToRoutine: Boolean = true,
) {
    fun showFinishChoiceDialog(): StrengthSessionFinishUiState {
        return copy(isFinishChoiceDialogVisible = true)
    }

    fun dismissFinishChoiceDialog(): StrengthSessionFinishUiState {
        return copy(isFinishChoiceDialogVisible = false)
    }

    fun withFinishRpe(rpe: Int): StrengthSessionFinishUiState {
        return copy(finishRpe = rpe)
    }

    fun withApplyWorkoutResultToRoutine(apply: Boolean): StrengthSessionFinishUiState {
        return copy(applyWorkoutResultToRoutine = apply)
    }

    fun withIntervalsLoginRequired(): StrengthSessionFinishUiState {
        return copy(
            uploadMessage = null,
            uploadError = "Intervals.icu 업데이트는 로그인 후 사용할 수 있습니다."
        )
    }

    fun withUploadStarted(): StrengthSessionFinishUiState {
        return copy(
            isUploading = true,
            uploadMessage = null,
            uploadError = null
        )
    }

    fun withUploadSucceeded(): StrengthSessionFinishUiState {
        return copy(
            isUploading = false,
            uploadMessage = "Intervals.icu에 업로드했습니다.",
            uploadError = null
        )
    }

    fun withUploadFailed(errorMessage: String?): StrengthSessionFinishUiState {
        return copy(
            isUploading = false,
            uploadError = errorMessage ?: "업로드하지 못했습니다."
        )
    }

    fun showCalendarRoutineDeleteConfirm(): StrengthSessionFinishUiState {
        return copy(isCalendarRoutineDeleteConfirmVisible = true)
    }

    fun dismissCalendarRoutineDeleteConfirm(): StrengthSessionFinishUiState {
        return copy(isCalendarRoutineDeleteConfirmVisible = false)
    }

    fun withCalendarRoutineDeleteStarted(): StrengthSessionFinishUiState {
        return copy(
            isDeletingCalendarRoutine = true,
            uploadError = null
        )
    }

    fun withCalendarRoutineDeleteFinished(): StrengthSessionFinishUiState {
        return copy(isDeletingCalendarRoutine = false)
    }

    fun withCalendarRoutineDeleteFailed(errorMessage: String?): StrengthSessionFinishUiState {
        return copy(
            isDeletingCalendarRoutine = false,
            uploadError = errorMessage ?: "Routine을 삭제하지 못했습니다."
        )
    }
}

internal fun strengthSessionFinishUiStateSaver(): Saver<MutableState<StrengthSessionFinishUiState>, List<Any?>> {
    return Saver(
        save = { state ->
            listOf(
                state.value.isUploading,
                state.value.uploadMessage,
                state.value.uploadError,
                state.value.isFinishChoiceDialogVisible,
                state.value.isCalendarRoutineDeleteConfirmVisible,
                state.value.isDeletingCalendarRoutine,
                state.value.finishRpe,
                state.value.applyWorkoutResultToRoutine
            )
        },
        restore = { saved ->
            mutableStateOf(
                StrengthSessionFinishUiState(
                    isUploading = saved.getOrNull(0) as? Boolean ?: false,
                    uploadMessage = saved.getOrNull(1) as? String,
                    uploadError = saved.getOrNull(2) as? String,
                    isFinishChoiceDialogVisible = saved.getOrNull(3) as? Boolean ?: false,
                    isCalendarRoutineDeleteConfirmVisible = saved.getOrNull(4) as? Boolean ?: false,
                    isDeletingCalendarRoutine = saved.getOrNull(5) as? Boolean ?: false,
                    finishRpe = saved.getOrNull(6) as? Int ?: 7,
                    applyWorkoutResultToRoutine = saved.getOrNull(7) as? Boolean ?: true
                )
            )
        }
    )
}

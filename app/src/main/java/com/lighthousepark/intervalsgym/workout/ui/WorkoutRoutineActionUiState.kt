package com.lighthousepark.intervalsgym.workout.ui

import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.training.TrainingItem

internal data class WorkoutRoutineActionUiState(
    val isUploadingStrengthSession: Boolean = false,
    val uploadedInThisScreen: Boolean = false,
    val uploadMessage: String? = null,
    val uploadError: String? = null,
    val isDeleteConfirmVisible: Boolean = false,
    val isDeletingRoutine: Boolean = false,
    val deleteError: String? = null,
) {
    val displayError: String?
        get() = uploadError ?: deleteError

    fun withUploadLoginRequired(): WorkoutRoutineActionUiState {
        return copy(
            uploadMessage = null,
            uploadError = "Intervals.icu 업데이트는 로그인 후 사용할 수 있습니다."
        )
    }

    fun withUploadStarted(): WorkoutRoutineActionUiState {
        return copy(
            isUploadingStrengthSession = true,
            uploadMessage = null,
            uploadError = null
        )
    }

    fun withUploadSucceeded(): WorkoutRoutineActionUiState {
        return copy(
            isUploadingStrengthSession = false,
            uploadedInThisScreen = true,
            uploadMessage = "Intervals.icu에 업로드했습니다.",
            uploadError = null
        )
    }

    fun withUploadFailed(errorMessage: String?): WorkoutRoutineActionUiState {
        return copy(
            isUploadingStrengthSession = false,
            uploadError = errorMessage ?: "업로드하지 못했습니다."
        )
    }

    fun showDeleteConfirm(): WorkoutRoutineActionUiState {
        return copy(isDeleteConfirmVisible = true)
    }

    fun dismissDeleteConfirm(): WorkoutRoutineActionUiState {
        return copy(isDeleteConfirmVisible = false)
    }

    fun withDeleteStarted(): WorkoutRoutineActionUiState {
        return copy(
            isDeleteConfirmVisible = false,
            isDeletingRoutine = true,
            deleteError = null
        )
    }

    fun withDeleteFinished(): WorkoutRoutineActionUiState {
        return copy(isDeletingRoutine = false)
    }

    fun withDeleteFailed(errorMessage: String?): WorkoutRoutineActionUiState {
        return copy(
            isDeletingRoutine = false,
            deleteError = errorMessage ?: "Routine을 삭제하지 못했습니다."
        )
    }
}

internal fun canUploadLocalStrengthWorkout(
    localSession: CompletedStrengthSession?,
    apiKey: String,
    uploadedInThisScreen: Boolean,
    routine: TrainingItem?,
): Boolean {
    return localSession != null &&
        apiKey.isNotBlank() &&
        !uploadedInThisScreen &&
        (!localSession.uploadedToIntervals || routine?.isLocalOnlyStrengthResult == true)
}

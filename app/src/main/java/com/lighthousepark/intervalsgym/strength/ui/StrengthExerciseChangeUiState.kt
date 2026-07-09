package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.StrengthExercise

internal data class StrengthExerciseChangeUiState(
    val isChangingCurrentExercise: Boolean,
    val isCurrentExerciseTypeDialogVisible: Boolean,
    val shouldReturnToOngoingAfterExerciseChange: Boolean,
    val pendingAddedExerciseEntryId: Int?,
    val exerciseToConfigure: StrengthExercise?,
    val exerciseToConfigureSearchQuery: String,
    val isCustomExerciseDialogVisible: Boolean,
) {
    fun finish(): StrengthExerciseChangeUiState {
        return inactive()
    }

    fun clearPendingSelectionForOpenedSet(): StrengthExerciseChangeUiState {
        return copy(
            shouldReturnToOngoingAfterExerciseChange = false,
            pendingAddedExerciseEntryId = null
        )
    }

    fun beginAddedExercise(entryId: Int): StrengthExerciseChangeUiState {
        return copy(
            isChangingCurrentExercise = true,
            isCurrentExerciseTypeDialogVisible = false,
            shouldReturnToOngoingAfterExerciseChange = true,
            pendingAddedExerciseEntryId = entryId,
            exerciseToConfigure = null,
            exerciseToConfigureSearchQuery = "",
            isCustomExerciseDialogVisible = false
        )
    }

    fun beginExistingExerciseChange(): StrengthExerciseChangeUiState {
        return copy(
            isChangingCurrentExercise = true,
            isCurrentExerciseTypeDialogVisible = false,
            shouldReturnToOngoingAfterExerciseChange = false,
            pendingAddedExerciseEntryId = null,
            exerciseToConfigure = null,
            isCustomExerciseDialogVisible = false
        )
    }

    fun showCurrentExerciseTypeDialog(canShow: Boolean): StrengthExerciseChangeUiState {
        return copy(
            isCurrentExerciseTypeDialogVisible = canShow,
            shouldReturnToOngoingAfterExerciseChange = false,
            pendingAddedExerciseEntryId = null
        )
    }

    fun hideCurrentExerciseTypeDialog(): StrengthExerciseChangeUiState {
        return copy(isCurrentExerciseTypeDialogVisible = false)
    }

    fun selectExerciseToConfigure(
        exercise: StrengthExercise,
        searchQuery: String,
    ): StrengthExerciseChangeUiState {
        return copy(
            exerciseToConfigure = exercise,
            exerciseToConfigureSearchQuery = searchQuery
        )
    }

    fun dismissExerciseConfig(): StrengthExerciseChangeUiState {
        return copy(exerciseToConfigure = null)
    }

    fun showCustomExerciseDialog(): StrengthExerciseChangeUiState {
        return copy(isCustomExerciseDialogVisible = true)
    }

    fun dismissCustomExerciseDialog(): StrengthExerciseChangeUiState {
        return copy(isCustomExerciseDialogVisible = false)
    }

    fun addCustomExercise(exercise: StrengthExercise): StrengthExerciseChangeUiState {
        return copy(
            isCustomExerciseDialogVisible = false,
            exerciseToConfigureSearchQuery = "",
            exerciseToConfigure = exercise
        )
    }

    companion object {
        fun inactive(): StrengthExerciseChangeUiState {
            return StrengthExerciseChangeUiState(
                isChangingCurrentExercise = false,
                isCurrentExerciseTypeDialogVisible = false,
                shouldReturnToOngoingAfterExerciseChange = false,
                pendingAddedExerciseEntryId = null,
                exerciseToConfigure = null,
                exerciseToConfigureSearchQuery = "",
                isCustomExerciseDialogVisible = false
            )
        }
    }
}

package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.runtime.Composable
import com.lighthousepark.intervalsgym.strength.StrengthExercise
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.plannedWorkoutDeleteConfirmMessage

@Composable
internal fun StrengthSessionDialogs(
    restUiState: StrengthRestUiState,
    entries: List<StrengthRoutineEntry>,
    currentExerciseIndex: Int,
    isCurrentExerciseTypeDialogVisible: Boolean,
    sessionExerciseToConfigure: StrengthExercise?,
    sessionExerciseToConfigureSearchQuery: String,
    isSessionCustomExerciseDialogVisible: Boolean,
    finishUiState: StrengthSessionFinishUiState,
    apiKey: String,
    calendarRoutineItem: TrainingItem?,
    onAdjustRestSeconds: (Int) -> Unit,
    onSetRestSeconds: (Int) -> Unit,
    onDismissRestSheet: () -> Unit,
    onStopRest: () -> Unit,
    onBeginExistingExerciseChange: () -> Unit,
    onDismissCurrentExerciseTypeDialog: () -> Unit,
    onCurrentExerciseTypeDone: (StrengthRoutineEntry, String, String) -> Unit,
    onDismissExerciseConfig: () -> Unit,
    onExerciseConfigDone: (StrengthExercise, String, String) -> Unit,
    onDismissCustomExerciseDialog: () -> Unit,
    onAddCustomExercise: (String) -> Unit,
    onApplyWorkoutResultToRoutineChange: (Boolean) -> Unit,
    onFinishRpeChange: (Int) -> Unit,
    onDismissFinishChoiceDialog: () -> Unit,
    onSaveFinishedWorkout: () -> Unit,
    onDiscardFinishedWorkout: () -> Unit,
    onConfirmCalendarRoutineDelete: () -> Unit,
    onCancelCalendarRoutineDelete: () -> Unit,
) {
    if (restUiState.isSheetVisible) {
        restUiState.remainingSeconds?.let { remaining ->
            RestTimerBottomSheet(
                title = restUiState.title,
                remainingSeconds = remaining,
                onAdjustSeconds = onAdjustRestSeconds,
                onSetSeconds = onSetRestSeconds,
                onDismiss = onDismissRestSheet,
                onStop = onStopRest
            )
        }
    }

    val currentEntryForTypeDialog = entries.getOrNull(currentExerciseIndex)
    if (isCurrentExerciseTypeDialogVisible && currentEntryForTypeDialog != null) {
        StrengthExerciseTypeDialog(
            entry = currentEntryForTypeDialog,
            exercise = currentEntryForTypeDialog.exercise,
            initialEquipment = currentEntryForTypeDialog.equipment,
            initialVariation = currentEntryForTypeDialog.variation,
            confirmText = "저장",
            onExerciseChangeClick = onBeginExistingExerciseChange,
            onDismiss = onDismissCurrentExerciseTypeDialog,
            onDone = { equipment, variation ->
                onCurrentExerciseTypeDone(currentEntryForTypeDialog, equipment, variation)
            }
        )
    }

    sessionExerciseToConfigure?.let { exercise ->
        StrengthExerciseConfigDialog(
            exercise = exercise,
            initialSearchQuery = sessionExerciseToConfigureSearchQuery,
            onDismiss = onDismissExerciseConfig,
            onDone = { equipment, variation ->
                onExerciseConfigDone(exercise, equipment, variation)
            }
        )
    }

    if (isSessionCustomExerciseDialogVisible) {
        CustomStrengthExerciseDialog(
            onDismiss = onDismissCustomExerciseDialog,
            onAdd = onAddCustomExercise
        )
    }

    if (finishUiState.isFinishChoiceDialogVisible) {
        StrengthFinishChoiceDialog(
            apiKey = apiKey,
            entries = entries,
            finishRpe = finishUiState.finishRpe,
            applyWorkoutResultToRoutine = finishUiState.applyWorkoutResultToRoutine,
            isUploading = finishUiState.isUploading,
            onApplyWorkoutResultToRoutineChange = onApplyWorkoutResultToRoutineChange,
            onFinishRpeChange = onFinishRpeChange,
            onDismiss = onDismissFinishChoiceDialog,
            onSave = onSaveFinishedWorkout,
            onDiscard = onDiscardFinishedWorkout
        )
    }

    if (finishUiState.isCalendarRoutineDeleteConfirmVisible && calendarRoutineItem != null) {
        StrengthCalendarRoutineDeleteConfirmDialog(
            message = calendarRoutineItem.plannedWorkoutDeleteConfirmMessage(),
            isDeleting = finishUiState.isDeletingCalendarRoutine,
            onConfirm = onConfirmCalendarRoutineDelete,
            onCancel = onCancelCalendarRoutineDelete
        )
    }
}

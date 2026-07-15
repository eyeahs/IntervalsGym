package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.core.throttleRapidTaps
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StrengthRoutineEditTopBar(
    isChangingExercise: Boolean,
    isExerciseDetailVisible: Boolean,
    isAddingExercise: Boolean,
    isExerciseListVisible: Boolean,
    isNewRoutine: Boolean,
    onBack: () -> Unit,
    onHistory: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                when {
                    isChangingExercise -> "운동 목록"
                    isExerciseDetailVisible && isAddingExercise -> "운동 추가"
                    isExerciseDetailVisible -> "운동 상세"
                    isExerciseListVisible -> "운동 목록"
                    isNewRoutine -> "Routine 추가"
                    else -> "Routine 수정"
                }
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthRoutineEditBack)
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
            }
        },
        actions = {
            if (
                !isChangingExercise &&
                !isExerciseDetailVisible &&
                !isExerciseListVisible &&
                !isNewRoutine
            ) {
                IconButton(
                    onClick = onHistory,
                    modifier = Modifier
                        .throttleRapidTaps()
                        .debugContentDescription(TestContentDescriptions.StrengthRoutineEditHistory)
                ) {
                    Icon(Icons.Outlined.History, contentDescription = "Routine History")
                }
            }
        }
    )
}

@Composable
internal fun StrengthRoutineDeleteDialog(
    routine: StrengthWorkoutRoutine,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Routine 삭제") },
        text = {
            Text(
                text = "'${routine.name}' Routine을 삭제할까요? 삭제한 Routine은 복구할 수 없습니다."
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDelete,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthRoutineEditConfirmDelete)
            ) {
                Text("삭제", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthRoutineEditCancelDelete)
            ) {
                Text("취소")
            }
        }
    )
}

@Composable
internal fun StrengthRoutineUnsavedBackDialog(
    canSaveRoutine: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("변경사항 저장") },
        text = {
            Text(
                text = "Routine 수정 내용을 저장할까요?"
            )
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = canSaveRoutine,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthRoutineEditSaveUnsaved)
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = onDiscard,
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthRoutineEditDiscardUnsaved)
                ) {
                    Text("저장 안 함")
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthRoutineEditCancelUnsaved)
                ) {
                    Text("취소")
                }
            }
        }
    )
}

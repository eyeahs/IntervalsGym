package com.lighthousepark.intervalsgym.running.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.running.SavedRunningWorkoutRoutine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RunningRoutineListTopBar(
    onBack: () -> Unit,
    onManageRoutines: () -> Unit,
) {
    TopAppBar(
        title = { Text("러닝 routine 선택") },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningRoutineListBack)
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
            }
        },
        actions = {
            IconButton(
                onClick = onManageRoutines,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningRoutineListManage)
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = "러닝 Routine 관리")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RunningRoutineManagementTopBar(
    title: String,
    canDelete: Boolean,
    onBack: () -> Unit,
    onDelete: () -> Unit,
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningRoutineManagementBack)
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
            }
        },
        actions = {
            if (canDelete) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningRoutineDelete)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "러닝 Routine 삭제",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}

@Composable
internal fun RunningRoutineDeleteDialog(
    routine: SavedRunningWorkoutRoutine,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("러닝 Routine 삭제") },
        text = { Text("'${routine.name}' routine을 삭제할까요?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningRoutineConfirmDelete)
            ) {
                Text("삭제")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningRoutineCancelDelete)
            ) {
                Text("취소")
            }
        }
    )
}

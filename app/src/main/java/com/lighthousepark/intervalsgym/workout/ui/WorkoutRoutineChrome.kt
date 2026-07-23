package com.lighthousepark.intervalsgym.workout.ui

import com.lighthousepark.intervalsgym.core.localizedContentDescription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import com.lighthousepark.intervalsgym.core.LocalizedText as Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.plannedWorkoutDeleteConfirmMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WorkoutRoutineTopBar(
    title: String,
    canSaveRunningWorkoutRoutine: Boolean,
    canSaveStrengthWorkoutRoutine: Boolean,
    canDeleteRoutine: Boolean,
    isDeletingRoutine: Boolean,
    canUploadLocalWorkout: Boolean,
    isUploadingStrengthSession: Boolean,
    onBack: () -> Unit,
    onSaveRunningWorkoutRoutine: () -> Unit,
    onSaveStrengthWorkoutRoutine: () -> Unit,
    onDeleteClick: () -> Unit,
    onUploadLocalWorkout: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.WorkoutRoutineBack)
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = localizedContentDescription("뒤로"))
            }
        },
        actions = {
            if (canSaveRunningWorkoutRoutine) {
                IconButton(
                    onClick = onSaveRunningWorkoutRoutine,
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.WorkoutRoutineSaveRunning)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Save,
                        contentDescription = localizedContentDescription("러닝 Routine 저장")
                    )
                }
            }
            if (canSaveStrengthWorkoutRoutine) {
                IconButton(
                    onClick = onSaveStrengthWorkoutRoutine,
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.WorkoutRoutineSaveStrength)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Save,
                        contentDescription = localizedContentDescription("웨이트 Routine 로컬에 저장")
                    )
                }
            }
            if (canDeleteRoutine) {
                IconButton(
                    onClick = onDeleteClick,
                    enabled = !isDeletingRoutine,
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.WorkoutRoutineDelete)
                ) {
                    if (isDeletingRoutine) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = localizedContentDescription("Routine 삭제"),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            if (canUploadLocalWorkout) {
                IconButton(
                    onClick = onUploadLocalWorkout,
                    enabled = !isUploadingStrengthSession,
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.WorkoutRoutineUploadLocalWorkout)
                ) {
                    if (isUploadingStrengthSession) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Outlined.CloudUpload, contentDescription = localizedContentDescription("Intervals.icu 업로드"))
                    }
                }
            }
        }
    )
}

@Composable
internal fun WorkoutRoutineDeleteConfirmDialog(
    routine: TrainingItem,
    isDeletingRoutine: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isDeletingRoutine) onDismiss() },
        title = { Text("Routine 삭제") },
        text = {
            Text(text = routine.plannedWorkoutDeleteConfirmMessage())
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDeletingRoutine,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.WorkoutRoutineConfirmDelete)
            ) {
                Text("삭제", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeletingRoutine,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.WorkoutRoutineCancelDelete)
            ) {
                Text("취소")
            }
        }
    )
}

@Composable
internal fun WorkoutRoutineStartActionBar(
    isStrengthRoutine: Boolean,
    isRunningWorkoutRoutine: Boolean,
    heartRateDeviceLabel: String,
    heartRateStatusLabel: String,
    onHeartRateClick: () -> Unit,
    onStartWorkout: () -> Unit,
) {
    if (!isStrengthRoutine && !isRunningWorkoutRoutine) return

    Surface(
        modifier = Modifier.navigationBarsPadding(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isRunningWorkoutRoutine) {
                OutlinedButton(
                    onClick = onHeartRateClick,
                    modifier = Modifier
                        .weight(0.42f)
                        .height(56.dp)
                        .debugContentDescription(TestContentDescriptions.WorkoutRoutineHeartRate),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = heartRateDeviceLabel,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = heartRateStatusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    }
                }
            }
            Button(
                onClick = onStartWorkout,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .debugContentDescription(TestContentDescriptions.WorkoutRoutineStartWorkout),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = if (isStrengthRoutine) {
                        Icons.Outlined.FitnessCenter
                    } else {
                        Icons.AutoMirrored.Outlined.DirectionsRun
                    },
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("운동 시작")
            }
        }
    }
}

internal fun workoutRoutineHeartRateDeviceLabel(
    isConnected: Boolean,
    isConnecting: Boolean,
    connectedDeviceName: String?,
): String {
    return when {
        isConnected -> connectedDeviceName.orEmpty().ifBlank { "심박계" }
        isConnecting -> "연결 중"
        else -> "심박계"
    }
}

internal fun workoutRoutineHeartRateStatusLabel(
    isConnected: Boolean,
    heartRateBpm: Int?,
): String {
    return if (isConnected) {
        heartRateBpm?.let { "$it bpm" } ?: "-- bpm"
    } else {
        "연결"
    }
}

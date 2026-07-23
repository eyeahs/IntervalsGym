package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import com.lighthousepark.intervalsgym.core.LocalizedText as Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.core.formatWeight
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthRoutineUpdateSelection
import com.lighthousepark.intervalsgym.strength.completedStrengthTrainingLoad
import com.lighthousepark.intervalsgym.strength.strengthTrainingLoad
import com.lighthousepark.intervalsgym.strength.totalVolumeKg
import kotlin.math.roundToInt

@Composable
internal fun StrengthFinishChoiceDialog(
    apiKey: String,
    entries: List<StrengthRoutineEntry>,
    finishRpe: Int,
    routineUpdateAvailability: StrengthRoutineUpdateSelection,
    routineUpdateSelection: StrengthRoutineUpdateSelection,
    isUploading: Boolean,
    onRoutineUpdateSelectionChange: (StrengthRoutineUpdateSelection) -> Unit,
    onFinishRpeChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("운동 완료") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (apiKey.isBlank()) {
                        "운동 기록을 로컬에 저장하거나 삭제할 수 있습니다."
                    } else {
                        "운동 기록을 저장하면 로컬 기록에 남기고 Intervals.icu 업로드를 시도합니다."
                    }
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Routine 업데이트",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    RoutineUpdateCheckboxRow(
                        label = "순서",
                        checked = routineUpdateSelection.order,
                        enabled = routineUpdateAvailability.order,
                        contentDescription = TestContentDescriptions.StrengthFinishUpdateOrder,
                        onCheckedChange = {
                            onRoutineUpdateSelectionChange(routineUpdateSelection.copy(order = it))
                        }
                    )
                    RoutineUpdateCheckboxRow(
                        label = "세트 묶기",
                        checked = routineUpdateSelection.supersets,
                        enabled = routineUpdateAvailability.supersets,
                        contentDescription = TestContentDescriptions.StrengthFinishUpdateSupersets,
                        onCheckedChange = {
                            onRoutineUpdateSelectionChange(routineUpdateSelection.copy(supersets = it))
                        }
                    )
                    RoutineUpdateCheckboxRow(
                        label = "운동 종류 변경",
                        checked = routineUpdateSelection.exerciseTypes,
                        enabled = routineUpdateAvailability.exerciseTypes,
                        contentDescription = TestContentDescriptions.StrengthFinishUpdateExerciseTypes,
                        onCheckedChange = {
                            onRoutineUpdateSelectionChange(routineUpdateSelection.copy(exerciseTypes = it))
                        }
                    )
                    RoutineUpdateCheckboxRow(
                        label = "운동 상세 변경",
                        checked = routineUpdateSelection.exerciseDetails,
                        enabled = routineUpdateAvailability.exerciseDetails,
                        contentDescription = TestContentDescriptions.StrengthFinishUpdateExerciseDetails,
                        onCheckedChange = {
                            onRoutineUpdateSelectionChange(routineUpdateSelection.copy(exerciseDetails = it))
                        }
                    )
                    if (!routineUpdateAvailability.hasSelection) {
                        Text(
                            text = "운동 중 변경된 routine 항목이 없습니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "RPE",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = finishRpe.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = finishRpe.toFloat(),
                        onValueChange = { onFinishRpeChange(it.roundToInt().coerceIn(1, 10)) },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                    Text(
                        text = "Strength Load ${entries.completedStrengthTrainingLoad(finishRpe)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = !isUploading,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthFinishSave)
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDiscard,
                enabled = !isUploading,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthFinishDiscard)
            ) {
                Text("삭제")
            }
        }
    )
}

@Composable
private fun RoutineUpdateCheckboxRow(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    contentDescription: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            modifier = Modifier.debugContentDescription(contentDescription),
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
internal fun StrengthCalendarRoutineDeleteConfirmDialog(
    message: String,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onCancel() },
        title = { Text("Routine 삭제") },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDeleting,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthSessionCalendarRoutineConfirmDelete)
            ) {
                Text("삭제", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                enabled = !isDeleting,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthSessionCalendarRoutineCancelDelete)
            ) {
                Text("취소")
            }
        }
    )
}

@Composable
internal fun StrengthUploadPanel(
    apiKey: String,
    routineName: String,
    entries: List<StrengthRoutineEntry>,
    isUploading: Boolean,
    uploadMessage: String?,
    uploadError: String?,
    onUpload: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val completedSets = entries.sumOf { entry -> entry.records.count { it.completed } }
            val totalSets = entries.sumOf { it.records.size }
            val volume = entries.totalVolumeKg()
            val estimatedLoad = entries.strengthTrainingLoad(7)
            Text(
                text = "운동 완료 준비",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$routineName · $completedSets/$totalSets 세트 완료 · 볼륨 ${formatWeight(volume)} kg · 예상 Load $estimatedLoad",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (apiKey.isBlank()) {
                Text(
                    text = "Intervals.icu 업데이트는 로그인 후 사용할 수 있습니다.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            uploadMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
            uploadError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = onUpload,
                enabled = entries.isNotEmpty() && !isUploading,
                modifier = Modifier
                    .fillMaxWidth()
                    .debugContentDescription(TestContentDescriptions.StrengthUploadWorkout),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Outlined.CloudUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isUploading) "업로드 중" else "운동 완료")
            }
        }
    }
}

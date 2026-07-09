package com.lighthousepark.intervalsgym.running.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription

/**
 * UI tests: RunningSessionUiTest.runningFinishUploadChoiceDialog_invokesUploadAndGarminCallbacks,
 * runningFinishUploadChoiceDialog_disablesUnavailableActions.
 */
@Composable
internal fun RunningFinishUploadChoiceDialog(
    apiKey: String,
    isUploading: Boolean,
    finishError: String?,
    onUpload: () -> Unit,
    onUseGarmin: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("러닝 기록 업로드") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Garmin 원본 기록이 더 중요하면 업로드하지 않고 Garmin 동기화를 기다리는 편이 안전합니다. 지금 업로드하면 Intervals.icu에 수동 러닝 기록이 추가될 수 있습니다."
                )
                Text(
                    text = "앱 로컬에는 수행 결과를 저장했습니다.",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                finishError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onUpload,
                enabled = apiKey.isNotBlank() && !isUploading,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningFinishUpload)
            ) {
                Text(if (isUploading) "업로드 중" else "수동 업로드")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onUseGarmin,
                enabled = !isUploading,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningFinishUseGarmin)
            ) {
                Text("Garmin 결과 사용")
            }
        }
    )
}

/**
 * UI tests: RunningSessionUiTest.runningStopSaveDialog_invokesSaveAndDiscardCallbacks.
 */
@Composable
internal fun RunningStopSaveDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("운동 중지") },
        text = {
            Text("현재까지 수행한 러닝 기록을 로컬에 저장할까요?")
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningStopSave)
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDiscard,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningStopDiscard)
            ) {
                Text("삭제")
            }
        }
    )
}

package com.lighthousepark.intervalsgym.running.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription

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

package com.lighthousepark.intervalsgym.running.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import com.lighthousepark.intervalsgym.core.LocalizedText as Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.core.formatClock
import com.lighthousepark.intervalsgym.core.formatDuration

@Composable
internal fun RunningWarmupPanel(
    elapsedSeconds: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Warmup 중",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            RunningTimerText(
                text = formatClock(elapsedSeconds),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f),
                fontHeightRatio = 0.40f,
                maxFontSize = 102f
            )
            Text(
                text = "준비가 끝나면 첫 번째 Block을 시작하세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * UI tests: RunningSessionUiTest.runningFinishedPanel_closeButtonInvokesCallback.
 */
@Composable
internal fun RunningFinishedPanel(
    totalSeconds: Int,
    isUploading: Boolean = false,
    uploadError: String? = null,
    canRetryUpload: Boolean = false,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onRetryUpload: () -> Unit = {},
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Running Workout 완료",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "총 ${formatDuration(totalSeconds)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            when {
                isUploading -> Text(
                    text = "Intervals.icu에 기록 업로드 중...",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                uploadError != null -> {
                    Text(
                        text = uploadError,
                        color = MaterialTheme.colorScheme.error
                    )
                    if (canRetryUpload) {
                        OutlinedButton(
                            onClick = onRetryUpload,
                            modifier = Modifier
                                .fillMaxWidth()
                                .debugContentDescription(TestContentDescriptions.RunningFinishUpload)
                        ) {
                            Text("업로드 다시 시도")
                        }
                    }
                }
            }
            Button(
                onClick = onClose,
                enabled = !isUploading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .debugContentDescription(TestContentDescriptions.RunningFinishClose),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("닫기")
            }
        }
    }
}

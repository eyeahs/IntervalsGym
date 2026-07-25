package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import com.lighthousepark.intervalsgym.core.LocalizedText as Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.core.formatClock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RestTimerBottomSheet(
    remainingSeconds: Int,
    pendingSetDurationSeconds: Int? = null,
    onAdjustSeconds: (Int) -> Unit,
    onSetSeconds: (Int) -> Unit,
    onDismiss: () -> Unit,
    onStop: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = formatClock(remainingSeconds),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            pendingSetDurationSeconds?.let { durationSeconds ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "다음 세트 시간",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatClock(durationSeconds),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            RestTimeControls(
                onAdjustSeconds = onAdjustSeconds,
                onSetSeconds = onSetSeconds
            )
            OutlinedButton(
                onClick = onStop,
                modifier = Modifier
                    .fillMaxWidth()
                    .debugContentDescription(TestContentDescriptions.StrengthRestStop),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("휴식 중단")
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
internal fun RestTimeControls(
    onAdjustSeconds: (Int) -> Unit,
    onSetSeconds: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RestTimeBubble(text = "-10초", onClick = { onAdjustSeconds(-10) })
        RestTimeBubble(text = "+10초", onClick = { onAdjustSeconds(10) })
        RestTimeBubble(text = "30초", onClick = { onSetSeconds(30) })
        RestTimeBubble(text = "60초", onClick = { onSetSeconds(60) })
        RestTimeBubble(text = "90초", onClick = { onSetSeconds(90) })
        RestTimeBubble(text = "2분", onClick = { onSetSeconds(120) })
        RestTimeBubble(text = "3분", onClick = { onSetSeconds(180) })
        RestTimeBubble(text = "5분", onClick = { onSetSeconds(300) })
    }
}

@Composable
internal fun RestTimeBubble(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier
            .debugContentDescription(TestContentDescriptions.strengthRestTimeControl(text))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
        )
    }
}

@Composable
internal fun CollapsedRestTimerBar(
    remainingSeconds: Int,
    onStop: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .debugContentDescription(TestContentDescriptions.StrengthCollapsedRestStop),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = 8.dp
    ) {
        Button(
            onClick = onStop,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                text = "휴식 종료 · ${formatClock(remainingSeconds)}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

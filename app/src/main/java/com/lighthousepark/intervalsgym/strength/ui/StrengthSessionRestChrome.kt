package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import com.lighthousepark.intervalsgym.core.LocalizedText as Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.core.formatClock
import com.lighthousepark.intervalsgym.core.throttleRapidTaps
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RestTimerBottomSheet(
    remainingSeconds: Int,
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
internal fun RestTimerFloatingChip(
    remainingSeconds: Int,
    onClick: () -> Unit,
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    Surface(
        modifier = Modifier
            .navigationBarsPadding()
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .debugContentDescription(TestContentDescriptions.StrengthRestFloatingChip)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .throttleRapidTaps()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.86f),
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Outlined.Schedule, contentDescription = null)
            Text(
                text = formatClock(remainingSeconds),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

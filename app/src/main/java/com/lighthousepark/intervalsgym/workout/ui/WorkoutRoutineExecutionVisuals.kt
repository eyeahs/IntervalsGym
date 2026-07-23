package com.lighthousepark.intervalsgym.workout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import com.lighthousepark.intervalsgym.core.LocalizedText as Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.core.formatClock
import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.ui.theme.AppGraphOrange2
import com.lighthousepark.intervalsgym.ui.theme.AppGraphOrange4

/**
 * UI tests: WorkoutRoutineVisualsUiTest.runningTimerPanel_invokesToggleAndResetCallbacks,
 * runningTimerPanel_disablesToggleWhenNoDuration.
 */
@Composable
internal fun RunningTimerPanel(
    elapsedSeconds: Int,
    totalSeconds: Int,
    currentBlock: RoutineBlock?,
    blockRemaining: Int,
    remainingTotal: Int,
    isRunning: Boolean,
    onToggle: () -> Unit,
    onReset: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "수행 시간",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${formatClock(elapsedSeconds)} / ${formatClock(totalSeconds)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TimerStat(
                    title = "현재 Block",
                    value = currentBlock?.title ?: "대기",
                    detail = currentBlock?.targetText.orEmpty(),
                    modifier = Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.error
                )
                TimerStat(
                    title = "Block 남은 시간",
                    value = formatClock(blockRemaining),
                    detail = "전체 ${formatClock(remainingTotal)} 남음",
                    modifier = Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onToggle,
                    enabled = totalSeconds > 0,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .debugContentDescription(TestContentDescriptions.RunningTimerToggle)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isRunning) "일시정지" else "시작")
                }
                OutlinedButton(
                    onClick = onReset,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .debugContentDescription(TestContentDescriptions.RunningTimerReset)
                ) {
                    Icon(Icons.Outlined.RestartAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("리셋")
                }
            }
        }
    }
}

@Composable
internal fun TimerStat(
    title: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier,
    accent: Color,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun RoutineTimeline(
    blocks: List<RoutineBlock>,
    currentIndex: Int,
    elapsedSeconds: Int,
    totalSeconds: Int,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            blocks.forEach { block ->
                val color = when {
                    block.index == currentIndex -> MaterialTheme.colorScheme.error
                    elapsedSeconds >= block.endSecond -> MaterialTheme.colorScheme.primary
                    block.isRecovery -> AppGraphOrange2
                    else -> AppGraphOrange4
                }
                Box(
                    modifier = Modifier
                        .weight(block.durationSeconds.coerceAtLeast(1).toFloat())
                        .fillMaxHeight()
                        .background(color)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "진행률 ${if (totalSeconds > 0) elapsedSeconds * 100 / totalSeconds else 0}%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun RoutineBlockRow(
    block: RoutineBlock,
    isCurrent: Boolean,
    isDone: Boolean,
) {
    val containerColor = when {
        isCurrent -> MaterialTheme.colorScheme.error
        isDone -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = if (isCurrent) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = block.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = block.targetText.ifBlank { block.kind },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrent) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = formatClock(block.durationSeconds),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

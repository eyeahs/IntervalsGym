package com.lighthousepark.intervalsgym.running.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.running.RunningSessionPhase

/**
 * UI tests: RunningSessionUiTest.runningSessionActionBar_warmupPrimaryInvokesCallback,
 * runningSessionActionBar_blockActionsRespectPreviousAvailability,
 * runningSessionActionBar_lastBlockInvokesPreviousAndFinishCallbacks.
 */
@Composable
internal fun RunningSessionActionBar(
    phase: RunningSessionPhase,
    currentBlockIndex: Int,
    isLastBlock: Boolean,
    onPreviousBlock: () -> Unit,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (phase == RunningSessionPhase.FINISHED) return
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (phase == RunningSessionPhase.BLOCK) {
            OutlinedButton(
                onClick = onPreviousBlock,
                enabled = currentBlockIndex > 0,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .debugContentDescription(TestContentDescriptions.RunningPreviousBlock),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = "이전\nBlock",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2
                )
            }
        }
        Button(
            onClick = onPrimaryAction,
            modifier = Modifier
                .weight(if (phase == RunningSessionPhase.BLOCK) 2f else 1f)
                .fillMaxHeight()
                .debugContentDescription(TestContentDescriptions.RunningPrimaryAction),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (phase == RunningSessionPhase.BLOCK) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        ) {
            Text(
                when (phase) {
                    RunningSessionPhase.WARMUP -> "Warmup 종료"
                    RunningSessionPhase.BLOCK -> if (isLastBlock) "운동 마치기" else "Block 건너뛰기"
                    RunningSessionPhase.FINISHED -> ""
                }
            )
        }
    }
}

/**
 * UI tests: RunningSessionUiTest.runningSessionTopBar_invokesBackAndStopCallbacks,
 * runningSessionTopBar_hidesStopActionWhenFinished.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RunningSessionTopBar(
    routineName: String,
    phase: RunningSessionPhase,
    isStopEnabled: Boolean,
    onBack: () -> Unit,
    onStop: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = routineName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                enabled = isStopEnabled,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningSessionBack)
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
            }
        },
        actions = {
            if (phase != RunningSessionPhase.FINISHED) {
                TextButton(
                    onClick = onStop,
                    enabled = isStopEnabled,
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningStopWorkout)
                ) {
                    Text(
                        text = "Stop",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}

package com.lighthousepark.intervalsgym.running.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.running.HeartRateDevice
import com.lighthousepark.intervalsgym.running.HeartRateSensorState
import kotlinx.coroutines.delay

/**
 * UI tests: WorkoutRoutineScreenUiTest.heartRateDevicePicker_emptyStateInvokesRescanAndDismissCallbacks,
 * heartRateDevicePicker_emptyStateHidesDisconnectAction,
 * heartRateConnectionAutoDismissEffect_dismissesOnlyAfterDisconnectedStateConnects.
 */
@Composable
internal fun HeartRateDevicePickerDialog(
    state: HeartRateSensorState,
    onDismiss: () -> Unit,
    onDeviceSelected: (HeartRateDevice) -> Unit,
    onRescan: () -> Unit,
    onDisconnect: () -> Unit,
) {
    HeartRateConnectionAutoDismissEffect(
        isConnected = state.isConnected,
        onDismiss = onDismiss
    )
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.isConnecting, state.connectionDeadlineMillis) {
        while (state.isConnecting) {
            nowMillis = System.currentTimeMillis()
            delay(1_000L)
        }
    }
    val connectionRemainingSeconds = if (state.isConnecting && state.connectionDeadlineMillis > 0L) {
        (((state.connectionDeadlineMillis - nowMillis).coerceAtLeast(0L) + 999L) / 1000L).toInt()
    } else {
        0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("심박계 연결") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.isConnecting) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "심박계 연결 대기",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = state.connectedDeviceName.orEmpty().ifBlank { "심박계" },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${connectionRemainingSeconds}초 남음",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
                if (state.isConnected) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "연결된 심박계",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = state.connectedDeviceName.orEmpty().ifBlank { "심박계" },
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = state.heartRateBpm?.let { "$it bpm" } ?: "-- bpm",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                state.statusMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (state.isScanning) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("심박계를 검색 중입니다.")
                    }
                }
                if (state.devices.isEmpty() && !state.isScanning) {
                    Text(
                        text = "검색된 심박계가 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LazyColumn(
                    modifier = Modifier.heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.devices, key = { it.address }) { device ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDeviceSelected(device) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = device.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = device.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onRescan,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.HeartRatePickerRescan)
            ) {
                Text(if (state.isScanning) "검색 중" else "다시 검색")
            }
        },
        dismissButton = {
            Row {
                if (state.isConnected || state.isConnecting) {
                    TextButton(
                        onClick = onDisconnect,
                        modifier = Modifier.debugContentDescription(TestContentDescriptions.HeartRatePickerDisconnect)
                    ) {
                        Text("연결 해제")
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.HeartRatePickerDismiss)
                ) {
                    Text("닫기")
                }
            }
        }
    )
}

@Composable
internal fun HeartRateConnectionAutoDismissEffect(
    isConnected: Boolean,
    onDismiss: () -> Unit,
) {
    var hasObservedDisconnectedState by remember { mutableStateOf(!isConnected) }
    LaunchedEffect(isConnected) {
        if (!isConnected) {
            hasObservedDisconnectedState = true
        } else if (hasObservedDisconnectedState) {
            hasObservedDisconnectedState = false
            onDismiss()
        }
    }
}

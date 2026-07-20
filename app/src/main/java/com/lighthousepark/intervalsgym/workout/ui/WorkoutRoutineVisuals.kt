package com.lighthousepark.intervalsgym.workout.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.SyncAlt
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.core.formatDuration
import com.lighthousepark.intervalsgym.core.formatShortMonthDay
import com.lighthousepark.intervalsgym.core.formatWeight
import com.lighthousepark.intervalsgym.running.RunningRoutePoint
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.TrainingSportType

/**
 * UI tests: WorkoutRoutineVisualsUiTest.localRunningSessionGraphSection_invokesDeleteCallback,
 * WorkoutRoutineScreenUiTest.localRunningSessionDetail_deleteRemovesHistoryAndNavigatesBack.
 */
@Composable
internal fun LocalRunningSessionGraphSection(
    blocks: List<RoutineBlock>,
    totalSeconds: Int,
    routePoints: List<RunningRoutePoint>,
    canMergeWithGarmin: Boolean = false,
    isRunningMergeBusy: Boolean = false,
    isApplyingRunningMerge: Boolean = false,
    isMergedWithGarmin: Boolean = false,
    runningMergeMessage: String? = null,
    runningMergeError: String? = null,
    onMergeWithGarmin: () -> Unit = {},
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "로컬 러닝 기록 그래프",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.LocalRunningSessionDelete)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "로컬 기록 삭제",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            RoutineWorkoutGraphCanvas(
                blocks = blocks,
                totalSeconds = totalSeconds,
                sportType = TrainingSportType.RUNNING,
                height = 190.dp
            )
            if (routePoints.isNotEmpty()) {
                LocalRunningRoutePreview(routePoints = routePoints)
            }
            if (canMergeWithGarmin) {
                OutlinedButton(
                    onClick = onMergeWithGarmin,
                    enabled = !isRunningMergeBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .debugContentDescription(TestContentDescriptions.RunningMergeGarmin)
                ) {
                    if (isRunningMergeBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Outlined.SyncAlt, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        when {
                            isApplyingRunningMerge -> "Garmin 기록 병합 중"
                            isRunningMergeBusy -> "Garmin 기록 확인 중"
                            else -> "Garmin 기록과 병합"
                        }
                    )
                }
            }
            if (isMergedWithGarmin) {
                Text(
                    text = runningMergeMessage ?: "Garmin 기록과 병합됨",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            } else {
                runningMergeMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            runningMergeError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
internal fun TrainingItemDetailCard(
    item: TrainingItem,
    totalSeconds: Int,
    isStrengthRoutine: Boolean,
    strengthSession: CompletedStrengthSession?,
    uploadMessage: String?,
    uploadError: String?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isStrengthRoutine) Icons.Outlined.FitnessCenter else if (item.isRoutine) Icons.Outlined.Schedule else Icons.Outlined.Route,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                TrainingTypeLabel(isRoutine = item.isRoutine, resultLabel = "Summary")
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricChip(icon = Icons.Outlined.Today, text = item.date.formatShortMonthDay() + " " + item.timeLabel)
                if (totalSeconds > 0) {
                    MetricChip(icon = Icons.Outlined.Schedule, text = formatDuration(totalSeconds))
                }
                item.load?.let { MetricChip(icon = Icons.Outlined.Speed, text = "Load $it") }
                item.weightLiftedKg?.takeIf { it > 0.0 }?.let {
                    MetricChip(icon = Icons.Outlined.FitnessCenter, text = "Weight ${formatWeight(it)} kg")
                }
            }
            strengthSession?.let { workout ->
                StrengthSessionSummary(
                    workout = workout,
                    uploadMessage = uploadMessage,
                    uploadError = uploadError
                )
            }
            if (isStrengthRoutine) {
                Text(
                    text = "IntervalsGym 웨이트 Routine",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
internal fun DetailSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

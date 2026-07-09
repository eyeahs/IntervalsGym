package com.lighthousepark.intervalsgym.training.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface as MaterialSurface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.formatClock
import com.lighthousepark.intervalsgym.core.formatDistance
import com.lighthousepark.intervalsgym.core.formatDuration
import com.lighthousepark.intervalsgym.core.formatKoreanMonthDay
import com.lighthousepark.intervalsgym.core.formatWeight
import com.lighthousepark.intervalsgym.data.strengthRoutineForDisplay
import com.lighthousepark.intervalsgym.data.visibleRoutineDescription
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.totalVolumeKg
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.displayTimeLabel
import com.lighthousepark.intervalsgym.training.sportType
import com.lighthousepark.intervalsgym.training.workoutRoutineBlocksForPreview
import com.lighthousepark.intervalsgym.training.workoutRoutineTotalSecondsForPreview
import com.lighthousepark.intervalsgym.workout.ui.MetricChip
import com.lighthousepark.intervalsgym.workout.ui.RoutineWorkoutGraphCanvas
import com.lighthousepark.intervalsgym.workout.ui.TrainingSportIcon
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun DayHeader(
    day: LocalDate,
    count: Int,
    modifier: Modifier = Modifier,
    isDropTarget: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isDropTarget) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.56f)
                } else {
                    Color.Transparent
                }
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .then(onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier)
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = day.formatKoreanMonthDay(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${count}개",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.62f))
        )
    }
}

@Composable
internal fun TrainingItemRow(
    item: TrainingItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isApiPendingMove: Boolean = false,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TrainingStatusIcons(
                    item = item,
                    color = MaterialTheme.colorScheme.primary,
                    iconSize = 24.dp,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                TrainingSportIcon(
                    sportType = item.sportType(),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.name.ifBlank { item.type },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (isApiPendingMove) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "API반영중",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val strengthRoutine = item.strengthRoutineForDisplay()
                item.displayTimeLabel()?.let {
                    MetricChip(icon = Icons.Outlined.Today, text = it)
                }
                strengthRoutine?.entries?.takeIf { it.isNotEmpty() }?.let { entries ->
                    MetricChip(icon = Icons.Outlined.FitnessCenter, text = "${entries.size}종목")
                    entries.totalVolumeKg().takeIf { it > 0.0 }?.let { volume ->
                        MetricChip(icon = Icons.Outlined.FitnessCenter, text = "Lift ${formatWeight(volume)} kg")
                    }
                }
                item.durationSeconds?.let {
                    MetricChip(icon = Icons.Outlined.Schedule, text = formatDuration(it))
                }
                item.distanceMeters?.takeIf { it > 0.0 }?.let {
                    MetricChip(icon = Icons.Outlined.Route, text = formatDistance(it))
                }
                item.weightLiftedKg?.takeIf { it > 0.0 }?.let {
                    MetricChip(icon = Icons.Outlined.FitnessCenter, text = "${formatWeight(it)} kg")
                }
                item.load?.let {
                    MetricChip(icon = Icons.Outlined.Speed, text = "Load $it")
                }
            }
            val previewBlocks = item.workoutRoutineBlocksForPreview()
            if (previewBlocks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                RoutineWorkoutGraphCanvas(
                    blocks = previewBlocks,
                    totalSeconds = item.workoutRoutineTotalSecondsForPreview(previewBlocks),
                    sportType = item.sportType(),
                    height = 112.dp
                )
            } else {
                item.description.visibleRoutineDescription().takeIf { it.isNotBlank() }?.let { description ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            item.matchedStrengthSession?.let { workout ->
                Spacer(modifier = Modifier.height(10.dp))
                StrengthMatchSummary(workout = workout)
            }
            if (item.isLocalOnlyRunningResult) {
                Spacer(modifier = Modifier.height(10.dp))
                LocalRunningResultSummary(item = item)
            }
        }
    }
}

@Composable
internal fun LocalRunningResultSummary(item: TrainingItem) {
    MaterialSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (item.description.orEmpty().contains("업로드됨")) {
                    "로컬 러닝 기록 저장됨 · Intervals.icu 업로드됨"
                } else {
                    "로컬 러닝 기록 저장됨"
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = listOfNotNull(
                    item.durationSeconds?.let { "운동 시간 ${formatDuration(it)}" },
                    item.distanceMeters?.takeIf { it > 0.0 }?.let { "예상 거리 ${formatDistance(it)}" }
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
internal fun StrengthMatchSummary(workout: CompletedStrengthSession) {
    val completedSets = workout.setEvents.size
    val totalRestSeconds = workout.restEvents.sumOf { it.actualSeconds }
    val volume = workout.entries.totalVolumeKg()
    MaterialSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "로컬 상세 기록 매칭",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${completedSets}세트 · Load ${workout.trainingLoad} · 볼륨 ${formatWeight(volume)} kg · 실제 휴식 ${formatClock(totalRestSeconds)}",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

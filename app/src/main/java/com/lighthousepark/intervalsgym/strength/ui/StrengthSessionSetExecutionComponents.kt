package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.core.throttleRapidTaps
import com.lighthousepark.intervalsgym.core.formatShortMonthDayTime
import com.lighthousepark.intervalsgym.core.formatWeight
import com.lighthousepark.intervalsgym.strength.CompletedStrengthExerciseHistory
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.isUnilateral
import com.lighthousepark.intervalsgym.strength.unilateralRepsSummary
import com.lighthousepark.intervalsgym.strength.unilateralWeightSummary
import com.lighthousepark.intervalsgym.strength.weightInputUnitLabel
import com.lighthousepark.intervalsgym.strength.withPropagatedRecordChange
import com.lighthousepark.intervalsgym.strength.withRecordReplaced
import com.lighthousepark.intervalsgym.strength.withRecords
import com.lighthousepark.intervalsgym.workout.ui.EmptyView
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Dialog preview for configured set details before execution.
 * This is not the active set screen; use [StrengthSetExecutionScreen] for performing sets.
 */
@Composable
internal fun StrengthExerciseSetDialog(
    entry: StrengthRoutineEntry,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("확인")
            }
        },
        title = { Text(entry.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                entry.records.forEachIndexed { index, record ->
                    Text(
                        text = if (entry.isUnilateral()) {
                            "Set ${index + 1}  ${record.unilateralWeightSummary()}  ${record.unilateralRepsSummary()}  휴식 ${record.restSeconds.ifBlank { "-" }}초"
                        } else {
                            "Set ${index + 1}  ${record.weightKg.ifBlank { "-" }}kg  ${record.reps.ifBlank { "-" }}회  휴식 ${record.restSeconds.ifBlank { "-" }}초"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    )
}

/**
 * Sub-screen of [StrengthSessionScreen] for completing and editing sets during a workout.
 * Keep active-set completion and in-workout set edits here.
 *
 * UI tests: StrengthSessionUiTest.setExecutionScreen_invokesExerciseChangeAndAddSetCallbacks,
 * setExecutionScreen_currentSetRecordsActualValuesWithoutChangingPlan.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun StrengthSetExecutionScreen(
    entry: StrengthRoutineEntry?,
    currentSetIndex: Int = 0,
    resettableCompletedSetRecordId: Int? = null,
    recentHistory: List<CompletedStrengthExerciseHistory> = emptyList(),
    modifier: Modifier = Modifier,
    onExerciseClick: () -> Unit,
    onEntryChange: (StrengthRoutineEntry) -> Unit,
    onAddSet: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (entry == null) {
            item {
                EmptyView(message = "수행할 세트가 없습니다.")
            }
        } else {
            val activeSetIndex = entry.records.indexOfFirst { !it.completed }
                .takeIf { it >= 0 }
                ?: currentSetIndex
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .debugContentDescription(TestContentDescriptions.StrengthSetExecutionExercise)
                        .throttleRapidTaps()
                        .clickable(onClick = onExerciseClick),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = entry.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = entry.exercise.group,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (entry.note.isNotBlank()) {
                                Text(
                                    text = entry.note,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "변경",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            itemsIndexed(entry.records, key = { _, record -> record.id }) { index, record ->
                StrengthSetRecordRow(
                    index = index,
                    record = record,
                    modifier = Modifier.animateItem(),
                    isUnilateral = entry.isUnilateral(),
                    weightUnit = entry.weightInputUnitLabel(),
                    showCompletion = false,
                    canResetCompleted = record.id == resettableCompletedSetRecordId,
                    showActualInput = index == activeSetIndex,
                    onDelete = if (entry.records.size > 1) {
                        {
                            onEntryChange(
                                entry.withRecords(
                                    entry.records.filterIndexed { recordIndex, _ -> recordIndex != index }
                                )
                            )
                        }
                    } else {
                        null
                    },
                    onActualRecordChange = { next ->
                        onEntryChange(entry.withRecordReplaced(index, next))
                    },
                    onRecordChange = { next ->
                        onEntryChange(entry.withPropagatedRecordChange(index, next))
                    }
                )
            }
            item {
                OutlinedButton(
                    onClick = onAddSet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .debugContentDescription(TestContentDescriptions.StrengthSetExecutionAddSet),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("세트 추가")
                }
            }
            if (recentHistory.isNotEmpty()) {
                item {
                    StrengthExerciseRecentHistorySection(history = recentHistory)
                }
            }
        }
    }
}

@Composable
private fun StrengthExerciseRecentHistorySection(
    history: List<CompletedStrengthExerciseHistory>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "최근 수행 History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "같은 운동, 기구, 타입 기준",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "최근 ${history.size}개",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            history.forEachIndexed { index, item ->
                if (index > 0) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
                StrengthExerciseHistoryItem(item = item)
            }
        }
    }
}

@Composable
private fun StrengthExerciseHistoryItem(
    item: CompletedStrengthExerciseHistory,
) {
    val startedAt = remember(item.session.startedAtMillis) {
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(item.session.startedAtMillis),
            ZoneId.systemDefault()
        )
    }
    val rows = remember(item) { item.toStrengthExerciseHistoryRows() }
    val volume = remember(item) { item.historyVolumeKg() }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = startedAt.formatShortMonthDayTime(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = item.session.routineName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${rows.size}세트 · ${formatWeight(volume)}kg",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
        rows.take(5).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = row.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(48.dp)
                )
                Text(
                    text = row.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (rows.size > 5) {
            Text(
                text = "+${rows.size - 5}세트 더 있음",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

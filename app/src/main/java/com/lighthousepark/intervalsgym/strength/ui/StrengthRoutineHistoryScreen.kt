package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.app.ROUTE_STRENGTH_HISTORY
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.core.formatShortMonthDayTime
import com.lighthousepark.intervalsgym.core.formatWeight
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.totalVolumeKg
import com.lighthousepark.intervalsgym.workout.ui.EmptyView
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Route owner for [ROUTE_STRENGTH_HISTORY].
 * Reuse this when choosing a previous completed workout snapshot for a routine.
 * UI tests: StrengthRoutineHistoryUiTest.historyScreen_filtersByRoutineAndSelectsMatchingWorkout,
 * historyScreen_showsEmptyStateWhenNoMatchingHistoryExists, historyScreen_backButtonInvokesBackCallback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StrengthRoutineHistoryScreen(
    routine: StrengthWorkoutRoutine?,
    history: List<CompletedStrengthSession>,
    onHistorySelected: (CompletedStrengthSession) -> Unit,
    onBack: () -> Unit,
) {
    val routineHistory = remember(routine?.id, history) {
        history
            .filter { workout -> routine == null || workout.routineId == routine.id }
            .sortedByDescending { it.startedAtMillis }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${routine?.name ?: "웨이트 routine"} history 선택",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthHistoryBack)
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (routineHistory.isEmpty()) {
            EmptyView(
                message = "저장된 history가 없습니다.",
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(routineHistory, key = { it.id }) { workout ->
                StrengthRoutineHistoryRow(
                    workout = workout,
                    onClick = { onHistorySelected(workout) }
                )
            }
        }
    }
}

@Composable
private fun StrengthRoutineHistoryRow(
    workout: CompletedStrengthSession,
    onClick: () -> Unit,
) {
    val startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(workout.startedAtMillis), ZoneId.systemDefault())
    val completedSets = workout.setEvents.size
    val totalSets = workout.entries.sumOf { it.records.size }
    val volume = workout.entries.totalVolumeKg()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .debugContentDescription(TestContentDescriptions.strengthHistoryRow(workout.id))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = startedAt.formatShortMonthDayTime(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${workout.entries.size}개 운동 · Load ${workout.trainingLoad} · $completedSets/$totalSets 세트 · ${formatWeight(volume)} kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = if (workout.uploadedToIntervals) "업로드됨" else "미동기화",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (workout.uploadedToIntervals) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = workout.entries.joinToString(" · ") { it.title },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.MainActivity
import com.lighthousepark.intervalsgym.R
import com.lighthousepark.intervalsgym.app.*
import com.lighthousepark.intervalsgym.core.*
import com.lighthousepark.intervalsgym.data.*
import com.lighthousepark.intervalsgym.login.*
import com.lighthousepark.intervalsgym.overlay.*
import com.lighthousepark.intervalsgym.running.*
import com.lighthousepark.intervalsgym.running.ui.*
import com.lighthousepark.intervalsgym.strength.*
import com.lighthousepark.intervalsgym.strength.ui.*
import com.lighthousepark.intervalsgym.training.*
import com.lighthousepark.intervalsgym.training.ui.*
import com.lighthousepark.intervalsgym.workout.ui.*

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
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Route owner for [ROUTE_STRENGTH_HISTORY].
 * Reuse this when choosing a previous completed workout snapshot for a plan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StrengthPlanHistoryScreen(
    plan: StrengthWorkoutPlan?,
    history: List<CompletedStrengthWorkout>,
    onHistorySelected: (CompletedStrengthWorkout) -> Unit,
    onBack: () -> Unit,
) {
    val planHistory = remember(plan?.id, history) {
        history
            .filter { workout -> plan == null || workout.planId == plan.id }
            .sortedByDescending { it.startedAtMillis }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${plan?.name ?: "웨이트 plan"} history 선택",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (planHistory.isEmpty()) {
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
            items(planHistory, key = { it.id }) { workout ->
                StrengthPlanHistoryRow(
                    workout = workout,
                    onClick = { onHistorySelected(workout) }
                )
            }
        }
    }
}

@Composable
private fun StrengthPlanHistoryRow(
    workout: CompletedStrengthWorkout,
    onClick: () -> Unit,
) {
    val startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(workout.startedAtMillis), ZoneId.systemDefault())
    val completedSets = workout.setEvents.size
    val totalSets = workout.entries.sumOf { it.records.size }
    val volume = workout.entries.totalVolumeKg()
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                        text = startedAt.format(DateTimeFormatter.ofPattern("M/d HH:mm", Locale.KOREAN)),
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

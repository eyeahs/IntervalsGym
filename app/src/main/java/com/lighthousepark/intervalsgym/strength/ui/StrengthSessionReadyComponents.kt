package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthSetRecord
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.supersetGroupLabels
import com.lighthousepark.intervalsgym.workout.ui.buildStrengthSetSummary

/**
 * Sub-screen of StrengthSessionScreen shown before a strength workout starts.
 * Keep pre-start exercise expansion and edit/start actions here.
 * UI tests: StrengthSessionUiTest.readyScreen_startButtonInvokesStart,
 * readyScreen_editButtonInvokesEditRoutine, readyScreen_entryRowTogglesSetDetails.
 */
@Composable
internal fun StrengthSessionReadyScreen(
    routine: StrengthWorkoutRoutine,
    entries: List<StrengthRoutineEntry>,
    modifier: Modifier = Modifier,
    onStart: () -> Unit,
    onEditRoutine: (() -> Unit)?,
) {
    var expandedEntryIds by remember(routine.id, entries) { mutableStateOf(emptySet<Int>()) }
    val supersetLabels = remember(entries) { entries.supersetGroupLabels() }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "운동 목록",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (routine.location.isNotBlank()) {
                        Text(
                            text = "장소 · ${routine.location.trim()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            items(entries, key = { it.id }) { entry ->
                val isExpanded = entry.id in expandedEntryIds
                val supersetLabel = entry.supersetGroupId?.let { supersetLabels[it] }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .debugContentDescription(TestContentDescriptions.strengthReadyEntry(entry.id))
                        .animateContentSize(animationSpec = spring())
                        .clickable {
                            expandedEntryIds = if (isExpanded) {
                                expandedEntryIds - entry.id
                            } else {
                                expandedEntryIds + entry.id
                            }
                        },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        supersetLabel?.let { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${entry.records.size}세트",
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
                        if (isExpanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                entry.records.forEachIndexed { index, record ->
                                    StrengthReadySetRow(
                                        entry = entry,
                                        record = record,
                                        index = index
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onEditRoutine != null) {
                    OutlinedButton(
                        onClick = onEditRoutine,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .debugContentDescription(TestContentDescriptions.StrengthEditWorkoutRoutine),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("운동 수정", maxLines = 1)
                    }
                }
                Button(
                    onClick = onStart,
                    enabled = entries.isNotEmpty(),
                    modifier = Modifier
                        .weight(if (onEditRoutine != null) 2f else 1f)
                        .height(52.dp)
                        .debugContentDescription(TestContentDescriptions.StrengthStartWorkout),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("운동 시작", maxLines = 1)
                }
            }
        }
    }
}

@Composable
internal fun StrengthReadySetRow(
    entry: StrengthRoutineEntry,
    record: StrengthSetRecord,
    index: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Set ${index + 1}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(52.dp)
        )
        Text(
            text = buildStrengthSetSummary(entry, record),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

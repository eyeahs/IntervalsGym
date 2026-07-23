package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.core.throttleRapidTaps
import com.lighthousepark.intervalsgym.strength.StrengthExercise
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthSetMetricType
import com.lighthousepark.intervalsgym.strength.isUnilateral
import com.lighthousepark.intervalsgym.strength.weightInputUnitLabel
import com.lighthousepark.intervalsgym.strength.withPropagatedRecordChange

@Composable
internal fun ExerciseSearchRow(
    exercise: StrengthExercise,
    title: String = exercise.nameKo,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .debugContentDescription(TestContentDescriptions.strengthExerciseSearchResult(exercise.id))
            .throttleRapidTaps()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${exercise.nameEn} · ${exercise.group}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun ChoiceGrid(
    title: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    isOptionEnabled: (String) -> Boolean = { true },
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        options.chunked(2).forEach { rowOptions ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowOptions.forEach { option ->
                    if (option == selected) {
                        Button(
                            onClick = { onSelected(option) },
                            enabled = isOptionEnabled(option),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .debugContentDescription(TestContentDescriptions.strengthChoiceOption(title, option))
                        ) {
                            Text(option)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onSelected(option) },
                            enabled = isOptionEnabled(option),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .debugContentDescription(TestContentDescriptions.strengthChoiceOption(title, option))
                        ) {
                            Text(option)
                        }
                    }
                }
                repeat(2 - rowOptions.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
internal fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { next ->
            if (next.all { it.isDigit() || it == '.' }) onValueChange(next)
        },
        modifier = modifier,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}

@Composable
internal fun StrengthRoutineEntryCard(
    entry: StrengthRoutineEntry,
    onEntryChange: (StrengthRoutineEntry) -> Unit,
    onDelete: (() -> Unit)? = null,
    showCompletion: Boolean = true,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (entry.setMetricType == StrengthSetMetricType.DURATION) {
                            "${entry.targetSets}세트 x ${entry.records.firstOrNull()?.durationSeconds.orEmpty().ifBlank { "-" }}초 · 휴식 ${entry.restSeconds}초"
                        } else {
                            "${entry.targetSets}세트 x ${entry.targetReps}회 · 휴식 ${entry.restSeconds}초"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = "삭제")
                    }
                }
            }
            entry.records.forEachIndexed { index, record ->
                StrengthSetRecordRow(
                    index = index,
                    record = record,
                    isUnilateral = entry.isUnilateral(),
                    setMetricType = entry.setMetricType,
                    weightUnit = entry.weightInputUnitLabel(),
                    showCompletion = showCompletion,
                    onRecordChange = { next ->
                        onEntryChange(entry.withPropagatedRecordChange(index, next))
                    }
                )
            }
        }
    }
}

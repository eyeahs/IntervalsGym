package com.lighthousepark.intervalsgym.workout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.core.formatClock
import com.lighthousepark.intervalsgym.core.formatDuration
import com.lighthousepark.intervalsgym.core.formatWeight
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthSetRecord
import com.lighthousepark.intervalsgym.strength.isUnilateral
import com.lighthousepark.intervalsgym.strength.totalVolumeKg

@Composable
internal fun StrengthSessionSummary(
    workout: CompletedStrengthSession,
    uploadMessage: String?,
    uploadError: String?,
) {
    val totalRestSeconds = workout.restEvents.sumOf { it.actualSeconds }
    val volume = workout.entries.totalVolumeKg()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = if (workout.uploadedToIntervals) "로컬 기록 · Intervals.icu 업로드됨" else "로컬 기록 · Intervals.icu 미동기화",
            style = MaterialTheme.typography.labelLarge,
            color = if (workout.uploadedToIntervals) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${workout.setEvents.size}세트 · RPE ${workout.rpe} · Load ${workout.trainingLoad} · 볼륨 ${formatWeight(volume)} kg · 운동 시간 ${formatDuration(workout.durationSeconds)} · 실제 휴식 ${formatClock(totalRestSeconds)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        uploadMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
        uploadError?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

/**
 * UI tests: WorkoutRoutineVisualsUiTest.localStrengthSessionDetailSection_rendersCompletedSetWithActualRest.
 */
@Composable
internal fun LocalStrengthSessionDetailSection(
    workout: CompletedStrengthSession,
) {
    DetailSection(title = "웨이트 상세 기록") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            workout.entries.forEachIndexed { entryIndex, entry ->
                if (entryIndex > 0) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
                StrengthSessionExerciseDetail(
                    workout = workout,
                    entry = entry
                )
            }
        }
    }
}

@Composable
internal fun StrengthSessionExerciseDetail(
    workout: CompletedStrengthSession,
    entry: StrengthRoutineEntry,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = entry.title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        entry.records.forEachIndexed { index, record ->
            StrengthSessionSetDetailRow(
                workout = workout,
                entry = entry,
                record = record,
                setIndex = index
            )
        }
    }
}

/**
 * UI tests: WorkoutRoutineVisualsUiTest.localStrengthSessionDetailSection_rendersCompletedSetWithActualRest.
 */
@Composable
internal fun StrengthSessionSetDetailRow(
    workout: CompletedStrengthSession,
    entry: StrengthRoutineEntry,
    record: StrengthSetRecord,
    setIndex: Int,
) {
    val completedEvent = workout.setEvents.firstOrNull {
        it.exerciseEntryId == entry.id && it.setRecordId == record.id
    }
    val restEvent = completedEvent?.let { event ->
        workout.restEvents.firstOrNull { it.afterSetSequence == event.sequence }
    }
    val rawWeight = completedEvent?.weightKg
        ?: record.weightKg.ifBlank { entry.targetWeightKg.ifBlank { "-" } }
    val rawReps = completedEvent?.reps ?: record.reps.ifBlank { "-" }
    val plannedRest = completedEvent?.targetRestSeconds
        ?: record.restSeconds.toIntOrNull()
        ?: entry.restSeconds
    val isCompleted = completedEvent != null || record.completed
    val weightText = displayWeightText(rawWeight)
    val repsText = if (entry.isUnilateral()) {
        "각 ${displayUnilateralRepsText(rawReps)}"
    } else {
        displayRepsText(rawReps)
    }
    val actualRestText = restEvent?.let { " · 실제 ${formatClock(it.actualSeconds)}" }.orEmpty()
    val detailText = "$weightText x $repsText · 휴식 ${plannedRest}초$actualRestText"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .debugContentDescription(TestContentDescriptions.strengthSessionSetDetail(entry.id, record.id)),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Set ${setIndex + 1}",
            style = MaterialTheme.typography.labelLarge,
            color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(52.dp)
        )
        Text(
            text = detailText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (isCompleted) "완료" else "미완료",
            style = MaterialTheme.typography.labelMedium,
            color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}

internal fun displayWeightText(raw: String): String {
    val value = raw.trim()
    if (value.isBlank() || value == "-") return "-kg"
    if (value.contains("좌") || value.contains("우")) {
        val numbers = Regex("""\d+(?:\.\d+)?""").findAll(value).map { it.value }.toList()
        val distinctNumbers = numbers.distinct()
        return when {
            distinctNumbers.size == 1 -> "${distinctNumbers.first()}kg"
            numbers.isEmpty() -> "-kg"
            else -> value
        }
    }
    return if (value.contains("kg", ignoreCase = true)) value else "${value}kg"
}

internal fun displayRepsText(raw: String): String {
    val value = raw.trim()
    if (value.isBlank() || value == "-") return "-회"
    return if (value.contains("회")) value else "${value}회"
}

internal fun displayUnilateralRepsText(raw: String): String {
    val value = raw.trim()
    if (value.isBlank() || value == "-") return "-회"
    if (value.contains("좌") || value.contains("우")) {
        val numbers = Regex("""\d+""").findAll(value).map { it.value }.toList()
        val distinctNumbers = numbers.distinct()
        return when {
            distinctNumbers.size == 1 -> "${distinctNumbers.first()}회"
            numbers.isEmpty() -> "-회"
            else -> displayRepsText(value)
        }
    }
    return displayRepsText(value)
}

internal fun buildStrengthSetSummary(
    entry: StrengthRoutineEntry,
    record: StrengthSetRecord,
): String {
    val weight = displayWeightText(record.summaryWeightText(entry))
    val reps = if (entry.isUnilateral()) {
        "각 ${displayUnilateralRepsText(record.summaryRepsText())}"
    } else {
        displayRepsText(record.summaryRepsText())
    }
    val rest = record.restSeconds.ifBlank { entry.restSeconds.takeIf { it > 0 }?.toString().orEmpty() }
        .ifBlank { "-" }
    return "$weight x $reps · 휴식 ${rest}초"
}

internal fun StrengthSetRecord.summaryWeightText(entry: StrengthRoutineEntry): String {
    if (weightKg.isNotBlank()) return weightKg
    val left = leftWeightKg.trim()
    val right = rightWeightKg.trim()
    return when {
        left.isNotBlank() && right.isNotBlank() && left == right -> left
        left.isNotBlank() && right.isNotBlank() -> "좌 ${left}kg / 우 ${right}kg"
        left.isNotBlank() -> left
        right.isNotBlank() -> right
        else -> entry.targetWeightKg
    }
}

internal fun StrengthSetRecord.summaryRepsText(): String {
    if (reps.isNotBlank()) return reps
    val left = leftReps.trim()
    val right = rightReps.trim()
    return when {
        left.isNotBlank() && right.isNotBlank() && left == right -> left
        left.isNotBlank() && right.isNotBlank() -> "좌 ${left}회 / 우 ${right}회"
        left.isNotBlank() -> left
        else -> right
    }
}

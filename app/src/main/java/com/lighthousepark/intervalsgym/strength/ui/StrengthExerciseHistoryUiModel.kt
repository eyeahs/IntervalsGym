package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.core.formatClock
import com.lighthousepark.intervalsgym.strength.CompletedStrengthExerciseHistory
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.isUnilateral
import com.lighthousepark.intervalsgym.strength.weightInputUnitLabel
import com.lighthousepark.intervalsgym.workout.ui.displayRepsText
import com.lighthousepark.intervalsgym.workout.ui.displayWeightText

internal data class StrengthExerciseHistoryRow(
    val label: String,
    val detail: String,
)

internal fun CompletedStrengthExerciseHistory.toStrengthExerciseHistoryRows(): List<StrengthExerciseHistoryRow> {
    if (setEvents.isNotEmpty()) {
        return setEvents.map { event ->
            val actualRestSeconds = session.restEvents
                .firstOrNull { rest -> rest.afterSetSequence == event.sequence }
                ?.actualSeconds
            StrengthExerciseHistoryRow(
                label = "Set ${event.setIndex + 1}",
                detail = strengthHistorySetDetail(
                    entry = entry,
                    weightKg = event.weightKg,
                    reps = event.reps,
                    plannedRestSeconds = event.targetRestSeconds,
                    actualRestSeconds = actualRestSeconds
                )
            )
        }
    }
    val records = entry.records
        .filter { record -> record.completed }
        .ifEmpty {
            entry.records.filter { record -> record.weightKg.isNotBlank() || record.reps.isNotBlank() }
        }
        .ifEmpty { entry.records }
    return records.mapIndexed { index, record ->
        StrengthExerciseHistoryRow(
            label = "Set ${index + 1}",
            detail = strengthHistorySetDetail(
                entry = entry,
                weightKg = record.weightKg.ifBlank { entry.targetWeightKg },
                reps = record.reps,
                plannedRestSeconds = record.restSeconds.toIntOrNull() ?: entry.restSeconds,
                actualRestSeconds = null
            )
        )
    }
}

internal fun CompletedStrengthExerciseHistory.historyVolumeKg(): Double {
    val sideMultiplier = if (entry.isUnilateral()) 2.0 else 1.0
    if (setEvents.isNotEmpty()) {
        return setEvents.sumOf { event ->
            event.weightKg.firstNumberAsDouble() * event.reps.firstNumberAsInt() * sideMultiplier
        }
    }
    val records = entry.records
        .filter { record -> record.completed }
        .ifEmpty {
            entry.records.filter { record -> record.weightKg.isNotBlank() || record.reps.isNotBlank() }
        }
    return records.sumOf { record ->
        val weight = record.weightKg.firstNumberAsDouble()
            .takeIf { it > 0.0 }
            ?: entry.targetWeightKg.firstNumberAsDouble()
        val reps = record.reps.firstNumberAsInt()
            .takeIf { it > 0 }
            ?: entry.targetReps
        weight * reps * sideMultiplier
    }
}

private fun strengthHistorySetDetail(
    entry: StrengthRoutineEntry,
    weightKg: String,
    reps: String,
    plannedRestSeconds: Int,
    actualRestSeconds: Int?,
): String {
    val weight = strengthHistoryWeightText(entry, weightKg)
    val repsText = if (entry.isUnilateral()) {
        "각 ${displayRepsText(reps).removeSuffix("회")}회"
    } else {
        displayRepsText(reps)
    }
    val plannedRest = plannedRestSeconds.takeIf { it > 0 }?.toString() ?: "-"
    val actualRest = actualRestSeconds?.let { " · 실제 ${formatClock(it)}" }.orEmpty()
    return "$weight x $repsText · 휴식 ${plannedRest}초$actualRest"
}

private fun strengthHistoryWeightText(
    entry: StrengthRoutineEntry,
    weightKg: String,
): String {
    val value = weightKg.trim()
    if (entry.weightInputUnitLabel() == "체중" && value.isBlank()) return "체중"
    return displayWeightText(value.ifBlank { "-" })
}

private fun String.firstNumberAsDouble(): Double {
    return Regex("""\d+(?:\.\d+)?""").find(this)?.value?.toDoubleOrNull() ?: 0.0
}

private fun String.firstNumberAsInt(): Int {
    return Regex("""\d+""").find(this)?.value?.toIntOrNull() ?: 0
}

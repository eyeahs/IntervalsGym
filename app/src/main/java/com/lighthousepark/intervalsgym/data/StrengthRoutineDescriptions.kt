package com.lighthousepark.intervalsgym.data

import android.util.Base64
import com.lighthousepark.intervalsgym.app.INTERVALS_GYM_STRENGTH_ROUTINE_ID_PREFIX
import com.lighthousepark.intervalsgym.app.INTERVALS_GYM_STRENGTH_ROUTINE_PREFIX
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.isUnilateral
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.TrainingSportType
import com.lighthousepark.intervalsgym.training.sportType
import java.nio.charset.StandardCharsets

internal fun StrengthWorkoutRoutine.toIntervalsRoutineDescription(): String {
    val setCount = entries.sumOf { it.records.size }
    val encodedRoutine = java.util.Base64.getEncoder()
        .encodeToString(listOf(this).toJsonString().toByteArray(StandardCharsets.UTF_8))
    return buildString {
        appendLine("$INTERVALS_GYM_STRENGTH_ROUTINE_ID_PREFIX $id")
        appendLine("$INTERVALS_GYM_STRENGTH_ROUTINE_PREFIX $encodedRoutine")
        appendLine("IntervalsGym 웨이트 Routine")
        appendLine("운동 ${entries.size}개 · ${setCount}세트")
        appendLine()
        entries.forEach { entry ->
            appendLine("- ${entry.title}")
            if (entry.note.isNotBlank()) {
                appendLine("  메모: ${entry.note}")
            }
            entry.records.forEachIndexed { index, record ->
                if (entry.isUnilateral()) {
                    appendLine(
                        "  Set ${index + 1}: ${record.weightKg.ifBlank { "-" }}kg x 각 ${record.reps.ifBlank { "-" }}회, 휴식 ${record.restSeconds.ifBlank { "-" }}초"
                    )
                } else {
                    appendLine(
                        "  Set ${index + 1}: ${record.weightKg.ifBlank { "-" }}kg x ${record.reps.ifBlank { "-" }}회, 휴식 ${record.restSeconds.ifBlank { "-" }}초"
                    )
                }
            }
        }
    }
}

internal fun String?.visibleRoutineDescription(): String {
    if (isNullOrBlank()) return ""
    return lineSequence()
        .filterNot { line ->
            val trimmed = line.trim()
            trimmed.startsWith(INTERVALS_GYM_STRENGTH_ROUTINE_PREFIX) ||
                trimmed.startsWith(INTERVALS_GYM_STRENGTH_ROUTINE_ID_PREFIX) ||
                trimmed == "로컬 러닝 기록" ||
                trimmed.startsWith("로컬 러닝 기록 ·")
        }
        .joinToString("\n")
        .trim()
}

internal fun TrainingItem.detailRoutineDescription(): String {
    return pairedRoutine?.description.visibleRoutineDescription()
        .ifBlank { description.visibleRoutineDescription() }
}

internal fun TrainingItem.workoutDetailDescription(
    isWeightTrainingItem: Boolean,
    strengthRoutine: StrengthWorkoutRoutine?,
): String {
    if (!isWeightTrainingItem) return detailRoutineDescription()
    return if (!isRoutine && strengthRoutine == null) {
        description.orEmpty().trim()
    } else {
        ""
    }
}

internal fun TrainingItem.strengthRoutineForDisplay(): StrengthWorkoutRoutine? {
    if (sportType() != TrainingSportType.STRENGTH) return null
    if (!isRoutine && pairedRoutine == null) return null
    return matchedStrengthRoutine
        ?: pairedRoutine?.matchedStrengthRoutine
        ?: description.toIntervalsGymStrengthRoutine()
        ?: pairedRoutine?.description.toIntervalsGymStrengthRoutine()
}

internal fun String?.toIntervalsGymStrengthRoutine(): StrengthWorkoutRoutine? {
    if (isNullOrBlank()) return null
    val encoded = lineSequence()
        .map { it.trim() }
        .firstOrNull { it.startsWith(INTERVALS_GYM_STRENGTH_ROUTINE_PREFIX) }
        ?.removePrefix(INTERVALS_GYM_STRENGTH_ROUTINE_PREFIX)
        ?.trim()
        ?: return null
    return runCatching {
        val decodedBytes = runCatching {
            Base64.decode(encoded, Base64.DEFAULT)
        }.getOrElse {
            java.util.Base64.getDecoder().decode(encoded)
        }
        val decoded = String(decodedBytes, StandardCharsets.UTF_8)
        decoded.toStrengthWorkoutRoutines().firstOrNull()
    }.getOrNull()
}

internal fun String?.toIntervalsGymStrengthRoutineId(): Int? {
    if (isNullOrBlank()) return null
    return lineSequence()
        .map { it.trim() }
        .firstOrNull { it.startsWith(INTERVALS_GYM_STRENGTH_ROUTINE_ID_PREFIX) }
        ?.removePrefix(INTERVALS_GYM_STRENGTH_ROUTINE_ID_PREFIX)
        ?.trim()
        ?.toIntOrNull()
}

package com.lighthousepark.intervalsgym.strength

import com.lighthousepark.intervalsgym.core.formatClock
import com.lighthousepark.intervalsgym.core.formatDuration
import com.lighthousepark.intervalsgym.core.formatWeight
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.math.sqrt

internal fun StrengthSession.toIntervalsDescription(): String {
    val completedEvents = setEvents.sortedBy { it.sequence }
    val hasCompletionEvents = completedEvents.isNotEmpty()
    val totalVolume = if (hasCompletionEvents) {
        completedEvents.totalCompletedVolumeKg(entries)
    } else {
        entries.completedVolumeKg()
    }
    val completedSets = if (hasCompletionEvents) {
        completedEvents.size
    } else {
        entries.sumOf { entry -> entry.records.count { it.completed } }
    }
    val totalSets = if (hasCompletionEvents) {
        completedEvents.size
    } else {
        entries.sumOf { it.records.size }
    }
    val safeDurationSeconds = durationSeconds ?: entries.totalDurationSeconds()

    return buildString {
        appendLine("IntervalsGym 웨이트 트레이닝 기록")
        if (location.isNotBlank()) appendLine("장소: ${location.trim()}")
        appendLine("총 세트: $completedSets/$totalSets")
        appendLine("총 볼륨: ${formatWeight(totalVolume)} kg")
        appendLine("Weight Lifted: ${formatWeight(totalVolume)} kg")
        appendLine("RPE: $rpe")
        appendLine("Strength Load: $trainingLoad")
        appendLine("총 수행 시간: ${formatDuration(safeDurationSeconds)}")
        if (hasCompletionEvents) {
            appendLine("실제 휴식 합계: ${formatClock(restEvents.sumOf { it.actualSeconds })}")
        }
        appendLine()
        if (hasCompletionEvents) {
            appendCompletedSetEvents(completedEvents, restEvents, entries)
        } else {
            appendRoutineEntries(entries)
        }
    }
}

private fun StringBuilder.appendCompletedSetEvents(
    setEvents: List<StrengthSetCompletionEvent>,
    restEvents: List<StrengthRestEvent>,
    entries: List<StrengthRoutineEntry>,
) {
    val entriesById = entries.associateBy { it.id }
    val restEventsBySetSequence = restEvents.associateBy { it.afterSetSequence }
    var currentExerciseEntryId: Int? = null
    setEvents.forEach { event ->
        if (currentExerciseEntryId != event.exerciseEntryId) {
            if (currentExerciseEntryId != null) appendLine()
            appendLine("- ${event.exerciseTitle}")
            currentExerciseEntryId = event.exerciseEntryId
        }
        val entry = entriesById[event.exerciseEntryId]
        val weight = event.weightKg.ifBlank { entry?.targetWeightKg.orEmpty() }.ifBlank { "-" }
        val reps = event.reps.ifBlank { entry?.targetReps?.toString().orEmpty() }.ifBlank { "-" }
        val repsLabel = if (entry?.isUnilateral() == true && !reps.startsWith("각 ")) "각 ${reps}" else reps
        val rest = event.targetRestSeconds.takeIf { it > 0 }?.toString() ?: "-"
        appendLine("  Set ${event.setIndex + 1}: ${weight}kg x ${repsLabel}회, 계획 휴식 ${rest}초, 완료")
        restEventsBySetSequence[event.sequence]?.let { restEvent ->
            val actualRest = restEvent.actualSeconds.takeIf { it > 0 }
            val restStatus = when {
                actualRest != null -> "실제 휴식 ${formatClock(actualRest)}"
                restEvent.endedAtMillis == null -> "휴식 진행 중"
                else -> "실제 휴식 00:00"
            }
            appendLine("    $restStatus")
        }
    }
    appendLine()
}

private fun StringBuilder.appendRoutineEntries(entries: List<StrengthRoutineEntry>) {
    entries.forEach { entry ->
        appendLine("- ${entry.title}")
        if (entry.note.isNotBlank()) {
            appendLine("  메모: ${entry.note}")
        }
        appendLine("  Routine: ${entry.targetSets}세트 x ${entry.targetReps}회, 휴식 ${entry.restSeconds}초")
        entry.records.forEachIndexed { index, record ->
            val status = if (record.completed) "완료" else "미완료"
            val weight = record.weightKg.ifBlank { entry.targetWeightKg.ifBlank { "-" } }
            val reps = record.reps.ifBlank { "-" }
            val rest = record.restSeconds.ifBlank { entry.restSeconds.takeIf { it > 0 }?.toString() ?: "-" }
            if (entry.isUnilateral()) {
                appendLine("  Set ${index + 1}: ${weight}kg x 각 ${reps}회, 휴식 ${rest}초, $status")
            } else {
                appendLine("  Set ${index + 1}: ${weight}kg x ${reps}회, 휴식 ${rest}초, $status")
            }
        }
        appendLine()
    }
}

internal fun buildStrengthTcx(
    name: String,
    startedAt: LocalDateTime,
    durationSeconds: Int,
): String {
    val start = startedAt.atZone(ZoneId.systemDefault()).toInstant()
    val end = start.plusSeconds(durationSeconds.toLong())
    val startText = DateTimeFormatter.ISO_INSTANT.format(start)
    val endText = DateTimeFormatter.ISO_INSTANT.format(end)
    val safeName = name.xmlEscape()

    return """
        <?xml version="1.0" encoding="UTF-8"?>
        <TrainingCenterDatabase xmlns="http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2 http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd">
          <Activities>
            <Activity Sport="Other">
              <Id>$startText</Id>
              <Lap StartTime="$startText">
                <TotalTimeSeconds>$durationSeconds</TotalTimeSeconds>
                <DistanceMeters>0.0</DistanceMeters>
                <Calories>0</Calories>
                <Intensity>Active</Intensity>
                <TriggerMethod>Manual</TriggerMethod>
                <Track>
                  <Trackpoint>
                    <Time>$startText</Time>
                  </Trackpoint>
                  <Trackpoint>
                    <Time>$endText</Time>
                  </Trackpoint>
                </Track>
              </Lap>
              <Notes>$safeName</Notes>
            </Activity>
          </Activities>
        </TrainingCenterDatabase>
    """.trimIndent()
}

internal fun List<StrengthRoutineEntry>.totalDurationSeconds(): Int {
    return sumOf { entry ->
        val setSeconds = entry.records.sumOf { record ->
            record.durationSeconds.toIntOrNull()
                ?: if (record.completed) 45 else 0
        }
        val activeRecords = entry.records.filter { it.completed }.takeIf { it.isNotEmpty() } ?: entry.records
        val restSeconds = activeRecords.dropLast(1).sumOf { record ->
            record.restSeconds.toIntOrNull() ?: entry.restSeconds
        }
        setSeconds + restSeconds
    }
}

internal fun List<StrengthRoutineEntry>.totalVolumeKg(): Double {
    return sumOf { entry ->
        entry.records.sumOf { record ->
            val weightText = if (record.completed) record.performedWeightKg else record.weightKg
            val repsText = if (record.completed) record.performedReps else record.reps
            val weight = weightText.toDoubleOrNull() ?: entry.targetWeightKg.toDoubleOrNull() ?: 0.0
            val reps = repsText.toIntOrNull() ?: entry.targetReps
            val sideMultiplier = if (entry.isUnilateral()) 2.0 else 1.0
            if (record.completed || record.weightKg.isNotBlank() || record.reps.isNotBlank()) {
                weight * reps * sideMultiplier
            } else {
                0.0
            }
        }
    }
}

internal fun List<StrengthRoutineEntry>.completedVolumeKg(): Double {
    return sumOf { entry ->
        entry.records
            .filter { it.completed }
            .sumOf { record ->
                val weight = record.performedWeightKg.toDoubleOrNull()
                    ?: entry.targetWeightKg.toDoubleOrNull()
                    ?: 0.0
                val reps = record.performedReps.toIntOrNull() ?: entry.targetReps
                val sideMultiplier = if (entry.isUnilateral()) 2.0 else 1.0
                weight * reps * sideMultiplier
            }
    }
}

internal fun List<StrengthRoutineEntry>.completedDurationSeconds(): Int {
    return sumOf { entry ->
        val completedRecords = entry.records.filter { it.completed }
        val setSeconds = completedRecords.sumOf { record ->
            record.durationSeconds.toIntOrNull() ?: 45
        }
        val restSeconds = completedRecords.dropLast(1).sumOf { record ->
            record.restSeconds.toIntOrNull() ?: entry.restSeconds
        }
        setSeconds + restSeconds
    }
}

internal fun List<StrengthRoutineEntry>.completedStrengthTrainingLoad(rpe: Int): Int {
    return strengthTrainingLoadFromMetrics(
        durationSeconds = completedDurationSeconds(),
        volumeKg = completedVolumeKg(),
        rpe = rpe
    )
}

internal fun List<StrengthSetCompletionEvent>.totalCompletedVolumeKg(
    entries: List<StrengthRoutineEntry>,
): Double {
    val entriesById = entries.associateBy { it.id }
    return sumOf { event ->
        val entry = entriesById[event.exerciseEntryId]
        val weight = event.weightKg.toDoubleOrNull() ?: entry?.targetWeightKg?.toDoubleOrNull() ?: 0.0
        val reps = event.reps.removePrefix("각 ").trim().toIntOrNull() ?: entry?.targetReps ?: 0
        val sideMultiplier = if (entry?.isUnilateral() == true) 2.0 else 1.0
        weight * reps * sideMultiplier
    }
}

internal fun List<StrengthRoutineEntry>.strengthTrainingLoad(rpe: Int): Int {
    return strengthTrainingLoadFromMetrics(
        durationSeconds = totalDurationSeconds(),
        volumeKg = totalVolumeKg(),
        rpe = rpe
    )
}

internal fun strengthTrainingLoadFromMetrics(
    durationSeconds: Int,
    volumeKg: Double,
    rpe: Int,
): Int {
    val durationMinutes = durationSeconds.coerceAtLeast(60) / 60.0
    val safeVolumeKg = volumeKg.coerceAtLeast(0.0)
    val safeRpe = rpe.coerceIn(1, 10)
    return (durationMinutes * safeRpe / 10.0 + sqrt(safeVolumeKg) * 0.15)
        .roundToInt()
        .coerceAtLeast(1)
}

private fun String.xmlEscape(): String {
    return replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}

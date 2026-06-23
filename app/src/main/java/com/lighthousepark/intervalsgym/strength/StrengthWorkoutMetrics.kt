package com.lighthousepark.intervalsgym.strength

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

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.math.sqrt

internal fun StrengthWorkoutSession.toIntervalsDescription(): String {
    val totalVolume = entries.totalVolumeKg()
    val completedSets = entries.sumOf { entry -> entry.records.count { it.completed } }
    val totalSets = entries.sumOf { it.records.size }

    return buildString {
        appendLine("IntervalsGym 웨이트 트레이닝 기록")
        appendLine("총 세트: $completedSets/$totalSets")
        appendLine("총 볼륨: ${formatWeight(totalVolume)} kg")
        appendLine("Weight Lifted: ${formatWeight(totalVolume)} kg")
        appendLine("RPE: $rpe")
        appendLine("Strength Load: $trainingLoad")
        appendLine("총 수행 시간: ${formatDuration(entries.totalDurationSeconds())}")
        appendLine()
        entries.forEach { entry ->
            appendLine("- ${entry.title}")
            appendLine("  Plan: ${entry.targetSets}세트 x ${entry.targetReps}회, 휴식 ${entry.restSeconds}초")
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

internal fun List<StrengthPlanEntry>.totalDurationSeconds(): Int {
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

internal fun List<StrengthPlanEntry>.totalVolumeKg(): Double {
    return sumOf { entry ->
        entry.records.sumOf { record ->
            val weight = record.weightKg.toDoubleOrNull() ?: entry.targetWeightKg.toDoubleOrNull() ?: 0.0
            val reps = record.reps.toIntOrNull() ?: entry.targetReps
            val sideMultiplier = if (entry.isUnilateral()) 2.0 else 1.0
            if (record.completed || record.weightKg.isNotBlank() || record.reps.isNotBlank()) {
                weight * reps * sideMultiplier
            } else {
                0.0
            }
        }
    }
}

internal fun List<StrengthPlanEntry>.strengthTrainingLoad(rpe: Int): Int {
    val durationMinutes = totalDurationSeconds().coerceAtLeast(60) / 60.0
    val volumeKg = totalVolumeKg().coerceAtLeast(0.0)
    val safeRpe = rpe.coerceIn(1, 10)
    return (durationMinutes * safeRpe / 10.0 + sqrt(volumeKg) * 0.15)
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

package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.core.formatClockTime
import com.lighthousepark.intervalsgym.core.formatTargetNumber
import com.lighthousepark.intervalsgym.core.optNullableDouble
import com.lighthousepark.intervalsgym.core.optNullableInt
import com.lighthousepark.intervalsgym.core.parseDateTime
import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.TrainingSportType
import com.lighthousepark.intervalsgym.training.sportType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import org.json.JSONArray
import org.json.JSONObject

internal fun JSONArray.toTrainingItems(isRoutine: Boolean): List<TrainingItem> {
    return (0 until length()).mapNotNull { index ->
        val json = optJSONObject(index) ?: return@mapNotNull null
        val dateTime = parseDateTime(json.optString("start_date_local"))
        val date = dateTime?.toLocalDate() ?: return@mapNotNull null
        val workoutDoc = json.optJSONObject("workout_doc")
        val blocks = if (isRoutine) workoutDoc.toRoutineBlocks() else emptyList()
        val remoteId = json.optString("id", index.toString())

        TrainingItem(
            id = "${if (isRoutine) "routine" else "activity"}-$remoteId",
            remoteId = remoteId,
            externalId = json.optString("external_id")
                .ifBlank { json.optString("externalId") }
                .cleanJsonText(),
            name = json.optString("name").ifBlank { json.optString("type", if (isRoutine) "Workout" else "Activity") },
            type = json.optString("type", if (isRoutine) "Workout" else "Activity"),
            date = date,
            startedAt = dateTime,
            timeLabel = dateTime.toLocalTime().formatClockTime(),
            durationSeconds = json.optNullableInt("moving_time")
                ?: json.optNullableInt("elapsed_time")
                ?: json.optNullableInt("duration")
                ?: workoutDoc?.optNullableInt("duration")
                ?: blocks.sumOf { it.durationSeconds }.takeIf { it > 0 },
            distanceMeters = json.optNullableDouble("distance") ?: workoutDoc?.optNullableDouble("distance"),
            weightLiftedKg = json.optNullableDouble("kg_lifted")
                ?: json.optNullableDouble("weight_lifted")
                ?: json.optNullableDouble("weightLifted"),
            load = json.optNullableInt("icu_training_load"),
            fitness = json.optNullableDouble("fitness")
                ?: json.optNullableDouble("ctl")
                ?: json.optNullableDouble("icu_fitness")
                ?: json.optNullableDouble("icu_ctl"),
            fatigue = json.optNullableDouble("fatigue")
                ?: json.optNullableDouble("atl")
                ?: json.optNullableDouble("icu_fatigue")
                ?: json.optNullableDouble("icu_atl"),
            form = json.optNullableDouble("form")
                ?: json.optNullableDouble("tsb")
                ?: json.optNullableDouble("icu_form")
                ?: json.optNullableDouble("icu_tsb"),
            description = json.optString("description").cleanJsonText()
                ?: workoutDoc?.optString("description").cleanJsonText(),
            blocks = blocks,
            isRoutine = isRoutine,
            workoutDocJson = workoutDoc?.toString()
        )
    }
}

internal fun TrainingItem.toCalendarRoutineCopyJson(date: LocalDate): JSONObject {
    val startTime = startedAt
        ?.toLocalTime()
        ?.format(DateTimeFormatter.ISO_LOCAL_TIME)
        ?: Regex("""\d{1,2}:\d{2}(?::\d{2})?""")
            .find(timeLabel)
            ?.value
            ?.let { if (it.count { char -> char == ':' } == 1) "$it:00" else it }
        ?: "00:00:00"
    val event = JSONObject()
        .put("category", "WORKOUT")
        .put("name", name.ifBlank { type.ifBlank { "Workout" } })
        .put("type", type.ifBlank { sportType().toIntervalsRoutineType() })
        .put("start_date_local", "${date}T$startTime")
        .put("external_id", movedCalendarRoutineExternalId(date))

    description?.takeIf { it.isNotBlank() }?.let { event.put("description", it) }
    durationSeconds?.takeIf { it > 0 }?.let { event.put("duration", it) }
    distanceMeters?.takeIf { it > 0.0 }?.let { event.put("distance", it.roundToInt()) }

    val workoutDoc = workoutDocJson
        ?.let { raw -> runCatching { JSONObject(raw) }.getOrNull() }
        ?: blocks.toFallbackWorkoutDocJson(description)
    workoutDoc?.let { event.put("workout_doc", it) }
    return event
}

private fun TrainingItem.movedCalendarRoutineExternalId(date: LocalDate): String {
    val sourceId = remoteId.ifBlank { id }.replace(Regex("""[^A-Za-z0-9_.-]"""), "-")
    return "intervals-gym-moved-routine-$sourceId-$date"
}

private fun TrainingSportType.toIntervalsRoutineType(): String {
    return when (this) {
        TrainingSportType.RUNNING -> "Run"
        TrainingSportType.CYCLING -> "Ride"
        TrainingSportType.STRENGTH -> "WeightTraining"
        TrainingSportType.OTHER -> "Workout"
    }
}

private fun List<RoutineBlock>.toFallbackWorkoutDocJson(description: String?): JSONObject? {
    if (isEmpty()) return null
    return JSONObject()
        .put("description", description.orEmpty())
        .put("duration", sumOf { it.durationSeconds.coerceAtLeast(0) })
        .put(
            "steps",
            JSONArray().also { steps ->
                forEachIndexed { index, block ->
                    steps.put(
                        JSONObject()
                            .put("duration", block.durationSeconds.coerceAtLeast(0))
                            .put("text", block.fallbackWorkoutStepText(index))
                            .put("intensity", block.kind.ifBlank { if (block.isRecovery) "recovery" else "work" })
                    )
                }
            }
        )
}

private fun RoutineBlock.fallbackWorkoutStepText(index: Int): String {
    return listOf(
        title.ifBlank { "Block ${index + 1}" },
        targetText
    ).filter { it.isNotBlank() }.distinct().joinToString(" · ")
}

private fun JSONObject?.toRoutineBlocks(): List<RoutineBlock> {
    val steps = this?.optJSONArray("steps") ?: return emptyList()
    val flatSteps = mutableListOf<RawRoutineStep>()
    flattenRoutineSteps(steps, flatSteps)

    var cursor = 0
    return flatSteps.mapIndexed { index, step ->
        val start = cursor
        cursor += step.durationSeconds
        RoutineBlock(
            index = index,
            title = step.title.ifBlank { "Block ${index + 1}" },
            kind = step.kind,
            targetText = step.targetText,
            durationSeconds = step.durationSeconds,
            startSecond = start,
            endSecond = cursor,
            isRecovery = step.kind.contains("rest", ignoreCase = true) ||
                step.kind.contains("recover", ignoreCase = true) ||
                step.kind.contains("warm", ignoreCase = true) ||
                step.kind.contains("cool", ignoreCase = true),
            repeatIteration = step.repeatIteration,
            repeatCount = step.repeatCount
        )
    }
}

private data class RawRoutineStep(
    val title: String,
    val kind: String,
    val targetText: String,
    val durationSeconds: Int,
    val repeatIteration: Int?,
    val repeatCount: Int?,
)

private data class RawRepeatProgress(
    val iteration: Int,
    val count: Int,
)

private fun flattenRoutineSteps(
    steps: JSONArray,
    output: MutableList<RawRoutineStep>,
    parentRepeatProgress: RawRepeatProgress? = null,
) {
    for (index in 0 until steps.length()) {
        val step = steps.optJSONObject(index) ?: continue
        val reps = step.optNullableInt("reps")?.coerceAtLeast(1) ?: 1
        val nested = step.optJSONArray("steps")

        repeat(reps) { repIndex ->
            val repeatProgress = if (reps > 1) {
                RawRepeatProgress(iteration = repIndex + 1, count = reps)
            } else {
                parentRepeatProgress
            }
            if (nested != null) {
                flattenRoutineSteps(nested, output, repeatProgress)
            } else {
                val duration = step.optNullableInt("duration") ?: return@repeat
                if (duration <= 0) return@repeat

                output += RawRoutineStep(
                    title = step.optString("text").ifBlank {
                        step.optString("intensity").ifBlank {
                            if (reps > 1) "반복 ${repIndex + 1}" else "Workout"
                        }
                    },
                    kind = step.optString("intensity").ifBlank {
                        when {
                            step.optBoolean("warmup", false) -> "warmup"
                            step.optBoolean("cooldown", false) -> "cooldown"
                            step.optBoolean("freeride", false) -> "free ride"
                            else -> "work"
                        }
                    },
                    targetText = step.targetText(),
                    durationSeconds = duration,
                    repeatIteration = repeatProgress?.iteration,
                    repeatCount = repeatProgress?.count
                )
            }
        }
    }
}

private fun JSONObject.targetText(): String {
    return listOfNotNull(
        valueText("_pace") ?: valueText("pace"),
        valueText("_grade") ?: valueText("grade"),
        valueText("_incline") ?: valueText("incline"),
        valueText("_hr") ?: valueText("hr"),
        valueText("_power") ?: valueText("power"),
        valueText("cadence")
    ).joinToString(" · ")
}

private fun JSONObject.valueText(name: String): String? {
    val value = optJSONObject(name) ?: return null
    val units = value.optString("units").takeIf { it.isNotBlank() }.orEmpty()
    val target = value.optString("target").takeIf { it.isNotBlank() }
    val range = when {
        value.has("start") && value.has("end") -> {
            "${formatTargetNumber(value.optDouble("start"))}-${formatTargetNumber(value.optDouble("end"))}"
        }
        value.has("value") -> formatTargetNumber(value.optDouble("value"))
        else -> return target
    }
    return listOf(range + units, target).filter { !it.isNullOrBlank() }.joinToString(" ")
}

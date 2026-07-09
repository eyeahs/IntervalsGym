package com.lighthousepark.intervalsgym.data

import android.content.SharedPreferences
import com.lighthousepark.intervalsgym.core.optNullableDouble
import com.lighthousepark.intervalsgym.core.optNullableInt
import com.lighthousepark.intervalsgym.core.parseDateTime
import com.lighthousepark.intervalsgym.running.toRunningRoutePoints
import com.lighthousepark.intervalsgym.running.toRunningRoutePointsJsonArray
import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.WeekTrainingData
import java.time.LocalDate
import org.json.JSONArray
import org.json.JSONObject

private const val INTERVALS_WEEK_CACHE_PREFIX = "intervals_week_cache"

internal fun intervalsWeekCacheKey(apiKey: String, weekStart: LocalDate, weekEnd: LocalDate): String {
    return "$INTERVALS_WEEK_CACHE_PREFIX:${apiKey.hashCode()}:$weekStart:$weekEnd"
}

internal fun saveIntervalsWeekCache(
    prefs: SharedPreferences,
    apiKey: String,
    weekStart: LocalDate,
    weekEnd: LocalDate,
    data: WeekTrainingData,
) {
    val json = JSONObject()
        .put("weekStart", weekStart.toString())
        .put("weekEnd", weekEnd.toString())
        .put("cachedAtMillis", System.currentTimeMillis())
        .put("activities", data.activities.toTrainingItemsJsonArray())
        .put("routines", data.routines.toTrainingItemsJsonArray())
    prefs.edit()
        .putString(intervalsWeekCacheKey(apiKey, weekStart, weekEnd), json.toString())
        .apply()
}

internal fun loadIntervalsWeekCache(
    prefs: SharedPreferences,
    apiKey: String,
    weekStart: LocalDate,
    weekEnd: LocalDate,
): WeekTrainingData? {
    val saved = prefs.getString(intervalsWeekCacheKey(apiKey, weekStart, weekEnd), null) ?: return null
    return runCatching {
        val json = JSONObject(saved)
        if (json.optString("weekStart") != weekStart.toString() || json.optString("weekEnd") != weekEnd.toString()) {
            return@runCatching null
        }
        WeekTrainingData(
            activities = json.optJSONArray("activities").toCachedTrainingItems(),
            routines = json.optJSONArray("routines").toCachedTrainingItems()
        )
    }.getOrNull()
}

internal fun removeCalendarRoutineFromIntervalsCaches(
    prefs: SharedPreferences,
    apiKey: String,
    routine: TrainingItem,
) {
    val keyPrefix = "$INTERVALS_WEEK_CACHE_PREFIX:${apiKey.hashCode()}:"
    val keys = prefs.all.keys.filter { it.startsWith(keyPrefix) }
    if (keys.isEmpty()) return

    val editor = prefs.edit()
    keys.forEach { key ->
        val saved = prefs.getString(key, null) ?: return@forEach
        runCatching {
            val json = JSONObject(saved)
            val routines = json.optJSONArray("routines") ?: return@runCatching
            val nextRoutines = JSONArray()
            for (index in 0 until routines.length()) {
                val item = routines.optJSONObject(index) ?: continue
                val itemId = item.optString("id")
                val itemRemoteId = item.optString("remoteId")
                val itemExternalId = item.optString("externalId").cleanJsonText()
                val shouldRemove = itemId == routine.id ||
                    itemRemoteId == routine.remoteId ||
                    (routine.externalId != null && itemExternalId == routine.externalId)
                if (!shouldRemove) nextRoutines.put(item)
            }
            json.put("routines", nextRoutines)
            editor.putString(key, json.toString())
        }
    }
    editor.apply()
}

internal fun List<TrainingItem>.toTrainingItemsJsonArray(): JSONArray {
    return JSONArray().also { array ->
        forEach { item ->
            array.put(item.toTrainingItemJsonObject(includePairedRoutine = true))
        }
    }
}

private fun TrainingItem.toTrainingItemJsonObject(includePairedRoutine: Boolean): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("remoteId", remoteId)
        .put("externalId", externalId ?: JSONObject.NULL)
        .put("name", name)
        .put("type", type)
        .put("date", date.toString())
        .put("startedAt", startedAt?.toString() ?: JSONObject.NULL)
        .put("timeLabel", timeLabel)
        .put("durationSeconds", durationSeconds ?: JSONObject.NULL)
        .put("distanceMeters", distanceMeters ?: JSONObject.NULL)
        .put("weightLiftedKg", weightLiftedKg ?: JSONObject.NULL)
        .put("load", load ?: JSONObject.NULL)
        .put("fitness", fitness ?: JSONObject.NULL)
        .put("fatigue", fatigue ?: JSONObject.NULL)
        .put("form", form ?: JSONObject.NULL)
        .put("description", description ?: JSONObject.NULL)
        .put("blocks", blocks.toRoutineBlocksJsonArray())
        .put("isRoutine", isRoutine)
        .put("matchedStrengthRoutineJson", matchedStrengthRoutine?.let { listOf(it).toJsonString() } ?: JSONObject.NULL)
        .put("isLocalOnlyStrengthResult", isLocalOnlyStrengthResult)
        .put("isLocalOnlyRunningResult", isLocalOnlyRunningResult)
        .put("actualRunningBlocks", actualRunningBlocks.toRoutineBlocksJsonArray())
        .put("actualRunningRoutePoints", actualRunningRoutePoints.toRunningRoutePointsJsonArray())
        .put("pairedRoutine", pairedRoutine?.takeIf { includePairedRoutine }?.toTrainingItemJsonObject(includePairedRoutine = false) ?: JSONObject.NULL)
        .put("workoutDocJson", workoutDocJson ?: JSONObject.NULL)
}

internal fun JSONArray?.toCachedTrainingItems(): List<TrainingItem> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { index ->
        val json = optJSONObject(index) ?: return@mapNotNull null
        val date = runCatching { LocalDate.parse(json.optString("date")) }.getOrNull() ?: return@mapNotNull null
        TrainingItem(
            id = json.optString("id").ifBlank { "cached-$index" },
            remoteId = json.optString("remoteId").ifBlank { index.toString() },
            externalId = json.optString("externalId").cleanJsonText(),
            name = json.optString("name").ifBlank { "Workout" },
            type = json.optString("type").ifBlank { "Workout" },
            date = date,
            startedAt = parseDateTime(json.optString("startedAt")),
            timeLabel = json.optString("timeLabel").ifBlank { "--:--" },
            durationSeconds = json.optNullableInt("durationSeconds"),
            distanceMeters = json.optNullableDouble("distanceMeters"),
            weightLiftedKg = json.optNullableDouble("weightLiftedKg"),
            load = json.optNullableInt("load"),
            fitness = json.optNullableDouble("fitness"),
            fatigue = json.optNullableDouble("fatigue"),
            form = json.optNullableDouble("form"),
            description = json.optString("description").cleanJsonText(),
            blocks = json.optJSONArray("blocks").toCachedRoutineBlocks(),
            isRoutine = json.optBoolean("isRoutine", false),
            matchedStrengthRoutine = json.optString("matchedStrengthRoutineJson").toStrengthWorkoutRoutines().firstOrNull(),
            isLocalOnlyStrengthResult = json.optBoolean("isLocalOnlyStrengthResult", false),
            isLocalOnlyRunningResult = json.optBoolean("isLocalOnlyRunningResult", false),
            actualRunningBlocks = json.optJSONArray("actualRunningBlocks").toCachedRoutineBlocks(),
            actualRunningRoutePoints = json.optJSONArray("actualRunningRoutePoints").toRunningRoutePoints(),
            pairedRoutine = json.optJSONObject("pairedRoutine")?.let { pairedJson ->
                JSONArray().put(pairedJson).toCachedTrainingItems().firstOrNull()
            },
            workoutDocJson = json.optString("workoutDocJson").cleanJsonText()
        )
    }
}

internal fun List<RoutineBlock>.toRoutineBlocksJsonArray(): JSONArray {
    return JSONArray().also { array ->
        forEach { block ->
            array.put(
                JSONObject()
                    .put("index", block.index)
                    .put("title", block.title)
                    .put("kind", block.kind)
                    .put("targetText", block.targetText)
                    .put("durationSeconds", block.durationSeconds)
                    .put("startSecond", block.startSecond)
                    .put("endSecond", block.endSecond)
                    .put("isRecovery", block.isRecovery)
            )
        }
    }
}

internal fun JSONArray?.toCachedRoutineBlocks(): List<RoutineBlock> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { index ->
        val json = optJSONObject(index) ?: return@mapNotNull null
        RoutineBlock(
            index = json.optNullableInt("index") ?: index,
            title = json.optString("title").ifBlank { "Block ${index + 1}" },
            kind = json.optString("kind"),
            targetText = json.optString("targetText"),
            durationSeconds = json.optNullableInt("durationSeconds") ?: 0,
            startSecond = json.optNullableInt("startSecond") ?: 0,
            endSecond = json.optNullableInt("endSecond") ?: 0,
            isRecovery = json.optBoolean("isRecovery", false)
        )
    }
}

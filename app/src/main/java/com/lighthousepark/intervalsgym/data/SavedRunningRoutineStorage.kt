package com.lighthousepark.intervalsgym.data

import android.content.SharedPreferences
import com.lighthousepark.intervalsgym.app.SAVED_RUNNING_ROUTINES_PREF
import com.lighthousepark.intervalsgym.core.optNullableInt
import com.lighthousepark.intervalsgym.running.SavedRunningWorkoutRoutine
import org.json.JSONArray
import org.json.JSONObject

internal fun upsertSavedRunningWorkoutRoutine(
    prefs: SharedPreferences,
    routine: SavedRunningWorkoutRoutine,
) {
    val nextRoutines = (listOf(routine) + loadSavedRunningWorkoutRoutines(prefs))
        .distinctBy { it.id }
        .take(100)
    saveSavedRunningWorkoutRoutines(prefs, nextRoutines)
}

internal fun loadSavedRunningWorkoutRoutines(prefs: SharedPreferences): List<SavedRunningWorkoutRoutine> {
    val saved = prefs.getString(SAVED_RUNNING_ROUTINES_PREF, null)
    val routines = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    return (0 until routines.length()).mapNotNull { index ->
        routines.optJSONObject(index).toSavedRunningWorkoutRoutine()
    }
}

internal fun deleteSavedRunningWorkoutRoutine(
    prefs: SharedPreferences,
    routineId: String,
) {
    saveSavedRunningWorkoutRoutines(
        prefs = prefs,
        routines = loadSavedRunningWorkoutRoutines(prefs).filterNot { it.id == routineId }
    )
}

private fun saveSavedRunningWorkoutRoutines(
    prefs: SharedPreferences,
    routines: List<SavedRunningWorkoutRoutine>,
) {
    val array = JSONArray().apply {
        routines.forEach { routine ->
            put(routine.toJsonObject())
        }
    }
    prefs.edit().putString(SAVED_RUNNING_ROUTINES_PREF, array.toString()).apply()
}

private fun SavedRunningWorkoutRoutine.toJsonObject(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("name", name)
        .put("description", description ?: JSONObject.NULL)
        .put("durationSeconds", durationSeconds)
        .put("blocks", blocks.toRoutineBlocksJsonArray())
        .put("workoutDocJson", workoutDocJson ?: JSONObject.NULL)
        .put("savedAtMillis", savedAtMillis)
}

private fun JSONObject?.toSavedRunningWorkoutRoutine(): SavedRunningWorkoutRoutine? {
    this ?: return null
    val blocks = optJSONArray("blocks").toCachedRoutineBlocks()
    if (blocks.isEmpty()) return null
    return SavedRunningWorkoutRoutine(
        id = optString("id").ifBlank { "saved-running-${optLong("savedAtMillis", System.currentTimeMillis())}" },
        name = optString("name").ifBlank { "러닝 Routine" },
        description = optString("description").cleanJsonText(),
        durationSeconds = optNullableInt("durationSeconds") ?: blocks.sumOf { it.durationSeconds },
        blocks = blocks,
        workoutDocJson = optString("workoutDocJson").cleanJsonText(),
        savedAtMillis = optLong("savedAtMillis", System.currentTimeMillis())
    )
}

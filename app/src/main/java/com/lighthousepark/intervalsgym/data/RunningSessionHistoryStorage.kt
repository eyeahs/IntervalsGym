package com.lighthousepark.intervalsgym.data

import android.content.SharedPreferences
import com.lighthousepark.intervalsgym.app.RUNNING_SESSION_HISTORY_PREF
import com.lighthousepark.intervalsgym.core.optNullableInt
import com.lighthousepark.intervalsgym.running.CompletedRunningSession
import com.lighthousepark.intervalsgym.running.buildDokdoTrackRoutePoints
import com.lighthousepark.intervalsgym.running.estimatedRunningDistanceMeters
import com.lighthousepark.intervalsgym.running.normalizedRunningActualBlocks
import com.lighthousepark.intervalsgym.running.toJsonObject
import com.lighthousepark.intervalsgym.running.toRunningRoutePoints
import org.json.JSONArray
import org.json.JSONObject

internal fun appendRunningSessionHistory(
    prefs: SharedPreferences,
    workout: CompletedRunningSession,
) {
    val saved = prefs.getString(RUNNING_SESSION_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    val nextHistory = JSONArray().apply {
        put(workout.toJsonObject())
        val maxPreviousItems = 99
        for (index in 0 until minOf(history.length(), maxPreviousItems)) {
            val existing = history.optJSONObject(index) ?: continue
            if (existing.optString("id") != workout.id) {
                put(existing)
            }
        }
    }
    prefs.edit().putString(RUNNING_SESSION_HISTORY_PREF, nextHistory.toString()).apply()
}

internal fun replaceRunningSessionHistory(
    prefs: SharedPreferences,
    workout: CompletedRunningSession,
) {
    val saved = prefs.getString(RUNNING_SESSION_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    var replaced = false
    val nextHistory = JSONArray().apply {
        for (index in 0 until history.length()) {
            val existing = history.optJSONObject(index) ?: continue
            if (existing.optString("id") == workout.id) {
                put(workout.toJsonObject())
                replaced = true
            } else {
                put(existing)
            }
        }
        if (!replaced) put(workout.toJsonObject())
    }
    prefs.edit().putString(RUNNING_SESSION_HISTORY_PREF, nextHistory.toString()).apply()
}

internal fun deleteRunningSessionHistory(
    prefs: SharedPreferences,
    sessionId: String,
) {
    val saved = prefs.getString(RUNNING_SESSION_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    val nextHistory = JSONArray().apply {
        for (index in 0 until history.length()) {
            val existing = history.optJSONObject(index) ?: continue
            if (existing.optString("id") != sessionId) {
                put(existing)
            }
        }
    }
    prefs.edit().putString(RUNNING_SESSION_HISTORY_PREF, nextHistory.toString()).apply()
}

internal fun loadCompletedRunningSessionHistory(prefs: SharedPreferences): List<CompletedRunningSession> {
    val saved = prefs.getString(RUNNING_SESSION_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    return (0 until history.length()).mapNotNull { index ->
        history.optJSONObject(index).toCompletedRunningSession()
    }
}

private fun JSONObject?.toCompletedRunningSession(): CompletedRunningSession? {
    this ?: return null
    val startedAtMillis = optLong("startedAtMillis", 0L)
    val endedAtMillis = optLong("endedAtMillis", startedAtMillis)
    if (startedAtMillis <= 0L) return null
    val durationSeconds = optNullableInt("durationSeconds")
        ?: ((endedAtMillis - startedAtMillis) / 1000L).toInt().coerceAtLeast(0)
    val warmupSeconds = optNullableInt("warmupSeconds") ?: 0
    val routineBlocks = optJSONArray("blocks").toCachedRoutineBlocks()
    val savedActualBlocks = optJSONArray("actualBlocks").toCachedRoutineBlocks()
    val actualBlocks = savedActualBlocks.normalizedRunningActualBlocks(
        routineBlocks = routineBlocks,
        activeDurationSeconds = (durationSeconds - warmupSeconds).coerceAtLeast(0)
    )
    val savedRoutePoints = optJSONArray("routePoints").toRunningRoutePoints()
    return CompletedRunningSession(
        id = optString("id").ifBlank { "running-$startedAtMillis" },
        name = optString("name").ifBlank { "러닝" },
        startedAtMillis = startedAtMillis,
        endedAtMillis = endedAtMillis,
        durationSeconds = durationSeconds,
        warmupSeconds = warmupSeconds,
        estimatedDistanceMeters = actualBlocks.estimatedRunningDistanceMeters(),
        blocks = routineBlocks,
        actualBlocks = actualBlocks,
        uploadedToIntervals = optBoolean("uploadedToIntervals", false),
        routePoints = savedRoutePoints.ifEmpty {
            buildDokdoTrackRoutePoints(
                actualBlocks = actualBlocks,
                warmupSeconds = warmupSeconds
            )
        }
    )
}

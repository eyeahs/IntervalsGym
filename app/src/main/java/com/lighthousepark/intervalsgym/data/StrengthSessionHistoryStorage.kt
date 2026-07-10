package com.lighthousepark.intervalsgym.data

import android.content.SharedPreferences
import com.lighthousepark.intervalsgym.app.STRENGTH_SESSION_HISTORY_PREF
import com.lighthousepark.intervalsgym.core.formatExternalIdTimestamp
import com.lighthousepark.intervalsgym.core.optNullableInt
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthRestEvent
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthSetCompletionEvent
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.strengthTrainingLoad
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import org.json.JSONArray
import org.json.JSONObject

internal fun buildCompletedStrengthSession(
    routine: StrengthWorkoutRoutine,
    entries: List<StrengthRoutineEntry>,
    setEvents: List<StrengthSetCompletionEvent>,
    restEvents: List<StrengthRestEvent>,
    startedAtMillis: Long,
    endedAtMillis: Long,
    rpe: Int,
    trainingLoad: Int,
    uploadedToIntervals: Boolean,
    appliedToRoutine: Boolean = true,
    routineUpdateEntries: List<StrengthRoutineEntry>? = null,
): CompletedStrengthSession {
    val safeStartedAt = startedAtMillis.takeIf { it > 0L } ?: endedAtMillis
    return CompletedStrengthSession(
        id = strengthSessionResultId(routine.id, safeStartedAt),
        routineId = routine.id,
        routineName = routine.name,
        startedAtMillis = safeStartedAt,
        endedAtMillis = endedAtMillis,
        durationSeconds = ((endedAtMillis - safeStartedAt) / 1000L).toInt().coerceAtLeast(0),
        intervalsExternalId = strengthIntervalsExternalId(safeStartedAt),
        entries = entries,
        setEvents = setEvents.sortedBy { it.sequence },
        restEvents = restEvents.sortedBy { it.startedAtMillis },
        rpe = rpe,
        trainingLoad = trainingLoad,
        uploadedToIntervals = uploadedToIntervals,
        appliedToRoutine = appliedToRoutine,
        routineUpdateEntries = routineUpdateEntries
    )
}

internal fun strengthSessionResultId(
    routineId: Int,
    startedAtMillis: Long,
): String {
    return "strength-$routineId-$startedAtMillis"
}

internal fun appendStrengthSessionHistory(
    prefs: SharedPreferences,
    workout: CompletedStrengthSession,
) {
    val saved = prefs.getString(STRENGTH_SESSION_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    val nextHistory = JSONArray().apply {
        put(workout.toJsonObject())
        val maxPreviousItems = 99
        for (index in 0 until minOf(history.length(), maxPreviousItems)) {
            val existing = history.optJSONObject(index) ?: continue
            if (!existing.matchesStrengthSession(workout)) {
                put(existing)
            }
        }
    }
    prefs.edit().putString(STRENGTH_SESSION_HISTORY_PREF, nextHistory.toString()).apply()
}

internal fun replaceStrengthSessionHistory(
    prefs: SharedPreferences,
    workout: CompletedStrengthSession,
) {
    val saved = prefs.getString(STRENGTH_SESSION_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    var replaced = false
    val nextHistory = JSONArray().apply {
        for (index in 0 until history.length()) {
            val existing = history.optJSONObject(index).toCompletedStrengthSession()
            if (existing?.matchesStrengthSession(workout) == true) {
                put(workout.toJsonObject())
                replaced = true
            } else {
                put(history.optJSONObject(index) ?: continue)
            }
        }
        if (!replaced) {
            put(workout.toJsonObject())
        }
    }
    prefs.edit().putString(STRENGTH_SESSION_HISTORY_PREF, nextHistory.toString()).apply()
}

internal fun deleteStrengthSessionHistory(
    prefs: SharedPreferences,
    workout: CompletedStrengthSession,
) {
    val saved = prefs.getString(STRENGTH_SESSION_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    val nextHistory = JSONArray().apply {
        for (index in 0 until history.length()) {
            val existing = history.optJSONObject(index) ?: continue
            if (!existing.matchesStrengthSession(workout)) {
                put(existing)
            }
        }
    }
    prefs.edit().putString(STRENGTH_SESSION_HISTORY_PREF, nextHistory.toString()).apply()
}

internal fun loadCompletedStrengthSessionHistory(prefs: SharedPreferences): List<CompletedStrengthSession> {
    val saved = prefs.getString(STRENGTH_SESSION_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    return (0 until history.length()).mapNotNull { index ->
        history.optJSONObject(index).toCompletedStrengthSession()
    }
}

internal fun CompletedStrengthSession.toStrengthSession(): StrengthSession {
    return StrengthSession(
        name = routineName,
        startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault()),
        entries = entries,
        rpe = rpe,
        trainingLoad = trainingLoad,
        durationSeconds = durationSeconds,
        setEvents = setEvents,
        restEvents = restEvents
    )
}

private fun JSONObject?.toCompletedStrengthSession(): CompletedStrengthSession? {
    this ?: return null
    val routineSnapshot = optJSONObject("routineSnapshot")
    val snapshotRoutine = routineSnapshot?.let {
        JSONArray().put(it).toString().toStrengthWorkoutRoutines().firstOrNull()
    }
    val routineId = optNullableInt("routineId") ?: snapshotRoutine?.id ?: 0
    val routineName = optString("routineName").ifBlank { snapshotRoutine?.name ?: "웨이트 트레이닝" }
    val startedAtMillis = optLong("startedAtMillis", 0L)
    val endedAtMillis = optLong("endedAtMillis", startedAtMillis)
    if (startedAtMillis <= 0L) return null
    val entries = snapshotRoutine?.entries.orEmpty()
    val routineUpdateEntries = optJSONObject("routineUpdateSnapshot")?.let { updateSnapshot ->
        JSONArray().put(updateSnapshot).toString().toStrengthWorkoutRoutines().firstOrNull()?.entries
    }
    val rpe = optNullableInt("rpe") ?: 7
    return CompletedStrengthSession(
        id = optString("id").ifBlank { strengthSessionResultId(routineId, startedAtMillis) },
        routineId = routineId,
        routineName = routineName,
        startedAtMillis = startedAtMillis,
        endedAtMillis = endedAtMillis,
        durationSeconds = optNullableInt("durationSeconds")
            ?: ((endedAtMillis - startedAtMillis) / 1000L).toInt().coerceAtLeast(0),
        intervalsExternalId = optString("intervalsExternalId")
            .ifBlank { strengthIntervalsExternalId(startedAtMillis) },
        entries = entries,
        setEvents = optJSONArray("setEvents").toStrengthSetCompletionEvents(),
        restEvents = optJSONArray("restEvents").toStrengthRestEvents(),
        rpe = rpe,
        trainingLoad = optNullableInt("trainingLoad") ?: entries.strengthTrainingLoad(rpe),
        uploadedToIntervals = optBoolean("uploadedToIntervals", false),
        appliedToRoutine = optBoolean("appliedToRoutine", true),
        routineUpdateEntries = routineUpdateEntries
    )
}

private fun JSONObject.matchesStrengthSession(workout: CompletedStrengthSession): Boolean {
    if (optString("id") == workout.id) return true
    return toCompletedStrengthSession()?.matchesStrengthSession(workout) == true
}

private fun CompletedStrengthSession.matchesStrengthSession(workout: CompletedStrengthSession): Boolean {
    return id == workout.id ||
        (routineId == workout.routineId && startedAtMillis == workout.startedAtMillis)
}

private fun CompletedStrengthSession.toJsonObject(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("routineId", routineId)
        .put("routineName", routineName)
        .put("startedAtMillis", startedAtMillis)
        .put("endedAtMillis", endedAtMillis)
        .put("durationSeconds", durationSeconds)
        .put("intervalsExternalId", intervalsExternalId)
        .put("rpe", rpe)
        .put("trainingLoad", trainingLoad)
        .put("uploadedToIntervals", uploadedToIntervals)
        .put("appliedToRoutine", appliedToRoutine)
        .put(
            "routineUpdateSnapshot",
            routineUpdateEntries?.let { updateEntries ->
                JSONArray(
                    listOf(
                        StrengthWorkoutRoutine(
                            id = routineId,
                            name = routineName,
                            entries = updateEntries
                        )
                    ).toJsonString()
                ).optJSONObject(0)
            } ?: JSONObject.NULL
        )
        .put(
            "routineSnapshot",
            JSONArray(
                listOf(
                    StrengthWorkoutRoutine(
                        id = routineId,
                        name = routineName,
                        entries = entries
                    )
                ).toJsonString()
            ).optJSONObject(0) ?: JSONObject()
        )
        .put("setEvents", setEvents.toSetEventsJsonArray())
        .put("restEvents", restEvents.toRestEventsJsonArray())
}

private fun strengthIntervalsExternalId(startedAtMillis: Long): String {
    val startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault())
    return "intervals-gym-${startedAt.formatExternalIdTimestamp()}"
}

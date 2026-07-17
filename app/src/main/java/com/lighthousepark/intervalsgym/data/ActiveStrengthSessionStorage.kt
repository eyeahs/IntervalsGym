package com.lighthousepark.intervalsgym.data

import android.content.SharedPreferences
import com.lighthousepark.intervalsgym.app.ACTIVE_STRENGTH_SESSION_PREF
import com.lighthousepark.intervalsgym.core.optNullableInt
import com.lighthousepark.intervalsgym.strength.ActiveStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import org.json.JSONArray
import org.json.JSONObject

internal fun loadActiveStrengthSession(prefs: SharedPreferences): ActiveStrengthSession? {
    return prefs.getString(ACTIVE_STRENGTH_SESSION_PREF, null).toActiveStrengthSession()
}

internal fun saveActiveStrengthSession(
    prefs: SharedPreferences,
    session: ActiveStrengthSession?,
) {
    if (session == null) {
        prefs.edit().remove(ACTIVE_STRENGTH_SESSION_PREF).apply()
    } else {
        prefs.edit().putString(ACTIVE_STRENGTH_SESSION_PREF, session.toJsonString()).apply()
    }
}

internal fun ActiveStrengthSession.toJsonString(): String {
    val routineJson = activeStrengthSessionRoutineJson(entries)
    val routineBaselineJson = activeStrengthSessionRoutineJson(routineBaselineEntries)

    return JSONObject()
        .put("routine", routineJson)
        .put("routineBaseline", routineBaselineJson)
        .put("hasStarted", hasStarted)
        .put("sessionStartedAtMillis", sessionStartedAtMillis)
        .put("isSetScreenVisible", isSetScreenVisible)
        .put("currentExerciseIndex", currentExerciseIndex)
        .put("currentSetIndex", currentSetIndex)
        .put("pendingExerciseIndex", pendingExerciseIndex)
        .put("pendingSetIndex", pendingSetIndex)
        .put("restEndAtMillis", restEndAtMillis)
        .put("isRestSheetVisible", isRestSheetVisible)
        .put("restTitle", restTitle)
        .put("setEvents", setEvents.toSetEventsJsonArray())
        .put("restEvents", restEvents.toRestEventsJsonArray())
        .put("activeRestEventId", activeRestEventId)
        .toString()
}

private fun String?.toActiveStrengthSession(): ActiveStrengthSession? {
    if (isNullOrBlank()) return null
    return runCatching {
        val json = JSONObject(this)
        val routine = json.optJSONObject("routine").toStrengthWorkoutRoutine()
            ?: return@runCatching null
        val routineBaseline = json.optJSONObject("routineBaseline").toStrengthWorkoutRoutine()
            ?: routine
        val restEndAtMillis = json.optLong("restEndAtMillis", 0L)
        val isExpiredRest = restEndAtMillis > 0L && restEndAtMillis <= System.currentTimeMillis()
        val restEvents = json.optJSONArray("restEvents").toStrengthRestEvents()
        val activeRestEventId = json.optNullableInt("activeRestEventId")

        ActiveStrengthSession(
            routineId = routine.id,
            routineName = routine.name,
            entries = routine.entries,
            hasStarted = json.optBoolean("hasStarted", false),
            sessionStartedAtMillis = json.optLong("sessionStartedAtMillis", 0L).takeIf { it > 0L }
                ?: if (json.optBoolean("hasStarted", false)) System.currentTimeMillis() else 0L,
            isSetScreenVisible = json.optBoolean("isSetScreenVisible", false),
            currentExerciseIndex = if (isExpiredRest) {
                json.optNullableInt("pendingExerciseIndex") ?: json.optNullableInt("currentExerciseIndex") ?: 0
            } else {
                json.optNullableInt("currentExerciseIndex") ?: 0
            },
            currentSetIndex = if (isExpiredRest) {
                json.optNullableInt("pendingSetIndex") ?: json.optNullableInt("currentSetIndex") ?: 0
            } else {
                json.optNullableInt("currentSetIndex") ?: 0
            },
            pendingExerciseIndex = if (isExpiredRest) null else json.optNullableInt("pendingExerciseIndex"),
            pendingSetIndex = if (isExpiredRest) null else json.optNullableInt("pendingSetIndex"),
            restEndAtMillis = if (isExpiredRest) 0L else restEndAtMillis,
            isRestSheetVisible = !isExpiredRest && json.optBoolean("isRestSheetVisible", false),
            restTitle = if (isExpiredRest) "" else json.optString("restTitle"),
            setEvents = json.optJSONArray("setEvents").toStrengthSetCompletionEvents(),
            restEvents = if (isExpiredRest && activeRestEventId != null) {
                finalizeRestEvents(restEvents, activeRestEventId, restEndAtMillis, "finished")
            } else {
                restEvents
            },
            activeRestEventId = if (isExpiredRest) null else activeRestEventId,
            routineBaselineEntries = routineBaseline.entries,
            routineLocation = routine.location
        )
    }.getOrNull()
}

private fun ActiveStrengthSession.activeStrengthSessionRoutineJson(
    routineEntries: List<StrengthRoutineEntry>,
): JSONObject {
    return JSONArray(
        listOf(
            StrengthWorkoutRoutine(
                id = routineId,
                name = routineName,
                entries = routineEntries,
                location = routineLocation
            )
        ).toJsonString()
    ).optJSONObject(0) ?: JSONObject()
}

private fun JSONObject?.toStrengthWorkoutRoutine(): StrengthWorkoutRoutine? {
    return this?.let { routineJson ->
        JSONArray()
            .put(routineJson)
            .toString()
            .toStrengthWorkoutRoutines()
            .firstOrNull()
    }
}

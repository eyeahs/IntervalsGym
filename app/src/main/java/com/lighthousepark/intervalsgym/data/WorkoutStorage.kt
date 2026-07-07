package com.lighthousepark.intervalsgym.data

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

import android.content.SharedPreferences
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import org.json.JSONArray
import org.json.JSONObject

internal fun loadStrengthRoutines(prefs: SharedPreferences): List<StrengthWorkoutRoutine> {
    val saved = prefs.getString(STRENGTH_ROUTINES_PREF, null)
    return saved.toStrengthWorkoutRoutines().takeIf { it.isNotEmpty() } ?: defaultStrengthRoutines()
}

internal fun loadScheduledStrengthRoutines(prefs: SharedPreferences): List<ScheduledStrengthRoutine> {
    val saved = prefs.getString(SCHEDULED_STRENGTH_ROUTINES_PREF, null)
    return runCatching {
        val array = JSONArray(saved ?: "[]")
        (0 until array.length()).mapNotNull { index ->
            val json = array.optJSONObject(index) ?: return@mapNotNull null
            val date = runCatching { LocalDate.parse(json.optString("date")) }.getOrNull()
                ?: return@mapNotNull null
            val routine = json.optString("routineJson")
                .toStrengthWorkoutRoutines()
                .firstOrNull()
                ?: return@mapNotNull null
            val externalId = json.optString("externalId")
                .ifBlank { routine.intervalsRoutineExternalId(date) }
            ScheduledStrengthRoutine(
                id = json.optString("id").ifBlank { routine.scheduledStrengthRoutineId(date) },
                date = date,
                routine = routine,
                uploadedToIntervals = json.optBoolean("uploadedToIntervals", false),
                externalId = externalId
            )
        }
    }.getOrElse { emptyList() }
}

internal fun upsertScheduledStrengthRoutine(
    prefs: SharedPreferences,
    scheduledRoutine: ScheduledStrengthRoutine,
) {
    val nextRoutines = (listOf(scheduledRoutine) + loadScheduledStrengthRoutines(prefs))
        .distinctBy { it.externalId }
    saveScheduledStrengthRoutines(prefs, nextRoutines)
}

internal fun removeScheduledStrengthRoutine(
    prefs: SharedPreferences,
    routine: TrainingItem,
) {
    val nextRoutines = loadScheduledStrengthRoutines(prefs).filterNot { scheduled -> scheduled.matchesTrainingItem(routine) }
    saveScheduledStrengthRoutines(prefs, nextRoutines)
}

internal fun moveScheduledStrengthRoutine(
    prefs: SharedPreferences,
    routine: TrainingItem,
    targetDate: LocalDate,
): ScheduledStrengthRoutine? {
    val result = loadScheduledStrengthRoutines(prefs).withMovedScheduledStrengthRoutine(routine, targetDate)
    if (result.movedRoutine != null) {
        saveScheduledStrengthRoutines(prefs, result.routines)
    }
    return result.movedRoutine
}

internal data class ScheduledStrengthRoutineMoveResult(
    val routines: List<ScheduledStrengthRoutine>,
    val movedRoutine: ScheduledStrengthRoutine?,
)

internal fun List<ScheduledStrengthRoutine>.withMovedScheduledStrengthRoutine(
    routine: TrainingItem,
    targetDate: LocalDate,
): ScheduledStrengthRoutineMoveResult {
    val sourceRoutine = firstOrNull { scheduled -> scheduled.matchesTrainingItem(routine) }
        ?: return ScheduledStrengthRoutineMoveResult(routines = this, movedRoutine = null)
    val movedRoutine = sourceRoutine.copy(
        id = sourceRoutine.routine.scheduledStrengthRoutineId(targetDate),
        date = targetDate,
        uploadedToIntervals = false,
        externalId = sourceRoutine.routine.intervalsRoutineExternalId(targetDate)
    )
    val nextRoutines = (listOf(movedRoutine) + filterNot { scheduled ->
        scheduled.matchesTrainingItem(routine) || scheduled.externalId == movedRoutine.externalId
    }).distinctBy { it.externalId }
    return ScheduledStrengthRoutineMoveResult(routines = nextRoutines, movedRoutine = movedRoutine)
}

private fun ScheduledStrengthRoutine.matchesTrainingItem(routine: TrainingItem): Boolean {
    return id == routine.remoteId ||
        id == routine.id.removePrefix("local-") ||
        externalId == routine.externalId
}

private fun saveScheduledStrengthRoutines(
    prefs: SharedPreferences,
    routines: List<ScheduledStrengthRoutine>,
) {
    val array = JSONArray().also { root ->
        routines.forEach { item ->
            root.put(
                JSONObject()
                    .put("id", item.id)
                    .put("date", item.date.toString())
                    .put("externalId", item.externalId)
                    .put("uploadedToIntervals", item.uploadedToIntervals)
                    .put("routineJson", listOf(item.routine).toJsonString())
            )
        }
    }
    prefs.edit().putString(SCHEDULED_STRENGTH_ROUTINES_PREF, array.toString()).apply()
}

internal fun List<TrainingItem>.withLocalStrengthRoutines(
    scheduledRoutines: List<ScheduledStrengthRoutine>,
    localRoutines: List<StrengthWorkoutRoutine> = emptyList(),
    start: LocalDate,
    end: LocalDate,
): List<TrainingItem> {
    val scheduledByExternalId = scheduledRoutines.associateBy { it.externalId }
    val localByRoutineId = localRoutines.associateBy { it.id }
    val matchedRemoteItems = map { item ->
        val matchedByDescriptionId = item.description.toIntervalsGymStrengthRoutineId()
            ?.let { localByRoutineId[it] }
        val matchedByExternalId = item.externalId
            ?.let { scheduledByExternalId[it] }
            ?.routine
            ?.let { scheduledRoutine -> localByRoutineId[scheduledRoutine.id] ?: scheduledRoutine }
        val matchedRoutine = matchedByDescriptionId
            ?: item.matchedStrengthRoutine
            ?: matchedByExternalId
        if (matchedRoutine == null || matchedRoutine == item.matchedStrengthRoutine) {
            item
        } else {
            item.copy(matchedStrengthRoutine = matchedRoutine)
        }
    }
    val remoteExternalIds = mapNotNull { it.externalId }.toSet()
    val localItems = scheduledRoutines
        .filter { scheduled -> !scheduled.date.isBefore(start) && !scheduled.date.isAfter(end) }
        .filterNot { scheduled -> scheduled.externalId in remoteExternalIds }
        .map { scheduled -> scheduled.toTrainingItem(localByRoutineId[scheduled.routine.id]) }
    return matchedRemoteItems + localItems
}

private fun ScheduledStrengthRoutine.toTrainingItem(localRoutine: StrengthWorkoutRoutine? = null): TrainingItem {
    val displayRoutine = localRoutine ?: routine
    return TrainingItem(
        id = "local-${id}",
        remoteId = id,
        externalId = externalId,
        name = displayRoutine.name,
        type = "Weight Training",
        date = date,
        startedAt = date.atStartOfDay(),
        timeLabel = "Routine",
        durationSeconds = displayRoutine.entries.totalDurationSeconds().takeIf { it > 0 },
        distanceMeters = null,
        weightLiftedKg = null,
        load = null,
        fitness = null,
        fatigue = null,
        form = null,
        description = displayRoutine.toIntervalsRoutineDescription(),
        blocks = emptyList(),
        isRoutine = true,
        matchedStrengthRoutine = displayRoutine
    )
}

internal fun List<StrengthWorkoutRoutine>.withLatestCompletedSession(
    history: List<CompletedStrengthSession>,
): List<StrengthWorkoutRoutine> {
    if (isEmpty() || history.isEmpty()) return this
    val latestByRoutineId = history
        .filter { it.appliedToRoutine && it.routineId != 0 && it.entries.isNotEmpty() }
        .groupBy { it.routineId }
        .mapValues { (_, workouts) -> workouts.maxByOrNull { it.startedAtMillis } }

    return map { routine ->
        val latestWorkout = latestByRoutineId[routine.id] ?: return@map routine
        routine.copy(entries = latestWorkout.entries.map { it.copyForWorkout() })
    }
}

internal fun ActiveStrengthSession.withLatestCompletedSession(
    history: List<CompletedStrengthSession>,
): ActiveStrengthSession {
    if (hasStarted || history.isEmpty()) return this
    val latestWorkout = history
        .filter { it.appliedToRoutine && it.routineId == routineId && it.entries.isNotEmpty() }
        .maxByOrNull { it.startedAtMillis }
        ?: return this
    return copy(entries = latestWorkout.entries.map { it.copyForWorkout() })
}

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

internal fun StrengthWorkoutRoutine.intervalsRoutineExternalId(date: LocalDate): String {
    return "intervals-gym-strength-routine-${id}-${date}"
}

internal fun StrengthWorkoutRoutine.scheduledStrengthRoutineId(date: LocalDate): String {
    return "scheduled-strength-routine-${id}-${date}"
}

internal fun loadActiveStrengthSession(prefs: SharedPreferences): ActiveStrengthSession? {
    return prefs.getString(ACTIVE_STRENGTH_SESSION_PREF, null).toActiveStrengthSession()
}

internal fun ActiveStrengthSession.toJsonString(): String {
    val routineJson = JSONArray(
        listOf(
            StrengthWorkoutRoutine(
                id = routineId,
                name = routineName,
                entries = entries
            )
        ).toJsonString()
    ).optJSONObject(0) ?: JSONObject()

    return JSONObject()
        .put("routine", routineJson)
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
        val routineJson = json.optJSONObject("routine") ?: return@runCatching null
        val routine = JSONArray()
            .put(routineJson)
            .toString()
            .toStrengthWorkoutRoutines()
            .firstOrNull() ?: return@runCatching null
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
            activeRestEventId = if (isExpiredRest) null else activeRestEventId
        )
    }.getOrNull()
}

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
): CompletedStrengthSession {
    val safeStartedAt = startedAtMillis.takeIf { it > 0L } ?: endedAtMillis
    return CompletedStrengthSession(
        id = "strength-${safeStartedAt}-${endedAtMillis}",
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
        appliedToRoutine = appliedToRoutine
    )
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
            if (existing.optString("id") != workout.id) {
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
            if (existing?.id == workout.id) {
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

internal fun loadCompletedStrengthSessionHistory(prefs: SharedPreferences): List<CompletedStrengthSession> {
    val saved = prefs.getString(STRENGTH_SESSION_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    return (0 until history.length()).mapNotNull { index ->
        history.optJSONObject(index).toCompletedStrengthSession()
    }
}

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

internal fun loadCompletedRunningSessionHistory(prefs: SharedPreferences): List<CompletedRunningSession> {
    val saved = prefs.getString(RUNNING_SESSION_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    return (0 until history.length()).mapNotNull { index ->
        history.optJSONObject(index).toCompletedRunningSession()
    }
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
    val rpe = optNullableInt("rpe") ?: 7
    return CompletedStrengthSession(
        id = optString("id").ifBlank { "strength-$startedAtMillis-$endedAtMillis" },
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
        appliedToRoutine = optBoolean("appliedToRoutine", true)
    )
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

private fun List<TrainingItem>.withMatchedStrengthSessions(
    history: List<CompletedStrengthSession>,
): List<TrainingItem> {
    if (history.isEmpty()) return this
    return map { item ->
        if (item.isRoutine) {
            item
        } else {
            item.copy(matchedStrengthSession = item.matchStrengthSession(history))
        }
    }
}

internal fun List<TrainingItem>.withLocalStrengthResults(
    history: List<CompletedStrengthSession>,
    weekStart: LocalDate,
    weekEnd: LocalDate,
): List<TrainingItem> {
    val matched = withMatchedStrengthSessions(history)
    val matchedWorkoutIds = matched.mapNotNull { it.matchedStrengthSession?.id }.toSet()
    val localOnlyItems = history
        .filter { workout ->
            val date = workout.startedLocalDate()
            date in weekStart..weekEnd && workout.id !in matchedWorkoutIds
        }
        .map { workout -> workout.toLocalTrainingItem() }
    return matched + localOnlyItems
}

private fun CompletedStrengthSession.startedLocalDate(): LocalDate {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault()).toLocalDate()
}

private fun CompletedStrengthSession.toLocalTrainingItem(): TrainingItem {
    val startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault())
    return TrainingItem(
        id = "local-strength-$id",
        remoteId = id,
        externalId = intervalsExternalId,
        name = routineName,
        type = "Weight Training",
        date = startedAt.toLocalDate(),
        startedAt = startedAt,
        timeLabel = startedAt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
        durationSeconds = durationSeconds,
        distanceMeters = null,
        weightLiftedKg = entries.totalVolumeKg(),
        load = trainingLoad,
        fitness = null,
        fatigue = null,
        form = null,
        description = if (uploadedToIntervals) {
            "로컬 웨이트 기록 · Intervals.icu에서 삭제되었을 수 있습니다."
        } else {
            "로컬 웨이트 기록 · Intervals.icu 미동기화"
        },
        blocks = emptyList(),
        isRoutine = false,
        matchedStrengthSession = this,
        isLocalOnlyStrengthResult = true
    )
}

internal fun CompletedStrengthSession.toStrengthSession(): StrengthSession {
    return StrengthSession(
        name = routineName,
        startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault()),
        entries = entries,
        rpe = rpe,
        trainingLoad = trainingLoad
    )
}

internal fun List<TrainingItem>.withLocalRunningResults(
    history: List<CompletedRunningSession>,
    weekStart: LocalDate,
    weekEnd: LocalDate,
): List<TrainingItem> {
    if (history.isEmpty()) return this
    val localOnlyItems = history
        .filter { workout ->
            val date = workout.startedLocalDate()
            date in weekStart..weekEnd && none { item -> item.matchesRunningSession(workout) }
        }
        .map { workout -> workout.toLocalTrainingItem() }
    return this + localOnlyItems
}

private fun CompletedRunningSession.startedLocalDate(): LocalDate {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault()).toLocalDate()
}

private fun CompletedRunningSession.toLocalTrainingItem(): TrainingItem {
    val startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault())
    return TrainingItem(
        id = "local-running-$id",
        remoteId = id,
        externalId = id,
        name = name,
        type = "Run",
        date = startedAt.toLocalDate(),
        startedAt = startedAt,
        timeLabel = startedAt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
        durationSeconds = durationSeconds,
        distanceMeters = estimatedDistanceMeters,
        weightLiftedKg = null,
        load = null,
        fitness = null,
        fatigue = null,
        form = null,
        description = if (uploadedToIntervals) {
            "로컬 러닝 기록 · Intervals.icu 업로드됨"
        } else {
            "로컬 러닝 기록"
        },
        blocks = blocks,
        isRoutine = false,
        isLocalOnlyRunningResult = true,
        actualRunningBlocks = actualBlocks,
        actualRunningRoutePoints = routePoints
    )
}

private fun TrainingItem.matchesRunningSession(workout: CompletedRunningSession): Boolean {
    if (isLocalOnlyRunningResult) {
        return remoteId == workout.id || id == "local-running-${workout.id}"
    }
    if (isRoutine || sportType() != TrainingSportType.RUNNING) return false
    val startedMillis = startedAt
        ?.atZone(ZoneId.systemDefault())
        ?.toInstant()
        ?.toEpochMilli()
        ?: return false
    val timeDiff = abs(startedMillis - workout.startedAtMillis)
    val durationDiff = durationSeconds?.let { abs(it - workout.durationSeconds) } ?: Int.MAX_VALUE
    return timeDiff <= 10 * 60 * 1000L || durationDiff <= 5 * 60
}

private fun TrainingItem.matchStrengthSession(
    history: List<CompletedStrengthSession>,
): CompletedStrengthSession? {
    externalId?.let { id ->
        history.firstOrNull { it.intervalsExternalId == id }?.let { return it }
    }
    val startedMillis = startedAt
        ?.atZone(ZoneId.systemDefault())
        ?.toInstant()
        ?.toEpochMilli()
        ?: return null
    val looksLikeStrength = name.contains("웨이트", ignoreCase = true) ||
        name.contains("strength", ignoreCase = true) ||
        description.orEmpty().contains("IntervalsGym 웨이트", ignoreCase = true)
    return history
        .filter { workout ->
            abs(workout.startedAtMillis - startedMillis) <= 2 * 60 * 1000L
        }
        .filter { workout ->
            looksLikeStrength ||
                workout.routineName.equals(name, ignoreCase = true) ||
                name.contains(workout.routineName, ignoreCase = true)
        }
        .minByOrNull { abs(it.startedAtMillis - startedMillis) }
}

private fun strengthIntervalsExternalId(startedAtMillis: Long): String {
    val startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault())
    return "intervals-gym-${startedAt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))}"
}

internal fun finalizeRestEvents(
    restEvents: List<StrengthRestEvent>,
    activeRestEventId: Int?,
    endedAtMillis: Long,
    reason: String,
): List<StrengthRestEvent> {
    if (activeRestEventId == null) return restEvents
    return restEvents.map { event ->
        if (event.id == activeRestEventId && event.endedAtMillis == null) {
            event.copy(
                endedAtMillis = endedAtMillis,
                endReason = reason
            )
        } else {
            event
        }
    }
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

private fun List<StrengthSetCompletionEvent>.toSetEventsJsonArray(): JSONArray {
    return JSONArray().also { array ->
        forEach { event ->
            array.put(
                JSONObject()
                    .put("sequence", event.sequence)
                    .put("exerciseEntryId", event.exerciseEntryId)
                    .put("exerciseTitle", event.exerciseTitle)
                    .put("exerciseGroup", event.exerciseGroup)
                    .put("exerciseId", event.exerciseId)
                    .put("equipment", event.equipment)
                    .put("variation", event.variation)
                    .put("setRecordId", event.setRecordId)
                    .put("setIndex", event.setIndex)
                    .put("weightKg", event.weightKg)
                    .put("reps", event.reps)
                    .put("targetRestSeconds", event.targetRestSeconds)
                    .put("completedAtMillis", event.completedAtMillis)
            )
        }
    }
}

private fun JSONArray?.toStrengthSetCompletionEvents(): List<StrengthSetCompletionEvent> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { index ->
        val json = optJSONObject(index) ?: return@mapNotNull null
        StrengthSetCompletionEvent(
            sequence = json.optNullableInt("sequence") ?: (index + 1),
            exerciseEntryId = json.optNullableInt("exerciseEntryId") ?: 0,
            exerciseTitle = json.optString("exerciseTitle"),
            exerciseGroup = json.optString("exerciseGroup"),
            exerciseId = json.optString("exerciseId"),
            equipment = json.optString("equipment"),
            variation = json.optString("variation"),
            setRecordId = json.optNullableInt("setRecordId") ?: 0,
            setIndex = json.optNullableInt("setIndex") ?: 0,
            weightKg = json.optString("weightKg"),
            reps = json.optString("reps"),
            targetRestSeconds = json.optNullableInt("targetRestSeconds") ?: 0,
            completedAtMillis = json.optLong("completedAtMillis", 0L)
        )
    }
}

private fun List<StrengthRestEvent>.toRestEventsJsonArray(): JSONArray {
    return JSONArray().also { array ->
        forEach { event ->
            array.put(
                JSONObject()
                    .put("id", event.id)
                    .put("afterSetSequence", event.afterSetSequence)
                    .put("exerciseEntryId", event.exerciseEntryId)
                    .put("exerciseTitle", event.exerciseTitle)
                    .put("setRecordId", event.setRecordId)
                    .put("setIndex", event.setIndex)
                    .put("startedAtMillis", event.startedAtMillis)
                    .put("plannedSeconds", event.plannedSeconds)
                    .put("targetEndAtMillis", event.targetEndAtMillis)
                    .put("endedAtMillis", event.endedAtMillis ?: JSONObject.NULL)
                    .put("actualSeconds", event.actualSeconds)
                    .put("endReason", event.endReason ?: JSONObject.NULL)
            )
        }
    }
}

private fun JSONArray?.toStrengthRestEvents(): List<StrengthRestEvent> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { index ->
        val json = optJSONObject(index) ?: return@mapNotNull null
        StrengthRestEvent(
            id = json.optNullableInt("id") ?: (index + 1),
            afterSetSequence = json.optNullableInt("afterSetSequence") ?: 0,
            exerciseEntryId = json.optNullableInt("exerciseEntryId") ?: 0,
            exerciseTitle = json.optString("exerciseTitle"),
            setRecordId = json.optNullableInt("setRecordId") ?: 0,
            setIndex = json.optNullableInt("setIndex") ?: 0,
            startedAtMillis = json.optLong("startedAtMillis", 0L),
            plannedSeconds = json.optNullableInt("plannedSeconds") ?: 0,
            targetEndAtMillis = json.optLong("targetEndAtMillis", 0L),
            endedAtMillis = json.optNullableLong("endedAtMillis"),
            endReason = json.optString("endReason").takeIf { it.isNotBlank() }
        )
    }
}

private fun List<String>.toStringJsonArray(): JSONArray {
    return JSONArray().also { array ->
        forEach { value -> array.put(value) }
    }
}

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { index ->
        optString(index).takeIf { it.isNotBlank() }
    }
}

internal fun String?.cleanJsonText(): String? {
    return this
        ?.trim()
        ?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
}

private fun List<String>.withPreferredOption(option: String): List<String> {
    val safeOption = option.takeIf { it.isNotBlank() } ?: return ifEmpty { listOf("기본") }
    return if (contains(safeOption)) this else listOf(safeOption) + this
}

private fun JSONObject.toStrengthExercise(): StrengthExercise {
    val exerciseId = optString("exerciseId")
    strengthExerciseCatalog.firstOrNull { it.id == exerciseId }?.let { return it }

    val nameKo = optString("exerciseNameKo")
        .ifBlank { optString("exerciseNameEn") }
        .ifBlank { optString("exerciseName") }
        .ifBlank { "사용자 운동" }
    val group = optString("exerciseGroup").ifBlank { "사용자 추가" }
    val isCustomExercise = group == "사용자 추가" || exerciseId.startsWith("custom_")
    val equipment = optString("equipment")
    val variation = optString("variation")
    val savedEquipmentOptions = optJSONArray("equipmentOptions")
        .toStringList()
    val equipmentOptions = if (isCustomExercise && (savedEquipmentOptions.isEmpty() || savedEquipmentOptions == listOf("기본"))) {
        CUSTOM_STRENGTH_EQUIPMENT_OPTIONS
    } else {
        savedEquipmentOptions.ifEmpty { listOf(equipment.ifBlank { "기본" }) }
    }
        .distinct()
        .withPreferredOption(equipment)
    val variationOptions = optJSONArray("variationOptions")
        .toStringList()
        .ifEmpty { listOf(variation.ifBlank { "기본" }) }
        .distinct()
        .withPreferredOption(variation)

    return StrengthExercise(
        id = exerciseId.ifBlank { customStrengthExercise(nameKo).id },
        nameKo = nameKo,
        nameEn = optString("exerciseNameEn").ifBlank { nameKo },
        group = group,
        equipmentOptions = equipmentOptions,
        variationOptions = variationOptions
    )
}

internal fun List<StrengthWorkoutRoutine>.toJsonString(): String {
    return JSONArray().also { routinesArray ->
        forEach { routine ->
            routinesArray.put(
                JSONObject()
                    .put("id", routine.id)
                    .put("name", routine.name)
                    .put(
                        "entries",
                        JSONArray().also { entriesArray ->
                            routine.entries.forEach { entry ->
                                entriesArray.put(
                                    JSONObject()
                                        .put("id", entry.id)
                                        .put("exerciseId", entry.exercise.id)
                                        .put("exerciseNameKo", entry.exercise.nameKo)
                                        .put("exerciseNameEn", entry.exercise.nameEn)
                                        .put("exerciseGroup", entry.exercise.group)
                                        .put("equipmentOptions", entry.exercise.equipmentOptions.toStringJsonArray())
                                        .put("variationOptions", entry.exercise.variationOptions.toStringJsonArray())
                                        .put("equipment", entry.equipment)
                                        .put("variation", entry.variation)
                                        .put("supersetGroupId", entry.supersetGroupId ?: JSONObject.NULL)
                                        .put("targetSets", entry.targetSets)
                                        .put("targetReps", entry.targetReps)
                                        .put("restSeconds", entry.restSeconds)
                                        .put("targetWeightKg", entry.targetWeightKg)
                                        .put("note", entry.note)
                                        .put(
                                            "records",
                                            JSONArray().also { recordsArray ->
                                                entry.records.forEach { record ->
                                                    recordsArray.put(
                                                        JSONObject()
                                                            .put("id", record.id)
                                                            .put("weightKg", record.weightKg)
                                                            .put("reps", record.reps)
                                                            .put("leftWeightKg", record.leftWeightKg)
                                                            .put("leftReps", record.leftReps)
                                                            .put("rightWeightKg", record.rightWeightKg)
                                                            .put("rightReps", record.rightReps)
                                                            .put("durationSeconds", record.durationSeconds)
                                                            .put("restSeconds", record.restSeconds)
                                                            .put("completed", record.completed)
                                                    )
                                                }
                                            }
                                        )
                                )
                            }
                        }
                    )
            )
        }
    }.toString()
}

internal fun String?.toStrengthWorkoutRoutines(): List<StrengthWorkoutRoutine> {
    if (isNullOrBlank()) return emptyList()
    return runCatching {
        val routinesArray = JSONArray(this)
        (0 until routinesArray.length()).mapNotNull { routineIndex ->
            val routineJson = routinesArray.optJSONObject(routineIndex) ?: return@mapNotNull null
            val entriesArray = routineJson.optJSONArray("entries") ?: JSONArray()
            val entries = (0 until entriesArray.length()).mapNotNull { entryIndex ->
                val entryJson = entriesArray.optJSONObject(entryIndex) ?: return@mapNotNull null
                val parsedExercise = entryJson.toStrengthExercise()
                val savedVariation = entryJson.optString("variation")
                val shouldMigrateHackSquat = parsedExercise.id == "squat" && savedVariation == "핵 스쿼트"
                val exercise = if (shouldMigrateHackSquat) {
                    strengthExerciseCatalog.firstOrNull { it.id == "hack_squat" } ?: parsedExercise
                } else {
                    parsedExercise
                }
                val recordsArray = entryJson.optJSONArray("records") ?: JSONArray()
                val records = (0 until recordsArray.length()).mapNotNull { recordIndex ->
                    val recordJson = recordsArray.optJSONObject(recordIndex) ?: return@mapNotNull null
                    StrengthSetRecord(
                        id = recordJson.optNullableInt("id") ?: (recordIndex + 1),
                        weightKg = recordJson.optString("weightKg"),
                        reps = recordJson.optString("reps"),
                        leftWeightKg = recordJson.optString("leftWeightKg").ifBlank { recordJson.optString("weightKg") },
                        leftReps = recordJson.optString("leftReps").ifBlank { recordJson.optString("reps") },
                        rightWeightKg = recordJson.optString("rightWeightKg").ifBlank { recordJson.optString("weightKg") },
                        rightReps = recordJson.optString("rightReps").ifBlank { recordJson.optString("reps") },
                        durationSeconds = recordJson.optString("durationSeconds"),
                        restSeconds = recordJson.optString("restSeconds"),
                        completed = recordJson.optBoolean("completed", false)
                    )
                }.ifEmpty {
                    listOf(
                        StrengthSetRecord(
                            id = 1,
                            weightKg = entryJson.optString("targetWeightKg"),
                            reps = entryJson.optNullableInt("targetReps")?.takeIf { it > 0 }?.toString().orEmpty(),
                            leftWeightKg = entryJson.optString("targetWeightKg"),
                            leftReps = entryJson.optNullableInt("targetReps")?.takeIf { it > 0 }?.toString().orEmpty(),
                            rightWeightKg = entryJson.optString("targetWeightKg"),
                            rightReps = entryJson.optNullableInt("targetReps")?.takeIf { it > 0 }?.toString().orEmpty(),
                            durationSeconds = "",
                            restSeconds = entryJson.optNullableInt("restSeconds")?.takeIf { it > 0 }?.toString().orEmpty(),
                            completed = false
                        )
                    )
                }
                StrengthRoutineEntry(
                    id = entryJson.optNullableInt("id") ?: (entryIndex + 1),
                    exercise = exercise,
                    equipment = if (shouldMigrateHackSquat) {
                        "머신"
                    } else if (entryJson.has("equipment")) {
                        entryJson.optString("equipment")
                    } else {
                        exercise.equipmentOptions.first()
                    },
                    variation = if (shouldMigrateHackSquat) {
                        "기본"
                    } else {
                        savedVariation.ifBlank { exercise.variationOptions.first() }
                    },
                    supersetGroupId = entryJson.optNullableInt("supersetGroupId"),
                    targetSets = entryJson.optNullableInt("targetSets") ?: records.size,
                    targetReps = entryJson.optNullableInt("targetReps") ?: records.firstOrNull()?.reps?.toIntOrNull() ?: 0,
                    restSeconds = entryJson.optNullableInt("restSeconds") ?: records.firstOrNull()?.restSeconds?.toIntOrNull() ?: 0,
                    targetWeightKg = entryJson.optString("targetWeightKg"),
                    note = entryJson.optString("note"),
                    records = records
                )
            }
            StrengthWorkoutRoutine(
                id = routineJson.optNullableInt("id") ?: (routineIndex + 1),
                name = routineJson.optString("name").ifBlank { "웨이트 Routine" },
                entries = entries
            )
        }
    }.getOrDefault(emptyList())
}

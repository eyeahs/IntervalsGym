package com.lighthousepark.intervalsgym.data

import android.content.SharedPreferences
import com.lighthousepark.intervalsgym.app.SCHEDULED_STRENGTH_ROUTINES_PREF
import com.lighthousepark.intervalsgym.core.ROUTINE_TIME_LABEL
import com.lighthousepark.intervalsgym.core.formatClockTime
import com.lighthousepark.intervalsgym.core.formatCompactClockTime
import com.lighthousepark.intervalsgym.core.toClockTimeOrNull
import com.lighthousepark.intervalsgym.strength.ScheduledStrengthRoutine
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.totalDurationSeconds
import com.lighthousepark.intervalsgym.training.TrainingItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local calendar storage for strength routines.
 *
 * Keep scheduled routine persistence, local ids, and Intervals external ids together
 * so calendar code does not need to rebuild these rules by hand.
 */
internal fun loadScheduledStrengthRoutines(prefs: SharedPreferences): List<ScheduledStrengthRoutine> {
    val saved = prefs.getString(SCHEDULED_STRENGTH_ROUTINES_PREF, null)
    return runCatching {
        val array = JSONArray(saved ?: "[]")
        (0 until array.length()).mapNotNull { index ->
            val json = array.optJSONObject(index) ?: return@mapNotNull null
            val date = runCatching { LocalDate.parse(json.optString(ScheduledStrengthRoutineJson.DATE)) }.getOrNull()
                ?: return@mapNotNull null
            val time = json.optString(ScheduledStrengthRoutineJson.TIME).toClockTimeOrNull()
            val routine = json.optString(ScheduledStrengthRoutineJson.ROUTINE_JSON)
                .toStrengthWorkoutRoutines()
                .firstOrNull()
                ?: return@mapNotNull null
            val externalId = json.optString(ScheduledStrengthRoutineJson.EXTERNAL_ID)
                .ifBlank { routine.intervalsRoutineExternalId(date, time) }
            ScheduledStrengthRoutine(
                id = json.optString(ScheduledStrengthRoutineJson.ID)
                    .ifBlank { routine.scheduledStrengthRoutineId(date, time) },
                date = date,
                time = time,
                routine = routine,
                uploadedToIntervals = json.optBoolean(ScheduledStrengthRoutineJson.UPLOADED_TO_INTERVALS, false),
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
        id = sourceRoutine.routine.scheduledStrengthRoutineId(targetDate, sourceRoutine.time),
        date = targetDate,
        uploadedToIntervals = false,
        externalId = sourceRoutine.routine.intervalsRoutineExternalId(targetDate, sourceRoutine.time)
    )
    val nextRoutines = (listOf(movedRoutine) + filterNot { scheduled ->
        scheduled.matchesTrainingItem(routine) || scheduled.externalId == movedRoutine.externalId
    }).distinctBy { it.externalId }
    return ScheduledStrengthRoutineMoveResult(routines = nextRoutines, movedRoutine = movedRoutine)
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

internal fun StrengthWorkoutRoutine.intervalsRoutineExternalId(date: LocalDate, time: LocalTime? = null): String {
    return "intervals-gym-strength-routine-${id}-${date}${time?.let { "-${it.formatCompactClockTime()}" }.orEmpty()}"
}

internal fun StrengthWorkoutRoutine.scheduledStrengthRoutineId(date: LocalDate, time: LocalTime? = null): String {
    return "scheduled-strength-routine-${id}-${date}${time?.let { "-${it.formatCompactClockTime()}" }.orEmpty()}"
}

private fun ScheduledStrengthRoutine.matchesTrainingItem(routine: TrainingItem): Boolean {
    return id == routine.remoteId ||
        id == routine.id.removePrefix(LOCAL_TRAINING_ITEM_ID_PREFIX) ||
        externalId == routine.externalId
}

private fun ScheduledStrengthRoutine.toTrainingItem(localRoutine: StrengthWorkoutRoutine? = null): TrainingItem {
    val displayRoutine = localRoutine ?: routine
    return TrainingItem(
        id = "$LOCAL_TRAINING_ITEM_ID_PREFIX$id",
        remoteId = id,
        externalId = externalId,
        name = displayRoutine.name,
        type = STRENGTH_TRAINING_TYPE,
        date = date,
        startedAt = LocalDateTime.of(date, time ?: LocalTime.MIDNIGHT),
        timeLabel = time?.formatClockTime() ?: ROUTINE_TIME_LABEL,
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

private fun saveScheduledStrengthRoutines(
    prefs: SharedPreferences,
    routines: List<ScheduledStrengthRoutine>,
) {
    val array = JSONArray().also { root ->
        routines.forEach { item ->
            root.put(item.toJsonObject())
        }
    }
    prefs.edit().putString(SCHEDULED_STRENGTH_ROUTINES_PREF, array.toString()).apply()
}

private fun ScheduledStrengthRoutine.toJsonObject(): JSONObject {
    return JSONObject()
        .put(ScheduledStrengthRoutineJson.ID, id)
        .put(ScheduledStrengthRoutineJson.DATE, date.toString())
        .put(ScheduledStrengthRoutineJson.TIME, time?.formatClockTime() ?: JSONObject.NULL)
        .put(ScheduledStrengthRoutineJson.EXTERNAL_ID, externalId)
        .put(ScheduledStrengthRoutineJson.UPLOADED_TO_INTERVALS, uploadedToIntervals)
        .put(ScheduledStrengthRoutineJson.ROUTINE_JSON, listOf(routine).toJsonString())
}

private object ScheduledStrengthRoutineJson {
    const val ID = "id"
    const val DATE = "date"
    const val TIME = "time"
    const val EXTERNAL_ID = "externalId"
    const val UPLOADED_TO_INTERVALS = "uploadedToIntervals"
    const val ROUTINE_JSON = "routineJson"
}

private const val LOCAL_TRAINING_ITEM_ID_PREFIX = "local-"
private const val STRENGTH_TRAINING_TYPE = "Weight Training"

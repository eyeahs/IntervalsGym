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

internal fun loadStrengthPlans(prefs: SharedPreferences): List<StrengthWorkoutPlan> {
    val saved = prefs.getString(STRENGTH_PLANS_PREF, null)
    return saved.toStrengthWorkoutPlans().takeIf { it.isNotEmpty() } ?: defaultStrengthPlans()
}

internal fun loadScheduledStrengthPlans(prefs: SharedPreferences): List<ScheduledStrengthPlan> {
    val saved = prefs.getString(SCHEDULED_STRENGTH_PLANS_PREF, null)
    return runCatching {
        val array = JSONArray(saved ?: "[]")
        (0 until array.length()).mapNotNull { index ->
            val json = array.optJSONObject(index) ?: return@mapNotNull null
            val date = runCatching { LocalDate.parse(json.optString("date")) }.getOrNull()
                ?: return@mapNotNull null
            val plan = json.optString("planJson")
                .toStrengthWorkoutPlans()
                .firstOrNull()
                ?: return@mapNotNull null
            val externalId = json.optString("externalId")
                .ifBlank { plan.intervalsPlanExternalId(date) }
            ScheduledStrengthPlan(
                id = json.optString("id").ifBlank { plan.scheduledStrengthPlanId(date) },
                date = date,
                plan = plan,
                uploadedToIntervals = json.optBoolean("uploadedToIntervals", false),
                externalId = externalId
            )
        }
    }.getOrElse { emptyList() }
}

internal fun upsertScheduledStrengthPlan(
    prefs: SharedPreferences,
    scheduledPlan: ScheduledStrengthPlan,
) {
    val nextPlans = (listOf(scheduledPlan) + loadScheduledStrengthPlans(prefs))
        .distinctBy { it.externalId }
    saveScheduledStrengthPlans(prefs, nextPlans)
}

internal fun removeScheduledStrengthPlan(
    prefs: SharedPreferences,
    plan: TrainingItem,
) {
    val nextPlans = loadScheduledStrengthPlans(prefs).filterNot { scheduled -> scheduled.matchesTrainingItem(plan) }
    saveScheduledStrengthPlans(prefs, nextPlans)
}

internal fun moveScheduledStrengthPlan(
    prefs: SharedPreferences,
    plan: TrainingItem,
    targetDate: LocalDate,
): ScheduledStrengthPlan? {
    val result = loadScheduledStrengthPlans(prefs).withMovedScheduledStrengthPlan(plan, targetDate)
    if (result.movedPlan != null) {
        saveScheduledStrengthPlans(prefs, result.plans)
    }
    return result.movedPlan
}

internal data class ScheduledStrengthPlanMoveResult(
    val plans: List<ScheduledStrengthPlan>,
    val movedPlan: ScheduledStrengthPlan?,
)

internal fun List<ScheduledStrengthPlan>.withMovedScheduledStrengthPlan(
    plan: TrainingItem,
    targetDate: LocalDate,
): ScheduledStrengthPlanMoveResult {
    val sourcePlan = firstOrNull { scheduled -> scheduled.matchesTrainingItem(plan) }
        ?: return ScheduledStrengthPlanMoveResult(plans = this, movedPlan = null)
    val movedPlan = sourcePlan.copy(
        id = sourcePlan.plan.scheduledStrengthPlanId(targetDate),
        date = targetDate,
        uploadedToIntervals = false,
        externalId = sourcePlan.plan.intervalsPlanExternalId(targetDate)
    )
    val nextPlans = (listOf(movedPlan) + filterNot { scheduled ->
        scheduled.matchesTrainingItem(plan) || scheduled.externalId == movedPlan.externalId
    }).distinctBy { it.externalId }
    return ScheduledStrengthPlanMoveResult(plans = nextPlans, movedPlan = movedPlan)
}

private fun ScheduledStrengthPlan.matchesTrainingItem(plan: TrainingItem): Boolean {
    return id == plan.remoteId ||
        id == plan.id.removePrefix("local-") ||
        externalId == plan.externalId
}

private fun saveScheduledStrengthPlans(
    prefs: SharedPreferences,
    plans: List<ScheduledStrengthPlan>,
) {
    val array = JSONArray().also { root ->
        plans.forEach { item ->
            root.put(
                JSONObject()
                    .put("id", item.id)
                    .put("date", item.date.toString())
                    .put("externalId", item.externalId)
                    .put("uploadedToIntervals", item.uploadedToIntervals)
                    .put("planJson", listOf(item.plan).toJsonString())
            )
        }
    }
    prefs.edit().putString(SCHEDULED_STRENGTH_PLANS_PREF, array.toString()).apply()
}

internal fun List<TrainingItem>.withLocalStrengthPlans(
    scheduledPlans: List<ScheduledStrengthPlan>,
    start: LocalDate,
    end: LocalDate,
): List<TrainingItem> {
    val scheduledByExternalId = scheduledPlans.associateBy { it.externalId }
    val matchedRemoteItems = map { item ->
        val matchedPlan = item.externalId?.let { scheduledByExternalId[it] }?.plan
        if (matchedPlan == null || item.matchedStrengthPlan != null) {
            item
        } else {
            item.copy(matchedStrengthPlan = matchedPlan)
        }
    }
    val remoteExternalIds = mapNotNull { it.externalId }.toSet()
    val localItems = scheduledPlans
        .filter { scheduled -> !scheduled.date.isBefore(start) && !scheduled.date.isAfter(end) }
        .filterNot { scheduled -> scheduled.externalId in remoteExternalIds }
        .map { scheduled -> scheduled.toTrainingItem() }
    return matchedRemoteItems + localItems
}

private fun ScheduledStrengthPlan.toTrainingItem(): TrainingItem {
    return TrainingItem(
        id = "local-${id}",
        remoteId = id,
        externalId = externalId,
        name = plan.name,
        type = "Weight Training",
        date = date,
        startedAt = date.atStartOfDay(),
        timeLabel = "Plan",
        durationSeconds = plan.entries.totalDurationSeconds().takeIf { it > 0 },
        distanceMeters = null,
        weightLiftedKg = null,
        load = null,
        fitness = null,
        fatigue = null,
        form = null,
        description = plan.toIntervalsPlanDescription(),
        blocks = emptyList(),
        isPlan = true,
        matchedStrengthPlan = plan
    )
}

internal fun List<StrengthWorkoutPlan>.withLatestCompletedWorkout(
    history: List<CompletedStrengthWorkout>,
): List<StrengthWorkoutPlan> {
    if (isEmpty() || history.isEmpty()) return this
    val latestByPlanId = history
        .filter { it.planId != 0 && it.entries.isNotEmpty() }
        .groupBy { it.planId }
        .mapValues { (_, workouts) -> workouts.maxByOrNull { it.startedAtMillis } }

    return map { plan ->
        val latestWorkout = latestByPlanId[plan.id] ?: return@map plan
        plan.copy(entries = latestWorkout.entries.map { it.copyForWorkout() })
    }
}

internal fun ActiveStrengthSession.withLatestCompletedWorkout(
    history: List<CompletedStrengthWorkout>,
): ActiveStrengthSession {
    if (hasStarted || history.isEmpty()) return this
    val latestWorkout = history
        .filter { it.planId == planId && it.entries.isNotEmpty() }
        .maxByOrNull { it.startedAtMillis }
        ?: return this
    return copy(entries = latestWorkout.entries.map { it.copyForWorkout() })
}

internal fun StrengthWorkoutPlan.toIntervalsPlanDescription(): String {
    val setCount = entries.sumOf { it.records.size }
    return buildString {
        appendLine("IntervalsGym 웨이트 Plan")
        appendLine("운동 ${entries.size}개 · ${setCount}세트")
        appendLine()
        entries.forEach { entry ->
            appendLine("- ${entry.title}")
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

internal fun String?.visiblePlanDescription(): String {
    if (isNullOrBlank()) return ""
    return lineSequence()
        .filterNot { line ->
            val trimmed = line.trim()
            trimmed.startsWith(INTERVALS_GYM_STRENGTH_PLAN_PREFIX) ||
                trimmed == "로컬 러닝 기록" ||
                trimmed.startsWith("로컬 러닝 기록 ·")
        }
        .joinToString("\n")
        .trim()
}

internal fun TrainingItem.detailPlanDescription(): String {
    return pairedPlan?.description.visiblePlanDescription()
        .ifBlank { description.visiblePlanDescription() }
}

internal fun TrainingItem.workoutDetailDescription(
    isWeightTrainingItem: Boolean,
    strengthPlan: StrengthWorkoutPlan?,
): String {
    if (!isWeightTrainingItem) return detailPlanDescription()
    return if (!isPlan && strengthPlan == null) {
        description.orEmpty().trim()
    } else {
        ""
    }
}

internal fun String?.toIntervalsGymStrengthPlan(): StrengthWorkoutPlan? {
    if (isNullOrBlank()) return null
    val encoded = lineSequence()
        .map { it.trim() }
        .firstOrNull { it.startsWith(INTERVALS_GYM_STRENGTH_PLAN_PREFIX) }
        ?.removePrefix(INTERVALS_GYM_STRENGTH_PLAN_PREFIX)
        ?.trim()
        ?: return null
    return runCatching {
        val decoded = String(Base64.decode(encoded, Base64.DEFAULT), StandardCharsets.UTF_8)
        decoded.toStrengthWorkoutPlans().firstOrNull()
    }.getOrNull()
}

internal fun StrengthWorkoutPlan.intervalsPlanExternalId(date: LocalDate): String {
    return "intervals-gym-strength-plan-${id}-${date}"
}

internal fun StrengthWorkoutPlan.scheduledStrengthPlanId(date: LocalDate): String {
    return "scheduled-strength-plan-${id}-${date}"
}

internal fun loadActiveStrengthSession(prefs: SharedPreferences): ActiveStrengthSession? {
    return prefs.getString(ACTIVE_STRENGTH_SESSION_PREF, null).toActiveStrengthSession()
}

internal fun ActiveStrengthSession.toJsonString(): String {
    val planJson = JSONArray(
        listOf(
            StrengthWorkoutPlan(
                id = planId,
                name = planName,
                entries = entries
            )
        ).toJsonString()
    ).optJSONObject(0) ?: JSONObject()

    return JSONObject()
        .put("plan", planJson)
        .put("hasStarted", hasStarted)
        .put("workoutStartedAtMillis", workoutStartedAtMillis)
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
        val planJson = json.optJSONObject("plan") ?: return@runCatching null
        val plan = JSONArray()
            .put(planJson)
            .toString()
            .toStrengthWorkoutPlans()
            .firstOrNull() ?: return@runCatching null
        val restEndAtMillis = json.optLong("restEndAtMillis", 0L)
        val isExpiredRest = restEndAtMillis > 0L && restEndAtMillis <= System.currentTimeMillis()
        val restEvents = json.optJSONArray("restEvents").toStrengthRestEvents()
        val activeRestEventId = json.optNullableInt("activeRestEventId")

        ActiveStrengthSession(
            planId = plan.id,
            planName = plan.name,
            entries = plan.entries,
            hasStarted = json.optBoolean("hasStarted", false),
            workoutStartedAtMillis = json.optLong("workoutStartedAtMillis", 0L).takeIf { it > 0L }
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

internal fun buildCompletedStrengthWorkout(
    plan: StrengthWorkoutPlan,
    entries: List<StrengthPlanEntry>,
    setEvents: List<StrengthSetCompletionEvent>,
    restEvents: List<StrengthRestEvent>,
    startedAtMillis: Long,
    endedAtMillis: Long,
    rpe: Int,
    trainingLoad: Int,
    uploadedToIntervals: Boolean,
): CompletedStrengthWorkout {
    val safeStartedAt = startedAtMillis.takeIf { it > 0L } ?: endedAtMillis
    return CompletedStrengthWorkout(
        id = "strength-${safeStartedAt}-${endedAtMillis}",
        planId = plan.id,
        planName = plan.name,
        startedAtMillis = safeStartedAt,
        endedAtMillis = endedAtMillis,
        durationSeconds = ((endedAtMillis - safeStartedAt) / 1000L).toInt().coerceAtLeast(0),
        intervalsExternalId = strengthIntervalsExternalId(safeStartedAt),
        entries = entries,
        setEvents = setEvents.sortedBy { it.sequence },
        restEvents = restEvents.sortedBy { it.startedAtMillis },
        rpe = rpe,
        trainingLoad = trainingLoad,
        uploadedToIntervals = uploadedToIntervals
    )
}

internal fun appendStrengthWorkoutHistory(
    prefs: SharedPreferences,
    workout: CompletedStrengthWorkout,
) {
    val saved = prefs.getString(STRENGTH_WORKOUT_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    val nextHistory = JSONArray().apply {
        put(workout.toJsonObject())
        val maxPreviousItems = 99
        for (index in 0 until minOf(history.length(), maxPreviousItems)) {
            put(history.optJSONObject(index) ?: continue)
        }
    }
    prefs.edit().putString(STRENGTH_WORKOUT_HISTORY_PREF, nextHistory.toString()).apply()
}

internal fun replaceStrengthWorkoutHistory(
    prefs: SharedPreferences,
    workout: CompletedStrengthWorkout,
) {
    val saved = prefs.getString(STRENGTH_WORKOUT_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    var replaced = false
    val nextHistory = JSONArray().apply {
        for (index in 0 until history.length()) {
            val existing = history.optJSONObject(index).toCompletedStrengthWorkout()
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
    prefs.edit().putString(STRENGTH_WORKOUT_HISTORY_PREF, nextHistory.toString()).apply()
}

internal fun loadCompletedStrengthWorkoutHistory(prefs: SharedPreferences): List<CompletedStrengthWorkout> {
    val saved = prefs.getString(STRENGTH_WORKOUT_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    return (0 until history.length()).mapNotNull { index ->
        history.optJSONObject(index).toCompletedStrengthWorkout()
    }
}

internal fun appendRunningWorkoutHistory(
    prefs: SharedPreferences,
    workout: CompletedRunningWorkout,
) {
    val saved = prefs.getString(RUNNING_WORKOUT_HISTORY_PREF, null)
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
    prefs.edit().putString(RUNNING_WORKOUT_HISTORY_PREF, nextHistory.toString()).apply()
}

internal fun replaceRunningWorkoutHistory(
    prefs: SharedPreferences,
    workout: CompletedRunningWorkout,
) {
    val saved = prefs.getString(RUNNING_WORKOUT_HISTORY_PREF, null)
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
    prefs.edit().putString(RUNNING_WORKOUT_HISTORY_PREF, nextHistory.toString()).apply()
}

internal fun deleteRunningWorkoutHistory(
    prefs: SharedPreferences,
    workoutId: String,
) {
    val saved = prefs.getString(RUNNING_WORKOUT_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    val nextHistory = JSONArray().apply {
        for (index in 0 until history.length()) {
            val existing = history.optJSONObject(index) ?: continue
            if (existing.optString("id") != workoutId) {
                put(existing)
            }
        }
    }
    prefs.edit().putString(RUNNING_WORKOUT_HISTORY_PREF, nextHistory.toString()).apply()
}

internal fun upsertSavedRunningWorkoutPlan(
    prefs: SharedPreferences,
    plan: SavedRunningWorkoutPlan,
) {
    val nextPlans = (listOf(plan) + loadSavedRunningWorkoutPlans(prefs))
        .distinctBy { it.id }
        .take(100)
    saveSavedRunningWorkoutPlans(prefs, nextPlans)
}

internal fun loadSavedRunningWorkoutPlans(prefs: SharedPreferences): List<SavedRunningWorkoutPlan> {
    val saved = prefs.getString(SAVED_RUNNING_PLANS_PREF, null)
    val plans = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    return (0 until plans.length()).mapNotNull { index ->
        plans.optJSONObject(index).toSavedRunningWorkoutPlan()
    }
}

internal fun deleteSavedRunningWorkoutPlan(
    prefs: SharedPreferences,
    planId: String,
) {
    saveSavedRunningWorkoutPlans(
        prefs = prefs,
        plans = loadSavedRunningWorkoutPlans(prefs).filterNot { it.id == planId }
    )
}

private fun saveSavedRunningWorkoutPlans(
    prefs: SharedPreferences,
    plans: List<SavedRunningWorkoutPlan>,
) {
    val array = JSONArray().apply {
        plans.forEach { plan ->
            put(plan.toJsonObject())
        }
    }
    prefs.edit().putString(SAVED_RUNNING_PLANS_PREF, array.toString()).apply()
}

internal fun loadCompletedRunningWorkoutHistory(prefs: SharedPreferences): List<CompletedRunningWorkout> {
    val saved = prefs.getString(RUNNING_WORKOUT_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    return (0 until history.length()).mapNotNull { index ->
        history.optJSONObject(index).toCompletedRunningWorkout()
    }
}

private fun JSONObject?.toCompletedStrengthWorkout(): CompletedStrengthWorkout? {
    this ?: return null
    val planSnapshot = optJSONObject("planSnapshot")
    val snapshotPlan = planSnapshot?.let {
        JSONArray().put(it).toString().toStrengthWorkoutPlans().firstOrNull()
    }
    val planId = optNullableInt("planId") ?: snapshotPlan?.id ?: 0
    val planName = optString("planName").ifBlank { snapshotPlan?.name ?: "웨이트 트레이닝" }
    val startedAtMillis = optLong("startedAtMillis", 0L)
    val endedAtMillis = optLong("endedAtMillis", startedAtMillis)
    if (startedAtMillis <= 0L) return null
    val entries = snapshotPlan?.entries.orEmpty()
    val rpe = optNullableInt("rpe") ?: 7
    return CompletedStrengthWorkout(
        id = optString("id").ifBlank { "strength-$startedAtMillis-$endedAtMillis" },
        planId = planId,
        planName = planName,
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
        uploadedToIntervals = optBoolean("uploadedToIntervals", false)
    )
}

private fun SavedRunningWorkoutPlan.toJsonObject(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("name", name)
        .put("description", description ?: JSONObject.NULL)
        .put("durationSeconds", durationSeconds)
        .put("blocks", blocks.toPlanBlocksJsonArray())
        .put("workoutDocJson", workoutDocJson ?: JSONObject.NULL)
        .put("savedAtMillis", savedAtMillis)
}

private fun JSONObject?.toSavedRunningWorkoutPlan(): SavedRunningWorkoutPlan? {
    this ?: return null
    val blocks = optJSONArray("blocks").toCachedPlanBlocks()
    if (blocks.isEmpty()) return null
    return SavedRunningWorkoutPlan(
        id = optString("id").ifBlank { "saved-running-${optLong("savedAtMillis", System.currentTimeMillis())}" },
        name = optString("name").ifBlank { "러닝 Plan" },
        description = optString("description").cleanJsonText(),
        durationSeconds = optNullableInt("durationSeconds") ?: blocks.sumOf { it.durationSeconds },
        blocks = blocks,
        workoutDocJson = optString("workoutDocJson").cleanJsonText(),
        savedAtMillis = optLong("savedAtMillis", System.currentTimeMillis())
    )
}

private fun JSONObject?.toCompletedRunningWorkout(): CompletedRunningWorkout? {
    this ?: return null
    val startedAtMillis = optLong("startedAtMillis", 0L)
    val endedAtMillis = optLong("endedAtMillis", startedAtMillis)
    if (startedAtMillis <= 0L) return null
    val durationSeconds = optNullableInt("durationSeconds")
        ?: ((endedAtMillis - startedAtMillis) / 1000L).toInt().coerceAtLeast(0)
    val warmupSeconds = optNullableInt("warmupSeconds") ?: 0
    val planBlocks = optJSONArray("blocks").toCachedPlanBlocks()
    val savedActualBlocks = optJSONArray("actualBlocks").toCachedPlanBlocks()
    val actualBlocks = savedActualBlocks.normalizedRunningActualBlocks(
        planBlocks = planBlocks,
        activeDurationSeconds = (durationSeconds - warmupSeconds).coerceAtLeast(0)
    )
    return CompletedRunningWorkout(
        id = optString("id").ifBlank { "running-$startedAtMillis" },
        name = optString("name").ifBlank { "러닝" },
        startedAtMillis = startedAtMillis,
        endedAtMillis = endedAtMillis,
        durationSeconds = durationSeconds,
        warmupSeconds = warmupSeconds,
        estimatedDistanceMeters = actualBlocks.estimatedRunningDistanceMeters(),
        blocks = planBlocks,
        actualBlocks = actualBlocks,
        uploadedToIntervals = optBoolean("uploadedToIntervals", false)
    )
}

private fun List<TrainingItem>.withMatchedStrengthWorkouts(
    history: List<CompletedStrengthWorkout>,
): List<TrainingItem> {
    if (history.isEmpty()) return this
    return map { item ->
        if (item.isPlan) {
            item
        } else {
            item.copy(matchedStrengthWorkout = item.matchStrengthWorkout(history))
        }
    }
}

internal fun List<TrainingItem>.withLocalStrengthResults(
    history: List<CompletedStrengthWorkout>,
    weekStart: LocalDate,
    weekEnd: LocalDate,
): List<TrainingItem> {
    val matched = withMatchedStrengthWorkouts(history)
    val matchedWorkoutIds = matched.mapNotNull { it.matchedStrengthWorkout?.id }.toSet()
    val localOnlyItems = history
        .filter { workout ->
            val date = workout.startedLocalDate()
            date in weekStart..weekEnd && workout.id !in matchedWorkoutIds
        }
        .map { workout -> workout.toLocalTrainingItem() }
    return matched + localOnlyItems
}

private fun CompletedStrengthWorkout.startedLocalDate(): LocalDate {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault()).toLocalDate()
}

private fun CompletedStrengthWorkout.toLocalTrainingItem(): TrainingItem {
    val startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault())
    return TrainingItem(
        id = "local-strength-$id",
        remoteId = id,
        externalId = intervalsExternalId,
        name = planName,
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
        isPlan = false,
        matchedStrengthWorkout = this,
        isLocalOnlyStrengthResult = true
    )
}

internal fun CompletedStrengthWorkout.toStrengthWorkoutSession(): StrengthWorkoutSession {
    return StrengthWorkoutSession(
        name = planName,
        startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault()),
        entries = entries,
        rpe = rpe,
        trainingLoad = trainingLoad
    )
}

internal fun List<TrainingItem>.withLocalRunningResults(
    history: List<CompletedRunningWorkout>,
    weekStart: LocalDate,
    weekEnd: LocalDate,
): List<TrainingItem> {
    if (history.isEmpty()) return this
    val localOnlyItems = history
        .filter { workout ->
            val date = workout.startedLocalDate()
            date in weekStart..weekEnd && none { item -> item.matchesRunningWorkout(workout) }
        }
        .map { workout -> workout.toLocalTrainingItem() }
    return this + localOnlyItems
}

private fun CompletedRunningWorkout.startedLocalDate(): LocalDate {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault()).toLocalDate()
}

private fun CompletedRunningWorkout.toLocalTrainingItem(): TrainingItem {
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
        isPlan = false,
        isLocalOnlyRunningResult = true,
        actualRunningBlocks = actualBlocks
    )
}

private fun TrainingItem.matchesRunningWorkout(workout: CompletedRunningWorkout): Boolean {
    if (isLocalOnlyRunningResult) {
        return remoteId == workout.id || id == "local-running-${workout.id}"
    }
    if (isPlan || sportType() != TrainingSportType.RUNNING) return false
    val startedMillis = startedAt
        ?.atZone(ZoneId.systemDefault())
        ?.toInstant()
        ?.toEpochMilli()
        ?: return false
    val timeDiff = abs(startedMillis - workout.startedAtMillis)
    val durationDiff = durationSeconds?.let { abs(it - workout.durationSeconds) } ?: Int.MAX_VALUE
    return timeDiff <= 10 * 60 * 1000L || durationDiff <= 5 * 60
}

private fun TrainingItem.matchStrengthWorkout(
    history: List<CompletedStrengthWorkout>,
): CompletedStrengthWorkout? {
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
                workout.planName.equals(name, ignoreCase = true) ||
                name.contains(workout.planName, ignoreCase = true)
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

private fun CompletedStrengthWorkout.toJsonObject(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("planId", planId)
        .put("planName", planName)
        .put("startedAtMillis", startedAtMillis)
        .put("endedAtMillis", endedAtMillis)
        .put("durationSeconds", durationSeconds)
        .put("intervalsExternalId", intervalsExternalId)
        .put("rpe", rpe)
        .put("trainingLoad", trainingLoad)
        .put("uploadedToIntervals", uploadedToIntervals)
        .put(
            "planSnapshot",
            JSONArray(
                listOf(
                    StrengthWorkoutPlan(
                        id = planId,
                        name = planName,
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

internal fun List<StrengthWorkoutPlan>.toJsonString(): String {
    return JSONArray().also { plansArray ->
        forEach { plan ->
            plansArray.put(
                JSONObject()
                    .put("id", plan.id)
                    .put("name", plan.name)
                    .put(
                        "entries",
                        JSONArray().also { entriesArray ->
                            plan.entries.forEach { entry ->
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

internal fun String?.toStrengthWorkoutPlans(): List<StrengthWorkoutPlan> {
    if (isNullOrBlank()) return emptyList()
    return runCatching {
        val plansArray = JSONArray(this)
        (0 until plansArray.length()).mapNotNull { planIndex ->
            val planJson = plansArray.optJSONObject(planIndex) ?: return@mapNotNull null
            val entriesArray = planJson.optJSONArray("entries") ?: JSONArray()
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
                StrengthPlanEntry(
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
                    records = records
                )
            }
            StrengthWorkoutPlan(
                id = planJson.optNullableInt("id") ?: (planIndex + 1),
                name = planJson.optString("name").ifBlank { "웨이트 Plan" },
                entries = entries
            )
        }
    }.getOrDefault(emptyList())
}

package com.lighthousepark.intervalsgym.data

import android.content.SharedPreferences
import com.lighthousepark.intervalsgym.core.optNullableInt
import com.lighthousepark.intervalsgym.core.parseDateTime
import com.lighthousepark.intervalsgym.running.CompletedRunningSession
import com.lighthousepark.intervalsgym.running.INTERVALS_GARMIN_ACTIVITY_SOURCE
import com.lighthousepark.intervalsgym.running.RunningActivityMergeCandidate
import com.lighthousepark.intervalsgym.running.RunningActivityMergeUpdate
import com.lighthousepark.intervalsgym.running.RunningRemoteActivity
import com.lighthousepark.intervalsgym.running.RunningRemoteActivityStreams
import com.lighthousepark.intervalsgym.running.RunningRemoteHeartRatePoint
import com.lighthousepark.intervalsgym.running.RunningRemoteStream
import com.lighthousepark.intervalsgym.running.evaluateRunningActivityMergeCandidate
import com.lighthousepark.intervalsgym.running.intervalsRunningExternalId
import com.lighthousepark.intervalsgym.running.mergedRunningActivityStreams
import com.lighthousepark.intervalsgym.running.rankedRunningMergeCandidates
import com.lighthousepark.intervalsgym.running.runningActivityMergeUpdate
import com.lighthousepark.intervalsgym.running.runningRecordStartedAtMillis
import com.lighthousepark.intervalsgym.running.withRunningMergeResult
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs
import org.json.JSONArray
import org.json.JSONObject

internal interface RunningActivityMergeRemoteDataSource {
    suspend fun loadActivities(start: LocalDate, end: LocalDate): List<RunningRemoteActivity>

    suspend fun loadHeartRate(activityId: String): List<RunningRemoteHeartRatePoint>

    suspend fun loadStreams(activityId: String): RunningRemoteActivityStreams

    suspend fun updateStreams(activityId: String, streams: RunningRemoteActivityStreams)

    suspend fun updateActivity(activityId: String, update: RunningActivityMergeUpdate)

    suspend fun deleteActivity(activityId: String)
}

internal class IntervalsRunningActivityMergeRemoteDataSource(
    private val repository: IntervalsRepository,
) : RunningActivityMergeRemoteDataSource {
    override suspend fun loadActivities(start: LocalDate, end: LocalDate): List<RunningRemoteActivity> {
        return repository.loadRunningMergeActivities(start, end)
    }

    override suspend fun loadHeartRate(activityId: String): List<RunningRemoteHeartRatePoint> {
        return repository.loadRunningMergeHeartRate(activityId)
    }

    override suspend fun loadStreams(activityId: String): RunningRemoteActivityStreams {
        return repository.loadRunningMergeStreams(activityId)
    }

    override suspend fun updateStreams(activityId: String, streams: RunningRemoteActivityStreams) {
        repository.updateRunningMergeStreams(activityId, streams)
    }

    override suspend fun updateActivity(activityId: String, update: RunningActivityMergeUpdate) {
        repository.updateRunningMergeActivity(activityId, update)
    }

    override suspend fun deleteActivity(activityId: String) {
        repository.deleteRunningMergeDuplicate(activityId)
    }
}

internal data class RunningActivityMergeResult(
    val session: CompletedRunningSession,
    val deletedDuplicateActivity: Boolean,
)

internal interface RunningActivityMergeActions {
    suspend fun findCandidates(session: CompletedRunningSession): List<RunningActivityMergeCandidate>

    suspend fun merge(
        session: CompletedRunningSession,
        candidate: RunningActivityMergeCandidate,
    ): RunningActivityMergeResult
}

internal class RunningActivityMergeUseCase(
    private val prefs: SharedPreferences,
    private val remoteDataSource: RunningActivityMergeRemoteDataSource,
) : RunningActivityMergeActions {
    override suspend fun findCandidates(session: CompletedRunningSession): List<RunningActivityMergeCandidate> {
        val recordStartedAtMillis = session.runningRecordStartedAtMillis()
        val sessionDate = Instant.ofEpochMilli(recordStartedAtMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val activities = remoteDataSource.loadActivities(
            start = sessionDate.minusDays(1),
            end = sessionDate.plusDays(1)
        )
        val duplicateActivityId = activities.firstOrNull {
            it.externalId == session.intervalsRunningExternalId()
        }?.id
        val nearbyGarminActivities = activities
            .asSequence()
            .filter { it.source == INTERVALS_GARMIN_ACTIVITY_SOURCE }
            .filter { abs(it.startedAtMillis - recordStartedAtMillis) <= CANDIDATE_LOAD_WINDOW_MILLIS }
            .sortedBy { abs(it.startedAtMillis - recordStartedAtMillis) }
            .take(MAX_HEART_RATE_CANDIDATES)
            .toList()
        val candidates = mutableListOf<RunningActivityMergeCandidate>()
        nearbyGarminActivities.forEach { activity ->
            val heartRate = try {
                remoteDataSource.loadHeartRate(activity.id)
            } catch (_: Exception) {
                emptyList()
            }
            evaluateRunningActivityMergeCandidate(
                    session = session,
                    activity = activity,
                    remoteHeartRate = heartRate,
                    duplicateActivityId = duplicateActivityId
                )?.let(candidates::add)
        }
        return candidates.rankedRunningMergeCandidates()
    }

    override suspend fun merge(
        session: CompletedRunningSession,
        candidate: RunningActivityMergeCandidate,
    ): RunningActivityMergeResult {
        val sourceStreams = remoteDataSource.loadStreams(candidate.activity.id)
        val mergedStreams = session.mergedRunningActivityStreams(candidate, sourceStreams)
        // Verify stream-write access before changing activity bounds. Updating the
        // activity metadata can make Intervals recalculate its displayed streams.
        remoteDataSource.updateStreams(
            activityId = candidate.activity.id,
            streams = sourceStreams
        )
        remoteDataSource.updateActivity(
            activityId = candidate.activity.id,
            update = session.runningActivityMergeUpdate(candidate)
        )
        try {
            remoteDataSource.updateStreams(
                activityId = candidate.activity.id,
                streams = mergedStreams
            )
        } catch (error: Exception) {
            runCatching {
                remoteDataSource.updateStreams(
                    activityId = candidate.activity.id,
                    streams = sourceStreams
                )
            }
            throw error
        }
        val duplicateId = candidate.duplicateActivityId
            ?.takeUnless { it == candidate.activity.id }
        if (duplicateId != null) {
            remoteDataSource.deleteActivity(duplicateId)
        }
        val mergedSession = session.withRunningMergeResult(candidate, mergedStreams)
        replaceRunningSessionHistory(prefs, mergedSession)
        return RunningActivityMergeResult(
            session = mergedSession,
            deletedDuplicateActivity = duplicateId != null
        )
    }
}

internal fun JSONArray.toRunningRemoteActivities(): List<RunningRemoteActivity> {
    return (0 until length()).mapNotNull { index ->
        val activity = optJSONObject(index) ?: return@mapNotNull null
        val id = activity.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val startedAt = parseDateTime(activity.optString("start_date_local")) ?: return@mapNotNull null
        val startedAtMillis = startedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        RunningRemoteActivity(
            id = id,
            name = activity.optString("name").ifBlank { "Garmin 러닝" },
            type = activity.optString("type"),
            source = activity.optString("source"),
            externalId = activity.optString("external_id")
                .takeIf { it.isNotBlank() && it != "null" },
            startedAtMillis = startedAtMillis,
            durationSeconds = activity.optNullableInt("elapsed_time")
                ?: activity.optNullableInt("moving_time")
                ?: 0,
            description = activity.optString("description")
                .takeIf { it.isNotBlank() && it != "null" }
        )
    }
}

internal fun JSONArray.toRunningRemoteHeartRatePoints(): List<RunningRemoteHeartRatePoint> {
    val streams = (0 until length()).mapNotNull { index -> optJSONObject(index) }
    val time = streams.firstOrNull { it.optString("type") == "time" }
        ?.optJSONArray("data")
        ?: return emptyList()
    val heartRate = streams.firstOrNull { it.optString("type") == "heartrate" }
        ?.optJSONArray("data")
        ?: return emptyList()
    return (0 until minOf(time.length(), heartRate.length())).mapNotNull { index ->
        if (time.isNull(index) || heartRate.isNull(index)) return@mapNotNull null
        val elapsedSeconds = (time.opt(index) as? Number)?.toDouble()?.toInt() ?: return@mapNotNull null
        val bpm = (heartRate.opt(index) as? Number)?.toDouble()?.toInt() ?: return@mapNotNull null
        if (elapsedSeconds < 0 || bpm <= 0) return@mapNotNull null
        RunningRemoteHeartRatePoint(elapsedSeconds = elapsedSeconds, bpm = bpm)
    }
}

internal fun JSONArray.toRunningRemoteActivityStreams(): RunningRemoteActivityStreams {
    return RunningRemoteActivityStreams(
        streams = (0 until length()).mapNotNull { index ->
            val json = optJSONObject(index) ?: return@mapNotNull null
            val type = json.optString("type").takeIf { it.isNotBlank() }
                ?: return@mapNotNull null
            val data = json.optJSONArray("data")?.toKotlinValues()
                ?: return@mapNotNull null
            val attributes = buildMap {
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    if (key != "type" && key != "data") {
                        put(key, json.opt(key).toKotlinJsonValue())
                    }
                }
            }
            RunningRemoteStream(
                type = type,
                data = data,
                attributes = attributes
            )
        }
    )
}

internal fun RunningRemoteActivityStreams.toIntervalsStreamsJson(): JSONArray {
    return JSONArray().apply {
        streams.forEach { stream ->
            put(
                JSONObject().apply {
                    stream.attributes.forEach { (key, value) ->
                        put(key, value.toJsonValue())
                    }
                    put("type", stream.type)
                    put("data", stream.data.toJsonArray())
                }
            )
        }
    }
}

private fun JSONArray.toKotlinValues(): List<Any?> {
    return (0 until length()).map { index -> opt(index).toKotlinJsonValue() }
}

private fun Any?.toKotlinJsonValue(): Any? {
    return when (this) {
        null, JSONObject.NULL -> null
        is JSONArray -> toKotlinValues()
        is JSONObject -> buildMap {
            val keys = keys()
            while (keys.hasNext()) {
                val key = keys.next()
                put(key, opt(key).toKotlinJsonValue())
            }
        }
        else -> this
    }
}

private fun Any?.toJsonValue(): Any {
    return when (this) {
        null -> JSONObject.NULL
        is List<*> -> toJsonArray()
        is Map<*, *> -> JSONObject().apply {
            this@toJsonValue.forEach { (key, value) ->
                if (key is String) put(key, value.toJsonValue())
            }
        }
        else -> this
    }
}

private fun List<*>.toJsonArray(): JSONArray {
    return JSONArray().apply {
        this@toJsonArray.forEach { value -> put(value.toJsonValue()) }
    }
}

private const val CANDIDATE_LOAD_WINDOW_MILLIS = 20 * 60 * 1_000L
private const val MAX_HEART_RATE_CANDIDATES = 4

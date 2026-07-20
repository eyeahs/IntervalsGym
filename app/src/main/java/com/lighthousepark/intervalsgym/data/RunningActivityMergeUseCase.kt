package com.lighthousepark.intervalsgym.data

import android.content.SharedPreferences
import com.lighthousepark.intervalsgym.core.optNullableInt
import com.lighthousepark.intervalsgym.core.parseDateTime
import com.lighthousepark.intervalsgym.running.CompletedRunningSession
import com.lighthousepark.intervalsgym.running.INTERVALS_GARMIN_ACTIVITY_SOURCE
import com.lighthousepark.intervalsgym.running.RunningActivityMergeCandidate
import com.lighthousepark.intervalsgym.running.RunningRemoteActivity
import com.lighthousepark.intervalsgym.running.RunningRemoteHeartRatePoint
import com.lighthousepark.intervalsgym.running.evaluateRunningActivityMergeCandidate
import com.lighthousepark.intervalsgym.running.intervalsRunningExternalId
import com.lighthousepark.intervalsgym.running.mergedIntervalsDescription
import com.lighthousepark.intervalsgym.running.rankedRunningMergeCandidates
import com.lighthousepark.intervalsgym.running.withRunningMergeResult
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs
import org.json.JSONArray

internal interface RunningActivityMergeRemoteDataSource {
    suspend fun loadActivities(start: LocalDate, end: LocalDate): List<RunningRemoteActivity>

    suspend fun loadHeartRate(activityId: String): List<RunningRemoteHeartRatePoint>

    suspend fun updateActivityDescription(activityId: String, description: String)

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

    override suspend fun updateActivityDescription(activityId: String, description: String) {
        repository.updateRunningMergeDescription(activityId, description)
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
        val sessionDate = Instant.ofEpochMilli(session.startedAtMillis)
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
            .filter { abs(it.startedAtMillis - session.startedAtMillis) <= CANDIDATE_LOAD_WINDOW_MILLIS }
            .sortedBy { abs(it.startedAtMillis - session.startedAtMillis) }
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
        remoteDataSource.updateActivityDescription(
            activityId = candidate.activity.id,
            description = session.mergedIntervalsDescription(candidate)
        )
        val duplicateId = candidate.duplicateActivityId
            ?.takeUnless { it == candidate.activity.id }
        if (duplicateId != null) {
            remoteDataSource.deleteActivity(duplicateId)
        }
        val mergedSession = session.withRunningMergeResult(candidate)
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

private const val CANDIDATE_LOAD_WINDOW_MILLIS = 20 * 60 * 1_000L
private const val MAX_HEART_RATE_CANDIDATES = 4

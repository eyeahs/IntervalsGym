package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.running.HeartRateSample
import com.lighthousepark.intervalsgym.running.INTERVALS_GARMIN_ACTIVITY_SOURCE
import com.lighthousepark.intervalsgym.running.RunningActivityMergeCandidate
import com.lighthousepark.intervalsgym.running.RunningActivityMergeMatchMethod
import com.lighthousepark.intervalsgym.running.RunningActivityMergeUpdate
import com.lighthousepark.intervalsgym.running.RunningRemoteActivity
import com.lighthousepark.intervalsgym.running.RunningRemoteActivityStreams
import com.lighthousepark.intervalsgym.running.RunningRemoteHeartRatePoint
import com.lighthousepark.intervalsgym.running.RunningRemoteStream
import com.lighthousepark.intervalsgym.running.intervalsRunningExternalId
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sin

class RunningActivityMergeUseCaseTest {
    @Test
    fun findCandidates_prefersMatchingGarminHeartRateAndFindsAppDuplicate() = runBlocking {
        val prefs = MemorySharedPreferences()
        val session = completedRunningSessionForStorage(
            id = "running-merge",
            name = "interval run",
            startedAtMillis = 1_000_000L,
            endedAtMillis = 1_180_000L
        ).copy(
            heartRateSamples = (0..180).map { second ->
                HeartRateSample(1_000_000L + second * 1_000L, syntheticHeartRate(second))
            }
        )
        val garmin = remoteActivity(id = "i-garmin", startedAtMillis = 995_000L)
        val duplicate = remoteActivity(
            id = "i-app",
            source = "OAUTH_CLIENT",
            externalId = session.intervalsRunningExternalId(),
            startedAtMillis = session.startedAtMillis
        )
        val remote = FakeRunningActivityMergeRemoteDataSource(
            activities = listOf(duplicate, garmin),
            heartRates = mapOf(
                garmin.id to (0..190).map { second ->
                    RunningRemoteHeartRatePoint(second, syntheticHeartRate(second - 5))
                }
            )
        )

        val candidates = RunningActivityMergeUseCase(prefs, remote).findCandidates(session)

        assertEquals(1, candidates.size)
        assertEquals("i-garmin", candidates.single().activity.id)
        assertEquals("i-app", candidates.single().duplicateActivityId)
        assertEquals(5, candidates.single().offsetSeconds)
        assertTrue((candidates.single().heartRateCorrelation ?: 0.0) > 0.99)
    }

    @Test
    fun merge_updatesGarminThenDeletesAppDuplicateAndPersistsLink() = runBlocking {
        val prefs = MemorySharedPreferences()
        val session = completedRunningSessionForStorage(
            id = "running-merge",
            name = "interval run",
            startedAtMillis = 1_000_000L,
            endedAtMillis = 1_180_000L
        )
        appendRunningSessionHistory(prefs, session)
        val remote = FakeRunningActivityMergeRemoteDataSource()
        val candidate = RunningActivityMergeCandidate(
            activity = remoteActivity(id = "i-garmin"),
            matchMethod = RunningActivityMergeMatchMethod.START_TIME,
            offsetSeconds = 3,
            heartRateCorrelation = null,
            comparedHeartRateSamples = 0,
            startDifferenceSeconds = 3,
            durationDifferenceSeconds = 0,
            duplicateActivityId = "i-app"
        )

        val result = RunningActivityMergeUseCase(prefs, remote).merge(session, candidate)

        assertEquals(
            listOf(
                "load-streams:i-garmin",
                "update-streams:i-garmin",
                "update:i-garmin",
                "update-streams:i-garmin",
                "delete:i-app"
            ),
            remote.actions
        )
        assertTrue(result.deletedDuplicateActivity)
        assertEquals(session.startedAtMillis, remote.updatedActivity?.startedAtMillis)
        assertEquals(session.durationSeconds, remote.updatedActivity?.durationSeconds)
        assertTrue(remote.updatedActivity?.description.orEmpty().contains("IntervalsGym 러닝 수행 정보"))
        val stored = loadCompletedRunningSessionHistory(prefs).single()
        assertEquals("i-garmin", stored.mergedIntervalsActivityId)
        assertEquals(3, stored.mergeOffsetSeconds)
        assertEquals(listOf(0, 177, 180), remote.updatedStreams?.stream("time")?.data)
    }

    @Test
    fun merge_doesNotDeleteWhenAppDuplicateIsMissing() = runBlocking {
        val prefs = MemorySharedPreferences()
        val session = completedRunningSessionForStorage(
            id = "running-merge",
            name = "run",
            startedAtMillis = 1_000_000L,
            endedAtMillis = 1_180_000L
        )
        val remote = FakeRunningActivityMergeRemoteDataSource()
        val candidate = RunningActivityMergeCandidate(
            activity = remoteActivity(id = "i-garmin"),
            matchMethod = RunningActivityMergeMatchMethod.START_TIME,
            offsetSeconds = 0,
            heartRateCorrelation = null,
            comparedHeartRateSamples = 0,
            startDifferenceSeconds = 0,
            durationDifferenceSeconds = 0,
            duplicateActivityId = null
        )

        val result = RunningActivityMergeUseCase(prefs, remote).merge(session, candidate)

        assertFalse(result.deletedDuplicateActivity)
        assertEquals(
            listOf(
                "load-streams:i-garmin",
                "update-streams:i-garmin",
                "update:i-garmin",
                "update-streams:i-garmin"
            ),
            remote.actions
        )
    }

    @Test
    fun merge_persistsGarminRouteAndAppHeartRate() = runBlocking {
        val prefs = MemorySharedPreferences()
        val session = completedRunningSessionForStorage(
            id = "running-merge",
            name = "run",
            startedAtMillis = 1_000_000L,
            endedAtMillis = 1_180_000L
        ).copy(
            heartRateSamples = listOf(
                HeartRateSample(timestampMillis = 1_060_000L, bpm = 158)
            )
        )
        appendRunningSessionHistory(prefs, session)
        val streams = RunningRemoteActivityStreams(
            streams = listOf(
                RunningRemoteStream("time", listOf(0, 180)),
                RunningRemoteStream(
                    "latlng",
                    listOf(listOf(37.1, 127.1), listOf(37.2, 127.2))
                ),
                RunningRemoteStream("altitude", listOf(10.0, 20.0)),
                RunningRemoteStream("heartrate", listOf(130, 140))
            )
        )
        val remote = FakeRunningActivityMergeRemoteDataSource(streams = streams)
        val candidate = RunningActivityMergeCandidate(
            activity = remoteActivity(id = "i-garmin"),
            matchMethod = RunningActivityMergeMatchMethod.START_TIME,
            offsetSeconds = 0,
            heartRateCorrelation = null,
            comparedHeartRateSamples = 0,
            startDifferenceSeconds = 0,
            durationDifferenceSeconds = 0,
            duplicateActivityId = null
        )

        val result = RunningActivityMergeUseCase(prefs, remote).merge(session, candidate)

        assertEquals(2, result.session.routePoints.size)
        assertEquals(37.1, result.session.routePoints.first().latitude, 0.0)
        assertEquals(20.0, result.session.routePoints.last().elevationMeters, 0.0)
        assertEquals(listOf(158), result.session.heartRateSamples.map { it.bpm })
        val stored = loadCompletedRunningSessionHistory(prefs).single()
        assertEquals(result.session.routePoints, stored.routePoints)
        assertEquals(listOf(158), stored.heartRateSamples.map { it.bpm })
    }

    @Test
    fun activityUpdateJson_usesAppStartAndDuration() {
        val startedAt = LocalDateTime.of(2026, 7, 24, 7, 30)
        val startedAtMillis = startedAt
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val json = RunningActivityMergeUpdate(
            startedAtMillis = startedAtMillis,
            durationSeconds = 1_200,
            description = "merged"
        ).toIntervalsActivityUpdateJson()

        assertEquals("2026-07-24T07:30:00", json.getString("start_date_local"))
        assertEquals(1_200, json.getInt("moving_time"))
        assertEquals(1_200, json.getInt("elapsed_time"))
        assertEquals("merged", json.getString("description"))
    }

    @Test
    fun remoteActivityJson_readsGarminSourceAndElapsedDuration() {
        val activities = JSONArray().put(
            JSONObject()
                .put("id", "i-garmin")
                .put("name", "Morning Run")
                .put("type", "Run")
                .put("source", "GARMIN_CONNECT")
                .put("external_id", "garmin-1")
                .put("start_date_local", "2026-07-20T07:30:00")
                .put("elapsed_time", 1_800)
        )

        val activity = activities.toRunningRemoteActivities().single()

        assertEquals("i-garmin", activity.id)
        assertEquals("GARMIN_CONNECT", activity.source)
        assertEquals("garmin-1", activity.externalId)
        assertEquals(1_800, activity.durationSeconds)
    }

    @Test
    fun remoteStreamsJson_pairsTimeAndHeartRateWhileSkippingNulls() {
        val streams = JSONArray()
            .put(
                JSONObject()
                    .put("type", "time")
                    .put("data", JSONArray().put(0).put(1).put(2))
            )
            .put(
                JSONObject()
                    .put("type", "heartrate")
                    .put("data", JSONArray().put(130).put(JSONObject.NULL).put(132))
            )

        val points = streams.toRunningRemoteHeartRatePoints()

        assertEquals(
            listOf(
                RunningRemoteHeartRatePoint(0, 130),
                RunningRemoteHeartRatePoint(2, 132)
            ),
            points
        )
    }

    @Test
    fun remoteStreamsJson_roundTripsRouteAndStreamAttributes() {
        val streams = JSONArray()
            .put(
                JSONObject()
                    .put("type", "time")
                    .put("data", JSONArray().put(0).put(60))
            )
            .put(
                JSONObject()
                    .put("type", "latlng")
                    .put("name", "Location")
                    .put(
                        "data",
                        JSONArray()
                            .put(JSONArray().put(37.1).put(127.1))
                            .put(JSONArray().put(37.2).put(127.2))
                    )
            )

        val encoded = streams
            .toRunningRemoteActivityStreams()
            .toIntervalsStreamsJson()
        val route = encoded.getJSONObject(1)

        assertEquals("latlng", route.getString("type"))
        assertEquals("Location", route.getString("name"))
        assertEquals(37.1, route.getJSONArray("data").getJSONArray(0).getDouble(0), 0.0)
        assertEquals(127.2, route.getJSONArray("data").getJSONArray(1).getDouble(1), 0.0)
    }

    private fun remoteActivity(
        id: String,
        source: String = INTERVALS_GARMIN_ACTIVITY_SOURCE,
        externalId: String? = null,
        startedAtMillis: Long = 1_000_000L,
    ): RunningRemoteActivity {
        return RunningRemoteActivity(
            id = id,
            name = "Garmin Run",
            type = "Run",
            source = source,
            externalId = externalId,
            startedAtMillis = startedAtMillis,
            durationSeconds = 180,
            description = "Garmin note"
        )
    }

    private fun syntheticHeartRate(second: Int): Int {
        return 135 +
            (sin(second / 9.0) * 13).toInt() +
            (sin(second / 27.0) * 8).toInt() +
            second.coerceAtLeast(0) / 45
    }
}

private class FakeRunningActivityMergeRemoteDataSource(
    private val activities: List<RunningRemoteActivity> = emptyList(),
    private val heartRates: Map<String, List<RunningRemoteHeartRatePoint>> = emptyMap(),
    private val streams: RunningRemoteActivityStreams = RunningRemoteActivityStreams(
        listOf(RunningRemoteStream("time", listOf(0, 180)))
    ),
) : RunningActivityMergeRemoteDataSource {
    val actions = mutableListOf<String>()
    var updatedActivity: RunningActivityMergeUpdate? = null
    var updatedStreams: RunningRemoteActivityStreams? = null

    override suspend fun loadActivities(start: LocalDate, end: LocalDate): List<RunningRemoteActivity> {
        return activities
    }

    override suspend fun loadHeartRate(activityId: String): List<RunningRemoteHeartRatePoint> {
        return heartRates[activityId].orEmpty()
    }

    override suspend fun loadStreams(activityId: String): RunningRemoteActivityStreams {
        actions += "load-streams:$activityId"
        return streams
    }

    override suspend fun updateStreams(
        activityId: String,
        streams: RunningRemoteActivityStreams,
    ) {
        actions += "update-streams:$activityId"
        updatedStreams = streams
    }

    override suspend fun updateActivity(activityId: String, update: RunningActivityMergeUpdate) {
        actions += "update:$activityId"
        updatedActivity = update
    }

    override suspend fun deleteActivity(activityId: String) {
        actions += "delete:$activityId"
    }

}

private fun RunningRemoteActivityStreams.stream(type: String): RunningRemoteStream {
    return streams.single { it.type == type }
}

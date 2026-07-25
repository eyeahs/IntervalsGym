package com.lighthousepark.intervalsgym.running

import com.lighthousepark.intervalsgym.training.RoutineBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sin

class RunningActivityMergeTest {
    @Test
    fun alignRunningHeartRateStreams_findsRemoteTimelineOffset() {
        val localStart = 1_000_000L
        val local = (0..180).map { second ->
            HeartRateSample(
                timestampMillis = localStart + second * 1_000L,
                bpm = syntheticHeartRate(second)
            )
        }
        val remote = (0..210).map { remoteSecond ->
            RunningRemoteHeartRatePoint(
                elapsedSeconds = remoteSecond,
                bpm = syntheticHeartRate(remoteSecond - 12)
            )
        }

        val alignment = alignRunningHeartRateStreams(
            sessionStartedAtMillis = localStart,
            localSamples = local,
            remotePoints = remote,
            expectedOffsetSeconds = 10
        )

        assertNotNull(alignment)
        assertEquals(12, alignment?.offsetSeconds)
        assertTrue((alignment?.correlation ?: 0.0) > 0.99)
    }

    @Test
    fun evaluateRunningActivityMergeCandidate_rejectsDifferentHeartRateShape() {
        val session = completedSession(
            heartRateSamples = (0..180).map { second ->
                HeartRateSample(second * 1_000L, syntheticHeartRate(second))
            }
        )
        val remote = (0..180).map { second ->
            RunningRemoteHeartRatePoint(second, 145)
        }

        val candidate = evaluateRunningActivityMergeCandidate(
            session = session,
            activity = remoteActivity(),
            remoteHeartRate = remote,
            duplicateActivityId = "i-app"
        )

        assertNull(candidate)
    }

    @Test
    fun evaluateRunningActivityMergeCandidate_fallsBackToStartTimeWithoutHeartRate() {
        val candidate = evaluateRunningActivityMergeCandidate(
            session = completedSession(),
            activity = remoteActivity(startedAtMillis = 45_000L, durationSeconds = 170),
            remoteHeartRate = emptyList(),
            duplicateActivityId = "i-app"
        )

        assertNotNull(candidate)
        assertEquals(RunningActivityMergeMatchMethod.START_TIME, candidate?.matchMethod)
        assertEquals(-45, candidate?.offsetSeconds)
    }

    @Test
    fun mergedIntervalsDescription_replacesPreviousSectionAndKeepsOriginalText() {
        val session = completedSession(
            actualBlocks = listOf(
                RoutineBlock(0, "빠르게", "work", "10km/h · 2%", 60, 0, 60, false)
            )
        )
        val activity = remoteActivity(
            description = "Garmin 메모\n\n--- IntervalsGym 병합 ---\nold\n--- 병합 정보 끝 ---"
        )
        val candidate = RunningActivityMergeCandidate(
            activity = activity,
            matchMethod = RunningActivityMergeMatchMethod.HEART_RATE,
            offsetSeconds = 5,
            heartRateCorrelation = 0.91,
            comparedHeartRateSamples = 120,
            startDifferenceSeconds = 5,
            durationDifferenceSeconds = 0,
            duplicateActivityId = "i-app"
        )

        val description = session.mergedIntervalsDescription(candidate)

        assertTrue(description.contains("Garmin 메모"))
        assertTrue(description.contains("심박 그래프 91% 일치"))
        assertTrue(description.contains("00:00–01:00"))
        assertFalse(description.contains("Warmup"))
        assertFalse(description.contains("old"))
    }

    @Test
    fun mergeUpdate_usesAppRecordStartAndEndWithoutWarmup() {
        val session = completedSession(
            warmupSeconds = 60,
            actualBlocks = listOf(
                RoutineBlock(0, "빠르게", "work", "10km/h", 120, 0, 120, false)
            )
        )
        val candidate = RunningActivityMergeCandidate(
            activity = remoteActivity(startedAtMillis = 0L, durationSeconds = 180),
            matchMethod = RunningActivityMergeMatchMethod.START_TIME,
            offsetSeconds = 60,
            heartRateCorrelation = null,
            comparedHeartRateSamples = 0,
            startDifferenceSeconds = 60,
            durationDifferenceSeconds = -60,
            duplicateActivityId = null
        )

        val update = session.runningActivityMergeUpdate(candidate)

        assertEquals(60_000L, update.startedAtMillis)
        assertEquals(120, update.durationSeconds)
        assertFalse(update.description.contains("Warmup"))
        assertTrue(update.description.contains("00:00–02:00"))
    }

    @Test
    fun evaluateCandidate_matchesAgainstAppRecordRangeAfterWarmup() {
        val candidate = evaluateRunningActivityMergeCandidate(
            session = completedSession(warmupSeconds = 60),
            activity = remoteActivity(startedAtMillis = 60_000L, durationSeconds = 120),
            remoteHeartRate = emptyList(),
            duplicateActivityId = null
        )

        assertNotNull(candidate)
        assertEquals(0, candidate?.startDifferenceSeconds)
        assertEquals(0, candidate?.durationDifferenceSeconds)
    }

    @Test
    fun mergedStreams_preserveGarminRouteAndPreferAppHeartRate() {
        val session = completedSession(
            warmupSeconds = 60,
            actualBlocks = listOf(
                RoutineBlock(0, "오르기", "work", "10km/h · 3%", 120, 0, 120, false)
            ),
            heartRateSamples = listOf(
                HeartRateSample(timestampMillis = 60_000L, bpm = 151),
                HeartRateSample(timestampMillis = 120_000L, bpm = 162),
                HeartRateSample(timestampMillis = 180_000L, bpm = 173)
            )
        )
        val source = RunningRemoteActivityStreams(
            streams = listOf(
                RunningRemoteStream("time", listOf(0, 10, 70, 130, 140)),
                RunningRemoteStream(
                    type = "latlng",
                    data = listOf(
                        listOf(37.0, 127.0),
                        listOf(37.1, 127.1),
                        listOf(37.2, 127.2),
                        listOf(37.3, 127.3),
                        listOf(37.4, 127.4)
                    ),
                    attributes = mapOf("name" to "Location")
                ),
                RunningRemoteStream("heartrate", listOf(120, 130, 140, 150, 160))
            )
        )
        val candidate = mergeCandidate(offsetSeconds = 10)

        val merged = session.mergedRunningActivityStreams(candidate, source)

        assertEquals(listOf(0, 60, 120), merged.stream("time").data)
        assertEquals(
            listOf(
                listOf(37.1, 127.1),
                listOf(37.2, 127.2),
                listOf(37.3, 127.3)
            ),
            merged.stream("latlng").data
        )
        assertEquals("Location", merged.stream("latlng").attributes["name"])
        assertEquals(listOf(151, 162, 173), merged.stream("heartrate").data)
        val altitude = merged.stream("altitude").data.map { (it as Number).toDouble() }
        assertEquals(3, altitude.size)
        assertEquals(0.0, altitude[0], 0.01)
        assertEquals(5.0, altitude[1], 0.01)
        assertEquals(10.0, altitude[2], 0.01)
    }

    @Test
    fun mergedStreams_keepGarminHeartRateWhenAppHeartRateIsMissing() {
        val source = RunningRemoteActivityStreams(
            streams = listOf(
                RunningRemoteStream("time", listOf(0, 60, 120, 180)),
                RunningRemoteStream("heartrate", listOf(130, 140, 150, 160))
            )
        )

        val merged = completedSession()
            .mergedRunningActivityStreams(mergeCandidate(), source)

        assertEquals(listOf(130, 140, 150, 160), merged.stream("heartrate").data)
    }

    @Test
    fun mergedStreams_addAppHeartRateRowsWithoutDroppingRouteStream() {
        val session = completedSession(
            heartRateSamples = listOf(HeartRateSample(timestampMillis = 30_000L, bpm = 155))
        )
        val source = RunningRemoteActivityStreams(
            streams = listOf(
                RunningRemoteStream("time", listOf(0, 180)),
                RunningRemoteStream(
                    "latlng",
                    listOf(listOf(37.0, 127.0), listOf(37.1, 127.1))
                )
            )
        )

        val merged = session.mergedRunningActivityStreams(mergeCandidate(), source)

        assertEquals(listOf(0, 30, 180), merged.stream("time").data)
        assertEquals(
            listOf(listOf(37.0, 127.0), null, listOf(37.1, 127.1)),
            merged.stream("latlng").data
        )
        assertEquals(listOf(null, 155, null), merged.stream("heartrate").data)
    }

    private fun completedSession(
        heartRateSamples: List<HeartRateSample> = emptyList(),
        actualBlocks: List<RoutineBlock> = emptyList(),
        warmupSeconds: Int = 0,
    ): CompletedRunningSession {
        return CompletedRunningSession(
            id = "running-1",
            name = "run",
            startedAtMillis = 0L,
            endedAtMillis = 180_000L,
            durationSeconds = 180,
            warmupSeconds = warmupSeconds,
            estimatedDistanceMeters = 0.0,
            blocks = actualBlocks,
            actualBlocks = actualBlocks,
            uploadedToIntervals = true,
            heartRateSamples = heartRateSamples
        )
    }

    private fun remoteActivity(
        startedAtMillis: Long = 0L,
        durationSeconds: Int = 180,
        description: String? = null,
    ): RunningRemoteActivity {
        return RunningRemoteActivity(
            id = "i-garmin",
            name = "Garmin Run",
            type = "Run",
            source = INTERVALS_GARMIN_ACTIVITY_SOURCE,
            externalId = null,
            startedAtMillis = startedAtMillis,
            durationSeconds = durationSeconds,
            description = description
        )
    }

    private fun mergeCandidate(offsetSeconds: Int = 0): RunningActivityMergeCandidate {
        return RunningActivityMergeCandidate(
            activity = remoteActivity(),
            matchMethod = RunningActivityMergeMatchMethod.START_TIME,
            offsetSeconds = offsetSeconds,
            heartRateCorrelation = null,
            comparedHeartRateSamples = 0,
            startDifferenceSeconds = offsetSeconds,
            durationDifferenceSeconds = 0,
            duplicateActivityId = null
        )
    }

    private fun RunningRemoteActivityStreams.stream(type: String): RunningRemoteStream {
        return streams.single { it.type == type }
    }

    private fun syntheticHeartRate(second: Int): Int {
        return 135 +
            (sin(second / 9.0) * 13).toInt() +
            (sin(second / 27.0) * 8).toInt() +
            second.coerceAtLeast(0) / 45
    }
}

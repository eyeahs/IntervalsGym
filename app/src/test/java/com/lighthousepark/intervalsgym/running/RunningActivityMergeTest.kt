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
        assertTrue(description.contains("00:05–01:05"))
        assertFalse(description.contains("old"))
    }

    private fun completedSession(
        heartRateSamples: List<HeartRateSample> = emptyList(),
        actualBlocks: List<RoutineBlock> = emptyList(),
    ): CompletedRunningSession {
        return CompletedRunningSession(
            id = "running-1",
            name = "run",
            startedAtMillis = 0L,
            endedAtMillis = 180_000L,
            durationSeconds = 180,
            warmupSeconds = 0,
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

    private fun syntheticHeartRate(second: Int): Int {
        return 135 +
            (sin(second / 9.0) * 13).toInt() +
            (sin(second / 27.0) * 8).toInt() +
            second.coerceAtLeast(0) / 45
    }
}

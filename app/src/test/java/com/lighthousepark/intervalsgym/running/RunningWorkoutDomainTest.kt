package com.lighthousepark.intervalsgym.running

import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningWorkoutDomainTest {
    @Test
    fun toCompletedRunningSession_storesDokdoRoutePoints() {
        val startedAt = LocalDateTime.of(2026, 6, 25, 7, 0)
        val startedAtMillis = startedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val heartRateSamples = listOf(
            HeartRateSample(timestampMillis = startedAtMillis + 10_000L, bpm = 132),
            HeartRateSample(timestampMillis = startedAtMillis + 70_000L, bpm = 138)
        )
        val session = RunningSession(
            name = "독도 러닝",
            startedAt = startedAt,
            endedAt = startedAt.plusMinutes(11),
            warmupSeconds = 60,
            blocks = listOf(routineBlock(index = 0, durationSeconds = 600, targetText = "10km/h")),
            actualBlocks = listOf(routineBlock(index = 0, durationSeconds = 600, targetText = "10km/h")),
            heartRateSamples = heartRateSamples
        )

        val completed = session.toCompletedRunningSession(uploadedToIntervals = false)

        assertTrue(completed.routePoints.isNotEmpty())
        assertEquals(DOKDO_ROUTE_CENTER_LATITUDE, completed.routePoints.map { it.latitude }.average(), 0.01)
        assertEquals(DOKDO_ROUTE_CENTER_LONGITUDE, completed.routePoints.map { it.longitude }.average(), 0.01)
        assertEquals(startedAtMillis + 60_000L, completed.startedAtMillis)
        assertEquals(600, completed.durationSeconds)
        assertEquals(0, completed.warmupSeconds)
        assertEquals(listOf(heartRateSamples.last()), completed.heartRateSamples)
        assertEquals(0, completed.routePoints.first().elapsedSeconds)
    }

    @Test
    fun buildRunningSessionForFinish_calculatesWarmupAndNormalizesActualTimeline() {
        val startedAt = LocalDateTime.of(2026, 6, 25, 7, 0)
        val startedAtMillis = startedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val blocks = listOf(
            routineBlock(index = 0, durationSeconds = 60),
            routineBlock(index = 1, durationSeconds = 30)
        )

        val session = buildRunningSessionForFinish(
            routineName = "Morning Run",
            startedAtMillis = startedAtMillis,
            endedAtMillis = startedAtMillis + 110_000L,
            blocks = blocks,
            actualBlocks = blocks,
            heartRateSamples = listOf(HeartRateSample(timestampMillis = startedAtMillis + 10_000L, bpm = 130))
        )

        assertEquals(startedAt, session.startedAt)
        assertEquals(20, session.warmupSeconds)
        assertEquals(listOf(0, 60), session.actualBlocks.map { it.startSecond })
        assertEquals(1, session.heartRateSamples.size)
    }

    @Test
    fun buildRunningSessionForFinish_usesRecordedBlocksWhenWorkoutStopsEarly() {
        val startedAt = LocalDateTime.of(2026, 6, 25, 7, 0)
        val startedAtMillis = startedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val plannedBlocks = listOf(routineBlock(index = 0, durationSeconds = 1_800))
        val actualBlocks = listOf(routineBlock(index = 0, durationSeconds = 120))

        val session = buildRunningSessionForFinish(
            routineName = "Early Stop",
            startedAtMillis = startedAtMillis,
            endedAtMillis = startedAtMillis + 420_000L,
            blocks = plannedBlocks,
            actualBlocks = actualBlocks,
            heartRateSamples = emptyList()
        )

        assertEquals(300, session.warmupSeconds)
        assertEquals(listOf(120), session.actualBlocks.map { it.durationSeconds })
    }

    @Test
    fun intervalsDescription_excludesWarmupFromRunningRecord() {
        val startedAt = LocalDateTime.of(2026, 6, 25, 7, 0)
        val session = RunningSession(
            name = "Morning Run",
            startedAt = startedAt,
            endedAt = startedAt.plusMinutes(2),
            warmupSeconds = 60,
            blocks = listOf(routineBlock(index = 0, durationSeconds = 60)),
            actualBlocks = listOf(routineBlock(index = 0, durationSeconds = 60))
        )

        val description = session.toIntervalsDescription()

        assertTrue(description.contains("총 수행 시간: 1분"))
        assertFalse(description.contains("Warmup"))
    }

    @Test
    fun intervalsDescription_includesCalculatedClimb() {
        val startedAt = LocalDateTime.of(2026, 6, 25, 7, 0)
        val session = RunningSession(
            name = "Incline Run",
            startedAt = startedAt,
            endedAt = startedAt.plusMinutes(6),
            warmupSeconds = 0,
            blocks = listOf(routineBlock(index = 0, durationSeconds = 360, targetText = "10km/h · 5%")),
            actualBlocks = listOf(routineBlock(index = 0, durationSeconds = 360, targetText = "10km/h · 5%"))
        )

        assertTrue(session.toIntervalsDescription().contains("예상 상승고도: 50 m"))
    }
}

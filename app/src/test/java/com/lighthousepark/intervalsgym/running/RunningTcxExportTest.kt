package com.lighthousepark.intervalsgym.running

import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningTcxExportTest {
    @Test
    fun buildRunningTcx_containsTrackPositionAndDistanceData() {
        val startedAt = LocalDateTime.of(2026, 6, 25, 7, 0)
        val session = RunningSession(
            name = "Morning & Run",
            startedAt = startedAt,
            endedAt = startedAt.plusMinutes(2),
            warmupSeconds = 60,
            blocks = listOf(routineBlock(index = 0, durationSeconds = 60, targetText = "10km/h")),
            actualBlocks = listOf(routineBlock(index = 0, durationSeconds = 60, targetText = "10km/h"))
        )

        val tcx = session.buildRunningTcx()
        val recordStart = startedAt.plusMinutes(1)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toString()
        val distanceValues = Regex("""<DistanceMeters>([0-9.]+)</DistanceMeters>""")
            .findAll(tcx)
            .mapNotNull { it.groupValues[1].toDoubleOrNull() }
            .toList()

        assertTrue(tcx.contains("""<Activity Sport="Running">"""))
        assertTrue(tcx.contains("<Trackpoint>"))
        assertTrue(tcx.contains("<Position>"))
        assertTrue(tcx.contains("<LatitudeDegrees>"))
        assertTrue(tcx.contains("<LongitudeDegrees>"))
        assertTrue(tcx.contains("<Notes>Morning &amp; Run</Notes>"))
        assertTrue(tcx.contains("""<Lap StartTime="$recordStart">"""))
        assertTrue(tcx.contains("<TotalTimeSeconds>60</TotalTimeSeconds>"))
        assertTrue(distanceValues.last() > 160.0)
    }

    @Test
    fun buildRunningTcx_includesHeartRateSamples() {
        val startedAt = LocalDateTime.of(2026, 6, 25, 7, 0)
        val startedAtMillis = startedAt
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val session = RunningSession(
            name = "Morning Run",
            startedAt = startedAt,
            endedAt = startedAt.plusMinutes(2),
            warmupSeconds = 60,
            blocks = listOf(routineBlock(index = 0, durationSeconds = 60, targetText = "10km/h")),
            actualBlocks = listOf(routineBlock(index = 0, durationSeconds = 60, targetText = "10km/h")),
            heartRateSamples = listOf(
                HeartRateSample(timestampMillis = startedAtMillis - 1_000L, bpm = 99),
                HeartRateSample(timestampMillis = startedAtMillis + 65_000L, bpm = 140),
                HeartRateSample(timestampMillis = startedAtMillis + 66_000L, bpm = 142),
                HeartRateSample(timestampMillis = startedAtMillis + 130_000L, bpm = 150)
            )
        )

        val tcx = session.buildRunningTcx()

        assertTrue(tcx.contains("<AverageHeartRateBpm>"))
        assertTrue(tcx.contains("<MaximumHeartRateBpm>"))
        assertTrue(tcx.contains("<HeartRateBpm>"))
        assertTrue(tcx.contains("<Value>141</Value>"))
        assertTrue(tcx.contains("<Value>142</Value>"))
        assertFalse(tcx.contains("<Value>99</Value>"))
        assertFalse(tcx.contains("<Value>150</Value>"))
    }

    @Test
    fun buildRunningTcx_writesCalculatedClimbAsIncreasingAltitude() {
        val startedAt = LocalDateTime.of(2026, 6, 25, 7, 0)
        val session = RunningSession(
            name = "Incline Run",
            startedAt = startedAt,
            endedAt = startedAt.plusMinutes(6),
            warmupSeconds = 0,
            blocks = listOf(routineBlock(index = 0, durationSeconds = 360, targetText = "10km/h · 5%")),
            actualBlocks = listOf(routineBlock(index = 0, durationSeconds = 360, targetText = "10km/h · 5%"))
        )

        val tcx = session.buildRunningTcx()
        val altitudeValues = Regex("""<AltitudeMeters>([0-9.]+)</AltitudeMeters>""")
            .findAll(tcx)
            .map { it.groupValues[1].toDouble() }
            .toList()

        assertTrue(altitudeValues.size > 2)
        assertEquals(0.0, altitudeValues.first(), 0.01)
        assertEquals(50.0, altitudeValues.last(), 0.01)
        assertTrue(altitudeValues.zipWithNext().all { (first, second) -> second >= first })
    }
}

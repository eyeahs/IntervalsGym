package com.lighthousepark.intervalsgym.core

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppFormattersTest {
    @Test
    fun parseDateTime_acceptsIntervalsStylePrefix() {
        assertEquals(
            LocalDateTime.of(2026, 6, 23, 14, 5, 9),
            parseDateTime("2026-06-23T14:05:09.123")
        )
        assertNull(parseDateTime(""))
    }

    @Test
    fun formatDuration_formatsKoreanDuration() {
        assertEquals("0분", formatDuration(0))
        assertEquals("59분", formatDuration(59 * 60))
        assertEquals("1시간 5분", formatDuration(65 * 60))
    }

    @Test
    fun formatClock_formatsTimerText() {
        assertEquals("00:05", formatClock(5))
        assertEquals("01:05", formatClock(65))
        assertEquals("1:01:05", formatClock(3665))
    }

    @Test
    fun formatGraphTime_formatsShortAndLongDurations() {
        assertEquals("0:05", formatGraphTime(5))
        assertEquals("1:05", formatGraphTime(65))
        assertEquals("1:01", formatGraphTime(3665))
    }

    @Test
    fun formatDistanceAndSummaryMetric_handleZeroNullAndDecimals() {
        val missingMetric: Double? = null

        assertEquals("0 km", formatDistance(0.0))
        assertEquals("3.2 km", formatDistance(3_200.0))
        assertEquals("-", missingMetric.formatSummaryMetric())
        assertEquals("42", 42.0.formatSummaryMetric())
        assertEquals("42.5", 42.5.formatSummaryMetric())
    }

    @Test
    fun formatWeight_trimsWholeNumberDecimals() {
        assertEquals("80", formatWeight(80.0))
        assertEquals("80.5", formatWeight(80.5))
    }
}

package com.lighthousepark.intervalsgym.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayFormattersTest {
    @Test
    fun runningOverlayClockText_formatsMinutesAndClampsNegativeSeconds() {
        assertEquals(250L, RUNNING_OVERLAY_TICK_MILLIS)
        assertEquals("00:00", formatRunningOverlayClockText(-3))
        assertEquals("01:05", formatRunningOverlayClockText(65))
        assertEquals("60:01", formatRunningOverlayClockText(3601))
    }

    @Test
    fun runningOverlayTargetText_includesRepeatProgressBeforeTargets() {
        assertEquals(
            "반복 3 / 8\n속도 3:45 (16km/h)\n경사도 2%\n심박 155 bpm",
            formatRunningOverlayTargetText(
                repeatProgress = "반복 3 / 8",
                targetSpeed = "3:45 (16km/h)",
                targetIncline = "2%",
                heartRateBpm = 155
            )
        )
    }

    @Test
    fun restOverlayText_formatsRemainingTime() {
        assertEquals("휴식\n00:00", formatRestOverlayText(-1))
        assertEquals("휴식\n02:03", formatRestOverlayText(123))
    }

    @Test
    fun workoutStatusClockUsesHourFormatOnlyWhenNeeded() {
        assertEquals("00:00", formatStatusClock(-1))
        assertEquals("59:59", formatStatusClock(3599))
        assertEquals("1:00:00", formatStatusClock(3600))
        assertEquals("1:01:01", formatStatusClock(3661))
    }
}

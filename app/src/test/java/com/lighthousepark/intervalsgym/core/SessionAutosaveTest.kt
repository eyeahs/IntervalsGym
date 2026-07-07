package com.lighthousepark.intervalsgym.core

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionAutosaveTest {
    @Test
    fun sessionAutoLocalSaveAtMillis_usesThirtyMinuteDelay() {
        assertEquals(30L * 60L * 1000L, SESSION_AUTO_LOCAL_SAVE_DELAY_MILLIS)
        assertEquals(2_800_000L, sessionAutoLocalSaveAtMillis(1_000_000L))
    }

    @Test
    fun sessionAutoLocalSaveDelayMillis_clampsElapsedDelayToZero() {
        assertEquals(1_800_000L, sessionAutoLocalSaveDelayMillis(1_000_000L, 1_000_000L))
        assertEquals(0L, sessionAutoLocalSaveDelayMillis(1_000_000L, 2_800_001L))
    }
}

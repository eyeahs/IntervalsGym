package com.lighthousepark.intervalsgym.core

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutAutosaveTest {
    @Test
    fun workoutAutoLocalSaveAtMillis_usesThirtyMinuteDelay() {
        assertEquals(30L * 60L * 1000L, WORKOUT_AUTO_LOCAL_SAVE_DELAY_MILLIS)
        assertEquals(2_800_000L, workoutAutoLocalSaveAtMillis(1_000_000L))
    }

    @Test
    fun workoutAutoLocalSaveDelayMillis_clampsElapsedDelayToZero() {
        assertEquals(1_800_000L, workoutAutoLocalSaveDelayMillis(1_000_000L, 1_000_000L))
        assertEquals(0L, workoutAutoLocalSaveDelayMillis(1_000_000L, 2_800_001L))
    }
}

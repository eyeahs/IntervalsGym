package com.lighthousepark.intervalsgym.training

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingRoutineResultMergeTest {
    @Test
    fun mergeTrainingRoutinesAndResults_pairsSameDaySameSport() {
        val routine = trainingItem(
            id = "routine-1",
            type = "Run",
            isRoutine = true,
            durationSeconds = 1800
        )
        val result = trainingItem(
            id = "activity-1",
            type = "Run",
            isRoutine = false,
            durationSeconds = 1820
        )

        val merged = mergeTrainingRoutinesAndResults(listOf(result), listOf(routine))

        assertEquals(1, merged.size)
        assertSame(routine, merged.single().pairedRoutine)
        assertEquals("merged-routine-1-activity-1", merged.single().id)
    }

    @Test
    fun mergeTrainingRoutinesAndResults_doesNotPairDifferentSportOrDate() {
        val routine = trainingItem(
            id = "routine-run",
            type = "Run",
            isRoutine = true
        )
        val rideResult = trainingItem(
            id = "activity-ride",
            type = "Ride",
            isRoutine = false
        )
        val nextDayRunResult = trainingItem(
            id = "activity-next-day-run",
            type = "Run",
            date = LocalDate.of(2026, 6, 24),
            isRoutine = false
        )

        val merged = mergeTrainingRoutinesAndResults(
            activities = listOf(rideResult, nextDayRunResult),
            routines = listOf(routine)
        )

        assertEquals(3, merged.size)
        assertNull(merged.first { it.id == "activity-ride" }.pairedRoutine)
        assertNull(merged.first { it.id == "activity-next-day-run" }.pairedRoutine)
        assertTrue(merged.any { it.id == "routine-run" && it.isRoutine })
    }

    @Test
    fun mergeTrainingRoutinesAndResults_prefersHighestScoredRoutine() {
        val looseRoutine = trainingItem(
            id = "routine-loose",
            type = "Run",
            name = "Evening Run",
            isRoutine = true,
            durationSeconds = 3600,
            distanceMeters = 10_000.0
        )
        val exactRoutine = trainingItem(
            id = "routine-exact",
            type = "Run",
            name = "Morning Run",
            isRoutine = true,
            durationSeconds = 1800,
            distanceMeters = 5_000.0
        )
        val result = trainingItem(
            id = "activity-1",
            type = "Run",
            name = "Morning Run",
            isRoutine = false,
            durationSeconds = 1810,
            distanceMeters = 5_020.0
        )

        val merged = mergeTrainingRoutinesAndResults(
            activities = listOf(result),
            routines = listOf(looseRoutine, exactRoutine)
        )

        assertEquals(2, merged.size)
        assertSame(exactRoutine, merged.first { it.id == "merged-routine-exact-activity-1" }.pairedRoutine)
        assertTrue(merged.any { it.id == "routine-loose" && it.isRoutine })
    }
}

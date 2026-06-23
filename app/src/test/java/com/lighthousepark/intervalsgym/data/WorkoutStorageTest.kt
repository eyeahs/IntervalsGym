package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.MainActivity
import com.lighthousepark.intervalsgym.R
import com.lighthousepark.intervalsgym.app.*
import com.lighthousepark.intervalsgym.core.*
import com.lighthousepark.intervalsgym.data.*
import com.lighthousepark.intervalsgym.login.*
import com.lighthousepark.intervalsgym.overlay.*
import com.lighthousepark.intervalsgym.running.*
import com.lighthousepark.intervalsgym.running.ui.*
import com.lighthousepark.intervalsgym.strength.*
import com.lighthousepark.intervalsgym.strength.ui.*
import com.lighthousepark.intervalsgym.training.*
import com.lighthousepark.intervalsgym.training.ui.*
import com.lighthousepark.intervalsgym.workout.ui.*

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutStorageTest {
    @Test
    fun visiblePlanDescription_hidesInternalMarkers() {
        val description = """
            설명
            $INTERVALS_GYM_STRENGTH_PLAN_PREFIX encoded
            로컬 러닝 기록
            로컬 러닝 기록 · Garmin 결과 대기
            본문
        """.trimIndent()

        assertEquals("설명\n본문", description.visiblePlanDescription())
    }

    @Test
    fun finalizeRestEvents_closesOnlyActiveOpenRest() {
        val events = listOf(
            StrengthRestEvent(
                id = 1,
                afterSetSequence = 1,
                exerciseEntryId = 1,
                exerciseTitle = "스쿼트",
                setRecordId = 1,
                setIndex = 0,
                startedAtMillis = 1000L,
                plannedSeconds = 60,
                targetEndAtMillis = 61000L,
                endedAtMillis = null,
                endReason = null
            ),
            StrengthRestEvent(
                id = 2,
                afterSetSequence = 2,
                exerciseEntryId = 1,
                exerciseTitle = "스쿼트",
                setRecordId = 2,
                setIndex = 1,
                startedAtMillis = 2000L,
                plannedSeconds = 60,
                targetEndAtMillis = 62000L,
                endedAtMillis = null,
                endReason = null
            )
        )

        val finalized = finalizeRestEvents(events, activeRestEventId = 2, endedAtMillis = 5000L, reason = "stopped")

        assertEquals(null, finalized[0].endedAtMillis)
        assertEquals(5000L, finalized[1].endedAtMillis)
        assertEquals("stopped", finalized[1].endReason)
    }

    @Test
    fun withLocalRunningResults_addsUnmatchedLocalWorkoutInsideRange() {
        val startedAtMillis = LocalDateTime.of(2026, 6, 23, 7, 30)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val localWorkout = CompletedRunningWorkout(
            id = "run-1",
            name = "러닝 Plan",
            startedAtMillis = startedAtMillis,
            endedAtMillis = startedAtMillis + 1_800_000L,
            durationSeconds = 1800,
            warmupSeconds = 60,
            estimatedDistanceMeters = 3000.0,
            blocks = emptyList(),
            actualBlocks = emptyList(),
            uploadedToIntervals = false
        )

        val items = emptyList<TrainingItem>().withLocalRunningResults(
            history = listOf(localWorkout),
            weekStart = LocalDate.of(2026, 6, 22),
            weekEnd = LocalDate.of(2026, 6, 28)
        )

        assertEquals(1, items.size)
        assertTrue(items.single().isLocalOnlyRunningResult)
        assertFalse(items.single().isPlan)
        assertEquals(3000.0, items.single().distanceMeters ?: 0.0, 0.01)
    }
}

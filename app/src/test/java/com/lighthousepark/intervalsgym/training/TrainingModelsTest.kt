package com.lighthousepark.intervalsgym.training

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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingModelsTest {
    @Test
    fun sportType_detectsStrengthRunningCycling() {
        assertEquals(TrainingSportType.STRENGTH, trainingItem(type = "WeightTraining").sportType())
        assertEquals(TrainingSportType.RUNNING, trainingItem(type = "Run").sportType())
        assertEquals(TrainingSportType.CYCLING, trainingItem(type = "Ride").sportType())
    }

    @Test
    fun displayTimeLabel_hidesRoutineAndZeroTime() {
        assertNull(trainingItem(timeLabel = "Routine").displayTimeLabel())
        assertNull(trainingItem(timeLabel = "00:00").displayTimeLabel())
        assertEquals("07:30", trainingItem(timeLabel = "07:30").displayTimeLabel())
    }

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

    @Test
    fun canDragCalendarRoutine_allowsRemoteRoutineAndPairedRoutineWhenLoggedIn() {
        val remoteRoutine = trainingItem(
            id = "routine-remote-1",
            type = "Run",
            isRoutine = true
        )
        val resultWithRoutine = trainingItem(
            id = "activity-1",
            type = "Run",
            isRoutine = false
        ).copy(pairedRoutine = remoteRoutine)

        assertTrue(remoteRoutine.canDragCalendarRoutine(emptySet(), canMoveRemoteRoutines = true))
        assertTrue(resultWithRoutine.canDragCalendarRoutine(emptySet(), canMoveRemoteRoutines = true))
        assertSame(remoteRoutine, resultWithRoutine.calendarRoutineForMove())
    }

    @Test
    fun canDragCalendarRoutine_blocksUnmatchedRemoteRoutineWhenLoggedOut() {
        val remoteRoutine = trainingItem(
            id = "routine-remote-1",
            type = "Ride",
            isRoutine = true
        )

        assertFalse(remoteRoutine.canDragCalendarRoutine(emptySet(), canMoveRemoteRoutines = false))
    }

    @Test
    fun strengthRoutineForDisplay_usesPairedRoutineWhenResultIsMerged() {
        val strengthRoutine = defaultStrengthRoutines().first().copy(id = 55, name = "표시 Routine")
        val pairedRoutine = trainingItem(
            id = "routine-strength",
            type = "Weight Training",
            isRoutine = true
        ).copy(matchedStrengthRoutine = strengthRoutine)
        val result = trainingItem(
            id = "activity-strength",
            type = "Weight Training",
            isRoutine = false
        ).copy(pairedRoutine = pairedRoutine)

        assertSame(strengthRoutine, result.strengthRoutineForDisplay())
    }

    @Test
    fun workoutRoutineBlocksForPreview_usesPairedRunningRoutineBlocksAndDescriptionContext() {
        val pairedRoutine = trainingItem(
            id = "routine-run",
            type = "Run",
            isRoutine = true,
            description = "1m 3:45 pace [16km/h 1%]",
            blocks = listOf(routineBlock(index = 0, targetText = "166.7% · 1%", durationSeconds = 60, startSecond = 0))
        )
        val result = trainingItem(
            id = "activity-run",
            type = "Run",
            isRoutine = false
        ).copy(pairedRoutine = pairedRoutine)

        val previewBlocks = result.workoutRoutineBlocksForPreview()

        assertEquals(1, previewBlocks.size)
        assertEquals("3:45 (16km/h)", previewBlocks.single().runningTargetSpeedText())
        assertEquals("1%", previewBlocks.single().runningInclineText())
    }

    @Test
    fun latestMetricValue_usesStartedAtBeforeDateAndSkipsNulls() {
        val olderWithMetric = trainingItem(
            id = "older",
            fitness = 10.0,
            startedAt = LocalDateTime.of(2026, 6, 23, 9, 0)
        )
        val newerWithoutMetric = trainingItem(
            id = "newer-null",
            fitness = null,
            startedAt = LocalDateTime.of(2026, 6, 24, 9, 0)
        )
        val newestWithMetric = trainingItem(
            id = "newest",
            fitness = 20.0,
            startedAt = LocalDateTime.of(2026, 6, 25, 9, 0)
        )

        assertEquals(
            20.0,
            listOf(olderWithMetric, newerWithoutMetric, newestWithMetric).latestMetricValue { it.fitness } ?: 0.0,
            0.01
        )
    }

    @Test
    fun runningGraphContext_doesNotOverrideExplicitUnitlessRecoverySpeed() {
        val blocks = listOf(
            routineBlock(index = 0, targetText = "2.7-2.8", durationSeconds = 600, startSecond = 0),
            routineBlock(index = 1, targetText = "1.6-1.7", durationSeconds = 60, startSecond = 600)
        )

        val contextualBlocks = blocks.withRunningGraphContext(
            description = "4x\n- 10m 10km/h 6:00 Pace\n- 1m 6km/h 10:00 Pace",
            name = "10m(10km/h,4%) * 4"
        )
        val graphBlocks = contextualBlocks.toWorkoutGraphBlocks(TrainingSportType.RUNNING)

        assertEquals(9.9f, graphBlocks[0].value, 0.2f)
        assertEquals(5.94f, graphBlocks[1].value, 0.2f)
        assertFalse(contextualBlocks[1].targetText.contains("10km/h", ignoreCase = true))
    }

    @Test
    fun runningGraphContext_usesLineMatchedDescriptionTargetsForRepeatedSprint() {
        val rawTargets = listOf(
            "166.7% · 1%",
            "125% · 1%",
            "111.1% · 1%",
            "100% · 1%",
            "83.3% · 1%",
            "166.7% · 1%"
        ) + List(6) {
            listOf(
                "83.3% · 1%",
                "62.5% · 1%",
                "",
                "166.7% · 1%"
            )
        }.flatten()
        val blocks = rawTargets.mapIndexed { index, target ->
            routineBlock(
                index = index,
                targetText = target,
                durationSeconds = 60,
                startSecond = index * 60
            )
        }

        val contextualBlocks = blocks.withRunningGraphContext(
            description = sprintRunDescription(),
            name = "Sprint"
        )
        val graphBlocks = contextualBlocks.toWorkoutGraphBlocks(TrainingSportType.RUNNING)

        assertEquals(WorkoutGraphUnit.SpeedKmh, graphBlocks[7].unit)
        assertEquals(16f, graphBlocks[7].value, 0.01f)
        assertEquals("3:45 (16km/h)", contextualBlocks[7].runningTargetSpeedText())
        assertEquals("1%", contextualBlocks[7].runningInclineText())
        assertEquals(12f, contextualBlocks[6].graphTargetSpeedKmh() ?: 0f, 0.01f)
        assertEquals(6f, contextualBlocks[9].graphTargetSpeedKmh() ?: 0f, 0.01f)
        assertEquals("", contextualBlocks[8].runningTargetSpeedText())
        assertEquals("", contextualBlocks[8].runningInclineText())
    }

    private fun trainingItem(
        id: String = "item",
        type: String = "Workout",
        name: String = type,
        isRoutine: Boolean = false,
        timeLabel: String = "08:00",
        date: LocalDate = LocalDate.of(2026, 6, 23),
        startedAt: LocalDateTime? = null,
        durationSeconds: Int? = null,
        distanceMeters: Double? = null,
        fitness: Double? = null,
        description: String? = null,
        blocks: List<RoutineBlock> = emptyList(),
    ): TrainingItem {
        return TrainingItem(
            id = id,
            remoteId = id,
            externalId = null,
            name = name,
            type = type,
            date = date,
            startedAt = startedAt,
            timeLabel = timeLabel,
            durationSeconds = durationSeconds,
            distanceMeters = distanceMeters,
            weightLiftedKg = null,
            load = null,
            fitness = fitness,
            fatigue = null,
            form = null,
            description = description,
            blocks = blocks,
            isRoutine = isRoutine
        )
    }

    private fun routineBlock(
        index: Int,
        targetText: String,
        durationSeconds: Int,
        startSecond: Int,
    ): RoutineBlock {
        return RoutineBlock(
            index = index,
            title = "Workout",
            kind = "work",
            targetText = targetText,
            durationSeconds = durationSeconds,
            startSecond = startSecond,
            endSecond = startSecond + durationSeconds,
            isRecovery = false
        )
    }

    private fun sprintRunDescription(): String {
        return """
            # Warmup
            - 1m 10:00 pace [6km/h 1%]
            - 1m 7:30 pace [8km/h 1%]
            - 3m 6:40 pace [9km/h 1%]
            - 2m 6:00 pace [10km/h 1%]
            - 1m 5:00 pace [12km/h 1%]
            - 1m 10:00 pace [6km/h 1%]

            # Sprint
            6x
            - 5s 5:00 pace [12km/h 1%] Ramp time
            - 15s 3:45 pace [16km/h 1%] All Out
            - 5s Rest
            - 40s 10:00 pace [6km/h 1%]
        """.trimIndent()
    }
}

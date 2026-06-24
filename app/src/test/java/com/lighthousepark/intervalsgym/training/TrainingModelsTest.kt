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
    fun displayTimeLabel_hidesPlanAndZeroTime() {
        assertNull(trainingItem(timeLabel = "Plan").displayTimeLabel())
        assertNull(trainingItem(timeLabel = "00:00").displayTimeLabel())
        assertEquals("07:30", trainingItem(timeLabel = "07:30").displayTimeLabel())
    }

    @Test
    fun mergeTrainingPlansAndResults_pairsSameDaySameSport() {
        val plan = trainingItem(
            id = "plan-1",
            type = "Run",
            isPlan = true,
            durationSeconds = 1800
        )
        val result = trainingItem(
            id = "activity-1",
            type = "Run",
            isPlan = false,
            durationSeconds = 1820
        )

        val merged = mergeTrainingPlansAndResults(listOf(result), listOf(plan))

        assertEquals(1, merged.size)
        assertSame(plan, merged.single().pairedPlan)
        assertEquals("merged-plan-1-activity-1", merged.single().id)
    }

    @Test
    fun canDragCalendarPlan_allowsRemotePlanAndPairedPlanWhenLoggedIn() {
        val remotePlan = trainingItem(
            id = "plan-remote-1",
            type = "Run",
            isPlan = true
        )
        val resultWithPlan = trainingItem(
            id = "activity-1",
            type = "Run",
            isPlan = false
        ).copy(pairedPlan = remotePlan)

        assertTrue(remotePlan.canDragCalendarPlan(emptySet(), canMoveRemotePlans = true))
        assertTrue(resultWithPlan.canDragCalendarPlan(emptySet(), canMoveRemotePlans = true))
        assertSame(remotePlan, resultWithPlan.calendarPlanForMove())
    }

    @Test
    fun canDragCalendarPlan_blocksUnmatchedRemotePlanWhenLoggedOut() {
        val remotePlan = trainingItem(
            id = "plan-remote-1",
            type = "Ride",
            isPlan = true
        )

        assertFalse(remotePlan.canDragCalendarPlan(emptySet(), canMoveRemotePlans = false))
    }

    @Test
    fun runningGraphContext_doesNotOverrideExplicitUnitlessRecoverySpeed() {
        val blocks = listOf(
            planBlock(index = 0, targetText = "2.7-2.8", durationSeconds = 600, startSecond = 0),
            planBlock(index = 1, targetText = "1.6-1.7", durationSeconds = 60, startSecond = 600)
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

    private fun trainingItem(
        id: String = "item",
        type: String = "Workout",
        name: String = type,
        isPlan: Boolean = false,
        timeLabel: String = "08:00",
        durationSeconds: Int? = null,
    ): TrainingItem {
        return TrainingItem(
            id = id,
            remoteId = id,
            externalId = null,
            name = name,
            type = type,
            date = LocalDate.of(2026, 6, 23),
            startedAt = null,
            timeLabel = timeLabel,
            durationSeconds = durationSeconds,
            distanceMeters = null,
            weightLiftedKg = null,
            load = null,
            fitness = null,
            fatigue = null,
            form = null,
            description = null,
            blocks = emptyList(),
            isPlan = isPlan
        )
    }

    private fun planBlock(
        index: Int,
        targetText: String,
        durationSeconds: Int,
        startSecond: Int,
    ): PlanBlock {
        return PlanBlock(
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
}

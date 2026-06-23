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
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
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
}

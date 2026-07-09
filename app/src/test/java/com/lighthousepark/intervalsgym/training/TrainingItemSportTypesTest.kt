package com.lighthousepark.intervalsgym.training

import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingItemSportTypesTest {
    @Test
    fun sportType_detectsStrengthRunningCycling() {
        assertEquals(TrainingSportType.STRENGTH, trainingItem(type = "WeightTraining").sportType())
        assertEquals(TrainingSportType.RUNNING, trainingItem(type = "Run").sportType())
        assertEquals(TrainingSportType.CYCLING, trainingItem(type = "Ride").sportType())
    }
}

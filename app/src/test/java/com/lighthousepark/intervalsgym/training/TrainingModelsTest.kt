package com.lighthousepark.intervalsgym.training

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingModelsTest {
    @Test
    fun trainingItem_defaultsKeepOptionalPairingAndLocalResultFieldsEmpty() {
        val item = trainingItem()

        assertFalse(item.isRoutine)
        assertTrue(item.actualRunningBlocks.isEmpty())
        assertTrue(item.actualRunningRoutePoints.isEmpty())
    }
}

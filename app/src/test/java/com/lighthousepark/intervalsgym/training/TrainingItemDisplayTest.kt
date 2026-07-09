package com.lighthousepark.intervalsgym.training

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrainingItemDisplayTest {
    @Test
    fun displayTimeLabel_hidesRoutineAndZeroTime() {
        assertNull(trainingItem(timeLabel = "Routine").displayTimeLabel())
        assertNull(trainingItem(timeLabel = "00:00").displayTimeLabel())
        assertEquals("07:30", trainingItem(timeLabel = "07:30").displayTimeLabel())
    }
}

package com.lighthousepark.intervalsgym.training

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutGraphFormattingTest {
    @Test
    fun speedAxisLabelForZeroShowsOnlyZero() {
        assertEquals(listOf("0"), 0f.formatGraphAxisLabels(WorkoutGraphUnit.SpeedKmh))
    }
}

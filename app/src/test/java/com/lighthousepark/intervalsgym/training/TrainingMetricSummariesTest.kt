package com.lighthousepark.intervalsgym.training

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingMetricSummariesTest {
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
}

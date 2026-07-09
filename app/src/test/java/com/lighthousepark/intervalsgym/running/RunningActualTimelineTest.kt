package com.lighthousepark.intervalsgym.running

import com.lighthousepark.intervalsgym.training.RoutineBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningActualTimelineTest {
    @Test
    fun toActualTimeline_rebuildsStartAndEndSeconds() {
        val timeline = listOf(
            routineBlock(index = 7, durationSeconds = 60),
            routineBlock(index = 8, durationSeconds = 30)
        ).toActualTimeline()

        assertEquals(2, timeline.size)
        assertEquals(0, timeline[0].index)
        assertEquals(0, timeline[0].startSecond)
        assertEquals(60, timeline[0].endSecond)
        assertEquals(1, timeline[1].index)
        assertEquals(60, timeline[1].startSecond)
        assertEquals(90, timeline[1].endSecond)
    }

    @Test
    fun scaledToTotalDuration_preservesRequestedTotalAndTimeline() {
        val scaled = listOf(
            routineBlock(index = 0, durationSeconds = 60),
            routineBlock(index = 1, durationSeconds = 60)
        ).scaledToTotalDuration(totalDurationSeconds = 30)

        assertEquals(30, scaled.sumOf { it.durationSeconds })
        assertEquals(0, scaled.first().startSecond)
        assertEquals(30, scaled.last().endSecond)
        assertTrue(scaled.all { it.durationSeconds > 0 })
    }

    @Test
    fun scaledToTotalDuration_returnsEmptyForNonPositiveInput() {
        assertTrue(emptyList<RoutineBlock>().scaledToTotalDuration(totalDurationSeconds = 30).isEmpty())
        assertTrue(listOf(routineBlock(index = 0, durationSeconds = 60)).scaledToTotalDuration(totalDurationSeconds = 0).isEmpty())
        assertTrue(
            listOf(
                routineBlock(index = 0, durationSeconds = 0),
                routineBlock(index = 1, durationSeconds = -30)
            ).scaledToTotalDuration(totalDurationSeconds = 30).isEmpty()
        )
    }

    @Test
    fun normalizedRunningActualBlocks_scalesRoutineWhenActualBlocksAreMissing() {
        val routineBlocks = listOf(
            routineBlock(index = 0, durationSeconds = 60, targetText = "6km/h · 1%"),
            routineBlock(index = 1, durationSeconds = 120, targetText = "12km/h · 3%")
        )

        val normalized = emptyList<RoutineBlock>().normalizedRunningActualBlocks(
            routineBlocks = routineBlocks,
            activeDurationSeconds = 45
        )

        assertEquals(listOf(15, 30), normalized.map { it.durationSeconds })
        assertEquals(listOf(0, 15), normalized.map { it.startSecond })
        assertEquals(listOf(15, 45), normalized.map { it.endSecond })
        assertEquals(listOf("6km/h · 1%", "12km/h · 3%"), normalized.map { it.targetText })
    }

    @Test
    fun normalizedRunningActualBlocks_shortensFullRoutineFallbackToActiveDuration() {
        val routineBlocks = listOf(
            routineBlock(index = 0, durationSeconds = 60, targetText = "6km/h · 1%"),
            routineBlock(index = 1, durationSeconds = 60, targetText = "16km/h · 1%")
        )

        val normalized = routineBlocks.normalizedRunningActualBlocks(
            routineBlocks = routineBlocks,
            activeDurationSeconds = 90
        )

        assertEquals(listOf(45, 45), normalized.map { it.durationSeconds })
        assertEquals(90, normalized.last().endSecond)
        assertEquals("16km/h · 1%", normalized.last().targetText)
    }

    @Test
    fun estimatedRunningDistanceMeters_usesRunningSpeedTargets() {
        val distanceMeters = listOf(
            routineBlock(index = 0, durationSeconds = 3600, targetText = "5km/h")
        ).estimatedRunningDistanceMeters()

        assertEquals(5000.0, distanceMeters, 0.01)
    }

    @Test
    fun recordRunningCurrentBlock_ceilClampsAndKeepsOriginalWhenInactive() {
        val block = routineBlock(index = 0, durationSeconds = 60, targetText = "6km/h")

        val shortRecord = recordRunningCurrentBlock(
            actualBlocks = emptyList(),
            currentBlock = block,
            blockStartedAtMillis = 1_000L,
            endMillis = 1_100L
        )
        val longRecord = recordRunningCurrentBlock(
            actualBlocks = emptyList(),
            currentBlock = block,
            blockStartedAtMillis = 1_000L,
            endMillis = 90_000L
        )
        val inactive = recordRunningCurrentBlock(
            actualBlocks = listOf(block),
            currentBlock = block,
            blockStartedAtMillis = 0L,
            endMillis = 90_000L
        )

        assertEquals(1, shortRecord.recordedBlock?.durationSeconds)
        assertEquals(60, longRecord.recordedBlock?.durationSeconds)
        assertEquals(null, inactive.recordedBlock)
        assertEquals(listOf(block), inactive.actualBlocks)
    }
}

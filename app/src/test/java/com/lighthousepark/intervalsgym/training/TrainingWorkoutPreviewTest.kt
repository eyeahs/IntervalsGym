package com.lighthousepark.intervalsgym.training

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TrainingWorkoutPreviewTest {
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
}

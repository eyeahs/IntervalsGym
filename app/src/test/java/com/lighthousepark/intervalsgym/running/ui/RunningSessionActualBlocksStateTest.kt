package com.lighthousepark.intervalsgym.running.ui

import com.lighthousepark.intervalsgym.running.routineBlock
import org.junit.Assert.assertEquals
import org.junit.Test

class RunningSessionActualBlocksStateTest {
    @Test
    fun restoredParsesSavedJsonIntoBlocks() {
        val saved = RunningSessionActualBlocksState(
            blocks = emptyList(),
            json = "[]"
        ).withBlocks(listOf(routineBlock(index = 0, durationSeconds = 45, targetText = "6km/h")))

        val restored = RunningSessionActualBlocksState.restored(saved.json)

        assertEquals(saved.blocks, restored.blocks)
        assertEquals(saved.json, restored.json)
    }

    @Test
    fun withBlocksUpdatesJsonAndBlockListTogether() {
        val initial = RunningSessionActualBlocksState.restored("[]")
        val nextBlocks = listOf(
            routineBlock(index = 0, durationSeconds = 30, targetText = "6km/h"),
            routineBlock(index = 1, durationSeconds = 60, targetText = "8km/h")
        )

        val updated = initial.withBlocks(nextBlocks)
        val restored = RunningSessionActualBlocksState.restored(updated.json)

        assertEquals(nextBlocks, updated.blocks)
        assertEquals(nextBlocks, restored.blocks)
    }
}

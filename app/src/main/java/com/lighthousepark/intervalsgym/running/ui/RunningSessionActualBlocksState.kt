package com.lighthousepark.intervalsgym.running.ui

import com.lighthousepark.intervalsgym.data.toRoutineBlocksJsonArray
import com.lighthousepark.intervalsgym.running.runningBlocksFromJson
import com.lighthousepark.intervalsgym.training.RoutineBlock

internal data class RunningSessionActualBlocksState(
    val blocks: List<RoutineBlock>,
    val json: String,
) {
    fun withBlocks(blocks: List<RoutineBlock>): RunningSessionActualBlocksState {
        return RunningSessionActualBlocksState(
            blocks = blocks,
            json = blocks.toRoutineBlocksJsonArray().toString()
        )
    }

    companion object {
        fun restored(json: String): RunningSessionActualBlocksState {
            return RunningSessionActualBlocksState(
                blocks = runningBlocksFromJson(json),
                json = json
            )
        }
    }
}

package com.lighthousepark.intervalsgym.running

import com.lighthousepark.intervalsgym.training.RoutineBlock

internal fun routineBlock(
    index: Int,
    durationSeconds: Int,
    targetText: String = "",
): RoutineBlock {
    return RoutineBlock(
        index = index,
        title = "Block ${index + 1}",
        kind = "work",
        targetText = targetText,
        durationSeconds = durationSeconds,
        startSecond = 0,
        endSecond = 0,
        isRecovery = false
    )
}

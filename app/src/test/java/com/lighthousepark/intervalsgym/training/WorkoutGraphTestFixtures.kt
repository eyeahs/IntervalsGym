package com.lighthousepark.intervalsgym.training

internal fun graphTestBlock(
    index: Int = 0,
    title: String = "Block 1",
    kind: String = "Run",
    targetText: String,
    durationSeconds: Int = 60,
    startSecond: Int = 0,
    endSecond: Int = startSecond + durationSeconds,
    isRecovery: Boolean = false,
): RoutineBlock {
    return RoutineBlock(
        index = index,
        title = title,
        kind = kind,
        targetText = targetText,
        durationSeconds = durationSeconds,
        startSecond = startSecond,
        endSecond = endSecond,
        isRecovery = isRecovery
    )
}

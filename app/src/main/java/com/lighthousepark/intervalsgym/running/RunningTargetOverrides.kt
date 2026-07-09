package com.lighthousepark.intervalsgym.running

import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.training.formatKmh
import com.lighthousepark.intervalsgym.training.graphTargetSpeedKmh
import com.lighthousepark.intervalsgym.training.runningInclinePercent
import kotlin.math.roundToInt

internal data class RunningTargetOverrideChange(
    val targetOverrides: List<String>,
    val nextSpeedKmh: Float,
    val nextInclinePercent: Float,
    val targetText: String,
)

internal const val RUNNING_SPEED_STEP_KMH = 0.5f
internal const val RUNNING_INCLINE_STEP_PERCENT = 0.5f
internal const val MAX_RUNNING_SPEED_KMH = 30f
internal const val MAX_RUNNING_INCLINE_PERCENT = 30f

internal fun RoutineBlock.withRunningTargetOverride(
    speedKmh: Float,
    inclinePercent: Float,
): RoutineBlock {
    return copy(targetText = runningTargetOverrideText(speedKmh, inclinePercent))
}

internal fun runningTargetOverrideText(
    speedKmh: Float,
    inclinePercent: Float,
): String {
    return listOf(
        formatKmh(speedKmh.coerceIn(0f, MAX_RUNNING_SPEED_KMH)),
        formatRunningInclinePercent(inclinePercent.coerceIn(0f, MAX_RUNNING_INCLINE_PERCENT))
    ).joinToString(" · ")
}

internal fun formatRunningInclinePercent(inclinePercent: Float): String {
    val safeIncline = inclinePercent.coerceIn(0f, MAX_RUNNING_INCLINE_PERCENT)
    return if (safeIncline % 1f == 0f) {
        "${safeIncline.roundToInt()}%"
    } else {
        String.format(java.util.Locale.US, "%.1f%%", safeIncline)
    }
}

internal fun runningTargetOverrideChange(
    blocks: List<RoutineBlock>,
    displayBlocks: List<RoutineBlock>,
    targetOverrides: List<String>,
    currentBlockIndex: Int,
    speedDeltaKmh: Float = 0f,
    inclineDeltaPercent: Float = 0f,
): RunningTargetOverrideChange? {
    val originalBlock = blocks.getOrNull(currentBlockIndex) ?: return null
    val activeBlock = displayBlocks.getOrNull(currentBlockIndex) ?: originalBlock
    val nextSpeed = ((activeBlock.graphTargetSpeedKmh() ?: 0f) + speedDeltaKmh)
        .coerceIn(0f, MAX_RUNNING_SPEED_KMH)
    val nextIncline = ((activeBlock.runningInclinePercent() ?: 0f) + inclineDeltaPercent)
        .coerceIn(0f, MAX_RUNNING_INCLINE_PERCENT)
    val targetText = runningTargetOverrideText(nextSpeed, nextIncline)
    val nextTargets = targetOverrides.toMutableList().apply {
        while (size < blocks.size) add("")
        this[currentBlockIndex] = targetText
    }
    return RunningTargetOverrideChange(
        targetOverrides = nextTargets,
        nextSpeedKmh = nextSpeed,
        nextInclinePercent = nextIncline,
        targetText = targetText
    )
}

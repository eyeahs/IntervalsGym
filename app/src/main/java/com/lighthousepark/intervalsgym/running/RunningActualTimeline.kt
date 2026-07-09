package com.lighthousepark.intervalsgym.running

import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.training.graphTargetSpeedKmh
import kotlin.math.roundToInt

internal data class RunningRecordedBlockResult(
    val actualBlocks: List<RoutineBlock>,
    val recordedBlock: RoutineBlock?,
)

internal fun List<RoutineBlock>.toActualTimeline(): List<RoutineBlock> {
    var cursor = 0
    return mapIndexedNotNull { index, block ->
        val duration = block.durationSeconds.coerceAtLeast(0)
        if (duration <= 0) return@mapIndexedNotNull null
        val start = cursor
        cursor += duration
        block.copy(
            index = index,
            durationSeconds = duration,
            startSecond = start,
            endSecond = cursor
        )
    }
}

internal fun List<RoutineBlock>.normalizedRunningActualBlocks(
    routineBlocks: List<RoutineBlock>,
    activeDurationSeconds: Int,
): List<RoutineBlock> {
    if (isEmpty()) {
        return if (activeDurationSeconds > 0 && routineBlocks.isNotEmpty()) {
            routineBlocks.scaledToTotalDuration(activeDurationSeconds)
        } else {
            emptyList()
        }
    }
    val routineDurationSeconds = routineBlocks.sumOf { it.durationSeconds.coerceAtLeast(0) }
    val actualDurationSeconds = sumOf { it.durationSeconds.coerceAtLeast(0) }
    val looksLikeRoutineFallback = routineBlocks.isNotEmpty() &&
        actualDurationSeconds == routineDurationSeconds &&
        activeDurationSeconds in 1 until routineDurationSeconds &&
        sameRunningTimelineAs(routineBlocks)
    return if (looksLikeRoutineFallback) {
        scaledToTotalDuration(activeDurationSeconds)
    } else {
        toActualTimeline()
    }
}

internal fun List<RoutineBlock>.scaledToTotalDuration(totalDurationSeconds: Int): List<RoutineBlock> {
    val safeTotalDuration = totalDurationSeconds.coerceAtLeast(0)
    val originalTotalDuration = sumOf { it.durationSeconds.coerceAtLeast(0) }
    if (safeTotalDuration <= 0 || originalTotalDuration <= 0) return emptyList()
    var remainingDuration = safeTotalDuration
    return mapIndexedNotNull { index, block ->
        if (remainingDuration <= 0) return@mapIndexedNotNull null
        val originalDuration = block.durationSeconds.coerceAtLeast(0)
        if (originalDuration <= 0) return@mapIndexedNotNull null
        val scaledDuration = if (index == lastIndex) {
            remainingDuration
        } else {
            ((originalDuration.toDouble() / originalTotalDuration.toDouble()) * safeTotalDuration)
                .roundToInt()
                .coerceAtLeast(1)
                .coerceAtMost(remainingDuration)
        }
        remainingDuration -= scaledDuration
        block.copy(durationSeconds = scaledDuration)
    }.toActualTimeline()
}

internal fun List<RoutineBlock>.estimatedRunningDistanceMeters(): Double {
    return sumOf { block ->
        val speedKmh = block.graphTargetSpeedKmh()?.toDouble() ?: return@sumOf 0.0
        speedKmh * 1000.0 * block.durationSeconds.coerceAtLeast(0).toDouble() / 3600.0
    }
}

internal fun recordRunningCurrentBlock(
    actualBlocks: List<RoutineBlock>,
    currentBlock: RoutineBlock?,
    blockStartedAtMillis: Long,
    endMillis: Long,
): RunningRecordedBlockResult {
    val block = currentBlock ?: return RunningRecordedBlockResult(actualBlocks, recordedBlock = null)
    if (blockStartedAtMillis <= 0L) return RunningRecordedBlockResult(actualBlocks, recordedBlock = null)
    val maxSeconds = block.durationSeconds.coerceAtLeast(0)
    val actualSeconds = (((endMillis - blockStartedAtMillis).coerceAtLeast(0L) + 999L) / 1000L)
        .toInt()
        .coerceIn(0, maxSeconds)
        .let { seconds ->
            if (maxSeconds > 0) seconds.coerceAtLeast(1) else 0
        }
    val recordedBlock = block.copy(durationSeconds = actualSeconds)
    return RunningRecordedBlockResult(
        actualBlocks = actualBlocks + recordedBlock,
        recordedBlock = recordedBlock
    )
}

private fun List<RoutineBlock>.sameRunningTimelineAs(other: List<RoutineBlock>): Boolean {
    if (size != other.size) return false
    return zip(other).all { (left, right) ->
        left.title == right.title &&
            left.kind == right.kind &&
            left.targetText == right.targetText &&
            left.durationSeconds == right.durationSeconds
    }
}

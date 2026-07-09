package com.lighthousepark.intervalsgym.running

import com.lighthousepark.intervalsgym.training.RoutineBlock

internal data class RunningSessionCatchUpResult(
    val currentBlockIndex: Int,
    val blockStartedAtMillis: Long,
    val blockEndAtMillis: Long,
    val actualBlocks: List<RoutineBlock>,
    val finishedAtMillis: Long? = null,
)

internal fun catchUpRunningSessionBlocks(
    blocks: List<RoutineBlock>,
    currentBlockIndex: Int,
    blockStartedAtMillis: Long,
    blockEndAtMillis: Long,
    actualBlocks: List<RoutineBlock>,
    nowMillis: Long,
): RunningSessionCatchUpResult? {
    if (blocks.isEmpty() || blockStartedAtMillis <= 0L) return null
    val safeCurrentIndex = currentBlockIndex.coerceIn(0, blocks.lastIndex)
    val currentBlock = blocks[safeCurrentIndex]
    val activeBlockEndAtMillis = blockEndAtMillis.takeIf { it > 0L }
        ?: (blockStartedAtMillis + currentBlock.durationSeconds.toDurationMillis())
    val nextActualBlocks = actualBlocks.take(safeCurrentIndex).toMutableList()
    if (nextActualBlocks.size < safeCurrentIndex) {
        for (index in nextActualBlocks.size until safeCurrentIndex) {
            nextActualBlocks += blocks[index].asFullActualBlock()
        }
    }

    if (nowMillis < activeBlockEndAtMillis) {
        return if (
            safeCurrentIndex != currentBlockIndex ||
            activeBlockEndAtMillis != blockEndAtMillis ||
            nextActualBlocks != actualBlocks
        ) {
            RunningSessionCatchUpResult(
                currentBlockIndex = safeCurrentIndex,
                blockStartedAtMillis = blockStartedAtMillis,
                blockEndAtMillis = activeBlockEndAtMillis,
                actualBlocks = nextActualBlocks
            )
        } else {
            null
        }
    }

    nextActualBlocks += currentBlock.asFullActualBlock()
    var nextBlockStartAtMillis = activeBlockEndAtMillis
    for (index in (safeCurrentIndex + 1) until blocks.size) {
        val block = blocks[index]
        val nextBlockEndAtMillis = nextBlockStartAtMillis + block.durationSeconds.toDurationMillis()
        if (nowMillis < nextBlockEndAtMillis) {
            return RunningSessionCatchUpResult(
                currentBlockIndex = index,
                blockStartedAtMillis = nextBlockStartAtMillis,
                blockEndAtMillis = nextBlockEndAtMillis,
                actualBlocks = nextActualBlocks
            )
        }
        nextActualBlocks += block.asFullActualBlock()
        nextBlockStartAtMillis = nextBlockEndAtMillis
    }

    return RunningSessionCatchUpResult(
        currentBlockIndex = blocks.lastIndex,
        blockStartedAtMillis = nextBlockStartAtMillis,
        blockEndAtMillis = 0L,
        actualBlocks = nextActualBlocks,
        finishedAtMillis = nextBlockStartAtMillis
    )
}

private fun RoutineBlock.asFullActualBlock(): RoutineBlock {
    return copy(durationSeconds = durationSeconds.coerceAtLeast(0))
}

private fun Int.toDurationMillis(): Long {
    return coerceAtLeast(0).toLong() * 1000L
}

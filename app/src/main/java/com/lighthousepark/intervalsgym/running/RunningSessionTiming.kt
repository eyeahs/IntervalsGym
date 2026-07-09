package com.lighthousepark.intervalsgym.running

import com.lighthousepark.intervalsgym.core.sessionAutoLocalSaveAtMillis
import com.lighthousepark.intervalsgym.core.sessionAutoLocalSaveDelayMillis

internal fun shouldAutoLocalSaveLastRunningBlock(
    currentBlockIndex: Int,
    blockCount: Int,
    blockEndAtMillis: Long,
    nowMillis: Long,
): Boolean {
    return blockCount > 0 &&
        currentBlockIndex == blockCount - 1 &&
        blockEndAtMillis > 0L &&
        nowMillis >= sessionAutoLocalSaveAtMillis(blockEndAtMillis)
}

internal fun runningAutoLocalSaveAtMillis(finishedAtMillis: Long): Long {
    return sessionAutoLocalSaveAtMillis(finishedAtMillis)
}

internal fun runningAutoLocalSaveDelayMillis(
    finishedAtMillis: Long,
    nowMillis: Long,
): Long {
    return sessionAutoLocalSaveDelayMillis(
        finishedAtMillis = finishedAtMillis,
        nowMillis = nowMillis
    )
}

package com.lighthousepark.intervalsgym.core

internal fun remainingCountdownSeconds(
    endAtMillis: Long,
    nowMillis: Long,
): Int {
    if (endAtMillis <= 0L) return 0
    val remainingMillis = (endAtMillis - nowMillis).coerceAtLeast(0L)
    val wholeSeconds = remainingMillis / 1_000L
    val partialSecond = remainingMillis % 1_000L
    return (wholeSeconds + if (partialSecond > 0L) 1L else 0L)
        .coerceAtMost(Int.MAX_VALUE.toLong())
        .toInt()
}

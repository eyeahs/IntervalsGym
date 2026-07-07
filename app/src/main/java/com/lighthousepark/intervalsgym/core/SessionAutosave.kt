package com.lighthousepark.intervalsgym.core

internal const val SESSION_AUTO_LOCAL_SAVE_DELAY_MILLIS = 30L * 60L * 1000L

internal fun sessionAutoLocalSaveAtMillis(finishedAtMillis: Long): Long {
    return finishedAtMillis + SESSION_AUTO_LOCAL_SAVE_DELAY_MILLIS
}

internal fun sessionAutoLocalSaveDelayMillis(
    finishedAtMillis: Long,
    nowMillis: Long,
): Long {
    return (sessionAutoLocalSaveAtMillis(finishedAtMillis) - nowMillis).coerceAtLeast(0L)
}

package com.lighthousepark.intervalsgym.core

internal const val WORKOUT_AUTO_LOCAL_SAVE_DELAY_MILLIS = 30L * 60L * 1000L

internal fun workoutAutoLocalSaveAtMillis(finishedAtMillis: Long): Long {
    return finishedAtMillis + WORKOUT_AUTO_LOCAL_SAVE_DELAY_MILLIS
}

internal fun workoutAutoLocalSaveDelayMillis(
    finishedAtMillis: Long,
    nowMillis: Long,
): Long {
    return (workoutAutoLocalSaveAtMillis(finishedAtMillis) - nowMillis).coerceAtLeast(0L)
}

package com.lighthousepark.intervalsgym.training

internal enum class TrainingSportType {
    RUNNING,
    CYCLING,
    STRENGTH,
    OTHER
}

internal data class RoutineBlock(
    val index: Int,
    val title: String,
    val kind: String,
    val targetText: String,
    val durationSeconds: Int,
    val startSecond: Int,
    val endSecond: Int,
    val isRecovery: Boolean,
)

internal enum class WorkoutGraphUnit {
    Watts,
    Percent,
    SpeedKmh,
}

internal data class WorkoutGraphBlock(
    val block: RoutineBlock,
    val value: Float,
    val unit: WorkoutGraphUnit,
    val intensityPercent: Float? = null,
)

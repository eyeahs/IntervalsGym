package com.lighthousepark.intervalsgym.training

internal fun List<RoutineBlock>.toWorkoutGraphBlocks(sportType: TrainingSportType): List<WorkoutGraphBlock> {
    data class RawGraphBlock(
        val block: RoutineBlock,
        val watts: Float?,
        val speedKmh: Float?,
        val percent: Float?,
    )

    val rawBlocks = map { block ->
        val watts = block.graphTargetWatts(sportType)
        val percent = block.graphTargetPercent()
        val speedKmh = if (sportType == TrainingSportType.CYCLING) null else block.graphTargetSpeedKmh()
        RawGraphBlock(block, watts, speedKmh, percent)
    }
    val inferredCyclingFtp = if (sportType == TrainingSportType.CYCLING) {
        rawBlocks.mapNotNull { raw ->
            val watts = raw.watts ?: return@mapNotNull null
            val percent = raw.percent?.takeIf { it > 0f } ?: return@mapNotNull null
            watts / (percent / 100f)
        }.takeIf { it.isNotEmpty() }?.average()?.toFloat()
            ?: rawBlocks.mapNotNull { it.watts }.maxOrNull()
    } else {
        null
    }

    return rawBlocks.map { raw ->
        val watts = raw.watts
        val percent = raw.percent
        val speedKmh = raw.speedKmh
        val cyclingIntensity = if (sportType == TrainingSportType.CYCLING) {
            percent ?: watts?.let { value ->
                inferredCyclingFtp?.takeIf { it > 0f }?.let { ftp -> value / ftp * 100f }
            }
        } else {
            percent
        }
        WorkoutGraphBlock(
            block = raw.block,
            value = watts ?: speedKmh ?: percent ?: 0f,
            unit = when {
                watts != null -> WorkoutGraphUnit.Watts
                speedKmh != null -> WorkoutGraphUnit.SpeedKmh
                percent != null -> WorkoutGraphUnit.Percent
                else -> WorkoutGraphUnit.Percent
            },
            intensityPercent = cyclingIntensity
        )
    }
}

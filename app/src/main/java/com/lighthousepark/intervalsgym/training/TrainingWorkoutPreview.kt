package com.lighthousepark.intervalsgym.training

import kotlin.math.abs

internal fun TrainingItem.workoutRoutineBlocksForPreview(): List<RoutineBlock> {
    val sportType = sportType()
    if (sportType != TrainingSportType.RUNNING && sportType != TrainingSportType.CYCLING) return emptyList()
    if (!isRoutine && pairedRoutine == null) return emptyList()
    val sourceBlocks = blocks.takeIf { it.isNotEmpty() }
        ?: pairedRoutine?.blocks?.takeIf { it.isNotEmpty() }
        ?: emptyList()
    val sourceDescription = description ?: pairedRoutine?.description
    return when (sportType) {
        TrainingSportType.RUNNING -> sourceBlocks.withRunningGraphContext(
            description = sourceDescription,
            name = name.ifBlank { pairedRoutine?.name.orEmpty() }
        )
        TrainingSportType.CYCLING -> sourceBlocks.withCyclingGraphContext(sourceDescription)
        else -> sourceBlocks
    }
}

internal fun TrainingItem.workoutRoutineTotalSecondsForPreview(blocks: List<RoutineBlock>): Int {
    return durationSeconds
        ?: pairedRoutine?.durationSeconds
        ?: blocks.sumOf { it.durationSeconds }
}

internal fun List<RoutineBlock>.withRunningGraphContext(
    description: String?,
    name: String,
): List<RoutineBlock> {
    val contexts = runningTargetContextSequence(description, size)
    if (contexts.size == size) {
        return mapIndexed { index, block ->
            val context = contexts[index]
            if (context.isBlank() || block.targetText.contains(context, ignoreCase = true)) {
                block
            } else {
                block.copy(targetText = listOf(block.targetText, context).filter { it.isNotBlank() }.joinToString(" · "))
            }
        }
    }

    val context = listOf(description.orEmpty(), name)
        .firstNotNullOfOrNull { it.runningPaceOrSpeedContext() }
        ?: return this
    val hasExplicitTargets = any { it.targetText.isNotBlank() }
    val contextSpeedKmh = context.runningContextSpeedKmh()
    return map { block ->
        val blockSpeedKmh = block.graphTargetSpeedKmh()
        val shouldApply = if (hasExplicitTargets) {
            block.targetText.isNotBlank()
        } else {
            !block.isRecovery
        }
        val shouldAppendContext = when {
            !shouldApply -> false
            block.targetText.contains(context, ignoreCase = true) -> false
            blockSpeedKmh == null -> true
            contextSpeedKmh == null -> false
            else -> abs(blockSpeedKmh - contextSpeedKmh) <= 0.25f
        }
        if (shouldAppendContext) {
            block.copy(targetText = listOf(block.targetText, context).filter { it.isNotBlank() }.joinToString(" · "))
        } else {
            block
        }
    }
}

private fun String.runningContextSpeedKmh(): Float? {
    return RoutineBlock(
        index = -1,
        title = "",
        kind = "",
        targetText = this,
        durationSeconds = 0,
        startSecond = 0,
        endSecond = 0,
        isRecovery = false
    ).graphTargetSpeedKmh()
}

internal fun List<RoutineBlock>.withCyclingGraphContext(description: String?): List<RoutineBlock> {
    if (description.isNullOrBlank() || isEmpty()) return this
    val contexts = cyclingPowerContextSequence(description, size).takeIf { it.size == size } ?: return this
    return mapIndexed { index, block ->
        val context = contexts[index]
        if (context.isBlank() || block.targetText.contains(context, ignoreCase = true)) {
            block
        } else {
            block.copy(targetText = listOf(block.targetText, context).filter { it.isNotBlank() }.joinToString(" · "))
        }
    }
}

internal fun String.runningPaceOrSpeedContext(): String? {
    val paceMatch = Regex("""\d{1,2}:\d{2}\s*(?:/km|pace)?""", RegexOption.IGNORE_CASE).find(this)
    val speedMatch = Regex("""\d+(?:\.\d+)?\s*km\s*/?\s*h""", RegexOption.IGNORE_CASE).find(this)
    val match = speedMatch ?: paceMatch ?: return null
    val segment = runningContextSegment(match.range.first, match.range.last + 1)
    return if (Regex("""\d+(?:\.\d+)?\s*%""").containsMatchIn(segment)) {
        segment
    } else {
        match.value.trim()
    }
}

private fun String.runningContextSegment(matchStart: Int, matchEnd: Int): String {
    val startDelimiters = listOf(
        lastIndexOf('\n', startIndex = matchStart.coerceAtLeast(0)),
        lastIndexOf(';', startIndex = matchStart.coerceAtLeast(0)),
        lastIndexOf('·', startIndex = matchStart.coerceAtLeast(0)),
        lastIndexOf('(', startIndex = matchStart.coerceAtLeast(0))
    )
    val endDelimiters = listOf(
        indexOf('\n', startIndex = matchEnd.coerceAtMost(length)),
        indexOf(';', startIndex = matchEnd.coerceAtMost(length)),
        indexOf('·', startIndex = matchEnd.coerceAtMost(length)),
        indexOf(')', startIndex = matchEnd.coerceAtMost(length))
    ).filter { it >= 0 }
    val start = ((startDelimiters.maxOrNull() ?: -1) + 1).coerceIn(0, length)
    val end = (endDelimiters.minOrNull() ?: length).coerceIn(start, length)
    return substring(start, end).trim()
}

package com.lighthousepark.intervalsgym.training

import com.lighthousepark.intervalsgym.MainActivity
import com.lighthousepark.intervalsgym.R
import com.lighthousepark.intervalsgym.app.*
import com.lighthousepark.intervalsgym.core.*
import com.lighthousepark.intervalsgym.data.*
import com.lighthousepark.intervalsgym.login.*
import com.lighthousepark.intervalsgym.overlay.*
import com.lighthousepark.intervalsgym.running.*
import com.lighthousepark.intervalsgym.running.ui.*
import com.lighthousepark.intervalsgym.strength.*
import com.lighthousepark.intervalsgym.strength.ui.*
import com.lighthousepark.intervalsgym.training.*
import com.lighthousepark.intervalsgym.training.ui.*
import com.lighthousepark.intervalsgym.workout.ui.*

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale
import kotlin.math.abs

internal data class WeekTrainingData(
    val activities: List<TrainingItem>,
    val routines: List<TrainingItem>,
)

internal data class TrainingItem(
    val id: String,
    val remoteId: String,
    val externalId: String?,
    val name: String,
    val type: String,
    val date: LocalDate,
    val startedAt: LocalDateTime?,
    val timeLabel: String,
    val durationSeconds: Int?,
    val distanceMeters: Double?,
    val weightLiftedKg: Double?,
    val load: Int?,
    val fitness: Double?,
    val fatigue: Double?,
    val form: Double?,
    val description: String?,
    val blocks: List<RoutineBlock>,
    val isRoutine: Boolean,
    val matchedStrengthSession: CompletedStrengthSession? = null,
    val matchedStrengthRoutine: StrengthWorkoutRoutine? = null,
    val isLocalOnlyStrengthResult: Boolean = false,
    val isLocalOnlyRunningResult: Boolean = false,
    val actualRunningBlocks: List<RoutineBlock> = emptyList(),
    val actualRunningRoutePoints: List<RunningRoutePoint> = emptyList(),
    val pairedRoutine: TrainingItem? = null,
    val workoutDocJson: String? = null,
)

internal fun TrainingItem.isWeightTrainingItem(): Boolean {
    val searchable = listOf(type, name, description.orEmpty())
        .joinToString(" ")
        .lowercase(Locale.KOREAN)
        .replace(" ", "")
        .replace("_", "")
        .replace("-", "")
    return isLocalOnlyStrengthResult ||
        matchedStrengthSession != null ||
        searchable.contains("weighttraining") ||
        searchable.contains("웨이트") ||
        searchable.contains("strength")
}

internal fun TrainingItem.isRunningItem(): Boolean {
    val searchable = listOf(type, name).joinToString(" ").lowercase(Locale.KOREAN).replace(" ", "")
    return searchable.contains("run") ||
        searchable.contains("running") ||
        searchable.contains("러닝") ||
        searchable.contains("런닝") ||
        searchable.contains("달리기")
}

internal fun TrainingItem.isCyclingItem(): Boolean {
    val searchable = listOf(type, name).joinToString(" ").lowercase(Locale.KOREAN).replace(" ", "")
    return searchable.contains("ride") ||
        searchable.contains("bike") ||
        searchable.contains("bicycle") ||
        searchable.contains("cycling") ||
        searchable.contains("cycle") ||
        searchable.contains("자전거") ||
        searchable.contains("사이클")
}

internal fun TrainingItem.sportType(): TrainingSportType {
    return when {
        isWeightTrainingItem() -> TrainingSportType.STRENGTH
        isCyclingItem() -> TrainingSportType.CYCLING
        isRunningItem() -> TrainingSportType.RUNNING
        else -> TrainingSportType.OTHER
    }
}

internal fun TrainingItem.displayTimeLabel(): String? {
    val value = timeLabel.trim()
    return value.takeUnless {
        it.isBlank() ||
            it == "00:00" ||
            it == "--:--" ||
            it.equals("Routine", ignoreCase = true)
    }
}

internal fun TrainingItem.plannedWorkoutDeleteConfirmMessage(): String {
    return plannedWorkoutDeleteConfirmMessage(date, name)
}

internal fun TrainingItem.strengthRoutineForDisplay(): StrengthWorkoutRoutine? {
    if (sportType() != TrainingSportType.STRENGTH) return null
    if (!isRoutine && pairedRoutine == null) return null
    return matchedStrengthRoutine
        ?: pairedRoutine?.matchedStrengthRoutine
        ?: description.toIntervalsGymStrengthRoutine()
        ?: pairedRoutine?.description.toIntervalsGymStrengthRoutine()
}

internal fun TrainingItem.calendarRoutineForMove(): TrainingItem? {
    return when {
        isRoutine -> this
        pairedRoutine?.isRoutine == true -> pairedRoutine
        else -> null
    }
}

internal fun TrainingItem.canDragCalendarRoutine(
    movableLocalRoutineKeys: Set<String>,
    canMoveRemoteRoutines: Boolean,
): Boolean {
    val routine = calendarRoutineForMove() ?: return false
    val isMovableLocalStrengthRoutine = listOfNotNull(
        routine.id,
        routine.id.removePrefix("local-"),
        routine.remoteId,
        routine.externalId
    ).any { key -> key in movableLocalRoutineKeys }
    val isMovableRemoteRoutine = canMoveRemoteRoutines &&
        routine.id.startsWith("routine-") &&
        routine.remoteId.isNotBlank()
    return isMovableLocalStrengthRoutine || isMovableRemoteRoutine
}

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
    val match = listOfNotNull(paceMatch, speedMatch).minByOrNull { it.range.first } ?: return null
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

internal fun mergeTrainingRoutinesAndResults(
    activities: List<TrainingItem>,
    routines: List<TrainingItem>,
): List<TrainingItem> {
    if (activities.isEmpty() || routines.isEmpty()) return activities + routines
    val unusedRoutines = routines.toMutableList()
    val mergedActivities = activities.map { activity ->
        val match = unusedRoutines
            .withIndex()
            .filter { (_, routine) -> routine.canMergeWithResult(activity) }
            .maxByOrNull { (_, routine) -> routine.mergeScoreForResult(activity) }
            ?: return@map activity
        unusedRoutines.removeAt(match.index)
        activity.copy(
            id = "merged-${match.value.id}-${activity.id}",
            matchedStrengthRoutine = activity.matchedStrengthRoutine ?: match.value.matchedStrengthRoutine,
            pairedRoutine = match.value
        )
    }
    return mergedActivities + unusedRoutines
}

private fun TrainingItem.canMergeWithResult(result: TrainingItem): Boolean {
    if (!isRoutine || result.isRoutine) return false
    if (date != result.date) return false
    if (sportType() != result.sportType()) return false
    if (sportType() == TrainingSportType.OTHER && normalizedTitle() != result.normalizedTitle()) return false
    return true
}

private fun TrainingItem.mergeScoreForResult(result: TrainingItem): Int {
    var score = 0
    if (normalizedTitle() == result.normalizedTitle()) score += 30
    if (sportType() == TrainingSportType.STRENGTH) score += 20
    if (durationSeconds != null && result.durationSeconds != null) {
        val diff = abs(durationSeconds - result.durationSeconds)
        score += when {
            diff <= 60 -> 12
            diff <= 300 -> 6
            else -> 0
        }
    }
    if (distanceMeters != null && result.distanceMeters != null) {
        val diff = abs(distanceMeters - result.distanceMeters)
        score += when {
            diff <= 50.0 -> 12
            diff <= 500.0 -> 6
            else -> 0
        }
    }
    return score
}

private fun TrainingItem.normalizedTitle(): String {
    return name.ifBlank { type }
        .lowercase(Locale.KOREAN)
        .replace(" ", "")
        .replace("_", "")
        .replace("-", "")
}

internal fun List<TrainingItem>.latestMetricValue(selector: (TrainingItem) -> Double?): Double? {
    return sortedWith(
        compareByDescending<TrainingItem> { it.startedAt ?: it.date.atStartOfDay() }
            .thenByDescending { it.date }
    ).firstNotNullOfOrNull(selector)
}

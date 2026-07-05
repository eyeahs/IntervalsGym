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
    val plans: List<TrainingItem>,
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
    val blocks: List<PlanBlock>,
    val isPlan: Boolean,
    val matchedStrengthWorkout: CompletedStrengthWorkout? = null,
    val matchedStrengthPlan: StrengthWorkoutPlan? = null,
    val isLocalOnlyStrengthResult: Boolean = false,
    val isLocalOnlyRunningResult: Boolean = false,
    val actualRunningBlocks: List<PlanBlock> = emptyList(),
    val actualRunningRoutePoints: List<RunningRoutePoint> = emptyList(),
    val pairedPlan: TrainingItem? = null,
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
        matchedStrengthWorkout != null ||
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
            it.equals("Plan", ignoreCase = true)
    }
}

internal fun TrainingItem.plannedWorkoutDeleteConfirmMessage(): String {
    return plannedWorkoutDeleteConfirmMessage(date, name)
}

internal fun TrainingItem.strengthPlanForDisplay(): StrengthWorkoutPlan? {
    if (sportType() != TrainingSportType.STRENGTH) return null
    if (!isPlan && pairedPlan == null) return null
    return matchedStrengthPlan
        ?: pairedPlan?.matchedStrengthPlan
        ?: description.toIntervalsGymStrengthPlan()
        ?: pairedPlan?.description.toIntervalsGymStrengthPlan()
}

internal fun TrainingItem.calendarPlanForMove(): TrainingItem? {
    return when {
        isPlan -> this
        pairedPlan?.isPlan == true -> pairedPlan
        else -> null
    }
}

internal fun TrainingItem.canDragCalendarPlan(
    movableLocalPlanKeys: Set<String>,
    canMoveRemotePlans: Boolean,
): Boolean {
    val plan = calendarPlanForMove() ?: return false
    val isMovableLocalStrengthPlan = listOfNotNull(
        plan.id,
        plan.id.removePrefix("local-"),
        plan.remoteId,
        plan.externalId
    ).any { key -> key in movableLocalPlanKeys }
    val isMovableRemotePlan = canMoveRemotePlans &&
        plan.id.startsWith("plan-") &&
        plan.remoteId.isNotBlank()
    return isMovableLocalStrengthPlan || isMovableRemotePlan
}

internal fun TrainingItem.workoutPlanBlocksForPreview(): List<PlanBlock> {
    val sportType = sportType()
    if (sportType != TrainingSportType.RUNNING && sportType != TrainingSportType.CYCLING) return emptyList()
    if (!isPlan && pairedPlan == null) return emptyList()
    val sourceBlocks = blocks.takeIf { it.isNotEmpty() }
        ?: pairedPlan?.blocks?.takeIf { it.isNotEmpty() }
        ?: emptyList()
    val sourceDescription = description ?: pairedPlan?.description
    return when (sportType) {
        TrainingSportType.RUNNING -> sourceBlocks.withRunningGraphContext(
            description = sourceDescription,
            name = name.ifBlank { pairedPlan?.name.orEmpty() }
        )
        TrainingSportType.CYCLING -> sourceBlocks.withCyclingGraphContext(sourceDescription)
        else -> sourceBlocks
    }
}

internal fun TrainingItem.workoutPlanTotalSecondsForPreview(blocks: List<PlanBlock>): Int {
    return durationSeconds
        ?: pairedPlan?.durationSeconds
        ?: blocks.sumOf { it.durationSeconds }
}

internal fun List<PlanBlock>.withRunningGraphContext(
    description: String?,
    name: String,
): List<PlanBlock> {
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
    return PlanBlock(
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

internal fun List<PlanBlock>.withCyclingGraphContext(description: String?): List<PlanBlock> {
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

internal fun mergeTrainingPlansAndResults(
    activities: List<TrainingItem>,
    plans: List<TrainingItem>,
): List<TrainingItem> {
    if (activities.isEmpty() || plans.isEmpty()) return activities + plans
    val unusedPlans = plans.toMutableList()
    val mergedActivities = activities.map { activity ->
        val match = unusedPlans
            .withIndex()
            .filter { (_, plan) -> plan.canMergeWithResult(activity) }
            .maxByOrNull { (_, plan) -> plan.mergeScoreForResult(activity) }
            ?: return@map activity
        unusedPlans.removeAt(match.index)
        activity.copy(
            id = "merged-${match.value.id}-${activity.id}",
            matchedStrengthPlan = activity.matchedStrengthPlan ?: match.value.matchedStrengthPlan,
            pairedPlan = match.value
        )
    }
    return mergedActivities + unusedPlans
}

private fun TrainingItem.canMergeWithResult(result: TrainingItem): Boolean {
    if (!isPlan || result.isPlan) return false
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

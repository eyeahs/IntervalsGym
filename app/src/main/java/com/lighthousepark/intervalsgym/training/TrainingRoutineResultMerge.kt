package com.lighthousepark.intervalsgym.training

import java.util.Locale
import kotlin.math.abs

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

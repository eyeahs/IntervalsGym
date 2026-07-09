package com.lighthousepark.intervalsgym.training

internal fun List<TrainingItem>.latestMetricValue(selector: (TrainingItem) -> Double?): Double? {
    return sortedWith(
        compareByDescending<TrainingItem> { it.startedAt ?: it.date.atStartOfDay() }
            .thenByDescending { it.date }
    ).firstNotNullOfOrNull(selector)
}

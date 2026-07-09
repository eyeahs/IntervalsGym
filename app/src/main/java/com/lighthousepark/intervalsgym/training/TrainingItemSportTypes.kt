package com.lighthousepark.intervalsgym.training

import java.util.Locale

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

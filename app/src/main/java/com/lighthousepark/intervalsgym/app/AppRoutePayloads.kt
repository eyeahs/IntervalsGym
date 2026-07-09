package com.lighthousepark.intervalsgym.app

import com.lighthousepark.intervalsgym.data.toCachedTrainingItems
import com.lighthousepark.intervalsgym.data.toJsonString
import com.lighthousepark.intervalsgym.data.toStrengthWorkoutRoutines
import com.lighthousepark.intervalsgym.data.toTrainingItemsJsonArray
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.training.TrainingItem
import org.json.JSONArray

internal fun TrainingItem?.toRouteJson(): String? {
    return this?.let { item -> listOf(item).toTrainingItemsJsonArray().toString() }
}

internal fun String?.toRouteTrainingItem(): TrainingItem? {
    if (isNullOrBlank()) return null
    return runCatching {
        JSONArray(this).toCachedTrainingItems().firstOrNull()
    }.getOrNull()
}

internal fun StrengthWorkoutRoutine?.toRouteJson(): String? {
    return this?.let { routine -> listOf(routine).toJsonString() }
}

internal fun String?.toRouteStrengthRoutine(): StrengthWorkoutRoutine? {
    return toStrengthWorkoutRoutines().firstOrNull()
}

package com.lighthousepark.intervalsgym.training

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

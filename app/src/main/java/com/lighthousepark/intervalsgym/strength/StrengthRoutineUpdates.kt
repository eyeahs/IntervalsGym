package com.lighthousepark.intervalsgym.strength

internal data class StrengthRoutineUpdateSelection(
    val order: Boolean = false,
    val supersets: Boolean = false,
    val exerciseTypes: Boolean = false,
    val exerciseDetails: Boolean = false,
) {
    val hasSelection: Boolean
        get() = order || supersets || exerciseTypes || exerciseDetails
}

internal fun strengthRoutineUpdateAvailability(
    routineEntries: List<StrengthRoutineEntry>,
    workoutEntries: List<StrengthRoutineEntry>,
): StrengthRoutineUpdateSelection {
    val routineById = routineEntries.associateBy { it.id }
    val workoutById = workoutEntries.associateBy { it.id }
    val commonIds = routineById.keys.intersect(workoutById.keys)
    val routineCommonOrder = routineEntries.map { it.id }.filter { it in commonIds }
    val workoutOrder = workoutEntries.map { it.id }
    val workoutCommonOrder = workoutOrder.filter { it in commonIds }
    val addedIds = workoutOrder.filter { it !in routineById }
    val newExercisesAreInterleaved = workoutOrder != routineCommonOrder + addedIds
    val membershipChanged = routineById.keys != workoutById.keys

    return StrengthRoutineUpdateSelection(
        order = routineCommonOrder != workoutCommonOrder || newExercisesAreInterleaved,
        supersets = commonIds.any { id ->
            routineById.getValue(id).supersetGroupId != workoutById.getValue(id).supersetGroupId
        } || workoutEntries.any { it.id !in routineById && it.supersetGroupId != null },
        exerciseTypes = membershipChanged || commonIds.any { id ->
            val routineEntry = routineById.getValue(id)
            val workoutEntry = workoutById.getValue(id)
            routineEntry.exercise != workoutEntry.exercise ||
                routineEntry.equipment != workoutEntry.equipment ||
                routineEntry.variation != workoutEntry.variation
        },
        exerciseDetails = commonIds.any { id ->
            !routineById.getValue(id).hasSamePlannedDetailsAs(workoutById.getValue(id))
        }
    )
}

internal fun mergeStrengthRoutineUpdates(
    routineEntries: List<StrengthRoutineEntry>,
    workoutEntries: List<StrengthRoutineEntry>,
    selection: StrengthRoutineUpdateSelection,
): List<StrengthRoutineEntry>? {
    val availability = strengthRoutineUpdateAvailability(routineEntries, workoutEntries)
    val effectiveSelection = StrengthRoutineUpdateSelection(
        order = selection.order && availability.order,
        supersets = selection.supersets && availability.supersets,
        exerciseTypes = selection.exerciseTypes && availability.exerciseTypes,
        exerciseDetails = selection.exerciseDetails && availability.exerciseDetails
    )
    if (!effectiveSelection.hasSelection) return null
    val routineById = routineEntries.associateBy { it.id }
    val workoutById = workoutEntries.associateBy { it.id }
    val targetIds = if (effectiveSelection.exerciseTypes) workoutById.keys else routineById.keys

    val mergedById = targetIds.mapNotNull { id ->
        val routineEntry = routineById[id]
        val workoutEntry = workoutById[id]
        when {
            routineEntry == null && workoutEntry != null -> {
                val workoutPlan = workoutEntry.copyForWorkout()
                id to if (effectiveSelection.supersets) {
                    workoutPlan
                } else {
                    workoutPlan.copy(supersetGroupId = null)
                }
            }
            routineEntry != null && workoutEntry == null -> id to routineEntry.copyForWorkout()
            routineEntry != null && workoutEntry != null -> {
                val routinePlan = routineEntry.copyForWorkout()
                val workoutPlan = workoutEntry.copyForWorkout()
                var merged = routinePlan
                if (effectiveSelection.exerciseTypes) {
                    merged = merged.copy(
                        exercise = workoutPlan.exercise,
                        equipment = workoutPlan.equipment,
                        variation = workoutPlan.variation
                    )
                }
                if (effectiveSelection.supersets) {
                    merged = merged.copy(supersetGroupId = workoutPlan.supersetGroupId)
                }
                if (effectiveSelection.exerciseDetails) {
                    merged = merged.copy(
                        targetSets = workoutPlan.targetSets,
                        targetReps = workoutPlan.targetReps,
                        restSeconds = workoutPlan.restSeconds,
                        targetWeightKg = workoutPlan.targetWeightKg,
                        note = workoutPlan.note,
                        records = workoutPlan.records
                    )
                }
                id to merged
            }
            else -> null
        }
    }.toMap()

    val preferredOrder = if (effectiveSelection.order) {
        workoutEntries.map { it.id }
    } else {
        routineEntries.map { it.id }
    }
    val orderedIds = preferredOrder.filter { it in mergedById } +
        mergedById.keys.filter { it !in preferredOrder }
    val orderedEntries = orderedIds.mapNotNull(mergedById::get)
    val membershipWasApplied = effectiveSelection.exerciseTypes && routineById.keys != workoutById.keys
    val mergedEntries = if (effectiveSelection.supersets || membershipWasApplied) {
        orderedEntries.normalizeSupersetGroups()
    } else {
        orderedEntries
    }
    val routinePlan = routineEntries.map { it.copyForWorkout() }
    return mergedEntries.takeUnless { it == routinePlan }
}

internal fun CompletedStrengthSession.appliedRoutineEntries(): List<StrengthRoutineEntry> {
    return routineUpdateEntries
        ?.map { it.copyForWorkout() }
        ?: entries.map { it.copyWorkoutResultToRoutine() }
}

private fun StrengthRoutineEntry.hasSamePlannedDetailsAs(other: StrengthRoutineEntry): Boolean {
    val routinePlan = copyForWorkout()
    val workoutPlan = other.copyForWorkout()
    return routinePlan.targetSets == workoutPlan.targetSets &&
        routinePlan.targetReps == workoutPlan.targetReps &&
        routinePlan.restSeconds == workoutPlan.restSeconds &&
        routinePlan.targetWeightKg == workoutPlan.targetWeightKg &&
        routinePlan.note == workoutPlan.note &&
        routinePlan.records == workoutPlan.records
}

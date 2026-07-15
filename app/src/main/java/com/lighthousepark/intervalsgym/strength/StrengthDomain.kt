package com.lighthousepark.intervalsgym.strength

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

internal data class StrengthExercise(
    val id: String,
    val nameKo: String,
    val nameEn: String,
    val group: String,
    val equipmentOptions: List<String>,
    val variationOptions: List<String>,
    val variationUnilateralModes: Map<String, String> = emptyMap(),
    val aliases: List<String> = emptyList(),
)

internal data class StrengthWorkoutRoutine(
    val id: Int,
    val name: String,
    val entries: List<StrengthRoutineEntry>,
)

internal data class ScheduledStrengthRoutine(
    val id: String,
    val date: LocalDate,
    val time: LocalTime? = null,
    val routine: StrengthWorkoutRoutine,
    val uploadedToIntervals: Boolean,
    val externalId: String,
)

internal data class ActiveStrengthSession(
    val routineId: Int,
    val routineName: String,
    val entries: List<StrengthRoutineEntry>,
    val hasStarted: Boolean,
    val sessionStartedAtMillis: Long,
    val isSetScreenVisible: Boolean,
    val currentExerciseIndex: Int,
    val currentSetIndex: Int,
    val pendingExerciseIndex: Int?,
    val pendingSetIndex: Int?,
    val restEndAtMillis: Long,
    val isRestSheetVisible: Boolean,
    val restTitle: String,
    val setEvents: List<StrengthSetCompletionEvent>,
    val restEvents: List<StrengthRestEvent>,
    val activeRestEventId: Int?,
    val routineBaselineEntries: List<StrengthRoutineEntry> = entries,
) {
    fun toWorkoutRoutine(): StrengthWorkoutRoutine {
        return StrengthWorkoutRoutine(
            id = routineId,
            name = routineName,
            entries = routineBaselineEntries
        )
    }
}

internal data class CompletedStrengthSession(
    val id: String,
    val routineId: Int,
    val routineName: String,
    val startedAtMillis: Long,
    val endedAtMillis: Long,
    val durationSeconds: Int,
    val intervalsExternalId: String,
    val entries: List<StrengthRoutineEntry>,
    val setEvents: List<StrengthSetCompletionEvent>,
    val restEvents: List<StrengthRestEvent>,
    val rpe: Int,
    val trainingLoad: Int,
    val uploadedToIntervals: Boolean,
    val appliedToRoutine: Boolean = true,
    val routineUpdateEntries: List<StrengthRoutineEntry>? = null,
)

internal data class CompletedStrengthExerciseHistory(
    val session: CompletedStrengthSession,
    val entry: StrengthRoutineEntry,
    val setEvents: List<StrengthSetCompletionEvent>,
)

internal data class StrengthSetCompletionEvent(
    val sequence: Int,
    val exerciseEntryId: Int,
    val exerciseTitle: String,
    val exerciseGroup: String,
    val exerciseId: String,
    val equipment: String,
    val variation: String,
    val setRecordId: Int,
    val setIndex: Int,
    val weightKg: String,
    val reps: String,
    val targetRestSeconds: Int,
    val completedAtMillis: Long,
)

internal data class StrengthRestEvent(
    val id: Int,
    val afterSetSequence: Int,
    val exerciseEntryId: Int,
    val exerciseTitle: String,
    val setRecordId: Int,
    val setIndex: Int,
    val startedAtMillis: Long,
    val plannedSeconds: Int,
    val targetEndAtMillis: Long,
    val endedAtMillis: Long?,
    val endReason: String?,
) {
    val actualSeconds: Int
        get() = endedAtMillis
            ?.let { ((it - startedAtMillis) / 1000L).toInt().coerceAtLeast(0) }
            ?: 0
}

internal data class StrengthRoutineEntry(
    val id: Int,
    val exercise: StrengthExercise,
    val equipment: String,
    val variation: String,
    val supersetGroupId: Int?,
    val targetSets: Int,
    val targetReps: Int,
    val restSeconds: Int,
    val targetWeightKg: String,
    val note: String = "",
    val records: List<StrengthSetRecord>,
) {
    val title: String
        get() = formatStrengthExerciseTitle(exercise, equipment, variation)
}

internal data class StrengthSetRecord(
    val id: Int,
    val weightKg: String,
    val reps: String,
    val actualWeightKg: String = "",
    val actualReps: String = "",
    val leftWeightKg: String = weightKg,
    val leftReps: String = reps,
    val rightWeightKg: String = weightKg,
    val rightReps: String = reps,
    val durationSeconds: String,
    val restSeconds: String,
    val completed: Boolean,
) {
    val performedWeightKg: String
        get() = actualWeightKg.ifBlank { weightKg }

    val performedReps: String
        get() = actualReps.ifBlank { reps }
}

internal data class StrengthSession(
    val name: String,
    val startedAt: LocalDateTime,
    val entries: List<StrengthRoutineEntry>,
    val rpe: Int,
    val trainingLoad: Int,
    val durationSeconds: Int? = null,
    val setEvents: List<StrengthSetCompletionEvent> = emptyList(),
    val restEvents: List<StrengthRestEvent> = emptyList(),
)

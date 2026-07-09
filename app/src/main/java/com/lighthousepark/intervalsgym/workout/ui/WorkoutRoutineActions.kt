package com.lighthousepark.intervalsgym.workout.ui

import android.content.SharedPreferences
import com.lighthousepark.intervalsgym.data.CalendarRoutineSyncUseCase
import com.lighthousepark.intervalsgym.data.RunningSessionSyncUseCase
import com.lighthousepark.intervalsgym.data.StrengthSessionSyncUseCase
import com.lighthousepark.intervalsgym.data.upsertSavedRunningWorkoutRoutine
import com.lighthousepark.intervalsgym.running.SavedRunningWorkoutRoutine
import com.lighthousepark.intervalsgym.running.runningBlocksDiagnosticText
import com.lighthousepark.intervalsgym.running.toSavedRunningWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.training.TrainingItem

internal sealed interface WorkoutRoutineLocalStrengthUploadAction

internal data object WorkoutRoutineLocalStrengthUploadLoginRequired : WorkoutRoutineLocalStrengthUploadAction

internal data class WorkoutRoutineLocalStrengthUploadReady(
    private val workout: CompletedStrengthSession,
) : WorkoutRoutineLocalStrengthUploadAction {
    suspend fun upload(syncUseCase: StrengthSessionSyncUseCase): CompletedStrengthSession {
        return syncUseCase.uploadStrengthSession(workout)
    }
}

internal fun planWorkoutRoutineLocalStrengthUpload(
    apiKey: String,
    localSession: CompletedStrengthSession?,
): WorkoutRoutineLocalStrengthUploadAction? {
    if (localSession == null) return null
    if (apiKey.isBlank()) return WorkoutRoutineLocalStrengthUploadLoginRequired
    return WorkoutRoutineLocalStrengthUploadReady(localSession)
}

internal data class WorkoutRoutineCalendarDeleteAction(
    val targetRoutine: TrainingItem,
) {
    suspend fun delete(syncUseCase: CalendarRoutineSyncUseCase) {
        syncUseCase.deleteRoutine(targetRoutine)
    }
}

internal fun planWorkoutRoutineCalendarDelete(
    routine: TrainingItem?,
): WorkoutRoutineCalendarDeleteAction? {
    return routine?.let(::WorkoutRoutineCalendarDeleteAction)
}

internal data class WorkoutRoutineLocalRunningDeleteAction(
    val sessionId: String,
) {
    fun delete(syncUseCase: RunningSessionSyncUseCase) {
        syncUseCase.deleteRunningSession(sessionId)
    }
}

internal fun planWorkoutRoutineLocalRunningDelete(
    routine: TrainingItem?,
): WorkoutRoutineLocalRunningDeleteAction? {
    return routine?.remoteId
        ?.takeIf { it.isNotBlank() }
        ?.let(::WorkoutRoutineLocalRunningDeleteAction)
}

internal sealed interface WorkoutRoutineSaveRunningRoutineAction {
    val toastMessage: String
}

internal data object WorkoutRoutineSaveRunningRoutineUnavailable : WorkoutRoutineSaveRunningRoutineAction {
    override val toastMessage: String = "저장할 수 있는 러닝 routine이 아닙니다."
}

internal data class WorkoutRoutineSaveRunningRoutineReady(
    val savedRoutine: SavedRunningWorkoutRoutine,
) : WorkoutRoutineSaveRunningRoutineAction {
    override val toastMessage: String = "러닝 Routine 저장됨"

    fun save(prefs: SharedPreferences) {
        upsertSavedRunningWorkoutRoutine(
            prefs = prefs,
            routine = savedRoutine
        )
    }
}

internal fun planWorkoutRoutineSaveRunningRoutine(
    routine: TrainingItem?,
    graphBlocks: List<RoutineBlock>,
): WorkoutRoutineSaveRunningRoutineAction {
    val savedRoutine = routine?.toSavedRunningWorkoutRoutine(graphBlocks)
    return savedRoutine?.let(::WorkoutRoutineSaveRunningRoutineReady)
        ?: WorkoutRoutineSaveRunningRoutineUnavailable
}

internal sealed interface WorkoutRoutineStartAction

internal data class WorkoutRoutineStartStrengthAction(
    val routine: StrengthWorkoutRoutine,
) : WorkoutRoutineStartAction

internal data class WorkoutRoutineStartRunningAction(
    val diagnosticDetails: String,
) : WorkoutRoutineStartAction

internal data object WorkoutRoutineStartUnavailable : WorkoutRoutineStartAction

internal fun planWorkoutRoutineStartAction(
    routine: TrainingItem?,
    graphBlocks: List<RoutineBlock>,
    intervalStrengthRoutine: StrengthWorkoutRoutine?,
): WorkoutRoutineStartAction {
    if (intervalStrengthRoutine != null) {
        return WorkoutRoutineStartStrengthAction(intervalStrengthRoutine)
    }
    if (routine == null || graphBlocks.isEmpty()) return WorkoutRoutineStartUnavailable
    return WorkoutRoutineStartRunningAction(
        diagnosticDetails = workoutRoutineStartRunningDiagnosticDetails(
            routine = routine,
            graphBlocks = graphBlocks
        )
    )
}

private fun workoutRoutineStartRunningDiagnosticDetails(
    routine: TrainingItem,
    graphBlocks: List<RoutineBlock>,
): String {
    return buildString {
        appendLine("start pressed")
        appendLine("id=${routine.id}")
        appendLine("name=${routine.name}")
        appendLine(graphBlocks.runningBlocksDiagnosticText(label = "startingGraphBlocks"))
    }
}

package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.data.CalendarRoutineSyncUseCase
import com.lighthousepark.intervalsgym.data.StrengthSessionSyncUseCase
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.training.TrainingItem

internal sealed interface StrengthSessionFinishAction {
    val result: CompletedStrengthSession?
    val shouldApplyToRoutine: Boolean
}

internal data class SaveFinishedStrengthSessionLocally(
    override val result: CompletedStrengthSession?,
    override val shouldApplyToRoutine: Boolean,
) : StrengthSessionFinishAction

internal data class UploadFinishedStrengthSession(
    override val result: CompletedStrengthSession?,
    override val shouldApplyToRoutine: Boolean,
) : StrengthSessionFinishAction

internal fun StrengthSessionResultSnapshot.planFinishedStrengthSession(
    syncUseCase: StrengthSessionSyncUseCase,
    canUploadToIntervals: Boolean,
    endedAtMillis: Long = endedAtMillis(),
): StrengthSessionFinishAction {
    val localResult = buildFinishedResult(
        syncUseCase = syncUseCase,
        endedAtMillis = endedAtMillis,
        uploadedToIntervals = false
    )
    return if (canUploadToIntervals) {
        UploadFinishedStrengthSession(
            result = localResult,
            shouldApplyToRoutine = applyWorkoutResultToRoutine
        )
    } else {
        SaveFinishedStrengthSessionLocally(
            result = localResult,
            shouldApplyToRoutine = applyWorkoutResultToRoutine
        )
    }
}

internal fun SaveFinishedStrengthSessionLocally.saveLocalResult(
    syncUseCase: StrengthSessionSyncUseCase,
): CompletedStrengthSession? {
    return result?.let { syncUseCase.saveStrengthSessionLocally(it) }
}

internal suspend fun UploadFinishedStrengthSession.uploadResult(
    syncUseCase: StrengthSessionSyncUseCase,
): CompletedStrengthSession {
    return result
        ?.let { syncUseCase.uploadStrengthSession(it) }
        ?: error("업로드할 웨이트 세션이 없습니다.")
}

internal data class StrengthSessionCalendarRoutineDeleteAction(
    val targetRoutine: TrainingItem,
) {
    suspend fun delete(syncUseCase: CalendarRoutineSyncUseCase) {
        syncUseCase.deleteRoutine(targetRoutine)
    }
}

internal fun planStrengthSessionCalendarRoutineDelete(
    calendarRoutineItem: TrainingItem?,
): StrengthSessionCalendarRoutineDeleteAction? {
    return calendarRoutineItem?.let(::StrengthSessionCalendarRoutineDeleteAction)
}

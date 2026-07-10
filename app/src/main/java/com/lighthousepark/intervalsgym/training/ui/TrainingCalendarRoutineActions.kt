package com.lighthousepark.intervalsgym.training.ui

import com.lighthousepark.intervalsgym.data.CalendarRoutineDeleteScope
import com.lighthousepark.intervalsgym.data.CalendarRoutineSyncUseCase
import com.lighthousepark.intervalsgym.strength.ScheduledStrengthRoutine
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.training.PendingCalendarRoutineMove
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.calendarIdentityKeys
import com.lighthousepark.intervalsgym.training.calendarRoutineForMove
import com.lighthousepark.intervalsgym.training.hasPendingCalendarRoutineMoveFor
import com.lighthousepark.intervalsgym.training.withPendingCalendarRoutineMove
import com.lighthousepark.intervalsgym.training.withoutCalendarRoutineMove
import com.lighthousepark.intervalsgym.training.withoutCalendarRoutineMoveIdentities
import java.time.LocalDate
import java.time.LocalTime

internal sealed interface TrainingCalendarRoutineSaveDecision {
    data object InvalidTime : TrainingCalendarRoutineSaveDecision
    data class Save(val plan: TrainingCalendarRoutineSavePlan) : TrainingCalendarRoutineSaveDecision
}

internal data class TrainingCalendarRoutineSavePlan(
    val routine: StrengthWorkoutRoutine,
    val targetDate: LocalDate,
    val targetTime: LocalTime,
    val requiresRemoteUpload: Boolean,
) {
    val routineId: Int
        get() = routine.id

    fun saveLocally(syncUseCase: CalendarRoutineSyncUseCase): ScheduledStrengthRoutine {
        return syncUseCase.saveStrengthRoutineLocally(
            routine = routine,
            targetDate = targetDate,
            targetTime = targetTime
        )
    }

    suspend fun upload(
        syncUseCase: CalendarRoutineSyncUseCase,
        scheduledRoutine: ScheduledStrengthRoutine,
    ): ScheduledStrengthRoutine {
        return syncUseCase.uploadSavedStrengthRoutine(scheduledRoutine)
    }
}

internal fun planTrainingCalendarRoutineSave(
    routine: StrengthWorkoutRoutine,
    targetDate: LocalDate,
    targetTime: LocalTime?,
    isRemoteConnected: Boolean,
): TrainingCalendarRoutineSaveDecision {
    val safeTargetTime = targetTime ?: return TrainingCalendarRoutineSaveDecision.InvalidTime
    return TrainingCalendarRoutineSaveDecision.Save(
        TrainingCalendarRoutineSavePlan(
            routine = routine,
            targetDate = targetDate,
            targetTime = safeTargetTime,
            requiresRemoteUpload = isRemoteConnected
        )
    )
}

internal sealed interface TrainingCalendarRoutineMoveDecision {
    data object Ignore : TrainingCalendarRoutineMoveDecision
    data class Blocked(val message: String) : TrainingCalendarRoutineMoveDecision
    data class Move(val plan: TrainingCalendarRoutineMovePlan) : TrainingCalendarRoutineMoveDecision
}

internal data class TrainingCalendarRoutineMovePlan(
    val sourceRoutine: TrainingItem,
    val pendingMove: PendingCalendarRoutineMove,
    val pendingCalendarRoutineMoves: Map<String, PendingCalendarRoutineMove>,
) {
    fun moveLocally(syncUseCase: CalendarRoutineSyncUseCase): ScheduledStrengthRoutine? {
        return syncUseCase.moveStrengthRoutineLocally(
            sourceRoutine = sourceRoutine,
            targetDate = pendingMove.targetDate
        )
    }

    suspend fun syncRemote(
        syncUseCase: CalendarRoutineSyncUseCase,
        movedRoutine: ScheduledStrengthRoutine?,
    ) {
        syncUseCase.syncMovedRoutine(
            sourceRoutine = sourceRoutine,
            targetDate = pendingMove.targetDate,
            movedRoutine = movedRoutine
        )
    }

    fun startedMessage(movedLocally: Boolean): String {
        val routineName = sourceRoutine.name.ifBlank { "Routine" }
        val targetText = "${pendingMove.targetDate.monthValue}/${pendingMove.targetDate.dayOfMonth}"
        return if (movedLocally) {
            "$routineName ${targetText}로 이동됨"
        } else {
            "$routineName ${targetText}로 이동 중..."
        }
    }

    fun failureMessage(movedLocally: Boolean): String {
        return if (movedLocally) {
            "로컬 일정은 이동됐지만 Intervals.icu 반영은 실패했습니다."
        } else {
            "Intervals.icu Routine 이동에 실패했습니다."
        }
    }

    fun rollbackPendingMove(
        pendingCalendarRoutineMoves: Map<String, PendingCalendarRoutineMove>,
    ): Map<String, PendingCalendarRoutineMove> {
        return pendingCalendarRoutineMoves.withoutCalendarRoutineMove(pendingMove)
    }
}

internal fun planTrainingCalendarRoutineMove(
    item: TrainingItem,
    targetDate: LocalDate,
    pendingCalendarRoutineMoves: Map<String, PendingCalendarRoutineMove>,
    isRemoteConnected: Boolean,
): TrainingCalendarRoutineMoveDecision {
    val sourceRoutine = item.calendarRoutineForMove() ?: return TrainingCalendarRoutineMoveDecision.Ignore
    if (pendingCalendarRoutineMoves.values.hasPendingCalendarRoutineMoveFor(sourceRoutine)) {
        return TrainingCalendarRoutineMoveDecision.Blocked(TRAINING_CALENDAR_PENDING_MOVE_MESSAGE)
    }
    if (sourceRoutine.date == targetDate) return TrainingCalendarRoutineMoveDecision.Ignore

    val pendingMove = PendingCalendarRoutineMove(sourceRoutine = sourceRoutine, targetDate = targetDate)
    return TrainingCalendarRoutineMoveDecision.Move(
        TrainingCalendarRoutineMovePlan(
            sourceRoutine = sourceRoutine,
            pendingMove = pendingMove,
            pendingCalendarRoutineMoves = pendingCalendarRoutineMoves.withPendingCalendarRoutineMove(
                move = pendingMove,
                isRemoteConnected = isRemoteConnected
            )
        )
    )
}

internal sealed interface TrainingCalendarRoutineDeleteDecision {
    data object Ignore : TrainingCalendarRoutineDeleteDecision
    data class Blocked(val message: String) : TrainingCalendarRoutineDeleteDecision
    data class Delete(val plan: TrainingCalendarRoutineDeletePlan) : TrainingCalendarRoutineDeleteDecision
}

internal data class TrainingCalendarRoutineDeletePlan(
    val targetRoutine: TrainingItem,
    val deleteKeys: Set<String>,
    val deleteScope: CalendarRoutineDeleteScope,
    val pendingCalendarRoutineMoves: Map<String, PendingCalendarRoutineMove>,
    val optimisticallyDeletedCalendarRoutineKeys: Set<String>,
) {
    val requiresRemoteDelete: Boolean
        get() = deleteScope == CalendarRoutineDeleteScope.REMOTE

    suspend fun delete(syncUseCase: CalendarRoutineSyncUseCase): CalendarRoutineDeleteScope {
        return syncUseCase.deleteRoutine(targetRoutine)
    }

    fun deletedMessage(): String {
        return "${targetRoutine.name.ifBlank { "Routine" }} 삭제됨"
    }

    fun clearOptimisticDeleteKeys(
        currentKeys: Set<String>,
    ): Set<String> {
        return currentKeys - deleteKeys
    }
}

internal fun planTrainingCalendarRoutineDelete(
    item: TrainingItem,
    pendingCalendarRoutineMoves: Map<String, PendingCalendarRoutineMove>,
    optimisticallyDeletedCalendarRoutineKeys: Set<String>,
    deleteScopeFor: (TrainingItem) -> CalendarRoutineDeleteScope,
): TrainingCalendarRoutineDeleteDecision {
    val targetRoutine = item.calendarRoutineForMove() ?: return TrainingCalendarRoutineDeleteDecision.Ignore
    if (pendingCalendarRoutineMoves.values.hasPendingCalendarRoutineMoveFor(targetRoutine)) {
        return TrainingCalendarRoutineDeleteDecision.Blocked(TRAINING_CALENDAR_PENDING_MOVE_MESSAGE)
    }
    val deleteKeys = targetRoutine.calendarIdentityKeys()
    val deleteScope = deleteScopeFor(targetRoutine)
    return TrainingCalendarRoutineDeleteDecision.Delete(
        TrainingCalendarRoutineDeletePlan(
            targetRoutine = targetRoutine,
            deleteKeys = deleteKeys,
            deleteScope = deleteScope,
            pendingCalendarRoutineMoves = pendingCalendarRoutineMoves.withoutCalendarRoutineMoveIdentities(deleteKeys),
            optimisticallyDeletedCalendarRoutineKeys = if (deleteScope == CalendarRoutineDeleteScope.REMOTE) {
                optimisticallyDeletedCalendarRoutineKeys + deleteKeys
            } else {
                optimisticallyDeletedCalendarRoutineKeys
            }
        )
    )
}

internal const val TRAINING_CALENDAR_PENDING_MOVE_MESSAGE = "이전 이동을 Intervals.icu에 반영 중입니다."
internal const val TRAINING_CALENDAR_LOCAL_MOVE_UNAVAILABLE_MESSAGE = "이동할 수 있는 로컬 웨이트 routine이 아닙니다."

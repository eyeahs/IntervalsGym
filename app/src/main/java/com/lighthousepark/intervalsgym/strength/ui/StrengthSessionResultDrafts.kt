package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.data.StrengthSessionResultDraft
import com.lighthousepark.intervalsgym.data.StrengthSessionSyncUseCase
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthRestEvent
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthRoutineUpdateSelection
import com.lighthousepark.intervalsgym.strength.StrengthSetCompletionEvent
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.completedStrengthSessionFinishedAtMillis
import com.lighthousepark.intervalsgym.strength.mergeStrengthRoutineUpdates

internal const val STRENGTH_RESULT_END_REASON_LIVE_UPDATE = "live_result_update"
internal const val STRENGTH_RESULT_END_REASON_WORKOUT_FINISHED = "workout_finished"
internal const val STRENGTH_RESULT_END_REASON_DISCARDED = "discarded"

/**
 * Snapshot of every mutable screen field needed to build local/Intervals strength results.
 * Keeping the field list here makes live-save and finish-save paths use the same source data.
 */
internal data class StrengthSessionResultSnapshot(
    val routine: StrengthWorkoutRoutine?,
    val entries: List<StrengthRoutineEntry>,
    val setEvents: List<StrengthSetCompletionEvent>,
    val restEvents: List<StrengthRestEvent>,
    val activeRestEventId: Int?,
    val sessionStartedAtMillis: Long,
    val finishRpe: Int,
    val routineUpdateSelection: StrengthRoutineUpdateSelection,
) {
    fun endedAtMillis(nowMillis: Long = System.currentTimeMillis()): Long {
        return completedStrengthSessionFinishedAtMillis(entries, setEvents) ?: nowMillis
    }

    fun toResultDraft(
        endedAtMillis: Long,
        endReason: String,
        includeRoutineUpdate: Boolean,
    ): StrengthSessionResultDraft {
        val routineUpdateEntries = if (includeRoutineUpdate) {
            routine?.let { workoutRoutine ->
                mergeStrengthRoutineUpdates(
                    routineEntries = workoutRoutine.entries,
                    workoutEntries = entries,
                    selection = routineUpdateSelection
                )
            }
        } else {
            null
        }
        return StrengthSessionResultDraft(
            routine = routine,
            entries = entries,
            setEvents = setEvents,
            restEvents = restEvents,
            activeRestEventId = activeRestEventId,
            sessionStartedAtMillis = sessionStartedAtMillis,
            endedAtMillis = endedAtMillis,
            endReason = endReason,
            rpe = finishRpe,
            routineUpdateEntries = routineUpdateEntries
        )
    }

    fun saveLiveResult(
        syncUseCase: StrengthSessionSyncUseCase,
        endedAtMillis: Long = endedAtMillis(),
        endReason: String = STRENGTH_RESULT_END_REASON_LIVE_UPDATE,
    ): CompletedStrengthSession? {
        return syncUseCase.saveLiveStrengthSessionResult(
            toResultDraft(
                endedAtMillis = endedAtMillis,
                endReason = endReason,
                includeRoutineUpdate = false
            )
        )
    }

    fun buildFinishedResult(
        syncUseCase: StrengthSessionSyncUseCase,
        endedAtMillis: Long,
        uploadedToIntervals: Boolean,
        endReason: String = STRENGTH_RESULT_END_REASON_WORKOUT_FINISHED,
    ): CompletedStrengthSession? {
        return syncUseCase.buildFinishedStrengthSessionResult(
            draft = toResultDraft(
                endedAtMillis = endedAtMillis,
                endReason = endReason,
                includeRoutineUpdate = true
            ),
            uploadedToIntervals = uploadedToIntervals
        )
    }

    fun deleteLiveResult(
        syncUseCase: StrengthSessionSyncUseCase,
        endedAtMillis: Long = endedAtMillis(),
        endReason: String = STRENGTH_RESULT_END_REASON_DISCARDED,
    ) {
        syncUseCase.deleteLiveStrengthSessionResult(
            toResultDraft(
                endedAtMillis = endedAtMillis,
                endReason = endReason,
                includeRoutineUpdate = false
            )
        )
    }
}

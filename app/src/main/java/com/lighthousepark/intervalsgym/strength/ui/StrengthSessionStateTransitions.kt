package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.ActiveStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthRestEvent
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthSetCompletionFollowUp
import com.lighthousepark.intervalsgym.strength.StrengthSetCompletionEvent
import com.lighthousepark.intervalsgym.strength.closeActiveStrengthRestEvent
import com.lighthousepark.intervalsgym.strength.completeStrengthSet
import com.lighthousepark.intervalsgym.strength.copyForWorkout
import com.lighthousepark.intervalsgym.strength.startStrengthRestTimer
import com.lighthousepark.intervalsgym.strength.updateStrengthRestTimerSeconds
import com.lighthousepark.intervalsgym.strength.withCurrentStrengthRestDetails
import com.lighthousepark.intervalsgym.strength.withCurrentStrengthSetDetails

internal data class StrengthSessionInteractionState(
    val entries: List<StrengthRoutineEntry>,
    val setEvents: List<StrengthSetCompletionEvent>,
    val restEvents: List<StrengthRestEvent>,
    val restUiState: StrengthRestUiState,
    val navigationUiState: StrengthSessionNavigationUiState,
)

internal fun restoredStrengthSessionInteractionState(
    activeSession: ActiveStrengthSession?,
    routineEntries: List<StrengthRoutineEntry>,
    shouldStartImmediately: Boolean,
    nowMillis: Long,
    restoredRestUiState: StrengthRestUiState,
): StrengthSessionInteractionState {
    return StrengthSessionInteractionState(
        entries = activeSession?.entries ?: routineEntries.map { it.copyForWorkout() },
        setEvents = activeSession?.setEvents.orEmpty(),
        restEvents = activeSession?.restEvents.orEmpty(),
        restUiState = restoredRestUiState,
        navigationUiState = StrengthSessionNavigationUiState.restored(
            activeSession = activeSession,
            shouldStartImmediately = shouldStartImmediately,
            nowMillis = nowMillis,
            isRestActive = restoredRestUiState.isActive
        )
    )
}

internal data class StrengthSessionStateTransition(
    val state: StrengthSessionInteractionState,
    val restOverlayCommand: StrengthRestOverlayCommand = StrengthRestOverlayCommand.NONE,
    val shouldRequestRestOverlayPermission: Boolean = false,
)

internal enum class StrengthRestOverlayCommand {
    NONE,
    START,
    STOP,
}

internal fun StrengthSessionInteractionState.withEntriesReplaced(
    nextEntries: List<StrengthRoutineEntry>,
    nowMillis: Long,
): StrengthSessionStateTransition {
    val syncedSetEvents = setEvents.withCurrentStrengthSetDetails(nextEntries)
    val syncedRestEvents = restEvents.withCurrentStrengthRestDetails(syncedSetEvents)
    val syncedRestUiState = restUiState.syncedWithActiveRestEvent(
        restEvents = syncedRestEvents,
        nowMillis = nowMillis
    )
    return StrengthSessionStateTransition(
        state = copy(
            entries = nextEntries,
            setEvents = syncedSetEvents,
            restEvents = syncedRestEvents,
            restUiState = syncedRestUiState ?: StrengthRestUiState.inactive()
        ),
        restOverlayCommand = if (syncedRestUiState == null) {
            StrengthRestOverlayCommand.STOP
        } else {
            StrengthRestOverlayCommand.NONE
        }
    )
}

internal fun StrengthSessionInteractionState.withClosedActiveRest(
    endedAtMillis: Long,
    reason: String,
): StrengthSessionStateTransition {
    val result = closeActiveStrengthRestEvent(
        restEvents = restEvents,
        activeRestEventId = restUiState.activeRestEventId,
        endedAtMillis = endedAtMillis,
        reason = reason
    )
    return StrengthSessionStateTransition(
        state = copy(
            restEvents = result.restEvents,
            restUiState = restUiState.copy(activeRestEventId = result.activeRestEventId)
        )
    )
}

internal fun StrengthSessionInteractionState.movedToPendingSet(
    endedAtMillis: Long,
    reason: String,
): StrengthSessionStateTransition {
    val closedState = withClosedActiveRest(
        endedAtMillis = endedAtMillis,
        reason = reason
    ).state
    return StrengthSessionStateTransition(
        state = closedState.copy(
            navigationUiState = closedState.navigationUiState.moveToPendingSet(),
            restUiState = StrengthRestUiState.inactive()
        ),
        restOverlayCommand = StrengthRestOverlayCommand.STOP
    )
}

internal fun StrengthSessionInteractionState.withStartedRest(
    title: String,
    seconds: Int,
    nowMillis: Long,
    restEvent: StrengthRestEvent? = null,
): StrengthSessionStateTransition {
    val result = startStrengthRestTimer(
        restEvents = restEvents,
        title = title,
        seconds = seconds,
        nowMillis = nowMillis,
        restEvent = restEvent
    )
    if (result == null) {
        return movedToPendingSet(
            endedAtMillis = nowMillis,
            reason = "finished"
        )
    }
    return StrengthSessionStateTransition(
        state = copy(
            restEvents = result.restEvents,
            restUiState = StrengthRestUiState.fromTimerStart(result)
        ),
        restOverlayCommand = StrengthRestOverlayCommand.STOP,
        shouldRequestRestOverlayPermission = true
    )
}

internal fun StrengthSessionInteractionState.withUpdatedRestSeconds(
    seconds: Int,
    nowMillis: Long,
): StrengthSessionStateTransition {
    val result = updateStrengthRestTimerSeconds(
        restEvents = restEvents,
        activeRestEventId = restUiState.activeRestEventId,
        seconds = seconds,
        nowMillis = nowMillis
    )
    if (result == null) {
        return movedToPendingSet(
            endedAtMillis = nowMillis,
            reason = "stopped"
        )
    }
    return StrengthSessionStateTransition(
        state = copy(
            restEvents = result.restEvents,
            restUiState = restUiState.withTimerSecondsResult(result)
        ),
        restOverlayCommand = if (restUiState.isSheetVisible) {
            StrengthRestOverlayCommand.STOP
        } else {
            StrengthRestOverlayCommand.START
        }
    )
}

internal fun StrengthSessionInteractionState.withCompletedCurrentSet(
    completedAtMillis: Long,
): StrengthSessionStateTransition? {
    val result = completeStrengthSet(
        entries = entries,
        currentExerciseIndex = navigationUiState.currentExerciseIndex,
        currentSetIndex = navigationUiState.currentSetIndex,
        nextSetEventSequence = (setEvents.maxOfOrNull { it.sequence } ?: 0) + 1,
        nextRestEventId = (restEvents.maxOfOrNull { it.id } ?: 0) + 1,
        completedAtMillis = completedAtMillis
    ) ?: return null
    val stateAfterSet = copy(
        entries = result.entries,
        setEvents = result.setEvent?.let { setEvents + it } ?: setEvents,
        navigationUiState = navigationUiState.applyCompletedSetResult(result)
    )

    return when (result.followUp) {
        StrengthSetCompletionFollowUp.NONE -> {
            StrengthSessionStateTransition(state = stateAfterSet)
        }
        StrengthSetCompletionFollowUp.HIDE_SET_SCREEN -> {
            StrengthSessionStateTransition(
                state = stateAfterSet.copy(
                    navigationUiState = stateAfterSet.navigationUiState.withSetScreenVisible(false)
                )
            )
        }
        StrengthSetCompletionFollowUp.START_REST -> {
            val restEvent = result.restEvent ?: return StrengthSessionStateTransition(state = stateAfterSet)
            stateAfterSet.withStartedRest(
                title = restEvent.exerciseTitle,
                seconds = restEvent.plannedSeconds,
                nowMillis = completedAtMillis,
                restEvent = restEvent
            )
        }
        StrengthSetCompletionFollowUp.MOVE_TO_PENDING_SET -> {
            stateAfterSet.movedToPendingSet(
                endedAtMillis = completedAtMillis,
                reason = "finished"
            )
        }
        StrengthSetCompletionFollowUp.FINISH_ALL_SETS -> {
            val closedState = stateAfterSet.withClosedActiveRest(
                endedAtMillis = completedAtMillis,
                reason = "finished"
            ).state
            StrengthSessionStateTransition(
                state = closedState.copy(
                    navigationUiState = closedState.navigationUiState.finishAllSets(),
                    restUiState = StrengthRestUiState.inactive()
                ),
                restOverlayCommand = StrengthRestOverlayCommand.STOP
            )
        }
    }
}

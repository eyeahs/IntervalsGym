package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.StrengthRestEvent
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.ActiveStrengthSession
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import com.lighthousepark.intervalsgym.strength.toSetEvent
import com.lighthousepark.intervalsgym.strength.withCompletedRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthSessionStateTransitionsTest {
    @Test
    fun restoredInteractionStateKeepsRuntimeFieldsTogether() {
        val routine = defaultStrengthRoutines().first()
        val completedEntry = routine.entries.first().withCompletedRecord(setIndex = 0)
        val setEvent = completedEntry.toSetEvent(sequence = 1, setIndex = 0)
        val restEvent = activeRestEvent(afterSetSequence = setEvent.sequence, targetEndAtMillis = 70_000L)
        val activeSession = ActiveStrengthSession(
            routineId = routine.id,
            routineName = routine.name,
            entries = listOf(completedEntry),
            hasStarted = true,
            sessionStartedAtMillis = 1_000L,
            isSetScreenVisible = true,
            currentExerciseIndex = 0,
            currentSetIndex = 0,
            pendingExerciseIndex = 0,
            pendingSetIndex = 1,
            restEndAtMillis = restEvent.targetEndAtMillis,
            isRestSheetVisible = true,
            restTitle = restEvent.exerciseTitle,
            setEvents = listOf(setEvent),
            restEvents = listOf(restEvent),
            activeRestEventId = restEvent.id
        )
        val restoredRestUiState = StrengthRestUiState.restored(activeSession, nowMillis = 10_000L)

        val state = restoredStrengthSessionInteractionState(
            activeSession = activeSession,
            routineEntries = routine.entries,
            shouldStartImmediately = false,
            nowMillis = 10_000L,
            restoredRestUiState = restoredRestUiState
        )

        assertEquals(listOf(completedEntry), state.entries)
        assertEquals(listOf(setEvent), state.setEvents)
        assertEquals(listOf(restEvent), state.restEvents)
        assertEquals(restoredRestUiState, state.restUiState)
        assertEquals(0, state.navigationUiState.currentExerciseIndex)
        assertEquals(0, state.navigationUiState.currentSetIndex)
        assertEquals(0, state.navigationUiState.pendingExerciseIndex)
        assertEquals(1, state.navigationUiState.pendingSetIndex)
    }

    @Test
    fun completeCurrentSetCreatesSetEventAndStartsRestTogether() {
        val state = interactionState()

        val transition = requireNotNull(
            state.withCompletedCurrentSet(completedAtMillis = 10_000L)
        )
        val nextState = transition.state

        assertTrue(nextState.entries[0].records[0].completed)
        assertEquals(1, nextState.setEvents.single().sequence)
        assertEquals(1, nextState.restEvents.single().id)
        assertEquals(1, nextState.restUiState.activeRestEventId)
        assertEquals(120, nextState.restUiState.remainingSeconds)
        assertEquals(130_000L, nextState.restUiState.endAtMillis)
        assertEquals(0, nextState.navigationUiState.currentExerciseIndex)
        assertEquals(0, nextState.navigationUiState.currentSetIndex)
        assertEquals(0, nextState.navigationUiState.pendingExerciseIndex)
        assertEquals(1, nextState.navigationUiState.pendingSetIndex)
        assertEquals(StrengthRestOverlayCommand.STOP, transition.restOverlayCommand)
        assertTrue(transition.shouldRequestRestOverlayPermission)
    }

    @Test
    fun completeCurrentSetFromOverlayHidesSheetAndStartsRestOverlay() {
        val transition = requireNotNull(
            interactionState().withCompletedCurrentSetFromOverlay(completedAtMillis = 10_000L)
        )

        assertTrue(transition.state.entries[0].records[0].completed)
        assertEquals(false, transition.state.restUiState.isSheetVisible)
        assertEquals(130_000L, transition.state.restUiState.endAtMillis)
        assertEquals(StrengthRestOverlayCommand.START, transition.restOverlayCommand)
    }

    @Test
    fun completeCurrentSetAdvancesThreeExerciseSupersetToFirstExerciseNextSet() {
        val entries = defaultStrengthRoutines().first().entries.map { entry ->
            entry.copy(
                supersetGroupId = 7,
                restSeconds = 0,
                records = entry.records.map { record -> record.copy(restSeconds = "0") }
            )
        }
        val state = interactionState(entries = entries)

        val afterSquat = requireNotNull(state.withCompletedCurrentSet(completedAtMillis = 10_000L)).state
        val afterBench = requireNotNull(afterSquat.withCompletedCurrentSet(completedAtMillis = 20_000L)).state
        val afterRow = requireNotNull(afterBench.withCompletedCurrentSet(completedAtMillis = 30_000L)).state

        assertTrue(afterRow.entries[0].records[0].completed)
        assertTrue(afterRow.entries[1].records[0].completed)
        assertTrue(afterRow.entries[2].records[0].completed)
        assertEquals(0, afterRow.navigationUiState.currentExerciseIndex)
        assertEquals(1, afterRow.navigationUiState.currentSetIndex)
    }

    @Test
    fun updateRestSecondsStartsHiddenOverlayWhenSheetIsNotVisible() {
        val restEvent = activeRestEvent(targetEndAtMillis = 70_000L)
        val state = interactionState(
            restEvents = listOf(restEvent),
            restUiState = StrengthRestUiState(
                activeRestEventId = restEvent.id,
                remainingSeconds = 60,
                endAtMillis = restEvent.targetEndAtMillis,
                isSheetVisible = false,
                title = restEvent.exerciseTitle
            )
        )

        val transition = state.withUpdatedRestSeconds(
            seconds = 45,
            nowMillis = 10_000L
        )

        assertEquals(45, transition.state.restUiState.remainingSeconds)
        assertEquals(55_000L, transition.state.restUiState.endAtMillis)
        assertEquals(55_000L, transition.state.restEvents.single().targetEndAtMillis)
        assertEquals(StrengthRestOverlayCommand.START, transition.restOverlayCommand)
    }

    @Test
    fun replacingEntriesClearsActiveRestWhenCompletedSetEventNoLongerExists() {
        val completedEntry = routineEntry().withCompletedRecord(setIndex = 0)
        val setEvent = completedEntry.toSetEvent(sequence = 1, setIndex = 0)
        val restEvent = activeRestEvent(afterSetSequence = setEvent.sequence)
        val state = interactionState(
            entries = listOf(completedEntry),
            setEvents = listOf(setEvent),
            restEvents = listOf(restEvent),
            restUiState = StrengthRestUiState(
                activeRestEventId = restEvent.id,
                remainingSeconds = 60,
                endAtMillis = restEvent.targetEndAtMillis,
                isSheetVisible = true,
                title = restEvent.exerciseTitle
            )
        )

        val transition = state.withEntriesReplaced(
            nextEntries = listOf(routineEntry()),
            nowMillis = 10_000L
        )

        assertTrue(transition.state.setEvents.isEmpty())
        assertTrue(transition.state.restEvents.isEmpty())
        assertEquals(StrengthRestUiState.inactive(), transition.state.restUiState)
        assertEquals(StrengthRestOverlayCommand.STOP, transition.restOverlayCommand)
    }

    private fun interactionState(
        entries: List<StrengthRoutineEntry> = listOf(routineEntry()),
        setEvents: List<com.lighthousepark.intervalsgym.strength.StrengthSetCompletionEvent> = emptyList(),
        restEvents: List<StrengthRestEvent> = emptyList(),
        restUiState: StrengthRestUiState = StrengthRestUiState.inactive(),
    ): StrengthSessionInteractionState {
        return StrengthSessionInteractionState(
            entries = entries,
            setEvents = setEvents,
            restEvents = restEvents,
            restUiState = restUiState,
            navigationUiState = StrengthSessionNavigationUiState(
                isSetScreenVisible = true,
                currentExerciseIndex = 0,
                currentSetIndex = 0,
                pendingExerciseIndex = null,
                pendingSetIndex = null
            )
        )
    }

    private fun routineEntry(): StrengthRoutineEntry {
        return defaultStrengthRoutines().first().entries.first()
    }

    private fun activeRestEvent(
        afterSetSequence: Int = 1,
        targetEndAtMillis: Long = 70_000L,
    ): StrengthRestEvent {
        return StrengthRestEvent(
            id = 1,
            afterSetSequence = afterSetSequence,
            exerciseEntryId = 1,
            exerciseTitle = "스쿼트",
            setRecordId = 1,
            setIndex = 0,
            startedAtMillis = 10_000L,
            plannedSeconds = 60,
            targetEndAtMillis = targetEndAtMillis,
            endedAtMillis = null,
            endReason = null
        )
    }
}

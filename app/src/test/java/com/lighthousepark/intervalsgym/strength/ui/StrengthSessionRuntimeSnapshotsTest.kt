package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.StrengthRestEvent
import com.lighthousepark.intervalsgym.strength.StrengthRoutineUpdateSelection
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import org.junit.Assert.assertEquals
import org.junit.Test

class StrengthSessionRuntimeSnapshotsTest {
    @Test
    fun runtimeSnapshotBuildsInteractionResultAndActiveSnapshotsFromSameSourceFields() {
        val routine = defaultStrengthRoutines().first()
        val entry = routine.entries.first()
        val restEvent = StrengthRestEvent(
            id = 42,
            afterSetSequence = 7,
            exerciseEntryId = entry.id,
            exerciseTitle = entry.title,
            setRecordId = entry.records.first().id,
            setIndex = 0,
            startedAtMillis = 10_000L,
            plannedSeconds = 75,
            targetEndAtMillis = 85_000L,
            endedAtMillis = null,
            endReason = null
        )
        val navigationUiState = StrengthSessionNavigationUiState(
            isSetScreenVisible = false,
            currentExerciseIndex = 1,
            currentSetIndex = 2,
            pendingExerciseIndex = 0,
            pendingSetIndex = 1
        )
        val restUiState = StrengthRestUiState(
            activeRestEventId = restEvent.id,
            remainingSeconds = 60,
            endAtMillis = restEvent.targetEndAtMillis,
            isSheetVisible = true,
            title = restEvent.exerciseTitle
        )
        val finishUiState = StrengthSessionFinishUiState(
            finishRpe = 9,
            routineUpdateSelection = StrengthRoutineUpdateSelection(order = true)
        )
        val runtimeSnapshot = StrengthSessionRuntimeSnapshot(
            routine = routine,
            entries = listOf(entry),
            hasStarted = true,
            sessionStartedAtMillis = 1_000L,
            navigationUiState = navigationUiState,
            restUiState = restUiState,
            setEvents = emptyList(),
            restEvents = listOf(restEvent),
            finishUiState = finishUiState
        )

        val interactionState = runtimeSnapshot.toInteractionState()
        val resultSnapshot = runtimeSnapshot.toResultSnapshot()
        val activeSession = requireNotNull(runtimeSnapshot.toActiveSessionSnapshot().toActiveSession())

        assertEquals(listOf(entry), interactionState.entries)
        assertEquals(navigationUiState, interactionState.navigationUiState)
        assertEquals(restUiState, interactionState.restUiState)

        assertEquals(restEvent.id, resultSnapshot.activeRestEventId)
        assertEquals(1_000L, resultSnapshot.sessionStartedAtMillis)
        assertEquals(9, resultSnapshot.finishRpe)
        assertEquals(finishUiState.routineUpdateSelection, resultSnapshot.routineUpdateSelection)

        assertEquals(routine.id, activeSession.routineId)
        assertEquals(listOf(entry), activeSession.entries)
        assertEquals(routine.entries, activeSession.routineBaselineEntries)
        assertEquals(restEvent.id, activeSession.activeRestEventId)
        assertEquals(restEvent.targetEndAtMillis, activeSession.restEndAtMillis)
        assertEquals(navigationUiState.currentExerciseIndex, activeSession.currentExerciseIndex)
        assertEquals(navigationUiState.currentSetIndex, activeSession.currentSetIndex)
        assertEquals(navigationUiState.pendingExerciseIndex, activeSession.pendingExerciseIndex)
        assertEquals(navigationUiState.pendingSetIndex, activeSession.pendingSetIndex)
    }
}

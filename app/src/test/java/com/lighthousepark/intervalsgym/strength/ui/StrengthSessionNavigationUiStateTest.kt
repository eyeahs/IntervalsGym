package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.ActiveStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthSetCompletionFollowUp
import com.lighthousepark.intervalsgym.strength.StrengthSetCompletionResult
import com.lighthousepark.intervalsgym.strength.StrengthSetMetricType
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthSessionNavigationUiStateTest {
    @Test
    fun restoredUsesPendingSetWhenSavedRestAlreadyExpired() {
        val routine = defaultStrengthRoutines().first()
        val session = ActiveStrengthSession(
            routineId = routine.id,
            routineName = routine.name,
            entries = routine.entries,
            hasStarted = true,
            sessionStartedAtMillis = 1_000L,
            isSetScreenVisible = true,
            currentExerciseIndex = 0,
            currentSetIndex = 0,
            pendingExerciseIndex = 1,
            pendingSetIndex = 2,
            restEndAtMillis = 2_000L,
            isRestSheetVisible = true,
            restTitle = "스쿼트",
            setEvents = emptyList(),
            restEvents = emptyList(),
            activeRestEventId = 7
        )

        val state = StrengthSessionNavigationUiState.restored(
            activeSession = session,
            shouldStartImmediately = false,
            nowMillis = 3_000L,
            isRestActive = false
        )

        assertEquals(1, state.currentExerciseIndex)
        assertEquals(2, state.currentSetIndex)
        assertNull(state.pendingExerciseIndex)
        assertNull(state.pendingSetIndex)
    }

    @Test
    fun openExerciseSetSelectsFirstIncompleteSetAndClearsPendingTarget() {
        val routine = defaultStrengthRoutines().first()
        val entries = routine.entries.mapIndexed { index, entry ->
            if (index == 0) {
                entry.copy(records = entry.records.mapIndexed { setIndex, record ->
                    record.copy(completed = setIndex == 0)
                })
            } else {
                entry
            }
        }
        val state = StrengthSessionNavigationUiState(
            isSetScreenVisible = false,
            currentExerciseIndex = 1,
            currentSetIndex = 0,
            pendingExerciseIndex = 2,
            pendingSetIndex = 1
        )

        val opened = state.openExerciseSet(entries, exerciseIndex = 0)

        assertTrue(opened.isSetScreenVisible)
        assertEquals(0, opened.currentExerciseIndex)
        assertEquals(1, opened.currentSetIndex)
        assertNull(opened.pendingExerciseIndex)
        assertNull(opened.pendingSetIndex)
    }

    @Test
    fun applyCompletedSetResultAndMoveToPendingSetKeepIndicesTogether() {
        val state = StrengthSessionNavigationUiState(
            isSetScreenVisible = true,
            currentExerciseIndex = 0,
            currentSetIndex = 0,
            pendingExerciseIndex = null,
            pendingSetIndex = null
        )
        val result = StrengthSetCompletionResult(
            entries = defaultStrengthRoutines().first().entries,
            setEvent = null,
            currentExerciseIndex = 0,
            currentSetIndex = 1,
            pendingExerciseIndex = 2,
            pendingSetIndex = 0,
            restEvent = null,
            followUp = StrengthSetCompletionFollowUp.START_REST
        )

        val afterSet = state.applyCompletedSetResult(result)
        val afterRest = afterSet.moveToPendingSet()

        assertEquals(0, afterSet.currentExerciseIndex)
        assertEquals(1, afterSet.currentSetIndex)
        assertEquals(2, afterSet.pendingExerciseIndex)
        assertEquals(0, afterSet.pendingSetIndex)
        assertEquals(2, afterRest.currentExerciseIndex)
        assertEquals(0, afterRest.currentSetIndex)
        assertNull(afterRest.pendingExerciseIndex)
        assertNull(afterRest.pendingSetIndex)
    }

    @Test
    fun finishAllSetsHidesSetScreenAndClearsPendingTarget() {
        val state = StrengthSessionNavigationUiState(
            isSetScreenVisible = true,
            currentExerciseIndex = 1,
            currentSetIndex = 2,
            pendingExerciseIndex = 2,
            pendingSetIndex = 0
        )

        val finished = state.finishAllSets()

        assertFalse(finished.isSetScreenVisible)
        assertEquals(1, finished.currentExerciseIndex)
        assertEquals(2, finished.currentSetIndex)
        assertNull(finished.pendingExerciseIndex)
        assertNull(finished.pendingSetIndex)
    }

    @Test
    fun pendingTimedSetDurationSeconds_returnsOnlyUpcomingDurationSetTime() {
        val entries = defaultStrengthRoutines().first().entries
        val timedEntries = entries.mapIndexed { index, entry ->
            if (index == 1) {
                entry.copy(
                    setMetricType = StrengthSetMetricType.DURATION,
                    records = entry.records.map { it.copy(durationSeconds = "45") }
                )
            } else {
                entry
            }
        }
        val timedState = StrengthSessionNavigationUiState(
            isSetScreenVisible = true,
            currentExerciseIndex = 0,
            currentSetIndex = 0,
            pendingExerciseIndex = 1,
            pendingSetIndex = 0
        )
        val repsState = timedState.copy(pendingExerciseIndex = 0)

        assertEquals(45, timedState.pendingTimedSetDurationSeconds(timedEntries))
        assertNull(repsState.pendingTimedSetDurationSeconds(timedEntries))
        assertNull(timedState.copy(pendingSetIndex = null).pendingTimedSetDurationSeconds(timedEntries))
    }

    @Test
    fun keepEntrySelectionAfterReorderTracksEntryIds() {
        val entries = defaultStrengthRoutines().first().entries
        val state = StrengthSessionNavigationUiState(
            isSetScreenVisible = false,
            currentExerciseIndex = 0,
            currentSetIndex = 0,
            pendingExerciseIndex = 1,
            pendingSetIndex = 0
        )
        val reordered = listOf(entries[1], entries[0]) + entries.drop(2)

        val kept = state.keepEntrySelectionAfterReorder(entries, reordered)

        assertEquals(1, kept.currentExerciseIndex)
        assertEquals(0, kept.pendingExerciseIndex)
    }
}

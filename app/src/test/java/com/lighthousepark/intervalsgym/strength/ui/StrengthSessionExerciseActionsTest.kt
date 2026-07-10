package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.completedStrengthSession
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import com.lighthousepark.intervalsgym.strength.strengthExerciseCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class StrengthSessionExerciseActionsTest {
    @Test
    fun addExerciseReservesNextIdWithoutAppendingPlaceholderEntry() {
        val state = interactionState()

        val result = state.withAddedExercise(
            exerciseChangeUiState = StrengthExerciseChangeUiState.inactive()
        )
        val nextExerciseChangeUiState = requireNotNull(result.exerciseChangeUiState)

        assertEquals(null, result.transition)
        assertEquals(3, state.entries.size)
        assertEquals(4, nextExerciseChangeUiState.pendingAddedExerciseEntryId)
        assertFalse(nextExerciseChangeUiState.isCurrentExerciseTypeDialogVisible)
    }

    @Test
    fun configurePendingAddedExerciseRestoresLatestMatchingHistoryAndReturnsToOngoingList() {
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val historyEntry = defaultStrengthRoutineEntry(
            id = 7,
            exercise = bench,
            weightKg = "82.5",
            reps = "5",
            restSeconds = "180"
        )
        val placeholder = defaultStrengthRoutineEntry(
            id = 9,
            exercise = strengthExerciseCatalog.first()
        )
        val state = interactionState(
            entries = defaultStrengthRoutines().first().entries + placeholder,
            navigationUiState = StrengthSessionNavigationUiState(
                isSetScreenVisible = true,
                currentExerciseIndex = 3,
                currentSetIndex = 0,
                pendingExerciseIndex = null,
                pendingSetIndex = null
            )
        )
        val exerciseChangeUiState = StrengthExerciseChangeUiState
            .inactive()
            .beginAddedExercise(entryId = placeholder.id)

        val result = requireNotNull(
            state.withConfiguredExercise(
                exerciseChangeUiState = exerciseChangeUiState,
                completedStrengthHistory = listOf(
                    completedStrengthSession(
                        id = "bench-history",
                        startedAtMillis = 1_000L,
                        entries = listOf(historyEntry),
                        setEvents = emptyList()
                    )
                ),
                exercise = bench,
                equipment = historyEntry.equipment,
                variation = historyEntry.variation,
                nowMillis = 10_000L
            )
        )
        val nextState = requireNotNull(result.transition).state
        val configuredEntry = nextState.entries.last()

        assertEquals(placeholder.id, configuredEntry.id)
        assertEquals(bench.id, configuredEntry.exercise.id)
        assertEquals("82.5", configuredEntry.records.first().weightKg)
        assertEquals("5", configuredEntry.records.first().reps)
        assertEquals("180", configuredEntry.records.first().restSeconds)
        assertFalse(configuredEntry.records.first().completed)
        assertFalse(nextState.navigationUiState.isSetScreenVisible)
        assertEquals(StrengthExerciseChangeUiState.inactive(), result.exerciseChangeUiState)
    }

    @Test
    fun configureMissingPendingAddedExerciseRecoversWithoutOverwritingExistingExercise() {
        val entries = defaultStrengthRoutines().first().entries
        val hackSquat = strengthExerciseCatalog.first { it.id == "hack_squat" }
        val state = interactionState(
            entries = entries,
            navigationUiState = StrengthSessionNavigationUiState(
                isSetScreenVisible = true,
                currentExerciseIndex = entries.lastIndex,
                currentSetIndex = 0,
                pendingExerciseIndex = null,
                pendingSetIndex = null
            )
        )
        val missingPendingEntryId = entries.maxOf { it.id } + 1
        val exerciseChangeUiState = StrengthExerciseChangeUiState
            .inactive()
            .beginAddedExercise(entryId = missingPendingEntryId)

        val result = requireNotNull(
            state.withConfiguredExercise(
                exerciseChangeUiState = exerciseChangeUiState,
                completedStrengthHistory = emptyList(),
                exercise = hackSquat,
                equipment = "머신",
                variation = "기본",
                nowMillis = 10_000L
            )
        )
        val nextState = requireNotNull(result.transition).state

        assertEquals(entries.map { it.exercise.id }, nextState.entries.dropLast(1).map { it.exercise.id })
        assertEquals(entries.size + 1, nextState.entries.size)
        assertEquals(missingPendingEntryId, nextState.entries.last().id)
        assertEquals(hackSquat.id, nextState.entries.last().exercise.id)
        assertEquals("머신", nextState.entries.last().equipment)
        assertEquals("기본", nextState.entries.last().variation)
        assertEquals(nextState.entries.lastIndex, nextState.navigationUiState.currentExerciseIndex)
        assertFalse(nextState.navigationUiState.isSetScreenVisible)
        assertEquals(StrengthExerciseChangeUiState.inactive(), result.exerciseChangeUiState)
    }

    @Test
    fun reorderExercisesNormalizesEntriesAndKeepsSelectionByEntryId() {
        val entries = defaultStrengthRoutines().first().entries
        val state = interactionState(
            entries = entries,
            navigationUiState = StrengthSessionNavigationUiState(
                isSetScreenVisible = false,
                currentExerciseIndex = 0,
                currentSetIndex = 0,
                pendingExerciseIndex = 1,
                pendingSetIndex = 0
            )
        )
        val reordered = listOf(entries[1], entries[0]) + entries.drop(2)

        val transition = requireNotNull(
            state.withReorderedExercises(
                nextEntries = reordered,
                nowMillis = 10_000L
            )
        )

        assertEquals(listOf(entries[1].id, entries[0].id, entries[2].id), transition.state.entries.map { it.id })
        assertEquals(1, transition.state.navigationUiState.currentExerciseIndex)
        assertEquals(0, transition.state.navigationUiState.pendingExerciseIndex)
    }

    @Test
    fun cancelExerciseChangeRemovesPendingAddedEntryAndFinishesChangeFlow() {
        val entries = defaultStrengthRoutines().first().entries
        val pendingEntry = defaultStrengthRoutineEntry(
            id = 12,
            exercise = strengthExerciseCatalog.first()
        )
        val state = interactionState(
            entries = entries + pendingEntry,
            navigationUiState = StrengthSessionNavigationUiState(
                isSetScreenVisible = true,
                currentExerciseIndex = 3,
                currentSetIndex = 0,
                pendingExerciseIndex = null,
                pendingSetIndex = null
            )
        )
        val exerciseChangeUiState = StrengthExerciseChangeUiState
            .inactive()
            .beginAddedExercise(entryId = pendingEntry.id)

        val result = state.withCanceledExerciseChange(
            exerciseChangeUiState = exerciseChangeUiState,
            nowMillis = 10_000L
        )
        val nextState = requireNotNull(result.transition).state
        val nextNavigation = requireNotNull(result.navigationUiState)

        assertEquals(entries.map { it.id }, nextState.entries.map { it.id })
        assertEquals(2, nextNavigation.currentExerciseIndex)
        assertFalse(nextNavigation.isSetScreenVisible)
        assertEquals(StrengthExerciseChangeUiState.inactive(), result.exerciseChangeUiState)
    }

    private fun interactionState(
        entries: List<StrengthRoutineEntry> = defaultStrengthRoutines().first().entries,
        navigationUiState: StrengthSessionNavigationUiState = StrengthSessionNavigationUiState(
            isSetScreenVisible = false,
            currentExerciseIndex = 0,
            currentSetIndex = 0,
            pendingExerciseIndex = null,
            pendingSetIndex = null
        ),
    ): StrengthSessionInteractionState {
        return StrengthSessionInteractionState(
            entries = entries,
            setEvents = emptyList(),
            restEvents = emptyList(),
            restUiState = StrengthRestUiState.inactive(),
            navigationUiState = navigationUiState
        )
    }
}

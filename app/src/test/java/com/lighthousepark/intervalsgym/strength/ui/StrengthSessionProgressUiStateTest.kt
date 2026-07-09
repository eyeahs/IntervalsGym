package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.ActiveStrengthSession
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthSessionProgressUiStateTest {
    @Test
    fun restoredIdleSessionKeepsStartTimeEmpty() {
        val state = StrengthSessionProgressUiState.restored(
            activeSession = null,
            shouldStartImmediately = false,
            nowMillis = 10_000L
        )

        assertFalse(state.hasStarted)
        assertEquals(0L, state.sessionStartedAtMillis)
        assertEquals(0, state.sessionElapsedSeconds)
    }

    @Test
    fun restoredImmediateStartUsesCurrentTime() {
        val state = StrengthSessionProgressUiState.restored(
            activeSession = null,
            shouldStartImmediately = true,
            nowMillis = 10_000L
        )

        assertTrue(state.hasStarted)
        assertEquals(10_000L, state.sessionStartedAtMillis)
        assertEquals(0, state.sessionElapsedSeconds)
    }

    @Test
    fun restoredActiveSessionCalculatesElapsedSeconds() {
        val state = StrengthSessionProgressUiState.restored(
            activeSession = activeSession(
                hasStarted = true,
                sessionStartedAtMillis = 2_000L
            ),
            shouldStartImmediately = false,
            nowMillis = 12_500L
        )

        assertTrue(state.hasStarted)
        assertEquals(2_000L, state.sessionStartedAtMillis)
        assertEquals(10, state.sessionElapsedSeconds)
    }

    @Test
    fun startedAndElapsedUpdatesKeepProgressFieldsTogether() {
        val state = StrengthSessionProgressUiState.restored(
            activeSession = null,
            shouldStartImmediately = false,
            nowMillis = 10_000L
        )
            .started(nowMillis = 20_000L)
            .withElapsedSeconds(-5)

        assertTrue(state.hasStarted)
        assertEquals(20_000L, state.sessionStartedAtMillis)
        assertEquals(0, state.sessionElapsedSeconds)
    }

    private fun activeSession(
        hasStarted: Boolean,
        sessionStartedAtMillis: Long,
    ): ActiveStrengthSession {
        val routine = defaultStrengthRoutines().first()
        return ActiveStrengthSession(
            routineId = routine.id,
            routineName = routine.name,
            entries = routine.entries,
            hasStarted = hasStarted,
            sessionStartedAtMillis = sessionStartedAtMillis,
            isSetScreenVisible = false,
            currentExerciseIndex = 0,
            currentSetIndex = 0,
            pendingExerciseIndex = null,
            pendingSetIndex = null,
            restEndAtMillis = 0L,
            isRestSheetVisible = false,
            restTitle = "",
            setEvents = emptyList(),
            restEvents = emptyList(),
            activeRestEventId = null
        )
    }
}

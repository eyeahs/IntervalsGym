package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.ActiveStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthRestEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthRestUiStateTest {
    @Test
    fun remainingSecondsUsesWallClockAndRoundsPartialSecondsUp() {
        assertEquals(0, remainingStrengthRestSeconds(endAtMillis = 10_000L, nowMillis = 10_000L))
        assertEquals(1, remainingStrengthRestSeconds(endAtMillis = 10_001L, nowMillis = 10_000L))
        assertEquals(15, remainingStrengthRestSeconds(endAtMillis = 25_000L, nowMillis = 10_500L))
    }

    @Test
    fun restored_usesOnlyActiveFutureRest() {
        val session = activeSession(
            restEndAtMillis = 30_000L,
            isRestSheetVisible = true,
            restTitle = "Squat",
            activeRestEventId = 7
        )

        val restored = StrengthRestUiState.restored(session, nowMillis = 10_000L)
        val expired = StrengthRestUiState.restored(session.copy(restEndAtMillis = 9_000L), nowMillis = 10_000L)

        assertEquals(7, restored.activeRestEventId)
        assertEquals(20, restored.remainingSeconds)
        assertEquals(30_000L, restored.endAtMillis)
        assertEquals("Squat", restored.title)
        assertTrue(restored.isSheetVisible)
        assertEquals(StrengthRestUiState.inactive(), expired)
    }

    @Test
    fun syncedWithActiveRestEvent_returnsNullWhenActiveEventDisappears() {
        val state = StrengthRestUiState(
            activeRestEventId = 3,
            remainingSeconds = 10,
            endAtMillis = 20_000L,
            isSheetVisible = true,
            title = "Bench"
        )

        assertNull(state.syncedWithActiveRestEvent(restEvents = emptyList(), nowMillis = 10_000L))
    }

    @Test
    fun syncedWithActiveRestEvent_updatesTitleAndRemainingTimeTogether() {
        val state = StrengthRestUiState(
            activeRestEventId = 3,
            remainingSeconds = 10,
            endAtMillis = 20_000L,
            isSheetVisible = true,
            title = "Bench"
        )
        val restEvent = StrengthRestEvent(
            id = 3,
            afterSetSequence = 1,
            exerciseEntryId = 2,
            exerciseTitle = "Row",
            setRecordId = 1,
            setIndex = 0,
            startedAtMillis = 1_000L,
            plannedSeconds = 30,
            targetEndAtMillis = 40_000L,
            endedAtMillis = null,
            endReason = null
        )

        val synced = state.syncedWithActiveRestEvent(
            restEvents = listOf(restEvent),
            nowMillis = 25_500L
        )

        requireNotNull(synced)
        assertEquals("Row", synced.title)
        assertEquals(14, synced.remainingSeconds)
        assertEquals(40_000L, synced.endAtMillis)
        assertTrue(synced.isSheetVisible)
    }

    private fun activeSession(
        restEndAtMillis: Long,
        isRestSheetVisible: Boolean,
        restTitle: String,
        activeRestEventId: Int?,
    ): ActiveStrengthSession {
        return ActiveStrengthSession(
            routineId = 1,
            routineName = "Routine",
            entries = emptyList(),
            hasStarted = true,
            sessionStartedAtMillis = 1_000L,
            isSetScreenVisible = true,
            currentExerciseIndex = 0,
            currentSetIndex = 0,
            pendingExerciseIndex = null,
            pendingSetIndex = null,
            restEndAtMillis = restEndAtMillis,
            isRestSheetVisible = isRestSheetVisible,
            restTitle = restTitle,
            setEvents = emptyList(),
            restEvents = emptyList(),
            activeRestEventId = activeRestEventId
        )
    }
}

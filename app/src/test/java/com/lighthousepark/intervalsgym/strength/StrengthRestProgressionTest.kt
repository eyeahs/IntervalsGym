package com.lighthousepark.intervalsgym.strength

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthRestProgressionTest {
    @Test
    fun closeActiveStrengthRestEvent_closesOnlyActiveOpenRest() {
        val events = listOf(
            strengthRestEvent(id = 1, endedAtMillis = null),
            strengthRestEvent(id = 2, endedAtMillis = null)
        )

        val result = closeActiveStrengthRestEvent(
            restEvents = events,
            activeRestEventId = 2,
            endedAtMillis = 5_000L,
            reason = "stopped"
        )

        assertEquals(null, result.restEvents[0].endedAtMillis)
        assertEquals(5_000L, result.restEvents[1].endedAtMillis)
        assertEquals("stopped", result.restEvents[1].endReason)
        assertEquals(null, result.activeRestEventId)
    }

    @Test
    fun startStrengthRestTimer_addsRestEventAndUsesItsTargetEnd() {
        val restEvent = strengthRestEvent(
            id = 7,
            targetEndAtMillis = 20_000L
        )

        val result = requireNotNull(
            startStrengthRestTimer(
                restEvents = emptyList(),
                title = "스쿼트",
                seconds = 90,
                nowMillis = 10_000L,
                restEvent = restEvent
            )
        )

        assertEquals(listOf(restEvent), result.restEvents)
        assertEquals(7, result.activeRestEventId)
        assertEquals("스쿼트", result.restTitle)
        assertEquals(90, result.restRemainingSeconds)
        assertEquals(20_000L, result.restEndAtMillis)
        assertTrue(result.isRestSheetVisible)
    }

    @Test
    fun updateStrengthRestTimerSeconds_updatesActiveOpenRestTarget() {
        val events = listOf(
            strengthRestEvent(id = 1, targetEndAtMillis = 10_000L),
            strengthRestEvent(id = 2, targetEndAtMillis = 11_000L)
        )

        val result = requireNotNull(
            updateStrengthRestTimerSeconds(
                restEvents = events,
                activeRestEventId = 2,
                seconds = 45,
                nowMillis = 20_000L
            )
        )

        assertEquals(10_000L, result.restEvents[0].targetEndAtMillis)
        assertEquals(65_000L, result.restEvents[1].targetEndAtMillis)
        assertEquals(45, result.restRemainingSeconds)
        assertEquals(65_000L, result.restEndAtMillis)
    }
}

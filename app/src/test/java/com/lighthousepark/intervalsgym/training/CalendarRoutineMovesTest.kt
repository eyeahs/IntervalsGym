package com.lighthousepark.intervalsgym.training

import com.lighthousepark.intervalsgym.data.intervalsRoutineExternalId
import com.lighthousepark.intervalsgym.data.scheduledStrengthRoutineId
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarRoutineMovesTest {
    @Test
    fun pendingCalendarRoutineMoves_renderSyntheticTargetWithTimeAwareStrengthExternalId() {
        val sourceDate = LocalDate.of(2026, 7, 8)
        val targetDate = LocalDate.of(2026, 7, 9)
        val time = LocalTime.of(19, 30)
        val strengthRoutine = defaultStrengthRoutines().first().copy(id = 77, name = "퇴근 후")
        val sourceRoutine = trainingItem(
            id = "local-${strengthRoutine.scheduledStrengthRoutineId(sourceDate, time)}",
            remoteId = strengthRoutine.scheduledStrengthRoutineId(sourceDate, time),
            externalId = strengthRoutine.intervalsRoutineExternalId(sourceDate, time),
            type = "Weight Training",
            name = strengthRoutine.name,
            date = sourceDate,
            startedAt = LocalDateTime.of(sourceDate, time),
            timeLabel = "19:30",
            isRoutine = true,
            matchedStrengthRoutine = strengthRoutine
        )
        val pendingMove = PendingCalendarRoutineMove(sourceRoutine, targetDate)

        val renderData = listOf(sourceRoutine).withPendingCalendarRoutineMoves(
            pendingMoves = listOf(pendingMove),
            start = sourceDate,
            end = targetDate
        )
        val syntheticTarget = renderData.routines.single()

        assertEquals(targetDate, syntheticTarget.date)
        assertEquals(LocalDateTime.of(targetDate, time), syntheticTarget.startedAt)
        assertEquals("19:30", syntheticTarget.timeLabel)
        assertEquals(strengthRoutine.intervalsRoutineExternalId(targetDate, time), syntheticTarget.externalId)
        assertTrue(renderData.pendingRoutineKeys.contains(strengthRoutine.intervalsRoutineExternalId(targetDate, time)))
        assertFalse(renderData.routines.any { it.id == sourceRoutine.id })
    }

    @Test
    fun pendingCalendarRoutineMoves_dropWhenRemoteTargetIsReflectedWithoutSource() {
        val sourceDate = LocalDate.of(2026, 7, 8)
        val targetDate = LocalDate.of(2026, 7, 9)
        val sourceRoutine = trainingItem(
            id = "routine-source",
            remoteId = "remote-source",
            externalId = "source-external",
            type = "Run",
            date = sourceDate,
            isRoutine = true
        )
        val pendingMove = PendingCalendarRoutineMove(sourceRoutine, targetDate)
        val reflectedTarget = trainingItem(
            id = "routine-target",
            remoteId = "remote-target",
            externalId = pendingMove.targetExternalId,
            type = "Run",
            date = targetDate,
            isRoutine = true
        )

        val remainingMoves = listOf(pendingMove).withoutReflectedMoves(listOf(reflectedTarget))

        assertTrue(remainingMoves.isEmpty())
    }

    @Test
    fun pendingCalendarRoutineMoveCollections_addDetectAndRemoveByIdentity() {
        val sourceDate = LocalDate.of(2026, 7, 8)
        val targetDate = LocalDate.of(2026, 7, 9)
        val sourceRoutine = trainingItem(
            id = "routine-source",
            remoteId = "remote-source",
            externalId = "source-external",
            type = "Run",
            date = sourceDate,
            isRoutine = true
        )
        val unrelatedRoutine = trainingItem(
            id = "routine-other",
            remoteId = "remote-other",
            externalId = "other-external",
            type = "Run",
            date = sourceDate,
            isRoutine = true
        )
        val pendingMove = PendingCalendarRoutineMove(sourceRoutine, targetDate)

        val withoutConnection = emptyMap<String, PendingCalendarRoutineMove>()
            .withPendingCalendarRoutineMove(pendingMove, isRemoteConnected = false)
        val withConnection = withoutConnection.withPendingCalendarRoutineMove(
            move = pendingMove,
            isRemoteConnected = true
        )

        assertTrue(withoutConnection.isEmpty())
        assertTrue(withConnection.values.hasPendingCalendarRoutineMoveFor(sourceRoutine))
        assertFalse(withConnection.values.hasPendingCalendarRoutineMoveFor(unrelatedRoutine))
        assertTrue(withConnection.withoutCalendarRoutineMove(pendingMove).isEmpty())
    }
}

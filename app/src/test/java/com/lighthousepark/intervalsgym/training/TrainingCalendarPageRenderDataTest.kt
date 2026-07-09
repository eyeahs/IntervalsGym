package com.lighthousepark.intervalsgym.training

import com.lighthousepark.intervalsgym.data.intervalsRoutineExternalId
import com.lighthousepark.intervalsgym.data.scheduledStrengthRoutineId
import com.lighthousepark.intervalsgym.strength.ScheduledStrengthRoutine
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingCalendarPageRenderDataTest {
    @Test
    fun trainingCalendarPageRenderData_filtersDeletedRendersPendingMovesAndSortsItems() {
        val range = TrainingDateRange(
            start = LocalDate.of(2026, 7, 6),
            end = LocalDate.of(2026, 7, 12)
        )
        val sourceDate = LocalDate.of(2026, 7, 8)
        val targetDate = LocalDate.of(2026, 7, 11)
        val strengthRoutine = defaultStrengthRoutines().first().copy(id = 88, name = "하체")
        val remoteSourceRoutine = trainingItem(
            id = "routine-source",
            remoteId = "remote-source",
            externalId = "source-external",
            type = "Weight Training",
            date = sourceDate,
            isRoutine = true,
            matchedStrengthRoutine = strengthRoutine
        )
        val deletedRoutine = trainingItem(
            id = "routine-deleted",
            remoteId = "remote-deleted",
            externalId = "deleted-external",
            type = "Run",
            date = sourceDate,
            isRoutine = true
        )
        val result = trainingItem(
            id = "activity-1",
            type = "Run",
            date = LocalDate.of(2026, 7, 9),
            isRoutine = false
        )
        val scheduled = ScheduledStrengthRoutine(
            id = strengthRoutine.scheduledStrengthRoutineId(LocalDate.of(2026, 7, 10)),
            date = LocalDate.of(2026, 7, 10),
            routine = strengthRoutine,
            uploadedToIntervals = false,
            externalId = strengthRoutine.intervalsRoutineExternalId(LocalDate.of(2026, 7, 10))
        )
        val pendingMove = PendingCalendarRoutineMove(remoteSourceRoutine, targetDate)

        val renderData = buildTrainingCalendarPageRenderData(
            pageRange = range,
            baseDate = LocalDate.of(2026, 7, 9),
            calendarMode = TrainingCalendarMode.WEEK,
            isRemoteConnected = true,
            isLoadedPage = true,
            loadedActivities = listOf(result),
            loadedRoutines = listOf(remoteSourceRoutine, deletedRoutine),
            cachedRemoteData = null,
            localStrengthHistory = emptyList(),
            localRunningHistory = emptyList(),
            localScheduledStrengthRoutines = listOf(scheduled),
            strengthRoutines = listOf(strengthRoutine),
            deletedCalendarRoutineIds = emptySet(),
            optimisticallyDeletedCalendarRoutineKeys = setOf("deleted-external"),
            pendingCalendarRoutineMoves = listOf(pendingMove)
        )

        assertEquals(LocalDate.of(2026, 7, 9), renderData.initialScrollDate)
        assertTrue(renderData.movableScheduledRoutineKeys.contains(scheduled.id))
        assertTrue(renderData.pendingRoutineKeys.contains(pendingMove.targetExternalId))
        assertFalse(renderData.routines.any { it.id == remoteSourceRoutine.id })
        assertFalse(renderData.routines.any { it.id == deletedRoutine.id })
        assertTrue(renderData.routines.any { it.date == targetDate && it.externalId == pendingMove.targetExternalId })
        assertEquals(
            renderData.sortedItems.map { it.date },
            renderData.sortedItems.map { it.date }.sorted()
        )
    }
}

package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.data.CalendarRoutineDeleteScope
import com.lighthousepark.intervalsgym.data.CalendarRoutineSyncUseCase
import com.lighthousepark.intervalsgym.data.MemorySharedPreferences
import com.lighthousepark.intervalsgym.data.RecordingCalendarRoutineRemoteDataSource
import com.lighthousepark.intervalsgym.data.RecordingStrengthSessionRemoteDataSource
import com.lighthousepark.intervalsgym.data.StrengthSessionSyncUseCase
import com.lighthousepark.intervalsgym.data.intervalsRoutineExternalId
import com.lighthousepark.intervalsgym.data.loadCompletedStrengthSessionHistory
import com.lighthousepark.intervalsgym.data.loadScheduledStrengthRoutines
import com.lighthousepark.intervalsgym.data.scheduledStrengthRoutineId
import com.lighthousepark.intervalsgym.data.trainingItem
import com.lighthousepark.intervalsgym.data.upsertScheduledStrengthRoutine
import com.lighthousepark.intervalsgym.strength.ScheduledStrengthRoutine
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthSessionFinishActionsTest {
    @Test
    fun planFinishedStrengthSessionWithoutApiKeySavesLocalResult() {
        val prefs = MemorySharedPreferences()
        val syncUseCase = StrengthSessionSyncUseCase(
            prefs = prefs,
            remoteDataSource = RecordingStrengthSessionRemoteDataSource()
        )
        val snapshot = strengthResultSnapshotForTest()

        val action = snapshot.planFinishedStrengthSession(
            syncUseCase = syncUseCase,
            canUploadToIntervals = false,
            endedAtMillis = 20_000L
        )
        assertTrue(action is SaveFinishedStrengthSessionLocally)
        action as SaveFinishedStrengthSessionLocally
        val savedResult = requireNotNull(action.saveLocalResult(syncUseCase))
        val history = loadCompletedStrengthSessionHistory(prefs)

        assertTrue(action.shouldApplyToRoutine)
        assertFalse(savedResult.uploadedToIntervals)
        assertEquals("85", savedResult.setEvents.single().weightKg)
        assertEquals(1, history.size)
        assertFalse(history.single().uploadedToIntervals)
    }

    @Test
    fun planFinishedStrengthSessionWithApiKeyUploadsPlannedResult() = runBlocking {
        val prefs = MemorySharedPreferences()
        val remote = RecordingStrengthSessionRemoteDataSource()
        val syncUseCase = StrengthSessionSyncUseCase(
            prefs = prefs,
            remoteDataSource = remote
        )
        val snapshot = strengthResultSnapshotForTest()

        val action = snapshot.planFinishedStrengthSession(
            syncUseCase = syncUseCase,
            canUploadToIntervals = true,
            endedAtMillis = 20_000L
        )
        assertTrue(action is UploadFinishedStrengthSession)
        action as UploadFinishedStrengthSession
        val uploadedResult = action.uploadResult(syncUseCase)
        val history = loadCompletedStrengthSessionHistory(prefs)

        assertTrue(action.shouldApplyToRoutine)
        assertTrue(uploadedResult.uploadedToIntervals)
        assertEquals("85", uploadedResult.setEvents.single().weightKg)
        assertEquals(1, remote.uploads.size)
        assertEquals(uploadedResult.id, history.single().id)
        assertTrue(history.single().uploadedToIntervals)
    }

    @Test
    fun calendarRoutineDeleteActionDeletesThroughCalendarSyncUseCase() = runBlocking {
        val prefs = MemorySharedPreferences()
        val remote = RecordingCalendarRoutineRemoteDataSource()
        val syncUseCase = CalendarRoutineSyncUseCase(
            prefs = prefs,
            apiKey = "api-key",
            remoteDataSource = remote
        )
        val date = LocalDate.of(2026, 7, 8)
        val routine = defaultStrengthRoutines().first().copy(id = 501, name = "삭제 루틴")
        val scheduledRoutine = ScheduledStrengthRoutine(
            id = routine.scheduledStrengthRoutineId(date),
            date = date,
            routine = routine,
            uploadedToIntervals = true,
            externalId = routine.intervalsRoutineExternalId(date)
        )
        upsertScheduledStrengthRoutine(prefs, scheduledRoutine)
        val targetItem = trainingItem(
            id = "routine-delete",
            remoteId = "remote-delete",
            externalId = scheduledRoutine.externalId,
            type = "Weight Training",
            isRoutine = true,
            matchedStrengthRoutine = routine
        )

        val action = planStrengthSessionCalendarRoutineDelete(targetItem)

        require(action is StrengthSessionCalendarRoutineDeleteAction)
        action.delete(syncUseCase)

        assertEquals(targetItem, action.targetRoutine)
        assertTrue(loadScheduledStrengthRoutines(prefs).isEmpty())
        assertEquals(listOf("remote-delete"), remote.deletedEventIds)
        assertEquals(CalendarRoutineDeleteScope.REMOTE, syncUseCase.deleteScopeFor(targetItem))
        assertNull(planStrengthSessionCalendarRoutineDelete(null))
    }
}

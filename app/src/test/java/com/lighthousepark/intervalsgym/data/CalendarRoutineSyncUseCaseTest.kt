package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.strength.ScheduledStrengthRoutine
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarRoutineSyncUseCaseTest {
    @Test
    fun calendarRoutineSyncUseCase_uploadSavedStrengthRoutineMarksLocalRoutineUploaded() = runBlocking {
        val prefs = MemorySharedPreferences()
        val remote = RecordingCalendarRoutineRemoteDataSource()
        val useCase = CalendarRoutineSyncUseCase(
            prefs = prefs,
            apiKey = "api-key",
            remoteDataSource = remote
        )
        val date = LocalDate.of(2026, 7, 8)
        val time = LocalTime.of(19, 30)
        val routine = defaultStrengthRoutines().first().copy(id = 78, name = "퇴근 후")

        val localRoutine = useCase.saveStrengthRoutineLocally(routine, date, time)
        val uploadedRoutine = useCase.uploadSavedStrengthRoutine(localRoutine)
        val restored = loadScheduledStrengthRoutines(prefs).single()

        assertFalse(localRoutine.uploadedToIntervals)
        assertTrue(uploadedRoutine.uploadedToIntervals)
        assertTrue(restored.uploadedToIntervals)
        assertEquals(listOf(RecordedStrengthUpload(routine, date, time)), remote.strengthUploads)
    }

    @Test
    fun calendarRoutineSyncUseCase_syncMovedLocalStrengthRoutineUploadsAndDeletesRemoteSource() = runBlocking {
        val prefs = MemorySharedPreferences()
        val remote = RecordingCalendarRoutineRemoteDataSource()
        val useCase = CalendarRoutineSyncUseCase(
            prefs = prefs,
            apiKey = "api-key",
            remoteDataSource = remote
        )
        val sourceDate = LocalDate.of(2026, 7, 8)
        val targetDate = LocalDate.of(2026, 7, 9)
        val time = LocalTime.of(19, 30)
        val routine = defaultStrengthRoutines().first().copy(id = 79, name = "원격 이동")
        val scheduledRoutine = ScheduledStrengthRoutine(
            id = routine.scheduledStrengthRoutineId(sourceDate, time),
            date = sourceDate,
            time = time,
            routine = routine,
            uploadedToIntervals = true,
            externalId = routine.intervalsRoutineExternalId(sourceDate, time)
        )
        upsertScheduledStrengthRoutine(prefs, scheduledRoutine)
        val sourceItem = trainingItem(
            id = "routine-remote-event",
            remoteId = "remote-event",
            externalId = scheduledRoutine.externalId,
            name = routine.name,
            type = "Weight Training",
            isRoutine = true,
            matchedStrengthRoutine = routine,
            startedAt = LocalDateTime.of(sourceDate, time)
        )

        val movedRoutine = useCase.moveStrengthRoutineLocally(sourceItem, targetDate)
        useCase.syncMovedRoutine(sourceItem, targetDate, movedRoutine)
        val restored = loadScheduledStrengthRoutines(prefs).single()

        requireNotNull(movedRoutine)
        assertEquals(targetDate, restored.date)
        assertEquals(time, restored.time)
        assertTrue(restored.uploadedToIntervals)
        assertEquals(listOf(RecordedStrengthUpload(routine, targetDate, time)), remote.strengthUploads)
        assertEquals(listOf("remote-event"), remote.deletedEventIds)
    }

    @Test
    fun calendarRoutineSyncUseCase_deleteRoutineRemovesLocalRoutineWithoutRemoteCall() = runBlocking {
        val prefs = MemorySharedPreferences()
        val remote = RecordingCalendarRoutineRemoteDataSource()
        val useCase = CalendarRoutineSyncUseCase(
            prefs = prefs,
            apiKey = "api-key",
            remoteDataSource = remote
        )
        val date = LocalDate.of(2026, 7, 8)
        val routine = defaultStrengthRoutines().first().copy(id = 80, name = "로컬 삭제")
        val scheduledRoutine = ScheduledStrengthRoutine(
            id = routine.scheduledStrengthRoutineId(date),
            date = date,
            routine = routine,
            uploadedToIntervals = false,
            externalId = routine.intervalsRoutineExternalId(date)
        )
        upsertScheduledStrengthRoutine(prefs, scheduledRoutine)
        val targetItem = trainingItem(
            id = "local-${scheduledRoutine.id}",
            remoteId = scheduledRoutine.id,
            externalId = scheduledRoutine.externalId,
            type = "Weight Training",
            isRoutine = true,
            matchedStrengthRoutine = routine
        )

        val deleteScope = useCase.deleteRoutine(targetItem)

        assertEquals(CalendarRoutineDeleteScope.LOCAL, deleteScope)
        assertTrue(loadScheduledStrengthRoutines(prefs).isEmpty())
        assertTrue(remote.deletedEventIds.isEmpty())
    }

    @Test
    fun calendarRoutineSyncUseCase_deleteRoutineDeletesRemoteAndLocalMirror() = runBlocking {
        val prefs = MemorySharedPreferences()
        val remote = RecordingCalendarRoutineRemoteDataSource()
        val useCase = CalendarRoutineSyncUseCase(
            prefs = prefs,
            apiKey = "api-key",
            remoteDataSource = remote
        )
        val date = LocalDate.of(2026, 7, 8)
        val routine = defaultStrengthRoutines().first().copy(id = 81, name = "원격 삭제")
        val scheduledRoutine = ScheduledStrengthRoutine(
            id = routine.scheduledStrengthRoutineId(date),
            date = date,
            routine = routine,
            uploadedToIntervals = true,
            externalId = routine.intervalsRoutineExternalId(date)
        )
        upsertScheduledStrengthRoutine(prefs, scheduledRoutine)
        val targetItem = trainingItem(
            id = "routine-remote-delete",
            remoteId = "remote-delete",
            externalId = scheduledRoutine.externalId,
            type = "Weight Training",
            isRoutine = true,
            matchedStrengthRoutine = routine
        )

        val deleteScope = useCase.deleteRoutine(targetItem)

        assertEquals(CalendarRoutineDeleteScope.REMOTE, deleteScope)
        assertTrue(loadScheduledStrengthRoutines(prefs).isEmpty())
        assertEquals(listOf("remote-delete"), remote.deletedEventIds)
    }
}

package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.strength.StrengthRestEvent
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthSessionSyncUseCaseTest {
    @Test
    fun savesLocalStrengthSession() {
        val prefs = MemorySharedPreferences()
        val useCase = StrengthSessionSyncUseCase(
            prefs = prefs,
            remoteDataSource = RecordingStrengthSessionRemoteDataSource()
        )
        val workout = completedStrengthSessionForStorage(
            id = "strength-local-sync",
            routineName = "로컬 웨이트",
            startedAtMillis = 1_000L,
            endedAtMillis = 61_000L
        ).copy(uploadedToIntervals = true)

        val localWorkout = useCase.saveStrengthSessionLocally(workout)
        val history = loadCompletedStrengthSessionHistory(prefs)

        assertFalse(localWorkout.uploadedToIntervals)
        assertEquals(1, history.size)
        assertEquals(localWorkout.id, history.single().id)
        assertFalse(history.single().uploadedToIntervals)
    }

    @Test
    fun liveResultUpdatesCompletedSetAndRestDetails() {
        val prefs = MemorySharedPreferences()
        val useCase = StrengthSessionSyncUseCase(
            prefs = prefs,
            remoteDataSource = RecordingStrengthSessionRemoteDataSource()
        )
        val routine = defaultStrengthRoutines().first()
        val completedEntry = routine.entries.first().copy(
            records = routine.entries.first().records.mapIndexed { index, record ->
                if (index == 0) record.copy(weightKg = "80", reps = "5", restSeconds = "30", completed = true) else record
            }
        )
        val setEvent = strengthSetEventForStorage(completedEntry)
        val restEvent = StrengthRestEvent(
            id = 1,
            afterSetSequence = setEvent.sequence,
            exerciseEntryId = completedEntry.id,
            exerciseTitle = completedEntry.title,
            setRecordId = completedEntry.records.first().id,
            setIndex = 0,
            startedAtMillis = setEvent.completedAtMillis,
            plannedSeconds = setEvent.targetRestSeconds,
            targetEndAtMillis = setEvent.completedAtMillis + setEvent.targetRestSeconds * 1000L,
            endedAtMillis = null,
            endReason = null
        )
        val editedEntry = completedEntry.copy(
            records = completedEntry.records.mapIndexed { index, record ->
                if (index == 0) record.copy(weightKg = "85", reps = "4", restSeconds = "45", completed = true) else record
            }
        )

        useCase.saveLiveStrengthSessionResult(
            StrengthSessionResultDraft(
                routine = routine,
                entries = listOf(completedEntry),
                setEvents = listOf(setEvent),
                restEvents = listOf(restEvent),
                activeRestEventId = restEvent.id,
                sessionStartedAtMillis = 1_000L,
                endedAtMillis = 11_000L,
                endReason = "live_result_update",
                rpe = 7,
                appliedToRoutine = true
            )
        )
        useCase.saveLiveStrengthSessionResult(
            StrengthSessionResultDraft(
                routine = routine,
                entries = listOf(editedEntry),
                setEvents = listOf(setEvent),
                restEvents = listOf(restEvent),
                activeRestEventId = restEvent.id,
                sessionStartedAtMillis = 1_000L,
                endedAtMillis = 12_000L,
                endReason = "live_result_update",
                rpe = 7,
                appliedToRoutine = true
            )
        )

        val history = loadCompletedStrengthSessionHistory(prefs)
        assertEquals(1, history.size)
        assertEquals("85", history.single().setEvents.single().weightKg)
        assertEquals("4", history.single().setEvents.single().reps)
        assertEquals(45, history.single().setEvents.single().targetRestSeconds)
        assertEquals(45, history.single().restEvents.single().plannedSeconds)
        assertEquals(55_000L, history.single().restEvents.single().targetEndAtMillis)
        assertEquals(null, history.single().restEvents.single().endedAtMillis)
    }

    @Test
    fun finishedResultClosesActiveRest() {
        val prefs = MemorySharedPreferences()
        val useCase = StrengthSessionSyncUseCase(
            prefs = prefs,
            remoteDataSource = RecordingStrengthSessionRemoteDataSource()
        )
        val routine = defaultStrengthRoutines().first()
        val completedEntry = routine.entries.first().copy(
            records = routine.entries.first().records.mapIndexed { index, record ->
                if (index == 0) record.copy(completed = true) else record
            }
        )
        val setEvent = strengthSetEventForStorage(completedEntry)
        val restEvent = StrengthRestEvent(
            id = 1,
            afterSetSequence = setEvent.sequence,
            exerciseEntryId = completedEntry.id,
            exerciseTitle = completedEntry.title,
            setRecordId = completedEntry.records.first().id,
            setIndex = 0,
            startedAtMillis = setEvent.completedAtMillis,
            plannedSeconds = setEvent.targetRestSeconds,
            targetEndAtMillis = setEvent.completedAtMillis + setEvent.targetRestSeconds * 1000L,
            endedAtMillis = null,
            endReason = null
        )

        val result = requireNotNull(
            useCase.buildFinishedStrengthSessionResult(
                draft = StrengthSessionResultDraft(
                    routine = routine,
                    entries = listOf(completedEntry),
                    setEvents = listOf(setEvent),
                    restEvents = listOf(restEvent),
                    activeRestEventId = restEvent.id,
                    sessionStartedAtMillis = 1_000L,
                    endedAtMillis = 20_000L,
                    endReason = "workout_finished",
                    rpe = 7,
                    appliedToRoutine = true
                ),
                uploadedToIntervals = false
            )
        )

        assertEquals(20_000L, result.restEvents.single().endedAtMillis)
        assertEquals("workout_finished", result.restEvents.single().endReason)
    }

    @Test
    fun finishedResultWithoutCompletedSetsDoesNotCountPlannedVolume() {
        val useCase = StrengthSessionSyncUseCase(
            prefs = MemorySharedPreferences(),
            remoteDataSource = RecordingStrengthSessionRemoteDataSource()
        )
        val routine = defaultStrengthRoutines().first()

        val result = requireNotNull(
            useCase.buildFinishedStrengthSessionResult(
                draft = StrengthSessionResultDraft(
                    routine = routine,
                    entries = routine.entries,
                    setEvents = emptyList(),
                    restEvents = emptyList(),
                    activeRestEventId = null,
                    sessionStartedAtMillis = 1_000L,
                    endedAtMillis = 61_000L,
                    endReason = "workout_finished",
                    rpe = 7,
                    appliedToRoutine = true
                ),
                uploadedToIntervals = false
            )
        )

        assertTrue(result.setEvents.isEmpty())
        assertEquals(1, result.trainingLoad)
    }

    @Test
    fun uploadsAndReplacesLocalStrengthSession() = runBlocking {
        val prefs = MemorySharedPreferences()
        val remote = RecordingStrengthSessionRemoteDataSource()
        val useCase = StrengthSessionSyncUseCase(
            prefs = prefs,
            remoteDataSource = remote
        )
        val workout = completedStrengthSessionForStorage(
            id = "strength-upload-sync",
            routineName = "업로드 웨이트",
            startedAtMillis = 1_000L,
            endedAtMillis = 61_000L
        )

        val localWorkout = useCase.saveStrengthSessionLocally(workout)
        val uploadedWorkout = useCase.uploadStrengthSession(localWorkout)
        val history = loadCompletedStrengthSessionHistory(prefs)

        assertEquals(localWorkout.id, uploadedWorkout.id)
        assertTrue(uploadedWorkout.uploadedToIntervals)
        assertEquals(listOf(localWorkout.toStrengthSession()), remote.uploads)
        assertEquals(1, history.size)
        assertEquals(uploadedWorkout.id, history.single().id)
        assertTrue(history.single().uploadedToIntervals)
    }

    @Test
    fun uploadsAdHocStrengthSessionWithoutHistoryWrite() = runBlocking {
        val prefs = MemorySharedPreferences()
        val remote = RecordingStrengthSessionRemoteDataSource()
        val useCase = StrengthSessionSyncUseCase(
            prefs = prefs,
            remoteDataSource = remote
        )
        val session = completedStrengthSessionForStorage(
            id = "strength-ad-hoc-upload",
            routineName = "즉석 웨이트",
            startedAtMillis = 1_000L,
            endedAtMillis = 61_000L
        ).toStrengthSession()

        useCase.uploadStrengthSession(session)
        val history = loadCompletedStrengthSessionHistory(prefs)

        assertEquals(listOf(session), remote.uploads)
        assertTrue(history.isEmpty())
    }
}

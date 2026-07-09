package com.lighthousepark.intervalsgym.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RunningSessionSyncUseCaseTest {
    @Test
    fun savesLocalRunningSession() {
        val prefs = MemorySharedPreferences()
        val useCase = RunningSessionSyncUseCase(
            prefs = prefs,
            remoteDataSource = RecordingRunningSessionRemoteDataSource()
        )
        val session = runningSessionForStorage(name = "로컬 러닝")

        val localSession = useCase.saveRunningSessionLocally(session)
        val history = loadCompletedRunningSessionHistory(prefs)

        assertFalse(localSession.uploadedToIntervals)
        assertEquals(1, history.size)
        assertEquals(localSession.id, history.single().id)
        assertFalse(history.single().uploadedToIntervals)
    }

    @Test
    fun uploadsAndReplacesLocalRunningSession() = runBlocking {
        val prefs = MemorySharedPreferences()
        val remote = RecordingRunningSessionRemoteDataSource()
        val useCase = RunningSessionSyncUseCase(
            prefs = prefs,
            remoteDataSource = remote
        )
        val session = runningSessionForStorage(name = "업로드 러닝")

        val localSession = useCase.saveRunningSessionLocally(session)
        val uploadedSession = useCase.uploadRunningSession(session)
        val history = loadCompletedRunningSessionHistory(prefs)

        assertEquals(localSession.id, uploadedSession.id)
        assertTrue(uploadedSession.uploadedToIntervals)
        assertEquals(listOf(session), remote.uploads)
        assertEquals(1, history.size)
        assertEquals(uploadedSession.id, history.single().id)
        assertTrue(history.single().uploadedToIntervals)
    }

    @Test
    fun deletesLocalRunningSession() {
        val prefs = MemorySharedPreferences()
        val useCase = RunningSessionSyncUseCase(
            prefs = prefs,
            remoteDataSource = RecordingRunningSessionRemoteDataSource()
        )
        val kept = completedRunningSessionForStorage(
            id = "running-kept",
            name = "keep",
            startedAtMillis = 1_000L,
            endedAtMillis = 61_000L
        )
        val deleted = completedRunningSessionForStorage(
            id = "running-deleted",
            name = "delete",
            startedAtMillis = 2_000L,
            endedAtMillis = 62_000L
        )
        appendRunningSessionHistory(prefs, kept)
        appendRunningSessionHistory(prefs, deleted)

        useCase.deleteRunningSession(deleted.id)

        val history = loadCompletedRunningSessionHistory(prefs)
        assertEquals(listOf(kept.id), history.map { it.id })
    }
}

package com.lighthousepark.intervalsgym.data

import android.content.SharedPreferences
import com.lighthousepark.intervalsgym.running.CompletedRunningSession
import com.lighthousepark.intervalsgym.running.RunningSession
import com.lighthousepark.intervalsgym.running.toCompletedRunningSession

internal interface RunningSessionRemoteDataSource {
    suspend fun uploadRunningSession(session: RunningSession)
}

internal class IntervalsRunningSessionRemoteDataSource(
    private val repository: IntervalsRepository,
) : RunningSessionRemoteDataSource {
    override suspend fun uploadRunningSession(session: RunningSession) {
        repository.uploadRunningSession(session)
    }
}

/**
 * Owns local history writes and remote upload replacement for completed running sessions.
 */
internal class RunningSessionSyncUseCase(
    private val prefs: SharedPreferences,
    private val remoteDataSource: RunningSessionRemoteDataSource,
) {
    fun saveRunningSessionLocally(session: RunningSession): CompletedRunningSession {
        val localSession = session.toCompletedRunningSession(uploadedToIntervals = false)
        appendRunningSessionHistory(prefs, localSession)
        return localSession
    }

    suspend fun uploadRunningSession(session: RunningSession): CompletedRunningSession {
        remoteDataSource.uploadRunningSession(session)
        val uploadedSession = session.toCompletedRunningSession(uploadedToIntervals = true)
        replaceRunningSessionHistory(prefs, uploadedSession)
        return uploadedSession
    }

    fun deleteRunningSession(sessionId: String) {
        deleteRunningSessionHistory(prefs, sessionId)
    }
}

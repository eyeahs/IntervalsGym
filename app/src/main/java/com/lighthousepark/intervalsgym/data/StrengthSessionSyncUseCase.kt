package com.lighthousepark.intervalsgym.data

import android.content.SharedPreferences
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthRestEvent
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthSetCompletionEvent
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.completedVolumeKg
import com.lighthousepark.intervalsgym.strength.strengthTrainingLoadFromMetrics
import com.lighthousepark.intervalsgym.strength.totalCompletedVolumeKg
import com.lighthousepark.intervalsgym.strength.withCurrentStrengthRestDetails
import com.lighthousepark.intervalsgym.strength.withCurrentStrengthSetDetails

internal data class StrengthSessionResultDraft(
    val routine: StrengthWorkoutRoutine?,
    val entries: List<StrengthRoutineEntry>,
    val setEvents: List<StrengthSetCompletionEvent>,
    val restEvents: List<StrengthRestEvent>,
    val activeRestEventId: Int?,
    val sessionStartedAtMillis: Long,
    val endedAtMillis: Long,
    val endReason: String,
    val rpe: Int,
    val routineUpdateEntries: List<StrengthRoutineEntry>?,
)

internal interface StrengthSessionRemoteDataSource {
    suspend fun uploadStrengthSession(session: StrengthSession)
}

internal class IntervalsStrengthSessionRemoteDataSource(
    private val repository: IntervalsRepository,
) : StrengthSessionRemoteDataSource {
    override suspend fun uploadStrengthSession(session: StrengthSession) {
        repository.uploadStrengthSession(session)
    }
}

/**
 * Owns local history writes and remote upload replacement for completed strength sessions.
 */
internal class StrengthSessionSyncUseCase(
    private val prefs: SharedPreferences,
    private val remoteDataSource: StrengthSessionRemoteDataSource,
) {
    fun saveStrengthSessionLocally(workout: CompletedStrengthSession): CompletedStrengthSession {
        val localWorkout = workout.copy(uploadedToIntervals = false)
        appendStrengthSessionHistory(prefs, localWorkout)
        return localWorkout
    }

    fun buildFinishedStrengthSessionResult(
        draft: StrengthSessionResultDraft,
        uploadedToIntervals: Boolean,
    ): CompletedStrengthSession? {
        return draft.toCompletedStrengthSession(
            uploadedToIntervals = uploadedToIntervals,
            finalizeActiveRest = true
        )
    }

    fun saveLiveStrengthSessionResult(draft: StrengthSessionResultDraft): CompletedStrengthSession? {
        val localWorkout = draft.toCompletedStrengthSession(
            uploadedToIntervals = false,
            finalizeActiveRest = false
        ) ?: return null
        if (localWorkout.setEvents.isEmpty()) {
            deleteStrengthSessionHistory(prefs, localWorkout)
            return null
        }
        appendStrengthSessionHistory(prefs, localWorkout)
        return localWorkout
    }

    fun deleteLiveStrengthSessionResult(draft: StrengthSessionResultDraft) {
        draft.toCompletedStrengthSession(
            uploadedToIntervals = false,
            finalizeActiveRest = true
        )?.let { workout ->
            deleteStrengthSessionHistory(prefs, workout)
        }
    }

    suspend fun uploadStrengthSession(workout: CompletedStrengthSession): CompletedStrengthSession {
        remoteDataSource.uploadStrengthSession(workout.toStrengthSession())
        val uploadedWorkout = workout.copy(uploadedToIntervals = true)
        appendStrengthSessionHistory(prefs, uploadedWorkout)
        return uploadedWorkout
    }

    suspend fun uploadStrengthSession(session: StrengthSession) {
        remoteDataSource.uploadStrengthSession(session)
    }

    private fun StrengthSessionResultDraft.toCompletedStrengthSession(
        uploadedToIntervals: Boolean,
        finalizeActiveRest: Boolean,
    ): CompletedStrengthSession? {
        val workoutRoutine = routine ?: return null
        val safeEndedAtMillis = endedAtMillis.takeIf { it > 0L } ?: System.currentTimeMillis()
        val safeStartedAtMillis = sessionStartedAtMillis.takeIf { it > 0L } ?: safeEndedAtMillis
        val syncedSetEvents = setEvents.withCurrentStrengthSetDetails(entries)
        val syncedRestEvents = restEvents.withCurrentStrengthRestDetails(syncedSetEvents)
        val resultRestEvents = if (finalizeActiveRest) {
            finalizeRestEvents(
                restEvents = syncedRestEvents,
                activeRestEventId = activeRestEventId,
                endedAtMillis = safeEndedAtMillis,
                reason = endReason
            )
        } else {
            syncedRestEvents
        }
        val durationSeconds = ((safeEndedAtMillis - safeStartedAtMillis) / 1000L)
            .toInt()
            .coerceAtLeast(0)
        val hasCompletionEvents = syncedSetEvents.isNotEmpty()
        val trainingDurationSeconds = durationSeconds
        val trainingVolumeKg = if (hasCompletionEvents) {
            syncedSetEvents.totalCompletedVolumeKg(entries)
        } else {
            entries.completedVolumeKg()
        }
        return buildCompletedStrengthSession(
            routine = workoutRoutine,
            entries = entries,
            setEvents = syncedSetEvents,
            restEvents = resultRestEvents,
            startedAtMillis = safeStartedAtMillis,
            endedAtMillis = safeEndedAtMillis,
            rpe = rpe,
            trainingLoad = strengthTrainingLoadFromMetrics(
                durationSeconds = trainingDurationSeconds,
                volumeKg = trainingVolumeKg,
                rpe = rpe
            ),
            uploadedToIntervals = uploadedToIntervals,
            appliedToRoutine = routineUpdateEntries != null,
            routineUpdateEntries = routineUpdateEntries
        )
    }
}

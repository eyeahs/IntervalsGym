package com.lighthousepark.intervalsgym.data

import android.content.SharedPreferences
import com.lighthousepark.intervalsgym.strength.ScheduledStrengthRoutine
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.training.TrainingItem
import java.time.LocalDate
import java.time.LocalTime

internal interface CalendarRoutineRemoteDataSource {
    suspend fun uploadStrengthRoutine(
        routine: StrengthWorkoutRoutine,
        date: LocalDate,
        time: LocalTime?,
    )

    suspend fun uploadCalendarRoutineCopy(routine: TrainingItem, date: LocalDate)

    suspend fun deleteCalendarRoutine(eventId: String)
}

internal class IntervalsCalendarRoutineRemoteDataSource(
    private val repository: IntervalsRepository,
) : CalendarRoutineRemoteDataSource {
    override suspend fun uploadStrengthRoutine(
        routine: StrengthWorkoutRoutine,
        date: LocalDate,
        time: LocalTime?,
    ) {
        repository.uploadStrengthRoutine(routine, date, time)
    }

    override suspend fun uploadCalendarRoutineCopy(routine: TrainingItem, date: LocalDate) {
        repository.uploadCalendarRoutineCopy(routine, date)
    }

    override suspend fun deleteCalendarRoutine(eventId: String) {
        repository.deleteCalendarRoutine(eventId)
    }
}

internal enum class CalendarRoutineDeleteScope {
    LOCAL,
    REMOTE,
}

/**
 * Coordinates local scheduled-routine storage with Intervals.icu calendar side effects.
 *
 * Compose screens should call this instead of hand-assembling storage, upload,
 * cache invalidation, and delete order.
 */
internal class CalendarRoutineSyncUseCase(
    private val prefs: SharedPreferences,
    private val apiKey: String,
    private val remoteDataSource: CalendarRoutineRemoteDataSource,
) {
    fun saveStrengthRoutineLocally(
        routine: StrengthWorkoutRoutine,
        targetDate: LocalDate,
        targetTime: LocalTime?,
    ): ScheduledStrengthRoutine {
        val scheduledRoutine = ScheduledStrengthRoutine(
            id = routine.scheduledStrengthRoutineId(targetDate, targetTime),
            date = targetDate,
            time = targetTime,
            routine = routine,
            uploadedToIntervals = false,
            externalId = routine.intervalsRoutineExternalId(targetDate, targetTime)
        )
        upsertScheduledStrengthRoutine(prefs, scheduledRoutine)
        return scheduledRoutine
    }

    suspend fun uploadSavedStrengthRoutine(
        scheduledRoutine: ScheduledStrengthRoutine,
    ): ScheduledStrengthRoutine {
        if (apiKey.isBlank()) return scheduledRoutine
        remoteDataSource.uploadStrengthRoutine(
            routine = scheduledRoutine.routine,
            date = scheduledRoutine.date,
            time = scheduledRoutine.time
        )
        val uploadedRoutine = scheduledRoutine.copy(uploadedToIntervals = true)
        upsertScheduledStrengthRoutine(prefs, uploadedRoutine)
        return uploadedRoutine
    }

    fun moveStrengthRoutineLocally(
        sourceRoutine: TrainingItem,
        targetDate: LocalDate,
    ): ScheduledStrengthRoutine? {
        return moveScheduledStrengthRoutine(prefs, sourceRoutine, targetDate)
    }

    suspend fun syncMovedRoutine(
        sourceRoutine: TrainingItem,
        targetDate: LocalDate,
        movedRoutine: ScheduledStrengthRoutine?,
    ) {
        if (apiKey.isBlank()) return
        if (movedRoutine != null) {
            uploadSavedStrengthRoutine(movedRoutine)
            if (sourceRoutine.isRemoteCalendarRoutine()) {
                remoteDataSource.deleteCalendarRoutine(sourceRoutine.remoteId)
                removeCalendarRoutineFromIntervalsCaches(prefs, apiKey, sourceRoutine)
            }
        } else {
            remoteDataSource.uploadCalendarRoutineCopy(sourceRoutine, targetDate)
            remoteDataSource.deleteCalendarRoutine(sourceRoutine.remoteId)
            removeCalendarRoutineFromIntervalsCaches(prefs, apiKey, sourceRoutine)
        }
    }

    fun deleteScopeFor(targetRoutine: TrainingItem): CalendarRoutineDeleteScope {
        return if (apiKey.isNotBlank() && !targetRoutine.id.startsWith(LOCAL_CALENDAR_ID_PREFIX)) {
            CalendarRoutineDeleteScope.REMOTE
        } else {
            CalendarRoutineDeleteScope.LOCAL
        }
    }

    suspend fun deleteRoutine(targetRoutine: TrainingItem): CalendarRoutineDeleteScope {
        val scope = deleteScopeFor(targetRoutine)
        when (scope) {
            CalendarRoutineDeleteScope.LOCAL -> deleteLocalRoutine(targetRoutine)
            CalendarRoutineDeleteScope.REMOTE -> deleteRemoteRoutine(targetRoutine)
        }
        return scope
    }

    private fun deleteLocalRoutine(targetRoutine: TrainingItem) {
        removeScheduledStrengthRoutine(prefs, targetRoutine)
    }

    private suspend fun deleteRemoteRoutine(targetRoutine: TrainingItem) {
        if (apiKey.isBlank()) return
        remoteDataSource.deleteCalendarRoutine(targetRoutine.remoteId)
        removeCalendarRoutineFromIntervalsCaches(prefs, apiKey, targetRoutine)
        removeScheduledStrengthRoutine(prefs, targetRoutine)
    }
}

private fun TrainingItem.isRemoteCalendarRoutine(): Boolean {
    return id.startsWith(REMOTE_CALENDAR_ID_PREFIX) && remoteId.isNotBlank()
}

private const val LOCAL_CALENDAR_ID_PREFIX = "local-"
private const val REMOTE_CALENDAR_ID_PREFIX = "routine-"

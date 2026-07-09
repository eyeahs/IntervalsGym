package com.lighthousepark.intervalsgym.data

import android.content.SharedPreferences

internal class IntervalsUseCaseFactory(
    private val apiKey: String,
) {
    private val repository = IntervalsRepository(apiKey)

    fun calendarRoutineSync(prefs: SharedPreferences): CalendarRoutineSyncUseCase {
        return CalendarRoutineSyncUseCase(
            prefs = prefs,
            apiKey = apiKey,
            remoteDataSource = IntervalsCalendarRoutineRemoteDataSource(repository)
        )
    }

    fun trainingCalendarData(prefs: SharedPreferences): TrainingCalendarDataUseCase {
        return TrainingCalendarDataUseCase(
            prefs = prefs,
            apiKey = apiKey,
            remoteDataSource = IntervalsTrainingCalendarRemoteDataSource(repository)
        )
    }

    fun strengthSessionSync(prefs: SharedPreferences): StrengthSessionSyncUseCase {
        return StrengthSessionSyncUseCase(
            prefs = prefs,
            remoteDataSource = IntervalsStrengthSessionRemoteDataSource(repository)
        )
    }

    fun runningSessionSync(prefs: SharedPreferences): RunningSessionSyncUseCase {
        return RunningSessionSyncUseCase(
            prefs = prefs,
            remoteDataSource = IntervalsRunningSessionRemoteDataSource(repository)
        )
    }
}

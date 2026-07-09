package com.lighthousepark.intervalsgym.data

import android.content.SharedPreferences
import com.lighthousepark.intervalsgym.running.CompletedRunningSession
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.ScheduledStrengthRoutine
import com.lighthousepark.intervalsgym.training.TrainingDateRange
import com.lighthousepark.intervalsgym.training.WeekTrainingData

internal data class TrainingCalendarLocalSnapshot(
    val strengthHistory: List<CompletedStrengthSession>,
    val runningHistory: List<CompletedRunningSession>,
    val scheduledStrengthRoutines: List<ScheduledStrengthRoutine>,
)

internal data class TrainingCalendarInitialLoad(
    val localSnapshot: TrainingCalendarLocalSnapshot,
    val data: WeekTrainingData,
    val cachedRemoteData: WeekTrainingData?,
    val shouldFetchRemote: Boolean,
)

internal interface TrainingCalendarRemoteDataSource {
    suspend fun loadWeek(range: TrainingDateRange): WeekTrainingData
}

internal class IntervalsTrainingCalendarRemoteDataSource(
    private val repository: IntervalsRepository,
) : TrainingCalendarRemoteDataSource {
    override suspend fun loadWeek(range: TrainingDateRange): WeekTrainingData {
        return repository.loadWeek(range.start, range.end)
    }
}

internal class TrainingCalendarDataUseCase(
    private val prefs: SharedPreferences,
    private val apiKey: String,
    private val remoteDataSource: TrainingCalendarRemoteDataSource,
) {
    fun loadLocalSnapshot(): TrainingCalendarLocalSnapshot {
        return TrainingCalendarLocalSnapshot(
            strengthHistory = loadCompletedStrengthSessionHistory(prefs),
            runningHistory = loadCompletedRunningSessionHistory(prefs),
            scheduledStrengthRoutines = loadScheduledStrengthRoutines(prefs)
        )
    }

    fun loadCachedRemoteWeek(range: TrainingDateRange): WeekTrainingData? {
        if (apiKey.isBlank()) return null
        return loadIntervalsWeekCache(prefs, apiKey, range.start, range.end)
    }

    fun initialLoad(
        range: TrainingDateRange,
        forceSync: Boolean,
    ): TrainingCalendarInitialLoad {
        val localSnapshot = loadLocalSnapshot()
        if (apiKey.isBlank()) {
            return TrainingCalendarInitialLoad(
                localSnapshot = localSnapshot,
                data = WeekTrainingData(
                    activities = emptyList(),
                    routines = emptyList()
                ),
                cachedRemoteData = null,
                shouldFetchRemote = false
            )
        }

        val cachedRemoteData = loadCachedRemoteWeek(range)
        val remoteData = cachedRemoteData
            ?: WeekTrainingData(activities = emptyList(), routines = emptyList())

        return TrainingCalendarInitialLoad(
            localSnapshot = localSnapshot,
            data = remoteData,
            cachedRemoteData = cachedRemoteData,
            shouldFetchRemote = cachedRemoteData == null || forceSync
        )
    }

    suspend fun fetchRemoteWeek(
        range: TrainingDateRange,
    ): WeekTrainingData {
        val remoteData = remoteDataSource.loadWeek(range)
        saveIntervalsWeekCache(prefs, apiKey, range.start, range.end, remoteData)
        return remoteData
    }
}

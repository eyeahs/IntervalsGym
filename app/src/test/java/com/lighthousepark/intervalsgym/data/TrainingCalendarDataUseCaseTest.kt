package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.training.TrainingDateRange
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.WeekTrainingData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingCalendarDataUseCaseTest {
    @Test
    fun trainingCalendarDataUseCase_loadsLocalSnapshotSeparatelyFromRemoteData() {
        val prefs = MemorySharedPreferences()
        val range = TrainingDateRange(LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 12))
        val startedAtMillis = LocalDateTime.of(2026, 7, 8, 7, 30)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        appendStrengthSessionHistory(
            prefs,
            completedStrengthSessionForStorage(
                id = "strength-local-calendar",
                routineName = "로컬 웨이트",
                startedAtMillis = startedAtMillis,
                endedAtMillis = startedAtMillis + 60_000L
            )
        )
        appendRunningSessionHistory(
            prefs,
            completedRunningSessionForStorage(
                id = "running-local-calendar",
                name = "로컬 러닝",
                startedAtMillis = startedAtMillis,
                endedAtMillis = startedAtMillis + 60_000L
            )
        )
        val useCase = TrainingCalendarDataUseCase(
            prefs = prefs,
            apiKey = "",
            remoteDataSource = RecordingTrainingCalendarRemoteDataSource()
        )

        val load = useCase.initialLoad(range, forceSync = false)

        assertFalse(load.shouldFetchRemote)
        assertEquals(null, load.cachedRemoteData)
        assertEquals(emptyList<TrainingItem>(), load.data.activities)
        assertEquals(listOf("strength-local-calendar"), load.localSnapshot.strengthHistory.map { it.id })
        assertEquals(listOf("running-local-calendar"), load.localSnapshot.runningHistory.map { it.id })
    }

    @Test
    fun trainingCalendarDataUseCase_usesCachedRemoteWeekUntilForced() {
        val prefs = MemorySharedPreferences()
        val range = TrainingDateRange(LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 12))
        val cachedData = WeekTrainingData(
            activities = listOf(trainingItem(id = "remote-activity", startedAt = range.start.atStartOfDay())),
            routines = listOf(trainingItem(id = "remote-routine", startedAt = range.start.atStartOfDay(), isRoutine = true))
        )
        saveIntervalsWeekCache(prefs, "api-key", range.start, range.end, cachedData)
        val useCase = TrainingCalendarDataUseCase(
            prefs = prefs,
            apiKey = "api-key",
            remoteDataSource = RecordingTrainingCalendarRemoteDataSource()
        )

        val cachedLoad = useCase.initialLoad(range, forceSync = false)
        val forcedLoad = useCase.initialLoad(range, forceSync = true)

        assertFalse(cachedLoad.shouldFetchRemote)
        assertEquals(listOf("remote-activity"), cachedLoad.data.activities.map { it.id })
        assertEquals(listOf("remote-routine"), cachedLoad.data.routines.map { it.id })
        assertTrue(forcedLoad.shouldFetchRemote)
        assertEquals(listOf("remote-activity"), forcedLoad.data.activities.map { it.id })
    }

    @Test
    fun trainingCalendarDataUseCase_fetchesAndCachesRemoteWeek() = runBlocking {
        val prefs = MemorySharedPreferences()
        val range = TrainingDateRange(LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 12))
        val remoteData = WeekTrainingData(
            activities = listOf(trainingItem(id = "remote-fetched", startedAt = range.start.atStartOfDay())),
            routines = emptyList()
        )
        val remote = RecordingTrainingCalendarRemoteDataSource(remoteData)
        val useCase = TrainingCalendarDataUseCase(
            prefs = prefs,
            apiKey = "api-key",
            remoteDataSource = remote
        )

        val fetched = useCase.fetchRemoteWeek(range)
        val cached = useCase.loadCachedRemoteWeek(range)

        assertEquals(listOf(range), remote.requests)
        assertEquals(listOf("remote-fetched"), fetched.activities.map { it.id })
        assertEquals(listOf("remote-fetched"), cached?.activities?.map { it.id })
    }
}

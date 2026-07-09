package com.lighthousepark.intervalsgym.training.ui

import com.lighthousepark.intervalsgym.data.TrainingCalendarInitialLoad
import com.lighthousepark.intervalsgym.data.TrainingCalendarLocalSnapshot
import com.lighthousepark.intervalsgym.training.TrainingDateRange
import com.lighthousepark.intervalsgym.training.WeekTrainingData
import com.lighthousepark.intervalsgym.training.trainingItem
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingCalendarUiStateTest {
    @Test
    fun initialLoadWithoutCacheShowsLoadingAndClearsRemoteData() {
        val range = trainingDateRange()
        val state = WeekUiState(
            weekStart = LocalDate.of(2026, 7, 1),
            weekEnd = LocalDate.of(2026, 7, 7),
            activities = listOf(trainingItem(id = "old")),
            routines = listOf(trainingItem(id = "old-routine")),
            error = "old"
        )

        val loaded = state.withTrainingCalendarInitialLoad(
            range = range,
            load = TrainingCalendarInitialLoad(
                localSnapshot = emptyLocalSnapshot(),
                data = WeekTrainingData(activities = emptyList(), routines = emptyList()),
                cachedRemoteData = null,
                shouldFetchRemote = true
            )
        )

        assertEquals(range.start, loaded.weekStart)
        assertEquals(range.end, loaded.weekEnd)
        assertTrue(loaded.isLoading)
        assertTrue(loaded.activities.isEmpty())
        assertTrue(loaded.routines.isEmpty())
        assertNull(loaded.error)
    }

    @Test
    fun initialLoadWithCacheKeepsCachedDataVisibleDuringBackgroundFetch() {
        val range = trainingDateRange()
        val cachedData = WeekTrainingData(
            activities = listOf(trainingItem(id = "cached-activity")),
            routines = listOf(trainingItem(id = "cached-routine"))
        )

        val loaded = WeekUiState(range.start, range.end).withTrainingCalendarInitialLoad(
            range = range,
            load = TrainingCalendarInitialLoad(
                localSnapshot = emptyLocalSnapshot(),
                data = cachedData,
                cachedRemoteData = cachedData,
                shouldFetchRemote = true
            )
        )

        assertFalse(loaded.isLoading)
        assertEquals(cachedData.activities, loaded.activities)
        assertEquals(cachedData.routines, loaded.routines)
    }

    @Test
    fun fetchedRemoteDataReplacesPageDataAndClearsError() {
        val range = trainingDateRange()
        val remoteData = WeekTrainingData(
            activities = listOf(trainingItem(id = "remote-activity")),
            routines = listOf(trainingItem(id = "remote-routine"))
        )

        val loaded = WeekUiState(
            weekStart = range.start,
            weekEnd = range.end,
            isLoading = true,
            error = "old"
        ).withFetchedRemoteData(range, remoteData)

        assertFalse(loaded.isLoading)
        assertNull(loaded.error)
        assertEquals(remoteData.activities, loaded.activities)
        assertEquals(remoteData.routines, loaded.routines)
    }

    @Test
    fun fetchFailureOnlyShowsErrorWhenThereIsNoCachedData() {
        val range = trainingDateRange()
        val cachedState = WeekUiState(
            weekStart = range.start,
            weekEnd = range.end,
            activities = listOf(trainingItem(id = "cached"))
        )
        val cachedData = WeekTrainingData(
            activities = cachedState.activities,
            routines = emptyList()
        )

        val withCacheFailure = cachedState.withRemoteFetchFailed(
            range = range,
            cachedRemoteData = cachedData,
            errorMessage = "network"
        )
        val withoutCacheFailure = cachedState.withRemoteFetchFailed(
            range = range,
            cachedRemoteData = null,
            errorMessage = null
        )

        assertEquals(cachedState, withCacheFailure)
        assertEquals("데이터를 불러오지 못했습니다.", withoutCacheFailure.error)
        assertFalse(withoutCacheFailure.isLoading)
    }

    @Test
    fun remotePageUiStateUsesLoadedRangeLoadingAndCacheState() {
        val range = trainingDateRange()
        val nextRange = TrainingDateRange(range.start.plusWeeks(1), range.end.plusWeeks(1))
        val loadingState = WeekUiState(
            weekStart = range.start,
            weekEnd = range.end,
            isLoading = true
        )
        val errorState = loadingState.copy(isLoading = false, error = "network")
        val cachedData = WeekTrainingData(
            activities = listOf(trainingItem(id = "cached")),
            routines = emptyList()
        )

        val loadedLoadingPage = loadingState.remotePageUiState(
            range = range,
            isRemoteConnected = true,
            cachedRemoteData = null
        )
        val unloadedCachedPage = loadingState.remotePageUiState(
            range = nextRange,
            isRemoteConnected = true,
            cachedRemoteData = cachedData
        )
        val unloadedNoCachePage = loadingState.remotePageUiState(
            range = nextRange,
            isRemoteConnected = true,
            cachedRemoteData = null
        )
        val errorPage = errorState.remotePageUiState(
            range = range,
            isRemoteConnected = true,
            cachedRemoteData = null
        )

        assertTrue(loadedLoadingPage.isLoadedPage)
        assertTrue(loadedLoadingPage.shouldShowRemoteLoading)
        assertFalse(unloadedCachedPage.shouldShowRemoteLoading)
        assertFalse(unloadedCachedPage.shouldShowRemoteError)
        assertTrue(unloadedNoCachePage.shouldShowRemoteLoading)
        assertTrue(errorPage.shouldShowRemoteError)
    }

    private fun trainingDateRange(): TrainingDateRange {
        return TrainingDateRange(
            start = LocalDate.of(2026, 7, 6),
            end = LocalDate.of(2026, 7, 12)
        )
    }

    private fun emptyLocalSnapshot(): TrainingCalendarLocalSnapshot {
        return TrainingCalendarLocalSnapshot(
            strengthHistory = emptyList(),
            runningHistory = emptyList(),
            scheduledStrengthRoutines = emptyList()
        )
    }
}

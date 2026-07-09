package com.lighthousepark.intervalsgym.training.ui

import com.lighthousepark.intervalsgym.data.TrainingCalendarInitialLoad
import com.lighthousepark.intervalsgym.training.TrainingDateRange
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.WeekTrainingData
import java.time.LocalDate

internal data class WeekUiState(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val isLoading: Boolean = false,
    val activities: List<TrainingItem> = emptyList(),
    val routines: List<TrainingItem> = emptyList(),
    val error: String? = null,
)

internal data class TrainingCalendarRemotePageUiState(
    val isLoadedPage: Boolean,
    val shouldShowRemoteLoading: Boolean,
    val shouldShowRemoteError: Boolean,
)

internal fun WeekUiState.isLoadedRange(range: TrainingDateRange): Boolean {
    return weekStart == range.start && weekEnd == range.end
}

internal fun WeekUiState.remotePageUiState(
    range: TrainingDateRange,
    isRemoteConnected: Boolean,
    cachedRemoteData: WeekTrainingData?,
): TrainingCalendarRemotePageUiState {
    val isLoadedPage = isLoadedRange(range)
    return TrainingCalendarRemotePageUiState(
        isLoadedPage = isLoadedPage,
        shouldShowRemoteLoading = isRemoteConnected && cachedRemoteData == null && (!isLoadedPage || isLoading),
        shouldShowRemoteError = isRemoteConnected && cachedRemoteData == null && error != null
    )
}

internal fun WeekUiState.withTrainingCalendarInitialLoad(
    range: TrainingDateRange,
    load: TrainingCalendarInitialLoad,
): WeekUiState {
    return copy(
        weekStart = range.start,
        weekEnd = range.end,
        activities = load.data.activities,
        routines = load.data.routines,
        isLoading = load.shouldFetchRemote && load.cachedRemoteData == null,
        error = null
    )
}

internal fun WeekUiState.withFetchedRemoteData(
    range: TrainingDateRange,
    data: WeekTrainingData,
): WeekUiState {
    return copy(
        weekStart = range.start,
        weekEnd = range.end,
        activities = data.activities,
        routines = data.routines,
        isLoading = false,
        error = null
    )
}

internal fun WeekUiState.withRemoteFetchFailed(
    range: TrainingDateRange,
    cachedRemoteData: WeekTrainingData?,
    errorMessage: String?,
): WeekUiState {
    if (cachedRemoteData != null) return this
    return copy(
        weekStart = range.start,
        weekEnd = range.end,
        isLoading = false,
        error = errorMessage ?: "데이터를 불러오지 못했습니다."
    )
}

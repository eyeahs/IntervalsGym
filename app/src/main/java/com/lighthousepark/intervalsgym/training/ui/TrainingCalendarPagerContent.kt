package com.lighthousepark.intervalsgym.training.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.data.TrainingCalendarDataUseCase
import com.lighthousepark.intervalsgym.data.TrainingCalendarLocalSnapshot
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.training.PendingCalendarRoutineMove
import com.lighthousepark.intervalsgym.training.TrainingCalendarMode
import com.lighthousepark.intervalsgym.training.TrainingDateRange
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.buildTrainingCalendarPageRenderData
import com.lighthousepark.intervalsgym.training.days
import com.lighthousepark.intervalsgym.training.rangeForPage
import com.lighthousepark.intervalsgym.workout.ui.ErrorView
import com.lighthousepark.intervalsgym.workout.ui.LoadingView
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TrainingCalendarPagerContent(
    pagerState: PagerState,
    baseDate: LocalDate,
    initialPage: Int,
    calendarMode: TrainingCalendarMode,
    isRemoteConnected: Boolean,
    weekUiState: WeekUiState,
    localSnapshot: TrainingCalendarLocalSnapshot,
    strengthRoutines: List<StrengthWorkoutRoutine>,
    deletedCalendarRoutineIds: Set<String>,
    optimisticallyDeletedCalendarRoutineKeys: Set<String>,
    pendingCalendarRoutineMoves: Collection<PendingCalendarRoutineMove>,
    calendarDragUiState: TrainingCalendarDragUiState,
    isCalendarRoutineDragging: Boolean,
    innerPadding: PaddingValues,
    calendarDataUseCase: TrainingCalendarDataUseCase,
    onCalendarDragUiStateChange: (TrainingCalendarDragUiState) -> Unit,
    onCalendarRoutineDragStarted: () -> Unit,
    onRefreshRange: (TrainingDateRange) -> Unit,
    onRoutineSelected: (TrainingItem) -> Unit,
    onIntervalStrengthRoutineSelected: (TrainingItem?, StrengthWorkoutRoutine) -> Unit,
    onMonthDaySelected: (LocalDate) -> Unit,
    onOpenRoutineSaveSheet: (LocalDate) -> Unit,
    onRoutineDateChanged: (TrainingItem, LocalDate) -> Unit,
    onRoutineDeleteRequested: (TrainingItem) -> Unit,
    onDragWeekShiftRequested: (Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .onGloballyPositioned { coordinates ->
                onCalendarDragUiStateChange(
                    calendarDragUiState.withContentLayout(
                        rootPosition = coordinates.positionInRoot(),
                        rootSize = coordinates.size
                    )
                )
            }
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = !isCalendarRoutineDragging,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val pageRange = calendarMode.rangeForPage(baseDate, (page - initialPage).toLong())
            val cachedPageData = if (!isRemoteConnected || weekUiState.isLoadedRange(pageRange)) {
                null
            } else {
                calendarDataUseCase.loadCachedRemoteWeek(pageRange)
            }
            val remotePageUiState = weekUiState.remotePageUiState(
                range = pageRange,
                isRemoteConnected = isRemoteConnected,
                cachedRemoteData = cachedPageData
            )
            val pageRenderData = buildTrainingCalendarPageRenderData(
                pageRange = pageRange,
                baseDate = baseDate,
                calendarMode = calendarMode,
                isRemoteConnected = isRemoteConnected,
                isLoadedPage = remotePageUiState.isLoadedPage,
                loadedActivities = weekUiState.activities,
                loadedRoutines = weekUiState.routines,
                cachedRemoteData = cachedPageData,
                localStrengthHistory = localSnapshot.strengthHistory,
                localRunningHistory = localSnapshot.runningHistory,
                localScheduledStrengthRoutines = localSnapshot.scheduledStrengthRoutines,
                strengthRoutines = strengthRoutines,
                deletedCalendarRoutineIds = deletedCalendarRoutineIds,
                optimisticallyDeletedCalendarRoutineKeys = optimisticallyDeletedCalendarRoutineKeys,
                pendingCalendarRoutineMoves = pendingCalendarRoutineMoves
            )

            Column(modifier = Modifier.fillMaxSize()) {
                if (calendarMode == TrainingCalendarMode.MONTH) {
                    WeekSummary(
                        activities = pageRenderData.activities,
                        routines = pageRenderData.routines,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
                when {
                    remotePageUiState.shouldShowRemoteLoading -> LoadingView()
                    remotePageUiState.shouldShowRemoteError -> ErrorView(
                        message = weekUiState.error.orEmpty(),
                        onRetry = { onRefreshRange(pageRange) }
                    )
                    calendarMode == TrainingCalendarMode.MONTH -> {
                        MonthlyTrainingCalendar(
                            range = pageRange,
                            items = pageRenderData.sortedItems,
                            onRoutineSelected = onRoutineSelected,
                            onIntervalStrengthRoutineSelected = onIntervalStrengthRoutineSelected,
                            onDaySelected = onMonthDaySelected
                        )
                    }
                    else -> {
                        TrainingList(
                            days = pageRange.days(),
                            items = pageRenderData.sortedItems,
                            emptyMessage = "주간 훈련 Routine 없음",
                            onRoutineSelected = onRoutineSelected,
                            onIntervalStrengthRoutineSelected = onIntervalStrengthRoutineSelected,
                            onDayHeaderClick = onOpenRoutineSaveSheet,
                            movableRoutineKeys = pageRenderData.movableScheduledRoutineKeys,
                            canMoveRemoteRoutines = isRemoteConnected,
                            onRoutineDateChanged = onRoutineDateChanged,
                            onRoutineDeleteRequested = onRoutineDeleteRequested,
                            onDragWeekShiftRequested = onDragWeekShiftRequested,
                            onDragDropTargetDateChanged = {
                                onCalendarDragUiStateChange(calendarDragUiState.withDropTargetDate(it))
                            },
                            onDragPointerRootPositionChanged = {
                                onCalendarDragUiStateChange(calendarDragUiState.withPointerRootPosition(it))
                            },
                            onDragOverlayChanged = {
                                onCalendarDragUiStateChange(calendarDragUiState.withOverlayState(it))
                            },
                            onDragStateChanged = { isDragging ->
                                if (isDragging) {
                                    onCalendarRoutineDragStarted()
                                }
                                onCalendarDragUiStateChange(calendarDragUiState.withDragging(isDragging))
                            },
                            externalDropTargetDate = calendarDragUiState.dropTargetDate,
                            externalDragPointerRootPosition = calendarDragUiState.pointerRootPosition,
                            shouldUpdateExternalDropTargetFromPointer =
                                isCalendarRoutineDragging && page == pagerState.currentPage,
                            externalDragActionBounds = calendarDragUiState.actionBounds,
                            dragViewportBounds = calendarDragUiState.viewportBounds,
                            renderLocalDragOverlay = false,
                            pendingApiMoveRoutineKeys = pageRenderData.pendingRoutineKeys,
                            initialScrollDate = pageRenderData.initialScrollDate,
                            header = {
                                WeekSummary(
                                    activities = pageRenderData.activities,
                                    routines = pageRenderData.routines,
                                    attachedToToolbar = true
                                )
                            }
                        )
                    }
                }
            }
        }
        CalendarRoutineExternalDragOverlayHost(
            dragUiState = calendarDragUiState,
            onActionPositioned = { action, bounds ->
                onCalendarDragUiStateChange(calendarDragUiState.withActionBounds(action, bounds))
            }
        )
    }
}

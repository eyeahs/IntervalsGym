package com.lighthousepark.intervalsgym.training.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import com.lighthousepark.intervalsgym.core.LocalizedText as Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.training.TrainingItem
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
internal fun TrainingCalendarScrollableDayList(
    listState: LazyListState,
    days: List<LocalDate>,
    groupedItems: Map<LocalDate, List<TrainingItem>>,
    emptyMessage: String,
    topContentPadding: Dp,
    showSingleDayEmptyMessage: Boolean,
    pendingApiMoveRoutineKeys: Set<String>,
    movableRoutineKeys: Set<String>,
    canMoveRemoteRoutines: Boolean,
    draggingRoutineId: String?,
    dropTargetDate: LocalDate?,
    externalDropTargetDate: LocalDate?,
    onDayHeaderClick: (LocalDate) -> Unit,
    onRoutineSelected: (TrainingItem) -> Unit,
    onIntervalStrengthRoutineSelected: (TrainingItem?, StrengthWorkoutRoutine) -> Unit,
    onRegisterDayDropBounds: (String, LocalDate, Rect) -> Unit,
    onRegisterDragTarget: (CalendarRoutineDragTarget) -> Unit,
    onRemoveDayDropBounds: (String) -> Unit,
    onRemoveDragTarget: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            start = 16.dp,
            top = topContentPadding,
            end = 16.dp,
            bottom = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (showSingleDayEmptyMessage) {
            item(key = "empty-training-list") {
                Text(
                    text = emptyMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        if (!showSingleDayEmptyMessage) {
            items(days, key = { day -> "day-section-$day" }) { day ->
                val dayItems = groupedItems[day].orEmpty()
                val isDropTarget = (draggingRoutineId != null && dropTargetDate == day) ||
                    externalDropTargetDate == day
                TrainingCalendarDaySection(
                    day = day,
                    dayItems = dayItems,
                    isDropTarget = isDropTarget,
                    pendingApiMoveRoutineKeys = pendingApiMoveRoutineKeys,
                    movableRoutineKeys = movableRoutineKeys,
                    canMoveRemoteRoutines = canMoveRemoteRoutines,
                    draggingRoutineId = draggingRoutineId,
                    onDayHeaderClick = onDayHeaderClick,
                    onRoutineSelected = onRoutineSelected,
                    onIntervalStrengthRoutineSelected = onIntervalStrengthRoutineSelected,
                    onRegisterDayDropBounds = onRegisterDayDropBounds,
                    onRegisterDragTarget = onRegisterDragTarget,
                    onRemoveDayDropBounds = onRemoveDayDropBounds,
                    onRemoveDragTarget = onRemoveDragTarget
                )
            }
        }
    }
}

@Composable
internal fun TrainingCalendarFloatingHeader(
    headerOffsetPx: Float,
    onHeaderHeightChanged: (Int) -> Unit,
    header: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f)
            .offset { IntOffset(x = 0, y = headerOffsetPx.roundToInt()) }
            .onSizeChanged { onHeaderHeightChanged(it.height) }
    ) {
        header()
    }
}

@Composable
internal fun BoxScope.TrainingCalendarLocalDragOverlayHost(
    renderLocalDragOverlay: Boolean,
    isDraggingCalendarRoutine: Boolean,
    activeDragAction: CalendarRoutineDragAction?,
    onDragActionPositioned: (CalendarRoutineDragAction, Rect) -> Unit,
    previewItem: TrainingItem?,
    previewRootPosition: Offset?,
    previewSize: IntSize,
    dragGrabOffset: Offset,
    dragPreviewScale: Float,
    listRootPosition: Offset,
) {
    AnimatedVisibility(
        visible = renderLocalDragOverlay && isDraggingCalendarRoutine,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = 24.dp)
            .zIndex(4f),
        enter = fadeIn(animationSpec = tween(120)),
        exit = fadeOut(animationSpec = tween(100))
    ) {
        CalendarRoutineDragActionButtons(
            activeAction = activeDragAction,
            onActionPositioned = onDragActionPositioned
        )
    }
    CalendarRoutineDragOverlay(
        overlay = if (renderLocalDragOverlay && previewItem != null && previewRootPosition != null) {
            CalendarRoutineDragOverlayState(
                item = previewItem,
                previewRootPosition = previewRootPosition,
                previewSize = previewSize,
                grabOffset = dragGrabOffset,
                scale = dragPreviewScale
            )
        } else {
            null
        },
        calendarContentRootPosition = listRootPosition,
        modifier = Modifier.zIndex(3f)
    )
}

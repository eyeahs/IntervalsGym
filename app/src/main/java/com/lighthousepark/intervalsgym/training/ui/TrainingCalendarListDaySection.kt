package com.lighthousepark.intervalsgym.training.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.data.strengthRoutineForDisplay
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.training.TrainingItem
import java.time.LocalDate

@Composable
internal fun TrainingCalendarDaySection(
    day: LocalDate,
    dayItems: List<TrainingItem>,
    isDropTarget: Boolean,
    pendingApiMoveRoutineKeys: Set<String>,
    movableRoutineKeys: Set<String>,
    canMoveRemoteRoutines: Boolean,
    draggingRoutineId: String?,
    onDayHeaderClick: (LocalDate) -> Unit,
    onRoutineSelected: (TrainingItem) -> Unit,
    onIntervalStrengthRoutineSelected: (TrainingItem?, StrengthWorkoutRoutine) -> Unit,
    onRegisterDayDropBounds: (String, LocalDate, Rect) -> Unit,
    onRegisterDragTarget: (CalendarRoutineDragTarget) -> Unit,
    onRemoveDayDropBounds: (String) -> Unit,
    onRemoveDragTarget: (String) -> Unit,
) {
    DisposableEffect(day) {
        onDispose {
            onRemoveDayDropBounds("section-$day")
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isDropTarget) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()
                onRegisterDayDropBounds(
                    "section-$day",
                    day,
                    Rect(
                        left = position.x,
                        top = position.y,
                        right = position.x + coordinates.size.width,
                        bottom = position.y + coordinates.size.height
                    )
                )
            }
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DayHeader(
            day = day,
            count = dayItems.size,
            onClick = { onDayHeaderClick(day) }
        )
        dayItems.forEach { item ->
            TrainingCalendarDayItemRow(
                item = item,
                pendingApiMoveRoutineKeys = pendingApiMoveRoutineKeys,
                movableRoutineKeys = movableRoutineKeys,
                canMoveRemoteRoutines = canMoveRemoteRoutines,
                draggingRoutineId = draggingRoutineId,
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

@Composable
private fun TrainingCalendarDayItemRow(
    item: TrainingItem,
    pendingApiMoveRoutineKeys: Set<String>,
    movableRoutineKeys: Set<String>,
    canMoveRemoteRoutines: Boolean,
    draggingRoutineId: String?,
    onRoutineSelected: (TrainingItem) -> Unit,
    onIntervalStrengthRoutineSelected: (TrainingItem?, StrengthWorkoutRoutine) -> Unit,
    onRegisterDayDropBounds: (String, LocalDate, Rect) -> Unit,
    onRegisterDragTarget: (CalendarRoutineDragTarget) -> Unit,
    onRemoveDayDropBounds: (String) -> Unit,
    onRemoveDragTarget: (String) -> Unit,
) {
    val itemDragState = item.trainingCalendarItemDragState(
        pendingApiMoveRoutineKeys = pendingApiMoveRoutineKeys,
        movableRoutineKeys = movableRoutineKeys,
        canMoveRemoteRoutines = canMoveRemoteRoutines,
        draggingRoutineId = draggingRoutineId
    )
    DisposableEffect(item.id) {
        onDispose {
            onRemoveDragTarget("row-${item.id}")
            onRemoveDayDropBounds("row-${item.id}")
        }
    }
    val dragModifier = Modifier
        .onGloballyPositioned { coordinates ->
            val position = coordinates.positionInRoot()
            val bounds = Rect(
                left = position.x,
                top = position.y,
                right = position.x + coordinates.size.width,
                bottom = position.y + coordinates.size.height
            )
            onRegisterDayDropBounds(
                "row-${item.id}",
                item.date,
                bounds
            )
            if (itemDragState.canDragRoutine) {
                onRegisterDragTarget(
                    CalendarRoutineDragTarget(
                        key = "row-${item.id}",
                        displayItem = item,
                        movableRoutine = itemDragState.movableRoutine,
                        bounds = bounds,
                        size = coordinates.size
                    )
                )
            }
        }
        .graphicsLayer {
            alpha = itemDragState.alpha
        }
    TrainingItemRow(
        item = item,
        isApiPendingMove = itemDragState.isApiPendingMove,
        modifier = dragModifier,
        onClick = {
            if (itemDragState.isApiPendingMove) return@TrainingItemRow
            val strengthRoutine = item.strengthRoutineForDisplay()
            if (item.isRoutine && strengthRoutine != null) {
                onIntervalStrengthRoutineSelected(item, strengthRoutine)
            } else {
                onRoutineSelected(item)
            }
        }
    )
}

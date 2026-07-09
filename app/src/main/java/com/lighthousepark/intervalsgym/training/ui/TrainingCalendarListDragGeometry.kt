package com.lighthousepark.intervalsgym.training.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import com.lighthousepark.intervalsgym.training.TrainingItem
import java.time.LocalDate

internal data class CalendarRoutineDragTarget(
    val key: String,
    val displayItem: TrainingItem,
    val movableRoutine: TrainingItem,
    val bounds: Rect,
    val size: IntSize,
)

internal fun calendarRoutineDragTargetAt(
    targets: Collection<CalendarRoutineDragTarget>,
    rootPosition: Offset,
): CalendarRoutineDragTarget? {
    return targets.lastOrNull { target ->
        target.bounds.containsInclusive(rootPosition)
    }
}

internal fun calendarRoutineDragActionAt(
    rootPosition: Offset,
    localActionBounds: Map<CalendarRoutineDragAction, Rect>,
    externalActionBounds: Map<CalendarRoutineDragAction, Rect>,
): CalendarRoutineDragAction? {
    return (localActionBounds + externalActionBounds).entries.lastOrNull { (_, bounds) ->
        bounds.containsInclusive(rootPosition)
    }?.key
}

internal fun calendarRoutineDropDateAt(
    rootPosition: Offset,
    dayDropBounds: Collection<Pair<LocalDate, Rect>>,
): LocalDate? {
    val boundsByDay = dayDropBounds.groupBy(
        keySelector = { (day, _) -> day },
        valueTransform = { (_, bounds) -> bounds }
    )
    return boundsByDay.entries
        .firstOrNull { (_, boundsList) ->
            boundsList.any { bounds -> bounds.containsInclusive(rootPosition) }
        }
        ?.key
        ?: boundsByDay.entries
            .minByOrNull { (_, boundsList) ->
                boundsList.minOf { bounds ->
                    when {
                        rootPosition.y < bounds.top -> bounds.top - rootPosition.y
                        rootPosition.y > bounds.bottom -> rootPosition.y - bounds.bottom
                        else -> 0f
                    }
                }
            }
            ?.key
}

internal fun calendarRoutineDragWeekShiftDirection(
    pointerXInViewport: Float,
    viewportWidth: Float,
    horizontalThreshold: Float,
): Int {
    return when {
        pointerXInViewport < horizontalThreshold -> -1
        viewportWidth - pointerXInViewport < horizontalThreshold -> 1
        else -> 0
    }
}

internal fun calendarRoutineAutoScrollDelta(
    pointerYInList: Float,
    listHeight: Int,
    topHotZone: Float,
    bottomHotZone: Float,
    canScrollBackward: Boolean,
    canScrollForward: Boolean,
): Float {
    val bottomDistance = listHeight - pointerYInList
    return when {
        topHotZone > 0f && pointerYInList < topHotZone && canScrollBackward -> {
            -((topHotZone - pointerYInList) / topHotZone * 34f).coerceIn(6f, 34f)
        }
        bottomHotZone > 0f && bottomDistance < bottomHotZone && canScrollForward -> {
            ((bottomHotZone - bottomDistance) / bottomHotZone * 34f).coerceIn(6f, 34f)
        }
        else -> 0f
    }
}

private fun Rect.containsInclusive(position: Offset): Boolean {
    return position.x in left..right && position.y in top..bottom
}

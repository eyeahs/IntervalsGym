package com.lighthousepark.intervalsgym.training.ui

import com.lighthousepark.intervalsgym.core.localizedContentDescription

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.lighthousepark.intervalsgym.training.TrainingItem
import java.time.LocalDate
import kotlin.math.roundToInt

internal data class CalendarRoutineDragOverlayState(
    val item: TrainingItem,
    val previewRootPosition: Offset,
    val previewSize: IntSize,
    val grabOffset: Offset,
    val scale: Float,
)

internal enum class CalendarRoutineDragAction {
    CANCEL,
    DELETE
}

internal data class TrainingCalendarDragUiState(
    val isDragging: Boolean = false,
    val dropTargetDate: LocalDate? = null,
    val pointerRootPosition: Offset? = null,
    val overlayState: CalendarRoutineDragOverlayState? = null,
    val actionBounds: Map<CalendarRoutineDragAction, Rect> = emptyMap(),
    val contentRootPosition: Offset = Offset.Zero,
    val contentRootSize: IntSize = IntSize.Zero,
) {
    val activeAction: CalendarRoutineDragAction?
        get() = pointerRootPosition?.let(::actionAt)

    val viewportBounds: Rect
        get() = Rect(
            left = contentRootPosition.x,
            top = contentRootPosition.y,
            right = contentRootPosition.x + contentRootSize.width,
            bottom = contentRootPosition.y + contentRootSize.height
        )

    fun withDragging(isDragging: Boolean): TrainingCalendarDragUiState {
        return if (isDragging) {
            copy(isDragging = true)
        } else {
            copy(
                isDragging = false,
                dropTargetDate = null,
                pointerRootPosition = null,
                overlayState = null,
                actionBounds = emptyMap()
            )
        }
    }

    fun withDropTargetDate(date: LocalDate?): TrainingCalendarDragUiState {
        return copy(dropTargetDate = date)
    }

    fun withPointerRootPosition(position: Offset?): TrainingCalendarDragUiState {
        return copy(pointerRootPosition = position)
    }

    fun withOverlayState(overlay: CalendarRoutineDragOverlayState?): TrainingCalendarDragUiState {
        return copy(overlayState = overlay)
    }

    fun withActionBounds(
        action: CalendarRoutineDragAction,
        bounds: Rect,
    ): TrainingCalendarDragUiState {
        return copy(actionBounds = actionBounds + (action to bounds))
    }

    fun withContentLayout(
        rootPosition: Offset,
        rootSize: IntSize,
    ): TrainingCalendarDragUiState {
        return copy(
            contentRootPosition = rootPosition,
            contentRootSize = rootSize
        )
    }

    private fun actionAt(rootPosition: Offset): CalendarRoutineDragAction? {
        return actionBounds.entries.lastOrNull { (_, bounds) ->
            rootPosition.x in bounds.left..bounds.right &&
                rootPosition.y in bounds.top..bounds.bottom
        }?.key
    }
}

@Composable
internal fun CalendarRoutineDragOverlay(
    overlay: CalendarRoutineDragOverlayState?,
    calendarContentRootPosition: Offset,
    modifier: Modifier = Modifier,
) {
    if (overlay == null || overlay.previewSize.width <= 0) return
    val density = LocalDensity.current
    TrainingItemRow(
        item = overlay.item,
        onClick = {},
        modifier = modifier
            .offset {
                IntOffset(
                    x = (overlay.previewRootPosition.x -
                        calendarContentRootPosition.x +
                        overlay.grabOffset.x * (1f - overlay.scale)).roundToInt(),
                    y = (overlay.previewRootPosition.y -
                        calendarContentRootPosition.y +
                        overlay.grabOffset.y * (1f - overlay.scale)).roundToInt()
                )
            }
            .width(with(density) { overlay.previewSize.width.toDp() })
            .graphicsLayer {
                shadowElevation = 18f
                scaleX = overlay.scale
                scaleY = overlay.scale
                transformOrigin = TransformOrigin(0f, 0f)
            }
    )
}

@Composable
internal fun CalendarRoutineDragActionButtons(
    activeAction: CalendarRoutineDragAction?,
    onActionPositioned: (CalendarRoutineDragAction, Rect) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CalendarRoutineDragActionButton(
            action = CalendarRoutineDragAction.CANCEL,
            active = activeAction == CalendarRoutineDragAction.CANCEL,
            icon = Icons.Outlined.Close,
            contentDescription = localizedContentDescription("이동 취소"),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            onPositioned = onActionPositioned
        )
        CalendarRoutineDragActionButton(
            action = CalendarRoutineDragAction.DELETE,
            active = activeAction == CalendarRoutineDragAction.DELETE,
            icon = Icons.Outlined.Delete,
            contentDescription = localizedContentDescription("Routine 삭제"),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            onPositioned = onActionPositioned
        )
    }
}

@Composable
internal fun BoxScope.CalendarRoutineExternalDragOverlayHost(
    dragUiState: TrainingCalendarDragUiState,
    onActionPositioned: (CalendarRoutineDragAction, Rect) -> Unit,
) {
    AnimatedVisibility(
        visible = dragUiState.isDragging,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = 24.dp)
            .zIndex(4f),
        enter = fadeIn(animationSpec = tween(120)),
        exit = fadeOut(animationSpec = tween(100))
    ) {
        CalendarRoutineDragActionButtons(
            activeAction = dragUiState.activeAction,
            onActionPositioned = onActionPositioned
        )
    }
    CalendarRoutineDragOverlay(
        overlay = dragUiState.overlayState,
        calendarContentRootPosition = dragUiState.contentRootPosition,
        modifier = Modifier.zIndex(5f)
    )
}

@Composable
private fun CalendarRoutineDragActionButton(
    action: CalendarRoutineDragAction,
    active: Boolean,
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    contentColor: Color,
    onPositioned: (CalendarRoutineDragAction, Rect) -> Unit,
) {
    FloatingActionButton(
        onClick = {},
        modifier = Modifier
            .size(56.dp)
            .graphicsLayer {
                scaleX = if (active) 1.12f else 1f
                scaleY = if (active) 1.12f else 1f
            }
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()
                onPositioned(
                    action,
                    Rect(
                        left = position.x,
                        top = position.y,
                        right = position.x + coordinates.size.width,
                        bottom = position.y + coordinates.size.height
                    )
                )
            },
        shape = RoundedCornerShape(999.dp),
        containerColor = if (active) contentColor else containerColor,
        contentColor = if (active) containerColor else contentColor
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(26.dp)
        )
    }
}

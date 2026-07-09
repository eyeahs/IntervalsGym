package com.lighthousepark.intervalsgym.training.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.workout.ui.EmptyView
import java.time.LocalDate
import kotlin.math.abs
import kotlinx.coroutines.delay

/**
 * Weekly training list with header collapse, day drop targets, and calendar routine drag handling.
 * Keep this render/gesture state outside the calendar route owner.
 */
@Composable
internal fun TrainingList(
    days: List<LocalDate>,
    items: List<TrainingItem>,
    emptyMessage: String,
    onRoutineSelected: (TrainingItem) -> Unit,
    onIntervalStrengthRoutineSelected: (TrainingItem?, StrengthWorkoutRoutine) -> Unit,
    onDayHeaderClick: (LocalDate) -> Unit = {},
    movableRoutineKeys: Set<String> = emptySet(),
    canMoveRemoteRoutines: Boolean = false,
    onRoutineDateChanged: (TrainingItem, LocalDate) -> Unit = { _, _ -> },
    onRoutineDeleteRequested: (TrainingItem) -> Unit = {},
    onDragWeekShiftRequested: (Int) -> Unit = {},
    onDragDropTargetDateChanged: (LocalDate?) -> Unit = {},
    onDragPointerRootPositionChanged: (Offset?) -> Unit = {},
    onDragOverlayChanged: (CalendarRoutineDragOverlayState?) -> Unit = {},
    onDragStateChanged: (Boolean) -> Unit = {},
    externalDropTargetDate: LocalDate? = null,
    externalDragPointerRootPosition: Offset? = null,
    shouldUpdateExternalDropTargetFromPointer: Boolean = false,
    externalDragActionBounds: Map<CalendarRoutineDragAction, Rect> = emptyMap(),
    dragViewportBounds: Rect? = null,
    renderLocalDragOverlay: Boolean = true,
    pendingApiMoveRoutineKeys: Set<String> = emptySet(),
    initialScrollDate: LocalDate? = null,
    header: (@Composable () -> Unit)? = null,
) {
    val grouped = items.groupBy { it.date }
    val shouldShowEmptyDays = days.size > 1

    if (items.isEmpty() && !shouldShowEmptyDays && header == null) {
        EmptyView(message = emptyMessage)
        return
    }

    val density = LocalDensity.current
    val currentOnRoutineDateChanged by rememberUpdatedState(onRoutineDateChanged)
    val currentOnRoutineDeleteRequested by rememberUpdatedState(onRoutineDeleteRequested)
    val currentOnDayHeaderClick by rememberUpdatedState(onDayHeaderClick)
    val currentOnDragWeekShiftRequested by rememberUpdatedState(onDragWeekShiftRequested)
    val currentOnDragDropTargetDateChanged by rememberUpdatedState(onDragDropTargetDateChanged)
    val currentOnDragPointerRootPositionChanged by rememberUpdatedState(onDragPointerRootPositionChanged)
    val currentOnDragOverlayChanged by rememberUpdatedState(onDragOverlayChanged)
    val currentOnDragStateChanged by rememberUpdatedState(onDragStateChanged)
    val listState = rememberLazyListState()
    var headerHeightPx by remember { mutableIntStateOf(0) }
    var headerOffsetPx by remember { mutableFloatStateOf(0f) }
    var didInitialScroll by remember(initialScrollDate, days) { mutableStateOf(false) }
    var listRootPosition by remember { mutableStateOf(Offset.Zero) }
    var listRootSize by remember { mutableStateOf(IntSize.Zero) }
    val dayDropBounds = remember(days, items) {
        mutableMapOf<String, Pair<LocalDate, Rect>>()
    }
    val dragTargets = remember(days, items, movableRoutineKeys, canMoveRemoteRoutines) {
        mutableMapOf<String, CalendarRoutineDragTarget>()
    }
    val dragActionBounds = remember {
        mutableMapOf<CalendarRoutineDragAction, Rect>()
    }
    var draggingRoutine by remember { mutableStateOf<TrainingItem?>(null) }
    var draggingDisplayItem by remember { mutableStateOf<TrainingItem?>(null) }
    var dropTargetDate by remember { mutableStateOf<LocalDate?>(null) }
    var dragGrabOffset by remember { mutableStateOf(Offset.Zero) }
    var dragPointerRootPosition by remember { mutableStateOf<Offset?>(null) }
    var dragPreviewRootPosition by remember { mutableStateOf<Offset?>(null) }
    var dragPreviewSize by remember { mutableStateOf(IntSize.Zero) }
    var dragPreviewTargetScale by remember { mutableFloatStateOf(1f) }
    var hasCalendarRoutineDragMoved by remember { mutableStateOf(false) }
    var dragWeekOffset by remember { mutableIntStateOf(0) }
    var lastDragWeekShiftAtMillis by remember { mutableStateOf(0L) }
    val dragPreviewScale by animateFloatAsState(
        targetValue = dragPreviewTargetScale,
        animationSpec = tween(durationMillis = 140),
        label = "calendarRoutineDragPreviewScale"
    )
    val isDraggingCalendarRoutine = draggingRoutine != null
    val visibleHeaderHeightDp = with(density) {
        (headerHeightPx + headerOffsetPx).coerceAtLeast(0f).toDp()
    }
    fun registerDayDropBounds(key: String, day: LocalDate, bounds: Rect) {
        dayDropBounds[key] = day to bounds
    }
    fun registerDragTarget(target: CalendarRoutineDragTarget) {
        dragTargets[target.key] = target
    }
    fun registerDragActionBounds(
        action: CalendarRoutineDragAction,
        bounds: Rect,
    ) {
        dragActionBounds[action] = bounds
    }
    fun resetCalendarRoutineDrag() {
        draggingRoutine = null
        draggingDisplayItem = null
        dropTargetDate = null
        dragPointerRootPosition = null
        dragPreviewRootPosition = null
        hasCalendarRoutineDragMoved = false
        dragWeekOffset = 0
        lastDragWeekShiftAtMillis = 0L
        dragActionBounds.clear()
        currentOnDragDropTargetDateChanged(null)
        currentOnDragPointerRootPositionChanged(null)
        currentOnDragOverlayChanged(null)
        currentOnDragStateChanged(false)
    }
    fun updateDragPointer(rootPosition: Offset?) {
        dragPointerRootPosition = rootPosition
        currentOnDragPointerRootPositionChanged(rootPosition)
    }
    fun updateDropTarget(rootPosition: Offset?) {
        val targetDate = rootPosition
            ?.let { calendarRoutineDropDateAt(it, dayDropBounds.values) }
            ?.plusWeeks(dragWeekOffset.toLong())
        dropTargetDate = targetDate
        currentOnDragDropTargetDateChanged(targetDate)
    }
    fun updateDragOverlay() {
        val previewItem = draggingDisplayItem
        val previewPosition = dragPreviewRootPosition
        currentOnDragOverlayChanged(
            if (previewItem != null && previewPosition != null && dragPreviewSize.width > 0) {
                CalendarRoutineDragOverlayState(
                    item = previewItem,
                    previewRootPosition = previewPosition,
                    previewSize = dragPreviewSize,
                    grabOffset = dragGrabOffset,
                    scale = dragPreviewScale
                )
            } else {
                null
            }
        )
    }
    val headerScrollConnection = rememberTrainingCalendarHeaderScrollConnection(
        headerEnabled = header != null,
        headerHeightPx = headerHeightPx,
        headerOffsetPx = headerOffsetPx,
        listState = listState,
        isDraggingCalendarRoutine = isDraggingCalendarRoutine,
        onHeaderOffsetChanged = { headerOffsetPx = it }
    )
    LaunchedEffect(headerHeightPx) {
        headerOffsetPx = coerceTrainingCalendarHeaderOffset(
            headerHeightPx = headerHeightPx,
            offsetPx = headerOffsetPx
        )
    }
    LaunchedEffect(listState.canScrollForward, listState.canScrollBackward, headerHeightPx) {
        headerOffsetPx = trainingCalendarHeaderOffsetAfterListScrollabilityChanged(
            currentOffsetPx = headerOffsetPx,
            canScrollForward = listState.canScrollForward,
            canScrollBackward = listState.canScrollBackward
        )
    }
    LaunchedEffect(initialScrollDate, days, headerHeightPx) {
        val targetDate = initialScrollDate ?: return@LaunchedEffect
        if (didInitialScroll || headerHeightPx == 0 || targetDate !in days) return@LaunchedEffect

        val targetIndex = days.indexOf(targetDate).coerceAtLeast(0)
        listState.scrollToItem(index = targetIndex)
        didInitialScroll = true
    }
    LaunchedEffect(
        externalDragPointerRootPosition,
        shouldUpdateExternalDropTargetFromPointer,
        days,
        items
    ) {
        val pointer = externalDragPointerRootPosition
        if (shouldUpdateExternalDropTargetFromPointer && pointer != null) {
            currentOnDragDropTargetDateChanged(calendarRoutineDropDateAt(pointer, dayDropBounds.values))
        }
    }
    LaunchedEffect(draggingRoutine) {
        while (draggingRoutine != null) {
            val pointer = dragPointerRootPosition
            if (pointer != null && listRootSize.height > 0) {
                val threshold = with(density) { 96.dp.toPx() }
                val horizontalThreshold = with(density) { 56.dp.toPx() }
                val viewportBounds = dragViewportBounds ?: Rect(
                    left = listRootPosition.x,
                    top = listRootPosition.y,
                    right = listRootPosition.x + listRootSize.width,
                    bottom = listRootPosition.y + listRootSize.height
                )
                val pointerXInViewport = pointer.x - viewportBounds.left
                val pointerYInList = pointer.y - listRootPosition.y
                val visibleHeaderHeightPx = (headerHeightPx + headerOffsetPx).coerceAtLeast(0f)
                val topHotZone = threshold + visibleHeaderHeightPx
                val horizontalDirection = calendarRoutineDragWeekShiftDirection(
                    pointerXInViewport = pointerXInViewport,
                    viewportWidth = viewportBounds.width,
                    horizontalThreshold = horizontalThreshold
                )
                if (
                    hasCalendarRoutineDragMoved &&
                    horizontalDirection != 0 &&
                    System.currentTimeMillis() - lastDragWeekShiftAtMillis > 650L
                ) {
                    dragWeekOffset += horizontalDirection
                    lastDragWeekShiftAtMillis = System.currentTimeMillis()
                    currentOnDragWeekShiftRequested(horizontalDirection)
                    updateDropTarget(pointer)
                }
                val scrollDelta = calendarRoutineAutoScrollDelta(
                    pointerYInList = pointerYInList,
                    listHeight = listRootSize.height,
                    topHotZone = topHotZone,
                    bottomHotZone = threshold,
                    canScrollBackward = listState.canScrollBackward,
                    canScrollForward = listState.canScrollForward
                )
                if (scrollDelta != 0f) {
                    listState.scrollBy(scrollDelta)
                    updateDropTarget(pointer)
                }
            }
            delay(16)
        }
    }
    LaunchedEffect(draggingDisplayItem?.id) {
        if (draggingDisplayItem == null) {
            dragPreviewTargetScale = 1f
        } else {
            dragPreviewTargetScale = 1f
            delay(16)
            dragPreviewTargetScale = 2f / 3f
        }
    }
    LaunchedEffect(
        draggingDisplayItem,
        dragPreviewRootPosition,
        dragPreviewSize,
        dragGrabOffset,
        dragPreviewScale
    ) {
        updateDragOverlay()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .onGloballyPositioned { coordinates ->
                listRootPosition = coordinates.positionInRoot()
                listRootSize = coordinates.size
            }
            .pointerInput(items, movableRoutineKeys, canMoveRemoteRoutines, listRootPosition) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val downRootPosition = listRootPosition + down.position
                    val target = calendarRoutineDragTargetAt(
                        targets = dragTargets.values,
                        rootPosition = downRootPosition
                    ) ?: return@awaitEachGesture
                    val longPress = awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture
                    val pointerRootPosition = listRootPosition + longPress.position
                    longPress.consume()

                    draggingRoutine = target.movableRoutine
                    draggingDisplayItem = target.displayItem
                    hasCalendarRoutineDragMoved = false
                    dragWeekOffset = 0
                    lastDragWeekShiftAtMillis = 0L
                    currentOnDragStateChanged(true)
                    dragGrabOffset = pointerRootPosition - Offset(target.bounds.left, target.bounds.top)
                    dragPreviewSize = target.size
                    updateDragPointer(pointerRootPosition)
                    dragPreviewRootPosition = pointerRootPosition - dragGrabOffset
                    updateDropTarget(pointerRootPosition)

                    val completed = drag(longPress.id) { change ->
                        val delta = change.positionChange()
                        if (abs(delta.x) + abs(delta.y) > 0.5f) {
                            hasCalendarRoutineDragMoved = true
                        }
                        change.consume()
                        val nextPointerRootPosition = listRootPosition + change.position
                        updateDragPointer(nextPointerRootPosition)
                        dragPreviewRootPosition = nextPointerRootPosition - dragGrabOffset
                        updateDropTarget(nextPointerRootPosition)
                    }
                    if (completed) {
                        val dragAction = dragPointerRootPosition?.let {
                            calendarRoutineDragActionAt(
                                rootPosition = it,
                                localActionBounds = dragActionBounds,
                                externalActionBounds = externalDragActionBounds
                            )
                        }
                        val targetDate = externalDropTargetDate ?: dropTargetDate
                        if (dragAction == CalendarRoutineDragAction.DELETE) {
                            currentOnRoutineDeleteRequested(target.movableRoutine)
                        } else if (dragAction != CalendarRoutineDragAction.CANCEL && targetDate != null && targetDate != target.movableRoutine.date) {
                            currentOnRoutineDateChanged(target.movableRoutine, targetDate)
                        }
                    }
                    resetCalendarRoutineDrag()
                }
            }
            .then(if (header != null) Modifier.nestedScroll(headerScrollConnection) else Modifier)
    ) {
        TrainingCalendarScrollableDayList(
            listState = listState,
            days = days,
            groupedItems = grouped,
            emptyMessage = emptyMessage,
            topContentPadding = if (header != null) visibleHeaderHeightDp + 14.dp else 16.dp,
            showSingleDayEmptyMessage = items.isEmpty() && !shouldShowEmptyDays,
            pendingApiMoveRoutineKeys = pendingApiMoveRoutineKeys,
            movableRoutineKeys = movableRoutineKeys,
            canMoveRemoteRoutines = canMoveRemoteRoutines,
            draggingRoutineId = draggingRoutine?.id,
            dropTargetDate = dropTargetDate,
            externalDropTargetDate = externalDropTargetDate,
            onDayHeaderClick = currentOnDayHeaderClick,
            onRoutineSelected = onRoutineSelected,
            onIntervalStrengthRoutineSelected = onIntervalStrengthRoutineSelected,
            onRegisterDayDropBounds = ::registerDayDropBounds,
            onRegisterDragTarget = ::registerDragTarget,
            onRemoveDayDropBounds = { key -> dayDropBounds.remove(key) },
            onRemoveDragTarget = { key -> dragTargets.remove(key) }
        )
        if (header != null) {
            TrainingCalendarFloatingHeader(
                headerOffsetPx = headerOffsetPx,
                onHeaderHeightChanged = { headerHeightPx = it },
                header = header
            )
        }
        val activeDragAction = dragPointerRootPosition?.let {
            calendarRoutineDragActionAt(
                rootPosition = it,
                localActionBounds = dragActionBounds,
                externalActionBounds = externalDragActionBounds
            )
        }
        TrainingCalendarLocalDragOverlayHost(
            renderLocalDragOverlay = renderLocalDragOverlay,
            isDraggingCalendarRoutine = draggingRoutine != null,
            activeDragAction = activeDragAction,
            onDragActionPositioned = ::registerDragActionBounds,
            previewItem = draggingDisplayItem,
            previewRootPosition = dragPreviewRootPosition,
            previewSize = dragPreviewSize,
            dragGrabOffset = dragGrabOffset,
            dragPreviewScale = dragPreviewScale,
            listRootPosition = listRootPosition
        )
    }
}

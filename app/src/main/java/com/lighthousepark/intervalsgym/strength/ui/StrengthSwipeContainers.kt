package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun PendingSwipeDeleteContainer(
    key: Any,
    enabled: Boolean,
    isPendingDelete: Boolean,
    modifier: Modifier = Modifier,
    onDeleteRequested: () -> Unit,
    onCommitDelete: () -> Unit,
    content: @Composable (Modifier, Boolean) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val viewConfiguration = LocalViewConfiguration.current
    val swipeOffsetX = remember(key) { Animatable(0f) }
    var rowWidth by remember(key) { mutableIntStateOf(0) }
    val deleteThreshold = with(density) { 92.dp.toPx() }
    val maxDragOffset = with(density) { 144.dp.toPx() }
    val touchSlop = viewConfiguration.touchSlop
    val swipeEnabled = enabled && !isPendingDelete

    LaunchedEffect(isPendingDelete, key) {
        if (isPendingDelete) {
            swipeOffsetX.snapTo(0f)
            delay(3_000)
            onCommitDelete()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (swipeEnabled) MaterialTheme.colorScheme.error.copy(alpha = 0.2f) else Color.Transparent)
            .onSizeChanged { rowWidth = it.width }
    ) {
        if (swipeEnabled) {
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .padding(end = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "삭제",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        val contentModifier = Modifier
            .fillMaxWidth()
            .padding(end = if (swipeEnabled) 8.dp else 0.dp)
            .offset { IntOffset(swipeOffsetX.value.roundToInt(), 0) }
            .then(
                if (swipeEnabled) {
                    Modifier.pointerInput(key, rowWidth, touchSlop) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val pointerId = down.id
                            var totalX = 0f
                            var totalY = 0f
                            var isHorizontalSwipe = false
                            var isCanceled = false

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                                if (change.changedToUpIgnoreConsumed()) break

                                val delta = change.positionChange()
                                if (delta.x == 0f && delta.y == 0f) continue
                                totalX += delta.x
                                totalY += delta.y

                                if (!isHorizontalSwipe) {
                                    val isVerticalIntent = abs(totalY) > touchSlop && abs(totalY) > abs(totalX)
                                    val isLeftSwipeIntent = totalX < -touchSlop && abs(totalX) > abs(totalY) * 1.2f
                                    if (isVerticalIntent) {
                                        isCanceled = true
                                        break
                                    }
                                    if (!isLeftSwipeIntent) continue
                                    isHorizontalSwipe = true
                                }

                                change.consume()
                                val nextOffset = (swipeOffsetX.value + delta.x).coerceIn(-maxDragOffset, 0f)
                                scope.launch {
                                    swipeOffsetX.snapTo(nextOffset)
                                }
                            }

                            if (isHorizontalSwipe && !isCanceled) {
                                scope.launch {
                                    if (swipeOffsetX.value <= -deleteThreshold) {
                                        swipeOffsetX.animateTo(
                                            targetValue = -rowWidth.toFloat().coerceAtLeast(maxDragOffset),
                                            animationSpec = tween(160)
                                        )
                                        onDeleteRequested()
                                    } else {
                                        swipeOffsetX.animateTo(0f, animationSpec = spring())
                                    }
                                }
                            } else if (swipeOffsetX.value != 0f) {
                                scope.launch {
                                    swipeOffsetX.animateTo(0f, animationSpec = spring())
                                }
                            }
                        }
                    }
                } else {
                    Modifier
                }
            )
        content(contentModifier, isPendingDelete)
    }
}

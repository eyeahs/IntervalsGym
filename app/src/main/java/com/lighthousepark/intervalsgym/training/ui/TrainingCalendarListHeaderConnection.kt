package com.lighthousepark.intervalsgym.training.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch

@Composable
internal fun rememberTrainingCalendarHeaderScrollConnection(
    headerEnabled: Boolean,
    headerHeightPx: Int,
    headerOffsetPx: Float,
    listState: LazyListState,
    isDraggingCalendarRoutine: Boolean,
    onHeaderOffsetChanged: (Float) -> Unit,
): NestedScrollConnection {
    val coroutineScope = rememberCoroutineScope()
    val currentHeaderOffsetPx = rememberUpdatedState(headerOffsetPx)
    val currentOnHeaderOffsetChanged = rememberUpdatedState(onHeaderOffsetChanged)
    return remember(headerEnabled, headerHeightPx, listState, isDraggingCalendarRoutine) {
        object : NestedScrollConnection {
            private suspend fun animateHeaderTo(targetOffset: Float) {
                val boundedTarget = coerceTrainingCalendarHeaderOffset(
                    headerHeightPx = headerHeightPx,
                    offsetPx = targetOffset
                )
                if (currentHeaderOffsetPx.value == boundedTarget) return

                Animatable(currentHeaderOffsetPx.value).animateTo(
                    targetValue = boundedTarget,
                    animationSpec = tween(durationMillis = 180)
                ) {
                    currentOnHeaderOffsetChanged.value(
                        coerceTrainingCalendarHeaderOffset(
                            headerHeightPx = headerHeightPx,
                            offsetPx = value
                        )
                    )
                }
                currentOnHeaderOffsetChanged.value(boundedTarget)
            }

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (isDraggingCalendarRoutine) {
                    return Offset.Zero
                }
                if (!headerEnabled || source != NestedScrollSource.UserInput || headerHeightPx == 0) {
                    return Offset.Zero
                }
                val result = trainingCalendarHeaderOffsetAfterScroll(
                    currentOffsetPx = currentHeaderOffsetPx.value,
                    headerHeightPx = headerHeightPx,
                    availableY = available.y,
                    canScrollForward = listState.canScrollForward
                )
                currentOnHeaderOffsetChanged.value(result.nextOffsetPx)

                return Offset(x = 0f, y = result.consumedY)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (isDraggingCalendarRoutine) {
                    return Velocity.Zero
                }
                if (!headerEnabled) return Velocity.Zero
                val targetOffset = trainingCalendarHeaderFlingTargetOffset(
                    headerHeightPx = headerHeightPx,
                    velocityY = available.y,
                    canScrollForward = listState.canScrollForward
                ) ?: return Velocity.Zero
                coroutineScope.launch {
                    animateHeaderTo(targetOffset)
                }
                return Velocity.Zero
            }
        }
    }
}

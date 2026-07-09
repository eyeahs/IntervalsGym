package com.lighthousepark.intervalsgym.training.ui

internal data class TrainingCalendarHeaderScrollResult(
    val nextOffsetPx: Float,
    val consumedY: Float,
)

internal fun coerceTrainingCalendarHeaderOffset(
    headerHeightPx: Int,
    offsetPx: Float,
): Float {
    if (headerHeightPx <= 0) return 0f
    return offsetPx.coerceIn(-headerHeightPx.toFloat(), 0f)
}

internal fun trainingCalendarHeaderOffsetAfterScroll(
    currentOffsetPx: Float,
    headerHeightPx: Int,
    availableY: Float,
    canScrollForward: Boolean,
): TrainingCalendarHeaderScrollResult {
    val boundedCurrentOffset = coerceTrainingCalendarHeaderOffset(
        headerHeightPx = headerHeightPx,
        offsetPx = currentOffsetPx
    )
    if (headerHeightPx <= 0 || availableY == 0f) {
        return TrainingCalendarHeaderScrollResult(
            nextOffsetPx = boundedCurrentOffset,
            consumedY = 0f
        )
    }
    val isScrollingUp = availableY < 0f
    if (isScrollingUp && !canScrollForward) {
        return TrainingCalendarHeaderScrollResult(
            nextOffsetPx = boundedCurrentOffset,
            consumedY = 0f
        )
    }

    val nextOffset = (boundedCurrentOffset + availableY).coerceIn(-headerHeightPx.toFloat(), 0f)
    return TrainingCalendarHeaderScrollResult(
        nextOffsetPx = nextOffset,
        consumedY = nextOffset - boundedCurrentOffset
    )
}

internal fun trainingCalendarHeaderFlingTargetOffset(
    headerHeightPx: Int,
    velocityY: Float,
    canScrollForward: Boolean,
): Float? {
    if (headerHeightPx <= 0 || velocityY == 0f) return null
    if (velocityY < 0f && !canScrollForward) return null
    return if (velocityY < 0f) {
        -headerHeightPx.toFloat()
    } else {
        0f
    }
}

internal fun trainingCalendarHeaderOffsetAfterListScrollabilityChanged(
    currentOffsetPx: Float,
    canScrollForward: Boolean,
    canScrollBackward: Boolean,
): Float {
    val canScroll = canScrollForward || canScrollBackward
    return if (!canScroll && currentOffsetPx < 0f) 0f else currentOffsetPx
}

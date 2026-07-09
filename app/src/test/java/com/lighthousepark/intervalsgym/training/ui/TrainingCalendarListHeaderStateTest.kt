package com.lighthousepark.intervalsgym.training.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrainingCalendarListHeaderStateTest {
    @Test
    fun headerOffsetAfterScrollCollapsesAndReportsConsumedDistance() {
        val result = trainingCalendarHeaderOffsetAfterScroll(
            currentOffsetPx = 0f,
            headerHeightPx = 120,
            availableY = -48f,
            canScrollForward = true
        )

        assertEquals(-48f, result.nextOffsetPx, FLOAT_TOLERANCE)
        assertEquals(-48f, result.consumedY, FLOAT_TOLERANCE)
    }

    @Test
    fun headerOffsetAfterScrollExpandsWithinBounds() {
        val result = trainingCalendarHeaderOffsetAfterScroll(
            currentOffsetPx = -90f,
            headerHeightPx = 120,
            availableY = 200f,
            canScrollForward = true
        )

        assertEquals(0f, result.nextOffsetPx, FLOAT_TOLERANCE)
        assertEquals(90f, result.consumedY, FLOAT_TOLERANCE)
    }

    @Test
    fun headerOffsetAfterScrollDoesNotCollapseAtListEnd() {
        val result = trainingCalendarHeaderOffsetAfterScroll(
            currentOffsetPx = -12f,
            headerHeightPx = 120,
            availableY = -32f,
            canScrollForward = false
        )

        assertEquals(-12f, result.nextOffsetPx, FLOAT_TOLERANCE)
        assertEquals(0f, result.consumedY, FLOAT_TOLERANCE)
    }

    @Test
    fun headerFlingTargetOffsetUsesVelocityDirection() {
        assertEquals(
            -120f,
            trainingCalendarHeaderFlingTargetOffset(
                headerHeightPx = 120,
                velocityY = -300f,
                canScrollForward = true
            ) ?: 0f,
            FLOAT_TOLERANCE
        )
        assertEquals(
            0f,
            trainingCalendarHeaderFlingTargetOffset(
                headerHeightPx = 120,
                velocityY = 300f,
                canScrollForward = true
            ) ?: -1f,
            FLOAT_TOLERANCE
        )
        assertNull(
            trainingCalendarHeaderFlingTargetOffset(
                headerHeightPx = 120,
                velocityY = -300f,
                canScrollForward = false
            )
        )
    }

    @Test
    fun headerOffsetResetsWhenListCannotScroll() {
        assertEquals(
            0f,
            trainingCalendarHeaderOffsetAfterListScrollabilityChanged(
                currentOffsetPx = -40f,
                canScrollForward = false,
                canScrollBackward = false
            ),
            FLOAT_TOLERANCE
        )
        assertEquals(
            -40f,
            trainingCalendarHeaderOffsetAfterListScrollabilityChanged(
                currentOffsetPx = -40f,
                canScrollForward = true,
                canScrollBackward = false
            ),
            FLOAT_TOLERANCE
        )
    }

    private companion object {
        const val FLOAT_TOLERANCE = 0.0001f
    }
}

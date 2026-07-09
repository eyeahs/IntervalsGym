package com.lighthousepark.intervalsgym.training.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import com.lighthousepark.intervalsgym.training.trainingItem
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrainingCalendarListDragGeometryTest {
    @Test
    fun dragTargetAtUsesLastContainingTarget() {
        val firstTarget = dragTarget(
            key = "first",
            bounds = Rect(left = 0f, top = 0f, right = 100f, bottom = 100f)
        )
        val lastTarget = dragTarget(
            key = "last",
            bounds = Rect(left = 0f, top = 0f, right = 100f, bottom = 100f)
        )

        assertEquals(
            lastTarget,
            calendarRoutineDragTargetAt(
                targets = listOf(firstTarget, lastTarget),
                rootPosition = Offset(50f, 50f)
            )
        )
        assertNull(
            calendarRoutineDragTargetAt(
                targets = listOf(firstTarget, lastTarget),
                rootPosition = Offset(150f, 50f)
            )
        )
    }

    @Test
    fun dragActionAtUsesExternalBoundsAfterLocalBounds() {
        val localBounds = linkedMapOf(
            CalendarRoutineDragAction.CANCEL to Rect(
                left = 0f,
                top = 0f,
                right = 100f,
                bottom = 100f
            )
        )
        val externalBounds = linkedMapOf(
            CalendarRoutineDragAction.DELETE to Rect(
                left = 0f,
                top = 0f,
                right = 100f,
                bottom = 100f
            )
        )

        assertEquals(
            CalendarRoutineDragAction.DELETE,
            calendarRoutineDragActionAt(
                rootPosition = Offset(50f, 50f),
                localActionBounds = localBounds,
                externalActionBounds = externalBounds
            )
        )
        assertNull(
            calendarRoutineDragActionAt(
                rootPosition = Offset(150f, 50f),
                localActionBounds = localBounds,
                externalActionBounds = externalBounds
            )
        )
    }

    @Test
    fun dropDateAtUsesContainingBoundsThenNearestVerticalDay() {
        val monday = LocalDate.of(2026, 7, 6)
        val tuesday = LocalDate.of(2026, 7, 7)
        val bounds = listOf(
            monday to Rect(left = 0f, top = 0f, right = 100f, bottom = 50f),
            tuesday to Rect(left = 0f, top = 100f, right = 100f, bottom = 150f)
        )

        assertEquals(
            tuesday,
            calendarRoutineDropDateAt(
                rootPosition = Offset(50f, 120f),
                dayDropBounds = bounds
            )
        )
        assertEquals(
            tuesday,
            calendarRoutineDropDateAt(
                rootPosition = Offset(200f, 80f),
                dayDropBounds = bounds
            )
        )
    }

    @Test
    fun dragWeekShiftDirectionUsesHorizontalHotZones() {
        assertEquals(
            -1,
            calendarRoutineDragWeekShiftDirection(
                pointerXInViewport = 20f,
                viewportWidth = 300f,
                horizontalThreshold = 56f
            )
        )
        assertEquals(
            1,
            calendarRoutineDragWeekShiftDirection(
                pointerXInViewport = 260f,
                viewportWidth = 300f,
                horizontalThreshold = 56f
            )
        )
        assertEquals(
            0,
            calendarRoutineDragWeekShiftDirection(
                pointerXInViewport = 150f,
                viewportWidth = 300f,
                horizontalThreshold = 56f
            )
        )
    }

    @Test
    fun autoScrollDeltaScalesNearTopAndBottomAndHonorsAvailability() {
        assertEquals(
            -17f,
            calendarRoutineAutoScrollDelta(
                pointerYInList = 48f,
                listHeight = 500,
                topHotZone = 96f,
                bottomHotZone = 96f,
                canScrollBackward = true,
                canScrollForward = true
            ),
            0.001f
        )
        assertEquals(
            17f,
            calendarRoutineAutoScrollDelta(
                pointerYInList = 452f,
                listHeight = 500,
                topHotZone = 96f,
                bottomHotZone = 96f,
                canScrollBackward = true,
                canScrollForward = true
            ),
            0.001f
        )
        assertEquals(
            0f,
            calendarRoutineAutoScrollDelta(
                pointerYInList = 48f,
                listHeight = 500,
                topHotZone = 96f,
                bottomHotZone = 96f,
                canScrollBackward = false,
                canScrollForward = true
            ),
            0.001f
        )
    }

    private fun dragTarget(key: String, bounds: Rect): CalendarRoutineDragTarget {
        val item = trainingItem(id = key)
        return CalendarRoutineDragTarget(
            key = key,
            displayItem = item,
            movableRoutine = item,
            bounds = bounds,
            size = IntSize(width = 100, height = 100)
        )
    }
}

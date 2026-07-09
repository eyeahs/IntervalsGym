package com.lighthousepark.intervalsgym.training.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingCalendarDragUiStateTest {
    @Test
    fun withDraggingFalseClearsTransientDragFieldsButKeepsLayout() {
        val state = TrainingCalendarDragUiState()
            .withContentLayout(rootPosition = Offset(10f, 20f), rootSize = IntSize(300, 400))
            .withDragging(true)
            .withDropTargetDate(LocalDate.of(2026, 7, 8))
            .withPointerRootPosition(Offset(30f, 40f))
            .withActionBounds(CalendarRoutineDragAction.DELETE, Rect(0f, 0f, 100f, 100f))
            .withOverlayState(overlay = null)

        val stopped = state.withDragging(false)

        assertEquals(false, stopped.isDragging)
        assertNull(stopped.dropTargetDate)
        assertNull(stopped.pointerRootPosition)
        assertNull(stopped.overlayState)
        assertTrue(stopped.actionBounds.isEmpty())
        assertEquals(Offset(10f, 20f), stopped.contentRootPosition)
        assertEquals(IntSize(300, 400), stopped.contentRootSize)
    }

    @Test
    fun activeActionUsesLastMatchingActionBounds() {
        val state = TrainingCalendarDragUiState()
            .withPointerRootPosition(Offset(50f, 50f))
            .withActionBounds(CalendarRoutineDragAction.CANCEL, Rect(0f, 0f, 100f, 100f))
            .withActionBounds(CalendarRoutineDragAction.DELETE, Rect(0f, 0f, 100f, 100f))

        assertEquals(CalendarRoutineDragAction.DELETE, state.activeAction)
    }

    @Test
    fun viewportBoundsAreDerivedFromContentLayout() {
        val state = TrainingCalendarDragUiState()
            .withContentLayout(rootPosition = Offset(10f, 20f), rootSize = IntSize(300, 400))

        assertEquals(Rect(10f, 20f, 310f, 420f), state.viewportBounds)
    }
}

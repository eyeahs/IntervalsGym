package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import org.junit.Assert.assertEquals
import org.junit.Test

class StrengthRoutineEntryDragActionsTest {
    @Test
    fun initialOverlayYUsesEntryPositionRelativeToRoot() {
        val entries = defaultStrengthRoutines().first().entries
        val layout = dragLayout(entries = entries, rootY = 20f)

        assertEquals(100f, layout.initialOverlayY(entries[1].id), 0.001f)
    }

    @Test
    fun clampedOverlayYKeepsDraggedEntryInsideKnownEntryBoundsAndRoot() {
        val entries = defaultStrengthRoutines().first().entries
        val layout = dragLayout(entries = entries, rootHeight = 250)

        assertEquals(0f, layout.clampedOverlayY(entries.first().id, -40f), 0.001f)
        assertEquals(150f, layout.clampedOverlayY(entries.first().id, 260f), 0.001f)
    }

    @Test
    fun draggingDownPastNextCenterMovesEntryDownOneSlot() {
        val entries = defaultStrengthRoutines().first().entries
        val layout = dragLayout(entries = entries)

        val update = layout.withDraggedEntryMoved(
            entryId = entries[0].id,
            previousOverlayY = 0f,
            targetOverlayY = 110f
        )

        assertEquals(listOf(entries[1].id, entries[0].id, entries[2].id), update.entries.map { it.id })
        assertEquals(110f, update.overlayY, 0.001f)
    }

    @Test
    fun draggingUpPastPreviousCenterMovesEntryUpOneSlot() {
        val entries = defaultStrengthRoutines().first().entries
        val layout = dragLayout(entries = entries)

        val update = layout.withDraggedEntryMoved(
            entryId = entries[1].id,
            previousOverlayY = 100f,
            targetOverlayY = -10f
        )

        assertEquals(listOf(entries[1].id, entries[0].id, entries[2].id), update.entries.map { it.id })
        assertEquals(0f, update.overlayY, 0.001f)
    }

    @Test
    fun dragUiStateKeepsGeometryOverlayAndEntriesTogether() {
        val entries = defaultStrengthRoutines().first().entries
        val started = dragUiState(entries).startDrag(
            entries = entries,
            entryId = entries[0].id
        )

        val update = started.moveDrag(
            entries = entries,
            entryId = entries[0].id,
            deltaY = 110f
        )
        val ended = update.state.endDrag()

        assertEquals(entries[0].id, started.draggingEntryId)
        assertEquals(0f, started.overlayY, 0.001f)
        assertEquals(listOf(entries[1].id, entries[0].id, entries[2].id), update.entries.map { it.id })
        assertEquals(110f, update.state.overlayY, 0.001f)
        assertEquals(null, ended.draggingEntryId)
        assertEquals(0f, ended.overlayY, 0.001f)
    }

    @Test
    fun dragUiStateIgnoresMoveFromNonDraggedEntry() {
        val entries = defaultStrengthRoutines().first().entries
        val started = dragUiState(entries).startDrag(
            entries = entries,
            entryId = entries[0].id
        )

        val update = started.moveDrag(
            entries = entries,
            entryId = entries[1].id,
            deltaY = 110f
        )

        assertEquals(started, update.state)
        assertEquals(entries, update.entries)
    }

    private fun dragLayout(
        entries: List<com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry>,
        rootY: Float = 0f,
        rootHeight: Int = 400,
    ): StrengthRoutineEntryDragLayout {
        return StrengthRoutineEntryDragLayout(
            entries = entries,
            entryHeights = entries.associate { it.id to 100 },
            entryRootYPositions = entries.mapIndexed { index, entry -> entry.id to rootY + index * 100f }.toMap(),
            rootY = rootY,
            rootHeight = rootHeight
        )
    }

    private fun dragUiState(
        entries: List<com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry>,
        rootY: Float = 0f,
        rootHeight: Int = 400,
    ): StrengthRoutineEntryDragUiState {
        return entries.fold(
            StrengthRoutineEntryDragUiState().withRootLayoutChanged(
                rootY = rootY,
                rootHeight = rootHeight
            )
        ) { state, entry ->
            val index = entries.indexOf(entry)
            state
                .withEntryHeightChanged(entry.id, 100)
                .withEntryRootYChanged(entry.id, rootY + index * 100f)
        }
    }
}

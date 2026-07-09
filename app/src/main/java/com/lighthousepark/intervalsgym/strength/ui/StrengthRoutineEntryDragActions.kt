package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.moveItem

internal data class StrengthRoutineEntryDragLayout(
    val entries: List<StrengthRoutineEntry>,
    val entryHeights: Map<Int, Int>,
    val entryRootYPositions: Map<Int, Float>,
    val rootY: Float,
    val rootHeight: Int,
) {
    fun initialOverlayY(entryId: Int): Float {
        return (entryRootYPositions[entryId] ?: rootY) - rootY
    }

    fun clampedOverlayY(entryId: Int, overlayY: Float): Float {
        val itemHeight = (entryHeights[entryId] ?: 0).toFloat()
        val (listTop, listBottom) = entryBounds() ?: return overlayY
        val minOverlayY = listTop.coerceAtLeast(0f)
        val maxOverlayY = (listBottom - itemHeight)
            .coerceAtLeast(minOverlayY)
            .coerceAtMost((rootHeight - itemHeight).coerceAtLeast(minOverlayY))
        return overlayY.coerceIn(minOverlayY, maxOverlayY)
    }

    fun withDraggedEntryMoved(
        entryId: Int,
        previousOverlayY: Float,
        targetOverlayY: Float,
    ): StrengthRoutineEntryDragUpdate {
        val nextOverlayY = clampedOverlayY(entryId, targetOverlayY)
        val consumedDeltaY = nextOverlayY - previousOverlayY
        if (consumedDeltaY == 0f) {
            return StrengthRoutineEntryDragUpdate(entries = entries, overlayY = nextOverlayY)
        }
        val currentIndex = entries.indexOfFirst { it.id == entryId }
        if (currentIndex < 0) {
            return StrengthRoutineEntryDragUpdate(entries = entries, overlayY = nextOverlayY)
        }
        val draggedHeight = (entryHeights[entryId] ?: 0).toFloat()
        val overlayCenterY = nextOverlayY + draggedHeight / 2f
        val movedEntries = when {
            consumedDeltaY > 0f && currentIndex < entries.lastIndex -> {
                val nextEntry = entries[currentIndex + 1]
                val nextCenterY = entryCenterY(nextEntry.id) ?: return StrengthRoutineEntryDragUpdate(entries, nextOverlayY)
                if (overlayCenterY >= nextCenterY) {
                    entries.moveItem(currentIndex, currentIndex + 1)
                } else {
                    entries
                }
            }
            consumedDeltaY < 0f && currentIndex > 0 -> {
                val previousEntry = entries[currentIndex - 1]
                val previousCenterY = entryCenterY(previousEntry.id)
                    ?: return StrengthRoutineEntryDragUpdate(entries, nextOverlayY)
                if (overlayCenterY <= previousCenterY) {
                    entries.moveItem(currentIndex, currentIndex - 1)
                } else {
                    entries
                }
            }
            else -> entries
        }
        return StrengthRoutineEntryDragUpdate(entries = movedEntries, overlayY = nextOverlayY)
    }

    private fun entryBounds(): Pair<Float, Float>? {
        val bounds = entries.mapNotNull { entry ->
            val top = entryRootYPositions[entry.id] ?: return@mapNotNull null
            val height = entryHeights[entry.id] ?: return@mapNotNull null
            val relativeTop = top - rootY
            relativeTop to relativeTop + height
        }
        val top = bounds.minOfOrNull { it.first } ?: return null
        val bottom = bounds.maxOfOrNull { it.second } ?: return null
        return top to bottom
    }

    private fun entryCenterY(entryId: Int): Float? {
        val top = entryRootYPositions[entryId] ?: return null
        val height = entryHeights[entryId] ?: return null
        return top - rootY + height / 2f
    }
}

internal data class StrengthRoutineEntryDragUpdate(
    val entries: List<StrengthRoutineEntry>,
    val overlayY: Float,
)

internal data class StrengthRoutineEntryDragUiState(
    val draggingEntryId: Int? = null,
    val overlayY: Float = 0f,
    val entryHeights: Map<Int, Int> = emptyMap(),
    val entryRootYPositions: Map<Int, Float> = emptyMap(),
    val rootY: Float = 0f,
    val rootHeight: Int = 0,
) {
    fun withRootLayoutChanged(rootY: Float, rootHeight: Int): StrengthRoutineEntryDragUiState {
        return copy(rootY = rootY, rootHeight = rootHeight)
    }

    fun withEntryHeightChanged(entryId: Int, height: Int): StrengthRoutineEntryDragUiState {
        return copy(entryHeights = entryHeights + (entryId to height))
    }

    fun withEntryRootYChanged(entryId: Int, rootY: Float): StrengthRoutineEntryDragUiState {
        return copy(entryRootYPositions = entryRootYPositions + (entryId to rootY))
    }

    fun startDrag(
        entries: List<StrengthRoutineEntry>,
        entryId: Int,
    ): StrengthRoutineEntryDragUiState {
        return copy(
            draggingEntryId = entryId,
            overlayY = dragLayout(entries).initialOverlayY(entryId)
        )
    }

    fun moveDrag(
        entries: List<StrengthRoutineEntry>,
        entryId: Int,
        deltaY: Float,
    ): StrengthRoutineEntryDragUiUpdate {
        if (draggingEntryId != entryId) {
            return StrengthRoutineEntryDragUiUpdate(
                state = this,
                entries = entries
            )
        }
        val update = dragLayout(entries).withDraggedEntryMoved(
            entryId = entryId,
            previousOverlayY = overlayY,
            targetOverlayY = overlayY + deltaY
        )
        return StrengthRoutineEntryDragUiUpdate(
            state = copy(overlayY = update.overlayY),
            entries = update.entries
        )
    }

    fun endDrag(): StrengthRoutineEntryDragUiState {
        return copy(
            draggingEntryId = null,
            overlayY = 0f
        )
    }

    fun clampedOverlayYOrNull(entries: List<StrengthRoutineEntry>): Float? {
        val entryId = draggingEntryId ?: return null
        return dragLayout(entries).clampedOverlayY(entryId, overlayY)
    }

    private fun dragLayout(entries: List<StrengthRoutineEntry>): StrengthRoutineEntryDragLayout {
        return StrengthRoutineEntryDragLayout(
            entries = entries,
            entryHeights = entryHeights,
            entryRootYPositions = entryRootYPositions,
            rootY = rootY,
            rootHeight = rootHeight
        )
    }
}

internal data class StrengthRoutineEntryDragUiUpdate(
    val state: StrengthRoutineEntryDragUiState,
    val entries: List<StrengthRoutineEntry>,
)

package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun StrengthRoutineEntryListEditor(
    routineName: String,
    entries: List<StrengthRoutineEntry>,
    supersetLabels: Map<Int, String>,
    pendingDeleteEntryIds: Set<Int>,
    isSupersetSelectionMode: Boolean,
    selectedSupersetEntryIds: Set<Int>,
    draggingEntryId: Int?,
    draggingOverlayY: Float?,
    canSave: Boolean,
    showDelete: Boolean,
    modifier: Modifier = Modifier,
    onRoutineNameChange: (String) -> Unit,
    onRootLayoutChanged: (rootY: Float, rootHeight: Int) -> Unit,
    onEntryHeightChanged: (entryId: Int, height: Int) -> Unit,
    onEntryRootYChanged: (entryId: Int, rootY: Float) -> Unit,
    onEntryDragStart: (entryId: Int) -> Unit,
    onEntryDrag: (entryId: Int, deltaY: Float) -> Unit,
    onEntryDragEnd: () -> Unit,
    onGroupSuperset: () -> Unit,
    onClearSelectedSupersetGroups: () -> Unit,
    onCancelSupersetSelection: () -> Unit,
    onStartSupersetSelection: () -> Unit,
    onAddExercise: () -> Unit,
    onSave: () -> Unit,
    onDeleteRoutine: () -> Unit,
    onEntryClick: (entryId: Int) -> Unit,
    onSupersetToggle: (entryId: Int) -> Unit,
    onEntryDeleteRequested: (entryId: Int) -> Unit,
    onEntryDeleteCommitted: (entryId: Int) -> Unit,
    onEntryDeleteRestored: (entryId: Int) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                onRootLayoutChanged(coordinates.positionInRoot().y, coordinates.size.height)
            }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 128.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = routineName,
                    onValueChange = onRoutineNameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .debugContentDescription(TestContentDescriptions.StrengthRoutineEditName),
                    label = { Text("Routine 이름") },
                    placeholder = { Text("새 웨이트 Routine") },
                    singleLine = true
                )
            }
            if (entries.isEmpty()) {
                item {
                    Text(
                        text = "운동을 추가해 Routine을 구성하세요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                if (isSupersetSelectionMode) {
                    item {
                        SupersetEditPanel(
                            isSelectionMode = isSupersetSelectionMode,
                            selectedCount = selectedSupersetEntryIds.size,
                            canClearSelectedGroups = entries.any {
                                it.id in selectedSupersetEntryIds && it.supersetGroupId != null
                            },
                            onGroupSelected = onGroupSuperset,
                            onClearSelectedGroups = onClearSelectedSupersetGroups,
                            onCancel = onCancelSupersetSelection
                        )
                    }
                }
                itemsIndexed(entries, key = { _, entry -> entry.id }) { _, entry ->
                    val isPendingDelete = entry.id in pendingDeleteEntryIds
                    val isDragging = draggingEntryId == entry.id
                    val reorderModifier = if (isSupersetSelectionMode || isPendingDelete) {
                        Modifier
                    } else {
                        Modifier.pointerInput(entry.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { onEntryDragStart(entry.id) },
                                onDragEnd = onEntryDragEnd,
                                onDragCancel = onEntryDragEnd
                            ) { change, dragAmount ->
                                change.consume()
                                onEntryDrag(entry.id, dragAmount.y)
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .animateItem()
                            .onSizeChanged { size -> onEntryHeightChanged(entry.id, size.height) }
                            .onGloballyPositioned { coordinates ->
                                onEntryRootYChanged(entry.id, coordinates.positionInRoot().y)
                            }
                            .then(reorderModifier)
                    ) {
                        StrengthRoutineExerciseRow(
                            entry = entry,
                            supersetLabel = entry.supersetGroupId?.let { supersetLabels[it] },
                            isSupersetSelectionMode = isSupersetSelectionMode,
                            isSupersetSelected = entry.id in selectedSupersetEntryIds,
                            isPendingDelete = isPendingDelete,
                            isDragging = false,
                            dragHandleModifier = Modifier,
                            modifier = Modifier.alpha(if (isDragging) 0f else 1f),
                            onClick = { onEntryClick(entry.id) },
                            onSupersetToggle = { onSupersetToggle(entry.id) },
                            onDelete = { onEntryDeleteRequested(entry.id) },
                            onCommitDelete = { onEntryDeleteCommitted(entry.id) },
                            onRestore = { onEntryDeleteRestored(entry.id) }
                        )
                    }
                }
            }
        }
        StrengthRoutineEditBottomBar(
            canGroupSuperset = entries.size >= 2 && !isSupersetSelectionMode,
            canSave = canSave,
            showDelete = showDelete,
            onGroupSuperset = onStartSupersetSelection,
            onAddExercise = onAddExercise,
            onSave = onSave,
            onDelete = onDeleteRoutine,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
        StrengthRoutineEntryDragOverlay(
            entry = draggingEntryId?.let { id -> entries.firstOrNull { it.id == id } },
            supersetLabels = supersetLabels,
            isSupersetSelectionMode = isSupersetSelectionMode,
            selectedSupersetEntryIds = selectedSupersetEntryIds,
            pendingDeleteEntryIds = pendingDeleteEntryIds,
            overlayY = draggingOverlayY
        )
    }
}

@Composable
private fun StrengthRoutineEntryDragOverlay(
    entry: StrengthRoutineEntry?,
    supersetLabels: Map<Int, String>,
    isSupersetSelectionMode: Boolean,
    selectedSupersetEntryIds: Set<Int>,
    pendingDeleteEntryIds: Set<Int>,
    overlayY: Float?,
) {
    if (entry == null || overlayY == null) return
    StrengthRoutineExerciseRow(
        entry = entry,
        supersetLabel = entry.supersetGroupId?.let { supersetLabels[it] },
        isSupersetSelectionMode = isSupersetSelectionMode,
        isSupersetSelected = entry.id in selectedSupersetEntryIds,
        isPendingDelete = entry.id in pendingDeleteEntryIds,
        isDragging = true,
        dragHandleModifier = Modifier,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .offset { IntOffset(0, overlayY.roundToInt()) }
            .zIndex(4f)
            .graphicsLayer {
                shadowElevation = 18f
                scaleX = 1.015f
                scaleY = 1.015f
            },
        onClick = {},
        onSupersetToggle = {},
        onDelete = {},
        onCommitDelete = {},
        onRestore = {}
    )
}

package com.lighthousepark.intervalsgym.strength.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import com.lighthousepark.intervalsgym.core.LocalizedText as Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.supersetGroupLabels
import kotlin.math.roundToInt

/**
 * Sub-screen of StrengthSessionScreen for the in-progress exercise list.
 * It coordinates exercise switching while set execution stays in StrengthSetExecutionScreen.
 * UI tests: StrengthSessionUiTest.ongoingRoutine_addExerciseButtonInvokesCallback,
 * ongoingRoutine_supersetSelectionGroupsRowsAndMovesSecondBelowTop.
 */
@Composable
internal fun StrengthSessionOngoingRoutineScreen(
    routine: StrengthWorkoutRoutine,
    entries: List<StrengthRoutineEntry>,
    currentExerciseIndex: Int,
    uploadMessage: String?,
    uploadError: String?,
    supersetSelectionUiState: StrengthSupersetSelectionUiState,
    modifier: Modifier = Modifier,
    onExerciseClick: (Int) -> Unit,
    onAddExercise: () -> Unit,
    onEntriesChange: (List<StrengthRoutineEntry>) -> Unit,
) {
    var displayEntries by remember { mutableStateOf(entries) }
    var entryDragUiState by remember { mutableStateOf(StrengthRoutineEntryDragUiState()) }
    val supersetLabels = remember(displayEntries) { displayEntries.supersetGroupLabels() }
    val currentEntryId = entries.getOrNull(currentExerciseIndex)?.id

    LaunchedEffect(entries) {
        if (entryDragUiState.draggingEntryId == null) {
            displayEntries = entries
        }
        supersetSelectionUiState.reconcile(entries)
    }

    BackHandler(enabled = supersetSelectionUiState.isSelectionMode) {
        supersetSelectionUiState.close()
    }

    fun startEntryDrag(entryId: Int) {
        if (supersetSelectionUiState.isSelectionMode) return
        displayEntries = entries
        entryDragUiState = entryDragUiState.startDrag(
            entries = entries,
            entryId = entryId
        )
    }

    fun updateEntryDrag(entryId: Int, deltaY: Float) {
        val update = entryDragUiState.moveDrag(
            entries = displayEntries,
            entryId = entryId,
            deltaY = deltaY
        )
        entryDragUiState = update.state
        displayEntries = update.entries
    }

    fun endEntryDrag() {
        onEntriesChange(displayEntries)
        entryDragUiState = entryDragUiState.endDrag()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                entryDragUiState = entryDragUiState.withRootLayoutChanged(
                    rootY = coordinates.positionInRoot().y,
                    rootHeight = coordinates.size.height
                )
            }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "진행 중 운동",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            itemsIndexed(displayEntries, key = { _, entry -> entry.id }) { _, entry ->
                val completedSets = entry.records.count { it.completed }
                val isComplete = entry.records.isNotEmpty() && completedSets == entry.records.size
                val isCurrent = entry.id == currentEntryId
                val isDragging = entryDragUiState.draggingEntryId == entry.id
                val isSupersetSelected = entry.id in supersetSelectionUiState.selectedEntryIds
                val supersetLabel = entry.supersetGroupId?.let { supersetLabels[it] }
                val reorderModifier = if (supersetSelectionUiState.isSelectionMode) {
                    Modifier
                } else {
                    Modifier.pointerInput(entry.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { startEntryDrag(entry.id) },
                            onDragEnd = ::endEntryDrag,
                            onDragCancel = ::endEntryDrag
                        ) { change, dragAmount ->
                            change.consume()
                            updateEntryDrag(entry.id, dragAmount.y)
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .animateItem()
                        .onSizeChanged { size ->
                            entryDragUiState = entryDragUiState.withEntryHeightChanged(entry.id, size.height)
                        }
                        .onGloballyPositioned { coordinates ->
                            entryDragUiState = entryDragUiState.withEntryRootYChanged(
                                entryId = entry.id,
                                rootY = coordinates.positionInRoot().y
                            )
                        }
                        .then(reorderModifier)
                ) {
                    StrengthOngoingExerciseRow(
                        entry = entry,
                        supersetLabel = supersetLabel,
                        completedSets = completedSets,
                        isComplete = isComplete,
                        isCurrent = isCurrent,
                        isSupersetSelectionMode = supersetSelectionUiState.isSelectionMode,
                        isSupersetSelected = isSupersetSelected,
                        isDragging = false,
                        dragHandleModifier = Modifier,
                        modifier = Modifier.alpha(if (isDragging) 0f else 1f),
                        onClick = {
                            if (supersetSelectionUiState.isSelectionMode) {
                                supersetSelectionUiState.toggle(entry, displayEntries)
                            } else {
                                entries.indexOfFirst { it.id == entry.id }
                                    .takeIf { it >= 0 }
                                    ?.let(onExerciseClick)
                            }
                        },
                    )
                }
            }
            if (!supersetSelectionUiState.isSelectionMode) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = supersetSelectionUiState::start,
                            enabled = displayEntries.size >= 2,
                            modifier = Modifier
                                .weight(1f)
                                .debugContentDescription(TestContentDescriptions.StrengthGroupSuperset),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("슈퍼세트", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(
                            onClick = onAddExercise,
                            modifier = Modifier
                                .weight(2f)
                                .debugContentDescription(TestContentDescriptions.StrengthAddExercise),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("신규 운동 추가", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
            if (uploadMessage != null || uploadError != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            uploadMessage?.let {
                                Text(it, color = MaterialTheme.colorScheme.primary)
                            }
                            uploadError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
        val draggingEntry = entryDragUiState.draggingEntryId?.let { id ->
            displayEntries.firstOrNull { it.id == id }
        }
        if (draggingEntry != null) {
            val overlayY = entryDragUiState.clampedOverlayYOrNull(displayEntries) ?: 0f
            val completedSets = draggingEntry.records.count { it.completed }
            val isComplete = draggingEntry.records.isNotEmpty() && completedSets == draggingEntry.records.size
            StrengthOngoingExerciseRow(
                entry = draggingEntry,
                supersetLabel = draggingEntry.supersetGroupId?.let { supersetLabels[it] },
                completedSets = completedSets,
                isComplete = isComplete,
                isCurrent = draggingEntry.id == currentEntryId,
                isSupersetSelectionMode = false,
                isSupersetSelected = false,
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
            )
        }
    }
}

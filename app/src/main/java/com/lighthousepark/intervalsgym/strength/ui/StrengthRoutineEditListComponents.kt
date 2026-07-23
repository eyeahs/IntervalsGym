package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import com.lighthousepark.intervalsgym.core.LocalizedText as Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.font.FontWeight
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun StrengthRoutineEntryListEditor(
    routineName: String,
    routineLocation: String,
    availableLocations: List<String>,
    entries: List<StrengthRoutineEntry>,
    supersetLabels: Map<Int, String>,
    pendingDeleteEntryIds: Set<Int>,
    isSupersetSelectionMode: Boolean,
    selectedSupersetEntryIds: Set<Int>,
    canGroupSelectedSuperset: Boolean,
    canClearSelectedSuperset: Boolean,
    draggingEntryId: Int?,
    draggingOverlayY: Float?,
    canSave: Boolean,
    showDelete: Boolean,
    modifier: Modifier = Modifier,
    onRoutineNameChange: (String) -> Unit,
    onRoutineLocationChange: (String) -> Unit,
    onAddRoutineLocation: (String) -> Unit,
    onRemoveRoutineLocation: (String) -> Unit,
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
            item {
                StrengthRoutineLocationEditor(
                    location = routineLocation,
                    availableLocations = availableLocations,
                    onLocationChange = onRoutineLocationChange,
                    onAddLocation = onAddRoutineLocation,
                    onRemoveLocation = onRemoveRoutineLocation
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
        if (isSupersetSelectionMode) {
            StrengthSupersetSelectionBottomBar(
                canGroup = canGroupSelectedSuperset,
                canClear = canClearSelectedSuperset,
                onGroup = onGroupSuperset,
                onClear = onClearSelectedSupersetGroups,
                onCancel = onCancelSupersetSelection,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
        } else {
            StrengthRoutineEditBottomBar(
                canGroupSuperset = entries.size >= 2,
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
        }
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
internal fun StrengthRoutineLocationEditor(
    location: String,
    availableLocations: List<String>,
    onLocationChange: (String) -> Unit,
    onAddLocation: (String) -> Unit,
    onRemoveLocation: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isLocationPickerVisible by rememberSaveable { mutableStateOf(false) }
    var isAddLocationDialogVisible by rememberSaveable { mutableStateOf(false) }
    var newLocationName by rememberSaveable { mutableStateOf("") }

    OutlinedButton(
        onClick = { isLocationPickerVisible = true },
        modifier = modifier
            .fillMaxWidth()
            .debugContentDescription(TestContentDescriptions.StrengthRoutineEditLocation)
    ) {
        Text(
            text = location.trim().takeIf { it.isNotEmpty() }
                ?.let { "장소 · $it" }
                ?: "장소 · 미지정"
        )
    }

    if (isLocationPickerVisible) {
        AlertDialog(
            onDismissRequest = { isLocationPickerVisible = false },
            title = { Text("장소 선택") },
            text = {
                Column(
                    modifier = Modifier.debugContentDescription(
                        TestContentDescriptions.StrengthRoutineEditLocationPicker
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp)
                    ) {
                        item {
                            Text(
                                text = "장소 미지정",
                                color = if (location.isBlank()) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                fontWeight = if (location.isBlank()) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onLocationChange("")
                                        isLocationPickerVisible = false
                                    }
                                    .padding(vertical = 14.dp)
                            )
                        }
                        items(availableLocations, key = { savedLocation -> savedLocation.lowercase() }) { savedLocation ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onLocationChange(savedLocation)
                                        isLocationPickerVisible = false
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = savedLocation,
                                    color = if (location.equals(savedLocation, ignoreCase = true)) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    fontWeight = if (location.equals(savedLocation, ignoreCase = true)) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 10.dp)
                                )
                                TextButton(
                                    onClick = { onRemoveLocation(savedLocation) },
                                    modifier = Modifier.debugContentDescription(
                                        TestContentDescriptions.strengthRoutineEditRemoveLocation(savedLocation)
                                    )
                                ) {
                                    Text("제거")
                                }
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            isLocationPickerVisible = false
                            isAddLocationDialogVisible = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .debugContentDescription(
                                TestContentDescriptions.StrengthRoutineEditAddLocation
                            )
                    ) {
                        Text("새 장소 추가")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isLocationPickerVisible = false }) {
                    Text("닫기")
                }
            }
        )
    }

    if (isAddLocationDialogVisible) {
        AlertDialog(
            onDismissRequest = { isAddLocationDialogVisible = false },
            title = { Text("새 장소 추가") },
            text = {
                OutlinedTextField(
                    value = newLocationName,
                    onValueChange = { newLocationName = it },
                    label = { Text("장소 이름") },
                    placeholder = { Text("예: 회사 근처 헬스장") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .debugContentDescription(
                            TestContentDescriptions.StrengthRoutineEditLocationName
                        )
                )
            },
            confirmButton = {
                TextButton(
                    enabled = newLocationName.isNotBlank(),
                    onClick = {
                        onAddLocation(newLocationName)
                        newLocationName = ""
                        isAddLocationDialogVisible = false
                    },
                    modifier = Modifier.debugContentDescription(
                        TestContentDescriptions.StrengthRoutineEditConfirmLocation
                    )
                ) {
                    Text("추가")
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddLocationDialogVisible = false }) {
                    Text("취소")
                }
            }
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

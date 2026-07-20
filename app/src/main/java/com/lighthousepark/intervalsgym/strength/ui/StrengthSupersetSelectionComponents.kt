package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthSetGroupType
import com.lighthousepark.intervalsgym.ui.theme.AppHighlight
import com.lighthousepark.intervalsgym.ui.theme.AppHighlightContainer

internal class StrengthSupersetSelectionUiState {
    var isSelectionMode by mutableStateOf(false)
        private set

    var selectedEntryIds by mutableStateOf(emptySet<Int>())
        private set

    var selectedSupersetGroupId by mutableStateOf<Int?>(null)
        private set

    fun start() {
        isSelectionMode = true
        selectedEntryIds = emptySet()
        selectedSupersetGroupId = null
    }

    fun toggle(
        entry: StrengthRoutineEntry,
        entries: List<StrengthRoutineEntry>,
    ) {
        val groupId = entry.supersetGroupId
        if (groupId == null) {
            selectedEntryIds = if (entry.id in selectedEntryIds) {
                selectedEntryIds - entry.id
            } else {
                selectedEntryIds + entry.id
            }
            return
        }

        val previousGroupEntryIds = selectedSupersetGroupId?.let { selectedGroupId ->
            entries.filter { it.supersetGroupId == selectedGroupId }.map { it.id }.toSet()
        }.orEmpty()
        if (selectedSupersetGroupId == groupId) {
            selectedSupersetGroupId = null
            selectedEntryIds -= previousGroupEntryIds
        } else {
            val nextGroupEntryIds = entries
                .filter { it.supersetGroupId == groupId }
                .map { it.id }
                .toSet()
            selectedSupersetGroupId = groupId
            selectedEntryIds = (selectedEntryIds - previousGroupEntryIds) + nextGroupEntryIds
        }
    }

    fun reconcile(entries: List<StrengthRoutineEntry>) {
        selectedEntryIds = selectedEntryIds.intersect(entries.map { it.id }.toSet())
        val selectedGroupId = selectedSupersetGroupId ?: return
        val selectedGroupEntryIds = entries
            .filter { it.supersetGroupId == selectedGroupId }
            .map { it.id }
            .toSet()
        if (selectedGroupEntryIds.size < 2) {
            selectedSupersetGroupId = null
        } else {
            selectedEntryIds += selectedGroupEntryIds
        }
    }

    fun canGroup(entries: List<StrengthRoutineEntry>): Boolean {
        val selectedLooseEntryCount = entries.count { entry ->
            entry.id in selectedEntryIds && entry.supersetGroupId == null
        }
        return if (selectedSupersetGroupId == null) {
            selectedLooseEntryCount >= 2
        } else {
            true
        }
    }

    fun canClear(entries: List<StrengthRoutineEntry>): Boolean {
        val selectedGroupId = selectedSupersetGroupId ?: return false
        return entries.count { it.supersetGroupId == selectedGroupId } >= 2
    }

    fun groupedEntries(
        entries: List<StrengthRoutineEntry>,
        setGroupType: StrengthSetGroupType = StrengthSetGroupType.SUPERSET,
    ): List<StrengthRoutineEntry>? {
        val selectedGroupId = selectedSupersetGroupId
        val nextEntries = if (selectedGroupId == null) {
            entries.withSelectedEntriesGroupedAsSuperset(selectedEntryIds, setGroupType)
        } else {
            entries.withSelectedEntriesAddedToSupersetGroup(
                selectedEntryIds = selectedEntryIds,
                supersetGroupId = selectedGroupId,
                setGroupType = setGroupType
            )
        }
        val changedEntries = nextEntries.takeUnless { it == entries }
        close()
        return changedEntries
    }

    fun clearedEntries(entries: List<StrengthRoutineEntry>): List<StrengthRoutineEntry>? {
        val nextEntries = entries.withSelectedSupersetGroupsCleared(selectedEntryIds)
        if (nextEntries == entries) return null
        close()
        return nextEntries
    }

    fun close() {
        isSelectionMode = false
        selectedEntryIds = emptySet()
        selectedSupersetGroupId = null
    }
}

@Composable
internal fun rememberStrengthSupersetSelectionUiState(
    routineId: Int?,
): StrengthSupersetSelectionUiState {
    return remember(routineId) { StrengthSupersetSelectionUiState() }
}

internal fun strengthSupersetSelectionContainerColor(
    isSelected: Boolean,
    defaultColor: Color,
): Color = if (isSelected) AppHighlightContainer else defaultColor

@Composable
internal fun StrengthSupersetSelectionMarker(
    entryId: Int,
    supersetLabel: String?,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(if (supersetLabel == null) 24.dp else 48.dp)
            .height(48.dp),
        contentAlignment = Alignment.Center
    ) {
        if (supersetLabel == null) {
            Icon(
                imageVector = if (isSelected) {
                    Icons.Outlined.CheckCircle
                } else {
                    Icons.Outlined.RadioButtonUnchecked
                },
                contentDescription = if (isSelected) "선택됨" else "선택",
                tint = if (isSelected) AppHighlight else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(24.dp)
                    .debugContentDescription(
                        TestContentDescriptions.strengthSupersetEntryToggle(entryId)
                    )
            )
        } else {
            Text(
                text = supersetLabel.substringAfterLast(' '),
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) AppHighlight else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.debugContentDescription(
                    TestContentDescriptions.strengthSupersetEntryLabel(entryId)
                )
            )
        }
    }
}

@Composable
internal fun StrengthSupersetSelectionBottomBar(
    canGroup: Boolean,
    canClear: Boolean,
    onGroup: () -> Unit,
    onClear: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onGroup,
                enabled = canGroup,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .debugContentDescription(TestContentDescriptions.StrengthConfirmSuperset),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("선택 묶기", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(
                onClick = onClear,
                enabled = canClear,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .debugContentDescription(TestContentDescriptions.StrengthClearSuperset),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("묶기 해제", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            TextButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(0.72f)
                    .height(52.dp)
                    .debugContentDescription(TestContentDescriptions.StrengthCancelSuperset),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("취소", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

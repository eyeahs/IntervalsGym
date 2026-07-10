package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.core.throttleRapidTaps
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry

/**
 * UI tests: StrengthRoutineEditUiTest.editBottomBar_exposesAllPrimaryActions.
 */
@Composable
internal fun StrengthRoutineEditBottomBar(
    canGroupSuperset: Boolean,
    canSave: Boolean,
    showDelete: Boolean,
    onGroupSuperset: () -> Unit,
    onAddExercise: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onGroupSuperset,
                    enabled = canGroupSuperset,
                    modifier = Modifier
                        .weight(1f)
                        .debugContentDescription(TestContentDescriptions.StrengthRoutineEditGroupSuperset),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("슈퍼세트 묶기", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(
                    onClick = onAddExercise,
                    modifier = Modifier
                        .weight(1f)
                        .throttleRapidTaps()
                        .debugContentDescription(TestContentDescriptions.StrengthRoutineEditAddExercise),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("신규 운동 추가", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSave,
                    enabled = canSave,
                    modifier = Modifier
                        .weight(1f)
                        .throttleRapidTaps()
                        .debugContentDescription(TestContentDescriptions.StrengthRoutineEditSave),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Routine 저장", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (showDelete) {
                    Button(
                        onClick = onDelete,
                        modifier = Modifier
                            .weight(1f)
                            .throttleRapidTaps()
                            .debugContentDescription(TestContentDescriptions.StrengthRoutineEditDelete),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Routine 삭제", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * UI tests: StrengthRoutineEditUiTest.exerciseRow_clicksNormalCallback,
 * exerciseRow_clicksSupersetSelectionCallback, exerciseRow_pendingDeleteRestoresFromButtonAndRowClick.
 */
@Composable
internal fun StrengthRoutineExerciseRow(
    entry: StrengthRoutineEntry,
    supersetLabel: String?,
    isSupersetSelectionMode: Boolean,
    isSupersetSelected: Boolean,
    isPendingDelete: Boolean,
    isDragging: Boolean,
    dragHandleModifier: Modifier,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onSupersetToggle: () -> Unit,
    onDelete: () -> Unit,
    onCommitDelete: () -> Unit,
    onRestore: () -> Unit,
) {
    val swipeDeleteEnabled = !isSupersetSelectionMode && !isPendingDelete && !isDragging

    PendingSwipeDeleteContainer(
        key = entry.id,
        enabled = swipeDeleteEnabled,
        isPendingDelete = isPendingDelete,
        modifier = modifier.debugContentDescription(TestContentDescriptions.strengthRoutineExerciseRow(entry.id)),
        onDeleteRequested = onDelete,
        onCommitDelete = onCommitDelete
    ) { swipeModifier, _ ->
        Card(
            modifier = swipeModifier
                .throttleRapidTaps(enabled = !isSupersetSelectionMode && !isPendingDelete)
                .clickable(
                    onClick = when {
                        isPendingDelete -> onRestore
                        isSupersetSelectionMode -> onSupersetToggle
                        else -> onClick
                    }
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isPendingDelete -> MaterialTheme.colorScheme.surfaceVariant
                    isDragging -> MaterialTheme.colorScheme.primaryContainer
                    else -> strengthSupersetSelectionContainerColor(
                        isSelected = isSupersetSelected,
                        defaultColor = MaterialTheme.colorScheme.surface
                    )
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    isPendingDelete -> Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    isSupersetSelectionMode -> StrengthSupersetSelectionMarker(
                        entryId = entry.id,
                        supersetLabel = supersetLabel,
                        isSelected = isSupersetSelected
                    )

                    else -> Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(40.dp)
                            .then(dragHandleModifier),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.DragIndicator,
                            contentDescription = "드래그해서 순서 변경",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(2.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .alpha(if (isPendingDelete) 0.58f else 1f)
                ) {
                    supersetLabel?.takeUnless { isSupersetSelectionMode }?.let { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${entry.records.size}세트 · ${entry.exercise.group}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isPendingDelete) {
                    TextButton(
                        onClick = onRestore,
                        modifier = Modifier.debugContentDescription(
                            TestContentDescriptions.strengthRoutineExerciseRestore(entry.id)
                        )
                    ) {
                        Text("복구")
                    }
                }
            }
        }
    }
}

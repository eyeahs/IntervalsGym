package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.core.localizedContentDescription

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.lighthousepark.intervalsgym.core.LocalizedText as Text
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

@Composable
internal fun StrengthOngoingExerciseRow(
    entry: StrengthRoutineEntry,
    supersetLabel: String?,
    completedSets: Int,
    isComplete: Boolean,
    isCurrent: Boolean,
    isSupersetSelectionMode: Boolean,
    isSupersetSelected: Boolean,
    isDragging: Boolean,
    dragHandleModifier: Modifier,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val baseContainerColor = strengthSupersetSelectionContainerColor(
        isSelected = isSupersetSelected,
        defaultColor = if (isCurrent) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .debugContentDescription(TestContentDescriptions.strengthOngoingEntry(entry.id))
            .throttleRapidTaps(enabled = !isSupersetSelectionMode)
            .alpha(if (isComplete && !isDragging) 0.62f else 1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) MaterialTheme.colorScheme.secondaryContainer else baseContainerColor
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!isSupersetSelectionMode) {
                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .height(40.dp)
                        .then(dragHandleModifier),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DragIndicator,
                        contentDescription = localizedContentDescription("길게 눌러 순서 변경"),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if (isSupersetSelectionMode) {
                StrengthSupersetSelectionMarker(
                    entryId = entry.id,
                    supersetLabel = supersetLabel,
                    isSelected = isSupersetSelected
                )
            } else {
                Icon(
                    imageVector = if (isComplete) Icons.Outlined.CheckCircle else Icons.Outlined.FitnessCenter,
                    contentDescription = null,
                    tint = if (isComplete) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                supersetLabel?.takeUnless { isSupersetSelectionMode }?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (isComplete) {
                        Text(
                            text = "완료",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "$completedSets/${entry.records.size} 세트 완료",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (entry.note.isNotBlank()) {
                    Text(
                        text = entry.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.strength.StrengthSetRecord

@Composable
internal fun StrengthSetRecordRow(
    index: Int,
    record: StrengthSetRecord,
    modifier: Modifier = Modifier,
    isUnilateral: Boolean = false,
    weightUnit: String = "kg",
    showCompletion: Boolean = true,
    canResetCompleted: Boolean = showCompletion,
    showActualInput: Boolean = false,
    onDelete: (() -> Unit)? = null,
    onActualRecordChange: ((StrengthSetRecord) -> Unit)? = null,
    onRecordChange: (StrengthSetRecord) -> Unit,
) {
    val rowBackground = when {
        record.completed -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentAlpha = if (record.completed) 0.48f else 1f
    val swipeEnabled = onDelete != null && !record.completed
    val resetSwipeEnabled = record.completed && canResetCompleted
    val actualInputCallback = onActualRecordChange.takeIf { showActualInput && !record.completed }
    val hasActualInputCell = actualInputCallback != null
    val completedResultDiffers = record.completed &&
        (record.performedWeightKg != record.weightKg || record.performedReps != record.reps)
    val hasResultCell = hasActualInputCell || completedResultDiffers

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CompletedSetResetSwipeContainer(
            key = record.id,
            enabled = resetSwipeEnabled,
            modifier = Modifier.debugContentDescription(TestContentDescriptions.strengthSetRecordRow(record.id)),
            onResetRequested = { onRecordChange(record.copy(completed = false)) }
        ) { resetSwipeModifier ->
            PendingSwipeDeleteContainer(
                key = record.id,
                enabled = swipeEnabled,
                isPendingDelete = false,
                onDeleteRequested = { onDelete?.invoke() },
                onCommitDelete = {
                    onDelete?.invoke()
                }
            ) { swipeModifier, pendingDelete ->
                val effectiveContentAlpha = if (pendingDelete) 0.58f else contentAlpha
                Column(
                    modifier = resetSwipeModifier
                        .then(swipeModifier),
                    verticalArrangement = Arrangement.spacedBy((-6).dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(1f)
                            .shadow(
                                elevation = if (hasResultCell) 3.dp else 0.dp,
                                shape = RoundedCornerShape(20.dp),
                                clip = false
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (pendingDelete) {
                                    MaterialTheme.colorScheme.surfaceVariant
                                } else {
                                    rowBackground
                                }
                            )
                            .debugContentDescription(
                                TestContentDescriptions.strengthPlannedSetRecord(record.id)
                            )
                            .padding(start = 14.dp, top = 10.dp, end = 14.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}세트",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .width(48.dp)
                                .alpha(effectiveContentAlpha)
                        )
                        SetMetricField(
                            value = record.weightKg,
                            onValueChange = { onRecordChange(record.copy(weightKg = it)) },
                            unit = weightUnit,
                            readOnly = record.completed,
                            testContentDescription = TestContentDescriptions.strengthPlannedSetWeight(record.id),
                            modifier = Modifier
                                .weight(1f)
                                .alpha(effectiveContentAlpha)
                        )
                        Text(
                            text = "/",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.alpha(effectiveContentAlpha)
                        )
                        SetMetricField(
                            value = record.reps,
                            onValueChange = { onRecordChange(record.copy(reps = it)) },
                            prefix = if (isUnilateral) "각" else null,
                            unit = "회",
                            readOnly = record.completed,
                            testContentDescription = TestContentDescriptions.strengthPlannedSetReps(record.id),
                            modifier = Modifier
                                .weight(1f)
                                .alpha(effectiveContentAlpha)
                        )
                        SetMetricField(
                            value = record.restSeconds,
                            onValueChange = { onRecordChange(record.copy(restSeconds = it)) },
                            unit = "초",
                            readOnly = record.completed,
                            modifier = Modifier
                                .weight(1f)
                                .alpha(effectiveContentAlpha)
                        )
                        if (record.completed) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "완료된 세트",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    actualInputCallback?.let { onActualChange ->
                        StrengthActualSetRecordCell(
                            record = record,
                            isUnilateral = isUnilateral,
                            weightUnit = weightUnit,
                            pendingDelete = pendingDelete,
                            onRecordChange = onActualChange
                        )
                    }
                    if (completedResultDiffers) {
                        StrengthActualSetRecordCell(
                            record = record,
                            isUnilateral = isUnilateral,
                            weightUnit = weightUnit,
                            pendingDelete = false,
                            readOnly = true,
                            onRecordChange = {}
                        )
                    }
                }
            }
        }
        if (showCompletion) {
            OutlinedButton(
                onClick = { onRecordChange(record.copy(completed = !record.completed)) },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (record.completed) "완료됨" else "완료 체크")
            }
        }
    }
}

@Composable
private fun StrengthActualSetRecordCell(
    record: StrengthSetRecord,
    isUnilateral: Boolean,
    weightUnit: String,
    pendingDelete: Boolean,
    readOnly: Boolean = false,
    modifier: Modifier = Modifier,
    onRecordChange: (StrengthSetRecord) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (pendingDelete) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.tertiaryContainer
                },
                shape = RoundedCornerShape(
                    bottomStart = 18.dp,
                    bottomEnd = 18.dp
                )
            )
            .debugContentDescription(TestContentDescriptions.strengthActualSetRecord(record.id))
            .padding(start = 14.dp, top = 16.dp, end = 14.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "결과",
            style = MaterialTheme.typography.bodyLarge,
            color = if (pendingDelete) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onTertiaryContainer
            },
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(48.dp)
        )
        SetMetricField(
            value = record.performedWeightKg,
            onValueChange = { onRecordChange(record.copy(actualWeightKg = it)) },
            unit = weightUnit,
            readOnly = readOnly,
            testContentDescription = TestContentDescriptions.strengthActualSetWeight(record.id),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "/",
            style = MaterialTheme.typography.titleLarge,
            color = if (pendingDelete) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onTertiaryContainer
            },
            fontWeight = FontWeight.Bold
        )
        SetMetricField(
            value = record.performedReps,
            onValueChange = { onRecordChange(record.copy(actualReps = it)) },
            prefix = if (isUnilateral) "각" else null,
            unit = "회",
            readOnly = readOnly,
            testContentDescription = TestContentDescriptions.strengthActualSetReps(record.id),
            modifier = Modifier.weight(1f)
        )
    }
}

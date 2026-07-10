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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    showActualInput: Boolean = false,
    onDelete: (() -> Unit)? = null,
    onActualRecordChange: ((StrengthSetRecord) -> Unit)? = null,
    onRecordChange: (StrengthSetRecord) -> Unit,
) {
    val rowBackground = when {
        record.completed -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentAlpha = if (record.completed) 0.48f else 1f
    val swipeEnabled = onDelete != null && !record.completed
    val resetSwipeEnabled = record.completed && showCompletion

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
                        .then(swipeModifier)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (pendingDelete) MaterialTheme.colorScheme.surfaceVariant else rowBackground)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
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
                            modifier = Modifier
                                .weight(1f)
                                .alpha(effectiveContentAlpha)
                        )
                        SetMetricField(
                            value = record.restSeconds,
                            onValueChange = { onRecordChange(record.copy(restSeconds = it)) },
                            unit = "초",
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
                    if (showActualInput && !record.completed && onActualRecordChange != null) {
                        StrengthActualSetRecordCell(
                            record = record,
                            isUnilateral = isUnilateral,
                            weightUnit = weightUnit,
                            pendingDelete = pendingDelete,
                            onRecordChange = onActualRecordChange
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
    onRecordChange: (StrengthSetRecord) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (pendingDelete) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                }
            )
            .debugContentDescription(TestContentDescriptions.strengthActualSetRecord(record.id))
            .padding(start = 14.dp, top = 10.dp, end = 14.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "실제",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(48.dp)
        )
        SetMetricField(
            value = record.performedWeightKg,
            onValueChange = { onRecordChange(record.copy(actualWeightKg = it)) },
            unit = weightUnit,
            testContentDescription = TestContentDescriptions.strengthActualSetWeight(record.id),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "/",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Bold
        )
        SetMetricField(
            value = record.performedReps,
            onValueChange = { onRecordChange(record.copy(actualReps = it)) },
            prefix = if (isUnilateral) "각" else null,
            unit = "회",
            testContentDescription = TestContentDescriptions.strengthActualSetReps(record.id),
            modifier = Modifier.weight(1f)
        )
    }
}

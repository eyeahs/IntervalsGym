package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.strength.StrengthExercise
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthSetRecord
import com.lighthousepark.intervalsgym.strength.baseVariationOptions
import com.lighthousepark.intervalsgym.strength.customStrengthExercise
import com.lighthousepark.intervalsgym.strength.defaultStrengthSetRecord
import com.lighthousepark.intervalsgym.strength.isUnilateral
import com.lighthousepark.intervalsgym.strength.weightInputUnitLabel
import com.lighthousepark.intervalsgym.strength.withPropagatedRecordChange
import com.lighthousepark.intervalsgym.strength.withRecords

@Composable
internal fun StrengthExerciseDetailEditor(
    entry: StrengthRoutineEntry,
    isChangingExercise: Boolean,
    onEntryChange: (StrengthRoutineEntry) -> Unit,
    onChangingExerciseChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    fun updateRecords(records: List<StrengthSetRecord>) {
        onEntryChange(entry.withRecords(records))
    }

    var isTypeDialogVisible by remember(entry.id) { mutableStateOf(false) }
    var exerciseForChange by remember(entry.id) { mutableStateOf<StrengthExercise?>(null) }
    var exerciseForChangeSearchQuery by remember(entry.id) { mutableStateOf("") }
    var isCustomExerciseDialogVisible by remember(entry.id) { mutableStateOf(false) }

    if (isTypeDialogVisible) {
        StrengthExerciseTypeDialog(
            entry = entry,
            exercise = entry.exercise,
            initialEquipment = entry.equipment,
            initialVariation = entry.variation,
            onDismiss = { isTypeDialogVisible = false },
            onDone = { equipment, variation ->
                isTypeDialogVisible = false
                onEntryChange(
                    entry.copy(
                        equipment = equipment,
                        variation = variation
                    )
                )
            }
        )
    }

    exerciseForChange?.let { exercise ->
        StrengthExerciseTypeDialog(
            entry = entry,
            exercise = exercise,
            initialEquipment = exercise.equipmentOptions.firstOrNull().orEmpty(),
            initialVariation = exercise.baseVariationOptions().firstOrNull().orEmpty(),
            initialSearchQuery = exerciseForChangeSearchQuery,
            onDismiss = { exerciseForChange = null },
            onDone = { equipment, variation ->
                exerciseForChange = null
                onChangingExerciseChange(false)
                onEntryChange(
                    entry.copy(
                        exercise = exercise,
                        equipment = equipment,
                        variation = variation
                    )
                )
            }
        )
    }

    if (isCustomExerciseDialogVisible) {
        CustomStrengthExerciseDialog(
            onDismiss = { isCustomExerciseDialogVisible = false },
            onAdd = { name ->
                isCustomExerciseDialogVisible = false
                exerciseForChangeSearchQuery = ""
                exerciseForChange = customStrengthExercise(name)
            }
        )
    }

    if (isChangingExercise) {
        StrengthExerciseListScreen(
            modifier = modifier,
            onAddCustomExercise = { isCustomExerciseDialogVisible = true },
            onExerciseSelected = { exercise, searchQuery ->
                exerciseForChangeSearchQuery = searchQuery
                exerciseForChange = exercise
            }
        )
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = entry.exercise.group,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = entry.note,
                            onValueChange = { onEntryChange(entry.copy(note = it)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("메모") },
                            placeholder = { Text("예: 무릎 각도 확인") },
                            minLines = 2,
                            maxLines = 4
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isTypeDialogVisible = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .debugContentDescription(TestContentDescriptions.StrengthExerciseDetailChangeType),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("타입 변경", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            OutlinedButton(
                                onClick = { onChangingExerciseChange(true) },
                                modifier = Modifier
                                    .weight(1f)
                                    .debugContentDescription(TestContentDescriptions.StrengthExerciseDetailChangeExercise),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("운동 변경", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
            itemsIndexed(entry.records, key = { _, record -> record.id }) { index, record ->
                StrengthSetRecordRow(
                    index = index,
                    record = record,
                    modifier = Modifier.animateItem(),
                    isUnilateral = entry.isUnilateral(),
                    weightUnit = entry.weightInputUnitLabel(),
                    showCompletion = false,
                    onDelete = if (entry.records.size > 1) {
                        {
                            updateRecords(entry.records.filterIndexed { recordIndex, _ -> recordIndex != index })
                        }
                    } else {
                        null
                    },
                    onRecordChange = { next ->
                        onEntryChange(entry.withPropagatedRecordChange(index, next))
                    }
                )
            }
            item {
                OutlinedButton(
                    onClick = {
                        updateRecords(entry.records + defaultStrengthSetRecord(entry))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .debugContentDescription(TestContentDescriptions.StrengthExerciseDetailAddSet),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("세트 추가")
                }
            }
        }
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            tonalElevation = 3.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .debugContentDescription(TestContentDescriptions.StrengthExerciseDetailDeleteExercise),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("운동 삭제", maxLines = 1)
                }
            }
        }
    }
}

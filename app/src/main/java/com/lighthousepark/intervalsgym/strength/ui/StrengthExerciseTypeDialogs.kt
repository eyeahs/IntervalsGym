package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.strength.StrengthExercise
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.UNILATERAL_MODE_OPTIONS
import com.lighthousepark.intervalsgym.strength.baseVariationOptions
import com.lighthousepark.intervalsgym.strength.combineVariationAndUnilateral
import com.lighthousepark.intervalsgym.strength.equipmentOptionsWithBodyweight
import com.lighthousepark.intervalsgym.strength.forcedUnilateralModeForVariation
import com.lighthousepark.intervalsgym.strength.inferEquipmentFromSearch
import com.lighthousepark.intervalsgym.strength.inferUnilateralFromSearch
import com.lighthousepark.intervalsgym.strength.inferVariationFromSearch
import com.lighthousepark.intervalsgym.strength.splitVariationAndUnilateral

@Composable
internal fun StrengthExerciseConfigDialog(
    exercise: StrengthExercise,
    initialSearchQuery: String = "",
    onDismiss: () -> Unit,
    onDone: (String, String) -> Unit,
) {
    val isCustomExercise = exercise.group == "사용자 추가" || exercise.id.startsWith("custom_")
    val equipmentOptions = remember(exercise.id) { exercise.equipmentOptionsWithBodyweight() }
    val inferredEquipment = remember(exercise.id, initialSearchQuery, equipmentOptions) {
        exercise.inferEquipmentFromSearch(initialSearchQuery, equipmentOptions)
    }
    val inferredVariation = remember(exercise.id, initialSearchQuery) {
        exercise.inferVariationFromSearch(initialSearchQuery)
    }
    val inferredUnilateral = remember(exercise.id, initialSearchQuery) {
        exercise.inferUnilateralFromSearch(initialSearchQuery)
    }
    var selectedEquipment by remember(exercise.id, initialSearchQuery) {
        mutableStateOf(inferredEquipment ?: exercise.equipmentOptions.first())
    }
    var selectedVariation by remember(exercise.id, initialSearchQuery) {
        mutableStateOf(inferredVariation ?: exercise.baseVariationOptions().first())
    }
    var selectedUnilateral by remember(exercise.id, initialSearchQuery) {
        mutableStateOf(inferredUnilateral ?: "양쪽")
    }
    var customEquipment by remember(exercise.id, initialSearchQuery) { mutableStateOf("") }
    val equipment = if (selectedEquipment == "직접 입력") customEquipment.trim() else selectedEquipment
    val canComplete = selectedEquipment != "직접 입력" || equipment.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(exercise.nameKo) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = exercise.group,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ChoiceGrid(
                    title = "기구",
                    options = equipmentOptions,
                    selected = selectedEquipment,
                    onSelected = { selectedEquipment = if (selectedEquipment == it) "" else it }
                )
                if (isCustomExercise && selectedEquipment == "직접 입력") {
                    OutlinedTextField(
                        value = customEquipment,
                        onValueChange = { customEquipment = it },
                        label = { Text("기구 직접 입력") },
                        placeholder = { Text("예: 케이블") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (!isCustomExercise) {
                    ChoiceGrid(
                        title = "세부 타입",
                        options = exercise.baseVariationOptions(),
                        selected = selectedVariation,
                        onSelected = { selectedVariation = it }
                    )
                }
                ChoiceGrid(
                    title = "좌우 방식",
                    options = UNILATERAL_MODE_OPTIONS,
                    selected = selectedUnilateral,
                    onSelected = { selectedUnilateral = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDone(
                        equipment,
                        if (isCustomExercise) {
                            combineVariationAndUnilateral("기본", selectedUnilateral)
                        } else {
                            combineVariationAndUnilateral(selectedVariation, selectedUnilateral)
                        }
                    )
                },
                enabled = canComplete,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthExerciseConfigDone)
            ) {
                Text("완료")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthExerciseConfigCancel)
            ) {
                Text("취소")
            }
        }
    )
}

@Composable
internal fun CustomStrengthExerciseDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("신규 운동 추가") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("운동 이름") },
                placeholder = { Text("예: 케이블 풀오버") },
                singleLine = true,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthCustomExerciseName)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(name.trim()) },
                enabled = name.isNotBlank(),
                modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthCustomExerciseAdd)
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthCustomExerciseCancel)
            ) {
                Text("취소")
            }
        }
    )
}

@Composable
internal fun StrengthExerciseTypeDialog(
    entry: StrengthRoutineEntry,
    exercise: StrengthExercise,
    initialEquipment: String,
    initialVariation: String,
    initialSearchQuery: String = "",
    confirmText: String = "완료",
    onExerciseChangeClick: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onDone: (String, String) -> Unit,
) {
    val isCustomExercise = exercise.group == "사용자 추가" || exercise.id.startsWith("custom_")
    val equipmentOptions = remember(exercise.id) { exercise.equipmentOptionsWithBodyweight() }
    val inferredEquipment = remember(exercise.id, initialSearchQuery, equipmentOptions) {
        exercise.inferEquipmentFromSearch(initialSearchQuery, equipmentOptions)
    }
    val inferredVariation = remember(exercise.id, initialSearchQuery) {
        exercise.inferVariationFromSearch(initialSearchQuery)
    }
    val inferredUnilateral = remember(exercise.id, initialSearchQuery) {
        exercise.inferUnilateralFromSearch(initialSearchQuery)
    }
    val initialEquipmentSelection = remember(exercise.id, initialEquipment, initialSearchQuery) {
        val preferredEquipment = inferredEquipment ?: initialEquipment
        when {
            preferredEquipment.isBlank() -> ""
            preferredEquipment in equipmentOptions -> preferredEquipment
            isCustomExercise -> "직접 입력"
            else -> preferredEquipment
        }
    }
    val initialCustomEquipment = remember(exercise.id, initialEquipment, initialSearchQuery) {
        val preferredEquipment = inferredEquipment ?: initialEquipment
        preferredEquipment.takeIf { it.isNotBlank() && it !in equipmentOptions }.orEmpty()
    }
    val variationParts = remember(exercise.id, initialVariation, initialSearchQuery) {
        val preferredVariation = inferredVariation?.let {
            combineVariationAndUnilateral(it, inferredUnilateral ?: "양쪽")
        } ?: initialVariation
        splitVariationAndUnilateral(exercise, preferredVariation)
    }
    var selectedEquipment by remember(exercise.id, initialEquipment, initialSearchQuery) {
        mutableStateOf(initialEquipmentSelection)
    }
    var customEquipment by remember(exercise.id, initialEquipment, initialSearchQuery) {
        mutableStateOf(initialCustomEquipment)
    }
    var selectedVariation by remember(exercise.id, initialVariation, initialSearchQuery) {
        mutableStateOf(variationParts.first.ifBlank { exercise.baseVariationOptions().firstOrNull().orEmpty() })
    }
    var selectedUnilateral by remember(exercise.id, initialVariation, initialSearchQuery) {
        mutableStateOf(variationParts.second.ifBlank { "양쪽" })
    }
    val forcedUnilateral = exercise.forcedUnilateralModeForVariation(selectedVariation)
    val effectiveUnilateral = forcedUnilateral ?: selectedUnilateral
    val equipment = if (selectedEquipment == "직접 입력") customEquipment.trim() else selectedEquipment
    val canComplete = selectedEquipment != "직접 입력" || equipment.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${exercise.nameKo} 타입 변경") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = exercise.group,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ChoiceGrid(
                    title = "기구",
                    options = equipmentOptions,
                    selected = selectedEquipment,
                    onSelected = { selectedEquipment = if (selectedEquipment == it) "" else it }
                )
                if (isCustomExercise && selectedEquipment == "직접 입력") {
                    OutlinedTextField(
                        value = customEquipment,
                        onValueChange = { customEquipment = it },
                        label = { Text("기구 직접 입력") },
                        placeholder = { Text("예: 케이블") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (!isCustomExercise) {
                    ChoiceGrid(
                        title = "세부 타입",
                        options = exercise.baseVariationOptions(),
                        selected = selectedVariation,
                        onSelected = {
                            selectedVariation = it
                            selectedUnilateral = exercise.forcedUnilateralModeForVariation(it) ?: selectedUnilateral
                        }
                    )
                }
                ChoiceGrid(
                    title = "좌우 방식",
                    options = UNILATERAL_MODE_OPTIONS,
                    selected = effectiveUnilateral,
                    onSelected = { if (forcedUnilateral == null) selectedUnilateral = it },
                    isOptionEnabled = { forcedUnilateral == null || it == forcedUnilateral }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDone(
                        equipment,
                        if (isCustomExercise) {
                            combineVariationAndUnilateral("기본", effectiveUnilateral)
                        } else {
                            combineVariationAndUnilateral(selectedVariation, effectiveUnilateral)
                        }
                    )
                },
                enabled = canComplete,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthExerciseConfigDone)
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            Row {
                onExerciseChangeClick?.let { onClick ->
                    TextButton(
                        onClick = onClick,
                        modifier = Modifier.debugContentDescription(
                            TestContentDescriptions.StrengthExerciseDetailChangeExercise
                        )
                    ) {
                        Text("운동 변경")
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthExerciseConfigCancel)
                ) {
                    Text("취소")
                }
            }
        }
    )
}

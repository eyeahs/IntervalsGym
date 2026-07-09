package com.lighthousepark.intervalsgym.training.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.core.formatKoreanMonthDayWeekday
import com.lighthousepark.intervalsgym.core.toClockTimeOrNull
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.training.toEpochMillis
import com.lighthousepark.intervalsgym.training.toLocalDateFromMillis
import java.time.LocalDate

/**
 * Modal sheet for saving or uploading a strength routine to a selected calendar date.
 * Reuse it from [WeeklyTrainingScreen] instead of creating another routine-save screen.
 * UI tests: TrainingCalendarUiTest.strengthRoutineSaveBottomSheet_dateButtonOpensDatePicker,
 * strengthRoutineSaveBottomSheet_disablesDateAndRowsWhileSaving.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StrengthRoutineSaveBottomSheet(
    routines: List<StrengthWorkoutRoutine>,
    selectedDate: LocalDate,
    selectedTimeText: String,
    savingRoutineId: Int?,
    message: String?,
    error: String?,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onTimeChanged: (String) -> Unit,
    onRoutineSelected: (StrengthWorkoutRoutine) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val isTimeValid = selectedTimeText.toClockTimeOrNull() != null

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis
                            ?.toLocalDateFromMillis()
                            ?.let(onDateSelected)
                        showDatePicker = false
                    }
                ) {
                    Text("변경")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Routine 추가",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        enabled = savingRoutineId == null,
                        modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthRoutineSaveDate),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(selectedDate.formatKoreanMonthDayWeekday())
                    }
                    OutlinedTextField(
                        value = selectedTimeText,
                        onValueChange = { next ->
                            if (next.length <= 5 && next.all { it.isDigit() || it == ':' }) {
                                onTimeChanged(next)
                            }
                        },
                        enabled = savingRoutineId == null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .debugContentDescription(TestContentDescriptions.StrengthRoutineSaveTime),
                        label = { Text("시간") },
                        placeholder = { Text("HH:mm") },
                        singleLine = true,
                        isError = !isTimeValid,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
            if (message != null || error != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            message?.let {
                                Text(it, color = MaterialTheme.colorScheme.primary)
                            }
                            error?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
            if (routines.isEmpty()) {
                item {
                    Text(
                        text = "저장할 웨이트 Routine이 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(routines, key = { it.id }) { routine ->
                    StrengthRoutineSaveRow(
                        routine = routine,
                        isSaving = savingRoutineId == routine.id,
                        enabled = savingRoutineId == null && isTimeValid,
                        onClick = { onRoutineSelected(routine) }
                    )
                }
            }
        }
    }
}

/**
 * UI tests: TrainingCalendarUiTest.strengthRoutineSaveRow_invokesRoutineSelection.
 */
@Composable
internal fun StrengthRoutineSaveRow(
    routine: StrengthWorkoutRoutine,
    isSaving: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val setCount = routine.entries.sumOf { it.records.size }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled || isSaving) 1f else 0.58f)
            .debugContentDescription(TestContentDescriptions.strengthRoutineSaveRow(routine.id))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = routine.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${routine.entries.size}개 운동 · ${setCount}세트",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Outlined.CloudUpload, contentDescription = null)
            }
        }
    }
}

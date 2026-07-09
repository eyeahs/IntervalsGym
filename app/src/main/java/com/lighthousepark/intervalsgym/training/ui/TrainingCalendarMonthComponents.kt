package com.lighthousepark.intervalsgym.training.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.data.strengthRoutineForDisplay
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.training.TrainingDateRange
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.monthCalendarDays
import com.lighthousepark.intervalsgym.training.sportType
import com.lighthousepark.intervalsgym.workout.ui.TrainingSportIcon
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun MonthlyTrainingCalendar(
    range: TrainingDateRange,
    items: List<TrainingItem>,
    onRoutineSelected: (TrainingItem) -> Unit,
    onIntervalStrengthRoutineSelected: (TrainingItem?, StrengthWorkoutRoutine) -> Unit,
    onDaySelected: (LocalDate) -> Unit,
) {
    val grouped = items.groupBy { it.date }
    val calendarDays = remember(range.start, range.end) { range.monthCalendarDays() }
    val weeks = remember(calendarDays) { calendarDays.chunked(7) }
    val weekLabels = remember {
        listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item(key = "month-weekdays") {
            Row(modifier = Modifier.fillMaxWidth()) {
                weekLabels.forEach { dayOfWeek ->
                    Text(
                        text = dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.KOREAN),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 8.dp)
                    )
                }
            }
        }
        itemsIndexed(weeks, key = { index, _ -> "week-$index" }) { _, week ->
            val visibleItemCount = week
                .maxOfOrNull { day -> grouped[day].orEmpty().size.coerceAtMost(3) }
                ?.coerceAtLeast(2)
                ?: 2
            val cellHeight = if (visibleItemCount >= 3) 92.dp else 72.dp
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    MonthlyCalendarDayCell(
                        day = day,
                        isInCurrentMonth = !day.isBefore(range.start) && !day.isAfter(range.end),
                        items = grouped[day].orEmpty(),
                        visibleItemCount = visibleItemCount,
                        modifier = Modifier
                            .weight(1f)
                            .height(cellHeight),
                        onRoutineSelected = onRoutineSelected,
                        onIntervalStrengthRoutineSelected = onIntervalStrengthRoutineSelected,
                        onDaySelected = onDaySelected
                    )
                }
            }
        }
    }
}

/**
 * UI tests: TrainingCalendarUiTest.monthlyCalendarDayCell_selectsEmptyDay,
 * monthlyCalendarDayCell_selectsResultItem, monthlyCalendarDayCell_routesStrengthRoutineChipToStrengthCallback.
 */
@Composable
internal fun MonthlyCalendarDayCell(
    day: LocalDate,
    isInCurrentMonth: Boolean,
    items: List<TrainingItem>,
    visibleItemCount: Int,
    modifier: Modifier = Modifier,
    onRoutineSelected: (TrainingItem) -> Unit,
    onIntervalStrengthRoutineSelected: (TrainingItem?, StrengthWorkoutRoutine) -> Unit,
    onDaySelected: (LocalDate) -> Unit,
) {
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
    val today = remember { LocalDate.now() }
    val isToday = day == today
    Column(
        modifier = modifier
            .border(0.5.dp, borderColor)
            .background(
                when {
                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
                    isInCurrentMonth -> MaterialTheme.colorScheme.surface
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
                }
            )
            .debugContentDescription(TestContentDescriptions.monthlyCalendarDay(day))
            .clickable { onDaySelected(day) }
            .padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = day.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
            color = when {
                isToday -> MaterialTheme.colorScheme.primary
                isInCurrentMonth -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
            },
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
        items.take(visibleItemCount).forEach { item ->
            MonthlyCalendarItemChip(
                item = item,
                onClick = {
                    val strengthRoutine = item.strengthRoutineForDisplay()
                    if (item.isRoutine && strengthRoutine != null) {
                        onIntervalStrengthRoutineSelected(item, strengthRoutine)
                    } else {
                        onRoutineSelected(item)
                    }
                }
            )
        }
    }
}

/**
 * UI tests: TrainingCalendarUiTest.monthlyCalendarDayCell_selectsResultItem,
 * monthlyCalendarDayCell_routesStrengthRoutineChipToStrengthCallback.
 */
@Composable
internal fun MonthlyCalendarItemChip(
    item: TrainingItem,
    onClick: () -> Unit,
) {
    val color = if (item.isRoutine) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(17.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.14f))
            .debugContentDescription(TestContentDescriptions.monthlyCalendarItem(item.id))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        TrainingStatusIcons(
            item = item,
            color = color,
            iconSize = 12.dp,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        TrainingSportIcon(
            sportType = item.sportType(),
            showBackground = false,
            modifier = Modifier.size(12.dp)
        )
    }
}

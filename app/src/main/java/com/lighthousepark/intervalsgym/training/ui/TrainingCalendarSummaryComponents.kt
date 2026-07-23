package com.lighthousepark.intervalsgym.training.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.lighthousepark.intervalsgym.core.LocalizedText as Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.core.formatDistance
import com.lighthousepark.intervalsgym.core.formatDuration
import com.lighthousepark.intervalsgym.core.formatSummaryMetric
import com.lighthousepark.intervalsgym.training.TrainingCalendarMode
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.isCyclingItem
import com.lighthousepark.intervalsgym.training.isRunningItem
import com.lighthousepark.intervalsgym.training.latestMetricValue

internal data class SummaryDetail(
    val text: String,
    val icon: ImageVector? = null,
)

@Composable
internal fun WeekSummary(
    activities: List<TrainingItem>,
    routines: List<TrainingItem>,
    modifier: Modifier = Modifier,
    attachedToToolbar: Boolean = false,
) {
    val allItems = activities + routines
    val completedLoad = activities.sumOf { it.load ?: 0 }
    val plannedLoad = routines.sumOf { it.load ?: 0 }
    val completedTime = activities.sumOf { it.durationSeconds ?: 0 }
    val plannedTime = routines.sumOf { it.durationSeconds ?: 0 }
    val totalTime = allItems.sumOf { it.durationSeconds ?: 0 }
    val completedRunningDistance = activities
        .filter { it.isRunningItem() }
        .sumOf { it.distanceMeters ?: 0.0 }
    val plannedRunningDistance = routines
        .filter { it.isRunningItem() }
        .sumOf { it.distanceMeters ?: 0.0 }
    val totalRunningDistance = allItems
        .filter { it.isRunningItem() }
        .sumOf { it.distanceMeters ?: 0.0 }
    val completedCyclingDistance = activities
        .filter { it.isCyclingItem() }
        .sumOf { it.distanceMeters ?: 0.0 }
    val plannedCyclingDistance = routines
        .filter { it.isCyclingItem() }
        .sumOf { it.distanceMeters ?: 0.0 }
    val totalCyclingDistance = allItems
        .filter { it.isCyclingItem() }
        .sumOf { it.distanceMeters ?: 0.0 }
    val fitness = allItems.latestMetricValue { it.fitness }
    val fatigue = allItems.latestMetricValue { it.fatigue }
    val form = allItems.latestMetricValue { it.form }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .debugContentDescription(TestContentDescriptions.TrainingCalendarWeekSummary),
        shape = if (attachedToToolbar) {
            RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
        } else {
            RoundedCornerShape(20.dp)
        },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryMetricColumn(
                    title = "Routine",
                    value = "${routines.size}회",
                    details = listOf(
                        SummaryDetail(formatDuration(plannedTime)),
                        SummaryDetail(formatDistance(plannedRunningDistance), Icons.AutoMirrored.Outlined.DirectionsRun),
                        SummaryDetail(formatDistance(plannedCyclingDistance), Icons.AutoMirrored.Outlined.DirectionsBike),
                        SummaryDetail("Load $plannedLoad")
                    ),
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricColumn(
                    title = "완료",
                    value = "${activities.size}회",
                    details = listOf(
                        SummaryDetail(formatDuration(completedTime)),
                        SummaryDetail(formatDistance(completedRunningDistance), Icons.AutoMirrored.Outlined.DirectionsRun),
                        SummaryDetail(formatDistance(completedCyclingDistance), Icons.AutoMirrored.Outlined.DirectionsBike),
                        SummaryDetail("Load $completedLoad")
                    ),
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricColumn(
                    title = "Total(예상)",
                    value = "${allItems.size}회",
                    details = listOf(
                        SummaryDetail(formatDuration(totalTime)),
                        SummaryDetail(formatDistance(totalRunningDistance), Icons.AutoMirrored.Outlined.DirectionsRun),
                        SummaryDetail(formatDistance(totalCyclingDistance), Icons.AutoMirrored.Outlined.DirectionsBike),
                        SummaryDetail("Load ${completedLoad + plannedLoad}")
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            if (fitness != null || fatigue != null || form != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    fitness?.let {
                        SummaryMetricColumn(
                            title = "Fitness",
                            value = it.formatSummaryMetric(),
                            details = listOf(SummaryDetail("CTL")),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    fatigue?.let {
                        SummaryMetricColumn(
                            title = "Fatigue",
                            value = it.formatSummaryMetric(),
                            details = listOf(SummaryDetail("ATL")),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    form?.let {
                        SummaryMetricColumn(
                            title = "Form",
                            value = it.formatSummaryMetric(),
                            details = listOf(SummaryDetail("TSB")),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SummaryMetricColumn(
    title: String,
    value: String,
    details: List<SummaryDetail>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        details.forEach { detail ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                detail.icon?.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = detail.text,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun CalendarModeIcon(
    mode: TrainingCalendarMode,
    modifier: Modifier = Modifier,
) {
    val outlineColor = MaterialTheme.colorScheme.onSurfaceVariant
    val accentColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.075f
        val corner = size.minDimension * 0.16f
        val headerHeight = size.height * 0.24f
        val innerLeft = size.width * 0.22f
        val innerTop = size.height * 0.38f
        val innerRight = size.width * 0.78f
        val innerBottom = size.height * 0.82f

        drawRoundRect(
            color = outlineColor,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(size.width - strokeWidth, size.height - strokeWidth),
            cornerRadius = CornerRadius(corner, corner),
            style = Stroke(width = strokeWidth)
        )
        drawLine(
            color = outlineColor,
            start = Offset(size.width * 0.18f, headerHeight),
            end = Offset(size.width * 0.82f, headerHeight),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = outlineColor,
            start = Offset(size.width * 0.32f, 0f),
            end = Offset(size.width * 0.32f, size.height * 0.16f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = outlineColor,
            start = Offset(size.width * 0.68f, 0f),
            end = Offset(size.width * 0.68f, size.height * 0.16f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        when (mode) {
            TrainingCalendarMode.DAY -> {
                val side = size.minDimension * 0.24f
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset((size.width - side) / 2f, innerTop),
                    size = Size(side, side),
                    cornerRadius = CornerRadius(side * 0.22f, side * 0.22f)
                )
            }
            TrainingCalendarMode.WEEK -> {
                val blockHeight = size.height * 0.16f
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(innerLeft, (innerTop + innerBottom - blockHeight) / 2f),
                    size = Size(innerRight - innerLeft, blockHeight),
                    cornerRadius = CornerRadius(blockHeight / 2f, blockHeight / 2f)
                )
            }
            TrainingCalendarMode.MONTH -> {
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(innerLeft, innerTop),
                    size = Size(innerRight - innerLeft, innerBottom - innerTop),
                    cornerRadius = CornerRadius(size.minDimension * 0.07f, size.minDimension * 0.07f)
                )
            }
        }
    }
}

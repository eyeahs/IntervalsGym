package com.lighthousepark.intervalsgym.workout.ui

import com.lighthousepark.intervalsgym.MainActivity
import com.lighthousepark.intervalsgym.R
import com.lighthousepark.intervalsgym.app.*
import com.lighthousepark.intervalsgym.core.*
import com.lighthousepark.intervalsgym.data.*
import com.lighthousepark.intervalsgym.login.*
import com.lighthousepark.intervalsgym.overlay.*
import com.lighthousepark.intervalsgym.running.*
import com.lighthousepark.intervalsgym.running.ui.*
import com.lighthousepark.intervalsgym.strength.*
import com.lighthousepark.intervalsgym.strength.ui.*
import com.lighthousepark.intervalsgym.training.*
import com.lighthousepark.intervalsgym.training.ui.*
import com.lighthousepark.intervalsgym.workout.ui.*

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Surface as MaterialSurface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lighthousepark.intervalsgym.ui.theme.IntervalsGymTheme
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun PlanWorkoutGraph(
    blocks: List<PlanBlock>,
    totalSeconds: Int,
    modifier: Modifier = Modifier,
    title: String = "그래프",
    sportType: TrainingSportType = TrainingSportType.OTHER,
) {
    DetailSection(title = title) {
        PlanWorkoutGraphCanvas(
            blocks = blocks,
            totalSeconds = totalSeconds,
            modifier = modifier,
            sportType = sportType,
            height = 190.dp
        )
    }
}

@Composable
internal fun LocalRunningWorkoutGraphSection(
    blocks: List<PlanBlock>,
    totalSeconds: Int,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "로컬 러닝 기록 그래프",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "로컬 기록 삭제",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            PlanWorkoutGraphCanvas(
                blocks = blocks,
                totalSeconds = totalSeconds,
                sportType = TrainingSportType.RUNNING,
                height = 190.dp
            )
        }
    }
}

@Composable
internal fun PlanWorkoutGraphCanvas(
    blocks: List<PlanBlock>,
    totalSeconds: Int,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp,
    sportType: TrainingSportType = TrainingSportType.OTHER,
    progressSeconds: Int? = null,
) {
    val graphBlocks = remember(blocks, sportType) { blocks.toWorkoutGraphBlocks(sportType) }
    val unit = when {
        graphBlocks.any { it.unit == WorkoutGraphUnit.Watts && it.value > 0f } -> WorkoutGraphUnit.Watts
        graphBlocks.any { it.unit == WorkoutGraphUnit.SpeedKmh && it.value > 0f } -> WorkoutGraphUnit.SpeedKmh
        graphBlocks.any { it.unit == WorkoutGraphUnit.Percent && it.value > 0f } -> WorkoutGraphUnit.Percent
        else -> WorkoutGraphUnit.Percent
    }
    val values = graphBlocks
        .filter { it.unit == unit }
        .map { it.value }
    val yMax = values.maxOrNull()?.takeIf { it > 0f } ?: 1f
    val graphTotalSeconds = (totalSeconds.takeIf { it > 0 } ?: blocks.sumOf { it.durationSeconds }).coerceAtLeast(1)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val axisColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val lineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
    val speedLineColor = Color(0xFF7EDFD2).copy(alpha = 0.62f)
    val thresholdColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val progressColor = MaterialTheme.colorScheme.error
    val activeBlockColor = Color(0xFFFFC857)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceColor)
    ) {
        val compact = size.height < 150.dp.toPx()
        val left = when {
            unit == WorkoutGraphUnit.SpeedKmh && compact -> 52.dp.toPx()
            unit == WorkoutGraphUnit.SpeedKmh -> 58.dp.toPx()
            compact -> 34.dp.toPx()
            else -> 42.dp.toPx()
        }
        val right = 10.dp.toPx()
        val top = if (compact) 10.dp.toPx() else 14.dp.toPx()
        val bottom = if (compact) 24.dp.toPx() else 30.dp.toPx()
        val chartWidth = (size.width - left - right).coerceAtLeast(1f)
        val chartHeight = (size.height - top - bottom).coerceAtLeast(1f)
        val bottomY = top + chartHeight
        val textSize = (if (compact) 10f else 12f) * density
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = labelColor.toArgb()
            this.textSize = textSize
        }

        fun xFor(seconds: Int): Float {
            return left + (seconds.coerceIn(0, graphTotalSeconds).toFloat() / graphTotalSeconds.toFloat()) * chartWidth
        }

        fun yFor(value: Float): Float {
            val ratio = (value / yMax).coerceIn(0f, 1f)
            return bottomY - chartHeight * ratio
        }

        val activeGraphBlock = progressSeconds?.let { progress ->
            graphBlocks.firstOrNull { graphBlock ->
                progress >= graphBlock.block.startSecond && progress < graphBlock.block.endSecond
            } ?: graphBlocks.lastOrNull { progress >= it.block.endSecond }
        }

        drawLine(axisColor, Offset(left, top), Offset(left, bottomY), strokeWidth = 1.dp.toPx())
        drawLine(axisColor, Offset(left, bottomY), Offset(left + chartWidth, bottomY), strokeWidth = 1.dp.toPx())

        val midValue = yMax / 2f
        listOf(0f, midValue, yMax).forEach { value ->
            val y = yFor(value)
            drawLine(axisColor.copy(alpha = 0.28f), Offset(left, y), Offset(left + chartWidth, y), strokeWidth = 1.dp.toPx())
            labelPaint.textAlign = Paint.Align.RIGHT
            val labelX = left - 7.dp.toPx()
            val labels = value.formatGraphAxisLabels(unit)
            if (labels.size == 1) {
                drawContext.canvas.nativeCanvas.drawText(
                    labels.first(),
                    labelX,
                    y + textSize / 3f,
                    labelPaint
                )
            } else {
                drawContext.canvas.nativeCanvas.drawText(
                    labels[0],
                    labelX,
                    y + textSize * 0.05f,
                    labelPaint
                )
                drawContext.canvas.nativeCanvas.drawText(
                    labels[1],
                    labelX,
                    y + textSize * 1.15f,
                    labelPaint
                )
            }
        }

        val threshold = when (unit) {
            WorkoutGraphUnit.Watts -> {
                if (sportType == TrainingSportType.CYCLING) {
                    graphBlocks.firstNotNullOfOrNull { graphBlock ->
                        val percent = graphBlock.intensityPercent?.takeIf { it > 0f } ?: return@firstNotNullOfOrNull null
                        graphBlock.value / (percent / 100f)
                    }
                } else {
                    values.maxOrNull()?.let { it * 0.9f }
                }
            }
            WorkoutGraphUnit.Percent -> 100f
            WorkoutGraphUnit.SpeedKmh -> null
        }?.takeIf { it > 0f && it < yMax }
        threshold?.let {
            val y = yFor(it)
            drawLine(
                color = thresholdColor,
                start = Offset(left, y),
                end = Offset(left + chartWidth, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()))
            )
        }

        activeGraphBlock?.block?.let { block ->
            val x = xFor(block.startSecond)
            val width = (xFor(block.endSecond) - x).coerceAtLeast(1.5.dp.toPx())
            drawRect(
                color = activeBlockColor.copy(alpha = 0.18f),
                topLeft = Offset(x, top),
                size = Size(width, chartHeight)
            )
        }

        graphBlocks.forEach { graphBlock ->
            val block = graphBlock.block
            val value = if (graphBlock.unit == unit) graphBlock.value else 0f
            val x = xFor(block.startSecond)
            val width = (xFor(block.endSecond) - x).coerceAtLeast(1.5.dp.toPx())
            val barHeight = if (value > 0f) (bottomY - yFor(value)).coerceAtLeast(4.dp.toPx()) else 4.dp.toPx()
            val y = bottomY - barHeight
            val color = if (graphBlock.block.index == activeGraphBlock?.block?.index) {
                activeBlockColor
            } else {
                graphBlock.graphColor(yMax, unit, sportType)
            }
            val fillAlpha = if (unit == WorkoutGraphUnit.SpeedKmh) 0.52f else 0.72f
            drawRect(
                color = color.copy(alpha = fillAlpha),
                topLeft = Offset(x, y),
                size = Size(width, barHeight)
            )
            drawRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(width, barHeight),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        val stepPath = Path()
        var hasStepPoint = false
        graphBlocks.forEach { graphBlock ->
            val block = graphBlock.block
            val value = if (graphBlock.unit == unit) graphBlock.value else 0f
            val y = yFor(value)
            val xStart = xFor(block.startSecond)
            val xEnd = xFor(block.endSecond)
            if (!hasStepPoint) {
                stepPath.moveTo(xStart, y)
                hasStepPoint = true
            } else {
                stepPath.lineTo(xStart, y)
            }
            stepPath.lineTo(xEnd, y)
        }
        if (hasStepPoint) {
            val isSpeedGraph = unit == WorkoutGraphUnit.SpeedKmh
            drawPath(
                path = stepPath,
                color = if (isSpeedGraph) speedLineColor else lineColor,
                style = Stroke(
                    width = if (isSpeedGraph) 1.1.dp.toPx() else 1.5.dp.toPx()
                )
            )
        }

        progressSeconds?.let { progress ->
            val x = xFor(progress)
            drawLine(
                color = progressColor,
                start = Offset(x, top),
                end = Offset(x, bottomY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        val tickSeconds = listOf(
            0,
            graphTotalSeconds / 2,
            graphTotalSeconds
        ).distinct()
        tickSeconds.forEach { seconds ->
            val x = xFor(seconds)
            labelPaint.textAlign = when (seconds) {
                0 -> Paint.Align.LEFT
                graphTotalSeconds -> Paint.Align.RIGHT
                else -> Paint.Align.CENTER
            }
            drawLine(axisColor.copy(alpha = 0.3f), Offset(x, bottomY), Offset(x, bottomY + 4.dp.toPx()), strokeWidth = 1.dp.toPx())
            drawContext.canvas.nativeCanvas.drawText(
                formatGraphTime(seconds),
                x,
                bottomY + (if (compact) 17.dp.toPx() else 21.dp.toPx()),
                labelPaint
            )
        }
    }
}

@Composable
internal fun TrainingItemDetailCard(
    item: TrainingItem,
    totalSeconds: Int,
    isStrengthPlan: Boolean,
    strengthWorkout: CompletedStrengthWorkout?,
    uploadMessage: String?,
    uploadError: String?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isStrengthPlan) Icons.Outlined.FitnessCenter else if (item.isPlan) Icons.Outlined.Schedule else Icons.Outlined.Route,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                TrainingTypeLabel(isPlan = item.isPlan, resultLabel = "Summary")
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricChip(icon = Icons.Outlined.Today, text = item.date.format(DateTimeFormatter.ofPattern("M/d")) + " " + item.timeLabel)
                if (totalSeconds > 0) {
                    MetricChip(icon = Icons.Outlined.Schedule, text = formatDuration(totalSeconds))
                }
                item.load?.let { MetricChip(icon = Icons.Outlined.Speed, text = "Load $it") }
                item.weightLiftedKg?.takeIf { it > 0.0 }?.let {
                    MetricChip(icon = Icons.Outlined.FitnessCenter, text = "Weight ${formatWeight(it)} kg")
                }
            }
            strengthWorkout?.let { workout ->
                StrengthWorkoutSummary(
                    workout = workout,
                    uploadMessage = uploadMessage,
                    uploadError = uploadError
                )
            }
            if (isStrengthPlan) {
                Text(
                    text = "IntervalsGym 웨이트 Plan",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
internal fun StrengthWorkoutSummary(
    workout: CompletedStrengthWorkout,
    uploadMessage: String?,
    uploadError: String?,
) {
    val totalRestSeconds = workout.restEvents.sumOf { it.actualSeconds }
    val volume = workout.entries.totalVolumeKg()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = if (workout.uploadedToIntervals) "로컬 기록 · Intervals.icu 업로드됨" else "로컬 기록 · Intervals.icu 미동기화",
            style = MaterialTheme.typography.labelLarge,
            color = if (workout.uploadedToIntervals) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${workout.setEvents.size}세트 · RPE ${workout.rpe} · Load ${workout.trainingLoad} · 볼륨 ${formatWeight(volume)} kg · 운동 시간 ${formatDuration(workout.durationSeconds)} · 실제 휴식 ${formatClock(totalRestSeconds)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        uploadMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
        uploadError?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
internal fun LocalStrengthWorkoutDetailSection(
    workout: CompletedStrengthWorkout,
) {
    DetailSection(title = "웨이트 상세 기록") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            workout.entries.forEachIndexed { entryIndex, entry ->
                if (entryIndex > 0) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
                StrengthWorkoutExerciseDetail(
                    workout = workout,
                    entry = entry
                )
            }
        }
    }
}

@Composable
internal fun StrengthWorkoutExerciseDetail(
    workout: CompletedStrengthWorkout,
    entry: StrengthPlanEntry,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = entry.title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        entry.records.forEachIndexed { index, record ->
            StrengthWorkoutSetDetailRow(
                workout = workout,
                entry = entry,
                record = record,
                setIndex = index
            )
        }
    }
}

@Composable
internal fun StrengthWorkoutSetDetailRow(
    workout: CompletedStrengthWorkout,
    entry: StrengthPlanEntry,
    record: StrengthSetRecord,
    setIndex: Int,
) {
    val completedEvent = workout.setEvents.firstOrNull {
        it.exerciseEntryId == entry.id && it.setRecordId == record.id
    }
    val restEvent = completedEvent?.let { event ->
        workout.restEvents.firstOrNull { it.afterSetSequence == event.sequence }
    }
    val rawWeight = completedEvent?.weightKg
        ?: record.weightKg.ifBlank { entry.targetWeightKg.ifBlank { "-" } }
    val rawReps = completedEvent?.reps ?: record.reps.ifBlank { "-" }
    val plannedRest = completedEvent?.targetRestSeconds
        ?: record.restSeconds.toIntOrNull()
        ?: entry.restSeconds
    val isCompleted = completedEvent != null || record.completed
    val weightText = displayWeightText(rawWeight)
    val repsText = if (entry.isUnilateral()) {
        "각 ${displayUnilateralRepsText(rawReps)}"
    } else {
        displayRepsText(rawReps)
    }
    val actualRestText = restEvent?.let { " · 실제 ${formatClock(it.actualSeconds)}" }.orEmpty()
    val detailText = "$weightText x $repsText · 휴식 ${plannedRest}초$actualRestText"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Set ${setIndex + 1}",
            style = MaterialTheme.typography.labelLarge,
            color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(52.dp)
        )
        Text(
            text = detailText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (isCompleted) "완료" else "미완료",
            style = MaterialTheme.typography.labelMedium,
            color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}

internal fun displayWeightText(raw: String): String {
    val value = raw.trim()
    if (value.isBlank() || value == "-") return "-kg"
    if (value.contains("좌") || value.contains("우")) {
        val numbers = Regex("""\d+(?:\.\d+)?""").findAll(value).map { it.value }.toList()
        val distinctNumbers = numbers.distinct()
        return when {
            distinctNumbers.size == 1 -> "${distinctNumbers.first()}kg"
            numbers.isEmpty() -> "-kg"
            else -> value
        }
    }
    return if (value.contains("kg", ignoreCase = true)) value else "${value}kg"
}

internal fun displayRepsText(raw: String): String {
    val value = raw.trim()
    if (value.isBlank() || value == "-") return "-회"
    return if (value.contains("회")) value else "${value}회"
}

internal fun displayUnilateralRepsText(raw: String): String {
    val value = raw.trim()
    if (value.isBlank() || value == "-") return "-회"
    if (value.contains("좌") || value.contains("우")) {
        val numbers = Regex("""\d+""").findAll(value).map { it.value }.toList()
        val distinctNumbers = numbers.distinct()
        return when {
            distinctNumbers.size == 1 -> "${distinctNumbers.first()}회"
            numbers.isEmpty() -> "-회"
            else -> displayRepsText(value)
        }
    }
    return displayRepsText(value)
}

internal fun buildStrengthSetSummary(
    entry: StrengthPlanEntry,
    record: StrengthSetRecord,
): String {
    val weight = displayWeightText(record.summaryWeightText(entry))
    val reps = if (entry.isUnilateral()) {
        "각 ${displayUnilateralRepsText(record.summaryRepsText())}"
    } else {
        displayRepsText(record.summaryRepsText())
    }
    val rest = record.restSeconds.ifBlank { entry.restSeconds.takeIf { it > 0 }?.toString().orEmpty() }
        .ifBlank { "-" }
    return "$weight x $reps · 휴식 ${rest}초"
}

internal fun StrengthSetRecord.summaryWeightText(entry: StrengthPlanEntry): String {
    if (weightKg.isNotBlank()) return weightKg
    val left = leftWeightKg.trim()
    val right = rightWeightKg.trim()
    return when {
        left.isNotBlank() && right.isNotBlank() && left == right -> left
        left.isNotBlank() && right.isNotBlank() -> "좌 ${left}kg / 우 ${right}kg"
        left.isNotBlank() -> left
        right.isNotBlank() -> right
        else -> entry.targetWeightKg
    }
}

internal fun StrengthSetRecord.summaryRepsText(): String {
    if (reps.isNotBlank()) return reps
    val left = leftReps.trim()
    val right = rightReps.trim()
    return when {
        left.isNotBlank() && right.isNotBlank() && left == right -> left
        left.isNotBlank() && right.isNotBlank() -> "좌 ${left}회 / 우 ${right}회"
        left.isNotBlank() -> left
        else -> right
    }
}

@Composable
internal fun DetailSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
internal fun RunningTimerPanel(
    elapsedSeconds: Int,
    totalSeconds: Int,
    currentBlock: PlanBlock?,
    blockRemaining: Int,
    remainingTotal: Int,
    isRunning: Boolean,
    onToggle: () -> Unit,
    onReset: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "수행 시간",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${formatClock(elapsedSeconds)} / ${formatClock(totalSeconds)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TimerStat(
                    title = "현재 Block",
                    value = currentBlock?.title ?: "대기",
                    detail = currentBlock?.targetText.orEmpty(),
                    modifier = Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.error
                )
                TimerStat(
                    title = "Block 남은 시간",
                    value = formatClock(blockRemaining),
                    detail = "전체 ${formatClock(remainingTotal)} 남음",
                    modifier = Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onToggle,
                    enabled = totalSeconds > 0,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isRunning) "일시정지" else "시작")
                }
                OutlinedButton(
                    onClick = onReset,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.RestartAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("리셋")
                }
            }
        }
    }
}

@Composable
internal fun TimerStat(
    title: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier,
    accent: Color,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun PlanTimeline(
    blocks: List<PlanBlock>,
    currentIndex: Int,
    elapsedSeconds: Int,
    totalSeconds: Int,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            blocks.forEach { block ->
                val color = when {
                    block.index == currentIndex -> MaterialTheme.colorScheme.error
                    elapsedSeconds >= block.endSecond -> MaterialTheme.colorScheme.primary
                    block.isRecovery -> Color(0xFF8AA7B0)
                    else -> Color(0xFF2F7D6D)
                }
                Box(
                    modifier = Modifier
                        .weight(block.durationSeconds.coerceAtLeast(1).toFloat())
                        .fillMaxHeight()
                        .background(color)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "진행률 ${if (totalSeconds > 0) elapsedSeconds * 100 / totalSeconds else 0}%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun PlanBlockRow(
    block: PlanBlock,
    isCurrent: Boolean,
    isDone: Boolean,
) {
    val containerColor = when {
        isCurrent -> MaterialTheme.colorScheme.error
        isDone -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = if (isCurrent) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = block.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = block.targetText.ifBlank { block.kind },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrent) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = formatClock(block.durationSeconds),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
internal fun MetricChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun LoadingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(12.dp))
        Text("Intervals.icu에서 가져오는 중")
    }
}

@Composable
internal fun EmptyView(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(42.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onRetry, shape = RoundedCornerShape(20.dp)) {
            Icon(Icons.Outlined.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("다시 시도")
        }
    }
}

internal data class WeekUiState(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val isLoading: Boolean = false,
    val activities: List<TrainingItem> = emptyList(),
    val plans: List<TrainingItem> = emptyList(),
    val error: String? = null,
)

internal fun TrainingSportType.icon(): ImageVector {
    return when (this) {
        TrainingSportType.RUNNING -> Icons.AutoMirrored.Outlined.DirectionsRun
        TrainingSportType.CYCLING -> Icons.AutoMirrored.Outlined.DirectionsBike
        TrainingSportType.STRENGTH -> Icons.Outlined.FitnessCenter
        TrainingSportType.OTHER -> Icons.Outlined.Route
    }
}

@Composable
internal fun TrainingSportIcon(
    sportType: TrainingSportType,
    modifier: Modifier = Modifier,
    showBackground: Boolean = true,
) {
    val tint = when (sportType) {
        TrainingSportType.RUNNING -> MaterialTheme.colorScheme.tertiary
        TrainingSportType.CYCLING -> MaterialTheme.colorScheme.secondary
        TrainingSportType.STRENGTH -> MaterialTheme.colorScheme.primary
        TrainingSportType.OTHER -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    if (showBackground) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(999.dp))
                .background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = sportType.icon(),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
    } else {
        Icon(
            imageVector = sportType.icon(),
            contentDescription = null,
            tint = tint,
            modifier = modifier
        )
    }
}

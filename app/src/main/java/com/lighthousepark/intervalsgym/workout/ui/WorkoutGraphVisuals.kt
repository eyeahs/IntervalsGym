package com.lighthousepark.intervalsgym.workout.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.formatGraphTime
import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.training.TrainingSportType
import com.lighthousepark.intervalsgym.training.WorkoutGraphUnit
import com.lighthousepark.intervalsgym.training.formatGraphAxisLabels
import com.lighthousepark.intervalsgym.training.graphColor
import com.lighthousepark.intervalsgym.training.toWorkoutGraphBlocks
import com.lighthousepark.intervalsgym.ui.theme.AppGraphActive
import com.lighthousepark.intervalsgym.ui.theme.AppGraphBackground
import com.lighthousepark.intervalsgym.ui.theme.AppGraphGrid
import com.lighthousepark.intervalsgym.ui.theme.AppGraphLabel
import com.lighthousepark.intervalsgym.ui.theme.AppGraphOrange3
import com.lighthousepark.intervalsgym.ui.theme.AppGraphOrange4
import com.lighthousepark.intervalsgym.ui.theme.AppGraphThreshold

@Composable
internal fun RoutineWorkoutGraph(
    blocks: List<RoutineBlock>,
    totalSeconds: Int,
    modifier: Modifier = Modifier,
    title: String = "그래프",
    sportType: TrainingSportType = TrainingSportType.OTHER,
) {
    DetailSection(title = title) {
        RoutineWorkoutGraphCanvas(
            blocks = blocks,
            totalSeconds = totalSeconds,
            modifier = modifier,
            sportType = sportType,
            height = 190.dp
        )
    }
}

@Composable
internal fun RoutineWorkoutGraphCanvas(
    blocks: List<RoutineBlock>,
    totalSeconds: Int,
    modifier: Modifier = Modifier,
    height: Dp,
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
    val surfaceColor = AppGraphBackground
    val axisColor = AppGraphGrid.copy(alpha = 0.7f)
    val labelColor = AppGraphLabel
    val lineColor = AppGraphOrange4.copy(alpha = 0.84f)
    val speedLineColor = AppGraphOrange3.copy(alpha = 0.82f)
    val thresholdColor = AppGraphThreshold.copy(alpha = 0.72f)
    val progressColor = AppGraphOrange4
    val activeBlockColor = AppGraphActive

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

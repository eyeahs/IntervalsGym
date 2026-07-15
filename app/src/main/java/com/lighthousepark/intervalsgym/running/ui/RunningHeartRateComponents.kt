package com.lighthousepark.intervalsgym.running.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.running.HEART_RATE_GRAPH_WINDOW_MILLIS
import com.lighthousepark.intervalsgym.running.HeartRateSample
import com.lighthousepark.intervalsgym.ui.theme.AppGraphHeartRate

/**
 * UI tests: RunningSessionUiTest.heartRateGraph_connectButtonInvokesCallback.
 */
@Composable
internal fun HeartRateGraph(
    samples: List<HeartRateSample>,
    isHeartRateConnected: Boolean,
    heartRateBpm: Int?,
    onHeartRateClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    graphHeight: Dp = 64.dp,
) {
    val now = remember(samples) {
        maxOf(System.currentTimeMillis(), samples.lastOrNull()?.timestampMillis ?: 0L)
    }
    val windowStartMillis = now - HEART_RATE_GRAPH_WINDOW_MILLIS
    val visibleSamples = remember(samples, now) {
        samples.filter { it.timestampMillis >= windowStartMillis }
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(contentColor.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "심박 그래프",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "최근 5분",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.72f)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(graphHeight),
            contentAlignment = Alignment.Center
        ) {
            if (visibleSamples.isNotEmpty()) {
                val minBpm = visibleSamples.minOf { it.bpm }.let { (it - 5).coerceAtLeast(40) }
                val maxBpm = visibleSamples.maxOf { it.bpm }.let { (it + 5).coerceAtLeast(minBpm + 10) }
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(graphHeight)
                ) {
                    val gridColor = contentColor.copy(alpha = 0.18f)
                    val lineColor = AppGraphHeartRate
                    val textColor = contentColor.copy(alpha = 0.62f).toArgb()
                    repeat(3) { index ->
                        val y = size.height * index / 2f
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    val points = visibleSamples.map { sample ->
                        val xRatio = (
                            (sample.timestampMillis - windowStartMillis).toFloat() /
                                HEART_RATE_GRAPH_WINDOW_MILLIS.toFloat()
                            ).coerceIn(0f, 1f)
                        val yRatio = ((sample.bpm - minBpm).toFloat() / (maxBpm - minBpm).toFloat())
                            .coerceIn(0f, 1f)
                        Offset(
                            x = size.width * xRatio,
                            y = size.height - size.height * yRatio
                        )
                    }
                    points.zipWithNext().forEach { (start, end) ->
                        drawLine(
                            color = lineColor,
                            start = start,
                            end = end,
                            strokeWidth = 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                    points.lastOrNull()?.let { point ->
                        drawCircle(
                            color = lineColor,
                            radius = 3.dp.toPx(),
                            center = point
                        )
                    }
                    val labelPaint = Paint().apply {
                        textSize = 10.dp.toPx()
                        color = textColor
                        textAlign = Paint.Align.LEFT
                        isAntiAlias = true
                    }
                    drawContext.canvas.nativeCanvas.apply {
                        drawText("${maxBpm}bpm", 0f, 10.dp.toPx(), labelPaint)
                        drawText("${minBpm}bpm", 0f, size.height - 2.dp.toPx(), labelPaint)
                    }
                }
            }
            if (isHeartRateConnected) {
                Text(
                    text = heartRateBpm?.let { "$it bpm" } ?: "-- bpm",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppGraphHeartRate,
                    textAlign = TextAlign.Center
                )
            } else {
                OutlinedButton(
                    onClick = onHeartRateClick,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningConnectHeartRate),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "심박계 연결",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

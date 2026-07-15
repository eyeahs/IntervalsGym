package com.lighthousepark.intervalsgym.workout.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.running.RunningRoutePoint
import com.lighthousepark.intervalsgym.ui.theme.AppGraphRouteBackground
import com.lighthousepark.intervalsgym.ui.theme.AppGraphRouteFinish
import com.lighthousepark.intervalsgym.ui.theme.AppGraphRouteLand
import com.lighthousepark.intervalsgym.ui.theme.AppGraphRouteStart

@Composable
internal fun LocalRunningRoutePreview(
    routePoints: List<RunningRoutePoint>,
) {
    val routeColor = MaterialTheme.colorScheme.primary
    val startColor = AppGraphRouteStart
    val finishColor = AppGraphRouteFinish
    val seaColor = AppGraphRouteBackground
    val islandColor = AppGraphRouteLand
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "독도 400m 가상 트랙",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${routePoints.size} points",
                style = MaterialTheme.typography.labelMedium,
                color = labelColor
            )
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(seaColor)
        ) {
            val padding = 18.dp.toPx()
            val minLat = routePoints.minOf { it.latitude }
            val maxLat = routePoints.maxOf { it.latitude }
            val minLon = routePoints.minOf { it.longitude }
            val maxLon = routePoints.maxOf { it.longitude }
            val latRange = (maxLat - minLat).takeIf { it > 0.0 } ?: 0.0001
            val lonRange = (maxLon - minLon).takeIf { it > 0.0 } ?: 0.0001
            val drawWidth = (size.width - padding * 2f).coerceAtLeast(1f)
            val drawHeight = (size.height - padding * 2f).coerceAtLeast(1f)

            fun pointFor(routePoint: RunningRoutePoint): Offset {
                val x = padding + ((routePoint.longitude - minLon) / lonRange).toFloat() * drawWidth
                val y = padding + ((maxLat - routePoint.latitude) / latRange).toFloat() * drawHeight
                return Offset(x, y)
            }

            drawCircle(
                color = islandColor.copy(alpha = 0.9f),
                radius = 7.dp.toPx(),
                center = Offset(size.width / 2f - 10.dp.toPx(), size.height / 2f)
            )
            drawCircle(
                color = islandColor.copy(alpha = 0.85f),
                radius = 5.dp.toPx(),
                center = Offset(size.width / 2f + 11.dp.toPx(), size.height / 2f - 2.dp.toPx())
            )

            val path = Path()
            routePoints.forEachIndexed { index, routePoint ->
                val point = pointFor(routePoint)
                if (index == 0) {
                    path.moveTo(point.x, point.y)
                } else {
                    path.lineTo(point.x, point.y)
                }
            }
            drawPath(
                path = path,
                color = routeColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
            drawCircle(color = startColor, radius = 4.dp.toPx(), center = pointFor(routePoints.first()))
            drawCircle(color = finishColor, radius = 4.dp.toPx(), center = pointFor(routePoints.last()))
        }
    }
}

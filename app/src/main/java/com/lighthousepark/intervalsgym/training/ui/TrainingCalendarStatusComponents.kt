package com.lighthousepark.intervalsgym.training.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.training.TrainingItem

@Composable
internal fun TrainingStatusIcons(
    item: TrainingItem,
    color: Color,
    iconSize: Dp,
    horizontalArrangement: Arrangement.Horizontal,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement
    ) {
        if (item.isRoutine || item.pairedRoutine != null) {
            TrainingStatusIconContainer(
                color = color,
                size = iconSize
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize * 0.67f),
                    tint = color
                )
            }
        }
        if (!item.isRoutine) {
            TrainingStatusIconContainer(
                color = color,
                size = iconSize
            ) {
                ResultCheckIcon(
                    modifier = Modifier.size(iconSize * 0.67f),
                    color = color
                )
            }
        }
    }
}

@Composable
internal fun TrainingStatusIconContainer(
    color: Color,
    size: Dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
internal fun ResultCheckIcon(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.11f
        drawCircle(
            color = color.copy(alpha = 0.18f),
            radius = size.minDimension / 2f
        )
        drawCircle(
            color = color,
            radius = size.minDimension / 2f - strokeWidth / 2f,
            style = Stroke(width = strokeWidth)
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.28f, size.height * 0.53f),
            end = Offset(size.width * 0.44f, size.height * 0.69f),
            strokeWidth = strokeWidth * 1.35f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.44f, size.height * 0.69f),
            end = Offset(size.width * 0.74f, size.height * 0.34f),
            strokeWidth = strokeWidth * 1.35f,
            cap = StrokeCap.Round
        )
    }
}

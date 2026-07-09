package com.lighthousepark.intervalsgym.workout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface as MaterialSurface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.training.TrainingSportType

@Composable
internal fun MetricChip(icon: ImageVector, text: String) {
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
internal fun TrainingTypeLabel(
    isRoutine: Boolean,
    resultLabel: String = "Result",
) {
    val containerColor = if (isRoutine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (isRoutine) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

    MaterialSurface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = if (isRoutine) "Routine" else resultLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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

/**
 * UI tests: WorkoutRoutineVisualsUiTest.errorView_retryButtonInvokesCallback.
 */
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
        OutlinedButton(
            onClick = onRetry,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.debugContentDescription(TestContentDescriptions.WorkoutErrorRetry)
        ) {
            Icon(Icons.Outlined.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("다시 시도")
        }
    }
}

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

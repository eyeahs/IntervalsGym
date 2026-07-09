package com.lighthousepark.intervalsgym.running.ui

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.core.formatClock
import com.lighthousepark.intervalsgym.running.MAX_RUNNING_INCLINE_PERCENT
import com.lighthousepark.intervalsgym.running.MAX_RUNNING_SPEED_KMH
import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.training.graphTargetSpeedKmh
import com.lighthousepark.intervalsgym.training.runningInclinePercent
import com.lighthousepark.intervalsgym.training.runningInclineText
import com.lighthousepark.intervalsgym.training.runningTargetSpeedText

/**
 * UI tests: RunningSessionUiTest.runningBlockPanel_exposesStepperActions,
 * runningTargetStepper_ignoresDisabledDecreaseAndInvokesEnabledIncrease.
 */
@Composable
internal fun RunningBlockPanel(
    block: RoutineBlock?,
    blockIndex: Int,
    blockCount: Int,
    remainingSeconds: Int,
    blinkOn: Boolean,
    isLastBlock: Boolean,
    onSpeedDecrease: () -> Unit,
    onSpeedIncrease: () -> Unit,
    onInclineDecrease: () -> Unit,
    onInclineIncrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val speedText = block?.runningTargetSpeedText().orEmpty().ifBlank { "-" }
    val inclineText = block?.runningInclineText().orEmpty().ifBlank { "-" }
    val speedKmh = block?.graphTargetSpeedKmh() ?: 0f
    val inclinePercent = block?.runningInclinePercent() ?: 0f
    val blockDurationText = formatClock(block?.durationSeconds ?: 0)
    val blockProgressText = "남은 ${formatClock(remainingSeconds)} / $blockDurationText"
    val blockTitle = block?.title
        ?.replace("Workout", "", ignoreCase = true)
        ?.trim()
        .orEmpty()
    val timerColor = if (remainingSeconds in 1..5 && blinkOn) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = listOf(
                    "Block ${blockIndex + 1} / $blockCount",
                    blockProgressText,
                    blockTitle
                ).filter { it.isNotBlank() }.joinToString(" · "),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RunningTargetStepper(
                    label = "속도",
                    value = speedText.takeIf { it != "-" } ?: "0km/h",
                    onDecrease = onSpeedDecrease,
                    onIncrease = onSpeedIncrease,
                    canDecrease = speedKmh > 0f,
                    canIncrease = speedKmh < MAX_RUNNING_SPEED_KMH,
                    modifier = Modifier.weight(1f)
                )
                RunningTargetStepper(
                    label = "경사도",
                    value = inclineText.takeIf { it != "-" } ?: "0%",
                    onDecrease = onInclineDecrease,
                    onIncrease = onInclineIncrease,
                    canDecrease = inclinePercent > 0f,
                    canIncrease = inclinePercent < MAX_RUNNING_INCLINE_PERCENT,
                    modifier = Modifier.weight(1f)
                )
            }
            RunningTimerText(
                text = formatClock(remainingSeconds),
                color = timerColor,
                modifier = Modifier.weight(1f),
                fontHeightRatio = 0.56f,
                maxFontSize = 138f
            )
        }
    }
}

@Composable
internal fun RunningTargetStepper(
    label: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    canDecrease: Boolean,
    canIncrease: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RunningTargetStepButton(
                icon = Icons.Outlined.Remove,
                contentDescription = "$label 감소",
                testContentDescription = TestContentDescriptions.runningTargetStepper(label, "decrease"),
                enabled = canDecrease,
                onStep = onDecrease
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            RunningTargetStepButton(
                icon = Icons.Outlined.Add,
                contentDescription = "$label 증가",
                testContentDescription = TestContentDescriptions.runningTargetStepper(label, "increase"),
                enabled = canIncrease,
                onStep = onIncrease
            )
        }
    }
}

@Composable
private fun RunningTargetStepButton(
    icon: ImageVector,
    contentDescription: String,
    testContentDescription: String,
    enabled: Boolean,
    onStep: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestOnStep by rememberUpdatedState(onStep)
    val repeatHandler = remember { Handler(Looper.getMainLooper()) }
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
    }
    val repeatStep = remember {
        object : Runnable {
            override fun run() {
                latestOnStep()
                repeatHandler.postDelayed(this, 92L)
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            repeatHandler.removeCallbacks(repeatStep)
        }
    }
    Surface(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(12.dp))
            .debugContentDescription(testContentDescription)
            .pointerInteropFilter { event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        if (enabled) {
                            latestOnStep()
                            repeatHandler.removeCallbacks(repeatStep)
                            repeatHandler.postDelayed(repeatStep, 420L)
                        }
                        true
                    }
                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        repeatHandler.removeCallbacks(repeatStep)
                        true
                    }
                    else -> true
                }
            },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 0.12f else 0.04f),
        contentColor = contentColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
internal fun RunningTimerText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    fontHeightRatio: Float,
    maxFontSize: Float,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val heightBasedFontSize = with(density) {
            (maxHeight.toPx() * fontHeightRatio).toSp().value
        }
        val widthBasedFontSize = with(density) {
            (maxWidth.toPx() / (text.length.coerceAtLeast(1) * 0.58f)).toSp().value
        }
        val fontSizeValue = minOf(heightBasedFontSize, widthBasedFontSize)
            .coerceIn(48f, maxFontSize)
        Text(
            text = text,
            fontSize = fontSizeValue.sp,
            lineHeight = fontSizeValue.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

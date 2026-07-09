package com.lighthousepark.intervalsgym.training.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface as MaterialSurface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription

/**
 * UI tests: TrainingCalendarUiTest.weeklyFabMenu_invokesExpandedActionCallbacks.
 */
@Composable
internal fun WeeklyTrainingFabMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onWorkoutClick: () -> Unit,
    onPlanAddClick: () -> Unit,
    onRoutineSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(180),
        label = "weekly-fab-rotation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(150)) + slideInVertically(
                animationSpec = tween(180),
                initialOffsetY = { it / 2 }
            ),
            exit = fadeOut(animationSpec = tween(120)) + slideOutVertically(
                animationSpec = tween(140),
                targetOffsetY = { it / 2 }
            )
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FabActionButton(
                    text = "운동 실행",
                    icon = Icons.Outlined.FitnessCenter,
                    onClick = onWorkoutClick
                )
                FabActionButton(
                    text = "계획 추가",
                    icon = Icons.Outlined.CalendarMonth,
                    onClick = onPlanAddClick
                )
                FabActionButton(
                    text = "Routine 관리",
                    icon = Icons.Outlined.Edit,
                    onClick = onRoutineSaveClick
                )
            }
        }
        FloatingActionButton(
            onClick = { onExpandedChange(!expanded) },
            modifier = Modifier
                .size(56.dp)
                .debugContentDescription(TestContentDescriptions.TrainingCalendarFabMenu),
            shape = RoundedCornerShape(999.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = if (expanded) "메뉴 닫기" else "메뉴 열기",
                modifier = Modifier.graphicsLayer {
                    rotationZ = rotation
                }
            )
        }
    }
}

@Composable
internal fun FabActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MaterialSurface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 6.dp
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .debugContentDescription(TestContentDescriptions.trainingCalendarFabAction(text)),
            shape = RoundedCornerShape(999.dp)
        ) {
            Icon(imageVector = icon, contentDescription = text)
        }
    }
}

package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.core.localizedContentDescription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import com.lighthousepark.intervalsgym.core.LocalizedText as Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.core.formatClock
import com.lighthousepark.intervalsgym.core.throttleRapidTaps

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StrengthSessionTopBar(
    title: String,
    isWorkoutActive: Boolean,
    elapsedSeconds: Int,
    showTimerBadgeAsNavigation: Boolean,
    showReadyActions: Boolean,
    showCalendarRoutineDelete: Boolean,
    isDeletingCalendarRoutine: Boolean,
    onBack: () -> Unit,
    onCalendarRoutineDelete: () -> Unit,
    onHistoryClick: () -> Unit,
) {
    TopAppBar(
        title = {
            StrengthSessionTopBarTitle(
                title = title,
                isWorkoutActive = isWorkoutActive,
                elapsedSeconds = elapsedSeconds
            )
        },
        navigationIcon = {
            if (showTimerBadgeAsNavigation) {
                StrengthSessionTimerBadge(
                    elapsedSeconds = elapsedSeconds,
                    modifier = Modifier.padding(start = 12.dp)
                )
            } else {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .throttleRapidTaps()
                        .debugContentDescription(TestContentDescriptions.StrengthSessionBack)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = localizedContentDescription("뒤로"))
                }
            }
        },
        actions = {
            if (showReadyActions) {
                if (showCalendarRoutineDelete) {
                    IconButton(
                        onClick = onCalendarRoutineDelete,
                        enabled = !isDeletingCalendarRoutine,
                        modifier = Modifier
                            .throttleRapidTaps()
                            .debugContentDescription(TestContentDescriptions.StrengthSessionCalendarRoutineDelete)
                    ) {
                        if (isDeletingCalendarRoutine) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = localizedContentDescription("Routine 삭제"),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                IconButton(
                    onClick = onHistoryClick,
                    modifier = Modifier
                        .throttleRapidTaps()
                        .debugContentDescription(TestContentDescriptions.StrengthSessionHistory)
                ) {
                    Icon(Icons.Outlined.Schedule, contentDescription = "History")
                }
            }
        }
    )
}

@Composable
internal fun StrengthSessionTopBarTitle(
    title: String,
    isWorkoutActive: Boolean,
    elapsedSeconds: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (isWorkoutActive) {
            StrengthSessionTimerBadge(elapsedSeconds = elapsedSeconds)
        }
    }
}

@Composable
private fun StrengthSessionTimerBadge(
    elapsedSeconds: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Text(
            text = formatClock(elapsedSeconds),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
internal fun StrengthSetBottomBar(
    allDone: Boolean,
    currentLabel: String,
    isUploading: Boolean,
    onCompleteSet: () -> Unit,
) {
    Surface(
        modifier = Modifier.navigationBarsPadding(),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!allDone) {
                Text(
                    text = currentLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Button(
                onClick = onCompleteSet,
                enabled = !isUploading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .throttleRapidTaps(enabled = allDone)
                    .debugContentDescription(TestContentDescriptions.StrengthCompleteSet),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    when {
                        isUploading -> "업로드 중"
                        allDone -> "운동 목록으로"
                        else -> "세트 완료"
                    }
                )
            }
        }
    }
}

@Composable
internal fun StrengthSessionOngoingBottomBar(
    activeExerciseLabel: String,
    isUploading: Boolean,
    onResumeExercise: () -> Unit,
    onFinish: () -> Unit,
) {
    Surface(
        modifier = Modifier.navigationBarsPadding(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onResumeExercise,
                enabled = !isUploading && activeExerciseLabel.isNotBlank(),
                modifier = Modifier
                    .weight(1.4f)
                    .height(52.dp)
                    .throttleRapidTaps()
                    .debugContentDescription(TestContentDescriptions.StrengthResumeWorkoutExercise),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = activeExerciseLabel.ifBlank { "수행 중 운동" },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Button(
                onClick = onFinish,
                enabled = !isUploading,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .throttleRapidTaps()
                    .debugContentDescription(TestContentDescriptions.StrengthFinishWorkout),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Outlined.CloudUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isUploading) "업로드 중" else "운동 종료")
            }
        }
    }
}

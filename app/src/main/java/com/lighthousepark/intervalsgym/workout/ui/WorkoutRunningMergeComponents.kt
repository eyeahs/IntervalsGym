package com.lighthousepark.intervalsgym.workout.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import com.lighthousepark.intervalsgym.core.LocalizedText as Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.core.formatDuration
import com.lighthousepark.intervalsgym.core.formatShortMonthDayTime
import com.lighthousepark.intervalsgym.running.RunningActivityMergeCandidate
import com.lighthousepark.intervalsgym.running.RunningActivityMergeMatchMethod
import java.time.Instant
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
internal fun WorkoutRunningMergeConfirmDialog(
    candidates: List<RunningActivityMergeCandidate>,
    selectedCandidateId: String?,
    isMerging: Boolean,
    onCandidateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isMerging) onDismiss() },
        title = { Text("Garmin 기록 병합") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Garmin 활동은 유지하고 IntervalsGym 블록 수행 정보를 추가합니다. 앱이 자동 업로드한 중복 기록이 있으면 병합 후 삭제합니다.")
                candidates.forEach { candidate ->
                    RunningMergeCandidateRow(
                        candidate = candidate,
                        selected = candidate.activity.id == selectedCandidateId,
                        enabled = !isMerging,
                        onClick = { onCandidateSelected(candidate.activity.id) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = selectedCandidateId != null && !isMerging,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningMergeConfirm)
            ) {
                Text("병합")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isMerging,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningMergeCancel)
            ) {
                Text("취소")
            }
        }
    )
}

@Composable
private fun RunningMergeCandidateRow(
    candidate: RunningActivityMergeCandidate,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 4.dp)
            .debugContentDescription(TestContentDescriptions.runningMergeCandidate(candidate.activity.id)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            enabled = enabled
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(candidate.activity.name, fontWeight = FontWeight.SemiBold)
            val startedAt = Instant.ofEpochMilli(candidate.activity.startedAtMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
            Text("${startedAt.formatShortMonthDayTime()} · ${formatDuration(candidate.activity.durationSeconds)}")
            Text(candidate.runningMergeMatchLabel())
        }
    }
}

internal fun RunningActivityMergeCandidate.runningMergeMatchLabel(): String {
    return when (matchMethod) {
        RunningActivityMergeMatchMethod.HEART_RATE -> {
            val percent = ((heartRateCorrelation ?: 0.0) * 100.0).roundToInt()
            "심박 일치 $percent% · 시작 ${offsetSeconds.signedSecondsLabel()}"
        }
        RunningActivityMergeMatchMethod.START_TIME -> {
            "시작 시각 기준 · 차이 ${abs(startDifferenceSeconds)}초"
        }
    }
}

private fun Int.signedSecondsLabel(): String {
    return when {
        this > 0 -> "+${this}초"
        this < 0 -> "${this}초"
        else -> "0초"
    }
}

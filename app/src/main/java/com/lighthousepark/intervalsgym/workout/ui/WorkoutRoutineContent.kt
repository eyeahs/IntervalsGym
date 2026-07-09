package com.lighthousepark.intervalsgym.workout.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.data.workoutDetailDescription
import com.lighthousepark.intervalsgym.running.RunningRoutePoint
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.sportType

@Composable
internal fun WorkoutRoutineDetailContent(
    routine: TrainingItem,
    detailTotalSeconds: Int,
    totalSeconds: Int,
    graphBlocks: List<RoutineBlock>,
    isWeightTrainingItem: Boolean,
    isRunningWorkoutRoutine: Boolean,
    intervalStrengthRoutine: StrengthWorkoutRoutine?,
    localSession: CompletedStrengthSession?,
    uploadMessage: String?,
    uploadError: String?,
    localRunningGraphBlocks: List<RoutineBlock>,
    localRunningRoutePoints: List<RunningRoutePoint>,
    innerPadding: PaddingValues,
    onDeleteLocalRunningSession: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = if (intervalStrengthRoutine != null || isRunningWorkoutRoutine) 96.dp else 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TrainingItemDetailCard(
                item = routine,
                totalSeconds = detailTotalSeconds,
                isStrengthRoutine = intervalStrengthRoutine != null,
                strengthSession = localSession,
                uploadMessage = uploadMessage,
                uploadError = uploadError
            )
        }
        localSession?.let { workout ->
            item {
                LocalStrengthSessionDetailSection(
                    workout = workout
                )
            }
        }
        routine.workoutDetailDescription(
            isWeightTrainingItem = isWeightTrainingItem,
            strengthRoutine = intervalStrengthRoutine
        ).takeIf { it.isNotBlank() }?.let { description ->
            item {
                DetailSection(title = "설명") {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (!isWeightTrainingItem && graphBlocks.isNotEmpty()) {
            item {
                if (routine.isLocalOnlyRunningResult) {
                    RoutineWorkoutGraph(
                        blocks = graphBlocks,
                        totalSeconds = totalSeconds,
                        sportType = routine.sportType(),
                        title = "Routine 그래프"
                    )
                } else {
                    RoutineWorkoutGraph(
                        blocks = graphBlocks,
                        totalSeconds = totalSeconds,
                        sportType = routine.sportType()
                    )
                }
            }
        }
        if (localRunningGraphBlocks.isNotEmpty()) {
            item {
                LocalRunningSessionGraphSection(
                    blocks = localRunningGraphBlocks,
                    totalSeconds = localRunningGraphBlocks.sumOf { it.durationSeconds },
                    routePoints = localRunningRoutePoints,
                    onDelete = onDeleteLocalRunningSession
                )
            }
        }
    }
}

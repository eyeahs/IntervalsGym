package com.lighthousepark.intervalsgym.training

import com.lighthousepark.intervalsgym.running.RunningRoutePoint
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import java.time.LocalDate
import java.time.LocalDateTime

internal data class WeekTrainingData(
    val activities: List<TrainingItem>,
    val routines: List<TrainingItem>,
)

internal data class TrainingItem(
    val id: String,
    val remoteId: String,
    val externalId: String?,
    val name: String,
    val type: String,
    val date: LocalDate,
    val startedAt: LocalDateTime?,
    val timeLabel: String,
    val durationSeconds: Int?,
    val distanceMeters: Double?,
    val weightLiftedKg: Double?,
    val load: Int?,
    val fitness: Double?,
    val fatigue: Double?,
    val form: Double?,
    val description: String?,
    val blocks: List<RoutineBlock>,
    val isRoutine: Boolean,
    val matchedStrengthSession: CompletedStrengthSession? = null,
    val matchedStrengthRoutine: StrengthWorkoutRoutine? = null,
    val isLocalOnlyStrengthResult: Boolean = false,
    val isLocalOnlyRunningResult: Boolean = false,
    val actualRunningBlocks: List<RoutineBlock> = emptyList(),
    val actualRunningRoutePoints: List<RunningRoutePoint> = emptyList(),
    val pairedRoutine: TrainingItem? = null,
    val workoutDocJson: String? = null,
)

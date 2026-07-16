package com.lighthousepark.intervalsgym.workout.ui

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.lighthousepark.intervalsgym.app.PREFS_NAME
import com.lighthousepark.intervalsgym.core.DiagnosticsLogger
import com.lighthousepark.intervalsgym.data.IntervalsUseCaseFactory
import com.lighthousepark.intervalsgym.data.loadSavedRunningWorkoutRoutines
import com.lighthousepark.intervalsgym.data.toIntervalsGymStrengthRoutine
import com.lighthousepark.intervalsgym.running.SavedRunningWorkoutRoutine
import com.lighthousepark.intervalsgym.running.rememberHeartRateSensorState
import com.lighthousepark.intervalsgym.running.runningBlocksDiagnosticText
import com.lighthousepark.intervalsgym.running.ui.HeartRateDevicePickerDialog
import com.lighthousepark.intervalsgym.running.ui.RunningSessionScreen
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.TrainingSportType
import com.lighthousepark.intervalsgym.training.isWeightTrainingItem
import com.lighthousepark.intervalsgym.training.sportType
import com.lighthousepark.intervalsgym.training.withCyclingGraphContext
import com.lighthousepark.intervalsgym.training.withRunningGraphContext
import kotlinx.coroutines.launch

/**
 * Route owner for [ROUTE_WORKOUT_ROUTINE].
 * This displays an Intervals/local routine or result detail and starts routed strength/running execution when supported.
 * UI tests: WorkoutRoutineScreenUiTest.strengthRoutineDetail_startWorkoutInvokesStrengthStartCallback,
 * runningRoutineDetail_saveButtonPersistsExecutableRunningRoutine, runningRoutineDetail_heartRateButtonIsAccessible,
 * routineDetail_backButtonInvokesBackCallback, routineDetail_confirmDeleteInvokesRoutineDeletedCallback,
 * routineDetail_cancelDeleteDoesNotInvokeRoutineDeletedCallback,
 * localStrengthSessionDetail_exposesUploadActionWhenApiKeyExists,
 * localStrengthSessionDetail_hidesUploadActionWhenApiKeyIsBlank,
 * localRunningSessionDetail_deleteRemovesHistoryAndNavigatesBack.
 */
@Composable
internal fun WorkoutRoutineScreen(
    apiKey: String,
    routine: TrainingItem?,
    onStartStrengthRoutine: (StrengthWorkoutRoutine) -> Unit,
    onStrengthSessionUploaded: (CompletedStrengthSession) -> Unit,
    onRoutineDeleted: (TrainingItem) -> Unit,
    onBack: () -> Unit,
) {
    val screenContext = LocalContext.current
    val prefs = remember(screenContext) { screenContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    val intervalsUseCaseFactory = remember(apiKey) { IntervalsUseCaseFactory(apiKey) }
    val calendarRoutineSync = remember(intervalsUseCaseFactory, prefs) {
        intervalsUseCaseFactory.calendarRoutineSync(prefs)
    }
    val strengthSessionSync = remember(intervalsUseCaseFactory, prefs) {
        intervalsUseCaseFactory.strengthSessionSync(prefs)
    }
    val runningSessionSync = remember(intervalsUseCaseFactory, prefs) {
        intervalsUseCaseFactory.runningSessionSync(prefs)
    }
    val blocks = remember(routine) { routine?.blocks.orEmpty() }
    val graphBlocks = remember(blocks, routine?.description, routine?.name, routine?.type) {
        when (routine?.sportType()) {
            TrainingSportType.RUNNING -> blocks.withRunningGraphContext(routine.description, routine.name)
            TrainingSportType.CYCLING -> blocks.withCyclingGraphContext(routine.description)
            else -> blocks
        }
    }
    val totalSeconds = remember(blocks, routine) { blocks.sumOf { it.durationSeconds }.takeIf { it > 0 } ?: (routine?.durationSeconds ?: 0) }
    val intervalStrengthRoutine = remember(routine?.matchedStrengthRoutine, routine?.description) {
        routine?.matchedStrengthRoutine ?: routine?.description.toIntervalsGymStrengthRoutine()
    }
    var localSession by remember(routine?.matchedStrengthSession?.id) { mutableStateOf(routine?.matchedStrengthSession) }
    val isWeightTrainingItem = remember(routine, localSession, intervalStrengthRoutine) {
        localSession != null ||
            intervalStrengthRoutine != null ||
            routine?.isWeightTrainingItem() == true
    }
    val isRunningWorkoutRoutine = remember(routine, graphBlocks, isWeightTrainingItem) {
        routine?.sportType() == TrainingSportType.RUNNING &&
            routine?.isLocalOnlyRunningResult != true &&
            routine.actualRunningBlocks.isEmpty() &&
            !isWeightTrainingItem &&
            graphBlocks.isNotEmpty()
    }
    LaunchedEffect(routine?.id, graphBlocks) {
        val targetRoutine = routine ?: return@LaunchedEffect
        if (targetRoutine.sportType() == TrainingSportType.RUNNING && graphBlocks.isNotEmpty()) {
            DiagnosticsLogger.log(
                context = screenContext,
                tag = "RunningRoutine",
                message = buildString {
                    appendLine("detail opened")
                    appendLine("logFile=${DiagnosticsLogger.diagnosticLogFile(screenContext).absolutePath}")
                    appendLine("id=${targetRoutine.id}")
                    appendLine("remoteId=${targetRoutine.remoteId}")
                    appendLine("name=${targetRoutine.name}")
                    appendLine("type=${targetRoutine.type}")
                    appendLine("isRoutine=${targetRoutine.isRoutine}")
                    appendLine("durationSeconds=${targetRoutine.durationSeconds}")
                    appendLine("description=${targetRoutine.description.orEmpty().take(512).replace("\n", "\\n")}")
                    appendLine(blocks.runningBlocksDiagnosticText(label = "rawBlocks"))
                    appendLine(graphBlocks.runningBlocksDiagnosticText(label = "graphBlocks"))
                }
            )
        }
    }
    var isRunningSession by rememberSaveable(routine?.id) { mutableStateOf(false) }
    var actionUiState by remember(routine?.matchedStrengthSession?.id) {
        mutableStateOf(WorkoutRoutineActionUiState())
    }
    var savedRunningRoutines by remember(routine?.description) { mutableStateOf(loadSavedRunningWorkoutRoutines(prefs)) }
    val isSavedRunningWorkoutRoutine = remember(routine?.description, savedRunningRoutines) {
        savedRunningRoutines.hasSameInternalDescriptionAs(routine?.description)
    }
    val canUploadLocalWorkout = canUploadLocalStrengthWorkout(
        localSession = localSession,
        apiKey = apiKey,
        uploadedInThisScreen = actionUiState.uploadedInThisScreen,
        routine = routine
    )
    val localRunningGraphBlocks = remember(routine?.actualRunningBlocks) { routine?.actualRunningBlocks.orEmpty() }
    val localRunningRoutePoints = remember(routine?.actualRunningRoutePoints) { routine?.actualRunningRoutePoints.orEmpty() }
    val detailTotalSeconds = remember(routine?.durationSeconds, totalSeconds, localRunningGraphBlocks) {
        if (routine?.isLocalOnlyRunningResult == true || localRunningGraphBlocks.isNotEmpty()) {
            routine?.durationSeconds ?: localRunningGraphBlocks.sumOf { it.durationSeconds }
        } else {
            totalSeconds
        }
    }
    val heartRateState = rememberHeartRateSensorState()
    var isHeartRatePickerVisible by remember { mutableStateOf(false) }
    val heartRatePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            isHeartRatePickerVisible = true
            heartRateState.startScan()
        } else {
            heartRateState.onPermissionDenied()
        }
    }

    fun openHeartRatePicker() {
        val missingPermissions = heartRateState.missingPermissions()
        if (missingPermissions.isNotEmpty()) {
            heartRatePermissionLauncher.launch(missingPermissions)
        } else {
            isHeartRatePickerVisible = true
            heartRateState.startScan()
        }
    }

    fun deleteLocalRunningSession() {
        val deleteAction = planWorkoutRoutineLocalRunningDelete(routine) ?: return
        deleteAction.delete(runningSessionSync)
        onBack()
    }

    fun uploadLocalSession() {
        when (val uploadAction = planWorkoutRoutineLocalStrengthUpload(apiKey, localSession)) {
            null -> return
            WorkoutRoutineLocalStrengthUploadLoginRequired -> {
                actionUiState = actionUiState.withUploadLoginRequired()
            }
            is WorkoutRoutineLocalStrengthUploadReady -> {
                scope.launch {
                    actionUiState = actionUiState.withUploadStarted()
                    try {
                        val uploaded = uploadAction.upload(strengthSessionSync)
                        localSession = uploaded
                        onStrengthSessionUploaded(uploaded)
                        actionUiState = actionUiState.withUploadSucceeded()
                    } catch (error: Exception) {
                        actionUiState = actionUiState.withUploadFailed(error.message)
                    }
                }
            }
        }
    }

    fun deleteCalendarRoutine() {
        val deleteAction = planWorkoutRoutineCalendarDelete(routine) ?: return
        scope.launch {
            actionUiState = actionUiState.withDeleteStarted()
            try {
                deleteAction.delete(calendarRoutineSync)
                onRoutineDeleted(deleteAction.targetRoutine)
            } catch (error: Exception) {
                actionUiState = actionUiState.withDeleteFailed(error.message)
            }
        }
    }

    fun saveRunningWorkoutRoutine() {
        when (val saveAction = planWorkoutRoutineSaveRunningRoutine(routine, graphBlocks)) {
            WorkoutRoutineSaveRunningRoutineUnavailable -> {
                Toast.makeText(
                    screenContext,
                    saveAction.toastMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
            is WorkoutRoutineSaveRunningRoutineReady -> {
                saveAction.save(prefs)
                savedRunningRoutines = loadSavedRunningWorkoutRoutines(prefs)
                Toast.makeText(
                    screenContext,
                    saveAction.toastMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun startWorkout() {
        when (val startAction = planWorkoutRoutineStartAction(routine, graphBlocks, intervalStrengthRoutine)) {
            is WorkoutRoutineStartStrengthAction -> {
                onStartStrengthRoutine(startAction.routine)
            }
            is WorkoutRoutineStartRunningAction -> {
                DiagnosticsLogger.log(
                    context = screenContext,
                    tag = "RunningRoutine",
                    message = startAction.diagnosticDetails
                )
                isRunningSession = true
            }
            WorkoutRoutineStartUnavailable -> Unit
        }
    }

    if (isRunningSession && routine != null) {
        RunningSessionScreen(
            apiKey = apiKey,
            routineName = routine.name.ifBlank { "Running Routine" },
            blocks = graphBlocks,
            totalSeconds = totalSeconds,
            isHeartRateConnected = heartRateState.isConnected,
            heartRateBpm = heartRateState.heartRateBpm,
            heartRateSamples = heartRateState.heartRateSamples,
            onHeartRateClick = ::openHeartRatePicker,
            onBack = { isRunningSession = false },
            onWorkoutFinished = {
                isRunningSession = false
                onBack()
            }
        )
        if (isHeartRatePickerVisible) {
            HeartRateDevicePickerDialog(
                state = heartRateState,
                onDismiss = {
                    heartRateState.stopScan()
                    isHeartRatePickerVisible = false
                },
                onDeviceSelected = { device ->
                    heartRateState.connect(device)
                },
                onRescan = { heartRateState.startScan() },
                onDisconnect = { heartRateState.disconnect() }
            )
        }
        return
    }

    if (isHeartRatePickerVisible) {
        HeartRateDevicePickerDialog(
            state = heartRateState,
            onDismiss = {
                heartRateState.stopScan()
                isHeartRatePickerVisible = false
            },
            onDeviceSelected = { device ->
                heartRateState.connect(device)
            },
            onRescan = { heartRateState.startScan() },
            onDisconnect = { heartRateState.disconnect() }
        )
    }

    if (actionUiState.isDeleteConfirmVisible && routine != null) {
        WorkoutRoutineDeleteConfirmDialog(
            routine = routine,
            isDeletingRoutine = actionUiState.isDeletingRoutine,
            onDismiss = {
                actionUiState = actionUiState.dismissDeleteConfirm()
            },
            onConfirm = {
                actionUiState = actionUiState.dismissDeleteConfirm()
                deleteCalendarRoutine()
            }
        )
    }

    Scaffold(
        topBar = {
            WorkoutRoutineTopBar(
                title = routine?.name ?: "Running Routine",
                canSaveRunningWorkoutRoutine = isRunningWorkoutRoutine && !isSavedRunningWorkoutRoutine,
                canDeleteRoutine = routine?.isRoutine == true,
                isDeletingRoutine = actionUiState.isDeletingRoutine,
                canUploadLocalWorkout = canUploadLocalWorkout,
                isUploadingStrengthSession = actionUiState.isUploadingStrengthSession,
                onBack = onBack,
                onSaveRunningWorkoutRoutine = ::saveRunningWorkoutRoutine,
                onDeleteClick = { actionUiState = actionUiState.showDeleteConfirm() },
                onUploadLocalWorkout = ::uploadLocalSession
            )
        },
        bottomBar = {
            WorkoutRoutineStartActionBar(
                isStrengthRoutine = intervalStrengthRoutine != null,
                isRunningWorkoutRoutine = isRunningWorkoutRoutine,
                heartRateDeviceLabel = workoutRoutineHeartRateDeviceLabel(
                    isConnected = heartRateState.isConnected,
                    isConnecting = heartRateState.isConnecting,
                    connectedDeviceName = heartRateState.connectedDeviceName
                ),
                heartRateStatusLabel = workoutRoutineHeartRateStatusLabel(
                    isConnected = heartRateState.isConnected,
                    heartRateBpm = heartRateState.heartRateBpm
                ),
                onHeartRateClick = ::openHeartRatePicker,
                onStartWorkout = ::startWorkout
            )
        }
    ) { innerPadding ->
        if (routine == null) {
            EmptyView(message = "선택된 항목이 없습니다.")
            return@Scaffold
        }

        WorkoutRoutineDetailContent(
            routine = routine,
            detailTotalSeconds = detailTotalSeconds,
            totalSeconds = totalSeconds,
            graphBlocks = graphBlocks,
            isWeightTrainingItem = isWeightTrainingItem,
            isRunningWorkoutRoutine = isRunningWorkoutRoutine,
            intervalStrengthRoutine = intervalStrengthRoutine,
            localSession = localSession,
            uploadMessage = actionUiState.uploadMessage,
            uploadError = actionUiState.displayError,
            localRunningGraphBlocks = localRunningGraphBlocks,
            localRunningRoutePoints = localRunningRoutePoints,
            innerPadding = innerPadding,
            onDeleteLocalRunningSession = ::deleteLocalRunningSession
        )
    }
}

private fun List<SavedRunningWorkoutRoutine>.hasSameInternalDescriptionAs(description: String?): Boolean {
    val target = description.normalizedRunningRoutineDescription()
    if (target.isBlank()) return false
    return any { savedRoutine ->
        savedRoutine.description.normalizedRunningRoutineDescription() == target
    }
}

private fun String?.normalizedRunningRoutineDescription(): String {
    return orEmpty().trim()
}

package com.lighthousepark.intervalsgym.running.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.lighthousepark.intervalsgym.app.PREFS_NAME
import com.lighthousepark.intervalsgym.data.IntervalsUseCaseFactory
import com.lighthousepark.intervalsgym.running.HeartRateSample
import com.lighthousepark.intervalsgym.running.RUNNING_INCLINE_STEP_PERCENT
import com.lighthousepark.intervalsgym.running.RUNNING_SPEED_STEP_KMH
import com.lighthousepark.intervalsgym.running.RunningSessionPhase
import com.lighthousepark.intervalsgym.running.runningSessionProgressSnapshot
import com.lighthousepark.intervalsgym.training.RoutineBlock
import kotlinx.coroutines.launch

/**
 * Running execution screen launched from [WorkoutRoutineScreen].
 * Owns warmup, block progression, local result saving, optional upload prompt, and running overlay updates.
 */
@Composable
internal fun RunningSessionScreen(
    apiKey: String,
    routineName: String,
    blocks: List<RoutineBlock>,
    totalSeconds: Int,
    isHeartRateConnected: Boolean,
    heartRateBpm: Int?,
    heartRateSamples: List<HeartRateSample>,
    onHeartRateClick: () -> Unit,
    onBack: () -> Unit,
    onWorkoutFinished: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    val intervalsUseCaseFactory = remember(apiKey) { IntervalsUseCaseFactory(apiKey) }
    val runningSessionSync = remember(intervalsUseCaseFactory, prefs) {
        intervalsUseCaseFactory.runningSessionSync(prefs)
    }
    var progressUiState by rememberSaveable(routineName, saver = runningSessionProgressUiStateSaver()) {
        mutableStateOf(RunningSessionProgressUiState.initial())
    }
    val phase = progressUiState.phase
    val currentBlockIndex = progressUiState.currentBlockIndex
    val warmupStartedAtMillis = progressUiState.warmupStartedAtMillis
    val blockEndAtMillis = progressUiState.blockEndAtMillis
    val blockStartedAtMillis = progressUiState.blockStartedAtMillis
    var actualBlocksJson by rememberSaveable(routineName, blocks.size) { mutableStateOf("[]") }
    var actualBlocksState by remember(routineName, blocks.size) {
        mutableStateOf(RunningSessionActualBlocksState.restored(actualBlocksJson))
    }
    val actualBlocks = actualBlocksState.blocks
    var finishUiState by rememberSaveable(routineName, saver = runningSessionFinishUiStateSaver()) {
        mutableStateOf(RunningSessionFinishUiState())
    }
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var blinkOn by remember { mutableStateOf(false) }
    var workoutHeartRateSamples by remember(routineName) { mutableStateOf<List<HeartRateSample>>(emptyList()) }
    var targetTextOverrides by rememberSaveable(routineName, blocks.size) {
        mutableStateOf(List(blocks.size) { "" })
    }
    RunningTargetOverridesSizeEffect(
        blockCount = blocks.size,
        targetOverrides = targetTextOverrides,
        onTargetOverridesChanged = { targetTextOverrides = it }
    )
    RunningWorkoutHeartRateSamplesEffect(
        heartRateSamples = heartRateSamples,
        warmupStartedAtMillis = warmupStartedAtMillis,
        workoutHeartRateSamples = workoutHeartRateSamples,
        onWorkoutHeartRateSamplesChanged = { workoutHeartRateSamples = it }
    )
    val displayBlocks = remember(blocks, targetTextOverrides) {
        blocks.mapIndexed { index, block ->
            targetTextOverrides.getOrNull(index)
                ?.takeIf { it.isNotBlank() }
                ?.let { block.copy(targetText = it) }
                ?: block
        }
    }
    val currentBlock = displayBlocks.getOrNull(currentBlockIndex)
    val isLastBlock = currentBlockIndex == blocks.lastIndex
    val progressSnapshot = runningSessionProgressSnapshot(
        phase = phase,
        currentBlock = currentBlock,
        warmupStartedAtMillis = warmupStartedAtMillis,
        blockEndAtMillis = blockEndAtMillis,
        nowMillis = nowMillis,
        totalSeconds = totalSeconds
    )
    val warmupElapsedSeconds = progressSnapshot.warmupElapsedSeconds
    val blockRemainingSeconds = progressSnapshot.blockRemainingSeconds
    val progressSeconds = progressSnapshot.progressSeconds
    val isUrgent = progressSnapshot.isUrgent

    fun logRunningSessionEvent(
        event: String,
        details: String = "",
        throwable: Throwable? = null,
    ) {
        val progress = progressUiState
        logRunningSessionDiagnosticEvent(
            context = context,
            snapshot = RunningSessionDiagnosticSnapshot(
                routineName = routineName,
                phase = progress.phase,
                currentBlockIndex = progress.currentBlockIndex,
                blockCount = blocks.size,
                totalSeconds = totalSeconds,
                warmupStartedAtMillis = progress.warmupStartedAtMillis,
                blockStartedAtMillis = progress.blockStartedAtMillis,
                blockEndAtMillis = progress.blockEndAtMillis,
                finishedAtMillis = finishUiState.finishedAtMillis
            ),
            event = event,
            details = details,
            throwable = throwable
        )
    }

    RunningSessionStartupEffect(
        context = context,
        routineName = routineName,
        blocks = blocks,
        onLogRunningSessionEvent = ::logRunningSessionEvent
    )
    RunningWarmupTickerEffect(
        phase = phase,
        warmupStartedAtMillis = warmupStartedAtMillis,
        onNowMillisChanged = { nowMillis = it }
    )

    fun currentResultSnapshot(
        actualBlocksForSession: List<RoutineBlock> = actualBlocks,
    ): RunningSessionResultSnapshot {
        return RunningSessionResultSnapshot(
            routineName = routineName,
            startedAtMillis = warmupStartedAtMillis,
            blocks = blocks,
            actualBlocks = actualBlocksForSession,
            heartRateSamples = workoutHeartRateSamples
        )
    }

    fun updateActualBlocks(nextActualBlocks: List<RoutineBlock>): List<RoutineBlock> {
        val nextState = actualBlocksState.withBlocks(nextActualBlocks)
        actualBlocksState = nextState
        actualBlocksJson = nextState.json
        return nextState.blocks
    }

    fun recordCurrentBlock(endMillis: Long = System.currentTimeMillis()): List<RoutineBlock> {
        val action = planRunningSessionRecordBlockAction(
            actualBlocks = actualBlocks,
            currentBlock = currentBlock,
            progressUiState = progressUiState,
            endMillis = endMillis
        ) ?: return actualBlocks
        val nextActualBlocks = updateActualBlocks(action.actualBlocks)
        logRunningSessionEvent(
            event = "record block",
            details = action.diagnosticDetails
        )
        progressUiState = action.progressUiState
        return nextActualBlocks
    }

    fun finishWorkout(
        endedAtMillis: Long = System.currentTimeMillis(),
        actualBlocksForFinish: List<RoutineBlock>? = null,
    ) {
        if (phase == RunningSessionPhase.FINISHED || finishUiState.isFinishDialogVisible) return
        val actualBlocksForSession = actualBlocksForFinish ?: if (phase == RunningSessionPhase.BLOCK) {
            recordCurrentBlock(endedAtMillis)
        } else {
            actualBlocks
        }
        updateActualBlocks(actualBlocksForSession)
        val localSession = currentResultSnapshot(actualBlocksForSession).saveLocalResult(
            syncUseCase = runningSessionSync,
            endedAtMillis = endedAtMillis
        )
        logRunningSessionEvent(
            event = "finish saved local",
            details = runningFinishedLocalSessionDiagnosticDetails(
                endedAtMillis = endedAtMillis,
                localSession = localSession
            )
        )
        progressUiState = progressUiState.withFinished()
        finishUiState = finishUiState.withFinishedLocalSession(
            endedAtMillis = endedAtMillis,
            localSessionId = localSession.id
        )
        stopRunningSessionRuntime(context)
    }

    fun catchUpElapsedBlocks(observedAtMillis: Long = System.currentTimeMillis()): Boolean {
        val action = planRunningSessionCatchUpAction(
            displayBlocks = displayBlocks,
            progressUiState = progressUiState,
            actualBlocks = actualBlocks,
            observedAtMillis = observedAtMillis
        ) ?: return false
        logRunningSessionEvent(
            event = "catch up elapsed blocks",
            details = action.diagnosticDetails
        )
        updateActualBlocks(action.actualBlocks)
        progressUiState = action.progressUiState
        nowMillis = action.observedAtMillis
        action.finishedAtMillis?.let { finishedAtMillis ->
            finishWorkout(
                endedAtMillis = finishedAtMillis,
                actualBlocksForFinish = action.actualBlocks
            )
        }
        return true
    }

    fun startBlock(index: Int) {
        val startedAtMillis = System.currentTimeMillis()
        when (
            val action = planRunningSessionStartBlockAction(
                blocks = blocks,
                displayBlocks = displayBlocks,
                progressUiState = progressUiState,
                index = index,
                startedAtMillis = startedAtMillis
            )
        ) {
            RunningSessionStartBlockUnavailable -> {
                finishWorkout()
            }
            is RunningSessionStartBlockReady -> {
                progressUiState = action.progressUiState
                nowMillis = action.nowMillis
                logRunningSessionEvent(
                    event = "block started",
                    details = action.diagnosticDetails
                )
            }
        }
    }

    fun updateCurrentBlockTarget(speedDeltaKmh: Float = 0f, inclineDeltaPercent: Float = 0f) {
        val action = planRunningSessionTargetOverrideAction(
            blocks = blocks,
            displayBlocks = displayBlocks,
            targetOverrides = targetTextOverrides,
            currentBlockIndex = currentBlockIndex,
            speedDeltaKmh = speedDeltaKmh,
            inclineDeltaPercent = inclineDeltaPercent
        ) ?: return
        targetTextOverrides = action.targetOverrides
        logRunningSessionEvent(
            event = "target override",
            details = action.diagnosticDetails
        )
    }

    fun moveToNextBlock() {
        recordCurrentBlock()
        val nextIndex = currentBlockIndex + 1
        if (nextIndex < blocks.size) {
            startBlock(nextIndex)
        } else {
            finishWorkout()
        }
    }

    fun moveToPreviousBlock() {
        val action = planRunningSessionPreviousBlockAction(
            actualBlocks = actualBlocks,
            progressUiState = progressUiState
        ) ?: return
        updateActualBlocks(action.actualBlocks)
        progressUiState = action.progressUiState
        startBlock(action.previousBlockIndex)
    }

    fun handlePrimaryAction() {
        when (phase) {
            RunningSessionPhase.WARMUP -> startBlock(0)
            RunningSessionPhase.BLOCK -> moveToNextBlock()
            RunningSessionPhase.FINISHED -> onBack()
        }
    }

    fun stopWorkoutWithoutSaving() {
        logRunningSessionEvent(event = "stop without saving")
        finishUiState = finishUiState.discarded()
        stopRunningSessionRuntime(context)
        onWorkoutFinished()
    }

    fun requestWorkoutExit() {
        if (phase == RunningSessionPhase.FINISHED) {
            onWorkoutFinished()
        } else {
            finishUiState = finishUiState.withStopSaveDialogVisible(true)
        }
    }

    RunningSessionBackHandler(
        enabled = finishUiState.isExitBackHandlerEnabled,
        onBack = ::requestWorkoutExit
    )
    RunningBlockProgressEffect(
        phase = phase,
        blockStartedAtMillis = blockStartedAtMillis,
        blockEndAtMillis = blockEndAtMillis,
        currentBlockIndex = currentBlockIndex,
        currentBlockTargetText = currentBlock?.targetText,
        onNowMillisChanged = { nowMillis = it },
        onCatchUpElapsedBlocks = ::catchUpElapsedBlocks,
        isWorkoutFinished = { phase == RunningSessionPhase.FINISHED },
        onMoveToNextBlock = ::moveToNextBlock
    )
    RunningUrgentBlinkEffect(
        isUrgent = isUrgent,
        onBlinkChanged = { blinkOn = it }
    )
    RunningLastBlockAutoSaveEffect(
        phase = phase,
        currentBlockIndex = currentBlockIndex,
        blockEndAtMillis = blockEndAtMillis,
        blockCount = blocks.size,
        onLogRunningSessionEvent = ::logRunningSessionEvent,
        onCatchUpElapsedBlocks = { catchUpElapsedBlocks() }
    )
    RunningWorkoutStatusEffect(
        context = context,
        routineName = routineName,
        phase = phase,
        currentBlockIndex = currentBlockIndex,
        blockCount = blocks.size,
        currentBlock = currentBlock,
        blockEndAtMillis = blockEndAtMillis,
        warmupStartedAtMillis = warmupStartedAtMillis,
        heartRateBpm = heartRateBpm
    )
    RunningOverlayLifecycleEffect(
        context = context,
        phase = phase,
        currentBlockIndex = currentBlockIndex,
        currentBlock = currentBlock,
        isLastBlock = isLastBlock,
        blockEndAtMillis = blockEndAtMillis,
        warmupStartedAtMillis = warmupStartedAtMillis,
        heartRateBpm = heartRateBpm,
        onLogRunningSessionEvent = ::logRunningSessionEvent,
        onCatchUpElapsedBlocks = { catchUpElapsedBlocks() }
    )
    RunningOverlayActionEffect(onPrimaryAction = ::handlePrimaryAction)

    fun uploadRunningSessionAndFinish() {
        when (val uploadAction = currentResultSnapshot().planRunningSessionUpload(apiKey, finishUiState)) {
            RunningSessionUploadLoginRequired -> {
                finishUiState = finishUiState.withUploadLoginRequired()
                logRunningSessionEvent(event = "upload blocked no api key")
            }
            is RunningSessionUploadReady -> {
                logRunningSessionEvent(
                    event = "upload started",
                    details = uploadAction.startedDiagnosticDetails
                )
                scope.launch {
                    finishUiState = finishUiState.withUploadStarted()
                    try {
                        val uploadedSession = uploadAction.uploadResult(runningSessionSync)
                        finishUiState = finishUiState.withUploadSucceeded(uploadedSession.id)
                        logRunningSessionEvent(
                            event = "upload succeeded",
                            details = "localRunningSessionId=${uploadedSession.id}"
                        )
                        onWorkoutFinished()
                    } catch (error: Exception) {
                        val errorMessage = error.message ?: "업로드하지 못했습니다."
                        finishUiState = finishUiState.withUploadFailed(error.message)
                        logRunningSessionEvent(
                            event = "upload failed",
                            details = "message=$errorMessage",
                            throwable = error
                        )
                    }
                }
            }
        }
    }

    RunningSessionDialogs(
        apiKey = apiKey,
        finishUiState = finishUiState,
        onStopSaveDismiss = { finishUiState = finishUiState.withStopSaveDialogVisible(false) },
        onSave = {
            finishUiState = finishUiState.withStopSaveDialogVisible(false)
            finishWorkout()
        },
        onDiscard = ::stopWorkoutWithoutSaving,
        onUpload = ::uploadRunningSessionAndFinish,
        onUseGarmin = onWorkoutFinished
    )

    RunningSessionScaffold(
        routineName = routineName,
        phase = phase,
        finishUiState = finishUiState,
        displayBlocks = displayBlocks,
        totalSeconds = totalSeconds,
        progressSeconds = progressSeconds,
        heartRateSamples = heartRateSamples,
        isHeartRateConnected = isHeartRateConnected,
        heartRateBpm = heartRateBpm,
        warmupElapsedSeconds = warmupElapsedSeconds,
        currentBlock = currentBlock,
        currentBlockIndex = currentBlockIndex,
        blockCount = blocks.size,
        blockRemainingSeconds = blockRemainingSeconds,
        blinkOn = blinkOn,
        isLastBlock = isLastBlock,
        onHeartRateClick = onHeartRateClick,
        onBackRequested = ::requestWorkoutExit,
        onStopRequested = { finishUiState = finishUiState.withStopSaveDialogVisible(true) },
        onFinishedClose = onBack,
        onSpeedDecrease = { updateCurrentBlockTarget(speedDeltaKmh = -RUNNING_SPEED_STEP_KMH) },
        onSpeedIncrease = { updateCurrentBlockTarget(speedDeltaKmh = RUNNING_SPEED_STEP_KMH) },
        onInclineDecrease = {
            updateCurrentBlockTarget(inclineDeltaPercent = -RUNNING_INCLINE_STEP_PERCENT)
        },
        onInclineIncrease = {
            updateCurrentBlockTarget(inclineDeltaPercent = RUNNING_INCLINE_STEP_PERCENT)
        },
        onPreviousBlock = ::moveToPreviousBlock,
        onPrimaryAction = ::handlePrimaryAction
    )
}

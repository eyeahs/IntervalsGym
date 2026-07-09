package com.lighthousepark.intervalsgym.running.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lighthousepark.intervalsgym.running.HeartRateSample
import com.lighthousepark.intervalsgym.running.RunningSessionPhase
import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.training.TrainingSportType
import com.lighthousepark.intervalsgym.workout.ui.RoutineWorkoutGraphCanvas

@Composable
internal fun RunningSessionDialogs(
    apiKey: String,
    finishUiState: RunningSessionFinishUiState,
    onStopSaveDismiss: () -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onUpload: () -> Unit,
    onUseGarmin: () -> Unit,
) {
    if (finishUiState.isStopSaveDialogVisible) {
        RunningStopSaveDialog(
            onDismiss = onStopSaveDismiss,
            onSave = onSave,
            onDiscard = onDiscard
        )
    }

    if (finishUiState.isFinishDialogVisible) {
        RunningFinishUploadChoiceDialog(
            apiKey = apiKey,
            isUploading = finishUiState.isUploading,
            finishError = finishUiState.error,
            onUpload = onUpload,
            onUseGarmin = onUseGarmin
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun RunningSessionScaffold(
    routineName: String,
    phase: RunningSessionPhase,
    finishUiState: RunningSessionFinishUiState,
    displayBlocks: List<RoutineBlock>,
    totalSeconds: Int,
    progressSeconds: Int?,
    heartRateSamples: List<HeartRateSample>,
    isHeartRateConnected: Boolean,
    heartRateBpm: Int?,
    warmupElapsedSeconds: Int,
    currentBlock: RoutineBlock?,
    currentBlockIndex: Int,
    blockCount: Int,
    blockRemainingSeconds: Int,
    blinkOn: Boolean,
    isLastBlock: Boolean,
    onHeartRateClick: () -> Unit,
    onBackRequested: () -> Unit,
    onStopRequested: () -> Unit,
    onFinishedClose: () -> Unit,
    onSpeedDecrease: () -> Unit,
    onSpeedIncrease: () -> Unit,
    onInclineDecrease: () -> Unit,
    onInclineIncrease: () -> Unit,
    onPreviousBlock: () -> Unit,
    onPrimaryAction: () -> Unit,
) {
    Scaffold(
        topBar = {
            RunningSessionTopBar(
                routineName = routineName,
                phase = phase,
                isStopEnabled = finishUiState.isExitBackHandlerEnabled,
                onBack = onBackRequested,
                onStop = onStopRequested
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            val gap = 12.dp
            val routineGraphHeight = 128.dp
            val heartGraphCanvasHeight = 54.dp
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(gap)
            ) {
                RoutineWorkoutGraphCanvas(
                    blocks = displayBlocks,
                    totalSeconds = totalSeconds,
                    sportType = TrainingSportType.RUNNING,
                    height = routineGraphHeight,
                    progressSeconds = progressSeconds
                )
                HeartRateGraph(
                    samples = heartRateSamples,
                    isHeartRateConnected = isHeartRateConnected,
                    heartRateBpm = heartRateBpm,
                    onHeartRateClick = onHeartRateClick,
                    modifier = Modifier.fillMaxWidth(),
                    graphHeight = heartGraphCanvasHeight
                )
                when (phase) {
                    RunningSessionPhase.WARMUP -> RunningWarmupPanel(
                        elapsedSeconds = warmupElapsedSeconds,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    RunningSessionPhase.BLOCK -> RunningBlockPanel(
                        block = currentBlock,
                        blockIndex = currentBlockIndex,
                        blockCount = blockCount,
                        remainingSeconds = blockRemainingSeconds,
                        blinkOn = blinkOn,
                        isLastBlock = isLastBlock,
                        onSpeedDecrease = onSpeedDecrease,
                        onSpeedIncrease = onSpeedIncrease,
                        onInclineDecrease = onInclineDecrease,
                        onInclineIncrease = onInclineIncrease,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    RunningSessionPhase.FINISHED -> RunningFinishedPanel(
                        totalSeconds = totalSeconds,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        onClose = onFinishedClose
                    )
                }
                if (phase != RunningSessionPhase.FINISHED) {
                    RunningSessionActionBar(
                        phase = phase,
                        currentBlockIndex = currentBlockIndex,
                        isLastBlock = isLastBlock,
                        onPreviousBlock = onPreviousBlock,
                        onPrimaryAction = onPrimaryAction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )
                }
            }
        }
    }
}

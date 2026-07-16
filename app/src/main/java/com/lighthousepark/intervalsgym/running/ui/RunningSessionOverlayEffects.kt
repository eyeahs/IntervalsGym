package com.lighthousepark.intervalsgym.running.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.lighthousepark.intervalsgym.overlay.RunningOverlayRequests
import com.lighthousepark.intervalsgym.overlay.WorkoutStatusForegroundService
import com.lighthousepark.intervalsgym.overlay.startRunningOverlay
import com.lighthousepark.intervalsgym.overlay.startWorkoutStatusService
import com.lighthousepark.intervalsgym.overlay.stopRunningOverlay
import com.lighthousepark.intervalsgym.overlay.stopWorkoutStatusService
import com.lighthousepark.intervalsgym.running.RunningSessionPhase
import com.lighthousepark.intervalsgym.running.runningBlockDiagnosticText
import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.training.runningInclineText
import com.lighthousepark.intervalsgym.training.runningTargetSpeedText

@Composable
internal fun RunningWorkoutStatusEffect(
    context: Context,
    routineName: String,
    phase: RunningSessionPhase,
    currentBlockIndex: Int,
    blockCount: Int,
    currentBlock: RoutineBlock?,
    blockEndAtMillis: Long,
    warmupStartedAtMillis: Long,
    heartRateBpm: Int?,
) {
    val currentSpeedText = remember(currentBlock) { currentBlock?.runningTargetSpeedText().orEmpty() }
    val currentInclineText = remember(currentBlock) { currentBlock?.runningInclineText().orEmpty() }
    LaunchedEffect(
        phase,
        currentBlockIndex,
        currentBlock?.targetText,
        blockEndAtMillis,
        heartRateBpm,
        warmupStartedAtMillis
    ) {
        when (phase) {
            RunningSessionPhase.WARMUP -> startWorkoutStatusService(
                context = context,
                workoutType = WorkoutStatusForegroundService.TYPE_RUNNING,
                title = routineName,
                phaseLabel = "Warmup",
                startAtMillis = warmupStartedAtMillis,
                heartRateBpm = heartRateBpm
            )
            RunningSessionPhase.BLOCK -> {
                val detailText = listOfNotNull(
                    currentSpeedText.takeIf { it.isNotBlank() }?.let { "속도 $it" },
                    currentInclineText.takeIf { it.isNotBlank() }?.let { "경사도 $it" }
                ).joinToString(" / ")
                startWorkoutStatusService(
                    context = context,
                    workoutType = WorkoutStatusForegroundService.TYPE_RUNNING,
                    title = routineName,
                    phaseLabel = "Block ${currentBlockIndex + 1}/$blockCount",
                    detailText = detailText,
                    startAtMillis = warmupStartedAtMillis,
                    endAtMillis = blockEndAtMillis,
                    heartRateBpm = heartRateBpm
                )
            }
            RunningSessionPhase.FINISHED -> stopWorkoutStatusService(context)
        }
    }
}

@Composable
internal fun RunningOverlayLifecycleEffect(
    context: Context,
    phase: RunningSessionPhase,
    currentBlockIndex: Int,
    currentBlock: RoutineBlock?,
    isLastBlock: Boolean,
    blockEndAtMillis: Long,
    warmupStartedAtMillis: Long,
    heartRateBpm: Int?,
    onLogRunningSessionEvent: RunningSessionEventLogger,
    onCatchUpElapsedBlocks: () -> Boolean,
) {
    val currentSpeedText = remember(currentBlock) { currentBlock?.runningTargetSpeedText().orEmpty() }
    val currentInclineText = remember(currentBlock) { currentBlock?.runningInclineText().orEmpty() }
    val currentLogger by rememberUpdatedState(onLogRunningSessionEvent)
    val currentCatchUpElapsedBlocks by rememberUpdatedState(onCatchUpElapsedBlocks)
    val showRunningOverlayIfNeeded by rememberUpdatedState(
        newValue = {
            when (phase) {
                RunningSessionPhase.WARMUP -> {
                    currentLogger(
                        "overlay start",
                        "title=Warmup actionLabel=Warmup skip startAtMillis=$warmupStartedAtMillis",
                        null
                    )
                    startRunningOverlay(
                        context = context,
                        title = "Warmup",
                        endAtMillis = 0L,
                        startAtMillis = warmupStartedAtMillis,
                        actionLabel = "Warmup skip",
                        openAppOnAction = runningOverlayOpensAppOnPrimaryAction(
                            phase = phase,
                            isLastBlock = isLastBlock
                        ),
                        heartRateBpm = heartRateBpm
                    )
                }
                RunningSessionPhase.BLOCK -> {
                    val overlayTitle = if (isLastBlock) {
                        "완료"
                    } else {
                        currentBlock?.title ?: "Block ${currentBlockIndex + 1}"
                    }
                    val overlayActionLabel = if (isLastBlock) "저장" else "Block skip"
                    currentLogger(
                        "overlay start",
                        buildString {
                            appendLine("title=$overlayTitle")
                            appendLine("actionLabel=$overlayActionLabel")
                            appendLine("targetSpeed=$currentSpeedText")
                            appendLine("targetIncline=$currentInclineText")
                            appendLine("endAtMillis=$blockEndAtMillis")
                            appendLine(currentBlock?.runningBlockDiagnosticText().orEmpty())
                        },
                        null
                    )
                    startRunningOverlay(
                        context = context,
                        title = overlayTitle,
                        endAtMillis = blockEndAtMillis,
                        actionLabel = overlayActionLabel,
                        openAppOnAction = runningOverlayOpensAppOnPrimaryAction(
                            phase = phase,
                            isLastBlock = isLastBlock
                        ),
                        targetSpeed = currentSpeedText,
                        targetIncline = currentInclineText,
                        heartRateBpm = heartRateBpm
                    )
                }
                RunningSessionPhase.FINISHED -> stopRunningOverlay(context)
            }
        }
    )

    DisposableEffect(context) {
        val lifecycle = (context as? LifecycleOwner)?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    currentCatchUpElapsedBlocks()
                    showRunningOverlayIfNeeded()
                }
                Lifecycle.Event.ON_RESUME -> {
                    stopRunningOverlay(context)
                    currentCatchUpElapsedBlocks()
                }
                else -> Unit
            }
        }
        lifecycle?.addObserver(observer)
        if (lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == false) {
            showRunningOverlayIfNeeded()
        }
        onDispose {
            lifecycle?.removeObserver(observer)
            if (lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true) {
                stopRunningOverlay(context)
            }
        }
    }

    LaunchedEffect(
        phase,
        currentBlockIndex,
        currentBlock?.targetText,
        blockEndAtMillis,
        heartRateBpm,
        warmupStartedAtMillis
    ) {
        val lifecycle = (context as? LifecycleOwner)?.lifecycle
        if (lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == false) {
            showRunningOverlayIfNeeded()
        }
    }
}

@Composable
internal fun RunningOverlayActionEffect(
    actionRequestOverride: Int? = null,
    onPrimaryAction: () -> Unit,
) {
    val actionRequest = actionRequestOverride ?: RunningOverlayRequests.actionRequest
    val currentOnPrimaryAction by rememberUpdatedState(onPrimaryAction)
    var handledOverlayActionRequest by remember {
        mutableIntStateOf(actionRequest)
    }
    LaunchedEffect(actionRequest) {
        while (actionRequest > handledOverlayActionRequest) {
            handledOverlayActionRequest += 1
            currentOnPrimaryAction()
        }
    }
}

internal fun stopRunningSessionRuntime(context: Context) {
    stopRunningOverlay(context)
    stopWorkoutStatusService(context)
}

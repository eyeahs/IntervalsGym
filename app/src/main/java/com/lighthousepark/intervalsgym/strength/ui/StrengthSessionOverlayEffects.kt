package com.lighthousepark.intervalsgym.strength.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.lighthousepark.intervalsgym.overlay.RestOverlayRequests
import com.lighthousepark.intervalsgym.overlay.WorkoutStatusForegroundService
import com.lighthousepark.intervalsgym.overlay.notifyRestFinished
import com.lighthousepark.intervalsgym.overlay.requestOverlayPermissionIfNeeded
import com.lighthousepark.intervalsgym.overlay.startRestOverlay
import com.lighthousepark.intervalsgym.overlay.startStrengthSetCompleteOverlay
import com.lighthousepark.intervalsgym.overlay.startWorkoutStatusService
import com.lighthousepark.intervalsgym.overlay.stopRestOverlay
import com.lighthousepark.intervalsgym.overlay.stopWorkoutStatusService
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import kotlinx.coroutines.delay

@Composable
internal fun StrengthWorkoutStatusServiceEffect(
    context: Context,
    hasStarted: Boolean,
    sessionStartedAtMillis: Long,
    routineName: String?,
    activeExerciseTitle: String,
    restUiState: StrengthRestUiState,
) {
    LaunchedEffect(
        hasStarted,
        sessionStartedAtMillis,
        routineName,
        activeExerciseTitle,
        restUiState.remainingSeconds,
        restUiState.endAtMillis,
        restUiState.title
    ) {
        if (hasStarted && sessionStartedAtMillis > 0L) {
            val isResting = restUiState.remainingSeconds != null &&
                restUiState.endAtMillis > System.currentTimeMillis()
            startWorkoutStatusService(
                context = context,
                workoutType = WorkoutStatusForegroundService.TYPE_STRENGTH,
                title = routineName ?: "웨이트 트레이닝",
                phaseLabel = if (isResting) "휴식" else "운동 중",
                detailText = if (isResting) restUiState.title else activeExerciseTitle,
                startAtMillis = sessionStartedAtMillis,
                endAtMillis = restUiState.endAtMillis.takeIf { isResting } ?: 0L
            )
        }
    }
}

@Composable
internal fun StrengthRestCountdownEffect(
    context: Context,
    remainingSeconds: Int?,
    endAtMillis: Long,
    onRemainingSecondsChange: (Int) -> Unit,
    onRestFinished: () -> Unit,
) {
    LaunchedEffect(remainingSeconds, endAtMillis) {
        val remaining = remainingSeconds ?: return@LaunchedEffect
        val wallClockRemaining = remainingStrengthRestSeconds(
            endAtMillis = endAtMillis,
            nowMillis = System.currentTimeMillis()
        )
        if (wallClockRemaining <= 0) {
            notifyRestFinished(context)
            onRestFinished()
            return@LaunchedEffect
        }
        if (wallClockRemaining != remaining) {
            onRemainingSecondsChange(wallClockRemaining)
            return@LaunchedEffect
        }

        delay(1_000)
        val nextRemaining = remainingStrengthRestSeconds(
            endAtMillis = endAtMillis,
            nowMillis = System.currentTimeMillis()
        )
        if (nextRemaining > 0) {
            onRemainingSecondsChange(nextRemaining)
        } else {
            notifyRestFinished(context)
            onRestFinished()
        }
    }
}

internal enum class StrengthFloatingOverlayMode {
    HIDDEN,
    REST,
    SET_COMPLETE,
}

@Composable
internal fun StrengthFloatingOverlayEffect(
    context: Context,
    hasStarted: Boolean,
    isSetScreenVisible: Boolean,
    isChangingCurrentExercise: Boolean,
    restUiState: StrengthRestUiState,
    activeSetOverlayTitle: String,
) {
    val lifecycle = remember(context) { (context as? LifecycleOwner)?.lifecycle }
    var isAppInForeground by remember(lifecycle) {
        mutableStateOf(lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) ?: true)
    }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> isAppInForeground = false
                Lifecycle.Event.ON_RESUME -> isAppInForeground = true
                else -> Unit
            }
        }
        lifecycle?.addObserver(observer)
        onDispose {
            lifecycle?.removeObserver(observer)
        }
    }

    LaunchedEffect(
        hasStarted,
        isSetScreenVisible,
        isChangingCurrentExercise,
        restUiState.activeRestEventId,
        restUiState.remainingSeconds != null,
        restUiState.endAtMillis,
        restUiState.isSheetVisible,
        restUiState.title,
        activeSetOverlayTitle,
        isAppInForeground
    ) {
        when (
            strengthFloatingOverlayMode(
                hasStarted = hasStarted,
                isSetScreenVisible = isSetScreenVisible,
                isChangingCurrentExercise = isChangingCurrentExercise,
                restUiState = restUiState,
                activeSetOverlayTitle = activeSetOverlayTitle,
                isAppInForeground = isAppInForeground,
                nowMillis = System.currentTimeMillis()
            )
        ) {
            StrengthFloatingOverlayMode.HIDDEN -> stopRestOverlay(context)
            StrengthFloatingOverlayMode.REST -> {
                startRestOverlay(context, restUiState.title, restUiState.endAtMillis)
            }
            StrengthFloatingOverlayMode.SET_COMPLETE -> {
                startStrengthSetCompleteOverlay(context, activeSetOverlayTitle)
            }
        }
    }
}

internal fun strengthFloatingOverlayMode(
    hasStarted: Boolean,
    isSetScreenVisible: Boolean,
    isChangingCurrentExercise: Boolean,
    restUiState: StrengthRestUiState,
    activeSetOverlayTitle: String,
    isAppInForeground: Boolean,
    nowMillis: Long,
): StrengthFloatingOverlayMode {
    val isRestActive = hasStarted && restUiState.isActive && restUiState.endAtMillis > nowMillis
    if (isRestActive) {
        return if (!isAppInForeground || !restUiState.isSheetVisible) {
            StrengthFloatingOverlayMode.REST
        } else {
            StrengthFloatingOverlayMode.HIDDEN
        }
    }
    return if (
        hasStarted &&
        isSetScreenVisible &&
        !isChangingCurrentExercise &&
        activeSetOverlayTitle.isNotBlank() &&
        !isAppInForeground
    ) {
        StrengthFloatingOverlayMode.SET_COMPLETE
    } else {
        StrengthFloatingOverlayMode.HIDDEN
    }
}

@Composable
internal fun StrengthShowRestSheetOverlayRequestEffect(
    isRestTimerActive: Boolean,
    onShowRestSheet: () -> Unit,
) {
    LaunchedEffect(RestOverlayRequests.showSheetRequest) {
        if (RestOverlayRequests.showSheetRequest > 0 && isRestTimerActive) {
            onShowRestSheet()
        }
    }
}

@Composable
internal fun StrengthSetCompleteOverlayRequestEffect(
    canCompleteSet: Boolean,
    onCompleteSetRequest: () -> Unit,
) {
    var handledCompleteSetOverlayRequest by remember {
        mutableIntStateOf(RestOverlayRequests.completeSetRequest)
    }

    LaunchedEffect(RestOverlayRequests.completeSetRequest) {
        val request = RestOverlayRequests.completeSetRequest
        if (request <= handledCompleteSetOverlayRequest) return@LaunchedEffect
        handledCompleteSetOverlayRequest = request
        if (canCompleteSet) {
            onCompleteSetRequest()
        }
    }
}

internal fun strengthSetCompleteOverlayTitle(
    entries: List<StrengthRoutineEntry>,
    currentExerciseIndex: Int,
    currentSetIndex: Int,
): String {
    return entries.getOrNull(currentExerciseIndex)?.let { entry ->
        val nextSet = entry.records.indexOfFirst { !it.completed }
            .takeIf { it >= 0 }
            ?: currentSetIndex
        "Set ${nextSet + 1} · ${entry.title}"
    }.orEmpty()
}

internal fun requestStrengthSessionOverlayPermission(context: Context) {
    requestOverlayPermissionIfNeeded(context)
}

internal fun startStrengthRestOverlay(
    context: Context,
    title: String,
    endAtMillis: Long,
) {
    startRestOverlay(context, title, endAtMillis)
}

internal fun stopStrengthRestOverlay(context: Context) {
    stopRestOverlay(context)
}

internal fun stopStrengthSessionRuntime(context: Context) {
    stopRestOverlay(context)
    stopWorkoutStatusService(context)
}

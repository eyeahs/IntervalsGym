package com.lighthousepark.intervalsgym.strength.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
    onRemainingSecondsChange: (Int) -> Unit,
    onRestFinished: () -> Unit,
) {
    LaunchedEffect(remainingSeconds) {
        val remaining = remainingSeconds ?: return@LaunchedEffect
        if (remaining > 0) {
            delay(1_000)
            onRemainingSecondsChange(remaining - 1)
        } else {
            notifyRestFinished(context)
            onRestFinished()
        }
    }
}

@Composable
internal fun StrengthRestOverlayLifecycleEffect(
    context: Context,
    restUiState: StrengthRestUiState,
) {
    DisposableEffect(context, restUiState.endAtMillis, restUiState.title, restUiState.isSheetVisible) {
        val lifecycle = (context as? LifecycleOwner)?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (
                (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_RESUME) &&
                restUiState.endAtMillis > System.currentTimeMillis()
            ) {
                if (event == Lifecycle.Event.ON_PAUSE || !restUiState.isSheetVisible) {
                    startRestOverlay(context, restUiState.title, restUiState.endAtMillis)
                } else {
                    stopRestOverlay(context)
                }
            }
        }
        lifecycle?.addObserver(observer)
        onDispose {
            lifecycle?.removeObserver(observer)
        }
    }
}

@Composable
internal fun StrengthRestOverlayVisibilityEffect(
    context: Context,
    restUiState: StrengthRestUiState,
) {
    LaunchedEffect(restUiState.isSheetVisible, restUiState.endAtMillis, restUiState.title) {
        if (restUiState.endAtMillis > System.currentTimeMillis()) {
            if (restUiState.isSheetVisible) {
                stopRestOverlay(context)
            } else {
                startRestOverlay(context, restUiState.title, restUiState.endAtMillis)
            }
        }
    }
}

@Composable
internal fun StrengthSetCompleteOverlayVisibilityEffect(
    context: Context,
    hasStarted: Boolean,
    isSetScreenVisible: Boolean,
    isChangingCurrentExercise: Boolean,
    isResting: Boolean,
    activeSetOverlayTitle: String,
) {
    LaunchedEffect(
        hasStarted,
        isSetScreenVisible,
        isChangingCurrentExercise,
        isResting,
        activeSetOverlayTitle
    ) {
        val shouldShowSetCompleteOverlay = hasStarted &&
            isSetScreenVisible &&
            !isChangingCurrentExercise &&
            !isResting &&
            activeSetOverlayTitle.isNotBlank()
        if (shouldShowSetCompleteOverlay) {
            startStrengthSetCompleteOverlay(context, activeSetOverlayTitle)
        } else if (!isResting) {
            stopRestOverlay(context)
        }
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

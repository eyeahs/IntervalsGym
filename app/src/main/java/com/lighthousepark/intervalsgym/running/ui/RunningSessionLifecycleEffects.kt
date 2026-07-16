package com.lighthousepark.intervalsgym.running.ui

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.lighthousepark.intervalsgym.core.DiagnosticsLogger
import com.lighthousepark.intervalsgym.overlay.requestOverlayPermissionIfNeeded
import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.running.runningBlocksDiagnosticText

internal typealias RunningSessionEventLogger = (String, String, Throwable?) -> Unit

@Composable
internal fun RunningSessionStartupEffect(
    context: Context,
    routineName: String,
    blocks: List<RoutineBlock>,
    requestOverlayPermissionOnStart: Boolean,
    onLogRunningSessionEvent: RunningSessionEventLogger,
) {
    val currentLogger by rememberUpdatedState(onLogRunningSessionEvent)
    LaunchedEffect(Unit) {
        if (requestOverlayPermissionOnStart) {
            requestOverlayPermissionIfNeeded(context)
        }
        currentLogger(
            "session opened",
            buildString {
                appendLine("logFile=${DiagnosticsLogger.diagnosticLogFile(context).absolutePath}")
                appendLine(blocks.runningBlocksDiagnosticText(label = "sessionBlocks"))
                appendLine("routineName=$routineName")
            },
            null
        )
    }
}

@Composable
internal fun RunningSessionBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    BackHandler(enabled = enabled, onBack = onBack)
}

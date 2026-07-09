package com.lighthousepark.intervalsgym.strength.ui

import android.content.Context

internal fun StrengthSessionStateTransition.dispatchRestOverlaySideEffects(context: Context) {
    if (shouldRequestRestOverlayPermission) {
        requestStrengthSessionOverlayPermission(context)
    }
    when (restOverlayCommand) {
        StrengthRestOverlayCommand.NONE -> Unit
        StrengthRestOverlayCommand.START -> {
            startStrengthRestOverlay(context, state.restUiState.title, state.restUiState.endAtMillis)
        }
        StrengthRestOverlayCommand.STOP -> {
            stopStrengthRestOverlay(context)
        }
    }
}

package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.ActiveStrengthSession

internal data class StrengthSessionProgressUiState(
    val hasStarted: Boolean,
    val sessionStartedAtMillis: Long,
    val sessionElapsedSeconds: Int,
) {
    fun started(nowMillis: Long): StrengthSessionProgressUiState {
        return copy(
            hasStarted = true,
            sessionStartedAtMillis = nowMillis,
            sessionElapsedSeconds = 0
        )
    }

    fun withElapsedSeconds(elapsedSeconds: Int): StrengthSessionProgressUiState {
        return copy(sessionElapsedSeconds = elapsedSeconds.coerceAtLeast(0))
    }

    companion object {
        fun restored(
            activeSession: ActiveStrengthSession?,
            shouldStartImmediately: Boolean,
            nowMillis: Long,
        ): StrengthSessionProgressUiState {
            val restoredHasStarted = activeSession?.hasStarted ?: shouldStartImmediately
            val restoredStartedAtMillis = activeSession?.sessionStartedAtMillis
                ?.takeIf { it > 0L }
                ?: if (restoredHasStarted) nowMillis else 0L
            val elapsedSeconds = if (restoredHasStarted && restoredStartedAtMillis > 0L) {
                ((nowMillis - restoredStartedAtMillis) / 1000L).toInt().coerceAtLeast(0)
            } else {
                0
            }
            return StrengthSessionProgressUiState(
                hasStarted = restoredHasStarted,
                sessionStartedAtMillis = restoredStartedAtMillis,
                sessionElapsedSeconds = elapsedSeconds
            )
        }
    }
}

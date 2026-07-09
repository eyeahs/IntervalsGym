package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.ActiveStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthRestEvent
import com.lighthousepark.intervalsgym.strength.StrengthRestTimerSecondsResult
import com.lighthousepark.intervalsgym.strength.StrengthRestTimerStartResult

internal data class StrengthRestUiState(
    val activeRestEventId: Int?,
    val remainingSeconds: Int?,
    val endAtMillis: Long,
    val isSheetVisible: Boolean,
    val title: String,
) {
    val isActive: Boolean
        get() = remainingSeconds != null && endAtMillis > 0L

    fun withSheetVisible(isVisible: Boolean): StrengthRestUiState {
        return copy(isSheetVisible = isVisible)
    }

    fun withRemainingSeconds(remainingSeconds: Int?): StrengthRestUiState {
        return copy(remainingSeconds = remainingSeconds)
    }

    fun syncedWithActiveRestEvent(
        restEvents: List<StrengthRestEvent>,
        nowMillis: Long,
    ): StrengthRestUiState? {
        val activeId = activeRestEventId ?: return this
        val event = restEvents.firstOrNull { it.id == activeId && it.endedAtMillis == null }
            ?: return null
        val nextRemainingSeconds = ((event.targetEndAtMillis - nowMillis) / 1000L)
            .toInt()
            .coerceAtLeast(0)
        return copy(
            title = event.exerciseTitle,
            endAtMillis = event.targetEndAtMillis,
            remainingSeconds = nextRemainingSeconds,
            isSheetVisible = isSheetVisible && nextRemainingSeconds > 0
        )
    }

    fun withTimerSecondsResult(result: StrengthRestTimerSecondsResult): StrengthRestUiState {
        return copy(
            remainingSeconds = result.restRemainingSeconds,
            endAtMillis = result.restEndAtMillis
        )
    }

    companion object {
        fun inactive(): StrengthRestUiState {
            return StrengthRestUiState(
                activeRestEventId = null,
                remainingSeconds = null,
                endAtMillis = 0L,
                isSheetVisible = false,
                title = ""
            )
        }

        fun restored(
            activeSession: ActiveStrengthSession?,
            nowMillis: Long,
        ): StrengthRestUiState {
            val restoredEndAtMillis = activeSession
                ?.restEndAtMillis
                ?.takeIf { it > nowMillis }
                ?: return inactive()
            return StrengthRestUiState(
                activeRestEventId = activeSession.activeRestEventId,
                remainingSeconds = ((restoredEndAtMillis - nowMillis) / 1000L)
                    .toInt()
                    .coerceAtLeast(1),
                endAtMillis = restoredEndAtMillis,
                isSheetVisible = activeSession.isRestSheetVisible,
                title = activeSession.restTitle
            )
        }

        fun fromTimerStart(result: StrengthRestTimerStartResult): StrengthRestUiState {
            return StrengthRestUiState(
                activeRestEventId = result.activeRestEventId,
                remainingSeconds = result.restRemainingSeconds,
                endAtMillis = result.restEndAtMillis,
                isSheetVisible = result.isRestSheetVisible,
                title = result.restTitle
            )
        }
    }
}

package com.lighthousepark.intervalsgym.strength

internal data class StrengthRestEventCloseResult(
    val restEvents: List<StrengthRestEvent>,
    val activeRestEventId: Int?,
)

internal data class StrengthRestTimerStartResult(
    val restEvents: List<StrengthRestEvent>,
    val activeRestEventId: Int?,
    val restTitle: String,
    val restRemainingSeconds: Int,
    val restEndAtMillis: Long,
    val isRestSheetVisible: Boolean,
)

internal data class StrengthRestTimerSecondsResult(
    val restEvents: List<StrengthRestEvent>,
    val restRemainingSeconds: Int,
    val restEndAtMillis: Long,
)

internal fun closeActiveStrengthRestEvent(
    restEvents: List<StrengthRestEvent>,
    activeRestEventId: Int?,
    endedAtMillis: Long,
    reason: String,
): StrengthRestEventCloseResult {
    if (activeRestEventId == null) {
        return StrengthRestEventCloseResult(
            restEvents = restEvents,
            activeRestEventId = null
        )
    }
    return StrengthRestEventCloseResult(
        restEvents = restEvents.map { event ->
            if (event.id == activeRestEventId && event.endedAtMillis == null) {
                event.copy(
                    endedAtMillis = endedAtMillis,
                    endReason = reason
                )
            } else {
                event
            }
        },
        activeRestEventId = null
    )
}

internal fun startStrengthRestTimer(
    restEvents: List<StrengthRestEvent>,
    title: String,
    seconds: Int,
    nowMillis: Long,
    restEvent: StrengthRestEvent? = null,
): StrengthRestTimerStartResult? {
    if (seconds <= 0) return null
    return StrengthRestTimerStartResult(
        restEvents = restEvent?.let { restEvents + it } ?: restEvents,
        activeRestEventId = restEvent?.id,
        restTitle = title,
        restRemainingSeconds = seconds,
        restEndAtMillis = restEvent?.targetEndAtMillis ?: (nowMillis + seconds * 1000L),
        isRestSheetVisible = true
    )
}

internal fun updateStrengthRestTimerSeconds(
    restEvents: List<StrengthRestEvent>,
    activeRestEventId: Int?,
    seconds: Int,
    nowMillis: Long,
): StrengthRestTimerSecondsResult? {
    val safeSeconds = seconds.coerceAtLeast(0)
    if (safeSeconds == 0) return null
    val nextEndAtMillis = nowMillis + safeSeconds * 1000L
    return StrengthRestTimerSecondsResult(
        restEvents = restEvents.map { event ->
            if (event.id == activeRestEventId && event.endedAtMillis == null) {
                event.copy(targetEndAtMillis = nextEndAtMillis)
            } else {
                event
            }
        },
        restRemainingSeconds = safeSeconds,
        restEndAtMillis = nextEndAtMillis
    )
}

package com.lighthousepark.intervalsgym.core

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange

internal class RapidActionThrottle(
    private val throttleMillis: Long = 500L,
    private val nowMillis: () -> Long = { System.nanoTime() / 1_000_000L },
) {
    private var lastAcceptedMillis: Long? = null

    fun tryRun(action: () -> Unit): Boolean {
        val now = nowMillis()
        val lastAccepted = lastAcceptedMillis
        if (lastAccepted != null && now - lastAccepted in 0 until throttleMillis) return false
        lastAcceptedMillis = now
        action()
        return true
    }
}

internal fun Modifier.throttleRapidTaps(
    enabled: Boolean = true,
    throttleMillis: Long = 500L,
): Modifier = if (!enabled) {
    this
} else {
    pointerInput(throttleMillis) {
        var lastAcceptedTapUpMillis: Long? = null
        var shouldBlockIfTap = false
        var movedBeyondTapSlop = false
        var accumulatedMove = Offset.Zero
        val tapSlop = viewConfiguration.touchSlop

        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val changedToDown = event.changes.any { it.changedToDownIgnoreConsumed() }
                if (changedToDown) {
                    val downMillis = event.changes.firstOrNull { it.changedToDownIgnoreConsumed() }?.uptimeMillis
                        ?: event.changes.firstOrNull()?.uptimeMillis
                        ?: 0L
                    val lastAccepted = lastAcceptedTapUpMillis
                    shouldBlockIfTap = lastAccepted != null &&
                        downMillis - lastAccepted in 1 until throttleMillis
                    movedBeyondTapSlop = false
                    accumulatedMove = Offset.Zero
                }
                val eventMove = event.changes.fold(Offset.Zero) { total, change ->
                    total + change.positionChange()
                }
                accumulatedMove += eventMove
                if (accumulatedMove.getDistance() > tapSlop) {
                    movedBeyondTapSlop = true
                }
                val changedToUp = event.changes.any { it.changedToUpIgnoreConsumed() }
                if (changedToUp && shouldBlockIfTap && !movedBeyondTapSlop) {
                    event.changes.forEach { it.consume() }
                } else if (changedToUp && !movedBeyondTapSlop) {
                    lastAcceptedTapUpMillis = event.changes
                        .firstOrNull { it.changedToUpIgnoreConsumed() }
                        ?.uptimeMillis
                        ?: event.changes.firstOrNull()?.uptimeMillis
                        ?: lastAcceptedTapUpMillis
                        ?: 0L
                }
                if (event.changes.none { it.pressed }) {
                    shouldBlockIfTap = false
                    movedBeyondTapSlop = false
                    accumulatedMove = Offset.Zero
                }
            }
        }
    }
}

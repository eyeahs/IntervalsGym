package com.lighthousepark.intervalsgym.core

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange

internal fun Modifier.throttleRapidTaps(throttleMillis: Long = 500L): Modifier = pointerInput(throttleMillis) {
    var lastAcceptedTapUpMillis = 0L
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
                shouldBlockIfTap = downMillis - lastAcceptedTapUpMillis in 1 until throttleMillis
                movedBeyondTapSlop = false
                accumulatedMove = Offset.Zero
            }
            val eventMove = event.changes.fold(Offset.Zero) { total, change -> total + change.positionChange() }
            accumulatedMove += eventMove
            if (accumulatedMove.getDistance() > tapSlop) {
                movedBeyondTapSlop = true
            }
            val changedToUp = event.changes.any { it.changedToUpIgnoreConsumed() }
            if (changedToUp && shouldBlockIfTap && !movedBeyondTapSlop) {
                event.changes.forEach { it.consume() }
            } else if (changedToUp && !movedBeyondTapSlop) {
                lastAcceptedTapUpMillis = event.changes.firstOrNull { it.changedToUpIgnoreConsumed() }?.uptimeMillis
                    ?: event.changes.firstOrNull()?.uptimeMillis
                    ?: lastAcceptedTapUpMillis
            }
            if (event.changes.none { it.pressed }) {
                shouldBlockIfTap = false
                movedBeyondTapSlop = false
                accumulatedMove = Offset.Zero
            }
        }
    }
}

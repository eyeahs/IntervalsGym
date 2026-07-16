package com.lighthousepark.intervalsgym.running.ui

import com.lighthousepark.intervalsgym.running.RunningSessionPhase

internal fun runningOverlayOpensAppOnPrimaryAction(
    phase: RunningSessionPhase,
    isLastBlock: Boolean,
): Boolean {
    return phase == RunningSessionPhase.BLOCK && isLastBlock
}

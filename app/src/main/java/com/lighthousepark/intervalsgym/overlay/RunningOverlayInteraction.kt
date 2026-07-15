package com.lighthousepark.intervalsgym.overlay

internal enum class RunningOverlayTapTarget {
    CONTENT,
    PRIMARY_ACTION,
}

internal data class RunningOverlayTapPlan(
    val requestPrimaryAction: Boolean,
    val requestOpen: Boolean,
    val openApp: Boolean,
)

internal fun planRunningOverlayTap(
    target: RunningOverlayTapTarget,
    openAppOnPrimaryAction: Boolean,
): RunningOverlayTapPlan {
    return when (target) {
        RunningOverlayTapTarget.CONTENT -> RunningOverlayTapPlan(
            requestPrimaryAction = false,
            requestOpen = true,
            openApp = true
        )
        RunningOverlayTapTarget.PRIMARY_ACTION -> RunningOverlayTapPlan(
            requestPrimaryAction = true,
            requestOpen = false,
            openApp = openAppOnPrimaryAction
        )
    }
}

package com.lighthousepark.intervalsgym.running

import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.training.containsRunningSpeedTarget
import com.lighthousepark.intervalsgym.training.graphTargetSpeedKmh
import com.lighthousepark.intervalsgym.training.runningInclinePercent
import com.lighthousepark.intervalsgym.training.runningInclineText
import com.lighthousepark.intervalsgym.training.runningTargetSpeedText
import java.util.Locale

internal fun List<RoutineBlock>.runningBlocksDiagnosticText(
    label: String,
    maxBlocks: Int = 80,
): String {
    return buildString {
        appendLine("$label count=$size")
        this@runningBlocksDiagnosticText.take(maxBlocks).forEach { block ->
            appendLine(block.runningBlockDiagnosticText())
        }
        if (size > maxBlocks) {
            appendLine("... ${size - maxBlocks} more blocks omitted")
        }
    }.trimEnd()
}

internal fun RoutineBlock.runningBlockDiagnosticText(): String {
    val speedKmh = graphTargetSpeedKmh()
    val inclinePercent = runningInclinePercent()
    val flags = runningDiagnosticFlags(speedKmh, inclinePercent)
    return buildString {
        append("#")
        append(index)
        append(" title=\"")
        append(title.oneLine())
        append("\" kind=\"")
        append(kind.oneLine())
        append("\" duration=")
        append(durationSeconds)
        append("s start=")
        append(startSecond)
        append("s end=")
        append(endSecond)
        append("s recovery=")
        append(isRecovery)
        append(" target=\"")
        append(targetText.oneLine())
        append("\" speedKmh=")
        append(speedKmh.diagnosticFloat())
        append(" speedText=\"")
        append(runningTargetSpeedText())
        append("\" inclinePercent=")
        append(inclinePercent.diagnosticFloat())
        append(" inclineText=\"")
        append(runningInclineText())
        append("\"")
        if (flags.isNotEmpty()) {
            append(" flags=")
            append(flags.joinToString(","))
        }
    }
}

private fun RoutineBlock.runningDiagnosticFlags(
    speedKmh: Float?,
    inclinePercent: Float?,
): List<String> {
    return buildList {
        if (targetText.containsRunningSpeedTarget() && speedKmh == null) {
            add("speed-target-not-parsed")
        }
        if (targetText.contains("%") && speedKmh != null && inclinePercent == null) {
            add("incline-not-parsed")
        }
        if (speedKmh != null && speedKmh <= 0f) {
            add("non-positive-speed")
        }
        if (inclinePercent != null && inclinePercent !in 0f..MAX_RUNNING_INCLINE_PERCENT) {
            add("incline-out-of-range")
        }
    }
}

private fun String.oneLine(): String {
    return replace("\n", "\\n").replace("\r", "\\r")
}

private fun Float?.diagnosticFloat(): String {
    return this?.let { String.format(Locale.US, "%.2f", it) } ?: "-"
}

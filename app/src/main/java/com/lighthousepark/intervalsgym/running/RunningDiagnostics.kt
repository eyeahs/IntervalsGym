package com.lighthousepark.intervalsgym.running

import com.lighthousepark.intervalsgym.training.RoutineBlock

internal fun List<RoutineBlock>.runningBlocksDiagnosticText(
    label: String,
    maxBlocks: Int = 8,
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
        append("\"")
    }
}

private fun String.oneLine(maxChars: Int = 160): String {
    val singleLine = replace("\n", "\\n").replace("\r", "\\r")
    return if (singleLine.length <= maxChars) singleLine else singleLine.take(maxChars) + "…"
}

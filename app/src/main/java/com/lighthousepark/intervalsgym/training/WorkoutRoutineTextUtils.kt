package com.lighthousepark.intervalsgym.training

import com.lighthousepark.intervalsgym.core.formatKoreanMonthDay
import java.time.LocalDate

internal fun plannedWorkoutDeleteConfirmMessage(date: LocalDate, name: String): String {
    val dateText = date.formatKoreanMonthDay()
    return "$dateText 운동 Routine을 삭제하겠습니까?\n${name.ifBlank { "운동 Routine" }}"
}

internal fun cyclingPowerContextSequence(description: String?, blockCount: Int): List<String> {
    if (description.isNullOrBlank() || blockCount <= 0) return emptyList()

    val repeatCount = Regex("""(?m)^\s*(\d+)\s*x\s*$""", RegexOption.IGNORE_CASE)
        .find(description)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()
    val stepContexts = description.lineSequence()
        .map { line ->
            Regex("""(\d+(?:\.\d+)?)\s*%\s*(?:ftp)?""", RegexOption.IGNORE_CASE)
                .find(line)
                ?.let { "${it.groupValues[1]}%ftp" }
                ?: Regex("""(\d+(?:\.\d+)?)\s*w\b""", RegexOption.IGNORE_CASE)
                    .find(line)
                    ?.value
                    ?.trim()
        }
        .filterNotNull()
        .toList()
    if (stepContexts.isEmpty()) return emptyList()

    val repeated = repeatCount
        ?.takeIf { it > 0 }
        ?.let { count -> List(count) { stepContexts }.flatten() }
    if (repeatCount != null) {
        return repeated.takeIf { it?.size == blockCount }.orEmpty()
    }
    return when {
        stepContexts.size == blockCount -> stepContexts
        blockCount % stepContexts.size == 0 -> List(blockCount / stepContexts.size) { stepContexts }.flatten()
        else -> emptyList()
    }
}

internal fun runningTargetContextSequence(description: String?, blockCount: Int): List<String> {
    if (description.isNullOrBlank() || blockCount <= 0) return emptyList()

    val lines = description.lineSequence().toList()
    val contexts = mutableListOf<String>()
    var index = 0
    while (index < lines.size) {
        val repeatCount = lines[index].runningRepeatCount()
        if (repeatCount != null) {
            index += 1
            val group = mutableListOf<String>()
            while (index < lines.size) {
                val line = lines[index]
                if (line.runningRepeatCount() != null || line.isWorkoutSectionHeader()) break
                val stepContext = line.runningStepContext()
                if (stepContext != null) {
                    group += stepContext
                    index += 1
                    continue
                }
                if (group.isNotEmpty() && line.isBlank()) break
                index += 1
            }
            if (repeatCount > 0 && group.isNotEmpty()) {
                repeat(repeatCount) { contexts += group }
            }
            continue
        }

        lines[index].runningStepContext()?.let { contexts += it }
        index += 1
    }
    return contexts.takeIf { it.size == blockCount }.orEmpty()
}

private fun String.runningRepeatCount(): Int? {
    return Regex("""^\s*(\d+)\s*x\s*$""", RegexOption.IGNORE_CASE)
        .find(this)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()
}

private fun String.isWorkoutSectionHeader(): Boolean {
    return trimStart().startsWith("#")
}

private fun String.runningStepContext(): String? {
    val stepBody = Regex("""^\s*(?:[-*+]\s+|\d+[.)]\s+)(.+)$""")
        .find(this)
        ?.groupValues
        ?.getOrNull(1)
        ?.trim()
        ?: return null
    val bracketContexts = Regex("""[\[(]([^\]\)]*)[\])]""")
        .findAll(stepBody)
        .map { it.groupValues[1].trim() }
        .toList()
    val bracketContext = bracketContexts.firstOrNull { context ->
        context.containsRunningSpeedTarget()
    } ?: bracketContexts.firstOrNull { context ->
        Regex("""\d+(?:\.\d+)?\s*%""").containsMatchIn(context)
    }
    return bracketContext ?: stepBody.runningPaceOrSpeedContext().orEmpty()
}

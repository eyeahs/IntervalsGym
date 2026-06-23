package com.lighthousepark.intervalsgym

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun plannedWorkoutDeleteConfirmMessage(date: LocalDate, name: String): String {
    val dateText = date.format(DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN))
    return "$dateText 운동 계획을 삭제하겠습니까?\n${name.ifBlank { "운동 Plan" }}"
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

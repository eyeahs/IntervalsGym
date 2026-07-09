package com.lighthousepark.intervalsgym.training

import java.util.Locale

internal fun RoutineBlock.graphTargetWatts(sportType: TrainingSportType = TrainingSportType.OTHER): Float? {
    return graphTargetSourcesByPriority().firstNotNullOfOrNull { source ->
        source.parseGraphTargetWatts()
            ?: if (sportType == TrainingSportType.CYCLING) source.parseCyclingUnitlessWatts() else null
    }
}

internal fun String.parseGraphTargetWatts(): Float? {
    val range = Regex("""(\d+(?:\.\d+)?)\s*(?:-|–|~|to)\s*(\d+(?:\.\d+)?)\s*w\b""", RegexOption.IGNORE_CASE)
        .find(this)
        ?.let { match ->
            val start = match.groupValues[1].toFloatOrNull()
            val end = match.groupValues[2].toFloatOrNull()
            if (start != null && end != null) (start + end) / 2f else null
        }
    if (range != null) return range

    val values = Regex("""(\d+(?:\.\d+)?)\s*w\b""", RegexOption.IGNORE_CASE)
        .findAll(this)
        .mapNotNull { it.groupValues[1].toFloatOrNull() }
        .toList()
    return values.takeIf { it.isNotEmpty() }?.average()?.toFloat()
}

internal fun String.parseCyclingUnitlessWatts(): Float? {
    val range = Regex("""^\s*(\d+(?:\.\d+)?)\s*(?:-|–|~|to)\s*(\d+(?:\.\d+)?)(?=\s*(?:$|·))""", RegexOption.IGNORE_CASE)
        .find(this)
        ?.let { match ->
            val start = match.groupValues[1].toFloatOrNull()
            val end = match.groupValues[2].toFloatOrNull()
            if (start != null && end != null) (start + end) / 2f else null
        }
    if (range != null && range > 20f) return range

    val single = Regex("""^\s*(\d+(?:\.\d+)?)(?=\s*(?:$|·))""", RegexOption.IGNORE_CASE)
        .find(this)
        ?.groupValues
        ?.getOrNull(1)
        ?.toFloatOrNull()
    return single?.takeIf { it > 20f }
}

internal fun RoutineBlock.graphTargetPercent(): Float? {
    return graphTargetSourcesByPriority().firstNotNullOfOrNull { source ->
        source.parseGraphTargetPercent()
    }
}

internal fun String.parseGraphTargetPercent(): Float? {
    val range = Regex("""(\d+(?:\.\d+)?)\s*(?:-|–|~|to)\s*(\d+(?:\.\d+)?)\s*%""", RegexOption.IGNORE_CASE)
        .findAll(this)
        .firstNotNullOfOrNull { match ->
            if (percentTargetLooksLikeRunningIncline(match.range.first)) return@firstNotNullOfOrNull null
            val start = match.groupValues[1].toFloatOrNull()
            val end = match.groupValues[2].toFloatOrNull()
            if (start != null && end != null) (start + end) / 2f else null
        }
    if (range != null) return range

    val values = Regex("""(\d+(?:\.\d+)?)\s*%""", RegexOption.IGNORE_CASE)
        .findAll(this)
        .filterNot { percentTargetLooksLikeRunningIncline(it.range.first) }
        .mapNotNull { it.groupValues[1].toFloatOrNull() }
        .toList()
    return values.takeIf { it.isNotEmpty() }?.average()?.toFloat()
}

internal fun String.percentTargetLooksLikeRunningIncline(matchStart: Int): Boolean {
    val delimiterIndex = listOf(
        lastIndexOf('·', startIndex = matchStart.coerceAtLeast(0)),
        lastIndexOf('\n', startIndex = matchStart.coerceAtLeast(0)),
        lastIndexOf(';', startIndex = matchStart.coerceAtLeast(0))
    ).maxOrNull() ?: -1
    val segmentBeforePercent = substring((delimiterIndex + 1).coerceAtLeast(0), matchStart.coerceIn(0, length))
        .lowercase(Locale.KOREAN)
    return segmentBeforePercent.contains("pace") ||
        segmentBeforePercent.contains("페이스") ||
        segmentBeforePercent.contains("/km") ||
        Regex("""\d{1,2}:\d{2}""").containsMatchIn(segmentBeforePercent)
}

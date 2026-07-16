package com.lighthousepark.intervalsgym.training

import com.lighthousepark.intervalsgym.running.MAX_RUNNING_INCLINE_PERCENT
import java.util.Locale
import kotlin.math.roundToInt

private val RUNNING_INCLINE_PERCENT_REGEX = Regex(
    """(\d+(?:\.\d+)?)\s*%""",
    RegexOption.IGNORE_CASE
)
private val RUNNING_PACE_REGEX = Regex(
    """(\d{1,2}):(\d{2})\s*(?:/km|pace)?""",
    RegexOption.IGNORE_CASE
)
private val RUNNING_KMH_REGEX = Regex(
    """(\d+(?:\.\d+)?)\s*km\s*/?\s*h""",
    RegexOption.IGNORE_CASE
)
private val RUNNING_UNITLESS_RANGE_REGEX = Regex(
    """^\s*(\d+(?:\.\d+)?)\s*(?:-|–|~|to)\s*(\d+(?:\.\d+)?)(?=\s*(?:$|·))""",
    RegexOption.IGNORE_CASE
)
private val RUNNING_BRACKET_CONTEXT_REGEX = Regex("""[\[(]([^\]\)]*)[\])]""")
private val RUNNING_KMH_RANGE_REGEX = Regex(
    """(\d+(?:\.\d+)?)\s*(?:-|–|~|to)\s*(\d+(?:\.\d+)?)\s*km\s*/?\s*h""",
    RegexOption.IGNORE_CASE
)
private val RUNNING_PACE_RANGE_REGEX = Regex(
    """(\d{1,2}):(\d{2})\s*(?:-|–|~|to)\s*(\d{1,2}):(\d{2})\s*(?:/km|pace)?""",
    RegexOption.IGNORE_CASE
)
private val RUNNING_STANDALONE_PERCENT_REGEX = Regex("""\d+(?:\.\d+)?\s*%""")

internal fun RoutineBlock.graphTargetSpeedKmh(): Float? {
    return graphTargetSourcesByPriority().firstNotNullOfOrNull { source ->
        parseGraphTargetSpeedKmh(source)
    }
}

internal fun RoutineBlock.graphTargetSource(): String {
    return listOf(targetText, title, kind).joinToString(" ")
}

internal fun RoutineBlock.graphTargetSourcesByPriority(): List<String> {
    val primary = targetText.trim()
    val fallback = listOf(title, kind)
        .joinToString(" ")
        .trim()
    return listOf(primary, fallback).filter { it.isNotBlank() }
}

internal fun RoutineBlock.runningTargetSpeedText(): String {
    val speedKmh = graphTargetSpeedKmh() ?: return ""
    return "${formatPaceFromKmh(speedKmh)} (${formatKmh(speedKmh)})"
}

internal fun RoutineBlock.runningInclineText(): String {
    val incline = runningInclinePercent() ?: return ""
    return if (incline % 1f == 0f) {
        "${incline.roundToInt()}%"
    } else {
        String.format(Locale.US, "%.1f%%", incline)
    }
}

internal fun RoutineBlock.runningInclinePercent(): Float? {
    targetText.trim().takeIf { it.isNotBlank() }?.let { primary ->
        primary.parseRunningInclinePercent()?.let { return it }
        if (primary.containsRunningSpeedTarget()) return null
    }
    val source = listOf(title, kind).joinToString(" ").trim()
    return source.parseRunningInclinePercent()
}

internal fun String.parseRunningInclinePercent(): Float? {
    val segments = runningTargetSegments()
    val values = RUNNING_INCLINE_PERCENT_REGEX
        .findAll(this)
        .filter { match ->
            val segmentIndex = segments.indexOfFirst { segment ->
                match.range.first in segment.start until segment.endExclusive
            }
            val segment = segments.getOrNull(segmentIndex)?.text.orEmpty()
            val previousSegment = segments.getOrNull(segmentIndex - 1)?.text.orEmpty()
            val nextSegment = segments.getOrNull(segmentIndex + 1)?.text.orEmpty()
            segment.hasRunningInclineCue() ||
                segment.hasRunningSpeedCue() ||
                (segment.isStandalonePercentSegment() && (
                    previousSegment.hasRunningSpeedCue() ||
                        nextSegment.hasRunningSpeedCue()
                    ))
        }
        .mapNotNull { it.groupValues[1].toFloatOrNull() }
        .filter { it in 0f..MAX_RUNNING_INCLINE_PERCENT }
        .toList()
    return values.takeIf { it.isNotEmpty() }?.average()?.toFloat()
}

internal fun String.containsRunningSpeedTarget(): Boolean {
    return RUNNING_PACE_REGEX.containsMatchIn(this) || RUNNING_KMH_REGEX.containsMatchIn(this)
}

internal fun String.windowAround(index: Int, radius: Int = 18): String {
    val start = (index - radius).coerceAtLeast(0)
    val end = (index + radius).coerceAtMost(length)
    return substring(start, end)
}

private fun RoutineBlock.parseGraphTargetSpeedKmh(source: String): Float? {
    source.parseBracketedGraphTargetKmh()?.let { return it }
    graphTargetPaceSpeedKmh(source)?.let { return it }

    val unitlessRange = RUNNING_UNITLESS_RANGE_REGEX
        .find(source)
        ?.let { match ->
            val start = match.groupValues[1].toFloatOrNull()
            val end = match.groupValues[2].toFloatOrNull()
            if (start != null && end != null) (start + end) / 2f else null
        }
    if (unitlessRange != null) {
        return if (unitlessRange <= 5f) unitlessRange * 3.6f else unitlessRange
    }

    return source.parseGraphTargetKmh()
}

private fun String.parseBracketedGraphTargetKmh(): Float? {
    return RUNNING_BRACKET_CONTEXT_REGEX
        .findAll(this)
        .firstNotNullOfOrNull { match -> match.groupValues[1].parseGraphTargetKmh() }
}

private fun String.parseGraphTargetKmh(): Float? {
    val kmhRange = RUNNING_KMH_RANGE_REGEX
        .find(this)
        ?.let { match ->
            val start = match.groupValues[1].toFloatOrNull()
            val end = match.groupValues[2].toFloatOrNull()
            if (start != null && end != null) (start + end) / 2f else null
        }
    if (kmhRange != null) return kmhRange

    val kmhValues = RUNNING_KMH_REGEX
        .findAll(this)
        .mapNotNull { it.groupValues[1].toFloatOrNull() }
        .toList()
    return kmhValues.takeIf { it.isNotEmpty() }?.average()?.toFloat()
}

private fun RoutineBlock.graphTargetPaceSpeedKmh(source: String): Float? {
    val paceRange = RUNNING_PACE_RANGE_REGEX
        .find(source)
        ?.let { match ->
            val start = match.groupValues[1].toIntOrNull()?.let { minutes ->
                match.groupValues[2].toIntOrNull()?.let { seconds -> minutes * 60 + seconds }
            }
            val end = match.groupValues[3].toIntOrNull()?.let { minutes ->
                match.groupValues[4].toIntOrNull()?.let { seconds -> minutes * 60 + seconds }
            }
            if (start != null && end != null) (start + end) / 2f else null
        }
    if (paceRange != null && paceRange > 0f) return 3600f / paceRange

    val paceValues = RUNNING_PACE_REGEX
        .findAll(source)
        .mapNotNull { match ->
            val minutes = match.groupValues[1].toIntOrNull()
            val seconds = match.groupValues[2].toIntOrNull()
            if (minutes != null && seconds != null) minutes * 60 + seconds else null
        }
        .filter { it > 0 }
        .toList()
    return paceValues.takeIf { it.isNotEmpty() }
        ?.average()
        ?.let { 3600f / it.toFloat() }
}

private data class RunningTargetSegment(
    val start: Int,
    val endExclusive: Int,
    val text: String,
)

private fun String.runningTargetSegments(): List<RunningTargetSegment> {
    val segments = mutableListOf<RunningTargetSegment>()
    var segmentStart = 0
    forEachIndexed { index, char ->
        if (char == '·' || char == '\n' || char == ';') {
            addRunningTargetSegment(segments, segmentStart, index)
            segmentStart = index + 1
        }
    }
    addRunningTargetSegment(segments, segmentStart, length)
    return segments
}

private fun String.addRunningTargetSegment(
    segments: MutableList<RunningTargetSegment>,
    start: Int,
    endExclusive: Int,
) {
    val text = substring(start.coerceIn(0, length), endExclusive.coerceIn(start, length)).trim()
    if (text.isNotBlank()) {
        segments += RunningTargetSegment(
            start = start,
            endExclusive = endExclusive,
            text = text
        )
    }
}

private fun String.hasRunningInclineCue(): Boolean {
    return contains("incline", ignoreCase = true) ||
        contains("grade", ignoreCase = true) ||
        contains("경사")
}

private fun String.hasRunningSpeedCue(): Boolean {
    return contains("pace", ignoreCase = true) ||
        contains("페이스") ||
        RUNNING_PACE_REGEX.containsMatchIn(this) ||
        RUNNING_KMH_REGEX.containsMatchIn(this)
}

private fun String.isStandalonePercentSegment(): Boolean {
    return trim().matches(RUNNING_STANDALONE_PERCENT_REGEX)
}

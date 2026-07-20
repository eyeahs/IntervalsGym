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
private val RUNNING_KMH_RANGE_REGEX = Regex(
    """(\d+(?:\.\d+)?)\s*(?:-|–|~|to)\s*(\d+(?:\.\d+)?)\s*km\s*/?\s*h""",
    RegexOption.IGNORE_CASE
)
private val RUNNING_PACE_RANGE_REGEX = Regex(
    """(\d{1,2}):(\d{2})\s*(?:-|–|~|to)\s*(\d{1,2}):(\d{2})\s*(?:/km|pace)?""",
    RegexOption.IGNORE_CASE
)
private val RUNNING_STANDALONE_PERCENT_REGEX = Regex("""\d+(?:\.\d+)?\s*%""")

internal data class RunningTargetDisplay(
    val speedKmh: Float,
    val paceText: String,
    val speedText: String,
)

internal fun RoutineBlock.graphTargetSpeedKmh(): Float? {
    return runningTargetDisplay()?.speedKmh
}

internal fun RoutineBlock.runningTargetDisplay(): RunningTargetDisplay? {
    val target = graphTargetSourcesByPriority()
        .firstNotNullOfOrNull { source -> source.parseRunningTarget() }
        ?: return null
    return RunningTargetDisplay(
        speedKmh = target.speedKmh,
        paceText = target.authoredPaceText ?: formatPaceFromKmh(target.speedKmh),
        speedText = target.authoredSpeedText ?: formatKmh(target.speedKmh).removeSuffix("km/h")
    )
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
    val target = runningTargetDisplay() ?: return ""
    return "${target.paceText} (${target.speedText}km/h)"
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

private fun String.parseRunningTarget(): ParsedRunningTarget? {
    parseExplicitKmhTarget()?.let { return it }

    val unitlessRange = RUNNING_UNITLESS_RANGE_REGEX
        .find(this)
        ?.let { match ->
            val start = match.groupValues[1].toFloatOrNull()
            val end = match.groupValues[2].toFloatOrNull()
            if (start != null && end != null) (start + end) / 2f else null
        }
    if (unitlessRange != null) {
        val speedKmh = if (unitlessRange <= 5f) unitlessRange * 3.6f else unitlessRange
        return ParsedRunningTarget(speedKmh = speedKmh)
    }

    return parsePaceTarget()
}

private fun String.parseExplicitKmhTarget(): ParsedRunningTarget? {
    val ranges = RUNNING_KMH_RANGE_REGEX.findAll(this).mapNotNull { match ->
        val start = match.groupValues[1].toFloatOrNull()
        val end = match.groupValues[2].toFloatOrNull()
        if (start != null && end != null) {
            IndexedRunningTarget(
                range = match.range,
                target = ParsedRunningTarget(
                    speedKmh = (start + end) / 2f,
                    authoredSpeedText = "${match.groupValues[1]}-${match.groupValues[2]}"
                )
            )
        } else {
            null
        }
    }.toList()
    val singles = RUNNING_KMH_REGEX.findAll(this)
        .filter { match -> ranges.none { range -> match.range.first in range.range } }
        .mapNotNull { match ->
            match.groupValues[1].toFloatOrNull()?.let { speedKmh ->
                IndexedRunningTarget(
                    range = match.range,
                    target = ParsedRunningTarget(
                        speedKmh = speedKmh,
                        authoredSpeedText = match.groupValues[1]
                    )
                )
            }
        }
        .toList()
    return (ranges + singles).maxByOrNull { it.range.first }?.target
}

private fun String.parsePaceTarget(): ParsedRunningTarget? {
    val ranges = RUNNING_PACE_RANGE_REGEX.findAll(this).mapNotNull { match ->
        val startSeconds = match.paceSeconds(minutesIndex = 1, secondsIndex = 2)
        val endSeconds = match.paceSeconds(minutesIndex = 3, secondsIndex = 4)
        if (startSeconds != null && endSeconds != null) {
            IndexedRunningTarget(
                range = match.range,
                target = ParsedRunningTarget(
                    speedKmh = 3600f / ((startSeconds + endSeconds) / 2f),
                    authoredPaceText = "${startSeconds.formatPaceSeconds()}–${endSeconds.formatPaceSeconds()}"
                )
            )
        } else {
            null
        }
    }.toList()
    val singles = RUNNING_PACE_REGEX.findAll(this)
        .filter { match -> ranges.none { range -> match.range.first in range.range } }
        .mapNotNull { match ->
            match.paceSeconds(minutesIndex = 1, secondsIndex = 2)?.let { secondsPerKm ->
                IndexedRunningTarget(
                    range = match.range,
                    target = ParsedRunningTarget(
                        speedKmh = 3600f / secondsPerKm,
                        authoredPaceText = secondsPerKm.formatPaceSeconds()
                    )
                )
            }
        }
        .toList()
    return (ranges + singles).maxByOrNull { it.range.first }?.target
}

private fun MatchResult.paceSeconds(
    minutesIndex: Int,
    secondsIndex: Int,
): Int? {
    val minutes = groupValues[minutesIndex].toIntOrNull() ?: return null
    val seconds = groupValues[secondsIndex].toIntOrNull()?.takeIf { it in 0..59 } ?: return null
    return (minutes * 60 + seconds).takeIf { it > 0 }
}

private fun Int.formatPaceSeconds(): String {
    return String.format(Locale.US, "%d:%02d", this / 60, this % 60)
}

private data class ParsedRunningTarget(
    val speedKmh: Float,
    val authoredPaceText: String? = null,
    val authoredSpeedText: String? = null,
)

private data class IndexedRunningTarget(
    val range: IntRange,
    val target: ParsedRunningTarget,
)

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

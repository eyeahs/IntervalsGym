package com.lighthousepark.intervalsgym

import androidx.compose.ui.graphics.Color
import java.util.Locale
import kotlin.math.roundToInt

internal enum class TrainingSportType {
    RUNNING,
    CYCLING,
    STRENGTH,
    OTHER
}

internal data class PlanBlock(
    val index: Int,
    val title: String,
    val kind: String,
    val targetText: String,
    val durationSeconds: Int,
    val startSecond: Int,
    val endSecond: Int,
    val isRecovery: Boolean,
)

internal enum class WorkoutGraphUnit {
    Watts,
    Percent,
    SpeedKmh,
}

internal data class WorkoutGraphBlock(
    val block: PlanBlock,
    val value: Float,
    val unit: WorkoutGraphUnit,
    val intensityPercent: Float? = null,
)

internal fun List<PlanBlock>.toWorkoutGraphBlocks(sportType: TrainingSportType): List<WorkoutGraphBlock> {
    data class RawGraphBlock(
        val block: PlanBlock,
        val watts: Float?,
        val speedKmh: Float?,
        val percent: Float?,
    )

    val rawBlocks = map { block ->
        val watts = block.graphTargetWatts(sportType)
        val percent = block.graphTargetPercent()
        val speedKmh = if (sportType == TrainingSportType.CYCLING) null else block.graphTargetSpeedKmh()
        RawGraphBlock(block, watts, speedKmh, percent)
    }
    val inferredCyclingFtp = if (sportType == TrainingSportType.CYCLING) {
        rawBlocks.mapNotNull { raw ->
            val watts = raw.watts ?: return@mapNotNull null
            val percent = raw.percent?.takeIf { it > 0f } ?: return@mapNotNull null
            watts / (percent / 100f)
        }.takeIf { it.isNotEmpty() }?.average()?.toFloat()
            ?: rawBlocks.mapNotNull { it.watts }.maxOrNull()
    } else {
        null
    }

    return rawBlocks.map { raw ->
        val watts = raw.watts
        val percent = raw.percent
        val speedKmh = raw.speedKmh
        val cyclingIntensity = if (sportType == TrainingSportType.CYCLING) {
            percent ?: watts?.let { value ->
                inferredCyclingFtp?.takeIf { it > 0f }?.let { ftp -> value / ftp * 100f }
            }
        } else {
            percent
        }
        WorkoutGraphBlock(
            block = raw.block,
            value = watts ?: speedKmh ?: percent ?: 0f,
            unit = when {
                watts != null -> WorkoutGraphUnit.Watts
                speedKmh != null -> WorkoutGraphUnit.SpeedKmh
                percent != null -> WorkoutGraphUnit.Percent
                else -> WorkoutGraphUnit.Percent
            },
            intensityPercent = cyclingIntensity
        )
    }
}

internal fun PlanBlock.graphTargetWatts(sportType: TrainingSportType = TrainingSportType.OTHER): Float? {
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

internal fun PlanBlock.graphTargetPercent(): Float? {
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

internal fun PlanBlock.graphTargetSpeedKmh(): Float? {
    return graphTargetSourcesByPriority().firstNotNullOfOrNull { source ->
        parseGraphTargetSpeedKmh(source)
    }
}

private fun PlanBlock.parseGraphTargetSpeedKmh(source: String): Float? {
    graphTargetPaceSpeedKmh(source)?.let { return it }

    val kmhRange = Regex("""(\d+(?:\.\d+)?)\s*(?:-|–|~|to)\s*(\d+(?:\.\d+)?)\s*km\s*/?\s*h""", RegexOption.IGNORE_CASE)
        .find(source)
        ?.let { match ->
            val start = match.groupValues[1].toFloatOrNull()
            val end = match.groupValues[2].toFloatOrNull()
            if (start != null && end != null) (start + end) / 2f else null
        }
    if (kmhRange != null) return kmhRange

    val kmhValues = Regex("""(\d+(?:\.\d+)?)\s*km\s*/?\s*h""", RegexOption.IGNORE_CASE)
        .findAll(source)
        .mapNotNull { it.groupValues[1].toFloatOrNull() }
        .toList()
    if (kmhValues.isNotEmpty()) return kmhValues.average().toFloat()

    val unitlessRange = Regex("""^\s*(\d+(?:\.\d+)?)\s*(?:-|–|~|to)\s*(\d+(?:\.\d+)?)\s*$""", RegexOption.IGNORE_CASE)
        .find(targetText)
        ?.let { match ->
            val start = match.groupValues[1].toFloatOrNull()
            val end = match.groupValues[2].toFloatOrNull()
            if (start != null && end != null) (start + end) / 2f else null
        }
    if (unitlessRange != null) {
        return if (unitlessRange <= 5f) unitlessRange * 3.6f else unitlessRange
    }

    return null
}

private fun PlanBlock.graphTargetPaceSpeedKmh(source: String): Float? {
    val paceRange = Regex("""(\d{1,2}):(\d{2})\s*(?:-|–|~|to)\s*(\d{1,2}):(\d{2})\s*(?:/km|pace)?""", RegexOption.IGNORE_CASE)
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

    val paceValues = Regex("""(\d{1,2}):(\d{2})\s*(?:/km|pace)?""", RegexOption.IGNORE_CASE)
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

internal fun PlanBlock.graphTargetSource(): String {
    return listOf(targetText, title, kind).joinToString(" ")
}

internal fun PlanBlock.graphTargetSourcesByPriority(): List<String> {
    val primary = targetText.trim()
    val fallback = listOf(title, kind)
        .joinToString(" ")
        .trim()
    return listOf(primary, fallback).filter { it.isNotBlank() }
}

internal fun PlanBlock.runningTargetSpeedText(): String {
    val speedKmh = graphTargetSpeedKmh() ?: return ""
    return "${formatPaceFromKmh(speedKmh)} (${formatKmh(speedKmh)})"
}

internal fun PlanBlock.runningInclineText(): String {
    val incline = runningInclinePercent() ?: return ""
    return if (incline % 1f == 0f) {
        "${incline.roundToInt()}%"
    } else {
        String.format(Locale.US, "%.1f%%", incline)
    }
}

internal fun PlanBlock.runningInclinePercent(): Float? {
    targetText.trim().takeIf { it.isNotBlank() }?.let { primary ->
        primary.parseRunningInclinePercent()?.let { return it }
        if (primary.containsRunningSpeedTarget()) return null
    }
    val source = listOf(title, kind).joinToString(" ").trim()
    return source.parseRunningInclinePercent()
}

internal fun String.parseRunningInclinePercent(): Float? {
    val values = Regex("""(\d+(?:\.\d+)?)\s*%""", RegexOption.IGNORE_CASE)
        .findAll(this)
        .filter { match ->
            val window = windowAround(match.range.first, radius = 36)
            percentTargetLooksLikeRunningIncline(match.range.first) ||
                window.contains("incline", ignoreCase = true) ||
                window.contains("grade", ignoreCase = true) ||
                window.contains("경사") ||
                Regex("""\d{1,2}:\d{2}""").containsMatchIn(window) ||
                Regex("""\d+(?:\.\d+)?\s*km\s*/?\s*h""", RegexOption.IGNORE_CASE).containsMatchIn(window)
        }
        .mapNotNull { it.groupValues[1].toFloatOrNull() }
        .toList()
    return values.takeIf { it.isNotEmpty() }?.average()?.toFloat()
}

internal fun String.containsRunningSpeedTarget(): Boolean {
    return Regex("""\d{1,2}:\d{2}\s*(?:/km|pace)?""", RegexOption.IGNORE_CASE).containsMatchIn(this) ||
        Regex("""\d+(?:\.\d+)?\s*km\s*/?\s*h""", RegexOption.IGNORE_CASE).containsMatchIn(this)
}

internal fun String.windowAround(index: Int, radius: Int = 18): String {
    val start = (index - radius).coerceAtLeast(0)
    val end = (index + radius).coerceAtMost(length)
    return substring(start, end)
}

internal fun WorkoutGraphBlock.graphColor(
    yMax: Float,
    selectedUnit: WorkoutGraphUnit,
    sportType: TrainingSportType,
): Color {
    if (unit != selectedUnit || value <= 0f) return Color(0xFF79BEB0)
    if (sportType == TrainingSportType.CYCLING) {
        val percent = intensityPercent ?: if (yMax > 0f) value / yMax * 100f else 0f
        return cyclingPowerZoneColor(percent)
    }
    if (unit == WorkoutGraphUnit.SpeedKmh) return Color(0xFF62B8A8)
    val ratio = (value / yMax).coerceIn(0f, 1f)
    return when {
        block.isRecovery && ratio < 0.55f -> Color(0xFF70BFAF)
        ratio >= 0.72f -> Color(0xFFFF9B55)
        ratio >= 0.5f -> Color(0xFF6DBC5C)
        else -> Color(0xFF62B8A8)
    }
}

private fun cyclingPowerZoneColor(percentOfFtp: Float): Color {
    return when {
        percentOfFtp < 55f -> Color(0xFF8FCBC1)
        percentOfFtp < 76f -> Color(0xFF78C56D)
        percentOfFtp < 88f -> Color(0xFFF2D45C)
        percentOfFtp < 95f -> Color(0xFFFFBF78)
        percentOfFtp < 106f -> Color(0xFFFF9B55)
        percentOfFtp < 121f -> Color(0xFFFF6B4A)
        else -> Color(0xFFE9445F)
    }
}

internal fun Float.formatGraphAxisLabels(unit: WorkoutGraphUnit): List<String> {
    return when (unit) {
        WorkoutGraphUnit.Watts -> {
            val rounded = roundToInt()
            listOf(if (rounded == 0) "0w" else rounded.toString())
        }
        WorkoutGraphUnit.Percent -> {
            val rounded = roundToInt()
            listOf(if (rounded == 0) "0%" else "$rounded%")
        }
        WorkoutGraphUnit.SpeedKmh -> {
            if (this <= 0f) listOf("0") else listOf(formatPaceFromKmh(this), "(${formatKmh(this)})")
        }
    }
}

internal fun formatPaceFromKmh(kmh: Float): String {
    if (kmh <= 0f) return "-"
    val secondsPerKm = (3600f / kmh).roundToInt()
    val minutes = secondsPerKm / 60
    val seconds = secondsPerKm % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}

internal fun formatKmh(kmh: Float): String {
    return if (kmh % 1f == 0f) {
        "${kmh.roundToInt()}km/h"
    } else {
        String.format(Locale.US, "%.1fkm/h", kmh)
    }
}

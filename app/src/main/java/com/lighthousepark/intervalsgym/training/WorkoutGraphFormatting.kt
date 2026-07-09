package com.lighthousepark.intervalsgym.training

import androidx.compose.ui.graphics.Color
import java.util.Locale
import kotlin.math.roundToInt

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

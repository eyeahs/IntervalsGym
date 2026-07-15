package com.lighthousepark.intervalsgym.training

import androidx.compose.ui.graphics.Color
import com.lighthousepark.intervalsgym.ui.theme.AppGraphInactive
import com.lighthousepark.intervalsgym.ui.theme.AppGraphOrange1
import com.lighthousepark.intervalsgym.ui.theme.AppGraphOrange2
import com.lighthousepark.intervalsgym.ui.theme.AppGraphOrange3
import com.lighthousepark.intervalsgym.ui.theme.AppGraphOrange4
import com.lighthousepark.intervalsgym.ui.theme.AppGraphOrange5
import com.lighthousepark.intervalsgym.ui.theme.AppGraphOrange6
import com.lighthousepark.intervalsgym.ui.theme.AppGraphOrange7
import java.util.Locale
import kotlin.math.roundToInt

internal fun WorkoutGraphBlock.graphColor(
    yMax: Float,
    selectedUnit: WorkoutGraphUnit,
    sportType: TrainingSportType,
): Color {
    if (unit != selectedUnit || value <= 0f) return AppGraphInactive
    if (sportType == TrainingSportType.CYCLING) {
        val percent = intensityPercent ?: if (yMax > 0f) value / yMax * 100f else 0f
        return cyclingPowerZoneColor(percent)
    }
    if (unit == WorkoutGraphUnit.SpeedKmh) return AppGraphOrange3
    val ratio = (value / yMax).coerceIn(0f, 1f)
    return when {
        block.isRecovery && ratio < 0.55f -> AppGraphOrange1
        ratio >= 0.72f -> AppGraphOrange6
        ratio >= 0.5f -> AppGraphOrange4
        else -> AppGraphOrange2
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
        percentOfFtp < 55f -> AppGraphOrange1
        percentOfFtp < 76f -> AppGraphOrange2
        percentOfFtp < 88f -> AppGraphOrange3
        percentOfFtp < 95f -> AppGraphOrange4
        percentOfFtp < 106f -> AppGraphOrange5
        percentOfFtp < 121f -> AppGraphOrange6
        else -> AppGraphOrange7
    }
}

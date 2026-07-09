package com.lighthousepark.intervalsgym.core

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt
import org.json.JSONObject

internal fun parseDateTime(value: String?): LocalDateTime? {
    if (value.isNullOrBlank()) return null
    return runCatching {
        LocalDateTime.parse(value.take(19), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }.getOrNull()
}

internal fun JSONObject.optNullableInt(name: String): Int? {
    if (!has(name) || isNull(name)) return null
    return runCatching { optDouble(name).toInt() }.getOrNull()
}

internal fun JSONObject.optNullableLong(name: String): Long? {
    if (!has(name) || isNull(name)) return null
    return runCatching { optLong(name) }.getOrNull()
}

internal fun JSONObject.optNullableDouble(name: String): Double? {
    if (!has(name) || isNull(name)) return null
    return runCatching { optDouble(name) }.getOrNull()
}

internal fun String.urlEncode(): String = URLEncoder.encode(this, StandardCharsets.UTF_8.name())

internal fun formatDistance(meters: Double): String {
    if (meters <= 0.0) return "0 km"
    return String.format(Locale.US, "%.1f km", meters / 1000.0)
}

internal fun Double?.formatSummaryMetric(): String {
    val value = this ?: return "-"
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}

internal fun formatDuration(seconds: Int): String {
    if (seconds <= 0) return "0분"
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "${hours}시간 ${minutes}분" else "${minutes}분"
}

internal fun formatClock(seconds: Int): String {
    val safeSeconds = seconds.coerceAtLeast(0)
    val hours = safeSeconds / 3600
    val minutes = (safeSeconds % 3600) / 60
    val secs = safeSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, secs)
    }
}

internal fun formatGraphTime(seconds: Int): String {
    val safeSeconds = seconds.coerceAtLeast(0)
    val hours = safeSeconds / 3600
    val minutes = (safeSeconds % 3600) / 60
    val secs = safeSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d", hours, minutes)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, secs)
    }
}

internal fun formatWeight(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}

internal fun Double.roundedKg(): Double {
    return (this * 10.0).roundToInt() / 10.0
}

internal fun formatTargetNumber(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}

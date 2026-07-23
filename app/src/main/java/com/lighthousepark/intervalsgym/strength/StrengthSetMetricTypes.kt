package com.lighthousepark.intervalsgym.strength

internal val SET_METRIC_TYPE_OPTIONS = listOf("횟수", "시간")

internal val StrengthSetMetricType.displayLabel: String
    get() = when (this) {
        StrengthSetMetricType.REPS -> "횟수"
        StrengthSetMetricType.DURATION -> "시간"
    }

internal fun strengthSetMetricTypeForLabel(label: String): StrengthSetMetricType {
    return StrengthSetMetricType.entries.firstOrNull { it.displayLabel == label }
        ?: StrengthSetMetricType.REPS
}

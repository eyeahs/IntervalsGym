package com.lighthousepark.intervalsgym.core

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal const val ROUTINE_TIME_LABEL = "Routine"

internal val AppClockTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
internal val AppCompactClockTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HHmm")
internal val AppIntervalsClockTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
internal val AppExternalIdTimestampFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
internal val AppShortMonthDayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d")
internal val AppShortMonthDayTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d HH:mm", Locale.KOREAN)
internal val AppKoreanShortDateWeekdayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d E", Locale.KOREAN)
internal val AppKoreanMonthDayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)
internal val AppKoreanMonthDayWeekdayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일 E", Locale.KOREAN)
internal val AppKoreanYearMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN)

internal fun LocalTime.formatClockTime(): String {
    return format(AppClockTimeFormatter)
}

internal fun LocalTime.formatCompactClockTime(): String {
    return format(AppCompactClockTimeFormatter)
}

internal fun LocalTime.formatIntervalsClockTime(): String {
    return format(AppIntervalsClockTimeFormatter)
}

internal fun LocalDateTime.formatExternalIdTimestamp(): String {
    return format(AppExternalIdTimestampFormatter)
}

internal fun LocalDate.formatShortMonthDay(): String {
    return format(AppShortMonthDayFormatter)
}

internal fun LocalDateTime.formatShortMonthDayTime(): String {
    return format(AppShortMonthDayTimeFormatter)
}

internal fun LocalDate.formatKoreanShortDateWeekday(): String {
    return format(AppKoreanShortDateWeekdayFormatter)
}

internal fun LocalDate.formatKoreanMonthDay(): String {
    return format(AppKoreanMonthDayFormatter)
}

internal fun LocalDate.formatKoreanMonthDayWeekday(): String {
    return format(AppKoreanMonthDayWeekdayFormatter)
}

internal fun LocalDate.formatKoreanYearMonth(): String {
    return format(AppKoreanYearMonthFormatter)
}

internal fun String.toClockTimeOrNull(): LocalTime? {
    return runCatching { LocalTime.parse(this, AppClockTimeFormatter) }.getOrNull()
}

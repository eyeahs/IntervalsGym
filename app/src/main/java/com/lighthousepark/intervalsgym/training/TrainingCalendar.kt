package com.lighthousepark.intervalsgym.training

import com.lighthousepark.intervalsgym.core.formatKoreanShortDateWeekday
import com.lighthousepark.intervalsgym.core.formatKoreanYearMonth
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

internal enum class TrainingCalendarMode(val title: String) {
    DAY("하루 훈련"),
    WEEK("주간 훈련"),
    MONTH("월간 훈련");

    fun next(): TrainingCalendarMode {
        return when (this) {
            DAY -> WEEK
            WEEK -> MONTH
            MONTH -> DAY
        }
    }
}

internal data class TrainingDateRange(
    val start: LocalDate,
    val end: LocalDate,
)

internal fun TrainingCalendarMode.rangeForPage(baseDate: LocalDate, pageOffset: Long): TrainingDateRange {
    return when (this) {
        TrainingCalendarMode.DAY -> {
            val date = baseDate.plusDays(pageOffset)
            TrainingDateRange(start = date, end = date)
        }
        TrainingCalendarMode.WEEK -> {
            val start = baseDate
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .plusWeeks(pageOffset)
            TrainingDateRange(start = start, end = start.plusDays(6))
        }
        TrainingCalendarMode.MONTH -> {
            val start = baseDate
                .withDayOfMonth(1)
                .plusMonths(pageOffset)
            TrainingDateRange(start = start, end = start.withDayOfMonth(start.lengthOfMonth()))
        }
    }
}

internal fun TrainingCalendarMode.pageOffsetForDate(baseDate: LocalDate, date: LocalDate): Long {
    return when (this) {
        TrainingCalendarMode.DAY -> ChronoUnit.DAYS.between(baseDate, date)
        TrainingCalendarMode.WEEK -> {
            val baseWeekStart = baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val targetWeekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            ChronoUnit.WEEKS.between(baseWeekStart, targetWeekStart)
        }
        TrainingCalendarMode.MONTH -> {
            val baseMonthStart = baseDate.withDayOfMonth(1)
            val targetMonthStart = date.withDayOfMonth(1)
            ChronoUnit.MONTHS.between(baseMonthStart, targetMonthStart)
        }
    }
}

internal fun TrainingCalendarMode.dateLabel(range: TrainingDateRange): String {
    return when (this) {
        TrainingCalendarMode.DAY -> range.start.formatKoreanShortDateWeekday()
        TrainingCalendarMode.WEEK -> "${range.start.monthValue}/${range.start.dayOfMonth} - ${range.end.monthValue}/${range.end.dayOfMonth}"
        TrainingCalendarMode.MONTH -> range.start.formatKoreanYearMonth()
    }
}

internal fun TrainingDateRange.days(): List<LocalDate> {
    return (0L..ChronoUnit.DAYS.between(start, end)).map { start.plusDays(it) }
}

internal fun TrainingDateRange.monthCalendarDays(): List<LocalDate> {
    val calendarStart = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val calendarEnd = end.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    return (0L..ChronoUnit.DAYS.between(calendarStart, calendarEnd)).map { calendarStart.plusDays(it) }
}

internal fun LocalDate.toEpochMillis(): Long {
    return atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

internal fun Long.toLocalDateFromMillis(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
}

internal fun Long.toLocalDateTime(): LocalDateTime {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
}

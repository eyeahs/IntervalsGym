package com.lighthousepark.intervalsgym.training

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingCalendarTest {
    @Test
    fun calendarModeNext_cyclesThroughAllModes() {
        assertEquals(TrainingCalendarMode.WEEK, TrainingCalendarMode.DAY.next())
        assertEquals(TrainingCalendarMode.MONTH, TrainingCalendarMode.WEEK.next())
        assertEquals(TrainingCalendarMode.DAY, TrainingCalendarMode.MONTH.next())
    }

    @Test
    fun weekRangeForPage_startsOnMonday() {
        val range = TrainingCalendarMode.WEEK.rangeForPage(
            baseDate = LocalDate.of(2026, 6, 23),
            pageOffset = 0
        )

        assertEquals(LocalDate.of(2026, 6, 22), range.start)
        assertEquals(LocalDate.of(2026, 6, 28), range.end)
    }

    @Test
    fun monthCalendarDays_coversFullWeeks() {
        val days = TrainingCalendarMode.MONTH
            .rangeForPage(LocalDate.of(2026, 6, 23), pageOffset = 0)
            .monthCalendarDays()

        assertEquals(LocalDate.of(2026, 6, 1), days.first())
        assertEquals(LocalDate.of(2026, 7, 5), days.last())
        assertEquals(35, days.size)
    }

    @Test
    fun pageOffsetForDate_matchesCalendarMode() {
        val baseDate = LocalDate.of(2026, 6, 23)

        assertEquals(2, TrainingCalendarMode.DAY.pageOffsetForDate(baseDate, LocalDate.of(2026, 6, 25)))
        assertEquals(1, TrainingCalendarMode.WEEK.pageOffsetForDate(baseDate, LocalDate.of(2026, 7, 1)))
        assertEquals(1, TrainingCalendarMode.MONTH.pageOffsetForDate(baseDate, LocalDate.of(2026, 7, 15)))
    }

    @Test
    fun dateLabel_formatsEachCalendarMode() {
        val baseDate = LocalDate.of(2026, 6, 23)

        assertEquals(
            "6/23 화",
            TrainingCalendarMode.DAY.dateLabel(TrainingCalendarMode.DAY.rangeForPage(baseDate, pageOffset = 0))
        )
        assertEquals(
            "6/22 - 6/28",
            TrainingCalendarMode.WEEK.dateLabel(TrainingCalendarMode.WEEK.rangeForPage(baseDate, pageOffset = 0))
        )
        assertEquals(
            "2026년 6월",
            TrainingCalendarMode.MONTH.dateLabel(TrainingCalendarMode.MONTH.rangeForPage(baseDate, pageOffset = 0))
        )
    }
}

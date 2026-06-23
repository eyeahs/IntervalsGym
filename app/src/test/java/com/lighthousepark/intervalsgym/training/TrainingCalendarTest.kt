package com.lighthousepark.intervalsgym.training

import com.lighthousepark.intervalsgym.MainActivity
import com.lighthousepark.intervalsgym.R
import com.lighthousepark.intervalsgym.app.*
import com.lighthousepark.intervalsgym.core.*
import com.lighthousepark.intervalsgym.data.*
import com.lighthousepark.intervalsgym.login.*
import com.lighthousepark.intervalsgym.overlay.*
import com.lighthousepark.intervalsgym.running.*
import com.lighthousepark.intervalsgym.running.ui.*
import com.lighthousepark.intervalsgym.strength.*
import com.lighthousepark.intervalsgym.strength.ui.*
import com.lighthousepark.intervalsgym.training.*
import com.lighthousepark.intervalsgym.training.ui.*
import com.lighthousepark.intervalsgym.workout.ui.*

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingCalendarTest {
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
}

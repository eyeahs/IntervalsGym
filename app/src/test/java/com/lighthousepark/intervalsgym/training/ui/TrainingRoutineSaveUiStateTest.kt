package com.lighthousepark.intervalsgym.training.ui

import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingRoutineSaveUiStateTest {
    @Test
    fun openShowsSheetForTargetDateAndClearsPreviousMessage() {
        val state = TrainingRoutineSaveUiState
            .initial(
                baseDate = LocalDate.of(2026, 7, 8),
                currentTime = LocalTime.of(9, 5)
            )
            .withLocalSaved(
                targetDate = LocalDate.of(2026, 7, 8),
                targetTime = LocalTime.of(9, 5)
            )

        val opened = state.open(LocalDate.of(2026, 7, 9))

        assertTrue(opened.isSheetVisible)
        assertEquals("2026-07-09", opened.selectedDateText)
        assertEquals("09:05", opened.selectedTimeText)
        assertNull(opened.message)
        assertNull(opened.error)
    }

    @Test
    fun invalidTimeClearsMessageAndReportsTimeError() {
        val state = TrainingRoutineSaveUiState
            .initial(
                baseDate = LocalDate.of(2026, 7, 8),
                currentTime = LocalTime.of(9, 5)
            )
            .withSelectedTimeText("9am")
            .withLocalSaved(
                targetDate = LocalDate.of(2026, 7, 8),
                targetTime = LocalTime.of(9, 5)
            )

        val invalid = state.withInvalidTimeError()

        assertNull(invalid.selectedTime)
        assertNull(invalid.message)
        assertEquals("시간은 HH:mm 형식으로 입력해주세요.", invalid.error)
    }

    @Test
    fun uploadStatesKeepLocalSaveFallbackVisibleOnFailure() {
        val state = TrainingRoutineSaveUiState.initial(
            baseDate = LocalDate.of(2026, 7, 8),
            currentTime = LocalTime.of(9, 5)
        )

        val uploading = state.withUploadStarted(routineId = 42)
        val failed = uploading.withUploadFailed(
            targetDate = LocalDate.of(2026, 7, 9),
            targetTime = LocalTime.of(18, 30),
            errorMessage = "network"
        )

        assertEquals(42, uploading.savingRoutineId)
        assertEquals("Intervals.icu에 업로드 중...", uploading.message)
        assertNull(failed.savingRoutineId)
        assertEquals("7/9 18:30 로컬에 저장됨", failed.message)
        assertEquals("network", failed.error)
    }

    @Test
    fun selectedDateFallsBackWhenSavedDateTextIsInvalid() {
        val state = TrainingRoutineSaveUiState
            .initial(
                baseDate = LocalDate.of(2026, 7, 8),
                currentTime = LocalTime.of(9, 5)
            )
            .copy(selectedDateText = "not-a-date")

        assertEquals(
            LocalDate.of(2026, 7, 10),
            state.selectedDate(fallback = LocalDate.of(2026, 7, 10))
        )
    }
}

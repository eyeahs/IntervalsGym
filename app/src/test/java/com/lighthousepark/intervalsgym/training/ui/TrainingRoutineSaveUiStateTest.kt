package com.lighthousepark.intervalsgym.training.ui

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingRoutineSaveUiStateTest {
    @Test
    fun openShowsSheetForTargetDateAndClearsPreviousMessage() {
        val state = TrainingRoutineSaveUiState
            .initial(baseDate = LocalDate.of(2026, 7, 8))
            .withLocalSaved(targetDate = LocalDate.of(2026, 7, 8))

        val opened = state.open(LocalDate.of(2026, 7, 9))

        assertTrue(opened.isSheetVisible)
        assertEquals("2026-07-09", opened.selectedDateText)
        assertNull(opened.message)
        assertNull(opened.error)
    }

    @Test
    fun uploadStatesKeepLocalSaveFallbackVisibleOnFailure() {
        val state = TrainingRoutineSaveUiState.initial(baseDate = LocalDate.of(2026, 7, 8))

        val uploading = state.withUploadStarted(routineId = 42)
        val failed = uploading.withUploadFailed(
            targetDate = LocalDate.of(2026, 7, 9),
            errorMessage = "network"
        )

        assertEquals(42, uploading.savingRoutineId)
        assertEquals("Intervals.icu에 업로드 중...", uploading.message)
        assertNull(failed.savingRoutineId)
        assertEquals("7/9 로컬에 저장됨", failed.message)
        assertEquals("network", failed.error)
    }

    @Test
    fun selectedDateFallsBackWhenSavedDateTextIsInvalid() {
        val state = TrainingRoutineSaveUiState
            .initial(baseDate = LocalDate.of(2026, 7, 8))
            .copy(selectedDateText = "not-a-date")

        assertEquals(
            LocalDate.of(2026, 7, 10),
            state.selectedDate(fallback = LocalDate.of(2026, 7, 10))
        )
    }
}

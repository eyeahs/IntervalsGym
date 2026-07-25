package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.StrengthSetMetricType
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import org.junit.Assert.assertEquals
import org.junit.Test

class StrengthSessionOverlayEffectsTest {
    @Test
    fun foregroundSetScreenKeepsFloatingUiHidden() {
        assertEquals(
            StrengthFloatingOverlayMode.HIDDEN,
            overlayMode(
                restUiState = StrengthRestUiState.inactive(),
                isAppInForeground = true
            )
        )
    }

    @Test
    fun backgroundSetScreenShowsSetNavigationFloatingUi() {
        assertEquals(
            StrengthFloatingOverlayMode.SET_NAVIGATION,
            overlayMode(
                restUiState = StrengthRestUiState.inactive(),
                isAppInForeground = false
            )
        )
    }

    @Test
    fun foregroundRestNeverShowsSystemFloatingUi() {
        val visibleSheet = activeRestUiState(isSheetVisible = true)

        assertEquals(
            StrengthFloatingOverlayMode.HIDDEN,
            overlayMode(
                restUiState = visibleSheet,
                isAppInForeground = true
            )
        )
        assertEquals(
            StrengthFloatingOverlayMode.HIDDEN,
            overlayMode(
                restUiState = visibleSheet.withSheetVisible(false),
                isAppInForeground = true
            )
        )
    }

    @Test
    fun backgroundRestAlwaysWinsOverSetCompleteFloatingUi() {
        assertEquals(
            StrengthFloatingOverlayMode.REST,
            overlayMode(
                restUiState = activeRestUiState(isSheetVisible = true),
                isAppInForeground = false
            )
        )
    }

    @Test
    fun stoppedWorkoutHidesAnOtherwiseActiveRestOverlay() {
        assertEquals(
            StrengthFloatingOverlayMode.HIDDEN,
            overlayMode(
                restUiState = activeRestUiState(isSheetVisible = false),
                isAppInForeground = false,
                hasStarted = false
            )
        )
    }

    @Test
    fun rapidRestSheetTransitionsHaveOneDeterministicModePerState() {
        val activeRest = activeRestUiState(isSheetVisible = true)
        val modes = listOf(
            overlayMode(activeRest, isAppInForeground = true),
            overlayMode(activeRest.withSheetVisible(false), isAppInForeground = true),
            overlayMode(activeRest, isAppInForeground = true),
            overlayMode(StrengthRestUiState.inactive(), isAppInForeground = true)
        )

        assertEquals(
            listOf(
                StrengthFloatingOverlayMode.HIDDEN,
                StrengthFloatingOverlayMode.HIDDEN,
                StrengthFloatingOverlayMode.HIDDEN,
                StrengthFloatingOverlayMode.HIDDEN
            ),
            modes
        )
    }

    @Test
    fun setNavigationOverlayTextShowsCurrentWeightAndTargetOnSeparateLines() {
        val entry = defaultStrengthRoutines().first().entries.first().copy(
            records = defaultStrengthRoutines().first().entries.first().records.mapIndexed { index, record ->
                if (index == 0) {
                    record.copy(weightKg = "50", reps = "12", completed = false)
                } else {
                    record
                }
            }
        )
        val durationEntry = entry.copy(
            setMetricType = StrengthSetMetricType.DURATION,
            records = entry.records.map { record -> record.copy(durationSeconds = "45") }
        )

        assertEquals(
            "50kg\n12회",
            strengthSetNavigationOverlayText(listOf(entry), currentExerciseIndex = 0, currentSetIndex = 0)
        )
        assertEquals(
            "50kg\n45초",
            strengthSetNavigationOverlayText(
                listOf(durationEntry),
                currentExerciseIndex = 0,
                currentSetIndex = 0
            )
        )
    }

    private fun overlayMode(
        restUiState: StrengthRestUiState,
        isAppInForeground: Boolean,
        hasStarted: Boolean = true,
    ): StrengthFloatingOverlayMode {
        return strengthFloatingOverlayMode(
            hasStarted = hasStarted,
            isSetScreenVisible = true,
            isChangingCurrentExercise = false,
            restUiState = restUiState,
            activeSetOverlayTitle = "Set 1 · 스쿼트",
            isAppInForeground = isAppInForeground,
            nowMillis = 10_000L
        )
    }

    private fun activeRestUiState(isSheetVisible: Boolean): StrengthRestUiState {
        return StrengthRestUiState(
            activeRestEventId = 1,
            remainingSeconds = 60,
            endAtMillis = 70_000L,
            isSheetVisible = isSheetVisible,
            title = "스쿼트"
        )
    }
}

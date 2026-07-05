package com.lighthousepark.intervalsgym.workout.ui

import com.lighthousepark.intervalsgym.strength.StrengthPlanEntry
import com.lighthousepark.intervalsgym.strength.StrengthSetRecord
import com.lighthousepark.intervalsgym.strength.defaultStrengthPlanEntry
import com.lighthousepark.intervalsgym.strength.strengthExerciseCatalog
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutPlanVisualsTest {
    @Test
    fun displayWeightText_formatsBlankSimpleAndUnilateralValues() {
        assertEquals("-kg", displayWeightText(""))
        assertEquals("20kg", displayWeightText("20"))
        assertEquals("20kg", displayWeightText("20kg"))
        assertEquals("12kg", displayWeightText("좌 12kg / 우 12kg"))
        assertEquals("좌 12kg / 우 14kg", displayWeightText("좌 12kg / 우 14kg"))
    }

    @Test
    fun displayRepsText_formatsBilateralAndUnilateralValues() {
        assertEquals("-회", displayRepsText(""))
        assertEquals("8회", displayRepsText("8"))
        assertEquals("8회", displayUnilateralRepsText("좌 8회 / 우 8회"))
        assertEquals("좌 8회 / 우 10회", displayUnilateralRepsText("좌 8회 / 우 10회"))
    }

    @Test
    fun buildStrengthSetSummary_usesEntryFallbackAndUnilateralCompression() {
        val entry = unilateralLungeEntry()
        val record = StrengthSetRecord(
            id = 7,
            weightKg = "",
            reps = "",
            leftWeightKg = "12",
            leftReps = "8",
            rightWeightKg = "12",
            rightReps = "8",
            durationSeconds = "",
            restSeconds = "",
            completed = true
        )

        assertEquals("12kg x 각 8회 · 휴식 90초", buildStrengthSetSummary(entry, record))
    }

    @Test
    fun buildStrengthSetSummary_preservesAsymmetricUnilateralValues() {
        val entry = unilateralLungeEntry()
        val record = StrengthSetRecord(
            id = 8,
            weightKg = "",
            reps = "",
            leftWeightKg = "12",
            leftReps = "8",
            rightWeightKg = "14",
            rightReps = "10",
            durationSeconds = "",
            restSeconds = "75",
            completed = true
        )

        assertEquals(
            "좌 12kg / 우 14kg x 각 좌 8회 / 우 10회 · 휴식 75초",
            buildStrengthSetSummary(entry, record)
        )
    }
}

private fun unilateralLungeEntry(): StrengthPlanEntry {
    val lunge = strengthExerciseCatalog.first { it.id == "lunge" }
    return defaultStrengthPlanEntry(
        id = 1,
        exercise = lunge,
        weightKg = "",
        reps = "8",
        restSeconds = "90"
    ).copy(
        equipment = "덤벨",
        variation = "한쪽 워킹",
        targetWeightKg = "10"
    )
}

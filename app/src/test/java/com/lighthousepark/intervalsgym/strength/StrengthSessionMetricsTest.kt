package com.lighthousepark.intervalsgym.strength

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

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthSessionMetricsTest {
    @Test
    fun totalVolumeKg_countsCompletedAndEditedSets() {
        val entry = defaultStrengthRoutineEntry(
            id = 1,
            exercise = strengthExerciseCatalog.first { it.id == "bench_press" },
            weightKg = "80",
            reps = "5",
            restSeconds = "120"
        ).copy(
            records = listOf(
                StrengthSetRecord(
                    id = 1,
                    weightKg = "80",
                    reps = "5",
                    durationSeconds = "45",
                    restSeconds = "120",
                    completed = true
                ),
                StrengthSetRecord(
                    id = 2,
                    weightKg = "",
                    reps = "",
                    durationSeconds = "",
                    restSeconds = "120",
                    completed = false
                )
            )
        )

        assertEquals(400.0, listOf(entry).totalVolumeKg(), 0.01)
    }

    @Test
    fun totalDurationSeconds_usesCompletedFallbackSetDurationAndRest() {
        val entry = defaultStrengthRoutineEntry(
            id = 1,
            exercise = strengthExerciseCatalog.first { it.id == "squat" },
            weightKg = "100",
            reps = "3",
            restSeconds = "90"
        ).copy(
            records = listOf(
                StrengthSetRecord(
                    id = 1,
                    weightKg = "100",
                    reps = "3",
                    durationSeconds = "",
                    restSeconds = "90",
                    completed = true
                ),
                StrengthSetRecord(
                    id = 2,
                    weightKg = "100",
                    reps = "3",
                    durationSeconds = "30",
                    restSeconds = "90",
                    completed = true
                )
            )
        )

        assertEquals(165, listOf(entry).totalDurationSeconds())
    }

    @Test
    fun buildStrengthTcx_escapesWorkoutName() {
        val tcx = buildStrengthTcx(
            name = "A&B <Test>",
            startedAt = LocalDateTime.of(2026, 6, 23, 10, 0),
            durationSeconds = 60
        )

        assertTrue(tcx.contains("A&amp;B &lt;Test&gt;"))
    }
}

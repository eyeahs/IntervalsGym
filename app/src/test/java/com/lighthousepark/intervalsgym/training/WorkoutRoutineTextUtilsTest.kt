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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WorkoutRoutineTextUtilsTest {
    @Test
    fun plannedWorkoutDeleteConfirmMessage_formatsKoreanDateAndRoutineName() {
        val message = plannedWorkoutDeleteConfirmMessage(
            date = LocalDate.of(2026, 6, 23),
            name = "마운틴 머신 6m(20% 5km/h) * 7"
        )

        assertEquals(
            "6월 23일 운동 Routine을 삭제하겠습니까?\n마운틴 머신 6m(20% 5km/h) * 7",
            message
        )
    }

    @Test
    fun plannedWorkoutDeleteConfirmMessage_usesFallbackNameForBlankRoutine() {
        val message = plannedWorkoutDeleteConfirmMessage(
            date = LocalDate.of(2026, 6, 24),
            name = ""
        )

        assertEquals("6월 24일 운동 Routine을 삭제하겠습니까?\n운동 Routine", message)
    }

    @Test
    fun cyclingPowerContextSequence_expandsExplicitRepeats() {
        val contexts = cyclingPowerContextSequence(
            description = """
                5x
                - 10m 100%
                - 1m 50%
            """.trimIndent(),
            blockCount = 10
        )

        assertEquals(
            listOf(
                "100%ftp", "50%ftp",
                "100%ftp", "50%ftp",
                "100%ftp", "50%ftp",
                "100%ftp", "50%ftp",
                "100%ftp", "50%ftp"
            ),
            contexts
        )
    }

    @Test
    fun cyclingPowerContextSequence_keepsExistingFtpSuffixAndWattTargets() {
        val contexts = cyclingPowerContextSequence(
            description = """
                - 6m 120% FTP
                - 2m 180w
            """.trimIndent(),
            blockCount = 2
        )

        assertEquals(listOf("120%ftp", "180w"), contexts)
    }

    @Test
    fun cyclingPowerContextSequence_returnsEmptyWhenBlockCountDoesNotMatch() {
        val contexts = cyclingPowerContextSequence(
            description = """
                5x
                - 10m 100%
                - 1m 50%
            """.trimIndent(),
            blockCount = 8
        )

        assertTrue(contexts.isEmpty())
    }

    @Test
    fun runningTargetContextSequence_expandsWarmupAndRepeatedSprintSteps() {
        val contexts = runningTargetContextSequence(
            description = sprintRunDescription(),
            blockCount = 30
        )

        assertEquals("6km/h 1%", contexts[0])
        assertEquals("12km/h 1%", contexts[4])
        assertEquals("6km/h 1%", contexts[5])
        assertEquals("12km/h 1%", contexts[6])
        assertEquals("16km/h 1%", contexts[7])
        assertEquals("", contexts[8])
        assertEquals("6km/h 1%", contexts[9])
        assertEquals("16km/h 1%", contexts[27])
        assertEquals("", contexts[28])
        assertEquals("6km/h 1%", contexts[29])
    }

    @Test
    fun runningTargetContextSequence_returnsEmptyWhenBlockCountDoesNotMatch() {
        val contexts = runningTargetContextSequence(
            description = sprintRunDescription(),
            blockCount = 29
        )

        assertTrue(contexts.isEmpty())
    }

    @Test
    fun runningTargetContextSequence_prefersSpeedBracketOverEarlierEffortPercent() {
        val contexts = runningTargetContextSequence(
            description = """
                # Sprint
                - 15s 3:45 pace (RPE 95%) [16km/h 1%] All Out
            """.trimIndent(),
            blockCount = 1
        )

        assertEquals(listOf("16km/h 1%"), contexts)
    }

    private fun sprintRunDescription(): String {
        return """
            # Warmup
            - 1m 10:00 pace [6km/h 1%]
            - 1m 7:30 pace [8km/h 1%]
            - 3m 6:40 pace [9km/h 1%]
            - 2m 6:00 pace [10km/h 1%]
            - 1m 5:00 pace [12km/h 1%]
            - 1m 10:00 pace [6km/h 1%]

            # Sprint
            6x
            - 5s 5:00 pace [12km/h 1%] Ramp time
            - 15s 3:45 pace [16km/h 1%] All Out
            - 5s Rest
            - 40s 10:00 pace [6km/h 1%]
        """.trimIndent()
    }
}

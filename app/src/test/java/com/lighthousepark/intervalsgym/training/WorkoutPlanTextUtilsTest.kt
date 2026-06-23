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

class WorkoutPlanTextUtilsTest {
    @Test
    fun plannedWorkoutDeleteConfirmMessage_formatsKoreanDateAndPlanName() {
        val message = plannedWorkoutDeleteConfirmMessage(
            date = LocalDate.of(2026, 6, 23),
            name = "마운틴 머신 6m(20% 5km/h) * 7"
        )

        assertEquals(
            "6월 23일 운동 계획을 삭제하겠습니까?\n마운틴 머신 6m(20% 5km/h) * 7",
            message
        )
    }

    @Test
    fun plannedWorkoutDeleteConfirmMessage_usesFallbackNameForBlankPlan() {
        val message = plannedWorkoutDeleteConfirmMessage(
            date = LocalDate.of(2026, 6, 24),
            name = ""
        )

        assertEquals("6월 24일 운동 계획을 삭제하겠습니까?\n운동 Plan", message)
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
}

package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.running.CompletedRunningSession
import com.lighthousepark.intervalsgym.strength.totalVolumeKg
import com.lighthousepark.intervalsgym.training.TrainingItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingLocalResultMergeTest {
    @Test
    fun withLocalRunningResults_addsUnmatchedLocalWorkoutInsideRange() {
        val startedAtMillis = LocalDateTime.of(2026, 6, 23, 7, 30)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val localSession = CompletedRunningSession(
            id = "run-1",
            name = "러닝 Routine",
            startedAtMillis = startedAtMillis,
            endedAtMillis = startedAtMillis + 1_800_000L,
            durationSeconds = 1800,
            warmupSeconds = 60,
            estimatedDistanceMeters = 3000.0,
            blocks = emptyList(),
            actualBlocks = emptyList(),
            uploadedToIntervals = false
        )

        val items = emptyList<TrainingItem>().withLocalRunningResults(
            history = listOf(localSession),
            weekStart = LocalDate.of(2026, 6, 22),
            weekEnd = LocalDate.of(2026, 6, 28)
        )

        assertEquals(1, items.size)
        assertTrue(items.single().isLocalOnlyRunningResult)
        assertFalse(items.single().isRoutine)
        assertEquals(3000.0, items.single().distanceMeters ?: 0.0, 0.01)
    }

    @Test
    fun withLocalRunningResults_skipsWorkoutMatchedByRemoteResultTime() {
        val startedAt = LocalDateTime.of(2026, 6, 23, 7, 30)
        val startedAtMillis = startedAt
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val remoteResult = trainingItem(
            id = "garmin-run-1",
            remoteId = "garmin-run-1",
            type = "Run",
            startedAt = startedAt.plusMinutes(8),
            durationSeconds = 1800
        )
        val localSession = completedRunningSessionForStorage(
            id = "run-1",
            name = "러닝 Routine",
            startedAtMillis = startedAtMillis,
            endedAtMillis = startedAtMillis + 1_800_000L
        )

        val items = listOf(remoteResult).withLocalRunningResults(
            history = listOf(localSession),
            weekStart = LocalDate.of(2026, 6, 22),
            weekEnd = LocalDate.of(2026, 6, 28)
        )

        assertEquals(1, items.size)
        assertEquals(remoteResult.id, items.single().id)
        assertFalse(items.single().isLocalOnlyRunningResult)
    }

    @Test
    fun withLocalRunningResults_skipsWorkoutAlreadyRepresentedByLocalResult() {
        val startedAt = LocalDateTime.of(2026, 6, 23, 7, 30)
        val startedAtMillis = startedAt
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val existingLocalResult = trainingItem(
            id = "local-running-run-1",
            remoteId = "run-1",
            type = "Run",
            startedAt = startedAt,
            durationSeconds = 1800,
            isLocalOnlyRunningResult = true
        )
        val localSession = completedRunningSessionForStorage(
            id = "run-1",
            name = "러닝 Routine",
            startedAtMillis = startedAtMillis,
            endedAtMillis = startedAtMillis + 1_800_000L
        )

        val items = listOf(existingLocalResult).withLocalRunningResults(
            history = listOf(localSession),
            weekStart = LocalDate.of(2026, 6, 22),
            weekEnd = LocalDate.of(2026, 6, 28)
        )

        assertEquals(1, items.size)
        assertEquals(existingLocalResult.id, items.single().id)
        assertTrue(items.single().isLocalOnlyRunningResult)
    }

    @Test
    fun withLocalStrengthResults_addsUnmatchedLocalWorkoutInsideRange() {
        val startedAtMillis = LocalDateTime.of(2026, 6, 23, 19, 30)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val localSession = completedStrengthSessionForStorage(
            id = "strength-1",
            routineName = "하체",
            startedAtMillis = startedAtMillis,
            endedAtMillis = startedAtMillis + 3_600_000L
        )

        val items = emptyList<TrainingItem>().withLocalStrengthResults(
            history = listOf(localSession),
            weekStart = LocalDate.of(2026, 6, 22),
            weekEnd = LocalDate.of(2026, 6, 28)
        )

        assertEquals(1, items.size)
        assertTrue(items.single().isLocalOnlyStrengthResult)
        assertFalse(items.single().isRoutine)
        assertEquals(localSession.id, items.single().matchedStrengthSession?.id)
        assertEquals(localSession.entries.totalVolumeKg(), items.single().weightLiftedKg ?: 0.0, 0.01)
    }

    @Test
    fun withLocalStrengthResults_skipsWorkoutMatchedByRemoteExternalId() {
        val startedAt = LocalDateTime.of(2026, 6, 23, 19, 30)
        val startedAtMillis = startedAt
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val localSession = completedStrengthSessionForStorage(
            id = "strength-remote-match",
            routineName = "하체",
            startedAtMillis = startedAtMillis,
            endedAtMillis = startedAtMillis + 3_600_000L
        )
        val remoteResult = trainingItem(
            id = "intervals-strength-1",
            externalId = localSession.intervalsExternalId,
            name = "하체",
            type = "Weight Training",
            startedAt = startedAt.plusMinutes(20),
            durationSeconds = localSession.durationSeconds
        )

        val items = listOf(remoteResult).withLocalStrengthResults(
            history = listOf(localSession),
            weekStart = LocalDate.of(2026, 6, 22),
            weekEnd = LocalDate.of(2026, 6, 28)
        )

        assertEquals(1, items.size)
        assertEquals(remoteResult.id, items.single().id)
        assertFalse(items.single().isLocalOnlyStrengthResult)
        assertEquals(localSession.id, items.single().matchedStrengthSession?.id)
    }
}

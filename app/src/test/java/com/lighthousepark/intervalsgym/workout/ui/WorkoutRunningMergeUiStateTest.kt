package com.lighthousepark.intervalsgym.workout.ui

import com.lighthousepark.intervalsgym.running.INTERVALS_GARMIN_ACTIVITY_SOURCE
import com.lighthousepark.intervalsgym.running.RunningActivityMergeCandidate
import com.lighthousepark.intervalsgym.running.RunningActivityMergeMatchMethod
import com.lighthousepark.intervalsgym.running.RunningRemoteActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutRunningMergeUiStateTest {
    @Test
    fun candidates_openConfirmationWithBestCandidateSelected() {
        val candidates = listOf(candidate("i-1"), candidate("i-2"))

        val state = WorkoutRunningMergeUiState()
            .findingCandidates()
            .withCandidates(candidates)

        assertFalse(state.isFindingCandidates)
        assertTrue(state.isConfirmVisible)
        assertEquals("i-1", state.selectedCandidateId)
        assertEquals(candidates.first(), state.selectedCandidate)
    }

    @Test
    fun emptyCandidates_showActionableErrorWithoutConfirmation() {
        val state = WorkoutRunningMergeUiState()
            .findingCandidates()
            .withCandidates(emptyList())

        assertFalse(state.isConfirmVisible)
        assertTrue(state.error.orEmpty().contains("Garmin"))
        assertNull(state.selectedCandidate)
    }

    @Test
    fun mergedState_reportsDuplicateDeletion() {
        val state = WorkoutRunningMergeUiState()
            .merging()
            .merged(deletedDuplicate = true)

        assertFalse(state.isBusy)
        assertTrue(state.message.orEmpty().contains("중복 기록을 삭제"))
    }

    private fun candidate(id: String): RunningActivityMergeCandidate {
        return RunningActivityMergeCandidate(
            activity = RunningRemoteActivity(
                id = id,
                name = "Garmin Run",
                type = "Run",
                source = INTERVALS_GARMIN_ACTIVITY_SOURCE,
                externalId = null,
                startedAtMillis = 1_000L,
                durationSeconds = 60,
                description = null
            ),
            matchMethod = RunningActivityMergeMatchMethod.START_TIME,
            offsetSeconds = 0,
            heartRateCorrelation = null,
            comparedHeartRateSamples = 0,
            startDifferenceSeconds = 0,
            durationDifferenceSeconds = 0,
            duplicateActivityId = null
        )
    }
}

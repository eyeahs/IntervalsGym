package com.lighthousepark.intervalsgym.workout.ui

import com.lighthousepark.intervalsgym.data.completedStrengthSessionForStorage
import com.lighthousepark.intervalsgym.data.trainingItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutRoutineActionUiStateTest {
    @Test
    fun uploadStateTransitionsKeepMessageAndLoadingExclusive() {
        val started = WorkoutRoutineActionUiState().withUploadStarted()
        val succeeded = started.withUploadSucceeded()
        val failed = started.withUploadFailed(null)

        assertTrue(started.isUploadingStrengthSession)
        assertNull(started.uploadMessage)
        assertNull(started.uploadError)
        assertFalse(succeeded.isUploadingStrengthSession)
        assertTrue(succeeded.uploadedInThisScreen)
        assertEquals("Intervals.icu에 업로드했습니다.", succeeded.uploadMessage)
        assertNull(succeeded.uploadError)
        assertFalse(failed.isUploadingStrengthSession)
        assertEquals("업로드하지 못했습니다.", failed.uploadError)
    }

    @Test
    fun deleteStateTransitionsHideConfirmAndExposeDeleteError() {
        val started = WorkoutRoutineActionUiState()
            .showDeleteConfirm()
            .withDeleteStarted()
        val failed = started.withDeleteFailed("삭제 실패")

        assertFalse(started.isDeleteConfirmVisible)
        assertTrue(started.isDeletingRoutine)
        assertFalse(failed.isDeletingRoutine)
        assertEquals("삭제 실패", failed.deleteError)
        assertEquals("삭제 실패", failed.displayError)
    }

    @Test
    fun canUploadLocalStrengthWorkoutRequiresApiAndNotAlreadyUploadedInThisScreen() {
        val localWorkout = completedStrengthSessionForStorage(
            id = "local-strength",
            routineName = "Local",
            startedAtMillis = 1_000L,
            endedAtMillis = 2_000L
        ).copy(uploadedToIntervals = false)
        val routine = trainingItem(
            id = "local-result",
            isLocalOnlyRunningResult = false
        ).copy(
            matchedStrengthSession = localWorkout,
            isLocalOnlyStrengthResult = true
        )

        assertTrue(
            canUploadLocalStrengthWorkout(
                localSession = localWorkout,
                apiKey = "api-key",
                uploadedInThisScreen = false,
                routine = routine
            )
        )
        assertFalse(
            canUploadLocalStrengthWorkout(
                localSession = localWorkout,
                apiKey = "",
                uploadedInThisScreen = false,
                routine = routine
            )
        )
        assertFalse(
            canUploadLocalStrengthWorkout(
                localSession = localWorkout,
                apiKey = "api-key",
                uploadedInThisScreen = true,
                routine = routine
            )
        )
    }

    @Test
    fun heartRateLabelsReflectConnectionState() {
        assertEquals(
            "Polar H10",
            workoutRoutineHeartRateDeviceLabel(
                isConnected = true,
                isConnecting = false,
                connectedDeviceName = "Polar H10"
            )
        )
        assertEquals(
            "심박계",
            workoutRoutineHeartRateDeviceLabel(
                isConnected = true,
                isConnecting = false,
                connectedDeviceName = ""
            )
        )
        assertEquals(
            "연결 중",
            workoutRoutineHeartRateDeviceLabel(
                isConnected = false,
                isConnecting = true,
                connectedDeviceName = null
            )
        )
        assertEquals("142 bpm", workoutRoutineHeartRateStatusLabel(isConnected = true, heartRateBpm = 142))
        assertEquals("-- bpm", workoutRoutineHeartRateStatusLabel(isConnected = true, heartRateBpm = null))
        assertEquals("연결", workoutRoutineHeartRateStatusLabel(isConnected = false, heartRateBpm = 142))
    }
}

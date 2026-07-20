package com.lighthousepark.intervalsgym.workout.ui

import com.lighthousepark.intervalsgym.data.MemorySharedPreferences
import com.lighthousepark.intervalsgym.data.RecordingRunningSessionRemoteDataSource
import com.lighthousepark.intervalsgym.data.RecordingStrengthSessionRemoteDataSource
import com.lighthousepark.intervalsgym.data.RunningSessionSyncUseCase
import com.lighthousepark.intervalsgym.data.StrengthSessionSyncUseCase
import com.lighthousepark.intervalsgym.data.appendRunningSessionHistory
import com.lighthousepark.intervalsgym.data.completedRunningSessionForStorage
import com.lighthousepark.intervalsgym.data.completedStrengthSessionForStorage
import com.lighthousepark.intervalsgym.data.loadCompletedRunningSessionHistory
import com.lighthousepark.intervalsgym.data.loadSavedRunningWorkoutRoutines
import com.lighthousepark.intervalsgym.data.trainingItem
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import com.lighthousepark.intervalsgym.training.RoutineBlock
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutRoutineActionsTest {
    @Test
    fun localStrengthUploadPlanBlocksMissingInputsAndUploadsReadyWorkout() = runBlocking {
        val workout = completedStrengthSessionForStorage(
            id = "strength-upload",
            routineName = "Local Strength",
            startedAtMillis = 1_000L,
            endedAtMillis = 61_000L
        )
        val prefs = MemorySharedPreferences()
        val remote = RecordingStrengthSessionRemoteDataSource()
        val syncUseCase = StrengthSessionSyncUseCase(
            prefs = prefs,
            remoteDataSource = remote
        )

        assertNull(planWorkoutRoutineLocalStrengthUpload(apiKey = "api", localSession = null))
        assertEquals(
            WorkoutRoutineLocalStrengthUploadLoginRequired,
            planWorkoutRoutineLocalStrengthUpload(apiKey = "", localSession = workout)
        )

        val action = planWorkoutRoutineLocalStrengthUpload(
            apiKey = "api",
            localSession = workout
        )

        require(action is WorkoutRoutineLocalStrengthUploadReady)
        val uploaded = action.upload(syncUseCase)

        assertTrue(uploaded.uploadedToIntervals)
        assertEquals(1, remote.uploads.size)
    }

    @Test
    fun localRunningDeleteActionDeletesTargetSessionIdOnly() {
        val prefs = MemorySharedPreferences()
        val syncUseCase = RunningSessionSyncUseCase(
            prefs = prefs,
            remoteDataSource = RecordingRunningSessionRemoteDataSource()
        )
        val kept = completedRunningSessionForStorage(
            id = "kept",
            name = "Kept",
            startedAtMillis = 1_000L,
            endedAtMillis = 61_000L
        )
        val deleted = completedRunningSessionForStorage(
            id = "deleted",
            name = "Deleted",
            startedAtMillis = 2_000L,
            endedAtMillis = 62_000L
        )
        appendRunningSessionHistory(prefs, kept)
        appendRunningSessionHistory(prefs, deleted)

        val action = planWorkoutRoutineLocalRunningDelete(
            routine = trainingItem(remoteId = deleted.id)
        )

        require(action is WorkoutRoutineLocalRunningDeleteAction)
        action.delete(syncUseCase)

        assertEquals(listOf(kept.id), loadCompletedRunningSessionHistory(prefs).map { it.id })
        assertNull(planWorkoutRoutineLocalRunningDelete(trainingItem(remoteId = "")))
    }

    @Test
    fun localRunningDeleteActionUsesMatchedLocalSessionInsteadOfRemoteActivityId() {
        val localSession = completedRunningSessionForStorage(
            id = "local-session",
            name = "Run",
            startedAtMillis = 1_000L,
            endedAtMillis = 61_000L
        )

        val action = planWorkoutRoutineLocalRunningDelete(
            routine = trainingItem(remoteId = "i-garmin"),
            localSession = localSession
        )

        assertEquals("local-session", (action as WorkoutRoutineLocalRunningDeleteAction).sessionId)
    }

    @Test
    fun saveRunningRoutineActionPlansUnavailableAndPersistsReadyRoutine() {
        val prefs = MemorySharedPreferences()
        val blocks = listOf(routineBlock())
        val routine = trainingItem(
            id = "routine-run",
            remoteId = "remote-run",
            externalId = "external-run",
            name = "Tempo",
            type = "Run",
            isRoutine = true,
            description = "1m 10:00 pace [6km/h 1%]"
        )

        assertEquals(
            WorkoutRoutineSaveRunningRoutineUnavailable,
            planWorkoutRoutineSaveRunningRoutine(routine = null, graphBlocks = blocks)
        )
        assertEquals(
            WorkoutRoutineSaveRunningRoutineUnavailable,
            planWorkoutRoutineSaveRunningRoutine(routine = routine, graphBlocks = emptyList())
        )

        val action = planWorkoutRoutineSaveRunningRoutine(
            routine = routine,
            graphBlocks = blocks
        )

        require(action is WorkoutRoutineSaveRunningRoutineReady)
        action.save(prefs)

        val savedRoutine = loadSavedRunningWorkoutRoutines(prefs).single()
        assertEquals("러닝 Routine 저장됨", action.toastMessage)
        assertEquals("saved-running-external-run", savedRoutine.id)
        assertEquals(blocks, savedRoutine.blocks)
    }

    @Test
    fun startActionPrefersStrengthRoutineAndBuildsRunningDiagnostics() {
        val strengthRoutine = defaultStrengthRoutines().first()
        val runningRoutine = trainingItem(
            id = "run-start",
            name = "Hill Run",
            type = "Run"
        )
        val blocks = listOf(routineBlock(targetText = "8km/h"))

        val strengthAction = planWorkoutRoutineStartAction(
            routine = runningRoutine,
            graphBlocks = blocks,
            intervalStrengthRoutine = strengthRoutine
        )
        val runningAction = planWorkoutRoutineStartAction(
            routine = runningRoutine,
            graphBlocks = blocks,
            intervalStrengthRoutine = null
        )

        require(strengthAction is WorkoutRoutineStartStrengthAction)
        require(runningAction is WorkoutRoutineStartRunningAction)
        assertEquals(strengthRoutine, strengthAction.routine)
        assertTrue(runningAction.diagnosticDetails.contains("id=run-start"))
        assertTrue(runningAction.diagnosticDetails.contains("startingGraphBlocks"))
        assertEquals(
            WorkoutRoutineStartUnavailable,
            planWorkoutRoutineStartAction(
                routine = null,
                graphBlocks = blocks,
                intervalStrengthRoutine = null
            )
        )
    }

    private fun routineBlock(
        targetText: String = "6km/h",
    ): RoutineBlock {
        return RoutineBlock(
            index = 0,
            title = "Block 1",
            kind = "work",
            targetText = targetText,
            durationSeconds = 60,
            startSecond = 0,
            endSecond = 60,
            isRecovery = false
        )
    }
}

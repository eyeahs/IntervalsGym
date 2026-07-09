package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.running.toSavedRunningWorkoutRoutine
import com.lighthousepark.intervalsgym.running.toTrainingItem
import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.TrainingSportType
import com.lighthousepark.intervalsgym.training.sportType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class RunningRoutineStorageTest {
    @Test
    fun savedRunningWorkoutRoutine_roundTripsToExecutableTrainingItem() {
        val source = TrainingItem(
            id = "routine-remote-1",
            remoteId = "remote-1",
            externalId = "external-1",
            name = "UAE 40/20",
            type = "Run",
            date = LocalDate.of(2026, 6, 23),
            startedAt = null,
            timeLabel = "Routine",
            durationSeconds = null,
            distanceMeters = null,
            weightLiftedKg = null,
            load = null,
            fitness = null,
            fatigue = null,
            form = null,
            description = "12:00 pace",
            blocks = emptyList(),
            isRoutine = true
        )
        val blocks = listOf(
            RoutineBlock(
                index = 0,
                title = "Block 1",
                kind = "work",
                targetText = "12:00",
                durationSeconds = 60,
                startSecond = 0,
                endSecond = 60,
                isRecovery = false
            )
        )

        val saved = source.toSavedRunningWorkoutRoutine(blocks)
        val executable = saved?.toTrainingItem()

        assertEquals("saved-running-external-1", saved?.id)
        assertEquals(60, saved?.durationSeconds)
        assertEquals(false, executable?.isRoutine)
        assertEquals(TrainingSportType.RUNNING, executable?.sportType())
        assertEquals(1, executable?.blocks?.size)
    }

    @Test
    fun savedRunningWorkoutRoutine_upsertReplacesSameIdAndKeepsLatestFirst() {
        val prefs = MemorySharedPreferences()
        val original = savedRunningWorkoutRoutineForStorage(id = "saved-1", name = "before")
        val replacement = original.copy(name = "after", savedAtMillis = 2_000L)
        val other = savedRunningWorkoutRoutineForStorage(id = "saved-2", name = "other")

        upsertSavedRunningWorkoutRoutine(prefs, other)
        upsertSavedRunningWorkoutRoutine(prefs, original)
        upsertSavedRunningWorkoutRoutine(prefs, replacement)

        val routines = loadSavedRunningWorkoutRoutines(prefs)
        assertEquals(listOf("saved-1", "saved-2"), routines.map { it.id })
        assertEquals("after", routines.first().name)
    }

    @Test
    fun deleteSavedRunningWorkoutRoutine_removesOnlyTargetRoutine() {
        val prefs = MemorySharedPreferences()
        val first = savedRunningWorkoutRoutineForStorage(id = "saved-1", name = "first")
        val second = savedRunningWorkoutRoutineForStorage(id = "saved-2", name = "second")
        upsertSavedRunningWorkoutRoutine(prefs, first)
        upsertSavedRunningWorkoutRoutine(prefs, second)

        deleteSavedRunningWorkoutRoutine(prefs, "saved-1")

        val routines = loadSavedRunningWorkoutRoutines(prefs)
        assertEquals(1, routines.size)
        assertEquals("saved-2", routines.single().id)
    }
}

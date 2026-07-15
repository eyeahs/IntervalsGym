package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class StrengthRoutineStorageTest {
    @Test
    fun loadStrengthRoutines_fallsBackToDefaultsWhenStorageIsEmpty() {
        val routines = loadStrengthRoutines(MemorySharedPreferences())

        assertEquals(defaultStrengthRoutines().map { it.name }, routines.map { it.name })
    }

    @Test
    fun saveStrengthRoutineLibrary_roundTripsStoredRoutines() {
        val prefs = MemorySharedPreferences()
        val routine = defaultStrengthRoutines().first().copy(id = 88, name = "저장 Routine")

        saveStrengthRoutineLibrary(prefs, listOf(routine))

        val restored = loadStrengthRoutines(prefs)
        assertEquals(listOf(88), restored.map { it.id })
        assertEquals(listOf("저장 Routine"), restored.map { it.name })
    }

    @Test
    fun manualRoutineEditIsNotOverwrittenByOlderCompletedWorkout() {
        val prefs = MemorySharedPreferences()
        val routine = defaultStrengthRoutines().first()
        val workoutEntries = routine.entries.map { it.copy(note = "과거 운동 업데이트") }
        val completedWorkout = completedStrengthSessionForStorage(
            id = "completed-before-manual-edit",
            routineName = routine.name,
            startedAtMillis = 1_000L,
            endedAtMillis = 61_000L,
            entries = workoutEntries
        ).copy(uploadedToIntervals = true)
        appendStrengthSessionHistory(prefs, completedWorkout)
        val useCase = StrengthAppStateStorageUseCase(prefs)
        val manuallyEdited = routine.copy(
            entries = routine.entries.map { it.copy(note = "Routine 수정 화면 저장") }
        )

        val saved = useCase.saveStrengthRoutines(
            routines = listOf(manuallyEdited),
            completedHistory = listOf(completedWorkout)
        )
        val restored = useCase.loadSnapshot().routines.single()

        assertEquals("Routine 수정 화면 저장", saved.single().entries.first().note)
        assertEquals("Routine 수정 화면 저장", restored.entries.first().note)
    }

    @Test
    fun newCompletedWorkoutIsAppliedOnceAfterRoutineLibraryWasInitialized() {
        val prefs = MemorySharedPreferences()
        val routine = defaultStrengthRoutines().first()
        val useCase = StrengthAppStateStorageUseCase(prefs)
        useCase.saveStrengthRoutines(listOf(routine), emptyList())
        val updatedEntries = routine.entries.map { it.copy(note = "새 운동 업데이트") }
        val completedWorkout = completedStrengthSessionForStorage(
            id = "completed-after-library-save",
            routineName = routine.name,
            startedAtMillis = 2_000L,
            endedAtMillis = 62_000L,
            entries = updatedEntries
        )
        appendStrengthSessionHistory(prefs, completedWorkout)

        val firstLoad = useCase.loadSnapshot().routines.single()
        val manuallyEdited = firstLoad.copy(
            entries = firstLoad.entries.map { it.copy(note = "이후 수동 수정") }
        )
        useCase.saveStrengthRoutines(listOf(manuallyEdited), listOf(completedWorkout))
        val secondLoad = useCase.loadSnapshot().routines.single()

        assertEquals("새 운동 업데이트", firstLoad.entries.first().note)
        assertEquals("이후 수동 수정", secondLoad.entries.first().note)
        assertFalse(secondLoad.entries.any { it.note == "새 운동 업데이트" })
    }
}

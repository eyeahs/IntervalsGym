package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import org.junit.Assert.assertEquals
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
}

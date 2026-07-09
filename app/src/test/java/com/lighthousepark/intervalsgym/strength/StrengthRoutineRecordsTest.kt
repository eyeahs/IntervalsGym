package com.lighthousepark.intervalsgym.strength

import org.junit.Assert.assertEquals
import org.junit.Test

class StrengthRoutineRecordsTest {
    @Test
    fun setRecordChange_propagatesOnlyToFollowingSets() {
        val entry = defaultStrengthRoutineEntry(
            id = 1,
            exercise = strengthExerciseCatalog.first { it.id == "squat" },
            weightKg = "60",
            reps = "8",
            restSeconds = "90"
        )
        val changed = entry.records[1].copy(weightKg = "70", reps = "6", restSeconds = "120")

        val next = entry.withPropagatedRecordChange(1, changed)

        assertEquals("60", next.records[0].weightKg)
        assertEquals("8", next.records[0].reps)
        assertEquals("70", next.records[1].weightKg)
        assertEquals("6", next.records[1].reps)
        assertEquals("70", next.records[2].weightKg)
        assertEquals("6", next.records[2].reps)
        assertEquals("120", next.records[2].restSeconds)
    }
}

package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.app.INTERVALS_GYM_STRENGTH_ROUTINE_PREFIX
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthRoutineJsonTest {
    @Test
    fun strengthRoutineDescription_roundTripsEmbeddedRoutineJson() {
        val routine = defaultStrengthRoutines().first().copy(id = 88, name = "임베디드 Routine")
        val encoded = java.util.Base64.getEncoder().encodeToString(
            listOf(routine).toJsonString().toByteArray()
        )
        val description = """
            IntervalsGym 웨이트 Routine
            $INTERVALS_GYM_STRENGTH_ROUTINE_PREFIX $encoded
        """.trimIndent()

        val parsed = description.toIntervalsGymStrengthRoutine()

        requireNotNull(parsed)
        assertEquals(routine.id, parsed.id)
        assertEquals(routine.name, parsed.name)
        assertEquals(routine.entries.map { it.title }, parsed.entries.map { it.title })
        assertEquals(routine.entries.first().records.size, parsed.entries.first().records.size)
    }

    @Test
    fun strengthRoutineEntryNote_roundTripsThroughStorageAndIntervalsDescription() {
        val note = "왼쪽 무릎 각도 확인"
        val routine = defaultStrengthRoutines().first().copy(
            entries = defaultStrengthRoutines().first().entries.mapIndexed { index, entry ->
                if (index == 0) entry.copy(note = note) else entry
            }
        )

        val restored = listOf(routine).toJsonString().toStrengthWorkoutRoutines().single()
        val description = routine.toIntervalsRoutineDescription()
        val embedded = description.toIntervalsGymStrengthRoutine()

        assertEquals(note, restored.entries.first().note)
        assertTrue(description.contains("메모: $note"))
        assertEquals(note, embedded?.entries?.first()?.note)
    }
}

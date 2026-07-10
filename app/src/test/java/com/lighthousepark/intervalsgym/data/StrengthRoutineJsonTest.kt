package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.app.INTERVALS_GYM_STRENGTH_ROUTINE_PREFIX
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import org.json.JSONArray
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

    @Test
    fun strengthSetActualValues_roundTripAndLegacyRecordsDefaultToPlan() {
        val routine = defaultStrengthRoutines().first().let { source ->
            source.copy(
                entries = source.entries.mapIndexed { entryIndex, entry ->
                    if (entryIndex == 0) {
                        entry.copy(
                            records = entry.records.mapIndexed { recordIndex, record ->
                                if (recordIndex == 0) {
                                    record.copy(actualWeightKg = "72.5", actualReps = "6")
                                } else {
                                    record
                                }
                            }
                        )
                    } else {
                        entry
                    }
                }
            )
        }
        val encoded = listOf(routine).toJsonString()

        val restored = encoded.toStrengthWorkoutRoutines().single()
        val restoredRecord = restored.entries.first().records.first()

        assertEquals("72.5", restoredRecord.actualWeightKg)
        assertEquals("6", restoredRecord.actualReps)

        val legacyJson = JSONArray(encoded).apply {
            val records = getJSONObject(0)
                .getJSONArray("entries")
                .getJSONObject(0)
                .getJSONArray("records")
            for (index in 0 until records.length()) {
                records.getJSONObject(index).remove("actualWeightKg")
                records.getJSONObject(index).remove("actualReps")
            }
        }.toString()
        val legacyRecord = legacyJson.toStrengthWorkoutRoutines().single().entries.first().records.first()

        assertEquals("", legacyRecord.actualWeightKg)
        assertEquals("", legacyRecord.actualReps)
        assertEquals(legacyRecord.weightKg, legacyRecord.performedWeightKg)
        assertEquals(legacyRecord.reps, legacyRecord.performedReps)
    }
}

package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.app.INTERVALS_GYM_STRENGTH_ROUTINE_PREFIX
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import com.lighthousepark.intervalsgym.strength.StrengthSetGroupType
import com.lighthousepark.intervalsgym.strength.StrengthSetMetricType
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthRoutineJsonTest {
    @Test
    fun routineLocationRoundTripsAndLegacyRoutineDefaultsToUnspecified() {
        val routine = defaultStrengthRoutines().first().copy(location = "회사 헬스장")
        val encoded = listOf(routine).toJsonString()

        val restored = encoded.toStrengthWorkoutRoutines().single()
        val legacyJson = JSONArray(encoded).apply {
            getJSONObject(0).remove("location")
        }.toString()
        val legacy = legacyJson.toStrengthWorkoutRoutines().single()

        assertEquals("회사 헬스장", restored.location)
        assertEquals("", legacy.location)
    }

    @Test
    fun setGroupTypeRoundTripsAndLegacyGroupDefaultsToSuperset() {
        val routine = defaultStrengthRoutines().first().let { source ->
            source.copy(entries = source.entries.mapIndexed { index, entry ->
                if (index < 2) {
                    entry.copy(supersetGroupId = 7, setGroupType = StrengthSetGroupType.PAIRED_SET)
                } else {
                    entry
                }
            })
        }
        val encoded = listOf(routine).toJsonString()
        val restored = encoded.toStrengthWorkoutRoutines().single()
        val legacyJson = JSONArray(encoded).apply {
            getJSONObject(0).getJSONArray("entries").let { entries ->
                for (index in 0 until entries.length()) {
                    entries.getJSONObject(index).remove("setGroupType")
                }
            }
        }.toString()
        val legacy = legacyJson.toStrengthWorkoutRoutines().single()

        assertEquals(StrengthSetGroupType.PAIRED_SET, restored.entries.first().setGroupType)
        assertEquals(StrengthSetGroupType.SUPERSET, legacy.entries.first().setGroupType)
    }

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

        assertEquals(note, restored.entries.first().note)
        assertTrue(description.contains("메모: $note"))
        assertEquals(null, description.toIntervalsGymStrengthRoutine())
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

    @Test
    fun setMetricTypeAndActualDuration_roundTripWhileLegacyDefaultsToReps() {
        val routine = defaultStrengthRoutines().first().let { source ->
            source.copy(
                entries = source.entries.mapIndexed { index, entry ->
                    if (index == 0) {
                        entry.copy(
                            setMetricType = StrengthSetMetricType.DURATION,
                            records = entry.records.map { record ->
                                record.copy(
                                    durationSeconds = "45",
                                    actualDurationSeconds = "40"
                                )
                            }
                        )
                    } else {
                        entry
                    }
                }
            )
        }
        val encoded = listOf(routine).toJsonString()

        val restored = encoded.toStrengthWorkoutRoutines().single().entries.first()
        assertEquals(StrengthSetMetricType.DURATION, restored.setMetricType)
        assertEquals("45", restored.records.first().durationSeconds)
        assertEquals("40", restored.records.first().actualDurationSeconds)
        assertEquals("40", restored.records.first().performedDurationSeconds)

        val legacyJson = JSONArray(encoded).apply {
            val entry = getJSONObject(0).getJSONArray("entries").getJSONObject(0)
            entry.remove("setMetricType")
            val records = entry.getJSONArray("records")
            for (index in 0 until records.length()) {
                records.getJSONObject(index).remove("actualDurationSeconds")
            }
        }.toString()
        val legacyEntry = legacyJson.toStrengthWorkoutRoutines().single().entries.first()

        assertEquals(StrengthSetMetricType.REPS, legacyEntry.setMetricType)
        assertEquals("", legacyEntry.records.first().actualDurationSeconds)
        assertEquals("45", legacyEntry.records.first().performedDurationSeconds)
    }
}

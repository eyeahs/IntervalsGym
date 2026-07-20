package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.app.INTERVALS_GYM_STRENGTH_ROUTINE_ID_PREFIX
import com.lighthousepark.intervalsgym.app.INTERVALS_GYM_STRENGTH_ROUTINE_PREFIX
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthRoutineDescriptionStorageTest {
    @Test
    fun visibleRoutineDescription_hidesInternalMarkers() {
        val description = """
            설명
            $INTERVALS_GYM_STRENGTH_ROUTINE_ID_PREFIX 7
            $INTERVALS_GYM_STRENGTH_ROUTINE_PREFIX encoded
            로컬 러닝 기록
            로컬 러닝 기록 · Garmin 결과 대기
            본문
        """.trimIndent()

        assertEquals("설명\n본문", description.visibleRoutineDescription())
    }

    @Test
    fun intervalsRoutineDescription_containsOnlyHumanReadablePlan() {
        val routine = defaultStrengthRoutines().first().copy(id = 88, name = "원본 Routine")
        val description = routine.toIntervalsRoutineDescription()

        assertEquals(null, description.toIntervalsGymStrengthRoutineId())
        assertEquals(null, description.toIntervalsGymStrengthRoutine())
        assertFalse(description.contains(INTERVALS_GYM_STRENGTH_ROUTINE_ID_PREFIX))
        assertFalse(description.contains(INTERVALS_GYM_STRENGTH_ROUTINE_PREFIX))
        assertTrue(description.contains("IntervalsGym 웨이트 Routine"))
    }

    @Test
    fun strengthRoutineDescription_returnsNullForMalformedEmbeddedRoutineJson() {
        val description = """
            IntervalsGym 웨이트 Routine
            $INTERVALS_GYM_STRENGTH_ROUTINE_PREFIX not-base64
        """.trimIndent()

        assertEquals(null, description.toIntervalsGymStrengthRoutine())
    }

    @Test
    fun strengthRoutineForDisplay_usesPairedRoutineWhenResultIsMerged() {
        val strengthRoutine = defaultStrengthRoutines().first().copy(id = 55, name = "표시 Routine")
        val pairedRoutine = trainingItem(
            id = "routine-strength",
            type = "Weight Training",
            isRoutine = true
        ).copy(matchedStrengthRoutine = strengthRoutine)
        val result = trainingItem(
            id = "activity-strength",
            type = "Weight Training",
            isRoutine = false
        ).copy(pairedRoutine = pairedRoutine)

        assertEquals(strengthRoutine, result.strengthRoutineForDisplay())
    }

    @Test
    fun workoutDetailDescription_showsRawWeightResultDescriptionWhenRoutineIsUnmatched() {
        val rawDescription = "원본 웨이트 설명\nSet 1: 10kg x 8회"
        val result = trainingItem(
            type = "Weight Training",
            isRoutine = false,
            description = rawDescription
        )
        val matchedRoutine = defaultStrengthRoutines().first()
        val pairedRoutine = trainingItem(
            id = "routine-1",
            type = "Weight Training",
            isRoutine = true,
            description = matchedRoutine.toIntervalsRoutineDescription(),
            matchedStrengthRoutine = matchedRoutine
        )

        assertEquals(rawDescription, result.workoutDetailDescription(isWeightTrainingItem = true, strengthRoutine = null))
        assertEquals(rawDescription, result.copy(pairedRoutine = pairedRoutine).workoutDetailDescription(isWeightTrainingItem = true, strengthRoutine = null))
        assertEquals("", result.workoutDetailDescription(isWeightTrainingItem = true, strengthRoutine = matchedRoutine))
        assertEquals("", result.copy(pairedRoutine = pairedRoutine).workoutDetailDescription(isWeightTrainingItem = true, strengthRoutine = matchedRoutine))
    }
}

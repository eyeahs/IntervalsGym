package com.lighthousepark.intervalsgym.strength

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthSessionMetricsTest {
    @Test
    fun totalVolumeKg_countsCompletedAndEditedSets() {
        val entry = defaultStrengthRoutineEntry(
            id = 1,
            exercise = strengthExerciseCatalog.first { it.id == "bench_press" },
            weightKg = "80",
            reps = "5",
            restSeconds = "120"
        ).copy(
            records = listOf(
                StrengthSetRecord(
                    id = 1,
                    weightKg = "80",
                    reps = "5",
                    durationSeconds = "45",
                    restSeconds = "120",
                    completed = true
                ),
                StrengthSetRecord(
                    id = 2,
                    weightKg = "",
                    reps = "",
                    durationSeconds = "",
                    restSeconds = "120",
                    completed = false
                )
            )
        )

        assertEquals(400.0, listOf(entry).totalVolumeKg(), 0.01)
    }

    @Test
    fun totalDurationSeconds_usesCompletedFallbackSetDurationAndRest() {
        val entry = defaultStrengthRoutineEntry(
            id = 1,
            exercise = strengthExerciseCatalog.first { it.id == "squat" },
            weightKg = "100",
            reps = "3",
            restSeconds = "90"
        ).copy(
            records = listOf(
                StrengthSetRecord(
                    id = 1,
                    weightKg = "100",
                    reps = "3",
                    durationSeconds = "",
                    restSeconds = "90",
                    completed = true
                ),
                StrengthSetRecord(
                    id = 2,
                    weightKg = "100",
                    reps = "3",
                    durationSeconds = "30",
                    restSeconds = "90",
                    completed = true
                )
            )
        )

        assertEquals(165, listOf(entry).totalDurationSeconds())
    }

    @Test
    fun toIntervalsDescription_prefersCompletedSetEventsAndActualRest() {
        val entry = defaultStrengthRoutineEntry(
            id = 1,
            exercise = strengthExerciseCatalog.first { it.id == "bench_press" },
            weightKg = "80",
            reps = "5",
            restSeconds = "120"
        )
        val setEvent = StrengthSetCompletionEvent(
            sequence = 1,
            exerciseEntryId = entry.id,
            exerciseTitle = "벤치 프레스",
            exerciseGroup = entry.exercise.group,
            exerciseId = entry.exercise.id,
            equipment = entry.equipment,
            variation = entry.variation,
            setRecordId = entry.records.first().id,
            setIndex = 0,
            weightKg = "85",
            reps = "4",
            targetRestSeconds = 45,
            completedAtMillis = 10_000L
        )
        val restEvent = StrengthRestEvent(
            id = 1,
            afterSetSequence = setEvent.sequence,
            exerciseEntryId = entry.id,
            exerciseTitle = setEvent.exerciseTitle,
            setRecordId = setEvent.setRecordId,
            setIndex = setEvent.setIndex,
            startedAtMillis = 10_000L,
            plannedSeconds = 45,
            targetEndAtMillis = 55_000L,
            endedAtMillis = 52_000L,
            endReason = "finished"
        )
        val description = StrengthSession(
            name = "테스트 웨이트",
            startedAt = LocalDateTime.of(2026, 6, 23, 10, 0),
            entries = listOf(entry),
            rpe = 8,
            trainingLoad = 12,
            durationSeconds = 480,
            setEvents = listOf(setEvent),
            restEvents = listOf(restEvent)
        ).toIntervalsDescription()

        assertTrue(description.contains("총 세트: 1/1"))
        assertTrue(description.contains("총 볼륨: 340 kg"))
        assertTrue(description.contains("총 수행 시간: 8분"))
        assertTrue(description.contains("실제 휴식 합계: 00:42"))
        assertTrue(description.contains("Set 1: 85kg x 4회, 계획 휴식 45초, 완료"))
        assertTrue(description.contains("실제 휴식 00:42"))
    }

    @Test
    fun buildStrengthTcx_escapesWorkoutName() {
        val tcx = buildStrengthTcx(
            name = "A&B <Test>",
            startedAt = LocalDateTime.of(2026, 6, 23, 10, 0),
            durationSeconds = 60
        )

        assertTrue(tcx.contains("A&amp;B &lt;Test&gt;"))
    }
}

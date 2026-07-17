package com.lighthousepark.intervalsgym.strength

import org.junit.Assert.assertEquals
import org.junit.Test

class StrengthExerciseHistoryTest {
    @Test
    fun recentMatchingStrengthExerciseHistory_filtersByExerciseEquipmentAndVariation() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val barbellSquat = defaultStrengthRoutineEntry(
            id = 1,
            exercise = squat,
            weightKg = "60",
            reps = "8",
            restSeconds = "90"
        ).copy(records = defaultStrengthRoutineEntry(1, squat, "60", "8", "90").records.map { it.copy(completed = true) })
        val smithSquat = barbellSquat.copy(
            id = 2,
            equipment = "스미스",
            records = barbellSquat.records.map { it.copy(id = it.id + 10) }
        )
        val older = completedStrengthSession(
            id = "older",
            startedAtMillis = 1_000L,
            entries = listOf(barbellSquat),
            setEvents = listOf(barbellSquat.toSetEvent(sequence = 1, setIndex = 0))
        )
        val newer = completedStrengthSession(
            id = "newer",
            startedAtMillis = 3_000L,
            entries = listOf(barbellSquat),
            setEvents = listOf(barbellSquat.toSetEvent(sequence = 2, setIndex = 1))
        )
        val differentEquipment = completedStrengthSession(
            id = "smith",
            startedAtMillis = 2_000L,
            entries = listOf(smithSquat),
            setEvents = listOf(smithSquat.toSetEvent(sequence = 3, setIndex = 0))
        )

        val history = listOf(older, differentEquipment, newer).recentMatchingStrengthExerciseHistory(
            exercise = squat,
            equipment = "바벨",
            variation = "백 스쿼트"
        )

        assertEquals(listOf("newer", "older"), history.map { it.session.id })
        assertEquals(listOf(2), history.first().setEvents.map { it.sequence })
    }

    @Test
    fun machineHistoryRequiresSameLocationButFreeWeightHistoryDoesNot() {
        val legPress = strengthExerciseCatalog.first { it.id == "leg_press" }
        val machineEntry = defaultStrengthRoutineEntry(
            id = 1,
            exercise = legPress,
            weightKg = "100"
        )
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val barbellEntry = defaultStrengthRoutineEntry(
            id = 2,
            exercise = squat,
            weightKg = "80"
        )
        val otherGym = completedStrengthSession(
            id = "other-gym",
            startedAtMillis = 2_000L,
            entries = listOf(machineEntry, barbellEntry),
            setEvents = emptyList(),
            location = "다른 헬스장"
        )

        val machineHistory = listOf(otherGym).recentMatchingStrengthExerciseHistory(
            exercise = legPress,
            equipment = "머신",
            variation = "기본",
            location = "회사 헬스장"
        )
        val freeWeightHistory = listOf(otherGym).recentMatchingStrengthExerciseHistory(
            exercise = squat,
            equipment = "바벨",
            variation = "백 스쿼트",
            location = "회사 헬스장"
        )

        assertEquals(emptyList<CompletedStrengthExerciseHistory>(), machineHistory)
        assertEquals(listOf("other-gym"), freeWeightHistory.map { it.session.id })
    }

    @Test
    fun locationSpecificHistoryRecognizesMachineSmithAndCableEquipment() {
        assertEquals(true, "팩 덱 머신".usesLocationSpecificStrengthHistory())
        assertEquals(true, "스미스".usesLocationSpecificStrengthHistory())
        assertEquals(true, "Cable".usesLocationSpecificStrengthHistory())
        assertEquals(false, "덤벨".usesLocationSpecificStrengthHistory())
    }
}

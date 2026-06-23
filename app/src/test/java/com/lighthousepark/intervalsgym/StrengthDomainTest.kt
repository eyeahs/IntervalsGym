package com.lighthousepark.intervalsgym

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthDomainTest {
    @Test
    fun exerciseSearch_ignoresWhitespaceAndUsesAliases() {
        val legCurl = strengthExerciseCatalog.first { it.id == "leg_curl" }
        val shoulderRaise = strengthExerciseCatalog.first { it.id == "shoulder_raise" }

        assertTrue(legCurl.matchesSearch("레그컬"))
        assertTrue(shoulderRaise.matchesSearch("레터럴레이즈"))
    }

    @Test
    fun chestFlySearch_prefillsPecDeckEquipment() {
        val fly = strengthExerciseCatalog.first { it.id == "chest_fly" }
        val options = fly.equipmentOptionsWithBodyweight()

        assertEquals("팩 덱 머신", fly.inferEquipmentFromSearch("펙덱플라이", options))
    }

    @Test
    fun variationAndUnilateral_areSplitAndCombinedSeparately() {
        val legCurl = strengthExerciseCatalog.first { it.id == "leg_curl" }

        assertEquals("라잉" to "싱글레그", splitVariationAndUnilateral(legCurl, "싱글레그 라잉"))
        assertEquals("싱글레그 라잉", combineVariationAndUnilateral("라잉", "싱글레그"))
    }

    @Test
    fun setRecordChange_propagatesOnlyToFollowingSets() {
        val entry = defaultStrengthPlanEntry(
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

    @Test
    fun strengthTitleFormatting_keepsExerciseSpecificOrdering() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val row = strengthExerciseCatalog.first { it.id == "row" }

        assertEquals("바벨 백스쿼트", formatStrengthExerciseTitle(squat, "바벨", "백 스쿼트"))
        assertEquals("플랫 바벨 벤치프레스", formatStrengthExerciseTitle(bench, "바벨", "플랫"))
        assertEquals("바벨 로우 벤트오버", formatStrengthExerciseTitle(row, "바벨", "벤트오버"))
    }
}

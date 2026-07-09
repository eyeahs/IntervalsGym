package com.lighthousepark.intervalsgym.strength

import org.junit.Assert.assertEquals
import org.junit.Test

class StrengthExercisePresentationTest {
    @Test
    fun strengthTitleFormatting_keepsExerciseSpecificOrdering() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val row = strengthExerciseCatalog.first { it.id == "row" }
        val legCurl = strengthExerciseCatalog.first { it.id == "leg_curl" }
        val deadlift = strengthExerciseCatalog.first { it.id == "deadlift" }

        assertEquals("바벨 백스쿼트", formatStrengthExerciseTitle(squat, "바벨", "백 스쿼트"))
        assertEquals("플랫 바벨 벤치프레스", formatStrengthExerciseTitle(bench, "바벨", "플랫"))
        assertEquals("바벨 로우 벤트오버", formatStrengthExerciseTitle(row, "바벨", "벤트오버"))
        assertEquals("싱글 바벨 백스쿼트", formatStrengthExerciseTitle(squat, "바벨", "한쪽 백 스쿼트"))
        assertEquals("싱글 라잉 머신 레그 컬", formatStrengthExerciseTitle(legCurl, "머신", "한쪽 라잉"))
        assertEquals("싱글 바벨 데드리프트", formatStrengthExerciseTitle(deadlift, "바벨", "싱글레그"))
    }
}

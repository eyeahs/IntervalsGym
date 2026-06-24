package com.lighthousepark.intervalsgym.strength

import com.lighthousepark.intervalsgym.MainActivity
import com.lighthousepark.intervalsgym.R
import com.lighthousepark.intervalsgym.app.*
import com.lighthousepark.intervalsgym.core.*
import com.lighthousepark.intervalsgym.data.*
import com.lighthousepark.intervalsgym.login.*
import com.lighthousepark.intervalsgym.overlay.*
import com.lighthousepark.intervalsgym.running.*
import com.lighthousepark.intervalsgym.running.ui.*
import com.lighthousepark.intervalsgym.strength.*
import com.lighthousepark.intervalsgym.strength.ui.*
import com.lighthousepark.intervalsgym.training.*
import com.lighthousepark.intervalsgym.training.ui.*
import com.lighthousepark.intervalsgym.workout.ui.*

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
    fun overheadExtension_isSearchableAsShoulderExercise() {
        val overheadExtension = strengthExerciseCatalog.first { it.id == "overhead_extension" }

        assertEquals("어깨", overheadExtension.group)
        assertTrue(overheadExtension.matchesSearch("오버 헤드 익스텐션"))
        assertTrue(overheadExtension.matchesSearch("Over Head Extension"))
    }

    @Test
    fun deadbugSearch_selectsDeadbugCrunchVariation() {
        val crunch = strengthExerciseCatalog.first { it.id == "crunch" }

        assertTrue(crunch.matchesSearch("데드버그"))
        assertTrue(crunch.matchesSearch("deadbug"))
        assertEquals("데드버그", crunch.inferVariationFromSearch("데드버그"))
        assertEquals("데드버그", crunch.inferVariationFromSearch("dead bug"))
        assertEquals("데드버그 크런치", crunch.searchResultTitle("데드버그"))
    }

    @Test
    fun variationAndUnilateral_areSplitAndCombinedSeparately() {
        val legCurl = strengthExerciseCatalog.first { it.id == "leg_curl" }

        assertEquals("라잉" to "한쪽", splitVariationAndUnilateral(legCurl, "싱글레그 라잉"))
        assertEquals("라잉" to "한쪽", splitVariationAndUnilateral(legCurl, "한쪽 라잉"))
        assertEquals("한쪽 라잉", combineVariationAndUnilateral("라잉", "한쪽"))
    }

    @Test
    fun unilateralSearch_usesSingleOneSideMode() {
        val legCurl = strengthExerciseCatalog.first { it.id == "leg_curl" }
        val latPulldown = strengthExerciseCatalog.first { it.id == "lat_pulldown" }

        assertEquals(listOf("양쪽", "한쪽"), UNILATERAL_MODE_OPTIONS)
        assertEquals("한쪽", legCurl.inferUnilateralFromSearch("싱글레그 라잉 레그 컬"))
        assertEquals("한쪽", latPulldown.inferUnilateralFromSearch("싱글암 랫풀다운"))
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
    fun defaultStrengthEntry_usesTenKgExceptBodyweight() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val pushUp = strengthExerciseCatalog.first { it.id == "push_up" }

        val weightedEntry = defaultStrengthPlanEntry(id = 1, exercise = squat)
        val bodyweightEntry = defaultStrengthPlanEntry(id = 2, exercise = pushUp)

        assertEquals("10", weightedEntry.targetWeightKg)
        assertEquals(listOf("10", "10", "10"), weightedEntry.records.map { it.weightKg })
        assertEquals("", bodyweightEntry.targetWeightKg)
        assertEquals(listOf("", "", ""), bodyweightEntry.records.map { it.weightKg })
    }

    @Test
    fun recentMatchingStrengthExerciseHistory_filtersByExerciseEquipmentAndVariation() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val barbellSquat = defaultStrengthPlanEntry(
            id = 1,
            exercise = squat,
            weightKg = "60",
            reps = "8",
            restSeconds = "90"
        ).copy(records = defaultStrengthPlanEntry(1, squat, "60", "8", "90").records.map { it.copy(completed = true) })
        val smithSquat = barbellSquat.copy(
            id = 2,
            equipment = "스미스",
            records = barbellSquat.records.map { it.copy(id = it.id + 10) }
        )
        val older = completedStrengthWorkout(
            id = "older",
            startedAtMillis = 1_000L,
            entries = listOf(barbellSquat),
            setEvents = listOf(barbellSquat.toSetEvent(sequence = 1, setIndex = 0))
        )
        val newer = completedStrengthWorkout(
            id = "newer",
            startedAtMillis = 3_000L,
            entries = listOf(barbellSquat),
            setEvents = listOf(barbellSquat.toSetEvent(sequence = 2, setIndex = 1))
        )
        val differentEquipment = completedStrengthWorkout(
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

        assertEquals(listOf("newer", "older"), history.map { it.workout.id })
        assertEquals(listOf(2), history.first().setEvents.map { it.sequence })
    }

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

private fun completedStrengthWorkout(
    id: String,
    startedAtMillis: Long,
    entries: List<StrengthPlanEntry>,
    setEvents: List<StrengthSetCompletionEvent>,
): CompletedStrengthWorkout {
    return CompletedStrengthWorkout(
        id = id,
        planId = 1,
        planName = "history",
        startedAtMillis = startedAtMillis,
        endedAtMillis = startedAtMillis + 600_000L,
        durationSeconds = 600,
        intervalsExternalId = id,
        entries = entries,
        setEvents = setEvents,
        restEvents = emptyList(),
        rpe = 7,
        trainingLoad = 1,
        uploadedToIntervals = true
    )
}

private fun StrengthPlanEntry.toSetEvent(
    sequence: Int,
    setIndex: Int,
): StrengthSetCompletionEvent {
    val record = records[setIndex]
    return StrengthSetCompletionEvent(
        sequence = sequence,
        exerciseEntryId = id,
        exerciseTitle = title,
        exerciseGroup = exercise.group,
        exerciseId = exercise.id,
        equipment = equipment,
        variation = variation,
        setRecordId = record.id,
        setIndex = setIndex,
        weightKg = record.weightKg,
        reps = record.reps,
        targetRestSeconds = record.restSeconds.toIntOrNull() ?: restSeconds,
        completedAtMillis = sequence * 1_000L
    )
}

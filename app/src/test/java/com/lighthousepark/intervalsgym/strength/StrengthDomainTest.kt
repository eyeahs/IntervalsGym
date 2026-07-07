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
import org.junit.Assert.assertFalse
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
    fun variationUnilateralMode_usesExerciseCatalogData() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }

        assertEquals("한쪽", squat.forcedUnilateralModeForVariation("불가리안 스플릿"))
        assertEquals("불가리안 스플릿" to "한쪽", splitVariationAndUnilateral(squat, "불가리안 스플릿"))
        assertEquals("불가리안 스플릿" to "한쪽", splitVariationAndUnilateral(squat, "양쪽 불가리안 스플릿"))
        assertEquals(null, squat.forcedUnilateralModeForVariation("백 스쿼트"))
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

    @Test
    fun defaultStrengthEntry_usesTenKgExceptBodyweight() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val pushUp = strengthExerciseCatalog.first { it.id == "push_up" }

        val weightedEntry = defaultStrengthRoutineEntry(id = 1, exercise = squat)
        val bodyweightEntry = defaultStrengthRoutineEntry(id = 2, exercise = pushUp)

        assertEquals("10", weightedEntry.targetWeightKg)
        assertEquals(listOf("10", "10", "10"), weightedEntry.records.map { it.weightKg })
        assertEquals("", bodyweightEntry.targetWeightKg)
        assertEquals(listOf("", "", ""), bodyweightEntry.records.map { it.weightKg })
    }

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

    @Test
    fun groupSelectedEntriesAsSuperset_movesSelectedEntriesBelowTopSelectedEntry() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val row = strengthExerciseCatalog.first { it.id == "row" }
        val deadlift = strengthExerciseCatalog.first { it.id == "deadlift" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat),
            defaultStrengthRoutineEntry(id = 2, exercise = bench),
            defaultStrengthRoutineEntry(id = 3, exercise = row),
            defaultStrengthRoutineEntry(id = 4, exercise = deadlift)
        )

        val grouped = entries.groupSelectedEntriesAsSuperset(
            selectedEntryIds = setOf(1, 3),
            supersetGroupId = 7
        )

        assertEquals(listOf(1, 3, 2, 4), grouped.map { it.id })
        assertEquals(listOf(7, 7), grouped.take(2).map { it.supersetGroupId })
        assertEquals(null, grouped[2].supersetGroupId)
        assertEquals(null, grouped[3].supersetGroupId)
    }

    @Test
    fun groupSelectedEntriesAsSuperset_keepsAlreadyAdjacentEntriesInPlace() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val row = strengthExerciseCatalog.first { it.id == "row" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat),
            defaultStrengthRoutineEntry(id = 2, exercise = bench),
            defaultStrengthRoutineEntry(id = 3, exercise = row)
        )

        val grouped = entries.groupSelectedEntriesAsSuperset(
            selectedEntryIds = setOf(2, 3),
            supersetGroupId = 8
        )

        assertEquals(listOf(1, 2, 3), grouped.map { it.id })
        assertEquals(listOf(null, 8, 8), grouped.map { it.supersetGroupId })
    }

    @Test
    fun normalizeSupersetGroups_clearsGroupsWithSingleRemainingEntry() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val row = strengthExerciseCatalog.first { it.id == "row" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat).copy(supersetGroupId = 7),
            defaultStrengthRoutineEntry(id = 2, exercise = bench).copy(supersetGroupId = 8),
            defaultStrengthRoutineEntry(id = 3, exercise = row).copy(supersetGroupId = 8)
        )

        val normalized = entries.normalizeSupersetGroups()

        assertEquals(listOf(null, 8, 8), normalized.map { it.supersetGroupId })
    }

    @Test
    fun nextIncompleteSet_prefersNextSupersetExerciseInSameSetRound() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val row = strengthExerciseCatalog.first { it.id == "row" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat).copy(supersetGroupId = 7).withCompletedRecord(0),
            defaultStrengthRoutineEntry(id = 2, exercise = bench).copy(supersetGroupId = 7),
            defaultStrengthRoutineEntry(id = 3, exercise = row)
        )

        val next = nextIncompleteSet(entries, fromExerciseIndex = 0, fromSetIndex = 0)

        assertEquals(1 to 0, next)
        assertTrue(isImmediateSupersetTransition(entries, fromExerciseIndex = 0, fromSetIndex = 0, toSet = next))
    }

    @Test
    fun nextIncompleteSet_startsAtFirstExerciseWhenCurrentIndexIsNegative() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat).withCompletedRecord(0),
            defaultStrengthRoutineEntry(id = 2, exercise = bench)
        )

        val next = nextIncompleteSet(entries, fromExerciseIndex = -1, fromSetIndex = 0)

        assertEquals(0 to 1, next)
    }

    @Test
    fun nextIncompleteSet_returnsNullWhenEverySetIsCompleted() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat).withCompletedRecords(0, 1, 2),
            defaultStrengthRoutineEntry(id = 2, exercise = bench).withCompletedRecords(0, 1, 2)
        )

        val next = nextIncompleteSet(entries, fromExerciseIndex = 0, fromSetIndex = 2)

        assertEquals(null, next)
    }

    @Test
    fun nextIncompleteSet_returnsNextSupersetRoundAfterLastSupersetExercise() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat).copy(supersetGroupId = 7).withCompletedRecord(0),
            defaultStrengthRoutineEntry(id = 2, exercise = bench).copy(supersetGroupId = 7).withCompletedRecord(0)
        )

        val next = nextIncompleteSet(entries, fromExerciseIndex = 1, fromSetIndex = 0)

        assertEquals(0 to 1, next)
        assertEquals(false, isImmediateSupersetTransition(entries, fromExerciseIndex = 1, fromSetIndex = 0, toSet = next))
    }

    @Test
    fun shouldAdvanceCurrentExerciseAfterCompletedExercise_movesAfterLastSet() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat).withCompletedRecords(0, 1, 2),
            defaultStrengthRoutineEntry(id = 2, exercise = bench)
        )
        val next = nextIncompleteSet(entries, fromExerciseIndex = 0, fromSetIndex = 2)

        assertEquals(1 to 0, next)
        assertTrue(
            shouldAdvanceCurrentExerciseAfterCompletedExercise(
                entries = entries,
                fromExerciseIndex = 0,
                toSet = next
            )
        )
    }

    @Test
    fun shouldAdvanceCurrentExerciseAfterCompletedExercise_staysWhenSameExerciseHasMoreSets() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat).withCompletedRecord(0),
            defaultStrengthRoutineEntry(id = 2, exercise = bench)
        )
        val next = nextIncompleteSet(entries, fromExerciseIndex = 0, fromSetIndex = 0)

        assertEquals(0 to 1, next)
        assertFalse(
            shouldAdvanceCurrentExerciseAfterCompletedExercise(
                entries = entries,
                fromExerciseIndex = 0,
                toSet = next
            )
        )
    }

    @Test
    fun exerciseChangeFocusIndex_prefersPendingAddedEntryOverStaleCurrentIndex() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
        val entries = listOf(
            defaultStrengthRoutineEntry(id = 1, exercise = squat),
            defaultStrengthRoutineEntry(id = 9, exercise = bench)
        )

        val focusIndex = entries.exerciseChangeFocusIndex(
            currentExerciseIndex = 7,
            pendingAddedEntryId = 9
        )

        assertEquals(1, focusIndex)
    }

    @Test
    fun exerciseChangeFocusIndex_clampsWhenPendingEntryIsMissing() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val entries = listOf(defaultStrengthRoutineEntry(id = 1, exercise = squat))

        val focusIndex = entries.exerciseChangeFocusIndex(
            currentExerciseIndex = 7,
            pendingAddedEntryId = 99
        )

        assertEquals(0, focusIndex)
    }

    @Test
    fun nextStrengthWorkoutRoutineId_doesNotReuseDeletedRoutineIdsStillReferencedByHistory() {
        val existingRoutine = defaultStrengthRoutines().first().copy(id = 1)
        val deletedRoutineHistory = completedStrengthSession(
            id = "deleted-routine-workout",
            routineId = 2,
            startedAtMillis = 1_000L,
            entries = existingRoutine.entries,
            setEvents = emptyList()
        )

        val nextId = nextStrengthWorkoutRoutineId(
            routines = listOf(existingRoutine),
            history = listOf(deletedRoutineHistory)
        )

        assertEquals(3, nextId)
    }

    @Test
    fun nextStrengthWorkoutRoutineId_reservesScheduledAndActiveRoutineIds() {
        val existingRoutine = defaultStrengthRoutines().first().copy(id = 1)
        val scheduledRoutine = ScheduledStrengthRoutine(
            id = "scheduled",
            date = java.time.LocalDate.of(2026, 7, 1),
            routine = existingRoutine.copy(id = 4),
            uploadedToIntervals = false,
            externalId = "scheduled-external"
        )
        val activeSession = ActiveStrengthSession(
            routineId = 5,
            routineName = "active",
            entries = emptyList(),
            hasStarted = false,
            sessionStartedAtMillis = 0L,
            isSetScreenVisible = false,
            currentExerciseIndex = 0,
            currentSetIndex = 0,
            pendingExerciseIndex = null,
            pendingSetIndex = null,
            restEndAtMillis = 0L,
            isRestSheetVisible = false,
            restTitle = "",
            setEvents = emptyList(),
            restEvents = emptyList(),
            activeRestEventId = null
        )

        val nextId = nextStrengthWorkoutRoutineId(
            routines = listOf(existingRoutine),
            scheduledRoutines = listOf(scheduledRoutine),
            activeSession = activeSession,
            reservedIds = listOf(6)
        )

        assertEquals(7, nextId)
    }

    @Test
    fun completedStrengthSessionAutoLocalSaveAtMillis_usesLastCompletedSetWhenAllSetsAreDone() {
        val entry = defaultStrengthRoutineEntry(
            id = 1,
            exercise = strengthExerciseCatalog.first { it.id == "squat" }
        ).withCompletedRecords(0, 1, 2)
        val setEvents = listOf(
            entry.toSetEvent(sequence = 1, setIndex = 0).copy(completedAtMillis = 1_000L),
            entry.toSetEvent(sequence = 2, setIndex = 1).copy(completedAtMillis = 2_000L),
            entry.toSetEvent(sequence = 3, setIndex = 2).copy(completedAtMillis = 3_000L)
        )

        assertEquals(3_000L, completedStrengthSessionFinishedAtMillis(listOf(entry), setEvents))
        assertEquals(
            3_000L + SESSION_AUTO_LOCAL_SAVE_DELAY_MILLIS,
            completedStrengthSessionAutoLocalSaveAtMillis(listOf(entry), setEvents)
        )
        assertFalse(
            shouldAutoLocalSaveCompletedStrengthSession(
                entries = listOf(entry),
                setEvents = setEvents,
                nowMillis = 3_000L + SESSION_AUTO_LOCAL_SAVE_DELAY_MILLIS - 1L
            )
        )
        assertTrue(
            shouldAutoLocalSaveCompletedStrengthSession(
                entries = listOf(entry),
                setEvents = setEvents,
                nowMillis = 3_000L + SESSION_AUTO_LOCAL_SAVE_DELAY_MILLIS
            )
        )
    }

    @Test
    fun completedStrengthSessionFinishedAtMillis_waitsUntilEverySetIsDone() {
        val entry = defaultStrengthRoutineEntry(
            id = 1,
            exercise = strengthExerciseCatalog.first { it.id == "squat" }
        ).withCompletedRecords(0, 1)
        val setEvents = listOf(
            entry.toSetEvent(sequence = 1, setIndex = 0),
            entry.toSetEvent(sequence = 2, setIndex = 1)
        )

        assertEquals(null, completedStrengthSessionFinishedAtMillis(listOf(entry), setEvents))
        assertEquals(null, completedStrengthSessionAutoLocalSaveAtMillis(listOf(entry), setEvents))
        assertFalse(
            shouldAutoLocalSaveCompletedStrengthSession(
                entries = listOf(entry),
                setEvents = setEvents,
                nowMillis = Long.MAX_VALUE
            )
        )
    }
}

private fun completedStrengthSession(
    id: String,
    routineId: Int = 1,
    startedAtMillis: Long,
    entries: List<StrengthRoutineEntry>,
    setEvents: List<StrengthSetCompletionEvent>,
): CompletedStrengthSession {
    return CompletedStrengthSession(
        id = id,
        routineId = routineId,
        routineName = "history",
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

private fun StrengthRoutineEntry.withCompletedRecord(setIndex: Int): StrengthRoutineEntry {
    return copy(
        records = records.mapIndexed { index, record ->
            if (index == setIndex) record.copy(completed = true) else record
        }
    )
}

private fun StrengthRoutineEntry.withCompletedRecords(vararg setIndices: Int): StrengthRoutineEntry {
    val completedSetIndices = setIndices.toSet()
    return copy(
        records = records.mapIndexed { index, record ->
            if (index in completedSetIndices) record.copy(completed = true) else record
        }
    )
}

private fun StrengthRoutineEntry.toSetEvent(
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

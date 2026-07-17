package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.running.RunningRoutePoint
import com.lighthousepark.intervalsgym.strength.ScheduledStrengthRoutine
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.WeekTrainingData
import java.time.LocalDate
import java.time.LocalDateTime
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingCacheJsonTest {
    @Test
    fun intervalsWeekCache_roundTripsTrainingItemsWithNestedRoutineAndRunningActuals() {
        val prefs = MemorySharedPreferences()
        val weekStart = LocalDate.of(2026, 6, 22)
        val weekEnd = LocalDate.of(2026, 6, 28)
        val pairedRoutine = trainingCacheItem(
            id = "routine-run",
            type = "Run",
            date = LocalDate.of(2026, 6, 23),
            isRoutine = true,
            blocks = listOf(
                cacheRoutineBlock(
                    index = 0,
                    targetText = "16km/h 1%",
                    repeatIteration = 2,
                    repeatCount = 4
                )
            ),
            workoutDocJson = """{"sport":"run"}"""
        )
        val activity = trainingCacheItem(
            id = "activity-run",
            type = "Run",
            date = LocalDate.of(2026, 6, 23),
            isRoutine = false,
            actualRunningBlocks = listOf(cacheRoutineBlock(index = 0, targetText = "16km/h 1%", durationSeconds = 15)),
            actualRunningRoutePoints = listOf(RunningRoutePoint(elapsedSeconds = 5, latitude = 37.1, longitude = 131.8)),
            pairedRoutine = pairedRoutine
        )
        val strengthRoutine = defaultStrengthRoutines().first().copy(id = 77, name = "캐시 웨이트")
        val strengthRoutineItem = trainingCacheItem(
            id = "routine-strength",
            type = "Weight Training",
            date = LocalDate.of(2026, 6, 24),
            isRoutine = true,
            matchedStrengthRoutine = strengthRoutine
        )

        saveIntervalsWeekCache(
            prefs = prefs,
            apiKey = "api-key-a",
            weekStart = weekStart,
            weekEnd = weekEnd,
            data = WeekTrainingData(
                activities = listOf(activity),
                routines = listOf(strengthRoutineItem)
            )
        )

        val cached = loadIntervalsWeekCache(prefs, "api-key-a", weekStart, weekEnd)

        requireNotNull(cached)
        assertEquals(1, cached.activities.size)
        assertEquals("activity-run", cached.activities.single().id)
        assertEquals("routine-run", cached.activities.single().pairedRoutine?.id)
        assertEquals("""{"sport":"run"}""", cached.activities.single().pairedRoutine?.workoutDocJson)
        assertEquals(2, cached.activities.single().pairedRoutine?.blocks?.single()?.repeatIteration)
        assertEquals(4, cached.activities.single().pairedRoutine?.blocks?.single()?.repeatCount)
        assertEquals(1, cached.activities.single().actualRunningBlocks.size)
        assertEquals(1, cached.activities.single().actualRunningRoutePoints.size)
        assertEquals(1, cached.routines.size)
        assertEquals(77, cached.routines.single().matchedStrengthRoutine?.id)
        assertEquals("캐시 웨이트", cached.routines.single().matchedStrengthRoutine?.name)
    }

    @Test
    fun cachedRoutineBlocks_readsLegacyShapeWithoutRepeatMetadata() {
        val legacyBlocks = JSONArray().put(
            JSONObject()
                .put("index", 0)
                .put("title", "Legacy Block")
                .put("kind", "work")
                .put("targetText", "10km/h")
                .put("durationSeconds", 60)
                .put("startSecond", 0)
                .put("endSecond", 60)
                .put("isRecovery", false)
        )

        val restored = legacyBlocks.toCachedRoutineBlocks().single()

        assertNull(restored.repeatIteration)
        assertNull(restored.repeatCount)
    }

    @Test
    fun intervalsWeekCache_returnsNullWhenStoredRangeDoesNotMatchRequestedRange() {
        val prefs = MemorySharedPreferences()
        val weekStart = LocalDate.of(2026, 6, 22)
        val weekEnd = LocalDate.of(2026, 6, 28)
        val key = intervalsWeekCacheKey("api-key-a", weekStart, weekEnd)
        prefs.edit()
            .putString(
                key,
                """
                    {
                      "weekStart": "2026-06-15",
                      "weekEnd": "2026-06-21",
                      "activities": [],
                      "routines": []
                    }
                """.trimIndent()
            )
            .apply()

        assertNull(loadIntervalsWeekCache(prefs, "api-key-a", weekStart, weekEnd))
    }

    @Test
    fun removeCalendarRoutineFromIntervalsCaches_removesOnlyMatchingApiKeyAndRoutine() {
        val prefs = MemorySharedPreferences()
        val weekStart = LocalDate.of(2026, 6, 22)
        val weekEnd = LocalDate.of(2026, 6, 28)
        val targetRoutine = trainingCacheItem(
            id = "routine-target",
            remoteId = "remote-target",
            externalId = "external-target",
            type = "Run",
            date = LocalDate.of(2026, 6, 23),
            isRoutine = true
        )
        val otherRoutine = trainingCacheItem(
            id = "routine-other",
            remoteId = "remote-other",
            externalId = "external-other",
            type = "Run",
            date = LocalDate.of(2026, 6, 24),
            isRoutine = true
        )
        saveIntervalsWeekCache(
            prefs = prefs,
            apiKey = "api-key-a",
            weekStart = weekStart,
            weekEnd = weekEnd,
            data = WeekTrainingData(activities = emptyList(), routines = listOf(targetRoutine, otherRoutine))
        )
        saveIntervalsWeekCache(
            prefs = prefs,
            apiKey = "api-key-b",
            weekStart = weekStart,
            weekEnd = weekEnd,
            data = WeekTrainingData(activities = emptyList(), routines = listOf(targetRoutine))
        )

        removeCalendarRoutineFromIntervalsCaches(prefs, "api-key-a", targetRoutine)

        val apiARoutines = loadIntervalsWeekCache(prefs, "api-key-a", weekStart, weekEnd)?.routines.orEmpty()
        val apiBRoutines = loadIntervalsWeekCache(prefs, "api-key-b", weekStart, weekEnd)?.routines.orEmpty()
        assertEquals(listOf("routine-other"), apiARoutines.map { it.id })
        assertEquals(listOf("routine-target"), apiBRoutines.map { it.id })
    }

    @Test
    fun removeCalendarRoutineFromIntervalsCaches_matchesRemoteIdOrExternalIdWhenLocalIdDiffers() {
        val prefs = MemorySharedPreferences()
        val weekStart = LocalDate.of(2026, 6, 22)
        val weekEnd = LocalDate.of(2026, 6, 28)
        val remoteIdMatchedRoutine = trainingCacheItem(
            id = "cached-remote-id-routine",
            remoteId = "shared-remote-id",
            externalId = "cached-external-1",
            type = "Run",
            date = LocalDate.of(2026, 6, 23),
            isRoutine = true
        )
        val externalIdMatchedRoutine = trainingCacheItem(
            id = "cached-external-id-routine",
            remoteId = "cached-remote-2",
            externalId = "shared-external-id",
            type = "Run",
            date = LocalDate.of(2026, 6, 24),
            isRoutine = true
        )
        val keptRoutine = trainingCacheItem(
            id = "cached-kept-routine",
            remoteId = "kept-remote-id",
            externalId = "kept-external-id",
            type = "Run",
            date = LocalDate.of(2026, 6, 25),
            isRoutine = true
        )
        saveIntervalsWeekCache(
            prefs = prefs,
            apiKey = "api-key-a",
            weekStart = weekStart,
            weekEnd = weekEnd,
            data = WeekTrainingData(
                activities = emptyList(),
                routines = listOf(remoteIdMatchedRoutine, externalIdMatchedRoutine, keptRoutine)
            )
        )

        removeCalendarRoutineFromIntervalsCaches(
            prefs = prefs,
            apiKey = "api-key-a",
            routine = trainingCacheItem(
                id = "local-different-id",
                remoteId = "shared-remote-id",
                externalId = null,
                type = "Run",
                date = LocalDate.of(2026, 6, 23),
                isRoutine = true
            )
        )
        removeCalendarRoutineFromIntervalsCaches(
            prefs = prefs,
            apiKey = "api-key-a",
            routine = trainingCacheItem(
                id = "another-local-different-id",
                remoteId = "another-remote-id",
                externalId = "shared-external-id",
                type = "Run",
                date = LocalDate.of(2026, 6, 24),
                isRoutine = true
            )
        )

        val routines = loadIntervalsWeekCache(prefs, "api-key-a", weekStart, weekEnd)?.routines.orEmpty()
        assertEquals(listOf("cached-kept-routine"), routines.map { it.id })
    }

    @Test
    fun withLocalStrengthRoutines_matchesRemoteRoutineAndKeepsUnmatchedLocalRoutine() {
        val date = LocalDate.of(2026, 6, 23)
        val matchedRoutine = defaultStrengthRoutines().first().copy(id = 20, name = "원격 매칭")
        val localOnlyRoutine = defaultStrengthRoutines().last().copy(id = 21, name = "로컬 유지")
        val scheduledMatched = scheduledStrengthRoutine(matchedRoutine, date)
        val scheduledLocalOnly = scheduledStrengthRoutine(localOnlyRoutine, date.plusDays(1))
        val remoteRoutine = trainingCacheItem(
            id = "remote-strength-routine",
            remoteId = "remote-strength-routine",
            externalId = scheduledMatched.externalId,
            type = "Weight Training",
            date = date,
            isRoutine = true,
            matchedStrengthRoutine = null
        )

        val merged = listOf(remoteRoutine).withLocalStrengthRoutines(
            scheduledRoutines = listOf(scheduledMatched, scheduledLocalOnly),
            start = date,
            end = date.plusDays(6)
        )

        assertEquals(2, merged.size)
        val matchedRemote = merged.first { it.id == "remote-strength-routine" }
        val localOnly = merged.first { it.externalId == scheduledLocalOnly.externalId }
        assertSame(matchedRoutine, matchedRemote.matchedStrengthRoutine)
        assertTrue(localOnly.id.startsWith("local-"))
        assertEquals("로컬 유지", localOnly.matchedStrengthRoutine?.name)
        assertFalse(localOnly.isLocalOnlyStrengthResult)
        assertTrue(localOnly.isRoutine)
    }
}

private fun scheduledStrengthRoutine(
    routine: StrengthWorkoutRoutine,
    date: LocalDate,
): ScheduledStrengthRoutine {
    return ScheduledStrengthRoutine(
        id = routine.scheduledStrengthRoutineId(date),
        date = date,
        routine = routine,
        uploadedToIntervals = true,
        externalId = routine.intervalsRoutineExternalId(date)
    )
}

private fun trainingCacheItem(
    id: String,
    type: String,
    date: LocalDate,
    isRoutine: Boolean,
    remoteId: String = id,
    externalId: String? = "$id-external",
    matchedStrengthRoutine: StrengthWorkoutRoutine? = null,
    blocks: List<RoutineBlock> = emptyList(),
    actualRunningBlocks: List<RoutineBlock> = emptyList(),
    actualRunningRoutePoints: List<RunningRoutePoint> = emptyList(),
    pairedRoutine: TrainingItem? = null,
    workoutDocJson: String? = null,
): TrainingItem {
    return TrainingItem(
        id = id,
        remoteId = remoteId,
        externalId = externalId,
        name = id,
        type = type,
        date = date,
        startedAt = LocalDateTime.of(date.year, date.month, date.dayOfMonth, 7, 30),
        timeLabel = if (isRoutine) "Routine" else "07:30",
        durationSeconds = blocks.sumOf { it.durationSeconds }.takeIf { it > 0 },
        distanceMeters = null,
        weightLiftedKg = null,
        load = null,
        fitness = null,
        fatigue = null,
        form = null,
        description = null,
        blocks = blocks,
        isRoutine = isRoutine,
        matchedStrengthRoutine = matchedStrengthRoutine,
        actualRunningBlocks = actualRunningBlocks,
        actualRunningRoutePoints = actualRunningRoutePoints,
        pairedRoutine = pairedRoutine,
        workoutDocJson = workoutDocJson
    )
}

private fun cacheRoutineBlock(
    index: Int,
    targetText: String,
    durationSeconds: Int = 60,
    repeatIteration: Int? = null,
    repeatCount: Int? = null,
): RoutineBlock {
    return RoutineBlock(
        index = index,
        title = "Block ${index + 1}",
        kind = "work",
        targetText = targetText,
        durationSeconds = durationSeconds,
        startSecond = index * durationSeconds,
        endSecond = (index + 1) * durationSeconds,
        isRecovery = false,
        repeatIteration = repeatIteration,
        repeatCount = repeatCount
    )
}

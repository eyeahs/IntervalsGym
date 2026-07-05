package com.lighthousepark.intervalsgym.data

import android.content.SharedPreferences
import com.lighthousepark.intervalsgym.running.RunningRoutePoint
import com.lighthousepark.intervalsgym.strength.ScheduledStrengthPlan
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutPlan
import com.lighthousepark.intervalsgym.strength.defaultStrengthPlans
import com.lighthousepark.intervalsgym.training.PlanBlock
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.WeekTrainingData
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingCacheJsonTest {
    @Test
    fun intervalsWeekCache_roundTripsTrainingItemsWithNestedPlanAndRunningActuals() {
        val prefs = CacheMemorySharedPreferences()
        val weekStart = LocalDate.of(2026, 6, 22)
        val weekEnd = LocalDate.of(2026, 6, 28)
        val pairedPlan = trainingCacheItem(
            id = "plan-run",
            type = "Run",
            date = LocalDate.of(2026, 6, 23),
            isPlan = true,
            blocks = listOf(cachePlanBlock(index = 0, targetText = "16km/h 1%")),
            workoutDocJson = """{"sport":"run"}"""
        )
        val activity = trainingCacheItem(
            id = "activity-run",
            type = "Run",
            date = LocalDate.of(2026, 6, 23),
            isPlan = false,
            actualRunningBlocks = listOf(cachePlanBlock(index = 0, targetText = "16km/h 1%", durationSeconds = 15)),
            actualRunningRoutePoints = listOf(RunningRoutePoint(elapsedSeconds = 5, latitude = 37.1, longitude = 131.8)),
            pairedPlan = pairedPlan
        )
        val strengthPlan = defaultStrengthPlans().first().copy(id = 77, name = "캐시 웨이트")
        val strengthPlanItem = trainingCacheItem(
            id = "plan-strength",
            type = "Weight Training",
            date = LocalDate.of(2026, 6, 24),
            isPlan = true,
            matchedStrengthPlan = strengthPlan
        )

        saveIntervalsWeekCache(
            prefs = prefs,
            apiKey = "api-key-a",
            weekStart = weekStart,
            weekEnd = weekEnd,
            data = WeekTrainingData(
                activities = listOf(activity),
                plans = listOf(strengthPlanItem)
            )
        )

        val cached = loadIntervalsWeekCache(prefs, "api-key-a", weekStart, weekEnd)

        requireNotNull(cached)
        assertEquals(1, cached.activities.size)
        assertEquals("activity-run", cached.activities.single().id)
        assertEquals("plan-run", cached.activities.single().pairedPlan?.id)
        assertEquals("""{"sport":"run"}""", cached.activities.single().pairedPlan?.workoutDocJson)
        assertEquals(1, cached.activities.single().actualRunningBlocks.size)
        assertEquals(1, cached.activities.single().actualRunningRoutePoints.size)
        assertEquals(1, cached.plans.size)
        assertEquals(77, cached.plans.single().matchedStrengthPlan?.id)
        assertEquals("캐시 웨이트", cached.plans.single().matchedStrengthPlan?.name)
    }

    @Test
    fun intervalsWeekCache_returnsNullWhenStoredRangeDoesNotMatchRequestedRange() {
        val prefs = CacheMemorySharedPreferences()
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
                      "plans": []
                    }
                """.trimIndent()
            )
            .apply()

        assertNull(loadIntervalsWeekCache(prefs, "api-key-a", weekStart, weekEnd))
    }

    @Test
    fun removeCalendarPlanFromIntervalsCaches_removesOnlyMatchingApiKeyAndPlan() {
        val prefs = CacheMemorySharedPreferences()
        val weekStart = LocalDate.of(2026, 6, 22)
        val weekEnd = LocalDate.of(2026, 6, 28)
        val targetPlan = trainingCacheItem(
            id = "plan-target",
            remoteId = "remote-target",
            externalId = "external-target",
            type = "Run",
            date = LocalDate.of(2026, 6, 23),
            isPlan = true
        )
        val otherPlan = trainingCacheItem(
            id = "plan-other",
            remoteId = "remote-other",
            externalId = "external-other",
            type = "Run",
            date = LocalDate.of(2026, 6, 24),
            isPlan = true
        )
        saveIntervalsWeekCache(
            prefs = prefs,
            apiKey = "api-key-a",
            weekStart = weekStart,
            weekEnd = weekEnd,
            data = WeekTrainingData(activities = emptyList(), plans = listOf(targetPlan, otherPlan))
        )
        saveIntervalsWeekCache(
            prefs = prefs,
            apiKey = "api-key-b",
            weekStart = weekStart,
            weekEnd = weekEnd,
            data = WeekTrainingData(activities = emptyList(), plans = listOf(targetPlan))
        )

        removeCalendarPlanFromIntervalsCaches(prefs, "api-key-a", targetPlan)

        val apiAPlans = loadIntervalsWeekCache(prefs, "api-key-a", weekStart, weekEnd)?.plans.orEmpty()
        val apiBPlans = loadIntervalsWeekCache(prefs, "api-key-b", weekStart, weekEnd)?.plans.orEmpty()
        assertEquals(listOf("plan-other"), apiAPlans.map { it.id })
        assertEquals(listOf("plan-target"), apiBPlans.map { it.id })
    }

    @Test
    fun removeCalendarPlanFromIntervalsCaches_matchesRemoteIdOrExternalIdWhenLocalIdDiffers() {
        val prefs = CacheMemorySharedPreferences()
        val weekStart = LocalDate.of(2026, 6, 22)
        val weekEnd = LocalDate.of(2026, 6, 28)
        val remoteIdMatchedPlan = trainingCacheItem(
            id = "cached-remote-id-plan",
            remoteId = "shared-remote-id",
            externalId = "cached-external-1",
            type = "Run",
            date = LocalDate.of(2026, 6, 23),
            isPlan = true
        )
        val externalIdMatchedPlan = trainingCacheItem(
            id = "cached-external-id-plan",
            remoteId = "cached-remote-2",
            externalId = "shared-external-id",
            type = "Run",
            date = LocalDate.of(2026, 6, 24),
            isPlan = true
        )
        val keptPlan = trainingCacheItem(
            id = "cached-kept-plan",
            remoteId = "kept-remote-id",
            externalId = "kept-external-id",
            type = "Run",
            date = LocalDate.of(2026, 6, 25),
            isPlan = true
        )
        saveIntervalsWeekCache(
            prefs = prefs,
            apiKey = "api-key-a",
            weekStart = weekStart,
            weekEnd = weekEnd,
            data = WeekTrainingData(
                activities = emptyList(),
                plans = listOf(remoteIdMatchedPlan, externalIdMatchedPlan, keptPlan)
            )
        )

        removeCalendarPlanFromIntervalsCaches(
            prefs = prefs,
            apiKey = "api-key-a",
            plan = trainingCacheItem(
                id = "local-different-id",
                remoteId = "shared-remote-id",
                externalId = null,
                type = "Run",
                date = LocalDate.of(2026, 6, 23),
                isPlan = true
            )
        )
        removeCalendarPlanFromIntervalsCaches(
            prefs = prefs,
            apiKey = "api-key-a",
            plan = trainingCacheItem(
                id = "another-local-different-id",
                remoteId = "another-remote-id",
                externalId = "shared-external-id",
                type = "Run",
                date = LocalDate.of(2026, 6, 24),
                isPlan = true
            )
        )

        val plans = loadIntervalsWeekCache(prefs, "api-key-a", weekStart, weekEnd)?.plans.orEmpty()
        assertEquals(listOf("cached-kept-plan"), plans.map { it.id })
    }

    @Test
    fun withLocalStrengthPlans_matchesRemotePlanAndKeepsUnmatchedLocalPlan() {
        val date = LocalDate.of(2026, 6, 23)
        val matchedPlan = defaultStrengthPlans().first().copy(id = 20, name = "원격 매칭")
        val localOnlyPlan = defaultStrengthPlans().last().copy(id = 21, name = "로컬 유지")
        val scheduledMatched = scheduledStrengthPlan(matchedPlan, date)
        val scheduledLocalOnly = scheduledStrengthPlan(localOnlyPlan, date.plusDays(1))
        val remotePlan = trainingCacheItem(
            id = "remote-strength-plan",
            remoteId = "remote-strength-plan",
            externalId = scheduledMatched.externalId,
            type = "Weight Training",
            date = date,
            isPlan = true,
            matchedStrengthPlan = null
        )

        val merged = listOf(remotePlan).withLocalStrengthPlans(
            scheduledPlans = listOf(scheduledMatched, scheduledLocalOnly),
            start = date,
            end = date.plusDays(6)
        )

        assertEquals(2, merged.size)
        val matchedRemote = merged.first { it.id == "remote-strength-plan" }
        val localOnly = merged.first { it.externalId == scheduledLocalOnly.externalId }
        assertSame(matchedPlan, matchedRemote.matchedStrengthPlan)
        assertTrue(localOnly.id.startsWith("local-"))
        assertEquals("로컬 유지", localOnly.matchedStrengthPlan?.name)
        assertFalse(localOnly.isLocalOnlyStrengthResult)
        assertTrue(localOnly.isPlan)
    }
}

private fun scheduledStrengthPlan(
    plan: StrengthWorkoutPlan,
    date: LocalDate,
): ScheduledStrengthPlan {
    return ScheduledStrengthPlan(
        id = plan.scheduledStrengthPlanId(date),
        date = date,
        plan = plan,
        uploadedToIntervals = true,
        externalId = plan.intervalsPlanExternalId(date)
    )
}

private fun trainingCacheItem(
    id: String,
    type: String,
    date: LocalDate,
    isPlan: Boolean,
    remoteId: String = id,
    externalId: String? = "$id-external",
    matchedStrengthPlan: StrengthWorkoutPlan? = null,
    blocks: List<PlanBlock> = emptyList(),
    actualRunningBlocks: List<PlanBlock> = emptyList(),
    actualRunningRoutePoints: List<RunningRoutePoint> = emptyList(),
    pairedPlan: TrainingItem? = null,
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
        timeLabel = if (isPlan) "Plan" else "07:30",
        durationSeconds = blocks.sumOf { it.durationSeconds }.takeIf { it > 0 },
        distanceMeters = null,
        weightLiftedKg = null,
        load = null,
        fitness = null,
        fatigue = null,
        form = null,
        description = null,
        blocks = blocks,
        isPlan = isPlan,
        matchedStrengthPlan = matchedStrengthPlan,
        actualRunningBlocks = actualRunningBlocks,
        actualRunningRoutePoints = actualRunningRoutePoints,
        pairedPlan = pairedPlan,
        workoutDocJson = workoutDocJson
    )
}

private fun cachePlanBlock(
    index: Int,
    targetText: String,
    durationSeconds: Int = 60,
): PlanBlock {
    return PlanBlock(
        index = index,
        title = "Block ${index + 1}",
        kind = "work",
        targetText = targetText,
        durationSeconds = durationSeconds,
        startSecond = index * durationSeconds,
        endSecond = (index + 1) * durationSeconds,
        isRecovery = false
    )
}

private class CacheMemorySharedPreferences : SharedPreferences {
    private val values = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = values.toMutableMap()

    override fun getString(key: String?, defValue: String?): String? {
        return values[key] as? String ?: defValue
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        @Suppress("UNCHECKED_CAST")
        return values[key] as? MutableSet<String> ?: defValues
    }

    override fun getInt(key: String?, defValue: Int): Int = values[key] as? Int ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = values[key] as? Long ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = values[key] as? Float ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = values[key] as? Boolean ?: defValue
    override fun contains(key: String?): Boolean = values.containsKey(key)
    override fun edit(): SharedPreferences.Editor = Editor()
    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?,
    ) = Unit

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?,
    ) = Unit

    private inner class Editor : SharedPreferences.Editor {
        override fun putString(key: String?, value: String?): SharedPreferences.Editor = apply {
            if (key != null) values[key] = value
        }

        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = apply {
            if (key != null) this@CacheMemorySharedPreferences.values[key] = values
        }

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor = apply {
            if (key != null) values[key] = value
        }

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor = apply {
            if (key != null) values[key] = value
        }

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = apply {
            if (key != null) values[key] = value
        }

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = apply {
            if (key != null) values[key] = value
        }

        override fun remove(key: String?): SharedPreferences.Editor = apply {
            if (key != null) values.remove(key)
        }

        override fun clear(): SharedPreferences.Editor = apply {
            values.clear()
        }

        override fun commit(): Boolean = true
        override fun apply() = Unit
    }
}

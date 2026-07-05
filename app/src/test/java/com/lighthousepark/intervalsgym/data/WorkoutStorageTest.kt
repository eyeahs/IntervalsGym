package com.lighthousepark.intervalsgym.data

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

import android.content.SharedPreferences
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutStorageTest {
    @Test
    fun visiblePlanDescription_hidesInternalMarkers() {
        val description = """
            설명
            $INTERVALS_GYM_STRENGTH_PLAN_PREFIX encoded
            로컬 러닝 기록
            로컬 러닝 기록 · Garmin 결과 대기
            본문
        """.trimIndent()

        assertEquals("설명\n본문", description.visiblePlanDescription())
    }

    @Test
    fun workoutDetailDescription_showsRawWeightResultDescriptionWhenPlanIsUnmatched() {
        val rawDescription = "원본 웨이트 설명\nSet 1: 10kg x 8회"
        val result = trainingItem(
            type = "Weight Training",
            isPlan = false,
            description = rawDescription
        )
        val matchedPlan = defaultStrengthPlans().first()
        val pairedPlan = trainingItem(
            id = "plan-1",
            type = "Weight Training",
            isPlan = true,
            description = matchedPlan.toIntervalsPlanDescription(),
            matchedStrengthPlan = matchedPlan
        )

        assertEquals(rawDescription, result.workoutDetailDescription(isWeightTrainingItem = true, strengthPlan = null))
        assertEquals(rawDescription, result.copy(pairedPlan = pairedPlan).workoutDetailDescription(isWeightTrainingItem = true, strengthPlan = null))
        assertEquals("", result.workoutDetailDescription(isWeightTrainingItem = true, strengthPlan = matchedPlan))
        assertEquals("", result.copy(pairedPlan = pairedPlan).workoutDetailDescription(isWeightTrainingItem = true, strengthPlan = matchedPlan))
    }

    @Test
    fun finalizeRestEvents_closesOnlyActiveOpenRest() {
        val events = listOf(
            StrengthRestEvent(
                id = 1,
                afterSetSequence = 1,
                exerciseEntryId = 1,
                exerciseTitle = "스쿼트",
                setRecordId = 1,
                setIndex = 0,
                startedAtMillis = 1000L,
                plannedSeconds = 60,
                targetEndAtMillis = 61000L,
                endedAtMillis = null,
                endReason = null
            ),
            StrengthRestEvent(
                id = 2,
                afterSetSequence = 2,
                exerciseEntryId = 1,
                exerciseTitle = "스쿼트",
                setRecordId = 2,
                setIndex = 1,
                startedAtMillis = 2000L,
                plannedSeconds = 60,
                targetEndAtMillis = 62000L,
                endedAtMillis = null,
                endReason = null
            )
        )

        val finalized = finalizeRestEvents(events, activeRestEventId = 2, endedAtMillis = 5000L, reason = "stopped")

        assertEquals(null, finalized[0].endedAtMillis)
        assertEquals(5000L, finalized[1].endedAtMillis)
        assertEquals("stopped", finalized[1].endReason)
    }

    @Test
    fun withLocalRunningResults_addsUnmatchedLocalWorkoutInsideRange() {
        val startedAtMillis = LocalDateTime.of(2026, 6, 23, 7, 30)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val localWorkout = CompletedRunningWorkout(
            id = "run-1",
            name = "러닝 Plan",
            startedAtMillis = startedAtMillis,
            endedAtMillis = startedAtMillis + 1_800_000L,
            durationSeconds = 1800,
            warmupSeconds = 60,
            estimatedDistanceMeters = 3000.0,
            blocks = emptyList(),
            actualBlocks = emptyList(),
            uploadedToIntervals = false
        )

        val items = emptyList<TrainingItem>().withLocalRunningResults(
            history = listOf(localWorkout),
            weekStart = LocalDate.of(2026, 6, 22),
            weekEnd = LocalDate.of(2026, 6, 28)
        )

        assertEquals(1, items.size)
        assertTrue(items.single().isLocalOnlyRunningResult)
        assertFalse(items.single().isPlan)
        assertEquals(3000.0, items.single().distanceMeters ?: 0.0, 0.01)
    }

    @Test
    fun withLocalRunningResults_skipsWorkoutMatchedByRemoteResultTime() {
        val startedAt = LocalDateTime.of(2026, 6, 23, 7, 30)
        val startedAtMillis = startedAt
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val remoteResult = trainingItem(
            id = "garmin-run-1",
            remoteId = "garmin-run-1",
            type = "Run",
            startedAt = startedAt.plusMinutes(8),
            durationSeconds = 1800
        )
        val localWorkout = completedRunningWorkoutForStorage(
            id = "run-1",
            name = "러닝 Plan",
            startedAtMillis = startedAtMillis,
            endedAtMillis = startedAtMillis + 1_800_000L
        )

        val items = listOf(remoteResult).withLocalRunningResults(
            history = listOf(localWorkout),
            weekStart = LocalDate.of(2026, 6, 22),
            weekEnd = LocalDate.of(2026, 6, 28)
        )

        assertEquals(1, items.size)
        assertEquals(remoteResult.id, items.single().id)
        assertFalse(items.single().isLocalOnlyRunningResult)
    }

    @Test
    fun withLocalRunningResults_skipsWorkoutAlreadyRepresentedByLocalResult() {
        val startedAt = LocalDateTime.of(2026, 6, 23, 7, 30)
        val startedAtMillis = startedAt
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val existingLocalResult = trainingItem(
            id = "local-running-run-1",
            remoteId = "run-1",
            type = "Run",
            startedAt = startedAt,
            durationSeconds = 1800,
            isLocalOnlyRunningResult = true
        )
        val localWorkout = completedRunningWorkoutForStorage(
            id = "run-1",
            name = "러닝 Plan",
            startedAtMillis = startedAtMillis,
            endedAtMillis = startedAtMillis + 1_800_000L
        )

        val items = listOf(existingLocalResult).withLocalRunningResults(
            history = listOf(localWorkout),
            weekStart = LocalDate.of(2026, 6, 22),
            weekEnd = LocalDate.of(2026, 6, 28)
        )

        assertEquals(1, items.size)
        assertEquals(existingLocalResult.id, items.single().id)
        assertTrue(items.single().isLocalOnlyRunningResult)
    }

    @Test
    fun withLocalStrengthResults_addsUnmatchedLocalWorkoutInsideRange() {
        val startedAtMillis = LocalDateTime.of(2026, 6, 23, 19, 30)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val localWorkout = completedStrengthWorkoutForStorage(
            id = "strength-1",
            planName = "하체",
            startedAtMillis = startedAtMillis,
            endedAtMillis = startedAtMillis + 3_600_000L
        )

        val items = emptyList<TrainingItem>().withLocalStrengthResults(
            history = listOf(localWorkout),
            weekStart = LocalDate.of(2026, 6, 22),
            weekEnd = LocalDate.of(2026, 6, 28)
        )

        assertEquals(1, items.size)
        assertTrue(items.single().isLocalOnlyStrengthResult)
        assertFalse(items.single().isPlan)
        assertEquals(localWorkout.id, items.single().matchedStrengthWorkout?.id)
        assertEquals(localWorkout.entries.totalVolumeKg(), items.single().weightLiftedKg ?: 0.0, 0.01)
    }

    @Test
    fun withLocalStrengthResults_skipsWorkoutMatchedByRemoteExternalId() {
        val startedAt = LocalDateTime.of(2026, 6, 23, 19, 30)
        val startedAtMillis = startedAt
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val localWorkout = completedStrengthWorkoutForStorage(
            id = "strength-remote-match",
            planName = "하체",
            startedAtMillis = startedAtMillis,
            endedAtMillis = startedAtMillis + 3_600_000L
        )
        val remoteResult = trainingItem(
            id = "intervals-strength-1",
            externalId = localWorkout.intervalsExternalId,
            name = "하체",
            type = "Weight Training",
            startedAt = startedAt.plusMinutes(20),
            durationSeconds = localWorkout.durationSeconds
        )

        val items = listOf(remoteResult).withLocalStrengthResults(
            history = listOf(localWorkout),
            weekStart = LocalDate.of(2026, 6, 22),
            weekEnd = LocalDate.of(2026, 6, 28)
        )

        assertEquals(1, items.size)
        assertEquals(remoteResult.id, items.single().id)
        assertFalse(items.single().isLocalOnlyStrengthResult)
        assertEquals(localWorkout.id, items.single().matchedStrengthWorkout?.id)
    }

    @Test
    fun savedRunningWorkoutPlan_roundTripsToExecutableTrainingItem() {
        val source = TrainingItem(
            id = "plan-remote-1",
            remoteId = "remote-1",
            externalId = "external-1",
            name = "UAE 40/20",
            type = "Run",
            date = LocalDate.of(2026, 6, 23),
            startedAt = null,
            timeLabel = "Plan",
            durationSeconds = null,
            distanceMeters = null,
            weightLiftedKg = null,
            load = null,
            fitness = null,
            fatigue = null,
            form = null,
            description = "12:00 pace",
            blocks = emptyList(),
            isPlan = true
        )
        val blocks = listOf(
            PlanBlock(
                index = 0,
                title = "Block 1",
                kind = "work",
                targetText = "12:00",
                durationSeconds = 60,
                startSecond = 0,
                endSecond = 60,
                isRecovery = false
            )
        )

        val saved = source.toSavedRunningWorkoutPlan(blocks)
        val executable = saved?.toTrainingItem()

        assertEquals("saved-running-external-1", saved?.id)
        assertEquals(60, saved?.durationSeconds)
        assertEquals(false, executable?.isPlan)
        assertEquals(TrainingSportType.RUNNING, executable?.sportType())
        assertEquals(1, executable?.blocks?.size)
    }

    @Test
    fun moveScheduledStrengthPlan_updatesStoredDateAndIds() {
        val sourceDate = LocalDate.of(2026, 6, 23)
        val targetDate = LocalDate.of(2026, 6, 25)
        val plan = defaultStrengthPlans().first().copy(id = 42, name = "런닝보강")
        val scheduledPlan = ScheduledStrengthPlan(
            id = plan.scheduledStrengthPlanId(sourceDate),
            date = sourceDate,
            plan = plan,
            uploadedToIntervals = true,
            externalId = plan.intervalsPlanExternalId(sourceDate)
        )
        val item = TrainingItem(
            id = "local-${scheduledPlan.id}",
            remoteId = scheduledPlan.id,
            externalId = scheduledPlan.externalId,
            name = plan.name,
            type = "Weight Training",
            date = sourceDate,
            startedAt = sourceDate.atStartOfDay(),
            timeLabel = "Plan",
            durationSeconds = null,
            distanceMeters = null,
            weightLiftedKg = null,
            load = null,
            fitness = null,
            fatigue = null,
            form = null,
            description = null,
            blocks = emptyList(),
            isPlan = true,
            matchedStrengthPlan = plan
        )

        val moveResult = listOf(scheduledPlan).withMovedScheduledStrengthPlan(item, targetDate)
        val movedPlan = moveResult.movedPlan

        assertEquals(targetDate, movedPlan?.date)
        assertEquals(false, movedPlan?.uploadedToIntervals)
        assertEquals(1, moveResult.plans.size)
        assertEquals(targetDate, moveResult.plans.single().date)
        assertEquals(plan.scheduledStrengthPlanId(targetDate), moveResult.plans.single().id)
        assertEquals(plan.intervalsPlanExternalId(targetDate), moveResult.plans.single().externalId)
    }

    @Test
    fun strengthPlanDescription_roundTripsEmbeddedPlanJson() {
        val plan = defaultStrengthPlans().first().copy(id = 88, name = "임베디드 Plan")
        val encoded = java.util.Base64.getEncoder().encodeToString(
            listOf(plan).toJsonString().toByteArray()
        )
        val description = """
            IntervalsGym 웨이트 Plan
            $INTERVALS_GYM_STRENGTH_PLAN_PREFIX $encoded
        """.trimIndent()

        val parsed = description.toIntervalsGymStrengthPlan()

        requireNotNull(parsed)
        assertEquals(plan.id, parsed.id)
        assertEquals(plan.name, parsed.name)
        assertEquals(plan.entries.map { it.title }, parsed.entries.map { it.title })
        assertEquals(plan.entries.first().records.size, parsed.entries.first().records.size)
    }

    @Test
    fun strengthPlanDescription_returnsNullForMalformedEmbeddedPlanJson() {
        val description = """
            IntervalsGym 웨이트 Plan
            $INTERVALS_GYM_STRENGTH_PLAN_PREFIX not-base64
        """.trimIndent()

        assertEquals(null, description.toIntervalsGymStrengthPlan())
    }

    @Test
    fun upsertScheduledStrengthPlan_replacesSameExternalIdAndPersistsLatestPlan() {
        val prefs = MemorySharedPreferences()
        val date = LocalDate.of(2026, 7, 1)
        val originalPlan = defaultStrengthPlans().first().copy(id = 11, name = "before")
        val replacementPlan = originalPlan.copy(name = "after")
        val original = ScheduledStrengthPlan(
            id = originalPlan.scheduledStrengthPlanId(date),
            date = date,
            plan = originalPlan,
            uploadedToIntervals = true,
            externalId = originalPlan.intervalsPlanExternalId(date)
        )
        val replacement = original.copy(plan = replacementPlan, uploadedToIntervals = false)

        upsertScheduledStrengthPlan(prefs, original)
        upsertScheduledStrengthPlan(prefs, replacement)

        val plans = loadScheduledStrengthPlans(prefs)
        assertEquals(1, plans.size)
        assertEquals("after", plans.single().plan.name)
        assertFalse(plans.single().uploadedToIntervals)
    }

    @Test
    fun removeScheduledStrengthPlan_matchesLocalIdRemoteIdOrExternalId() {
        val prefs = MemorySharedPreferences()
        val firstDate = LocalDate.of(2026, 7, 1)
        val secondDate = LocalDate.of(2026, 7, 2)
        val firstPlan = defaultStrengthPlans().first().copy(id = 12, name = "remove")
        val secondPlan = defaultStrengthPlans().last().copy(id = 13, name = "keep")
        val removable = ScheduledStrengthPlan(
            id = firstPlan.scheduledStrengthPlanId(firstDate),
            date = firstDate,
            plan = firstPlan,
            uploadedToIntervals = true,
            externalId = firstPlan.intervalsPlanExternalId(firstDate)
        )
        val keep = ScheduledStrengthPlan(
            id = secondPlan.scheduledStrengthPlanId(secondDate),
            date = secondDate,
            plan = secondPlan,
            uploadedToIntervals = true,
            externalId = secondPlan.intervalsPlanExternalId(secondDate)
        )
        upsertScheduledStrengthPlan(prefs, removable)
        upsertScheduledStrengthPlan(prefs, keep)

        removeScheduledStrengthPlan(
            prefs,
            trainingItem(
                id = "local-${removable.id}",
                remoteId = removable.id,
                externalId = removable.externalId,
                type = "Weight Training",
                isPlan = true,
                matchedStrengthPlan = firstPlan
            )
        )

        val plans = loadScheduledStrengthPlans(prefs)
        assertEquals(1, plans.size)
        assertEquals(keep.externalId, plans.single().externalId)
    }

    @Test
    fun loadScheduledStrengthPlans_derivesMissingLegacyExternalId() {
        val prefs = MemorySharedPreferences()
        val date = LocalDate.of(2026, 7, 3)
        val plan = defaultStrengthPlans().first().copy(id = 31, name = "legacy")
        val legacyJson = org.json.JSONArray().put(
            org.json.JSONObject()
                .put("id", plan.scheduledStrengthPlanId(date))
                .put("date", date.toString())
                .put("uploadedToIntervals", true)
                .put("planJson", listOf(plan).toJsonString())
        )

        prefs.edit().putString(SCHEDULED_STRENGTH_PLANS_PREF, legacyJson.toString()).apply()

        val plans = loadScheduledStrengthPlans(prefs)
        assertEquals(1, plans.size)
        assertEquals(plan.scheduledStrengthPlanId(date), plans.single().id)
        assertEquals(plan.intervalsPlanExternalId(date), plans.single().externalId)
        assertTrue(plans.single().uploadedToIntervals)
    }

    @Test
    fun loadStrengthPlans_readsOnlySavedPlanKeyNotScheduledCalendarPlans() {
        val prefs = MemorySharedPreferences()
        val date = LocalDate.of(2026, 7, 4)
        val scheduledOnlyPlan = defaultStrengthPlans().first().copy(id = 41, name = "캘린더 전용 Plan")
        val scheduledPlan = ScheduledStrengthPlan(
            id = scheduledOnlyPlan.scheduledStrengthPlanId(date),
            date = date,
            plan = scheduledOnlyPlan,
            uploadedToIntervals = true,
            externalId = scheduledOnlyPlan.intervalsPlanExternalId(date)
        )

        upsertScheduledStrengthPlan(prefs, scheduledPlan)

        val plansWithoutSavedKey = loadStrengthPlans(prefs)
        assertEquals(defaultStrengthPlans().map { it.id }, plansWithoutSavedKey.map { it.id })
        assertFalse(plansWithoutSavedKey.any { it.name == "캘린더 전용 Plan" })

        val savedPlan = defaultStrengthPlans().last().copy(id = 42, name = "로컬 저장 Plan")
        prefs.edit().putString(STRENGTH_PLANS_PREF, listOf(savedPlan).toJsonString()).apply()

        val plansWithSavedKey = loadStrengthPlans(prefs)
        assertEquals(1, plansWithSavedKey.size)
        assertEquals("로컬 저장 Plan", plansWithSavedKey.single().name)
    }

    @Test
    fun scheduledStrengthPlanOperations_doNotMutateSavedStrengthPlans() {
        val prefs = MemorySharedPreferences()
        val date = LocalDate.of(2026, 7, 5)
        val savedPlan = defaultStrengthPlans().first().copy(id = 51, name = "로그인과 무관한 로컬 Plan")
        prefs.edit().putString(STRENGTH_PLANS_PREF, listOf(savedPlan).toJsonString()).apply()

        val scheduledOnlyPlan = defaultStrengthPlans().last().copy(id = 52, name = "동기화 캘린더 Plan")
        val scheduledPlan = ScheduledStrengthPlan(
            id = scheduledOnlyPlan.scheduledStrengthPlanId(date),
            date = date,
            plan = scheduledOnlyPlan,
            uploadedToIntervals = true,
            externalId = scheduledOnlyPlan.intervalsPlanExternalId(date)
        )
        val scheduledTrainingItem = trainingItem(
            id = "local-${scheduledPlan.id}",
            remoteId = scheduledPlan.id,
            externalId = scheduledPlan.externalId,
            type = "Weight Training",
            isPlan = true,
            matchedStrengthPlan = scheduledOnlyPlan
        )

        upsertScheduledStrengthPlan(prefs, scheduledPlan)
        moveScheduledStrengthPlan(prefs, scheduledTrainingItem, date.plusDays(1))
        removeScheduledStrengthPlan(prefs, scheduledTrainingItem.copy(externalId = scheduledOnlyPlan.intervalsPlanExternalId(date.plusDays(1))))

        val savedPlans = loadStrengthPlans(prefs)
        assertEquals(1, savedPlans.size)
        assertEquals(savedPlan.id, savedPlans.single().id)
        assertEquals("로그인과 무관한 로컬 Plan", savedPlans.single().name)
    }

    @Test
    fun withLocalStrengthPlans_preservesExistingMatchedRemotePlan() {
        val date = LocalDate.of(2026, 7, 4)
        val localPlan = defaultStrengthPlans().first().copy(id = 41, name = "로컬 scheduled")
        val remoteEmbeddedPlan = localPlan.copy(name = "원격 embedded")
        val scheduledPlan = ScheduledStrengthPlan(
            id = localPlan.scheduledStrengthPlanId(date),
            date = date,
            plan = localPlan,
            uploadedToIntervals = true,
            externalId = localPlan.intervalsPlanExternalId(date)
        )
        val remotePlan = trainingItem(
            id = "remote-strength-plan",
            remoteId = "remote-strength-plan",
            externalId = scheduledPlan.externalId,
            type = "Weight Training",
            isPlan = true,
            matchedStrengthPlan = remoteEmbeddedPlan
        )

        val merged = listOf(remotePlan).withLocalStrengthPlans(
            scheduledPlans = listOf(scheduledPlan),
            start = date,
            end = date
        )

        assertEquals(1, merged.size)
        assertEquals("원격 embedded", merged.single().matchedStrengthPlan?.name)
    }

    @Test
    fun appendStrengthWorkoutHistory_deduplicatesExistingWorkoutId() {
        val prefs = MemorySharedPreferences()
        val original = completedStrengthWorkoutForStorage(
            id = "strength-same",
            planName = "before",
            startedAtMillis = 1_000L,
            endedAtMillis = 61_000L
        )
        val replacement = original.copy(planName = "after", uploadedToIntervals = true)

        appendStrengthWorkoutHistory(prefs, original)
        appendStrengthWorkoutHistory(prefs, replacement)

        val history = loadCompletedStrengthWorkoutHistory(prefs)
        assertEquals(1, history.size)
        assertEquals("after", history.single().planName)
        assertTrue(history.single().uploadedToIntervals)
    }

    @Test
    fun appendRunningWorkoutHistory_deduplicatesExistingWorkoutId() {
        val prefs = MemorySharedPreferences()
        val original = completedRunningWorkoutForStorage(
            id = "running-same",
            name = "before",
            startedAtMillis = 1_000L,
            endedAtMillis = 61_000L
        )
        val replacement = original.copy(name = "after", uploadedToIntervals = true)

        appendRunningWorkoutHistory(prefs, original)
        appendRunningWorkoutHistory(prefs, replacement)

        val history = loadCompletedRunningWorkoutHistory(prefs)
        assertEquals(1, history.size)
        assertEquals("after", history.single().name)
        assertTrue(history.single().uploadedToIntervals)
    }

    @Test
    fun savedRunningWorkoutPlan_upsertReplacesSameIdAndKeepsLatestFirst() {
        val prefs = MemorySharedPreferences()
        val original = savedRunningWorkoutPlanForStorage(id = "saved-1", name = "before")
        val replacement = original.copy(name = "after", savedAtMillis = 2_000L)
        val other = savedRunningWorkoutPlanForStorage(id = "saved-2", name = "other")

        upsertSavedRunningWorkoutPlan(prefs, other)
        upsertSavedRunningWorkoutPlan(prefs, original)
        upsertSavedRunningWorkoutPlan(prefs, replacement)

        val plans = loadSavedRunningWorkoutPlans(prefs)
        assertEquals(listOf("saved-1", "saved-2"), plans.map { it.id })
        assertEquals("after", plans.first().name)
    }

    @Test
    fun deleteSavedRunningWorkoutPlan_removesOnlyTargetPlan() {
        val prefs = MemorySharedPreferences()
        val first = savedRunningWorkoutPlanForStorage(id = "saved-1", name = "first")
        val second = savedRunningWorkoutPlanForStorage(id = "saved-2", name = "second")
        upsertSavedRunningWorkoutPlan(prefs, first)
        upsertSavedRunningWorkoutPlan(prefs, second)

        deleteSavedRunningWorkoutPlan(prefs, "saved-1")

        val plans = loadSavedRunningWorkoutPlans(prefs)
        assertEquals(1, plans.size)
        assertEquals("saved-2", plans.single().id)
    }

    @Test
    fun activeStrengthSession_roundTripsCurrentSetAndRestState() {
        val prefs = MemorySharedPreferences()
        val plan = defaultStrengthPlans().first()
        val setEvent = strengthSetEventForStorage(plan.entries.first())
        val restEvent = StrengthRestEvent(
            id = 1,
            afterSetSequence = setEvent.sequence,
            exerciseEntryId = plan.entries.first().id,
            exerciseTitle = plan.entries.first().title,
            setRecordId = plan.entries.first().records.first().id,
            setIndex = 0,
            startedAtMillis = 10_000L,
            plannedSeconds = 60,
            targetEndAtMillis = System.currentTimeMillis() + 60_000L,
            endedAtMillis = null,
            endReason = null
        )
        val session = ActiveStrengthSession(
            planId = plan.id,
            planName = plan.name,
            entries = plan.entries,
            hasStarted = true,
            workoutStartedAtMillis = 1_000L,
            isSetScreenVisible = true,
            currentExerciseIndex = 1,
            currentSetIndex = 2,
            pendingExerciseIndex = 2,
            pendingSetIndex = 0,
            restEndAtMillis = restEvent.targetEndAtMillis,
            isRestSheetVisible = true,
            restTitle = "스쿼트",
            setEvents = listOf(setEvent),
            restEvents = listOf(restEvent),
            activeRestEventId = restEvent.id
        )

        prefs.edit().putString(ACTIVE_STRENGTH_SESSION_PREF, session.toJsonString()).apply()
        val restored = loadActiveStrengthSession(prefs)

        requireNotNull(restored)
        assertEquals(plan.id, restored.planId)
        assertTrue(restored.hasStarted)
        assertEquals(1, restored.currentExerciseIndex)
        assertEquals(2, restored.currentSetIndex)
        assertEquals(2, restored.pendingExerciseIndex)
        assertEquals(0, restored.pendingSetIndex)
        assertEquals(restEvent.id, restored.activeRestEventId)
        assertEquals(1, restored.setEvents.size)
        assertEquals(1, restored.restEvents.size)
        assertEquals(null, restored.restEvents.single().endedAtMillis)
    }

    @Test
    fun activeStrengthSession_expiredRestRestoresPendingSetAndFinalizesRestEvent() {
        val prefs = MemorySharedPreferences()
        val plan = defaultStrengthPlans().first()
        val setEvent = strengthSetEventForStorage(plan.entries.first())
        val expiredRest = StrengthRestEvent(
            id = 2,
            afterSetSequence = setEvent.sequence,
            exerciseEntryId = plan.entries.first().id,
            exerciseTitle = plan.entries.first().title,
            setRecordId = plan.entries.first().records.first().id,
            setIndex = 0,
            startedAtMillis = 1_000L,
            plannedSeconds = 60,
            targetEndAtMillis = 2_000L,
            endedAtMillis = null,
            endReason = null
        )
        val session = ActiveStrengthSession(
            planId = plan.id,
            planName = plan.name,
            entries = plan.entries,
            hasStarted = true,
            workoutStartedAtMillis = 1_000L,
            isSetScreenVisible = true,
            currentExerciseIndex = 0,
            currentSetIndex = 0,
            pendingExerciseIndex = 1,
            pendingSetIndex = 2,
            restEndAtMillis = 2_000L,
            isRestSheetVisible = true,
            restTitle = "스쿼트",
            setEvents = listOf(setEvent),
            restEvents = listOf(expiredRest),
            activeRestEventId = expiredRest.id
        )

        prefs.edit().putString(ACTIVE_STRENGTH_SESSION_PREF, session.toJsonString()).apply()
        val restored = loadActiveStrengthSession(prefs)

        requireNotNull(restored)
        assertEquals(1, restored.currentExerciseIndex)
        assertEquals(2, restored.currentSetIndex)
        assertEquals(null, restored.pendingExerciseIndex)
        assertEquals(null, restored.pendingSetIndex)
        assertEquals(0L, restored.restEndAtMillis)
        assertFalse(restored.isRestSheetVisible)
        assertEquals(null, restored.activeRestEventId)
        assertEquals(2_000L, restored.restEvents.single().endedAtMillis)
        assertEquals("finished", restored.restEvents.single().endReason)
    }

    @Test
    fun strengthPlansWithLatestCompletedWorkout_useNewestAppliedHistoryAndResetCompletedFlags() {
        val plan = defaultStrengthPlans().first()
        val oldEntries = plan.entries.map { entry ->
            entry.copy(records = entry.records.map { it.copy(weightKg = "40", completed = true) })
        }
        val newEntries = plan.entries.map { entry ->
            entry.copy(records = entry.records.map { it.copy(weightKg = "80", completed = true) })
        }
        val ignoredEntries = plan.entries.map { entry ->
            entry.copy(records = entry.records.map { it.copy(weightKg = "120", completed = true) })
        }
        val oldWorkout = completedStrengthWorkoutForStorage(
            id = "old",
            planName = plan.name,
            startedAtMillis = 1_000L,
            endedAtMillis = 61_000L,
            entries = oldEntries
        )
        val newWorkout = completedStrengthWorkoutForStorage(
            id = "new",
            planName = plan.name,
            startedAtMillis = 3_000L,
            endedAtMillis = 63_000L,
            entries = newEntries
        )
        val ignoredWorkout = completedStrengthWorkoutForStorage(
            id = "ignored",
            planName = plan.name,
            startedAtMillis = 5_000L,
            endedAtMillis = 65_000L,
            entries = ignoredEntries
        ).copy(appliedToPlan = false)

        val updated = listOf(plan).withLatestCompletedWorkout(
            history = listOf(oldWorkout, ignoredWorkout, newWorkout)
        )

        assertEquals("80", updated.single().entries.first().records.first().weightKg)
        assertFalse(updated.single().entries.first().records.first().completed)
    }

    @Test
    fun activeStrengthSessionWithLatestCompletedWorkout_updatesOnlyBeforeWorkoutStarts() {
        val plan = defaultStrengthPlans().first()
        val completedEntries = plan.entries.map { entry ->
            entry.copy(records = entry.records.map { it.copy(weightKg = "90", completed = true) })
        }
        val workout = completedStrengthWorkoutForStorage(
            id = "history",
            planName = plan.name,
            startedAtMillis = 3_000L,
            endedAtMillis = 63_000L,
            entries = completedEntries
        )
        val idleSession = ActiveStrengthSession(
            planId = plan.id,
            planName = plan.name,
            entries = plan.entries,
            hasStarted = false,
            workoutStartedAtMillis = 0L,
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
        val startedSession = idleSession.copy(hasStarted = true)

        val updatedIdleSession = idleSession.withLatestCompletedWorkout(listOf(workout))
        val unchangedStartedSession = startedSession.withLatestCompletedWorkout(listOf(workout))

        assertEquals("90", updatedIdleSession.entries.first().records.first().weightKg)
        assertFalse(updatedIdleSession.entries.first().records.first().completed)
        assertEquals(plan.entries.first().records.first().weightKg, unchangedStartedSession.entries.first().records.first().weightKg)
    }
}

private fun trainingItem(
    id: String = "item-1",
    remoteId: String = id,
    externalId: String? = null,
    name: String = "테스트",
    type: String = "Run",
    isPlan: Boolean = false,
    description: String? = null,
    matchedStrengthPlan: StrengthWorkoutPlan? = null,
    startedAt: LocalDateTime? = LocalDate.of(2026, 6, 24).atStartOfDay(),
    durationSeconds: Int? = null,
    isLocalOnlyRunningResult: Boolean = false,
): TrainingItem {
    return TrainingItem(
        id = id,
        remoteId = remoteId,
        externalId = externalId,
        name = name,
        type = type,
        date = (startedAt?.toLocalDate() ?: LocalDate.of(2026, 6, 24)),
        startedAt = startedAt,
        timeLabel = if (isPlan) "Plan" else "08:00",
        durationSeconds = durationSeconds,
        distanceMeters = null,
        weightLiftedKg = null,
        load = null,
        fitness = null,
        fatigue = null,
        form = null,
        description = description,
        blocks = emptyList(),
        isPlan = isPlan,
        matchedStrengthPlan = matchedStrengthPlan,
        isLocalOnlyRunningResult = isLocalOnlyRunningResult
    )
}

private fun completedStrengthWorkoutForStorage(
    id: String,
    planName: String,
    startedAtMillis: Long,
    endedAtMillis: Long,
    entries: List<StrengthPlanEntry>? = null,
): CompletedStrengthWorkout {
    val plan = defaultStrengthPlans().first()
    return CompletedStrengthWorkout(
        id = id,
        planId = plan.id,
        planName = planName,
        startedAtMillis = startedAtMillis,
        endedAtMillis = endedAtMillis,
        durationSeconds = ((endedAtMillis - startedAtMillis) / 1000L).toInt(),
        intervalsExternalId = id,
        entries = entries ?: plan.entries,
        setEvents = emptyList(),
        restEvents = emptyList(),
        rpe = 7,
        trainingLoad = plan.entries.strengthTrainingLoad(7),
        uploadedToIntervals = false
    )
}

private fun completedRunningWorkoutForStorage(
    id: String,
    name: String,
    startedAtMillis: Long,
    endedAtMillis: Long,
): CompletedRunningWorkout {
    return CompletedRunningWorkout(
        id = id,
        name = name,
        startedAtMillis = startedAtMillis,
        endedAtMillis = endedAtMillis,
        durationSeconds = ((endedAtMillis - startedAtMillis) / 1000L).toInt(),
        warmupSeconds = 0,
        estimatedDistanceMeters = 0.0,
        blocks = emptyList(),
        actualBlocks = emptyList(),
        uploadedToIntervals = false
    )
}

private fun savedRunningWorkoutPlanForStorage(
    id: String,
    name: String,
): SavedRunningWorkoutPlan {
    return SavedRunningWorkoutPlan(
        id = id,
        name = name,
        description = "1m 10:00 pace [6km/h 1%]",
        durationSeconds = 60,
        blocks = listOf(
            PlanBlock(
                index = 0,
                title = "Block 1",
                kind = "work",
                targetText = "6km/h · 1%",
                durationSeconds = 60,
                startSecond = 0,
                endSecond = 60,
                isRecovery = false
            )
        ),
        workoutDocJson = null,
        savedAtMillis = 1_000L
    )
}

private fun strengthSetEventForStorage(entry: StrengthPlanEntry): StrengthSetCompletionEvent {
    val record = entry.records.first()
    return StrengthSetCompletionEvent(
        sequence = 1,
        exerciseEntryId = entry.id,
        exerciseTitle = entry.title,
        exerciseGroup = entry.exercise.group,
        exerciseId = entry.exercise.id,
        equipment = entry.equipment,
        variation = entry.variation,
        setRecordId = record.id,
        setIndex = 0,
        weightKg = record.weightKg,
        reps = record.reps,
        targetRestSeconds = record.restSeconds.toIntOrNull() ?: entry.restSeconds,
        completedAtMillis = 10_000L
    )
}

private class MemorySharedPreferences : SharedPreferences {
    private val values = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = values.toMutableMap()

    override fun getString(key: String?, defValue: String?): String? {
        return values[key] as? String ?: defValue
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        @Suppress("UNCHECKED_CAST")
        return (values[key] as? Set<String>)?.toMutableSet() ?: defValues
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
        private val edits = mutableMapOf<String, Any?>()
        private val removals = mutableSetOf<String>()
        private var shouldClear = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor = apply {
            key?.let { edits[it] = value }
        }

        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = apply {
            key?.let { edits[it] = values }
        }

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor = apply {
            key?.let { edits[it] = value }
        }

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor = apply {
            key?.let { edits[it] = value }
        }

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = apply {
            key?.let { edits[it] = value }
        }

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = apply {
            key?.let { edits[it] = value }
        }

        override fun remove(key: String?): SharedPreferences.Editor = apply {
            key?.let { removals += it }
        }

        override fun clear(): SharedPreferences.Editor = apply {
            shouldClear = true
        }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            if (shouldClear) values.clear()
            removals.forEach(values::remove)
            edits.forEach { (key, value) -> values[key] = value }
        }
    }
}

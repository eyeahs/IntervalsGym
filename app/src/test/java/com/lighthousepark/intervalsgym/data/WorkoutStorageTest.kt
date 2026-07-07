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
    fun intervalsRoutineDescription_embedsLocalRoutineIdAndSnapshot() {
        val routine = defaultStrengthRoutines().first().copy(id = 88, name = "원본 Routine")
        val description = routine.toIntervalsRoutineDescription()

        assertEquals(88, description.toIntervalsGymStrengthRoutineId())
        assertEquals(88, description.toIntervalsGymStrengthRoutine()?.id)
        assertEquals("원본 Routine", description.toIntervalsGymStrengthRoutine()?.name)
        assertFalse(description.visibleRoutineDescription().contains(INTERVALS_GYM_STRENGTH_ROUTINE_ID_PREFIX))
    }

    @Test
    fun strengthRoutineEntryNote_roundTripsThroughStorageAndIntervalsDescription() {
        val note = "왼쪽 무릎 각도 확인"
        val routine = defaultStrengthRoutines().first().copy(
            entries = defaultStrengthRoutines().first().entries.mapIndexed { index, entry ->
                if (index == 0) entry.copy(note = note) else entry
            }
        )

        val restored = listOf(routine).toJsonString().toStrengthWorkoutRoutines().single()
        val description = routine.toIntervalsRoutineDescription()
        val embedded = description.toIntervalsGymStrengthRoutine()

        assertEquals(note, restored.entries.first().note)
        assertTrue(description.contains("메모: $note"))
        assertEquals(note, embedded?.entries?.first()?.note)
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
        val localSession = CompletedRunningSession(
            id = "run-1",
            name = "러닝 Routine",
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
            history = listOf(localSession),
            weekStart = LocalDate.of(2026, 6, 22),
            weekEnd = LocalDate.of(2026, 6, 28)
        )

        assertEquals(1, items.size)
        assertTrue(items.single().isLocalOnlyRunningResult)
        assertFalse(items.single().isRoutine)
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
        val localSession = completedRunningSessionForStorage(
            id = "run-1",
            name = "러닝 Routine",
            startedAtMillis = startedAtMillis,
            endedAtMillis = startedAtMillis + 1_800_000L
        )

        val items = listOf(remoteResult).withLocalRunningResults(
            history = listOf(localSession),
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
        val localSession = completedRunningSessionForStorage(
            id = "run-1",
            name = "러닝 Routine",
            startedAtMillis = startedAtMillis,
            endedAtMillis = startedAtMillis + 1_800_000L
        )

        val items = listOf(existingLocalResult).withLocalRunningResults(
            history = listOf(localSession),
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
        val localSession = completedStrengthSessionForStorage(
            id = "strength-1",
            routineName = "하체",
            startedAtMillis = startedAtMillis,
            endedAtMillis = startedAtMillis + 3_600_000L
        )

        val items = emptyList<TrainingItem>().withLocalStrengthResults(
            history = listOf(localSession),
            weekStart = LocalDate.of(2026, 6, 22),
            weekEnd = LocalDate.of(2026, 6, 28)
        )

        assertEquals(1, items.size)
        assertTrue(items.single().isLocalOnlyStrengthResult)
        assertFalse(items.single().isRoutine)
        assertEquals(localSession.id, items.single().matchedStrengthSession?.id)
        assertEquals(localSession.entries.totalVolumeKg(), items.single().weightLiftedKg ?: 0.0, 0.01)
    }

    @Test
    fun withLocalStrengthResults_skipsWorkoutMatchedByRemoteExternalId() {
        val startedAt = LocalDateTime.of(2026, 6, 23, 19, 30)
        val startedAtMillis = startedAt
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val localSession = completedStrengthSessionForStorage(
            id = "strength-remote-match",
            routineName = "하체",
            startedAtMillis = startedAtMillis,
            endedAtMillis = startedAtMillis + 3_600_000L
        )
        val remoteResult = trainingItem(
            id = "intervals-strength-1",
            externalId = localSession.intervalsExternalId,
            name = "하체",
            type = "Weight Training",
            startedAt = startedAt.plusMinutes(20),
            durationSeconds = localSession.durationSeconds
        )

        val items = listOf(remoteResult).withLocalStrengthResults(
            history = listOf(localSession),
            weekStart = LocalDate.of(2026, 6, 22),
            weekEnd = LocalDate.of(2026, 6, 28)
        )

        assertEquals(1, items.size)
        assertEquals(remoteResult.id, items.single().id)
        assertFalse(items.single().isLocalOnlyStrengthResult)
        assertEquals(localSession.id, items.single().matchedStrengthSession?.id)
    }

    @Test
    fun savedRunningWorkoutRoutine_roundTripsToExecutableTrainingItem() {
        val source = TrainingItem(
            id = "routine-remote-1",
            remoteId = "remote-1",
            externalId = "external-1",
            name = "UAE 40/20",
            type = "Run",
            date = LocalDate.of(2026, 6, 23),
            startedAt = null,
            timeLabel = "Routine",
            durationSeconds = null,
            distanceMeters = null,
            weightLiftedKg = null,
            load = null,
            fitness = null,
            fatigue = null,
            form = null,
            description = "12:00 pace",
            blocks = emptyList(),
            isRoutine = true
        )
        val blocks = listOf(
            RoutineBlock(
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

        val saved = source.toSavedRunningWorkoutRoutine(blocks)
        val executable = saved?.toTrainingItem()

        assertEquals("saved-running-external-1", saved?.id)
        assertEquals(60, saved?.durationSeconds)
        assertEquals(false, executable?.isRoutine)
        assertEquals(TrainingSportType.RUNNING, executable?.sportType())
        assertEquals(1, executable?.blocks?.size)
    }

    @Test
    fun moveScheduledStrengthRoutine_updatesStoredDateAndIds() {
        val sourceDate = LocalDate.of(2026, 6, 23)
        val targetDate = LocalDate.of(2026, 6, 25)
        val routine = defaultStrengthRoutines().first().copy(id = 42, name = "런닝보강")
        val scheduledRoutine = ScheduledStrengthRoutine(
            id = routine.scheduledStrengthRoutineId(sourceDate),
            date = sourceDate,
            routine = routine,
            uploadedToIntervals = true,
            externalId = routine.intervalsRoutineExternalId(sourceDate)
        )
        val item = TrainingItem(
            id = "local-${scheduledRoutine.id}",
            remoteId = scheduledRoutine.id,
            externalId = scheduledRoutine.externalId,
            name = routine.name,
            type = "Weight Training",
            date = sourceDate,
            startedAt = sourceDate.atStartOfDay(),
            timeLabel = "Routine",
            durationSeconds = null,
            distanceMeters = null,
            weightLiftedKg = null,
            load = null,
            fitness = null,
            fatigue = null,
            form = null,
            description = null,
            blocks = emptyList(),
            isRoutine = true,
            matchedStrengthRoutine = routine
        )

        val moveResult = listOf(scheduledRoutine).withMovedScheduledStrengthRoutine(item, targetDate)
        val movedRoutine = moveResult.movedRoutine

        assertEquals(targetDate, movedRoutine?.date)
        assertEquals(false, movedRoutine?.uploadedToIntervals)
        assertEquals(1, moveResult.routines.size)
        assertEquals(targetDate, moveResult.routines.single().date)
        assertEquals(routine.scheduledStrengthRoutineId(targetDate), moveResult.routines.single().id)
        assertEquals(routine.intervalsRoutineExternalId(targetDate), moveResult.routines.single().externalId)
    }

    @Test
    fun strengthRoutineDescription_roundTripsEmbeddedRoutineJson() {
        val routine = defaultStrengthRoutines().first().copy(id = 88, name = "임베디드 Routine")
        val encoded = java.util.Base64.getEncoder().encodeToString(
            listOf(routine).toJsonString().toByteArray()
        )
        val description = """
            IntervalsGym 웨이트 Routine
            $INTERVALS_GYM_STRENGTH_ROUTINE_PREFIX $encoded
        """.trimIndent()

        val parsed = description.toIntervalsGymStrengthRoutine()

        requireNotNull(parsed)
        assertEquals(routine.id, parsed.id)
        assertEquals(routine.name, parsed.name)
        assertEquals(routine.entries.map { it.title }, parsed.entries.map { it.title })
        assertEquals(routine.entries.first().records.size, parsed.entries.first().records.size)
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
    fun upsertScheduledStrengthRoutine_replacesSameExternalIdAndPersistsLatestRoutine() {
        val prefs = MemorySharedPreferences()
        val date = LocalDate.of(2026, 7, 1)
        val originalRoutine = defaultStrengthRoutines().first().copy(id = 11, name = "before")
        val replacementRoutine = originalRoutine.copy(name = "after")
        val original = ScheduledStrengthRoutine(
            id = originalRoutine.scheduledStrengthRoutineId(date),
            date = date,
            routine = originalRoutine,
            uploadedToIntervals = true,
            externalId = originalRoutine.intervalsRoutineExternalId(date)
        )
        val replacement = original.copy(routine = replacementRoutine, uploadedToIntervals = false)

        upsertScheduledStrengthRoutine(prefs, original)
        upsertScheduledStrengthRoutine(prefs, replacement)

        val routines = loadScheduledStrengthRoutines(prefs)
        assertEquals(1, routines.size)
        assertEquals("after", routines.single().routine.name)
        assertFalse(routines.single().uploadedToIntervals)
    }

    @Test
    fun removeScheduledStrengthRoutine_matchesLocalIdRemoteIdOrExternalId() {
        val prefs = MemorySharedPreferences()
        val firstDate = LocalDate.of(2026, 7, 1)
        val secondDate = LocalDate.of(2026, 7, 2)
        val firstRoutine = defaultStrengthRoutines().first().copy(id = 12, name = "remove")
        val secondRoutine = defaultStrengthRoutines().last().copy(id = 13, name = "keep")
        val removable = ScheduledStrengthRoutine(
            id = firstRoutine.scheduledStrengthRoutineId(firstDate),
            date = firstDate,
            routine = firstRoutine,
            uploadedToIntervals = true,
            externalId = firstRoutine.intervalsRoutineExternalId(firstDate)
        )
        val keep = ScheduledStrengthRoutine(
            id = secondRoutine.scheduledStrengthRoutineId(secondDate),
            date = secondDate,
            routine = secondRoutine,
            uploadedToIntervals = true,
            externalId = secondRoutine.intervalsRoutineExternalId(secondDate)
        )
        upsertScheduledStrengthRoutine(prefs, removable)
        upsertScheduledStrengthRoutine(prefs, keep)

        removeScheduledStrengthRoutine(
            prefs,
            trainingItem(
                id = "local-${removable.id}",
                remoteId = removable.id,
                externalId = removable.externalId,
                type = "Weight Training",
                isRoutine = true,
                matchedStrengthRoutine = firstRoutine
            )
        )

        val routines = loadScheduledStrengthRoutines(prefs)
        assertEquals(1, routines.size)
        assertEquals(keep.externalId, routines.single().externalId)
    }

    @Test
    fun loadScheduledStrengthRoutines_derivesMissingLegacyExternalId() {
        val prefs = MemorySharedPreferences()
        val date = LocalDate.of(2026, 7, 3)
        val routine = defaultStrengthRoutines().first().copy(id = 31, name = "legacy")
        val legacyJson = org.json.JSONArray().put(
            org.json.JSONObject()
                .put("id", routine.scheduledStrengthRoutineId(date))
                .put("date", date.toString())
                .put("uploadedToIntervals", true)
                .put("routineJson", listOf(routine).toJsonString())
        )

        prefs.edit().putString(SCHEDULED_STRENGTH_ROUTINES_PREF, legacyJson.toString()).apply()

        val routines = loadScheduledStrengthRoutines(prefs)
        assertEquals(1, routines.size)
        assertEquals(routine.scheduledStrengthRoutineId(date), routines.single().id)
        assertEquals(routine.intervalsRoutineExternalId(date), routines.single().externalId)
        assertTrue(routines.single().uploadedToIntervals)
    }

    @Test
    fun loadStrengthRoutines_readsOnlySavedRoutineKeyNotScheduledCalendarRoutines() {
        val prefs = MemorySharedPreferences()
        val date = LocalDate.of(2026, 7, 4)
        val scheduledOnlyRoutine = defaultStrengthRoutines().first().copy(id = 41, name = "캘린더 전용 Routine")
        val scheduledRoutine = ScheduledStrengthRoutine(
            id = scheduledOnlyRoutine.scheduledStrengthRoutineId(date),
            date = date,
            routine = scheduledOnlyRoutine,
            uploadedToIntervals = true,
            externalId = scheduledOnlyRoutine.intervalsRoutineExternalId(date)
        )

        upsertScheduledStrengthRoutine(prefs, scheduledRoutine)

        val routinesWithoutSavedKey = loadStrengthRoutines(prefs)
        assertEquals(defaultStrengthRoutines().map { it.id }, routinesWithoutSavedKey.map { it.id })
        assertFalse(routinesWithoutSavedKey.any { it.name == "캘린더 전용 Routine" })

        val savedRoutine = defaultStrengthRoutines().last().copy(id = 42, name = "로컬 저장 Routine")
        prefs.edit().putString(STRENGTH_ROUTINES_PREF, listOf(savedRoutine).toJsonString()).apply()

        val routinesWithSavedKey = loadStrengthRoutines(prefs)
        assertEquals(1, routinesWithSavedKey.size)
        assertEquals("로컬 저장 Routine", routinesWithSavedKey.single().name)
    }

    @Test
    fun scheduledStrengthRoutineOperations_doNotMutateSavedStrengthRoutines() {
        val prefs = MemorySharedPreferences()
        val date = LocalDate.of(2026, 7, 5)
        val savedRoutine = defaultStrengthRoutines().first().copy(id = 51, name = "로그인과 무관한 로컬 Routine")
        prefs.edit().putString(STRENGTH_ROUTINES_PREF, listOf(savedRoutine).toJsonString()).apply()

        val scheduledOnlyRoutine = defaultStrengthRoutines().last().copy(id = 52, name = "동기화 캘린더 Routine")
        val scheduledRoutine = ScheduledStrengthRoutine(
            id = scheduledOnlyRoutine.scheduledStrengthRoutineId(date),
            date = date,
            routine = scheduledOnlyRoutine,
            uploadedToIntervals = true,
            externalId = scheduledOnlyRoutine.intervalsRoutineExternalId(date)
        )
        val scheduledTrainingItem = trainingItem(
            id = "local-${scheduledRoutine.id}",
            remoteId = scheduledRoutine.id,
            externalId = scheduledRoutine.externalId,
            type = "Weight Training",
            isRoutine = true,
            matchedStrengthRoutine = scheduledOnlyRoutine
        )

        upsertScheduledStrengthRoutine(prefs, scheduledRoutine)
        moveScheduledStrengthRoutine(prefs, scheduledTrainingItem, date.plusDays(1))
        removeScheduledStrengthRoutine(prefs, scheduledTrainingItem.copy(externalId = scheduledOnlyRoutine.intervalsRoutineExternalId(date.plusDays(1))))

        val savedRoutines = loadStrengthRoutines(prefs)
        assertEquals(1, savedRoutines.size)
        assertEquals(savedRoutine.id, savedRoutines.single().id)
        assertEquals("로그인과 무관한 로컬 Routine", savedRoutines.single().name)
    }

    @Test
    fun withLocalStrengthRoutines_preservesExistingMatchedRemoteRoutine() {
        val date = LocalDate.of(2026, 7, 4)
        val localRoutine = defaultStrengthRoutines().first().copy(id = 41, name = "로컬 scheduled")
        val remoteEmbeddedRoutine = localRoutine.copy(name = "원격 embedded")
        val scheduledRoutine = ScheduledStrengthRoutine(
            id = localRoutine.scheduledStrengthRoutineId(date),
            date = date,
            routine = localRoutine,
            uploadedToIntervals = true,
            externalId = localRoutine.intervalsRoutineExternalId(date)
        )
        val remoteRoutine = trainingItem(
            id = "remote-strength-routine",
            remoteId = "remote-strength-routine",
            externalId = scheduledRoutine.externalId,
            type = "Weight Training",
            isRoutine = true,
            matchedStrengthRoutine = remoteEmbeddedRoutine
        )

        val merged = listOf(remoteRoutine).withLocalStrengthRoutines(
            scheduledRoutines = listOf(scheduledRoutine),
            start = date,
            end = date
        )

        assertEquals(1, merged.size)
        assertEquals("원격 embedded", merged.single().matchedStrengthRoutine?.name)
    }

    @Test
    fun withLocalStrengthRoutines_prefersCurrentLocalRoutineMatchedByUploadedRoutineId() {
        val date = LocalDate.of(2026, 7, 4)
        val uploadedRoutine = defaultStrengthRoutines().first().copy(id = 41, name = "업로드 당시 Routine")
        val currentRoutine = uploadedRoutine.copy(name = "수정된 로컬 Routine")
        val remoteEmbeddedRoutine = uploadedRoutine.copy(name = "원격 snapshot")
        val remoteRoutine = trainingItem(
            id = "remote-strength-routine",
            remoteId = "remote-strength-routine",
            externalId = "remote-external-id",
            type = "Weight Training",
            isRoutine = true,
            description = uploadedRoutine.toIntervalsRoutineDescription(),
            matchedStrengthRoutine = remoteEmbeddedRoutine
        )

        val merged = listOf(remoteRoutine).withLocalStrengthRoutines(
            scheduledRoutines = emptyList(),
            localRoutines = listOf(currentRoutine),
            start = date,
            end = date
        )

        assertEquals(1, merged.size)
        assertEquals(41, merged.single().matchedStrengthRoutine?.id)
        assertEquals("수정된 로컬 Routine", merged.single().matchedStrengthRoutine?.name)
    }

    @Test
    fun appendStrengthSessionHistory_deduplicatesExistingSessionId() {
        val prefs = MemorySharedPreferences()
        val original = completedStrengthSessionForStorage(
            id = "strength-same",
            routineName = "before",
            startedAtMillis = 1_000L,
            endedAtMillis = 61_000L
        )
        val replacement = original.copy(routineName = "after", uploadedToIntervals = true)

        appendStrengthSessionHistory(prefs, original)
        appendStrengthSessionHistory(prefs, replacement)

        val history = loadCompletedStrengthSessionHistory(prefs)
        assertEquals(1, history.size)
        assertEquals("after", history.single().routineName)
        assertTrue(history.single().uploadedToIntervals)
    }

    @Test
    fun appendRunningSessionHistory_deduplicatesExistingSessionId() {
        val prefs = MemorySharedPreferences()
        val original = completedRunningSessionForStorage(
            id = "running-same",
            name = "before",
            startedAtMillis = 1_000L,
            endedAtMillis = 61_000L
        )
        val replacement = original.copy(name = "after", uploadedToIntervals = true)

        appendRunningSessionHistory(prefs, original)
        appendRunningSessionHistory(prefs, replacement)

        val history = loadCompletedRunningSessionHistory(prefs)
        assertEquals(1, history.size)
        assertEquals("after", history.single().name)
        assertTrue(history.single().uploadedToIntervals)
    }

    @Test
    fun savedRunningWorkoutRoutine_upsertReplacesSameIdAndKeepsLatestFirst() {
        val prefs = MemorySharedPreferences()
        val original = savedRunningWorkoutRoutineForStorage(id = "saved-1", name = "before")
        val replacement = original.copy(name = "after", savedAtMillis = 2_000L)
        val other = savedRunningWorkoutRoutineForStorage(id = "saved-2", name = "other")

        upsertSavedRunningWorkoutRoutine(prefs, other)
        upsertSavedRunningWorkoutRoutine(prefs, original)
        upsertSavedRunningWorkoutRoutine(prefs, replacement)

        val routines = loadSavedRunningWorkoutRoutines(prefs)
        assertEquals(listOf("saved-1", "saved-2"), routines.map { it.id })
        assertEquals("after", routines.first().name)
    }

    @Test
    fun deleteSavedRunningWorkoutRoutine_removesOnlyTargetRoutine() {
        val prefs = MemorySharedPreferences()
        val first = savedRunningWorkoutRoutineForStorage(id = "saved-1", name = "first")
        val second = savedRunningWorkoutRoutineForStorage(id = "saved-2", name = "second")
        upsertSavedRunningWorkoutRoutine(prefs, first)
        upsertSavedRunningWorkoutRoutine(prefs, second)

        deleteSavedRunningWorkoutRoutine(prefs, "saved-1")

        val routines = loadSavedRunningWorkoutRoutines(prefs)
        assertEquals(1, routines.size)
        assertEquals("saved-2", routines.single().id)
    }

    @Test
    fun activeStrengthSession_roundTripsCurrentSetAndRestState() {
        val prefs = MemorySharedPreferences()
        val routine = defaultStrengthRoutines().first()
        val setEvent = strengthSetEventForStorage(routine.entries.first())
        val restEvent = StrengthRestEvent(
            id = 1,
            afterSetSequence = setEvent.sequence,
            exerciseEntryId = routine.entries.first().id,
            exerciseTitle = routine.entries.first().title,
            setRecordId = routine.entries.first().records.first().id,
            setIndex = 0,
            startedAtMillis = 10_000L,
            plannedSeconds = 60,
            targetEndAtMillis = System.currentTimeMillis() + 60_000L,
            endedAtMillis = null,
            endReason = null
        )
        val session = ActiveStrengthSession(
            routineId = routine.id,
            routineName = routine.name,
            entries = routine.entries,
            hasStarted = true,
            sessionStartedAtMillis = 1_000L,
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
        assertEquals(routine.id, restored.routineId)
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
        val routine = defaultStrengthRoutines().first()
        val setEvent = strengthSetEventForStorage(routine.entries.first())
        val expiredRest = StrengthRestEvent(
            id = 2,
            afterSetSequence = setEvent.sequence,
            exerciseEntryId = routine.entries.first().id,
            exerciseTitle = routine.entries.first().title,
            setRecordId = routine.entries.first().records.first().id,
            setIndex = 0,
            startedAtMillis = 1_000L,
            plannedSeconds = 60,
            targetEndAtMillis = 2_000L,
            endedAtMillis = null,
            endReason = null
        )
        val session = ActiveStrengthSession(
            routineId = routine.id,
            routineName = routine.name,
            entries = routine.entries,
            hasStarted = true,
            sessionStartedAtMillis = 1_000L,
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
    fun strengthRoutinesWithLatestCompletedSession_useNewestAppliedHistoryAndResetCompletedFlags() {
        val routine = defaultStrengthRoutines().first()
        val oldEntries = routine.entries.map { entry ->
            entry.copy(records = entry.records.map { it.copy(weightKg = "40", completed = true) })
        }
        val newEntries = routine.entries.map { entry ->
            entry.copy(records = entry.records.map { it.copy(weightKg = "80", completed = true) })
        }
        val ignoredEntries = routine.entries.map { entry ->
            entry.copy(records = entry.records.map { it.copy(weightKg = "120", completed = true) })
        }
        val oldWorkout = completedStrengthSessionForStorage(
            id = "old",
            routineName = routine.name,
            startedAtMillis = 1_000L,
            endedAtMillis = 61_000L,
            entries = oldEntries
        )
        val newWorkout = completedStrengthSessionForStorage(
            id = "new",
            routineName = routine.name,
            startedAtMillis = 3_000L,
            endedAtMillis = 63_000L,
            entries = newEntries
        )
        val ignoredWorkout = completedStrengthSessionForStorage(
            id = "ignored",
            routineName = routine.name,
            startedAtMillis = 5_000L,
            endedAtMillis = 65_000L,
            entries = ignoredEntries
        ).copy(appliedToRoutine = false)

        val updated = listOf(routine).withLatestCompletedSession(
            history = listOf(oldWorkout, ignoredWorkout, newWorkout)
        )

        assertEquals("80", updated.single().entries.first().records.first().weightKg)
        assertFalse(updated.single().entries.first().records.first().completed)
    }

    @Test
    fun activeStrengthSessionWithLatestCompletedSession_updatesOnlyBeforeSessionStarts() {
        val routine = defaultStrengthRoutines().first()
        val completedEntries = routine.entries.map { entry ->
            entry.copy(records = entry.records.map { it.copy(weightKg = "90", completed = true) })
        }
        val workout = completedStrengthSessionForStorage(
            id = "history",
            routineName = routine.name,
            startedAtMillis = 3_000L,
            endedAtMillis = 63_000L,
            entries = completedEntries
        )
        val idleSession = ActiveStrengthSession(
            routineId = routine.id,
            routineName = routine.name,
            entries = routine.entries,
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
        val startedSession = idleSession.copy(hasStarted = true)

        val updatedIdleSession = idleSession.withLatestCompletedSession(listOf(workout))
        val unchangedStartedSession = startedSession.withLatestCompletedSession(listOf(workout))

        assertEquals("90", updatedIdleSession.entries.first().records.first().weightKg)
        assertFalse(updatedIdleSession.entries.first().records.first().completed)
        assertEquals(routine.entries.first().records.first().weightKg, unchangedStartedSession.entries.first().records.first().weightKg)
    }
}

private fun trainingItem(
    id: String = "item-1",
    remoteId: String = id,
    externalId: String? = null,
    name: String = "테스트",
    type: String = "Run",
    isRoutine: Boolean = false,
    description: String? = null,
    matchedStrengthRoutine: StrengthWorkoutRoutine? = null,
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
        timeLabel = if (isRoutine) "Routine" else "08:00",
        durationSeconds = durationSeconds,
        distanceMeters = null,
        weightLiftedKg = null,
        load = null,
        fitness = null,
        fatigue = null,
        form = null,
        description = description,
        blocks = emptyList(),
        isRoutine = isRoutine,
        matchedStrengthRoutine = matchedStrengthRoutine,
        isLocalOnlyRunningResult = isLocalOnlyRunningResult
    )
}

private fun completedStrengthSessionForStorage(
    id: String,
    routineName: String,
    startedAtMillis: Long,
    endedAtMillis: Long,
    entries: List<StrengthRoutineEntry>? = null,
): CompletedStrengthSession {
    val routine = defaultStrengthRoutines().first()
    return CompletedStrengthSession(
        id = id,
        routineId = routine.id,
        routineName = routineName,
        startedAtMillis = startedAtMillis,
        endedAtMillis = endedAtMillis,
        durationSeconds = ((endedAtMillis - startedAtMillis) / 1000L).toInt(),
        intervalsExternalId = id,
        entries = entries ?: routine.entries,
        setEvents = emptyList(),
        restEvents = emptyList(),
        rpe = 7,
        trainingLoad = routine.entries.strengthTrainingLoad(7),
        uploadedToIntervals = false
    )
}

private fun completedRunningSessionForStorage(
    id: String,
    name: String,
    startedAtMillis: Long,
    endedAtMillis: Long,
): CompletedRunningSession {
    return CompletedRunningSession(
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

private fun savedRunningWorkoutRoutineForStorage(
    id: String,
    name: String,
): SavedRunningWorkoutRoutine {
    return SavedRunningWorkoutRoutine(
        id = id,
        name = name,
        description = "1m 10:00 pace [6km/h 1%]",
        durationSeconds = 60,
        blocks = listOf(
            RoutineBlock(
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

private fun strengthSetEventForStorage(entry: StrengthRoutineEntry): StrengthSetCompletionEvent {
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

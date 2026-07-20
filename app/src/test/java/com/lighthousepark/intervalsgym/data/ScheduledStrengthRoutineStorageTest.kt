package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.app.SCHEDULED_STRENGTH_ROUTINES_PREF
import com.lighthousepark.intervalsgym.app.STRENGTH_ROUTINES_PREF
import com.lighthousepark.intervalsgym.strength.ScheduledStrengthRoutine
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import com.lighthousepark.intervalsgym.training.TrainingItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduledStrengthRoutineStorageTest {
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
    fun scheduledStrengthRoutine_roundTripsTimeAndDisplaysTimeLabel() {
        val prefs = MemorySharedPreferences()
        val date = LocalDate.of(2026, 7, 8)
        val time = LocalTime.of(19, 30)
        val routine = defaultStrengthRoutines().first().copy(id = 77, name = "퇴근 후")
        val scheduledRoutine = ScheduledStrengthRoutine(
            id = routine.scheduledStrengthRoutineId(date, time),
            date = date,
            time = time,
            routine = routine,
            uploadedToIntervals = false,
            externalId = routine.intervalsRoutineExternalId(date, time)
        )

        upsertScheduledStrengthRoutine(prefs, scheduledRoutine)
        val restored = loadScheduledStrengthRoutines(prefs).single()
        val calendarItem = emptyList<TrainingItem>().withLocalStrengthRoutines(
            scheduledRoutines = listOf(restored),
            localRoutines = listOf(routine),
            start = date,
            end = date
        ).single()

        assertEquals(time, restored.time)
        assertEquals(routine.scheduledStrengthRoutineId(date, time), restored.id)
        assertEquals(routine.intervalsRoutineExternalId(date, time), restored.externalId)
        assertEquals(LocalDateTime.of(date, time), calendarItem.startedAt)
        assertEquals("19:30", calendarItem.timeLabel)
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
            externalId = uploadedRoutine.intervalsRoutineExternalId(date),
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
    fun intervalsRoutineExternalIdRestoresRoutineIdWithOrWithoutTime() {
        assertEquals(
            41,
            "intervals-gym-strength-routine-41-2026-07-04".toIntervalsGymStrengthRoutineIdFromExternalId()
        )
        assertEquals(
            41,
            "intervals-gym-strength-routine-41-2026-07-04-1930".toIntervalsGymStrengthRoutineIdFromExternalId()
        )
        assertEquals(null, "remote-external-id".toIntervalsGymStrengthRoutineIdFromExternalId())
    }
}

package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.app.ACTIVE_STRENGTH_SESSION_PREF
import com.lighthousepark.intervalsgym.strength.ActiveStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthRestEvent
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveStrengthSessionStorageTest {
    @Test
    fun roundTripsCurrentSetAndRestState() {
        val prefs = MemorySharedPreferences()
        val routine = defaultStrengthRoutines().first().copy(location = "회사 헬스장")
        val activeEntries = routine.entries.mapIndexed { entryIndex, entry ->
            if (entryIndex == 1) {
                entry.copy(
                    records = entry.records.mapIndexed { recordIndex, record ->
                        if (recordIndex == 2) {
                            record.copy(actualWeightKg = "72.5", actualReps = "6")
                        } else {
                            record
                        }
                    }
                )
            } else {
                entry
            }
        }
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
            entries = activeEntries,
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
            activeRestEventId = restEvent.id,
            routineBaselineEntries = routine.entries,
            routineLocation = routine.location
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
        assertEquals("72.5", restored.entries[1].records[2].actualWeightKg)
        assertEquals("6", restored.entries[1].records[2].actualReps)
        assertEquals(routine.entries, restored.routineBaselineEntries)
        assertEquals("회사 헬스장", restored.routineLocation)
        assertEquals("회사 헬스장", restored.toWorkoutRoutine().location)
    }

    @Test
    fun legacyActiveSessionWithoutRoutineBaselineUsesCurrentEntriesAsFallback() {
        val prefs = MemorySharedPreferences()
        val routine = defaultStrengthRoutines().first()
        val changedEntries = routine.entries.reversed()
        val session = ActiveStrengthSession(
            routineId = routine.id,
            routineName = routine.name,
            entries = changedEntries,
            hasStarted = true,
            sessionStartedAtMillis = 1_000L,
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
            activeRestEventId = null,
            routineBaselineEntries = routine.entries
        )
        val legacyJson = JSONObject(session.toJsonString())
            .apply { remove("routineBaseline") }
            .toString()

        prefs.edit().putString(ACTIVE_STRENGTH_SESSION_PREF, legacyJson).apply()
        val restored = requireNotNull(loadActiveStrengthSession(prefs))

        assertEquals(changedEntries, restored.entries)
        assertEquals(changedEntries, restored.routineBaselineEntries)
    }

    @Test
    fun expiredRestRestoresPendingSetAndFinalizesRestEvent() {
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
    fun strengthRoutinesWithLatestCompletedSessionUseNewestAppliedHistoryAndResetCompletedFlags() {
        val routine = defaultStrengthRoutines().first()
        val oldEntries = routine.entries.map { entry ->
            entry.copy(records = entry.records.map { it.copy(weightKg = "40", completed = true) })
        }
        val newEntries = routine.entries.map { entry ->
            entry.copy(
                records = entry.records.map {
                    it.copy(weightKg = "60", actualWeightKg = "80", completed = true)
                }
            )
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
    fun strengthRoutinesWithLatestCompletedSessionPreferSelectiveRoutineSnapshot() {
        val routine = defaultStrengthRoutines().first()
        val performedEntries = routine.entries.map { entry ->
            entry.copy(
                records = entry.records.map {
                    it.copy(actualWeightKg = "100", actualReps = "1", completed = true)
                }
            )
        }
        val routineUpdateEntries = routine.entries.reversed().mapIndexed { index, entry ->
            if (index == 0) entry.copy(note = "선택 반영") else entry
        }
        val workout = completedStrengthSessionForStorage(
            id = "selective-history",
            routineName = routine.name,
            startedAtMillis = 3_000L,
            endedAtMillis = 63_000L,
            entries = performedEntries
        ).copy(routineUpdateEntries = routineUpdateEntries)

        val updated = listOf(routine).withLatestCompletedSession(listOf(workout)).single()

        assertEquals(routineUpdateEntries.map { it.id }, updated.entries.map { it.id })
        assertEquals("선택 반영", updated.entries.first().note)
        assertTrue(updated.entries.all { entry -> entry.records.none { it.completed } })
        assertTrue(updated.entries.all { entry -> entry.records.none { it.actualWeightKg.isNotBlank() } })
    }

    @Test
    fun withLatestCompletedSessionUpdatesOnlyBeforeSessionStarts() {
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
        assertEquals(
            routine.entries.first().records.first().weightKg,
            unchangedStartedSession.entries.first().records.first().weightKg
        )
    }
}

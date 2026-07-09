package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.strength.CompletedStrengthExerciseHistory
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthRestEvent
import com.lighthousepark.intervalsgym.strength.StrengthSetCompletionEvent
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.strengthExerciseCatalog
import org.junit.Assert.assertEquals
import org.junit.Test

class StrengthExerciseHistoryUiModelTest {
    @Test
    fun historyRows_useSetEventsAndActualRestWhenAvailable() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }
        val entry = defaultStrengthRoutineEntry(
            id = 1,
            exercise = squat,
            weightKg = "60",
            reps = "8",
            restSeconds = "90"
        )
        val event = StrengthSetCompletionEvent(
            sequence = 7,
            exerciseEntryId = entry.id,
            exerciseTitle = entry.title,
            exerciseGroup = squat.group,
            exerciseId = squat.id,
            equipment = entry.equipment,
            variation = entry.variation,
            setRecordId = entry.records.first().id,
            setIndex = 0,
            weightKg = "62.5",
            reps = "6",
            targetRestSeconds = 90,
            completedAtMillis = 10_000L
        )
        val session = completedSession(
            entry = entry,
            setEvents = listOf(event),
            restEvents = listOf(
                StrengthRestEvent(
                    id = 1,
                    afterSetSequence = event.sequence,
                    exerciseEntryId = entry.id,
                    exerciseTitle = entry.title,
                    setRecordId = entry.records.first().id,
                    setIndex = 0,
                    startedAtMillis = 10_000L,
                    plannedSeconds = 90,
                    targetEndAtMillis = 100_000L,
                    endedAtMillis = 55_000L,
                    endReason = "finished"
                )
            )
        )

        val rows = CompletedStrengthExerciseHistory(session, entry, listOf(event))
            .toStrengthExerciseHistoryRows()

        assertEquals("Set 1", rows.single().label)
        assertEquals("62.5kg x 6회 · 휴식 90초 · 실제 00:45", rows.single().detail)
    }

    @Test
    fun historyVolumeKg_doublesUnilateralRecordVolume() {
        val legCurl = strengthExerciseCatalog.first { it.id == "leg_curl" }
        val entry = defaultStrengthRoutineEntry(
            id = 2,
            exercise = legCurl,
            weightKg = "20",
            reps = "10",
            restSeconds = "60"
        ).copy(
            variation = "한쪽 라잉",
            records = listOf(
                defaultStrengthRoutineEntry(2, legCurl, "20", "10", "60").records.first().copy(completed = true)
            )
        )
        val session = completedSession(entry = entry)

        val volume = CompletedStrengthExerciseHistory(session, entry, emptyList()).historyVolumeKg()

        assertEquals(400.0, volume, 0.01)
    }

    private fun completedSession(
        entry: com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry,
        setEvents: List<StrengthSetCompletionEvent> = emptyList(),
        restEvents: List<StrengthRestEvent> = emptyList(),
    ): CompletedStrengthSession {
        return CompletedStrengthSession(
            id = "history",
            routineId = 1,
            routineName = "history",
            startedAtMillis = 1_000L,
            endedAtMillis = 601_000L,
            durationSeconds = 600,
            intervalsExternalId = "history",
            entries = listOf(entry),
            setEvents = setEvents,
            restEvents = restEvents,
            rpe = 7,
            trainingLoad = 1,
            uploadedToIntervals = true
        )
    }
}

package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.training.TrainingItem
import java.time.LocalDate
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IntervalsTrainingJsonTest {
    @Test
    fun toTrainingItems_flattensNestedRoutineStepsWithTargets() {
        val json = JSONArray().put(
            JSONObject()
                .put("id", "42")
                .put("external_id", "routine-external")
                .put("name", "Hill Run")
                .put("type", "Run")
                .put("start_date_local", "2026-07-08T07:30:00")
                .put(
                    "workout_doc",
                    JSONObject()
                        .put("duration", 120)
                        .put(
                            "steps",
                            JSONArray().put(
                                JSONObject()
                                    .put("reps", 2)
                                    .put(
                                        "steps",
                                        JSONArray().put(
                                            JSONObject()
                                                .put("duration", 30)
                                                .put("text", "Fast")
                                                .put("intensity", "work")
                                                .put("_pace", JSONObject().put("start", 4.5).put("end", 5.0).put("units", "/km"))
                                        )
                                    )
                            )
                        )
                )
        )

        val item = json.toTrainingItems(isRoutine = true).single()

        assertEquals("routine-42", item.id)
        assertEquals("07:30", item.timeLabel)
        assertEquals(2, item.blocks.size)
        assertEquals(listOf(0, 30), item.blocks.map { it.startSecond })
        assertEquals(listOf(30, 60), item.blocks.map { it.endSecond })
        assertEquals("4.5-5/km", item.blocks.first().targetText)
        assertEquals(listOf(1, 2), item.blocks.map { it.repeatIteration })
        assertEquals(listOf(2, 2), item.blocks.map { it.repeatCount })
    }

    @Test
    fun toCalendarRoutineCopyJson_usesLabelTimeAndFallbackWorkoutDoc() {
        val item = trainingItemForIntervalsJson(
            timeLabel = "07:45",
            blocks = listOf(
                RoutineBlock(
                    index = 0,
                    title = "Tempo",
                    kind = "work",
                    targetText = "5:00/km",
                    durationSeconds = 600,
                    startSecond = 0,
                    endSecond = 600,
                    isRecovery = false
                )
            )
        )

        val json = item.toCalendarRoutineCopyJson(LocalDate.of(2026, 7, 9))
        val steps = json.getJSONObject("workout_doc").getJSONArray("steps")

        assertEquals("2026-07-09T07:45:00", json.getString("start_date_local"))
        assertTrue(json.getString("external_id").contains("intervals-gym-moved-routine-remote-1-2026-07-09"))
        assertEquals(600, json.getJSONObject("workout_doc").getInt("duration"))
        assertEquals("Tempo · 5:00/km", steps.getJSONObject(0).getString("text"))
    }

    private fun trainingItemForIntervalsJson(
        timeLabel: String,
        blocks: List<RoutineBlock>,
    ): TrainingItem {
        return TrainingItem(
            id = "routine-1",
            remoteId = "remote-1",
            externalId = "external-1",
            name = "Tempo Run",
            type = "Run",
            date = LocalDate.of(2026, 7, 8),
            startedAt = null,
            timeLabel = timeLabel,
            durationSeconds = blocks.sumOf { it.durationSeconds },
            distanceMeters = 5_000.0,
            weightLiftedKg = null,
            load = null,
            fitness = null,
            fatigue = null,
            form = null,
            description = "Tempo workout",
            blocks = blocks,
            isRoutine = true
        )
    }
}

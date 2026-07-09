package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.core.optNullableInt
import com.lighthousepark.intervalsgym.core.optNullableLong
import com.lighthousepark.intervalsgym.strength.StrengthRestEvent
import com.lighthousepark.intervalsgym.strength.StrengthSetCompletionEvent
import org.json.JSONArray
import org.json.JSONObject

internal fun finalizeRestEvents(
    restEvents: List<StrengthRestEvent>,
    activeRestEventId: Int?,
    endedAtMillis: Long,
    reason: String,
): List<StrengthRestEvent> {
    if (activeRestEventId == null) return restEvents
    return restEvents.map { event ->
        if (event.id == activeRestEventId && event.endedAtMillis == null) {
            event.copy(
                endedAtMillis = endedAtMillis,
                endReason = reason
            )
        } else {
            event
        }
    }
}

internal fun List<StrengthSetCompletionEvent>.toSetEventsJsonArray(): JSONArray {
    return JSONArray().also { array ->
        forEach { event ->
            array.put(
                JSONObject()
                    .put("sequence", event.sequence)
                    .put("exerciseEntryId", event.exerciseEntryId)
                    .put("exerciseTitle", event.exerciseTitle)
                    .put("exerciseGroup", event.exerciseGroup)
                    .put("exerciseId", event.exerciseId)
                    .put("equipment", event.equipment)
                    .put("variation", event.variation)
                    .put("setRecordId", event.setRecordId)
                    .put("setIndex", event.setIndex)
                    .put("weightKg", event.weightKg)
                    .put("reps", event.reps)
                    .put("targetRestSeconds", event.targetRestSeconds)
                    .put("completedAtMillis", event.completedAtMillis)
            )
        }
    }
}

internal fun JSONArray?.toStrengthSetCompletionEvents(): List<StrengthSetCompletionEvent> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { index ->
        val json = optJSONObject(index) ?: return@mapNotNull null
        StrengthSetCompletionEvent(
            sequence = json.optNullableInt("sequence") ?: (index + 1),
            exerciseEntryId = json.optNullableInt("exerciseEntryId") ?: 0,
            exerciseTitle = json.optString("exerciseTitle"),
            exerciseGroup = json.optString("exerciseGroup"),
            exerciseId = json.optString("exerciseId"),
            equipment = json.optString("equipment"),
            variation = json.optString("variation"),
            setRecordId = json.optNullableInt("setRecordId") ?: 0,
            setIndex = json.optNullableInt("setIndex") ?: 0,
            weightKg = json.optString("weightKg"),
            reps = json.optString("reps"),
            targetRestSeconds = json.optNullableInt("targetRestSeconds") ?: 0,
            completedAtMillis = json.optLong("completedAtMillis", 0L)
        )
    }
}

internal fun List<StrengthRestEvent>.toRestEventsJsonArray(): JSONArray {
    return JSONArray().also { array ->
        forEach { event ->
            array.put(
                JSONObject()
                    .put("id", event.id)
                    .put("afterSetSequence", event.afterSetSequence)
                    .put("exerciseEntryId", event.exerciseEntryId)
                    .put("exerciseTitle", event.exerciseTitle)
                    .put("setRecordId", event.setRecordId)
                    .put("setIndex", event.setIndex)
                    .put("startedAtMillis", event.startedAtMillis)
                    .put("plannedSeconds", event.plannedSeconds)
                    .put("targetEndAtMillis", event.targetEndAtMillis)
                    .put("endedAtMillis", event.endedAtMillis ?: JSONObject.NULL)
                    .put("actualSeconds", event.actualSeconds)
                    .put("endReason", event.endReason ?: JSONObject.NULL)
            )
        }
    }
}

internal fun JSONArray?.toStrengthRestEvents(): List<StrengthRestEvent> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { index ->
        val json = optJSONObject(index) ?: return@mapNotNull null
        StrengthRestEvent(
            id = json.optNullableInt("id") ?: (index + 1),
            afterSetSequence = json.optNullableInt("afterSetSequence") ?: 0,
            exerciseEntryId = json.optNullableInt("exerciseEntryId") ?: 0,
            exerciseTitle = json.optString("exerciseTitle"),
            setRecordId = json.optNullableInt("setRecordId") ?: 0,
            setIndex = json.optNullableInt("setIndex") ?: 0,
            startedAtMillis = json.optLong("startedAtMillis", 0L),
            plannedSeconds = json.optNullableInt("plannedSeconds") ?: 0,
            targetEndAtMillis = json.optLong("targetEndAtMillis", 0L),
            endedAtMillis = json.optNullableLong("endedAtMillis"),
            endReason = json.optString("endReason").takeIf { it.isNotBlank() }
        )
    }
}

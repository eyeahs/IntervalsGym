package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.core.optNullableInt
import com.lighthousepark.intervalsgym.strength.CUSTOM_STRENGTH_EQUIPMENT_OPTIONS
import com.lighthousepark.intervalsgym.strength.StrengthExercise
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthSetMetricType
import com.lighthousepark.intervalsgym.strength.StrengthSetRecord
import com.lighthousepark.intervalsgym.strength.StrengthSetGroupType
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.customStrengthExercise
import com.lighthousepark.intervalsgym.strength.effectiveSetGroupType
import com.lighthousepark.intervalsgym.strength.strengthExerciseCatalog
import org.json.JSONArray
import org.json.JSONObject

private fun List<String>.toStringJsonArray(): JSONArray {
    return JSONArray().also { array ->
        forEach { value -> array.put(value) }
    }
}

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { index ->
        optString(index).takeIf { it.isNotBlank() }
    }
}

private fun List<String>.withPreferredOption(option: String): List<String> {
    val safeOption = option.takeIf { it.isNotBlank() } ?: return ifEmpty { listOf("기본") }
    return if (contains(safeOption)) this else listOf(safeOption) + this
}

private fun JSONObject.toStrengthExercise(): StrengthExercise {
    val exerciseId = optString("exerciseId")
    strengthExerciseCatalog.firstOrNull { it.id == exerciseId }?.let { return it }

    val nameKo = optString("exerciseNameKo")
        .ifBlank { optString("exerciseNameEn") }
        .ifBlank { optString("exerciseName") }
        .ifBlank { "사용자 운동" }
    val group = optString("exerciseGroup").ifBlank { "사용자 추가" }
    val isCustomExercise = group == "사용자 추가" || exerciseId.startsWith("custom_")
    val equipment = optString("equipment")
    val variation = optString("variation")
    val savedEquipmentOptions = optJSONArray("equipmentOptions")
        .toStringList()
    val equipmentOptions = if (isCustomExercise && (savedEquipmentOptions.isEmpty() || savedEquipmentOptions == listOf("기본"))) {
        CUSTOM_STRENGTH_EQUIPMENT_OPTIONS
    } else {
        savedEquipmentOptions.ifEmpty { listOf(equipment.ifBlank { "기본" }) }
    }
        .distinct()
        .withPreferredOption(equipment)
    val variationOptions = optJSONArray("variationOptions")
        .toStringList()
        .ifEmpty { listOf(variation.ifBlank { "기본" }) }
        .distinct()
        .withPreferredOption(variation)

    return StrengthExercise(
        id = exerciseId.ifBlank { customStrengthExercise(nameKo).id },
        nameKo = nameKo,
        nameEn = optString("exerciseNameEn").ifBlank { nameKo },
        group = group,
        equipmentOptions = equipmentOptions,
        variationOptions = variationOptions,
        single = optBoolean("single", false)
    )
}

internal fun List<StrengthWorkoutRoutine>.toJsonString(): String {
    return JSONArray().also { routinesArray ->
        forEach { routine ->
            routinesArray.put(
                JSONObject()
                    .put("id", routine.id)
                    .put("name", routine.name)
                    .put("location", routine.location)
                    .put(
                        "entries",
                        JSONArray().also { entriesArray ->
                            routine.entries.forEach { entry ->
                                entriesArray.put(
                                    JSONObject()
                                        .put("id", entry.id)
                                        .put("exerciseId", entry.exercise.id)
                                        .put("exerciseNameKo", entry.exercise.nameKo)
                                        .put("exerciseNameEn", entry.exercise.nameEn)
                                        .put("exerciseGroup", entry.exercise.group)
                                        .put("single", entry.exercise.single)
                                        .put("equipmentOptions", entry.exercise.equipmentOptions.toStringJsonArray())
                                        .put("variationOptions", entry.exercise.variationOptions.toStringJsonArray())
                                        .put("equipment", entry.equipment)
                                        .put("variation", entry.variation)
                                        .put("supersetGroupId", entry.supersetGroupId ?: JSONObject.NULL)
                                        .put(
                                            "setGroupType",
                                            entry.effectiveSetGroupType()?.name ?: JSONObject.NULL
                                        )
                                        .put("targetSets", entry.targetSets)
                                        .put("targetReps", entry.targetReps)
                                        .put("setMetricType", entry.setMetricType.name)
                                        .put("restSeconds", entry.restSeconds)
                                        .put("targetWeightKg", entry.targetWeightKg)
                                        .put("note", entry.note)
                                        .put(
                                            "records",
                                            JSONArray().also { recordsArray ->
                                                entry.records.forEach { record ->
                                                    recordsArray.put(
                                                        JSONObject()
                                                            .put("id", record.id)
                                                            .put("weightKg", record.weightKg)
                                                            .put("reps", record.reps)
                                                            .put("actualWeightKg", record.actualWeightKg)
                                                            .put("actualReps", record.actualReps)
                                                            .put("actualDurationSeconds", record.actualDurationSeconds)
                                                            .put("leftWeightKg", record.leftWeightKg)
                                                            .put("leftReps", record.leftReps)
                                                            .put("rightWeightKg", record.rightWeightKg)
                                                            .put("rightReps", record.rightReps)
                                                            .put("durationSeconds", record.durationSeconds)
                                                            .put("restSeconds", record.restSeconds)
                                                            .put("completed", record.completed)
                                                    )
                                                }
                                            }
                                        )
                                )
                            }
                        }
                    )
            )
        }
    }.toString()
}

internal fun String?.toStrengthWorkoutRoutines(): List<StrengthWorkoutRoutine> {
    if (isNullOrBlank()) return emptyList()
    return runCatching {
        val routinesArray = JSONArray(this)
        (0 until routinesArray.length()).mapNotNull { routineIndex ->
            val routineJson = routinesArray.optJSONObject(routineIndex) ?: return@mapNotNull null
            val entriesArray = routineJson.optJSONArray("entries") ?: JSONArray()
            val entries = (0 until entriesArray.length()).mapNotNull { entryIndex ->
                val entryJson = entriesArray.optJSONObject(entryIndex) ?: return@mapNotNull null
                val parsedExercise = entryJson.toStrengthExercise()
                val savedVariation = entryJson.optString("variation")
                val shouldMigrateHackSquat = parsedExercise.id == "squat" && savedVariation == "핵 스쿼트"
                val exercise = if (shouldMigrateHackSquat) {
                    strengthExerciseCatalog.firstOrNull { it.id == "hack_squat" } ?: parsedExercise
                } else {
                    parsedExercise
                }
                val recordsArray = entryJson.optJSONArray("records") ?: JSONArray()
                val records = (0 until recordsArray.length()).mapNotNull { recordIndex ->
                    val recordJson = recordsArray.optJSONObject(recordIndex) ?: return@mapNotNull null
                    StrengthSetRecord(
                        id = recordJson.optNullableInt("id") ?: (recordIndex + 1),
                        weightKg = recordJson.optString("weightKg"),
                        reps = recordJson.optString("reps"),
                        actualWeightKg = recordJson.optString("actualWeightKg"),
                        actualReps = recordJson.optString("actualReps"),
                        actualDurationSeconds = recordJson.optString("actualDurationSeconds"),
                        leftWeightKg = recordJson.optString("leftWeightKg").ifBlank { recordJson.optString("weightKg") },
                        leftReps = recordJson.optString("leftReps").ifBlank { recordJson.optString("reps") },
                        rightWeightKg = recordJson.optString("rightWeightKg").ifBlank { recordJson.optString("weightKg") },
                        rightReps = recordJson.optString("rightReps").ifBlank { recordJson.optString("reps") },
                        durationSeconds = recordJson.optString("durationSeconds"),
                        restSeconds = recordJson.optString("restSeconds"),
                        completed = recordJson.optBoolean("completed", false)
                    )
                }.ifEmpty {
                    listOf(
                        StrengthSetRecord(
                            id = 1,
                            weightKg = entryJson.optString("targetWeightKg"),
                            reps = entryJson.optNullableInt("targetReps")?.takeIf { it > 0 }?.toString().orEmpty(),
                            leftWeightKg = entryJson.optString("targetWeightKg"),
                            leftReps = entryJson.optNullableInt("targetReps")?.takeIf { it > 0 }?.toString().orEmpty(),
                            rightWeightKg = entryJson.optString("targetWeightKg"),
                            rightReps = entryJson.optNullableInt("targetReps")?.takeIf { it > 0 }?.toString().orEmpty(),
                            durationSeconds = "",
                            restSeconds = entryJson.optNullableInt("restSeconds")?.takeIf { it > 0 }?.toString().orEmpty(),
                            completed = false
                        )
                    )
                }
                val supersetGroupId = entryJson.optNullableInt("supersetGroupId")
                StrengthRoutineEntry(
                    id = entryJson.optNullableInt("id") ?: (entryIndex + 1),
                    exercise = exercise,
                    equipment = if (shouldMigrateHackSquat) {
                        "머신"
                    } else if (entryJson.has("equipment")) {
                        entryJson.optString("equipment")
                    } else {
                        exercise.equipmentOptions.first()
                    },
                    variation = if (shouldMigrateHackSquat) {
                        "기본"
                    } else {
                        savedVariation.ifBlank { exercise.variationOptions.first() }
                    },
                    supersetGroupId = supersetGroupId,
                    targetSets = entryJson.optNullableInt("targetSets") ?: records.size,
                    targetReps = entryJson.optNullableInt("targetReps") ?: records.firstOrNull()?.reps?.toIntOrNull() ?: 0,
                    restSeconds = entryJson.optNullableInt("restSeconds") ?: records.firstOrNull()?.restSeconds?.toIntOrNull() ?: 0,
                    targetWeightKg = entryJson.optString("targetWeightKg"),
                    note = entryJson.optString("note"),
                    records = records,
                    setGroupType = supersetGroupId?.let {
                        runCatching {
                            StrengthSetGroupType.valueOf(entryJson.optString("setGroupType"))
                        }.getOrDefault(StrengthSetGroupType.SUPERSET)
                    },
                    setMetricType = runCatching {
                        StrengthSetMetricType.valueOf(entryJson.optString("setMetricType"))
                    }.getOrDefault(StrengthSetMetricType.REPS)
                )
            }
            StrengthWorkoutRoutine(
                id = routineJson.optNullableInt("id") ?: (routineIndex + 1),
                name = routineJson.optString("name").ifBlank { "웨이트 Routine" },
                entries = entries,
                location = routineJson.optString("location")
            )
        }
    }.getOrDefault(emptyList())
}

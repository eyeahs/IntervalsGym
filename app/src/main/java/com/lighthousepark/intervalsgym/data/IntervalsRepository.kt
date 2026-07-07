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

import java.io.ByteArrayOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

internal const val INTERVALS_BEARER_CREDENTIAL_PREFIX = "bearer:"

internal fun intervalsBearerCredential(accessToken: String): String {
    return "$INTERVALS_BEARER_CREDENTIAL_PREFIX$accessToken"
}

internal class IntervalsRepository(private val credential: String) {
    suspend fun loadWeek(start: LocalDate, end: LocalDate): WeekTrainingData = withContext(Dispatchers.IO) {
        val activities = getJsonArray(
            path = "/api/v1/athlete/0/activities",
            params = mapOf("oldest" to start.toString(), "newest" to end.toString())
        ).toTrainingItems(isRoutine = false)

        val routines = getJsonArray(
            path = "/api/v1/athlete/0/events",
            params = mapOf(
                "oldest" to start.toString(),
                "newest" to end.toString(),
                "category" to "WORKOUT",
                "resolve" to "true"
            )
        ).toTrainingItems(isRoutine = true)

        WeekTrainingData(
            activities = activities.sortedBy { it.date },
            routines = routines.sortedBy { it.date }
        )
    }

    suspend fun uploadStrengthSession(session: StrengthSession) = withContext(Dispatchers.IO) {
        val durationSeconds = session.entries.totalDurationSeconds().coerceAtLeast(60)
        val description = session.toIntervalsDescription()
        val externalId = "intervals-gym-${session.startedAt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))}"
        postManualActivity(
            name = session.name,
            description = description,
            externalId = externalId,
            startedAt = session.startedAt,
            durationSeconds = durationSeconds,
            volumeKg = session.entries.totalVolumeKg(),
            trainingLoad = session.trainingLoad
        )
    }

    suspend fun uploadRunningSession(session: RunningSession) = withContext(Dispatchers.IO) {
        val externalId = "intervals-gym-run-${session.startedAt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))}"
        postActivityFile(
            name = session.name,
            description = session.toIntervalsDescription(),
            externalId = externalId,
            fileName = "$externalId.tcx",
            contentType = "application/vnd.garmin.tcx+xml",
            fileBytes = session.buildRunningTcx().toByteArray(Charsets.UTF_8)
        )
    }

    suspend fun uploadStrengthRoutine(routine: StrengthWorkoutRoutine, date: LocalDate) = withContext(Dispatchers.IO) {
        val event = JSONObject()
            .put("category", "WORKOUT")
            .put("name", routine.name)
            .put("type", "WeightTraining")
            .put("start_date_local", "${date}T00:00:00")
            .put("description", routine.toIntervalsRoutineDescription())
            .put("external_id", routine.intervalsRoutineExternalId(date))
        postJsonObject(
            path = "/api/v1/athlete/0/events",
            json = event
        )
    }

    suspend fun uploadCalendarRoutineCopy(routine: TrainingItem, date: LocalDate) = withContext(Dispatchers.IO) {
        postJsonObject(
            path = "/api/v1/athlete/0/events",
            json = routine.toCalendarRoutineCopyJson(date)
        )
    }

    suspend fun deleteCalendarRoutine(eventId: String) = withContext(Dispatchers.IO) {
        deleteRequest(path = "/api/v1/athlete/0/events/${eventId.urlEncode()}")
    }

    private fun getJsonArray(path: String, params: Map<String, String>): JSONArray {
        val query = params.entries.joinToString("&") { (key, value) ->
            "${key.urlEncode()}=${value.urlEncode()}"
        }
        val url = URL("https://intervals.icu$path?$query")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", authHeader())
        }

        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val body = BufferedReader(InputStreamReader(stream ?: connection.inputStream)).use { it.readText() }

        if (status !in 200..299) {
            throw IllegalStateException(
                when (status) {
                    401 -> "Intervals 인증이 만료되었거나 권한이 없습니다."
                    403 -> "Intervals.icu 권한이 부족합니다."
                    else -> "Intervals.icu 요청 실패: HTTP $status"
                }
            )
        }
        return JSONArray(body)
    }

    private fun postJsonObject(path: String, json: JSONObject): JSONObject {
        val url = URL("https://intervals.icu$path")
        val body = json.toString().toByteArray(Charsets.UTF_8)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 20_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", authHeader())
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Content-Length", body.size.toString())
        }
        connection.outputStream.use { it.write(body) }
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val bodyText = stream?.let { BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() } }.orEmpty()

        if (status !in 200..299) {
            throw IllegalStateException(
                when (status) {
                    401 -> "Intervals 인증이 만료되었거나 권한이 없습니다."
                    403 -> "Intervals.icu 캘린더 권한이 부족합니다."
                    else -> "Intervals.icu 요청 실패: HTTP $status ${bodyText.take(120)}"
                }
            )
        }
        return JSONObject(bodyText.ifBlank { "{}" })
    }

    private fun deleteRequest(path: String) {
        val url = URL("https://intervals.icu$path")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "DELETE"
            connectTimeout = 20_000
            readTimeout = 20_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", authHeader())
        }
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val bodyText = stream?.let { BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() } }.orEmpty()

        if (status !in 200..299 && status != 404) {
            throw IllegalStateException(
                when (status) {
                    401 -> "Intervals 인증이 만료되었거나 권한이 없습니다."
                    403 -> "Intervals.icu 캘린더 권한이 부족합니다."
                    else -> "Intervals.icu 삭제 실패: HTTP $status ${bodyText.take(120)}"
                }
            )
        }
    }

    private fun postManualActivity(
        name: String,
        description: String,
        externalId: String,
        startedAt: LocalDateTime,
        durationSeconds: Int,
        volumeKg: Double,
        trainingLoad: Int,
    ) {
        val liftedKg = volumeKg.roundedKg()
        val activity = JSONObject()
            .put("name", name)
            .put("type", "WeightTraining")
            .put("start_date_local", startedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .put("moving_time", durationSeconds)
            .put("elapsed_time", durationSeconds)
            .put("description", description)
            .put("external_id", externalId)
            .put("kg_lifted", liftedKg)
            .put("icu_training_load", trainingLoad)
            .put("training_load", trainingLoad)
        postJsonObject(
            path = "/api/v1/athlete/0/activities/manual",
            json = activity
        )
    }

    private fun postActivityFile(
        name: String,
        description: String,
        externalId: String,
        fileName: String,
        contentType: String,
        fileBytes: ByteArray,
    ) {
        val query = mapOf(
            "name" to name,
            "description" to description,
            "external_id" to externalId
        ).entries.joinToString("&") { (key, value) ->
            "${key.urlEncode()}=${value.urlEncode()}"
        }
        val url = URL("https://intervals.icu/api/v1/athlete/0/activities?$query")
        val boundary = "IntervalsGymBoundary${UUID.randomUUID().toString().replace("-", "")}"
        val body = buildActivityUploadMultipartBody(
            boundary = boundary,
            fileName = fileName,
            contentType = contentType,
            fileBytes = fileBytes
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", authHeader())
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            setRequestProperty("Content-Length", body.size.toString())
        }
        connection.outputStream.use { it.write(body) }
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val bodyText = stream?.let { BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() } }.orEmpty()

        if (status !in 200..299) {
            throw IllegalStateException(
                when (status) {
                    401 -> "Intervals 인증이 만료되었거나 권한이 없습니다."
                    403 -> "Intervals.icu 활동 업로드 권한이 부족합니다."
                    else -> "Intervals.icu 활동 업로드 실패: HTTP $status ${bodyText.take(120)}"
                }
            )
        }
    }

    private fun authHeader(): String {
        if (credential.startsWith(INTERVALS_BEARER_CREDENTIAL_PREFIX)) {
            return "Bearer ${credential.removePrefix(INTERVALS_BEARER_CREDENTIAL_PREFIX)}"
        }
        throw IllegalStateException("Intervals OAuth 로그인이 필요합니다.")
    }
}

internal fun buildActivityUploadMultipartBody(
    boundary: String,
    fileName: String,
    contentType: String,
    fileBytes: ByteArray,
): ByteArray {
    val lineBreak = "\r\n"
    return ByteArrayOutputStream().use { output ->
        fun writeText(text: String) {
            output.write(text.toByteArray(Charsets.UTF_8))
        }
        writeText("--$boundary$lineBreak")
        writeText("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"$lineBreak")
        writeText("Content-Type: $contentType$lineBreak")
        writeText(lineBreak)
        output.write(fileBytes)
        writeText(lineBreak)
        writeText("--$boundary--$lineBreak")
        output.toByteArray()
    }
}

internal fun JSONArray.toTrainingItems(isRoutine: Boolean): List<TrainingItem> {
    return (0 until length()).mapNotNull { index ->
        val json = optJSONObject(index) ?: return@mapNotNull null
        val dateTime = parseDateTime(json.optString("start_date_local"))
        val date = dateTime?.toLocalDate() ?: return@mapNotNull null
        val workoutDoc = json.optJSONObject("workout_doc")
        val blocks = if (isRoutine) workoutDoc.toRoutineBlocks() else emptyList()
        val remoteId = json.optString("id", index.toString())

        TrainingItem(
            id = "${if (isRoutine) "routine" else "activity"}-$remoteId",
            remoteId = remoteId,
            externalId = json.optString("external_id")
                .ifBlank { json.optString("externalId") }
                .cleanJsonText(),
            name = json.optString("name").ifBlank { json.optString("type", if (isRoutine) "Workout" else "Activity") },
            type = json.optString("type", if (isRoutine) "Workout" else "Activity"),
            date = date,
            startedAt = dateTime,
            timeLabel = dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
            durationSeconds = json.optNullableInt("moving_time")
                ?: json.optNullableInt("elapsed_time")
                ?: json.optNullableInt("duration")
                ?: workoutDoc?.optNullableInt("duration")
                ?: blocks.sumOf { it.durationSeconds }.takeIf { it > 0 },
            distanceMeters = json.optNullableDouble("distance") ?: workoutDoc?.optNullableDouble("distance"),
            weightLiftedKg = json.optNullableDouble("kg_lifted")
                ?: json.optNullableDouble("weight_lifted")
                ?: json.optNullableDouble("weightLifted"),
            load = json.optNullableInt("icu_training_load"),
            fitness = json.optNullableDouble("fitness")
                ?: json.optNullableDouble("ctl")
                ?: json.optNullableDouble("icu_fitness")
                ?: json.optNullableDouble("icu_ctl"),
            fatigue = json.optNullableDouble("fatigue")
                ?: json.optNullableDouble("atl")
                ?: json.optNullableDouble("icu_fatigue")
                ?: json.optNullableDouble("icu_atl"),
            form = json.optNullableDouble("form")
                ?: json.optNullableDouble("tsb")
                ?: json.optNullableDouble("icu_form")
                ?: json.optNullableDouble("icu_tsb"),
            description = json.optString("description").cleanJsonText()
                ?: workoutDoc?.optString("description").cleanJsonText(),
            blocks = blocks,
            isRoutine = isRoutine,
            workoutDocJson = workoutDoc?.toString()
        )
    }
}

private fun TrainingItem.toCalendarRoutineCopyJson(date: LocalDate): JSONObject {
    val startTime = startedAt
        ?.toLocalTime()
        ?.format(DateTimeFormatter.ISO_LOCAL_TIME)
        ?: Regex("""\d{1,2}:\d{2}(?::\d{2})?""")
            .find(timeLabel)
            ?.value
            ?.let { if (it.count { char -> char == ':' } == 1) "$it:00" else it }
        ?: "00:00:00"
    val event = JSONObject()
        .put("category", "WORKOUT")
        .put("name", name.ifBlank { type.ifBlank { "Workout" } })
        .put("type", type.ifBlank { sportType().toIntervalsRoutineType() })
        .put("start_date_local", "${date}T$startTime")
        .put("external_id", movedCalendarRoutineExternalId(date))

    description?.takeIf { it.isNotBlank() }?.let { event.put("description", it) }
    durationSeconds?.takeIf { it > 0 }?.let { event.put("duration", it) }
    distanceMeters?.takeIf { it > 0.0 }?.let { event.put("distance", it.roundToInt()) }

    val workoutDoc = workoutDocJson
        ?.let { raw -> runCatching { JSONObject(raw) }.getOrNull() }
        ?: blocks.toFallbackWorkoutDocJson(description)
    workoutDoc?.let { event.put("workout_doc", it) }
    return event
}

private fun TrainingItem.movedCalendarRoutineExternalId(date: LocalDate): String {
    val sourceId = remoteId.ifBlank { id }.replace(Regex("""[^A-Za-z0-9_.-]"""), "-")
    return "intervals-gym-moved-routine-$sourceId-$date"
}

private fun TrainingSportType.toIntervalsRoutineType(): String {
    return when (this) {
        TrainingSportType.RUNNING -> "Run"
        TrainingSportType.CYCLING -> "Ride"
        TrainingSportType.STRENGTH -> "WeightTraining"
        TrainingSportType.OTHER -> "Workout"
    }
}

private fun List<RoutineBlock>.toFallbackWorkoutDocJson(description: String?): JSONObject? {
    if (isEmpty()) return null
    return JSONObject()
        .put("description", description.orEmpty())
        .put("duration", sumOf { it.durationSeconds.coerceAtLeast(0) })
        .put(
            "steps",
            JSONArray().also { steps ->
                forEachIndexed { index, block ->
                    steps.put(
                        JSONObject()
                            .put("duration", block.durationSeconds.coerceAtLeast(0))
                            .put("text", block.fallbackWorkoutStepText(index))
                            .put("intensity", block.kind.ifBlank { if (block.isRecovery) "recovery" else "work" })
                    )
                }
            }
        )
}

private fun RoutineBlock.fallbackWorkoutStepText(index: Int): String {
    return listOf(
        title.ifBlank { "Block ${index + 1}" },
        targetText
    ).filter { it.isNotBlank() }.distinct().joinToString(" · ")
}

private fun JSONObject?.toRoutineBlocks(): List<RoutineBlock> {
    val steps = this?.optJSONArray("steps") ?: return emptyList()
    val flatSteps = mutableListOf<RawRoutineStep>()
    flattenRoutineSteps(steps, flatSteps)

    var cursor = 0
    return flatSteps.mapIndexed { index, step ->
        val start = cursor
        cursor += step.durationSeconds
        RoutineBlock(
            index = index,
            title = step.title.ifBlank { "Block ${index + 1}" },
            kind = step.kind,
            targetText = step.targetText,
            durationSeconds = step.durationSeconds,
            startSecond = start,
            endSecond = cursor,
            isRecovery = step.kind.contains("rest", ignoreCase = true) ||
                step.kind.contains("recover", ignoreCase = true) ||
                step.kind.contains("warm", ignoreCase = true) ||
                step.kind.contains("cool", ignoreCase = true)
        )
    }
}

private data class RawRoutineStep(
    val title: String,
    val kind: String,
    val targetText: String,
    val durationSeconds: Int,
)

private fun flattenRoutineSteps(steps: JSONArray, output: MutableList<RawRoutineStep>) {
    for (index in 0 until steps.length()) {
        val step = steps.optJSONObject(index) ?: continue
        val reps = step.optNullableInt("reps")?.coerceAtLeast(1) ?: 1
        val nested = step.optJSONArray("steps")

        repeat(reps) { repIndex ->
            if (nested != null) {
                flattenRoutineSteps(nested, output)
            } else {
                val duration = step.optNullableInt("duration") ?: return@repeat
                if (duration <= 0) return@repeat

                output += RawRoutineStep(
                    title = step.optString("text").ifBlank {
                        step.optString("intensity").ifBlank {
                            if (reps > 1) "반복 ${repIndex + 1}" else "Workout"
                        }
                    },
                    kind = step.optString("intensity").ifBlank {
                        when {
                            step.optBoolean("warmup", false) -> "warmup"
                            step.optBoolean("cooldown", false) -> "cooldown"
                            step.optBoolean("freeride", false) -> "free ride"
                            else -> "work"
                        }
                    },
                    targetText = step.targetText(),
                    durationSeconds = duration
                )
            }
        }
    }
}

private fun JSONObject.targetText(): String {
    return listOfNotNull(
        valueText("_pace") ?: valueText("pace"),
        valueText("_grade") ?: valueText("grade"),
        valueText("_incline") ?: valueText("incline"),
        valueText("_hr") ?: valueText("hr"),
        valueText("_power") ?: valueText("power"),
        valueText("cadence")
    ).joinToString(" · ")
}

private fun JSONObject.valueText(name: String): String? {
    val value = optJSONObject(name) ?: return null
    val units = value.optString("units").takeIf { it.isNotBlank() }.orEmpty()
    val target = value.optString("target").takeIf { it.isNotBlank() }
    val range = when {
        value.has("start") && value.has("end") -> {
            "${formatTargetNumber(value.optDouble("start"))}-${formatTargetNumber(value.optDouble("end"))}"
        }
        value.has("value") -> formatTargetNumber(value.optDouble("value"))
        else -> return target
    }
    return listOf(range + units, target).filter { !it.isNullOrBlank() }.joinToString(" ")
}

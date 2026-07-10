package com.lighthousepark.intervalsgym.data

import com.lighthousepark.intervalsgym.core.formatExternalIdTimestamp
import com.lighthousepark.intervalsgym.core.formatIntervalsClockTime
import com.lighthousepark.intervalsgym.core.roundedKg
import com.lighthousepark.intervalsgym.core.urlEncode
import com.lighthousepark.intervalsgym.running.RunningSession
import com.lighthousepark.intervalsgym.running.buildRunningTcx
import com.lighthousepark.intervalsgym.running.toIntervalsDescription
import com.lighthousepark.intervalsgym.strength.StrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.completedVolumeKg
import com.lighthousepark.intervalsgym.strength.toIntervalsDescription
import com.lighthousepark.intervalsgym.strength.totalCompletedVolumeKg
import com.lighthousepark.intervalsgym.strength.totalDurationSeconds
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.WeekTrainingData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

internal const val INTERVALS_BEARER_CREDENTIAL_PREFIX = "bearer:"

internal fun intervalsBearerCredential(accessToken: String): String {
    return "$INTERVALS_BEARER_CREDENTIAL_PREFIX$accessToken"
}

internal class IntervalsRepository(
    credential: String,
    private val apiClient: IntervalsApiClient = IntervalsApiClient(credential),
) {
    suspend fun loadWeek(start: LocalDate, end: LocalDate): WeekTrainingData = withContext(Dispatchers.IO) {
        val activities = apiClient.getJsonArray(
            path = "/api/v1/athlete/0/activities",
            params = mapOf("oldest" to start.toString(), "newest" to end.toString())
        ).toTrainingItems(isRoutine = false)

        val routines = apiClient.getJsonArray(
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
        val durationSeconds = (session.durationSeconds ?: session.entries.totalDurationSeconds()).coerceAtLeast(60)
        val volumeKg = if (session.setEvents.isNotEmpty()) {
            session.setEvents.totalCompletedVolumeKg(session.entries)
        } else {
            session.entries.completedVolumeKg()
        }
        val description = session.toIntervalsDescription()
        val externalId = "intervals-gym-${session.startedAt.formatExternalIdTimestamp()}"
        postManualActivity(
            name = session.name,
            description = description,
            externalId = externalId,
            startedAt = session.startedAt,
            durationSeconds = durationSeconds,
            volumeKg = volumeKg,
            trainingLoad = session.trainingLoad
        )
    }

    suspend fun uploadRunningSession(session: RunningSession) = withContext(Dispatchers.IO) {
        val externalId = "intervals-gym-run-${session.startedAt.formatExternalIdTimestamp()}"
        apiClient.postActivityFile(
            name = session.name,
            description = session.toIntervalsDescription(),
            externalId = externalId,
            fileName = "$externalId.tcx",
            contentType = "application/vnd.garmin.tcx+xml",
            fileBytes = session.buildRunningTcx().toByteArray(Charsets.UTF_8)
        )
    }

    suspend fun uploadStrengthRoutine(routine: StrengthWorkoutRoutine, date: LocalDate, time: LocalTime? = null) = withContext(Dispatchers.IO) {
        val startTime = (time ?: LocalTime.MIDNIGHT).formatIntervalsClockTime()
        val event = JSONObject()
            .put("category", "WORKOUT")
            .put("name", routine.name)
            .put("type", "WeightTraining")
            .put("start_date_local", "${date}T$startTime")
            .put("description", routine.toIntervalsRoutineDescription())
            .put("external_id", routine.intervalsRoutineExternalId(date, time))
        apiClient.postJsonObject(
            path = "/api/v1/athlete/0/events",
            json = event
        )
    }

    suspend fun uploadCalendarRoutineCopy(routine: TrainingItem, date: LocalDate) = withContext(Dispatchers.IO) {
        apiClient.postJsonObject(
            path = "/api/v1/athlete/0/events",
            json = routine.toCalendarRoutineCopyJson(date)
        )
    }

    suspend fun deleteCalendarRoutine(eventId: String) = withContext(Dispatchers.IO) {
        apiClient.deleteRequest(path = "/api/v1/athlete/0/events/${eventId.urlEncode()}")
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
        apiClient.postJsonObject(
            path = "/api/v1/athlete/0/activities/manual",
            json = activity
        )
    }
}

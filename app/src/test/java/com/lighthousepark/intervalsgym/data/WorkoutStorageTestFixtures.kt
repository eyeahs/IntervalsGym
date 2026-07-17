package com.lighthousepark.intervalsgym.data

import android.content.SharedPreferences
import com.lighthousepark.intervalsgym.running.CompletedRunningSession
import com.lighthousepark.intervalsgym.running.RunningSession
import com.lighthousepark.intervalsgym.running.SavedRunningWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthSetCompletionEvent
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines
import com.lighthousepark.intervalsgym.strength.strengthTrainingLoad
import com.lighthousepark.intervalsgym.training.RoutineBlock
import com.lighthousepark.intervalsgym.training.TrainingDateRange
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.WeekTrainingData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

internal fun trainingItem(
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

internal fun completedStrengthSessionForStorage(
    id: String,
    routineName: String,
    startedAtMillis: Long,
    endedAtMillis: Long,
    entries: List<StrengthRoutineEntry>? = null,
    location: String = "",
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
        uploadedToIntervals = false,
        location = location
    )
}

internal fun completedRunningSessionForStorage(
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

internal fun runningSessionForStorage(name: String): RunningSession {
    val block = RoutineBlock(
        index = 0,
        title = "Block 1",
        kind = "work",
        targetText = "6km/h",
        durationSeconds = 60,
        startSecond = 0,
        endSecond = 60,
        isRecovery = false
    )
    return RunningSession(
        name = name,
        startedAt = LocalDateTime.of(2026, 7, 8, 7, 0),
        endedAt = LocalDateTime.of(2026, 7, 8, 7, 2),
        warmupSeconds = 60,
        blocks = listOf(block),
        actualBlocks = listOf(block),
        heartRateSamples = emptyList()
    )
}

internal fun savedRunningWorkoutRoutineForStorage(
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

internal fun strengthSetEventForStorage(entry: StrengthRoutineEntry): StrengthSetCompletionEvent {
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

internal data class RecordedStrengthUpload(
    val routine: StrengthWorkoutRoutine,
    val date: LocalDate,
    val time: LocalTime?,
)

internal class RecordingCalendarRoutineRemoteDataSource : CalendarRoutineRemoteDataSource {
    val strengthUploads = mutableListOf<RecordedStrengthUpload>()
    val copiedRoutines = mutableListOf<Pair<TrainingItem, LocalDate>>()
    val deletedEventIds = mutableListOf<String>()

    override suspend fun uploadStrengthRoutine(
        routine: StrengthWorkoutRoutine,
        date: LocalDate,
        time: LocalTime?,
    ) {
        strengthUploads += RecordedStrengthUpload(routine, date, time)
    }

    override suspend fun uploadCalendarRoutineCopy(routine: TrainingItem, date: LocalDate) {
        copiedRoutines += routine to date
    }

    override suspend fun deleteCalendarRoutine(eventId: String) {
        deletedEventIds += eventId
    }
}

internal class RecordingStrengthSessionRemoteDataSource : StrengthSessionRemoteDataSource {
    val uploads = mutableListOf<StrengthSession>()

    override suspend fun uploadStrengthSession(session: StrengthSession) {
        uploads += session
    }
}

internal class RecordingRunningSessionRemoteDataSource : RunningSessionRemoteDataSource {
    val uploads = mutableListOf<RunningSession>()

    override suspend fun uploadRunningSession(session: RunningSession) {
        uploads += session
    }
}

internal class RecordingTrainingCalendarRemoteDataSource(
    private val data: WeekTrainingData = WeekTrainingData(activities = emptyList(), routines = emptyList()),
) : TrainingCalendarRemoteDataSource {
    val requests = mutableListOf<TrainingDateRange>()

    override suspend fun loadWeek(range: TrainingDateRange): WeekTrainingData {
        requests += range
        return data
    }
}

internal class MemorySharedPreferences : SharedPreferences {
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

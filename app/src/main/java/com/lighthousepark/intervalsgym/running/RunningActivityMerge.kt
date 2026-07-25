package com.lighthousepark.intervalsgym.running

import com.lighthousepark.intervalsgym.core.formatClock
import com.lighthousepark.intervalsgym.core.formatDuration
import com.lighthousepark.intervalsgym.training.runningInclineText
import com.lighthousepark.intervalsgym.training.runningTargetSpeedText
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

internal const val INTERVALS_GARMIN_ACTIVITY_SOURCE = "GARMIN_CONNECT"
internal const val INTERVALS_GYM_RUNNING_EXTERNAL_ID_PREFIX = "intervals-gym-run-"

internal data class RunningRemoteHeartRatePoint(
    val elapsedSeconds: Int,
    val bpm: Int,
)

internal data class RunningRemoteStream(
    val type: String,
    val data: List<Any?>,
    val attributes: Map<String, Any?> = emptyMap(),
)

internal data class RunningRemoteActivityStreams(
    val streams: List<RunningRemoteStream>,
)

internal data class RunningHeartRateAlignment(
    /** App session start expressed on the remote activity timeline. */
    val offsetSeconds: Int,
    val correlation: Double,
    val comparedSamples: Int,
)

internal data class RunningRemoteActivity(
    val id: String,
    val name: String,
    val type: String,
    val source: String,
    val externalId: String?,
    val startedAtMillis: Long,
    val durationSeconds: Int,
    val description: String?,
)

internal enum class RunningActivityMergeMatchMethod {
    HEART_RATE,
    START_TIME,
}

internal data class RunningActivityMergeCandidate(
    val activity: RunningRemoteActivity,
    val matchMethod: RunningActivityMergeMatchMethod,
    val offsetSeconds: Int,
    val heartRateCorrelation: Double?,
    val comparedHeartRateSamples: Int,
    val startDifferenceSeconds: Int,
    val durationDifferenceSeconds: Int,
    val duplicateActivityId: String?,
)

internal data class RunningActivityMergeUpdate(
    val startedAtMillis: Long,
    val durationSeconds: Int,
    val description: String,
)

internal fun CompletedRunningSession.runningRecordStartedAtMillis(): Long {
    return (startedAtMillis + warmupSeconds.coerceAtLeast(0) * 1_000L)
        .coerceAtMost(endedAtMillis)
}

internal fun CompletedRunningSession.runningRecordDurationSeconds(): Int {
    return ((endedAtMillis - runningRecordStartedAtMillis()).coerceAtLeast(0L) / 1_000L)
        .toInt()
}

internal fun alignRunningHeartRateStreams(
    sessionStartedAtMillis: Long,
    localSamples: List<HeartRateSample>,
    remotePoints: List<RunningRemoteHeartRatePoint>,
    expectedOffsetSeconds: Int,
    searchRadiusSeconds: Int = 5 * 60,
): RunningHeartRateAlignment? {
    val localBySecond = localSamples
        .asSequence()
        .filter { it.timestampMillis >= sessionStartedAtMillis && it.bpm > 0 }
        .groupBy { ((it.timestampMillis - sessionStartedAtMillis) / 1_000L).toInt() }
        .mapValues { (_, samples) -> samples.map { it.bpm }.average() }
        .smoothedValues()
    val remoteBySecond = remotePoints
        .asSequence()
        .filter { it.elapsedSeconds >= 0 && it.bpm > 0 }
        .groupBy { it.elapsedSeconds }
        .mapValues { (_, points) -> points.map { it.bpm }.average() }
        .smoothedValues()
    if (localBySecond.size < MIN_HEART_RATE_ALIGNMENT_SAMPLES ||
        remoteBySecond.size < MIN_HEART_RATE_ALIGNMENT_SAMPLES
    ) {
        return null
    }
    val requiredComparableSamples = maxOf(
        MIN_HEART_RATE_ALIGNMENT_SAMPLES,
        minOf(localBySecond.size, remoteBySecond.size) / 2
    )

    val minimumOffset = (expectedOffsetSeconds - searchRadiusSeconds)
        .coerceAtLeast(-MAX_HEART_RATE_ALIGNMENT_OFFSET_SECONDS)
    val maximumOffset = (expectedOffsetSeconds + searchRadiusSeconds)
        .coerceAtMost(MAX_HEART_RATE_ALIGNMENT_OFFSET_SECONDS)
    return (minimumOffset..maximumOffset)
        .mapNotNull { offset ->
            val pairs = localBySecond.mapNotNull { (localSecond, localBpm) ->
                remoteBySecond[localSecond + offset]?.let { remoteBpm -> localBpm to remoteBpm }
            }
            if (pairs.size < requiredComparableSamples) return@mapNotNull null
            val correlation = pearsonCorrelation(pairs) ?: return@mapNotNull null
            RunningHeartRateAlignment(
                offsetSeconds = offset,
                correlation = correlation,
                comparedSamples = pairs.size
            )
        }
        .maxWithOrNull(
            compareBy<RunningHeartRateAlignment> { alignment ->
                alignment.correlation -
                    abs(alignment.offsetSeconds - expectedOffsetSeconds).toDouble() /
                    HEART_RATE_EXPECTED_OFFSET_PENALTY_DIVISOR
            }.thenBy { it.comparedSamples }
        )
}

internal fun evaluateRunningActivityMergeCandidate(
    session: CompletedRunningSession,
    activity: RunningRemoteActivity,
    remoteHeartRate: List<RunningRemoteHeartRatePoint>,
    duplicateActivityId: String?,
): RunningActivityMergeCandidate? {
    if (activity.source != INTERVALS_GARMIN_ACTIVITY_SOURCE) return null
    if (!activity.type.contains("run", ignoreCase = true)) return null
    val recordStartedAtMillis = session.runningRecordStartedAtMillis()
    val recordDurationSeconds = session.runningRecordDurationSeconds()
    val startDifferenceSeconds = ((recordStartedAtMillis - activity.startedAtMillis) / 1_000L).toInt()
    val durationDifferenceSeconds = recordDurationSeconds - activity.durationSeconds
    if (abs(startDifferenceSeconds) > MAX_RUNNING_MERGE_START_DIFFERENCE_SECONDS) return null
    if (
        abs(durationDifferenceSeconds) >
        maxOf(MAX_RUNNING_MERGE_DURATION_DIFFERENCE_SECONDS, recordDurationSeconds / 3)
    ) {
        return null
    }

    val alignment = alignRunningHeartRateStreams(
        sessionStartedAtMillis = recordStartedAtMillis,
        localSamples = session.heartRateSamples,
        remotePoints = remoteHeartRate,
        expectedOffsetSeconds = startDifferenceSeconds
    )
    val hasComparableHeartRate = session.heartRateSamples.size >= MIN_HEART_RATE_ALIGNMENT_SAMPLES &&
        remoteHeartRate.size >= MIN_HEART_RATE_ALIGNMENT_SAMPLES
    if (hasComparableHeartRate &&
        (alignment == null || alignment.correlation < MIN_RUNNING_MERGE_HEART_RATE_CORRELATION)
    ) {
        return null
    }
    if (!hasComparableHeartRate &&
        (abs(startDifferenceSeconds) > FALLBACK_RUNNING_MERGE_START_DIFFERENCE_SECONDS ||
            abs(durationDifferenceSeconds) > FALLBACK_RUNNING_MERGE_DURATION_DIFFERENCE_SECONDS)
    ) {
        return null
    }

    return RunningActivityMergeCandidate(
        activity = activity,
        matchMethod = if (alignment != null) {
            RunningActivityMergeMatchMethod.HEART_RATE
        } else {
            RunningActivityMergeMatchMethod.START_TIME
        },
        offsetSeconds = alignment?.offsetSeconds ?: startDifferenceSeconds,
        heartRateCorrelation = alignment?.correlation,
        comparedHeartRateSamples = alignment?.comparedSamples ?: 0,
        startDifferenceSeconds = startDifferenceSeconds,
        durationDifferenceSeconds = durationDifferenceSeconds,
        duplicateActivityId = duplicateActivityId
    )
}

internal fun List<RunningActivityMergeCandidate>.rankedRunningMergeCandidates(): List<RunningActivityMergeCandidate> {
    return sortedWith(
        compareByDescending<RunningActivityMergeCandidate> { it.matchMethod == RunningActivityMergeMatchMethod.HEART_RATE }
            .thenByDescending { it.heartRateCorrelation ?: -1.0 }
            .thenBy { abs(it.startDifferenceSeconds) }
            .thenBy { abs(it.durationDifferenceSeconds) }
    )
}

internal fun CompletedRunningSession.withRunningMergeResult(
    candidate: RunningActivityMergeCandidate,
    streams: RunningRemoteActivityStreams,
): CompletedRunningSession {
    val mergedRoutePoints = streams.runningRoutePoints()
    val mergedHeartRateSamples = streams.runningHeartRateSamples(
        startedAtMillis = runningRecordStartedAtMillis()
    )
    return copy(
        routePoints = mergedRoutePoints.ifEmpty { routePoints },
        heartRateSamples = mergedHeartRateSamples.ifEmpty { heartRateSamples },
        mergedIntervalsActivityId = candidate.activity.id,
        mergeOffsetSeconds = candidate.offsetSeconds,
        mergeCorrelation = candidate.heartRateCorrelation
    )
}

internal fun CompletedRunningSession.mergedIntervalsDescription(
    candidate: RunningActivityMergeCandidate,
): String {
    val original = candidate.activity.description
        .orEmpty()
        .removePreviousIntervalsGymMergeSection()
        .trim()
    val mergeSection = buildString {
        appendLine(RUNNING_MERGE_SECTION_START)
        appendLine("IntervalsGym 러닝 수행 정보")
        appendLine("총 수행 시간: ${formatDuration(runningRecordDurationSeconds())}")
        actualBlocks.estimatedRunningClimbMeters()
            .takeIf { it > 0.0 }
            ?.let { climbMeters ->
                appendLine("예상 상승고도: ${climbMeters.roundToInt()} m")
            }
        when (candidate.matchMethod) {
            RunningActivityMergeMatchMethod.HEART_RATE -> {
                val percent = ((candidate.heartRateCorrelation ?: 0.0) * 100.0).roundToInt()
                appendLine("시간 정렬: 심박 그래프 $percent% 일치 · ${candidate.offsetSeconds.formatSignedSeconds()}")
            }
            RunningActivityMergeMatchMethod.START_TIME -> {
                appendLine("시간 정렬: 시작 시각 기준 · ${candidate.offsetSeconds.formatSignedSeconds()}")
            }
        }
        appendLine()
        actualBlocks.forEachIndexed { index, block ->
            val startSecond = block.startSecond.coerceAtLeast(0)
            val endSecond = block.endSecond.coerceAtLeast(startSecond)
            val speed = block.runningTargetSpeedText().ifBlank { "-" }
            val incline = block.runningInclineText().ifBlank { "-" }
            appendLine(
                "- ${formatClock(startSecond)}–${formatClock(endSecond)} " +
                    "Block ${index + 1} ${block.title}: $speed · $incline"
            )
        }
        append(RUNNING_MERGE_SECTION_END)
    }
    return listOf(original, mergeSection)
        .filter { it.isNotBlank() }
        .joinToString("\n\n")
}

internal fun CompletedRunningSession.runningActivityMergeUpdate(
    candidate: RunningActivityMergeCandidate,
): RunningActivityMergeUpdate {
    return RunningActivityMergeUpdate(
        startedAtMillis = runningRecordStartedAtMillis(),
        durationSeconds = runningRecordDurationSeconds(),
        description = mergedIntervalsDescription(candidate)
    )
}

internal fun CompletedRunningSession.mergedRunningActivityStreams(
    candidate: RunningActivityMergeCandidate,
    source: RunningRemoteActivityStreams,
): RunningRemoteActivityStreams {
    val timeStream = source.streams.firstOrNull { it.type == RUNNING_TIME_STREAM_TYPE }
        ?: error("Garmin 활동의 시간 스트림을 찾을 수 없습니다.")
    val durationSeconds = runningRecordDurationSeconds()
    val sourceRowsByMergedSecond = timeStream.data
        .mapIndexedNotNull { index, value ->
            val remoteSecond = (value as? Number)?.toDouble()?.roundToInt()
                ?: return@mapIndexedNotNull null
            val mergedSecond = remoteSecond - candidate.offsetSeconds
            if (mergedSecond !in 0..durationSeconds) return@mapIndexedNotNull null
            mergedSecond to index
        }
        .toMap()
    val appHeartRateBySecond = appHeartRateByRunningSecond(durationSeconds)
    val mergedSeconds = buildSet {
        add(0)
        add(durationSeconds)
        addAll(sourceRowsByMergedSecond.keys)
        addAll(appHeartRateBySecond.keys)
    }.sorted()
    val estimatedClimbMeters = actualBlocks.estimatedRunningClimbMeters()
    val sourceAltitude = source.streams
        .firstOrNull { it.type == RUNNING_ALTITUDE_STREAM_TYPE }
        ?.data
    val sourceAltitudeBaseline = sourceRowsByMergedSecond
        .toSortedMap()
        .values
        .asSequence()
        .mapNotNull { sourceIndex ->
            (sourceAltitude?.getOrNull(sourceIndex) as? Number)?.toDouble()
        }
        .firstOrNull()
        ?: 0.0

    val mergedStreams = source.streams.map { stream ->
        val mergedData = when {
            stream.type == RUNNING_TIME_STREAM_TYPE -> mergedSeconds
            stream.type == RUNNING_HEART_RATE_STREAM_TYPE && appHeartRateBySecond.isNotEmpty() -> {
                mergedSeconds.map(appHeartRateBySecond::get)
            }
            stream.type == RUNNING_ALTITUDE_STREAM_TYPE && estimatedClimbMeters > 0.0 -> {
                mergedSeconds.map { second ->
                    sourceAltitudeBaseline + actualBlocks.estimatedRunningClimbMetersAtElapsed(second)
                }
            }
            else -> {
                mergedSeconds.map { second ->
                    sourceRowsByMergedSecond[second]?.let(stream.data::getOrNull)
                }
            }
        }
        stream.copy(data = mergedData)
    }.toMutableList()
    if (appHeartRateBySecond.isNotEmpty() &&
        mergedStreams.none { it.type == RUNNING_HEART_RATE_STREAM_TYPE }
    ) {
        mergedStreams += RunningRemoteStream(
            type = RUNNING_HEART_RATE_STREAM_TYPE,
            data = mergedSeconds.map(appHeartRateBySecond::get)
        )
    }
    if (estimatedClimbMeters > 0.0 &&
        mergedStreams.none { it.type == RUNNING_ALTITUDE_STREAM_TYPE }
    ) {
        mergedStreams += RunningRemoteStream(
            type = RUNNING_ALTITUDE_STREAM_TYPE,
            data = mergedSeconds.map { second ->
                actualBlocks.estimatedRunningClimbMetersAtElapsed(second)
            }
        )
    }
    return RunningRemoteActivityStreams(mergedStreams)
}

internal fun RunningRemoteActivityStreams.runningRoutePoints(): List<RunningRoutePoint> {
    val time = streams.firstOrNull { it.type == RUNNING_TIME_STREAM_TYPE }?.data
        ?: return emptyList()
    val latLng = streams.firstOrNull { it.type == RUNNING_LAT_LNG_STREAM_TYPE }?.data
        ?: return emptyList()
    val altitude = streams.firstOrNull { it.type == RUNNING_ALTITUDE_STREAM_TYPE }?.data
    return (0 until minOf(time.size, latLng.size)).mapNotNull { index ->
        val elapsedSeconds = (time[index] as? Number)?.toDouble()?.roundToInt()
            ?: return@mapNotNull null
        val coordinates = latLng[index] as? List<*> ?: return@mapNotNull null
        val latitude = (coordinates.getOrNull(0) as? Number)?.toDouble()
            ?: return@mapNotNull null
        val longitude = (coordinates.getOrNull(1) as? Number)?.toDouble()
            ?: return@mapNotNull null
        RunningRoutePoint(
            elapsedSeconds = elapsedSeconds,
            latitude = latitude,
            longitude = longitude,
            elevationMeters = (altitude?.getOrNull(index) as? Number)?.toDouble() ?: 0.0
        )
    }
}

internal fun RunningRemoteActivityStreams.runningHeartRateSamples(
    startedAtMillis: Long,
): List<HeartRateSample> {
    val time = streams.firstOrNull { it.type == RUNNING_TIME_STREAM_TYPE }?.data
        ?: return emptyList()
    val heartRate = streams.firstOrNull { it.type == RUNNING_HEART_RATE_STREAM_TYPE }?.data
        ?: return emptyList()
    return (0 until minOf(time.size, heartRate.size)).mapNotNull { index ->
        val elapsedSeconds = (time[index] as? Number)?.toDouble()?.roundToInt()
            ?: return@mapNotNull null
        val bpm = (heartRate[index] as? Number)?.toDouble()?.roundToInt()
            ?.takeIf { it > 0 }
            ?: return@mapNotNull null
        HeartRateSample(
            timestampMillis = startedAtMillis + elapsedSeconds.coerceAtLeast(0) * 1_000L,
            bpm = bpm
        )
    }
}

private fun CompletedRunningSession.appHeartRateByRunningSecond(
    durationSeconds: Int,
): Map<Int, Int> {
    val startedAtMillis = runningRecordStartedAtMillis()
    return heartRateSamples
        .asSequence()
        .filter { it.bpm > 0 }
        .filter { it.timestampMillis in startedAtMillis..endedAtMillis }
        .groupBy { sample ->
            ((sample.timestampMillis - startedAtMillis) / 1_000L)
                .toInt()
                .coerceIn(0, durationSeconds)
        }
        .mapValues { (_, samples) -> samples.map { it.bpm }.average().roundToInt() }
}

private fun Map<Int, Double>.smoothedValues(radius: Int = 2): Map<Int, Double> {
    if (isEmpty()) return emptyMap()
    return keys.associateWith { second ->
        ((second - radius)..(second + radius))
            .mapNotNull(::get)
            .average()
    }
}

private fun pearsonCorrelation(pairs: List<Pair<Double, Double>>): Double? {
    val firstAverage = pairs.sumOf { it.first } / pairs.size
    val secondAverage = pairs.sumOf { it.second } / pairs.size
    var covariance = 0.0
    var firstVariance = 0.0
    var secondVariance = 0.0
    pairs.forEach { (first, second) ->
        val firstDelta = first - firstAverage
        val secondDelta = second - secondAverage
        covariance += firstDelta * secondDelta
        firstVariance += firstDelta * firstDelta
        secondVariance += secondDelta * secondDelta
    }
    val denominator = sqrt(firstVariance * secondVariance)
    if (denominator <= 0.0001) return null
    return (covariance / denominator).coerceIn(-1.0, 1.0)
}

private fun String.removePreviousIntervalsGymMergeSection(): String {
    val start = indexOf(RUNNING_MERGE_SECTION_START)
    if (start < 0) return this
    val end = indexOf(RUNNING_MERGE_SECTION_END, start)
    if (end < 0) return substring(0, start)
    return removeRange(start, end + RUNNING_MERGE_SECTION_END.length)
}

private fun Int.formatSignedSeconds(): String {
    return when {
        this > 0 -> "+${this}초"
        this < 0 -> "${this}초"
        else -> "0초"
    }
}

private const val MIN_HEART_RATE_ALIGNMENT_SAMPLES = 30
private const val MAX_HEART_RATE_ALIGNMENT_OFFSET_SECONDS = 20 * 60
private const val HEART_RATE_EXPECTED_OFFSET_PENALTY_DIVISOR = 3_000.0
private const val MIN_RUNNING_MERGE_HEART_RATE_CORRELATION = 0.72
private const val MAX_RUNNING_MERGE_START_DIFFERENCE_SECONDS = 20 * 60
private const val MAX_RUNNING_MERGE_DURATION_DIFFERENCE_SECONDS = 10 * 60
private const val FALLBACK_RUNNING_MERGE_START_DIFFERENCE_SECONDS = 3 * 60
private const val FALLBACK_RUNNING_MERGE_DURATION_DIFFERENCE_SECONDS = 5 * 60
private const val RUNNING_MERGE_SECTION_START = "--- IntervalsGym 병합 ---"
private const val RUNNING_MERGE_SECTION_END = "--- 병합 정보 끝 ---"
private const val RUNNING_TIME_STREAM_TYPE = "time"
private const val RUNNING_HEART_RATE_STREAM_TYPE = "heartrate"
private const val RUNNING_LAT_LNG_STREAM_TYPE = "latlng"
private const val RUNNING_ALTITUDE_STREAM_TYPE = "altitude"

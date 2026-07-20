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
    val startDifferenceSeconds = ((session.startedAtMillis - activity.startedAtMillis) / 1_000L).toInt()
    val durationDifferenceSeconds = session.durationSeconds - activity.durationSeconds
    if (abs(startDifferenceSeconds) > MAX_RUNNING_MERGE_START_DIFFERENCE_SECONDS) return null
    if (abs(durationDifferenceSeconds) > maxOf(MAX_RUNNING_MERGE_DURATION_DIFFERENCE_SECONDS, session.durationSeconds / 3)) {
        return null
    }

    val alignment = alignRunningHeartRateStreams(
        sessionStartedAtMillis = session.startedAtMillis,
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
): CompletedRunningSession {
    return copy(
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
        appendLine("총 수행 시간: ${formatDuration(durationSeconds)}")
        appendLine("Warmup: ${formatClock(warmupSeconds)}")
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
            val startSecond = (candidate.offsetSeconds + warmupSeconds + block.startSecond).coerceAtLeast(0)
            val endSecond = (candidate.offsetSeconds + warmupSeconds + block.endSecond).coerceAtLeast(startSecond)
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

package com.lighthousepark.intervalsgym.training

import com.lighthousepark.intervalsgym.core.formatClockTime
import com.lighthousepark.intervalsgym.data.intervalsRoutineExternalId
import java.time.LocalDate

internal data class PendingCalendarRoutineMove(
    val sourceRoutine: TrainingItem,
    val targetDate: LocalDate,
) {
    val key: String = sourceRoutine.calendarMoveKey()
    val targetExternalId: String = sourceRoutine.pendingMoveTargetExternalId(targetDate)

    fun identityKeys(): Set<String> {
        return sourceRoutine.calendarIdentityKeys() +
            key +
            targetExternalId +
            "$PENDING_MOVE_ID_PREFIX$key-$targetDate"
    }
}

internal data class CalendarRoutineRenderData(
    val routines: List<TrainingItem>,
    val pendingRoutineKeys: Set<String>,
)

internal fun TrainingItem.calendarMoveKey(): String {
    return externalId?.takeIf { it.isNotBlank() }
        ?: remoteId.takeIf { it.isNotBlank() }
        ?: id
}

internal fun TrainingItem.calendarIdentityKeys(): Set<String> {
    return listOf(id, remoteId, externalId, calendarMoveKey())
        .filterNotNull()
        .filter { it.isNotBlank() }
        .toSet()
}

internal fun TrainingItem.hasCalendarIdentityIn(keys: Set<String>): Boolean {
    return calendarIdentityKeys().any { it in keys }
}

internal fun Collection<PendingCalendarRoutineMove>.hasPendingCalendarRoutineMoveFor(
    routine: TrainingItem,
): Boolean {
    val routineKeys = routine.calendarIdentityKeys()
    return any { move -> move.identityKeys().any { key -> key in routineKeys } }
}

internal fun Map<String, PendingCalendarRoutineMove>.withPendingCalendarRoutineMove(
    move: PendingCalendarRoutineMove,
    isRemoteConnected: Boolean,
): Map<String, PendingCalendarRoutineMove> {
    return if (isRemoteConnected) this + (move.key to move) else this
}

internal fun Map<String, PendingCalendarRoutineMove>.withoutCalendarRoutineMove(
    move: PendingCalendarRoutineMove,
): Map<String, PendingCalendarRoutineMove> {
    return withoutCalendarRoutineMoveIdentities(move.identityKeys())
}

internal fun Map<String, PendingCalendarRoutineMove>.withoutCalendarRoutineMoveIdentities(
    identityKeys: Set<String>,
): Map<String, PendingCalendarRoutineMove> {
    return filter { (key, move) ->
        key !in identityKeys && move.identityKeys().none { identity -> identity in identityKeys }
    }
}

internal fun List<TrainingItem>.withPendingCalendarRoutineMoves(
    pendingMoves: Collection<PendingCalendarRoutineMove>,
    start: LocalDate,
    end: LocalDate,
): CalendarRoutineRenderData {
    if (pendingMoves.isEmpty()) return CalendarRoutineRenderData(routines = this, pendingRoutineKeys = emptySet())

    val moves = pendingMoves.filter { move ->
        move.sourceRoutine.date in start..end || move.targetDate in start..end
    }
    if (moves.isEmpty()) return CalendarRoutineRenderData(routines = this, pendingRoutineKeys = emptySet())

    val withoutSources = filterNot { item -> moves.any { move -> item.isPendingMoveSource(move) } }
    val syntheticTargets = moves
        .filter { move -> move.targetDate in start..end }
        .filter { move -> withoutSources.none { item -> item.isPendingMoveTarget(move) } }
        .map { move -> move.sourceRoutine.withPendingMoveDate(move) }
    val renderedRoutines = withoutSources + syntheticTargets

    return CalendarRoutineRenderData(
        routines = renderedRoutines,
        pendingRoutineKeys = renderedRoutines.pendingRoutineKeysFor(moves)
    )
}

internal fun TrainingItem.isApiPendingMove(pendingRoutineKeys: Set<String>): Boolean {
    val routine = calendarRoutineForMove() ?: this
    return listOfNotNull(routine.id, routine.remoteId, routine.externalId, routine.calendarMoveKey())
        .any { key -> key in pendingRoutineKeys }
}

internal fun Collection<PendingCalendarRoutineMove>.withoutReflectedMoves(
    routines: List<TrainingItem>,
): Map<String, PendingCalendarRoutineMove> {
    return filterNot { move ->
        val hasTarget = routines.any { routine -> routine.isPendingMoveTarget(move) && !routine.isSyntheticPendingMove() }
        val hasSource = routines.any { routine -> routine.isPendingMoveSource(move) }
        hasTarget && !hasSource
    }.associateBy { move -> move.key }
}

private fun List<TrainingItem>.pendingRoutineKeysFor(moves: List<PendingCalendarRoutineMove>): Set<String> {
    val keys = mutableSetOf<String>()
    for (item in this) {
        val move = moves.firstOrNull { pendingMove -> item.isPendingMoveTarget(pendingMove) }
            ?: continue
        keys += move.key
        keys += move.targetExternalId
        keys += item.id
        keys += item.remoteId
        item.externalId?.let { keys += it }
    }
    return keys
}

private fun TrainingItem.pendingMoveTargetExternalId(targetDate: LocalDate): String {
    return matchedStrengthRoutine?.intervalsRoutineExternalId(targetDate, startedAt?.toLocalTime())
        ?: movedCalendarRoutineExternalId(targetDate)
}

private fun TrainingItem.movedCalendarRoutineExternalId(date: LocalDate): String {
    val sourceId = remoteId.ifBlank { id }.replace(Regex("""[^A-Za-z0-9_.-]"""), "-")
    return "intervals-gym-moved-routine-$sourceId-$date"
}

private fun TrainingItem.withPendingMoveDate(move: PendingCalendarRoutineMove): TrainingItem {
    val movedStart = startedAt?.let { move.targetDate.atTime(it.toLocalTime()) }
        ?: move.targetDate.atStartOfDay()
    return copy(
        id = "$PENDING_MOVE_ID_PREFIX${move.key}-${move.targetDate}",
        externalId = move.targetExternalId,
        date = move.targetDate,
        startedAt = movedStart,
        timeLabel = movedStart.toLocalTime().formatClockTime()
    )
}

private fun TrainingItem.isPendingMoveSource(move: PendingCalendarRoutineMove): Boolean {
    return date == move.sourceRoutine.date &&
        listOfNotNull(id, remoteId, externalId).any { key ->
            key == move.sourceRoutine.id ||
                key == move.sourceRoutine.remoteId ||
                key == move.sourceRoutine.externalId ||
                key == move.key
        }
}

private fun TrainingItem.isPendingMoveTarget(move: PendingCalendarRoutineMove): Boolean {
    return date == move.targetDate &&
        listOfNotNull(id, remoteId, externalId).any { key ->
            key == move.targetExternalId ||
                key == "$PENDING_MOVE_ID_PREFIX${move.key}-${move.targetDate}"
        }
}

private fun TrainingItem.isSyntheticPendingMove(): Boolean {
    return id.startsWith(PENDING_MOVE_ID_PREFIX)
}

private const val PENDING_MOVE_ID_PREFIX = "pending-move-"

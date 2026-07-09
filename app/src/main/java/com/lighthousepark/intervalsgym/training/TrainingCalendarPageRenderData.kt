package com.lighthousepark.intervalsgym.training

import com.lighthousepark.intervalsgym.data.withLocalRunningResults
import com.lighthousepark.intervalsgym.data.withLocalStrengthResults
import com.lighthousepark.intervalsgym.data.withLocalStrengthRoutines
import com.lighthousepark.intervalsgym.running.CompletedRunningSession
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.ScheduledStrengthRoutine
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import java.time.LocalDate

internal data class TrainingCalendarPageRenderData(
    val activities: List<TrainingItem>,
    val routines: List<TrainingItem>,
    val sortedItems: List<TrainingItem>,
    val movableScheduledRoutineKeys: Set<String>,
    val pendingRoutineKeys: Set<String>,
    val initialScrollDate: LocalDate?,
    val hasCachedRemoteData: Boolean,
)

internal fun buildTrainingCalendarPageRenderData(
    pageRange: TrainingDateRange,
    baseDate: LocalDate,
    calendarMode: TrainingCalendarMode,
    isRemoteConnected: Boolean,
    isLoadedPage: Boolean,
    loadedActivities: List<TrainingItem>,
    loadedRoutines: List<TrainingItem>,
    cachedRemoteData: WeekTrainingData?,
    localStrengthHistory: List<CompletedStrengthSession>,
    localRunningHistory: List<CompletedRunningSession>,
    localScheduledStrengthRoutines: List<ScheduledStrengthRoutine>,
    strengthRoutines: List<StrengthWorkoutRoutine>,
    deletedCalendarRoutineIds: Set<String>,
    optimisticallyDeletedCalendarRoutineKeys: Set<String>,
    pendingCalendarRoutineMoves: Collection<PendingCalendarRoutineMove>,
): TrainingCalendarPageRenderData {
    val remotePageActivities = when {
        !isRemoteConnected -> emptyList()
        isLoadedPage -> loadedActivities
        cachedRemoteData != null -> cachedRemoteData.activities
        else -> emptyList()
    }
    val pageActivities = remotePageActivities
        .withLocalStrengthResults(localStrengthHistory, pageRange.start, pageRange.end)
        .withLocalRunningResults(localRunningHistory, pageRange.start, pageRange.end)
    val remotePageRoutines = when {
        !isRemoteConnected -> emptyList()
        isLoadedPage -> loadedRoutines
        cachedRemoteData != null -> cachedRemoteData.routines
        else -> emptyList()
    }.filterNot { item ->
        item.id in deletedCalendarRoutineIds ||
            item.remoteId in deletedCalendarRoutineIds ||
            item.hasCalendarIdentityIn(optimisticallyDeletedCalendarRoutineKeys)
    }
    val basePageRoutines = remotePageRoutines.withLocalStrengthRoutines(
        scheduledRoutines = localScheduledStrengthRoutines,
        localRoutines = strengthRoutines,
        start = pageRange.start,
        end = pageRange.end
    ).filterNot { item ->
        item.hasCalendarIdentityIn(optimisticallyDeletedCalendarRoutineKeys)
    }
    val pageRoutineRenderData = basePageRoutines.withPendingCalendarRoutineMoves(
        pendingMoves = pendingCalendarRoutineMoves,
        start = pageRange.start,
        end = pageRange.end
    )
    val pageRoutines = pageRoutineRenderData.routines
    val sortedPageItems = mergeTrainingRoutinesAndResults(
        activities = pageActivities,
        routines = pageRoutines
    ).sortedWith(
        compareBy<TrainingItem> { it.date }
            .thenBy { it.timeLabel }
            .thenBy { if (it.isRoutine) 0 else 1 }
    )

    return TrainingCalendarPageRenderData(
        activities = pageActivities,
        routines = pageRoutines,
        sortedItems = sortedPageItems,
        movableScheduledRoutineKeys = localScheduledStrengthRoutines.movableScheduledRoutineKeys(),
        pendingRoutineKeys = pageRoutineRenderData.pendingRoutineKeys,
        initialScrollDate = baseDate.takeIf {
            calendarMode == TrainingCalendarMode.WEEK &&
                !it.isBefore(pageRange.start) &&
                !it.isAfter(pageRange.end)
        },
        hasCachedRemoteData = cachedRemoteData != null
    )
}

internal fun List<ScheduledStrengthRoutine>.movableScheduledRoutineKeys(): Set<String> {
    return flatMap { scheduled ->
        listOf(
            scheduled.id,
            "local-${scheduled.id}",
            scheduled.externalId
        )
    }.toSet()
}

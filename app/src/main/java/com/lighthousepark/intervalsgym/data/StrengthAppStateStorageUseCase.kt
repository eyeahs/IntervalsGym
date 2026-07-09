package com.lighthousepark.intervalsgym.data

import android.content.SharedPreferences
import com.lighthousepark.intervalsgym.strength.ActiveStrengthSession
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.nextStrengthWorkoutRoutineId

internal data class StrengthAppStateSnapshot(
    val completedStrengthHistory: List<CompletedStrengthSession>,
    val routines: List<StrengthWorkoutRoutine>,
    val activeSession: ActiveStrengthSession?,
)

internal class StrengthAppStateStorageUseCase(
    private val prefs: SharedPreferences,
) {
    private val historyQuery = SessionHistoryQueryUseCase(prefs)

    fun loadSnapshot(
        activeSessionOverride: ActiveStrengthSession? = loadActiveStrengthSession(prefs),
    ): StrengthAppStateSnapshot {
        val completedHistory = historyQuery.loadStrengthHistory()
        return StrengthAppStateSnapshot(
            completedStrengthHistory = completedHistory,
            routines = loadStrengthRoutines(prefs).withLatestCompletedSession(completedHistory),
            activeSession = activeSessionOverride?.withLatestCompletedSession(completedHistory)
        )
    }

    fun saveStrengthRoutines(
        routines: List<StrengthWorkoutRoutine>,
        completedHistory: List<CompletedStrengthSession>,
    ): List<StrengthWorkoutRoutine> {
        saveStrengthRoutineLibrary(prefs, routines)
        return routines.withLatestCompletedSession(completedHistory)
    }

    fun saveActiveSession(session: ActiveStrengthSession?) {
        saveActiveStrengthSession(prefs, session)
    }

    fun nextStrengthRoutineId(
        routines: List<StrengthWorkoutRoutine>,
        completedHistory: List<CompletedStrengthSession>,
        activeSession: ActiveStrengthSession?,
        reservedIds: List<Int>,
    ): Int {
        return nextStrengthWorkoutRoutineId(
            routines = routines,
            history = completedHistory,
            scheduledRoutines = loadScheduledStrengthRoutines(prefs),
            activeSession = activeSession,
            reservedIds = reservedIds
        )
    }
}

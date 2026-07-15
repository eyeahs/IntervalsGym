package com.lighthousepark.intervalsgym.data

import android.content.SharedPreferences
import com.lighthousepark.intervalsgym.app.STRENGTH_ROUTINES_PREF
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
        val savedRoutines = loadStrengthRoutines(prefs)
        val appliedHistoryIds = loadAppliedStrengthRoutineHistoryIds()
        val unappliedHistory = when {
            appliedHistoryIds != null -> completedHistory.filter { it.id !in appliedHistoryIds }
            !prefs.contains(STRENGTH_ROUTINES_PREF) -> completedHistory
            else -> emptyList()
        }
        val routines = savedRoutines.withLatestCompletedSession(unappliedHistory)
        if (routines != savedRoutines) {
            saveStrengthRoutineLibrary(prefs, routines)
        }
        saveAppliedStrengthRoutineHistoryIds(completedHistory)
        return StrengthAppStateSnapshot(
            completedStrengthHistory = completedHistory,
            routines = routines,
            activeSession = activeSessionOverride
        )
    }

    fun saveStrengthRoutines(
        routines: List<StrengthWorkoutRoutine>,
        completedHistory: List<CompletedStrengthSession>,
    ): List<StrengthWorkoutRoutine> {
        saveStrengthRoutineLibrary(prefs, routines)
        saveAppliedStrengthRoutineHistoryIds(completedHistory)
        return routines
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

    private fun loadAppliedStrengthRoutineHistoryIds(): Set<String>? {
        if (!prefs.contains(APPLIED_STRENGTH_ROUTINE_HISTORY_IDS_PREF)) return null
        return prefs.getStringSet(APPLIED_STRENGTH_ROUTINE_HISTORY_IDS_PREF, emptySet())
            ?.toSet()
            .orEmpty()
    }

    private fun saveAppliedStrengthRoutineHistoryIds(
        completedHistory: List<CompletedStrengthSession>,
    ) {
        val appliedIds = loadAppliedStrengthRoutineHistoryIds().orEmpty() +
            completedHistory.map { it.id }
        prefs.edit()
            .putStringSet(
                APPLIED_STRENGTH_ROUTINE_HISTORY_IDS_PREF,
                appliedIds.toMutableSet()
            )
            .apply()
    }
}

private const val APPLIED_STRENGTH_ROUTINE_HISTORY_IDS_PREF =
    "applied_strength_routine_history_ids"

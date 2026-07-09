package com.lighthousepark.intervalsgym.data

import android.content.SharedPreferences
import com.lighthousepark.intervalsgym.running.CompletedRunningSession
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession

internal class SessionHistoryQueryUseCase(
    private val prefs: SharedPreferences,
) {
    fun loadStrengthHistory(): List<CompletedStrengthSession> {
        return loadCompletedStrengthSessionHistory(prefs)
    }

    fun loadRunningHistory(): List<CompletedRunningSession> {
        return loadCompletedRunningSessionHistory(prefs)
    }
}

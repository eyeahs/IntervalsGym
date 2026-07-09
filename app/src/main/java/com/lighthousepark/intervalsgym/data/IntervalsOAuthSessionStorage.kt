package com.lighthousepark.intervalsgym.data

import android.content.SharedPreferences
import com.lighthousepark.intervalsgym.app.INTERVALS_LOGIN_PROMPT_SEEN_PREF
import com.lighthousepark.intervalsgym.app.INTERVALS_OAUTH_STATE_PREF
import com.lighthousepark.intervalsgym.app.INTERVALS_OAUTH_TOKEN_PREF
import com.lighthousepark.intervalsgym.app.LEGACY_INTERVALS_CREDENTIAL_PREF

internal class IntervalsOAuthSessionStorage(
    private val prefs: SharedPreferences,
) {
    fun loadToken(): IntervalsOAuthToken? {
        return prefs.getString(INTERVALS_OAUTH_TOKEN_PREF, null).toIntervalsOAuthToken()
    }

    fun hasSeenLoginPrompt(): Boolean {
        return prefs.getBoolean(INTERVALS_LOGIN_PROMPT_SEEN_PREF, false)
    }

    fun savePendingState(state: String) {
        prefs.edit().putString(INTERVALS_OAUTH_STATE_PREF, state).apply()
    }

    fun loadPendingState(): String {
        return prefs.getString(INTERVALS_OAUTH_STATE_PREF, "").orEmpty()
    }

    fun saveConnectedToken(token: IntervalsOAuthToken) {
        prefs.edit()
            .putString(INTERVALS_OAUTH_TOKEN_PREF, token.toJsonString())
            .remove(LEGACY_INTERVALS_CREDENTIAL_PREF)
            .remove(INTERVALS_OAUTH_STATE_PREF)
            .putBoolean(INTERVALS_LOGIN_PROMPT_SEEN_PREF, true)
            .apply()
    }

    fun clearConnectedToken() {
        prefs.edit()
            .remove(LEGACY_INTERVALS_CREDENTIAL_PREF)
            .remove(INTERVALS_OAUTH_TOKEN_PREF)
            .remove(INTERVALS_OAUTH_STATE_PREF)
            .apply()
    }

    fun markLoginPromptSeen() {
        prefs.edit()
            .remove(LEGACY_INTERVALS_CREDENTIAL_PREF)
            .putBoolean(INTERVALS_LOGIN_PROMPT_SEEN_PREF, true)
            .apply()
    }
}

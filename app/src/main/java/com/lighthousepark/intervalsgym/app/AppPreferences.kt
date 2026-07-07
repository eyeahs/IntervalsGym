package com.lighthousepark.intervalsgym.app

import com.lighthousepark.intervalsgym.MainActivity
import com.lighthousepark.intervalsgym.R
import com.lighthousepark.intervalsgym.app.*
import com.lighthousepark.intervalsgym.core.*
import com.lighthousepark.intervalsgym.data.*
import com.lighthousepark.intervalsgym.login.*
import com.lighthousepark.intervalsgym.overlay.*
import com.lighthousepark.intervalsgym.running.*
import com.lighthousepark.intervalsgym.running.ui.*
import com.lighthousepark.intervalsgym.strength.*
import com.lighthousepark.intervalsgym.strength.ui.*
import com.lighthousepark.intervalsgym.training.*
import com.lighthousepark.intervalsgym.training.ui.*
import com.lighthousepark.intervalsgym.workout.ui.*

internal const val PREFS_NAME = "intervals_gym"
internal const val LEGACY_INTERVALS_CREDENTIAL_PREF = "intervals_api_key"
internal const val INTERVALS_LOGIN_PROMPT_SEEN_PREF = "intervals_login_prompt_seen"
internal const val STRENGTH_ROUTINES_PREF = "strength_routines"
internal const val ACTIVE_STRENGTH_SESSION_PREF = "active_strength_session"
internal const val STRENGTH_SESSION_HISTORY_PREF = "strength_session_history"
internal const val RUNNING_SESSION_HISTORY_PREF = "running_session_history"
internal const val SAVED_RUNNING_ROUTINES_PREF = "saved_running_routines"
internal const val SCHEDULED_STRENGTH_ROUTINES_PREF = "scheduled_strength_routines"
internal const val INTERVALS_OAUTH_TOKEN_PREF = "intervals_oauth_token"
internal const val INTERVALS_OAUTH_STATE_PREF = "intervals_oauth_state"
internal const val INTERVALS_GYM_STRENGTH_ROUTINE_PREFIX = "INTERVALS_GYM_STRENGTH_ROUTINE:"
internal const val INTERVALS_GYM_STRENGTH_ROUTINE_ID_PREFIX = "INTERVALS_GYM_STRENGTH_ROUTINE_ID:"

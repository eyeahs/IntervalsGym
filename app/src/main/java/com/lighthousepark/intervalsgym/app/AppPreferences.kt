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
internal const val API_KEY_PREF = "intervals_api_key"
internal const val INTERVALS_LOGIN_PROMPT_SEEN_PREF = "intervals_login_prompt_seen"
internal const val STRENGTH_PLANS_PREF = "strength_plans"
internal const val ACTIVE_STRENGTH_SESSION_PREF = "active_strength_session"
internal const val STRENGTH_WORKOUT_HISTORY_PREF = "strength_workout_history"
internal const val RUNNING_WORKOUT_HISTORY_PREF = "running_workout_history"
internal const val SAVED_RUNNING_PLANS_PREF = "saved_running_plans"
internal const val SCHEDULED_STRENGTH_PLANS_PREF = "scheduled_strength_plans"
internal const val INTERVALS_OAUTH_TOKEN_PREF = "intervals_oauth_token"
internal const val INTERVALS_OAUTH_STATE_PREF = "intervals_oauth_state"
internal const val INTERVALS_GYM_STRENGTH_PLAN_PREFIX = "INTERVALS_GYM_STRENGTH_PLAN:"

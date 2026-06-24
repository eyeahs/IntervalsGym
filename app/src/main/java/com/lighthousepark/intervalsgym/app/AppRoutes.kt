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

internal const val ROUTE_LOGIN = "login"
internal const val ROUTE_WEEK = "week"
internal const val ROUTE_TRAINING_DAY = "training_day"
internal const val ROUTE_WORKOUT_PLAN = "workout_plan"
internal const val ROUTE_RUNNING_PLANS = "running_plans"
internal const val ROUTE_RUNNING_MANAGE = "running_manage"
internal const val ROUTE_STRENGTH_PLANS = "strength_plans"
internal const val ROUTE_STRENGTH_MANAGE = "strength_manage"
internal const val ROUTE_STRENGTH_PLAN_EDIT = "strength_plan_edit"
internal const val ROUTE_STRENGTH_SESSION = "strength_session"
internal const val ROUTE_STRENGTH_HISTORY = "strength_history"

internal fun trainingDayRoute(date: java.time.LocalDate): String {
    return "$ROUTE_TRAINING_DAY/$date"
}

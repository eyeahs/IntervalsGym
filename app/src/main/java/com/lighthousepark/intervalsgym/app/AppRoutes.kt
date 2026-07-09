package com.lighthousepark.intervalsgym.app

import java.time.LocalDate

internal const val ROUTE_LOGIN = "login"
internal const val ROUTE_WEEK = "week"
internal const val ROUTE_TRAINING_DAY = "training_day"
internal const val ROUTE_WORKOUT_ROUTINE = "workout_routine"
internal const val ROUTE_RUNNING_ROUTINES = "running_routines"
internal const val ROUTE_RUNNING_MANAGE = "running_manage"
internal const val ROUTE_STRENGTH_ROUTINES = "strength_routines"
internal const val ROUTE_STRENGTH_MANAGE = "strength_manage"
internal const val ROUTE_STRENGTH_ROUTINE_EDIT = "strength_routine_edit"
internal const val ROUTE_STRENGTH_SESSION = "strength_session"
internal const val ROUTE_STRENGTH_HISTORY = "strength_history"

internal fun trainingDayRoute(date: LocalDate): String {
    return "$ROUTE_TRAINING_DAY/$date"
}

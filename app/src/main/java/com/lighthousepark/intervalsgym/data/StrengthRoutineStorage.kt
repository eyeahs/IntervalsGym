package com.lighthousepark.intervalsgym.data

import android.content.SharedPreferences
import com.lighthousepark.intervalsgym.app.STRENGTH_ROUTINES_PREF
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.defaultStrengthRoutines

internal fun loadStrengthRoutines(prefs: SharedPreferences): List<StrengthWorkoutRoutine> {
    val saved = prefs.getString(STRENGTH_ROUTINES_PREF, null)
    return saved.toStrengthWorkoutRoutines().takeIf { it.isNotEmpty() } ?: defaultStrengthRoutines()
}

internal fun saveStrengthRoutineLibrary(
    prefs: SharedPreferences,
    routines: List<StrengthWorkoutRoutine>,
) {
    prefs.edit().putString(STRENGTH_ROUTINES_PREF, routines.toJsonString()).apply()
}

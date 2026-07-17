package com.lighthousepark.intervalsgym.data

import android.content.SharedPreferences
import org.json.JSONArray

private const val STRENGTH_LOCATIONS_PREF = "strength_locations"

internal fun loadStrengthLocations(prefs: SharedPreferences): List<String> {
    val saved = prefs.getString(STRENGTH_LOCATIONS_PREF, null)
    val locations = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    return (0 until locations.length())
        .mapNotNull { index -> locations.optString(index).normalizedStrengthLocationOrNull() }
        .distinctBy { location -> location.lowercase() }
}

internal fun addStrengthLocation(
    prefs: SharedPreferences,
    location: String,
): List<String> {
    val normalizedLocation = location.normalizedStrengthLocationOrNull()
        ?: return loadStrengthLocations(prefs)
    val savedLocations = loadStrengthLocations(prefs)
    val nextLocations = if (savedLocations.any { it.equals(normalizedLocation, ignoreCase = true) }) {
        savedLocations
    } else {
        savedLocations + normalizedLocation
    }
    prefs.edit()
        .putString(STRENGTH_LOCATIONS_PREF, JSONArray(nextLocations).toString())
        .apply()
    return nextLocations
}

internal fun removeStrengthLocation(
    prefs: SharedPreferences,
    location: String,
): List<String> {
    val normalizedLocation = location.normalizedStrengthLocationOrNull()
        ?: return loadStrengthLocations(prefs)
    val nextLocations = loadStrengthLocations(prefs)
        .filterNot { savedLocation -> savedLocation.equals(normalizedLocation, ignoreCase = true) }
    prefs.edit()
        .putString(STRENGTH_LOCATIONS_PREF, JSONArray(nextLocations).toString())
        .apply()
    return nextLocations
}

private fun String.normalizedStrengthLocationOrNull(): String? {
    return trim().takeIf { it.isNotEmpty() }
}

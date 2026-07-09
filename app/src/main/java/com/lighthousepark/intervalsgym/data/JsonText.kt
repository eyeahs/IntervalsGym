package com.lighthousepark.intervalsgym.data

internal fun String?.cleanJsonText(): String? {
    return this
        ?.trim()
        ?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
}

package com.lighthousepark.intervalsgym.strength

internal fun StrengthRoutineEntry.isUnilateral(): Boolean {
    if (exercise.single) return true
    if (splitVariationAndUnilateral(exercise, variation).second == "한쪽") return true
    val text = listOf(exercise.nameKo, exercise.nameEn, equipment, variation, title)
        .joinToString(" ")
        .normalizedSearchText()
    return UNILATERAL_VARIATION_KEYWORDS
        .any { keyword -> text.contains(keyword.normalizedSearchText()) }
}

internal fun StrengthRoutineEntry.weightInputUnitLabel(): String {
    return if (equipment.trim() == "맨몸") "체중" else "kg"
}

internal fun StrengthSetRecord.unilateralWeightSummary(): String {
    return "${weightKg.ifBlank { "-" }}kg"
}

internal fun StrengthSetRecord.unilateralRepsSummary(): String {
    return "각 ${reps.ifBlank { "-" }}회"
}

internal fun formatStrengthExerciseTitle(
    exercise: StrengthExercise,
    equipment: String,
    variation: String,
): String {
    val safeEquipment = equipment.takeUnless { it.isBlank() || it == "기본" }
    val rawVariation = variation.takeUnless { it.isBlank() || it == "기본" }
    val isUnilateral = rawVariation?.hasUnilateralMarker() == true
    val safeVariation = rawVariation
        ?.withoutUnilateralMarker()
        ?.takeUnless { it.isBlank() || it == "기본" }

    val title = when (exercise.id) {
        "squat" -> {
            val squatVariation = safeVariation?.replace(" ", "")
            val suffix = "스쿼트".takeUnless { squatVariation?.contains("스쿼트") == true }
            listOfNotNull(safeEquipment, squatVariation, suffix).joinToString(" ")
        }
        "bench_press" -> listOfNotNull(safeVariation, safeEquipment, "벤치프레스").joinToString(" ")
        "row" -> listOfNotNull(safeEquipment, "로우", safeVariation).joinToString(" ")
        else -> listOfNotNull(safeVariation, safeEquipment, exercise.nameKo).joinToString(" ")
    }
    return listOfNotNull("싱글".takeIf { isUnilateral }, title)
        .joinToString(" ")
}

private fun String.hasUnilateralMarker(): Boolean {
    val normalizedText = normalizedSearchText()
    return UNILATERAL_VARIATION_KEYWORDS.any { keyword ->
        normalizedText.contains(keyword.normalizedSearchText())
    }
}

private fun String.withoutUnilateralMarker(): String {
    return replace(Regex("(?i)single\\s*leg|single\\s*arm|single"), " ")
        .let { text ->
            listOf("싱글레그", "싱글암", "싱글", "원암", "한팔", "한쪽").fold(text) { acc, keyword ->
                acc.replace(keyword, " ")
            }
        }
        .trim()
        .replace(Regex("\\s+"), " ")
}

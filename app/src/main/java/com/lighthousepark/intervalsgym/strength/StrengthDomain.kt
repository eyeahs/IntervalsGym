package com.lighthousepark.intervalsgym.strength

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

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

internal val CUSTOM_STRENGTH_EQUIPMENT_OPTIONS = listOf("맨몸", "바벨", "덤벨", "케틀벨", "스미스", "트랩바", "케이블", "직접 입력")

internal data class StrengthExercise(
    val id: String,
    val nameKo: String,
    val nameEn: String,
    val group: String,
    val equipmentOptions: List<String>,
    val variationOptions: List<String>,
    val aliases: List<String> = emptyList(),
)

internal val UNILATERAL_MODE_OPTIONS = listOf("양쪽", "한쪽")
private val UNILATERAL_VARIATION_KEYWORDS = listOf("싱글레그", "싱글암", "싱글", "원암", "한팔", "한쪽", "singleleg", "singlearm", "single")

internal fun StrengthExercise.equipmentOptionsWithBodyweight(): List<String> {
    return (equipmentOptions + listOf("맨몸", "케이블")).distinct()
}

internal fun StrengthExercise.baseVariationOptions(): List<String> {
    return variationOptions
        .filterNot { option -> option.normalizedSearchText().let { normalized ->
            UNILATERAL_VARIATION_KEYWORDS.any { keyword -> normalized == keyword.normalizedSearchText() }
        } }
        .ifEmpty { listOf("기본") }
}

internal fun StrengthExercise.matchesSearch(query: String): Boolean {
    val normalizedQuery = query.normalizedSearchText()
    if (normalizedQuery.isBlank()) return true
    return searchableText().any { it.normalizedSearchText().contains(normalizedQuery) }
}

internal fun StrengthExercise.searchableText(): List<String> {
    return listOf(nameKo, nameEn, group) + equipmentOptions + variationOptions + aliases
}

internal fun StrengthExercise.inferEquipmentFromSearch(
    query: String,
    equipmentOptions: List<String>,
): String? {
    val normalizedQuery = query.normalizedSearchText()
    if (normalizedQuery.isBlank()) return null
    val aliasMatches = listOf(
        "팩 덱 머신" to listOf("펙덱", "팩덱", "팩댁", "팩 덱", "팩 댁", "pecdeck", "pec deck"),
        "버터플라이 머신" to listOf("버터플라이", "butterfly"),
        "덤벨" to listOf("덤벨", "dumbbell", "db"),
        "케이블" to listOf("케이블", "cable"),
        "밴드" to listOf("밴드", "band"),
        "머신" to listOf("머신", "machine")
    )
    aliasMatches.firstOrNull { (equipment, aliases) ->
        equipment in equipmentOptions &&
            aliases.any { alias -> normalizedQuery.contains(alias.normalizedSearchText()) }
    }?.let { return it.first }
    return equipmentOptions.firstOrNull { option ->
        normalizedQuery.contains(option.normalizedSearchText())
    }
}

internal fun StrengthExercise.inferVariationFromSearch(query: String): String? {
    val normalizedQuery = query.normalizedSearchText()
    if (normalizedQuery.isBlank()) return null
    val variationOptions = baseVariationOptions()
    val aliasMatches = listOf(
        "리버스 펙덱" to listOf("리버스펙덱", "리버스팩덱", "reversepecdeck", "reversefly"),
        "인버티드" to listOf("인버티드", "invertedfly", "inverted"),
        "데드버그" to listOf("데드 버그", "deadbug", "dead bug", "deadbugcrunch", "dead bug crunch")
    )
    aliasMatches.firstOrNull { (variation, aliases) ->
        variation in variationOptions &&
            aliases.any { alias -> normalizedQuery.contains(alias.normalizedSearchText()) }
    }?.let { return it.first }
    return variationOptions.firstOrNull { option ->
        option != "기본" && normalizedQuery.contains(option.normalizedSearchText())
    }
}

internal fun StrengthExercise.searchResultTitle(query: String): String {
    val matchedVariation = inferVariationFromSearch(query)
        ?.takeUnless { it == "기본" || nameKo.normalizedSearchText().contains(it.normalizedSearchText()) }
    return listOfNotNull(matchedVariation, nameKo).joinToString(" ")
}

internal fun StrengthExercise.inferUnilateralFromSearch(query: String): String? {
    val normalizedQuery = query.normalizedSearchText()
    if (normalizedQuery.isBlank()) return null
    return "한쪽".takeIf {
        UNILATERAL_VARIATION_KEYWORDS.any { keyword ->
            normalizedQuery.contains(keyword.normalizedSearchText())
        }
    }
}

internal fun String.normalizedSearchText(): String {
    return lowercase(Locale.KOREAN).replace(Regex("\\s+"), "")
}

internal fun splitVariationAndUnilateral(
    exercise: StrengthExercise,
    variation: String,
): Pair<String, String> {
    val baseOptions = exercise.baseVariationOptions()
    val normalizedVariation = variation.normalizedSearchText()
    val isUnilateral = UNILATERAL_VARIATION_KEYWORDS.any { keyword ->
        normalizedVariation.contains(keyword.normalizedSearchText())
    }
    val unilateral = if (isUnilateral) "한쪽" else "양쪽"
    val base = baseOptions.firstOrNull { option ->
        normalizedVariation.contains(option.normalizedSearchText())
    } ?: baseOptions.first()
    return base to unilateral
}

internal fun combineVariationAndUnilateral(
    variation: String,
    unilateral: String,
): String {
    val safeVariation = variation.ifBlank { "기본" }
    val safeUnilateral = unilateral.takeUnless { it.isBlank() || it == "양쪽" }
    return listOfNotNull(safeUnilateral, safeVariation.takeUnless { it == "기본" })
        .joinToString(" ")
        .ifBlank { "기본" }
}

internal data class StrengthWorkoutPlan(
    val id: Int,
    val name: String,
    val entries: List<StrengthPlanEntry>,
)

internal data class ScheduledStrengthPlan(
    val id: String,
    val date: LocalDate,
    val plan: StrengthWorkoutPlan,
    val uploadedToIntervals: Boolean,
    val externalId: String,
)

internal data class ActiveStrengthSession(
    val planId: Int,
    val planName: String,
    val entries: List<StrengthPlanEntry>,
    val hasStarted: Boolean,
    val workoutStartedAtMillis: Long,
    val isSetScreenVisible: Boolean,
    val currentExerciseIndex: Int,
    val currentSetIndex: Int,
    val pendingExerciseIndex: Int?,
    val pendingSetIndex: Int?,
    val restEndAtMillis: Long,
    val isRestSheetVisible: Boolean,
    val restTitle: String,
    val setEvents: List<StrengthSetCompletionEvent>,
    val restEvents: List<StrengthRestEvent>,
    val activeRestEventId: Int?,
) {
    fun toWorkoutPlan(): StrengthWorkoutPlan {
        return StrengthWorkoutPlan(
            id = planId,
            name = planName,
            entries = entries
        )
    }
}

internal data class CompletedStrengthWorkout(
    val id: String,
    val planId: Int,
    val planName: String,
    val startedAtMillis: Long,
    val endedAtMillis: Long,
    val durationSeconds: Int,
    val intervalsExternalId: String,
    val entries: List<StrengthPlanEntry>,
    val setEvents: List<StrengthSetCompletionEvent>,
    val restEvents: List<StrengthRestEvent>,
    val rpe: Int,
    val trainingLoad: Int,
    val uploadedToIntervals: Boolean,
)

internal data class StrengthSetCompletionEvent(
    val sequence: Int,
    val exerciseEntryId: Int,
    val exerciseTitle: String,
    val exerciseGroup: String,
    val exerciseId: String,
    val equipment: String,
    val variation: String,
    val setRecordId: Int,
    val setIndex: Int,
    val weightKg: String,
    val reps: String,
    val targetRestSeconds: Int,
    val completedAtMillis: Long,
)

internal data class StrengthRestEvent(
    val id: Int,
    val afterSetSequence: Int,
    val exerciseEntryId: Int,
    val exerciseTitle: String,
    val setRecordId: Int,
    val setIndex: Int,
    val startedAtMillis: Long,
    val plannedSeconds: Int,
    val targetEndAtMillis: Long,
    val endedAtMillis: Long?,
    val endReason: String?,
) {
    val actualSeconds: Int
        get() = endedAtMillis
            ?.let { ((it - startedAtMillis) / 1000L).toInt().coerceAtLeast(0) }
            ?: 0
}

internal data class StrengthPlanEntry(
    val id: Int,
    val exercise: StrengthExercise,
    val equipment: String,
    val variation: String,
    val supersetGroupId: Int?,
    val targetSets: Int,
    val targetReps: Int,
    val restSeconds: Int,
    val targetWeightKg: String,
    val records: List<StrengthSetRecord>,
) {
    val title: String
        get() = formatStrengthExerciseTitle(exercise, equipment, variation)
}

internal data class StrengthSetRecord(
    val id: Int,
    val weightKg: String,
    val reps: String,
    val leftWeightKg: String = weightKg,
    val leftReps: String = reps,
    val rightWeightKg: String = weightKg,
    val rightReps: String = reps,
    val durationSeconds: String,
    val restSeconds: String,
    val completed: Boolean,
)

internal data class StrengthWorkoutSession(
    val name: String,
    val startedAt: LocalDateTime,
    val entries: List<StrengthPlanEntry>,
    val rpe: Int,
    val trainingLoad: Int,
)

internal val strengthExerciseCatalog = listOf(
    StrengthExercise("deadlift", "데드리프트", "Deadlift", "하체/후면사슬", listOf("바벨", "덤벨", "케틀벨", "스미스", "트랩바"), listOf("기본", "루마니안", "스모", "스티프레그", "싱글레그", "블록 풀")),
    StrengthExercise("bench_press", "벤치프레스", "Bench Press", "가슴", listOf("바벨", "덤벨", "스미스", "머신"), listOf("플랫", "인클라인", "디클라인", "클로즈그립", "와이드그립", "템포")),
    StrengthExercise("chest_press", "체스트 프레스", "Chest Press", "가슴", listOf("머신", "케이블"), listOf("기본", "인클라인", "디클라인", "시티드", "플레이트 로드")),
    StrengthExercise("squat", "스쿼트", "Squat", "하체", listOf("바벨", "덤벨", "케틀벨", "스미스", "머신"), listOf("백 스쿼트", "프론트 스쿼트", "고블릿", "불가리안 스플릿", "박스")),
    StrengthExercise("hack_squat", "핵스쿼트", "Hack Squat", "하체", listOf("머신"), listOf("기본", "리버스", "싱글레그")),
    StrengthExercise("overhead_press", "오버헤드 프레스", "Overhead Press", "어깨", listOf("바벨", "덤벨", "케틀벨", "스미스", "머신"), listOf("스탠딩", "시티드", "푸시 프레스", "아놀드", "싱글암")),
    StrengthExercise("overhead_extension", "오버헤드 익스텐션", "Overhead Extension", "어깨", listOf("덤벨", "케이블", "밴드", "EZ바", "바벨", "케틀벨"), listOf("기본", "스탠딩", "시티드", "인클라인 벤치"), aliases = listOf("오버 헤드 익스텐션", "Over Head Extension")),
    StrengthExercise("row", "로우", "Row", "등", listOf("바벨", "덤벨", "케이블", "머신", "랜드마인"), listOf("벤트오버", "원암", "시티드", "펜들레이", "체스트 서포티드", "티바")),
    StrengthExercise("pull_up", "풀업", "Pull-up", "등", listOf("맨몸", "어시스트 머신", "밴드", "중량벨트"), listOf("풀업", "친업", "뉴트럴그립", "와이드그립", "클로즈그립")),
    StrengthExercise("lat_pulldown", "랫풀다운", "Lat Pulldown", "등", listOf("케이블", "머신"), listOf("와이드그립", "언더그립", "뉴트럴그립", "싱글암", "스트레이트암")),
    StrengthExercise("lunge", "런지", "Lunge", "하체", listOf("맨몸", "덤벨", "바벨", "스미스", "케틀벨", "케이블"), listOf("워킹", "리버스", "포워드", "사이드", "불가리안", "회전")),
    StrengthExercise("hip_thrust", "힙 쓰러스트", "Hip Thrust", "둔근", listOf("바벨", "덤벨", "스미스", "머신", "밴드"), listOf("기본", "싱글레그", "글루트 브릿지", "템포", "밴드 어브덕션")),
    StrengthExercise("leg_press", "레그프레스", "Leg Press", "하체", listOf("머신"), listOf("기본", "하이 풋", "로우 풋", "와이드", "싱글레그")),
    StrengthExercise("leg_extension", "레그 익스텐션", "Leg Extension", "대퇴사두", listOf("머신"), listOf("기본", "싱글레그", "템포", "피크 수축")),
    StrengthExercise("leg_curl", "레그 컬", "Leg Curl", "햄스트링", listOf("머신", "케이블", "밴드"), listOf("라잉", "시티드", "스탠딩", "싱글레그")),
    StrengthExercise("calf_raise", "카프 레이즈", "Calf Raise", "종아리", listOf("머신", "덤벨", "바벨", "스미스", "맨몸"), listOf("스탠딩", "시티드", "싱글레그", "레그프레스")),
    StrengthExercise("chest_fly", "플라이", "Fly", "가슴", listOf("팩 덱 머신", "버터플라이 머신", "덤벨", "케이블", "밴드"), listOf("기본", "플랫", "인클라인", "디클라인", "하이투로우", "로우투하이"), aliases = listOf("체스트 플라이", "Chest Fly", "Pec Deck", "펙덱", "펙 덱", "팩덱", "팩 덱", "팩 댁", "버터플라이")),
    StrengthExercise("dip", "딥스", "Dip", "가슴/삼두", listOf("맨몸", "어시스트 머신", "중량벨트"), listOf("가슴 중심", "삼두 중심", "벤치 딥", "링 딥")),
    StrengthExercise("push_up", "푸쉬업", "Push-up", "가슴", listOf("맨몸", "밴드", "중량조끼"), listOf("기본", "인클라인", "디클라인", "다이아몬드", "와이드", "아처")),
    StrengthExercise("shoulder_raise", "숄더 레이즈", "Shoulder Raise", "어깨", listOf("덤벨", "케이블", "머신", "밴드"), listOf("사이드", "프론트", "리어델트", "Y 레이즈", "린어웨이"), aliases = listOf("레터럴 레이즈", "래터럴 레이즈", "Lateral Raise")),
    StrengthExercise("rear_delt_fly", "리어 델트 플라이", "Rear Delt Fly", "후면어깨", listOf("팩 덱 머신", "버터플라이 머신", "덤벨", "케이블", "밴드"), listOf("기본", "벤트오버", "시티드", "인클라인", "리버스 펙덱", "인버티드"), aliases = listOf("리어델트 플라이", "후면 어깨 플라이", "후면어깨 플라이", "리버스 플라이", "인버티드 플라이", "Reverse Fly", "Inverted Fly")),
    StrengthExercise("face_pull", "페이스 풀", "Face Pull", "후면어깨", listOf("케이블", "밴드"), listOf("로프", "밴드", "하이풀", "외회전")),
    StrengthExercise("biceps_curl", "바이셉스 컬", "Biceps Curl", "이두", listOf("덤벨", "바벨", "EZ바", "케이블", "머신", "밴드"), listOf("스탠딩", "시티드", "해머", "프리처", "인클라인", "컨센트레이션")),
    StrengthExercise("triceps_extension", "트라이셉스 익스텐션", "Triceps Extension", "삼두", listOf("덤벨", "EZ바", "케이블", "머신", "밴드"), listOf("오버헤드", "스컬크러셔", "푸시다운", "킥백", "로프")),
    StrengthExercise("clean", "클린", "Clean", "전신/파워", listOf("바벨", "덤벨", "케틀벨"), listOf("파워 클린", "행 클린", "머슬 클린", "클린 풀")),
    StrengthExercise("snatch", "스내치", "Snatch", "전신/파워", listOf("바벨", "덤벨", "케틀벨"), listOf("파워 스내치", "행 스내치", "머슬 스내치", "스내치 풀")),
    StrengthExercise("kettlebell_swing", "케틀벨 스윙", "Kettlebell Swing", "후면사슬", listOf("케틀벨", "덤벨"), listOf("러시안", "아메리칸", "싱글암", "핸드투핸드")),
    StrengthExercise("farmers_carry", "파머스 캐리", "Farmer's Carry", "전신/그립", listOf("덤벨", "케틀벨", "트랩바", "캐리 핸들"), listOf("양손", "싱글암", "슈트케이스", "랙 캐리", "오버헤드 캐리")),
    StrengthExercise("plank", "플랭크", "Plank", "코어", listOf("맨몸", "중량", "밴드"), listOf("기본", "사이드", "RKC", "리버스", "숄더탭")),
    StrengthExercise("crunch", "크런치", "Crunch", "코어", listOf("맨몸", "케이블", "머신", "짐볼"), listOf("기본", "케이블", "리버스", "바이시클", "데드버그"), aliases = listOf("데드 버그", "데드버그 크런치", "Dead Bug", "Deadbug", "Dead Bug Crunch")),
    StrengthExercise("woodchop", "우드찹", "Woodchop", "코어", listOf("케이블", "밴드", "메디신볼"), listOf("하이투로우", "로우투하이", "수평", "하프니링")),
)

internal fun defaultStrengthPlans(): List<StrengthWorkoutPlan> {
    val squat = strengthExerciseCatalog.first { it.id == "squat" }
    val bench = strengthExerciseCatalog.first { it.id == "bench_press" }
    val row = strengthExerciseCatalog.first { it.id == "row" }
    return listOf(
        StrengthWorkoutPlan(
            id = 1,
            name = "전신 기본",
            entries = listOf(
                defaultStrengthPlanEntry(id = 1, exercise = squat, weightKg = "", reps = "8", restSeconds = "120"),
                defaultStrengthPlanEntry(id = 2, exercise = bench, weightKg = "", reps = "8", restSeconds = "120"),
                defaultStrengthPlanEntry(id = 3, exercise = row, weightKg = "", reps = "10", restSeconds = "90")
            )
        )
    )
}

internal fun defaultStrengthPlanEntry(
    id: Int,
    exercise: StrengthExercise,
    weightKg: String = defaultStrengthWeightForEquipment(exercise.equipmentOptions.first()),
    reps: String = "8",
    restSeconds: String = "120",
): StrengthPlanEntry {
    val records = List(3) { index ->
        StrengthSetRecord(
            id = index + 1,
            weightKg = weightKg,
            reps = reps,
            durationSeconds = "",
            restSeconds = restSeconds,
            completed = false
        )
    }
    return StrengthPlanEntry(
        id = id,
        exercise = exercise,
        equipment = exercise.equipmentOptions.first(),
        variation = exercise.variationOptions.first(),
        supersetGroupId = null,
        targetSets = records.size,
        targetReps = reps.toIntOrNull() ?: 0,
        restSeconds = restSeconds.toIntOrNull() ?: 0,
        targetWeightKg = weightKg,
        records = records
    )
}

internal fun defaultStrengthWeightForEquipment(equipment: String): String {
    return if (equipment.trim() == "맨몸") "" else "10"
}

internal fun customStrengthExercise(name: String): StrengthExercise {
    val safeName = name.trim().ifBlank { "사용자 운동" }
    val safeId = "custom_" + safeName
        .lowercase(Locale.US)
        .replace(Regex("[^a-z0-9가-힣]+"), "_")
        .trim('_')
        .ifBlank { System.currentTimeMillis().toString() }
    return StrengthExercise(
        id = safeId,
        nameKo = safeName,
        nameEn = safeName,
        group = "사용자 추가",
        equipmentOptions = CUSTOM_STRENGTH_EQUIPMENT_OPTIONS,
        variationOptions = listOf("기본")
    )
}

internal fun defaultStrengthSetRecord(entry: StrengthPlanEntry): StrengthSetRecord {
    val last = entry.records.lastOrNull()
    val weightKg = last?.weightKg ?: entry.targetWeightKg
    val reps = last?.reps ?: entry.targetReps.takeIf { it > 0 }?.toString().orEmpty()
    return StrengthSetRecord(
        id = (entry.records.maxOfOrNull { it.id } ?: 0) + 1,
        weightKg = weightKg,
        reps = reps,
        leftWeightKg = last?.leftWeightKg ?: weightKg,
        leftReps = last?.leftReps ?: reps,
        rightWeightKg = last?.rightWeightKg ?: weightKg,
        rightReps = last?.rightReps ?: reps,
        durationSeconds = last?.durationSeconds.orEmpty(),
        restSeconds = last?.restSeconds ?: entry.restSeconds.takeIf { it > 0 }?.toString().orEmpty(),
        completed = false
    )
}

internal fun StrengthPlanEntry.withRecords(records: List<StrengthSetRecord>): StrengthPlanEntry {
    val first = records.firstOrNull()
    return copy(
        targetSets = records.size,
        targetReps = first?.reps?.toIntOrNull() ?: targetReps,
        restSeconds = first?.restSeconds?.toIntOrNull() ?: restSeconds,
        targetWeightKg = first?.weightKg ?: targetWeightKg,
        records = records
    )
}

internal fun List<StrengthPlanEntry>.supersetGroupLabels(): Map<Int, String> {
    return mapNotNull { it.supersetGroupId }
        .distinct()
        .mapIndexed { index, groupId -> groupId to "슈퍼세트 ${supersetGroupName(index)}" }
        .toMap()
}

internal fun <T> List<T>.moveItem(fromIndex: Int, toIndex: Int): List<T> {
    if (fromIndex !in indices || toIndex !in indices || fromIndex == toIndex) return this
    return toMutableList().apply {
        add(toIndex, removeAt(fromIndex))
    }
}

private fun supersetGroupName(index: Int): String {
    return if (index in 0 until 26) {
        ('A'.code + index).toChar().toString()
    } else {
        (index + 1).toString()
    }
}

internal fun List<StrengthPlanEntry>.normalizeSupersetGroups(): List<StrengthPlanEntry> {
    val validGroupIds = mapNotNull { it.supersetGroupId }
        .groupingBy { it }
        .eachCount()
        .filterValues { it >= 2 }
        .keys
    return map { entry ->
        if (entry.supersetGroupId != null && entry.supersetGroupId !in validGroupIds) {
            entry.copy(supersetGroupId = null)
        } else {
            entry
        }
    }
}

internal fun StrengthPlanEntry.withPropagatedRecordChange(
    changedIndex: Int,
    changedRecord: StrengthSetRecord,
): StrengthPlanEntry {
    val nextRecords = records.mapIndexed { index, old ->
        when {
            index < changedIndex -> old
            index == changedIndex -> changedRecord
            else -> old.copy(
                weightKg = changedRecord.weightKg,
                reps = changedRecord.reps,
                restSeconds = changedRecord.restSeconds,
                leftWeightKg = changedRecord.weightKg,
                leftReps = changedRecord.reps,
                rightWeightKg = changedRecord.weightKg,
                rightReps = changedRecord.reps
            )
        }
    }
    return withRecords(nextRecords)
}

internal fun StrengthPlanEntry.isUnilateral(): Boolean {
    val text = listOf(exercise.nameKo, exercise.nameEn, equipment, variation, title)
        .joinToString(" ")
        .lowercase(Locale.KOREAN)
        .replace(" ", "")
    return UNILATERAL_VARIATION_KEYWORDS
        .any { keyword -> text.contains(keyword) }
}

internal fun StrengthPlanEntry.weightInputUnitLabel(): String {
    return if (equipment.trim() == "맨몸") "체중" else "kg"
}

internal fun StrengthSetRecord.unilateralWeightSummary(): String {
    return "${weightKg.ifBlank { "-" }}kg"
}

internal fun StrengthSetRecord.unilateralRepsSummary(): String {
    return "각 ${reps.ifBlank { "-" }}회"
}

internal fun StrengthPlanEntry.copyForWorkout(): StrengthPlanEntry {
    return copy(records = records.map { it.copy(completed = false) })
}

internal fun List<CompletedStrengthWorkout>.latestMatchingStrengthEntry(
    exercise: StrengthExercise,
    equipment: String,
    variation: String,
): StrengthPlanEntry? {
    return sortedByDescending { it.startedAtMillis }
        .asSequence()
        .flatMap { it.entries.asSequence() }
        .firstOrNull { entry ->
            entry.exercise.id == exercise.id &&
                entry.equipment == equipment &&
                entry.variation == variation
        }
}

internal fun StrengthPlanEntry.copyAsNewPlanEntry(
    id: Int,
    exercise: StrengthExercise,
    equipment: String,
    variation: String,
): StrengthPlanEntry {
    return copy(
        id = id,
        exercise = exercise,
        equipment = equipment,
        variation = variation,
        supersetGroupId = null,
        records = records.mapIndexed { index, record ->
            record.copy(
                id = index + 1,
                durationSeconds = "",
                completed = false
            )
        }
    )
}

internal fun List<StrengthPlanEntry>.allSetsCompleted(): Boolean {
    return isNotEmpty() && all { entry -> entry.records.isNotEmpty() && entry.records.all { it.completed } }
}

internal fun nextIncompleteSet(
    entries: List<StrengthPlanEntry>,
    fromExerciseIndex: Int,
    fromSetIndex: Int,
): Pair<Int, Int>? {
    for (exerciseIndex in fromExerciseIndex until entries.size) {
        val entry = entries[exerciseIndex]
        val setStart = if (exerciseIndex == fromExerciseIndex) fromSetIndex + 1 else 0
        for (setIndex in setStart until entry.records.size) {
            if (!entry.records[setIndex].completed) return exerciseIndex to setIndex
        }
    }
    for (exerciseIndex in 0 until fromExerciseIndex.coerceAtMost(entries.size)) {
        val entry = entries[exerciseIndex]
        for (setIndex in entry.records.indices) {
            if (!entry.records[setIndex].completed) return exerciseIndex to setIndex
        }
    }
    return null
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

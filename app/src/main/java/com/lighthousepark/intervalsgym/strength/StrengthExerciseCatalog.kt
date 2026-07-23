package com.lighthousepark.intervalsgym.strength

import java.util.Locale

internal val CUSTOM_STRENGTH_EQUIPMENT_OPTIONS = listOf("맨몸", "바벨", "덤벨", "케틀벨", "스미스", "트랩바", "케이블", "직접 입력")

internal val UNILATERAL_MODE_OPTIONS = listOf("양쪽", "한쪽")
internal val UNILATERAL_VARIATION_KEYWORDS = listOf("싱글레그", "싱글암", "싱글", "원암", "한팔", "한쪽", "singleleg", "singlearm", "single")

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

internal fun StrengthExercise.forcedUnilateralModeForVariation(variation: String): String? {
    if (single) return "한쪽"
    val normalizedVariation = variation.normalizedSearchText()
    if (normalizedVariation.isBlank()) return null
    return variationUnilateralModes.entries.firstOrNull { (variationOption, _) ->
        normalizedVariation == variationOption.normalizedSearchText()
    }?.value?.takeIf { it in UNILATERAL_MODE_OPTIONS }
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
    return base to (exercise.forcedUnilateralModeForVariation(base) ?: unilateral)
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

internal val strengthExerciseCatalog = listOf(
    StrengthExercise("deadlift", "데드리프트", "Deadlift", "하체/후면사슬", listOf("바벨", "덤벨", "케틀벨", "스미스", "트랩바"), listOf("기본", "루마니안", "스모", "스티프레그", "싱글레그", "블록 풀")),
    StrengthExercise("bench_press", "벤치프레스", "Bench Press", "가슴", listOf("바벨", "덤벨", "스미스", "머신"), listOf("플랫", "인클라인", "디클라인", "클로즈그립", "와이드그립", "템포")),
    StrengthExercise("chest_press", "체스트 프레스", "Chest Press", "가슴", listOf("머신", "케이블"), listOf("기본", "인클라인", "디클라인", "시티드", "플레이트 로드")),
    StrengthExercise(
        "squat",
        "스쿼트",
        "Squat",
        "하체",
        listOf("바벨", "덤벨", "케틀벨", "스미스", "머신"),
        listOf("백 스쿼트", "프론트 스쿼트", "고블릿", "불가리안 스플릿", "박스"),
        variationUnilateralModes = mapOf("불가리안 스플릿" to "한쪽")
    ),
    StrengthExercise("hack_squat", "핵스쿼트", "Hack Squat", "하체", listOf("머신"), listOf("기본", "리버스", "싱글레그")),
    StrengthExercise("overhead_press", "오버헤드 프레스", "Overhead Press", "어깨", listOf("바벨", "덤벨", "케틀벨", "스미스", "머신"), listOf("스탠딩", "시티드", "푸시 프레스", "아놀드", "싱글암")),
    StrengthExercise("overhead_extension", "오버헤드 익스텐션", "Overhead Extension", "어깨", listOf("덤벨", "케이블", "밴드", "EZ바", "바벨", "케틀벨"), listOf("기본", "스탠딩", "시티드", "인클라인 벤치"), aliases = listOf("오버 헤드 익스텐션", "Over Head Extension")),
    StrengthExercise("row", "로우", "Row", "등", listOf("바벨", "덤벨", "케이블", "머신", "랜드마인"), listOf("벤트오버", "원암", "시티드", "펜들레이", "체스트 서포티드", "티바")),
    StrengthExercise("pull_up", "풀업", "Pull-up", "등", listOf("맨몸", "어시스트 머신", "밴드", "중량벨트"), listOf("풀업", "친업", "뉴트럴그립", "와이드그립", "클로즈그립")),
    StrengthExercise("lat_pulldown", "랫풀다운", "Lat Pulldown", "등", listOf("케이블", "머신"), listOf("와이드그립", "언더그립", "뉴트럴그립", "싱글암", "스트레이트암")),
    StrengthExercise("lunge", "런지", "Lunge", "하체", listOf("맨몸", "덤벨", "바벨", "스미스", "케틀벨", "케이블"), listOf("워킹", "리버스", "포워드", "사이드", "불가리안", "회전"), single = true),
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
    StrengthExercise(
        "plank",
        "플랭크",
        "Plank",
        "코어",
        listOf("맨몸", "중량", "밴드"),
        listOf("기본", "사이드", "코펜하겐", "RKC", "리버스", "숄더탭"),
        variationUnilateralModes = mapOf("사이드" to "한쪽", "코펜하겐" to "한쪽")
    ),
    StrengthExercise("crunch", "크런치", "Crunch", "코어", listOf("맨몸", "케이블", "머신", "짐볼"), listOf("기본", "케이블", "리버스", "바이시클", "데드버그"), aliases = listOf("데드 버그", "데드버그 크런치", "Dead Bug", "Deadbug", "Dead Bug Crunch")),
    StrengthExercise("woodchop", "우드찹", "Woodchop", "코어", listOf("케이블", "밴드", "메디신볼"), listOf("하이투로우", "로우투하이", "수평", "하프니링")),
)

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

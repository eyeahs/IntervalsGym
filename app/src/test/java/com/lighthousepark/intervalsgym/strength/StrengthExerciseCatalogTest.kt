package com.lighthousepark.intervalsgym.strength

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthExerciseCatalogTest {
    @Test
    fun exerciseSearch_ignoresWhitespaceAndUsesAliases() {
        val legCurl = strengthExerciseCatalog.first { it.id == "leg_curl" }
        val shoulderRaise = strengthExerciseCatalog.first { it.id == "shoulder_raise" }

        assertTrue(legCurl.matchesSearch("레그컬"))
        assertTrue(shoulderRaise.matchesSearch("레터럴레이즈"))
    }

    @Test
    fun chestFlySearch_prefillsPecDeckEquipment() {
        val fly = strengthExerciseCatalog.first { it.id == "chest_fly" }
        val options = fly.equipmentOptionsWithBodyweight()

        assertEquals("팩 덱 머신", fly.inferEquipmentFromSearch("펙덱플라이", options))
    }

    @Test
    fun overheadExtension_isSearchableAsShoulderExercise() {
        val overheadExtension = strengthExerciseCatalog.first { it.id == "overhead_extension" }

        assertEquals("어깨", overheadExtension.group)
        assertTrue(overheadExtension.matchesSearch("오버 헤드 익스텐션"))
        assertTrue(overheadExtension.matchesSearch("Over Head Extension"))
    }

    @Test
    fun deadbugSearch_selectsDeadbugCrunchVariation() {
        val crunch = strengthExerciseCatalog.first { it.id == "crunch" }

        assertTrue(crunch.matchesSearch("데드버그"))
        assertTrue(crunch.matchesSearch("deadbug"))
        assertEquals("데드버그", crunch.inferVariationFromSearch("데드버그"))
        assertEquals("데드버그", crunch.inferVariationFromSearch("dead bug"))
        assertEquals("데드버그 크런치", crunch.searchResultTitle("데드버그"))
    }

    @Test
    fun variationAndUnilateral_areSplitAndCombinedSeparately() {
        val legCurl = strengthExerciseCatalog.first { it.id == "leg_curl" }

        assertEquals("라잉" to "한쪽", splitVariationAndUnilateral(legCurl, "싱글레그 라잉"))
        assertEquals("라잉" to "한쪽", splitVariationAndUnilateral(legCurl, "한쪽 라잉"))
        assertEquals("한쪽 라잉", combineVariationAndUnilateral("라잉", "한쪽"))
    }

    @Test
    fun variationUnilateralMode_usesExerciseCatalogData() {
        val squat = strengthExerciseCatalog.first { it.id == "squat" }

        assertEquals("한쪽", squat.forcedUnilateralModeForVariation("불가리안 스플릿"))
        assertEquals("불가리안 스플릿" to "한쪽", splitVariationAndUnilateral(squat, "불가리안 스플릿"))
        assertEquals("불가리안 스플릿" to "한쪽", splitVariationAndUnilateral(squat, "양쪽 불가리안 스플릿"))
        assertEquals(null, squat.forcedUnilateralModeForVariation("백 스쿼트"))
    }

    @Test
    fun unilateralSearch_usesSingleOneSideMode() {
        val legCurl = strengthExerciseCatalog.first { it.id == "leg_curl" }
        val latPulldown = strengthExerciseCatalog.first { it.id == "lat_pulldown" }

        assertEquals(listOf("양쪽", "한쪽"), UNILATERAL_MODE_OPTIONS)
        assertEquals("한쪽", legCurl.inferUnilateralFromSearch("싱글레그 라잉 레그 컬"))
        assertEquals("한쪽", latPulldown.inferUnilateralFromSearch("싱글암 랫풀다운"))
    }
}

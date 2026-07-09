package com.lighthousepark.intervalsgym

import java.nio.file.Files
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthDomainArchitectureGuardTest {
    private val mainSourceRoot = ArchitectureGuardProject.mainSourceRoot
    private val testSourceRoot = ArchitectureGuardProject.testSourceRoot

    @Test
    fun strengthSessionProgressionRulesStayOutOfGenericStrengthDomain() {
        val strengthDomain = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthDomain.kt")
        )
        val completionTiming = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthSessionCompletionTiming.kt")
        )
        val setNavigation = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthSessionSetNavigation.kt")
        )
        val eventSync = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthSessionEventSync.kt")
        )
        val restProgression = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthRestProgression.kt")
        )
        val setCompletion = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthSetCompletionProgression.kt")
        )
        val strengthDomainTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthDomainTest.kt")
        )
        val completionTimingTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthSessionCompletionTimingTest.kt")
        )
        val setNavigationTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthSessionSetNavigationTest.kt")
        )
        val eventSyncTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthSessionEventSyncTest.kt")
        )
        val restProgressionTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthRestProgressionTest.kt")
        )
        val setCompletionTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthSetCompletionProgressionTest.kt")
        )
        val movedDefinitionsByOwner = mapOf(
            completionTiming to listOf(
                "internal fun List<StrengthRoutineEntry>.allSetsCompleted",
                "internal fun completedStrengthSessionFinishedAtMillis",
                "internal fun completedStrengthSessionAutoLocalSaveAtMillis",
                "internal fun shouldAutoLocalSaveCompletedStrengthSession"
            ),
            setNavigation to listOf(
                "internal fun List<StrengthRoutineEntry>.exerciseChangeFocusIndex",
                "internal fun nextIncompleteSet",
                "internal fun isImmediateSupersetTransition",
                "internal fun shouldAdvanceCurrentExerciseAfterCompletedExercise"
            ),
            eventSync to listOf(
                "internal fun List<StrengthSetCompletionEvent>.withCurrentStrengthSetDetails",
                "internal fun List<StrengthRestEvent>.withCurrentStrengthRestDetails"
            ),
            restProgression to listOf(
                "internal data class StrengthRestEventCloseResult",
                "internal data class StrengthRestTimerStartResult",
                "internal data class StrengthRestTimerSecondsResult",
                "internal fun closeActiveStrengthRestEvent",
                "internal fun startStrengthRestTimer",
                "internal fun updateStrengthRestTimerSeconds"
            ),
            setCompletion to listOf(
                "internal enum class StrengthSetCompletionFollowUp",
                "internal data class StrengthSetCompletionResult",
                "internal fun completeStrengthSet"
            )
        )

        assertFalse(
            "Broad progression bucket should stay split into focused rule files",
            Files.exists(mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthSessionProgression.kt"))
        )
        assertFalse(
            "Broad progression test bucket should stay split into focused test files",
            Files.exists(testSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthSessionProgressionTest.kt"))
        )
        movedDefinitionsByOwner.forEach { (owner, definitions) ->
            definitions.forEach { definition ->
                assertFalse("$definition belongs in a focused strength progression file", strengthDomain.contains(definition))
                assertTrue("$definition missing from its focused strength progression file", owner.contains(definition))
            }
        }
        assertFalse("Auto-save timing belongs with session progression rules", strengthDomain.contains("sessionAutoLocalSaveAtMillis"))

        val movedTestsByOwner = mapOf(
            setNavigationTest to listOf(
                "nextIncompleteSet_prefersNextSupersetExerciseInSameSetRound",
                "exerciseChangeFocusIndex_prefersPendingAddedEntryOverStaleCurrentIndex"
            ),
            setCompletionTest to listOf("completeStrengthSet_createsSetEventAndRestBeforeNextNonSupersetSet"),
            restProgressionTest to listOf("closeActiveStrengthRestEvent_closesOnlyActiveOpenRest"),
            eventSyncTest to listOf("strengthSetAndRestEvents_followCompletedSetEdits"),
            completionTimingTest to listOf("completedStrengthSessionAutoLocalSaveAtMillis_usesLastCompletedSetWhenAllSetsAreDone")
        )

        movedTestsByOwner.forEach { (owner, testNames) ->
            testNames.forEach { testName ->
                assertFalse("$testName belongs in a focused strength progression test file", strengthDomainTest.contains(testName))
                assertTrue("$testName missing from its focused strength progression test file", owner.contains(testName))
            }
        }
        assertFalse(
            "Progression timing constants belong in StrengthSessionCompletionTimingTest.kt",
            strengthDomainTest.contains("SESSION_AUTO_LOCAL_SAVE_DELAY_MILLIS")
        )
    }

    @Test
    fun strengthRoutineRulesStayOutOfGenericStrengthDomain() {
        val strengthDomain = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthDomain.kt")
        )
        val defaults = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthRoutineDefaults.kt")
        )
        val records = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthRoutineRecords.kt")
        )
        val supersets = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthSupersetGroups.kt")
        )
        val presentation = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthExercisePresentation.kt")
        )
        val history = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthExerciseHistory.kt")
        )
        val movedDefinitionsByOwner = mapOf(
            defaults to listOf(
                "internal fun defaultStrengthRoutines",
                "internal fun nextStrengthWorkoutRoutineId",
                "internal fun defaultStrengthRoutineEntry",
                "internal fun defaultStrengthWeightForEquipment",
                "internal fun defaultStrengthSetRecord"
            ),
            records to listOf(
                "internal fun StrengthRoutineEntry.withRecords",
                "internal fun StrengthRoutineEntry.withPropagatedRecordChange",
                "internal fun StrengthRoutineEntry.copyForWorkout",
                "internal fun StrengthRoutineEntry.copyAsNewRoutineEntry"
            ),
            supersets to listOf(
                "internal fun List<StrengthRoutineEntry>.supersetGroupLabels",
                "internal fun <T> List<T>.moveItem",
                "internal fun List<StrengthRoutineEntry>.groupSelectedEntriesAsSuperset",
                "internal fun List<StrengthRoutineEntry>.normalizeSupersetGroups"
            ),
            presentation to listOf(
                "internal fun StrengthRoutineEntry.isUnilateral",
                "internal fun StrengthRoutineEntry.weightInputUnitLabel",
                "internal fun StrengthSetRecord.unilateralWeightSummary",
                "internal fun StrengthSetRecord.unilateralRepsSummary",
                "internal fun formatStrengthExerciseTitle"
            ),
            history to listOf(
                "internal fun List<CompletedStrengthSession>.latestMatchingStrengthEntry",
                "internal fun List<CompletedStrengthSession>.recentMatchingStrengthExerciseHistory"
            )
        )

        movedDefinitionsByOwner.forEach { (owner, definitions) ->
            definitions.forEach { definition ->
                assertFalse("$definition belongs in a focused strength rule file", strengthDomain.contains(definition))
                assertTrue("$definition missing from its focused strength rule file", owner.contains(definition))
            }
        }
        assertTrue(strengthDomain.contains("internal data class StrengthWorkoutRoutine"))
        assertTrue(strengthDomain.contains("internal data class StrengthRoutineEntry"))
    }

    @Test
    fun strengthRoutineRuleTestsStayOutOfGenericStrengthDomainTest() {
        val strengthDomainTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthDomainTest.kt")
        )
        val routineRecordsTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthRoutineRecordsTest.kt")
        )
        val routineDefaultsTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthRoutineDefaultsTest.kt")
        )
        val exerciseHistoryTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthExerciseHistoryTest.kt")
        )
        val exercisePresentationTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthExercisePresentationTest.kt")
        )
        val supersetGroupsTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthSupersetGroupsTest.kt")
        )
        val movedTestsByOwner = mapOf(
            routineRecordsTest to listOf("setRecordChange_propagatesOnlyToFollowingSets"),
            routineDefaultsTest to listOf(
                "defaultStrengthEntry_usesTenKgExceptBodyweight",
                "nextStrengthWorkoutRoutineId_doesNotReuseDeletedRoutineIdsStillReferencedByHistory",
                "nextStrengthWorkoutRoutineId_reservesScheduledAndActiveRoutineIds"
            ),
            exerciseHistoryTest to listOf("recentMatchingStrengthExerciseHistory_filtersByExerciseEquipmentAndVariation"),
            exercisePresentationTest to listOf("strengthTitleFormatting_keepsExerciseSpecificOrdering"),
            supersetGroupsTest to listOf(
                "groupSelectedEntriesAsSuperset_movesSelectedEntriesBelowTopSelectedEntry",
                "groupSelectedEntriesAsSuperset_keepsAlreadyAdjacentEntriesInPlace",
                "normalizeSupersetGroups_clearsGroupsWithSingleRemainingEntry"
            )
        )

        movedTestsByOwner.forEach { (owner, testNames) ->
            testNames.forEach { testName ->
                assertFalse("$testName belongs in its focused strength test file", strengthDomainTest.contains(testName))
                assertTrue("$testName missing from its focused strength test file", owner.contains(testName))
            }
        }
        assertTrue(strengthDomainTest.contains("activeSessionToWorkoutRoutine_keepsRoutineIdentityAndEntries"))
    }

    @Test
    fun strengthExerciseCatalogAndSearchStayOutOfGenericStrengthDomain() {
        val strengthDomain = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthDomain.kt")
        )
        val exerciseCatalog = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthExerciseCatalog.kt")
        )
        val strengthDomainTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthDomainTest.kt")
        )
        val exerciseCatalogTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/StrengthExerciseCatalogTest.kt")
        )
        val movedDefinitions = listOf(
            "internal val CUSTOM_STRENGTH_EQUIPMENT_OPTIONS",
            "internal val UNILATERAL_MODE_OPTIONS",
            "internal val UNILATERAL_VARIATION_KEYWORDS",
            "internal fun StrengthExercise.equipmentOptionsWithBodyweight",
            "internal fun StrengthExercise.baseVariationOptions",
            "internal fun StrengthExercise.matchesSearch",
            "internal fun StrengthExercise.inferEquipmentFromSearch",
            "internal fun StrengthExercise.inferVariationFromSearch",
            "internal fun StrengthExercise.searchResultTitle",
            "internal fun StrengthExercise.inferUnilateralFromSearch",
            "internal fun StrengthExercise.forcedUnilateralModeForVariation",
            "internal fun String.normalizedSearchText",
            "internal fun splitVariationAndUnilateral",
            "internal fun combineVariationAndUnilateral",
            "internal val strengthExerciseCatalog",
            "internal fun customStrengthExercise"
        )

        movedDefinitions.forEach { definition ->
            assertFalse("$definition belongs in StrengthExerciseCatalog.kt", strengthDomain.contains(definition))
            assertTrue("$definition missing from StrengthExerciseCatalog.kt", exerciseCatalog.contains(definition))
        }
        assertTrue("StrengthExercise model should remain in StrengthDomain.kt", strengthDomain.contains("internal data class StrengthExercise"))
        listOf(
            "exerciseSearch_ignoresWhitespaceAndUsesAliases",
            "chestFlySearch_prefillsPecDeckEquipment",
            "deadbugSearch_selectsDeadbugCrunchVariation",
            "unilateralSearch_usesSingleOneSideMode"
        ).forEach { testName ->
            assertFalse("$testName belongs in StrengthExerciseCatalogTest.kt", strengthDomainTest.contains(testName))
            assertTrue("$testName missing from StrengthExerciseCatalogTest.kt", exerciseCatalogTest.contains(testName))
        }
    }
}

package com.lighthousepark.intervalsgym

import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DataStorageTestArchitectureGuardTest {
    private val testSourceRoot = ArchitectureGuardProject.testSourceRoot

    @Test
    fun sharedWorkoutStorageFixturesStayInFixtureFile() {
        val fixtureText = dataTestText("WorkoutStorageTestFixtures.kt")
        val misplacedFixtures = dataTestFiles()
            .filterNot { path -> path.fileName.toString() == "WorkoutStorageTestFixtures.kt" }
            .flatMap { path ->
                val text = Files.readString(path)
                listOf(
                    "class MemorySharedPreferences",
                    "class CacheMemorySharedPreferences",
                    "class RecordingTrainingCalendarRemoteDataSource",
                    "fun completedStrengthSessionForStorage"
                )
                    .filter { fixture -> text.contains(fixture) }
                    .map { fixture -> "${path.relativeToProject()}: $fixture" }
            }

        assertEquals(emptyList<String>(), misplacedFixtures)
        assertTrue(fixtureText.contains("class MemorySharedPreferences"))
        assertTrue(fixtureText.contains("class RecordingTrainingCalendarRemoteDataSource"))
        assertTrue(fixtureText.contains("fun completedStrengthSessionForStorage"))
    }

    @Test
    fun dataTestsDoNotUseProjectWildcardImports() {
        val violations = dataTestFiles()
            .flatMap { path ->
                val text = Files.readString(path)
                Regex("""import com\.lighthousepark\.intervalsgym\..*\.\*""")
                    .findAll(text)
                    .map { match -> "${path.relativeToProject()}: ${match.value}" }
                    .toList()
            }

        assertEquals(emptyList<String>(), violations)
    }

    @Test
    fun strengthRoutineStorageTestsStayFocused() {
        assertDataTestNamesOwnedBy(
            fileName = "StrengthRoutineStorageTest.kt",
            testNames = listOf(
                "loadStrengthRoutines_fallsBackToDefaultsWhenStorageIsEmpty",
                "saveStrengthRoutineLibrary_roundTripsStoredRoutines"
            )
        )
        assertDataTestNamesOwnedBy(
            fileName = "StrengthRoutineDescriptionStorageTest.kt",
            testNames = listOf(
                "visibleRoutineDescription_hidesInternalMarkers",
                "intervalsRoutineDescription_containsOnlyHumanReadablePlan",
                "strengthRoutineDescription_returnsNullForMalformedEmbeddedRoutineJson",
                "strengthRoutineForDisplay_usesPairedRoutineWhenResultIsMerged",
                "workoutDetailDescription_showsRawWeightResultDescriptionWhenRoutineIsUnmatched"
            )
        )
        assertDataTestNamesOwnedBy(
            fileName = "StrengthRoutineJsonTest.kt",
            testNames = listOf(
                "strengthRoutineDescription_roundTripsEmbeddedRoutineJson",
                "strengthRoutineEntryNote_roundTripsThroughStorageAndIntervalsDescription"
            )
        )
    }

    @Test
    fun sessionSyncUseCaseTestsStayFocused() {
        assertDataTestNamesOwnedBy(
            fileName = "StrengthSessionSyncUseCaseTest.kt",
            testNames = listOf(
                "liveResultUpdatesCompletedSetAndRestDetails",
                "finishedResultClosesActiveRest",
                "uploadsAndReplacesLocalStrengthSession"
            )
        )

        assertDataTestNamesOwnedBy(
            fileName = "RunningSessionSyncUseCaseTest.kt",
            testNames = listOf(
                "savesLocalRunningSession",
                "uploadsAndReplacesLocalRunningSession",
                "deletesLocalRunningSession"
            )
        )
    }

    @Test
    fun activeStrengthSessionStorageTestsStayFocused() {
        assertDataTestNamesOwnedBy(
            fileName = "ActiveStrengthSessionStorageTest.kt",
            testNames = listOf(
                "roundTripsCurrentSetAndRestState",
                "expiredRestRestoresPendingSetAndFinalizesRestEvent",
                "withLatestCompletedSessionUpdatesOnlyBeforeSessionStarts"
            )
        )
    }

    @Test
    fun trainingCalendarDataUseCaseTestsStayFocused() {
        assertDataTestNamesOwnedBy(
            fileName = "TrainingCalendarDataUseCaseTest.kt",
            testNames = listOf(
                "trainingCalendarDataUseCase_loadsLocalSnapshotSeparatelyFromRemoteData",
                "trainingCalendarDataUseCase_usesCachedRemoteWeekUntilForced",
                "trainingCalendarDataUseCase_fetchesAndCachesRemoteWeek"
            )
        )
    }

    @Test
    fun trainingLocalResultMergeTestsStayFocused() {
        assertDataTestNamesOwnedBy(
            fileName = "TrainingLocalResultMergeTest.kt",
            testNames = listOf(
                "withLocalRunningResults_addsUnmatchedLocalWorkoutInsideRange",
                "withLocalRunningResults_skipsWorkoutMatchedByRemoteResultTime",
                "withLocalRunningResults_skipsWorkoutAlreadyRepresentedByLocalResult",
                "withLocalRunningResults_attachesMergedBlocksToGarminActivity",
                "withLocalRunningResults_doesNotMatchOnlyBecauseDurationIsEqual",
                "withLocalStrengthResults_addsUnmatchedLocalWorkoutInsideRange",
                "withLocalStrengthResults_skipsWorkoutMatchedByRemoteExternalId"
            )
        )
    }

    @Test
    fun strengthSessionEventJsonTestsStayFocused() {
        assertDataTestNamesOwnedBy(
            fileName = "StrengthSessionEventJsonTest.kt",
            testNames = listOf("finalizeRestEvents_closesOnlyActiveOpenRest")
        )
    }

    @Test
    fun calendarRoutineSyncUseCaseTestsStayFocused() {
        assertDataTestNamesOwnedBy(
            fileName = "CalendarRoutineSyncUseCaseTest.kt",
            testNames = listOf(
                "calendarRoutineSyncUseCase_uploadSavedStrengthRoutineMarksLocalRoutineUploaded",
                "calendarRoutineSyncUseCase_syncMovedLocalStrengthRoutineUploadsAndDeletesRemoteSource"
            )
        )
    }

    @Test
    fun scheduledStrengthRoutineStorageTestsStayFocused() {
        assertDataTestNamesOwnedBy(
            fileName = "ScheduledStrengthRoutineStorageTest.kt",
            testNames = listOf(
                "moveScheduledStrengthRoutine_updatesStoredDateAndIds",
                "scheduledStrengthRoutine_roundTripsTimeAndDisplaysTimeLabel",
                "upsertScheduledStrengthRoutine_replacesSameExternalIdAndPersistsLatestRoutine",
                "removeScheduledStrengthRoutine_matchesLocalIdRemoteIdOrExternalId",
                "loadScheduledStrengthRoutines_derivesMissingLegacyExternalId",
                "loadStrengthRoutines_readsOnlySavedRoutineKeyNotScheduledCalendarRoutines",
                "scheduledStrengthRoutineOperations_doNotMutateSavedStrengthRoutines",
                "withLocalStrengthRoutines_preservesExistingMatchedRemoteRoutine",
                "withLocalStrengthRoutines_prefersCurrentLocalRoutineMatchedByUploadedRoutineId"
            )
        )
    }

    @Test
    fun sessionHistoryStorageTestsStayFocused() {
        assertDataTestNamesOwnedBy(
            fileName = "StrengthSessionHistoryStorageTest.kt",
            testNames = listOf(
                "appendStrengthSessionHistory_deduplicatesExistingSessionId",
                "buildCompletedStrengthSession_keepsStableIdAcrossResultUpdates",
                "appendStrengthSessionHistory_replacesSameStartedRoutineEvenWhenLegacyIdDiffers"
            )
        )
        assertDataTestNamesOwnedBy(
            fileName = "RunningSessionHistoryStorageTest.kt",
            testNames = listOf(
                "appendRunningSessionHistory_deduplicatesExistingSessionId",
                "runningSessionHistory_roundTripsHeartRateAndMergeMetadata",
                "runningSessionHistory_loadsLegacyShapeWithoutMergeFields"
            )
        )
    }

    @Test
    fun runningActivityMergeUseCaseTestsStayFocused() {
        assertDataTestNamesOwnedBy(
            fileName = "RunningActivityMergeUseCaseTest.kt",
            testNames = listOf(
                "findCandidates_prefersMatchingGarminHeartRateAndFindsAppDuplicate",
                "merge_updatesGarminThenDeletesAppDuplicateAndPersistsLink",
                "merge_doesNotDeleteWhenAppDuplicateIsMissing",
                "remoteActivityJson_readsGarminSourceAndElapsedDuration",
                "remoteStreamsJson_pairsTimeAndHeartRateWhileSkippingNulls"
            )
        )
    }

    @Test
    fun runningRoutineStorageTestsStayFocused() {
        assertDataTestNamesOwnedBy(
            fileName = "RunningRoutineStorageTest.kt",
            testNames = listOf(
                "savedRunningWorkoutRoutine_roundTripsToExecutableTrainingItem",
                "savedRunningWorkoutRoutine_upsertReplacesSameIdAndKeepsLatestFirst",
                "deleteSavedRunningWorkoutRoutine_removesOnlyTargetRoutine"
            )
        )
    }

    private fun assertDataTestNamesOwnedBy(fileName: String, testNames: List<String>) {
        val focusedText = dataTestText(fileName)
        val missingTests = testNames
            .filterNot { testName -> focusedText.contains(testName) }
        val misplacedTests = dataTestFiles()
            .filterNot { path -> path.fileName.toString() == fileName }
            .flatMap { path ->
                val text = Files.readString(path)
                testNames
                    .filter { testName -> text.contains(testName) }
                    .map { testName -> "${path.relativeToProject()}: $testName" }
            }

        assertEquals(emptyList<String>(), missingTests)
        assertEquals(emptyList<String>(), misplacedTests)
    }

    private fun dataTestText(fileName: String): String {
        return Files.readString(dataTest(fileName))
    }

    private fun dataTest(fileName: String) = testSourceRoot.resolve(
        "com/lighthousepark/intervalsgym/data/$fileName"
    )

    private fun dataTestFiles() = kotlinFiles(
        testSourceRoot.resolve("com/lighthousepark/intervalsgym/data")
    )
}

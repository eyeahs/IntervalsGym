package com.lighthousepark.intervalsgym

import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DataLayerBoundaryArchitectureGuardTest {
    private val mainSourceRoot = ArchitectureGuardProject.mainSourceRoot
    private val testSourceRoot = ArchitectureGuardProject.testSourceRoot

    @Test
    fun workoutStorageBucketsAreNotRecreated() {
        assertFalse(
            "Use focused storage files instead of recreating a catch-all WorkoutStorage.kt.",
            Files.exists(mainSourceRoot.resolve("com/lighthousepark/intervalsgym/data/WorkoutStorage.kt"))
        )
        assertFalse(
            "Use focused storage tests instead of recreating a catch-all WorkoutStorageTest.kt.",
            Files.exists(dataTest("WorkoutStorageTest.kt"))
        )
    }

    @Test
    fun completedHistoryStorageStaysSplitByWorkoutType() {
        val oldHistoryBucket = mainSourceRoot.resolve("com/lighthousepark/intervalsgym/data/SessionHistoryStorage.kt")
        val oldHistoryTestBucket = testSourceRoot.resolve("com/lighthousepark/intervalsgym/data/SessionHistoryStorageTest.kt")
        val strengthHistory = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/data/StrengthSessionHistoryStorage.kt")
        )
        val runningHistory = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/data/RunningSessionHistoryStorage.kt")
        )
        val savedRunningRoutine = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/data/SavedRunningRoutineStorage.kt")
        )

        assertFalse(
            "Completed history and saved routine persistence should stay split by owner.",
            Files.exists(oldHistoryBucket)
        )
        assertFalse(
            "Completed history tests should stay split by workout type.",
            Files.exists(oldHistoryTestBucket)
        )
        listOf(
            "internal fun buildCompletedStrengthSession",
            "internal fun strengthSessionResultId",
            "internal fun appendStrengthSessionHistory",
            "internal fun replaceStrengthSessionHistory",
            "internal fun deleteStrengthSessionHistory",
            "internal fun loadCompletedStrengthSessionHistory"
        ).forEach { definition ->
            assertTrue("$definition missing from StrengthSessionHistoryStorage.kt", strengthHistory.contains(definition))
            assertFalse("$definition belongs in strength history storage only", runningHistory.contains(definition))
            assertFalse("$definition belongs in strength history storage only", savedRunningRoutine.contains(definition))
        }
        listOf(
            "internal fun appendRunningSessionHistory",
            "internal fun replaceRunningSessionHistory",
            "internal fun deleteRunningSessionHistory",
            "internal fun loadCompletedRunningSessionHistory"
        ).forEach { definition ->
            assertTrue("$definition missing from RunningSessionHistoryStorage.kt", runningHistory.contains(definition))
            assertFalse("$definition belongs in running history storage only", strengthHistory.contains(definition))
            assertFalse("$definition belongs in running history storage only", savedRunningRoutine.contains(definition))
        }
        listOf(
            "internal fun upsertSavedRunningWorkoutRoutine",
            "internal fun loadSavedRunningWorkoutRoutines",
            "internal fun deleteSavedRunningWorkoutRoutine"
        ).forEach { definition ->
            assertTrue("$definition missing from SavedRunningRoutineStorage.kt", savedRunningRoutine.contains(definition))
            assertFalse("$definition belongs in saved running routine storage only", strengthHistory.contains(definition))
            assertFalse("$definition belongs in saved running routine storage only", runningHistory.contains(definition))
        }
    }

    @Test
    fun strengthRoutineStorageStaysSplitByConcern() {
        val libraryStorage = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/data/StrengthRoutineStorage.kt")
        )
        val latestSessions = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/data/StrengthRoutineLatestSessions.kt")
        )
        val descriptions = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/data/StrengthRoutineDescriptions.kt")
        )
        val json = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/data/StrengthRoutineJson.kt")
        )

        listOf(
            "internal fun loadStrengthRoutines",
            "internal fun saveStrengthRoutineLibrary"
        ).forEach { definition ->
            assertTrue("$definition missing from StrengthRoutineStorage.kt", libraryStorage.contains(definition))
            assertFalse("$definition belongs in strength routine library storage only", latestSessions.contains(definition))
            assertFalse("$definition belongs in strength routine library storage only", descriptions.contains(definition))
            assertFalse("$definition belongs in strength routine library storage only", json.contains(definition))
        }
        assertTrue(latestSessions.contains("withLatestCompletedSession("))
        assertFalse("Latest completed session rules belong in StrengthRoutineLatestSessions.kt", libraryStorage.contains("withLatestCompletedSession("))
        listOf(
            "internal fun StrengthWorkoutRoutine.toIntervalsRoutineDescription",
            "internal fun String?.visibleRoutineDescription",
            "internal fun TrainingItem.strengthRoutineForDisplay",
            "internal fun String?.toIntervalsGymStrengthRoutine",
            "internal fun String?.toIntervalsGymStrengthRoutineId"
        ).forEach { definition ->
            assertTrue("$definition missing from StrengthRoutineDescriptions.kt", descriptions.contains(definition))
            assertFalse("$definition belongs in strength routine descriptions only", libraryStorage.contains(definition))
            assertFalse("$definition belongs in strength routine descriptions only", json.contains(definition))
        }
        listOf(
            "internal fun List<StrengthWorkoutRoutine>.toJsonString",
            "internal fun String?.toStrengthWorkoutRoutines"
        ).forEach { definition ->
            assertTrue("$definition missing from StrengthRoutineJson.kt", json.contains(definition))
            assertFalse("$definition belongs in strength routine JSON only", libraryStorage.contains(definition))
            assertFalse("$definition belongs in strength routine JSON only", descriptions.contains(definition))
        }
    }

    @Test
    fun uiScreensDoNotWriteCompletedSessionHistoryDirectly() {
        val uiRoots = listOf(
            "com/lighthousepark/intervalsgym/app",
            "com/lighthousepark/intervalsgym/running/ui",
            "com/lighthousepark/intervalsgym/strength/ui",
            "com/lighthousepark/intervalsgym/training/ui",
            "com/lighthousepark/intervalsgym/workout/ui"
        ).map { mainSourceRoot.resolve(it) }
        val forbiddenCalls = listOf(
            "appendStrengthSessionHistory(",
            "replaceStrengthSessionHistory(",
            "appendRunningSessionHistory(",
            "replaceRunningSessionHistory(",
            "deleteRunningSessionHistory("
        )
        val violations = uiRoots
            .flatMap(::kotlinFiles)
            .flatMap { path ->
                val text = Files.readString(path)
                forbiddenCalls
                    .filter { call -> text.contains(call) }
                    .map { call -> "${path.relativeToProject()}: $call" }
            }

        assertEquals(emptyList<String>(), violations)
    }

    @Test
    fun intervalsRepositoryDoesNotOwnHttpConnectionMechanics() {
        val text = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/data/IntervalsRepository.kt")
        )

        assertFalse(text.contains("HttpURLConnection"))
        assertFalse(text.contains("multipart/form-data"))
        assertTrue(text.contains("IntervalsApiClient"))
    }

    @Test
    fun uiScreensUseIntervalsUseCaseFactoryInsteadOfRemoteWiring() {
        val uiRoots = listOf(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui"),
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui"),
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui"),
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui")
        )
        val forbiddenConstructors = listOf(
            "IntervalsRepository(",
            "IntervalsCalendarRoutineRemoteDataSource(",
            "IntervalsTrainingCalendarRemoteDataSource(",
            "IntervalsStrengthSessionRemoteDataSource(",
            "IntervalsRunningSessionRemoteDataSource(",
            "CalendarRoutineSyncUseCase(",
            "TrainingCalendarDataUseCase(",
            "StrengthSessionSyncUseCase(",
            "RunningSessionSyncUseCase("
        )
        val violations = uiRoots
            .flatMap(::kotlinFiles)
            .flatMap { path ->
                val content = Files.readString(path)
                forbiddenConstructors
                    .filter { constructor -> content.contains(constructor) }
                    .map { constructor -> "${path.relativeToProject()}: $constructor" }
            }

        assertEquals(emptyList<String>(), violations)
    }

    @Test
    fun uiScreensDelegateCalendarRoutineDeleteExecutionToUseCase() {
        val uiRoots = listOf(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui"),
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui"),
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui")
        )
        val forbiddenCalls = listOf(
            "shouldDeleteRemote(",
            "deleteRemoteRoutine(",
            "deleteLocalRoutine("
        )
        val violations = uiRoots
            .flatMap(::kotlinFiles)
            .flatMap { path ->
                val content = Files.readString(path)
                forbiddenCalls
                    .filter { call -> content.contains(call) }
                    .map { call -> "${path.relativeToProject()}: $call" }
            }
        val syncUseCase = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/data/CalendarRoutineSyncUseCase.kt")
        )

        assertEquals(emptyList<String>(), violations)
        assertTrue(syncUseCase.contains("fun deleteScopeFor("))
        assertTrue(syncUseCase.contains("suspend fun deleteRoutine("))
    }

    @Test
    fun appAndUiUseSessionHistoryQueryUseCaseInsteadOfStorageFunctions() {
        val roots = listOf(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/app"),
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui"),
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui"),
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui"),
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui")
        )
        val forbiddenCalls = listOf(
            "loadCompletedStrengthSessionHistory(",
            "loadCompletedRunningSessionHistory(",
            "appendStrengthSessionHistory(",
            "replaceStrengthSessionHistory(",
            "deleteStrengthSessionHistory(",
            "appendRunningSessionHistory(",
            "replaceRunningSessionHistory(",
            "deleteRunningSessionHistory("
        )
        val violations = roots
            .flatMap(::kotlinFiles)
            .flatMap { path ->
                val content = Files.readString(path)
                forbiddenCalls
                    .filter { call -> content.contains(call) }
                    .map { call -> "${path.relativeToProject()}: $call" }
            }

        assertEquals(emptyList<String>(), violations)
    }

    private fun dataTest(fileName: String) = testSourceRoot.resolve(
        "com/lighthousepark/intervalsgym/data/$fileName"
    )
}

package com.lighthousepark.intervalsgym

import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingDomainArchitectureGuardTest {
    private val mainSourceRoot = ArchitectureGuardProject.mainSourceRoot
    private val testSourceRoot = ArchitectureGuardProject.testSourceRoot

    @Test
    fun trainingCalendarDataUseCaseDoesNotAssembleDisplayResults() {
        val dataUseCase = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/data/TrainingCalendarDataUseCase.kt")
        )
        val pageRenderData = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/TrainingCalendarPageRenderData.kt")
        )
        val displayAssemblyCalls = listOf(
            "withLocalStrengthResults(",
            "withLocalRunningResults(",
            "withLocalStrengthRoutines(",
            "withPendingCalendarRoutineMoves(",
            "mergeTrainingRoutinesAndResults("
        )

        displayAssemblyCalls.forEach { call ->
            assertFalse("$call belongs behind buildTrainingCalendarPageRenderData", dataUseCase.contains(call))
            assertTrue("$call missing from TrainingCalendarPageRenderData.kt", pageRenderData.contains(call))
        }
    }

    @Test
    fun calendarRoutineMoveTestsStayFocused() {
        val trainingModelsTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/training/TrainingModelsTest.kt")
        )
        val calendarRoutineMovesTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/training/CalendarRoutineMovesTest.kt")
        )
        val focusedTestNames = listOf(
            "pendingCalendarRoutineMoves_renderSyntheticTargetWithTimeAwareStrengthExternalId",
            "pendingCalendarRoutineMoves_dropWhenRemoteTargetIsReflectedWithoutSource"
        )

        focusedTestNames.forEach { testName ->
            assertFalse("$testName belongs in CalendarRoutineMovesTest.kt", trainingModelsTest.contains(testName))
            assertTrue("$testName missing from CalendarRoutineMovesTest.kt", calendarRoutineMovesTest.contains(testName))
        }
    }

    @Test
    fun trainingItemModelDoesNotDecodeEmbeddedStrengthRoutineDescriptions() {
        val trainingModels = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/TrainingModels.kt")
        )
        val strengthRoutineDescriptions = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/data/StrengthRoutineDescriptions.kt")
        )

        assertFalse(trainingModels.contains("import com.lighthousepark.intervalsgym.data"))
        assertFalse(trainingModels.contains("toIntervalsGymStrengthRoutine("))
        assertFalse(trainingModels.contains("strengthRoutineForDisplay("))
        assertTrue(strengthRoutineDescriptions.contains("internal fun TrainingItem.strengthRoutineForDisplay("))
    }

    @Test
    fun trainingItemModelsStaySplitByConcern() {
        val trainingModels = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/TrainingModels.kt")
        )
        val sportTypes = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/TrainingItemSportTypes.kt")
        )
        val display = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/TrainingItemDisplay.kt")
        )
        val dragRules = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/TrainingCalendarRoutineDragRules.kt")
        )
        val preview = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/TrainingWorkoutPreview.kt")
        )
        val merge = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/TrainingRoutineResultMerge.kt")
        )
        val metricSummaries = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/TrainingMetricSummaries.kt")
        )
        val trainingModelsTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/training/TrainingModelsTest.kt")
        )
        val focusedTestFiles = listOf(
            "TrainingItemSportTypesTest.kt",
            "TrainingItemDisplayTest.kt",
            "TrainingCalendarRoutineDragRulesTest.kt",
            "TrainingWorkoutPreviewTest.kt",
            "TrainingRoutineResultMergeTest.kt",
            "TrainingMetricSummariesTest.kt"
        )

        assertTrue(trainingModels.contains("internal data class WeekTrainingData"))
        assertTrue(trainingModels.contains("internal data class TrainingItem"))
        assertFalse("TrainingModels.kt should stay as data shapes only.", trainingModels.contains("internal fun "))
        mapOf(
            sportTypes to listOf("internal fun TrainingItem.sportType(", "internal fun TrainingItem.isWeightTrainingItem("),
            display to listOf("internal fun TrainingItem.displayTimeLabel(", "internal fun TrainingItem.plannedWorkoutDeleteConfirmMessage("),
            dragRules to listOf("internal fun TrainingItem.calendarRoutineForMove(", "internal fun TrainingItem.canDragCalendarRoutine("),
            preview to listOf("internal fun TrainingItem.workoutRoutineBlocksForPreview(", "internal fun List<RoutineBlock>.withRunningGraphContext("),
            merge to listOf("internal fun mergeTrainingRoutinesAndResults("),
            metricSummaries to listOf("internal fun List<TrainingItem>.latestMetricValue(")
        ).forEach { (owner, definitions) ->
            definitions.forEach { definition ->
                assertTrue("$definition missing from its focused training item file", owner.contains(definition))
                assertFalse("$definition belongs outside TrainingModels.kt", trainingModels.contains(definition))
            }
        }
        focusedTestFiles.forEach { fileName ->
            assertTrue(
                "$fileName should own focused training item rule tests.",
                Files.exists(testSourceRoot.resolve("com/lighthousepark/intervalsgym/training/$fileName"))
            )
        }
        listOf(
            "mergeTrainingRoutinesAndResults_pairsSameDaySameSport",
            "canDragCalendarRoutine_allowsRemoteRoutineAndPairedRoutineWhenLoggedIn",
            "runningGraphContext_usesLineMatchedDescriptionTargetsForRepeatedSprint",
            "latestMetricValue_usesStartedAtBeforeDateAndSkipsNulls"
        ).forEach { movedTestName ->
            assertFalse("$movedTestName belongs in a focused training item test file.", trainingModelsTest.contains(movedTestName))
        }
    }

    @Test
    fun workoutGraphRulesStaySplitByConcern() {
        val oldGraphBucket = mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/WorkoutRoutineGraph.kt")
        val models = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/WorkoutGraphModels.kt")
        )
        val blocks = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/WorkoutGraphBlocks.kt")
        )
        val powerTargets = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/WorkoutGraphPowerTargets.kt")
        )
        val runningTargets = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/WorkoutGraphRunningTargets.kt")
        )
        val formatting = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/WorkoutGraphFormatting.kt")
        )
        val oldGraphTestBucket = testSourceRoot.resolve("com/lighthousepark/intervalsgym/training/WorkoutRoutineGraphTest.kt")
        val runningTargetsTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/training/WorkoutGraphRunningTargetsTest.kt")
        )
        val powerTargetsTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/training/WorkoutGraphPowerTargetsTest.kt")
        )
        val formattingTest = Files.readString(
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/training/WorkoutGraphFormattingTest.kt")
        )
        val definitionsByOwner = mapOf(
            models to listOf(
                "internal enum class TrainingSportType",
                "internal data class RoutineBlock",
                "internal enum class WorkoutGraphUnit",
                "internal data class WorkoutGraphBlock"
            ),
            blocks to listOf("internal fun List<RoutineBlock>.toWorkoutGraphBlocks"),
            powerTargets to listOf(
                "internal fun RoutineBlock.graphTargetWatts",
                "internal fun String.parseGraphTargetWatts",
                "internal fun String.parseCyclingUnitlessWatts",
                "internal fun RoutineBlock.graphTargetPercent",
                "internal fun String.parseGraphTargetPercent",
                "internal fun String.percentTargetLooksLikeRunningIncline"
            ),
            runningTargets to listOf(
                "internal data class RunningTargetDisplay",
                "internal fun RoutineBlock.graphTargetSpeedKmh",
                "internal fun RoutineBlock.runningTargetDisplay",
                "internal fun RoutineBlock.graphTargetSource",
                "internal fun RoutineBlock.graphTargetSourcesByPriority",
                "internal fun RoutineBlock.runningTargetSpeedText",
                "internal fun RoutineBlock.runningInclineText",
                "internal fun RoutineBlock.runningInclinePercent",
                "internal fun String.parseRunningInclinePercent",
                "internal fun String.containsRunningSpeedTarget"
            ),
            formatting to listOf(
                "internal fun WorkoutGraphBlock.graphColor",
                "internal fun Float.formatGraphAxisLabels",
                "internal fun formatPaceFromKmh",
                "internal fun formatKmh"
            )
        )

        assertFalse(
            "Broad workout graph bucket should stay split into focused training graph files",
            Files.exists(oldGraphBucket)
        )
        definitionsByOwner.forEach { (owner, definitions) ->
            definitions.forEach { definition ->
                assertTrue("$definition missing from its focused workout graph file", owner.contains(definition))
            }
        }
        assertFalse(
            "Broad workout graph test bucket should stay split into focused graph test files",
            Files.exists(oldGraphTestBucket)
        )
        assertTrue(runningTargetsTest.contains("runningGraph_usesPaceAsSpeedAndTreatsPercentAsIncline"))
        assertTrue(powerTargetsTest.contains("cyclingGraph_usesUnitlessWattsAndFtpPercentContext"))
        assertTrue(formattingTest.contains("speedAxisLabelForZeroShowsOnlyZero"))
        assertFalse(
            "Running target parsing must reuse top-level Regex instances instead of compiling during session ticks.",
            runningTargets.substringAfter("internal fun RoutineBlock.graphTargetSpeedKmh").contains("Regex(")
        )
    }

    @Test
    fun trainingDomainFilesDoNotUseProjectWildcardImports() {
        val trainingRoots = listOf(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training"),
            testSourceRoot.resolve("com/lighthousepark/intervalsgym/training")
        )
        val violations = trainingRoots
            .flatMap { root ->
                kotlinFiles(root)
                    .filter { path -> path.parent == root }
            }
            .filter { path ->
                Regex("""import com\.lighthousepark\.intervalsgym\..*\.\*""")
                    .containsMatchIn(Files.readString(path))
            }
            .map { it.relativeToProject() }

        assertEquals(emptyList<String>(), violations)
    }
}

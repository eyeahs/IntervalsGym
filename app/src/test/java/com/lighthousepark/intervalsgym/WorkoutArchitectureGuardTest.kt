package com.lighthousepark.intervalsgym

import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutArchitectureGuardTest {
    private val mainSourceRoot = ArchitectureGuardProject.mainSourceRoot

    @Test
    fun workoutVisualComponentsDoNotUseProjectWildcardOrTrainingUiImports() {
        val visualFiles = listOf(
            "WorkoutCommonVisuals.kt",
            "WorkoutRoutineVisuals.kt",
            "WorkoutRoutineContent.kt",
            "WorkoutStrengthSessionVisuals.kt",
            "WorkoutGraphVisuals.kt",
            "WorkoutRunningRouteVisuals.kt",
            "WorkoutRoutineExecutionVisuals.kt"
        ).map { fileName -> mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/$fileName") }
        val wildcardViolations = visualFiles
            .filter { path ->
                Regex("""import com\.lighthousepark\.intervalsgym\..*\.\*""")
                    .containsMatchIn(Files.readString(path))
            }
            .map { it.relativeToProject() }
        val trainingUiImports = visualFiles
            .filter { path ->
                Files.readString(path).contains("import com.lighthousepark.intervalsgym.training.ui.")
            }
            .map { it.relativeToProject() }

        assertEquals(emptyList<String>(), wildcardViolations)
        assertEquals(emptyList<String>(), trainingUiImports)
    }

    @Test
    fun workoutRoutineRouteOwnerDoesNotUseProjectWildcardImports() {
        val routeOwner = mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutRoutineScreen.kt")
        val text = Files.readString(routeOwner)

        assertFalse(
            "WorkoutRoutineScreen.kt should keep app/data/running/strength/training dependencies explicit.",
            Regex("""import com\.lighthousepark\.intervalsgym\..*\.\*""").containsMatchIn(text)
        )
    }

    @Test
    fun trainingItemComponentsDoNotOwnSharedWorkoutTypeLabel() {
        val trainingItemComponents = listOf(
            "TrainingCalendarMonthComponents.kt",
            "TrainingCalendarListItemComponents.kt",
            "TrainingCalendarStatusComponents.kt"
        ).joinToString("\n") { fileName ->
            Files.readString(mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/$fileName"))
        }
        val commonVisuals = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutCommonVisuals.kt")
        )

        assertFalse(trainingItemComponents.contains("internal fun TrainingTypeLabel"))
        assertTrue(commonVisuals.contains("internal fun TrainingTypeLabel"))
    }

    @Test
    fun heartRateDevicePickerDialogStaysWithRunningUi() {
        val workoutRoutineScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutRoutineScreen.kt")
        )
        val pickerDialog = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/running/ui/HeartRateDevicePickerDialog.kt")
        )

        assertFalse(workoutRoutineScreen.contains("fun HeartRateDevicePickerDialog"))
        assertTrue(pickerDialog.contains("fun HeartRateDevicePickerDialog"))
        assertTrue(pickerDialog.contains("HeartRateSensorState"))
    }

    @Test
    fun workoutRoutineActionStateOwnsUploadAndDeleteFlags() {
        val workoutRoutineScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutRoutineScreen.kt")
        )
        val actionState = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutRoutineActionUiState.kt")
        )
        val stateDefinitions = listOf(
            "internal data class WorkoutRoutineActionUiState",
            "internal fun canUploadLocalStrengthWorkout"
        )
        val forbiddenStateDeclarations = listOf(
            "var isUploadingStrengthSession",
            "var uploadedInThisScreen",
            "var uploadMessage",
            "var uploadError",
            "var isDeleteConfirmVisible",
            "var isDeletingRoutine",
            "var deleteError"
        )

        stateDefinitions.forEach { definition ->
            assertFalse("$definition belongs in WorkoutRoutineActionUiState.kt", workoutRoutineScreen.contains(definition))
            assertTrue("$definition missing from WorkoutRoutineActionUiState.kt", actionState.contains(definition))
        }
        forbiddenStateDeclarations.forEach { declaration ->
            assertFalse("$declaration belongs in WorkoutRoutineActionUiState", workoutRoutineScreen.contains(declaration))
        }
        assertTrue(workoutRoutineScreen.contains("var actionUiState"))
        assertTrue(workoutRoutineScreen.contains("canUploadLocalStrengthWorkout("))
        assertTrue(workoutRoutineScreen.contains("actionUiState.withUploadStarted("))
        assertTrue(workoutRoutineScreen.contains("actionUiState.withDeleteStarted("))
    }

    @Test
    fun workoutRunningMergeStateAndDialogStayOutOfRouteOwner() {
        val workoutRoutineScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutRoutineScreen.kt")
        )
        val mergeState = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutRunningMergeUiState.kt")
        )
        val mergeComponents = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutRunningMergeComponents.kt")
        )

        assertFalse(workoutRoutineScreen.contains("internal data class WorkoutRunningMergeUiState"))
        assertTrue(mergeState.contains("internal data class WorkoutRunningMergeUiState"))
        assertFalse(workoutRoutineScreen.contains("fun WorkoutRunningMergeConfirmDialog"))
        assertTrue(mergeComponents.contains("fun WorkoutRunningMergeConfirmDialog"))
        assertTrue(workoutRoutineScreen.contains("WorkoutRunningMergeConfirmDialog("))
        assertTrue(workoutRoutineScreen.contains("var runningMergeUiState"))
    }

    @Test
    fun workoutRoutineActionsOwnRouteActionPlanningAndSyncCalls() {
        val workoutRoutineScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutRoutineScreen.kt")
        )
        val actions = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutRoutineActions.kt")
        )
        val movedDefinitions = listOf(
            "internal fun planWorkoutRoutineLocalStrengthUpload(",
            "internal fun planWorkoutRoutineCalendarDelete(",
            "internal fun planWorkoutRoutineLocalRunningDelete(",
            "internal fun planWorkoutRoutineSaveRunningRoutine(",
            "internal fun planWorkoutRoutineStartAction("
        )
        val movedCalls = listOf(
            "syncUseCase.uploadStrengthSession(",
            "syncUseCase.deleteRoutine(",
            "syncUseCase.deleteRunningSession(",
            "toSavedRunningWorkoutRoutine(",
            "upsertSavedRunningWorkoutRoutine("
        )

        movedDefinitions.forEach { definition ->
            assertFalse("$definition belongs in WorkoutRoutineActions.kt", workoutRoutineScreen.contains(definition))
            assertTrue("$definition missing from WorkoutRoutineActions.kt", actions.contains(definition))
        }
        movedCalls.forEach { call ->
            assertFalse("$call belongs behind WorkoutRoutineActions.kt", workoutRoutineScreen.contains(call))
            assertTrue("$call missing from WorkoutRoutineActions.kt", actions.contains(call))
        }
        assertTrue(workoutRoutineScreen.contains("planWorkoutRoutineLocalStrengthUpload("))
        assertTrue(workoutRoutineScreen.contains("planWorkoutRoutineCalendarDelete("))
        assertTrue(workoutRoutineScreen.contains("planWorkoutRoutineLocalRunningDelete("))
        assertTrue(workoutRoutineScreen.contains("planWorkoutRoutineSaveRunningRoutine("))
        assertTrue(workoutRoutineScreen.contains("planWorkoutRoutineStartAction("))
    }

    @Test
    fun workoutRoutineChromeStaysOutOfRouteOwner() {
        val workoutRoutineScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutRoutineScreen.kt")
        )
        val chrome = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutRoutineChrome.kt")
        )
        val chromeDefinitions = listOf(
            "internal fun WorkoutRoutineTopBar",
            "internal fun WorkoutRoutineDeleteConfirmDialog",
            "internal fun WorkoutRoutineStartActionBar",
            "internal fun workoutRoutineHeartRateDeviceLabel",
            "internal fun workoutRoutineHeartRateStatusLabel"
        )
        val chromeCalls = listOf(
            "TopAppBar(",
            "AlertDialog(",
            "OutlinedButton(",
            "IconButton(",
            "Icons.Outlined.CloudUpload",
            "TestContentDescriptions.WorkoutRoutineStartWorkout"
        )

        chromeDefinitions.forEach { definition ->
            assertFalse("$definition belongs in WorkoutRoutineChrome.kt", workoutRoutineScreen.contains(definition))
            assertTrue("$definition missing from WorkoutRoutineChrome.kt", chrome.contains(definition))
        }
        chromeCalls.forEach { call ->
            assertFalse("$call belongs behind WorkoutRoutineChrome.kt", workoutRoutineScreen.contains(call))
            assertTrue("$call missing from WorkoutRoutineChrome.kt", chrome.contains(call))
        }
        assertTrue(workoutRoutineScreen.contains("WorkoutRoutineTopBar("))
        assertTrue(workoutRoutineScreen.contains("WorkoutRoutineDeleteConfirmDialog("))
        assertTrue(workoutRoutineScreen.contains("WorkoutRoutineStartActionBar("))
    }

    @Test
    fun workoutRoutineDetailContentStaysOutOfRouteOwner() {
        val workoutRoutineScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutRoutineScreen.kt")
        )
        val content = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutRoutineContent.kt")
        )
        val movedDefinitions = listOf(
            "internal fun WorkoutRoutineDetailContent"
        )
        val contentOnlyCalls = listOf(
            Regex("""(?m)^\s*LazyColumn\(""") to "LazyColumn(",
            Regex("""TrainingItemDetailCard\(""") to "TrainingItemDetailCard(",
            Regex("""LocalStrengthSessionDetailSection\(""") to "LocalStrengthSessionDetailSection(",
            Regex("""DetailSection\(title = "설명"\)""") to "DetailSection(title = \"설명\")",
            Regex("""RoutineWorkoutGraph\(""") to "RoutineWorkoutGraph(",
            Regex("""LocalRunningSessionGraphSection\(""") to "LocalRunningSessionGraphSection(",
            Regex("""workoutDetailDescription\(""") to "workoutDetailDescription("
        )

        movedDefinitions.forEach { definition ->
            assertFalse("$definition belongs in WorkoutRoutineContent.kt", workoutRoutineScreen.contains(definition))
            assertTrue("$definition missing from WorkoutRoutineContent.kt", content.contains(definition))
        }
        contentOnlyCalls.forEach { (pattern, call) ->
            assertFalse("$call belongs in WorkoutRoutineContent.kt", pattern.containsMatchIn(workoutRoutineScreen))
            assertTrue("$call missing from WorkoutRoutineContent.kt", pattern.containsMatchIn(content))
        }
        assertTrue(workoutRoutineScreen.contains("WorkoutRoutineDetailContent("))
    }

    @Test
    fun sharedWorkoutCommonVisualsStayOutOfWorkoutRoutineVisuals() {
        val workoutRoutineVisuals = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutRoutineVisuals.kt")
        )
        val commonVisuals = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutCommonVisuals.kt")
        )
        val trainingCalendarUiState = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/training/ui/TrainingCalendarUiState.kt")
        )
        val commonDefinitions = listOf(
            "internal fun MetricChip",
            "internal fun LoadingView",
            "internal fun EmptyView",
            "internal fun ErrorView",
            "internal fun TrainingSportType.icon",
            "internal fun TrainingSportIcon"
        )

        commonDefinitions.forEach { definition ->
            assertFalse("$definition belongs in WorkoutCommonVisuals.kt", workoutRoutineVisuals.contains(definition))
            assertTrue("$definition missing from WorkoutCommonVisuals.kt", commonVisuals.contains(definition))
        }
        assertFalse("WeekUiState belongs with training calendar UI state", workoutRoutineVisuals.contains("internal data class WeekUiState"))
        assertTrue(trainingCalendarUiState.contains("internal data class WeekUiState"))
    }

    @Test
    fun strengthSessionVisualsStayOutOfWorkoutRoutineVisuals() {
        val workoutRoutineVisuals = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutRoutineVisuals.kt")
        )
        val strengthVisuals = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutStrengthSessionVisuals.kt")
        )
        val movedDefinitions = listOf(
            "internal fun StrengthSessionSummary",
            "internal fun LocalStrengthSessionDetailSection",
            "internal fun StrengthSessionExerciseDetail",
            "internal fun StrengthSessionSetDetailRow",
            "internal fun buildStrengthSetSummary"
        )

        movedDefinitions.forEach { definition ->
            assertFalse("$definition belongs in WorkoutStrengthSessionVisuals.kt", workoutRoutineVisuals.contains(definition))
            assertTrue("$definition missing from WorkoutStrengthSessionVisuals.kt", strengthVisuals.contains(definition))
        }
    }

    @Test
    fun workoutGraphRouteAndExecutionVisualsStayOutOfWorkoutRoutineVisuals() {
        val workoutRoutineVisuals = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutRoutineVisuals.kt")
        )
        val graphVisuals = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutGraphVisuals.kt")
        )
        val routeVisuals = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutRunningRouteVisuals.kt")
        )
        val executionVisuals = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/workout/ui/WorkoutRoutineExecutionVisuals.kt")
        )
        val movedDefinitions = listOf(
            "internal fun RoutineWorkoutGraph" to graphVisuals,
            "internal fun RoutineWorkoutGraphCanvas" to graphVisuals,
            "internal fun LocalRunningRoutePreview" to routeVisuals,
            "internal fun RunningTimerPanel" to executionVisuals,
            "internal fun TimerStat" to executionVisuals,
            "internal fun RoutineTimeline" to executionVisuals,
            "internal fun RoutineBlockRow" to executionVisuals
        )

        movedDefinitions.forEach { (definition, ownerSource) ->
            assertFalse("$definition belongs outside WorkoutRoutineVisuals.kt", workoutRoutineVisuals.contains(definition))
            assertTrue("$definition missing from its workout visual owner", ownerSource.contains(definition))
        }
        assertFalse("Graph axis drawing belongs in WorkoutGraphVisuals.kt", workoutRoutineVisuals.contains("formatGraphAxisLabels"))
        assertFalse("Route path drawing belongs in WorkoutRunningRouteVisuals.kt", workoutRoutineVisuals.contains("drawPath("))
    }
}

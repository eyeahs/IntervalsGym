package com.lighthousepark.intervalsgym

import java.nio.file.Files
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthSessionStateArchitectureGuardTest {
    private val mainSourceRoot = ArchitectureGuardProject.mainSourceRoot

    @Test
    fun strengthSessionProgressUiStateOwnsStartAndElapsedFields() {
        val routedSessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionScreen.kt")
        )
        val progressUiState = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionProgressUiState.kt")
        )
        val forbiddenStateDeclarations = listOf(
            "var hasStarted",
            "var sessionStartedAtMillis",
            "var sessionElapsedSeconds"
        )
        val requiredDefinitions = listOf(
            "internal data class StrengthSessionProgressUiState",
            "fun started(",
            "fun withElapsedSeconds(",
            "fun restored("
        )

        forbiddenStateDeclarations.forEach { declaration ->
            assertFalse("$declaration belongs in StrengthSessionProgressUiState", routedSessionScreen.contains(declaration))
        }
        requiredDefinitions.forEach { definition ->
            assertFalse("$definition belongs in StrengthSessionProgressUiState.kt", routedSessionScreen.contains(definition))
            assertTrue("$definition missing from StrengthSessionProgressUiState.kt", progressUiState.contains(definition))
        }
        assertTrue(routedSessionScreen.contains("var progressUiState"))
        assertTrue(routedSessionScreen.contains("StrengthSessionProgressUiState.restored("))
        assertTrue(routedSessionScreen.contains("progressUiState.started("))
        assertTrue(routedSessionScreen.contains("progressUiState.withElapsedSeconds("))
    }

    @Test
    fun routedStrengthSessionScreenDoesNotBuildCompletedResultsDirectly() {
        val routedSessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionScreen.kt")
        )
        val resultDrafts = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionResultDrafts.kt")
        )
        val runtimeSnapshots = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionRuntimeSnapshots.kt")
        )
        val finishActions = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionFinishActions.kt")
        )
        val forbiddenCompletedResultCalls = listOf(
            "buildCompletedStrengthSession(",
            "finalizeRestEvents("
        )

        forbiddenCompletedResultCalls.forEach { call ->
            assertFalse("$call belongs behind StrengthSessionSyncUseCase", routedSessionScreen.contains(call))
        }
        assertFalse(
            "Finished strength results should use explicit local/upload status instead of guessing from the current API key.",
            routedSessionScreen.contains("uploadedToIntervals = apiKey.isNotBlank()")
        )
        assertFalse("StrengthSessionResultDraft fields belong in StrengthSessionResultDrafts.kt", routedSessionScreen.contains("StrengthSessionResultDraft("))
        assertTrue(resultDrafts.contains("StrengthSessionResultDraft("))
        assertFalse(
            "StrengthSessionResultSnapshot field assembly belongs in StrengthSessionRuntimeSnapshots.kt",
            routedSessionScreen.contains("return StrengthSessionResultSnapshot(")
        )
        assertTrue(runtimeSnapshots.contains("StrengthSessionResultSnapshot("))
        assertTrue(routedSessionScreen.contains("StrengthSessionRuntimeSnapshot("))
        listOf(
            "saveLiveStrengthSessionResult(",
            "buildFinishedStrengthSessionResult(",
            "deleteLiveStrengthSessionResult("
        ).forEach { call ->
            assertFalse("$call belongs behind StrengthSessionResultSnapshot actions", routedSessionScreen.contains(call))
            assertTrue("$call missing from StrengthSessionResultDrafts.kt", resultDrafts.contains(call))
        }
        listOf(
            "fun saveLiveResult(",
            "fun buildFinishedResult(",
            "fun deleteLiveResult("
        ).forEach { functionName ->
            assertTrue("$functionName missing from StrengthSessionResultDrafts.kt", resultDrafts.contains(functionName))
        }
        assertTrue(routedSessionScreen.contains("saveLiveResult("))
        assertTrue(routedSessionScreen.contains("deleteLiveResult("))
        assertFalse(
            "Finished strength session save/upload planning belongs in StrengthSessionFinishActions.kt",
            routedSessionScreen.contains("buildFinishedResult(")
        )
        assertTrue(routedSessionScreen.contains("planFinishedStrengthSession("))
        listOf(
            "internal sealed interface StrengthSessionFinishAction",
            "internal data class SaveFinishedStrengthSessionLocally",
            "internal data class UploadFinishedStrengthSession",
            "fun StrengthSessionResultSnapshot.planFinishedStrengthSession(",
            "fun SaveFinishedStrengthSessionLocally.saveLocalResult(",
            "suspend fun UploadFinishedStrengthSession.uploadResult(",
            "internal data class StrengthSessionCalendarRoutineDeleteAction",
            "internal fun planStrengthSessionCalendarRoutineDelete("
        ).forEach { definition ->
            assertTrue("$definition missing from StrengthSessionFinishActions.kt", finishActions.contains(definition))
        }
        listOf(
            "saveStrengthSessionLocally(",
            "uploadStrengthSession(",
            "deleteRoutine("
        ).forEach { call ->
            assertFalse("$call belongs behind StrengthSessionFinishActions.kt", routedSessionScreen.contains(call))
            assertTrue("$call missing from StrengthSessionFinishActions.kt", finishActions.contains(call))
        }
        assertTrue(routedSessionScreen.contains("planStrengthSessionCalendarRoutineDelete("))
        assertTrue(routedSessionScreen.contains("deleteAction.delete("))
    }

    @Test
    fun routedStrengthSessionScreenDoesNotBuildActiveSessionDirectly() {
        val routedSessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionScreen.kt")
        )
        val activeSnapshots = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthActiveSessionSnapshots.kt")
        )
        val runtimeSnapshots = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionRuntimeSnapshots.kt")
        )

        assertFalse("ActiveStrengthSession fields belong in StrengthActiveSessionSnapshots.kt", routedSessionScreen.contains("ActiveStrengthSession("))
        assertTrue(activeSnapshots.contains("ActiveStrengthSession("))
        assertFalse(
            "StrengthActiveSessionSnapshot field assembly belongs in StrengthSessionRuntimeSnapshots.kt",
            routedSessionScreen.contains("return StrengthActiveSessionSnapshot(")
        )
        assertTrue(runtimeSnapshots.contains("StrengthActiveSessionSnapshot("))
        assertTrue(routedSessionScreen.contains("StrengthSessionRuntimeSnapshot("))
    }

    @Test
    fun strengthRestUiStateOwnsRestTimerUiFields() {
        val routedSessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionScreen.kt")
        )
        val restUiState = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthRestUiState.kt")
        )
        val forbiddenStateDeclarations = listOf(
            "var restRemainingSeconds",
            "var restEndAtMillis",
            "var isRestSheetVisible",
            "var restTitle",
            "var activeRestEventId"
        )

        forbiddenStateDeclarations.forEach { declaration ->
            assertFalse("$declaration belongs in StrengthRestUiState.kt", routedSessionScreen.contains(declaration))
        }
        assertTrue(restUiState.contains("data class StrengthRestUiState"))
        assertTrue(routedSessionScreen.contains("val restUiState = interactionState.restUiState"))
    }

    @Test
    fun strengthNavigationUiStateOwnsSetNavigationFields() {
        val routedSessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionScreen.kt")
        )
        val navigationUiState = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionNavigationUiState.kt")
        )
        val forbiddenStateDeclarations = listOf(
            "var isSetScreenVisible",
            "var currentExerciseIndex",
            "var currentSetIndex",
            "var pendingExerciseIndex",
            "var pendingSetIndex"
        )

        forbiddenStateDeclarations.forEach { declaration ->
            assertFalse("$declaration belongs in StrengthSessionNavigationUiState.kt", routedSessionScreen.contains(declaration))
        }
        assertTrue(navigationUiState.contains("data class StrengthSessionNavigationUiState"))
        assertTrue(navigationUiState.contains("fun applyCompletedSetResult("))
        assertTrue(navigationUiState.contains("fun moveToPendingSet("))
        assertTrue(routedSessionScreen.contains("val navigationUiState = interactionState.navigationUiState"))
    }

    @Test
    fun strengthSessionInteractionStateOwnsEntriesEventsRestAndNavigationTogether() {
        val routedSessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionScreen.kt")
        )
        val transitions = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionStateTransitions.kt")
        )
        val forbiddenStateDeclarations = listOf(
            "var entries",
            "var navigationUiState",
            "var restUiState",
            "var setEvents",
            "var restEvents"
        )
        val requiredDefinitions = listOf(
            "internal data class StrengthSessionInteractionState",
            "internal fun restoredStrengthSessionInteractionState("
        )

        forbiddenStateDeclarations.forEach { declaration ->
            assertFalse("$declaration belongs in StrengthSessionInteractionState", routedSessionScreen.contains(declaration))
        }
        requiredDefinitions.forEach { definition ->
            assertFalse("$definition belongs in StrengthSessionStateTransitions.kt", routedSessionScreen.contains(definition))
            assertTrue("$definition missing from StrengthSessionStateTransitions.kt", transitions.contains(definition))
        }
        assertTrue(routedSessionScreen.contains("var interactionState"))
        assertTrue(routedSessionScreen.contains("restoredStrengthSessionInteractionState("))
        assertTrue(routedSessionScreen.contains("val entries = interactionState.entries"))
        assertTrue(routedSessionScreen.contains("val restUiState = interactionState.restUiState"))
        assertTrue(routedSessionScreen.contains("val setEvents = interactionState.setEvents"))
        assertTrue(routedSessionScreen.contains("interactionState = transition.state"))
    }

    @Test
    fun strengthSessionStateTransitionsOwnSetAndRestStateChanges() {
        val routedSessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionScreen.kt")
        )
        val transitions = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionStateTransitions.kt")
        )
        val runtimeSnapshots = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionRuntimeSnapshots.kt")
        )
        val runtimeSideEffects = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionRuntimeSideEffects.kt")
        )
        val transitionDefinitions = listOf(
            "internal data class StrengthSessionInteractionState",
            "internal data class StrengthSessionStateTransition",
            "internal fun StrengthSessionInteractionState.withEntriesReplaced",
            "internal fun StrengthSessionInteractionState.withClosedActiveRest",
            "internal fun StrengthSessionInteractionState.movedToPendingSet",
            "internal fun StrengthSessionInteractionState.withUpdatedRestSeconds",
            "internal fun StrengthSessionInteractionState.withCompletedCurrentSet"
        )
        val domainCalls = listOf(
            "closeActiveStrengthRestEvent(",
            "completeStrengthSet(",
            "startStrengthRestTimer(",
            "updateStrengthRestTimerSeconds(",
            "withCurrentStrengthSetDetails(",
            "withCurrentStrengthRestDetails("
        )

        transitionDefinitions.forEach { definition ->
            assertFalse("$definition belongs in StrengthSessionStateTransitions.kt", routedSessionScreen.contains(definition))
            assertTrue("$definition missing from StrengthSessionStateTransitions.kt", transitions.contains(definition))
        }
        domainCalls.forEach { call ->
            assertFalse("$call belongs behind StrengthSessionStateTransitions.kt", routedSessionScreen.contains(call))
            assertTrue("$call missing from StrengthSessionStateTransitions.kt", transitions.contains(call))
        }
        assertFalse(
            "StrengthSessionInteractionState field assembly belongs in StrengthSessionRuntimeSnapshots.kt",
            routedSessionScreen.contains("return StrengthSessionInteractionState(")
        )
        assertTrue(runtimeSnapshots.contains("StrengthSessionInteractionState("))
        assertFalse(
            "Rest overlay transition command dispatch belongs in StrengthSessionRuntimeSideEffects.kt",
            routedSessionScreen.contains("transition.restOverlayCommand")
        )
        assertTrue(runtimeSideEffects.contains("fun StrengthSessionStateTransition.dispatchRestOverlaySideEffects("))
        assertTrue(routedSessionScreen.contains("transition.dispatchRestOverlaySideEffects(context)"))
        assertTrue(routedSessionScreen.contains("currentInteractionState()"))
        assertTrue(routedSessionScreen.contains("applySessionTransition("))
    }

    @Test
    fun strengthSessionExerciseActionsOwnExerciseListStateChanges() {
        val routedSessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionScreen.kt")
        )
        val exerciseActions = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionExerciseActions.kt")
        )
        val actionDefinitions = listOf(
            "internal data class StrengthSessionExerciseActionResult",
            "internal fun StrengthSessionInteractionState.withOpenedExerciseSet",
            "internal fun StrengthSessionInteractionState.withAddedExercise",
            "internal fun StrengthSessionInteractionState.withConfiguredExercise",
            "internal fun StrengthSessionInteractionState.withCanceledExerciseChange",
            "internal fun StrengthSessionInteractionState.withReorderedExercises"
        )
        val movedDomainCalls = listOf(
            "copyAsNewRoutineEntry(",
            "defaultStrengthRoutineEntry(",
            "latestMatchingStrengthEntry(",
            "normalizeSupersetGroups()",
            "strengthExerciseCatalog.first()"
        )
        val screenActionCalls = listOf(
            "withOpenedExerciseSet(",
            "withAddedExercise(",
            "withConfiguredExercise(",
            "withCanceledExerciseChange(",
            "withReorderedExercises(",
            "applyExerciseActionResult("
        )

        actionDefinitions.forEach { definition ->
            assertFalse("$definition belongs in StrengthSessionExerciseActions.kt", routedSessionScreen.contains(definition))
            assertTrue("$definition missing from StrengthSessionExerciseActions.kt", exerciseActions.contains(definition))
        }
        movedDomainCalls.forEach { call ->
            assertFalse("$call belongs behind StrengthSessionExerciseActions.kt", routedSessionScreen.contains(call))
            assertTrue("$call missing from StrengthSessionExerciseActions.kt", exerciseActions.contains(call))
        }
        screenActionCalls.forEach { call ->
            assertTrue("$call missing from StrengthSessionScreen.kt", routedSessionScreen.contains(call))
        }
        assertFalse(
            "Pending added exercise cancellation belongs behind StrengthSessionExerciseActions.kt",
            routedSessionScreen.contains("filterNot { it.id ==")
        )
    }

    @Test
    fun strengthFinishUiStateOwnsFinishAndUploadFields() {
        val routedSessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionScreen.kt")
        )
        val finishUiState = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionFinishUiState.kt")
        )
        val forbiddenStateDeclarations = listOf(
            "var isUploading",
            "var uploadMessage",
            "var uploadError",
            "var isFinishChoiceDialogVisible",
            "var isCalendarRoutineDeleteConfirmVisible",
            "var isDeletingCalendarRoutine",
            "var finishRpe",
            "var routineUpdateSelection"
        )

        forbiddenStateDeclarations.forEach { declaration ->
            assertFalse("$declaration belongs in StrengthSessionFinishUiState.kt", routedSessionScreen.contains(declaration))
        }
        assertTrue(finishUiState.contains("data class StrengthSessionFinishUiState"))
        assertTrue(finishUiState.contains("strengthSessionFinishUiStateSaver"))
        assertTrue(routedSessionScreen.contains("var finishUiState"))
        assertTrue(routedSessionScreen.contains("finishUiState.withUploadStarted("))
        assertTrue(routedSessionScreen.contains("finishUiState.withUploadFailed("))
    }

    @Test
    fun strengthExerciseChangeUiStateOwnsExerciseChangeFlowFields() {
        val routedSessionScreen = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthSessionScreen.kt")
        )
        val exerciseChangeUiState = Files.readString(
            mainSourceRoot.resolve("com/lighthousepark/intervalsgym/strength/ui/StrengthExerciseChangeUiState.kt")
        )
        val forbiddenStateDeclarations = listOf(
            "var isChangingCurrentExercise",
            "var isCurrentExerciseTypeDialogVisible",
            "var shouldReturnToOngoingAfterExerciseChange",
            "var pendingAddedExerciseEntryId",
            "var sessionExerciseToConfigure",
            "var sessionExerciseToConfigureSearchQuery",
            "var isSessionCustomExerciseDialogVisible"
        )

        forbiddenStateDeclarations.forEach { declaration ->
            assertFalse("$declaration belongs in StrengthExerciseChangeUiState.kt", routedSessionScreen.contains(declaration))
        }
        assertTrue(exerciseChangeUiState.contains("data class StrengthExerciseChangeUiState"))
        assertTrue(exerciseChangeUiState.contains("fun beginAddedExercise"))
        assertTrue(exerciseChangeUiState.contains("fun beginExistingExerciseChange"))
        assertTrue(routedSessionScreen.contains("var exerciseChangeUiState"))
    }
}

package com.lighthousepark.intervalsgym.strength.ui

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.lighthousepark.intervalsgym.app.PREFS_NAME
import com.lighthousepark.intervalsgym.app.ROUTE_STRENGTH_SESSION
import com.lighthousepark.intervalsgym.data.IntervalsUseCaseFactory
import com.lighthousepark.intervalsgym.data.SessionHistoryQueryUseCase
import com.lighthousepark.intervalsgym.strength.ActiveStrengthSession
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthExercise
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.customStrengthExercise
import com.lighthousepark.intervalsgym.strength.defaultStrengthSetRecord
import com.lighthousepark.intervalsgym.strength.nextIncompleteSet
import com.lighthousepark.intervalsgym.strength.recentMatchingStrengthExerciseHistory
import com.lighthousepark.intervalsgym.strength.strengthRoutineUpdateAvailability
import com.lighthousepark.intervalsgym.strength.withRecords
import com.lighthousepark.intervalsgym.training.TrainingItem
import kotlinx.coroutines.launch

/**
 * Route owner for [ROUTE_STRENGTH_SESSION].
 * This is the single entry point for strength routine preview, ongoing workout list, set execution, rest timer, and finish/upload state.
 * UI tests: StrengthSessionUiTest.readyScreen_startButtonInvokesStart,
 * readyScreen_editButtonInvokesEditRoutine, readyScreen_entryRowTogglesSetDetails,
 * strengthSessionTopBar_readyActionsInvokeCallbacks,
 * strengthSessionTopBar_ongoingListShowsTimerInsteadOfBackAndHidesReadyActions.
 */
@Composable
internal fun StrengthSessionScreen(
    apiKey: String,
    routine: StrengthWorkoutRoutine?,
    calendarRoutineItem: TrainingItem?,
    isRoutineEditable: Boolean,
    activeSession: ActiveStrengthSession?,
    startImmediately: Boolean,
    onImmediateStartConsumed: () -> Unit,
    onSessionChange: (ActiveStrengthSession?) -> Unit,
    onSessionFinished: (CompletedStrengthSession?, Boolean) -> Unit,
    onHistoryClick: (StrengthWorkoutRoutine) -> Unit,
    onEditRoutine: (StrengthWorkoutRoutine) -> Unit,
    onCalendarRoutineDeleted: (TrainingItem) -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val sessionHistoryQuery = remember(prefs) { SessionHistoryQueryUseCase(prefs) }
    val completedStrengthHistory = remember(sessionHistoryQuery) { sessionHistoryQuery.loadStrengthHistory() }
    val intervalsUseCaseFactory = remember(apiKey) { IntervalsUseCaseFactory(apiKey) }
    val strengthSessionSync = remember(intervalsUseCaseFactory, prefs) {
        intervalsUseCaseFactory.strengthSessionSync(prefs)
    }
    val calendarRoutineSync = remember(intervalsUseCaseFactory, prefs) {
        intervalsUseCaseFactory.calendarRoutineSync(prefs)
    }
    val now = remember(activeSession?.routineId) { System.currentTimeMillis() }
    val shouldStartImmediately = activeSession == null && startImmediately
    val restoredRestUiState = remember(activeSession?.routineId) {
        StrengthRestUiState.restored(activeSession, now)
    }
    var progressUiState by remember(activeSession?.routineId, routine?.id) {
        mutableStateOf(
            StrengthSessionProgressUiState.restored(
                activeSession = activeSession,
                shouldStartImmediately = shouldStartImmediately,
                nowMillis = now
            )
        )
    }
    val hasStarted = progressUiState.hasStarted
    val sessionStartedAtMillis = progressUiState.sessionStartedAtMillis
    val sessionElapsedSeconds = progressUiState.sessionElapsedSeconds
    var interactionState by remember(activeSession?.routineId, routine?.id) {
        mutableStateOf(
            restoredStrengthSessionInteractionState(
                activeSession = activeSession,
                routineEntries = routine?.entries.orEmpty(),
                shouldStartImmediately = shouldStartImmediately,
                nowMillis = now,
                restoredRestUiState = restoredRestUiState
            )
        )
    }
    val entries = interactionState.entries
    val navigationUiState = interactionState.navigationUiState
    val isSetScreenVisible = navigationUiState.isSetScreenVisible
    val currentExerciseIndex = navigationUiState.currentExerciseIndex
    val currentSetIndex = navigationUiState.currentSetIndex
    val pendingExerciseIndex = navigationUiState.pendingExerciseIndex
    val pendingSetIndex = navigationUiState.pendingSetIndex
    var exerciseChangeUiState by remember(routine?.id) {
        mutableStateOf(StrengthExerciseChangeUiState.inactive())
    }
    val isChangingCurrentExercise = exerciseChangeUiState.isChangingCurrentExercise
    val isCurrentExerciseTypeDialogVisible = exerciseChangeUiState.isCurrentExerciseTypeDialogVisible
    val pendingAddedExerciseEntryId = exerciseChangeUiState.pendingAddedExerciseEntryId
    val sessionExerciseToConfigure = exerciseChangeUiState.exerciseToConfigure
    val sessionExerciseToConfigureSearchQuery = exerciseChangeUiState.exerciseToConfigureSearchQuery
    val isSessionCustomExerciseDialogVisible = exerciseChangeUiState.isCustomExerciseDialogVisible
    val restUiState = interactionState.restUiState
    val setEvents = interactionState.setEvents
    val restEvents = interactionState.restEvents
    var finishUiState by rememberSaveable(routine?.id, saver = strengthSessionFinishUiStateSaver()) {
        mutableStateOf(StrengthSessionFinishUiState())
    }

    StrengthStartImmediatelyEffect(
        shouldStartImmediately = shouldStartImmediately,
        onImmediateStartConsumed = onImmediateStartConsumed
    )
    StrengthReadyRoutineEntriesEffect(
        routineEntries = routine?.entries.orEmpty(),
        hasStarted = hasStarted,
        activeSessionRoutineId = activeSession?.routineId,
        onEntriesChange = { interactionState = interactionState.copy(entries = it) }
    )
    StrengthExerciseChangeFocusEffect(
        isChangingCurrentExercise = isChangingCurrentExercise,
        pendingAddedEntryId = pendingAddedExerciseEntryId,
        entries = entries,
        navigationUiState = navigationUiState,
        onNavigationUiStateChange = {
            interactionState = interactionState.copy(navigationUiState = it)
        }
    )

    fun clearActiveRestUi() {
        interactionState = interactionState.copy(restUiState = StrengthRestUiState.inactive())
        stopStrengthRestOverlay(context)
    }

    fun currentRuntimeSnapshot(): StrengthSessionRuntimeSnapshot {
        return StrengthSessionRuntimeSnapshot(
            routine = routine,
            entries = entries,
            hasStarted = hasStarted,
            sessionStartedAtMillis = sessionStartedAtMillis,
            navigationUiState = navigationUiState,
            restUiState = restUiState,
            setEvents = setEvents,
            restEvents = restEvents,
            finishUiState = finishUiState
        )
    }

    fun currentInteractionState(): StrengthSessionInteractionState {
        return interactionState
    }

    fun applySessionTransition(transition: StrengthSessionStateTransition) {
        interactionState = transition.state
        transition.dispatchRestOverlaySideEffects(context)
    }

    fun applyExerciseActionResult(result: StrengthSessionExerciseActionResult) {
        result.transition?.let(::applySessionTransition)
        result.navigationUiState?.let {
            interactionState = interactionState.copy(navigationUiState = it)
        }
        result.exerciseChangeUiState?.let { exerciseChangeUiState = it }
    }

    fun replaceSessionEntries(nextEntries: List<StrengthRoutineEntry>) {
        applySessionTransition(
            currentInteractionState().withEntriesReplaced(
                nextEntries = nextEntries,
                nowMillis = System.currentTimeMillis()
            )
        )
    }

    fun updateEntry(entry: StrengthRoutineEntry) {
        replaceSessionEntries(entries.map { if (it.id == entry.id) entry else it })
    }

    fun updateCurrentEntry(entry: StrengthRoutineEntry) {
        updateEntry(entry)
        if (
            entry.id == entries.getOrNull(navigationUiState.currentExerciseIndex)?.id &&
            navigationUiState.currentSetIndex >= entry.records.size
        ) {
            interactionState = interactionState.copy(
                navigationUiState = navigationUiState.clampCurrentSetForEntry(entry)
            )
        }
    }

    fun applyCurrentExerciseChange(exercise: StrengthExercise, equipment: String, variation: String) {
        currentInteractionState()
            .withConfiguredExercise(
                exerciseChangeUiState = exerciseChangeUiState,
                completedStrengthHistory = completedStrengthHistory,
                exercise = exercise,
                equipment = equipment,
                variation = variation,
                nowMillis = System.currentTimeMillis()
            )
            ?.let(::applyExerciseActionResult)
    }

    fun deleteCalendarRoutine() {
        val deleteAction = planStrengthSessionCalendarRoutineDelete(calendarRoutineItem) ?: return
        scope.launch {
            finishUiState = finishUiState.withCalendarRoutineDeleteStarted()
            try {
                deleteAction.delete(calendarRoutineSync)
                onCalendarRoutineDeleted(deleteAction.targetRoutine)
            } catch (error: Exception) {
                finishUiState = finishUiState.withCalendarRoutineDeleteFailed(error.message)
            } finally {
                finishUiState = finishUiState.withCalendarRoutineDeleteFinished()
            }
        }
    }

    fun closeActiveRestEvent(reason: String) {
        applySessionTransition(
            currentInteractionState().withClosedActiveRest(
                endedAtMillis = System.currentTimeMillis(),
                reason = reason
            )
        )
    }

    fun moveToPendingSet(reason: String = "finished") {
        applySessionTransition(
            currentInteractionState().movedToPendingSet(
                endedAtMillis = System.currentTimeMillis(),
                reason = reason
            )
        )
    }

    fun openExerciseSet(exerciseIndex: Int) {
        applyExerciseActionResult(
            currentInteractionState().withOpenedExerciseSet(
                exerciseChangeUiState = exerciseChangeUiState,
                exerciseIndex = exerciseIndex
            )
        )
    }

    fun addExerciseToSession() {
        applyExerciseActionResult(
            currentInteractionState().withAddedExercise(
                exerciseChangeUiState = exerciseChangeUiState,
                nowMillis = System.currentTimeMillis()
            )
        )
    }

    fun replaceExerciseOrderInSession(nextEntries: List<StrengthRoutineEntry>) {
        currentInteractionState()
            .withReorderedExercises(
                nextEntries = nextEntries,
                nowMillis = System.currentTimeMillis()
            )
            ?.let(::applySessionTransition)
    }

    fun setRestSeconds(seconds: Int) {
        applySessionTransition(
            currentInteractionState().withUpdatedRestSeconds(
                seconds = seconds,
                nowMillis = System.currentTimeMillis()
            )
        )
    }

    fun adjustRestSeconds(deltaSeconds: Int) {
        setRestSeconds((restUiState.remainingSeconds ?: 0) + deltaSeconds)
    }

    fun completeCurrentSet() {
        currentInteractionState()
            .withCompletedCurrentSet(completedAtMillis = System.currentTimeMillis())
            ?.let(::applySessionTransition)
    }

    fun currentResultSnapshot(): StrengthSessionResultSnapshot {
        return currentRuntimeSnapshot().toResultSnapshot()
    }

    fun currentStrengthSessionEndedAtMillis(): Long {
        return currentResultSnapshot().endedAtMillis()
    }

    fun currentActiveSessionSnapshot(): StrengthActiveSessionSnapshot {
        return currentRuntimeSnapshot().toActiveSessionSnapshot()
    }

    fun persistLiveStrengthSessionResult(
        endedAtMillis: Long = currentStrengthSessionEndedAtMillis(),
        endReason: String = STRENGTH_RESULT_END_REASON_LIVE_UPDATE,
    ): CompletedStrengthSession? {
        return currentResultSnapshot().saveLiveResult(
            syncUseCase = strengthSessionSync,
            endedAtMillis = endedAtMillis,
            endReason = endReason
        )
    }

    fun deleteCurrentLiveResult(
        endedAtMillis: Long,
        endReason: String,
    ) {
        currentResultSnapshot().deleteLiveResult(
            syncUseCase = strengthSessionSync,
            endedAtMillis = endedAtMillis,
            endReason = endReason
        )
    }

    fun finishWorkout() {
        val finishAction = currentResultSnapshot().planFinishedStrengthSession(
            syncUseCase = strengthSessionSync,
            canUploadToIntervals = apiKey.isNotBlank(),
            endedAtMillis = currentStrengthSessionEndedAtMillis()
        )
        when (finishAction) {
            is SaveFinishedStrengthSessionLocally -> {
                val persistedSession = finishAction.saveLocalResult(strengthSessionSync)
                stopStrengthSessionRuntime(context)
                onSessionFinished(persistedSession, finishAction.shouldApplyToRoutine)
            }
            is UploadFinishedStrengthSession -> scope.launch {
                finishUiState = finishUiState.withUploadStarted()
                try {
                    val uploadedSession = finishAction.uploadResult(strengthSessionSync)
                    finishUiState = finishUiState.withUploadSucceeded()
                    stopStrengthSessionRuntime(context)
                    onSessionFinished(uploadedSession, finishAction.shouldApplyToRoutine)
                } catch (error: Exception) {
                    finishUiState = finishUiState.withUploadFailed(error.message)
                }
            }
        }
    }

    StrengthLiveResultPersistenceEffect(
        hasStarted,
        routineId = routine?.id,
        entries = entries,
        setEvents = setEvents,
        restEvents = restEvents,
        activeRestEventId = restUiState.activeRestEventId,
        sessionStartedAtMillis = sessionStartedAtMillis,
        finishRpe = finishUiState.finishRpe,
        onPersistLiveResult = ::persistLiveStrengthSessionResult
    )

    fun discardWorkout() {
        deleteCurrentLiveResult(
            endedAtMillis = currentStrengthSessionEndedAtMillis(),
            endReason = STRENGTH_RESULT_END_REASON_DISCARDED
        )
        closeActiveRestEvent(STRENGTH_RESULT_END_REASON_DISCARDED)
        clearActiveRestUi()
        stopStrengthSessionRuntime(context)
        onSessionFinished(null, false)
    }

    StrengthActiveSessionPersistenceEffect(
        routineId = routine?.id,
        routineName = routine?.name,
        hasStarted = hasStarted,
        sessionStartedAtMillis = sessionStartedAtMillis,
        isSetScreenVisible = isSetScreenVisible,
        entries = entries,
        currentExerciseIndex = currentExerciseIndex,
        currentSetIndex = currentSetIndex,
        pendingExerciseIndex = pendingExerciseIndex,
        pendingSetIndex = pendingSetIndex,
        restUiState = restUiState,
        setEvents = setEvents,
        restEvents = restEvents,
        onPersistActiveSession = {
            currentActiveSessionSnapshot().toActiveSession()?.let(onSessionChange)
        }
    )

    StrengthWorkoutStatusServiceEffect(
        context = context,
        hasStarted = hasStarted,
        sessionStartedAtMillis = sessionStartedAtMillis,
        routineName = routine?.name,
        activeExerciseTitle = entries.getOrNull(currentExerciseIndex)?.title.orEmpty(),
        restUiState = restUiState
    )

    fun handleBack() {
        when {
            isCurrentExerciseTypeDialogVisible -> {
                exerciseChangeUiState = exerciseChangeUiState.hideCurrentExerciseTypeDialog()
            }
            sessionExerciseToConfigure != null -> {
                exerciseChangeUiState = exerciseChangeUiState.dismissExerciseConfig()
            }
            isSessionCustomExerciseDialogVisible -> {
                exerciseChangeUiState = exerciseChangeUiState.dismissCustomExerciseDialog()
            }
            isChangingCurrentExercise -> {
                applyExerciseActionResult(
                    currentInteractionState().withCanceledExerciseChange(
                        exerciseChangeUiState = exerciseChangeUiState,
                        nowMillis = System.currentTimeMillis()
                    )
                )
            }
            hasStarted && isSetScreenVisible -> {
                interactionState = interactionState.copy(
                    navigationUiState = navigationUiState.withSetScreenVisible(false)
                )
            }
            hasStarted -> onBack()
            else -> onBack()
        }
    }

    StrengthSessionBackHandler(
        enabled = isChangingCurrentExercise || hasStarted,
        onBack = ::handleBack
    )
    StrengthSessionElapsedTickerEffect(
        hasStarted = hasStarted,
        sessionStartedAtMillis = sessionStartedAtMillis,
        onElapsedSecondsChange = { progressUiState = progressUiState.withElapsedSeconds(it) }
    )

    StrengthRestCountdownEffect(
        context = context,
        remainingSeconds = restUiState.remainingSeconds,
        endAtMillis = restUiState.endAtMillis,
        onRemainingSecondsChange = {
            interactionState = interactionState.copy(
                restUiState = restUiState.withRemainingSeconds(it)
            )
        },
        onRestFinished = { moveToPendingSet() }
    )

    val activeSetOverlayTitle = strengthSetCompleteOverlayTitle(
        entries = entries,
        currentExerciseIndex = currentExerciseIndex,
        currentSetIndex = currentSetIndex
    )
    val isResting = restUiState.remainingSeconds != null
    val currentEntry = entries.getOrNull(currentExerciseIndex)
    val routineUpdateAvailability = strengthRoutineUpdateAvailability(
        routineEntries = routine?.entries.orEmpty(),
        workoutEntries = entries
    )
    val currentExerciseHistory = currentEntry?.let { entry ->
        completedStrengthHistory.recentMatchingStrengthExerciseHistory(
            exercise = entry.exercise,
            equipment = entry.equipment,
            variation = entry.variation
        )
    }.orEmpty()

    StrengthFloatingOverlayEffect(
        context = context,
        hasStarted = hasStarted,
        isSetScreenVisible = isSetScreenVisible,
        isChangingCurrentExercise = isChangingCurrentExercise,
        restUiState = restUiState,
        activeSetOverlayTitle = activeSetOverlayTitle
    )

    StrengthShowRestSheetOverlayRequestEffect(
        isRestTimerActive = isResting,
        onShowRestSheet = {
            interactionState = interactionState.copy(
                restUiState = restUiState.withSheetVisible(true)
            )
        }
    )

    StrengthSetCompleteOverlayRequestEffect(
        canCompleteSet = hasStarted &&
            isSetScreenVisible &&
            !isChangingCurrentExercise &&
            !isResting,
        onCompleteSetRequest = {
            currentInteractionState()
                .withCompletedCurrentSetFromOverlay(completedAtMillis = System.currentTimeMillis())
                ?.let(::applySessionTransition)
        }
    )

    StrengthSessionDialogs(
        restUiState = restUiState,
        entries = entries,
        currentExerciseIndex = currentExerciseIndex,
        isCurrentExerciseTypeDialogVisible = isCurrentExerciseTypeDialogVisible,
        sessionExerciseToConfigure = sessionExerciseToConfigure,
        sessionExerciseToConfigureSearchQuery = sessionExerciseToConfigureSearchQuery,
        isSessionCustomExerciseDialogVisible = isSessionCustomExerciseDialogVisible,
        finishUiState = finishUiState,
        routineUpdateAvailability = routineUpdateAvailability,
        apiKey = apiKey,
        calendarRoutineItem = calendarRoutineItem,
        onAdjustRestSeconds = ::adjustRestSeconds,
        onSetRestSeconds = ::setRestSeconds,
        onDismissRestSheet = {
            interactionState = interactionState.copy(
                restUiState = restUiState.withSheetVisible(false)
            )
        },
        onStopRest = { moveToPendingSet("stopped") },
        onBeginExistingExerciseChange = {
            exerciseChangeUiState = exerciseChangeUiState.beginExistingExerciseChange()
        },
        onDismissCurrentExerciseTypeDialog = {
            exerciseChangeUiState = exerciseChangeUiState.hideCurrentExerciseTypeDialog()
        },
        onCurrentExerciseTypeDone = { entry, equipment, variation ->
            exerciseChangeUiState = exerciseChangeUiState.hideCurrentExerciseTypeDialog()
            updateCurrentEntry(
                entry.copy(
                    equipment = equipment,
                    variation = variation
                )
            )
        },
        onDismissExerciseConfig = {
            exerciseChangeUiState = exerciseChangeUiState.dismissExerciseConfig()
        },
        onExerciseConfigDone = { exercise, equipment, variation ->
            applyCurrentExerciseChange(exercise, equipment, variation)
        },
        onDismissCustomExerciseDialog = {
            exerciseChangeUiState = exerciseChangeUiState.dismissCustomExerciseDialog()
        },
        onAddCustomExercise = { name ->
            exerciseChangeUiState = exerciseChangeUiState.addCustomExercise(customStrengthExercise(name))
        },
        onRoutineUpdateSelectionChange = {
            finishUiState = finishUiState.withRoutineUpdateSelection(it)
        },
        onFinishRpeChange = { finishUiState = finishUiState.withFinishRpe(it) },
        onDismissFinishChoiceDialog = { finishUiState = finishUiState.dismissFinishChoiceDialog() },
        onSaveFinishedWorkout = {
            finishUiState = finishUiState.dismissFinishChoiceDialog()
            finishWorkout()
        },
        onDiscardFinishedWorkout = {
            finishUiState = finishUiState.dismissFinishChoiceDialog()
            discardWorkout()
        },
        onConfirmCalendarRoutineDelete = {
            finishUiState = finishUiState.dismissCalendarRoutineDeleteConfirm()
            deleteCalendarRoutine()
        },
        onCancelCalendarRoutineDelete = {
            finishUiState = finishUiState.dismissCalendarRoutineDeleteConfirm()
        }
    )

    StrengthSessionScaffold(
        routineName = routine?.name,
        hasStarted = hasStarted,
        isChangingCurrentExercise = isChangingCurrentExercise,
        isSetScreenVisible = isSetScreenVisible,
        sessionElapsedSeconds = sessionElapsedSeconds,
        showCalendarRoutineDelete = calendarRoutineItem?.isRoutine == true,
        isDeletingCalendarRoutine = finishUiState.isDeletingCalendarRoutine,
        showRestTimerFloatingChip = restUiState.shouldShowFloatingChip(
            hasStarted = hasStarted,
            isChangingCurrentExercise = isChangingCurrentExercise,
            canDrawSystemOverlay = Settings.canDrawOverlays(context)
        ),
        restRemainingSeconds = restUiState.remainingSeconds ?: 0,
        entries = entries,
        currentExerciseIndex = currentExerciseIndex,
        currentSetIndex = currentSetIndex,
        isUploading = finishUiState.isUploading,
        onBack = ::handleBack,
        onCalendarRoutineDelete = {
            finishUiState = finishUiState.showCalendarRoutineDeleteConfirm()
        },
        onHistoryClick = { routine?.let(onHistoryClick) },
        onShowRestTimer = {
            interactionState = interactionState.copy(
                restUiState = restUiState.withSheetVisible(true)
            )
        },
        onCompleteSet = ::completeCurrentSet,
        onResumeCurrentExercise = { openExerciseSet(currentExerciseIndex) },
        onFinish = {
            finishUiState = finishUiState.showFinishChoiceDialog(routineUpdateAvailability)
        }
    ) { innerPadding ->
        StrengthSessionContentHost(
            routine = routine,
            entries = entries,
            hasStarted = hasStarted,
            isRoutineEditable = isRoutineEditable,
            isChangingCurrentExercise = isChangingCurrentExercise,
            isSetScreenVisible = isSetScreenVisible,
            isCurrentExerciseTypeDialogVisible = isCurrentExerciseTypeDialogVisible,
            currentExerciseIndex = currentExerciseIndex,
            currentSetIndex = currentSetIndex,
            currentEntry = currentEntry,
            recentHistory = currentExerciseHistory,
            finishUiState = finishUiState,
            innerPadding = innerPadding,
            onStart = {
                progressUiState = progressUiState.started(System.currentTimeMillis())
                val nextSet = nextIncompleteSet(entries, 0, -1)
                val nextNavigationUiState = if (nextSet != null) {
                    navigationUiState.openSet(nextSet.first, nextSet.second)
                } else {
                    navigationUiState.withSetScreenVisible(true)
                }
                interactionState = interactionState.copy(navigationUiState = nextNavigationUiState)
            },
            onEditRoutine = { routine?.let(onEditRoutine) },
            onAddCustomExercise = {
                exerciseChangeUiState = exerciseChangeUiState.showCustomExerciseDialog()
            },
            onExerciseSelected = { exercise, searchQuery ->
                exerciseChangeUiState = exerciseChangeUiState.selectExerciseToConfigure(
                    exercise = exercise,
                    searchQuery = searchQuery
                )
            },
            onSetScreenBack = {
                interactionState = interactionState.copy(
                    navigationUiState = navigationUiState.withSetScreenVisible(false)
                )
            },
            onCurrentExerciseClick = {
                exerciseChangeUiState = exerciseChangeUiState.showCurrentExerciseTypeDialog(
                    canShow = currentEntry != null
                )
            },
            onCurrentEntryChange = ::updateCurrentEntry,
            onAddSet = {
                currentEntry?.let { entry ->
                    val nextEntry = entry.withRecords(entry.records + defaultStrengthSetRecord(entry))
                    updateEntry(nextEntry)
                    interactionState = interactionState.copy(
                        navigationUiState = navigationUiState.withCurrentSetIndex(nextEntry.records.lastIndex)
                    )
                }
            },
            onOngoingExerciseClick = ::openExerciseSet,
            onAddExercise = ::addExerciseToSession,
            onEntriesChange = { nextEntries ->
                replaceExerciseOrderInSession(nextEntries)
            }
        )
    }
}

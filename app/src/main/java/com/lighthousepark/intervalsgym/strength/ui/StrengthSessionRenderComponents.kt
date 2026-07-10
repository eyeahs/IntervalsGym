package com.lighthousepark.intervalsgym.strength.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lighthousepark.intervalsgym.strength.CompletedStrengthExerciseHistory
import com.lighthousepark.intervalsgym.strength.StrengthExercise
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.allSetsCompleted
import com.lighthousepark.intervalsgym.workout.ui.EmptyView

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun StrengthSessionScaffold(
    routineName: String?,
    hasStarted: Boolean,
    isChangingCurrentExercise: Boolean,
    isSetScreenVisible: Boolean,
    sessionElapsedSeconds: Int,
    showCalendarRoutineDelete: Boolean,
    isDeletingCalendarRoutine: Boolean,
    showRestTimerFloatingChip: Boolean,
    restRemainingSeconds: Int,
    entries: List<StrengthRoutineEntry>,
    currentExerciseIndex: Int,
    currentSetIndex: Int,
    supersetSelectionUiState: StrengthSupersetSelectionUiState,
    isUploading: Boolean,
    onBack: () -> Unit,
    onCalendarRoutineDelete: () -> Unit,
    onHistoryClick: () -> Unit,
    onShowRestTimer: () -> Unit,
    onCompleteSet: () -> Unit,
    onResumeCurrentExercise: () -> Unit,
    onFinish: () -> Unit,
    onGroupSelectedSuperset: () -> Unit,
    onClearSelectedSuperset: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val hasRoutine = routineName != null
    val isOngoingExerciseListVisible = hasStarted && !isChangingCurrentExercise && !isSetScreenVisible
    Scaffold(
        topBar = {
            StrengthSessionTopBar(
                title = if (isChangingCurrentExercise) "운동 목록" else routineName ?: "웨이트 수행",
                isWorkoutActive = hasStarted && !isOngoingExerciseListVisible,
                elapsedSeconds = sessionElapsedSeconds,
                showTimerBadgeAsNavigation = isOngoingExerciseListVisible,
                showReadyActions = !hasStarted && hasRoutine && !isChangingCurrentExercise,
                showCalendarRoutineDelete = showCalendarRoutineDelete,
                isDeletingCalendarRoutine = isDeletingCalendarRoutine,
                onBack = onBack,
                onCalendarRoutineDelete = onCalendarRoutineDelete,
                onHistoryClick = onHistoryClick
            )
        },
        floatingActionButton = {
            if (showRestTimerFloatingChip) {
                RestTimerFloatingChip(
                    remainingSeconds = restRemainingSeconds,
                    onClick = onShowRestTimer
                )
            }
        },
        bottomBar = {
            if (hasStarted && hasRoutine && !isChangingCurrentExercise && isSetScreenVisible) {
                StrengthSetBottomBar(
                    allDone = entries.allSetsCompleted(),
                    currentLabel = strengthSetBottomBarCurrentLabel(
                        entries = entries,
                        currentExerciseIndex = currentExerciseIndex,
                        currentSetIndex = currentSetIndex
                    ),
                    onCompleteSet = onCompleteSet,
                    isUploading = isUploading
                )
            } else if (hasStarted && hasRoutine && !isChangingCurrentExercise) {
                if (supersetSelectionUiState.isSelectionMode) {
                    StrengthSupersetSelectionBottomBar(
                        canGroup = supersetSelectionUiState.canGroup(entries),
                        canClear = supersetSelectionUiState.canClear(entries),
                        onGroup = onGroupSelectedSuperset,
                        onClear = onClearSelectedSuperset,
                        onCancel = supersetSelectionUiState::close,
                        modifier = Modifier.navigationBarsPadding()
                    )
                } else {
                    StrengthSessionOngoingBottomBar(
                        activeExerciseLabel = entries.getOrNull(currentExerciseIndex)?.title.orEmpty(),
                        isUploading = isUploading,
                        onResumeExercise = onResumeCurrentExercise,
                        onFinish = onFinish
                    )
                }
            }
        },
        content = content
    )
}

private fun strengthSetBottomBarCurrentLabel(
    entries: List<StrengthRoutineEntry>,
    currentExerciseIndex: Int,
    currentSetIndex: Int,
): String {
    return entries.getOrNull(currentExerciseIndex)?.let { entry ->
        val nextSet = entry.records.indexOfFirst { !it.completed }
            .takeIf { it >= 0 }
            ?: currentSetIndex
        "Set ${nextSet + 1} · ${entry.title}"
    }.orEmpty()
}

@Composable
internal fun StrengthSessionContentHost(
    routine: StrengthWorkoutRoutine?,
    entries: List<StrengthRoutineEntry>,
    hasStarted: Boolean,
    isRoutineEditable: Boolean,
    isChangingCurrentExercise: Boolean,
    isSetScreenVisible: Boolean,
    isCurrentExerciseTypeDialogVisible: Boolean,
    currentExerciseIndex: Int,
    currentSetIndex: Int,
    supersetSelectionUiState: StrengthSupersetSelectionUiState,
    currentEntry: StrengthRoutineEntry?,
    resettableCompletedSetRecordId: Int?,
    recentHistory: List<CompletedStrengthExerciseHistory>,
    finishUiState: StrengthSessionFinishUiState,
    innerPadding: PaddingValues,
    onStart: () -> Unit,
    onEditRoutine: () -> Unit,
    onAddCustomExercise: () -> Unit,
    onExerciseSelected: (StrengthExercise, String) -> Unit,
    onSetScreenBack: () -> Unit,
    onCurrentExerciseClick: () -> Unit,
    onCurrentEntryChange: (StrengthRoutineEntry) -> Unit,
    onAddSet: () -> Unit,
    onOngoingExerciseClick: (Int) -> Unit,
    onAddExercise: () -> Unit,
    onEntriesChange: (List<StrengthRoutineEntry>) -> Unit,
) {
    if (routine == null) {
        EmptyView(message = "선택된 웨이트 Routine이 없습니다.")
        return
    }

    if (!hasStarted) {
        StrengthSessionReadyScreen(
            routine = routine,
            entries = entries,
            modifier = Modifier.padding(innerPadding),
            onStart = onStart,
            onEditRoutine = if (isRoutineEditable) onEditRoutine else null
        )
    } else if (isChangingCurrentExercise) {
        StrengthExerciseListScreen(
            modifier = Modifier.padding(innerPadding),
            onAddCustomExercise = onAddCustomExercise,
            onExerciseSelected = onExerciseSelected
        )
    } else if (isSetScreenVisible) {
        StrengthSessionBackHandler(
            enabled = !isCurrentExerciseTypeDialogVisible,
            onBack = onSetScreenBack
        )
        StrengthSetExecutionScreen(
            entry = currentEntry,
            currentSetIndex = currentSetIndex,
            resettableCompletedSetRecordId = resettableCompletedSetRecordId,
            recentHistory = recentHistory,
            modifier = Modifier.padding(innerPadding),
            onExerciseClick = onCurrentExerciseClick,
            onEntryChange = onCurrentEntryChange,
            onAddSet = onAddSet
        )
    } else {
        StrengthSessionOngoingRoutineScreen(
            routine = routine,
            entries = entries,
            currentExerciseIndex = currentExerciseIndex,
            uploadMessage = finishUiState.uploadMessage,
            uploadError = finishUiState.uploadError,
            supersetSelectionUiState = supersetSelectionUiState,
            modifier = Modifier.padding(innerPadding),
            onExerciseClick = onOngoingExerciseClick,
            onAddExercise = onAddExercise,
            onEntriesChange = onEntriesChange
        )
    }
}

package com.lighthousepark.intervalsgym.strength.ui

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.lighthousepark.intervalsgym.app.PREFS_NAME
import com.lighthousepark.intervalsgym.app.ROUTE_STRENGTH_ROUTINE_EDIT
import com.lighthousepark.intervalsgym.data.SessionHistoryQueryUseCase
import com.lighthousepark.intervalsgym.data.toJsonString
import com.lighthousepark.intervalsgym.data.toStrengthWorkoutRoutines
import com.lighthousepark.intervalsgym.strength.StrengthExercise
import com.lighthousepark.intervalsgym.strength.StrengthRoutineEntry
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.customStrengthExercise
import com.lighthousepark.intervalsgym.strength.supersetGroupLabels

/**
 * Route owner for [ROUTE_STRENGTH_ROUTINE_EDIT].
 * This owns strength routine creation/editing, exercise ordering, superset grouping, and nested exercise selection.
 * UI tests: StrengthRoutineEditUiTest.routineDeleteDialog_confirmInvokesDeleteCallback,
 * routineDeleteDialog_cancelKeepsRoutine, unsavedBackDialog_cancelsSavesAndDiscardsChanges.
 */
@Composable
internal fun StrengthRoutineEditScreen(
    routine: StrengthWorkoutRoutine?,
    onSave: (StrengthWorkoutRoutine) -> Unit,
    onDelete: (StrengthWorkoutRoutine) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val sessionHistoryQuery = remember(prefs) { SessionHistoryQueryUseCase(prefs) }
    val completedStrengthHistory = remember(routine?.id, sessionHistoryQuery) {
        sessionHistoryQuery.loadStrengthHistory()
    }
    var routineName by rememberSaveable(routine?.id) { mutableStateOf(routine?.name.orEmpty()) }
    var entries by rememberSaveable(routine?.id, saver = strengthRoutineEntriesStateSaver()) {
        mutableStateOf(routine?.entries.orEmpty())
    }
    var pendingAddedEntry by rememberSaveable(routine?.id, saver = strengthRoutineEntryStateSaver()) {
        mutableStateOf<StrengthRoutineEntry?>(null)
    }
    var selectedEntryId by rememberSaveable(routine?.id) { mutableStateOf<Int?>(null) }
    val supersetSelectionUiState = rememberStrengthSupersetSelectionUiState(routine?.id)
    val isSupersetSelectionMode = supersetSelectionUiState.isSelectionMode
    val selectedSupersetEntryIds = supersetSelectionUiState.selectedEntryIds
    var pendingDeleteEntryIds by remember(routine?.id) { mutableStateOf(emptySet<Int>()) }
    var isExerciseListVisible by rememberSaveable(routine?.id) { mutableStateOf(false) }
    var shouldReturnToExerciseListFromDetail by rememberSaveable(routine?.id) { mutableStateOf(false) }
    var isChangingSelectedEntryExercise by rememberSaveable(routine?.id, selectedEntryId) { mutableStateOf(false) }
    var exerciseToConfigure by remember { mutableStateOf<StrengthExercise?>(null) }
    var exerciseToConfigureSearchQuery by remember { mutableStateOf("") }
    var isCustomExerciseDialogVisible by remember { mutableStateOf(false) }
    var isRoutineDeleteDialogVisible by remember(routine?.id) { mutableStateOf(false) }
    var isUnsavedBackDialogVisible by remember(routine?.id) { mutableStateOf(false) }
    val selectedEntry = pendingAddedEntry ?: entries.firstOrNull { it.id == selectedEntryId }
    val supersetLabels = remember(entries) { entries.supersetGroupLabels() }
    val originalRoutineSnapshot = remember(routine?.id) {
        originalStrengthRoutineEditSnapshot(routine)
    }
    var entryDragUiState by remember { mutableStateOf(StrengthRoutineEntryDragUiState()) }

    fun updateEntry(entry: StrengthRoutineEntry) {
        if (pendingAddedEntry?.id == entry.id) {
            pendingAddedEntry = entry
        } else {
            entries = entries.map { if (it.id == entry.id) entry else it }
        }
    }

    fun currentEditableRoutine(): StrengthWorkoutRoutine {
        return editableStrengthRoutine(
            routine = routine,
            routineName = routineName,
            entries = entries,
            pendingDeleteEntryIds = pendingDeleteEntryIds
        )
    }

    fun saveCurrentRoutine() {
        onSave(currentEditableRoutine())
    }

    fun startEntryDrag(entryId: Int) {
        entryDragUiState = entryDragUiState.startDrag(
            entries = entries,
            entryId = entryId
        )
    }

    fun updateEntryDrag(entryId: Int, deltaY: Float) {
        val update = entryDragUiState.moveDrag(
            entries = entries,
            entryId = entryId,
            deltaY = deltaY
        )
        entryDragUiState = update.state
        entries = update.entries
    }

    fun endEntryDrag() {
        entryDragUiState = entryDragUiState.endDrag()
    }

    fun closeSupersetSelectionMode() {
        supersetSelectionUiState.close()
    }

    fun groupSelectedAsSuperset() {
        supersetSelectionUiState.groupedEntries(entries)?.let { entries = it }
    }

    fun clearSelectedSupersetGroups() {
        supersetSelectionUiState.clearedEntries(entries)?.let { entries = it }
    }

    fun applyEntryDeleteState(nextState: StrengthRoutineEntryDeleteState) {
        entries = nextState.entries
        pendingDeleteEntryIds = nextState.pendingDeleteEntryIds
        supersetSelectionUiState.reconcile(nextState.entries)
        selectedEntryId = nextState.selectedEntryId
    }

    fun currentEntryDeleteState(): StrengthRoutineEntryDeleteState {
        return StrengthRoutineEntryDeleteState(
            entries = entries,
            pendingDeleteEntryIds = pendingDeleteEntryIds,
            selectedSupersetEntryIds = selectedSupersetEntryIds,
            selectedEntryId = selectedEntryId
        )
    }

    fun requestEntryDelete(entryId: Int) {
        applyEntryDeleteState(currentEntryDeleteState().withDeleteRequested(entryId))
    }

    fun restoreEntryDelete(entryId: Int) {
        applyEntryDeleteState(currentEntryDeleteState().withDeleteRestored(entryId))
    }

    fun commitEntryDelete(entryId: Int) {
        applyEntryDeleteState(currentEntryDeleteState().withDeleteCommitted(entryId))
    }

    fun beginAddingExercise(exercise: StrengthExercise, equipment: String, variation: String) {
        val entry = addedStrengthRoutineEntry(
            entries = entries,
            completedStrengthHistory = completedStrengthHistory,
            exercise = exercise,
            equipment = equipment,
            variation = variation
        )
        pendingAddedEntry = entry
        selectedEntryId = entry.id
        isExerciseListVisible = false
        shouldReturnToExerciseListFromDetail = false
        isChangingSelectedEntryExercise = false
        exerciseToConfigure = null
    }

    fun cancelPendingAddedExercise() {
        pendingAddedEntry = null
        selectedEntryId = null
        isExerciseListVisible = false
        shouldReturnToExerciseListFromDetail = false
        isChangingSelectedEntryExercise = false
    }

    fun savePendingAddedExercise() {
        val entry = pendingAddedEntry ?: return
        entries = entries + entry
        pendingAddedEntry = null
        selectedEntryId = null
        isExerciseListVisible = false
        shouldReturnToExerciseListFromDetail = false
        isChangingSelectedEntryExercise = false
    }

    fun closeExerciseDetailToRoutineEdit() {
        selectedEntryId = null
        isExerciseListVisible = false
        shouldReturnToExerciseListFromDetail = false
        isChangingSelectedEntryExercise = false
    }

    fun handleBack() {
        when {
            isUnsavedBackDialogVisible -> isUnsavedBackDialogVisible = false
            isChangingSelectedEntryExercise -> isChangingSelectedEntryExercise = false
            selectedEntry != null -> {
                if (pendingAddedEntry != null) {
                    cancelPendingAddedExercise()
                } else {
                    closeExerciseDetailToRoutineEdit()
                }
            }
            isSupersetSelectionMode -> closeSupersetSelectionMode()
            isExerciseListVisible -> isExerciseListVisible = false
            currentEditableRoutine() != originalRoutineSnapshot -> isUnsavedBackDialogVisible = true
            else -> onBack()
        }
    }

    BackHandler(
        enabled = selectedEntry != null ||
            isExerciseListVisible ||
            isSupersetSelectionMode ||
            currentEditableRoutine() != originalRoutineSnapshot ||
            isUnsavedBackDialogVisible
    ) {
        handleBack()
    }

    exerciseToConfigure?.let { exercise ->
        StrengthExerciseConfigDialog(
            exercise = exercise,
            initialSearchQuery = exerciseToConfigureSearchQuery,
            onDismiss = { exerciseToConfigure = null },
            onDone = { equipment, variation ->
                beginAddingExercise(exercise, equipment, variation)
            }
        )
    }

    if (isCustomExerciseDialogVisible) {
        CustomStrengthExerciseDialog(
            onDismiss = { isCustomExerciseDialogVisible = false },
            onAdd = { name ->
                isCustomExerciseDialogVisible = false
                exerciseToConfigureSearchQuery = ""
                exerciseToConfigure = customStrengthExercise(name)
            }
        )
    }

    if (isRoutineDeleteDialogVisible && routine != null) {
        StrengthRoutineDeleteDialog(
            routine = routine,
            onDismiss = { isRoutineDeleteDialogVisible = false },
            onDelete = {
                isRoutineDeleteDialogVisible = false
                onDelete(routine)
            }
        )
    }

    if (isUnsavedBackDialogVisible) {
        val canSaveRoutine = currentEditableRoutine().entries.isNotEmpty() && currentEditableRoutine().name.isNotBlank()
        StrengthRoutineUnsavedBackDialog(
            canSaveRoutine = canSaveRoutine,
            onDismiss = { isUnsavedBackDialogVisible = false },
            onSave = {
                isUnsavedBackDialogVisible = false
                saveCurrentRoutine()
            },
            onDiscard = {
                isUnsavedBackDialogVisible = false
                onBack()
            }
        )
    }

    Scaffold(
        topBar = {
            StrengthRoutineEditTopBar(
                isChangingExercise = isChangingSelectedEntryExercise,
                isExerciseDetailVisible = selectedEntry != null,
                isAddingExercise = pendingAddedEntry != null,
                isExerciseListVisible = isExerciseListVisible,
                isNewRoutine = routine == null,
                onBack = ::handleBack
            )
        }
    ) { innerPadding ->
        if (selectedEntry != null) {
            StrengthExerciseDetailEditor(
                entry = selectedEntry,
                isChangingExercise = isChangingSelectedEntryExercise,
                isAddingExercise = pendingAddedEntry != null,
                onEntryChange = ::updateEntry,
                onChangingExerciseChange = { isChangingSelectedEntryExercise = it },
                onDelete = {
                    entries = entries.withoutRoutineEntry(selectedEntry.id)
                    selectedEntryId = null
                    shouldReturnToExerciseListFromDetail = false
                    isChangingSelectedEntryExercise = false
                },
                onCancel = ::cancelPendingAddedExercise,
                onSave = ::savePendingAddedExercise,
                modifier = Modifier.padding(innerPadding)
            )
        } else if (isExerciseListVisible) {
            StrengthExerciseListScreen(
                modifier = Modifier.padding(innerPadding),
                onAddCustomExercise = { isCustomExerciseDialogVisible = true },
                onExerciseSelected = { exercise, searchQuery ->
                    exerciseToConfigureSearchQuery = searchQuery
                    exerciseToConfigure = exercise
                }
            )
        } else {
            val draggingOverlayYOrNull = entryDragUiState.clampedOverlayYOrNull(entries)
            StrengthRoutineEntryListEditor(
                routineName = routineName,
                entries = entries,
                supersetLabels = supersetLabels,
                pendingDeleteEntryIds = pendingDeleteEntryIds,
                isSupersetSelectionMode = isSupersetSelectionMode,
                selectedSupersetEntryIds = selectedSupersetEntryIds,
                canGroupSelectedSuperset = supersetSelectionUiState.canGroup(entries),
                canClearSelectedSuperset = supersetSelectionUiState.canClear(entries),
                draggingEntryId = entryDragUiState.draggingEntryId,
                draggingOverlayY = draggingOverlayYOrNull,
                canSave = entries.isNotEmpty() && routineName.isNotBlank(),
                showDelete = routine != null,
                modifier = Modifier.padding(innerPadding),
                onRoutineNameChange = { routineName = it },
                onRootLayoutChanged = { rootY, rootHeight ->
                    entryDragUiState = entryDragUiState.withRootLayoutChanged(rootY, rootHeight)
                },
                onEntryHeightChanged = { entryId, height ->
                    entryDragUiState = entryDragUiState.withEntryHeightChanged(entryId, height)
                },
                onEntryRootYChanged = { entryId, rootY ->
                    entryDragUiState = entryDragUiState.withEntryRootYChanged(entryId, rootY)
                },
                onEntryDragStart = ::startEntryDrag,
                onEntryDrag = ::updateEntryDrag,
                onEntryDragEnd = ::endEntryDrag,
                onGroupSuperset = ::groupSelectedAsSuperset,
                onClearSelectedSupersetGroups = ::clearSelectedSupersetGroups,
                onCancelSupersetSelection = ::closeSupersetSelectionMode,
                onStartSupersetSelection = supersetSelectionUiState::start,
                onAddExercise = { isExerciseListVisible = true },
                onSave = ::saveCurrentRoutine,
                onDeleteRoutine = { isRoutineDeleteDialogVisible = true },
                onEntryClick = { entryId ->
                    shouldReturnToExerciseListFromDetail = false
                    isChangingSelectedEntryExercise = false
                    selectedEntryId = entryId
                },
                onSupersetToggle = { entryId ->
                    entries.firstOrNull { it.id == entryId }?.let { entry ->
                        supersetSelectionUiState.toggle(entry, entries)
                    }
                },
                onEntryDeleteRequested = ::requestEntryDelete,
                onEntryDeleteCommitted = ::commitEntryDelete,
                onEntryDeleteRestored = ::restoreEntryDelete
            )
        }
    }
}

private fun strengthRoutineEntriesStateSaver(): Saver<MutableState<List<StrengthRoutineEntry>>, String> {
    return Saver(
        save = { state ->
            listOf(
                StrengthWorkoutRoutine(
                    id = 0,
                    name = "",
                    entries = state.value
                )
            ).toJsonString()
        },
        restore = { saved ->
            mutableStateOf(saved.toStrengthWorkoutRoutines().firstOrNull()?.entries.orEmpty())
        }
    )
}

private fun strengthRoutineEntryStateSaver(): Saver<MutableState<StrengthRoutineEntry?>, String> {
    return Saver(
        save = { state ->
            listOf(
                StrengthWorkoutRoutine(
                    id = 0,
                    name = "",
                    entries = listOfNotNull(state.value)
                )
            ).toJsonString()
        },
        restore = { saved ->
            mutableStateOf(saved.toStrengthWorkoutRoutines().firstOrNull()?.entries?.firstOrNull())
        }
    )
}

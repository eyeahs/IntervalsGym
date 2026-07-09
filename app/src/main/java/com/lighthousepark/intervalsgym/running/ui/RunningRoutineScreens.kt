package com.lighthousepark.intervalsgym.running.ui

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.lighthousepark.intervalsgym.app.PREFS_NAME
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.data.deleteSavedRunningWorkoutRoutine
import com.lighthousepark.intervalsgym.data.loadSavedRunningWorkoutRoutines
import com.lighthousepark.intervalsgym.running.SavedRunningWorkoutRoutine

/**
 * Route owner for the saved running routine picker.
 * Keep this screen focused on choosing a routine to execute; editing and deletion live in [RunningRoutineManagementScreen].
 * UI tests: RunningRoutineScreensUiTest.routineList_selectsSavedRoutineAndOpensManagement,
 * routineList_emptyStateExposesManageAction, routineList_backButtonInvokesBackCallback.
 */
@Composable
internal fun RunningRoutineListScreen(
    onRoutineSelected: (SavedRunningWorkoutRoutine) -> Unit,
    onManageRoutines: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var routines by remember { mutableStateOf(loadSavedRunningWorkoutRoutines(prefs)) }

    RefreshRunningRoutinesOnResume {
        val refreshedRoutines = loadSavedRunningWorkoutRoutines(prefs)
        routines = refreshedRoutines
    }

    Scaffold(
        topBar = {
            RunningRoutineListTopBar(
                onBack = onBack,
                onManageRoutines = onManageRoutines
            )
        }
    ) { innerPadding ->
        RunningRoutineListContent(
            routines = routines,
            emptyText = "저장된 러닝 Routine이 없습니다. Intervals.icu Routine 상세에서 먼저 저장하세요.",
            emptyContentDescription = TestContentDescriptions.RunningRoutineListEmpty,
            showGraph = true,
            onRoutineSelected = onRoutineSelected,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

/**
 * Route owner for saved running routine management.
 * Selecting a routine opens a graph detail surface now; later block editing can extend that detail state.
 * UI tests: RunningRoutineScreensUiTest.routineManagement_emptyStateIsAccessible,
 * routineManagement_deletesSavedRoutineAfterConfirmation, routineManagement_cancelDeleteKeepsSavedRoutine,
 * routineManagement_backFromDetailReturnsToListThenInvokesBack.
 */
@Composable
internal fun RunningRoutineManagementScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var routines by remember { mutableStateOf(loadSavedRunningWorkoutRoutines(prefs)) }
    var selectedRoutine by remember { mutableStateOf<SavedRunningWorkoutRoutine?>(null) }
    var pendingDeleteRoutine by remember { mutableStateOf<SavedRunningWorkoutRoutine?>(null) }

    RefreshRunningRoutinesOnResume {
        val refreshedRoutines = loadSavedRunningWorkoutRoutines(prefs)
        routines = refreshedRoutines
        selectedRoutine = selectedRoutine?.let { selected ->
            refreshedRoutines.firstOrNull { it.id == selected.id }
        }
    }

    BackHandler(enabled = selectedRoutine != null) {
        selectedRoutine = null
    }

    pendingDeleteRoutine?.let { routine ->
        RunningRoutineDeleteDialog(
            routine = routine,
            onConfirm = {
                deleteSavedRunningWorkoutRoutine(prefs, routine.id)
                routines = loadSavedRunningWorkoutRoutines(prefs)
                selectedRoutine = null
                pendingDeleteRoutine = null
            },
            onDismiss = { pendingDeleteRoutine = null }
        )
    }

    Scaffold(
        topBar = {
            RunningRoutineManagementTopBar(
                title = selectedRoutine?.name ?: "러닝 Routine 관리",
                canDelete = selectedRoutine != null,
                onBack = {
                    if (selectedRoutine != null) {
                        selectedRoutine = null
                    } else {
                        onBack()
                    }
                },
                onDelete = {
                    selectedRoutine?.let { pendingDeleteRoutine = it }
                }
            )
        }
    ) { innerPadding ->
        val routine = selectedRoutine
        if (routine == null) {
            RunningRoutineListContent(
                routines = routines,
                emptyText = "저장된 러닝 Routine이 없습니다.",
                emptyContentDescription = TestContentDescriptions.RunningRoutineManagementEmpty,
                showPlayIcon = false,
                showGraph = false,
                onRoutineSelected = { selectedRoutine = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            RunningRoutineDetailContent(
                routine = routine,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

package com.lighthousepark.intervalsgym.running.ui

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.lighthousepark.intervalsgym.app.PREFS_NAME
import com.lighthousepark.intervalsgym.core.TestContentDescriptions
import com.lighthousepark.intervalsgym.core.debugContentDescription
import com.lighthousepark.intervalsgym.core.formatDuration
import com.lighthousepark.intervalsgym.data.deleteSavedRunningWorkoutRoutine
import com.lighthousepark.intervalsgym.data.loadSavedRunningWorkoutRoutines
import com.lighthousepark.intervalsgym.running.SavedRunningWorkoutRoutine
import com.lighthousepark.intervalsgym.training.TrainingSportType
import com.lighthousepark.intervalsgym.workout.ui.RoutineWorkoutGraphCanvas
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Route owner for the saved running routine picker.
 * Keep this screen focused on choosing a routine to execute; editing and deletion live in [RunningRoutineManagementScreen].
 * UI tests: RunningRoutineScreensUiTest.routineList_selectsSavedRoutineAndOpensManagement,
 * routineList_emptyStateExposesManageAction, routineList_backButtonInvokesBackCallback.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
            TopAppBar(
                title = { Text("러닝 routine 선택") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningRoutineListBack)
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onManageRoutines,
                        modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningRoutineListManage)
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = "러닝 Routine 관리")
                    }
                }
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
@OptIn(ExperimentalMaterial3Api::class)
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
        AlertDialog(
            onDismissRequest = { pendingDeleteRoutine = null },
            title = { Text("러닝 Routine 삭제") },
            text = { Text("'${routine.name}' routine을 삭제할까요?") },
            confirmButton = {
                Button(
                    onClick = {
                        deleteSavedRunningWorkoutRoutine(prefs, routine.id)
                        routines = loadSavedRunningWorkoutRoutines(prefs)
                        selectedRoutine = null
                        pendingDeleteRoutine = null
                    },
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningRoutineConfirmDelete)
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingDeleteRoutine = null },
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningRoutineCancelDelete)
                ) {
                    Text("취소")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedRoutine?.name ?: "러닝 Routine 관리") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectedRoutine != null) {
                                selectedRoutine = null
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningRoutineManagementBack)
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    selectedRoutine?.let { routine ->
                        IconButton(
                            onClick = { pendingDeleteRoutine = routine },
                            modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningRoutineDelete)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "러닝 Routine 삭제",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
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

@Composable
private fun RefreshRunningRoutinesOnResume(onResume: () -> Unit) {
    val context = LocalContext.current
    DisposableEffect(context) {
        val lifecycle = (context as? LifecycleOwner)?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onResume()
            }
        }
        lifecycle?.addObserver(observer)
        onDispose {
            lifecycle?.removeObserver(observer)
        }
    }
}

@Composable
private fun RunningRoutineListContent(
    routines: List<SavedRunningWorkoutRoutine>,
    emptyText: String,
    emptyContentDescription: String,
    showPlayIcon: Boolean = true,
    showGraph: Boolean = true,
    onRoutineSelected: (SavedRunningWorkoutRoutine) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (routines.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .debugContentDescription(emptyContentDescription),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = emptyText,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(routines, key = { it.id }) { routine ->
                RunningRoutineRow(
                    routine = routine,
                    showPlayIcon = showPlayIcon,
                    showGraph = showGraph,
                    onClick = { onRoutineSelected(routine) }
                )
            }
        }
    }
}

@Composable
private fun RunningRoutineRow(
    routine: SavedRunningWorkoutRoutine,
    showPlayIcon: Boolean,
    showGraph: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .debugContentDescription(TestContentDescriptions.runningSavedRoutine(routine.id))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.DirectionsRun,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = listOf(
                            formatDuration(routine.durationSeconds),
                            "${routine.blocks.size} blocks",
                            routine.savedAtLabel()
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (showPlayIcon) {
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (showGraph && routine.blocks.isNotEmpty()) {
                RoutineWorkoutGraphCanvas(
                    blocks = routine.blocks,
                    totalSeconds = routine.durationSeconds,
                    sportType = TrainingSportType.RUNNING,
                    height = 104.dp
                )
            }
        }
    }
}

@Composable
private fun RunningRoutineDetailContent(
    routine: SavedRunningWorkoutRoutine,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.DirectionsRun,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = routine.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${formatDuration(routine.durationSeconds)} · ${routine.blocks.size} blocks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    RoutineWorkoutGraphCanvas(
                        blocks = routine.blocks,
                        totalSeconds = routine.durationSeconds,
                        sportType = TrainingSportType.RUNNING,
                        height = 190.dp
                    )
                }
            }
        }
        routine.description
            ?.takeIf { it.isNotBlank() }
            ?.let { description ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "설명",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private fun SavedRunningWorkoutRoutine.savedAtLabel(): String {
    if (savedAtMillis <= 0L) return "저장됨"
    val formatter = DateTimeFormatter.ofPattern("M/d HH:mm")
    return Instant.ofEpochMilli(savedAtMillis)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

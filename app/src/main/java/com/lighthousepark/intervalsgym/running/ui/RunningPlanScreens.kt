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
import com.lighthousepark.intervalsgym.data.deleteSavedRunningWorkoutPlan
import com.lighthousepark.intervalsgym.data.loadSavedRunningWorkoutPlans
import com.lighthousepark.intervalsgym.running.SavedRunningWorkoutPlan
import com.lighthousepark.intervalsgym.training.TrainingSportType
import com.lighthousepark.intervalsgym.workout.ui.PlanWorkoutGraphCanvas
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Route owner for the saved running plan picker.
 * Keep this screen focused on choosing a plan to execute; editing and deletion live in [RunningPlanManagementScreen].
 * UI tests: RunningPlanScreensUiTest.planList_selectsSavedPlanAndOpensManagement,
 * planList_emptyStateExposesManageAction, planList_backButtonInvokesBackCallback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RunningPlanListScreen(
    onPlanSelected: (SavedRunningWorkoutPlan) -> Unit,
    onManagePlans: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var plans by remember { mutableStateOf(loadSavedRunningWorkoutPlans(prefs)) }

    RefreshRunningPlansOnResume {
        val refreshedPlans = loadSavedRunningWorkoutPlans(prefs)
        plans = refreshedPlans
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("러닝 plan 선택") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningPlanListBack)
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onManagePlans,
                        modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningPlanListManage)
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = "러닝 Plan 관리")
                    }
                }
            )
        }
    ) { innerPadding ->
        RunningPlanListContent(
            plans = plans,
            emptyText = "저장된 러닝 Plan이 없습니다. Intervals.icu plan 상세에서 먼저 저장하세요.",
            emptyContentDescription = TestContentDescriptions.RunningPlanListEmpty,
            showGraph = true,
            onPlanSelected = onPlanSelected,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

/**
 * Route owner for saved running plan management.
 * Selecting a plan opens a graph detail surface now; later block editing can extend that detail state.
 * UI tests: RunningPlanScreensUiTest.planManagement_emptyStateIsAccessible,
 * planManagement_deletesSavedPlanAfterConfirmation, planManagement_cancelDeleteKeepsSavedPlan,
 * planManagement_backFromDetailReturnsToListThenInvokesBack.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RunningPlanManagementScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var plans by remember { mutableStateOf(loadSavedRunningWorkoutPlans(prefs)) }
    var selectedPlan by remember { mutableStateOf<SavedRunningWorkoutPlan?>(null) }
    var pendingDeletePlan by remember { mutableStateOf<SavedRunningWorkoutPlan?>(null) }

    RefreshRunningPlansOnResume {
        val refreshedPlans = loadSavedRunningWorkoutPlans(prefs)
        plans = refreshedPlans
        selectedPlan = selectedPlan?.let { selected ->
            refreshedPlans.firstOrNull { it.id == selected.id }
        }
    }

    BackHandler(enabled = selectedPlan != null) {
        selectedPlan = null
    }

    pendingDeletePlan?.let { plan ->
        AlertDialog(
            onDismissRequest = { pendingDeletePlan = null },
            title = { Text("러닝 Plan 삭제") },
            text = { Text("'${plan.name}' plan을 삭제할까요?") },
            confirmButton = {
                Button(
                    onClick = {
                        deleteSavedRunningWorkoutPlan(prefs, plan.id)
                        plans = loadSavedRunningWorkoutPlans(prefs)
                        selectedPlan = null
                        pendingDeletePlan = null
                    },
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningPlanConfirmDelete)
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingDeletePlan = null },
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningPlanCancelDelete)
                ) {
                    Text("취소")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedPlan?.name ?: "러닝 Plan 관리") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectedPlan != null) {
                                selectedPlan = null
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningPlanManagementBack)
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    selectedPlan?.let { plan ->
                        IconButton(
                            onClick = { pendingDeletePlan = plan },
                            modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningPlanDelete)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "러닝 Plan 삭제",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        val plan = selectedPlan
        if (plan == null) {
            RunningPlanListContent(
                plans = plans,
                emptyText = "저장된 러닝 Plan이 없습니다.",
                emptyContentDescription = TestContentDescriptions.RunningPlanManagementEmpty,
                showPlayIcon = false,
                showGraph = false,
                onPlanSelected = { selectedPlan = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            RunningPlanDetailContent(
                plan = plan,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
private fun RefreshRunningPlansOnResume(onResume: () -> Unit) {
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
private fun RunningPlanListContent(
    plans: List<SavedRunningWorkoutPlan>,
    emptyText: String,
    emptyContentDescription: String,
    showPlayIcon: Boolean = true,
    showGraph: Boolean = true,
    onPlanSelected: (SavedRunningWorkoutPlan) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (plans.isEmpty()) {
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
            items(plans, key = { it.id }) { plan ->
                RunningPlanRow(
                    plan = plan,
                    showPlayIcon = showPlayIcon,
                    showGraph = showGraph,
                    onClick = { onPlanSelected(plan) }
                )
            }
        }
    }
}

@Composable
private fun RunningPlanRow(
    plan: SavedRunningWorkoutPlan,
    showPlayIcon: Boolean,
    showGraph: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .debugContentDescription(TestContentDescriptions.runningSavedPlan(plan.id))
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
                        text = plan.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = listOf(
                            formatDuration(plan.durationSeconds),
                            "${plan.blocks.size} blocks",
                            plan.savedAtLabel()
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
            if (showGraph && plan.blocks.isNotEmpty()) {
                PlanWorkoutGraphCanvas(
                    blocks = plan.blocks,
                    totalSeconds = plan.durationSeconds,
                    sportType = TrainingSportType.RUNNING,
                    height = 104.dp
                )
            }
        }
    }
}

@Composable
private fun RunningPlanDetailContent(
    plan: SavedRunningWorkoutPlan,
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
                                text = plan.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${formatDuration(plan.durationSeconds)} · ${plan.blocks.size} blocks",
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
                    PlanWorkoutGraphCanvas(
                        blocks = plan.blocks,
                        totalSeconds = plan.durationSeconds,
                        sportType = TrainingSportType.RUNNING,
                        height = 190.dp
                    )
                }
            }
        }
        plan.description
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

private fun SavedRunningWorkoutPlan.savedAtLabel(): String {
    if (savedAtMillis <= 0L) return "저장됨"
    val formatter = DateTimeFormatter.ofPattern("M/d HH:mm")
    return Instant.ofEpochMilli(savedAtMillis)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

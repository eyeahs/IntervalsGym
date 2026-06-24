package com.lighthousepark.intervalsgym.strength.ui

import com.lighthousepark.intervalsgym.MainActivity
import com.lighthousepark.intervalsgym.R
import com.lighthousepark.intervalsgym.app.*
import com.lighthousepark.intervalsgym.core.*
import com.lighthousepark.intervalsgym.data.*
import com.lighthousepark.intervalsgym.login.*
import com.lighthousepark.intervalsgym.overlay.*
import com.lighthousepark.intervalsgym.running.*
import com.lighthousepark.intervalsgym.running.ui.*
import com.lighthousepark.intervalsgym.strength.*
import com.lighthousepark.intervalsgym.strength.ui.*
import com.lighthousepark.intervalsgym.training.*
import com.lighthousepark.intervalsgym.training.ui.*
import com.lighthousepark.intervalsgym.workout.ui.*

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Surface as MaterialSurface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lighthousepark.intervalsgym.ui.theme.IntervalsGymTheme
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun StrengthPlanRow(
    plan: StrengthWorkoutPlan,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit,
) {
    val setCount = plan.entries.sumOf { it.records.size }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${plan.entries.size}개 운동 · ${setCount}세트",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = plan.entries.joinToString(" · ") { it.exercise.nameKo },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            trailing()
        }
    }
}

/**
 * Route owner for [ROUTE_STRENGTH_PLAN_EDIT].
 * This owns strength plan creation/editing, exercise ordering, superset grouping, and nested exercise selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StrengthPlanEditScreen(
    plan: StrengthWorkoutPlan?,
    onSave: (StrengthWorkoutPlan) -> Unit,
    onDelete: (StrengthWorkoutPlan) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val completedStrengthHistory = remember(plan?.id) { loadCompletedStrengthWorkoutHistory(prefs) }
    var planName by rememberSaveable(plan?.id) { mutableStateOf(plan?.name.orEmpty()) }
    var entries by rememberSaveable(plan?.id, saver = strengthPlanEntriesStateSaver()) {
        mutableStateOf(plan?.entries.orEmpty())
    }
    var selectedEntryId by rememberSaveable(plan?.id) { mutableStateOf<Int?>(null) }
    var isSupersetSelectionMode by remember(plan?.id) { mutableStateOf(false) }
    var selectedSupersetEntryIds by remember(plan?.id) { mutableStateOf(emptySet<Int>()) }
    var pendingDeleteEntryIds by remember(plan?.id) { mutableStateOf(emptySet<Int>()) }
    var isExerciseListVisible by rememberSaveable(plan?.id) { mutableStateOf(false) }
    var shouldReturnToExerciseListFromDetail by rememberSaveable(plan?.id) { mutableStateOf(false) }
    var isChangingSelectedEntryExercise by rememberSaveable(plan?.id, selectedEntryId) { mutableStateOf(false) }
    var exerciseToConfigure by remember { mutableStateOf<StrengthExercise?>(null) }
    var exerciseToConfigureSearchQuery by remember { mutableStateOf("") }
    var isCustomExerciseDialogVisible by remember { mutableStateOf(false) }
    var isPlanDeleteDialogVisible by remember(plan?.id) { mutableStateOf(false) }
    var isUnsavedBackDialogVisible by remember(plan?.id) { mutableStateOf(false) }
    val selectedEntry = entries.firstOrNull { it.id == selectedEntryId }
    val supersetLabels = remember(entries) { entries.supersetGroupLabels() }
    val originalPlanSnapshot = remember(plan?.id) {
        StrengthWorkoutPlan(
            id = plan?.id ?: 0,
            name = plan?.name.orEmpty().trim(),
            entries = plan?.entries.orEmpty().normalizeSupersetGroups()
        )
    }
    var draggingEntryId by remember { mutableStateOf<Int?>(null) }
    var draggingOffsetY by remember { mutableStateOf(0f) }
    var entryHeights by remember { mutableStateOf(emptyMap<Int, Int>()) }
    var editRootY by remember { mutableStateOf(0f) }
    var editRootHeight by remember { mutableIntStateOf(0) }
    var entryRootYPositions by remember { mutableStateOf(emptyMap<Int, Float>()) }
    var dragStartOverlayY by remember { mutableStateOf(0f) }

    fun updateEntry(entry: StrengthPlanEntry) {
        entries = entries.map { if (it.id == entry.id) entry else it }
    }

    fun currentEditablePlan(): StrengthWorkoutPlan {
        return StrengthWorkoutPlan(
            id = plan?.id ?: 0,
            name = planName.trim(),
            entries = entries
                .filterNot { it.id in pendingDeleteEntryIds }
                .normalizeSupersetGroups()
        )
    }

    fun saveCurrentPlan() {
        onSave(currentEditablePlan())
    }

    fun startEntryDrag(entryId: Int) {
        draggingEntryId = entryId
        draggingOffsetY = 0f
        dragStartOverlayY = (entryRootYPositions[entryId] ?: editRootY) - editRootY
    }

    fun entryDragBounds(): Pair<Float, Float>? {
        val bounds = entries.mapNotNull { entry ->
            val top = entryRootYPositions[entry.id] ?: return@mapNotNull null
            val height = entryHeights[entry.id] ?: return@mapNotNull null
            top to top + height
        }
        val top = bounds.minOfOrNull { it.first } ?: return null
        val bottom = bounds.maxOfOrNull { it.second } ?: return null
        return (top - editRootY) to (bottom - editRootY)
    }

    fun clampedEntryDragOffset(entryId: Int, offsetY: Float): Float {
        val itemHeight = (entryHeights[entryId] ?: 0).toFloat()
        val (listTop, listBottom) = entryDragBounds() ?: return offsetY
        val minOffset = listTop - dragStartOverlayY
        val maxOffset = (listBottom - itemHeight - dragStartOverlayY).coerceAtLeast(minOffset)
        return offsetY.coerceIn(minOffset, maxOffset)
    }

    fun updateEntryDrag(entryId: Int, deltaY: Float) {
        if (draggingEntryId != entryId) return
        val previousOffsetY = draggingOffsetY
        draggingOffsetY = clampedEntryDragOffset(entryId, draggingOffsetY + deltaY)
        val consumedDeltaY = draggingOffsetY - previousOffsetY
        if (consumedDeltaY == 0f) return
        val currentIndex = entries.indexOfFirst { it.id == entryId }
        if (currentIndex < 0) return
        val draggedHeight = (entryHeights[entryId] ?: 0).toFloat()
        val overlayCenterY = dragStartOverlayY + draggingOffsetY + draggedHeight / 2f

        if (consumedDeltaY > 0f && currentIndex < entries.lastIndex) {
            val nextEntry = entries[currentIndex + 1]
            val nextTop = (entryRootYPositions[nextEntry.id] ?: return) - editRootY
            val nextHeight = (entryHeights[nextEntry.id] ?: 0).toFloat()
            val nextCenterY = nextTop + nextHeight / 2f
            if (overlayCenterY > nextCenterY) {
                entries = entries.moveItem(currentIndex, currentIndex + 1)
            }
        } else if (consumedDeltaY < 0f && currentIndex > 0) {
            val previousEntry = entries[currentIndex - 1]
            val previousTop = (entryRootYPositions[previousEntry.id] ?: return) - editRootY
            val previousHeight = (entryHeights[previousEntry.id] ?: 0).toFloat()
            val previousCenterY = previousTop + previousHeight / 2f
            if (overlayCenterY < previousCenterY) {
                entries = entries.moveItem(currentIndex, currentIndex - 1)
            }
        }
    }

    fun endEntryDrag() {
        draggingEntryId = null
        draggingOffsetY = 0f
        dragStartOverlayY = 0f
    }

    fun closeSupersetSelectionMode() {
        isSupersetSelectionMode = false
        selectedSupersetEntryIds = emptySet()
    }

    fun groupSelectedAsSuperset() {
        if (selectedSupersetEntryIds.size < 2) return
        val nextGroupId = (entries.mapNotNull { it.supersetGroupId }.maxOrNull() ?: 0) + 1
        entries = entries
            .map { entry ->
                if (entry.id in selectedSupersetEntryIds) {
                    entry.copy(supersetGroupId = nextGroupId)
                } else {
                    entry
                }
            }
            .normalizeSupersetGroups()
        closeSupersetSelectionMode()
    }

    fun clearSelectedSupersetGroups() {
        val selectedGroupIds = entries
            .filter { it.id in selectedSupersetEntryIds }
            .mapNotNull { it.supersetGroupId }
            .toSet()
        if (selectedGroupIds.isEmpty()) return
        entries = entries.map { entry ->
            if (entry.supersetGroupId in selectedGroupIds) {
                entry.copy(supersetGroupId = null)
            } else {
                entry
            }
        }
        closeSupersetSelectionMode()
    }

    fun requestEntryDelete(entryId: Int) {
        pendingDeleteEntryIds = pendingDeleteEntryIds + entryId
        selectedSupersetEntryIds = selectedSupersetEntryIds - entryId
    }

    fun restoreEntryDelete(entryId: Int) {
        pendingDeleteEntryIds = pendingDeleteEntryIds - entryId
    }

    fun commitEntryDelete(entryId: Int) {
        if (entryId !in pendingDeleteEntryIds) return
        entries = entries
            .filterNot { it.id == entryId }
            .normalizeSupersetGroups()
        pendingDeleteEntryIds = pendingDeleteEntryIds - entryId
        selectedSupersetEntryIds = selectedSupersetEntryIds - entryId
        if (selectedEntryId == entryId) selectedEntryId = null
    }

    fun addExercise(exercise: StrengthExercise, equipment: String, variation: String) {
        val nextId = (entries.maxOfOrNull { it.id } ?: 0) + 1
        val entry = completedStrengthHistory
            .latestMatchingStrengthEntry(exercise, equipment, variation)
            ?.copyAsNewPlanEntry(
                id = nextId,
                exercise = exercise,
                equipment = equipment,
                variation = variation
            )
            ?: defaultStrengthPlanEntry(
                id = nextId,
                exercise = exercise,
                weightKg = defaultStrengthWeightForEquipment(equipment)
            ).copy(
                equipment = equipment,
                variation = variation
            )
        entries = entries + entry
        selectedEntryId = entry.id
        isExerciseListVisible = false
        shouldReturnToExerciseListFromDetail = false
        isChangingSelectedEntryExercise = false
        exerciseToConfigure = null
    }

    fun closeExerciseDetailToPlanEdit() {
        selectedEntryId = null
        isExerciseListVisible = false
        shouldReturnToExerciseListFromDetail = false
        isChangingSelectedEntryExercise = false
    }

    fun handleBack() {
        when {
            isUnsavedBackDialogVisible -> isUnsavedBackDialogVisible = false
            isChangingSelectedEntryExercise -> isChangingSelectedEntryExercise = false
            selectedEntry != null -> closeExerciseDetailToPlanEdit()
            isSupersetSelectionMode -> closeSupersetSelectionMode()
            isExerciseListVisible -> isExerciseListVisible = false
            currentEditablePlan() != originalPlanSnapshot -> isUnsavedBackDialogVisible = true
            else -> onBack()
        }
    }

    BackHandler(
        enabled = selectedEntry != null ||
            isExerciseListVisible ||
            isSupersetSelectionMode ||
            currentEditablePlan() != originalPlanSnapshot ||
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
                addExercise(exercise, equipment, variation)
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

    if (isPlanDeleteDialogVisible && plan != null) {
        AlertDialog(
            onDismissRequest = { isPlanDeleteDialogVisible = false },
            title = { Text("Plan 삭제") },
            text = {
                Text(
                    text = "'${plan.name}' Plan을 삭제할까요? 삭제한 Plan은 복구할 수 없습니다."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isPlanDeleteDialogVisible = false
                        onDelete(plan)
                    }
                ) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { isPlanDeleteDialogVisible = false }) {
                    Text("취소")
                }
            }
        )
    }

    if (isUnsavedBackDialogVisible) {
        val canSavePlan = currentEditablePlan().entries.isNotEmpty() && currentEditablePlan().name.isNotBlank()
        AlertDialog(
            onDismissRequest = { isUnsavedBackDialogVisible = false },
            title = { Text("변경사항 저장") },
            text = {
                Text(
                    text = "Plan 수정 내용을 저장할까요?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isUnsavedBackDialogVisible = false
                        saveCurrentPlan()
                    },
                    enabled = canSavePlan
                ) {
                    Text("저장")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            isUnsavedBackDialogVisible = false
                            onBack()
                        }
                    ) {
                        Text("저장 안 함")
                    }
                    TextButton(onClick = { isUnsavedBackDialogVisible = false }) {
                        Text("취소")
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            isChangingSelectedEntryExercise -> "운동 목록"
                            selectedEntry != null -> "운동 상세"
                            isExerciseListVisible -> "운동 목록"
                            plan == null -> "Plan 추가"
                            else -> "Plan 수정"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = ::handleBack
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (selectedEntry != null) {
            StrengthExerciseDetailEditor(
                entry = selectedEntry,
                isChangingExercise = isChangingSelectedEntryExercise,
                onEntryChange = ::updateEntry,
                onChangingExerciseChange = { isChangingSelectedEntryExercise = it },
                onAddExercise = {
                    closeExerciseDetailToPlanEdit()
                },
                onDelete = {
                    entries = entries.filterNot { it.id == selectedEntry.id }
                    selectedEntryId = null
                    shouldReturnToExerciseListFromDetail = false
                    isChangingSelectedEntryExercise = false
                },
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .onGloballyPositioned { coordinates ->
                        editRootY = coordinates.positionInRoot().y
                        editRootHeight = coordinates.size.height
                    }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 128.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = planName,
                            onValueChange = { planName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Plan 이름") },
                            placeholder = { Text("새 웨이트 Plan") },
                            singleLine = true
                        )
                    }
                    if (entries.isEmpty()) {
                        item {
                            Text(
                                text = "운동을 추가해 Plan을 구성하세요.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        if (isSupersetSelectionMode) {
                            item {
                                SupersetEditPanel(
                                    isSelectionMode = isSupersetSelectionMode,
                                    selectedCount = selectedSupersetEntryIds.size,
                                    canClearSelectedGroups = entries.any { it.id in selectedSupersetEntryIds && it.supersetGroupId != null },
                                    onGroupSelected = ::groupSelectedAsSuperset,
                                    onClearSelectedGroups = ::clearSelectedSupersetGroups,
                                    onCancel = ::closeSupersetSelectionMode
                                )
                            }
                        }
                        itemsIndexed(entries, key = { _, entry -> entry.id }) { index, entry ->
                            val isPendingDelete = entry.id in pendingDeleteEntryIds
                            val isDragging = draggingEntryId == entry.id
                            val reorderModifier = if (isSupersetSelectionMode || isPendingDelete) {
                            Modifier
                        } else {
                            Modifier.pointerInput(entry.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { startEntryDrag(entry.id) },
                                    onDragEnd = ::endEntryDrag,
                                    onDragCancel = ::endEntryDrag
                                ) { change, dragAmount ->
                                    change.consume()
                                    updateEntryDrag(entry.id, dragAmount.y)
                                }
                            }
                        }
                            Box(
                                modifier = Modifier
                                    .animateItem()
                                    .onSizeChanged { size ->
                                        entryHeights = entryHeights + (entry.id to size.height)
                                    }
                                    .onGloballyPositioned { coordinates ->
                                        entryRootYPositions = entryRootYPositions + (entry.id to coordinates.positionInRoot().y)
                                    }
                                    .then(reorderModifier)
                            ) {
                                StrengthPlanExerciseRow(
                                    entry = entry,
                                    supersetLabel = entry.supersetGroupId?.let { supersetLabels[it] },
                                    isSupersetSelectionMode = isSupersetSelectionMode,
                                    isSupersetSelected = entry.id in selectedSupersetEntryIds,
                                    isPendingDelete = isPendingDelete,
                                    isDragging = false,
                                    dragHandleModifier = Modifier,
                                    modifier = Modifier.alpha(if (isDragging) 0f else 1f),
                                    onClick = {
                                        shouldReturnToExerciseListFromDetail = false
                                        isChangingSelectedEntryExercise = false
                                        selectedEntryId = entry.id
                                    },
                                    onSupersetToggle = {
                                        selectedSupersetEntryIds = if (entry.id in selectedSupersetEntryIds) {
                                            selectedSupersetEntryIds - entry.id
                                        } else {
                                            selectedSupersetEntryIds + entry.id
                                        }
                                    },
                                    onDelete = {
                                        requestEntryDelete(entry.id)
                                    },
                                    onCommitDelete = {
                                        commitEntryDelete(entry.id)
                                    },
                                    onRestore = {
                                        restoreEntryDelete(entry.id)
                                    }
                                )
                            }
                        }
                    }
                }
                StrengthPlanEditBottomBar(
                    canGroupSuperset = entries.size >= 2 && !isSupersetSelectionMode,
                    canSave = entries.isNotEmpty() && planName.isNotBlank(),
                    showDelete = plan != null,
                    onGroupSuperset = { isSupersetSelectionMode = true },
                    onAddExercise = { isExerciseListVisible = true },
                    onSave = ::saveCurrentPlan,
                    onDelete = { isPlanDeleteDialogVisible = true },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                )
                val draggingEntry = draggingEntryId?.let { id -> entries.firstOrNull { it.id == id } }
                if (draggingEntry != null) {
                    val itemHeight = (entryHeights[draggingEntry.id] ?: 0).toFloat()
                    val (listTop, listBottom) = entryDragBounds() ?: (0f to editRootHeight.toFloat())
                    val minOverlayY = listTop.coerceAtLeast(0f)
                    val maxOverlayY = (listBottom - itemHeight)
                        .coerceAtLeast(minOverlayY)
                        .coerceAtMost((editRootHeight - itemHeight).coerceAtLeast(minOverlayY))
                    val overlayY = (dragStartOverlayY + draggingOffsetY)
                        .coerceIn(minOverlayY, maxOverlayY)
                    StrengthPlanExerciseRow(
                        entry = draggingEntry,
                        supersetLabel = draggingEntry.supersetGroupId?.let { supersetLabels[it] },
                        isSupersetSelectionMode = isSupersetSelectionMode,
                        isSupersetSelected = draggingEntry.id in selectedSupersetEntryIds,
                        isPendingDelete = draggingEntry.id in pendingDeleteEntryIds,
                        isDragging = true,
                        dragHandleModifier = Modifier,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .offset { IntOffset(0, overlayY.roundToInt()) }
                            .zIndex(4f)
                            .graphicsLayer {
                                shadowElevation = 18f
                                scaleX = 1.015f
                                scaleY = 1.015f
                            },
                        onClick = {},
                        onSupersetToggle = {},
                        onDelete = {},
                        onCommitDelete = {},
                        onRestore = {}
                    )
                }
            }
        }
    }
}

@Composable
internal fun StrengthPlanEditBottomBar(
    canGroupSuperset: Boolean,
    canSave: Boolean,
    showDelete: Boolean,
    onGroupSuperset: () -> Unit,
    onAddExercise: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onGroupSuperset,
                    enabled = canGroupSuperset,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("슈퍼세트 묶기", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(
                    onClick = onAddExercise,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("신규 운동 추가", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSave,
                    enabled = canSave,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("plan 저장", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (showDelete) {
                    Button(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("plan 삭제", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Nested sub-screen inside [StrengthPlanEditScreen] for choosing an exercise to add or change.
 * Do not register this as a separate route unless the whole edit flow is split.
 */
@Composable
internal fun StrengthExerciseListScreen(
    modifier: Modifier = Modifier,
    onAddCustomExercise: () -> Unit,
    onExerciseSelected: (StrengthExercise, String) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val candidates = remember(searchQuery) {
        strengthExerciseCatalog
            .asSequence()
            .filter { exercise -> exercise.matchesSearch(searchQuery) }
            .toList()
    }

    Column(modifier = modifier.fillMaxSize()) {
        Surface(shadowElevation = 3.dp) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                label = { Text("운동 검색") },
                singleLine = true
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "custom-exercise") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onAddCustomExercise),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Text(
                            text = "운동 생성",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            items(candidates, key = { it.id }) { exercise ->
                ExerciseSearchRow(
                    exercise = exercise,
                    title = exercise.searchResultTitle(searchQuery),
                    selected = false,
                    onClick = { onExerciseSelected(exercise, searchQuery) }
                )
            }
        }
    }
}

/**
 * Dialog used by [StrengthPlanEditScreen] after an exercise is chosen.
 * Equipment, variation, unilateral mode, and initial set defaults are configured here.
 */
@Composable
internal fun StrengthExerciseConfigDialog(
    exercise: StrengthExercise,
    initialSearchQuery: String = "",
    onDismiss: () -> Unit,
    onDone: (String, String) -> Unit,
) {
    val isCustomExercise = exercise.group == "사용자 추가" || exercise.id.startsWith("custom_")
    val equipmentOptions = remember(exercise.id) { exercise.equipmentOptionsWithBodyweight() }
    val inferredEquipment = remember(exercise.id, initialSearchQuery, equipmentOptions) {
        exercise.inferEquipmentFromSearch(initialSearchQuery, equipmentOptions)
    }
    val inferredVariation = remember(exercise.id, initialSearchQuery) {
        exercise.inferVariationFromSearch(initialSearchQuery)
    }
    val inferredUnilateral = remember(exercise.id, initialSearchQuery) {
        exercise.inferUnilateralFromSearch(initialSearchQuery)
    }
    var selectedEquipment by remember(exercise.id, initialSearchQuery) {
        mutableStateOf(inferredEquipment ?: exercise.equipmentOptions.first())
    }
    var selectedVariation by remember(exercise.id, initialSearchQuery) {
        mutableStateOf(inferredVariation ?: exercise.baseVariationOptions().first())
    }
    var selectedUnilateral by remember(exercise.id, initialSearchQuery) {
        mutableStateOf(inferredUnilateral ?: "양쪽")
    }
    var customEquipment by remember(exercise.id, initialSearchQuery) { mutableStateOf("") }
    val equipment = if (selectedEquipment == "직접 입력") customEquipment.trim() else selectedEquipment
    val canComplete = selectedEquipment != "직접 입력" || equipment.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(exercise.nameKo) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = exercise.group,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ChoiceGrid(
                    title = "기구",
                    options = equipmentOptions,
                    selected = selectedEquipment,
                    onSelected = { selectedEquipment = if (selectedEquipment == it) "" else it }
                )
                if (isCustomExercise && selectedEquipment == "직접 입력") {
                    OutlinedTextField(
                        value = customEquipment,
                        onValueChange = { customEquipment = it },
                        label = { Text("기구 직접 입력") },
                        placeholder = { Text("예: 케이블") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (!isCustomExercise) {
                    ChoiceGrid(
                        title = "세부 타입",
                        options = exercise.baseVariationOptions(),
                        selected = selectedVariation,
                        onSelected = { selectedVariation = it }
                    )
                }
                ChoiceGrid(
                    title = "좌우 방식",
                    options = UNILATERAL_MODE_OPTIONS,
                    selected = selectedUnilateral,
                    onSelected = { selectedUnilateral = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDone(
                        equipment,
                        if (isCustomExercise) {
                            combineVariationAndUnilateral("기본", selectedUnilateral)
                        } else {
                            combineVariationAndUnilateral(selectedVariation, selectedUnilateral)
                        }
                    )
                },
                enabled = canComplete
            ) {
                Text("완료")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

/**
 * Dialog for adding a user-defined strength exercise name.
 * Keep custom exercise creation here so catalog and search behavior remain centralized.
 */
@Composable
internal fun CustomStrengthExerciseDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("신규 운동 추가") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("운동 이름") },
                placeholder = { Text("예: 케이블 풀오버") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(name.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

/**
 * Inline panel inside [StrengthPlanEditScreen] for grouping selected exercises as supersets.
 * This is not a standalone screen.
 */
@Composable
internal fun SupersetEditPanel(
    isSelectionMode: Boolean,
    selectedCount: Int,
    canClearSelectedGroups: Boolean,
    onGroupSelected: () -> Unit,
    onClearSelectedGroups: () -> Unit,
    onCancel: () -> Unit,
) {
    if (!isSelectionMode) {
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "슈퍼세트로 묶을 운동을 선택하세요.",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${selectedCount}개 선택됨",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onGroupSelected,
                    enabled = selectedCount >= 2,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("선택 묶기")
                }
                OutlinedButton(
                    onClick = onClearSelectedGroups,
                    enabled = canClearSelectedGroups,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("묶음 해제")
                }
                TextButton(onClick = onCancel) {
                    Text("취소")
                }
            }
        }
    }
}

@Composable
internal fun PendingSwipeDeleteContainer(
    key: Any,
    enabled: Boolean,
    isPendingDelete: Boolean,
    modifier: Modifier = Modifier,
    onDeleteRequested: () -> Unit,
    onCommitDelete: () -> Unit,
    content: @Composable (Modifier, Boolean) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val viewConfiguration = LocalViewConfiguration.current
    val swipeOffsetX = remember(key) { Animatable(0f) }
    var rowWidth by remember(key) { mutableIntStateOf(0) }
    val deleteThreshold = with(density) { 92.dp.toPx() }
    val maxDragOffset = with(density) { 144.dp.toPx() }
    val touchSlop = viewConfiguration.touchSlop
    val swipeEnabled = enabled && !isPendingDelete

    LaunchedEffect(isPendingDelete, key) {
        if (isPendingDelete) {
            swipeOffsetX.snapTo(0f)
            delay(3_000)
            onCommitDelete()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (swipeEnabled) MaterialTheme.colorScheme.error.copy(alpha = 0.2f) else Color.Transparent)
            .onSizeChanged { rowWidth = it.width }
    ) {
        if (swipeEnabled) {
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .padding(end = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "삭제",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        val contentModifier = Modifier
            .fillMaxWidth()
            .padding(end = if (swipeEnabled) 8.dp else 0.dp)
            .offset { IntOffset(swipeOffsetX.value.roundToInt(), 0) }
            .then(
                if (swipeEnabled) {
                    Modifier.pointerInput(key, rowWidth, touchSlop) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val pointerId = down.id
                            var totalX = 0f
                            var totalY = 0f
                            var isHorizontalSwipe = false
                            var isCanceled = false

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                                if (change.changedToUpIgnoreConsumed()) break

                                val delta = change.positionChange()
                                if (delta.x == 0f && delta.y == 0f) continue
                                totalX += delta.x
                                totalY += delta.y

                                if (!isHorizontalSwipe) {
                                    val isVerticalIntent = abs(totalY) > touchSlop && abs(totalY) > abs(totalX)
                                    val isLeftSwipeIntent = totalX < -touchSlop && abs(totalX) > abs(totalY) * 1.2f
                                    if (isVerticalIntent) {
                                        isCanceled = true
                                        break
                                    }
                                    if (!isLeftSwipeIntent) continue
                                    isHorizontalSwipe = true
                                }

                                change.consume()
                                val nextOffset = (swipeOffsetX.value + delta.x).coerceIn(-maxDragOffset, 0f)
                                scope.launch {
                                    swipeOffsetX.snapTo(nextOffset)
                                }
                            }

                            if (isHorizontalSwipe && !isCanceled) {
                                scope.launch {
                                    if (swipeOffsetX.value <= -deleteThreshold) {
                                        swipeOffsetX.animateTo(
                                            targetValue = -rowWidth.toFloat().coerceAtLeast(maxDragOffset),
                                            animationSpec = tween(160)
                                        )
                                        onDeleteRequested()
                                    } else {
                                        swipeOffsetX.animateTo(0f, animationSpec = spring())
                                    }
                                }
                            } else if (swipeOffsetX.value != 0f) {
                                scope.launch {
                                    swipeOffsetX.animateTo(0f, animationSpec = spring())
                                }
                            }
                        }
                    }
                } else {
                    Modifier
                }
            )
        content(contentModifier, isPendingDelete)
    }
}

@Composable
internal fun StrengthPlanExerciseRow(
    entry: StrengthPlanEntry,
    supersetLabel: String?,
    isSupersetSelectionMode: Boolean,
    isSupersetSelected: Boolean,
    isPendingDelete: Boolean,
    isDragging: Boolean,
    dragHandleModifier: Modifier,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onSupersetToggle: () -> Unit,
    onDelete: () -> Unit,
    onCommitDelete: () -> Unit,
    onRestore: () -> Unit,
) {
    val swipeDeleteEnabled = !isSupersetSelectionMode && !isPendingDelete && !isDragging

    PendingSwipeDeleteContainer(
        key = entry.id,
        enabled = swipeDeleteEnabled,
        isPendingDelete = isPendingDelete,
        modifier = modifier,
        onDeleteRequested = onDelete,
        onCommitDelete = onCommitDelete
    ) { swipeModifier, _ ->
        Card(
            modifier = swipeModifier
                .clickable(
                    onClick = when {
                        isPendingDelete -> onRestore
                        isSupersetSelectionMode -> onSupersetToggle
                        else -> onClick
                    }
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isPendingDelete -> MaterialTheme.colorScheme.surfaceVariant
                    isSupersetSelected -> MaterialTheme.colorScheme.primaryContainer
                    isDragging -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }
            )
        ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .then(dragHandleModifier),
                contentAlignment = Alignment.Center
            ) {
                if (isPendingDelete) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (isSupersetSelectionMode) {
                    Icon(
                        imageVector = if (isSupersetSelected) Icons.Outlined.CheckCircle else Icons.Outlined.FitnessCenter,
                        contentDescription = if (isSupersetSelected) "선택됨" else "선택",
                        tint = if (isSupersetSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Icon(
                        Icons.Outlined.DragIndicator,
                        contentDescription = "드래그해서 순서 변경",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (isPendingDelete) 0.58f else 1f)
            ) {
                supersetLabel?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${entry.records.size}세트 · ${entry.exercise.group}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isPendingDelete) {
                TextButton(onClick = onRestore) {
                    Text("복구")
                }
            }
        }
        }
    }
}

private fun strengthPlanEntriesStateSaver(): Saver<MutableState<List<StrengthPlanEntry>>, String> {
    return Saver(
        save = { state ->
            listOf(
                StrengthWorkoutPlan(
                    id = 0,
                    name = "",
                    entries = state.value
                )
            ).toJsonString()
        },
        restore = { saved ->
            mutableStateOf(saved.toStrengthWorkoutPlans().firstOrNull()?.entries.orEmpty())
        }
    )
}

@Composable
internal fun StrengthExerciseDetailEditor(
    entry: StrengthPlanEntry,
    isChangingExercise: Boolean,
    onEntryChange: (StrengthPlanEntry) -> Unit,
    onChangingExerciseChange: (Boolean) -> Unit,
    onAddExercise: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    fun updateRecords(records: List<StrengthSetRecord>) {
        onEntryChange(entry.withRecords(records))
    }

    var isTypeDialogVisible by remember(entry.id) { mutableStateOf(false) }
    var exerciseForChange by remember(entry.id) { mutableStateOf<StrengthExercise?>(null) }
    var exerciseForChangeSearchQuery by remember(entry.id) { mutableStateOf("") }
    var isCustomExerciseDialogVisible by remember(entry.id) { mutableStateOf(false) }

    if (isTypeDialogVisible) {
        StrengthExerciseTypeDialog(
            entry = entry,
            exercise = entry.exercise,
            initialEquipment = entry.equipment,
            initialVariation = entry.variation,
            onDismiss = { isTypeDialogVisible = false },
            onDone = { equipment, variation ->
                isTypeDialogVisible = false
                onEntryChange(
                    entry.copy(
                        equipment = equipment,
                        variation = variation
                    )
                )
            }
        )
    }

    exerciseForChange?.let { exercise ->
        StrengthExerciseTypeDialog(
            entry = entry,
            exercise = exercise,
            initialEquipment = exercise.equipmentOptions.firstOrNull().orEmpty(),
            initialVariation = exercise.baseVariationOptions().firstOrNull().orEmpty(),
            initialSearchQuery = exerciseForChangeSearchQuery,
            onDismiss = { exerciseForChange = null },
            onDone = { equipment, variation ->
                exerciseForChange = null
                onChangingExerciseChange(false)
                onEntryChange(
                    entry.copy(
                        exercise = exercise,
                        equipment = equipment,
                        variation = variation
                    )
                )
            }
        )
    }

    if (isCustomExerciseDialogVisible) {
        CustomStrengthExerciseDialog(
            onDismiss = { isCustomExerciseDialogVisible = false },
            onAdd = { name ->
                isCustomExerciseDialogVisible = false
                exerciseForChangeSearchQuery = ""
                exerciseForChange = customStrengthExercise(name)
            }
        )
    }

    if (isChangingExercise) {
        StrengthExerciseListScreen(
            modifier = modifier,
            onAddCustomExercise = { isCustomExerciseDialogVisible = true },
            onExerciseSelected = { exercise, searchQuery ->
                exerciseForChangeSearchQuery = searchQuery
                exerciseForChange = exercise
            }
        )
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = entry.exercise.group,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isTypeDialogVisible = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("타입 변경", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            OutlinedButton(
                                onClick = { onChangingExerciseChange(true) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("운동 변경", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
            itemsIndexed(entry.records, key = { _, record -> record.id }) { index, record ->
                StrengthSetRecordRow(
                    index = index,
                    record = record,
                    modifier = Modifier.animateItem(),
                    isUnilateral = entry.isUnilateral(),
                    weightUnit = entry.weightInputUnitLabel(),
                    showCompletion = false,
                    onDelete = if (entry.records.size > 1) {
                        {
                            updateRecords(entry.records.filterIndexed { recordIndex, _ -> recordIndex != index })
                        }
                    } else {
                        null
                    },
                    onRecordChange = { next ->
                        onEntryChange(entry.withPropagatedRecordChange(index, next))
                    }
                )
            }
            item {
                OutlinedButton(
                    onClick = {
                        updateRecords(entry.records + defaultStrengthSetRecord(entry))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("세트 추가")
                }
            }
        }
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            tonalElevation = 3.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onAddExercise,
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("운동 추가", maxLines = 1)
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("운동 삭제", maxLines = 1)
                }
            }
        }
    }
}

/**
 * Dialog for changing equipment or variation of an existing strength entry.
 * Use this when only the type changes; use [StrengthExercisePickerScreen] when the exercise itself changes.
 */
@Composable
internal fun StrengthExerciseTypeDialog(
    entry: StrengthPlanEntry,
    exercise: StrengthExercise,
    initialEquipment: String,
    initialVariation: String,
    initialSearchQuery: String = "",
    onDismiss: () -> Unit,
    onDone: (String, String) -> Unit,
) {
    val isCustomExercise = exercise.group == "사용자 추가" || exercise.id.startsWith("custom_")
    val equipmentOptions = remember(exercise.id) { exercise.equipmentOptionsWithBodyweight() }
    val inferredEquipment = remember(exercise.id, initialSearchQuery, equipmentOptions) {
        exercise.inferEquipmentFromSearch(initialSearchQuery, equipmentOptions)
    }
    val inferredVariation = remember(exercise.id, initialSearchQuery) {
        exercise.inferVariationFromSearch(initialSearchQuery)
    }
    val inferredUnilateral = remember(exercise.id, initialSearchQuery) {
        exercise.inferUnilateralFromSearch(initialSearchQuery)
    }
    val initialEquipmentSelection = remember(exercise.id, initialEquipment, initialSearchQuery) {
        val preferredEquipment = inferredEquipment ?: initialEquipment
        when {
            preferredEquipment.isBlank() -> ""
            preferredEquipment in equipmentOptions -> preferredEquipment
            isCustomExercise -> "직접 입력"
            else -> preferredEquipment
        }
    }
    val initialCustomEquipment = remember(exercise.id, initialEquipment, initialSearchQuery) {
        val preferredEquipment = inferredEquipment ?: initialEquipment
        preferredEquipment.takeIf { it.isNotBlank() && it !in equipmentOptions }.orEmpty()
    }
    val variationParts = remember(exercise.id, initialVariation, initialSearchQuery) {
        val preferredVariation = inferredVariation?.let {
            combineVariationAndUnilateral(it, inferredUnilateral ?: "양쪽")
        } ?: initialVariation
        splitVariationAndUnilateral(exercise, preferredVariation)
    }
    var selectedEquipment by remember(exercise.id, initialEquipment, initialSearchQuery) { mutableStateOf(initialEquipmentSelection) }
    var customEquipment by remember(exercise.id, initialEquipment, initialSearchQuery) { mutableStateOf(initialCustomEquipment) }
    var selectedVariation by remember(exercise.id, initialVariation, initialSearchQuery) {
        mutableStateOf(variationParts.first.ifBlank { exercise.baseVariationOptions().firstOrNull().orEmpty() })
    }
    var selectedUnilateral by remember(exercise.id, initialVariation, initialSearchQuery) {
        mutableStateOf(variationParts.second.ifBlank { "양쪽" })
    }
    val equipment = if (selectedEquipment == "직접 입력") customEquipment.trim() else selectedEquipment
    val canComplete = selectedEquipment != "직접 입력" || equipment.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${exercise.nameKo} 타입 변경") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = exercise.group,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ChoiceGrid(
                    title = "기구",
                    options = equipmentOptions,
                    selected = selectedEquipment,
                    onSelected = { selectedEquipment = if (selectedEquipment == it) "" else it }
                )
                if (isCustomExercise && selectedEquipment == "직접 입력") {
                    OutlinedTextField(
                        value = customEquipment,
                        onValueChange = { customEquipment = it },
                        label = { Text("기구 직접 입력") },
                        placeholder = { Text("예: 케이블") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (!isCustomExercise) {
                    ChoiceGrid(
                        title = "세부 타입",
                        options = exercise.baseVariationOptions(),
                        selected = selectedVariation,
                        onSelected = { selectedVariation = it }
                    )
                }
                ChoiceGrid(
                    title = "좌우 방식",
                    options = UNILATERAL_MODE_OPTIONS,
                    selected = selectedUnilateral,
                    onSelected = { selectedUnilateral = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDone(
                        equipment,
                        if (isCustomExercise) {
                            combineVariationAndUnilateral("기본", selectedUnilateral)
                        } else {
                            combineVariationAndUnilateral(selectedVariation, selectedUnilateral)
                        }
                    )
                },
                enabled = canComplete
            ) {
                Text("완료")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

/**
 * Nested sub-screen for replacing an exercise during editing or active workout setup.
 * It intentionally mirrors the add-exercise picker to avoid another exercise selection UI.
 */
@Composable
internal fun StrengthExercisePickerScreen(
    entry: StrengthPlanEntry,
    onEntryChange: (StrengthPlanEntry) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember(entry.id) { mutableStateOf("") }
    val variationParts = remember(entry.exercise.id, entry.variation) {
        splitVariationAndUnilateral(entry.exercise, entry.variation)
    }
    val candidates = remember(searchQuery) {
        strengthExerciseCatalog
            .asSequence()
            .filter { exercise -> exercise.matchesSearch(searchQuery) }
            .take(12)
            .toList()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("운동 검색") },
                singleLine = true
            )
        }
        items(candidates, key = { it.id }) { exercise ->
            ExerciseSearchRow(
                exercise = exercise,
                title = exercise.searchResultTitle(searchQuery),
                selected = exercise.id == entry.exercise.id,
                onClick = {
                    onEntryChange(
                        entry.copy(
                            exercise = exercise,
                            equipment = exercise.equipmentOptions.first(),
                            variation = exercise.baseVariationOptions().first()
                        )
                    )
                }
            )
        }
        item {
            ChoiceGrid(
                title = "기구",
                options = entry.exercise.equipmentOptionsWithBodyweight(),
                selected = entry.equipment,
                onSelected = { onEntryChange(entry.copy(equipment = if (entry.equipment == it) "" else it)) }
            )
        }
        item {
            ChoiceGrid(
                title = "세부 타입",
                options = entry.exercise.baseVariationOptions(),
                selected = variationParts.first,
                onSelected = {
                    onEntryChange(
                        entry.copy(variation = combineVariationAndUnilateral(it, variationParts.second))
                    )
                }
            )
        }
        item {
            ChoiceGrid(
                title = "좌우 방식",
                options = UNILATERAL_MODE_OPTIONS,
                selected = variationParts.second,
                onSelected = {
                    onEntryChange(
                        entry.copy(variation = combineVariationAndUnilateral(variationParts.first, it))
                    )
                }
            )
        }
        item {
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("완료")
            }
        }
    }
}

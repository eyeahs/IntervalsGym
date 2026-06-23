package com.lighthousepark.intervalsgym.workout.ui

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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
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

/**
 * Route owner for [ROUTE_WORKOUT_PLAN].
 * This displays an Intervals/local plan or result detail and starts routed strength/running execution when supported.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WorkoutPlanScreen(
    apiKey: String,
    plan: TrainingItem?,
    onStartStrengthPlan: (StrengthWorkoutPlan) -> Unit,
    onStrengthWorkoutUploaded: (CompletedStrengthWorkout) -> Unit,
    onPlanDeleted: (TrainingItem) -> Unit,
    onBack: () -> Unit,
) {
    val screenContext = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember(apiKey) { IntervalsRepository(apiKey) }
    val blocks = remember(plan) { plan?.blocks.orEmpty() }
    val graphBlocks = remember(blocks, plan?.description, plan?.name, plan?.type) {
        when (plan?.sportType()) {
            TrainingSportType.RUNNING -> blocks.withRunningGraphContext(plan.description, plan.name)
            TrainingSportType.CYCLING -> blocks.withCyclingGraphContext(plan.description)
            else -> blocks
        }
    }
    val totalSeconds = remember(blocks, plan) { blocks.sumOf { it.durationSeconds }.takeIf { it > 0 } ?: (plan?.durationSeconds ?: 0) }
    val intervalStrengthPlan = remember(plan?.matchedStrengthPlan, plan?.description) {
        plan?.matchedStrengthPlan ?: plan?.description.toIntervalsGymStrengthPlan()
    }
    var localWorkout by remember(plan?.matchedStrengthWorkout?.id) { mutableStateOf(plan?.matchedStrengthWorkout) }
    val isWeightTrainingItem = remember(plan, localWorkout, intervalStrengthPlan) {
        localWorkout != null ||
            intervalStrengthPlan != null ||
            plan?.isWeightTrainingItem() == true
    }
    val isRunningWorkoutPlan = remember(plan, graphBlocks, isWeightTrainingItem) {
        plan?.sportType() == TrainingSportType.RUNNING &&
            plan?.isLocalOnlyRunningResult != true &&
            plan.actualRunningBlocks.isEmpty() &&
            !isWeightTrainingItem &&
            graphBlocks.isNotEmpty()
    }
    var isRunningSession by rememberSaveable(plan?.id) { mutableStateOf(false) }
    var isUploadingStrengthWorkout by remember { mutableStateOf(false) }
    var uploadedInThisScreen by remember(plan?.matchedStrengthWorkout?.id) { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var isDeleteConfirmVisible by remember { mutableStateOf(false) }
    var isDeletingPlan by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    val canUploadLocalWorkout = localWorkout != null &&
        apiKey.isNotBlank() &&
        !uploadedInThisScreen &&
        (!localWorkout!!.uploadedToIntervals || plan?.isLocalOnlyStrengthResult == true)
    val localRunningGraphBlocks = remember(plan?.actualRunningBlocks) { plan?.actualRunningBlocks.orEmpty() }
    val detailTotalSeconds = remember(plan?.durationSeconds, totalSeconds, localRunningGraphBlocks) {
        if (plan?.isLocalOnlyRunningResult == true || localRunningGraphBlocks.isNotEmpty()) {
            plan?.durationSeconds ?: localRunningGraphBlocks.sumOf { it.durationSeconds }
        } else {
            totalSeconds
        }
    }
    val heartRateState = rememberHeartRateSensorState()
    var isHeartRatePickerVisible by remember { mutableStateOf(false) }
    val heartRatePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            isHeartRatePickerVisible = true
            heartRateState.startScan()
        } else {
            heartRateState.onPermissionDenied()
        }
    }

    fun openHeartRatePicker() {
        val missingPermissions = heartRateState.missingPermissions()
        if (missingPermissions.isNotEmpty()) {
            heartRatePermissionLauncher.launch(missingPermissions)
        } else {
            isHeartRatePickerVisible = true
            heartRateState.startScan()
        }
    }

    fun deleteLocalRunningWorkout() {
        val workoutId = plan?.remoteId ?: return
        deleteRunningWorkoutHistory(
            prefs = screenContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
            workoutId = workoutId
        )
        onBack()
    }

    fun uploadLocalWorkout() {
        val workout = localWorkout ?: return
        if (apiKey.isBlank()) {
            uploadError = "Intervals.icu 업데이트는 로그인 후 사용할 수 있습니다."
            uploadMessage = null
            return
        }
        scope.launch {
            isUploadingStrengthWorkout = true
            uploadMessage = null
            uploadError = null
            try {
                repository.uploadStrengthWorkout(workout.toStrengthWorkoutSession())
                val uploaded = workout.copy(uploadedToIntervals = true)
                localWorkout = uploaded
                uploadedInThisScreen = true
                onStrengthWorkoutUploaded(uploaded)
                uploadMessage = "Intervals.icu에 업로드했습니다."
            } catch (error: Exception) {
                uploadError = error.message ?: "업로드하지 못했습니다."
            } finally {
                isUploadingStrengthWorkout = false
            }
        }
    }

    fun deleteCalendarPlan() {
        val targetPlan = plan ?: return
        scope.launch {
            isDeletingPlan = true
            deleteError = null
            try {
                val prefs = screenContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                if (apiKey.isNotBlank() && !targetPlan.id.startsWith("local-")) {
                    repository.deleteCalendarPlan(targetPlan.remoteId)
                    removeCalendarPlanFromIntervalsCaches(prefs, apiKey, targetPlan)
                }
                removeScheduledStrengthPlan(prefs, targetPlan)
                onPlanDeleted(targetPlan)
            } catch (error: Exception) {
                deleteError = error.message ?: "Plan을 삭제하지 못했습니다."
            } finally {
                isDeletingPlan = false
            }
        }
    }

    if (isRunningSession && plan != null) {
        RunningWorkoutSessionScreen(
            apiKey = apiKey,
            planName = plan.name.ifBlank { "Running Plan" },
            blocks = graphBlocks,
            totalSeconds = totalSeconds,
            isHeartRateConnected = heartRateState.isConnected,
            heartRateBpm = heartRateState.heartRateBpm,
            heartRateSamples = heartRateState.heartRateSamples,
            onHeartRateClick = ::openHeartRatePicker,
            onBack = { isRunningSession = false },
            onWorkoutFinished = {
                isRunningSession = false
                onBack()
            }
        )
        if (isHeartRatePickerVisible) {
            HeartRateDevicePickerDialog(
                state = heartRateState,
                onDismiss = {
                    heartRateState.stopScan()
                    isHeartRatePickerVisible = false
                },
                onDeviceSelected = { device ->
                    heartRateState.connect(device)
                },
                onRescan = { heartRateState.startScan() },
                onDisconnect = { heartRateState.disconnect() }
            )
        }
        return
    }

    if (isHeartRatePickerVisible) {
        HeartRateDevicePickerDialog(
            state = heartRateState,
            onDismiss = {
                heartRateState.stopScan()
                isHeartRatePickerVisible = false
            },
            onDeviceSelected = { device ->
                heartRateState.connect(device)
            },
            onRescan = { heartRateState.startScan() },
            onDisconnect = { heartRateState.disconnect() }
        )
    }

    if (isDeleteConfirmVisible && plan != null) {
        AlertDialog(
            onDismissRequest = { if (!isDeletingPlan) isDeleteConfirmVisible = false },
            title = { Text("Plan 삭제") },
            text = {
                Text(
                    text = plan.plannedWorkoutDeleteConfirmMessage()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDeleteConfirmVisible = false
                        deleteCalendarPlan()
                    },
                    enabled = !isDeletingPlan
                ) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { isDeleteConfirmVisible = false },
                    enabled = !isDeletingPlan
                ) {
                    Text("취소")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = plan?.name ?: "Running Plan",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    if (plan?.isPlan == true) {
                        IconButton(
                            onClick = { isDeleteConfirmVisible = true },
                            enabled = !isDeletingPlan
                        ) {
                            if (isDeletingPlan) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Plan 삭제",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    if (canUploadLocalWorkout) {
                        IconButton(
                            onClick = ::uploadLocalWorkout,
                            enabled = !isUploadingStrengthWorkout
                        ) {
                            if (isUploadingStrengthWorkout) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Outlined.CloudUpload, contentDescription = "Intervals.icu 업로드")
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (intervalStrengthPlan != null || isRunningWorkoutPlan) {
                Surface(
                    modifier = Modifier.navigationBarsPadding(),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isRunningWorkoutPlan) {
                            OutlinedButton(
                                onClick = ::openHeartRatePicker,
                                modifier = Modifier
                                    .weight(0.42f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = when {
                                            heartRateState.isConnected -> heartRateState.connectedDeviceName.orEmpty().ifBlank { "심박계" }
                                            heartRateState.isConnecting -> "연결 중"
                                            else -> "심박계"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (heartRateState.isConnected) {
                                            heartRateState.heartRateBpm?.let { "$it bpm" } ?: "-- bpm"
                                        } else {
                                            "연결"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        Button(
                            onClick = {
                                if (intervalStrengthPlan != null) {
                                    onStartStrengthPlan(intervalStrengthPlan)
                                } else {
                                    isRunningSession = true
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(
                                imageVector = if (intervalStrengthPlan != null) Icons.Outlined.FitnessCenter else Icons.AutoMirrored.Outlined.DirectionsRun,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("운동 시작")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (plan == null) {
            EmptyView(message = "선택된 항목이 없습니다.")
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = if (intervalStrengthPlan != null || isRunningWorkoutPlan) 96.dp else 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                TrainingItemDetailCard(
                    item = plan,
                    totalSeconds = detailTotalSeconds,
                    isStrengthPlan = intervalStrengthPlan != null,
                    strengthWorkout = localWorkout,
                    uploadMessage = uploadMessage,
                    uploadError = uploadError ?: deleteError
                )
            }
            localWorkout?.let { workout ->
                item {
                    LocalStrengthWorkoutDetailSection(
                        workout = workout
                    )
                }
            }
            if (!isWeightTrainingItem) {
                plan.detailPlanDescription().takeIf { it.isNotBlank() }?.let { description ->
                    item {
                        DetailSection(title = "설명") {
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            if (!isWeightTrainingItem && graphBlocks.isNotEmpty()) {
                item {
                    if (plan.isLocalOnlyRunningResult) {
                        PlanWorkoutGraph(
                            blocks = graphBlocks,
                            totalSeconds = totalSeconds,
                            sportType = plan.sportType(),
                            title = "Plan 그래프"
                        )
                    } else {
                        PlanWorkoutGraph(
                            blocks = graphBlocks,
                            totalSeconds = totalSeconds,
                            sportType = plan.sportType()
                        )
                    }
                }
            }
            if (localRunningGraphBlocks.isNotEmpty()) {
                item {
                    LocalRunningWorkoutGraphSection(
                        blocks = localRunningGraphBlocks,
                        totalSeconds = localRunningGraphBlocks.sumOf { it.durationSeconds },
                        onDelete = ::deleteLocalRunningWorkout
                    )
                }
            }
        }
    }
}

/**
 * Dialog shared by workout detail and running execution for BLE heart-rate device selection.
 * Keep scan/connect/disconnect UI here rather than adding a separate heart-rate screen.
 */
@Composable
internal fun HeartRateDevicePickerDialog(
    state: HeartRateSensorState,
    onDismiss: () -> Unit,
    onDeviceSelected: (HeartRateDevice) -> Unit,
    onRescan: () -> Unit,
    onDisconnect: () -> Unit,
) {
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.isConnecting, state.connectionDeadlineMillis) {
        while (state.isConnecting) {
            nowMillis = System.currentTimeMillis()
            delay(1_000L)
        }
    }
    val connectionRemainingSeconds = if (state.isConnecting && state.connectionDeadlineMillis > 0L) {
        (((state.connectionDeadlineMillis - nowMillis).coerceAtLeast(0L) + 999L) / 1000L).toInt()
    } else {
        0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("심박계 연결") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.isConnecting) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "심박계 연결 대기",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = state.connectedDeviceName.orEmpty().ifBlank { "심박계" },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${connectionRemainingSeconds}초 남음",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
                if (state.isConnected) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "연결된 심박계",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = state.connectedDeviceName.orEmpty().ifBlank { "심박계" },
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = state.heartRateBpm?.let { "$it bpm" } ?: "-- bpm",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                state.statusMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (state.isScanning) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("심박계를 검색 중입니다.")
                    }
                }
                if (state.devices.isEmpty() && !state.isScanning) {
                    Text(
                        text = "검색된 심박계가 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LazyColumn(
                    modifier = Modifier.heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.devices, key = { it.address }) { device ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDeviceSelected(device) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = device.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = device.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onRescan) {
                Text(if (state.isScanning) "검색 중" else "다시 검색")
            }
        },
        dismissButton = {
            Row {
                if (state.isConnected || state.isConnecting) {
                    TextButton(onClick = onDisconnect) {
                        Text("연결 해제")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("닫기")
                }
            }
        }
    )
}

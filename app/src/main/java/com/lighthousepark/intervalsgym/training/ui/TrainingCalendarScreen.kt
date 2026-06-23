package com.lighthousepark.intervalsgym.training.ui

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

internal data class SummaryDetail(
    val text: String,
    val icon: ImageVector? = null,
)

/**
 * Route owner for [ROUTE_WEEK].
 * This owns day/week/month training calendar UI, local/Intervals merge display, and the main FAB actions.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun WeeklyTrainingScreen(
    apiKey: String,
    strengthPlans: List<StrengthWorkoutPlan>,
    deletedCalendarPlanIds: Set<String>,
    onPlanSelected: (TrainingItem) -> Unit,
    onIntervalStrengthPlanSelected: (TrainingItem?, StrengthWorkoutPlan) -> Unit,
    onStrengthWorkout: () -> Unit,
    onLoginClick: () -> Unit,
    onLogout: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var localStrengthHistory by remember { mutableStateOf(loadCompletedStrengthWorkoutHistory(prefs)) }
    var localRunningHistory by remember { mutableStateOf(loadCompletedRunningWorkoutHistory(prefs)) }
    var localScheduledStrengthPlans by remember { mutableStateOf(loadScheduledStrengthPlans(prefs)) }
    val repository = remember(apiKey) { IntervalsRepository(apiKey) }
    val baseDate = remember { LocalDate.now() }
    val initialPage = remember { Int.MAX_VALUE / 2 }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { Int.MAX_VALUE })
    var calendarMode by rememberSaveable { mutableStateOf(TrainingCalendarMode.WEEK) }
    val selectedRange = calendarMode.rangeForPage(baseDate, (pagerState.settledPage - initialPage).toLong())
    var state by remember {
        mutableStateOf(
            WeekUiState(
                weekStart = selectedRange.start,
                weekEnd = selectedRange.end,
                isLoading = true
            )
        )
    }
    var showCalendar by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showFabActions by remember { mutableStateOf(false) }
    var showWorkoutActionSheet by remember { mutableStateOf(false) }
    var showPlanSaveSheet by remember { mutableStateOf(false) }
    var planSaveMessage by remember { mutableStateOf<String?>(null) }
    var planSaveError by remember { mutableStateOf<String?>(null) }
    var savingPlanId by remember { mutableStateOf<Int?>(null) }
    var planSaveDateText by rememberSaveable { mutableStateOf(baseDate.toString()) }
    var didInitialIntervalsSync by rememberSaveable(apiKey) { mutableStateOf(false) }

    fun refresh(
        targetRange: TrainingDateRange = selectedRange,
        forceSync: Boolean = false,
    ) {
        localStrengthHistory = loadCompletedStrengthWorkoutHistory(prefs)
        localRunningHistory = loadCompletedRunningWorkoutHistory(prefs)
        localScheduledStrengthPlans = loadScheduledStrengthPlans(prefs)
        if (apiKey.isBlank()) {
            state = state.copy(
                weekStart = targetRange.start,
                weekEnd = targetRange.end,
                activities = emptyList<TrainingItem>()
                    .withLocalStrengthResults(localStrengthHistory, targetRange.start, targetRange.end)
                    .withLocalRunningResults(localRunningHistory, targetRange.start, targetRange.end),
                plans = emptyList(),
                isLoading = false,
                error = null
            )
            return
        }
        val cachedData = loadIntervalsWeekCache(prefs, apiKey, targetRange.start, targetRange.end)
        if (cachedData != null) {
            state = state.copy(
                weekStart = targetRange.start,
                weekEnd = targetRange.end,
                activities = cachedData.activities
                    .withLocalStrengthResults(localStrengthHistory, targetRange.start, targetRange.end)
                    .withLocalRunningResults(localRunningHistory, targetRange.start, targetRange.end),
                plans = cachedData.plans,
                isLoading = false,
                error = null
            )
        }
        if (cachedData != null && !forceSync) {
            return
        }
        scope.launch {
            if (cachedData == null) {
                state = state.copy(weekStart = targetRange.start, weekEnd = targetRange.end, isLoading = true, error = null)
            }
            try {
                val data = repository.loadWeek(targetRange.start, targetRange.end)
                saveIntervalsWeekCache(prefs, apiKey, targetRange.start, targetRange.end, data)
                val visibleRange = calendarMode.rangeForPage(baseDate, (pagerState.settledPage - initialPage).toLong())
                if (visibleRange != targetRange) return@launch
                state = state.copy(
                    weekStart = targetRange.start,
                    weekEnd = targetRange.end,
                    activities = data.activities
                        .withLocalStrengthResults(localStrengthHistory, targetRange.start, targetRange.end)
                        .withLocalRunningResults(localRunningHistory, targetRange.start, targetRange.end),
                    plans = data.plans,
                    isLoading = false,
                    error = null
                )
            } catch (error: Exception) {
                val visibleRange = calendarMode.rangeForPage(baseDate, (pagerState.settledPage - initialPage).toLong())
                if (visibleRange != targetRange) return@launch
                if (cachedData == null) {
                    state = state.copy(
                        weekStart = targetRange.start,
                        weekEnd = targetRange.end,
                        isLoading = false,
                        error = error.message ?: "데이터를 불러오지 못했습니다."
                    )
                }
            }
        }
    }

    fun selectedPlanDate(): LocalDate {
        return if (!baseDate.isBefore(selectedRange.start) && !baseDate.isAfter(selectedRange.end)) {
            baseDate
        } else {
            selectedRange.start
        }
    }

    fun savePlanToCalendar(plan: StrengthWorkoutPlan, targetDate: LocalDate) {
        val localPlan = ScheduledStrengthPlan(
            id = plan.scheduledStrengthPlanId(targetDate),
            date = targetDate,
            plan = plan,
            uploadedToIntervals = false,
            externalId = plan.intervalsPlanExternalId(targetDate)
        )
        upsertScheduledStrengthPlan(prefs, localPlan)
        localScheduledStrengthPlans = loadScheduledStrengthPlans(prefs)
        planSaveError = null
        if (apiKey.isBlank()) {
            planSaveMessage = "${targetDate.monthValue}/${targetDate.dayOfMonth} 로컬에 저장됨"
            return
        }

        savingPlanId = plan.id
        planSaveMessage = "Intervals.icu에 업로드 중..."
        scope.launch {
            try {
                repository.uploadStrengthPlan(plan, targetDate)
                upsertScheduledStrengthPlan(prefs, localPlan.copy(uploadedToIntervals = true))
                localScheduledStrengthPlans = loadScheduledStrengthPlans(prefs)
                planSaveMessage = "${targetDate.monthValue}/${targetDate.dayOfMonth} Intervals.icu 업로드됨"
                planSaveError = null
                refresh(selectedRange, forceSync = true)
            } catch (error: Exception) {
                planSaveMessage = "${targetDate.monthValue}/${targetDate.dayOfMonth} 로컬에 저장됨"
                planSaveError = error.message ?: "Intervals.icu 업로드에 실패했습니다."
            } finally {
                savingPlanId = null
            }
        }
    }

    LaunchedEffect(apiKey, calendarMode, selectedRange.start, selectedRange.end) {
        if (apiKey.isNotBlank() && !didInitialIntervalsSync) {
            didInitialIntervalsSync = true
            refresh(selectedRange, forceSync = true)
        } else {
            refresh(selectedRange)
        }
    }

    DisposableEffect(context, selectedRange.start, selectedRange.end) {
        val lifecycle = (context as? LifecycleOwner)?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                localStrengthHistory = loadCompletedStrengthWorkoutHistory(prefs)
                localRunningHistory = loadCompletedRunningWorkoutHistory(prefs)
            }
        }
        lifecycle?.addObserver(observer)
        onDispose {
            lifecycle?.removeObserver(observer)
        }
    }

    if (showCalendar) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedRange.start.toEpochMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showCalendar = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDate = datePickerState.selectedDateMillis?.toLocalDateFromMillis()
                        if (selectedDate != null) {
                            val targetPage = initialPage + calendarMode.pageOffsetForDate(baseDate, selectedDate).toInt()
                            scope.launch {
                                pagerState.animateScrollToPage(targetPage)
                            }
                        }
                        showCalendar = false
                    }
                ) {
                    Text("이동")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCalendar = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showWorkoutActionSheet) {
        WorkoutActionBottomSheet(
            onDismiss = { showWorkoutActionSheet = false },
            onRunningClick = {},
            onStrengthClick = {
                showWorkoutActionSheet = false
                onStrengthWorkout()
            }
        )
    }

    if (showPlanSaveSheet) {
        val planSaveDate = runCatching { LocalDate.parse(planSaveDateText) }.getOrElse { selectedPlanDate() }
        StrengthPlanSaveBottomSheet(
            plans = strengthPlans,
            selectedDate = planSaveDate,
            savingPlanId = savingPlanId,
            message = planSaveMessage,
            error = planSaveError,
            onDismiss = { showPlanSaveSheet = false },
            onDateSelected = { planSaveDateText = it.toString() },
            onPlanSelected = { plan -> savePlanToCalendar(plan, planSaveDate) }
        )
    }

    Scaffold(
        floatingActionButton = {
            WeeklyTrainingFabMenu(
                expanded = showFabActions,
                onExpandedChange = { showFabActions = it },
                onWorkoutClick = {
                    showFabActions = false
                    showWorkoutActionSheet = true
                },
                onPlanSaveClick = {
                    showFabActions = false
                    planSaveMessage = null
                    planSaveError = null
                    planSaveDateText = selectedPlanDate().toString()
                    showPlanSaveSheet = true
                },
                modifier = Modifier.navigationBarsPadding()
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showCalendar = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(calendarMode.title)
                        Text(
                            text = calendarMode.dateLabel(selectedRange),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (baseDate < selectedRange.start || baseDate > selectedRange.end) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(initialPage)
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_today_word),
                                contentDescription = "오늘로 이동",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = { calendarMode = calendarMode.next() }
                    ) {
                        CalendarModeIcon(
                            mode = calendarMode,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Box {
                        IconButton(onClick = { showSettingsMenu = true }) {
                            Icon(imageVector = Icons.Outlined.Settings, contentDescription = "설정")
                        }
                        DropdownMenu(
                            expanded = showSettingsMenu,
                            onDismissRequest = { showSettingsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("새로고침") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null)
                                },
                                onClick = {
                                    showSettingsMenu = false
                                    refresh(forceSync = true)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (apiKey.isBlank()) "Intervals 로그인" else "Intervals 로그아웃") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                                },
                                onClick = {
                                    showSettingsMenu = false
                                    if (apiKey.isBlank()) {
                                        onLoginClick()
                                    } else {
                                        onLogout()
                                    }
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            val pageRange = calendarMode.rangeForPage(baseDate, (page - initialPage).toLong())
            val isLoadedPage = state.weekStart == pageRange.start && state.weekEnd == pageRange.end
            val cachedPageData = if (apiKey.isBlank() || isLoadedPage) {
                null
            } else {
                loadIntervalsWeekCache(prefs, apiKey, pageRange.start, pageRange.end)
            }
            val remotePageActivities = if (apiKey.isBlank()) {
                emptyList()
            } else if (isLoadedPage) {
                state.activities
            } else if (cachedPageData != null) {
                cachedPageData.activities
            } else {
                emptyList()
            }
            val pageActivities = remotePageActivities
                .withLocalStrengthResults(localStrengthHistory, pageRange.start, pageRange.end)
                .withLocalRunningResults(localRunningHistory, pageRange.start, pageRange.end)
            val remotePagePlans = if (apiKey.isBlank()) {
                emptyList()
            } else if (isLoadedPage) {
                state.plans
            } else if (cachedPageData != null) {
                cachedPageData.plans
            } else {
                emptyList()
            }.filterNot { it.id in deletedCalendarPlanIds || it.remoteId in deletedCalendarPlanIds }
            val pagePlans = remotePagePlans.withLocalStrengthPlans(
                scheduledPlans = localScheduledStrengthPlans,
                start = pageRange.start,
                end = pageRange.end
            )
            val sortedPageItems = mergeTrainingPlansAndResults(
                activities = pageActivities,
                plans = pagePlans
            ).sortedWith(
                compareBy<TrainingItem> { it.date }
                    .thenBy { it.timeLabel }
                    .thenBy { if (it.isPlan) 0 else 1 }
            )
            val initialTrainingListScrollDate = baseDate.takeIf {
                calendarMode == TrainingCalendarMode.WEEK &&
                    !it.isBefore(pageRange.start) &&
                    !it.isAfter(pageRange.end)
            }

            Column(modifier = Modifier.fillMaxSize()) {
                if (calendarMode == TrainingCalendarMode.MONTH) {
                    WeekSummary(
                        activities = pageActivities,
                        plans = pagePlans,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
                when {
                    apiKey.isBlank() -> {
                        if (calendarMode == TrainingCalendarMode.MONTH) {
                            MonthlyTrainingCalendar(
                                range = pageRange,
                                items = sortedPageItems,
                                onPlanSelected = onPlanSelected,
                                onIntervalStrengthPlanSelected = onIntervalStrengthPlanSelected
                            )
                        } else {
                            TrainingList(
                                days = pageRange.days(),
                                items = sortedPageItems,
                                emptyMessage = "주간 훈련 계획 없음",
                                onPlanSelected = onPlanSelected,
                                onIntervalStrengthPlanSelected = onIntervalStrengthPlanSelected,
                                initialScrollDate = initialTrainingListScrollDate,
                                header = {
                                    WeekSummary(
                                        activities = pageActivities,
                                        plans = pagePlans
                                    )
                                }
                            )
                        }
                    }
                    apiKey.isNotBlank() && cachedPageData == null && (!isLoadedPage || state.isLoading) -> LoadingView()
                    apiKey.isNotBlank() && cachedPageData == null && state.error != null -> ErrorView(message = state.error.orEmpty(), onRetry = { refresh(pageRange) })
                    else -> {
                        if (calendarMode == TrainingCalendarMode.MONTH) {
                            MonthlyTrainingCalendar(
                                range = pageRange,
                                items = sortedPageItems,
                                onPlanSelected = onPlanSelected,
                                onIntervalStrengthPlanSelected = onIntervalStrengthPlanSelected
                            )
                        } else {
                            TrainingList(
                                days = pageRange.days(),
                                items = sortedPageItems,
                                emptyMessage = "주간 훈련 계획 없음",
                                onPlanSelected = onPlanSelected,
                                onIntervalStrengthPlanSelected = onIntervalStrengthPlanSelected,
                                initialScrollDate = initialTrainingListScrollDate,
                                header = {
                                    WeekSummary(
                                        activities = pageActivities,
                                        plans = pagePlans
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun WeeklyTrainingFabMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onWorkoutClick: () -> Unit,
    onPlanSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(180),
        label = "weekly-fab-rotation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(150)) + slideInVertically(
                animationSpec = tween(180),
                initialOffsetY = { it / 2 }
            ),
            exit = fadeOut(animationSpec = tween(120)) + slideOutVertically(
                animationSpec = tween(140),
                targetOffsetY = { it / 2 }
            )
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FabActionButton(
                    text = "운동 실행",
                    icon = Icons.Outlined.FitnessCenter,
                    onClick = onWorkoutClick
                )
                FabActionButton(
                    text = "Plan 업로드",
                    icon = Icons.Outlined.Schedule,
                    onClick = onPlanSaveClick
                )
            }
        }
        FloatingActionButton(
            onClick = { onExpandedChange(!expanded) },
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(999.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = if (expanded) "메뉴 닫기" else "메뉴 열기",
                modifier = Modifier.graphicsLayer {
                    rotationZ = rotation
                }
            )
        }
    }
}

@Composable
internal fun FabActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MaterialSurface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 6.dp
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(999.dp)
        ) {
            Icon(imageVector = icon, contentDescription = text)
        }
    }
}

/**
 * Modal action sheet launched from the training calendar FAB.
 * This is not a route screen; keep running/strength launch choices here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WorkoutActionBottomSheet(
    onDismiss: () -> Unit,
    onRunningClick: () -> Unit,
    onStrengthClick: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "운동 실행",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(
                onClick = onRunningClick,
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.AutoMirrored.Outlined.DirectionsRun, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("런닝")
            }
            Button(
                onClick = onStrengthClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Outlined.FitnessCenter, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("웨이트")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Modal sheet for saving or uploading a strength plan to a selected calendar date.
 * Reuse it from [WeeklyTrainingScreen] instead of creating another plan-save screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StrengthPlanSaveBottomSheet(
    plans: List<StrengthWorkoutPlan>,
    selectedDate: LocalDate,
    savingPlanId: Int?,
    message: String?,
    error: String?,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onPlanSelected: (StrengthWorkoutPlan) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis
                            ?.toLocalDateFromMillis()
                            ?.let(onDateSelected)
                        showDatePicker = false
                    }
                ) {
                    Text("변경")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Plan 업로드",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        enabled = savingPlanId == null,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(selectedDate.format(DateTimeFormatter.ofPattern("M월 d일 E", Locale.KOREAN)))
                    }
                }
            }
            if (message != null || error != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            message?.let {
                                Text(it, color = MaterialTheme.colorScheme.primary)
                            }
                            error?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
            if (plans.isEmpty()) {
                item {
                    Text(
                        text = "저장할 웨이트 Plan이 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(plans, key = { it.id }) { plan ->
                    StrengthPlanSaveRow(
                        plan = plan,
                        isSaving = savingPlanId == plan.id,
                        enabled = savingPlanId == null,
                        onClick = { onPlanSelected(plan) }
                    )
                }
            }
        }
    }
}

@Composable
internal fun StrengthPlanSaveRow(
    plan: StrengthWorkoutPlan,
    isSaving: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val setCount = plan.entries.sumOf { it.records.size }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled || isSaving) 1f else 0.58f)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
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
            }
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Outlined.CloudUpload, contentDescription = null)
            }
        }
    }
}

@Composable
internal fun WeekSummary(
    activities: List<TrainingItem>,
    plans: List<TrainingItem>,
    modifier: Modifier = Modifier,
) {
    val allItems = activities + plans
    val completedLoad = activities.sumOf { it.load ?: 0 }
    val plannedLoad = plans.sumOf { it.load ?: 0 }
    val completedTime = activities.sumOf { it.durationSeconds ?: 0 }
    val plannedTime = plans.sumOf { it.durationSeconds ?: 0 }
    val totalTime = allItems.sumOf { it.durationSeconds ?: 0 }
    val completedRunningDistance = activities
        .filter { it.isRunningItem() }
        .sumOf { it.distanceMeters ?: 0.0 }
    val plannedRunningDistance = plans
        .filter { it.isRunningItem() }
        .sumOf { it.distanceMeters ?: 0.0 }
    val totalRunningDistance = allItems
        .filter { it.isRunningItem() }
        .sumOf { it.distanceMeters ?: 0.0 }
    val completedCyclingDistance = activities
        .filter { it.isCyclingItem() }
        .sumOf { it.distanceMeters ?: 0.0 }
    val plannedCyclingDistance = plans
        .filter { it.isCyclingItem() }
        .sumOf { it.distanceMeters ?: 0.0 }
    val totalCyclingDistance = allItems
        .filter { it.isCyclingItem() }
        .sumOf { it.distanceMeters ?: 0.0 }
    val fitness = allItems.latestMetricValue { it.fitness }
    val fatigue = allItems.latestMetricValue { it.fatigue }
    val form = allItems.latestMetricValue { it.form }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryMetricColumn(
                    title = "Plan",
                    value = "${plans.size}회",
                    details = listOf(
                        SummaryDetail(formatDuration(plannedTime)),
                        SummaryDetail(formatDistance(plannedRunningDistance), Icons.AutoMirrored.Outlined.DirectionsRun),
                        SummaryDetail(formatDistance(plannedCyclingDistance), Icons.AutoMirrored.Outlined.DirectionsBike),
                        SummaryDetail("Load $plannedLoad")
                    ),
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricColumn(
                    title = "완료",
                    value = "${activities.size}회",
                    details = listOf(
                        SummaryDetail(formatDuration(completedTime)),
                        SummaryDetail(formatDistance(completedRunningDistance), Icons.AutoMirrored.Outlined.DirectionsRun),
                        SummaryDetail(formatDistance(completedCyclingDistance), Icons.AutoMirrored.Outlined.DirectionsBike),
                        SummaryDetail("Load $completedLoad")
                    ),
                    modifier = Modifier.weight(1f)
                )
                SummaryMetricColumn(
                    title = "Total(예상)",
                    value = "${allItems.size}회",
                    details = listOf(
                        SummaryDetail(formatDuration(totalTime)),
                        SummaryDetail(formatDistance(totalRunningDistance), Icons.AutoMirrored.Outlined.DirectionsRun),
                        SummaryDetail(formatDistance(totalCyclingDistance), Icons.AutoMirrored.Outlined.DirectionsBike),
                        SummaryDetail("Load ${completedLoad + plannedLoad}")
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            if (fitness != null || fatigue != null || form != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    fitness?.let {
                        SummaryMetricColumn(
                            title = "Fitness",
                            value = it.formatSummaryMetric(),
                            details = listOf(SummaryDetail("CTL")),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    fatigue?.let {
                        SummaryMetricColumn(
                            title = "Fatigue",
                            value = it.formatSummaryMetric(),
                            details = listOf(SummaryDetail("ATL")),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    form?.let {
                        SummaryMetricColumn(
                            title = "Form",
                            value = it.formatSummaryMetric(),
                            details = listOf(SummaryDetail("TSB")),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SummaryMetricColumn(
    title: String,
    value: String,
    details: List<SummaryDetail>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        details.forEach { detail ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                detail.icon?.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = detail.text,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun CalendarModeIcon(
    mode: TrainingCalendarMode,
    modifier: Modifier = Modifier,
) {
    val outlineColor = MaterialTheme.colorScheme.onSurfaceVariant
    val accentColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.075f
        val corner = size.minDimension * 0.16f
        val headerHeight = size.height * 0.24f
        val innerLeft = size.width * 0.22f
        val innerTop = size.height * 0.38f
        val innerRight = size.width * 0.78f
        val innerBottom = size.height * 0.82f

        drawRoundRect(
            color = outlineColor,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(size.width - strokeWidth, size.height - strokeWidth),
            cornerRadius = CornerRadius(corner, corner),
            style = Stroke(width = strokeWidth)
        )
        drawLine(
            color = outlineColor,
            start = Offset(size.width * 0.18f, headerHeight),
            end = Offset(size.width * 0.82f, headerHeight),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = outlineColor,
            start = Offset(size.width * 0.32f, 0f),
            end = Offset(size.width * 0.32f, size.height * 0.16f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = outlineColor,
            start = Offset(size.width * 0.68f, 0f),
            end = Offset(size.width * 0.68f, size.height * 0.16f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        when (mode) {
            TrainingCalendarMode.DAY -> {
                val side = size.minDimension * 0.24f
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset((size.width - side) / 2f, innerTop),
                    size = Size(side, side),
                    cornerRadius = CornerRadius(side * 0.22f, side * 0.22f)
                )
            }
            TrainingCalendarMode.WEEK -> {
                val blockHeight = size.height * 0.16f
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(innerLeft, (innerTop + innerBottom - blockHeight) / 2f),
                    size = Size(innerRight - innerLeft, blockHeight),
                    cornerRadius = CornerRadius(blockHeight / 2f, blockHeight / 2f)
                )
            }
            TrainingCalendarMode.MONTH -> {
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(innerLeft, innerTop),
                    size = Size(innerRight - innerLeft, innerBottom - innerTop),
                    cornerRadius = CornerRadius(size.minDimension * 0.07f, size.minDimension * 0.07f)
                )
            }
        }
    }
}

@Composable
internal fun TrainingList(
    days: List<LocalDate>,
    items: List<TrainingItem>,
    emptyMessage: String,
    onPlanSelected: (TrainingItem) -> Unit,
    onIntervalStrengthPlanSelected: (TrainingItem?, StrengthWorkoutPlan) -> Unit,
    initialScrollDate: LocalDate? = null,
    header: (@Composable () -> Unit)? = null,
) {
    val grouped = items.groupBy { it.date }
    val shouldShowEmptyDays = days.size > 1

    if (items.isEmpty() && !shouldShowEmptyDays && header == null) {
        EmptyView(message = emptyMessage)
        return
    }

    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var headerHeightPx by remember { mutableIntStateOf(0) }
    var headerOffsetPx by remember { mutableFloatStateOf(0f) }
    var didInitialScroll by remember(initialScrollDate, days) { mutableStateOf(false) }
    val headerHeightDp = with(density) { headerHeightPx.toDp() }
    val visibleHeaderHeightDp = with(density) {
        (headerHeightPx + headerOffsetPx).coerceAtLeast(0f).toDp()
    }
    val headerScrollConnection = remember(headerHeightPx, listState) {
        object : NestedScrollConnection {
            private suspend fun animateHeaderTo(targetOffset: Float) {
                val boundedTarget = targetOffset.coerceIn(-headerHeightPx.toFloat(), 0f)
                if (headerOffsetPx == boundedTarget) return

                Animatable(headerOffsetPx).animateTo(
                    targetValue = boundedTarget,
                    animationSpec = tween(durationMillis = 180)
                ) {
                    headerOffsetPx = value.coerceIn(-headerHeightPx.toFloat(), 0f)
                }
                headerOffsetPx = boundedTarget
            }

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (header == null || source != NestedScrollSource.UserInput || headerHeightPx == 0) {
                    return Offset.Zero
                }
                val delta = available.y
                if (delta == 0f) return Offset.Zero
                val isScrollingUp = delta < 0f
                if (isScrollingUp && !listState.canScrollForward) {
                    return Offset.Zero
                }

                val previousOffset = headerOffsetPx
                val nextOffset = (previousOffset + delta).coerceIn(-headerHeightPx.toFloat(), 0f)
                headerOffsetPx = nextOffset
                val consumedY = nextOffset - previousOffset

                return Offset(x = 0f, y = consumedY)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (header == null || headerHeightPx == 0 || available.y == 0f) {
                    return Velocity.Zero
                }
                if (available.y < 0f && !listState.canScrollForward) {
                    return Velocity.Zero
                }
                val targetOffset = if (available.y < 0f) {
                    -headerHeightPx.toFloat()
                } else {
                    0f
                }
                coroutineScope.launch {
                    animateHeaderTo(targetOffset)
                }
                return Velocity.Zero
            }
        }
    }
    LaunchedEffect(headerHeightPx) {
        headerOffsetPx = headerOffsetPx.coerceIn(-headerHeightPx.toFloat(), 0f)
    }
    LaunchedEffect(listState.canScrollForward, listState.canScrollBackward, headerHeightPx) {
        val isListScrollable = listState.canScrollForward || listState.canScrollBackward
        if (!isListScrollable && headerOffsetPx < 0f) {
            headerOffsetPx = 0f
        }
    }
    LaunchedEffect(initialScrollDate, days, headerHeightPx) {
        val targetDate = initialScrollDate ?: return@LaunchedEffect
        if (didInitialScroll || headerHeightPx == 0 || targetDate !in days) return@LaunchedEffect

        var targetIndex = 0
        for (day in days) {
            if (day == targetDate) break
            targetIndex += 1 + grouped[day].orEmpty().size
        }
        listState.scrollToItem(index = targetIndex)
        didInitialScroll = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .then(if (header != null) Modifier.nestedScroll(headerScrollConnection) else Modifier)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                start = 16.dp,
                top = if (header != null) visibleHeaderHeightDp + 14.dp else 16.dp,
                end = 16.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        val showSingleDayEmptyMessage = items.isEmpty() && !shouldShowEmptyDays
        if (showSingleDayEmptyMessage) {
            item(key = "empty-training-list") {
                Text(
                    text = emptyMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        if (!showSingleDayEmptyMessage) {
            days.forEach { day ->
                val dayItems = grouped[day].orEmpty()
                item(key = "header-$day") {
                    DayHeader(day = day, count = dayItems.size)
                }
                if (dayItems.isNotEmpty()) {
                    items(dayItems, key = { it.id }) { item ->
                        TrainingItemRow(
                            item = item,
                            onClick = {
                                val strengthPlan = item.strengthPlanForDisplay()
                                if (item.isPlan && strengthPlan != null) {
                                    onIntervalStrengthPlanSelected(item, strengthPlan)
                                } else {
                                    onPlanSelected(item)
                                }
                            }
                        )
                    }
                }
            }
        }
        }
        if (header != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f)
                    .offset { IntOffset(x = 0, y = headerOffsetPx.roundToInt()) }
                    .onSizeChanged { headerHeightPx = it.height }
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp)
            ) {
                header()
            }
        }
    }
}

@Composable
internal fun MonthlyTrainingCalendar(
    range: TrainingDateRange,
    items: List<TrainingItem>,
    onPlanSelected: (TrainingItem) -> Unit,
    onIntervalStrengthPlanSelected: (TrainingItem?, StrengthWorkoutPlan) -> Unit,
) {
    val grouped = items.groupBy { it.date }
    val calendarDays = remember(range.start, range.end) { range.monthCalendarDays() }
    val weeks = remember(calendarDays) { calendarDays.chunked(7) }
    val weekLabels = remember {
        listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item(key = "month-weekdays") {
            Row(modifier = Modifier.fillMaxWidth()) {
                weekLabels.forEach { dayOfWeek ->
                    Text(
                        text = dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.KOREAN),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 8.dp)
                    )
                }
            }
        }
        itemsIndexed(weeks, key = { index, _ -> "week-$index" }) { _, week ->
            val visibleItemCount = week
                .maxOfOrNull { day -> grouped[day].orEmpty().size.coerceAtMost(3) }
                ?.coerceAtLeast(2)
                ?: 2
            val cellHeight = if (visibleItemCount >= 3) 92.dp else 72.dp
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    MonthlyCalendarDayCell(
                        day = day,
                        isInCurrentMonth = !day.isBefore(range.start) && !day.isAfter(range.end),
                        items = grouped[day].orEmpty(),
                        visibleItemCount = visibleItemCount,
                        modifier = Modifier
                            .weight(1f)
                            .height(cellHeight),
                        onPlanSelected = onPlanSelected,
                        onIntervalStrengthPlanSelected = onIntervalStrengthPlanSelected
                    )
                }
            }
        }
    }
}

@Composable
internal fun MonthlyCalendarDayCell(
    day: LocalDate,
    isInCurrentMonth: Boolean,
    items: List<TrainingItem>,
    visibleItemCount: Int,
    modifier: Modifier = Modifier,
    onPlanSelected: (TrainingItem) -> Unit,
    onIntervalStrengthPlanSelected: (TrainingItem?, StrengthWorkoutPlan) -> Unit,
) {
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
    val today = remember { LocalDate.now() }
    val isToday = day == today
    Column(
        modifier = modifier
            .border(0.5.dp, borderColor)
            .background(
                when {
                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
                    isInCurrentMonth -> MaterialTheme.colorScheme.surface
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
                }
            )
            .padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = day.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
            color = when {
                isToday -> MaterialTheme.colorScheme.primary
                isInCurrentMonth -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
            },
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
        items.take(visibleItemCount).forEach { item ->
            MonthlyCalendarItemChip(
                item = item,
                onClick = {
                    val strengthPlan = item.strengthPlanForDisplay()
                    if (item.isPlan && strengthPlan != null) {
                        onIntervalStrengthPlanSelected(item, strengthPlan)
                    } else {
                        onPlanSelected(item)
                    }
                }
            )
        }
    }
}

@Composable
internal fun MonthlyCalendarItemChip(
    item: TrainingItem,
    onClick: () -> Unit,
) {
    val color = if (item.isPlan) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(17.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.14f))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        TrainingStatusIcons(
            item = item,
            color = color,
            iconSize = 12.dp,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        TrainingSportIcon(
            sportType = item.sportType(),
            showBackground = false,
            modifier = Modifier.size(12.dp)
        )
    }
}

@Composable
internal fun DayHeader(day: LocalDate, count: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = day.format(DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${count}개",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.62f))
        )
    }
}

@Composable
internal fun TrainingItemRow(
    item: TrainingItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TrainingStatusIcons(
                    item = item,
                    color = MaterialTheme.colorScheme.primary,
                    iconSize = 24.dp,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                TrainingSportIcon(
                    sportType = item.sportType(),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.name.ifBlank { item.type },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val strengthPlan = item.strengthPlanForDisplay()
                item.displayTimeLabel()?.let {
                    MetricChip(icon = Icons.Outlined.Today, text = it)
                }
                strengthPlan?.entries?.takeIf { it.isNotEmpty() }?.let { entries ->
                    MetricChip(icon = Icons.Outlined.FitnessCenter, text = "${entries.size}종목")
                    entries.totalVolumeKg().takeIf { it > 0.0 }?.let { volume ->
                        MetricChip(icon = Icons.Outlined.FitnessCenter, text = "Lift ${formatWeight(volume)} kg")
                    }
                }
                item.durationSeconds?.let {
                    MetricChip(icon = Icons.Outlined.Schedule, text = formatDuration(it))
                }
                item.distanceMeters?.takeIf { it > 0.0 }?.let {
                    MetricChip(icon = Icons.Outlined.Route, text = formatDistance(it))
                }
                item.weightLiftedKg?.takeIf { it > 0.0 }?.let {
                    MetricChip(icon = Icons.Outlined.FitnessCenter, text = "${formatWeight(it)} kg")
                }
                item.load?.let {
                    MetricChip(icon = Icons.Outlined.Speed, text = "Load $it")
                }
            }
            val previewBlocks = item.workoutPlanBlocksForPreview()
            if (previewBlocks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                PlanWorkoutGraphCanvas(
                    blocks = previewBlocks,
                    totalSeconds = item.workoutPlanTotalSecondsForPreview(previewBlocks),
                    sportType = item.sportType(),
                    height = 112.dp
                )
            } else {
                item.description.visiblePlanDescription().takeIf { it.isNotBlank() }?.let { description ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            item.matchedStrengthWorkout?.let { workout ->
                Spacer(modifier = Modifier.height(10.dp))
                StrengthMatchSummary(workout = workout)
            }
            if (item.isLocalOnlyRunningResult) {
                Spacer(modifier = Modifier.height(10.dp))
                LocalRunningResultSummary(item = item)
            }
        }
    }
}

@Composable
internal fun LocalRunningResultSummary(item: TrainingItem) {
    MaterialSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (item.description.orEmpty().contains("업로드됨")) {
                    "로컬 러닝 기록 저장됨 · Intervals.icu 업로드됨"
                } else {
                    "로컬 러닝 기록 저장됨"
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = listOfNotNull(
                    item.durationSeconds?.let { "운동 시간 ${formatDuration(it)}" },
                    item.distanceMeters?.takeIf { it > 0.0 }?.let { "예상 거리 ${formatDistance(it)}" }
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
internal fun TrainingStatusIcons(
    item: TrainingItem,
    color: Color,
    iconSize: androidx.compose.ui.unit.Dp,
    horizontalArrangement: Arrangement.Horizontal,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement
    ) {
        if (item.isPlan || item.pairedPlan != null) {
            TrainingStatusIconContainer(
                color = color,
                size = iconSize
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize * 0.67f),
                    tint = color
                )
            }
        }
        if (!item.isPlan) {
            TrainingStatusIconContainer(
                color = color,
                size = iconSize
            ) {
                ResultCheckIcon(
                    modifier = Modifier.size(iconSize * 0.67f),
                    color = color
                )
            }
        }
    }
}

@Composable
internal fun TrainingStatusIconContainer(
    color: Color,
    size: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
internal fun ResultCheckIcon(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.11f
        drawCircle(
            color = color.copy(alpha = 0.18f),
            radius = size.minDimension / 2f
        )
        drawCircle(
            color = color,
            radius = size.minDimension / 2f - strokeWidth / 2f,
            style = Stroke(width = strokeWidth)
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.28f, size.height * 0.53f),
            end = Offset(size.width * 0.44f, size.height * 0.69f),
            strokeWidth = strokeWidth * 1.35f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.44f, size.height * 0.69f),
            end = Offset(size.width * 0.74f, size.height * 0.34f),
            strokeWidth = strokeWidth * 1.35f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
internal fun StrengthMatchSummary(workout: CompletedStrengthWorkout) {
    val completedSets = workout.setEvents.size
    val totalRestSeconds = workout.restEvents.sumOf { it.actualSeconds }
    val volume = workout.entries.totalVolumeKg()
    MaterialSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "로컬 상세 기록 매칭",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${completedSets}세트 · Load ${workout.trainingLoad} · 볼륨 ${formatWeight(volume)} kg · 실제 휴식 ${formatClock(totalRestSeconds)}",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun TrainingTypeLabel(
    isPlan: Boolean,
    resultLabel: String = "Result",
) {
    val containerColor = if (isPlan) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (isPlan) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

    MaterialSurface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = if (isPlan) "Plan" else resultLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

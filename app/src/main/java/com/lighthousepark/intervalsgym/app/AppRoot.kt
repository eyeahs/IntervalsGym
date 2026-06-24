package com.lighthousepark.intervalsgym.app

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
 * App shell state owner.
 * Keep cross-route state and navigation callbacks here; route UI should live in the owning screen composable.
 */
@Composable
internal fun IntervalsGymApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? ComponentActivity
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var apiKey by remember { mutableStateOf(prefs.getString(API_KEY_PREF, "").orEmpty()) }
    var hasSeenIntervalsLoginPrompt by remember {
        mutableStateOf(prefs.getBoolean(INTERVALS_LOGIN_PROMPT_SEEN_PREF, false))
    }
    var selectedPlan by remember { mutableStateOf<TrainingItem?>(null) }
    var completedStrengthHistory by remember { mutableStateOf(loadCompletedStrengthWorkoutHistory(prefs)) }
    var strengthPlans by remember {
        mutableStateOf(loadStrengthPlans(prefs).withLatestCompletedWorkout(completedStrengthHistory))
    }
    var activeStrengthSession by remember {
        mutableStateOf(loadActiveStrengthSession(prefs)?.withLatestCompletedWorkout(completedStrengthHistory))
    }
    var selectedStrengthPlanId by remember { mutableStateOf(activeStrengthSession?.planId) }
    var selectedStrengthPlanOverride by remember { mutableStateOf<StrengthWorkoutPlan?>(null) }
    var editingStrengthPlanId by remember { mutableStateOf<Int?>(null) }
    var historyStrengthPlanId by remember { mutableStateOf<Int?>(null) }
    var shouldStartStrengthPlanImmediately by remember { mutableStateOf(false) }
    var deletedCalendarPlanIds by remember { mutableStateOf(emptySet<String>()) }
    var selectedCalendarStrengthPlanItem by remember { mutableStateOf<TrainingItem?>(null) }
    val navController = rememberNavController()

    fun saveStrengthPlans(plans: List<StrengthWorkoutPlan>) {
        prefs.edit().putString(STRENGTH_PLANS_PREF, plans.toJsonString()).apply()
        strengthPlans = plans.withLatestCompletedWorkout(completedStrengthHistory)
    }

    fun refreshStrengthHistory() {
        completedStrengthHistory = loadCompletedStrengthWorkoutHistory(prefs)
        strengthPlans = loadStrengthPlans(prefs).withLatestCompletedWorkout(completedStrengthHistory)
        activeStrengthSession = activeStrengthSession?.withLatestCompletedWorkout(completedStrengthHistory)
    }

    fun saveActiveStrengthSession(session: ActiveStrengthSession?) {
        if (session == null) {
            prefs.edit().remove(ACTIVE_STRENGTH_SESSION_PREF).apply()
        } else {
            prefs.edit().putString(ACTIVE_STRENGTH_SESSION_PREF, session.toJsonString()).apply()
        }
        activeStrengthSession = session
    }

    fun updateStrengthPlanFromWorkout(workout: CompletedStrengthWorkout) {
        if (workout.planId == 0) return
        refreshStrengthHistory()
        val nextEntries = workout.entries.map { it.copyForWorkout() }
        saveStrengthPlans(
            strengthPlans.map { plan ->
                if (plan.id == workout.planId) {
                    plan.copy(entries = nextEntries)
                } else {
                    plan
                }
            }
        )
        if (selectedStrengthPlanId == workout.planId && selectedStrengthPlanOverride == null) {
            selectedStrengthPlanOverride = null
        }
    }

    AppNavGraph(
        navController = navController,
        apiKey = apiKey,
        hasActiveStrengthSession = activeStrengthSession != null,
        shouldShowInitialLogin = apiKey.isBlank() && !hasSeenIntervalsLoginPrompt,
        onLogin = { newApiKey ->
            prefs.edit()
                .putString(API_KEY_PREF, newApiKey)
                .putBoolean(INTERVALS_LOGIN_PROMPT_SEEN_PREF, true)
                .apply()
            apiKey = newApiKey
            hasSeenIntervalsLoginPrompt = true
            navController.navigate(ROUTE_WEEK) {
                popUpTo(ROUTE_LOGIN) { inclusive = true }
                launchSingleTop = true
            }
        },
        onSkipLogin = {
            prefs.edit()
                .putBoolean(INTERVALS_LOGIN_PROMPT_SEEN_PREF, true)
                .apply()
            apiKey = ""
            hasSeenIntervalsLoginPrompt = true
            selectedPlan = null
            navController.navigate(ROUTE_WEEK) {
                popUpTo(ROUTE_LOGIN) { inclusive = true }
                launchSingleTop = true
            }
        },
        selectedPlan = selectedPlan,
        deletedCalendarPlanIds = deletedCalendarPlanIds,
        selectedCalendarStrengthPlanItem = selectedCalendarStrengthPlanItem,
        onPlanSelected = { plan ->
            selectedPlan = plan
            navController.navigate(ROUTE_WORKOUT_PLAN)
        },
        onCalendarPlanDeleted = { plan ->
            deletedCalendarPlanIds = deletedCalendarPlanIds + plan.id + plan.remoteId
            selectedPlan = null
            selectedCalendarStrengthPlanItem = null
            navController.popBackStack()
        },
        onStrengthWorkoutUploaded = { uploadedWorkout ->
            replaceStrengthWorkoutHistory(prefs, uploadedWorkout.copy(uploadedToIntervals = true))
            refreshStrengthHistory()
            selectedPlan = selectedPlan?.let { selected ->
                if (selected.matchedStrengthWorkout?.id == uploadedWorkout.id) {
                    selected.copy(matchedStrengthWorkout = uploadedWorkout.copy(uploadedToIntervals = true))
                } else {
                    selected
                }
            }
        },
        onIntervalStrengthPlanSelected = { calendarItem, plan ->
            saveActiveStrengthSession(null)
            selectedCalendarStrengthPlanItem = calendarItem
            selectedStrengthPlanOverride = plan
            selectedStrengthPlanId = null
            navController.navigate(ROUTE_STRENGTH_SESSION)
        },
        onMonthDaySelected = { date ->
            navController.navigate(trainingDayRoute(date))
        },
        onStrengthWorkout = {
            navController.navigate(
                if (activeStrengthSession != null) ROUTE_STRENGTH_SESSION else ROUTE_STRENGTH_PLANS
            )
        },
        onRunningWorkout = {
            navController.navigate(ROUTE_RUNNING_PLANS)
        },
        strengthPlans = strengthPlans,
        activeStrengthSession = activeStrengthSession,
        selectedStrengthPlanId = selectedStrengthPlanId,
        selectedStrengthPlanOverride = selectedStrengthPlanOverride,
        editingStrengthPlanId = editingStrengthPlanId,
        historyStrengthPlanId = historyStrengthPlanId,
        onManageStrengthPlans = {
            navController.navigate(ROUTE_STRENGTH_MANAGE)
        },
        onStrengthPlanSelected = { plan ->
            saveActiveStrengthSession(null)
            selectedCalendarStrengthPlanItem = null
            shouldStartStrengthPlanImmediately = false
            selectedStrengthPlanOverride = null
            selectedStrengthPlanId = plan.id
            navController.navigate(ROUTE_STRENGTH_SESSION)
        },
        onStartStrengthPlanImmediately = { plan ->
            saveActiveStrengthSession(null)
            selectedCalendarStrengthPlanItem = null
            shouldStartStrengthPlanImmediately = true
            selectedStrengthPlanOverride = null
            selectedStrengthPlanId = plan.id
            navController.navigate(ROUTE_STRENGTH_SESSION)
        },
        onStrengthPlanHistory = { plan ->
            historyStrengthPlanId = plan.id
            navController.navigate(ROUTE_STRENGTH_HISTORY)
        },
        onStrengthHistorySelected = { workout ->
            saveActiveStrengthSession(null)
            selectedCalendarStrengthPlanItem = null
            selectedStrengthPlanId = workout.planId
            selectedStrengthPlanOverride = StrengthWorkoutPlan(
                id = workout.planId,
                name = workout.planName,
                entries = workout.entries.map { it.copyForWorkout() }
            )
            navController.navigate(ROUTE_STRENGTH_SESSION) {
                popUpTo(ROUTE_STRENGTH_PLANS)
            }
        },
        onAddStrengthPlan = {
            editingStrengthPlanId = null
            navController.navigate(ROUTE_STRENGTH_PLAN_EDIT)
        },
        onEditStrengthPlan = { plan ->
            editingStrengthPlanId = plan.id
            navController.navigate(ROUTE_STRENGTH_PLAN_EDIT)
        },
        onSaveStrengthPlan = { plan ->
            val savedPlan = if (plan.id == 0) {
                val nextId = (strengthPlans.maxOfOrNull { it.id } ?: 0) + 1
                plan.copy(id = nextId)
            } else {
                plan
            }
            val nextPlans = when {
                plan.id == 0 -> strengthPlans + savedPlan
                strengthPlans.any { it.id == plan.id } ->
                    strengthPlans.map { if (it.id == plan.id) savedPlan else it }
                else -> strengthPlans + savedPlan
            }
            saveStrengthPlans(nextPlans)
            if (selectedStrengthPlanOverride?.id == plan.id) {
                selectedStrengthPlanOverride = savedPlan
            }
            if (selectedStrengthPlanId == plan.id) {
                selectedStrengthPlanId = savedPlan.id
            }
            if (editingStrengthPlanId == plan.id) {
                editingStrengthPlanId = savedPlan.id
            }
            navController.popBackStack()
        },
        onDeleteStrengthPlan = { plan ->
            saveStrengthPlans(strengthPlans.filterNot { it.id == plan.id })
            if (selectedStrengthPlanId == plan.id) selectedStrengthPlanId = null
            if (activeStrengthSession?.planId == plan.id) saveActiveStrengthSession(null)
            val previousRoute = navController.previousBackStackEntry?.destination?.route
            if (previousRoute == ROUTE_STRENGTH_SESSION) {
                navController.popBackStack(ROUTE_STRENGTH_PLANS, inclusive = false)
            } else {
                navController.popBackStack()
            }
        },
        onActiveStrengthSessionChange = ::saveActiveStrengthSession,
        onActiveStrengthSessionFinished = { workout ->
            workout?.let { updateStrengthPlanFromWorkout(it) }
            saveActiveStrengthSession(null)
            shouldStartStrengthPlanImmediately = false
        },
        shouldStartStrengthPlanImmediately = shouldStartStrengthPlanImmediately,
        onImmediateStrengthPlanStartConsumed = {
            shouldStartStrengthPlanImmediately = false
        },
        onNavigateBack = {
            if (!navController.popBackStack()) {
                activity?.moveTaskToBack(true)
            }
        },
        onLoginClick = {
            navController.navigate(ROUTE_LOGIN)
        },
        onLogout = {
            prefs.edit().remove(API_KEY_PREF).apply()
            apiKey = ""
            selectedPlan = null
            selectedCalendarStrengthPlanItem = null
            deletedCalendarPlanIds = emptySet()
        }
    )
}

/**
 * Single route registry for the app.
 * Add new destinations here only after checking whether an existing route owner can be extended.
 */
@Composable
internal fun AppNavGraph(
    navController: NavHostController,
    apiKey: String,
    hasActiveStrengthSession: Boolean,
    shouldShowInitialLogin: Boolean,
    selectedPlan: TrainingItem?,
    deletedCalendarPlanIds: Set<String>,
    selectedCalendarStrengthPlanItem: TrainingItem?,
    onLogin: (String) -> Unit,
    onSkipLogin: () -> Unit,
    onPlanSelected: (TrainingItem) -> Unit,
    onCalendarPlanDeleted: (TrainingItem) -> Unit,
    onStrengthWorkoutUploaded: (CompletedStrengthWorkout) -> Unit,
    onIntervalStrengthPlanSelected: (TrainingItem?, StrengthWorkoutPlan) -> Unit,
    onMonthDaySelected: (LocalDate) -> Unit,
    onStrengthWorkout: () -> Unit,
    onRunningWorkout: () -> Unit,
    strengthPlans: List<StrengthWorkoutPlan>,
    activeStrengthSession: ActiveStrengthSession?,
    selectedStrengthPlanId: Int?,
    selectedStrengthPlanOverride: StrengthWorkoutPlan?,
    editingStrengthPlanId: Int?,
    historyStrengthPlanId: Int?,
    onManageStrengthPlans: () -> Unit,
    onStrengthPlanSelected: (StrengthWorkoutPlan) -> Unit,
    onStartStrengthPlanImmediately: (StrengthWorkoutPlan) -> Unit,
    onStrengthPlanHistory: (StrengthWorkoutPlan) -> Unit,
    onStrengthHistorySelected: (CompletedStrengthWorkout) -> Unit,
    onAddStrengthPlan: () -> Unit,
    onEditStrengthPlan: (StrengthWorkoutPlan) -> Unit,
    onSaveStrengthPlan: (StrengthWorkoutPlan) -> Unit,
    onDeleteStrengthPlan: (StrengthWorkoutPlan) -> Unit,
    onActiveStrengthSessionChange: (ActiveStrengthSession?) -> Unit,
    onActiveStrengthSessionFinished: (CompletedStrengthWorkout?) -> Unit,
    shouldStartStrengthPlanImmediately: Boolean,
    onImmediateStrengthPlanStartConsumed: () -> Unit,
    onNavigateBack: () -> Unit,
    onLoginClick: () -> Unit,
    onLogout: () -> Unit,
) {
    NavHost(
        navController = navController,
        modifier = Modifier
            .fillMaxSize()
            .throttleRapidTaps()
            .background(MaterialTheme.colorScheme.background),
        startDestination = when {
            hasActiveStrengthSession -> ROUTE_STRENGTH_SESSION
            shouldShowInitialLogin -> ROUTE_LOGIN
            else -> ROUTE_WEEK
        },
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(260)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(260)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(260)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(260)
            )
        }
    ) {
        composable(ROUTE_LOGIN) {
            LoginScreen(
                onLogin = onLogin,
                onSkipLogin = onSkipLogin
            )
        }
        composable(ROUTE_WEEK) {
            WeeklyTrainingScreen(
                apiKey = apiKey,
                strengthPlans = strengthPlans,
                deletedCalendarPlanIds = deletedCalendarPlanIds,
                onPlanSelected = onPlanSelected,
                onIntervalStrengthPlanSelected = onIntervalStrengthPlanSelected,
                onMonthDaySelected = onMonthDaySelected,
                onStrengthWorkout = onStrengthWorkout,
                onRunningWorkout = onRunningWorkout,
                onLoginClick = onLoginClick,
                onLogout = onLogout
            )
        }
        composable("$ROUTE_TRAINING_DAY/{date}") { backStackEntry ->
            val selectedDate = backStackEntry.arguments
                ?.getString("date")
                ?.let { raw -> runCatching { LocalDate.parse(raw) }.getOrNull() }
                ?: LocalDate.now()
            WeeklyTrainingScreen(
                apiKey = apiKey,
                strengthPlans = strengthPlans,
                deletedCalendarPlanIds = deletedCalendarPlanIds,
                initialDate = selectedDate,
                initialCalendarMode = TrainingCalendarMode.DAY,
                showBackButton = true,
                showCalendarModeButton = false,
                onPlanSelected = onPlanSelected,
                onIntervalStrengthPlanSelected = onIntervalStrengthPlanSelected,
                onStrengthWorkout = onStrengthWorkout,
                onRunningWorkout = onRunningWorkout,
                onLoginClick = onLoginClick,
                onLogout = onLogout,
                onBack = onNavigateBack
            )
        }
        composable(ROUTE_RUNNING_PLANS) {
            RunningPlanListScreen(
                onPlanSelected = { plan -> onPlanSelected(plan.toTrainingItem()) },
                onManagePlans = { navController.navigate(ROUTE_RUNNING_MANAGE) },
                onBack = onNavigateBack
            )
        }
        composable(ROUTE_RUNNING_MANAGE) {
            RunningPlanManagementScreen(
                onBack = onNavigateBack
            )
        }
        composable(ROUTE_WORKOUT_PLAN) {
            WorkoutPlanScreen(
                apiKey = apiKey,
                plan = selectedPlan,
                onStartStrengthPlan = { plan -> onIntervalStrengthPlanSelected(null, plan) },
                onStrengthWorkoutUploaded = onStrengthWorkoutUploaded,
                onPlanDeleted = onCalendarPlanDeleted,
                onBack = onNavigateBack
            )
        }
        composable(ROUTE_STRENGTH_PLANS) {
            StrengthPlanListScreen(
                plans = strengthPlans,
                onPlanSelected = onStrengthPlanSelected,
                onStartPlan = onStartStrengthPlanImmediately,
                onManagePlans = onManageStrengthPlans,
                onBack = onNavigateBack
            )
        }
        composable(ROUTE_STRENGTH_MANAGE) {
            StrengthPlanManagementScreen(
                plans = strengthPlans,
                onAddPlan = onAddStrengthPlan,
                onEditPlan = onEditStrengthPlan,
                onBack = onNavigateBack
            )
        }
        composable(ROUTE_STRENGTH_PLAN_EDIT) {
            StrengthPlanEditScreen(
                plan = strengthPlans.firstOrNull { it.id == editingStrengthPlanId }
                    ?: selectedStrengthPlanOverride?.takeIf { it.id == editingStrengthPlanId },
                onSave = onSaveStrengthPlan,
                onDelete = onDeleteStrengthPlan,
                onBack = onNavigateBack
            )
        }
        composable(ROUTE_STRENGTH_HISTORY) {
            val targetPlan = strengthPlans.firstOrNull { it.id == historyStrengthPlanId }
                ?: selectedStrengthPlanOverride?.takeIf { it.id == historyStrengthPlanId }
            StrengthPlanHistoryScreen(
                plan = targetPlan,
                history = loadCompletedStrengthWorkoutHistory(
                    LocalContext.current.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                ),
                onHistorySelected = onStrengthHistorySelected,
                onBack = onNavigateBack
            )
        }
        composable(ROUTE_STRENGTH_SESSION) {
            val sessionPlan = activeStrengthSession?.toWorkoutPlan()
                ?: selectedStrengthPlanOverride
                ?: strengthPlans.firstOrNull { it.id == selectedStrengthPlanId }
            val canEditSessionPlan = activeStrengthSession == null && sessionPlan != null &&
                (
                    selectedStrengthPlanOverride?.id == sessionPlan.id ||
                        selectedStrengthPlanId?.let { planId ->
                            strengthPlans.any { it.id == planId }
                        } == true
                    )
            StrengthWorkoutSessionScreen(
                apiKey = apiKey,
                plan = sessionPlan,
                calendarPlanItem = selectedCalendarStrengthPlanItem,
                isPlanEditable = canEditSessionPlan,
                activeSession = activeStrengthSession,
                startImmediately = shouldStartStrengthPlanImmediately,
                onImmediateStartConsumed = onImmediateStrengthPlanStartConsumed,
                onSessionChange = onActiveStrengthSessionChange,
                onSessionFinished = onActiveStrengthSessionFinished,
                onHistoryClick = onStrengthPlanHistory,
                onEditPlan = onEditStrengthPlan,
                onCalendarPlanDeleted = onCalendarPlanDeleted,
                onBack = onNavigateBack
            )
        }
    }
}

internal fun Modifier.throttleRapidTaps(throttleMillis: Long = 500L): Modifier = pointerInput(throttleMillis) {
    var lastAcceptedTapUpMillis = 0L
    var shouldBlockIfTap = false
    var movedBeyondTapSlop = false
    var accumulatedMove = Offset.Zero
    val tapSlop = viewConfiguration.touchSlop

    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val changedToDown = event.changes.any { it.changedToDownIgnoreConsumed() }
            if (changedToDown) {
                val downMillis = event.changes.firstOrNull { it.changedToDownIgnoreConsumed() }?.uptimeMillis
                    ?: event.changes.firstOrNull()?.uptimeMillis
                    ?: 0L
                shouldBlockIfTap = downMillis - lastAcceptedTapUpMillis in 1 until throttleMillis
                movedBeyondTapSlop = false
                accumulatedMove = Offset.Zero
            }
            val eventMove = event.changes.fold(Offset.Zero) { total, change -> total + change.positionChange() }
            accumulatedMove += eventMove
            if (accumulatedMove.getDistance() > tapSlop) {
                movedBeyondTapSlop = true
            }
            val changedToUp = event.changes.any { it.changedToUpIgnoreConsumed() }
            if (changedToUp && shouldBlockIfTap && !movedBeyondTapSlop) {
                event.changes.forEach { it.consume() }
            } else if (changedToUp && !movedBeyondTapSlop) {
                lastAcceptedTapUpMillis = event.changes.firstOrNull { it.changedToUpIgnoreConsumed() }?.uptimeMillis
                    ?: event.changes.firstOrNull()?.uptimeMillis
                    ?: lastAcceptedTapUpMillis
            }
            if (event.changes.none { it.pressed }) {
                shouldBlockIfTap = false
                movedBeyondTapSlop = false
                accumulatedMove = Offset.Zero
            }
        }
    }
}

package com.lighthousepark.intervalsgym

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

private const val PREFS_NAME = "intervals_gym"
private const val API_KEY_PREF = "intervals_api_key"
private const val INTERVALS_LOGIN_PROMPT_SEEN_PREF = "intervals_login_prompt_seen"
private const val STRENGTH_PLANS_PREF = "strength_plans"
private const val ACTIVE_STRENGTH_SESSION_PREF = "active_strength_session"
private const val STRENGTH_WORKOUT_HISTORY_PREF = "strength_workout_history"
private const val RUNNING_WORKOUT_HISTORY_PREF = "running_workout_history"
private const val SCHEDULED_STRENGTH_PLANS_PREF = "scheduled_strength_plans"
private const val INTERVALS_GYM_STRENGTH_PLAN_PREFIX = "INTERVALS_GYM_STRENGTH_PLAN:"
private const val ROUTE_LOGIN = "login"
private const val ROUTE_WEEK = "week"
private const val ROUTE_WORKOUT_PLAN = "workout_plan"
private const val ROUTE_STRENGTH_PLANS = "strength_plans"
private const val ROUTE_STRENGTH_MANAGE = "strength_manage"
private const val ROUTE_STRENGTH_PLAN_EDIT = "strength_plan_edit"
private const val ROUTE_STRENGTH_SESSION = "strength_session"
private const val ROUTE_STRENGTH_HISTORY = "strength_history"
private const val REST_NOTIFICATION_CHANNEL_ID = "strength_rest_timer"
private const val REST_NOTIFICATION_ID = 42
private enum class TrainingCalendarMode(val title: String) {
    DAY("하루 훈련"),
    WEEK("주간 훈련"),
    MONTH("월간 훈련");

    fun next(): TrainingCalendarMode {
        return when (this) {
            DAY -> WEEK
            WEEK -> MONTH
            MONTH -> DAY
        }
    }
}

private data class TrainingDateRange(
    val start: LocalDate,
    val end: LocalDate,
)

private data class SummaryDetail(
    val text: String,
    val icon: ImageVector? = null,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createRestNotificationChannel()
        requestRestNotificationPermission()
        setContent {
            IntervalsGymTheme {
                IntervalsGymApp()
            }
        }
    }

    private fun createRestNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REST_NOTIFICATION_CHANNEL_ID,
                "웨이트 휴식 타이머",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "세트 휴식 종료 알림"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 180, 400)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun requestRestNotificationPermission() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REST_NOTIFICATION_ID)
        }
    }
}

@Composable
private fun IntervalsGymApp() {
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
        onStrengthWorkout = {
            navController.navigate(
                if (activeStrengthSession != null) ROUTE_STRENGTH_SESSION else ROUTE_STRENGTH_PLANS
            )
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

@Composable
private fun AppNavGraph(
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
    onStrengthWorkout: () -> Unit,
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
                onStrengthWorkout = onStrengthWorkout,
                onLoginClick = onLoginClick,
                onLogout = onLogout
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

private fun Modifier.throttleRapidTaps(throttleMillis: Long = 500L): Modifier = pointerInput(throttleMillis) {
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

@Composable
private fun LoginScreen(
    onLogin: (String) -> Unit,
    onSkipLogin: () -> Unit,
) {
    var apiKey by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    val isValid = apiKey.isNotBlank()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Intervals 주간 훈련",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Intervals.icu 설정의 Developer Settings에서 API Key를 만든 뒤 붙여 넣으세요.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(28.dp))
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it.trim() },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("API Key") },
                singleLine = true,
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                supportingText = { Text("Basic auth의 username은 앱이 자동으로 API_KEY를 사용합니다.") }
            )
            TextButton(onClick = { showApiKey = !showApiKey }) {
                Text(if (showApiKey) "API Key 숨기기" else "API Key 확인하기")
            }
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = { onLogin(apiKey) },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("로그인")
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onSkipLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("건너뛰기")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun WeeklyTrainingScreen(
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
private fun WeeklyTrainingFabMenu(
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
private fun FabActionButton(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutActionBottomSheet(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StrengthPlanSaveBottomSheet(
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
private fun StrengthPlanSaveRow(
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
private fun WeekSummary(
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
private fun SummaryMetricColumn(
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
private fun CalendarModeIcon(
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
private fun TrainingList(
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
private fun MonthlyTrainingCalendar(
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
private fun MonthlyCalendarDayCell(
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
private fun MonthlyCalendarItemChip(
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
private fun DayHeader(day: LocalDate, count: Int) {
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
private fun TrainingItemRow(
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
private fun LocalRunningResultSummary(item: TrainingItem) {
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
private fun TrainingStatusIcons(
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
private fun TrainingStatusIconContainer(
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
private fun ResultCheckIcon(
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
private fun StrengthMatchSummary(workout: CompletedStrengthWorkout) {
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
private fun TrainingTypeLabel(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StrengthPlanListScreen(
    plans: List<StrengthWorkoutPlan>,
    onPlanSelected: (StrengthWorkoutPlan) -> Unit,
    onStartPlan: (StrengthWorkoutPlan) -> Unit,
    onManagePlans: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("웨이트 plan 선택") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = onManagePlans) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Plan 관리")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (plans.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = "수행할 웨이트 Plan이 없습니다. 우측 상단 관리에서 Plan을 추가하세요.",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(plans, key = { it.id }) { plan ->
                    StrengthPlanRow(
                        plan = plan,
                        onClick = { onPlanSelected(plan) },
                        trailing = {
                            IconButton(onClick = { onStartPlan(plan) }) {
                                Icon(Icons.Outlined.PlayArrow, contentDescription = "바로 운동 시작")
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StrengthPlanHistoryScreen(
    plan: StrengthWorkoutPlan?,
    history: List<CompletedStrengthWorkout>,
    onHistorySelected: (CompletedStrengthWorkout) -> Unit,
    onBack: () -> Unit,
) {
    val planHistory = remember(plan?.id, history) {
        history
            .filter { workout -> plan == null || workout.planId == plan.id }
            .sortedByDescending { it.startedAtMillis }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${plan?.name ?: "웨이트 plan"} history 선택",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (planHistory.isEmpty()) {
            EmptyView(
                message = "저장된 history가 없습니다.",
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(planHistory, key = { it.id }) { workout ->
                StrengthPlanHistoryRow(
                    workout = workout,
                    onClick = { onHistorySelected(workout) }
                )
            }
        }
    }
}

@Composable
private fun StrengthPlanHistoryRow(
    workout: CompletedStrengthWorkout,
    onClick: () -> Unit,
) {
    val startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(workout.startedAtMillis), ZoneId.systemDefault())
    val completedSets = workout.setEvents.size
    val totalSets = workout.entries.sumOf { it.records.size }
    val volume = workout.entries.totalVolumeKg()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = startedAt.format(DateTimeFormatter.ofPattern("M/d HH:mm", Locale.KOREAN)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${workout.entries.size}개 운동 · Load ${workout.trainingLoad} · $completedSets/$totalSets 세트 · ${formatWeight(volume)} kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = if (workout.uploadedToIntervals) "업로드됨" else "미동기화",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (workout.uploadedToIntervals) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = workout.entries.joinToString(" · ") { it.title },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StrengthPlanManagementScreen(
    plans: List<StrengthWorkoutPlan>,
    onAddPlan: () -> Unit,
    onEditPlan: (StrengthWorkoutPlan) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPlan,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Plan 추가")
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("웨이트 Plan 관리") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (plans.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = "저장된 웨이트 Plan이 없습니다.",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(plans, key = { it.id }) { plan ->
                    StrengthPlanRow(
                        plan = plan,
                        onClick = { onEditPlan(plan) },
                        trailing = {
                            IconButton(onClick = { onEditPlan(plan) }) {
                                Icon(Icons.Outlined.Edit, contentDescription = "수정")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StrengthPlanRow(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StrengthPlanEditScreen(
    plan: StrengthWorkoutPlan?,
    onSave: (StrengthWorkoutPlan) -> Unit,
    onDelete: (StrengthWorkoutPlan) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val completedStrengthHistory = remember(plan?.id) { loadCompletedStrengthWorkoutHistory(prefs) }
    var planName by remember(plan?.id) { mutableStateOf(plan?.name.orEmpty()) }
    var entries by remember(plan?.id) { mutableStateOf(plan?.entries.orEmpty()) }
    var selectedEntryId by remember(plan?.id) { mutableStateOf<Int?>(null) }
    var isSupersetSelectionMode by remember(plan?.id) { mutableStateOf(false) }
    var selectedSupersetEntryIds by remember(plan?.id) { mutableStateOf(emptySet<Int>()) }
    var pendingDeleteEntryIds by remember(plan?.id) { mutableStateOf(emptySet<Int>()) }
    var isExerciseListVisible by remember(plan?.id) { mutableStateOf(false) }
    var shouldReturnToExerciseListFromDetail by remember(plan?.id) { mutableStateOf(false) }
    var isChangingSelectedEntryExercise by remember(plan?.id, selectedEntryId) { mutableStateOf(false) }
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
                exercise = exercise
            ).copy(
                equipment = equipment,
                variation = variation
            )
        entries = entries + entry
        selectedEntryId = null
        isExerciseListVisible = false
        shouldReturnToExerciseListFromDetail = false
        isChangingSelectedEntryExercise = false
        exerciseToConfigure = null
    }

    fun handleBack() {
        when {
            isUnsavedBackDialogVisible -> isUnsavedBackDialogVisible = false
            isChangingSelectedEntryExercise -> isChangingSelectedEntryExercise = false
            selectedEntry != null -> {
                selectedEntryId = null
                isExerciseListVisible = shouldReturnToExerciseListFromDetail
                shouldReturnToExerciseListFromDetail = false
            }
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
                    contentPadding = PaddingValues(16.dp),
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
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isSupersetSelectionMode = true },
                                enabled = entries.size >= 2 && !isSupersetSelectionMode,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("슈퍼세트 묶기", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            OutlinedButton(
                                onClick = { isExerciseListVisible = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(Icons.Outlined.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("신규 운동 추가", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    saveCurrentPlan()
                                },
                                enabled = entries.isNotEmpty() && planName.isNotBlank(),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("저장")
                            }
                            if (plan != null) {
                                Button(
                                    onClick = { isPlanDeleteDialogVisible = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    )
                                ) {
                                    Icon(Icons.Outlined.Delete, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("삭제")
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
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
private fun StrengthExerciseListScreen(
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
                            text = "신규 운동 추가",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            items(candidates, key = { it.id }) { exercise ->
                ExerciseSearchRow(
                    exercise = exercise,
                    selected = false,
                    onClick = { onExerciseSelected(exercise, searchQuery) }
                )
            }
        }
    }
}

@Composable
private fun StrengthExerciseConfigDialog(
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

@Composable
private fun CustomStrengthExerciseDialog(
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

@Composable
private fun SupersetEditPanel(
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
private fun PendingSwipeDeleteContainer(
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
private fun StrengthPlanExerciseRow(
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

@Composable
private fun StrengthExerciseDetailEditor(
    entry: StrengthPlanEntry,
    isChangingExercise: Boolean,
    onEntryChange: (StrengthPlanEntry) -> Unit,
    onChangingExerciseChange: (Boolean) -> Unit,
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

    LazyColumn(
        modifier = modifier.fillMaxSize(),
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
        item {
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("운동 삭제")
            }
        }
    }
}

@Composable
private fun StrengthExerciseTypeDialog(
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

@Composable
private fun StrengthExercisePickerScreen(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StrengthWorkoutSessionScreen(
    apiKey: String,
    plan: StrengthWorkoutPlan?,
    calendarPlanItem: TrainingItem?,
    isPlanEditable: Boolean,
    activeSession: ActiveStrengthSession?,
    startImmediately: Boolean,
    onImmediateStartConsumed: () -> Unit,
    onSessionChange: (ActiveStrengthSession?) -> Unit,
    onSessionFinished: (CompletedStrengthWorkout?) -> Unit,
    onHistoryClick: (StrengthWorkoutPlan) -> Unit,
    onEditPlan: (StrengthWorkoutPlan) -> Unit,
    onCalendarPlanDeleted: (TrainingItem) -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val completedStrengthHistory = remember(context) { loadCompletedStrengthWorkoutHistory(prefs) }
    val repository = remember(apiKey) { IntervalsRepository(apiKey) }
    val now = remember(activeSession?.planId) { System.currentTimeMillis() }
    val shouldStartImmediately = activeSession == null && startImmediately
    val restoredRestActive = remember(activeSession?.planId) {
        activeSession?.restEndAtMillis?.let { it > System.currentTimeMillis() } == true
    }
    val initialExerciseIndex = remember(activeSession?.planId) {
        if (activeSession != null && activeSession.restEndAtMillis > 0 && activeSession.restEndAtMillis <= now) {
            activeSession.pendingExerciseIndex ?: activeSession.currentExerciseIndex
        } else {
            activeSession?.currentExerciseIndex ?: 0
        }
    }
    val initialSetIndex = remember(activeSession?.planId) {
        if (activeSession != null && activeSession.restEndAtMillis > 0 && activeSession.restEndAtMillis <= now) {
            activeSession.pendingSetIndex ?: activeSession.currentSetIndex
        } else {
            activeSession?.currentSetIndex ?: 0
        }
    }
    var entries by remember(activeSession?.planId, plan?.id) {
        mutableStateOf(activeSession?.entries ?: plan?.entries.orEmpty().map { it.copyForWorkout() })
    }
    var hasStarted by remember(activeSession?.planId, plan?.id) {
        mutableStateOf(activeSession?.hasStarted ?: shouldStartImmediately)
    }
    var workoutStartedAtMillis by remember(activeSession?.planId, plan?.id) {
        mutableStateOf(
            activeSession?.workoutStartedAtMillis?.takeIf { it > 0L }
                ?: if (activeSession?.hasStarted == true || shouldStartImmediately) now else 0L
        )
    }
    var workoutElapsedSeconds by remember(activeSession?.planId, plan?.id) {
        mutableIntStateOf(
            if ((activeSession?.hasStarted == true || shouldStartImmediately) && workoutStartedAtMillis > 0L) {
                ((System.currentTimeMillis() - workoutStartedAtMillis) / 1000L).toInt().coerceAtLeast(0)
            } else {
                0
            }
        )
    }
    var isSetScreenVisible by remember(activeSession?.planId, plan?.id) {
        mutableStateOf(activeSession?.isSetScreenVisible ?: shouldStartImmediately)
    }
    var currentExerciseIndex by remember(activeSession?.planId, plan?.id) { mutableIntStateOf(initialExerciseIndex) }
    var currentSetIndex by remember(activeSession?.planId, plan?.id) { mutableIntStateOf(initialSetIndex) }
    var isChangingCurrentExercise by remember(plan?.id) { mutableStateOf(false) }
    var shouldReturnToOngoingAfterExerciseChange by remember(plan?.id) { mutableStateOf(false) }
    var pendingAddedExerciseEntryId by remember(plan?.id) { mutableStateOf<Int?>(null) }
    var sessionExerciseToConfigure by remember { mutableStateOf<StrengthExercise?>(null) }
    var sessionExerciseToConfigureSearchQuery by remember { mutableStateOf("") }
    var isSessionCustomExerciseDialogVisible by remember { mutableStateOf(false) }
    var pendingExerciseIndex by remember(activeSession?.planId, plan?.id) {
        mutableStateOf(if (restoredRestActive) activeSession?.pendingExerciseIndex else null)
    }
    var pendingSetIndex by remember(activeSession?.planId, plan?.id) {
        mutableStateOf(if (restoredRestActive) activeSession?.pendingSetIndex else null)
    }
    var restRemainingSeconds by remember(activeSession?.planId, plan?.id) {
        mutableStateOf(
            activeSession?.restEndAtMillis
                ?.takeIf { it > now }
                ?.let { ((it - now) / 1000L).toInt().coerceAtLeast(1) }
        )
    }
    var restEndAtMillis by remember(activeSession?.planId, plan?.id) {
        mutableStateOf(activeSession?.restEndAtMillis?.takeIf { it > now } ?: 0L)
    }
    var isRestSheetVisible by remember(activeSession?.planId, plan?.id) {
        mutableStateOf(restoredRestActive && activeSession?.isRestSheetVisible == true)
    }
    var restTitle by remember(activeSession?.planId, plan?.id) {
        mutableStateOf(activeSession?.restTitle.takeIf { restoredRestActive }.orEmpty())
    }
    var setEvents by remember(activeSession?.planId, plan?.id) {
        mutableStateOf(activeSession?.setEvents.orEmpty())
    }
    var restEvents by remember(activeSession?.planId, plan?.id) {
        mutableStateOf(activeSession?.restEvents.orEmpty())
    }
    var activeRestEventId by remember(activeSession?.planId, plan?.id) {
        mutableStateOf(activeSession?.activeRestEventId.takeIf { restoredRestActive })
    }
    var isUploading by remember { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var isFinishChoiceDialogVisible by remember { mutableStateOf(false) }
    var isCalendarPlanDeleteConfirmVisible by remember { mutableStateOf(false) }
    var isDeletingCalendarPlan by remember { mutableStateOf(false) }
    var finishRpe by remember { mutableIntStateOf(7) }

    LaunchedEffect(shouldStartImmediately) {
        if (shouldStartImmediately) {
            onImmediateStartConsumed()
        }
    }

    LaunchedEffect(plan?.entries, hasStarted, activeSession?.planId) {
        if (!hasStarted && activeSession == null) {
            entries = plan?.entries.orEmpty().map { it.copyForWorkout() }
        }
    }

    fun updateEntry(entry: StrengthPlanEntry) {
        entries = entries.map { if (it.id == entry.id) entry else it }
    }

    fun updateCurrentEntry(entry: StrengthPlanEntry) {
        updateEntry(entry)
        if (entry.id == entries.getOrNull(currentExerciseIndex)?.id && currentSetIndex >= entry.records.size) {
            currentSetIndex = (entry.records.size - 1).coerceAtLeast(0)
        }
    }

    fun finishExerciseChange() {
        isChangingCurrentExercise = false
        shouldReturnToOngoingAfterExerciseChange = false
        pendingAddedExerciseEntryId = null
        sessionExerciseToConfigure = null
        isSessionCustomExerciseDialogVisible = false
    }

    fun applyCurrentExerciseChange(exercise: StrengthExercise, equipment: String, variation: String) {
        val entry = entries.getOrNull(currentExerciseIndex) ?: return
        val restoredEntry = if (entry.id == pendingAddedExerciseEntryId) {
            completedStrengthHistory
                .latestMatchingStrengthEntry(exercise, equipment, variation)
                ?.copyAsNewPlanEntry(
                    id = entry.id,
                    exercise = exercise,
                    equipment = equipment,
                    variation = variation
                )
        } else {
            null
        }
        updateCurrentEntry(
            restoredEntry ?: entry.copy(
                exercise = exercise,
                equipment = equipment,
                variation = variation
            )
        )
        finishExerciseChange()
    }

    fun deleteCalendarPlan() {
        val targetPlan = calendarPlanItem ?: return
        scope.launch {
            isDeletingCalendarPlan = true
            uploadError = null
            try {
                if (apiKey.isNotBlank() && !targetPlan.id.startsWith("local-")) {
                    repository.deleteCalendarPlan(targetPlan.remoteId)
                    removeCalendarPlanFromIntervalsCaches(prefs, apiKey, targetPlan)
                }
                removeScheduledStrengthPlan(prefs, targetPlan)
                onCalendarPlanDeleted(targetPlan)
            } catch (error: Exception) {
                uploadError = error.message ?: "Plan을 삭제하지 못했습니다."
            } finally {
                isDeletingCalendarPlan = false
            }
        }
    }

    fun closeActiveRestEvent(reason: String) {
        val eventId = activeRestEventId ?: return
        val endedAt = System.currentTimeMillis()
        restEvents = restEvents.map { event ->
            if (event.id == eventId && event.endedAtMillis == null) {
                event.copy(
                    endedAtMillis = endedAt,
                    endReason = reason
                )
            } else {
                event
            }
        }
        activeRestEventId = null
    }

    fun moveToPendingSet(reason: String = "finished") {
        closeActiveRestEvent(reason)
        pendingExerciseIndex?.let { currentExerciseIndex = it }
        pendingSetIndex?.let { currentSetIndex = it }
        pendingExerciseIndex = null
        pendingSetIndex = null
        restRemainingSeconds = null
        restEndAtMillis = 0L
        isRestSheetVisible = false
        restTitle = ""
        stopRestOverlay(context)
    }

    fun openExerciseSet(exerciseIndex: Int) {
        val safeIndex = exerciseIndex.coerceIn(0, (entries.size - 1).coerceAtLeast(0))
        currentExerciseIndex = safeIndex
        val entry = entries.getOrNull(safeIndex)
        val firstIncomplete = entry?.records?.indexOfFirst { !it.completed } ?: -1
        currentSetIndex = when {
            firstIncomplete >= 0 -> firstIncomplete
            entry != null && entry.records.isNotEmpty() -> entry.records.lastIndex
            else -> 0
        }
        isSetScreenVisible = true
        shouldReturnToOngoingAfterExerciseChange = false
        pendingAddedExerciseEntryId = null
    }

    fun addExerciseToSession() {
        val nextId = (entries.maxOfOrNull { it.id } ?: 0) + 1
        val entry = defaultStrengthPlanEntry(nextId, strengthExerciseCatalog.first())
        entries = entries + entry
        currentExerciseIndex = entries.lastIndex
        currentSetIndex = 0
        isSetScreenVisible = true
        isChangingCurrentExercise = true
        shouldReturnToOngoingAfterExerciseChange = true
        pendingAddedExerciseEntryId = nextId
    }

    fun moveExerciseInSession(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in entries.indices || toIndex !in entries.indices || fromIndex == toIndex) return
        val currentEntryId = entries.getOrNull(currentExerciseIndex)?.id
        val pendingEntryId = pendingExerciseIndex?.let { entries.getOrNull(it)?.id }
        entries = entries.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
        currentEntryId?.let { id ->
            entries.indexOfFirst { it.id == id }
                .takeIf { it >= 0 }
                ?.let { currentExerciseIndex = it }
        }
        pendingEntryId?.let { id ->
            pendingExerciseIndex = entries.indexOfFirst { it.id == id }.takeIf { it >= 0 }
        }
    }

    fun startRest(title: String, seconds: Int, restEvent: StrengthRestEvent? = null) {
        if (seconds <= 0) {
            moveToPendingSet()
            return
        }
        restEvent?.let { event ->
            restEvents = restEvents + event
            activeRestEventId = event.id
        }
        restTitle = title
        restRemainingSeconds = seconds
        restEndAtMillis = restEvent?.targetEndAtMillis ?: (System.currentTimeMillis() + seconds * 1000L)
        isRestSheetVisible = true
        requestOverlayPermissionIfNeeded(context)
        stopRestOverlay(context)
    }

    fun setRestSeconds(seconds: Int) {
        val safeSeconds = seconds.coerceAtLeast(0)
        if (safeSeconds == 0) {
            moveToPendingSet("stopped")
        } else {
            restRemainingSeconds = safeSeconds
            val nextEndAtMillis = System.currentTimeMillis() + safeSeconds * 1000L
            restEndAtMillis = nextEndAtMillis
            activeRestEventId?.let { eventId ->
                restEvents = restEvents.map { event ->
                    if (event.id == eventId && event.endedAtMillis == null) {
                        event.copy(targetEndAtMillis = nextEndAtMillis)
                    } else {
                        event
                    }
                }
            }
            if (isRestSheetVisible) {
                stopRestOverlay(context)
            } else {
                startRestOverlay(context, restTitle, restEndAtMillis)
            }
        }
    }

    fun adjustRestSeconds(deltaSeconds: Int) {
        setRestSeconds((restRemainingSeconds ?: 0) + deltaSeconds)
    }

    fun completeCurrentSet() {
        val entry = entries.getOrNull(currentExerciseIndex) ?: return
        val targetSetIndex = entry.records.indexOfFirst { !it.completed }
            .takeIf { it >= 0 }
            ?: currentSetIndex
        currentSetIndex = targetSetIndex
        val record = entry.records.getOrNull(targetSetIndex) ?: return
        if (record.completed) {
            val nextSetIndex = entry.records.indexOfFirst { !it.completed }
            if (nextSetIndex >= 0) {
                currentSetIndex = nextSetIndex
            } else {
                nextIncompleteSet(entries, currentExerciseIndex, targetSetIndex)?.let { (exerciseIndex, setIndex) ->
                    currentExerciseIndex = exerciseIndex
                    currentSetIndex = setIndex
                } ?: run {
                    isSetScreenVisible = false
                }
            }
            return
        }
        val completedAtMillis = System.currentTimeMillis()
        val isUnilateralEntry = entry.isUnilateral()
        val setEvent = StrengthSetCompletionEvent(
            sequence = (setEvents.maxOfOrNull { it.sequence } ?: 0) + 1,
            exerciseEntryId = entry.id,
            exerciseTitle = entry.title,
            exerciseGroup = entry.exercise.group,
            exerciseId = entry.exercise.id,
            equipment = entry.equipment,
            variation = entry.variation,
            setRecordId = record.id,
            setIndex = targetSetIndex,
            weightKg = record.weightKg,
            reps = if (isUnilateralEntry) "각 ${record.reps}" else record.reps,
            targetRestSeconds = record.restSeconds.toIntOrNull() ?: entry.restSeconds,
            completedAtMillis = completedAtMillis
        )
        setEvents = setEvents + setEvent
        val updatedEntry = entry.copy(
            records = entry.records.mapIndexed { index, old ->
                if (index == targetSetIndex) old.copy(completed = true) else old
            }
        )
        val updatedEntries = entries.map { if (it.id == entry.id) updatedEntry else it }
        entries = updatedEntries
        val nextIncomplete = nextIncompleteSet(updatedEntries, currentExerciseIndex, targetSetIndex)

        pendingExerciseIndex = nextIncomplete?.first
        pendingSetIndex = nextIncomplete?.second
        val restSeconds = record.restSeconds.toIntOrNull() ?: entry.restSeconds
        if (nextIncomplete != null && restSeconds > 0) {
            startRest(
                title = entry.title,
                seconds = restSeconds,
                restEvent = StrengthRestEvent(
                    id = (restEvents.maxOfOrNull { it.id } ?: 0) + 1,
                    afterSetSequence = setEvent.sequence,
                    exerciseEntryId = entry.id,
                    exerciseTitle = entry.title,
                    setRecordId = record.id,
                    setIndex = targetSetIndex,
                    startedAtMillis = completedAtMillis,
                    plannedSeconds = restSeconds,
                    targetEndAtMillis = completedAtMillis + restSeconds * 1000L,
                    endedAtMillis = null,
                    endReason = null
                )
            )
        } else if (nextIncomplete != null) {
            moveToPendingSet()
        } else {
            closeActiveRestEvent("finished")
            pendingExerciseIndex = null
            pendingSetIndex = null
            restRemainingSeconds = null
            restEndAtMillis = 0L
            isRestSheetVisible = false
            restTitle = ""
            isSetScreenVisible = false
            stopRestOverlay(context)
        }
    }

    fun uploadWorkout() {
        if (apiKey.isBlank()) {
            uploadMessage = null
            uploadError = "Intervals.icu 업데이트는 로그인 후 사용할 수 있습니다."
            return
        }
        val endedAtMillis = System.currentTimeMillis()
        val finalizedRestEvents = finalizeRestEvents(restEvents, activeRestEventId, endedAtMillis, "workout_finished")
        val trainingLoad = entries.strengthTrainingLoad(finishRpe)
        val localWorkout = plan?.let {
            buildCompletedStrengthWorkout(
                plan = it,
                entries = entries,
                setEvents = setEvents,
                restEvents = finalizedRestEvents,
                startedAtMillis = workoutStartedAtMillis,
                endedAtMillis = endedAtMillis,
                rpe = finishRpe,
                trainingLoad = trainingLoad,
                uploadedToIntervals = true
            )
        }
        scope.launch {
            isUploading = true
            uploadMessage = null
            uploadError = null
            try {
                repository.uploadStrengthWorkout(
                    StrengthWorkoutSession(
                        name = plan?.name ?: "웨이트 트레이닝",
                        startedAt = workoutStartedAtMillis
                            .takeIf { it > 0L }
                            ?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) }
                            ?: LocalDateTime.now().minusSeconds(entries.totalDurationSeconds().toLong()),
                        entries = entries,
                        rpe = finishRpe,
                        trainingLoad = trainingLoad
                    )
                )
                uploadMessage = "Intervals.icu에 업로드했습니다."
                localWorkout?.let { appendStrengthWorkoutHistory(prefs, it) }
                stopRestOverlay(context)
                onSessionFinished(localWorkout)
            } catch (error: Exception) {
                uploadError = error.message ?: "업로드하지 못했습니다."
            } finally {
                isUploading = false
            }
        }
    }

    fun finishWorkout() {
        val endedAtMillis = System.currentTimeMillis()
        val finalizedRestEvents = finalizeRestEvents(restEvents, activeRestEventId, endedAtMillis, "workout_finished")
        val trainingLoad = entries.strengthTrainingLoad(finishRpe)
        val localWorkout = plan?.let {
            buildCompletedStrengthWorkout(
                plan = it,
                entries = entries,
                setEvents = setEvents,
                restEvents = finalizedRestEvents,
                startedAtMillis = workoutStartedAtMillis,
                endedAtMillis = endedAtMillis,
                rpe = finishRpe,
                trainingLoad = trainingLoad,
                uploadedToIntervals = apiKey.isNotBlank()
            )
        }
        if (apiKey.isBlank()) {
            val savedWorkout = localWorkout?.copy(uploadedToIntervals = false)
            savedWorkout?.let { appendStrengthWorkoutHistory(prefs, it) }
            stopRestOverlay(context)
            onSessionFinished(savedWorkout)
        } else {
            uploadWorkout()
        }
    }

    fun discardWorkout() {
        closeActiveRestEvent("discarded")
        restRemainingSeconds = null
        restEndAtMillis = 0L
        isRestSheetVisible = false
        restTitle = ""
        stopRestOverlay(context)
        onSessionFinished(null)
    }

    LaunchedEffect(
        plan?.id,
        plan?.name,
        hasStarted,
        workoutStartedAtMillis,
        isSetScreenVisible,
        entries,
        currentExerciseIndex,
        currentSetIndex,
        pendingExerciseIndex,
        pendingSetIndex,
        restEndAtMillis,
        isRestSheetVisible,
        restTitle,
        setEvents,
        restEvents,
        activeRestEventId
    ) {
        if (hasStarted && plan != null) {
            onSessionChange(
                ActiveStrengthSession(
                    planId = plan.id,
                    planName = plan.name,
                    entries = entries,
                    hasStarted = hasStarted,
                    workoutStartedAtMillis = workoutStartedAtMillis,
                    isSetScreenVisible = isSetScreenVisible,
                    currentExerciseIndex = currentExerciseIndex,
                    currentSetIndex = currentSetIndex,
                    pendingExerciseIndex = pendingExerciseIndex,
                    pendingSetIndex = pendingSetIndex,
                    restEndAtMillis = restEndAtMillis,
                    isRestSheetVisible = isRestSheetVisible,
                    restTitle = restTitle,
                    setEvents = setEvents,
                    restEvents = restEvents,
                    activeRestEventId = activeRestEventId
                )
            )
        }
    }

    fun handleBack() {
        when {
            sessionExerciseToConfigure != null -> sessionExerciseToConfigure = null
            isSessionCustomExerciseDialogVisible -> isSessionCustomExerciseDialogVisible = false
            isChangingCurrentExercise -> {
                pendingAddedExerciseEntryId?.let { addedEntryId ->
                    entries = entries.filterNot { it.id == addedEntryId }
                    currentExerciseIndex = currentExerciseIndex.coerceIn(0, (entries.size - 1).coerceAtLeast(0))
                    currentSetIndex = currentSetIndex.coerceAtLeast(0)
                }
                isChangingCurrentExercise = false
                if (shouldReturnToOngoingAfterExerciseChange) {
                    isSetScreenVisible = false
                }
                shouldReturnToOngoingAfterExerciseChange = false
                pendingAddedExerciseEntryId = null
                sessionExerciseToConfigure = null
                isSessionCustomExerciseDialogVisible = false
            }
            hasStarted && isSetScreenVisible -> isSetScreenVisible = false
            hasStarted -> onBack()
            else -> onBack()
        }
    }

    BackHandler(enabled = isChangingCurrentExercise || hasStarted) {
        handleBack()
    }

    LaunchedEffect(hasStarted, workoutStartedAtMillis) {
        while (hasStarted && workoutStartedAtMillis > 0L) {
            workoutElapsedSeconds = ((System.currentTimeMillis() - workoutStartedAtMillis) / 1000L)
                .toInt()
                .coerceAtLeast(0)
            delay(1_000)
        }
    }

    LaunchedEffect(restRemainingSeconds) {
        val remaining = restRemainingSeconds ?: return@LaunchedEffect
        if (remaining > 0) {
            delay(1_000)
            restRemainingSeconds = remaining - 1
        } else {
            notifyRestFinished(context)
            moveToPendingSet()
        }
    }

    DisposableEffect(context, restEndAtMillis, restTitle, isRestSheetVisible) {
        val lifecycle = (context as? LifecycleOwner)?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (
                (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_RESUME) &&
                restEndAtMillis > System.currentTimeMillis()
            ) {
                if (event == Lifecycle.Event.ON_PAUSE || !isRestSheetVisible) {
                    startRestOverlay(context, restTitle, restEndAtMillis)
                } else {
                    stopRestOverlay(context)
                }
            }
        }
        lifecycle?.addObserver(observer)
        onDispose {
            lifecycle?.removeObserver(observer)
        }
    }

    LaunchedEffect(isRestSheetVisible, restEndAtMillis, restTitle) {
        if (restEndAtMillis > System.currentTimeMillis()) {
            if (isRestSheetVisible) {
                stopRestOverlay(context)
            } else {
                startRestOverlay(context, restTitle, restEndAtMillis)
            }
        }
    }

    LaunchedEffect(RestOverlayRequests.showSheetRequest) {
        if (RestOverlayRequests.showSheetRequest > 0 && restRemainingSeconds != null) {
            isRestSheetVisible = true
        }
    }

    if (isRestSheetVisible) restRemainingSeconds?.let { remaining ->
        RestTimerBottomSheet(
            title = restTitle,
            remainingSeconds = remaining,
            onAdjustSeconds = ::adjustRestSeconds,
            onSetSeconds = ::setRestSeconds,
            onDismiss = { isRestSheetVisible = false },
            onStop = { moveToPendingSet("stopped") }
        )
    }

    sessionExerciseToConfigure?.let { exercise ->
        StrengthExerciseConfigDialog(
            exercise = exercise,
            initialSearchQuery = sessionExerciseToConfigureSearchQuery,
            onDismiss = { sessionExerciseToConfigure = null },
            onDone = { equipment, variation ->
                applyCurrentExerciseChange(exercise, equipment, variation)
            }
        )
    }

    if (isSessionCustomExerciseDialogVisible) {
        CustomStrengthExerciseDialog(
            onDismiss = { isSessionCustomExerciseDialogVisible = false },
            onAdd = { name ->
                isSessionCustomExerciseDialogVisible = false
                sessionExerciseToConfigureSearchQuery = ""
                sessionExerciseToConfigure = customStrengthExercise(name)
            }
        )
    }

    if (isFinishChoiceDialogVisible) {
        AlertDialog(
            onDismissRequest = { isFinishChoiceDialogVisible = false },
            title = { Text("운동 완료") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (apiKey.isBlank()) {
                            "운동 기록을 로컬에 저장하거나 삭제할 수 있습니다."
                        } else {
                            "운동 기록을 저장하면 로컬 기록에 남기고 Intervals.icu 업로드를 시도합니다."
                        }
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "RPE",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = finishRpe.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = finishRpe.toFloat(),
                            onValueChange = { finishRpe = it.roundToInt().coerceIn(1, 10) },
                            valueRange = 1f..10f,
                            steps = 8
                        )
                        Text(
                            text = "Strength Load ${entries.strengthTrainingLoad(finishRpe)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isFinishChoiceDialogVisible = false
                        finishWorkout()
                    },
                    enabled = !isUploading
                ) {
                    Text("저장")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        isFinishChoiceDialogVisible = false
                        discardWorkout()
                    },
                    enabled = !isUploading
                ) {
                    Text("삭제")
                }
            }
        )
    }

    if (isCalendarPlanDeleteConfirmVisible && calendarPlanItem != null) {
        AlertDialog(
            onDismissRequest = { if (!isDeletingCalendarPlan) isCalendarPlanDeleteConfirmVisible = false },
            title = { Text("Plan 삭제") },
            text = {
                Text(
                    text = calendarPlanItem.plannedWorkoutDeleteConfirmMessage()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isCalendarPlanDeleteConfirmVisible = false
                        deleteCalendarPlan()
                    },
                    enabled = !isDeletingCalendarPlan
                ) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { isCalendarPlanDeleteConfirmVisible = false },
                    enabled = !isDeletingCalendarPlan
                ) {
                    Text("취소")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            val showBackIcon = true
            if (showBackIcon) {
                TopAppBar(
                    title = {
                        StrengthWorkoutTopBarTitle(
                            title = if (isChangingCurrentExercise) "운동 목록" else plan?.name ?: "웨이트 수행",
                            isWorkoutActive = hasStarted,
                            elapsedSeconds = workoutElapsedSeconds
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = ::handleBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                        }
                    },
                    actions = {
                        if (!hasStarted && plan != null && !isChangingCurrentExercise) {
                            if (calendarPlanItem?.isPlan == true) {
                                IconButton(
                                    onClick = { isCalendarPlanDeleteConfirmVisible = true },
                                    enabled = !isDeletingCalendarPlan
                                ) {
                                    if (isDeletingCalendarPlan) {
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
                            IconButton(onClick = { onHistoryClick(plan) }) {
                                Icon(Icons.Outlined.Schedule, contentDescription = "History")
                            }
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        StrengthWorkoutTopBarTitle(
                            title = if (isChangingCurrentExercise) "운동 목록" else plan?.name ?: "웨이트 수행",
                            isWorkoutActive = hasStarted,
                            elapsedSeconds = workoutElapsedSeconds
                        )
                    }
                )
            }
        },
        floatingActionButton = {
            if (
                hasStarted &&
                restRemainingSeconds != null &&
                !isRestSheetVisible &&
                !isChangingCurrentExercise &&
                !Settings.canDrawOverlays(context)
            ) {
                RestTimerFloatingChip(
                    title = restTitle,
                    remainingSeconds = restRemainingSeconds ?: 0,
                    onClick = { isRestSheetVisible = true }
                )
            }
        },
        bottomBar = {
            if (hasStarted && plan != null && !isChangingCurrentExercise && isSetScreenVisible) {
                StrengthSetBottomBar(
                    allDone = entries.allSetsCompleted(),
                    currentLabel = entries.getOrNull(currentExerciseIndex)?.let { entry ->
                        val nextSet = entry.records.indexOfFirst { !it.completed }
                            .takeIf { it >= 0 }
                            ?: currentSetIndex
                        "Set ${nextSet + 1} · ${entry.title}"
                    }.orEmpty(),
                    onCompleteSet = ::completeCurrentSet,
                    isUploading = isUploading
                )
            } else if (hasStarted && plan != null && !isChangingCurrentExercise) {
                StrengthWorkoutFinishBar(
                    isUploading = isUploading,
                    onFinish = { isFinishChoiceDialogVisible = true }
                )
            }
        }
    ) { innerPadding ->
        if (plan == null) {
            EmptyView(message = "선택된 웨이트 Plan이 없습니다.")
            return@Scaffold
        }

        if (!hasStarted) {
            StrengthWorkoutReadyScreen(
                plan = plan,
                entries = entries,
                modifier = Modifier.padding(innerPadding),
                onStart = {
                    hasStarted = true
                    workoutStartedAtMillis = System.currentTimeMillis()
                    workoutElapsedSeconds = 0
                    nextIncompleteSet(entries, 0, -1)?.let { (exerciseIndex, setIndex) ->
                        currentExerciseIndex = exerciseIndex
                        currentSetIndex = setIndex
                    }
                    isSetScreenVisible = true
                },
                onEditPlan = if (isPlanEditable) {
                    { onEditPlan(plan) }
                } else {
                    null
                }
            )
        } else {
            val currentEntry = entries.getOrNull(currentExerciseIndex)
            if (isChangingCurrentExercise && currentEntry != null) {
                StrengthExerciseListScreen(
                    modifier = Modifier.padding(innerPadding),
                    onAddCustomExercise = { isSessionCustomExerciseDialogVisible = true },
                    onExerciseSelected = { exercise, searchQuery ->
                        sessionExerciseToConfigureSearchQuery = searchQuery
                        sessionExerciseToConfigure = exercise
                    }
                )
            } else if (isSetScreenVisible) {
                StrengthSetExecutionScreen(
                    entry = currentEntry,
                    modifier = Modifier.padding(innerPadding),
                    onExerciseClick = {
                        shouldReturnToOngoingAfterExerciseChange = false
                        pendingAddedExerciseEntryId = null
                        isChangingCurrentExercise = true
                    },
                    onEntryChange = ::updateCurrentEntry,
                    onAddSet = {
                        currentEntry?.let { entry ->
                            val nextEntry = entry.withRecords(entry.records + defaultStrengthSetRecord(entry))
                            updateEntry(nextEntry)
                            currentSetIndex = nextEntry.records.lastIndex
                        }
                    }
                )
            } else {
                StrengthWorkoutOngoingPlanScreen(
                    plan = plan,
                    entries = entries,
                    currentExerciseIndex = currentExerciseIndex,
                    uploadMessage = uploadMessage,
                    uploadError = uploadError,
                    modifier = Modifier.padding(innerPadding),
                    onExerciseClick = ::openExerciseSet,
                    onAddExercise = ::addExerciseToSession,
                    onMoveExercise = { fromIndex, direction ->
                        val toIndex = (fromIndex + direction).coerceIn(entries.indices)
                        moveExerciseInSession(fromIndex, toIndex)
                    }
                )
            }
        }
    }
}

@Composable
private fun StrengthWorkoutTopBarTitle(
    title: String,
    isWorkoutActive: Boolean,
    elapsedSeconds: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (isWorkoutActive) {
            MaterialSurface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Text(
                    text = formatClock(elapsedSeconds),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun StrengthWorkoutReadyScreen(
    plan: StrengthWorkoutPlan,
    entries: List<StrengthPlanEntry>,
    modifier: Modifier = Modifier,
    onStart: () -> Unit,
    onEditPlan: (() -> Unit)?,
) {
    var expandedEntryIds by remember(plan.id, entries) { mutableStateOf(emptySet<Int>()) }
    val supersetLabels = remember(entries) { entries.supersetGroupLabels() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = onStart,
                enabled = entries.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("운동 시작")
            }
        }
        if (onEditPlan != null) {
            item {
                OutlinedButton(
                    onClick = onEditPlan,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("운동 수정")
                }
            }
        }
        item {
            Text(
                text = "운동 목록",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        items(entries, key = { it.id }) { entry ->
            val isExpanded = entry.id in expandedEntryIds
            val supersetLabel = entry.supersetGroupId?.let { supersetLabels[it] }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = spring())
                    .clickable {
                        expandedEntryIds = if (isExpanded) {
                            expandedEntryIds - entry.id
                        } else {
                            expandedEntryIds + entry.id
                        }
                    },
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        text = "${entry.records.size}세트",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            entry.records.forEachIndexed { index, record ->
                                StrengthReadySetRow(
                                    entry = entry,
                                    record = record,
                                    index = index
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StrengthReadySetRow(
    entry: StrengthPlanEntry,
    record: StrengthSetRecord,
    index: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Set ${index + 1}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(52.dp)
        )
        Text(
            text = buildStrengthSetSummary(entry, record),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StrengthWorkoutOngoingPlanScreen(
    plan: StrengthWorkoutPlan,
    entries: List<StrengthPlanEntry>,
    currentExerciseIndex: Int,
    uploadMessage: String?,
    uploadError: String?,
    modifier: Modifier = Modifier,
    onExerciseClick: (Int) -> Unit,
    onAddExercise: () -> Unit,
    onMoveExercise: (index: Int, direction: Int) -> Unit,
) {
    var draggingEntryId by remember { mutableStateOf<Int?>(null) }
    var draggingOffsetY by remember { mutableStateOf(0f) }
    var entryHeights by remember { mutableStateOf(emptyMap<Int, Int>()) }

    fun startEntryDrag(entryId: Int) {
        draggingEntryId = entryId
        draggingOffsetY = 0f
    }

    fun updateEntryDrag(entryId: Int, deltaY: Float) {
        if (draggingEntryId != entryId) return
        draggingOffsetY += deltaY
        val currentIndex = entries.indexOfFirst { it.id == entryId }
        if (currentIndex < 0) return

        if (draggingOffsetY > 0f && currentIndex < entries.lastIndex) {
            val nextEntry = entries[currentIndex + 1]
            val nextHeight = (entryHeights[nextEntry.id] ?: entryHeights[entryId] ?: 1).toFloat()
            if (draggingOffsetY >= nextHeight / 2f) {
                onMoveExercise(currentIndex, 1)
                draggingOffsetY -= nextHeight
            }
        } else if (draggingOffsetY < 0f && currentIndex > 0) {
            val previousEntry = entries[currentIndex - 1]
            val previousHeight = (entryHeights[previousEntry.id] ?: entryHeights[entryId] ?: 1).toFloat()
            if (-draggingOffsetY >= previousHeight / 2f) {
                onMoveExercise(currentIndex, -1)
                draggingOffsetY += previousHeight
            }
        }
    }

    fun endEntryDrag() {
        draggingEntryId = null
        draggingOffsetY = 0f
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "진행 중 운동",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        itemsIndexed(entries, key = { _, entry -> entry.id }) { index, entry ->
            val completedSets = entry.records.count { it.completed }
            val isComplete = entry.records.isNotEmpty() && completedSets == entry.records.size
            val isCurrent = index == currentExerciseIndex
            val isDragging = draggingEntryId == entry.id
            StrengthOngoingExerciseRow(
                entry = entry,
                completedSets = completedSets,
                isComplete = isComplete,
                isCurrent = isCurrent,
                isDragging = isDragging,
                dragHandleModifier = Modifier.pointerInput(entry.id) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { startEntryDrag(entry.id) },
                        onDragEnd = ::endEntryDrag,
                        onDragCancel = ::endEntryDrag
                    ) { change, dragAmount ->
                        change.consume()
                        updateEntryDrag(entry.id, dragAmount.y)
                    }
                },
                modifier = Modifier
                    .animateItem()
                    .onSizeChanged { size ->
                        entryHeights = entryHeights + (entry.id to size.height)
                    }
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) draggingOffsetY else 0f
                        shadowElevation = if (isDragging) 18f else 0f
                        scaleX = if (isDragging) 1.015f else 1f
                        scaleY = if (isDragging) 1.015f else 1f
                    },
                onClick = { onExerciseClick(index) },
            )
        }
        item {
            OutlinedButton(
                onClick = onAddExercise,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("신규 운동 추가")
            }
        }
        if (uploadMessage != null || uploadError != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        uploadMessage?.let {
                            Text(it, color = MaterialTheme.colorScheme.primary)
                        }
                        uploadError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StrengthOngoingExerciseRow(
    entry: StrengthPlanEntry,
    completedSets: Int,
    isComplete: Boolean,
    isCurrent: Boolean,
    isDragging: Boolean,
    dragHandleModifier: Modifier,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val baseContainerColor = if (isCurrent) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isComplete && !isDragging) 0.62f else 1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) MaterialTheme.colorScheme.secondaryContainer else baseContainerColor
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.DragIndicator,
                contentDescription = "길게 눌러 순서 변경",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(40.dp)
                    .then(dragHandleModifier)
            )
            Icon(
                imageVector = if (isComplete) Icons.Outlined.CheckCircle else Icons.Outlined.FitnessCenter,
                contentDescription = null,
                tint = if (isComplete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (isComplete) {
                        Text(
                            text = "완료",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "$completedSets/${entry.records.size} 세트 완료",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "이동",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StrengthExerciseSetDialog(
    entry: StrengthPlanEntry,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("확인")
            }
        },
        title = { Text(entry.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                entry.records.forEachIndexed { index, record ->
                    Text(
                        text = if (entry.isUnilateral()) {
                            "Set ${index + 1}  ${record.unilateralWeightSummary()}  ${record.unilateralRepsSummary()}  휴식 ${record.restSeconds.ifBlank { "-" }}초"
                        } else {
                            "Set ${index + 1}  ${record.weightKg.ifBlank { "-" }}kg  ${record.reps.ifBlank { "-" }}회  휴식 ${record.restSeconds.ifBlank { "-" }}초"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    )
}

@Composable
private fun StrengthSetExecutionScreen(
    entry: StrengthPlanEntry?,
    modifier: Modifier = Modifier,
    onExerciseClick: () -> Unit,
    onEntryChange: (StrengthPlanEntry) -> Unit,
    onAddSet: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (entry == null) {
            item {
                EmptyView(message = "수행할 세트가 없습니다.")
            }
        } else {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onExerciseClick),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                        }
                        Text(
                            text = "변경",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
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
                            onEntryChange(
                                entry.withRecords(
                                    entry.records.filterIndexed { recordIndex, _ -> recordIndex != index }
                                )
                            )
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
                    onClick = onAddSet,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("세트 추가")
                }
            }
        }
    }
}

@Composable
private fun StrengthSetBottomBar(
    allDone: Boolean,
    currentLabel: String,
    isUploading: Boolean,
    onCompleteSet: () -> Unit,
) {
    Surface(
        modifier = Modifier.navigationBarsPadding(),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!allDone) {
                Text(
                    text = currentLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Button(
                onClick = onCompleteSet,
                enabled = !isUploading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    when {
                        isUploading -> "업로드 중"
                        allDone -> "운동 목록으로"
                        else -> "세트 완료"
                    }
                )
            }
        }
    }
}

@Composable
private fun StrengthWorkoutFinishBar(
    isUploading: Boolean,
    onFinish: () -> Unit,
) {
    Surface(
        modifier = Modifier.navigationBarsPadding(),
        shadowElevation = 8.dp
    ) {
        Button(
            onClick = onFinish,
            enabled = !isUploading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Outlined.CloudUpload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isUploading) "업로드 중" else "운동 종료")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RestTimerBottomSheet(
    title: String,
    remainingSeconds: Int,
    onAdjustSeconds: (Int) -> Unit,
    onSetSeconds: (Int) -> Unit,
    onDismiss: () -> Unit,
    onStop: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title.ifBlank { "세트 휴식" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatClock(remainingSeconds),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            RestTimeControls(
                onAdjustSeconds = onAdjustSeconds,
                onSetSeconds = onSetSeconds
            )
            OutlinedButton(
                onClick = onStop,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("휴식 중단")
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun RestTimeControls(
    onAdjustSeconds: (Int) -> Unit,
    onSetSeconds: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RestTimeBubble(text = "-10초", onClick = { onAdjustSeconds(-10) })
        RestTimeBubble(text = "+10초", onClick = { onAdjustSeconds(10) })
        RestTimeBubble(text = "30초", onClick = { onSetSeconds(30) })
        RestTimeBubble(text = "60초", onClick = { onSetSeconds(60) })
        RestTimeBubble(text = "90초", onClick = { onSetSeconds(90) })
        RestTimeBubble(text = "2분", onClick = { onSetSeconds(120) })
        RestTimeBubble(text = "3분", onClick = { onSetSeconds(180) })
        RestTimeBubble(text = "5분", onClick = { onSetSeconds(300) })
    }
}

@Composable
private fun RestTimeBubble(
    text: String,
    onClick: () -> Unit,
) {
    MaterialSurface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
        )
    }
}

@Composable
private fun RestTimerFloatingChip(
    title: String,
    remainingSeconds: Int,
    onClick: () -> Unit,
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    MaterialSurface(
        modifier = Modifier
            .navigationBarsPadding()
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Outlined.Schedule, contentDescription = null)
            Text(
                text = "${title.ifBlank { "휴식" }} ${formatClock(remainingSeconds)}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StrengthUploadPanel(
    apiKey: String,
    planName: String,
    entries: List<StrengthPlanEntry>,
    isUploading: Boolean,
    uploadMessage: String?,
    uploadError: String?,
    onUpload: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val completedSets = entries.sumOf { entry -> entry.records.count { it.completed } }
            val totalSets = entries.sumOf { it.records.size }
            val volume = entries.totalVolumeKg()
            val estimatedLoad = entries.strengthTrainingLoad(7)
            Text(
                text = "운동 완료 준비",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$planName · $completedSets/$totalSets 세트 완료 · 볼륨 ${formatWeight(volume)} kg · 예상 Load $estimatedLoad",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (apiKey.isBlank()) {
                Text(
                    text = "Intervals.icu 업데이트는 로그인 후 사용할 수 있습니다.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            uploadMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
            uploadError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = onUpload,
                enabled = entries.isNotEmpty() && !isUploading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Outlined.CloudUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isUploading) "업로드 중" else "운동 완료")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StrengthWorkoutScreen(
    apiKey: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember(apiKey) { IntervalsRepository(apiKey) }
    var workoutName by remember { mutableStateOf("웨이트 트레이닝") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedExercise by remember { mutableStateOf(strengthExerciseCatalog.first()) }
    var selectedEquipment by remember { mutableStateOf(selectedExercise.equipmentOptions.first()) }
    var selectedVariation by remember { mutableStateOf(selectedExercise.baseVariationOptions().first()) }
    var selectedUnilateral by remember { mutableStateOf("양쪽") }
    var targetSets by remember { mutableStateOf("3") }
    var targetReps by remember { mutableStateOf("8") }
    var restSeconds by remember { mutableStateOf("120") }
    var targetWeight by remember { mutableStateOf("") }
    var nextPlanId by remember { mutableIntStateOf(1) }
    var planEntries by remember { mutableStateOf<List<StrengthPlanEntry>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    val candidates = remember(searchQuery) {
        strengthExerciseCatalog
            .filter { exercise -> exercise.matchesSearch(searchQuery) }
            .take(12)
    }

    fun selectExercise(exercise: StrengthExercise) {
        selectedExercise = exercise
        selectedEquipment = exercise.equipmentOptions.first()
        selectedVariation = exercise.baseVariationOptions().first()
        selectedUnilateral = "양쪽"
    }

    fun updateEntry(entry: StrengthPlanEntry) {
        planEntries = planEntries.map { if (it.id == entry.id) entry else it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("웨이트 Plan & 기록") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = workoutName,
                            onValueChange = { workoutName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Workout 이름") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("운동 검색") },
                            singleLine = true
                        )
                        Text(
                            text = "운동 선택",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        candidates.forEach { exercise ->
                            ExerciseSearchRow(
                                exercise = exercise,
                                selected = exercise.id == selectedExercise.id,
                                onClick = { selectExercise(exercise) }
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = selectedExercise.nameKo,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${selectedExercise.nameEn} · ${selectedExercise.group}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        ChoiceGrid(
                            title = "기구",
                            options = selectedExercise.equipmentOptionsWithBodyweight(),
                            selected = selectedEquipment,
                            onSelected = { selectedEquipment = if (selectedEquipment == it) "" else it }
                        )
                        ChoiceGrid(
                            title = "세부 타입",
                            options = selectedExercise.baseVariationOptions(),
                            selected = selectedVariation,
                            onSelected = { selectedVariation = it }
                        )
                        ChoiceGrid(
                            title = "좌우 방식",
                            options = UNILATERAL_MODE_OPTIONS,
                            selected = selectedUnilateral,
                            onSelected = { selectedUnilateral = it }
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            NumberField(
                                value = targetSets,
                                onValueChange = { targetSets = it },
                                label = "세트",
                                modifier = Modifier.weight(1f)
                            )
                            NumberField(
                                value = targetReps,
                                onValueChange = { targetReps = it },
                                label = "횟수",
                                modifier = Modifier.weight(1f)
                            )
                            NumberField(
                                value = restSeconds,
                                onValueChange = { restSeconds = it },
                                label = "휴식초",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        NumberField(
                            value = targetWeight,
                            onValueChange = { targetWeight = it },
                            label = "목표 무게 kg",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                val sets = targetSets.toIntOrNull()?.coerceIn(1, 20) ?: 1
                                val reps = targetReps.toIntOrNull()?.coerceAtLeast(0) ?: 0
                                val rest = restSeconds.toIntOrNull()?.coerceAtLeast(0) ?: 0
                                val records = List(sets) { index ->
                                    StrengthSetRecord(
                                        id = index + 1,
                                        weightKg = targetWeight,
                                        reps = reps.takeIf { it > 0 }?.toString().orEmpty(),
                                        durationSeconds = "",
                                        restSeconds = rest.toString(),
                                        completed = false
                                    )
                                }
                                planEntries = planEntries + StrengthPlanEntry(
                                    id = nextPlanId,
                                    exercise = selectedExercise,
                                    equipment = selectedEquipment,
                                    variation = combineVariationAndUnilateral(selectedVariation, selectedUnilateral),
                                    supersetGroupId = null,
                                    targetSets = sets,
                                    targetReps = reps,
                                    restSeconds = rest,
                                    targetWeightKg = targetWeight,
                                    records = records
                                )
                                nextPlanId += 1
                                uploadMessage = null
                                uploadError = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Outlined.FitnessCenter, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Plan에 추가")
                        }
                    }
                }
            }

            if (planEntries.isEmpty()) {
                item {
                    EmptyView(message = "운동을 선택하고 Plan에 추가하세요.")
                }
            } else {
                items(planEntries, key = { it.id }) { entry ->
                    StrengthPlanEntryCard(
                        entry = entry,
                        onEntryChange = ::updateEntry,
                        onDelete = {
                            planEntries = planEntries.filterNot { it.id == entry.id }
                        }
                    )
                }
            }

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
                        val completedSets = planEntries.sumOf { entry -> entry.records.count { it.completed } }
                        val totalSets = planEntries.sumOf { it.records.size }
                        val volume = planEntries.totalVolumeKg()
                        Text(
                            text = "업로드 준비",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$completedSets/$totalSets 세트 완료 · 볼륨 ${formatWeight(volume)} kg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        uploadMessage?.let {
                            Text(it, color = MaterialTheme.colorScheme.primary)
                        }
                        uploadError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                        Button(
                            onClick = {
                                if (apiKey.isBlank()) {
                                    uploadMessage = null
                                    uploadError = "Intervals.icu 업데이트는 로그인 후 사용할 수 있습니다."
                                    return@Button
                                }
                                scope.launch {
                                    isUploading = true
                                    uploadMessage = null
                                    uploadError = null
                                    try {
                                        repository.uploadStrengthWorkout(
                                            StrengthWorkoutSession(
                                                name = workoutName.ifBlank { "웨이트 트레이닝" },
                                                startedAt = LocalDateTime.now().minusSeconds(
                                                    planEntries.totalDurationSeconds().toLong()
                                                ),
                                                entries = planEntries,
                                                rpe = 7,
                                                trainingLoad = planEntries.strengthTrainingLoad(7)
                                            )
                                        )
                                        uploadMessage = "Intervals.icu에 업로드했습니다."
                                    } catch (error: Exception) {
                                        uploadError = error.message ?: "업로드하지 못했습니다."
                                    } finally {
                                        isUploading = false
                                    }
                                }
                            },
                            enabled = planEntries.isNotEmpty() && !isUploading,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Outlined.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isUploading) "업로드 중" else "Intervals.icu 업데이트")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseSearchRow(
    exercise: StrengthExercise,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = exercise.nameKo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${exercise.nameEn} · ${exercise.group}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ChoiceGrid(
    title: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        options.chunked(2).forEach { rowOptions ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowOptions.forEach { option ->
                    if (option == selected) {
                        Button(
                            onClick = { onSelected(option) },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(option)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onSelected(option) },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(option)
                        }
                    }
                }
                repeat(2 - rowOptions.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { next ->
            if (next.all { it.isDigit() || it == '.' }) onValueChange(next)
        },
        modifier = modifier,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}

@Composable
private fun StrengthPlanEntryCard(
    entry: StrengthPlanEntry,
    onEntryChange: (StrengthPlanEntry) -> Unit,
    onDelete: (() -> Unit)? = null,
    showCompletion: Boolean = true,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${entry.targetSets}세트 x ${entry.targetReps}회 · 휴식 ${entry.restSeconds}초",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = "삭제")
                    }
                }
            }
            entry.records.forEachIndexed { index, record ->
                StrengthSetRecordRow(
                    index = index,
                    record = record,
                    isUnilateral = entry.isUnilateral(),
                    weightUnit = entry.weightInputUnitLabel(),
                    showCompletion = showCompletion,
                    onRecordChange = { next ->
                        onEntryChange(entry.withPropagatedRecordChange(index, next))
                    }
                )
            }
        }
    }
}

@Composable
private fun StrengthSetRecordRow(
    index: Int,
    record: StrengthSetRecord,
    modifier: Modifier = Modifier,
    isUnilateral: Boolean = false,
    weightUnit: String = "kg",
    showCompletion: Boolean = true,
    onDelete: (() -> Unit)? = null,
    onRecordChange: (StrengthSetRecord) -> Unit,
) {
    val rowBackground = when {
        record.completed -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentAlpha = if (record.completed) 0.48f else 1f
    val swipeEnabled = onDelete != null && !record.completed

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PendingSwipeDeleteContainer(
            key = record.id,
            enabled = swipeEnabled,
            isPendingDelete = false,
            onDeleteRequested = { onDelete?.invoke() },
            onCommitDelete = {
                onDelete?.invoke()
            }
        ) { swipeModifier, pendingDelete ->
            val effectiveContentAlpha = if (pendingDelete) 0.58f else contentAlpha
            Column(
                modifier = swipeModifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (pendingDelete) MaterialTheme.colorScheme.surfaceVariant else rowBackground)
                    .padding(start = 14.dp, top = 10.dp, end = 14.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(if (isUnilateral) 8.dp else 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}세트",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .width(48.dp)
                            .alpha(effectiveContentAlpha)
                    )
                    SetMetricField(
                        value = record.weightKg,
                        onValueChange = { onRecordChange(record.copy(weightKg = it)) },
                        unit = weightUnit,
                        modifier = Modifier
                            .weight(1f)
                            .alpha(effectiveContentAlpha)
                    )
                    Text(
                        text = "/",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.alpha(effectiveContentAlpha)
                    )
                    SetMetricField(
                        value = record.reps,
                        onValueChange = { onRecordChange(record.copy(reps = it)) },
                        prefix = if (isUnilateral) "각" else null,
                        unit = "회",
                        modifier = Modifier
                            .weight(1f)
                            .alpha(effectiveContentAlpha)
                    )
                    SetMetricField(
                        value = record.restSeconds,
                        onValueChange = { onRecordChange(record.copy(restSeconds = it)) },
                        unit = "초",
                        modifier = Modifier
                            .weight(1f)
                            .alpha(effectiveContentAlpha)
                    )
                    if (record.completed) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "완료된 세트",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
        if (showCompletion) {
            OutlinedButton(
                onClick = { onRecordChange(record.copy(completed = !record.completed)) },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (record.completed) "완료됨" else "완료 체크")
            }
        }
    }
}

@Composable
private fun UnilateralSetSideRow(
    label: String,
    weightKg: String,
    reps: String,
    contentAlpha: Float,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .width(28.dp)
                .alpha(contentAlpha)
        )
        SetMetricField(
            value = weightKg,
            onValueChange = onWeightChange,
            unit = "kg",
            modifier = Modifier
                .weight(1f)
                .alpha(contentAlpha)
        )
        Text(
            text = "/",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.alpha(contentAlpha)
        )
        SetMetricField(
            value = reps,
            onValueChange = onRepsChange,
            unit = "회",
            modifier = Modifier
                .weight(1f)
                .alpha(contentAlpha)
        )
    }
}

@Composable
private fun SetMetricField(
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    onValueChange: (String) -> Unit,
) {
    var fieldValue by remember(value) {
        mutableStateOf(TextFieldValue(value, selection = TextRange(value.length)))
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        prefix?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
        BasicTextField(
            value = fieldValue,
            onValueChange = { next ->
                if (next.text.all { it.isDigit() || it == '.' }) {
                    fieldValue = next.copy(selection = TextRange(next.text.length))
                    onValueChange(next.text)
                }
            },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            decorationBox = { innerTextField ->
                if (value.isBlank()) {
                    Text(
                        text = "-",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.Bold
                    )
                }
                innerTextField()
            }
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutPlanScreen(
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

@Composable
private fun HeartRateDevicePickerDialog(
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

private enum class RunningWorkoutPhase {
    WARMUP,
    BLOCK,
    FINISHED
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun RunningWorkoutSessionScreen(
    apiKey: String,
    planName: String,
    blocks: List<PlanBlock>,
    totalSeconds: Int,
    isHeartRateConnected: Boolean,
    heartRateBpm: Int?,
    heartRateSamples: List<HeartRateSample>,
    onHeartRateClick: () -> Unit,
    onBack: () -> Unit,
    onWorkoutFinished: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    val repository = remember(apiKey) { IntervalsRepository(apiKey) }
    var phase by rememberSaveable(planName) { mutableStateOf(RunningWorkoutPhase.WARMUP) }
    var currentBlockIndex by rememberSaveable(planName) { mutableIntStateOf(0) }
    var warmupStartedAtMillis by rememberSaveable(planName) { mutableStateOf(System.currentTimeMillis()) }
    var blockEndAtMillis by rememberSaveable(planName) { mutableStateOf(0L) }
    var blockStartedAtMillis by rememberSaveable(planName) { mutableStateOf(0L) }
    var actualBlocks by remember { mutableStateOf(emptyList<PlanBlock>()) }
    var finishedAtMillis by rememberSaveable(planName) { mutableStateOf(0L) }
    var showFinishDialog by rememberSaveable(planName) { mutableStateOf(false) }
    var showStopSaveDialog by rememberSaveable(planName) { mutableStateOf(false) }
    var isUploadingRunningWorkout by remember { mutableStateOf(false) }
    var finishError by remember { mutableStateOf<String?>(null) }
    var localRunningWorkoutId by rememberSaveable(planName) { mutableStateOf<String?>(null) }
    var handledOverlayActionRequest by remember { mutableIntStateOf(RunningOverlayRequests.actionRequest) }
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var blinkOn by remember { mutableStateOf(false) }
    val currentBlock = blocks.getOrNull(currentBlockIndex)
    val isLastBlock = currentBlockIndex == blocks.lastIndex
    val warmupElapsedSeconds = if (phase == RunningWorkoutPhase.WARMUP) {
        ((nowMillis - warmupStartedAtMillis) / 1000L).toInt().coerceAtLeast(0)
    } else {
        0
    }
    val blockRemainingSeconds = if (phase == RunningWorkoutPhase.BLOCK && blockEndAtMillis > 0L) {
        (((blockEndAtMillis - nowMillis).coerceAtLeast(0L) + 999L) / 1000L).toInt()
    } else {
        0
    }
    val blockElapsedSeconds = currentBlock?.let { block ->
        if (phase == RunningWorkoutPhase.BLOCK) {
            (block.durationSeconds - blockRemainingSeconds).coerceIn(0, block.durationSeconds)
        } else {
            0
        }
    } ?: 0
    val progressSeconds = when (phase) {
        RunningWorkoutPhase.WARMUP -> null
        RunningWorkoutPhase.BLOCK -> currentBlock?.let { it.startSecond + blockElapsedSeconds }
        RunningWorkoutPhase.FINISHED -> totalSeconds
    }
    val isUrgent = phase == RunningWorkoutPhase.BLOCK && blockRemainingSeconds in 1..5

    LaunchedEffect(Unit) {
        requestOverlayPermissionIfNeeded(context)
    }

    LaunchedEffect(phase, warmupStartedAtMillis) {
        while (phase == RunningWorkoutPhase.WARMUP) {
            nowMillis = System.currentTimeMillis()
            delay(1_000L)
        }
    }

    fun runningSessionForFinish(
        endedAtMillis: Long,
        actualBlocksForSession: List<PlanBlock> = actualBlocks,
    ): RunningWorkoutSession {
        val blockSeconds = blocks.sumOf { it.durationSeconds }
        return RunningWorkoutSession(
            name = planName,
            startedAt = warmupStartedAtMillis.toLocalDateTime(),
            endedAt = endedAtMillis.toLocalDateTime(),
            warmupSeconds = ((endedAtMillis - warmupStartedAtMillis) / 1000L).toInt()
                .coerceAtLeast(0)
                .let { elapsed -> (elapsed - blockSeconds).coerceAtLeast(0) },
            blocks = blocks,
            actualBlocks = actualBlocksForSession.toActualTimeline()
        )
    }

    fun recordCurrentBlock(endMillis: Long = System.currentTimeMillis()): List<PlanBlock> {
        val block = currentBlock ?: return actualBlocks
        if (blockStartedAtMillis <= 0L) return actualBlocks
        val maxSeconds = block.durationSeconds.coerceAtLeast(0)
        val actualSeconds = (((endMillis - blockStartedAtMillis).coerceAtLeast(0L) + 999L) / 1000L)
            .toInt()
            .coerceIn(0, maxSeconds)
            .let { seconds ->
                if (maxSeconds > 0) seconds.coerceAtLeast(1) else 0
            }
        val nextActualBlocks = actualBlocks + block.copy(durationSeconds = actualSeconds)
        actualBlocks = nextActualBlocks
        blockStartedAtMillis = 0L
        return nextActualBlocks
    }

    fun finishWorkout() {
        if (showFinishDialog) return
        val endedAtMillis = System.currentTimeMillis()
        val actualBlocksForSession = if (phase == RunningWorkoutPhase.BLOCK) {
            recordCurrentBlock(endedAtMillis)
        } else {
            actualBlocks
        }
        val session = runningSessionForFinish(endedAtMillis, actualBlocksForSession)
        val localWorkout = session.toCompletedRunningWorkout(uploadedToIntervals = false)
        appendRunningWorkoutHistory(prefs, localWorkout)
        localRunningWorkoutId = localWorkout.id
        phase = RunningWorkoutPhase.FINISHED
        finishedAtMillis = endedAtMillis
        blockEndAtMillis = 0L
        finishError = null
        showFinishDialog = true
        stopRunningOverlay(context)
    }

    fun startBlock(index: Int) {
        val block = blocks.getOrNull(index)
        if (block == null) {
            finishWorkout()
            return
        }
        currentBlockIndex = index
        nowMillis = System.currentTimeMillis()
        blockStartedAtMillis = nowMillis
        blockEndAtMillis = nowMillis + block.durationSeconds.coerceAtLeast(0) * 1000L
        phase = RunningWorkoutPhase.BLOCK
    }

    fun moveToNextBlock() {
        recordCurrentBlock()
        val nextIndex = currentBlockIndex + 1
        if (nextIndex < blocks.size) {
            startBlock(nextIndex)
        } else {
            finishWorkout()
        }
    }

    fun moveToPreviousBlock() {
        if (phase != RunningWorkoutPhase.BLOCK || currentBlockIndex <= 0) return
        actualBlocks = actualBlocks.dropLast(1)
        blockStartedAtMillis = 0L
        startBlock(currentBlockIndex - 1)
    }

    fun handlePrimaryAction() {
        when (phase) {
            RunningWorkoutPhase.WARMUP -> startBlock(0)
            RunningWorkoutPhase.BLOCK -> moveToNextBlock()
            RunningWorkoutPhase.FINISHED -> onBack()
        }
    }

    fun stopWorkoutWithoutSaving() {
        showStopSaveDialog = false
        stopRunningOverlay(context)
        onWorkoutFinished()
    }

    fun requestWorkoutExit() {
        if (phase == RunningWorkoutPhase.FINISHED) {
            onWorkoutFinished()
        } else {
            showStopSaveDialog = true
        }
    }

    BackHandler(enabled = !showStopSaveDialog && !showFinishDialog) {
        requestWorkoutExit()
    }

    LaunchedEffect(phase, blockEndAtMillis, currentBlockIndex) {
        while (phase == RunningWorkoutPhase.BLOCK && blockEndAtMillis > 0L) {
            nowMillis = System.currentTimeMillis()
            if (nowMillis >= blockEndAtMillis) {
                moveToNextBlock()
                break
            }
            delay(250L)
        }
    }

    LaunchedEffect(isUrgent) {
        if (!isUrgent) {
            blinkOn = false
            return@LaunchedEffect
        }
        while (true) {
            blinkOn = !blinkOn
            delay(350L)
        }
    }

    val showRunningOverlayIfNeeded by rememberUpdatedState(
        newValue = {
            when (phase) {
                RunningWorkoutPhase.WARMUP -> startRunningOverlay(
                    context = context,
                    title = "Warmup",
                    endAtMillis = 0L,
                    startAtMillis = warmupStartedAtMillis,
                    actionLabel = "Warmup skip",
                    heartRateBpm = heartRateBpm
                )
                RunningWorkoutPhase.BLOCK -> startRunningOverlay(
                    context = context,
                    title = currentBlock?.title ?: "Block ${currentBlockIndex + 1}",
                    endAtMillis = blockEndAtMillis,
                    actionLabel = "Block skip",
                    targetSpeed = currentBlock?.runningTargetSpeedText().orEmpty(),
                    targetIncline = currentBlock?.runningInclineText().orEmpty(),
                    heartRateBpm = heartRateBpm
                )
                RunningWorkoutPhase.FINISHED -> stopRunningOverlay(context)
            }
        }
    )

    DisposableEffect(context) {
        val lifecycle = (context as? LifecycleOwner)?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> showRunningOverlayIfNeeded()
                Lifecycle.Event.ON_RESUME -> stopRunningOverlay(context)
                else -> Unit
            }
        }
        lifecycle?.addObserver(observer)
        if (lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == false) {
            showRunningOverlayIfNeeded()
        }
        onDispose {
            lifecycle?.removeObserver(observer)
            if (lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true) {
                stopRunningOverlay(context)
            }
        }
    }

    LaunchedEffect(phase, currentBlockIndex, blockEndAtMillis, heartRateBpm, warmupStartedAtMillis) {
        val lifecycle = (context as? LifecycleOwner)?.lifecycle
        if (lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == false) {
            showRunningOverlayIfNeeded()
        }
    }

    LaunchedEffect(RunningOverlayRequests.actionRequest) {
        if (RunningOverlayRequests.actionRequest > handledOverlayActionRequest) {
            handledOverlayActionRequest = RunningOverlayRequests.actionRequest
            handlePrimaryAction()
            stopRunningOverlay(context)
        }
    }

    fun uploadRunningWorkoutAndFinish() {
        if (apiKey.isBlank()) {
            finishError = "Intervals.icu 업로드는 로그인 후 사용할 수 있습니다."
            return
        }
        val endedAt = finishedAtMillis.takeIf { it > 0L } ?: System.currentTimeMillis()
        val session = runningSessionForFinish(endedAt)
        scope.launch {
            isUploadingRunningWorkout = true
            finishError = null
            try {
                repository.uploadRunningWorkout(session)
                replaceRunningWorkoutHistory(
                    prefs = prefs,
                    workout = session.toCompletedRunningWorkout(uploadedToIntervals = true)
                )
                localRunningWorkoutId = session.toCompletedRunningWorkout(uploadedToIntervals = true).id
                onWorkoutFinished()
            } catch (error: Exception) {
                finishError = error.message ?: "업로드하지 못했습니다."
            } finally {
                isUploadingRunningWorkout = false
            }
        }
    }

    if (showStopSaveDialog) {
        AlertDialog(
            onDismissRequest = { showStopSaveDialog = false },
            title = { Text("운동 중지") },
            text = {
                Text("현재까지 수행한 러닝 기록을 로컬에 저장할까요?")
            },
            confirmButton = {
                TextButton(onClick = {
                    showStopSaveDialog = false
                    finishWorkout()
                }) {
                    Text("저장")
                }
            },
            dismissButton = {
                TextButton(onClick = ::stopWorkoutWithoutSaving) {
                    Text("삭제")
                }
            }
        )
    }

    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("러닝 기록 업로드") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Garmin 원본 기록이 더 중요하면 업로드하지 않고 Garmin 동기화를 기다리는 편이 안전합니다. 지금 업로드하면 Intervals.icu에 수동 러닝 기록이 추가될 수 있습니다."
                    )
                    Text(
                        text = "앱 로컬에는 수행 결과를 저장했습니다.",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    finishError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = ::uploadRunningWorkoutAndFinish,
                    enabled = apiKey.isNotBlank() && !isUploadingRunningWorkout
                ) {
                    Text(if (isUploadingRunningWorkout) "업로드 중" else "수동 업로드")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onWorkoutFinished,
                    enabled = !isUploadingRunningWorkout
                ) {
                    Text("Garmin 결과 사용")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = planName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        requestWorkoutExit()
                    }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    if (phase != RunningWorkoutPhase.FINISHED) {
                        TextButton(
                            onClick = { showStopSaveDialog = true },
                            enabled = !showFinishDialog && !showStopSaveDialog
                        ) {
                            Text(
                                text = "Stop",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            val gap = 12.dp
            val planGraphHeight = 128.dp
            val heartGraphCanvasHeight = 54.dp
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(gap)
            ) {
                PlanWorkoutGraphCanvas(
                    blocks = blocks,
                    totalSeconds = totalSeconds,
                    sportType = TrainingSportType.RUNNING,
                    height = planGraphHeight,
                    progressSeconds = progressSeconds
                )
                HeartRateGraph(
                    samples = heartRateSamples,
                    isHeartRateConnected = isHeartRateConnected,
                    heartRateBpm = heartRateBpm,
                    onHeartRateClick = onHeartRateClick,
                    modifier = Modifier.fillMaxWidth(),
                    graphHeight = heartGraphCanvasHeight
                )
                when (phase) {
                    RunningWorkoutPhase.WARMUP -> RunningWarmupPanel(
                        elapsedSeconds = warmupElapsedSeconds,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    RunningWorkoutPhase.BLOCK -> RunningBlockPanel(
                        block = currentBlock,
                        blockIndex = currentBlockIndex,
                        blockCount = blocks.size,
                        remainingSeconds = blockRemainingSeconds,
                        blinkOn = blinkOn,
                        isLastBlock = isLastBlock,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    RunningWorkoutPhase.FINISHED -> RunningFinishedPanel(
                        totalSeconds = totalSeconds,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        onClose = onBack
                    )
                }
                if (phase != RunningWorkoutPhase.FINISHED) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (phase == RunningWorkoutPhase.BLOCK) {
                            OutlinedButton(
                                onClick = ::moveToPreviousBlock,
                                enabled = currentBlockIndex > 0,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Text(
                                    text = "이전\nBlock",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 2
                                )
                            }
                        }
                        Button(
                            onClick = when (phase) {
                                RunningWorkoutPhase.WARMUP -> ({ startBlock(0) })
                                RunningWorkoutPhase.BLOCK -> ::moveToNextBlock
                                RunningWorkoutPhase.FINISHED -> ({})
                            },
                            modifier = Modifier
                                .weight(if (phase == RunningWorkoutPhase.BLOCK) 2f else 1f)
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (phase == RunningWorkoutPhase.BLOCK) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        ) {
                            Text(
                                when (phase) {
                                    RunningWorkoutPhase.WARMUP -> "Warmup 종료"
                                    RunningWorkoutPhase.BLOCK -> if (isLastBlock) "운동 마치기" else "Block 건너뛰기"
                                    RunningWorkoutPhase.FINISHED -> ""
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
private fun RunningWarmupPanel(
    elapsedSeconds: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Warmup 중",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            RunningTimerText(
                text = formatClock(elapsedSeconds),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f),
                fontHeightRatio = 0.40f,
                maxFontSize = 102f
            )
            Text(
                text = "준비가 끝나면 첫 번째 Block을 시작하세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun RunningBlockPanel(
    block: PlanBlock?,
    blockIndex: Int,
    blockCount: Int,
    remainingSeconds: Int,
    blinkOn: Boolean,
    isLastBlock: Boolean,
    modifier: Modifier = Modifier,
) {
    val speedText = block?.runningTargetSpeedText().orEmpty().ifBlank { "-" }
    val inclineText = block?.runningInclineText().orEmpty().ifBlank { "-" }
    val blockDurationText = formatClock(block?.durationSeconds ?: 0)
    val blockTitle = block?.title
        ?.replace("Workout", "", ignoreCase = true)
        ?.trim()
        .orEmpty()
    val timerColor = if (remainingSeconds in 1..5 && blinkOn) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = listOf(
                    "Block ${blockIndex + 1} / $blockCount",
                    blockDurationText,
                    blockTitle
                ).filter { it.isNotBlank() }.joinToString(" · "),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "속도 : $speedText    경사도 : $inclineText",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            RunningTimerText(
                text = formatClock(remainingSeconds),
                color = timerColor,
                modifier = Modifier.weight(1f),
                fontHeightRatio = 0.58f,
                maxFontSize = 138f
            )
        }
    }
}

@Composable
private fun RunningTimerText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    fontHeightRatio: Float,
    maxFontSize: Float,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val heightBasedFontSize = with(density) {
            (maxHeight.toPx() * fontHeightRatio).toSp().value
        }
        val widthBasedFontSize = with(density) {
            (maxWidth.toPx() / (text.length.coerceAtLeast(1) * 0.58f)).toSp().value
        }
        val fontSizeValue = minOf(heightBasedFontSize, widthBasedFontSize)
            .coerceIn(48f, maxFontSize)
        Text(
            text = text,
            fontSize = fontSizeValue.sp,
            lineHeight = fontSizeValue.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HeartRateGraph(
    samples: List<HeartRateSample>,
    isHeartRateConnected: Boolean,
    heartRateBpm: Int?,
    onHeartRateClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    graphHeight: Dp = 64.dp,
) {
    val now = remember(samples) {
        maxOf(System.currentTimeMillis(), samples.lastOrNull()?.timestampMillis ?: 0L)
    }
    val windowStartMillis = now - HEART_RATE_GRAPH_WINDOW_MILLIS
    val visibleSamples = remember(samples, now) {
        samples.filter { it.timestampMillis >= windowStartMillis }
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(contentColor.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "심박 그래프",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "최근 5분",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.72f)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(graphHeight),
            contentAlignment = Alignment.Center
        ) {
            if (visibleSamples.isNotEmpty()) {
            val minBpm = visibleSamples.minOf { it.bpm }.let { (it - 5).coerceAtLeast(40) }
            val maxBpm = visibleSamples.maxOf { it.bpm }.let { (it + 5).coerceAtLeast(minBpm + 10) }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(graphHeight)
            ) {
                val gridColor = contentColor.copy(alpha = 0.18f)
                val lineColor = Color(0xFFEF4444)
                val textColor = contentColor.copy(alpha = 0.62f).toArgb()
                repeat(3) { index ->
                    val y = size.height * index / 2f
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                val points = visibleSamples.map { sample ->
                    val xRatio = ((sample.timestampMillis - windowStartMillis).toFloat() / HEART_RATE_GRAPH_WINDOW_MILLIS.toFloat())
                        .coerceIn(0f, 1f)
                    val yRatio = ((sample.bpm - minBpm).toFloat() / (maxBpm - minBpm).toFloat())
                        .coerceIn(0f, 1f)
                    Offset(
                        x = size.width * xRatio,
                        y = size.height - size.height * yRatio
                    )
                }
                points.zipWithNext().forEach { (start, end) ->
                    drawLine(
                        color = lineColor,
                        start = start,
                        end = end,
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                points.lastOrNull()?.let { point ->
                    drawCircle(
                        color = lineColor,
                        radius = 3.dp.toPx(),
                        center = point
                    )
                }
                val labelPaint = Paint().apply {
                    textSize = 10.dp.toPx()
                    color = textColor
                    textAlign = Paint.Align.LEFT
                    isAntiAlias = true
                }
                drawContext.canvas.nativeCanvas.apply {
                    drawText("${maxBpm}bpm", 0f, 10.dp.toPx(), labelPaint)
                    drawText("${minBpm}bpm", 0f, size.height - 2.dp.toPx(), labelPaint)
                }
            }
            }
            if (isHeartRateConnected) {
                Text(
                    text = heartRateBpm?.let { "$it bpm" } ?: "-- bpm",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444),
                    textAlign = TextAlign.Center
                )
            } else {
                OutlinedButton(
                    onClick = onHeartRateClick,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "심박계 연결",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun RunningBlockMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RunningFinishedPanel(
    totalSeconds: Int,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Running Workout 완료",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "총 ${formatDuration(totalSeconds)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("닫기")
            }
        }
    }
}

@Composable
private fun PlanWorkoutGraph(
    blocks: List<PlanBlock>,
    totalSeconds: Int,
    modifier: Modifier = Modifier,
    title: String = "그래프",
    sportType: TrainingSportType = TrainingSportType.OTHER,
) {
    DetailSection(title = title) {
        PlanWorkoutGraphCanvas(
            blocks = blocks,
            totalSeconds = totalSeconds,
            modifier = modifier,
            sportType = sportType,
            height = 190.dp
        )
    }
}

@Composable
private fun LocalRunningWorkoutGraphSection(
    blocks: List<PlanBlock>,
    totalSeconds: Int,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "로컬 러닝 기록 그래프",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "로컬 기록 삭제",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            PlanWorkoutGraphCanvas(
                blocks = blocks,
                totalSeconds = totalSeconds,
                sportType = TrainingSportType.RUNNING,
                height = 190.dp
            )
        }
    }
}

@Composable
private fun PlanWorkoutGraphCanvas(
    blocks: List<PlanBlock>,
    totalSeconds: Int,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp,
    sportType: TrainingSportType = TrainingSportType.OTHER,
    progressSeconds: Int? = null,
) {
    val graphBlocks = remember(blocks, sportType) { blocks.toWorkoutGraphBlocks(sportType) }
    val unit = when {
        graphBlocks.any { it.unit == WorkoutGraphUnit.Watts && it.value > 0f } -> WorkoutGraphUnit.Watts
        graphBlocks.any { it.unit == WorkoutGraphUnit.SpeedKmh && it.value > 0f } -> WorkoutGraphUnit.SpeedKmh
        graphBlocks.any { it.unit == WorkoutGraphUnit.Percent && it.value > 0f } -> WorkoutGraphUnit.Percent
        else -> WorkoutGraphUnit.Percent
    }
    val values = graphBlocks
        .filter { it.unit == unit }
        .map { it.value }
    val yMax = values.maxOrNull()?.takeIf { it > 0f } ?: 1f
    val graphTotalSeconds = (totalSeconds.takeIf { it > 0 } ?: blocks.sumOf { it.durationSeconds }).coerceAtLeast(1)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val axisColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val lineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
    val thresholdColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val progressColor = MaterialTheme.colorScheme.error
    val activeBlockColor = Color(0xFFFFC857)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceColor)
    ) {
        val compact = size.height < 150.dp.toPx()
        val left = when {
            unit == WorkoutGraphUnit.SpeedKmh && compact -> 52.dp.toPx()
            unit == WorkoutGraphUnit.SpeedKmh -> 58.dp.toPx()
            compact -> 34.dp.toPx()
            else -> 42.dp.toPx()
        }
        val right = 10.dp.toPx()
        val top = if (compact) 10.dp.toPx() else 14.dp.toPx()
        val bottom = if (compact) 24.dp.toPx() else 30.dp.toPx()
        val chartWidth = (size.width - left - right).coerceAtLeast(1f)
        val chartHeight = (size.height - top - bottom).coerceAtLeast(1f)
        val bottomY = top + chartHeight
        val textSize = (if (compact) 10f else 12f) * density
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = labelColor.toArgb()
            this.textSize = textSize
        }

        fun xFor(seconds: Int): Float {
            return left + (seconds.coerceIn(0, graphTotalSeconds).toFloat() / graphTotalSeconds.toFloat()) * chartWidth
        }

        fun yFor(value: Float): Float {
            val ratio = (value / yMax).coerceIn(0f, 1f)
            return bottomY - chartHeight * ratio
        }

        val activeGraphBlock = progressSeconds?.let { progress ->
            graphBlocks.firstOrNull { graphBlock ->
                progress >= graphBlock.block.startSecond && progress < graphBlock.block.endSecond
            } ?: graphBlocks.lastOrNull { progress >= it.block.endSecond }
        }

        drawLine(axisColor, Offset(left, top), Offset(left, bottomY), strokeWidth = 1.dp.toPx())
        drawLine(axisColor, Offset(left, bottomY), Offset(left + chartWidth, bottomY), strokeWidth = 1.dp.toPx())

        val midValue = yMax / 2f
        listOf(0f, midValue, yMax).forEach { value ->
            val y = yFor(value)
            drawLine(axisColor.copy(alpha = 0.28f), Offset(left, y), Offset(left + chartWidth, y), strokeWidth = 1.dp.toPx())
            labelPaint.textAlign = Paint.Align.RIGHT
            val labelX = left - 7.dp.toPx()
            val labels = value.formatGraphAxisLabels(unit)
            if (labels.size == 1) {
                drawContext.canvas.nativeCanvas.drawText(
                    labels.first(),
                    labelX,
                    y + textSize / 3f,
                    labelPaint
                )
            } else {
                drawContext.canvas.nativeCanvas.drawText(
                    labels[0],
                    labelX,
                    y + textSize * 0.05f,
                    labelPaint
                )
                drawContext.canvas.nativeCanvas.drawText(
                    labels[1],
                    labelX,
                    y + textSize * 1.15f,
                    labelPaint
                )
            }
        }

        val threshold = when (unit) {
            WorkoutGraphUnit.Watts -> {
                if (sportType == TrainingSportType.CYCLING) {
                    graphBlocks.firstNotNullOfOrNull { graphBlock ->
                        val percent = graphBlock.intensityPercent?.takeIf { it > 0f } ?: return@firstNotNullOfOrNull null
                        graphBlock.value / (percent / 100f)
                    }
                } else {
                    values.maxOrNull()?.let { it * 0.9f }
                }
            }
            WorkoutGraphUnit.Percent -> 100f
            WorkoutGraphUnit.SpeedKmh -> null
        }?.takeIf { it > 0f && it < yMax }
        threshold?.let {
            val y = yFor(it)
            drawLine(
                color = thresholdColor,
                start = Offset(left, y),
                end = Offset(left + chartWidth, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()))
            )
        }

        activeGraphBlock?.block?.let { block ->
            val x = xFor(block.startSecond)
            val width = (xFor(block.endSecond) - x).coerceAtLeast(1.5.dp.toPx())
            drawRect(
                color = activeBlockColor.copy(alpha = 0.18f),
                topLeft = Offset(x, top),
                size = Size(width, chartHeight)
            )
        }

        graphBlocks.forEach { graphBlock ->
            val block = graphBlock.block
            val value = if (graphBlock.unit == unit) graphBlock.value else 0f
            val x = xFor(block.startSecond)
            val width = (xFor(block.endSecond) - x).coerceAtLeast(1.5.dp.toPx())
            val barHeight = if (value > 0f) (bottomY - yFor(value)).coerceAtLeast(4.dp.toPx()) else 4.dp.toPx()
            val y = bottomY - barHeight
            val color = if (graphBlock.block.index == activeGraphBlock?.block?.index) {
                activeBlockColor
            } else {
                graphBlock.graphColor(yMax, unit, sportType)
            }
            drawRect(
                color = color.copy(alpha = 0.72f),
                topLeft = Offset(x, y),
                size = Size(width, barHeight)
            )
            drawRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(width, barHeight),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        val stepPath = Path()
        var hasStepPoint = false
        graphBlocks.forEach { graphBlock ->
            val block = graphBlock.block
            val value = if (graphBlock.unit == unit) graphBlock.value else 0f
            val y = yFor(value)
            val xStart = xFor(block.startSecond)
            val xEnd = xFor(block.endSecond)
            if (!hasStepPoint) {
                stepPath.moveTo(xStart, y)
                hasStepPoint = true
            } else {
                stepPath.lineTo(xStart, y)
            }
            stepPath.lineTo(xEnd, y)
        }
        if (hasStepPoint) {
            drawPath(
                path = stepPath,
                color = lineColor,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        progressSeconds?.let { progress ->
            val x = xFor(progress)
            drawLine(
                color = progressColor,
                start = Offset(x, top),
                end = Offset(x, bottomY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        val tickSeconds = listOf(
            0,
            graphTotalSeconds / 2,
            graphTotalSeconds
        ).distinct()
        labelPaint.textAlign = Paint.Align.CENTER
        tickSeconds.forEach { seconds ->
            val x = xFor(seconds)
            drawLine(axisColor.copy(alpha = 0.3f), Offset(x, bottomY), Offset(x, bottomY + 4.dp.toPx()), strokeWidth = 1.dp.toPx())
            drawContext.canvas.nativeCanvas.drawText(
                formatGraphTime(seconds),
                x,
                bottomY + (if (compact) 17.dp.toPx() else 21.dp.toPx()),
                labelPaint
            )
        }
    }
}

@Composable
private fun TrainingItemDetailCard(
    item: TrainingItem,
    totalSeconds: Int,
    isStrengthPlan: Boolean,
    strengthWorkout: CompletedStrengthWorkout?,
    uploadMessage: String?,
    uploadError: String?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isStrengthPlan) Icons.Outlined.FitnessCenter else if (item.isPlan) Icons.Outlined.Schedule else Icons.Outlined.Route,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                TrainingTypeLabel(isPlan = item.isPlan, resultLabel = "Summary")
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricChip(icon = Icons.Outlined.Today, text = item.date.format(DateTimeFormatter.ofPattern("M/d")) + " " + item.timeLabel)
                if (totalSeconds > 0) {
                    MetricChip(icon = Icons.Outlined.Schedule, text = formatDuration(totalSeconds))
                }
                item.load?.let { MetricChip(icon = Icons.Outlined.Speed, text = "Load $it") }
                item.weightLiftedKg?.takeIf { it > 0.0 }?.let {
                    MetricChip(icon = Icons.Outlined.FitnessCenter, text = "Weight ${formatWeight(it)} kg")
                }
            }
            strengthWorkout?.let { workout ->
                StrengthWorkoutSummary(
                    workout = workout,
                    uploadMessage = uploadMessage,
                    uploadError = uploadError
                )
            }
            if (isStrengthPlan) {
                Text(
                    text = "IntervalsGym 웨이트 Plan",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StrengthWorkoutSummary(
    workout: CompletedStrengthWorkout,
    uploadMessage: String?,
    uploadError: String?,
) {
    val totalRestSeconds = workout.restEvents.sumOf { it.actualSeconds }
    val volume = workout.entries.totalVolumeKg()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = if (workout.uploadedToIntervals) "로컬 기록 · Intervals.icu 업로드됨" else "로컬 기록 · Intervals.icu 미동기화",
            style = MaterialTheme.typography.labelLarge,
            color = if (workout.uploadedToIntervals) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${workout.setEvents.size}세트 · RPE ${workout.rpe} · Load ${workout.trainingLoad} · 볼륨 ${formatWeight(volume)} kg · 운동 시간 ${formatDuration(workout.durationSeconds)} · 실제 휴식 ${formatClock(totalRestSeconds)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        uploadMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
        uploadError?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun LocalStrengthWorkoutDetailSection(
    workout: CompletedStrengthWorkout,
) {
    DetailSection(title = "웨이트 상세 기록") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            workout.entries.forEachIndexed { entryIndex, entry ->
                if (entryIndex > 0) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
                StrengthWorkoutExerciseDetail(
                    workout = workout,
                    entry = entry
                )
            }
        }
    }
}

@Composable
private fun StrengthWorkoutExerciseDetail(
    workout: CompletedStrengthWorkout,
    entry: StrengthPlanEntry,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = entry.title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        entry.records.forEachIndexed { index, record ->
            StrengthWorkoutSetDetailRow(
                workout = workout,
                entry = entry,
                record = record,
                setIndex = index
            )
        }
    }
}

@Composable
private fun StrengthWorkoutSetDetailRow(
    workout: CompletedStrengthWorkout,
    entry: StrengthPlanEntry,
    record: StrengthSetRecord,
    setIndex: Int,
) {
    val completedEvent = workout.setEvents.firstOrNull {
        it.exerciseEntryId == entry.id && it.setRecordId == record.id
    }
    val restEvent = completedEvent?.let { event ->
        workout.restEvents.firstOrNull { it.afterSetSequence == event.sequence }
    }
    val rawWeight = completedEvent?.weightKg
        ?: record.weightKg.ifBlank { entry.targetWeightKg.ifBlank { "-" } }
    val rawReps = completedEvent?.reps ?: record.reps.ifBlank { "-" }
    val plannedRest = completedEvent?.targetRestSeconds
        ?: record.restSeconds.toIntOrNull()
        ?: entry.restSeconds
    val isCompleted = completedEvent != null || record.completed
    val weightText = displayWeightText(rawWeight)
    val repsText = if (entry.isUnilateral()) {
        "각 ${displayUnilateralRepsText(rawReps)}"
    } else {
        displayRepsText(rawReps)
    }
    val actualRestText = restEvent?.let { " · 실제 ${formatClock(it.actualSeconds)}" }.orEmpty()
    val detailText = "$weightText x $repsText · 휴식 ${plannedRest}초$actualRestText"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Set ${setIndex + 1}",
            style = MaterialTheme.typography.labelLarge,
            color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(52.dp)
        )
        Text(
            text = detailText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (isCompleted) "완료" else "미완료",
            style = MaterialTheme.typography.labelMedium,
            color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun displayWeightText(raw: String): String {
    val value = raw.trim()
    if (value.isBlank() || value == "-") return "-kg"
    if (value.contains("좌") || value.contains("우")) {
        val numbers = Regex("""\d+(?:\.\d+)?""").findAll(value).map { it.value }.toList()
        val distinctNumbers = numbers.distinct()
        return when {
            distinctNumbers.size == 1 -> "${distinctNumbers.first()}kg"
            numbers.isEmpty() -> "-kg"
            else -> value
        }
    }
    return if (value.contains("kg", ignoreCase = true)) value else "${value}kg"
}

private fun displayRepsText(raw: String): String {
    val value = raw.trim()
    if (value.isBlank() || value == "-") return "-회"
    return if (value.contains("회")) value else "${value}회"
}

private fun displayUnilateralRepsText(raw: String): String {
    val value = raw.trim()
    if (value.isBlank() || value == "-") return "-회"
    if (value.contains("좌") || value.contains("우")) {
        val numbers = Regex("""\d+""").findAll(value).map { it.value }.toList()
        val distinctNumbers = numbers.distinct()
        return when {
            distinctNumbers.size == 1 -> "${distinctNumbers.first()}회"
            numbers.isEmpty() -> "-회"
            else -> displayRepsText(value)
        }
    }
    return displayRepsText(value)
}

private fun buildStrengthSetSummary(
    entry: StrengthPlanEntry,
    record: StrengthSetRecord,
): String {
    val weight = displayWeightText(record.summaryWeightText(entry))
    val reps = if (entry.isUnilateral()) {
        "각 ${displayUnilateralRepsText(record.summaryRepsText())}"
    } else {
        displayRepsText(record.summaryRepsText())
    }
    val rest = record.restSeconds.ifBlank { entry.restSeconds.takeIf { it > 0 }?.toString().orEmpty() }
        .ifBlank { "-" }
    return "$weight x $reps · 휴식 ${rest}초"
}

private fun StrengthSetRecord.summaryWeightText(entry: StrengthPlanEntry): String {
    if (weightKg.isNotBlank()) return weightKg
    val left = leftWeightKg.trim()
    val right = rightWeightKg.trim()
    return when {
        left.isNotBlank() && right.isNotBlank() && left == right -> left
        left.isNotBlank() && right.isNotBlank() -> "좌 ${left}kg / 우 ${right}kg"
        left.isNotBlank() -> left
        right.isNotBlank() -> right
        else -> entry.targetWeightKg
    }
}

private fun StrengthSetRecord.summaryRepsText(): String {
    if (reps.isNotBlank()) return reps
    val left = leftReps.trim()
    val right = rightReps.trim()
    return when {
        left.isNotBlank() && right.isNotBlank() && left == right -> left
        left.isNotBlank() && right.isNotBlank() -> "좌 ${left}회 / 우 ${right}회"
        left.isNotBlank() -> left
        else -> right
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
private fun RunningTimerPanel(
    elapsedSeconds: Int,
    totalSeconds: Int,
    currentBlock: PlanBlock?,
    blockRemaining: Int,
    remainingTotal: Int,
    isRunning: Boolean,
    onToggle: () -> Unit,
    onReset: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "수행 시간",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${formatClock(elapsedSeconds)} / ${formatClock(totalSeconds)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TimerStat(
                    title = "현재 Block",
                    value = currentBlock?.title ?: "대기",
                    detail = currentBlock?.targetText.orEmpty(),
                    modifier = Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.error
                )
                TimerStat(
                    title = "Block 남은 시간",
                    value = formatClock(blockRemaining),
                    detail = "전체 ${formatClock(remainingTotal)} 남음",
                    modifier = Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onToggle,
                    enabled = totalSeconds > 0,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isRunning) "일시정지" else "시작")
                }
                OutlinedButton(
                    onClick = onReset,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.RestartAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("리셋")
                }
            }
        }
    }
}

@Composable
private fun TimerStat(
    title: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier,
    accent: Color,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PlanTimeline(
    blocks: List<PlanBlock>,
    currentIndex: Int,
    elapsedSeconds: Int,
    totalSeconds: Int,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            blocks.forEach { block ->
                val color = when {
                    block.index == currentIndex -> MaterialTheme.colorScheme.error
                    elapsedSeconds >= block.endSecond -> MaterialTheme.colorScheme.primary
                    block.isRecovery -> Color(0xFF8AA7B0)
                    else -> Color(0xFF2F7D6D)
                }
                Box(
                    modifier = Modifier
                        .weight(block.durationSeconds.coerceAtLeast(1).toFloat())
                        .fillMaxHeight()
                        .background(color)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "진행률 ${if (totalSeconds > 0) elapsedSeconds * 100 / totalSeconds else 0}%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PlanBlockRow(
    block: PlanBlock,
    isCurrent: Boolean,
    isDone: Boolean,
) {
    val containerColor = when {
        isCurrent -> MaterialTheme.colorScheme.error
        isDone -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = if (isCurrent) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = block.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = block.targetText.ifBlank { block.kind },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrent) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = formatClock(block.durationSeconds),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun MetricChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LoadingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(12.dp))
        Text("Intervals.icu에서 가져오는 중")
    }
}

@Composable
private fun EmptyView(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(42.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onRetry, shape = RoundedCornerShape(20.dp)) {
            Icon(Icons.Outlined.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("다시 시도")
        }
    }
}

private data class WeekUiState(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val isLoading: Boolean = false,
    val activities: List<TrainingItem> = emptyList(),
    val plans: List<TrainingItem> = emptyList(),
    val error: String? = null,
)

internal data class WeekTrainingData(
    val activities: List<TrainingItem>,
    val plans: List<TrainingItem>,
)

internal data class TrainingItem(
    val id: String,
    val remoteId: String,
    val externalId: String?,
    val name: String,
    val type: String,
    val date: LocalDate,
    val startedAt: LocalDateTime?,
    val timeLabel: String,
    val durationSeconds: Int?,
    val distanceMeters: Double?,
    val weightLiftedKg: Double?,
    val load: Int?,
    val fitness: Double?,
    val fatigue: Double?,
    val form: Double?,
    val description: String?,
    val blocks: List<PlanBlock>,
    val isPlan: Boolean,
    val matchedStrengthWorkout: CompletedStrengthWorkout? = null,
    val matchedStrengthPlan: StrengthWorkoutPlan? = null,
    val isLocalOnlyStrengthResult: Boolean = false,
    val isLocalOnlyRunningResult: Boolean = false,
    val actualRunningBlocks: List<PlanBlock> = emptyList(),
    val pairedPlan: TrainingItem? = null,
)

private fun TrainingItem.isWeightTrainingItem(): Boolean {
    val searchable = listOf(type, name, description.orEmpty())
        .joinToString(" ")
        .lowercase(Locale.KOREAN)
        .replace(" ", "")
        .replace("_", "")
        .replace("-", "")
    return isLocalOnlyStrengthResult ||
        matchedStrengthWorkout != null ||
        searchable.contains("weighttraining") ||
        searchable.contains("웨이트") ||
        searchable.contains("strength")
}

private fun TrainingItem.isRunningItem(): Boolean {
    val searchable = listOf(type, name).joinToString(" ").lowercase(Locale.KOREAN).replace(" ", "")
    return searchable.contains("run") ||
        searchable.contains("running") ||
        searchable.contains("러닝") ||
        searchable.contains("런닝") ||
        searchable.contains("달리기")
}

private fun TrainingItem.isCyclingItem(): Boolean {
    val searchable = listOf(type, name).joinToString(" ").lowercase(Locale.KOREAN).replace(" ", "")
    return searchable.contains("ride") ||
        searchable.contains("bike") ||
        searchable.contains("bicycle") ||
        searchable.contains("cycling") ||
        searchable.contains("cycle") ||
        searchable.contains("자전거") ||
        searchable.contains("사이클")
}

private fun TrainingItem.sportType(): TrainingSportType {
    return when {
        isWeightTrainingItem() -> TrainingSportType.STRENGTH
        isCyclingItem() -> TrainingSportType.CYCLING
        isRunningItem() -> TrainingSportType.RUNNING
        else -> TrainingSportType.OTHER
    }
}

private fun TrainingItem.displayTimeLabel(): String? {
    val value = timeLabel.trim()
    return value.takeUnless {
        it.isBlank() ||
            it == "00:00" ||
            it == "--:--" ||
            it.equals("Plan", ignoreCase = true)
    }
}

private fun TrainingItem.plannedWorkoutDeleteConfirmMessage(): String {
    return plannedWorkoutDeleteConfirmMessage(date, name)
}

private fun TrainingItem.strengthPlanForDisplay(): StrengthWorkoutPlan? {
    if (sportType() != TrainingSportType.STRENGTH) return null
    if (!isPlan && pairedPlan == null) return null
    return matchedStrengthPlan
        ?: pairedPlan?.matchedStrengthPlan
        ?: description.toIntervalsGymStrengthPlan()
        ?: pairedPlan?.description.toIntervalsGymStrengthPlan()
}

private fun TrainingItem.workoutPlanBlocksForPreview(): List<PlanBlock> {
    val sportType = sportType()
    if (sportType != TrainingSportType.RUNNING && sportType != TrainingSportType.CYCLING) return emptyList()
    if (!isPlan && pairedPlan == null) return emptyList()
    val sourceBlocks = blocks.takeIf { it.isNotEmpty() }
        ?: pairedPlan?.blocks?.takeIf { it.isNotEmpty() }
        ?: emptyList()
    val sourceDescription = description ?: pairedPlan?.description
    return when (sportType) {
        TrainingSportType.RUNNING -> sourceBlocks.withRunningGraphContext(
            description = sourceDescription,
            name = name.ifBlank { pairedPlan?.name.orEmpty() }
        )
        TrainingSportType.CYCLING -> sourceBlocks.withCyclingGraphContext(sourceDescription)
        else -> sourceBlocks
    }
}

private fun TrainingItem.workoutPlanTotalSecondsForPreview(blocks: List<PlanBlock>): Int {
    return durationSeconds
        ?: pairedPlan?.durationSeconds
        ?: blocks.sumOf { it.durationSeconds }
}

private fun List<PlanBlock>.withRunningGraphContext(
    description: String?,
    name: String,
): List<PlanBlock> {
    val context = listOf(description.orEmpty(), name)
        .firstNotNullOfOrNull { it.runningPaceOrSpeedContext() }
        ?: return this
    val hasExplicitTargets = any { it.targetText.isNotBlank() }
    return map { block ->
        val shouldApply = if (hasExplicitTargets) {
            block.targetText.isNotBlank()
        } else {
            !block.isRecovery
        }
        if (shouldApply && !block.targetText.contains(context, ignoreCase = true)) {
            block.copy(targetText = listOf(block.targetText, context).filter { it.isNotBlank() }.joinToString(" · "))
        } else {
            block
        }
    }
}

private fun List<PlanBlock>.withCyclingGraphContext(description: String?): List<PlanBlock> {
    if (description.isNullOrBlank() || isEmpty()) return this
    val contexts = cyclingPowerContextSequence(description, size).takeIf { it.size == size } ?: return this
    return mapIndexed { index, block ->
        val context = contexts[index]
        if (context.isBlank() || block.targetText.contains(context, ignoreCase = true)) {
            block
        } else {
            block.copy(targetText = listOf(block.targetText, context).filter { it.isNotBlank() }.joinToString(" · "))
        }
    }
}

private fun String.runningPaceOrSpeedContext(): String? {
    val paceMatch = Regex("""\d{1,2}:\d{2}\s*(?:/km|pace)?""", RegexOption.IGNORE_CASE).find(this)
    val speedMatch = Regex("""\d+(?:\.\d+)?\s*km\s*/?\s*h""", RegexOption.IGNORE_CASE).find(this)
    val match = listOfNotNull(paceMatch, speedMatch).minByOrNull { it.range.first } ?: return null
    val segment = runningContextSegment(match.range.first, match.range.last + 1)
    return if (Regex("""\d+(?:\.\d+)?\s*%""").containsMatchIn(segment)) {
        segment
    } else {
        match.value.trim()
    }
}

private fun String.runningContextSegment(matchStart: Int, matchEnd: Int): String {
    val startDelimiters = listOf(
        lastIndexOf('\n', startIndex = matchStart.coerceAtLeast(0)),
        lastIndexOf(';', startIndex = matchStart.coerceAtLeast(0)),
        lastIndexOf('·', startIndex = matchStart.coerceAtLeast(0)),
        lastIndexOf('(', startIndex = matchStart.coerceAtLeast(0))
    )
    val endDelimiters = listOf(
        indexOf('\n', startIndex = matchEnd.coerceAtMost(length)),
        indexOf(';', startIndex = matchEnd.coerceAtMost(length)),
        indexOf('·', startIndex = matchEnd.coerceAtMost(length)),
        indexOf(')', startIndex = matchEnd.coerceAtMost(length))
    ).filter { it >= 0 }
    val start = ((startDelimiters.maxOrNull() ?: -1) + 1).coerceIn(0, length)
    val end = (endDelimiters.minOrNull() ?: length).coerceIn(start, length)
    return substring(start, end).trim()
}

private fun mergeTrainingPlansAndResults(
    activities: List<TrainingItem>,
    plans: List<TrainingItem>,
): List<TrainingItem> {
    if (activities.isEmpty() || plans.isEmpty()) return activities + plans
    val unusedPlans = plans.toMutableList()
    val mergedActivities = activities.map { activity ->
        val match = unusedPlans
            .withIndex()
            .filter { (_, plan) -> plan.canMergeWithResult(activity) }
            .maxByOrNull { (_, plan) -> plan.mergeScoreForResult(activity) }
            ?: return@map activity
        unusedPlans.removeAt(match.index)
        activity.copy(
            id = "merged-${match.value.id}-${activity.id}",
            matchedStrengthPlan = activity.matchedStrengthPlan ?: match.value.matchedStrengthPlan,
            pairedPlan = match.value
        )
    }
    return mergedActivities + unusedPlans
}

private fun TrainingItem.canMergeWithResult(result: TrainingItem): Boolean {
    if (!isPlan || result.isPlan) return false
    if (date != result.date) return false
    if (sportType() != result.sportType()) return false
    if (sportType() == TrainingSportType.OTHER && normalizedTitle() != result.normalizedTitle()) return false
    return true
}

private fun TrainingItem.mergeScoreForResult(result: TrainingItem): Int {
    var score = 0
    if (normalizedTitle() == result.normalizedTitle()) score += 30
    if (sportType() == TrainingSportType.STRENGTH) score += 20
    if (durationSeconds != null && result.durationSeconds != null) {
        val diff = abs(durationSeconds - result.durationSeconds)
        score += when {
            diff <= 60 -> 12
            diff <= 300 -> 6
            else -> 0
        }
    }
    if (distanceMeters != null && result.distanceMeters != null) {
        val diff = abs(distanceMeters - result.distanceMeters)
        score += when {
            diff <= 50.0 -> 12
            diff <= 500.0 -> 6
            else -> 0
        }
    }
    return score
}

private fun TrainingItem.normalizedTitle(): String {
    return name.ifBlank { type }
        .lowercase(Locale.KOREAN)
        .replace(" ", "")
        .replace("_", "")
        .replace("-", "")
}

private fun TrainingSportType.icon(): ImageVector {
    return when (this) {
        TrainingSportType.RUNNING -> Icons.AutoMirrored.Outlined.DirectionsRun
        TrainingSportType.CYCLING -> Icons.AutoMirrored.Outlined.DirectionsBike
        TrainingSportType.STRENGTH -> Icons.Outlined.FitnessCenter
        TrainingSportType.OTHER -> Icons.Outlined.Route
    }
}

@Composable
private fun TrainingSportIcon(
    sportType: TrainingSportType,
    modifier: Modifier = Modifier,
    showBackground: Boolean = true,
) {
    val tint = when (sportType) {
        TrainingSportType.RUNNING -> MaterialTheme.colorScheme.tertiary
        TrainingSportType.CYCLING -> MaterialTheme.colorScheme.secondary
        TrainingSportType.STRENGTH -> MaterialTheme.colorScheme.primary
        TrainingSportType.OTHER -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    if (showBackground) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(999.dp))
                .background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = sportType.icon(),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
    } else {
        Icon(
            imageVector = sportType.icon(),
            contentDescription = null,
            tint = tint,
            modifier = modifier
        )
    }
}

private fun List<TrainingItem>.latestMetricValue(selector: (TrainingItem) -> Double?): Double? {
    return sortedWith(
        compareByDescending<TrainingItem> { it.startedAt ?: it.date.atStartOfDay() }
            .thenByDescending { it.date }
    ).firstNotNullOfOrNull(selector)
}

internal data class RunningWorkoutSession(
    val name: String,
    val startedAt: LocalDateTime,
    val endedAt: LocalDateTime,
    val warmupSeconds: Int,
    val blocks: List<PlanBlock>,
    val actualBlocks: List<PlanBlock>,
)

private data class CompletedRunningWorkout(
    val id: String,
    val name: String,
    val startedAtMillis: Long,
    val endedAtMillis: Long,
    val durationSeconds: Int,
    val warmupSeconds: Int,
    val estimatedDistanceMeters: Double,
    val blocks: List<PlanBlock>,
    val actualBlocks: List<PlanBlock>,
    val uploadedToIntervals: Boolean,
)

private fun notifyRestFinished(context: Context) {
    if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        REST_NOTIFICATION_ID,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val notification = NotificationCompat.Builder(context, REST_NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("휴식 종료")
        .setContentText("다음 세트를 시작할 시간입니다.")
        .setContentIntent(pendingIntent)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setVibrate(longArrayOf(0, 400, 180, 400))
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context).notify(REST_NOTIFICATION_ID, notification)
}

private fun requestOverlayPermissionIfNeeded(context: Context) {
    if (!Settings.canDrawOverlays(context)) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }
}

private fun startRestOverlay(context: Context, title: String, endAtMillis: Long) {
    if (!Settings.canDrawOverlays(context)) return
    val intent = Intent(context, RestTimerOverlayService::class.java).apply {
        putExtra(RestTimerOverlayService.EXTRA_TITLE, title)
        putExtra(RestTimerOverlayService.EXTRA_END_AT, endAtMillis)
    }
    runCatching { context.startService(intent) }
}

private fun stopRestOverlay(context: Context) {
    val intent = Intent(context, RestTimerOverlayService::class.java).apply {
        action = RestTimerOverlayService.ACTION_STOP
    }
    runCatching { context.startService(intent) }
}

private fun startRunningOverlay(
    context: Context,
    title: String,
    endAtMillis: Long,
    startAtMillis: Long = 0L,
    actionLabel: String,
    targetSpeed: String = "",
    targetIncline: String = "",
    heartRateBpm: Int? = null,
) {
    if (!Settings.canDrawOverlays(context)) return
    val intent = Intent(context, RunningWorkoutOverlayService::class.java).apply {
        putExtra(RunningWorkoutOverlayService.EXTRA_TITLE, title)
        putExtra(RunningWorkoutOverlayService.EXTRA_END_AT, endAtMillis)
        putExtra(RunningWorkoutOverlayService.EXTRA_START_AT, startAtMillis)
        putExtra(RunningWorkoutOverlayService.EXTRA_ACTION_LABEL, actionLabel)
        putExtra(RunningWorkoutOverlayService.EXTRA_TARGET_SPEED, targetSpeed)
        putExtra(RunningWorkoutOverlayService.EXTRA_TARGET_INCLINE, targetIncline)
        putExtra(RunningWorkoutOverlayService.EXTRA_HEART_RATE_BPM, heartRateBpm ?: 0)
    }
    runCatching { context.startService(intent) }
}

private fun stopRunningOverlay(context: Context) {
    val intent = Intent(context, RunningWorkoutOverlayService::class.java).apply {
        action = RunningWorkoutOverlayService.ACTION_STOP
    }
    runCatching { context.startService(intent) }
}

private fun loadStrengthPlans(prefs: SharedPreferences): List<StrengthWorkoutPlan> {
    val saved = prefs.getString(STRENGTH_PLANS_PREF, null)
    return saved.toStrengthWorkoutPlans().takeIf { it.isNotEmpty() } ?: defaultStrengthPlans()
}

private fun loadScheduledStrengthPlans(prefs: SharedPreferences): List<ScheduledStrengthPlan> {
    val saved = prefs.getString(SCHEDULED_STRENGTH_PLANS_PREF, null)
    return runCatching {
        val array = JSONArray(saved ?: "[]")
        (0 until array.length()).mapNotNull { index ->
            val json = array.optJSONObject(index) ?: return@mapNotNull null
            val date = runCatching { LocalDate.parse(json.optString("date")) }.getOrNull()
                ?: return@mapNotNull null
            val plan = json.optString("planJson")
                .toStrengthWorkoutPlans()
                .firstOrNull()
                ?: return@mapNotNull null
            val externalId = json.optString("externalId")
                .ifBlank { plan.intervalsPlanExternalId(date) }
            ScheduledStrengthPlan(
                id = json.optString("id").ifBlank { plan.scheduledStrengthPlanId(date) },
                date = date,
                plan = plan,
                uploadedToIntervals = json.optBoolean("uploadedToIntervals", false),
                externalId = externalId
            )
        }
    }.getOrElse { emptyList() }
}

private fun upsertScheduledStrengthPlan(
    prefs: SharedPreferences,
    scheduledPlan: ScheduledStrengthPlan,
) {
    val nextPlans = (listOf(scheduledPlan) + loadScheduledStrengthPlans(prefs))
        .distinctBy { it.externalId }
    val array = JSONArray().also { root ->
        nextPlans.forEach { item ->
            root.put(
                JSONObject()
                    .put("id", item.id)
                    .put("date", item.date.toString())
                    .put("externalId", item.externalId)
                    .put("uploadedToIntervals", item.uploadedToIntervals)
                    .put("planJson", listOf(item.plan).toJsonString())
            )
        }
    }
    prefs.edit().putString(SCHEDULED_STRENGTH_PLANS_PREF, array.toString()).apply()
}

private fun removeScheduledStrengthPlan(
    prefs: SharedPreferences,
    plan: TrainingItem,
) {
    val nextPlans = loadScheduledStrengthPlans(prefs).filterNot { scheduled ->
        scheduled.id == plan.remoteId ||
            scheduled.id == plan.id.removePrefix("local-") ||
            scheduled.externalId == plan.externalId
    }
    val array = JSONArray().also { root ->
        nextPlans.forEach { item ->
            root.put(
                JSONObject()
                    .put("id", item.id)
                    .put("date", item.date.toString())
                    .put("externalId", item.externalId)
                    .put("uploadedToIntervals", item.uploadedToIntervals)
                    .put("planJson", listOf(item.plan).toJsonString())
            )
        }
    }
    prefs.edit().putString(SCHEDULED_STRENGTH_PLANS_PREF, array.toString()).apply()
}

private fun List<TrainingItem>.withLocalStrengthPlans(
    scheduledPlans: List<ScheduledStrengthPlan>,
    start: LocalDate,
    end: LocalDate,
): List<TrainingItem> {
    val scheduledByExternalId = scheduledPlans.associateBy { it.externalId }
    val matchedRemoteItems = map { item ->
        val matchedPlan = item.externalId?.let { scheduledByExternalId[it] }?.plan
        if (matchedPlan == null || item.matchedStrengthPlan != null) {
            item
        } else {
            item.copy(matchedStrengthPlan = matchedPlan)
        }
    }
    val remoteExternalIds = mapNotNull { it.externalId }.toSet()
    val localItems = scheduledPlans
        .filter { scheduled -> !scheduled.date.isBefore(start) && !scheduled.date.isAfter(end) }
        .filterNot { scheduled -> scheduled.externalId in remoteExternalIds }
        .map { scheduled -> scheduled.toTrainingItem() }
    return matchedRemoteItems + localItems
}

private fun ScheduledStrengthPlan.toTrainingItem(): TrainingItem {
    return TrainingItem(
        id = "local-${id}",
        remoteId = id,
        externalId = externalId,
        name = plan.name,
        type = "Weight Training",
        date = date,
        startedAt = date.atStartOfDay(),
        timeLabel = "Plan",
        durationSeconds = plan.entries.totalDurationSeconds().takeIf { it > 0 },
        distanceMeters = null,
        weightLiftedKg = null,
        load = null,
        fitness = null,
        fatigue = null,
        form = null,
        description = plan.toIntervalsPlanDescription(),
        blocks = emptyList(),
        isPlan = true,
        matchedStrengthPlan = plan
    )
}

private fun List<StrengthWorkoutPlan>.withLatestCompletedWorkout(
    history: List<CompletedStrengthWorkout>,
): List<StrengthWorkoutPlan> {
    if (isEmpty() || history.isEmpty()) return this
    val latestByPlanId = history
        .filter { it.planId != 0 && it.entries.isNotEmpty() }
        .groupBy { it.planId }
        .mapValues { (_, workouts) -> workouts.maxByOrNull { it.startedAtMillis } }

    return map { plan ->
        val latestWorkout = latestByPlanId[plan.id] ?: return@map plan
        plan.copy(entries = latestWorkout.entries.map { it.copyForWorkout() })
    }
}

private fun ActiveStrengthSession.withLatestCompletedWorkout(
    history: List<CompletedStrengthWorkout>,
): ActiveStrengthSession {
    if (hasStarted || history.isEmpty()) return this
    val latestWorkout = history
        .filter { it.planId == planId && it.entries.isNotEmpty() }
        .maxByOrNull { it.startedAtMillis }
        ?: return this
    return copy(entries = latestWorkout.entries.map { it.copyForWorkout() })
}

internal fun StrengthWorkoutPlan.toIntervalsPlanDescription(): String {
    val setCount = entries.sumOf { it.records.size }
    return buildString {
        appendLine("IntervalsGym 웨이트 Plan")
        appendLine("운동 ${entries.size}개 · ${setCount}세트")
        appendLine()
        entries.forEach { entry ->
            appendLine("- ${entry.title}")
            entry.records.forEachIndexed { index, record ->
                if (entry.isUnilateral()) {
                    appendLine(
                        "  Set ${index + 1}: ${record.weightKg.ifBlank { "-" }}kg x 각 ${record.reps.ifBlank { "-" }}회, 휴식 ${record.restSeconds.ifBlank { "-" }}초"
                    )
                } else {
                    appendLine(
                        "  Set ${index + 1}: ${record.weightKg.ifBlank { "-" }}kg x ${record.reps.ifBlank { "-" }}회, 휴식 ${record.restSeconds.ifBlank { "-" }}초"
                    )
                }
            }
        }
    }
}

private fun String?.visiblePlanDescription(): String {
    if (isNullOrBlank()) return ""
    return lineSequence()
        .filterNot { line ->
            val trimmed = line.trim()
            trimmed.startsWith(INTERVALS_GYM_STRENGTH_PLAN_PREFIX) ||
                trimmed == "로컬 러닝 기록" ||
                trimmed.startsWith("로컬 러닝 기록 ·")
        }
        .joinToString("\n")
        .trim()
}

private fun TrainingItem.detailPlanDescription(): String {
    return pairedPlan?.description.visiblePlanDescription()
        .ifBlank { description.visiblePlanDescription() }
}

private fun String?.toIntervalsGymStrengthPlan(): StrengthWorkoutPlan? {
    if (isNullOrBlank()) return null
    val encoded = lineSequence()
        .map { it.trim() }
        .firstOrNull { it.startsWith(INTERVALS_GYM_STRENGTH_PLAN_PREFIX) }
        ?.removePrefix(INTERVALS_GYM_STRENGTH_PLAN_PREFIX)
        ?.trim()
        ?: return null
    return runCatching {
        val decoded = String(Base64.decode(encoded, Base64.DEFAULT), StandardCharsets.UTF_8)
        decoded.toStrengthWorkoutPlans().firstOrNull()
    }.getOrNull()
}

internal fun StrengthWorkoutPlan.intervalsPlanExternalId(date: LocalDate): String {
    return "intervals-gym-strength-plan-${id}-${date}"
}

private fun StrengthWorkoutPlan.scheduledStrengthPlanId(date: LocalDate): String {
    return "scheduled-strength-plan-${id}-${date}"
}

private fun loadActiveStrengthSession(prefs: SharedPreferences): ActiveStrengthSession? {
    return prefs.getString(ACTIVE_STRENGTH_SESSION_PREF, null).toActiveStrengthSession()
}

private fun ActiveStrengthSession.toJsonString(): String {
    val planJson = JSONArray(
        listOf(
            StrengthWorkoutPlan(
                id = planId,
                name = planName,
                entries = entries
            )
        ).toJsonString()
    ).optJSONObject(0) ?: JSONObject()

    return JSONObject()
        .put("plan", planJson)
        .put("hasStarted", hasStarted)
        .put("workoutStartedAtMillis", workoutStartedAtMillis)
        .put("isSetScreenVisible", isSetScreenVisible)
        .put("currentExerciseIndex", currentExerciseIndex)
        .put("currentSetIndex", currentSetIndex)
        .put("pendingExerciseIndex", pendingExerciseIndex)
        .put("pendingSetIndex", pendingSetIndex)
        .put("restEndAtMillis", restEndAtMillis)
        .put("isRestSheetVisible", isRestSheetVisible)
        .put("restTitle", restTitle)
        .put("setEvents", setEvents.toSetEventsJsonArray())
        .put("restEvents", restEvents.toRestEventsJsonArray())
        .put("activeRestEventId", activeRestEventId)
        .toString()
}

private fun String?.toActiveStrengthSession(): ActiveStrengthSession? {
    if (isNullOrBlank()) return null
    return runCatching {
        val json = JSONObject(this)
        val planJson = json.optJSONObject("plan") ?: return@runCatching null
        val plan = JSONArray()
            .put(planJson)
            .toString()
            .toStrengthWorkoutPlans()
            .firstOrNull() ?: return@runCatching null
        val restEndAtMillis = json.optLong("restEndAtMillis", 0L)
        val isExpiredRest = restEndAtMillis > 0L && restEndAtMillis <= System.currentTimeMillis()
        val restEvents = json.optJSONArray("restEvents").toStrengthRestEvents()
        val activeRestEventId = json.optNullableInt("activeRestEventId")

        ActiveStrengthSession(
            planId = plan.id,
            planName = plan.name,
            entries = plan.entries,
            hasStarted = json.optBoolean("hasStarted", false),
            workoutStartedAtMillis = json.optLong("workoutStartedAtMillis", 0L).takeIf { it > 0L }
                ?: if (json.optBoolean("hasStarted", false)) System.currentTimeMillis() else 0L,
            isSetScreenVisible = json.optBoolean("isSetScreenVisible", false),
            currentExerciseIndex = if (isExpiredRest) {
                json.optNullableInt("pendingExerciseIndex") ?: json.optNullableInt("currentExerciseIndex") ?: 0
            } else {
                json.optNullableInt("currentExerciseIndex") ?: 0
            },
            currentSetIndex = if (isExpiredRest) {
                json.optNullableInt("pendingSetIndex") ?: json.optNullableInt("currentSetIndex") ?: 0
            } else {
                json.optNullableInt("currentSetIndex") ?: 0
            },
            pendingExerciseIndex = if (isExpiredRest) null else json.optNullableInt("pendingExerciseIndex"),
            pendingSetIndex = if (isExpiredRest) null else json.optNullableInt("pendingSetIndex"),
            restEndAtMillis = if (isExpiredRest) 0L else restEndAtMillis,
            isRestSheetVisible = !isExpiredRest && json.optBoolean("isRestSheetVisible", false),
            restTitle = if (isExpiredRest) "" else json.optString("restTitle"),
            setEvents = json.optJSONArray("setEvents").toStrengthSetCompletionEvents(),
            restEvents = if (isExpiredRest && activeRestEventId != null) {
                finalizeRestEvents(restEvents, activeRestEventId, restEndAtMillis, "finished")
            } else {
                restEvents
            },
            activeRestEventId = if (isExpiredRest) null else activeRestEventId
        )
    }.getOrNull()
}

private fun buildCompletedStrengthWorkout(
    plan: StrengthWorkoutPlan,
    entries: List<StrengthPlanEntry>,
    setEvents: List<StrengthSetCompletionEvent>,
    restEvents: List<StrengthRestEvent>,
    startedAtMillis: Long,
    endedAtMillis: Long,
    rpe: Int,
    trainingLoad: Int,
    uploadedToIntervals: Boolean,
): CompletedStrengthWorkout {
    val safeStartedAt = startedAtMillis.takeIf { it > 0L } ?: endedAtMillis
    return CompletedStrengthWorkout(
        id = "strength-${safeStartedAt}-${endedAtMillis}",
        planId = plan.id,
        planName = plan.name,
        startedAtMillis = safeStartedAt,
        endedAtMillis = endedAtMillis,
        durationSeconds = ((endedAtMillis - safeStartedAt) / 1000L).toInt().coerceAtLeast(0),
        intervalsExternalId = strengthIntervalsExternalId(safeStartedAt),
        entries = entries,
        setEvents = setEvents.sortedBy { it.sequence },
        restEvents = restEvents.sortedBy { it.startedAtMillis },
        rpe = rpe,
        trainingLoad = trainingLoad,
        uploadedToIntervals = uploadedToIntervals
    )
}

private fun appendStrengthWorkoutHistory(
    prefs: SharedPreferences,
    workout: CompletedStrengthWorkout,
) {
    val saved = prefs.getString(STRENGTH_WORKOUT_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    val nextHistory = JSONArray().apply {
        put(workout.toJsonObject())
        val maxPreviousItems = 99
        for (index in 0 until minOf(history.length(), maxPreviousItems)) {
            put(history.optJSONObject(index) ?: continue)
        }
    }
    prefs.edit().putString(STRENGTH_WORKOUT_HISTORY_PREF, nextHistory.toString()).apply()
}

private fun replaceStrengthWorkoutHistory(
    prefs: SharedPreferences,
    workout: CompletedStrengthWorkout,
) {
    val saved = prefs.getString(STRENGTH_WORKOUT_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    var replaced = false
    val nextHistory = JSONArray().apply {
        for (index in 0 until history.length()) {
            val existing = history.optJSONObject(index).toCompletedStrengthWorkout()
            if (existing?.id == workout.id) {
                put(workout.toJsonObject())
                replaced = true
            } else {
                put(history.optJSONObject(index) ?: continue)
            }
        }
        if (!replaced) {
            put(workout.toJsonObject())
        }
    }
    prefs.edit().putString(STRENGTH_WORKOUT_HISTORY_PREF, nextHistory.toString()).apply()
}

private fun loadCompletedStrengthWorkoutHistory(prefs: SharedPreferences): List<CompletedStrengthWorkout> {
    val saved = prefs.getString(STRENGTH_WORKOUT_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    return (0 until history.length()).mapNotNull { index ->
        history.optJSONObject(index).toCompletedStrengthWorkout()
    }
}

private fun appendRunningWorkoutHistory(
    prefs: SharedPreferences,
    workout: CompletedRunningWorkout,
) {
    val saved = prefs.getString(RUNNING_WORKOUT_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    val nextHistory = JSONArray().apply {
        put(workout.toJsonObject())
        val maxPreviousItems = 99
        for (index in 0 until minOf(history.length(), maxPreviousItems)) {
            val existing = history.optJSONObject(index) ?: continue
            if (existing.optString("id") != workout.id) {
                put(existing)
            }
        }
    }
    prefs.edit().putString(RUNNING_WORKOUT_HISTORY_PREF, nextHistory.toString()).apply()
}

private fun replaceRunningWorkoutHistory(
    prefs: SharedPreferences,
    workout: CompletedRunningWorkout,
) {
    val saved = prefs.getString(RUNNING_WORKOUT_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    var replaced = false
    val nextHistory = JSONArray().apply {
        for (index in 0 until history.length()) {
            val existing = history.optJSONObject(index) ?: continue
            if (existing.optString("id") == workout.id) {
                put(workout.toJsonObject())
                replaced = true
            } else {
                put(existing)
            }
        }
        if (!replaced) put(workout.toJsonObject())
    }
    prefs.edit().putString(RUNNING_WORKOUT_HISTORY_PREF, nextHistory.toString()).apply()
}

private fun deleteRunningWorkoutHistory(
    prefs: SharedPreferences,
    workoutId: String,
) {
    val saved = prefs.getString(RUNNING_WORKOUT_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    val nextHistory = JSONArray().apply {
        for (index in 0 until history.length()) {
            val existing = history.optJSONObject(index) ?: continue
            if (existing.optString("id") != workoutId) {
                put(existing)
            }
        }
    }
    prefs.edit().putString(RUNNING_WORKOUT_HISTORY_PREF, nextHistory.toString()).apply()
}

private fun loadCompletedRunningWorkoutHistory(prefs: SharedPreferences): List<CompletedRunningWorkout> {
    val saved = prefs.getString(RUNNING_WORKOUT_HISTORY_PREF, null)
    val history = runCatching { JSONArray(saved ?: "[]") }.getOrElse { JSONArray() }
    return (0 until history.length()).mapNotNull { index ->
        history.optJSONObject(index).toCompletedRunningWorkout()
    }
}

private fun JSONObject?.toCompletedStrengthWorkout(): CompletedStrengthWorkout? {
    this ?: return null
    val planSnapshot = optJSONObject("planSnapshot")
    val snapshotPlan = planSnapshot?.let {
        JSONArray().put(it).toString().toStrengthWorkoutPlans().firstOrNull()
    }
    val planId = optNullableInt("planId") ?: snapshotPlan?.id ?: 0
    val planName = optString("planName").ifBlank { snapshotPlan?.name ?: "웨이트 트레이닝" }
    val startedAtMillis = optLong("startedAtMillis", 0L)
    val endedAtMillis = optLong("endedAtMillis", startedAtMillis)
    if (startedAtMillis <= 0L) return null
    val entries = snapshotPlan?.entries.orEmpty()
    val rpe = optNullableInt("rpe") ?: 7
    return CompletedStrengthWorkout(
        id = optString("id").ifBlank { "strength-$startedAtMillis-$endedAtMillis" },
        planId = planId,
        planName = planName,
        startedAtMillis = startedAtMillis,
        endedAtMillis = endedAtMillis,
        durationSeconds = optNullableInt("durationSeconds")
            ?: ((endedAtMillis - startedAtMillis) / 1000L).toInt().coerceAtLeast(0),
        intervalsExternalId = optString("intervalsExternalId")
            .ifBlank { strengthIntervalsExternalId(startedAtMillis) },
        entries = entries,
        setEvents = optJSONArray("setEvents").toStrengthSetCompletionEvents(),
        restEvents = optJSONArray("restEvents").toStrengthRestEvents(),
        rpe = rpe,
        trainingLoad = optNullableInt("trainingLoad") ?: entries.strengthTrainingLoad(rpe),
        uploadedToIntervals = optBoolean("uploadedToIntervals", false)
    )
}

private fun JSONObject?.toCompletedRunningWorkout(): CompletedRunningWorkout? {
    this ?: return null
    val startedAtMillis = optLong("startedAtMillis", 0L)
    val endedAtMillis = optLong("endedAtMillis", startedAtMillis)
    if (startedAtMillis <= 0L) return null
    val durationSeconds = optNullableInt("durationSeconds")
        ?: ((endedAtMillis - startedAtMillis) / 1000L).toInt().coerceAtLeast(0)
    val warmupSeconds = optNullableInt("warmupSeconds") ?: 0
    val planBlocks = optJSONArray("blocks").toCachedPlanBlocks()
    val savedActualBlocks = optJSONArray("actualBlocks").toCachedPlanBlocks()
    val actualBlocks = savedActualBlocks.normalizedRunningActualBlocks(
        planBlocks = planBlocks,
        activeDurationSeconds = (durationSeconds - warmupSeconds).coerceAtLeast(0)
    )
    return CompletedRunningWorkout(
        id = optString("id").ifBlank { "running-$startedAtMillis" },
        name = optString("name").ifBlank { "러닝" },
        startedAtMillis = startedAtMillis,
        endedAtMillis = endedAtMillis,
        durationSeconds = durationSeconds,
        warmupSeconds = warmupSeconds,
        estimatedDistanceMeters = actualBlocks.estimatedRunningDistanceMeters(),
        blocks = planBlocks,
        actualBlocks = actualBlocks,
        uploadedToIntervals = optBoolean("uploadedToIntervals", false)
    )
}

private fun List<TrainingItem>.withMatchedStrengthWorkouts(
    history: List<CompletedStrengthWorkout>,
): List<TrainingItem> {
    if (history.isEmpty()) return this
    return map { item ->
        if (item.isPlan) {
            item
        } else {
            item.copy(matchedStrengthWorkout = item.matchStrengthWorkout(history))
        }
    }
}

private fun List<TrainingItem>.withLocalStrengthResults(
    history: List<CompletedStrengthWorkout>,
    weekStart: LocalDate,
    weekEnd: LocalDate,
): List<TrainingItem> {
    val matched = withMatchedStrengthWorkouts(history)
    val matchedWorkoutIds = matched.mapNotNull { it.matchedStrengthWorkout?.id }.toSet()
    val localOnlyItems = history
        .filter { workout ->
            val date = workout.startedLocalDate()
            date in weekStart..weekEnd && workout.id !in matchedWorkoutIds
        }
        .map { workout -> workout.toLocalTrainingItem() }
    return matched + localOnlyItems
}

private fun CompletedStrengthWorkout.startedLocalDate(): LocalDate {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault()).toLocalDate()
}

private fun CompletedStrengthWorkout.toLocalTrainingItem(): TrainingItem {
    val startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault())
    return TrainingItem(
        id = "local-strength-$id",
        remoteId = id,
        externalId = intervalsExternalId,
        name = planName,
        type = "Weight Training",
        date = startedAt.toLocalDate(),
        startedAt = startedAt,
        timeLabel = startedAt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
        durationSeconds = durationSeconds,
        distanceMeters = null,
        weightLiftedKg = entries.totalVolumeKg(),
        load = trainingLoad,
        fitness = null,
        fatigue = null,
        form = null,
        description = if (uploadedToIntervals) {
            "로컬 웨이트 기록 · Intervals.icu에서 삭제되었을 수 있습니다."
        } else {
            "로컬 웨이트 기록 · Intervals.icu 미동기화"
        },
        blocks = emptyList(),
        isPlan = false,
        matchedStrengthWorkout = this,
        isLocalOnlyStrengthResult = true
    )
}

private fun CompletedStrengthWorkout.toStrengthWorkoutSession(): StrengthWorkoutSession {
    return StrengthWorkoutSession(
        name = planName,
        startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault()),
        entries = entries,
        rpe = rpe,
        trainingLoad = trainingLoad
    )
}

private fun List<TrainingItem>.withLocalRunningResults(
    history: List<CompletedRunningWorkout>,
    weekStart: LocalDate,
    weekEnd: LocalDate,
): List<TrainingItem> {
    if (history.isEmpty()) return this
    val localOnlyItems = history
        .filter { workout ->
            val date = workout.startedLocalDate()
            date in weekStart..weekEnd && none { item -> item.matchesRunningWorkout(workout) }
        }
        .map { workout -> workout.toLocalTrainingItem() }
    return this + localOnlyItems
}

private fun CompletedRunningWorkout.startedLocalDate(): LocalDate {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault()).toLocalDate()
}

private fun CompletedRunningWorkout.toLocalTrainingItem(): TrainingItem {
    val startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault())
    return TrainingItem(
        id = "local-running-$id",
        remoteId = id,
        externalId = id,
        name = name,
        type = "Run",
        date = startedAt.toLocalDate(),
        startedAt = startedAt,
        timeLabel = startedAt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
        durationSeconds = durationSeconds,
        distanceMeters = estimatedDistanceMeters,
        weightLiftedKg = null,
        load = null,
        fitness = null,
        fatigue = null,
        form = null,
        description = if (uploadedToIntervals) {
            "로컬 러닝 기록 · Intervals.icu 업로드됨"
        } else {
            "로컬 러닝 기록"
        },
        blocks = blocks,
        isPlan = false,
        isLocalOnlyRunningResult = true,
        actualRunningBlocks = actualBlocks
    )
}

private fun TrainingItem.matchesRunningWorkout(workout: CompletedRunningWorkout): Boolean {
    if (isLocalOnlyRunningResult) {
        return remoteId == workout.id || id == "local-running-${workout.id}"
    }
    if (isPlan || sportType() != TrainingSportType.RUNNING) return false
    val startedMillis = startedAt
        ?.atZone(ZoneId.systemDefault())
        ?.toInstant()
        ?.toEpochMilli()
        ?: return false
    val timeDiff = abs(startedMillis - workout.startedAtMillis)
    val durationDiff = durationSeconds?.let { abs(it - workout.durationSeconds) } ?: Int.MAX_VALUE
    return timeDiff <= 10 * 60 * 1000L || durationDiff <= 5 * 60
}

private fun TrainingItem.matchStrengthWorkout(
    history: List<CompletedStrengthWorkout>,
): CompletedStrengthWorkout? {
    externalId?.let { id ->
        history.firstOrNull { it.intervalsExternalId == id }?.let { return it }
    }
    val startedMillis = startedAt
        ?.atZone(ZoneId.systemDefault())
        ?.toInstant()
        ?.toEpochMilli()
        ?: return null
    val looksLikeStrength = name.contains("웨이트", ignoreCase = true) ||
        name.contains("strength", ignoreCase = true) ||
        description.orEmpty().contains("IntervalsGym 웨이트", ignoreCase = true)
    return history
        .filter { workout ->
            kotlin.math.abs(workout.startedAtMillis - startedMillis) <= 2 * 60 * 1000L
        }
        .filter { workout ->
            looksLikeStrength ||
                workout.planName.equals(name, ignoreCase = true) ||
                name.contains(workout.planName, ignoreCase = true)
        }
        .minByOrNull { kotlin.math.abs(it.startedAtMillis - startedMillis) }
}

private fun strengthIntervalsExternalId(startedAtMillis: Long): String {
    val startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault())
    return "intervals-gym-${startedAt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))}"
}

private fun finalizeRestEvents(
    restEvents: List<StrengthRestEvent>,
    activeRestEventId: Int?,
    endedAtMillis: Long,
    reason: String,
): List<StrengthRestEvent> {
    if (activeRestEventId == null) return restEvents
    return restEvents.map { event ->
        if (event.id == activeRestEventId && event.endedAtMillis == null) {
            event.copy(
                endedAtMillis = endedAtMillis,
                endReason = reason
            )
        } else {
            event
        }
    }
}

private fun CompletedStrengthWorkout.toJsonObject(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("planId", planId)
        .put("planName", planName)
        .put("startedAtMillis", startedAtMillis)
        .put("endedAtMillis", endedAtMillis)
        .put("durationSeconds", durationSeconds)
        .put("intervalsExternalId", intervalsExternalId)
        .put("rpe", rpe)
        .put("trainingLoad", trainingLoad)
        .put("uploadedToIntervals", uploadedToIntervals)
        .put(
            "planSnapshot",
            JSONArray(
                listOf(
                    StrengthWorkoutPlan(
                        id = planId,
                        name = planName,
                        entries = entries
                    )
                ).toJsonString()
            ).optJSONObject(0) ?: JSONObject()
        )
        .put("setEvents", setEvents.toSetEventsJsonArray())
        .put("restEvents", restEvents.toRestEventsJsonArray())
}

private fun List<StrengthSetCompletionEvent>.toSetEventsJsonArray(): JSONArray {
    return JSONArray().also { array ->
        forEach { event ->
            array.put(
                JSONObject()
                    .put("sequence", event.sequence)
                    .put("exerciseEntryId", event.exerciseEntryId)
                    .put("exerciseTitle", event.exerciseTitle)
                    .put("exerciseGroup", event.exerciseGroup)
                    .put("exerciseId", event.exerciseId)
                    .put("equipment", event.equipment)
                    .put("variation", event.variation)
                    .put("setRecordId", event.setRecordId)
                    .put("setIndex", event.setIndex)
                    .put("weightKg", event.weightKg)
                    .put("reps", event.reps)
                    .put("targetRestSeconds", event.targetRestSeconds)
                    .put("completedAtMillis", event.completedAtMillis)
            )
        }
    }
}

private fun JSONArray?.toStrengthSetCompletionEvents(): List<StrengthSetCompletionEvent> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { index ->
        val json = optJSONObject(index) ?: return@mapNotNull null
        StrengthSetCompletionEvent(
            sequence = json.optNullableInt("sequence") ?: (index + 1),
            exerciseEntryId = json.optNullableInt("exerciseEntryId") ?: 0,
            exerciseTitle = json.optString("exerciseTitle"),
            exerciseGroup = json.optString("exerciseGroup"),
            exerciseId = json.optString("exerciseId"),
            equipment = json.optString("equipment"),
            variation = json.optString("variation"),
            setRecordId = json.optNullableInt("setRecordId") ?: 0,
            setIndex = json.optNullableInt("setIndex") ?: 0,
            weightKg = json.optString("weightKg"),
            reps = json.optString("reps"),
            targetRestSeconds = json.optNullableInt("targetRestSeconds") ?: 0,
            completedAtMillis = json.optLong("completedAtMillis", 0L)
        )
    }
}

private fun List<StrengthRestEvent>.toRestEventsJsonArray(): JSONArray {
    return JSONArray().also { array ->
        forEach { event ->
            array.put(
                JSONObject()
                    .put("id", event.id)
                    .put("afterSetSequence", event.afterSetSequence)
                    .put("exerciseEntryId", event.exerciseEntryId)
                    .put("exerciseTitle", event.exerciseTitle)
                    .put("setRecordId", event.setRecordId)
                    .put("setIndex", event.setIndex)
                    .put("startedAtMillis", event.startedAtMillis)
                    .put("plannedSeconds", event.plannedSeconds)
                    .put("targetEndAtMillis", event.targetEndAtMillis)
                    .put("endedAtMillis", event.endedAtMillis ?: JSONObject.NULL)
                    .put("actualSeconds", event.actualSeconds)
                    .put("endReason", event.endReason ?: JSONObject.NULL)
            )
        }
    }
}

private fun JSONArray?.toStrengthRestEvents(): List<StrengthRestEvent> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { index ->
        val json = optJSONObject(index) ?: return@mapNotNull null
        StrengthRestEvent(
            id = json.optNullableInt("id") ?: (index + 1),
            afterSetSequence = json.optNullableInt("afterSetSequence") ?: 0,
            exerciseEntryId = json.optNullableInt("exerciseEntryId") ?: 0,
            exerciseTitle = json.optString("exerciseTitle"),
            setRecordId = json.optNullableInt("setRecordId") ?: 0,
            setIndex = json.optNullableInt("setIndex") ?: 0,
            startedAtMillis = json.optLong("startedAtMillis", 0L),
            plannedSeconds = json.optNullableInt("plannedSeconds") ?: 0,
            targetEndAtMillis = json.optLong("targetEndAtMillis", 0L),
            endedAtMillis = json.optNullableLong("endedAtMillis"),
            endReason = json.optString("endReason").takeIf { it.isNotBlank() }
        )
    }
}

private fun List<String>.toStringJsonArray(): JSONArray {
    return JSONArray().also { array ->
        forEach { value -> array.put(value) }
    }
}

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { index ->
        optString(index).takeIf { it.isNotBlank() }
    }
}

internal fun String?.cleanJsonText(): String? {
    return this
        ?.trim()
        ?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
}

private fun List<String>.withPreferredOption(option: String): List<String> {
    val safeOption = option.takeIf { it.isNotBlank() } ?: return ifEmpty { listOf("기본") }
    return if (contains(safeOption)) this else listOf(safeOption) + this
}

private fun JSONObject.toStrengthExercise(): StrengthExercise {
    val exerciseId = optString("exerciseId")
    strengthExerciseCatalog.firstOrNull { it.id == exerciseId }?.let { return it }

    val nameKo = optString("exerciseNameKo")
        .ifBlank { optString("exerciseNameEn") }
        .ifBlank { optString("exerciseName") }
        .ifBlank { "사용자 운동" }
    val group = optString("exerciseGroup").ifBlank { "사용자 추가" }
    val isCustomExercise = group == "사용자 추가" || exerciseId.startsWith("custom_")
    val equipment = optString("equipment")
    val variation = optString("variation")
    val savedEquipmentOptions = optJSONArray("equipmentOptions")
        .toStringList()
    val equipmentOptions = if (isCustomExercise && (savedEquipmentOptions.isEmpty() || savedEquipmentOptions == listOf("기본"))) {
        CUSTOM_STRENGTH_EQUIPMENT_OPTIONS
    } else {
        savedEquipmentOptions.ifEmpty { listOf(equipment.ifBlank { "기본" }) }
    }
        .distinct()
        .withPreferredOption(equipment)
    val variationOptions = optJSONArray("variationOptions")
        .toStringList()
        .ifEmpty { listOf(variation.ifBlank { "기본" }) }
        .distinct()
        .withPreferredOption(variation)

    return StrengthExercise(
        id = exerciseId.ifBlank { customStrengthExercise(nameKo).id },
        nameKo = nameKo,
        nameEn = optString("exerciseNameEn").ifBlank { nameKo },
        group = group,
        equipmentOptions = equipmentOptions,
        variationOptions = variationOptions
    )
}

private fun List<StrengthWorkoutPlan>.toJsonString(): String {
    return JSONArray().also { plansArray ->
        forEach { plan ->
            plansArray.put(
                JSONObject()
                    .put("id", plan.id)
                    .put("name", plan.name)
                    .put(
                        "entries",
                        JSONArray().also { entriesArray ->
                            plan.entries.forEach { entry ->
                                entriesArray.put(
                                    JSONObject()
                                        .put("id", entry.id)
                                        .put("exerciseId", entry.exercise.id)
                                        .put("exerciseNameKo", entry.exercise.nameKo)
                                        .put("exerciseNameEn", entry.exercise.nameEn)
                                        .put("exerciseGroup", entry.exercise.group)
                                        .put("equipmentOptions", entry.exercise.equipmentOptions.toStringJsonArray())
                                        .put("variationOptions", entry.exercise.variationOptions.toStringJsonArray())
                                        .put("equipment", entry.equipment)
                                        .put("variation", entry.variation)
                                        .put("supersetGroupId", entry.supersetGroupId ?: JSONObject.NULL)
                                        .put("targetSets", entry.targetSets)
                                        .put("targetReps", entry.targetReps)
                                        .put("restSeconds", entry.restSeconds)
                                        .put("targetWeightKg", entry.targetWeightKg)
                                        .put(
                                            "records",
                                            JSONArray().also { recordsArray ->
                                                entry.records.forEach { record ->
                                                    recordsArray.put(
                                                        JSONObject()
                                                            .put("id", record.id)
                                                            .put("weightKg", record.weightKg)
                                                            .put("reps", record.reps)
                                                            .put("leftWeightKg", record.leftWeightKg)
                                                            .put("leftReps", record.leftReps)
                                                            .put("rightWeightKg", record.rightWeightKg)
                                                            .put("rightReps", record.rightReps)
                                                            .put("durationSeconds", record.durationSeconds)
                                                            .put("restSeconds", record.restSeconds)
                                                            .put("completed", record.completed)
                                                    )
                                                }
                                            }
                                        )
                                )
                            }
                        }
                    )
            )
        }
    }.toString()
}

private fun String?.toStrengthWorkoutPlans(): List<StrengthWorkoutPlan> {
    if (isNullOrBlank()) return emptyList()
    return runCatching {
        val plansArray = JSONArray(this)
        (0 until plansArray.length()).mapNotNull { planIndex ->
            val planJson = plansArray.optJSONObject(planIndex) ?: return@mapNotNull null
            val entriesArray = planJson.optJSONArray("entries") ?: JSONArray()
            val entries = (0 until entriesArray.length()).mapNotNull { entryIndex ->
                val entryJson = entriesArray.optJSONObject(entryIndex) ?: return@mapNotNull null
                val parsedExercise = entryJson.toStrengthExercise()
                val savedVariation = entryJson.optString("variation")
                val shouldMigrateHackSquat = parsedExercise.id == "squat" && savedVariation == "핵 스쿼트"
                val exercise = if (shouldMigrateHackSquat) {
                    strengthExerciseCatalog.firstOrNull { it.id == "hack_squat" } ?: parsedExercise
                } else {
                    parsedExercise
                }
                val recordsArray = entryJson.optJSONArray("records") ?: JSONArray()
                val records = (0 until recordsArray.length()).mapNotNull { recordIndex ->
                    val recordJson = recordsArray.optJSONObject(recordIndex) ?: return@mapNotNull null
                    StrengthSetRecord(
                        id = recordJson.optNullableInt("id") ?: (recordIndex + 1),
                        weightKg = recordJson.optString("weightKg"),
                        reps = recordJson.optString("reps"),
                        leftWeightKg = recordJson.optString("leftWeightKg").ifBlank { recordJson.optString("weightKg") },
                        leftReps = recordJson.optString("leftReps").ifBlank { recordJson.optString("reps") },
                        rightWeightKg = recordJson.optString("rightWeightKg").ifBlank { recordJson.optString("weightKg") },
                        rightReps = recordJson.optString("rightReps").ifBlank { recordJson.optString("reps") },
                        durationSeconds = recordJson.optString("durationSeconds"),
                        restSeconds = recordJson.optString("restSeconds"),
                        completed = recordJson.optBoolean("completed", false)
                    )
                }.ifEmpty {
                    listOf(
                        StrengthSetRecord(
                            id = 1,
                            weightKg = entryJson.optString("targetWeightKg"),
                            reps = entryJson.optNullableInt("targetReps")?.takeIf { it > 0 }?.toString().orEmpty(),
                            leftWeightKg = entryJson.optString("targetWeightKg"),
                            leftReps = entryJson.optNullableInt("targetReps")?.takeIf { it > 0 }?.toString().orEmpty(),
                            rightWeightKg = entryJson.optString("targetWeightKg"),
                            rightReps = entryJson.optNullableInt("targetReps")?.takeIf { it > 0 }?.toString().orEmpty(),
                            durationSeconds = "",
                            restSeconds = entryJson.optNullableInt("restSeconds")?.takeIf { it > 0 }?.toString().orEmpty(),
                            completed = false
                        )
                    )
                }
                StrengthPlanEntry(
                    id = entryJson.optNullableInt("id") ?: (entryIndex + 1),
                    exercise = exercise,
                    equipment = if (shouldMigrateHackSquat) {
                        "머신"
                    } else if (entryJson.has("equipment")) {
                        entryJson.optString("equipment")
                    } else {
                        exercise.equipmentOptions.first()
                    },
                    variation = if (shouldMigrateHackSquat) {
                        "기본"
                    } else {
                        savedVariation.ifBlank { exercise.variationOptions.first() }
                    },
                    supersetGroupId = entryJson.optNullableInt("supersetGroupId"),
                    targetSets = entryJson.optNullableInt("targetSets") ?: records.size,
                    targetReps = entryJson.optNullableInt("targetReps") ?: records.firstOrNull()?.reps?.toIntOrNull() ?: 0,
                    restSeconds = entryJson.optNullableInt("restSeconds") ?: records.firstOrNull()?.restSeconds?.toIntOrNull() ?: 0,
                    targetWeightKg = entryJson.optString("targetWeightKg"),
                    records = records
                )
            }
            StrengthWorkoutPlan(
                id = planJson.optNullableInt("id") ?: (planIndex + 1),
                name = planJson.optString("name").ifBlank { "웨이트 Plan" },
                entries = entries
            )
        }
    }.getOrDefault(emptyList())
}

internal fun StrengthWorkoutSession.toIntervalsDescription(): String {
    val totalVolume = entries.totalVolumeKg()
    val completedSets = entries.sumOf { entry -> entry.records.count { it.completed } }
    val totalSets = entries.sumOf { it.records.size }

    return buildString {
        appendLine("IntervalsGym 웨이트 트레이닝 기록")
        appendLine("총 세트: $completedSets/$totalSets")
        appendLine("총 볼륨: ${formatWeight(totalVolume)} kg")
        appendLine("Weight Lifted: ${formatWeight(totalVolume)} kg")
        appendLine("RPE: $rpe")
        appendLine("Strength Load: $trainingLoad")
        appendLine("총 수행 시간: ${formatDuration(entries.totalDurationSeconds())}")
        appendLine()
        entries.forEach { entry ->
            appendLine("- ${entry.title}")
            appendLine("  Plan: ${entry.targetSets}세트 x ${entry.targetReps}회, 휴식 ${entry.restSeconds}초")
            entry.records.forEachIndexed { index, record ->
                val status = if (record.completed) "완료" else "미완료"
                val weight = record.weightKg.ifBlank { entry.targetWeightKg.ifBlank { "-" } }
                val reps = record.reps.ifBlank { "-" }
                val rest = record.restSeconds.ifBlank { entry.restSeconds.takeIf { it > 0 }?.toString() ?: "-" }
                if (entry.isUnilateral()) {
                    appendLine("  Set ${index + 1}: ${weight}kg x 각 ${reps}회, 휴식 ${rest}초, $status")
                } else {
                    appendLine("  Set ${index + 1}: ${weight}kg x ${reps}회, 휴식 ${rest}초, $status")
                }
            }
            appendLine()
        }
    }
}

internal fun RunningWorkoutSession.durationSeconds(): Int {
    return ChronoUnit.SECONDS.between(startedAt, endedAt).toInt().coerceAtLeast(0)
}

private fun RunningWorkoutSession.toCompletedRunningWorkout(uploadedToIntervals: Boolean): CompletedRunningWorkout {
    val startedAtMillis = startedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val endedAtMillis = endedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    return CompletedRunningWorkout(
        id = "running-$startedAtMillis",
        name = name,
        startedAtMillis = startedAtMillis,
        endedAtMillis = endedAtMillis,
        durationSeconds = durationSeconds(),
        warmupSeconds = warmupSeconds,
        estimatedDistanceMeters = estimatedDistanceMeters(),
        blocks = blocks,
        actualBlocks = actualBlocks,
        uploadedToIntervals = uploadedToIntervals
    )
}

private fun CompletedRunningWorkout.toJsonObject(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("name", name)
        .put("startedAtMillis", startedAtMillis)
        .put("endedAtMillis", endedAtMillis)
        .put("durationSeconds", durationSeconds)
        .put("warmupSeconds", warmupSeconds)
        .put("estimatedDistanceMeters", estimatedDistanceMeters)
        .put("blocks", blocks.toPlanBlocksJsonArray())
        .put("actualBlocks", actualBlocks.toPlanBlocksJsonArray())
        .put("uploadedToIntervals", uploadedToIntervals)
}

internal fun RunningWorkoutSession.estimatedDistanceMeters(): Double {
    return actualBlocks.estimatedRunningDistanceMeters()
}

internal fun RunningWorkoutSession.toIntervalsDescription(): String {
    val estimatedDistance = estimatedDistanceMeters()
    return buildString {
        appendLine("IntervalsGym 러닝 수행 기록")
        appendLine("Garmin 원본 기록이 있으면 Garmin 기록을 우선 사용하세요.")
        appendLine("총 수행 시간: ${formatDuration(durationSeconds())}")
        appendLine("Warmup: ${formatClock(warmupSeconds)}")
        if (estimatedDistance > 0.0) {
            appendLine("예상 거리: ${formatDistance(estimatedDistance)}")
        }
        appendLine()
        actualBlocks.forEachIndexed { index, block ->
            val speed = block.runningTargetSpeedText().ifBlank { "-" }
            val incline = block.runningInclineText().ifBlank { "-" }
            appendLine("- Block ${index + 1}: ${block.title}")
            appendLine("  실제 시간: ${formatClock(block.durationSeconds)}, 속도: $speed, 경사도: $incline")
        }
    }
}

private fun List<PlanBlock>.toActualTimeline(): List<PlanBlock> {
    var cursor = 0
    return mapIndexedNotNull { index, block ->
        val duration = block.durationSeconds.coerceAtLeast(0)
        if (duration <= 0) return@mapIndexedNotNull null
        val start = cursor
        cursor += duration
        block.copy(
            index = index,
            durationSeconds = duration,
            startSecond = start,
            endSecond = cursor
        )
    }
}

private fun List<PlanBlock>.normalizedRunningActualBlocks(
    planBlocks: List<PlanBlock>,
    activeDurationSeconds: Int,
): List<PlanBlock> {
    if (isEmpty()) {
        return if (activeDurationSeconds > 0 && planBlocks.isNotEmpty()) {
            planBlocks.scaledToTotalDuration(activeDurationSeconds)
        } else {
            emptyList()
        }
    }
    val planDurationSeconds = planBlocks.sumOf { it.durationSeconds.coerceAtLeast(0) }
    val actualDurationSeconds = sumOf { it.durationSeconds.coerceAtLeast(0) }
    val looksLikePlanFallback = planBlocks.isNotEmpty() &&
        actualDurationSeconds == planDurationSeconds &&
        activeDurationSeconds in 1 until planDurationSeconds &&
        sameRunningTimelineAs(planBlocks)
    return if (looksLikePlanFallback) {
        scaledToTotalDuration(activeDurationSeconds)
    } else {
        toActualTimeline()
    }
}

private fun List<PlanBlock>.sameRunningTimelineAs(other: List<PlanBlock>): Boolean {
    if (size != other.size) return false
    return zip(other).all { (left, right) ->
        left.title == right.title &&
            left.kind == right.kind &&
            left.targetText == right.targetText &&
            left.durationSeconds == right.durationSeconds
    }
}

private fun List<PlanBlock>.scaledToTotalDuration(totalDurationSeconds: Int): List<PlanBlock> {
    val safeTotalDuration = totalDurationSeconds.coerceAtLeast(0)
    val originalTotalDuration = sumOf { it.durationSeconds.coerceAtLeast(0) }
    if (safeTotalDuration <= 0 || originalTotalDuration <= 0) return emptyList()
    var remainingDuration = safeTotalDuration
    return mapIndexedNotNull { index, block ->
        if (remainingDuration <= 0) return@mapIndexedNotNull null
        val originalDuration = block.durationSeconds.coerceAtLeast(0)
        if (originalDuration <= 0) return@mapIndexedNotNull null
        val scaledDuration = if (index == lastIndex) {
            remainingDuration
        } else {
            ((originalDuration.toDouble() / originalTotalDuration.toDouble()) * safeTotalDuration)
                .roundToInt()
                .coerceAtLeast(1)
                .coerceAtMost(remainingDuration)
        }
        remainingDuration -= scaledDuration
        block.copy(durationSeconds = scaledDuration)
    }.toActualTimeline()
}

private fun List<PlanBlock>.estimatedRunningDistanceMeters(): Double {
    return sumOf { block ->
        val speedKmh = block.graphTargetSpeedKmh()?.toDouble() ?: return@sumOf 0.0
        speedKmh * 1000.0 * block.durationSeconds.coerceAtLeast(0).toDouble() / 3600.0
    }
}

private fun buildStrengthTcx(
    name: String,
    startedAt: LocalDateTime,
    durationSeconds: Int,
): String {
    val start = startedAt.atZone(ZoneId.systemDefault()).toInstant()
    val end = start.plusSeconds(durationSeconds.toLong())
    val startText = DateTimeFormatter.ISO_INSTANT.format(start)
    val endText = DateTimeFormatter.ISO_INSTANT.format(end)
    val safeName = name.xmlEscape()

    return """
        <?xml version="1.0" encoding="UTF-8"?>
        <TrainingCenterDatabase xmlns="http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2 http://www.garmin.com/xmlschemas/TrainingCenterDatabasev2.xsd">
          <Activities>
            <Activity Sport="Other">
              <Id>$startText</Id>
              <Lap StartTime="$startText">
                <TotalTimeSeconds>$durationSeconds</TotalTimeSeconds>
                <DistanceMeters>0.0</DistanceMeters>
                <Calories>0</Calories>
                <Intensity>Active</Intensity>
                <TriggerMethod>Manual</TriggerMethod>
                <Track>
                  <Trackpoint>
                    <Time>$startText</Time>
                  </Trackpoint>
                  <Trackpoint>
                    <Time>$endText</Time>
                  </Trackpoint>
                </Track>
              </Lap>
              <Notes>$safeName</Notes>
            </Activity>
          </Activities>
        </TrainingCenterDatabase>
    """.trimIndent()
}

internal fun List<StrengthPlanEntry>.totalDurationSeconds(): Int {
    return sumOf { entry ->
        val setSeconds = entry.records.sumOf { record ->
            record.durationSeconds.toIntOrNull()
                ?: if (record.completed) 45 else 0
        }
        val activeRecords = entry.records.filter { it.completed }.takeIf { it.isNotEmpty() } ?: entry.records
        val restSeconds = activeRecords.dropLast(1).sumOf { record ->
            record.restSeconds.toIntOrNull() ?: entry.restSeconds
        }
        setSeconds + restSeconds
    }
}

internal fun List<StrengthPlanEntry>.totalVolumeKg(): Double {
    return sumOf { entry ->
        entry.records.sumOf { record ->
            val weight = record.weightKg.toDoubleOrNull() ?: entry.targetWeightKg.toDoubleOrNull() ?: 0.0
            val reps = record.reps.toIntOrNull() ?: entry.targetReps
            val sideMultiplier = if (entry.isUnilateral()) 2.0 else 1.0
            if (record.completed || record.weightKg.isNotBlank() || record.reps.isNotBlank()) {
                weight * reps * sideMultiplier
            } else {
                0.0
            }
        }
    }
}

private fun List<StrengthPlanEntry>.strengthTrainingLoad(rpe: Int): Int {
    val durationMinutes = totalDurationSeconds().coerceAtLeast(60) / 60.0
    val volumeKg = totalVolumeKg().coerceAtLeast(0.0)
    val safeRpe = rpe.coerceIn(1, 10)
    return (durationMinutes * safeRpe / 10.0 + sqrt(volumeKg) * 0.15)
        .roundToInt()
        .coerceAtLeast(1)
}

private fun String.xmlEscape(): String {
    return replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}

private fun currentBlockIndex(blocks: List<PlanBlock>, elapsedSeconds: Int): Int {
    if (blocks.isEmpty()) return -1
    if (elapsedSeconds >= blocks.last().endSecond) return -1
    return blocks.indexOfFirst { elapsedSeconds in it.startSecond until it.endSecond }
}

internal fun parseDateTime(value: String?): LocalDateTime? {
    if (value.isNullOrBlank()) return null
    return runCatching {
        LocalDateTime.parse(value.take(19), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }.getOrNull()
}

internal fun JSONObject.optNullableInt(name: String): Int? {
    if (!has(name) || isNull(name)) return null
    return runCatching { optDouble(name).toInt() }.getOrNull()
}

private fun JSONObject.optNullableLong(name: String): Long? {
    if (!has(name) || isNull(name)) return null
    return runCatching { optLong(name) }.getOrNull()
}

internal fun JSONObject.optNullableDouble(name: String): Double? {
    if (!has(name) || isNull(name)) return null
    return runCatching { optDouble(name) }.getOrNull()
}

private fun TrainingCalendarMode.rangeForPage(baseDate: LocalDate, pageOffset: Long): TrainingDateRange {
    return when (this) {
        TrainingCalendarMode.DAY -> {
            val date = baseDate.plusDays(pageOffset)
            TrainingDateRange(start = date, end = date)
        }
        TrainingCalendarMode.WEEK -> {
            val start = baseDate
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .plusWeeks(pageOffset)
            TrainingDateRange(start = start, end = start.plusDays(6))
        }
        TrainingCalendarMode.MONTH -> {
            val start = baseDate
                .withDayOfMonth(1)
                .plusMonths(pageOffset)
            TrainingDateRange(start = start, end = start.withDayOfMonth(start.lengthOfMonth()))
        }
    }
}

private fun TrainingCalendarMode.pageOffsetForDate(baseDate: LocalDate, date: LocalDate): Long {
    return when (this) {
        TrainingCalendarMode.DAY -> ChronoUnit.DAYS.between(baseDate, date)
        TrainingCalendarMode.WEEK -> {
            val baseWeekStart = baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val targetWeekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            ChronoUnit.WEEKS.between(baseWeekStart, targetWeekStart)
        }
        TrainingCalendarMode.MONTH -> {
            val baseMonthStart = baseDate.withDayOfMonth(1)
            val targetMonthStart = date.withDayOfMonth(1)
            ChronoUnit.MONTHS.between(baseMonthStart, targetMonthStart)
        }
    }
}

private fun TrainingCalendarMode.dateLabel(range: TrainingDateRange): String {
    return when (this) {
        TrainingCalendarMode.DAY -> range.start.format(DateTimeFormatter.ofPattern("M/d E", Locale.KOREAN))
        TrainingCalendarMode.WEEK -> "${range.start.monthValue}/${range.start.dayOfMonth} - ${range.end.monthValue}/${range.end.dayOfMonth}"
        TrainingCalendarMode.MONTH -> range.start.format(DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN))
    }
}

private fun TrainingDateRange.days(): List<LocalDate> {
    return (0L..ChronoUnit.DAYS.between(start, end)).map { start.plusDays(it) }
}

private fun TrainingDateRange.monthCalendarDays(): List<LocalDate> {
    val calendarStart = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val calendarEnd = end.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    return (0L..ChronoUnit.DAYS.between(calendarStart, calendarEnd)).map { calendarStart.plusDays(it) }
}

private fun LocalDate.toEpochMillis(): Long {
    return atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun Long.toLocalDateFromMillis(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
}

private fun Long.toLocalDateTime(): LocalDateTime {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
}

internal fun String.urlEncode(): String = URLEncoder.encode(this, StandardCharsets.UTF_8.name())

internal fun formatDistance(meters: Double): String {
    if (meters <= 0.0) return "0 km"
    return String.format(Locale.US, "%.1f km", meters / 1000.0)
}

private fun Double?.formatSummaryMetric(): String {
    val value = this ?: return "-"
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}

internal fun formatDuration(seconds: Int): String {
    if (seconds <= 0) return "0분"
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "${hours}시간 ${minutes}분" else "${minutes}분"
}

internal fun formatClock(seconds: Int): String {
    val safeSeconds = seconds.coerceAtLeast(0)
    val hours = safeSeconds / 3600
    val minutes = (safeSeconds % 3600) / 60
    val secs = safeSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, secs)
    }
}

private fun formatGraphTime(seconds: Int): String {
    val safeSeconds = seconds.coerceAtLeast(0)
    val hours = safeSeconds / 3600
    val minutes = (safeSeconds % 3600) / 60
    val secs = safeSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d", hours, minutes)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, secs)
    }
}

internal fun formatWeight(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}

internal fun Double.roundedKg(): Double {
    return (this * 10.0).roundToInt() / 10.0
}

internal fun formatTargetNumber(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    IntervalsGymTheme {
        LoginScreen(onLogin = {}, onSkipLogin = {})
    }
}

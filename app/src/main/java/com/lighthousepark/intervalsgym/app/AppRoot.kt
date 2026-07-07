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
import android.widget.Toast
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
import org.json.JSONArray

/**
 * App shell state owner.
 * Keep cross-route state and navigation callbacks here; route UI should live in the owning screen composable.
 */
@Composable
internal fun IntervalsGymApp(
    intervalsOAuthCallbackUri: Uri? = null,
    onIntervalsOAuthCallbackConsumed: () -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? ComponentActivity
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val appScope = rememberCoroutineScope()
    var intervalsOAuthToken by remember { mutableStateOf(prefs.getString(INTERVALS_OAUTH_TOKEN_PREF, null).toIntervalsOAuthToken()) }
    var isIntervalsOAuthConnecting by remember { mutableStateOf(false) }
    val intervalsOAuthRepository = remember { IntervalsOAuthRepository() }
    val intervalsAuthCredential = remember(intervalsOAuthToken) {
        intervalsOAuthToken?.accessToken
            ?.takeIf { it.isNotBlank() }
            ?.let(::intervalsBearerCredential)
            .orEmpty()
    }
    var hasSeenIntervalsLoginPrompt by remember {
        mutableStateOf(prefs.getBoolean(INTERVALS_LOGIN_PROMPT_SEEN_PREF, false))
    }
    var selectedRoutineJson by rememberSaveable { mutableStateOf<String?>(null) }
    var completedStrengthHistory by remember { mutableStateOf(loadCompletedStrengthSessionHistory(prefs)) }
    var strengthRoutines by remember {
        mutableStateOf(loadStrengthRoutines(prefs).withLatestCompletedSession(completedStrengthHistory))
    }
    var activeStrengthSession by remember {
        mutableStateOf(loadActiveStrengthSession(prefs)?.withLatestCompletedSession(completedStrengthHistory))
    }
    var selectedStrengthRoutineId by rememberSaveable { mutableStateOf(activeStrengthSession?.routineId) }
    var selectedStrengthRoutineOverrideJson by rememberSaveable { mutableStateOf<String?>(null) }
    var editingStrengthRoutineId by rememberSaveable { mutableStateOf<Int?>(null) }
    var historyStrengthRoutineId by rememberSaveable { mutableStateOf<Int?>(null) }
    var shouldStartStrengthRoutineImmediately by rememberSaveable { mutableStateOf(false) }
    var deletedCalendarRoutineIdList by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var selectedCalendarStrengthRoutineItemJson by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedRoutine = remember(selectedRoutineJson) { selectedRoutineJson.toRouteTrainingItem() }
    val selectedStrengthRoutineOverride = remember(selectedStrengthRoutineOverrideJson) {
        selectedStrengthRoutineOverrideJson.toRouteStrengthRoutine()
    }
    val deletedCalendarRoutineIds = remember(deletedCalendarRoutineIdList) { deletedCalendarRoutineIdList.toSet() }
    val selectedCalendarStrengthRoutineItem = remember(selectedCalendarStrengthRoutineItemJson) {
        selectedCalendarStrengthRoutineItemJson.toRouteTrainingItem()
    }
    val navController = rememberNavController()

    fun setSelectedRoutine(routine: TrainingItem?) {
        selectedRoutineJson = routine.toRouteJson()
    }

    fun setSelectedStrengthRoutineOverride(routine: StrengthWorkoutRoutine?) {
        selectedStrengthRoutineOverrideJson = routine.toRouteJson()
    }

    fun setSelectedCalendarStrengthRoutineItem(item: TrainingItem?) {
        selectedCalendarStrengthRoutineItemJson = item.toRouteJson()
    }

    fun saveStrengthRoutines(routines: List<StrengthWorkoutRoutine>) {
        prefs.edit().putString(STRENGTH_ROUTINES_PREF, routines.toJsonString()).apply()
        strengthRoutines = routines.withLatestCompletedSession(completedStrengthHistory)
    }

    fun refreshStrengthHistory() {
        completedStrengthHistory = loadCompletedStrengthSessionHistory(prefs)
        strengthRoutines = loadStrengthRoutines(prefs).withLatestCompletedSession(completedStrengthHistory)
        activeStrengthSession = activeStrengthSession?.withLatestCompletedSession(completedStrengthHistory)
    }

    fun saveActiveStrengthSession(session: ActiveStrengthSession?) {
        if (session == null) {
            prefs.edit().remove(ACTIVE_STRENGTH_SESSION_PREF).apply()
        } else {
            prefs.edit().putString(ACTIVE_STRENGTH_SESSION_PREF, session.toJsonString()).apply()
        }
        activeStrengthSession = session
    }

    fun updateStrengthRoutineFromWorkout(workout: CompletedStrengthSession) {
        if (workout.routineId == 0) return
        refreshStrengthHistory()
        val nextEntries = workout.entries.map { it.copyForWorkout() }
        saveStrengthRoutines(
            strengthRoutines.map { routine ->
                if (routine.id == workout.routineId) {
                    routine.copy(entries = nextEntries)
                } else {
                    routine
                }
            }
        )
        if (selectedStrengthRoutineId == workout.routineId && selectedStrengthRoutineOverride == null) {
            setSelectedStrengthRoutineOverride(null)
        }
    }

    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun startIntervalsOAuthLogin() {
        if (isIntervalsOAuthConnecting) return
        if (!intervalsOAuthRepository.isConfigured) {
            showToast("Intervals OAuth 설정이 없습니다.")
            return
        }
        val state = intervalsOAuthRepository.newState()
        prefs.edit().putString(INTERVALS_OAUTH_STATE_PREF, state).apply()
        context.startActivity(
            Intent(Intent.ACTION_VIEW, intervalsOAuthRepository.authorizationUri(state))
        )
    }

    fun logoutIntervalsOAuth() {
        prefs.edit()
            .remove(LEGACY_INTERVALS_CREDENTIAL_PREF)
            .remove(INTERVALS_OAUTH_TOKEN_PREF)
            .remove(INTERVALS_OAUTH_STATE_PREF)
            .apply()
        intervalsOAuthToken = null
        showToast("Intervals 로그아웃했습니다.")
    }

    LaunchedEffect(intervalsOAuthCallbackUri) {
        val uri = intervalsOAuthCallbackUri ?: return@LaunchedEffect
        if (!intervalsOAuthRepository.isRedirectUri(uri)) return@LaunchedEffect
        onIntervalsOAuthCallbackConsumed()
        val callback = intervalsOAuthRepository.parseAuthorizationCallback(uri)
        val expectedState = prefs.getString(INTERVALS_OAUTH_STATE_PREF, "").orEmpty()
        when {
            callback.error != null -> {
                showToast("Intervals OAuth 연결이 취소되었습니다.")
            }
            expectedState.isBlank() || callback.state != expectedState -> {
                showToast("Intervals OAuth 상태가 맞지 않습니다.")
            }
            callback.code.isNullOrBlank() -> {
                showToast("Intervals OAuth 인증 코드를 받지 못했습니다.")
            }
            else -> {
                appScope.launch {
                    isIntervalsOAuthConnecting = true
                    try {
                        val token = intervalsOAuthRepository.exchangeAuthorizationCode(callback.code)
                        prefs.edit()
                            .putString(INTERVALS_OAUTH_TOKEN_PREF, token.toJsonString())
                            .remove(LEGACY_INTERVALS_CREDENTIAL_PREF)
                            .remove(INTERVALS_OAUTH_STATE_PREF)
                            .putBoolean(INTERVALS_LOGIN_PROMPT_SEEN_PREF, true)
                            .apply()
                        intervalsOAuthToken = token
                        hasSeenIntervalsLoginPrompt = true
                        showToast("Intervals OAuth 연결 완료")
                        navController.navigate(ROUTE_WEEK) {
                            popUpTo(ROUTE_LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    } catch (error: Exception) {
                        showToast(error.message ?: "Intervals OAuth 연결 실패")
                    } finally {
                        isIntervalsOAuthConnecting = false
                    }
                }
            }
        }
    }

    AppNavGraph(
        navController = navController,
        apiKey = intervalsAuthCredential,
        isIntervalsOAuthConfigured = intervalsOAuthRepository.isConfigured,
        intervalsOAuthConnectedLabel = intervalsOAuthToken?.athleteName ?: intervalsOAuthToken?.athleteId?.let { "Athlete $it" },
        isIntervalsOAuthConnecting = isIntervalsOAuthConnecting,
        hasActiveStrengthSession = activeStrengthSession != null,
        shouldShowInitialLogin = intervalsAuthCredential.isBlank() && !hasSeenIntervalsLoginPrompt,
        onOAuthLogin = ::startIntervalsOAuthLogin,
        onSkipLogin = {
            prefs.edit()
                .remove(LEGACY_INTERVALS_CREDENTIAL_PREF)
                .putBoolean(INTERVALS_LOGIN_PROMPT_SEEN_PREF, true)
                .apply()
            hasSeenIntervalsLoginPrompt = true
            setSelectedRoutine(null)
            navController.navigate(ROUTE_WEEK) {
                popUpTo(ROUTE_LOGIN) { inclusive = true }
                launchSingleTop = true
            }
        },
        selectedRoutine = selectedRoutine,
        deletedCalendarRoutineIds = deletedCalendarRoutineIds,
        selectedCalendarStrengthRoutineItem = selectedCalendarStrengthRoutineItem,
        onRoutineSelected = { routine ->
            setSelectedRoutine(routine)
            navController.navigate(ROUTE_WORKOUT_ROUTINE)
        },
        onCalendarRoutineDeleted = { routine ->
            deletedCalendarRoutineIdList = (deletedCalendarRoutineIdList + routine.id + routine.remoteId).distinct()
            setSelectedRoutine(null)
            setSelectedCalendarStrengthRoutineItem(null)
            navController.popBackStack()
        },
        onStrengthSessionUploaded = { uploadedSession ->
            replaceStrengthSessionHistory(prefs, uploadedSession.copy(uploadedToIntervals = true))
            refreshStrengthHistory()
            setSelectedRoutine(selectedRoutine?.let { selected ->
                if (selected.matchedStrengthSession?.id == uploadedSession.id) {
                    selected.copy(matchedStrengthSession = uploadedSession.copy(uploadedToIntervals = true))
                } else {
                    selected
                }
            })
        },
        onIntervalStrengthRoutineSelected = { calendarItem, routine ->
            saveActiveStrengthSession(null)
            setSelectedCalendarStrengthRoutineItem(calendarItem)
            setSelectedStrengthRoutineOverride(routine)
            selectedStrengthRoutineId = null
            navController.navigate(ROUTE_STRENGTH_SESSION)
        },
        onMonthDaySelected = { date ->
            navController.navigate(trainingDayRoute(date))
        },
        onStrengthSession = {
            navController.navigate(
                if (activeStrengthSession != null) ROUTE_STRENGTH_SESSION else ROUTE_STRENGTH_ROUTINES
            )
        },
        onRunningSession = {
            navController.navigate(ROUTE_RUNNING_ROUTINES)
        },
        strengthRoutines = strengthRoutines,
        activeStrengthSession = activeStrengthSession,
        selectedStrengthRoutineId = selectedStrengthRoutineId,
        selectedStrengthRoutineOverride = selectedStrengthRoutineOverride,
        editingStrengthRoutineId = editingStrengthRoutineId,
        historyStrengthRoutineId = historyStrengthRoutineId,
        onManageStrengthRoutines = {
            navController.navigate(ROUTE_STRENGTH_MANAGE)
        },
        onStrengthRoutineSelected = { routine ->
            saveActiveStrengthSession(null)
            setSelectedCalendarStrengthRoutineItem(null)
            shouldStartStrengthRoutineImmediately = false
            setSelectedStrengthRoutineOverride(null)
            selectedStrengthRoutineId = routine.id
            navController.navigate(ROUTE_STRENGTH_SESSION)
        },
        onStartStrengthRoutineImmediately = { routine ->
            saveActiveStrengthSession(null)
            setSelectedCalendarStrengthRoutineItem(null)
            shouldStartStrengthRoutineImmediately = true
            setSelectedStrengthRoutineOverride(null)
            selectedStrengthRoutineId = routine.id
            navController.navigate(ROUTE_STRENGTH_SESSION)
        },
        onStrengthRoutineHistory = { routine ->
            historyStrengthRoutineId = routine.id
            navController.navigate(ROUTE_STRENGTH_HISTORY)
        },
        onStrengthHistorySelected = { workout ->
            saveActiveStrengthSession(null)
            setSelectedCalendarStrengthRoutineItem(null)
            selectedStrengthRoutineId = workout.routineId
            setSelectedStrengthRoutineOverride(
                StrengthWorkoutRoutine(
                    id = workout.routineId,
                    name = workout.routineName,
                    entries = workout.entries.map { it.copyForWorkout() }
                )
            )
            navController.navigate(ROUTE_STRENGTH_SESSION) {
                popUpTo(ROUTE_STRENGTH_ROUTINES)
            }
        },
        onAddStrengthRoutine = {
            editingStrengthRoutineId = null
            navController.navigate(ROUTE_STRENGTH_ROUTINE_EDIT)
        },
        onEditStrengthRoutine = { routine ->
            editingStrengthRoutineId = routine.id
            navController.navigate(ROUTE_STRENGTH_ROUTINE_EDIT)
        },
        onSaveStrengthRoutine = { routine ->
            val savedRoutine = if (routine.id == 0) {
                val nextId = nextStrengthWorkoutRoutineId(
                    routines = strengthRoutines,
                    history = completedStrengthHistory,
                    scheduledRoutines = loadScheduledStrengthRoutines(prefs),
                    activeSession = activeStrengthSession,
                    reservedIds = listOfNotNull(selectedStrengthRoutineId, selectedStrengthRoutineOverride?.id, editingStrengthRoutineId)
                )
                routine.copy(id = nextId)
            } else {
                routine
            }
            val nextRoutines = when {
                routine.id == 0 -> strengthRoutines + savedRoutine
                strengthRoutines.any { it.id == routine.id } ->
                    strengthRoutines.map { if (it.id == routine.id) savedRoutine else it }
                else -> strengthRoutines + savedRoutine
            }
            saveStrengthRoutines(nextRoutines)
            if (selectedStrengthRoutineOverride?.id == routine.id) {
                setSelectedStrengthRoutineOverride(savedRoutine)
            }
            if (selectedStrengthRoutineId == routine.id) {
                selectedStrengthRoutineId = savedRoutine.id
            }
            if (editingStrengthRoutineId == routine.id) {
                editingStrengthRoutineId = savedRoutine.id
            }
            navController.popBackStack()
        },
        onDeleteStrengthRoutine = { routine ->
            saveStrengthRoutines(strengthRoutines.filterNot { it.id == routine.id })
            if (selectedStrengthRoutineId == routine.id) selectedStrengthRoutineId = null
            if (activeStrengthSession?.routineId == routine.id) saveActiveStrengthSession(null)
            val previousRoute = navController.previousBackStackEntry?.destination?.route
            if (previousRoute == ROUTE_STRENGTH_SESSION) {
                navController.popBackStack(ROUTE_STRENGTH_ROUTINES, inclusive = false)
            } else {
                navController.popBackStack()
            }
        },
        onActiveStrengthSessionChange = ::saveActiveStrengthSession,
        onActiveStrengthSessionFinished = { workout, applyToRoutine ->
            workout?.let {
                if (applyToRoutine && it.appliedToRoutine) {
                    updateStrengthRoutineFromWorkout(it)
                } else {
                    refreshStrengthHistory()
                }
            }
            saveActiveStrengthSession(null)
            shouldStartStrengthRoutineImmediately = false
        },
        shouldStartStrengthRoutineImmediately = shouldStartStrengthRoutineImmediately,
        onImmediateStrengthRoutineStartConsumed = {
            shouldStartStrengthRoutineImmediately = false
        },
        onNavigateBack = {
            if (!navController.popBackStack()) {
                activity?.moveTaskToBack(true)
            }
        },
        onLoginClick = {
            startIntervalsOAuthLogin()
        },
        onLogout = {
            logoutIntervalsOAuth()
            setSelectedRoutine(null)
            setSelectedCalendarStrengthRoutineItem(null)
            deletedCalendarRoutineIdList = emptyList()
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
    isIntervalsOAuthConfigured: Boolean,
    intervalsOAuthConnectedLabel: String?,
    isIntervalsOAuthConnecting: Boolean,
    hasActiveStrengthSession: Boolean,
    shouldShowInitialLogin: Boolean,
    selectedRoutine: TrainingItem?,
    deletedCalendarRoutineIds: Set<String>,
    selectedCalendarStrengthRoutineItem: TrainingItem?,
    onOAuthLogin: () -> Unit,
    onSkipLogin: () -> Unit,
    onRoutineSelected: (TrainingItem) -> Unit,
    onCalendarRoutineDeleted: (TrainingItem) -> Unit,
    onStrengthSessionUploaded: (CompletedStrengthSession) -> Unit,
    onIntervalStrengthRoutineSelected: (TrainingItem?, StrengthWorkoutRoutine) -> Unit,
    onMonthDaySelected: (LocalDate) -> Unit,
    onStrengthSession: () -> Unit,
    onRunningSession: () -> Unit,
    strengthRoutines: List<StrengthWorkoutRoutine>,
    activeStrengthSession: ActiveStrengthSession?,
    selectedStrengthRoutineId: Int?,
    selectedStrengthRoutineOverride: StrengthWorkoutRoutine?,
    editingStrengthRoutineId: Int?,
    historyStrengthRoutineId: Int?,
    onManageStrengthRoutines: () -> Unit,
    onStrengthRoutineSelected: (StrengthWorkoutRoutine) -> Unit,
    onStartStrengthRoutineImmediately: (StrengthWorkoutRoutine) -> Unit,
    onStrengthRoutineHistory: (StrengthWorkoutRoutine) -> Unit,
    onStrengthHistorySelected: (CompletedStrengthSession) -> Unit,
    onAddStrengthRoutine: () -> Unit,
    onEditStrengthRoutine: (StrengthWorkoutRoutine) -> Unit,
    onSaveStrengthRoutine: (StrengthWorkoutRoutine) -> Unit,
    onDeleteStrengthRoutine: (StrengthWorkoutRoutine) -> Unit,
    onActiveStrengthSessionChange: (ActiveStrengthSession?) -> Unit,
    onActiveStrengthSessionFinished: (CompletedStrengthSession?, Boolean) -> Unit,
    shouldStartStrengthRoutineImmediately: Boolean,
    onImmediateStrengthRoutineStartConsumed: () -> Unit,
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
                onOAuthLogin = onOAuthLogin,
                onSkipLogin = onSkipLogin,
                isOAuthConfigured = isIntervalsOAuthConfigured,
                isOAuthConnecting = isIntervalsOAuthConnecting
            )
        }
        composable(ROUTE_WEEK) {
            WeeklyTrainingScreen(
                apiKey = apiKey,
                strengthRoutines = strengthRoutines,
                deletedCalendarRoutineIds = deletedCalendarRoutineIds,
                onRoutineSelected = onRoutineSelected,
                onIntervalStrengthRoutineSelected = onIntervalStrengthRoutineSelected,
                onMonthDaySelected = onMonthDaySelected,
                onManageRoutines = onManageStrengthRoutines,
                onStrengthSession = onStrengthSession,
                onRunningSession = onRunningSession,
                onLoginClick = onLoginClick,
                onLogout = onLogout,
                isIntervalsOAuthConfigured = isIntervalsOAuthConfigured,
                intervalsOAuthConnectedLabel = intervalsOAuthConnectedLabel,
                isIntervalsOAuthConnecting = isIntervalsOAuthConnecting
            )
        }
        composable("$ROUTE_TRAINING_DAY/{date}") { backStackEntry ->
            val selectedDate = backStackEntry.arguments
                ?.getString("date")
                ?.let { raw -> runCatching { LocalDate.parse(raw) }.getOrNull() }
                ?: LocalDate.now()
            WeeklyTrainingScreen(
                apiKey = apiKey,
                strengthRoutines = strengthRoutines,
                deletedCalendarRoutineIds = deletedCalendarRoutineIds,
                initialDate = selectedDate,
                initialCalendarMode = TrainingCalendarMode.DAY,
                showBackButton = true,
                showCalendarModeButton = false,
                onRoutineSelected = onRoutineSelected,
                onIntervalStrengthRoutineSelected = onIntervalStrengthRoutineSelected,
                onManageRoutines = onManageStrengthRoutines,
                onStrengthSession = onStrengthSession,
                onRunningSession = onRunningSession,
                onLoginClick = onLoginClick,
                onLogout = onLogout,
                isIntervalsOAuthConfigured = isIntervalsOAuthConfigured,
                intervalsOAuthConnectedLabel = intervalsOAuthConnectedLabel,
                isIntervalsOAuthConnecting = isIntervalsOAuthConnecting,
                onBack = onNavigateBack
            )
        }
        composable(ROUTE_RUNNING_ROUTINES) {
            RunningRoutineListScreen(
                onRoutineSelected = { routine -> onRoutineSelected(routine.toTrainingItem()) },
                onManageRoutines = { navController.navigate(ROUTE_RUNNING_MANAGE) },
                onBack = onNavigateBack
            )
        }
        composable(ROUTE_RUNNING_MANAGE) {
            RunningRoutineManagementScreen(
                onBack = onNavigateBack
            )
        }
        composable(ROUTE_WORKOUT_ROUTINE) {
            WorkoutRoutineScreen(
                apiKey = apiKey,
                routine = selectedRoutine,
                onStartStrengthRoutine = { routine -> onIntervalStrengthRoutineSelected(null, routine) },
                onStrengthSessionUploaded = onStrengthSessionUploaded,
                onRoutineDeleted = onCalendarRoutineDeleted,
                onBack = onNavigateBack
            )
        }
        composable(ROUTE_STRENGTH_ROUTINES) {
            StrengthRoutineListScreen(
                routines = strengthRoutines,
                onRoutineSelected = onStrengthRoutineSelected,
                onStartRoutine = onStartStrengthRoutineImmediately,
                onManageRoutines = onManageStrengthRoutines,
                onBack = onNavigateBack
            )
        }
        composable(ROUTE_STRENGTH_MANAGE) {
            StrengthRoutineManagementScreen(
                routines = strengthRoutines,
                onAddRoutine = onAddStrengthRoutine,
                onEditRoutine = onEditStrengthRoutine,
                onBack = onNavigateBack
            )
        }
        composable(ROUTE_STRENGTH_ROUTINE_EDIT) {
            StrengthRoutineEditScreen(
                routine = strengthRoutines.firstOrNull { it.id == editingStrengthRoutineId }
                    ?: selectedStrengthRoutineOverride?.takeIf { it.id == editingStrengthRoutineId },
                onSave = onSaveStrengthRoutine,
                onDelete = onDeleteStrengthRoutine,
                onBack = onNavigateBack
            )
        }
        composable(ROUTE_STRENGTH_HISTORY) {
            val targetRoutine = strengthRoutines.firstOrNull { it.id == historyStrengthRoutineId }
                ?: selectedStrengthRoutineOverride?.takeIf { it.id == historyStrengthRoutineId }
            StrengthRoutineHistoryScreen(
                routine = targetRoutine,
                history = loadCompletedStrengthSessionHistory(
                    LocalContext.current.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                ),
                onHistorySelected = onStrengthHistorySelected,
                onBack = onNavigateBack
            )
        }
        composable(ROUTE_STRENGTH_SESSION) {
            val sessionRoutine = activeStrengthSession?.toWorkoutRoutine()
                ?: selectedStrengthRoutineOverride
                ?: strengthRoutines.firstOrNull { it.id == selectedStrengthRoutineId }
            val canEditSessionRoutine = activeStrengthSession == null && sessionRoutine != null &&
                (
                    selectedStrengthRoutineOverride?.id == sessionRoutine.id ||
                        selectedStrengthRoutineId?.let { routineId ->
                            strengthRoutines.any { it.id == routineId }
                        } == true
                    )
            StrengthSessionScreen(
                apiKey = apiKey,
                routine = sessionRoutine,
                calendarRoutineItem = selectedCalendarStrengthRoutineItem,
                isRoutineEditable = canEditSessionRoutine,
                activeSession = activeStrengthSession,
                startImmediately = shouldStartStrengthRoutineImmediately,
                onImmediateStartConsumed = onImmediateStrengthRoutineStartConsumed,
                onSessionChange = onActiveStrengthSessionChange,
                onSessionFinished = onActiveStrengthSessionFinished,
                onHistoryClick = onStrengthRoutineHistory,
                onEditRoutine = onEditStrengthRoutine,
                onCalendarRoutineDeleted = onCalendarRoutineDeleted,
                onBack = onNavigateBack
            )
        }
    }
}

private fun TrainingItem?.toRouteJson(): String? {
    return this?.let { item -> listOf(item).toTrainingItemsJsonArray().toString() }
}

private fun String?.toRouteTrainingItem(): TrainingItem? {
    if (isNullOrBlank()) return null
    return runCatching {
        JSONArray(this).toCachedTrainingItems().firstOrNull()
    }.getOrNull()
}

private fun StrengthWorkoutRoutine?.toRouteJson(): String? {
    return this?.let { routine -> listOf(routine).toJsonString() }
}

private fun String?.toRouteStrengthRoutine(): StrengthWorkoutRoutine? {
    return toStrengthWorkoutRoutines().firstOrNull()
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

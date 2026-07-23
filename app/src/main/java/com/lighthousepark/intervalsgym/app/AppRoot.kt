package com.lighthousepark.intervalsgym.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.lighthousepark.intervalsgym.data.IntervalsOAuthSessionStorage
import com.lighthousepark.intervalsgym.data.IntervalsOAuthRepository
import com.lighthousepark.intervalsgym.data.StrengthAppStateStorageUseCase
import com.lighthousepark.intervalsgym.data.intervalsBearerCredential
import com.lighthousepark.intervalsgym.core.localizedAppText
import com.lighthousepark.intervalsgym.strength.ActiveStrengthSession
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.clonedForLocalLibrary
import com.lighthousepark.intervalsgym.strength.copyForLocalLibrary
import com.lighthousepark.intervalsgym.training.TrainingItem
import kotlinx.coroutines.launch

/**
 * App shell state owner.
 * Keep cross-route state and navigation callbacks here; route UI should live in the owning screen composable.
 */
@Composable
internal fun IntervalsGymApp(
    intervalsOAuthCallbackUri: Uri? = null,
    onIntervalsOAuthCallbackConsumed: () -> Unit = {},
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val appScope = rememberCoroutineScope()
    val intervalsOAuthSessionStorage = remember(prefs) { IntervalsOAuthSessionStorage(prefs) }
    var intervalsOAuthToken by remember { mutableStateOf(intervalsOAuthSessionStorage.loadToken()) }
    var isIntervalsOAuthConnecting by remember { mutableStateOf(false) }
    val intervalsOAuthRepository = remember { IntervalsOAuthRepository() }
    val intervalsAuthCredential = remember(intervalsOAuthToken) {
        intervalsOAuthToken?.accessToken
            ?.takeIf { it.isNotBlank() }
            ?.let(::intervalsBearerCredential)
            .orEmpty()
    }
    var hasSeenIntervalsLoginPrompt by remember {
        mutableStateOf(intervalsOAuthSessionStorage.hasSeenLoginPrompt())
    }
    var selectedRoutineJson by rememberSaveable { mutableStateOf<String?>(null) }
    val strengthAppStateStorage = remember(prefs) { StrengthAppStateStorageUseCase(prefs) }
    val initialStrengthAppState = remember(strengthAppStateStorage) { strengthAppStateStorage.loadSnapshot() }
    var completedStrengthHistory by remember {
        mutableStateOf(initialStrengthAppState.completedStrengthHistory)
    }
    var strengthRoutines by remember {
        mutableStateOf(initialStrengthAppState.routines)
    }
    var activeStrengthSession by remember {
        mutableStateOf(initialStrengthAppState.activeSession)
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

    fun persistStrengthRoutines(routines: List<StrengthWorkoutRoutine>) {
        strengthRoutines = strengthAppStateStorage.saveStrengthRoutines(
            routines = routines,
            completedHistory = completedStrengthHistory
        )
    }

    fun refreshStrengthHistory() {
        val snapshot = strengthAppStateStorage.loadSnapshot(activeStrengthSession)
        completedStrengthHistory = snapshot.completedStrengthHistory
        strengthRoutines = snapshot.routines
        activeStrengthSession = snapshot.activeSession
    }

    fun persistActiveStrengthSession(session: ActiveStrengthSession?) {
        strengthAppStateStorage.saveActiveSession(session)
        activeStrengthSession = session
    }

    fun updateStrengthRoutineFromWorkout(workout: CompletedStrengthSession) {
        if (workout.routineId == 0) return
        refreshStrengthHistory()
        persistStrengthRoutines(strengthRoutines.withWorkoutResultApplied(workout))
        if (selectedStrengthRoutineId == workout.routineId && selectedStrengthRoutineOverride == null) {
            setSelectedStrengthRoutineOverride(null)
        }
    }

    fun showToast(message: String) {
        Toast.makeText(context, context.localizedAppText(message), Toast.LENGTH_SHORT).show()
    }

    fun startIntervalsOAuthLogin() {
        if (isIntervalsOAuthConnecting) return
        if (!intervalsOAuthRepository.isConfigured) {
            showToast("Intervals OAuth 설정이 없습니다.")
            return
        }
        val state = intervalsOAuthRepository.newState()
        intervalsOAuthSessionStorage.savePendingState(state)
        context.startActivity(
            Intent(Intent.ACTION_VIEW, intervalsOAuthRepository.authorizationUri(state))
        )
    }

    fun logoutIntervalsOAuth() {
        intervalsOAuthSessionStorage.clearConnectedToken()
        intervalsOAuthToken = null
        showToast("Intervals 로그아웃했습니다.")
    }

    LaunchedEffect(intervalsOAuthCallbackUri) {
        val uri = intervalsOAuthCallbackUri ?: return@LaunchedEffect
        if (!intervalsOAuthRepository.isRedirectUri(uri)) return@LaunchedEffect
        onIntervalsOAuthCallbackConsumed()
        val callback = intervalsOAuthRepository.parseAuthorizationCallback(uri)
        val expectedState = intervalsOAuthSessionStorage.loadPendingState()
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
                        intervalsOAuthSessionStorage.saveConnectedToken(token)
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
            intervalsOAuthSessionStorage.markLoginPromptSeen()
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
            deletedCalendarRoutineIdList = deletedCalendarRoutineIdList.withDeletedCalendarRoutineIds(routine)
            setSelectedRoutine(null)
            setSelectedCalendarStrengthRoutineItem(null)
            navController.popBackStack()
        },
        onStrengthSessionUploaded = { uploadedSession ->
            val syncedSession = uploadedSession.copy(uploadedToIntervals = true)
            refreshStrengthHistory()
            setSelectedRoutine(selectedRoutine?.let { selected ->
                if (selected.matchedStrengthSession?.id == syncedSession.id) {
                    selected.copy(matchedStrengthSession = syncedSession)
                } else {
                    selected
                }
            })
        },
        onIntervalStrengthRoutineSelected = { calendarItem, routine ->
            persistActiveStrengthSession(null)
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
        completedStrengthHistory = completedStrengthHistory,
        activeStrengthSession = activeStrengthSession,
        selectedStrengthRoutineId = selectedStrengthRoutineId,
        selectedStrengthRoutineOverride = selectedStrengthRoutineOverride,
        editingStrengthRoutineId = editingStrengthRoutineId,
        historyStrengthRoutineId = historyStrengthRoutineId,
        onManageStrengthRoutines = {
            navController.navigate(ROUTE_STRENGTH_MANAGE)
        },
        onStrengthRoutineSelected = { routine ->
            persistActiveStrengthSession(null)
            setSelectedCalendarStrengthRoutineItem(null)
            shouldStartStrengthRoutineImmediately = false
            setSelectedStrengthRoutineOverride(null)
            selectedStrengthRoutineId = routine.id
            navController.navigate(ROUTE_STRENGTH_SESSION)
        },
        onStartStrengthRoutineImmediately = { routine ->
            persistActiveStrengthSession(null)
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
        onSaveIntervalStrengthRoutineLocally = { routine ->
            val targetId = if (strengthRoutines.none { it.id == routine.id }) {
                routine.id
            } else {
                strengthAppStateStorage.nextStrengthRoutineId(
                    routines = strengthRoutines,
                    completedHistory = completedStrengthHistory,
                    activeSession = activeStrengthSession,
                    reservedIds = listOfNotNull(
                        selectedStrengthRoutineId,
                        selectedStrengthRoutineOverride?.id,
                        editingStrengthRoutineId
                    )
                )
            }
            val savedRoutine = routine.copyForLocalLibrary(targetId)
            persistStrengthRoutines(strengthRoutines + savedRoutine)
            setSelectedRoutine(selectedRoutine?.copy(matchedStrengthRoutine = savedRoutine))
        },
        onStrengthHistorySelected = { workout ->
            persistActiveStrengthSession(null)
            setSelectedCalendarStrengthRoutineItem(null)
            selectedStrengthRoutineId = workout.routineId
            setSelectedStrengthRoutineOverride(workout.toRouteStrengthRoutineOverride())
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
        onCloneStrengthRoutine = { routine ->
            val newRoutineId = strengthAppStateStorage.nextStrengthRoutineId(
                routines = strengthRoutines,
                completedHistory = completedStrengthHistory,
                activeSession = activeStrengthSession,
                reservedIds = listOfNotNull(
                    selectedStrengthRoutineId,
                    selectedStrengthRoutineOverride?.id,
                    editingStrengthRoutineId
                )
            )
            persistStrengthRoutines(
                strengthRoutines + routine.clonedForLocalLibrary(newRoutineId, strengthRoutines)
            )
        },
        onSaveStrengthRoutine = { routine ->
            val newRoutineId = if (routine.id == 0) {
                strengthAppStateStorage.nextStrengthRoutineId(
                    routines = strengthRoutines,
                    completedHistory = completedStrengthHistory,
                    activeSession = activeStrengthSession,
                    reservedIds = listOfNotNull(selectedStrengthRoutineId, selectedStrengthRoutineOverride?.id, editingStrengthRoutineId)
                )
            } else {
                routine.id
            }
            val saveResult = appStrengthRoutineSaveResult(
                routine = routine,
                newRoutineId = newRoutineId,
                currentRoutines = strengthRoutines,
                selectedStrengthRoutineId = selectedStrengthRoutineId,
                selectedStrengthRoutineOverride = selectedStrengthRoutineOverride,
                editingStrengthRoutineId = editingStrengthRoutineId
            )
            persistStrengthRoutines(saveResult.routines)
            selectedStrengthRoutineId = saveResult.selectedStrengthRoutineId
            editingStrengthRoutineId = saveResult.editingStrengthRoutineId
            setSelectedStrengthRoutineOverride(saveResult.selectedStrengthRoutineOverride)
            navController.popBackStack()
        },
        onDeleteStrengthRoutine = { routine ->
            persistStrengthRoutines(strengthRoutines.withoutStrengthRoutine(routine))
            selectedStrengthRoutineId = selectedStrengthRoutineId.withoutDeletedStrengthRoutine(routine)
            if (activeStrengthSession.isForRoutine(routine)) persistActiveStrengthSession(null)
            val previousRoute = navController.previousBackStackEntry?.destination?.route
            if (previousRoute == ROUTE_STRENGTH_SESSION) {
                navController.popBackStack(ROUTE_STRENGTH_ROUTINES, inclusive = false)
            } else {
                navController.popBackStack()
            }
        },
        onActiveStrengthSessionChange = ::persistActiveStrengthSession,
        onActiveStrengthSessionFinished = { workout, applyToRoutine ->
            workout?.let {
                if (applyToRoutine && it.appliedToRoutine) {
                    updateStrengthRoutineFromWorkout(it)
                } else {
                    refreshStrengthHistory()
                }
            }
            persistActiveStrengthSession(null)
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

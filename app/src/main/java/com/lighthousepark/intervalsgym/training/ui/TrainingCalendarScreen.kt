package com.lighthousepark.intervalsgym.training.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.lighthousepark.intervalsgym.app.PREFS_NAME
import com.lighthousepark.intervalsgym.app.ROUTE_WEEK
import com.lighthousepark.intervalsgym.core.localizedAppText
import com.lighthousepark.intervalsgym.data.IntervalsUseCaseFactory
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.training.PendingCalendarRoutineMove
import com.lighthousepark.intervalsgym.training.TrainingCalendarMode
import com.lighthousepark.intervalsgym.training.TrainingDateRange
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.pageOffsetForDate
import com.lighthousepark.intervalsgym.training.rangeForPage
import com.lighthousepark.intervalsgym.training.withoutReflectedMoves
import java.time.LocalDate
import kotlinx.coroutines.launch

/**
 * Route owner for [ROUTE_WEEK].
 * This owns day/week/month training calendar UI, local/Intervals merge display, and the main FAB actions.
 * UI tests: TrainingCalendarUiTest.weeklyTrainingScreen_settingsLoginActionInvokesLoginWhenConfigured,
 * weeklyTrainingScreen_settingsAuthActionInvokesLogoutWhenConnected,
 * weeklyTrainingScreen_settingsRefreshActionClosesMenu,
 * weeklyTrainingScreen_settingsAuthActionDisabledWhenOAuthIsUnavailable,
 * weeklyTrainingScreen_backButtonInvokesBackCallback,
 * weeklyTrainingScreen_calendarModeButtonCyclesTitle.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun WeeklyTrainingScreen(
    apiKey: String,
    strengthRoutines: List<StrengthWorkoutRoutine>,
    deletedCalendarRoutineIds: Set<String>,
    initialDate: LocalDate = LocalDate.now(),
    initialCalendarMode: TrainingCalendarMode = TrainingCalendarMode.WEEK,
    showBackButton: Boolean = false,
    showCalendarModeButton: Boolean = true,
    onRoutineSelected: (TrainingItem) -> Unit,
    onIntervalStrengthRoutineSelected: (TrainingItem?, StrengthWorkoutRoutine) -> Unit,
    onMonthDaySelected: (LocalDate) -> Unit = {},
    onManageRoutines: () -> Unit,
    onStrengthSession: () -> Unit,
    onRunningSession: () -> Unit,
    onLoginClick: () -> Unit,
    onLogout: () -> Unit,
    isIntervalsOAuthConfigured: Boolean = false,
    intervalsOAuthConnectedLabel: String? = null,
    isIntervalsOAuthConnecting: Boolean = false,
    onBack: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val intervalsUseCaseFactory = remember(apiKey) { IntervalsUseCaseFactory(apiKey) }
    val calendarRoutineSync = remember(intervalsUseCaseFactory, prefs) {
        intervalsUseCaseFactory.calendarRoutineSync(prefs)
    }
    val calendarDataUseCase = remember(intervalsUseCaseFactory, prefs) {
        intervalsUseCaseFactory.trainingCalendarData(prefs)
    }
    var localSnapshot by remember(calendarDataUseCase) {
        mutableStateOf(calendarDataUseCase.loadLocalSnapshot())
    }
    val baseDate = remember(initialDate) { initialDate }
    val today = remember { LocalDate.now() }
    val initialPage = remember { Int.MAX_VALUE / 2 }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { Int.MAX_VALUE })
    var calendarMode by rememberSaveable(initialCalendarMode) { mutableStateOf(initialCalendarMode) }
    val selectedRange = calendarMode.rangeForPage(baseDate, (pagerState.settledPage - initialPage).toLong())
    var state by remember {
        mutableStateOf(
            WeekUiState(
                weekStart = selectedRange.start,
                weekEnd = selectedRange.end,
                isLoading = apiKey.isNotBlank()
            )
        )
    }
    var showCalendar by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showFabActions by remember { mutableStateOf(false) }
    var showWorkoutActionSheet by remember { mutableStateOf(false) }
    var routineSaveUiState by rememberSaveable(baseDate, saver = trainingRoutineSaveUiStateSaver()) {
        mutableStateOf(TrainingRoutineSaveUiState.initial(baseDate))
    }
    var didInitialIntervalsSync by rememberSaveable(apiKey) { mutableStateOf(false) }
    var pendingCalendarRoutineMoves by remember(apiKey) { mutableStateOf<Map<String, PendingCalendarRoutineMove>>(emptyMap()) }
    var optimisticallyDeletedCalendarRoutineKeys by remember(apiKey) { mutableStateOf(emptySet<String>()) }
    var calendarDragUiState by remember { mutableStateOf(TrainingCalendarDragUiState()) }
    val isCalendarRoutineDragging = calendarDragUiState.isDragging

    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, context.localizedAppText(message), duration).show()
    }

    fun refresh(
        targetRange: TrainingDateRange = selectedRange,
        forceSync: Boolean = false,
    ) {
        val initialLoad = calendarDataUseCase.initialLoad(
            range = targetRange,
            forceSync = forceSync
        )
        localSnapshot = initialLoad.localSnapshot
        state = state.withTrainingCalendarInitialLoad(
            range = targetRange,
            load = initialLoad
        )
        if (!initialLoad.shouldFetchRemote) {
            return
        }

        scope.launch {
            try {
                val data = calendarDataUseCase.fetchRemoteWeek(range = targetRange)
                pendingCalendarRoutineMoves = pendingCalendarRoutineMoves.values
                    .withoutReflectedMoves(data.routines)
                val visibleRange = calendarMode.rangeForPage(
                    baseDate,
                    (pagerState.settledPage - initialPage).toLong()
                )
                if (visibleRange != targetRange) return@launch
                localSnapshot = calendarDataUseCase.loadLocalSnapshot()
                state = state.withFetchedRemoteData(
                    range = targetRange,
                    data = data
                )
            } catch (error: Exception) {
                val visibleRange = calendarMode.rangeForPage(
                    baseDate,
                    (pagerState.settledPage - initialPage).toLong()
                )
                if (visibleRange != targetRange) return@launch
                state = state.withRemoteFetchFailed(
                    range = targetRange,
                    cachedRemoteData = initialLoad.cachedRemoteData,
                    errorMessage = error.message
                )
            }
        }
    }

    fun selectedRoutineDate(): LocalDate {
        return if (!baseDate.isBefore(selectedRange.start) && !baseDate.isAfter(selectedRange.end)) {
            baseDate
        } else {
            selectedRange.start
        }
    }

    fun openRoutineSaveSheet(targetDate: LocalDate) {
        showFabActions = false
        routineSaveUiState = routineSaveUiState.open(targetDate)
    }

    fun saveRoutineToCalendar(routine: StrengthWorkoutRoutine, targetDate: LocalDate) {
        val savePlan = planTrainingCalendarRoutineSave(
            routine = routine,
            targetDate = targetDate,
            isRemoteConnected = apiKey.isNotBlank()
        )
        val localRoutine = savePlan.saveLocally(calendarRoutineSync)
        localSnapshot = calendarDataUseCase.loadLocalSnapshot()
        if (!savePlan.requiresRemoteUpload) {
            routineSaveUiState = routineSaveUiState.withLocalSaved(savePlan.targetDate)
            return
        }

        routineSaveUiState = routineSaveUiState.withUploadStarted(savePlan.routineId)
        scope.launch {
            try {
                savePlan.upload(calendarRoutineSync, localRoutine)
                localSnapshot = calendarDataUseCase.loadLocalSnapshot()
                routineSaveUiState = routineSaveUiState.withUploadSucceeded(savePlan.targetDate)
                refresh(selectedRange, forceSync = true)
            } catch (error: Exception) {
                routineSaveUiState = routineSaveUiState.withUploadFailed(
                    targetDate = savePlan.targetDate,
                    errorMessage = error.message
                )
            }
        }
    }

    fun moveRoutineToDate(item: TrainingItem, targetDate: LocalDate) {
        val movePlan = when (
            val decision = planTrainingCalendarRoutineMove(
                item = item,
                targetDate = targetDate,
                pendingCalendarRoutineMoves = pendingCalendarRoutineMoves,
                isRemoteConnected = apiKey.isNotBlank()
            )
        ) {
            TrainingCalendarRoutineMoveDecision.Ignore -> return
            is TrainingCalendarRoutineMoveDecision.Blocked -> {
                showToast(decision.message)
                return
            }
            is TrainingCalendarRoutineMoveDecision.Move -> decision.plan
        }
        pendingCalendarRoutineMoves = movePlan.pendingCalendarRoutineMoves
        val movedRoutine = movePlan.moveLocally(calendarRoutineSync)
        if (movedRoutine == null && apiKey.isBlank()) {
            showToast(TRAINING_CALENDAR_LOCAL_MOVE_UNAVAILABLE_MESSAGE)
            return
        }

        if (movedRoutine != null) {
            localSnapshot = calendarDataUseCase.loadLocalSnapshot()
            showToast(movePlan.startedMessage(movedLocally = true))

            if (apiKey.isBlank()) return
        } else {
            showToast(movePlan.startedMessage(movedLocally = false))
        }

        scope.launch {
            try {
                movePlan.syncRemote(calendarRoutineSync, movedRoutine)
                localSnapshot = calendarDataUseCase.loadLocalSnapshot()
                refresh(selectedRange, forceSync = true)
            } catch (error: Exception) {
                pendingCalendarRoutineMoves = movePlan.rollbackPendingMove(pendingCalendarRoutineMoves)
                showToast(
                    movePlan.failureMessage(movedLocally = movedRoutine != null),
                    Toast.LENGTH_LONG
                )
            }
        }
    }

    fun deleteDraggedCalendarRoutine(item: TrainingItem) {
        val deletePlan = when (
            val decision = planTrainingCalendarRoutineDelete(
                item = item,
                pendingCalendarRoutineMoves = pendingCalendarRoutineMoves,
                optimisticallyDeletedCalendarRoutineKeys = optimisticallyDeletedCalendarRoutineKeys,
                deleteScopeFor = calendarRoutineSync::deleteScopeFor
            )
        ) {
            TrainingCalendarRoutineDeleteDecision.Ignore -> return
            is TrainingCalendarRoutineDeleteDecision.Blocked -> {
                showToast(decision.message)
                return
            }
            is TrainingCalendarRoutineDeleteDecision.Delete -> decision.plan
        }
        optimisticallyDeletedCalendarRoutineKeys = deletePlan.optimisticallyDeletedCalendarRoutineKeys
        pendingCalendarRoutineMoves = deletePlan.pendingCalendarRoutineMoves

        if (!deletePlan.requiresRemoteDelete) {
            scope.launch {
                deletePlan.delete(calendarRoutineSync)
                localSnapshot = calendarDataUseCase.loadLocalSnapshot()
                showToast(deletePlan.deletedMessage())
            }
            return
        }

        scope.launch {
            try {
                deletePlan.delete(calendarRoutineSync)
                optimisticallyDeletedCalendarRoutineKeys = deletePlan.clearOptimisticDeleteKeys(
                    optimisticallyDeletedCalendarRoutineKeys
                )
                localSnapshot = calendarDataUseCase.loadLocalSnapshot()
                showToast(deletePlan.deletedMessage())
                refresh(selectedRange, forceSync = true)
            } catch (error: Exception) {
                optimisticallyDeletedCalendarRoutineKeys = deletePlan.clearOptimisticDeleteKeys(
                    optimisticallyDeletedCalendarRoutineKeys
                )
                showToast(
                    error.message ?: "Routine을 삭제하지 못했습니다.",
                    Toast.LENGTH_LONG
                )
            }
        }
    }

    fun shiftCalendarRoutineDragToAdjacentWeek(direction: Int) {
        if (calendarMode != TrainingCalendarMode.WEEK) return
        scope.launch {
            val targetPage = (pagerState.settledPage + direction).coerceIn(0, Int.MAX_VALUE - 1)
            pagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(calendarDataUseCase, calendarMode, selectedRange.start, selectedRange.end) {
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
                localSnapshot = calendarDataUseCase.loadLocalSnapshot()
            }
        }
        lifecycle?.addObserver(observer)
        onDispose {
            lifecycle?.removeObserver(observer)
        }
    }

    if (showCalendar) {
        TrainingCalendarDatePickerDialog(
            selectedDate = selectedRange.start,
            onDismiss = { showCalendar = false },
            onDateSelected = { selectedDate ->
                val targetPage = initialPage + calendarMode.pageOffsetForDate(baseDate, selectedDate).toInt()
                scope.launch {
                    pagerState.animateScrollToPage(targetPage)
                }
                showCalendar = false
            }
        )
    }

    if (showWorkoutActionSheet) {
        WorkoutActionBottomSheet(
            onDismiss = { showWorkoutActionSheet = false },
            onRunningClick = {
                showWorkoutActionSheet = false
                onRunningSession()
            },
            onStrengthClick = {
                showWorkoutActionSheet = false
                onStrengthSession()
            }
        )
    }

    if (routineSaveUiState.isSheetVisible) {
        val routineSaveDate = routineSaveUiState.selectedDate(selectedRoutineDate())
        StrengthRoutineSaveBottomSheet(
            routines = strengthRoutines,
            selectedDate = routineSaveDate,
            savingRoutineId = routineSaveUiState.savingRoutineId,
            message = routineSaveUiState.message,
            error = routineSaveUiState.error,
            onDismiss = { routineSaveUiState = routineSaveUiState.dismiss() },
            onDateSelected = { routineSaveUiState = routineSaveUiState.withSelectedDate(it) },
            onRoutineSelected = { routine -> saveRoutineToCalendar(routine, routineSaveDate) }
        )
    }

    Scaffold(
        floatingActionButton = {
            if (!isCalendarRoutineDragging) {
                WeeklyTrainingFabMenu(
                    expanded = showFabActions,
                    onExpandedChange = { showFabActions = it },
                    onWorkoutClick = {
                        showFabActions = false
                        showWorkoutActionSheet = true
                    },
                    onPlanAddClick = {
                        openRoutineSaveSheet(selectedRoutineDate())
                    },
                    onRoutineSaveClick = {
                        showFabActions = false
                        onManageRoutines()
                    },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        },
        topBar = {
            TrainingCalendarTopBar(
                calendarMode = calendarMode,
                selectedRange = selectedRange,
                today = today,
                showBackButton = showBackButton,
                showCalendarModeButton = showCalendarModeButton,
                showSettingsMenu = showSettingsMenu,
                isIntervalsOAuthConnecting = isIntervalsOAuthConnecting,
                apiKey = apiKey,
                intervalsOAuthConnectedLabel = intervalsOAuthConnectedLabel,
                isIntervalsOAuthConfigured = isIntervalsOAuthConfigured,
                onTitleClick = { showCalendar = true },
                onTodayClick = {
                    val targetPage = initialPage + calendarMode.pageOffsetForDate(baseDate, today).toInt()
                    scope.launch {
                        pagerState.animateScrollToPage(targetPage)
                    }
                },
                onCalendarModeClick = { calendarMode = calendarMode.next() },
                onSettingsMenuExpandedChange = { showSettingsMenu = it },
                onRefreshClick = { refresh(forceSync = true) },
                onLoginClick = onLoginClick,
                onLogout = onLogout,
                onBack = onBack
            )
        }
    ) { innerPadding ->
        TrainingCalendarPagerContent(
            pagerState = pagerState,
            baseDate = baseDate,
            initialPage = initialPage,
            calendarMode = calendarMode,
            isRemoteConnected = apiKey.isNotBlank(),
            weekUiState = state,
            localSnapshot = localSnapshot,
            strengthRoutines = strengthRoutines,
            deletedCalendarRoutineIds = deletedCalendarRoutineIds,
            optimisticallyDeletedCalendarRoutineKeys = optimisticallyDeletedCalendarRoutineKeys,
            pendingCalendarRoutineMoves = pendingCalendarRoutineMoves.values,
            calendarDragUiState = calendarDragUiState,
            isCalendarRoutineDragging = isCalendarRoutineDragging,
            innerPadding = innerPadding,
            calendarDataUseCase = calendarDataUseCase,
            onCalendarDragUiStateChange = { calendarDragUiState = it },
            onCalendarRoutineDragStarted = { showFabActions = false },
            onRefreshRange = { refresh(it) },
            onRoutineSelected = onRoutineSelected,
            onIntervalStrengthRoutineSelected = onIntervalStrengthRoutineSelected,
            onMonthDaySelected = onMonthDaySelected,
            onOpenRoutineSaveSheet = ::openRoutineSaveSheet,
            onRoutineDateChanged = ::moveRoutineToDate,
            onRoutineDeleteRequested = ::deleteDraggedCalendarRoutine,
            onDragWeekShiftRequested = ::shiftCalendarRoutineDragToAdjacentWeek
        )
    }
}

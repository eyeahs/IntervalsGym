package com.lighthousepark.intervalsgym.app

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lighthousepark.intervalsgym.core.throttleRapidTaps
import com.lighthousepark.intervalsgym.login.LoginScreen
import com.lighthousepark.intervalsgym.running.toTrainingItem
import com.lighthousepark.intervalsgym.running.ui.RunningRoutineListScreen
import com.lighthousepark.intervalsgym.running.ui.RunningRoutineManagementScreen
import com.lighthousepark.intervalsgym.strength.ActiveStrengthSession
import com.lighthousepark.intervalsgym.strength.CompletedStrengthSession
import com.lighthousepark.intervalsgym.strength.StrengthWorkoutRoutine
import com.lighthousepark.intervalsgym.strength.ui.StrengthRoutineEditScreen
import com.lighthousepark.intervalsgym.strength.ui.StrengthRoutineHistoryScreen
import com.lighthousepark.intervalsgym.strength.ui.StrengthRoutineListScreen
import com.lighthousepark.intervalsgym.strength.ui.StrengthRoutineManagementScreen
import com.lighthousepark.intervalsgym.strength.ui.StrengthSessionScreen
import com.lighthousepark.intervalsgym.training.TrainingCalendarMode
import com.lighthousepark.intervalsgym.training.TrainingItem
import com.lighthousepark.intervalsgym.training.ui.WeeklyTrainingScreen
import com.lighthousepark.intervalsgym.workout.ui.WorkoutRoutineScreen
import java.time.LocalDate

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
    completedStrengthHistory: List<CompletedStrengthSession>,
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
                history = completedStrengthHistory,
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

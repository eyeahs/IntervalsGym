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
import androidx.compose.material3.Checkbox
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
 * Route owner for [ROUTE_STRENGTH_SESSION].
 * This is the single entry point for strength routine preview, ongoing workout list, set execution, rest timer, and finish/upload state.
 * UI tests: StrengthSessionUiTest.readyScreen_startButtonInvokesStart,
 * readyScreen_editButtonInvokesEditRoutine, readyScreen_entryRowTogglesSetDetails,
 * strengthSessionTopBar_readyActionsInvokeCallbacks,
 * strengthSessionTopBar_ongoingListShowsTimerInsteadOfBackAndHidesReadyActions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StrengthSessionScreen(
    apiKey: String,
    routine: StrengthWorkoutRoutine?,
    calendarRoutineItem: TrainingItem?,
    isRoutineEditable: Boolean,
    activeSession: ActiveStrengthSession?,
    startImmediately: Boolean,
    onImmediateStartConsumed: () -> Unit,
    onSessionChange: (ActiveStrengthSession?) -> Unit,
    onSessionFinished: (CompletedStrengthSession?, Boolean) -> Unit,
    onHistoryClick: (StrengthWorkoutRoutine) -> Unit,
    onEditRoutine: (StrengthWorkoutRoutine) -> Unit,
    onCalendarRoutineDeleted: (TrainingItem) -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val completedStrengthHistory = remember(context) { loadCompletedStrengthSessionHistory(prefs) }
    val repository = remember(apiKey) { IntervalsRepository(apiKey) }
    val now = remember(activeSession?.routineId) { System.currentTimeMillis() }
    val shouldStartImmediately = activeSession == null && startImmediately
    val restoredRestActive = remember(activeSession?.routineId) {
        activeSession?.restEndAtMillis?.let { it > System.currentTimeMillis() } == true
    }
    val initialExerciseIndex = remember(activeSession?.routineId) {
        if (activeSession != null && activeSession.restEndAtMillis > 0 && activeSession.restEndAtMillis <= now) {
            activeSession.pendingExerciseIndex ?: activeSession.currentExerciseIndex
        } else {
            activeSession?.currentExerciseIndex ?: 0
        }
    }
    val initialSetIndex = remember(activeSession?.routineId) {
        if (activeSession != null && activeSession.restEndAtMillis > 0 && activeSession.restEndAtMillis <= now) {
            activeSession.pendingSetIndex ?: activeSession.currentSetIndex
        } else {
            activeSession?.currentSetIndex ?: 0
        }
    }
    var entries by remember(activeSession?.routineId, routine?.id) {
        mutableStateOf(activeSession?.entries ?: routine?.entries.orEmpty().map { it.copyForWorkout() })
    }
    var hasStarted by remember(activeSession?.routineId, routine?.id) {
        mutableStateOf(activeSession?.hasStarted ?: shouldStartImmediately)
    }
    var sessionStartedAtMillis by remember(activeSession?.routineId, routine?.id) {
        mutableStateOf(
            activeSession?.sessionStartedAtMillis?.takeIf { it > 0L }
                ?: if (activeSession?.hasStarted == true || shouldStartImmediately) now else 0L
        )
    }
    var sessionElapsedSeconds by remember(activeSession?.routineId, routine?.id) {
        mutableIntStateOf(
            if ((activeSession?.hasStarted == true || shouldStartImmediately) && sessionStartedAtMillis > 0L) {
                ((System.currentTimeMillis() - sessionStartedAtMillis) / 1000L).toInt().coerceAtLeast(0)
            } else {
                0
            }
        )
    }
    var isSetScreenVisible by remember(activeSession?.routineId, routine?.id) {
        mutableStateOf(activeSession?.isSetScreenVisible ?: shouldStartImmediately)
    }
    var currentExerciseIndex by remember(activeSession?.routineId, routine?.id) { mutableIntStateOf(initialExerciseIndex) }
    var currentSetIndex by remember(activeSession?.routineId, routine?.id) { mutableIntStateOf(initialSetIndex) }
    var isChangingCurrentExercise by remember(routine?.id) { mutableStateOf(false) }
    var isCurrentExerciseTypeDialogVisible by remember(routine?.id) { mutableStateOf(false) }
    var shouldReturnToOngoingAfterExerciseChange by remember(routine?.id) { mutableStateOf(false) }
    var pendingAddedExerciseEntryId by remember(routine?.id) { mutableStateOf<Int?>(null) }
    var sessionExerciseToConfigure by remember { mutableStateOf<StrengthExercise?>(null) }
    var sessionExerciseToConfigureSearchQuery by remember { mutableStateOf("") }
    var isSessionCustomExerciseDialogVisible by remember { mutableStateOf(false) }
    var pendingExerciseIndex by remember(activeSession?.routineId, routine?.id) {
        mutableStateOf(if (restoredRestActive) activeSession?.pendingExerciseIndex else null)
    }
    var pendingSetIndex by remember(activeSession?.routineId, routine?.id) {
        mutableStateOf(if (restoredRestActive) activeSession?.pendingSetIndex else null)
    }
    var restRemainingSeconds by remember(activeSession?.routineId, routine?.id) {
        mutableStateOf(
            activeSession?.restEndAtMillis
                ?.takeIf { it > now }
                ?.let { ((it - now) / 1000L).toInt().coerceAtLeast(1) }
        )
    }
    var restEndAtMillis by remember(activeSession?.routineId, routine?.id) {
        mutableStateOf(activeSession?.restEndAtMillis?.takeIf { it > now } ?: 0L)
    }
    var isRestSheetVisible by remember(activeSession?.routineId, routine?.id) {
        mutableStateOf(restoredRestActive && activeSession?.isRestSheetVisible == true)
    }
    var restTitle by remember(activeSession?.routineId, routine?.id) {
        mutableStateOf(activeSession?.restTitle.takeIf { restoredRestActive }.orEmpty())
    }
    var setEvents by remember(activeSession?.routineId, routine?.id) {
        mutableStateOf(activeSession?.setEvents.orEmpty())
    }
    var restEvents by remember(activeSession?.routineId, routine?.id) {
        mutableStateOf(activeSession?.restEvents.orEmpty())
    }
    var activeRestEventId by remember(activeSession?.routineId, routine?.id) {
        mutableStateOf(activeSession?.activeRestEventId.takeIf { restoredRestActive })
    }
    var isUploading by remember { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var isFinishChoiceDialogVisible by remember { mutableStateOf(false) }
    var isCalendarRoutineDeleteConfirmVisible by remember { mutableStateOf(false) }
    var isDeletingCalendarRoutine by remember { mutableStateOf(false) }
    var finishRpe by remember { mutableIntStateOf(7) }
    var applyWorkoutResultToRoutine by rememberSaveable(routine?.id) { mutableStateOf(true) }
    var handledCompleteSetOverlayRequest by remember { mutableIntStateOf(RestOverlayRequests.completeSetRequest) }
    var autoSavedStrengthSessionId by rememberSaveable(activeSession?.routineId, routine?.id) {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(shouldStartImmediately) {
        if (shouldStartImmediately) {
            onImmediateStartConsumed()
        }
    }

    LaunchedEffect(routine?.entries, hasStarted, activeSession?.routineId) {
        if (!hasStarted && activeSession == null) {
            entries = routine?.entries.orEmpty().map { it.copyForWorkout() }
        }
    }

    LaunchedEffect(isChangingCurrentExercise, pendingAddedExerciseEntryId, entries) {
        if (!isChangingCurrentExercise) return@LaunchedEffect
        val focusIndex = entries.exerciseChangeFocusIndex(
            currentExerciseIndex = currentExerciseIndex,
            pendingAddedEntryId = pendingAddedExerciseEntryId
        )
        val focusEntry = entries.getOrNull(focusIndex) ?: return@LaunchedEffect
        if (currentExerciseIndex != focusIndex) {
            currentExerciseIndex = focusIndex
        }
        if (currentSetIndex !in focusEntry.records.indices) {
            currentSetIndex = 0
        }
    }

    fun updateEntry(entry: StrengthRoutineEntry) {
        entries = entries.map { if (it.id == entry.id) entry else it }
    }

    fun updateCurrentEntry(entry: StrengthRoutineEntry) {
        val previousEntry = entries.firstOrNull { it.id == entry.id }
        val resetSetKeys = previousEntry?.records
            ?.mapIndexedNotNull { index, previousRecord ->
                val nextRecord = entry.records.getOrNull(index) ?: return@mapIndexedNotNull null
                if (previousRecord.completed && !nextRecord.completed) {
                    previousRecord.id to index
                } else {
                    null
                }
            }
            .orEmpty()
            .toSet()
        if (resetSetKeys.isNotEmpty()) {
            val resetSequences = setEvents
                .filter { event ->
                    event.exerciseEntryId == entry.id && (event.setRecordId to event.setIndex) in resetSetKeys
                }
                .map { it.sequence }
                .toSet()
            setEvents = setEvents.filterNot { event ->
                event.exerciseEntryId == entry.id && (event.setRecordId to event.setIndex) in resetSetKeys
            }
            restEvents = restEvents.filterNot { event -> event.afterSetSequence in resetSequences }
            if (activeRestEventId != null && restEvents.none { it.id == activeRestEventId }) {
                activeRestEventId = null
                restRemainingSeconds = null
                restEndAtMillis = 0L
                isRestSheetVisible = false
                restTitle = ""
                stopRestOverlay(context)
            }
        }
        updateEntry(entry)
        if (entry.id == entries.getOrNull(currentExerciseIndex)?.id && currentSetIndex >= entry.records.size) {
            currentSetIndex = (entry.records.size - 1).coerceAtLeast(0)
        }
    }

    fun finishExerciseChange() {
        isChangingCurrentExercise = false
        isCurrentExerciseTypeDialogVisible = false
        shouldReturnToOngoingAfterExerciseChange = false
        pendingAddedExerciseEntryId = null
        sessionExerciseToConfigure = null
        isSessionCustomExerciseDialogVisible = false
    }

    fun applyCurrentExerciseChange(exercise: StrengthExercise, equipment: String, variation: String) {
        val targetEntryId = pendingAddedExerciseEntryId ?: entries.getOrNull(currentExerciseIndex)?.id ?: return
        val targetExerciseIndex = entries.indexOfFirst { it.id == targetEntryId }.takeIf { it >= 0 } ?: return
        val entry = entries.getOrNull(targetExerciseIndex) ?: return
        currentExerciseIndex = targetExerciseIndex
        val shouldReturnToOngoing = shouldReturnToOngoingAfterExerciseChange
        val restoredEntry = if (entry.id == pendingAddedExerciseEntryId) {
            completedStrengthHistory
                .latestMatchingStrengthEntry(exercise, equipment, variation)
                ?.copyAsNewRoutineEntry(
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
        if (shouldReturnToOngoing) {
            isSetScreenVisible = false
        }
    }

    fun deleteCalendarRoutine() {
        val targetRoutine = calendarRoutineItem ?: return
        scope.launch {
            isDeletingCalendarRoutine = true
            uploadError = null
            try {
                if (apiKey.isNotBlank() && !targetRoutine.id.startsWith("local-")) {
                    repository.deleteCalendarRoutine(targetRoutine.remoteId)
                    removeCalendarRoutineFromIntervalsCaches(prefs, apiKey, targetRoutine)
                }
                removeScheduledStrengthRoutine(prefs, targetRoutine)
                onCalendarRoutineDeleted(targetRoutine)
            } catch (error: Exception) {
                uploadError = error.message ?: "Routine을 삭제하지 못했습니다."
            } finally {
                isDeletingCalendarRoutine = false
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
        val entry = defaultStrengthRoutineEntry(nextId, strengthExerciseCatalog.first())
        val nextEntries = entries + entry
        entries = nextEntries
        currentExerciseIndex = nextEntries.lastIndex
        currentSetIndex = 0
        isChangingCurrentExercise = true
        shouldReturnToOngoingAfterExerciseChange = true
        pendingAddedExerciseEntryId = nextId
        isSetScreenVisible = true
    }

    fun replaceExerciseOrderInSession(nextEntries: List<StrengthRoutineEntry>) {
        if (nextEntries == entries) return
        val currentEntryId = entries.getOrNull(currentExerciseIndex)?.id
        val pendingEntryId = pendingExerciseIndex?.let { entries.getOrNull(it)?.id }
        val normalizedEntries = nextEntries.normalizeSupersetGroups()
        entries = normalizedEntries
        currentEntryId?.let { id ->
            normalizedEntries.indexOfFirst { it.id == id }
                .takeIf { it >= 0 }
                ?.let { currentExerciseIndex = it }
        }
        pendingEntryId?.let { id ->
            pendingExerciseIndex = normalizedEntries.indexOfFirst { it.id == id }.takeIf { it >= 0 }
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
        val shouldAdvanceCurrentExercise = shouldAdvanceCurrentExerciseAfterCompletedExercise(
            entries = updatedEntries,
            fromExerciseIndex = currentExerciseIndex,
            toSet = nextIncomplete
        )

        pendingExerciseIndex = nextIncomplete?.first
        pendingSetIndex = nextIncomplete?.second
        val restSeconds = record.restSeconds.toIntOrNull() ?: entry.restSeconds
        val skipRestForSupersetTransition = isImmediateSupersetTransition(
            entries = updatedEntries,
            fromExerciseIndex = currentExerciseIndex,
            fromSetIndex = targetSetIndex,
            toSet = nextIncomplete
        )
        if (shouldAdvanceCurrentExercise && nextIncomplete != null) {
            currentExerciseIndex = nextIncomplete.first
            currentSetIndex = nextIncomplete.second
        }
        if (nextIncomplete != null && restSeconds > 0 && !skipRestForSupersetTransition) {
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

    fun currentStrengthSessionEndedAtMillis(): Long {
        return completedStrengthSessionFinishedAtMillis(entries, setEvents)
            ?: System.currentTimeMillis()
    }

    fun uploadSession() {
        if (apiKey.isBlank()) {
            uploadMessage = null
            uploadError = "Intervals.icu 업데이트는 로그인 후 사용할 수 있습니다."
            return
        }
        val endedAtMillis = currentStrengthSessionEndedAtMillis()
        val finalizedRestEvents = finalizeRestEvents(restEvents, activeRestEventId, endedAtMillis, "workout_finished")
        val trainingLoad = entries.strengthTrainingLoad(finishRpe)
        val localSession = routine?.let {
            buildCompletedStrengthSession(
                routine = it,
                entries = entries,
                setEvents = setEvents,
                restEvents = finalizedRestEvents,
                startedAtMillis = sessionStartedAtMillis,
                endedAtMillis = endedAtMillis,
                rpe = finishRpe,
                trainingLoad = trainingLoad,
                uploadedToIntervals = true,
                appliedToRoutine = applyWorkoutResultToRoutine
            )
        }
        scope.launch {
            isUploading = true
            uploadMessage = null
            uploadError = null
            try {
                repository.uploadStrengthSession(
                    StrengthSession(
                        name = routine?.name ?: "웨이트 트레이닝",
                        startedAt = sessionStartedAtMillis
                            .takeIf { it > 0L }
                            ?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) }
                            ?: LocalDateTime.now().minusSeconds(entries.totalDurationSeconds().toLong()),
                        entries = entries,
                        rpe = finishRpe,
                        trainingLoad = trainingLoad
                    )
                )
                uploadMessage = "Intervals.icu에 업로드했습니다."
                localSession?.let { appendStrengthSessionHistory(prefs, it) }
                stopRestOverlay(context)
                stopWorkoutStatusService(context)
                onSessionFinished(localSession, applyWorkoutResultToRoutine)
            } catch (error: Exception) {
                uploadError = error.message ?: "업로드하지 못했습니다."
            } finally {
                isUploading = false
            }
        }
    }

    fun saveStrengthSessionLocally(
        endedAtMillis: Long,
        endReason: String,
    ): CompletedStrengthSession? {
        val workoutRoutine = routine ?: return null
        val safeEndedAtMillis = endedAtMillis.takeIf { it > 0L } ?: System.currentTimeMillis()
        val finalizedRestEvents = finalizeRestEvents(restEvents, activeRestEventId, safeEndedAtMillis, endReason)
        val trainingLoad = entries.strengthTrainingLoad(finishRpe)
        val localSession = buildCompletedStrengthSession(
            routine = workoutRoutine,
            entries = entries,
            setEvents = setEvents,
            restEvents = finalizedRestEvents,
            startedAtMillis = sessionStartedAtMillis,
            endedAtMillis = safeEndedAtMillis,
            rpe = finishRpe,
            trainingLoad = trainingLoad,
            uploadedToIntervals = false,
            appliedToRoutine = applyWorkoutResultToRoutine
        )
        if (autoSavedStrengthSessionId == localSession.id) return localSession
        appendStrengthSessionHistory(prefs, localSession)
        autoSavedStrengthSessionId = localSession.id
        DiagnosticsLogger.log(
            context = context,
            tag = "StrengthSession",
            message = buildString {
                appendLine("event=auto local save")
                appendLine("reason=$endReason")
                appendLine("routineName=${workoutRoutine.name}")
                appendLine("localSessionId=${localSession.id}")
                appendLine("startedAtMillis=${localSession.startedAtMillis}")
                appendLine("endedAtMillis=${localSession.endedAtMillis}")
                appendLine("setEvents=${setEvents.size}")
                appendLine("restEvents=${finalizedRestEvents.size}")
            }
        )
        stopRestOverlay(context)
        stopWorkoutStatusService(context)
        onSessionFinished(localSession, applyWorkoutResultToRoutine)
        return localSession
    }

    fun finishWorkout() {
        val endedAtMillis = currentStrengthSessionEndedAtMillis()
        val finalizedRestEvents = finalizeRestEvents(restEvents, activeRestEventId, endedAtMillis, "workout_finished")
        val trainingLoad = entries.strengthTrainingLoad(finishRpe)
        val localSession = routine?.let {
            buildCompletedStrengthSession(
                routine = it,
                entries = entries,
                setEvents = setEvents,
                restEvents = finalizedRestEvents,
                startedAtMillis = sessionStartedAtMillis,
                endedAtMillis = endedAtMillis,
                rpe = finishRpe,
                trainingLoad = trainingLoad,
                uploadedToIntervals = apiKey.isNotBlank(),
                appliedToRoutine = applyWorkoutResultToRoutine
            )
        }
        if (apiKey.isBlank()) {
            val savedSession = localSession?.copy(uploadedToIntervals = false)
            savedSession?.let { appendStrengthSessionHistory(prefs, it) }
            stopRestOverlay(context)
            stopWorkoutStatusService(context)
            onSessionFinished(savedSession, applyWorkoutResultToRoutine)
        } else {
            uploadSession()
        }
    }

    LaunchedEffect(
        hasStarted,
        routine?.id,
        entries,
        setEvents,
        restEvents,
        activeRestEventId,
        sessionStartedAtMillis,
        applyWorkoutResultToRoutine
    ) {
        val finishedAtMillis = completedStrengthSessionFinishedAtMillis(entries, setEvents)
            ?: return@LaunchedEffect
        if (!hasStarted || routine == null) return@LaunchedEffect
        val delayMillis = sessionAutoLocalSaveDelayMillis(
            finishedAtMillis = finishedAtMillis,
            nowMillis = System.currentTimeMillis()
        )
        if (delayMillis > 0L) {
            delay(delayMillis)
        }
        if (
            hasStarted &&
            shouldAutoLocalSaveCompletedStrengthSession(
                entries = entries,
                setEvents = setEvents,
                nowMillis = System.currentTimeMillis()
            )
        ) {
            saveStrengthSessionLocally(
                endedAtMillis = finishedAtMillis,
                endReason = "auto_local_save_after_last_set"
            )
        }
    }

    fun discardWorkout() {
        closeActiveRestEvent("discarded")
        restRemainingSeconds = null
        restEndAtMillis = 0L
        isRestSheetVisible = false
        restTitle = ""
        stopRestOverlay(context)
        stopWorkoutStatusService(context)
        onSessionFinished(null, false)
    }

    LaunchedEffect(
        routine?.id,
        routine?.name,
        hasStarted,
        sessionStartedAtMillis,
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
        if (hasStarted && routine != null) {
            onSessionChange(
                ActiveStrengthSession(
                    routineId = routine.id,
                    routineName = routine.name,
                    entries = entries,
                    hasStarted = hasStarted,
                    sessionStartedAtMillis = sessionStartedAtMillis,
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

    LaunchedEffect(
        hasStarted,
        sessionStartedAtMillis,
        routine?.name,
        entries.getOrNull(currentExerciseIndex)?.title,
        restRemainingSeconds,
        restEndAtMillis,
        restTitle
    ) {
        if (hasStarted && sessionStartedAtMillis > 0L) {
            val isResting = restRemainingSeconds != null && restEndAtMillis > System.currentTimeMillis()
            startWorkoutStatusService(
                context = context,
                workoutType = WorkoutStatusForegroundService.TYPE_STRENGTH,
                title = routine?.name ?: "웨이트 트레이닝",
                phaseLabel = if (isResting) "휴식" else "운동 중",
                detailText = if (isResting) {
                    restTitle
                } else {
                    entries.getOrNull(currentExerciseIndex)?.title.orEmpty()
                },
                startAtMillis = sessionStartedAtMillis,
                endAtMillis = restEndAtMillis.takeIf { isResting } ?: 0L
            )
        }
    }

    fun handleBack() {
        when {
            isCurrentExerciseTypeDialogVisible -> isCurrentExerciseTypeDialogVisible = false
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
                isCurrentExerciseTypeDialogVisible = false
            }
            hasStarted && isSetScreenVisible -> isSetScreenVisible = false
            hasStarted -> onBack()
            else -> onBack()
        }
    }

    BackHandler(enabled = isChangingCurrentExercise || hasStarted) {
        handleBack()
    }

    LaunchedEffect(hasStarted, sessionStartedAtMillis) {
        while (hasStarted && sessionStartedAtMillis > 0L) {
            sessionElapsedSeconds = ((System.currentTimeMillis() - sessionStartedAtMillis) / 1000L)
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

    val activeSetOverlayTitle = entries.getOrNull(currentExerciseIndex)?.let { entry ->
        val nextSet = entry.records.indexOfFirst { !it.completed }
            .takeIf { it >= 0 }
            ?: currentSetIndex
        "Set ${nextSet + 1} · ${entry.title}"
    }.orEmpty()

    LaunchedEffect(
        hasStarted,
        isSetScreenVisible,
        isChangingCurrentExercise,
        restRemainingSeconds,
        activeSetOverlayTitle
    ) {
        val isResting = restRemainingSeconds != null
        val shouldShowSetCompleteOverlay = hasStarted &&
            isSetScreenVisible &&
            !isChangingCurrentExercise &&
            !isResting &&
            activeSetOverlayTitle.isNotBlank()
        if (shouldShowSetCompleteOverlay) {
            startStrengthSetCompleteOverlay(context, activeSetOverlayTitle)
        } else if (!isResting) {
            stopRestOverlay(context)
        }
    }

    LaunchedEffect(RestOverlayRequests.showSheetRequest) {
        if (RestOverlayRequests.showSheetRequest > 0 && restRemainingSeconds != null) {
            isRestSheetVisible = true
        }
    }

    LaunchedEffect(RestOverlayRequests.completeSetRequest) {
        val request = RestOverlayRequests.completeSetRequest
        if (request <= handledCompleteSetOverlayRequest) return@LaunchedEffect
        handledCompleteSetOverlayRequest = request
        if (
            hasStarted &&
            isSetScreenVisible &&
            !isChangingCurrentExercise &&
            restRemainingSeconds == null
        ) {
            completeCurrentSet()
            if (restRemainingSeconds != null && restEndAtMillis > System.currentTimeMillis()) {
                isRestSheetVisible = false
                startRestOverlay(context, restTitle, restEndAtMillis)
            }
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

    val currentEntryForTypeDialog = entries.getOrNull(currentExerciseIndex)
    if (isCurrentExerciseTypeDialogVisible && currentEntryForTypeDialog != null) {
        StrengthExerciseTypeDialog(
            entry = currentEntryForTypeDialog,
            exercise = currentEntryForTypeDialog.exercise,
            initialEquipment = currentEntryForTypeDialog.equipment,
            initialVariation = currentEntryForTypeDialog.variation,
            confirmText = "저장",
            onExerciseChangeClick = {
                isCurrentExerciseTypeDialogVisible = false
                shouldReturnToOngoingAfterExerciseChange = false
                pendingAddedExerciseEntryId = null
                isChangingCurrentExercise = true
            },
            onDismiss = { isCurrentExerciseTypeDialogVisible = false },
            onDone = { equipment, variation ->
                isCurrentExerciseTypeDialogVisible = false
                updateCurrentEntry(
                    currentEntryForTypeDialog.copy(
                        equipment = equipment,
                        variation = variation
                    )
                )
            }
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
        StrengthFinishChoiceDialog(
            apiKey = apiKey,
            entries = entries,
            finishRpe = finishRpe,
            applyWorkoutResultToRoutine = applyWorkoutResultToRoutine,
            isUploading = isUploading,
            onApplyWorkoutResultToRoutineChange = { applyWorkoutResultToRoutine = it },
            onFinishRpeChange = { finishRpe = it },
            onDismiss = { isFinishChoiceDialogVisible = false },
            onSave = {
                isFinishChoiceDialogVisible = false
                finishWorkout()
            },
            onDiscard = {
                isFinishChoiceDialogVisible = false
                discardWorkout()
            }
        )
    }

    if (isCalendarRoutineDeleteConfirmVisible && calendarRoutineItem != null) {
        StrengthCalendarRoutineDeleteConfirmDialog(
            message = calendarRoutineItem.plannedWorkoutDeleteConfirmMessage(),
            isDeleting = isDeletingCalendarRoutine,
            onConfirm = {
                isCalendarRoutineDeleteConfirmVisible = false
                deleteCalendarRoutine()
            },
            onCancel = { isCalendarRoutineDeleteConfirmVisible = false }
        )
    }

    Scaffold(
        topBar = {
            val isOngoingExerciseListVisible = hasStarted && !isChangingCurrentExercise && !isSetScreenVisible
            StrengthSessionTopBar(
                title = if (isChangingCurrentExercise) "운동 목록" else routine?.name ?: "웨이트 수행",
                isWorkoutActive = hasStarted && !isOngoingExerciseListVisible,
                elapsedSeconds = sessionElapsedSeconds,
                showTimerBadgeAsNavigation = isOngoingExerciseListVisible,
                showReadyActions = !hasStarted && routine != null && !isChangingCurrentExercise,
                showCalendarRoutineDelete = calendarRoutineItem?.isRoutine == true,
                isDeletingCalendarRoutine = isDeletingCalendarRoutine,
                onBack = ::handleBack,
                onCalendarRoutineDelete = { isCalendarRoutineDeleteConfirmVisible = true },
                onHistoryClick = { routine?.let(onHistoryClick) }
            )
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
            if (hasStarted && routine != null && !isChangingCurrentExercise && isSetScreenVisible) {
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
            } else if (hasStarted && routine != null && !isChangingCurrentExercise) {
                StrengthSessionFinishBar(
                    isUploading = isUploading,
                    onFinish = { isFinishChoiceDialogVisible = true }
                )
            }
        }
    ) { innerPadding ->
        if (routine == null) {
            EmptyView(message = "선택된 웨이트 Routine이 없습니다.")
            return@Scaffold
        }

        if (!hasStarted) {
            StrengthSessionReadyScreen(
                routine = routine,
                entries = entries,
                modifier = Modifier.padding(innerPadding),
                onStart = {
                    hasStarted = true
                    sessionStartedAtMillis = System.currentTimeMillis()
                    sessionElapsedSeconds = 0
                    nextIncompleteSet(entries, 0, -1)?.let { (exerciseIndex, setIndex) ->
                        currentExerciseIndex = exerciseIndex
                        currentSetIndex = setIndex
                    }
                    isSetScreenVisible = true
                },
                onEditRoutine = if (isRoutineEditable) {
                    { onEditRoutine(routine) }
                } else {
                    null
                }
            )
        } else {
            val currentEntry = entries.getOrNull(currentExerciseIndex)
            if (isChangingCurrentExercise) {
                StrengthExerciseListScreen(
                    modifier = Modifier.padding(innerPadding),
                    onAddCustomExercise = { isSessionCustomExerciseDialogVisible = true },
                    onExerciseSelected = { exercise, searchQuery ->
                        sessionExerciseToConfigureSearchQuery = searchQuery
                        sessionExerciseToConfigure = exercise
                    }
                )
            } else if (isSetScreenVisible) {
                BackHandler(enabled = !isCurrentExerciseTypeDialogVisible) {
                    isSetScreenVisible = false
                }
                StrengthSetExecutionScreen(
                    entry = currentEntry,
                    recentHistory = currentEntry?.let { entry ->
                        completedStrengthHistory.recentMatchingStrengthExerciseHistory(
                            exercise = entry.exercise,
                            equipment = entry.equipment,
                            variation = entry.variation
                        )
                    }.orEmpty(),
                    modifier = Modifier.padding(innerPadding),
                    onExerciseClick = {
                        shouldReturnToOngoingAfterExerciseChange = false
                        pendingAddedExerciseEntryId = null
                        isCurrentExerciseTypeDialogVisible = currentEntry != null
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
                StrengthSessionOngoingRoutineScreen(
                    routine = routine,
                    entries = entries,
                    currentExerciseIndex = currentExerciseIndex,
                    uploadMessage = uploadMessage,
                    uploadError = uploadError,
                    modifier = Modifier.padding(innerPadding),
                    onExerciseClick = ::openExerciseSet,
                    onAddExercise = ::addExerciseToSession,
                    onEntriesChange = { nextEntries ->
                        replaceExerciseOrderInSession(nextEntries)
                    }
                )
            }
        }
    }
}

/**
 * UI tests: StrengthSessionUiTest.finishChoiceDialog_invokesSaveDiscardAndApplyCallbacks,
 * finishChoiceDialog_disablesSaveAndDiscardWhileUploading.
 */
@Composable
internal fun StrengthFinishChoiceDialog(
    apiKey: String,
    entries: List<StrengthRoutineEntry>,
    finishRpe: Int,
    applyWorkoutResultToRoutine: Boolean,
    isUploading: Boolean,
    onApplyWorkoutResultToRoutineChange: (Boolean) -> Unit,
    onFinishRpeChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .debugContentDescription(TestContentDescriptions.StrengthFinishApplyToRoutine)
                        .clickable { onApplyWorkoutResultToRoutineChange(!applyWorkoutResultToRoutine) }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = applyWorkoutResultToRoutine,
                        onCheckedChange = onApplyWorkoutResultToRoutineChange
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "현재 수행 결과를 routine에 반영",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "세트 수, 무게, 횟수, 휴식 시간을 다음 수행 기본값으로 사용합니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
                        onValueChange = { onFinishRpeChange(it.roundToInt().coerceIn(1, 10)) },
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
                onClick = onSave,
                enabled = !isUploading,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthFinishSave)
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDiscard,
                enabled = !isUploading,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthFinishDiscard)
            ) {
                Text("삭제")
            }
        }
    )
}

/**
 * UI tests: StrengthSessionUiTest.calendarRoutineDeleteConfirmDialog_invokesConfirmAndCancelCallbacks,
 * calendarRoutineDeleteConfirmDialog_disablesActionsWhileDeleting.
 */
@Composable
internal fun StrengthCalendarRoutineDeleteConfirmDialog(
    message: String,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onCancel() },
        title = { Text("Routine 삭제") },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDeleting,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthSessionCalendarRoutineConfirmDelete)
            ) {
                Text("삭제", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                enabled = !isDeleting,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthSessionCalendarRoutineCancelDelete)
            ) {
                Text("취소")
            }
        }
    )
}

/**
 * UI tests: StrengthSessionUiTest.strengthSessionTopBar_readyActionsInvokeCallbacks,
 * strengthSessionTopBar_ongoingListShowsTimerInsteadOfBackAndHidesReadyActions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StrengthSessionTopBar(
    title: String,
    isWorkoutActive: Boolean,
    elapsedSeconds: Int,
    showTimerBadgeAsNavigation: Boolean,
    showReadyActions: Boolean,
    showCalendarRoutineDelete: Boolean,
    isDeletingCalendarRoutine: Boolean,
    onBack: () -> Unit,
    onCalendarRoutineDelete: () -> Unit,
    onHistoryClick: () -> Unit,
) {
    TopAppBar(
        title = {
            StrengthSessionTopBarTitle(
                title = title,
                isWorkoutActive = isWorkoutActive,
                elapsedSeconds = elapsedSeconds
            )
        },
        navigationIcon = {
            if (showTimerBadgeAsNavigation) {
                StrengthSessionTimerBadge(
                    elapsedSeconds = elapsedSeconds,
                    modifier = Modifier.padding(start = 12.dp)
                )
            } else {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthSessionBack)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                }
            }
        },
        actions = {
            if (showReadyActions) {
                if (showCalendarRoutineDelete) {
                    IconButton(
                        onClick = onCalendarRoutineDelete,
                        enabled = !isDeletingCalendarRoutine,
                        modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthSessionCalendarRoutineDelete)
                    ) {
                        if (isDeletingCalendarRoutine) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Routine 삭제",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                IconButton(
                    onClick = onHistoryClick,
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.StrengthSessionHistory)
                ) {
                    Icon(Icons.Outlined.Schedule, contentDescription = "History")
                }
            }
        }
    )
}

@Composable
internal fun StrengthSessionTopBarTitle(
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
            StrengthSessionTimerBadge(elapsedSeconds = elapsedSeconds)
        }
    }
}

@Composable
private fun StrengthSessionTimerBadge(
    elapsedSeconds: Int,
    modifier: Modifier = Modifier,
) {
    MaterialSurface(
        modifier = modifier,
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

/**
 * Sub-screen of [StrengthSessionScreen] shown before a strength workout starts.
 * Keep pre-start exercise expansion and edit/start actions here.
 * UI tests: StrengthSessionUiTest.readyScreen_startButtonInvokesStart,
 * readyScreen_editButtonInvokesEditRoutine, readyScreen_entryRowTogglesSetDetails.
 */
@Composable
internal fun StrengthSessionReadyScreen(
    routine: StrengthWorkoutRoutine,
    entries: List<StrengthRoutineEntry>,
    modifier: Modifier = Modifier,
    onStart: () -> Unit,
    onEditRoutine: (() -> Unit)?,
) {
    var expandedEntryIds by remember(routine.id, entries) { mutableStateOf(emptySet<Int>()) }
    val supersetLabels = remember(entries) { entries.supersetGroupLabels() }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                        .debugContentDescription(TestContentDescriptions.strengthReadyEntry(entry.id))
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
                        if (entry.note.isNotBlank()) {
                            Text(
                                text = entry.note,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onEditRoutine != null) {
                    OutlinedButton(
                        onClick = onEditRoutine,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .debugContentDescription(TestContentDescriptions.StrengthEditWorkoutRoutine),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("운동 수정", maxLines = 1)
                    }
                }
                Button(
                    onClick = onStart,
                    enabled = entries.isNotEmpty(),
                    modifier = Modifier
                        .weight(if (onEditRoutine != null) 2f else 1f)
                        .height(52.dp)
                        .debugContentDescription(TestContentDescriptions.StrengthStartWorkout),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("운동 시작", maxLines = 1)
                }
            }
        }
    }
}

@Composable
internal fun StrengthReadySetRow(
    entry: StrengthRoutineEntry,
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

/**
 * Sub-screen of [StrengthSessionScreen] for the in-progress exercise list.
 * It coordinates exercise switching while set execution stays in [StrengthSetExecutionScreen].
 * UI tests: StrengthSessionUiTest.ongoingRoutine_addExerciseButtonInvokesCallback,
 * ongoingRoutine_supersetSelectionGroupsRowsAndMovesSecondBelowTop.
 */
@Composable
internal fun StrengthSessionOngoingRoutineScreen(
    routine: StrengthWorkoutRoutine,
    entries: List<StrengthRoutineEntry>,
    currentExerciseIndex: Int,
    uploadMessage: String?,
    uploadError: String?,
    modifier: Modifier = Modifier,
    onExerciseClick: (Int) -> Unit,
    onAddExercise: () -> Unit,
    onEntriesChange: (List<StrengthRoutineEntry>) -> Unit,
) {
    var displayEntries by remember { mutableStateOf(entries) }
    var draggingEntryId by remember { mutableStateOf<Int?>(null) }
    var draggingOverlayY by remember { mutableStateOf(0f) }
    var entryHeights by remember { mutableStateOf(emptyMap<Int, Int>()) }
    var entryRootYPositions by remember { mutableStateOf(emptyMap<Int, Float>()) }
    var listRootY by remember { mutableStateOf(0f) }
    var listRootHeight by remember { mutableStateOf(0) }
    var isSupersetSelectionMode by remember { mutableStateOf(false) }
    var selectedSupersetEntryIds by remember { mutableStateOf(emptySet<Int>()) }
    val supersetLabels = remember(displayEntries) { displayEntries.supersetGroupLabels() }
    val currentEntryId = entries.getOrNull(currentExerciseIndex)?.id

    LaunchedEffect(entries) {
        if (draggingEntryId == null) {
            displayEntries = entries
        }
        val entryIds = entries.map { it.id }.toSet()
        selectedSupersetEntryIds = selectedSupersetEntryIds.intersect(entryIds)
    }

    BackHandler(enabled = isSupersetSelectionMode) {
        isSupersetSelectionMode = false
        selectedSupersetEntryIds = emptySet()
    }

    fun startEntryDrag(entryId: Int) {
        if (isSupersetSelectionMode) return
        displayEntries = entries
        draggingEntryId = entryId
        draggingOverlayY = (entryRootYPositions[entryId] ?: listRootY) - listRootY
    }

    fun entryDragBounds(): Pair<Float, Float>? {
        val bounds = displayEntries.mapNotNull { entry ->
            val top = entryRootYPositions[entry.id] ?: return@mapNotNull null
            val height = entryHeights[entry.id] ?: return@mapNotNull null
            (top - listRootY) to (top - listRootY + height)
        }
        val top = bounds.minOfOrNull { it.first } ?: return null
        val bottom = bounds.maxOfOrNull { it.second } ?: return null
        return top to bottom
    }

    fun clampedEntryOverlayY(entryId: Int, overlayY: Float): Float {
        val itemHeight = (entryHeights[entryId] ?: 0).toFloat()
        val (listTop, listBottom) = entryDragBounds() ?: return overlayY
        val minOverlayY = listTop.coerceAtLeast(0f)
        val maxOverlayY = (listBottom - itemHeight)
            .coerceAtLeast(minOverlayY)
            .coerceAtMost((listRootHeight - itemHeight).coerceAtLeast(minOverlayY))
        return overlayY.coerceIn(minOverlayY, maxOverlayY)
    }

    fun updateEntryDrag(entryId: Int, deltaY: Float) {
        if (draggingEntryId != entryId) return
        val previousOverlayY = draggingOverlayY
        draggingOverlayY = clampedEntryOverlayY(entryId, draggingOverlayY + deltaY)
        val consumedDeltaY = draggingOverlayY - previousOverlayY
        if (consumedDeltaY == 0f) return
        val currentIndex = displayEntries.indexOfFirst { it.id == entryId }
        if (currentIndex < 0) return
        val draggedHeight = (entryHeights[entryId] ?: 0).toFloat()
        val overlayCenterY = draggingOverlayY + draggedHeight / 2f

        if (consumedDeltaY > 0f && currentIndex < displayEntries.lastIndex) {
            val nextEntry = displayEntries[currentIndex + 1]
            val nextTop = (entryRootYPositions[nextEntry.id] ?: return) - listRootY
            val nextHeight = (entryHeights[nextEntry.id] ?: 0).toFloat()
            val nextCenterY = nextTop + nextHeight / 2f
            if (overlayCenterY > nextCenterY) {
                displayEntries = displayEntries.moveItem(currentIndex, currentIndex + 1)
            }
        } else if (consumedDeltaY < 0f && currentIndex > 0) {
            val previousEntry = displayEntries[currentIndex - 1]
            val previousTop = (entryRootYPositions[previousEntry.id] ?: return) - listRootY
            val previousHeight = (entryHeights[previousEntry.id] ?: 0).toFloat()
            val previousCenterY = previousTop + previousHeight / 2f
            if (overlayCenterY < previousCenterY) {
                displayEntries = displayEntries.moveItem(currentIndex, currentIndex - 1)
            }
        }
    }

    fun endEntryDrag() {
        onEntriesChange(displayEntries)
        draggingEntryId = null
        draggingOverlayY = 0f
    }

    fun closeSupersetSelectionMode() {
        isSupersetSelectionMode = false
        selectedSupersetEntryIds = emptySet()
    }

    fun groupSelectedAsSuperset() {
        if (selectedSupersetEntryIds.size < 2) return
        val nextGroupId = (displayEntries.mapNotNull { it.supersetGroupId }.maxOrNull() ?: 0) + 1
        onEntriesChange(
            displayEntries
                .groupSelectedEntriesAsSuperset(
                    selectedEntryIds = selectedSupersetEntryIds,
                    supersetGroupId = nextGroupId
                )
                .normalizeSupersetGroups()
        )
        closeSupersetSelectionMode()
    }

    fun clearSelectedSupersetGroups() {
        val selectedGroupIds = displayEntries
            .filter { it.id in selectedSupersetEntryIds }
            .mapNotNull { it.supersetGroupId }
            .toSet()
        if (selectedGroupIds.isEmpty()) return
        onEntriesChange(
            displayEntries.map { entry ->
                if (entry.supersetGroupId in selectedGroupIds) {
                    entry.copy(supersetGroupId = null)
                } else {
                    entry
                }
            }.normalizeSupersetGroups()
        )
        closeSupersetSelectionMode()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                listRootY = coordinates.positionInRoot().y
                listRootHeight = coordinates.size.height
            }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
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
                        text = routine.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isSupersetSelectionMode) {
                item {
                    SupersetEditPanel(
                        isSelectionMode = isSupersetSelectionMode,
                        selectedCount = selectedSupersetEntryIds.size,
                        canClearSelectedGroups = entries.any {
                            it.id in selectedSupersetEntryIds && it.supersetGroupId != null
                        },
                        onGroupSelected = ::groupSelectedAsSuperset,
                        onClearSelectedGroups = ::clearSelectedSupersetGroups,
                        onCancel = ::closeSupersetSelectionMode
                    )
                }
            }
            itemsIndexed(displayEntries, key = { _, entry -> entry.id }) { _, entry ->
                val completedSets = entry.records.count { it.completed }
                val isComplete = entry.records.isNotEmpty() && completedSets == entry.records.size
                val isCurrent = entry.id == currentEntryId
                val isDragging = draggingEntryId == entry.id
                val isSupersetSelected = entry.id in selectedSupersetEntryIds
                val supersetLabel = entry.supersetGroupId?.let { supersetLabels[it] }
                val reorderModifier = if (isSupersetSelectionMode) {
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
                    StrengthOngoingExerciseRow(
                        entry = entry,
                        supersetLabel = supersetLabel,
                        completedSets = completedSets,
                        isComplete = isComplete,
                        isCurrent = isCurrent,
                        isSupersetSelectionMode = isSupersetSelectionMode,
                        isSupersetSelected = isSupersetSelected,
                        isDragging = false,
                        dragHandleModifier = Modifier,
                        modifier = Modifier.alpha(if (isDragging) 0f else 1f),
                        onClick = {
                            if (isSupersetSelectionMode) {
                                selectedSupersetEntryIds = if (entry.id in selectedSupersetEntryIds) {
                                    selectedSupersetEntryIds - entry.id
                                } else {
                                    selectedSupersetEntryIds + entry.id
                                }
                            } else {
                                entries.indexOfFirst { it.id == entry.id }
                                    .takeIf { it >= 0 }
                                    ?.let(onExerciseClick)
                            }
                        },
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { isSupersetSelectionMode = true },
                        enabled = displayEntries.size >= 2 && !isSupersetSelectionMode,
                        modifier = Modifier
                            .weight(1f)
                            .debugContentDescription(TestContentDescriptions.StrengthGroupSuperset),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("슈퍼세트 묶기", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    OutlinedButton(
                        onClick = onAddExercise,
                        modifier = Modifier
                            .weight(2f)
                            .debugContentDescription(TestContentDescriptions.StrengthAddExercise),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("신규 운동 추가", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
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
        val draggingEntry = draggingEntryId?.let { id -> displayEntries.firstOrNull { it.id == id } }
        if (draggingEntry != null) {
            val itemHeight = (entryHeights[draggingEntry.id] ?: 0).toFloat()
            val (listTop, listBottom) = entryDragBounds() ?: (0f to listRootHeight.toFloat())
            val minOverlayY = listTop.coerceAtLeast(0f)
            val maxOverlayY = (listBottom - itemHeight)
                .coerceAtLeast(minOverlayY)
                .coerceAtMost((listRootHeight - itemHeight).coerceAtLeast(minOverlayY))
            val overlayY = draggingOverlayY
                .coerceIn(minOverlayY, maxOverlayY)
            val completedSets = draggingEntry.records.count { it.completed }
            val isComplete = draggingEntry.records.isNotEmpty() && completedSets == draggingEntry.records.size
            StrengthOngoingExerciseRow(
                entry = draggingEntry,
                supersetLabel = draggingEntry.supersetGroupId?.let { supersetLabels[it] },
                completedSets = completedSets,
                isComplete = isComplete,
                isCurrent = draggingEntry.id == currentEntryId,
                isSupersetSelectionMode = false,
                isSupersetSelected = false,
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
            )
        }
    }
}

@Composable
internal fun StrengthOngoingExerciseRow(
    entry: StrengthRoutineEntry,
    supersetLabel: String?,
    completedSets: Int,
    isComplete: Boolean,
    isCurrent: Boolean,
    isSupersetSelectionMode: Boolean,
    isSupersetSelected: Boolean,
    isDragging: Boolean,
    dragHandleModifier: Modifier,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val baseContainerColor = when {
        isSupersetSelected -> MaterialTheme.colorScheme.primaryContainer
        isCurrent -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .debugContentDescription(TestContentDescriptions.strengthOngoingEntry(entry.id))
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
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(40.dp)
                    .then(if (isSupersetSelectionMode) Modifier else dragHandleModifier),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSupersetSelectionMode) {
                        if (isSupersetSelected) Icons.Outlined.CheckCircle else Icons.Outlined.FitnessCenter
                    } else {
                        Icons.Outlined.DragIndicator
                    },
                    contentDescription = if (isSupersetSelectionMode) {
                        if (isSupersetSelected) "선택됨" else "선택"
                    } else {
                        "길게 눌러 순서 변경"
                    },
                    tint = when {
                        isSupersetSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
            Icon(
                imageVector = if (isComplete) Icons.Outlined.CheckCircle else Icons.Outlined.FitnessCenter,
                contentDescription = null,
                tint = if (isComplete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                supersetLabel?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
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
                if (entry.note.isNotBlank()) {
                    Text(
                        text = entry.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Dialog preview for configured set details before execution.
 * This is not the active set screen; use [StrengthSetExecutionScreen] for performing sets.
 */
@Composable
internal fun StrengthExerciseSetDialog(
    entry: StrengthRoutineEntry,
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

/**
 * Sub-screen of [StrengthSessionScreen] for completing and editing sets during a workout.
 * Keep active-set completion and in-workout set edits here.
 */
/**
 * UI tests: StrengthSessionUiTest.setExecutionScreen_invokesExerciseChangeAndAddSetCallbacks.
 */
@Composable
internal fun StrengthSetExecutionScreen(
    entry: StrengthRoutineEntry?,
    recentHistory: List<CompletedStrengthExerciseHistory> = emptyList(),
    modifier: Modifier = Modifier,
    onExerciseClick: () -> Unit,
    onEntryChange: (StrengthRoutineEntry) -> Unit,
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
                        .debugContentDescription(TestContentDescriptions.StrengthSetExecutionExercise)
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
                            if (entry.note.isNotBlank()) {
                                Text(
                                    text = entry.note,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .debugContentDescription(TestContentDescriptions.StrengthSetExecutionAddSet),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("세트 추가")
                }
            }
            if (recentHistory.isNotEmpty()) {
                item {
                    StrengthExerciseRecentHistorySection(history = recentHistory)
                }
            }
        }
    }
}

@Composable
private fun StrengthExerciseRecentHistorySection(
    history: List<CompletedStrengthExerciseHistory>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "최근 수행 History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "같은 운동, 기구, 타입 기준",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "최근 ${history.size}개",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            history.forEachIndexed { index, item ->
                if (index > 0) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
                StrengthExerciseHistoryItem(item = item)
            }
        }
    }
}

@Composable
private fun StrengthExerciseHistoryItem(
    item: CompletedStrengthExerciseHistory,
) {
    val startedAt = remember(item.session.startedAtMillis) {
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(item.session.startedAtMillis),
            ZoneId.systemDefault()
        )
    }
    val rows = remember(item) { item.toStrengthExerciseHistoryRows() }
    val volume = remember(item) { item.historyVolumeKg() }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = startedAt.format(DateTimeFormatter.ofPattern("M/d HH:mm", Locale.KOREAN)),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = item.session.routineName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${rows.size}세트 · ${formatWeight(volume)}kg",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
        rows.take(5).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = row.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(48.dp)
                )
                Text(
                    text = row.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (rows.size > 5) {
            Text(
                text = "+${rows.size - 5}세트 더 있음",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class StrengthExerciseHistoryRow(
    val label: String,
    val detail: String,
)

private fun CompletedStrengthExerciseHistory.toStrengthExerciseHistoryRows(): List<StrengthExerciseHistoryRow> {
    if (setEvents.isNotEmpty()) {
        return setEvents.map { event ->
            val actualRestSeconds = session.restEvents
                .firstOrNull { rest -> rest.afterSetSequence == event.sequence }
                ?.actualSeconds
            StrengthExerciseHistoryRow(
                label = "Set ${event.setIndex + 1}",
                detail = strengthHistorySetDetail(
                    entry = entry,
                    weightKg = event.weightKg,
                    reps = event.reps,
                    plannedRestSeconds = event.targetRestSeconds,
                    actualRestSeconds = actualRestSeconds
                )
            )
        }
    }
    val records = entry.records
        .filter { record -> record.completed }
        .ifEmpty {
            entry.records.filter { record -> record.weightKg.isNotBlank() || record.reps.isNotBlank() }
        }
        .ifEmpty { entry.records }
    return records.mapIndexed { index, record ->
        StrengthExerciseHistoryRow(
            label = "Set ${index + 1}",
            detail = strengthHistorySetDetail(
                entry = entry,
                weightKg = record.weightKg.ifBlank { entry.targetWeightKg },
                reps = record.reps,
                plannedRestSeconds = record.restSeconds.toIntOrNull() ?: entry.restSeconds,
                actualRestSeconds = null
            )
        )
    }
}

private fun CompletedStrengthExerciseHistory.historyVolumeKg(): Double {
    val sideMultiplier = if (entry.isUnilateral()) 2.0 else 1.0
    if (setEvents.isNotEmpty()) {
        return setEvents.sumOf { event ->
            event.weightKg.firstNumberAsDouble() * event.reps.firstNumberAsInt() * sideMultiplier
        }
    }
    val records = entry.records
        .filter { record -> record.completed }
        .ifEmpty {
            entry.records.filter { record -> record.weightKg.isNotBlank() || record.reps.isNotBlank() }
        }
    return records.sumOf { record ->
        val weight = record.weightKg.firstNumberAsDouble()
            .takeIf { it > 0.0 }
            ?: entry.targetWeightKg.firstNumberAsDouble()
        val reps = record.reps.firstNumberAsInt()
            .takeIf { it > 0 }
            ?: entry.targetReps
        weight * reps * sideMultiplier
    }
}

private fun strengthHistorySetDetail(
    entry: StrengthRoutineEntry,
    weightKg: String,
    reps: String,
    plannedRestSeconds: Int,
    actualRestSeconds: Int?,
): String {
    val weight = strengthHistoryWeightText(entry, weightKg)
    val repsText = if (entry.isUnilateral()) {
        "각 ${displayRepsText(reps).removeSuffix("회")}회"
    } else {
        displayRepsText(reps)
    }
    val plannedRest = plannedRestSeconds.takeIf { it > 0 }?.toString() ?: "-"
    val actualRest = actualRestSeconds?.let { " · 실제 ${formatClock(it)}" }.orEmpty()
    return "$weight x $repsText · 휴식 ${plannedRest}초$actualRest"
}

private fun strengthHistoryWeightText(
    entry: StrengthRoutineEntry,
    weightKg: String,
): String {
    val value = weightKg.trim()
    if (entry.weightInputUnitLabel() == "체중" && value.isBlank()) return "체중"
    return displayWeightText(value.ifBlank { "-" })
}

private fun String.firstNumberAsDouble(): Double {
    return Regex("""\d+(?:\.\d+)?""").find(this)?.value?.toDoubleOrNull() ?: 0.0
}

private fun String.firstNumberAsInt(): Int {
    return Regex("""\d+""").find(this)?.value?.toIntOrNull() ?: 0
}

/**
 * UI tests: StrengthSessionUiTest.setBottomBar_completeButtonInvokesCallback.
 */
@Composable
internal fun StrengthSetBottomBar(
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
                    .height(52.dp)
                    .debugContentDescription(TestContentDescriptions.StrengthCompleteSet),
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

/**
 * UI tests: StrengthSessionUiTest.finishBar_invokesFinishWhenNotUploadingAndDisablesWhileUploading.
 */
@Composable
internal fun StrengthSessionFinishBar(
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
                .height(52.dp)
                .debugContentDescription(TestContentDescriptions.StrengthFinishWorkout),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Outlined.CloudUpload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isUploading) "업로드 중" else "운동 종료")
        }
    }
}

/**
 * Bottom sheet used by [StrengthSessionScreen] during rest.
 * Do not create a separate rest screen; overlay and notification behavior are coordinated from the session screen.
 * UI tests: StrengthSessionUiTest.restTimerBottomSheet_stopButtonInvokesCallback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RestTimerBottomSheet(
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
                modifier = Modifier
                    .fillMaxWidth()
                    .debugContentDescription(TestContentDescriptions.StrengthRestStop),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("휴식 중단")
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/**
 * UI tests: StrengthSessionUiTest.restTimeControls_invokeAdjustAndSetCallbacks.
 */
@Composable
internal fun RestTimeControls(
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

/**
 * UI tests: StrengthSessionUiTest.restTimeControls_invokeAdjustAndSetCallbacks.
 */
@Composable
internal fun RestTimeBubble(
    text: String,
    onClick: () -> Unit,
) {
    MaterialSurface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier
            .debugContentDescription(TestContentDescriptions.strengthRestTimeControl(text))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
        )
    }
}

/**
 * UI tests: StrengthSessionUiTest.restTimerFloatingChip_displaysRemainingTimeAndInvokesClick.
 */
@Composable
internal fun RestTimerFloatingChip(
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
            .debugContentDescription(TestContentDescriptions.StrengthRestFloatingChip)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.86f),
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

/**
 * UI tests: StrengthSessionUiTest.uploadPanel_invokesUploadOnlyWhenEntriesAreAvailable,
 * uploadPanel_displaysSyncMessagesAndDisablesWhileUploading.
 */
@Composable
internal fun StrengthUploadPanel(
    apiKey: String,
    routineName: String,
    entries: List<StrengthRoutineEntry>,
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
                text = "$routineName · $completedSets/$totalSets 세트 완료 · 볼륨 ${formatWeight(volume)} kg · 예상 Load $estimatedLoad",
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
                modifier = Modifier
                    .fillMaxWidth()
                    .debugContentDescription(TestContentDescriptions.StrengthUploadWorkout),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Outlined.CloudUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isUploading) "업로드 중" else "운동 완료")
            }
        }
    }
}

/**
 * Legacy/manual strength workout surface kept for older entry points.
 * Prefer [StrengthSessionScreen] for the current routed strength workout flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StrengthSessionScreen(
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
    var nextRoutineId by remember { mutableIntStateOf(1) }
    var routineEntries by remember { mutableStateOf<List<StrengthRoutineEntry>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    val forcedUnilateral = selectedExercise.forcedUnilateralModeForVariation(selectedVariation)
    val effectiveUnilateral = forcedUnilateral ?: selectedUnilateral

    val candidates = remember(searchQuery) {
        strengthExerciseCatalog
            .filter { exercise -> exercise.matchesSearch(searchQuery) }
            .take(12)
    }

    fun selectExercise(exercise: StrengthExercise) {
        selectedExercise = exercise
        selectedEquipment = exercise.equipmentOptions.first()
        selectedVariation = exercise.baseVariationOptions().first()
        selectedUnilateral = exercise.forcedUnilateralModeForVariation(selectedVariation) ?: "양쪽"
    }

    fun updateEntry(entry: StrengthRoutineEntry) {
        routineEntries = routineEntries.map { if (it.id == entry.id) entry else it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("웨이트 Routine & 기록") },
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
                                title = exercise.searchResultTitle(searchQuery),
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
                            onSelected = {
                                selectedVariation = it
                                selectedUnilateral = selectedExercise.forcedUnilateralModeForVariation(it) ?: selectedUnilateral
                            }
                        )
                        ChoiceGrid(
                            title = "좌우 방식",
                            options = UNILATERAL_MODE_OPTIONS,
                            selected = effectiveUnilateral,
                            onSelected = { if (forcedUnilateral == null) selectedUnilateral = it },
                            isOptionEnabled = { forcedUnilateral == null || it == forcedUnilateral }
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
                                routineEntries = routineEntries + StrengthRoutineEntry(
                                    id = nextRoutineId,
                                    exercise = selectedExercise,
                                    equipment = selectedEquipment,
                                    variation = combineVariationAndUnilateral(selectedVariation, effectiveUnilateral),
                                    supersetGroupId = null,
                                    targetSets = sets,
                                    targetReps = reps,
                                    restSeconds = rest,
                                    targetWeightKg = targetWeight,
                                    records = records
                                )
                                nextRoutineId += 1
                                uploadMessage = null
                                uploadError = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Outlined.FitnessCenter, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Routine에 추가")
                        }
                    }
                }
            }

            if (routineEntries.isEmpty()) {
                item {
                    EmptyView(message = "운동을 선택하고 Routine에 추가하세요.")
                }
            } else {
                items(routineEntries, key = { it.id }) { entry ->
                    StrengthRoutineEntryCard(
                        entry = entry,
                        onEntryChange = ::updateEntry,
                        onDelete = {
                            routineEntries = routineEntries.filterNot { it.id == entry.id }
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
                        val completedSets = routineEntries.sumOf { entry -> entry.records.count { it.completed } }
                        val totalSets = routineEntries.sumOf { it.records.size }
                        val volume = routineEntries.totalVolumeKg()
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
                                        repository.uploadStrengthSession(
                                            StrengthSession(
                                                name = workoutName.ifBlank { "웨이트 트레이닝" },
                                                startedAt = LocalDateTime.now().minusSeconds(
                                                    routineEntries.totalDurationSeconds().toLong()
                                                ),
                                                entries = routineEntries,
                                                rpe = 7,
                                                trainingLoad = routineEntries.strengthTrainingLoad(7)
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
                            enabled = routineEntries.isNotEmpty() && !isUploading,
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

/**
 * UI tests: StrengthRoutineEditUiTest.exerciseConfigDialog_completesWithInferredSearchDefaults,
 * StrengthExerciseListUiTest.exerciseList_searchShowsMatchingExercisesWithoutSetEmptyView.
 */
@Composable
internal fun ExerciseSearchRow(
    exercise: StrengthExercise,
    title: String = exercise.nameKo,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .debugContentDescription(TestContentDescriptions.strengthExerciseSearchResult(exercise.id))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
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

/**
 * UI tests: StrengthRoutineEditUiTest.exerciseTypeDialog_completesSelectedEquipmentVariationAndUnilateral,
 * exerciseConfigDialog_completesWithInferredSearchDefaults.
 */
@Composable
internal fun ChoiceGrid(
    title: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    isOptionEnabled: (String) -> Boolean = { true },
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
                            enabled = isOptionEnabled(option),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .debugContentDescription(TestContentDescriptions.strengthChoiceOption(title, option))
                        ) {
                            Text(option)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onSelected(option) },
                            enabled = isOptionEnabled(option),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .debugContentDescription(TestContentDescriptions.strengthChoiceOption(title, option))
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
internal fun NumberField(
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
internal fun StrengthRoutineEntryCard(
    entry: StrengthRoutineEntry,
    onEntryChange: (StrengthRoutineEntry) -> Unit,
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
internal fun StrengthSetRecordRow(
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
    val resetSwipeEnabled = record.completed && showCompletion

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CompletedSetResetSwipeContainer(
            key = record.id,
            enabled = resetSwipeEnabled,
            modifier = Modifier.debugContentDescription(TestContentDescriptions.strengthSetRecordRow(record.id)),
            onResetRequested = { onRecordChange(record.copy(completed = false)) }
        ) { resetSwipeModifier ->
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
                    modifier = resetSwipeModifier
                        .then(swipeModifier)
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
private fun CompletedSetResetSwipeContainer(
    key: Any,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onResetRequested: () -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val viewConfiguration = LocalViewConfiguration.current
    val swipeOffsetX = remember(key) { Animatable(0f) }
    var rowWidth by remember(key) { mutableIntStateOf(0) }
    val resetThreshold = with(density) { 92.dp.toPx() }
    val maxDragOffset = with(density) { 144.dp.toPx() }
    val touchSlop = viewConfiguration.touchSlop

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (enabled) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.62f) else Color.Transparent)
            .onSizeChanged { rowWidth = it.width }
    ) {
        if (enabled) {
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .padding(end = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "미완료",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Outlined.RestartAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        val contentModifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(swipeOffsetX.value.roundToInt(), 0) }
            .then(
                if (enabled) {
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
                                    if (swipeOffsetX.value <= -resetThreshold) {
                                        swipeOffsetX.animateTo(
                                            targetValue = -rowWidth.toFloat().coerceAtLeast(maxDragOffset),
                                            animationSpec = tween(160)
                                        )
                                        onResetRequested()
                                        swipeOffsetX.snapTo(0f)
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
        content(contentModifier)
    }
}

@Composable
internal fun UnilateralSetSideRow(
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
internal fun SetMetricField(
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

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
 * This is the single entry point for strength plan preview, ongoing workout list, set execution, rest timer, and finish/upload state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StrengthWorkoutSessionScreen(
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
internal fun StrengthWorkoutTopBarTitle(
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

/**
 * Sub-screen of [StrengthWorkoutSessionScreen] shown before a strength workout starts.
 * Keep pre-start exercise expansion and edit/start actions here.
 */
@Composable
internal fun StrengthWorkoutReadyScreen(
    plan: StrengthWorkoutPlan,
    entries: List<StrengthPlanEntry>,
    modifier: Modifier = Modifier,
    onStart: () -> Unit,
    onEditPlan: (() -> Unit)?,
) {
    var expandedEntryIds by remember(plan.id, entries) { mutableStateOf(emptySet<Int>()) }
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
                if (onEditPlan != null) {
                    OutlinedButton(
                        onClick = onEditPlan,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
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
                        .weight(if (onEditPlan != null) 2f else 1f)
                        .height(52.dp),
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

/**
 * Sub-screen of [StrengthWorkoutSessionScreen] for the in-progress exercise list.
 * It coordinates exercise switching while set execution stays in [StrengthSetExecutionScreen].
 */
@Composable
internal fun StrengthWorkoutOngoingPlanScreen(
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
internal fun StrengthOngoingExerciseRow(
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

/**
 * Dialog preview for configured set details before execution.
 * This is not the active set screen; use [StrengthSetExecutionScreen] for performing sets.
 */
@Composable
internal fun StrengthExerciseSetDialog(
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

/**
 * Sub-screen of [StrengthWorkoutSessionScreen] for completing and editing sets during a workout.
 * Keep active-set completion and in-workout set edits here.
 */
@Composable
internal fun StrengthSetExecutionScreen(
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
internal fun StrengthWorkoutFinishBar(
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

/**
 * Bottom sheet used by [StrengthWorkoutSessionScreen] during rest.
 * Do not create a separate rest screen; overlay and notification behavior are coordinated from the session screen.
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

@Composable
internal fun RestTimeBubble(
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
internal fun StrengthUploadPanel(
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

/**
 * Legacy/manual strength workout surface kept for older entry points.
 * Prefer [StrengthWorkoutSessionScreen] for the current routed strength workout flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StrengthWorkoutScreen(
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

@Composable
internal fun ChoiceGrid(
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
internal fun StrengthPlanEntryCard(
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

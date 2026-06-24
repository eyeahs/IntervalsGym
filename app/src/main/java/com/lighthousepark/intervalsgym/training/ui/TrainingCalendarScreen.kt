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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
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
import androidx.compose.ui.geometry.Rect
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
import androidx.compose.ui.unit.IntSize
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

private data class PendingCalendarPlanMove(
    val sourcePlan: TrainingItem,
    val targetDate: LocalDate,
) {
    val key: String = sourcePlan.calendarMoveKey()
    val targetExternalId: String = sourcePlan.pendingMoveTargetExternalId(targetDate)
}

private data class CalendarPlanRenderData(
    val plans: List<TrainingItem>,
    val pendingPlanKeys: Set<String>,
)

private data class CalendarPlanDragOverlayState(
    val item: TrainingItem,
    val previewRootPosition: Offset,
    val previewSize: IntSize,
    val grabOffset: Offset,
    val scale: Float,
)

private enum class CalendarPlanDragAction {
    CANCEL,
    DELETE
}

private fun TrainingItem.calendarMoveKey(): String {
    return externalId?.takeIf { it.isNotBlank() }
        ?: remoteId.takeIf { it.isNotBlank() }
        ?: id
}

private fun TrainingItem.calendarIdentityKeys(): Set<String> {
    return listOf(id, remoteId, externalId, calendarMoveKey())
        .filterNotNull()
        .filter { it.isNotBlank() }
        .toSet()
}

private fun TrainingItem.hasCalendarIdentityIn(keys: Set<String>): Boolean {
    return calendarIdentityKeys().any { it in keys }
}

private fun TrainingItem.pendingMoveTargetExternalId(targetDate: LocalDate): String {
    return matchedStrengthPlan?.intervalsPlanExternalId(targetDate)
        ?: movedCalendarPlanExternalId(targetDate)
}

private fun TrainingItem.movedCalendarPlanExternalId(date: LocalDate): String {
    val sourceId = remoteId.ifBlank { id }.replace(Regex("""[^A-Za-z0-9_.-]"""), "-")
    return "intervals-gym-moved-plan-$sourceId-$date"
}

private fun TrainingItem.withPendingMoveDate(move: PendingCalendarPlanMove): TrainingItem {
    val movedStart = startedAt?.let { move.targetDate.atTime(it.toLocalTime()) }
        ?: move.targetDate.atStartOfDay()
    return copy(
        id = "pending-move-${move.key}-${move.targetDate}",
        externalId = move.targetExternalId,
        date = move.targetDate,
        startedAt = movedStart,
        timeLabel = movedStart.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
    )
}

private fun TrainingItem.isPendingMoveSource(move: PendingCalendarPlanMove): Boolean {
    return date == move.sourcePlan.date &&
        listOfNotNull(id, remoteId, externalId).any { key ->
            key == move.sourcePlan.id ||
                key == move.sourcePlan.remoteId ||
                key == move.sourcePlan.externalId ||
                key == move.key
        }
}

private fun TrainingItem.isPendingMoveTarget(move: PendingCalendarPlanMove): Boolean {
    return date == move.targetDate &&
        listOfNotNull(id, remoteId, externalId).any { key ->
            key == move.targetExternalId ||
                key == "pending-move-${move.key}-${move.targetDate}"
        }
}

private fun List<TrainingItem>.withPendingCalendarPlanMoves(
    pendingMoves: Collection<PendingCalendarPlanMove>,
    start: LocalDate,
    end: LocalDate,
): CalendarPlanRenderData {
    if (pendingMoves.isEmpty()) return CalendarPlanRenderData(plans = this, pendingPlanKeys = emptySet())

    val moves = pendingMoves
        .filter { move ->
            !move.sourcePlan.date.isBefore(start) && !move.sourcePlan.date.isAfter(end) ||
                !move.targetDate.isBefore(start) && !move.targetDate.isAfter(end)
        }
    if (moves.isEmpty()) return CalendarPlanRenderData(plans = this, pendingPlanKeys = emptySet())

    val withoutSources = filterNot { item -> moves.any { move -> item.isPendingMoveSource(move) } }
    val pendingTargets = mutableSetOf<String>()
    val syntheticTargets = moves
        .filter { move -> !move.targetDate.isBefore(start) && !move.targetDate.isAfter(end) }
        .filter { move -> withoutSources.none { item -> item.isPendingMoveTarget(move) } }
        .map { move -> move.sourcePlan.withPendingMoveDate(move) }

    (withoutSources + syntheticTargets).forEach { item ->
        moves.firstOrNull { move -> item.isPendingMoveTarget(move) }?.let { move ->
            pendingTargets += move.key
            pendingTargets += move.targetExternalId
            pendingTargets += item.id
            pendingTargets += item.remoteId
            item.externalId?.let(pendingTargets::add)
        }
    }

    return CalendarPlanRenderData(
        plans = withoutSources + syntheticTargets,
        pendingPlanKeys = pendingTargets
    )
}

private fun TrainingItem.isApiPendingMove(pendingPlanKeys: Set<String>): Boolean {
    val plan = calendarPlanForMove() ?: this
    return listOfNotNull(plan.id, plan.remoteId, plan.externalId, plan.calendarMoveKey())
        .any { key -> key in pendingPlanKeys }
}

private fun Collection<PendingCalendarPlanMove>.withoutReflectedMoves(plans: List<TrainingItem>): Map<String, PendingCalendarPlanMove> {
    return filterNot { move ->
        val hasTarget = plans.any { plan -> plan.isPendingMoveTarget(move) && !plan.id.startsWith("pending-move-") }
        val hasSource = plans.any { plan -> plan.isPendingMoveSource(move) }
        hasTarget && !hasSource
    }.associateBy { move -> move.key }
}

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
    initialDate: LocalDate = LocalDate.now(),
    initialCalendarMode: TrainingCalendarMode = TrainingCalendarMode.WEEK,
    showBackButton: Boolean = false,
    showCalendarModeButton: Boolean = true,
    onPlanSelected: (TrainingItem) -> Unit,
    onIntervalStrengthPlanSelected: (TrainingItem?, StrengthWorkoutPlan) -> Unit,
    onMonthDaySelected: (LocalDate) -> Unit = {},
    onStrengthWorkout: () -> Unit,
    onRunningWorkout: () -> Unit,
    onLoginClick: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val density = LocalDensity.current
    val prefs = remember(context) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var localStrengthHistory by remember { mutableStateOf(loadCompletedStrengthWorkoutHistory(prefs)) }
    var localRunningHistory by remember { mutableStateOf(loadCompletedRunningWorkoutHistory(prefs)) }
    var localScheduledStrengthPlans by remember { mutableStateOf(loadScheduledStrengthPlans(prefs)) }
    val repository = remember(apiKey) { IntervalsRepository(apiKey) }
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
    var pendingCalendarPlanMoves by remember(apiKey) { mutableStateOf<Map<String, PendingCalendarPlanMove>>(emptyMap()) }
    var optimisticallyDeletedCalendarPlanKeys by remember(apiKey) { mutableStateOf(emptySet<String>()) }
    var isCalendarPlanDragging by remember { mutableStateOf(false) }
    var calendarDragDropTargetDate by remember { mutableStateOf<LocalDate?>(null) }
    var calendarDragPointerRootPosition by remember { mutableStateOf<Offset?>(null) }
    var calendarDragOverlayState by remember { mutableStateOf<CalendarPlanDragOverlayState?>(null) }
    var calendarDragActionBounds by remember { mutableStateOf<Map<CalendarPlanDragAction, Rect>>(emptyMap()) }
    var calendarContentRootPosition by remember { mutableStateOf(Offset.Zero) }
    var calendarContentRootSize by remember { mutableStateOf(IntSize.Zero) }

    fun resetCalendarDragUiState() {
        calendarDragDropTargetDate = null
        calendarDragPointerRootPosition = null
        calendarDragOverlayState = null
        calendarDragActionBounds = emptyMap()
    }

    fun calendarDragActionAt(rootPosition: Offset): CalendarPlanDragAction? {
        return calendarDragActionBounds.entries.lastOrNull { (_, bounds) ->
            rootPosition.x in bounds.left..bounds.right &&
                rootPosition.y in bounds.top..bounds.bottom
        }?.key
    }

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
                pendingCalendarPlanMoves = pendingCalendarPlanMoves.values.withoutReflectedMoves(data.plans)
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

    fun movePlanToDate(item: TrainingItem, targetDate: LocalDate) {
        val sourcePlan = item.calendarPlanForMove() ?: return
        if (sourcePlan.date == targetDate) return
        val pendingMove = PendingCalendarPlanMove(sourcePlan = sourcePlan, targetDate = targetDate)
        if (apiKey.isNotBlank()) {
            pendingCalendarPlanMoves = pendingCalendarPlanMoves + (pendingMove.key to pendingMove)
        }
        val movedPlan = moveScheduledStrengthPlan(prefs, sourcePlan, targetDate)
        if (movedPlan == null && apiKey.isBlank()) {
            android.widget.Toast.makeText(context, "이동할 수 있는 로컬 웨이트 plan이 아닙니다.", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        if (movedPlan != null) {
            localScheduledStrengthPlans = loadScheduledStrengthPlans(prefs)
            android.widget.Toast.makeText(
                context,
                "${sourcePlan.name.ifBlank { "Plan" }} ${targetDate.monthValue}/${targetDate.dayOfMonth}로 이동됨",
                android.widget.Toast.LENGTH_SHORT
            ).show()

            if (apiKey.isBlank()) return
        } else {
            android.widget.Toast.makeText(
                context,
                "${sourcePlan.name.ifBlank { "Plan" }} ${targetDate.monthValue}/${targetDate.dayOfMonth}로 이동 중...",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        scope.launch {
            try {
                if (movedPlan != null) {
                    repository.uploadStrengthPlan(movedPlan.plan, targetDate)
                    upsertScheduledStrengthPlan(prefs, movedPlan.copy(uploadedToIntervals = true))
                    localScheduledStrengthPlans = loadScheduledStrengthPlans(prefs)
                    if (sourcePlan.id.startsWith("plan-") && sourcePlan.remoteId.isNotBlank()) {
                        repository.deleteCalendarPlan(sourcePlan.remoteId)
                        removeCalendarPlanFromIntervalsCaches(prefs, apiKey, sourcePlan)
                    }
                } else {
                    repository.uploadCalendarPlanCopy(sourcePlan, targetDate)
                    repository.deleteCalendarPlan(sourcePlan.remoteId)
                    removeCalendarPlanFromIntervalsCaches(prefs, apiKey, sourcePlan)
                }
                refresh(selectedRange, forceSync = true)
            } catch (error: Exception) {
                pendingCalendarPlanMoves = pendingCalendarPlanMoves - pendingMove.key
                android.widget.Toast.makeText(
                    context,
                    if (movedPlan != null) {
                        "로컬 일정은 이동됐지만 Intervals.icu 반영은 실패했습니다."
                    } else {
                        "Intervals.icu plan 이동에 실패했습니다."
                    },
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun deleteDraggedCalendarPlan(item: TrainingItem) {
        val targetPlan = item.calendarPlanForMove() ?: return
        val deleteKeys = targetPlan.calendarIdentityKeys()
        val shouldDeleteRemote = apiKey.isNotBlank() && !targetPlan.id.startsWith("local-")
        if (shouldDeleteRemote) {
            optimisticallyDeletedCalendarPlanKeys = optimisticallyDeletedCalendarPlanKeys + deleteKeys
        }
        pendingCalendarPlanMoves = pendingCalendarPlanMoves - targetPlan.calendarMoveKey()

        if (!shouldDeleteRemote) {
            removeScheduledStrengthPlan(prefs, targetPlan)
            localScheduledStrengthPlans = loadScheduledStrengthPlans(prefs)
            android.widget.Toast.makeText(
                context,
                "${targetPlan.name.ifBlank { "Plan" }} 삭제됨",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }

        scope.launch {
            try {
                repository.deleteCalendarPlan(targetPlan.remoteId)
                removeCalendarPlanFromIntervalsCaches(prefs, apiKey, targetPlan)
                removeScheduledStrengthPlan(prefs, targetPlan)
                localScheduledStrengthPlans = loadScheduledStrengthPlans(prefs)
                android.widget.Toast.makeText(
                    context,
                    "${targetPlan.name.ifBlank { "Plan" }} 삭제됨",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                refresh(selectedRange, forceSync = true)
            } catch (error: Exception) {
                optimisticallyDeletedCalendarPlanKeys = optimisticallyDeletedCalendarPlanKeys - deleteKeys
                android.widget.Toast.makeText(
                    context,
                    error.message ?: "Plan을 삭제하지 못했습니다.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun shiftCalendarPlanDragToAdjacentWeek(direction: Int) {
        if (calendarMode != TrainingCalendarMode.WEEK) return
        scope.launch {
            val targetPage = (pagerState.settledPage + direction).coerceIn(0, Int.MAX_VALUE - 1)
            pagerState.animateScrollToPage(targetPage)
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
            onRunningClick = {
                showWorkoutActionSheet = false
                onRunningWorkout()
            },
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
            if (!isCalendarPlanDragging) {
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
            }
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
                    if (today < selectedRange.start || today > selectedRange.end) {
                        IconButton(
                            onClick = {
                                val targetPage = initialPage + calendarMode.pageOffsetForDate(baseDate, today).toInt()
                                scope.launch {
                                    pagerState.animateScrollToPage(targetPage)
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
                    if (showCalendarModeButton) {
                        IconButton(
                            onClick = { calendarMode = calendarMode.next() }
                        ) {
                            CalendarModeIcon(
                                mode = calendarMode,
                                modifier = Modifier.size(24.dp)
                            )
                        }
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
                },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "뒤로"
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
                .onGloballyPositioned { coordinates ->
                    calendarContentRootPosition = coordinates.positionInRoot()
                    calendarContentRootSize = coordinates.size
                }
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = !isCalendarPlanDragging,
                modifier = Modifier.fillMaxSize()
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
            }.filterNot {
                it.id in deletedCalendarPlanIds ||
                    it.remoteId in deletedCalendarPlanIds ||
                    it.hasCalendarIdentityIn(optimisticallyDeletedCalendarPlanKeys)
            }
            val basePagePlans = remotePagePlans.withLocalStrengthPlans(
                scheduledPlans = localScheduledStrengthPlans,
                start = pageRange.start,
                end = pageRange.end
            ).filterNot { it.hasCalendarIdentityIn(optimisticallyDeletedCalendarPlanKeys) }
            val pagePlanRenderData = basePagePlans.withPendingCalendarPlanMoves(
                pendingMoves = pendingCalendarPlanMoves.values,
                start = pageRange.start,
                end = pageRange.end
            )
            val pagePlans = pagePlanRenderData.plans
            val movableScheduledPlanKeys = localScheduledStrengthPlans.flatMap { scheduled ->
                listOf(
                    scheduled.id,
                    "local-${scheduled.id}",
                    scheduled.externalId
                )
            }.toSet()
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
                                onIntervalStrengthPlanSelected = onIntervalStrengthPlanSelected,
                                onDaySelected = onMonthDaySelected
                            )
                        } else {
                            TrainingList(
                                days = pageRange.days(),
                                items = sortedPageItems,
                                emptyMessage = "주간 훈련 계획 없음",
                                onPlanSelected = onPlanSelected,
                                onIntervalStrengthPlanSelected = onIntervalStrengthPlanSelected,
                                movablePlanKeys = movableScheduledPlanKeys,
                                canMoveRemotePlans = apiKey.isNotBlank(),
                                onPlanDateChanged = ::movePlanToDate,
                                onPlanDeleteRequested = ::deleteDraggedCalendarPlan,
                                onDragWeekShiftRequested = ::shiftCalendarPlanDragToAdjacentWeek,
                                onDragDropTargetDateChanged = { calendarDragDropTargetDate = it },
                                onDragPointerRootPositionChanged = { calendarDragPointerRootPosition = it },
                                onDragOverlayChanged = { calendarDragOverlayState = it },
                                onDragStateChanged = { isDragging ->
                                    isCalendarPlanDragging = isDragging
                                    if (isDragging) {
                                        showFabActions = false
                                    } else {
                                        resetCalendarDragUiState()
                                    }
                                },
                                externalDropTargetDate = calendarDragDropTargetDate,
                                externalDragPointerRootPosition = calendarDragPointerRootPosition,
                                shouldUpdateExternalDropTargetFromPointer = isCalendarPlanDragging && page == pagerState.currentPage,
                                externalDragActionBounds = calendarDragActionBounds,
                                dragViewportBounds = Rect(
                                    left = calendarContentRootPosition.x,
                                    top = calendarContentRootPosition.y,
                                    right = calendarContentRootPosition.x + calendarContentRootSize.width,
                                    bottom = calendarContentRootPosition.y + calendarContentRootSize.height
                                ),
                                renderLocalDragOverlay = false,
                                pendingApiMovePlanKeys = pagePlanRenderData.pendingPlanKeys,
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
                                onIntervalStrengthPlanSelected = onIntervalStrengthPlanSelected,
                                onDaySelected = onMonthDaySelected
                            )
                        } else {
                            TrainingList(
                                days = pageRange.days(),
                                items = sortedPageItems,
                                emptyMessage = "주간 훈련 계획 없음",
                                onPlanSelected = onPlanSelected,
                                onIntervalStrengthPlanSelected = onIntervalStrengthPlanSelected,
                                movablePlanKeys = movableScheduledPlanKeys,
                                canMoveRemotePlans = apiKey.isNotBlank(),
                                onPlanDateChanged = ::movePlanToDate,
                                onPlanDeleteRequested = ::deleteDraggedCalendarPlan,
                                onDragWeekShiftRequested = ::shiftCalendarPlanDragToAdjacentWeek,
                                onDragDropTargetDateChanged = { calendarDragDropTargetDate = it },
                                onDragPointerRootPositionChanged = { calendarDragPointerRootPosition = it },
                                onDragOverlayChanged = { calendarDragOverlayState = it },
                                onDragStateChanged = { isDragging ->
                                    isCalendarPlanDragging = isDragging
                                    if (isDragging) {
                                        showFabActions = false
                                    } else {
                                        resetCalendarDragUiState()
                                    }
                                },
                                externalDropTargetDate = calendarDragDropTargetDate,
                                externalDragPointerRootPosition = calendarDragPointerRootPosition,
                                shouldUpdateExternalDropTargetFromPointer = isCalendarPlanDragging && page == pagerState.currentPage,
                                externalDragActionBounds = calendarDragActionBounds,
                                dragViewportBounds = Rect(
                                    left = calendarContentRootPosition.x,
                                    top = calendarContentRootPosition.y,
                                    right = calendarContentRootPosition.x + calendarContentRootSize.width,
                                    bottom = calendarContentRootPosition.y + calendarContentRootSize.height
                                ),
                                renderLocalDragOverlay = false,
                                pendingApiMovePlanKeys = pagePlanRenderData.pendingPlanKeys,
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
            val activeCalendarDragAction = calendarDragPointerRootPosition?.let(::calendarDragActionAt)
            AnimatedVisibility(
                visible = isCalendarPlanDragging,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
                    .zIndex(4f),
                enter = fadeIn(animationSpec = tween(120)),
                exit = fadeOut(animationSpec = tween(100))
            ) {
                CalendarPlanDragActionButtons(
                    activeAction = activeCalendarDragAction,
                    onActionPositioned = { action, bounds ->
                        calendarDragActionBounds = calendarDragActionBounds + (action to bounds)
                    }
                )
            }
            calendarDragOverlayState?.let { overlay ->
                if (overlay.previewSize.width > 0) {
                    TrainingItemRow(
                        item = overlay.item,
                        onClick = {},
                        modifier = Modifier
                            .zIndex(5f)
                            .offset {
                                IntOffset(
                                    x = (overlay.previewRootPosition.x -
                                        calendarContentRootPosition.x +
                                        overlay.grabOffset.x * (1f - overlay.scale)).roundToInt(),
                                    y = (overlay.previewRootPosition.y -
                                        calendarContentRootPosition.y +
                                        overlay.grabOffset.y * (1f - overlay.scale)).roundToInt()
                                )
                            }
                            .width(with(density) { overlay.previewSize.width.toDp() })
                            .graphicsLayer {
                                shadowElevation = 18f
                                scaleX = overlay.scale
                                scaleY = overlay.scale
                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
                            }
                    )
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
                    text = "plan 계획 추가",
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
                        text = "plan 계획 추가",
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
private fun TrainingList(
    days: List<LocalDate>,
    items: List<TrainingItem>,
    emptyMessage: String,
    onPlanSelected: (TrainingItem) -> Unit,
    onIntervalStrengthPlanSelected: (TrainingItem?, StrengthWorkoutPlan) -> Unit,
    movablePlanKeys: Set<String> = emptySet(),
    canMoveRemotePlans: Boolean = false,
    onPlanDateChanged: (TrainingItem, LocalDate) -> Unit = { _, _ -> },
    onPlanDeleteRequested: (TrainingItem) -> Unit = {},
    onDragWeekShiftRequested: (Int) -> Unit = {},
    onDragDropTargetDateChanged: (LocalDate?) -> Unit = {},
    onDragPointerRootPositionChanged: (Offset?) -> Unit = {},
    onDragOverlayChanged: (CalendarPlanDragOverlayState?) -> Unit = {},
    onDragStateChanged: (Boolean) -> Unit = {},
    externalDropTargetDate: LocalDate? = null,
    externalDragPointerRootPosition: Offset? = null,
    shouldUpdateExternalDropTargetFromPointer: Boolean = false,
    externalDragActionBounds: Map<CalendarPlanDragAction, Rect> = emptyMap(),
    dragViewportBounds: Rect? = null,
    renderLocalDragOverlay: Boolean = true,
    pendingApiMovePlanKeys: Set<String> = emptySet(),
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
    val currentOnPlanDateChanged by rememberUpdatedState(onPlanDateChanged)
    val currentOnPlanDeleteRequested by rememberUpdatedState(onPlanDeleteRequested)
    val currentOnDragWeekShiftRequested by rememberUpdatedState(onDragWeekShiftRequested)
    val currentOnDragDropTargetDateChanged by rememberUpdatedState(onDragDropTargetDateChanged)
    val currentOnDragPointerRootPositionChanged by rememberUpdatedState(onDragPointerRootPositionChanged)
    val currentOnDragOverlayChanged by rememberUpdatedState(onDragOverlayChanged)
    val currentOnDragStateChanged by rememberUpdatedState(onDragStateChanged)
    val listState = rememberLazyListState()
    var headerHeightPx by remember { mutableIntStateOf(0) }
    var headerOffsetPx by remember { mutableFloatStateOf(0f) }
    var didInitialScroll by remember(initialScrollDate, days) { mutableStateOf(false) }
    var listRootPosition by remember { mutableStateOf(Offset.Zero) }
    var listRootSize by remember { mutableStateOf(IntSize.Zero) }
    val dayDropBounds = remember(days, items) {
        mutableMapOf<String, Pair<LocalDate, androidx.compose.ui.geometry.Rect>>()
    }
    data class CalendarPlanDragTarget(
        val key: String,
        val displayItem: TrainingItem,
        val movablePlan: TrainingItem,
        val bounds: androidx.compose.ui.geometry.Rect,
        val size: IntSize,
    )
    val dragTargets = remember(days, items, movablePlanKeys, canMoveRemotePlans) {
        mutableMapOf<String, CalendarPlanDragTarget>()
    }
    val dragActionBounds = remember {
        mutableMapOf<CalendarPlanDragAction, androidx.compose.ui.geometry.Rect>()
    }
    var draggingPlan by remember { mutableStateOf<TrainingItem?>(null) }
    var draggingDisplayItem by remember { mutableStateOf<TrainingItem?>(null) }
    var dropTargetDate by remember { mutableStateOf<LocalDate?>(null) }
    var dragGrabOffset by remember { mutableStateOf(Offset.Zero) }
    var dragPointerRootPosition by remember { mutableStateOf<Offset?>(null) }
    var dragPreviewRootPosition by remember { mutableStateOf<Offset?>(null) }
    var dragPreviewSize by remember { mutableStateOf(IntSize.Zero) }
    var dragPreviewTargetScale by remember { mutableFloatStateOf(1f) }
    var hasCalendarPlanDragMoved by remember { mutableStateOf(false) }
    var dragWeekOffset by remember { mutableIntStateOf(0) }
    var lastDragWeekShiftAtMillis by remember { mutableStateOf(0L) }
    val dragPreviewScale by animateFloatAsState(
        targetValue = dragPreviewTargetScale,
        animationSpec = tween(durationMillis = 140),
        label = "calendarPlanDragPreviewScale"
    )
    val isDraggingCalendarPlan = draggingPlan != null
    val headerHeightDp = with(density) { headerHeightPx.toDp() }
    val visibleHeaderHeightDp = with(density) {
        (headerHeightPx + headerOffsetPx).coerceAtLeast(0f).toDp()
    }
    fun registerDayDropBounds(key: String, day: LocalDate, bounds: androidx.compose.ui.geometry.Rect) {
        dayDropBounds[key] = day to bounds
    }
    fun registerDragTarget(target: CalendarPlanDragTarget) {
        dragTargets[target.key] = target
    }
    fun dragTargetAt(rootPosition: Offset): CalendarPlanDragTarget? {
        return dragTargets.values.lastOrNull { target ->
            rootPosition.x in target.bounds.left..target.bounds.right &&
                rootPosition.y in target.bounds.top..target.bounds.bottom
        }
    }
    fun registerDragActionBounds(
        action: CalendarPlanDragAction,
        bounds: androidx.compose.ui.geometry.Rect,
    ) {
        dragActionBounds[action] = bounds
    }
    fun dragActionAt(rootPosition: Offset): CalendarPlanDragAction? {
        return (dragActionBounds + externalDragActionBounds).entries.lastOrNull { (_, bounds) ->
            rootPosition.x in bounds.left..bounds.right &&
                rootPosition.y in bounds.top..bounds.bottom
        }?.key
    }
    fun resetCalendarPlanDrag() {
        draggingPlan = null
        draggingDisplayItem = null
        dropTargetDate = null
        dragPointerRootPosition = null
        dragPreviewRootPosition = null
        hasCalendarPlanDragMoved = false
        dragWeekOffset = 0
        lastDragWeekShiftAtMillis = 0L
        dragActionBounds.clear()
        currentOnDragDropTargetDateChanged(null)
        currentOnDragPointerRootPositionChanged(null)
        currentOnDragOverlayChanged(null)
        currentOnDragStateChanged(false)
    }
    fun dropDateAt(rootPosition: Offset): LocalDate? {
        val boundsByDay = dayDropBounds.values.groupBy(
            keySelector = { (day, _) -> day },
            valueTransform = { (_, bounds) -> bounds }
        )
        return boundsByDay.entries
            .firstOrNull { (_, boundsList) ->
                boundsList.any { bounds ->
                    rootPosition.x in bounds.left..bounds.right &&
                        rootPosition.y in bounds.top..bounds.bottom
                }
            }
            ?.key
            ?: boundsByDay.entries
                .minByOrNull { (_, boundsList) ->
                    boundsList.minOf { bounds ->
                        when {
                            rootPosition.y < bounds.top -> bounds.top - rootPosition.y
                            rootPosition.y > bounds.bottom -> rootPosition.y - bounds.bottom
                            else -> 0f
                        }
                    }
                }
                ?.key
    }
    fun updateDragPointer(rootPosition: Offset?) {
        dragPointerRootPosition = rootPosition
        currentOnDragPointerRootPositionChanged(rootPosition)
    }
    fun updateDropTarget(rootPosition: Offset?) {
        val targetDate = rootPosition
            ?.let(::dropDateAt)
            ?.plusWeeks(dragWeekOffset.toLong())
        dropTargetDate = targetDate
        currentOnDragDropTargetDateChanged(targetDate)
    }
    fun updateDragOverlay() {
        val previewItem = draggingDisplayItem
        val previewPosition = dragPreviewRootPosition
        currentOnDragOverlayChanged(
            if (previewItem != null && previewPosition != null && dragPreviewSize.width > 0) {
                CalendarPlanDragOverlayState(
                    item = previewItem,
                    previewRootPosition = previewPosition,
                    previewSize = dragPreviewSize,
                    grabOffset = dragGrabOffset,
                    scale = dragPreviewScale
                )
            } else {
                null
            }
        )
    }
    val headerScrollConnection = remember(headerHeightPx, listState, isDraggingCalendarPlan) {
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
                if (isDraggingCalendarPlan) {
                    return Offset.Zero
                }
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
                if (isDraggingCalendarPlan) {
                    return Velocity.Zero
                }
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

        val targetIndex = days.indexOf(targetDate).coerceAtLeast(0)
        listState.scrollToItem(index = targetIndex)
        didInitialScroll = true
    }
    LaunchedEffect(
        externalDragPointerRootPosition,
        shouldUpdateExternalDropTargetFromPointer,
        days,
        items
    ) {
        val pointer = externalDragPointerRootPosition
        if (shouldUpdateExternalDropTargetFromPointer && pointer != null) {
            currentOnDragDropTargetDateChanged(dropDateAt(pointer))
        }
    }
    LaunchedEffect(draggingPlan) {
        while (draggingPlan != null) {
            val pointer = dragPointerRootPosition
            if (pointer != null && listRootSize.height > 0) {
                val threshold = with(density) { 96.dp.toPx() }
                val horizontalThreshold = with(density) { 56.dp.toPx() }
                val viewportBounds = dragViewportBounds ?: Rect(
                    left = listRootPosition.x,
                    top = listRootPosition.y,
                    right = listRootPosition.x + listRootSize.width,
                    bottom = listRootPosition.y + listRootSize.height
                )
                val pointerXInViewport = pointer.x - viewportBounds.left
                val pointerYInList = pointer.y - listRootPosition.y
                val visibleHeaderHeightPx = (headerHeightPx + headerOffsetPx).coerceAtLeast(0f)
                val topHotZone = threshold + visibleHeaderHeightPx
                val bottomDistance = listRootSize.height - pointerYInList
                val horizontalDirection = when {
                    pointerXInViewport < horizontalThreshold -> -1
                    viewportBounds.width - pointerXInViewport < horizontalThreshold -> 1
                    else -> 0
                }
                if (
                    hasCalendarPlanDragMoved &&
                    horizontalDirection != 0 &&
                    System.currentTimeMillis() - lastDragWeekShiftAtMillis > 650L
                ) {
                    dragWeekOffset += horizontalDirection
                    lastDragWeekShiftAtMillis = System.currentTimeMillis()
                    currentOnDragWeekShiftRequested(horizontalDirection)
                    updateDropTarget(pointer)
                }
                val scrollDelta = when {
                    pointerYInList < topHotZone && listState.canScrollBackward -> {
                        -((topHotZone - pointerYInList) / topHotZone * 34f).coerceIn(6f, 34f)
                    }
                    bottomDistance < threshold && listState.canScrollForward -> {
                        ((threshold - bottomDistance) / threshold * 34f).coerceIn(6f, 34f)
                    }
                    else -> 0f
                }
                if (scrollDelta != 0f) {
                    listState.scrollBy(scrollDelta)
                    updateDropTarget(pointer)
                }
            }
            delay(16)
        }
    }
    LaunchedEffect(draggingDisplayItem?.id) {
        if (draggingDisplayItem == null) {
            dragPreviewTargetScale = 1f
        } else {
            dragPreviewTargetScale = 1f
            delay(16)
            dragPreviewTargetScale = 2f / 3f
        }
    }
    LaunchedEffect(
        draggingDisplayItem,
        dragPreviewRootPosition,
        dragPreviewSize,
        dragGrabOffset,
        dragPreviewScale
    ) {
        updateDragOverlay()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .onGloballyPositioned { coordinates ->
                listRootPosition = coordinates.positionInRoot()
                listRootSize = coordinates.size
            }
            .pointerInput(items, movablePlanKeys, canMoveRemotePlans, listRootPosition) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val downRootPosition = listRootPosition + down.position
                    val target = dragTargetAt(downRootPosition) ?: return@awaitEachGesture
                    val longPress = awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture
                    val pointerRootPosition = listRootPosition + longPress.position
                    longPress.consume()

                    draggingPlan = target.movablePlan
                    draggingDisplayItem = target.displayItem
                    hasCalendarPlanDragMoved = false
                    dragWeekOffset = 0
                    lastDragWeekShiftAtMillis = 0L
                    currentOnDragStateChanged(true)
                    dragGrabOffset = pointerRootPosition - Offset(target.bounds.left, target.bounds.top)
                    dragPreviewSize = target.size
                    updateDragPointer(pointerRootPosition)
                    dragPreviewRootPosition = pointerRootPosition - dragGrabOffset
                    updateDropTarget(pointerRootPosition)

                    val completed = drag(longPress.id) { change ->
                        val delta = change.positionChange()
                        if (abs(delta.x) + abs(delta.y) > 0.5f) {
                            hasCalendarPlanDragMoved = true
                        }
                        change.consume()
                        val nextPointerRootPosition = listRootPosition + change.position
                        updateDragPointer(nextPointerRootPosition)
                        dragPreviewRootPosition = nextPointerRootPosition - dragGrabOffset
                        updateDropTarget(nextPointerRootPosition)
                    }
                    if (completed) {
                        val dragAction = dragPointerRootPosition?.let(::dragActionAt)
                        val targetDate = externalDropTargetDate ?: dropTargetDate
                        if (dragAction == CalendarPlanDragAction.DELETE) {
                            currentOnPlanDeleteRequested(target.movablePlan)
                        } else if (dragAction != CalendarPlanDragAction.CANCEL && targetDate != null && targetDate != target.movablePlan.date) {
                            currentOnPlanDateChanged(target.movablePlan, targetDate)
                        }
                    }
                    resetCalendarPlanDrag()
                }
            }
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
            items(days, key = { day -> "day-section-$day" }) { day ->
                val dayItems = grouped[day].orEmpty()
                val isDropTarget = (draggingPlan != null && dropTargetDate == day) ||
                    externalDropTargetDate == day
                DisposableEffect(day) {
                    onDispose {
                        dayDropBounds.remove("section-$day")
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isDropTarget) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            } else {
                                Color.Transparent
                            }
                        )
                        .onGloballyPositioned { coordinates ->
                            val position = coordinates.positionInRoot()
                            registerDayDropBounds(
                                key = "section-$day",
                                day = day,
                                bounds = androidx.compose.ui.geometry.Rect(
                                    left = position.x,
                                    top = position.y,
                                    right = position.x + coordinates.size.width,
                                    bottom = position.y + coordinates.size.height
                                )
                            )
                        }
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DayHeader(day = day, count = dayItems.size)
                    dayItems.forEach { item ->
                        val movablePlan = item.calendarPlanForMove() ?: item
                        val canDragPlan = item.canDragCalendarPlan(
                            movableLocalPlanKeys = movablePlanKeys,
                            canMoveRemotePlans = canMoveRemotePlans
                        )
                        val isDragging = draggingPlan?.id == movablePlan.id
                        val isApiPendingMove = item.isApiPendingMove(pendingApiMovePlanKeys)
                        DisposableEffect(item.id) {
                            onDispose {
                                dragTargets.remove("row-${item.id}")
                                dayDropBounds.remove("row-${item.id}")
                            }
                        }
                        val dragModifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                val position = coordinates.positionInRoot()
                                val bounds = androidx.compose.ui.geometry.Rect(
                                    left = position.x,
                                    top = position.y,
                                    right = position.x + coordinates.size.width,
                                    bottom = position.y + coordinates.size.height
                                )
                                registerDayDropBounds(
                                    key = "row-${item.id}",
                                    day = item.date,
                                    bounds = bounds
                                )
                                if (canDragPlan) {
                                    registerDragTarget(
                                        CalendarPlanDragTarget(
                                            key = "row-${item.id}",
                                            displayItem = item,
                                            movablePlan = movablePlan,
                                            bounds = bounds,
                                            size = coordinates.size
                                        )
                                    )
                                }
                            }
                            .graphicsLayer {
                                alpha = when {
                                    isDragging -> 0.2f
                                    isApiPendingMove -> 0.5f
                                    else -> 1f
                                }
                            }
                        TrainingItemRow(
                            item = item,
                            isApiPendingMove = isApiPendingMove,
                            modifier = dragModifier,
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
        val activeDragAction = dragPointerRootPosition?.let(::dragActionAt)
        AnimatedVisibility(
            visible = renderLocalDragOverlay && draggingPlan != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
                .zIndex(4f),
            enter = fadeIn(animationSpec = tween(120)),
            exit = fadeOut(animationSpec = tween(100))
        ) {
            CalendarPlanDragActionButtons(
                activeAction = activeDragAction,
                onActionPositioned = { action, bounds ->
                    registerDragActionBounds(action, bounds)
                }
            )
        }
        val previewItem = draggingDisplayItem
        val previewPosition = dragPreviewRootPosition
        if (renderLocalDragOverlay && previewItem != null && previewPosition != null && dragPreviewSize.width > 0) {
            TrainingItemRow(
                item = previewItem,
                onClick = {},
                modifier = Modifier
                    .zIndex(3f)
                    .offset {
                        IntOffset(
                            x = (previewPosition.x - listRootPosition.x + dragGrabOffset.x * (1f - dragPreviewScale)).roundToInt(),
                            y = (previewPosition.y - listRootPosition.y + dragGrabOffset.y * (1f - dragPreviewScale)).roundToInt()
                        )
                    }
                    .width(with(density) { dragPreviewSize.width.toDp() })
                    .graphicsLayer {
                        shadowElevation = 18f
                        scaleX = dragPreviewScale
                        scaleY = dragPreviewScale
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
                    }
            )
        }
    }
}

@Composable
private fun CalendarPlanDragActionButtons(
    activeAction: CalendarPlanDragAction?,
    onActionPositioned: (CalendarPlanDragAction, androidx.compose.ui.geometry.Rect) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CalendarPlanDragActionButton(
            action = CalendarPlanDragAction.CANCEL,
            active = activeAction == CalendarPlanDragAction.CANCEL,
            icon = Icons.Outlined.Close,
            contentDescription = "이동 취소",
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            onPositioned = onActionPositioned
        )
        CalendarPlanDragActionButton(
            action = CalendarPlanDragAction.DELETE,
            active = activeAction == CalendarPlanDragAction.DELETE,
            icon = Icons.Outlined.Delete,
            contentDescription = "Plan 삭제",
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            onPositioned = onActionPositioned
        )
    }
}

@Composable
private fun CalendarPlanDragActionButton(
    action: CalendarPlanDragAction,
    active: Boolean,
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    contentColor: Color,
    onPositioned: (CalendarPlanDragAction, androidx.compose.ui.geometry.Rect) -> Unit,
) {
    FloatingActionButton(
        onClick = {},
        modifier = Modifier
            .size(56.dp)
            .graphicsLayer {
                scaleX = if (active) 1.12f else 1f
                scaleY = if (active) 1.12f else 1f
            }
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()
                onPositioned(
                    action,
                    androidx.compose.ui.geometry.Rect(
                        left = position.x,
                        top = position.y,
                        right = position.x + coordinates.size.width,
                        bottom = position.y + coordinates.size.height
                    )
                )
            },
        shape = RoundedCornerShape(999.dp),
        containerColor = if (active) contentColor else containerColor,
        contentColor = if (active) containerColor else contentColor
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
internal fun MonthlyTrainingCalendar(
    range: TrainingDateRange,
    items: List<TrainingItem>,
    onPlanSelected: (TrainingItem) -> Unit,
    onIntervalStrengthPlanSelected: (TrainingItem?, StrengthWorkoutPlan) -> Unit,
    onDaySelected: (LocalDate) -> Unit,
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
                        onIntervalStrengthPlanSelected = onIntervalStrengthPlanSelected,
                        onDaySelected = onDaySelected
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
    onDaySelected: (LocalDate) -> Unit,
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
            .clickable { onDaySelected(day) }
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
internal fun DayHeader(
    day: LocalDate,
    count: Int,
    modifier: Modifier = Modifier,
    isDropTarget: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isDropTarget) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.56f)
                } else {
                    Color.Transparent
                }
            ),
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
    modifier: Modifier = Modifier,
    isApiPendingMove: Boolean = false,
) {
    Card(
        modifier = modifier
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
                if (isApiPendingMove) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "API반영중",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
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

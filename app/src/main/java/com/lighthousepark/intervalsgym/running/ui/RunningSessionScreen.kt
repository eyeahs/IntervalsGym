package com.lighthousepark.intervalsgym.running.ui

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
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.MotionEvent
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
import androidx.compose.material.icons.outlined.Remove
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
import androidx.compose.ui.input.pointer.pointerInteropFilter
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

internal enum class RunningSessionPhase {
    WARMUP,
    BLOCK,
    FINISHED
}

/**
 * Running execution screen launched from [WorkoutRoutineScreen].
 * Owns warmup, block progression, local result saving, optional upload prompt, and running overlay updates.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun RunningSessionScreen(
    apiKey: String,
    routineName: String,
    blocks: List<RoutineBlock>,
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
    var phase by rememberSaveable(routineName) { mutableStateOf(RunningSessionPhase.WARMUP) }
    var currentBlockIndex by rememberSaveable(routineName) { mutableIntStateOf(0) }
    var warmupStartedAtMillis by rememberSaveable(routineName) { mutableStateOf(System.currentTimeMillis()) }
    var blockEndAtMillis by rememberSaveable(routineName) { mutableStateOf(0L) }
    var blockStartedAtMillis by rememberSaveable(routineName) { mutableStateOf(0L) }
    var actualBlocksJson by rememberSaveable(routineName, blocks.size) { mutableStateOf("[]") }
    var actualBlocks by remember(routineName, blocks.size) {
        mutableStateOf(actualBlocksJson.toRunningWorkoutRoutineBlocks())
    }
    var finishedAtMillis by rememberSaveable(routineName) { mutableStateOf(0L) }
    var showFinishDialog by rememberSaveable(routineName) { mutableStateOf(false) }
    var showStopSaveDialog by rememberSaveable(routineName) { mutableStateOf(false) }
    var isUploadingRunningSession by remember { mutableStateOf(false) }
    var finishError by remember { mutableStateOf<String?>(null) }
    var localRunningSessionId by rememberSaveable(routineName) { mutableStateOf<String?>(null) }
    var handledOverlayActionRequest by remember { mutableIntStateOf(RunningOverlayRequests.actionRequest) }
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var blinkOn by remember { mutableStateOf(false) }
    var workoutHeartRateSamples by remember(routineName) { mutableStateOf<List<HeartRateSample>>(emptyList()) }
    var targetTextOverrides by rememberSaveable(routineName, blocks.size) {
        mutableStateOf(List(blocks.size) { "" })
    }
    LaunchedEffect(blocks.size) {
        if (targetTextOverrides.size != blocks.size) {
            targetTextOverrides = List(blocks.size) { index ->
                targetTextOverrides.getOrNull(index).orEmpty()
            }
        }
    }
    LaunchedEffect(heartRateSamples, warmupStartedAtMillis) {
        val sessionSamples = heartRateSamples.filter { it.timestampMillis >= warmupStartedAtMillis }
        if (sessionSamples.isNotEmpty()) {
            workoutHeartRateSamples = (workoutHeartRateSamples + sessionSamples)
                .distinctBy { it.timestampMillis }
                .sortedBy { it.timestampMillis }
        }
    }
    val displayBlocks = remember(blocks, targetTextOverrides) {
        blocks.mapIndexed { index, block ->
            targetTextOverrides.getOrNull(index)
                ?.takeIf { it.isNotBlank() }
                ?.let { block.copy(targetText = it) }
                ?: block
        }
    }
    val currentBlock = displayBlocks.getOrNull(currentBlockIndex)
    val isLastBlock = currentBlockIndex == blocks.lastIndex
    val warmupElapsedSeconds = if (phase == RunningSessionPhase.WARMUP) {
        ((nowMillis - warmupStartedAtMillis) / 1000L).toInt().coerceAtLeast(0)
    } else {
        0
    }
    val blockRemainingSeconds = if (phase == RunningSessionPhase.BLOCK && blockEndAtMillis > 0L) {
        (((blockEndAtMillis - nowMillis).coerceAtLeast(0L) + 999L) / 1000L).toInt()
    } else {
        0
    }
    val blockElapsedSeconds = currentBlock?.let { block ->
        if (phase == RunningSessionPhase.BLOCK) {
            (block.durationSeconds - blockRemainingSeconds).coerceIn(0, block.durationSeconds)
        } else {
            0
        }
    } ?: 0
    val progressSeconds = when (phase) {
        RunningSessionPhase.WARMUP -> null
        RunningSessionPhase.BLOCK -> currentBlock?.let { it.startSecond + blockElapsedSeconds }
        RunningSessionPhase.FINISHED -> totalSeconds
    }
    val isUrgent = phase == RunningSessionPhase.BLOCK && blockRemainingSeconds in 1..5

    fun logRunningSessionEvent(
        event: String,
        details: String = "",
        throwable: Throwable? = null,
    ) {
        DiagnosticsLogger.log(
            context = context,
            tag = "RunningSession",
            message = buildString {
                appendLine("event=$event")
                appendLine("routineName=$routineName")
                appendLine("phase=$phase")
                appendLine("currentBlockIndex=$currentBlockIndex")
                appendLine("blockCount=${blocks.size}")
                appendLine("totalSeconds=$totalSeconds")
                appendLine("warmupStartedAtMillis=$warmupStartedAtMillis")
                appendLine("blockStartedAtMillis=$blockStartedAtMillis")
                appendLine("blockEndAtMillis=$blockEndAtMillis")
                appendLine("finishedAtMillis=$finishedAtMillis")
                if (details.isNotBlank()) appendLine(details)
            },
            throwable = throwable
        )
    }

    LaunchedEffect(Unit) {
        requestOverlayPermissionIfNeeded(context)
        logRunningSessionEvent(
            event = "session opened",
            details = buildString {
                appendLine("logFile=${DiagnosticsLogger.diagnosticLogFile(context).absolutePath}")
                appendLine(blocks.runningBlocksDiagnosticText(label = "sessionBlocks"))
            }
        )
    }

    LaunchedEffect(phase, warmupStartedAtMillis) {
        while (phase == RunningSessionPhase.WARMUP) {
            nowMillis = System.currentTimeMillis()
            delay(1_000L)
        }
    }

    fun runningSessionForFinish(
        endedAtMillis: Long,
        actualBlocksForSession: List<RoutineBlock> = actualBlocks,
    ): RunningSession {
        val blockSeconds = blocks.sumOf { it.durationSeconds }
        return RunningSession(
            name = routineName,
            startedAt = warmupStartedAtMillis.toLocalDateTime(),
            endedAt = endedAtMillis.toLocalDateTime(),
            warmupSeconds = ((endedAtMillis - warmupStartedAtMillis) / 1000L).toInt()
                .coerceAtLeast(0)
                .let { elapsed -> (elapsed - blockSeconds).coerceAtLeast(0) },
            blocks = blocks,
            actualBlocks = actualBlocksForSession.toActualTimeline(),
            heartRateSamples = workoutHeartRateSamples
        )
    }

    fun updateActualBlocks(nextActualBlocks: List<RoutineBlock>): List<RoutineBlock> {
        actualBlocks = nextActualBlocks
        actualBlocksJson = nextActualBlocks.toRoutineBlocksJsonArray().toString()
        return nextActualBlocks
    }

    fun recordCurrentBlock(endMillis: Long = System.currentTimeMillis()): List<RoutineBlock> {
        val block = currentBlock ?: return actualBlocks
        if (blockStartedAtMillis <= 0L) return actualBlocks
        val maxSeconds = block.durationSeconds.coerceAtLeast(0)
        val actualSeconds = (((endMillis - blockStartedAtMillis).coerceAtLeast(0L) + 999L) / 1000L)
            .toInt()
            .coerceIn(0, maxSeconds)
            .let { seconds ->
                if (maxSeconds > 0) seconds.coerceAtLeast(1) else 0
            }
        val nextActualBlocks = updateActualBlocks(actualBlocks + block.copy(durationSeconds = actualSeconds))
        logRunningSessionEvent(
            event = "record block",
            details = buildString {
                appendLine("endMillis=$endMillis")
                appendLine("actualSeconds=$actualSeconds")
                appendLine(block.copy(durationSeconds = actualSeconds).runningBlockDiagnosticText())
            }
        )
        blockStartedAtMillis = 0L
        return nextActualBlocks
    }

    fun finishWorkout(
        endedAtMillis: Long = System.currentTimeMillis(),
        actualBlocksForFinish: List<RoutineBlock>? = null,
    ) {
        if (phase == RunningSessionPhase.FINISHED || showFinishDialog) return
        val actualBlocksForSession = actualBlocksForFinish ?: if (phase == RunningSessionPhase.BLOCK) {
            recordCurrentBlock(endedAtMillis)
        } else {
            actualBlocks
        }
        updateActualBlocks(actualBlocksForSession)
        val session = runningSessionForFinish(endedAtMillis, actualBlocksForSession)
        val localSession = session.toCompletedRunningSession(uploadedToIntervals = false)
        appendRunningSessionHistory(prefs, localSession)
        logRunningSessionEvent(
            event = "finish saved local",
            details = buildString {
                appendLine("endedAtMillis=$endedAtMillis")
                appendLine("localSessionId=${localSession.id}")
                appendLine("durationSeconds=${localSession.durationSeconds}")
                appendLine("warmupSeconds=${localSession.warmupSeconds}")
                appendLine("estimatedDistanceMeters=${localSession.estimatedDistanceMeters}")
                appendLine(localSession.actualBlocks.runningBlocksDiagnosticText(label = "actualBlocks"))
            }
        )
        localRunningSessionId = localSession.id
        phase = RunningSessionPhase.FINISHED
        finishedAtMillis = endedAtMillis
        blockEndAtMillis = 0L
        finishError = null
        showFinishDialog = true
        stopRunningOverlay(context)
        stopWorkoutStatusService(context)
    }

    fun catchUpElapsedBlocks(observedAtMillis: Long = System.currentTimeMillis()): Boolean {
        if (phase != RunningSessionPhase.BLOCK) return false
        val result = catchUpRunningSessionBlocks(
            blocks = displayBlocks,
            currentBlockIndex = currentBlockIndex,
            blockStartedAtMillis = blockStartedAtMillis,
            blockEndAtMillis = blockEndAtMillis,
            actualBlocks = actualBlocks,
            nowMillis = observedAtMillis
        ) ?: return false
        logRunningSessionEvent(
            event = "catch up elapsed blocks",
            details = buildString {
                appendLine("observedAtMillis=$observedAtMillis")
                appendLine("resultCurrentBlockIndex=${result.currentBlockIndex}")
                appendLine("resultBlockStartedAtMillis=${result.blockStartedAtMillis}")
                appendLine("resultBlockEndAtMillis=${result.blockEndAtMillis}")
                appendLine("resultFinishedAtMillis=${result.finishedAtMillis}")
                appendLine(result.actualBlocks.runningBlocksDiagnosticText(label = "actualBlocksAfterCatchUp"))
            }
        )
        updateActualBlocks(result.actualBlocks)
        currentBlockIndex = result.currentBlockIndex
        blockStartedAtMillis = result.blockStartedAtMillis
        blockEndAtMillis = result.blockEndAtMillis
        nowMillis = observedAtMillis
        result.finishedAtMillis?.let { finishedAtMillis ->
            finishWorkout(
                endedAtMillis = finishedAtMillis,
                actualBlocksForFinish = result.actualBlocks
            )
        }
        return true
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
        phase = RunningSessionPhase.BLOCK
        logRunningSessionEvent(
            event = "block started",
            details = buildString {
                appendLine("requestedIndex=$index")
                appendLine("startedAtMillis=$nowMillis")
                appendLine("scheduledEndAtMillis=$blockEndAtMillis")
                appendLine(displayBlocks.getOrNull(index)?.runningBlockDiagnosticText() ?: block.runningBlockDiagnosticText())
            }
        )
    }

    fun updateCurrentBlockTarget(speedDeltaKmh: Float = 0f, inclineDeltaPercent: Float = 0f) {
        val originalBlock = blocks.getOrNull(currentBlockIndex) ?: return
        val activeBlock = currentBlock ?: originalBlock
        val nextSpeed = ((activeBlock.graphTargetSpeedKmh() ?: 0f) + speedDeltaKmh)
            .coerceIn(0f, MAX_RUNNING_SPEED_KMH)
        val nextIncline = ((activeBlock.runningInclinePercent() ?: 0f) + inclineDeltaPercent)
            .coerceIn(0f, MAX_RUNNING_INCLINE_PERCENT)
        val nextTargets = targetTextOverrides.toMutableList().apply {
            while (size < blocks.size) add("")
            this[currentBlockIndex] = runningTargetOverrideText(nextSpeed, nextIncline)
        }
        targetTextOverrides = nextTargets
        logRunningSessionEvent(
            event = "target override",
            details = buildString {
                appendLine("speedDeltaKmh=$speedDeltaKmh")
                appendLine("inclineDeltaPercent=$inclineDeltaPercent")
                appendLine("nextSpeedKmh=$nextSpeed")
                appendLine("nextInclinePercent=$nextIncline")
                appendLine(originalBlock.copy(targetText = runningTargetOverrideText(nextSpeed, nextIncline)).runningBlockDiagnosticText())
            }
        )
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
        if (phase != RunningSessionPhase.BLOCK || currentBlockIndex <= 0) return
        updateActualBlocks(actualBlocks.dropLast(1))
        blockStartedAtMillis = 0L
        startBlock(currentBlockIndex - 1)
    }

    fun handlePrimaryAction() {
        when (phase) {
            RunningSessionPhase.WARMUP -> startBlock(0)
            RunningSessionPhase.BLOCK -> moveToNextBlock()
            RunningSessionPhase.FINISHED -> onBack()
        }
    }

    fun stopWorkoutWithoutSaving() {
        logRunningSessionEvent(event = "stop without saving")
        showStopSaveDialog = false
        stopRunningOverlay(context)
        stopWorkoutStatusService(context)
        onWorkoutFinished()
    }

    fun requestWorkoutExit() {
        if (phase == RunningSessionPhase.FINISHED) {
            onWorkoutFinished()
        } else {
            showStopSaveDialog = true
        }
    }

    BackHandler(enabled = !showStopSaveDialog && !showFinishDialog) {
        requestWorkoutExit()
    }

    LaunchedEffect(phase, blockStartedAtMillis, blockEndAtMillis, currentBlockIndex, currentBlock?.targetText) {
        while (phase == RunningSessionPhase.BLOCK && blockStartedAtMillis > 0L) {
            val observedAtMillis = System.currentTimeMillis()
            nowMillis = observedAtMillis
            if (catchUpElapsedBlocks(observedAtMillis)) {
                if (phase == RunningSessionPhase.FINISHED) break
                continue
            }
            if (blockEndAtMillis > 0L && observedAtMillis >= blockEndAtMillis) {
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

    LaunchedEffect(phase, currentBlockIndex, blockEndAtMillis, blocks.size) {
        if (
            phase != RunningSessionPhase.BLOCK ||
            currentBlockIndex != blocks.lastIndex ||
            blockEndAtMillis <= 0L
        ) {
            return@LaunchedEffect
        }
        val delayMillis = sessionAutoLocalSaveDelayMillis(
            finishedAtMillis = blockEndAtMillis,
            nowMillis = System.currentTimeMillis()
        )
        if (delayMillis > 0L) {
            delay(delayMillis)
        }
        if (
            phase == RunningSessionPhase.BLOCK &&
            shouldAutoLocalSaveLastRunningBlock(
                currentBlockIndex = currentBlockIndex,
                blockCount = blocks.size,
                blockEndAtMillis = blockEndAtMillis,
                nowMillis = System.currentTimeMillis()
            )
        ) {
            logRunningSessionEvent(
                event = "auto local save last block timeout",
                details = buildString {
                    appendLine("lastBlockEndAtMillis=$blockEndAtMillis")
                    appendLine("autoSaveAtMillis=${sessionAutoLocalSaveAtMillis(blockEndAtMillis)}")
                }
            )
            catchUpElapsedBlocks()
        }
    }

    val showRunningOverlayIfNeeded by rememberUpdatedState(
        newValue = {
            when (phase) {
                RunningSessionPhase.WARMUP -> {
                    logRunningSessionEvent(
                        event = "overlay start",
                        details = "title=Warmup actionLabel=Warmup skip startAtMillis=$warmupStartedAtMillis"
                    )
                    startRunningOverlay(
                        context = context,
                        title = "Warmup",
                        endAtMillis = 0L,
                        startAtMillis = warmupStartedAtMillis,
                        actionLabel = "Warmup skip",
                        heartRateBpm = heartRateBpm
                    )
                }
                RunningSessionPhase.BLOCK -> {
                    val overlayTitle = if (isLastBlock) "완료" else currentBlock?.title ?: "Block ${currentBlockIndex + 1}"
                    val overlayActionLabel = if (isLastBlock) "저장" else "Block skip"
                    val speedText = currentBlock?.runningTargetSpeedText().orEmpty()
                    val inclineText = currentBlock?.runningInclineText().orEmpty()
                    logRunningSessionEvent(
                        event = "overlay start",
                        details = buildString {
                            appendLine("title=$overlayTitle")
                            appendLine("actionLabel=$overlayActionLabel")
                            appendLine("targetSpeed=$speedText")
                            appendLine("targetIncline=$inclineText")
                            appendLine("endAtMillis=$blockEndAtMillis")
                            appendLine(currentBlock?.runningBlockDiagnosticText().orEmpty())
                        }
                    )
                    startRunningOverlay(
                        context = context,
                        title = overlayTitle,
                        endAtMillis = blockEndAtMillis,
                        actionLabel = overlayActionLabel,
                        targetSpeed = speedText,
                        targetIncline = inclineText,
                        heartRateBpm = heartRateBpm
                    )
                }
                RunningSessionPhase.FINISHED -> stopRunningOverlay(context)
            }
        }
    )

    LaunchedEffect(
        phase,
        currentBlockIndex,
        currentBlock?.targetText,
        blockEndAtMillis,
        heartRateBpm,
        warmupStartedAtMillis
    ) {
        when (phase) {
            RunningSessionPhase.WARMUP -> startWorkoutStatusService(
                context = context,
                workoutType = WorkoutStatusForegroundService.TYPE_RUNNING,
                title = routineName,
                phaseLabel = "Warmup",
                startAtMillis = warmupStartedAtMillis,
                heartRateBpm = heartRateBpm
            )
            RunningSessionPhase.BLOCK -> {
                val speedText = currentBlock?.runningTargetSpeedText().orEmpty()
                val inclineText = currentBlock?.runningInclineText().orEmpty()
                val detailText = listOfNotNull(
                    speedText.takeIf { it.isNotBlank() }?.let { "속도 $it" },
                    inclineText.takeIf { it.isNotBlank() }?.let { "경사도 $it" }
                ).joinToString(" / ")
                startWorkoutStatusService(
                    context = context,
                    workoutType = WorkoutStatusForegroundService.TYPE_RUNNING,
                    title = routineName,
                    phaseLabel = "Block ${currentBlockIndex + 1}/${blocks.size}",
                    detailText = detailText,
                    startAtMillis = warmupStartedAtMillis,
                    endAtMillis = blockEndAtMillis,
                    heartRateBpm = heartRateBpm
                )
            }
            RunningSessionPhase.FINISHED -> stopWorkoutStatusService(context)
        }
    }

    DisposableEffect(context) {
        val lifecycle = (context as? LifecycleOwner)?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    catchUpElapsedBlocks()
                    showRunningOverlayIfNeeded()
                }
                Lifecycle.Event.ON_RESUME -> {
                    stopRunningOverlay(context)
                    catchUpElapsedBlocks()
                }
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

    LaunchedEffect(
        phase,
        currentBlockIndex,
        currentBlock?.targetText,
        blockEndAtMillis,
        heartRateBpm,
        warmupStartedAtMillis
    ) {
        val lifecycle = (context as? LifecycleOwner)?.lifecycle
        if (lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == false) {
            showRunningOverlayIfNeeded()
        }
    }

    LaunchedEffect(RunningOverlayRequests.actionRequest) {
        if (RunningOverlayRequests.actionRequest > handledOverlayActionRequest) {
            handledOverlayActionRequest = RunningOverlayRequests.actionRequest
            handlePrimaryAction()
        }
    }

    fun uploadRunningSessionAndFinish() {
        if (apiKey.isBlank()) {
            finishError = "Intervals.icu 업로드는 로그인 후 사용할 수 있습니다."
            logRunningSessionEvent(event = "upload blocked no api key")
            return
        }
        val endedAt = finishedAtMillis.takeIf { it > 0L } ?: System.currentTimeMillis()
        val session = runningSessionForFinish(endedAt)
        logRunningSessionEvent(
            event = "upload started",
            details = buildString {
                appendLine("endedAtMillis=$endedAt")
                appendLine("heartRateSamples=${session.heartRateSamples.size}")
                appendLine(session.actualBlocks.runningBlocksDiagnosticText(label = "uploadActualBlocks"))
            }
        )
        scope.launch {
            isUploadingRunningSession = true
            finishError = null
            try {
                repository.uploadRunningSession(session)
                replaceRunningSessionHistory(
                    prefs = prefs,
                    workout = session.toCompletedRunningSession(uploadedToIntervals = true)
                )
                localRunningSessionId = session.toCompletedRunningSession(uploadedToIntervals = true).id
                logRunningSessionEvent(
                    event = "upload succeeded",
                    details = "localRunningSessionId=${localRunningSessionId.orEmpty()}"
                )
                onWorkoutFinished()
            } catch (error: Exception) {
                finishError = error.message ?: "업로드하지 못했습니다."
                logRunningSessionEvent(
                    event = "upload failed",
                    details = "message=${finishError.orEmpty()}",
                    throwable = error
                )
            } finally {
                isUploadingRunningSession = false
            }
        }
    }

    if (showStopSaveDialog) {
        RunningStopSaveDialog(
            onDismiss = { showStopSaveDialog = false },
            onSave = {
                showStopSaveDialog = false
                finishWorkout()
            },
            onDiscard = ::stopWorkoutWithoutSaving
        )
    }

    if (showFinishDialog) {
        RunningFinishUploadChoiceDialog(
            apiKey = apiKey,
            isUploading = isUploadingRunningSession,
            finishError = finishError,
            onUpload = ::uploadRunningSessionAndFinish,
            onUseGarmin = onWorkoutFinished
        )
    }

    Scaffold(
        topBar = {
            RunningSessionTopBar(
                routineName = routineName,
                phase = phase,
                isStopEnabled = !showFinishDialog && !showStopSaveDialog,
                onBack = ::requestWorkoutExit,
                onStop = { showStopSaveDialog = true }
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
            val routineGraphHeight = 128.dp
            val heartGraphCanvasHeight = 54.dp
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(gap)
            ) {
                RoutineWorkoutGraphCanvas(
                    blocks = displayBlocks,
                    totalSeconds = totalSeconds,
                    sportType = TrainingSportType.RUNNING,
                    height = routineGraphHeight,
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
                    RunningSessionPhase.WARMUP -> RunningWarmupPanel(
                        elapsedSeconds = warmupElapsedSeconds,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    RunningSessionPhase.BLOCK -> RunningBlockPanel(
                        block = currentBlock,
                        blockIndex = currentBlockIndex,
                        blockCount = blocks.size,
                        remainingSeconds = blockRemainingSeconds,
                        blinkOn = blinkOn,
                        isLastBlock = isLastBlock,
                        onSpeedDecrease = { updateCurrentBlockTarget(speedDeltaKmh = -RUNNING_SPEED_STEP_KMH) },
                        onSpeedIncrease = { updateCurrentBlockTarget(speedDeltaKmh = RUNNING_SPEED_STEP_KMH) },
                        onInclineDecrease = {
                            updateCurrentBlockTarget(inclineDeltaPercent = -RUNNING_INCLINE_STEP_PERCENT)
                        },
                        onInclineIncrease = {
                            updateCurrentBlockTarget(inclineDeltaPercent = RUNNING_INCLINE_STEP_PERCENT)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    RunningSessionPhase.FINISHED -> RunningFinishedPanel(
                        totalSeconds = totalSeconds,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        onClose = onBack
                    )
                }
                if (phase != RunningSessionPhase.FINISHED) {
                    RunningSessionActionBar(
                        phase = phase,
                        currentBlockIndex = currentBlockIndex,
                        isLastBlock = isLastBlock,
                        onPreviousBlock = ::moveToPreviousBlock,
                        onPrimaryAction = ::handlePrimaryAction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )
                }
            }
        }
    }
}

/**
 * UI tests: RunningSessionUiTest.runningSessionActionBar_warmupPrimaryInvokesCallback,
 * runningSessionActionBar_blockActionsRespectPreviousAvailability,
 * runningSessionActionBar_lastBlockInvokesPreviousAndFinishCallbacks.
 */
@Composable
internal fun RunningSessionActionBar(
    phase: RunningSessionPhase,
    currentBlockIndex: Int,
    isLastBlock: Boolean,
    onPreviousBlock: () -> Unit,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (phase == RunningSessionPhase.FINISHED) return
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (phase == RunningSessionPhase.BLOCK) {
            OutlinedButton(
                onClick = onPreviousBlock,
                enabled = currentBlockIndex > 0,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .debugContentDescription(TestContentDescriptions.RunningPreviousBlock),
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
            onClick = onPrimaryAction,
            modifier = Modifier
                .weight(if (phase == RunningSessionPhase.BLOCK) 2f else 1f)
                .fillMaxHeight()
                .debugContentDescription(TestContentDescriptions.RunningPrimaryAction),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (phase == RunningSessionPhase.BLOCK) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        ) {
            Text(
                when (phase) {
                    RunningSessionPhase.WARMUP -> "Warmup 종료"
                    RunningSessionPhase.BLOCK -> if (isLastBlock) "운동 마치기" else "Block 건너뛰기"
                    RunningSessionPhase.FINISHED -> ""
                }
            )
        }
    }
}

/**
 * UI tests: RunningSessionUiTest.runningSessionTopBar_invokesBackAndStopCallbacks,
 * runningSessionTopBar_hidesStopActionWhenFinished.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RunningSessionTopBar(
    routineName: String,
    phase: RunningSessionPhase,
    isStopEnabled: Boolean,
    onBack: () -> Unit,
    onStop: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = routineName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningSessionBack)
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
            }
        },
        actions = {
            if (phase != RunningSessionPhase.FINISHED) {
                TextButton(
                    onClick = onStop,
                    enabled = isStopEnabled,
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningStopWorkout)
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

/**
 * UI tests: RunningSessionUiTest.runningFinishUploadChoiceDialog_invokesUploadAndGarminCallbacks,
 * runningFinishUploadChoiceDialog_disablesUnavailableActions.
 */
@Composable
internal fun RunningFinishUploadChoiceDialog(
    apiKey: String,
    isUploading: Boolean,
    finishError: String?,
    onUpload: () -> Unit,
    onUseGarmin: () -> Unit,
) {
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
                onClick = onUpload,
                enabled = apiKey.isNotBlank() && !isUploading,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningFinishUpload)
            ) {
                Text(if (isUploading) "업로드 중" else "수동 업로드")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onUseGarmin,
                enabled = !isUploading,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningFinishUseGarmin)
            ) {
                Text("Garmin 결과 사용")
            }
        }
    )
}

/**
 * UI tests: RunningSessionUiTest.runningStopSaveDialog_invokesSaveAndDiscardCallbacks.
 */
@Composable
internal fun RunningStopSaveDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("운동 중지") },
        text = {
            Text("현재까지 수행한 러닝 기록을 로컬에 저장할까요?")
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningStopSave)
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDiscard,
                modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningStopDiscard)
            ) {
                Text("삭제")
            }
        }
    )
}

private fun String.toRunningWorkoutRoutineBlocks(): List<RoutineBlock> {
    return runCatching { JSONArray(this).toCachedRoutineBlocks() }.getOrElse { emptyList() }
}

@Composable
internal fun RunningWarmupPanel(
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

/**
 * UI tests: RunningSessionUiTest.runningBlockPanel_exposesStepperActions,
 * runningTargetStepper_ignoresDisabledDecreaseAndInvokesEnabledIncrease.
 */
@Composable
internal fun RunningBlockPanel(
    block: RoutineBlock?,
    blockIndex: Int,
    blockCount: Int,
    remainingSeconds: Int,
    blinkOn: Boolean,
    isLastBlock: Boolean,
    onSpeedDecrease: () -> Unit,
    onSpeedIncrease: () -> Unit,
    onInclineDecrease: () -> Unit,
    onInclineIncrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val speedText = block?.runningTargetSpeedText().orEmpty().ifBlank { "-" }
    val inclineText = block?.runningInclineText().orEmpty().ifBlank { "-" }
    val speedKmh = block?.graphTargetSpeedKmh() ?: 0f
    val inclinePercent = block?.runningInclinePercent() ?: 0f
    val blockDurationText = formatClock(block?.durationSeconds ?: 0)
    val blockProgressText = "남은 ${formatClock(remainingSeconds)} / $blockDurationText"
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
                    blockProgressText,
                    blockTitle
                ).filter { it.isNotBlank() }.joinToString(" · "),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RunningTargetStepper(
                    label = "속도",
                    value = speedText.takeIf { it != "-" } ?: "0km/h",
                    onDecrease = onSpeedDecrease,
                    onIncrease = onSpeedIncrease,
                    canDecrease = speedKmh > 0f,
                    canIncrease = speedKmh < MAX_RUNNING_SPEED_KMH,
                    modifier = Modifier.weight(1f)
                )
                RunningTargetStepper(
                    label = "경사도",
                    value = inclineText.takeIf { it != "-" } ?: "0%",
                    onDecrease = onInclineDecrease,
                    onIncrease = onInclineIncrease,
                    canDecrease = inclinePercent > 0f,
                    canIncrease = inclinePercent < MAX_RUNNING_INCLINE_PERCENT,
                    modifier = Modifier.weight(1f)
                )
            }
            RunningTimerText(
                text = formatClock(remainingSeconds),
                color = timerColor,
                modifier = Modifier.weight(1f),
                fontHeightRatio = 0.56f,
                maxFontSize = 138f
            )
        }
    }
}

@Composable
internal fun RunningTargetStepper(
    label: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    canDecrease: Boolean,
    canIncrease: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RunningTargetStepButton(
                icon = Icons.Outlined.Remove,
                contentDescription = "$label 감소",
                testContentDescription = TestContentDescriptions.runningTargetStepper(label, "decrease"),
                enabled = canDecrease,
                onStep = onDecrease
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            RunningTargetStepButton(
                icon = Icons.Outlined.Add,
                contentDescription = "$label 증가",
                testContentDescription = TestContentDescriptions.runningTargetStepper(label, "increase"),
                enabled = canIncrease,
                onStep = onIncrease
            )
        }
    }
}

@Composable
private fun RunningTargetStepButton(
    icon: ImageVector,
    contentDescription: String,
    testContentDescription: String,
    enabled: Boolean,
    onStep: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestOnStep by rememberUpdatedState(onStep)
    val repeatHandler = remember { Handler(Looper.getMainLooper()) }
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
    }
    val repeatStep = remember {
        object : Runnable {
            override fun run() {
                latestOnStep()
                repeatHandler.postDelayed(this, 92L)
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            repeatHandler.removeCallbacks(repeatStep)
        }
    }
    Surface(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(12.dp))
            .debugContentDescription(testContentDescription)
            .pointerInteropFilter { event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        if (enabled) {
                            latestOnStep()
                            repeatHandler.removeCallbacks(repeatStep)
                            repeatHandler.postDelayed(repeatStep, 420L)
                        }
                        true
                    }
                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        repeatHandler.removeCallbacks(repeatStep)
                        true
                    }
                    else -> true
                }
            },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 0.12f else 0.04f),
        contentColor = contentColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
internal fun RunningTimerText(
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

/**
 * UI tests: RunningSessionUiTest.heartRateGraph_connectButtonInvokesCallback.
 */
@Composable
internal fun HeartRateGraph(
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
                    modifier = Modifier.debugContentDescription(TestContentDescriptions.RunningConnectHeartRate),
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
internal fun RunningBlockMetric(
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

/**
 * UI tests: RunningSessionUiTest.runningFinishedPanel_closeButtonInvokesCallback.
 */
@Composable
internal fun RunningFinishedPanel(
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
                    .height(54.dp)
                    .debugContentDescription(TestContentDescriptions.RunningFinishClose),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("닫기")
            }
        }
    }
}

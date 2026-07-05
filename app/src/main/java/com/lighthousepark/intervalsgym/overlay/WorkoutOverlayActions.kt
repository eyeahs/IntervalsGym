package com.lighthousepark.intervalsgym.overlay

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
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal const val REST_NOTIFICATION_CHANNEL_ID = "strength_rest_timer"
internal const val REST_NOTIFICATION_ID = 42
internal const val REST_FINISHED_NOTIFICATION_TIMEOUT_MILLIS = 10_000L
private val restFinishedNotificationSequence = AtomicInteger(0)

internal fun notifyRestFinished(context: Context) {
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
        .setTimeoutAfter(REST_FINISHED_NOTIFICATION_TIMEOUT_MILLIS)
        .build()

    val sequence = restFinishedNotificationSequence.incrementAndGet()
    val appContext = context.applicationContext
    NotificationManagerCompat.from(appContext).notify(REST_NOTIFICATION_ID, notification)
    Handler(Looper.getMainLooper()).postDelayed(
        {
            if (shouldCancelRestFinishedNotification(restFinishedNotificationSequence.get(), sequence)) {
                NotificationManagerCompat.from(appContext).cancel(REST_NOTIFICATION_ID)
            }
        },
        REST_FINISHED_NOTIFICATION_TIMEOUT_MILLIS
    )
}

internal fun shouldCancelRestFinishedNotification(
    currentSequence: Int,
    notificationSequence: Int,
): Boolean {
    return currentSequence == notificationSequence
}

internal fun requestOverlayPermissionIfNeeded(context: Context) {
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

internal fun startRestOverlay(context: Context, title: String, endAtMillis: Long) {
    if (!Settings.canDrawOverlays(context)) return
    val intent = Intent(context, RestTimerOverlayService::class.java).apply {
        putExtra(RestTimerOverlayService.EXTRA_MODE, RestTimerOverlayService.MODE_REST_TIMER)
        putExtra(RestTimerOverlayService.EXTRA_TITLE, title)
        putExtra(RestTimerOverlayService.EXTRA_END_AT, endAtMillis)
    }
    runCatching { context.startService(intent) }
}

internal fun startStrengthSetCompleteOverlay(context: Context, title: String) {
    if (!Settings.canDrawOverlays(context)) return
    val intent = Intent(context, RestTimerOverlayService::class.java).apply {
        putExtra(RestTimerOverlayService.EXTRA_MODE, RestTimerOverlayService.MODE_SET_COMPLETE)
        putExtra(RestTimerOverlayService.EXTRA_TITLE, title)
        putExtra(RestTimerOverlayService.EXTRA_END_AT, 0L)
    }
    runCatching { context.startService(intent) }
}

internal fun stopRestOverlay(context: Context) {
    val intent = Intent(context, RestTimerOverlayService::class.java).apply {
        action = RestTimerOverlayService.ACTION_STOP
    }
    runCatching { context.startService(intent) }
}

internal fun startRunningOverlay(
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

internal fun stopRunningOverlay(context: Context) {
    val intent = Intent(context, RunningWorkoutOverlayService::class.java).apply {
        action = RunningWorkoutOverlayService.ACTION_STOP
    }
    runCatching { context.startService(intent) }
}

internal fun startWorkoutStatusService(
    context: Context,
    workoutType: String,
    title: String,
    phaseLabel: String = "",
    detailText: String = "",
    startAtMillis: Long = 0L,
    endAtMillis: Long = 0L,
    heartRateBpm: Int? = null,
) {
    val intent = Intent(context, WorkoutStatusForegroundService::class.java).apply {
        putExtra(WorkoutStatusForegroundService.EXTRA_WORKOUT_TYPE, workoutType)
        putExtra(WorkoutStatusForegroundService.EXTRA_TITLE, title)
        putExtra(WorkoutStatusForegroundService.EXTRA_PHASE_LABEL, phaseLabel)
        putExtra(WorkoutStatusForegroundService.EXTRA_DETAIL_TEXT, detailText)
        putExtra(WorkoutStatusForegroundService.EXTRA_START_AT, startAtMillis)
        putExtra(WorkoutStatusForegroundService.EXTRA_END_AT, endAtMillis)
        putExtra(WorkoutStatusForegroundService.EXTRA_HEART_RATE_BPM, heartRateBpm ?: 0)
    }
    runCatching { ContextCompat.startForegroundService(context, intent) }
}

internal fun stopWorkoutStatusService(context: Context) {
    val intent = Intent(context, WorkoutStatusForegroundService::class.java).apply {
        action = WorkoutStatusForegroundService.ACTION_STOP
    }
    runCatching { context.startService(intent) }
}

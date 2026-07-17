package com.lighthousepark.intervalsgym.overlay

import com.lighthousepark.intervalsgym.MainActivity
import com.lighthousepark.intervalsgym.R
import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.atomic.AtomicInteger

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
    openAppOnAction: Boolean = false,
    repeatProgress: String = "",
    targetSpeed: String = "",
    targetIncline: String = "",
    heartRateBpm: Int? = null,
) {
    if (!Settings.canDrawOverlays(context)) return
    val intent = Intent(context, RunningSessionOverlayService::class.java).apply {
        putExtra(RunningSessionOverlayService.EXTRA_TITLE, title)
        putExtra(RunningSessionOverlayService.EXTRA_END_AT, endAtMillis)
        putExtra(RunningSessionOverlayService.EXTRA_START_AT, startAtMillis)
        putExtra(RunningSessionOverlayService.EXTRA_ACTION_LABEL, actionLabel)
        putExtra(RunningSessionOverlayService.EXTRA_OPEN_APP_ON_ACTION, openAppOnAction)
        putExtra(RunningSessionOverlayService.EXTRA_REPEAT_PROGRESS, repeatProgress)
        putExtra(RunningSessionOverlayService.EXTRA_TARGET_SPEED, targetSpeed)
        putExtra(RunningSessionOverlayService.EXTRA_TARGET_INCLINE, targetIncline)
        putExtra(RunningSessionOverlayService.EXTRA_HEART_RATE_BPM, heartRateBpm ?: 0)
    }
    runCatching { context.startService(intent) }
}

internal fun stopRunningOverlay(context: Context) {
    val intent = Intent(context, RunningSessionOverlayService::class.java).apply {
        action = RunningSessionOverlayService.ACTION_STOP
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

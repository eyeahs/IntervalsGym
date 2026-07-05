package com.lighthousepark.intervalsgym.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.lighthousepark.intervalsgym.MainActivity
import com.lighthousepark.intervalsgym.R
import java.util.Locale

internal const val WORKOUT_STATUS_CHANNEL_ID = "active_workout_status"
internal const val WORKOUT_STATUS_NOTIFICATION_ID = 44

class WorkoutStatusForegroundService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var workoutType: String = TYPE_STRENGTH
    private var title: String = ""
    private var phaseLabel: String = ""
    private var detailText: String = ""
    private var startAtMillis: Long = 0L
    private var endAtMillis: Long = 0L
    private var heartRateBpm: Int = 0

    private val tick = object : Runnable {
        override fun run() {
            updateNotification()
            handler.postDelayed(this, 1_000L)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY
        when (intent.action) {
            ACTION_STOP -> {
                stopForegroundCompat()
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                workoutType = intent.getStringExtra(EXTRA_WORKOUT_TYPE).orEmpty().ifBlank { TYPE_STRENGTH }
                title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
                phaseLabel = intent.getStringExtra(EXTRA_PHASE_LABEL).orEmpty()
                detailText = intent.getStringExtra(EXTRA_DETAIL_TEXT).orEmpty()
                startAtMillis = intent.getLongExtra(EXTRA_START_AT, 0L)
                endAtMillis = intent.getLongExtra(EXTRA_END_AT, 0L)
                heartRateBpm = intent.getIntExtra(EXTRA_HEART_RATE_BPM, 0)
                ensureChannel()
                startForeground(WORKOUT_STATUS_NOTIFICATION_ID, buildNotification())
                handler.removeCallbacks(tick)
                handler.post(tick)
                return START_REDELIVER_INTENT
            }
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        super.onDestroy()
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        manager.notify(WORKOUT_STATUS_NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            WORKOUT_STATUS_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, WORKOUT_STATUS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notificationTitle())
            .setContentText(notificationText())
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText()))
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()
    }

    private fun notificationTitle(): String {
        return when (workoutType) {
            TYPE_RUNNING -> "런닝 운동 중"
            else -> "웨이트 운동 중"
        }
    }

    private fun notificationText(): String {
        val now = System.currentTimeMillis()
        val timeText = when {
            endAtMillis > now -> "남은 ${formatStatusClock(((endAtMillis - now) / 1000L).toInt())}"
            startAtMillis > 0L -> formatStatusClock(((now - startAtMillis) / 1000L).toInt())
            else -> ""
        }
        val parts = listOf(
            title.takeIf { it.isNotBlank() },
            phaseLabel.takeIf { it.isNotBlank() },
            timeText.takeIf { it.isNotBlank() },
            detailText.takeIf { it.isNotBlank() },
            heartRateBpm.takeIf { it > 0 }?.let { "심박 $it bpm" }
        )
        return parts.filterNotNull().joinToString(" · ")
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            WORKOUT_STATUS_CHANNEL_ID,
            "운동 중",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "런닝/웨이트 운동 중 타이머와 심박 상태"
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    companion object {
        const val ACTION_STOP = "com.lighthousepark.intervalsgym.action.STOP_WORKOUT_STATUS"
        const val EXTRA_WORKOUT_TYPE = "extra_workout_type"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_PHASE_LABEL = "extra_phase_label"
        const val EXTRA_DETAIL_TEXT = "extra_detail_text"
        const val EXTRA_START_AT = "extra_start_at"
        const val EXTRA_END_AT = "extra_end_at"
        const val EXTRA_HEART_RATE_BPM = "extra_heart_rate_bpm"
        const val TYPE_RUNNING = "running"
        const val TYPE_STRENGTH = "strength"
    }
}

internal fun formatStatusClock(seconds: Int): String {
    val safeSeconds = seconds.coerceAtLeast(0)
    val hours = safeSeconds / 3600
    val minutes = (safeSeconds % 3600) / 60
    val remainingSeconds = safeSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, remainingSeconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, remainingSeconds)
    }
}

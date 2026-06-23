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

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import java.util.Locale
import kotlin.math.abs

object RunningOverlayRequests {
    var actionRequest by mutableIntStateOf(0)
        private set

    var openRequest by mutableIntStateOf(0)
        private set

    fun requestAction() {
        actionRequest += 1
    }

    fun requestOpen() {
        openRequest += 1
    }
}

class RunningWorkoutOverlayService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var windowManager: WindowManager? = null
    private var overlayView: LinearLayout? = null
    private var titleView: TextView? = null
    private var timeView: TextView? = null
    private var targetView: TextView? = null
    private var actionButton: Button? = null
    private var endAtMillis: Long = 0L
    private var startAtMillis: Long = 0L
    private var title: String = "Warmup"
    private var actionLabel: String = "종료"
    private var targetSpeed: String = ""
    private var targetIncline: String = ""
    private var heartRateBpm: Int = 0
    private var blinkOn = false

    private val tick = object : Runnable {
        override fun run() {
            updateContent()
            handler.postDelayed(this, 500L)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopSelf()
            else -> {
                title = intent?.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "Warmup" }
                actionLabel = intent?.getStringExtra(EXTRA_ACTION_LABEL).orEmpty()
                targetSpeed = intent?.getStringExtra(EXTRA_TARGET_SPEED).orEmpty()
                targetIncline = intent?.getStringExtra(EXTRA_TARGET_INCLINE).orEmpty()
                heartRateBpm = intent?.getIntExtra(EXTRA_HEART_RATE_BPM, 0) ?: 0
                endAtMillis = intent?.getLongExtra(EXTRA_END_AT, 0L) ?: 0L
                startAtMillis = intent?.getLongExtra(EXTRA_START_AT, 0L)
                    ?.takeIf { it > 0L }
                    ?: System.currentTimeMillis()
                if (Settings.canDrawOverlays(this)) {
                    showOverlay()
                    updateContent()
                    handler.removeCallbacks(tick)
                    handler.post(tick)
                } else {
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        overlayView?.let { view ->
            runCatching { windowManager?.removeView(view) }
        }
        overlayView = null
        titleView = null
        timeView = null
        targetView = null
        actionButton = null
        super.onDestroy()
    }

    private fun showOverlay() {
        if (overlayView != null) return
        windowManager = getSystemService(WindowManager::class.java)

        val titleText = TextView(this).apply {
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
        }
        val timerText = TextView(this).apply {
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
        }
        val targetText = TextView(this).apply {
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xDDFFFFFF.toInt())
            gravity = Gravity.CENTER
            setLineSpacing(2f, 1f)
        }
        val button = Button(this).apply {
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFFFFFFFF.toInt())
            background = GradientDrawable().apply {
                setColor(0xFF2563EB.toInt())
                cornerRadius = 24f
            }
            minHeight = 0
            minWidth = 0
            setPadding(14, 0, 14, 0)
            setOnClickListener {
                RunningOverlayRequests.requestAction()
                launchRunningWorkoutScreen()
            }
        }

        val view = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(18, 12, 18, 6)
            elevation = 10f
            setOnClickListener {
                RunningOverlayRequests.requestOpen()
                launchRunningWorkoutScreen()
            }
            addView(titleText, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            addView(timerText, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            addView(targetText, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            addView(Space(this@RunningWorkoutOverlayService), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
            addView(
                button,
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 58).apply {
                    topMargin = 28
                }
            )
        }
        val params = WindowManager.LayoutParams(
            360,
            348,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 180
        }

        var downX = 0f
        var downY = 0f
        var startX = 0
        var startY = 0
        var didDrag = false
        view.setOnTouchListener { touchedView, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    startX = params.x
                    startY = params.y
                    didDrag = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - downX
                    val deltaY = event.rawY - downY
                    if (abs(deltaX) > 3f || abs(deltaY) > 3f) {
                        didDrag = true
                        params.x = (startX + deltaX).toInt()
                        params.y = (startY + deltaY).toInt()
                        windowManager?.updateViewLayout(touchedView, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!didDrag) {
                        touchedView.performClick()
                    }
                    true
                }
                else -> true
            }
        }

        overlayView = view
        titleView = titleText
        timeView = timerText
        targetView = targetText
        actionButton = button
        windowManager?.addView(view, params)
    }

    private fun updateContent() {
        val now = System.currentTimeMillis()
        val remainingSeconds = if (endAtMillis > 0L) {
            ((endAtMillis - now) / 1000L).coerceAtLeast(0L).toInt()
        } else {
            0
        }
        val isWarmup = endAtMillis <= 0L && title.equals("Warmup", ignoreCase = true)
        val elapsedSeconds = ((now - startAtMillis) / 1000L).coerceAtLeast(0L).toInt()
        titleView?.visibility = View.GONE
        val targetText = if (isWarmup) {
            "Warmup"
        } else {
            listOfNotNull(
                targetSpeed.ifBlank { null }?.let { "속도 $it" },
                targetIncline.ifBlank { null }?.let { "경사도 $it" },
                heartRateBpm.takeIf { it > 0 }?.let { "심박 $it bpm" }
            ).joinToString("\n")
        }
        targetView?.visibility = if (targetText.isBlank()) View.GONE else View.VISIBLE
        targetView?.text = targetText
        actionButton?.visibility = if (actionLabel.isBlank()) View.GONE else View.VISIBLE
        actionButton?.text = actionLabel
        timeView?.visibility = if (endAtMillis > 0L || isWarmup) View.VISIBLE else View.GONE
        timeView?.text = formatRunningOverlayClockText(if (isWarmup) elapsedSeconds else remainingSeconds)
        val urgent = endAtMillis > 0L && remainingSeconds in 1..5
        if (urgent) blinkOn = !blinkOn else blinkOn = false
        val backgroundColor = if (urgent && blinkOn) 0xEFD32F2F.toInt() else 0xDD111827.toInt()
        overlayView?.background = GradientDrawable().apply {
            setColor(backgroundColor)
            cornerRadius = 36f
        }
        if (endAtMillis > 0L && remainingSeconds <= 0) {
            stopSelf()
        }
    }

    private fun launchRunningWorkoutScreen() {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(launchIntent)
    }

    companion object {
        const val ACTION_STOP = "com.lighthousepark.intervalsgym.STOP_RUNNING_OVERLAY"
        const val EXTRA_TITLE = "title"
        const val EXTRA_END_AT = "end_at"
        const val EXTRA_START_AT = "start_at"
        const val EXTRA_ACTION_LABEL = "action_label"
        const val EXTRA_TARGET_SPEED = "target_speed"
        const val EXTRA_TARGET_INCLINE = "target_incline"
        const val EXTRA_HEART_RATE_BPM = "heart_rate_bpm"
    }
}

private fun formatRunningOverlayClockText(seconds: Int): String {
    val minutes = seconds.coerceAtLeast(0) / 60
    val secs = seconds.coerceAtLeast(0) % 60
    return String.format(Locale.US, "%02d:%02d", minutes, secs)
}

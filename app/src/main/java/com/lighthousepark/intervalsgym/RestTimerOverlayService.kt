package com.lighthousepark.intervalsgym

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
import android.view.WindowManager
import android.widget.TextView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

object RestOverlayRequests {
    var showSheetRequest by mutableIntStateOf(0)
        private set

    fun requestShowSheet() {
        showSheetRequest += 1
    }
}

class RestTimerOverlayService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var windowManager: WindowManager? = null
    private var overlayView: TextView? = null
    private var endAtMillis: Long = 0L
    private var title: String = "휴식"

    private val tick = object : Runnable {
        override fun run() {
            val remainingSeconds = ((endAtMillis - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L).toInt()
            overlayView?.text = formatClockText(remainingSeconds)
            if (remainingSeconds > 0) {
                handler.postDelayed(this, 1000L)
            } else {
                stopSelf()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopSelf()
            else -> {
                title = intent?.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "휴식" }
                endAtMillis = intent?.getLongExtra(EXTRA_END_AT, 0L) ?: 0L
                if (Settings.canDrawOverlays(this) && endAtMillis > System.currentTimeMillis()) {
                    showOverlay()
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
        super.onDestroy()
    }

    private fun showOverlay() {
        if (overlayView != null) return
        windowManager = getSystemService(WindowManager::class.java)
        val view = TextView(this).apply {
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(0xDD111827.toInt())
                cornerRadius = 72f
            }
            minWidth = 288
            minHeight = 208
            setPadding(28, 20, 28, 20)
            elevation = 10f
        }
        val params = WindowManager.LayoutParams(
            288,
            208,
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
        var moved = false
        view.setOnTouchListener { touchedView, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    startX = params.x
                    startY = params.y
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - downX
                    val deltaY = event.rawY - downY
                    moved = moved || kotlin.math.abs(deltaX) > 8f || kotlin.math.abs(deltaY) > 8f
                    params.x = (startX + deltaX).toInt()
                    params.y = (startY + (event.rawY - downY)).toInt()
                    windowManager?.updateViewLayout(touchedView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) {
                        touchedView.performClick()
                        RestOverlayRequests.requestShowSheet()
                        val launchIntent = Intent(this@RestTimerOverlayService, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(launchIntent)
                    }
                    true
                }
                else -> true
            }
        }

        overlayView = view
        windowManager?.addView(view, params)
    }

    companion object {
        const val ACTION_STOP = "com.lighthousepark.intervalsgym.STOP_REST_OVERLAY"
        const val EXTRA_TITLE = "title"
        const val EXTRA_END_AT = "end_at"
    }
}

private fun formatClockText(seconds: Int): String {
    val minutes = seconds.coerceAtLeast(0) / 60
    val secs = seconds.coerceAtLeast(0) % 60
    return String.format(Locale.US, "%02d:%02d", minutes, secs)
}

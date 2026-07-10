package com.lighthousepark.intervalsgym.overlay

import com.lighthousepark.intervalsgym.MainActivity
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
    var completeSetRequest by mutableIntStateOf(0)
        private set
    private var consumedShowSheetRequest = 0

    fun requestShowSheet() {
        showSheetRequest += 1
    }

    @Synchronized
    fun consumePendingShowSheetRequest(): Boolean {
        if (showSheetRequest <= consumedShowSheetRequest) return false
        consumedShowSheetRequest = showSheetRequest
        return true
    }

    fun requestCompleteSet() {
        completeSetRequest += 1
    }
}

class RestTimerOverlayService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var windowManager: WindowManager? = null
    private var overlayView: TextView? = null
    private var endAtMillis: Long = 0L
    private var title: String = "휴식"
    private var mode: String = MODE_REST_TIMER

    private val tick = object : Runnable {
        override fun run() {
            if (mode != MODE_REST_TIMER) return
            val remainingSeconds = ((endAtMillis - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L).toInt()
            overlayView?.text = formatRestOverlayText(remainingSeconds)
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
            ACTION_STOP -> stopSelf(startId)
            else -> {
                title = intent?.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "휴식" }
                endAtMillis = intent?.getLongExtra(EXTRA_END_AT, 0L) ?: 0L
                mode = intent?.getStringExtra(EXTRA_MODE).orEmpty().ifBlank { MODE_REST_TIMER }
                val canShow = mode == MODE_SET_COMPLETE || endAtMillis > System.currentTimeMillis()
                if (Settings.canDrawOverlays(this) && canShow) {
                    showOverlay()
                    handler.removeCallbacks(tick)
                    if (mode == MODE_REST_TIMER) {
                        handler.post(tick)
                    } else {
                        overlayView?.text = setCompleteOverlayText()
                    }
                } else {
                    stopSelf(startId)
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
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(0xBB111827.toInt())
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
                        if (mode == MODE_SET_COMPLETE) {
                            RestOverlayRequests.requestCompleteSet()
                        } else {
                            val launchIntent = Intent(this@RestTimerOverlayService, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                putExtra(EXTRA_SHOW_REST_SHEET, true)
                            }
                            startActivity(launchIntent)
                        }
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
        const val EXTRA_MODE = "mode"
        const val EXTRA_SHOW_REST_SHEET = "show_rest_sheet"
        const val MODE_REST_TIMER = "rest_timer"
        const val MODE_SET_COMPLETE = "set_complete"
    }
}

internal fun formatRestOverlayText(seconds: Int): String {
    val minutes = seconds.coerceAtLeast(0) / 60
    val secs = seconds.coerceAtLeast(0) % 60
    return String.format(Locale.US, "휴식\n%02d:%02d", minutes, secs)
}

internal fun setCompleteOverlayText(): String {
    return "세트\n완료"
}

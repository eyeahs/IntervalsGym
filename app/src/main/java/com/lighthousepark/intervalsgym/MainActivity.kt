package com.lighthousepark.intervalsgym

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lighthousepark.intervalsgym.app.IntervalsGymApp
import com.lighthousepark.intervalsgym.overlay.REST_NOTIFICATION_CHANNEL_ID
import com.lighthousepark.intervalsgym.overlay.REST_NOTIFICATION_ID
import com.lighthousepark.intervalsgym.ui.theme.IntervalsGymTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createRestNotificationChannel()
        requestRestNotificationPermission()
        setContent {
            IntervalsGymTheme {
                IntervalsGymApp()
            }
        }
    }

    private fun createRestNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REST_NOTIFICATION_CHANNEL_ID,
                "웨이트 휴식 타이머",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "세트 휴식 종료 알림"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 180, 400)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun requestRestNotificationPermission() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REST_NOTIFICATION_ID)
        }
    }
}

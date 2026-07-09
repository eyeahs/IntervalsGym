package com.lighthousepark.intervalsgym.running

import android.bluetooth.BluetoothDevice

internal const val HEART_RATE_GRAPH_WINDOW_MILLIS = 5 * 60 * 1000L

internal data class HeartRateDevice(
    val name: String,
    val address: String,
    val device: BluetoothDevice,
)

internal data class HeartRateSample(
    val timestampMillis: Long,
    val bpm: Int,
)

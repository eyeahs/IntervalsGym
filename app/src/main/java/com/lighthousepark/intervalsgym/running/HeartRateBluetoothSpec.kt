package com.lighthousepark.intervalsgym.running

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.le.ScanFilter
import android.os.Build
import android.os.ParcelUuid
import java.util.UUID

internal const val HEART_RATE_CONNECT_TIMEOUT_MILLIS = 15_000L
internal const val HEART_RATE_RECONNECT_DELAY_MILLIS = 2_000L

internal val HEART_RATE_SERVICE_UUID: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
internal val HEART_RATE_MEASUREMENT_UUID: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
private val CLIENT_CHARACTERISTIC_CONFIG_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

internal fun heartRateScanFilters(): List<ScanFilter> {
    return listOf(
        ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(HEART_RATE_SERVICE_UUID))
            .build()
    )
}

internal fun BluetoothGatt.heartRateMeasurementCharacteristic(): BluetoothGattCharacteristic? {
    return getService(HEART_RATE_SERVICE_UUID)
        ?.getCharacteristic(HEART_RATE_MEASUREMENT_UUID)
}

@SuppressLint("MissingPermission")
internal fun BluetoothGatt.enableHeartRateMeasurementNotifications(
    characteristic: BluetoothGattCharacteristic,
) {
    setCharacteristicNotification(characteristic, true)
    characteristic
        .getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
        ?.let { descriptor ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                writeDescriptor(
                    descriptor,
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                )
            } else {
                @Suppress("DEPRECATION")
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                @Suppress("DEPRECATION")
                writeDescriptor(descriptor)
            }
        }
}

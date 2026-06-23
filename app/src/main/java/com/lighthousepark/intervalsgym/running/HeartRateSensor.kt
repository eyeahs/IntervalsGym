package com.lighthousepark.intervalsgym.running

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
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import java.util.UUID

internal const val HEART_RATE_GRAPH_WINDOW_MILLIS = 5 * 60 * 1000L
private const val HEART_RATE_CONNECT_TIMEOUT_MILLIS = 15_000L
private val HEART_RATE_SERVICE_UUID: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
private val HEART_RATE_MEASUREMENT_UUID: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
private val CLIENT_CHARACTERISTIC_CONFIG_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

internal data class HeartRateDevice(
    val name: String,
    val address: String,
    val device: BluetoothDevice,
)

internal data class HeartRateSample(
    val timestampMillis: Long,
    val bpm: Int,
)

internal class HeartRateSensorState(
    private val context: Context,
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter = bluetoothManager?.adapter
    private var bluetoothGatt: BluetoothGatt? = null
    private val connectionTimeoutRunnable = Runnable {
        if (isConnecting) {
            val deviceName = connectedDeviceName.orEmpty().ifBlank { "심박계" }
            disconnect()
            statusMessage = "$deviceName 연결 시간이 초과되었습니다."
        }
    }

    var devices by mutableStateOf<List<HeartRateDevice>>(emptyList())
        private set
    var isScanning by mutableStateOf(false)
        private set
    var isConnecting by mutableStateOf(false)
        private set
    var isConnected by mutableStateOf(false)
        private set
    var connectedDeviceName by mutableStateOf<String?>(null)
        private set
    var connectionDeadlineMillis by mutableStateOf(0L)
        private set
    var heartRateBpm by mutableStateOf<Int?>(null)
        private set
    var heartRateSamples by mutableStateOf<List<HeartRateSample>>(emptyList())
        private set
    var statusMessage by mutableStateOf<String?>(null)
        private set

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device ?: return
            val address = runCatching { device.address }.getOrNull().orEmpty()
            if (address.isBlank()) return
            val name = (result.scanRecord?.deviceName
                ?: if (hasConnectPermission()) runCatching { device.name }.getOrNull() else null
                ?: "심박계 $address").orEmpty()
            mainHandler.post {
                if (devices.none { it.address == address }) {
                    devices = devices + HeartRateDevice(name = name, address = address, device = device)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            mainHandler.post {
                isScanning = false
                statusMessage = "심박계 검색 실패: $errorCode"
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> mainHandler.post {
                    clearConnectionTimeout()
                    isConnecting = false
                    isConnected = true
                    connectionDeadlineMillis = 0L
                    statusMessage = "심박계 연결됨"
                    if (hasConnectPermission()) {
                        runCatching { gatt.discoverServices() }
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> mainHandler.post {
                    clearConnectionTimeout()
                    isConnecting = false
                    isConnected = false
                    connectionDeadlineMillis = 0L
                    connectedDeviceName = null
                    heartRateBpm = null
                    statusMessage = "심박계 연결 해제됨"
                    runCatching { gatt.close() }
                    if (bluetoothGatt == gatt) bluetoothGatt = null
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS || !hasConnectPermission()) return
            val characteristic = gatt
                .getService(HEART_RATE_SERVICE_UUID)
                ?.getCharacteristic(HEART_RATE_MEASUREMENT_UUID)
                ?: return
            runCatching {
                gatt.setCharacteristicNotification(characteristic, true)
                characteristic
                    .getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
                    ?.let { descriptor ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            gatt.writeDescriptor(
                                descriptor,
                                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            @Suppress("DEPRECATION")
                            gatt.writeDescriptor(descriptor)
                        }
                    }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
        ) {
            if (characteristic.uuid == HEART_RATE_MEASUREMENT_UUID) {
                @Suppress("DEPRECATION")
                updateHeartRate(characteristic.value)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
        ) {
            if (characteristic.uuid == HEART_RATE_MEASUREMENT_UUID) {
                updateHeartRate(value)
            }
        }
    }

    fun missingPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        ).filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
    }

    fun hasPermissions(): Boolean = missingPermissions().isEmpty()

    fun onPermissionDenied() {
        statusMessage = "심박계 연결 권한이 필요합니다."
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (!hasPermissions()) {
            onPermissionDenied()
            return
        }
        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            statusMessage = "Bluetooth를 켜주세요."
            return
        }
        stopScan()
        devices = emptyList()
        statusMessage = null
        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(HEART_RATE_SERVICE_UUID))
                .build()
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        adapter.bluetoothLeScanner?.startScan(filters, settings, scanCallback)
        isScanning = true
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (isScanning && hasPermissions()) {
            runCatching { bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback) }
        }
        isScanning = false
    }

    @SuppressLint("MissingPermission")
    fun connect(device: HeartRateDevice) {
        if (!hasPermissions()) {
            onPermissionDenied()
            return
        }
        stopScan()
        disconnect()
        isConnecting = true
        connectedDeviceName = device.name
        connectionDeadlineMillis = System.currentTimeMillis() + HEART_RATE_CONNECT_TIMEOUT_MILLIS
        statusMessage = "${device.name} 연결 중"
        scheduleConnectionTimeout()
        bluetoothGatt = device.device.connectGatt(
            context,
            false,
            gattCallback,
            BluetoothDevice.TRANSPORT_LE
        )
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        clearConnectionTimeout()
        if (hasConnectPermission()) {
            runCatching { bluetoothGatt?.disconnect() }
            runCatching { bluetoothGatt?.close() }
        }
        bluetoothGatt = null
        isConnecting = false
        isConnected = false
        connectionDeadlineMillis = 0L
        connectedDeviceName = null
        heartRateBpm = null
        heartRateSamples = emptyList()
    }

    fun close() {
        stopScan()
        disconnect()
    }

    private fun hasConnectPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun scheduleConnectionTimeout() {
        clearConnectionTimeout()
        mainHandler.postDelayed(connectionTimeoutRunnable, HEART_RATE_CONNECT_TIMEOUT_MILLIS)
    }

    private fun clearConnectionTimeout() {
        mainHandler.removeCallbacks(connectionTimeoutRunnable)
    }

    private fun updateHeartRate(value: ByteArray) {
        val bpm = parseHeartRateMeasurement(value) ?: return
        val now = System.currentTimeMillis()
        mainHandler.post {
            clearConnectionTimeout()
            isConnecting = false
            connectionDeadlineMillis = 0L
            heartRateBpm = bpm
            heartRateSamples = (heartRateSamples + HeartRateSample(now, bpm))
                .filter { now - it.timestampMillis <= HEART_RATE_GRAPH_WINDOW_MILLIS }
            isConnected = true
            statusMessage = null
        }
    }
}

@Composable
internal fun rememberHeartRateSensorState(): HeartRateSensorState {
    val context = LocalContext.current.applicationContext
    val state = remember(context) { HeartRateSensorState(context) }
    DisposableEffect(state) {
        onDispose { state.close() }
    }
    return state
}

internal fun parseHeartRateMeasurement(value: ByteArray): Int? {
    if (value.size < 2) return null
    val isUInt16 = (value[0].toInt() and 0x01) == 0x01
    return if (isUInt16) {
        if (value.size < 3) null else {
            (value[1].toInt() and 0xFF) or ((value[2].toInt() and 0xFF) shl 8)
        }
    } else {
        value[1].toInt() and 0xFF
    }?.takeIf { it in 1..255 }
}

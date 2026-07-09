package com.lighthousepark.intervalsgym.running

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

private var sharedHeartRateSensorState: HeartRateSensorState? = null

internal class HeartRateSensorState(
    private val context: Context,
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter = bluetoothManager?.adapter
    private var bluetoothGatt: BluetoothGatt? = null
    private var selectedDevice: HeartRateDevice? = null
    private var shouldAutoReconnect = false
    private val reconnectRunnable = Runnable {
        val device = selectedDevice ?: return@Runnable
        if (shouldAutoReconnect && hasPermissions()) {
            connectInternal(device, isReconnect = true)
        }
    }
    private val connectionTimeoutRunnable = Runnable {
        if (isConnecting) {
            val deviceName = connectedDeviceName.orEmpty().ifBlank { "심박계" }
            closeGatt()
            isConnecting = false
            isConnected = false
            connectionDeadlineMillis = 0L
            heartRateBpm = null
            if (shouldAutoReconnect && selectedDevice != null) {
                statusMessage = "$deviceName 연결 시간이 초과되어 재시도합니다."
                scheduleReconnect()
            } else {
                connectedDeviceName = null
                statusMessage = "$deviceName 연결 시간이 초과되었습니다."
            }
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
                    clearReconnect()
                    isConnecting = false
                    isConnected = true
                    connectionDeadlineMillis = 0L
                    statusMessage = "심박계 연결됨"
                    if (hasConnectPermission()) {
                        runCatching { gatt.discoverServices() }
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> mainHandler.post {
                    val isActiveGatt = bluetoothGatt == gatt
                    clearConnectionTimeout()
                    isConnected = false
                    connectionDeadlineMillis = 0L
                    heartRateBpm = null
                    runCatching { gatt.close() }
                    if (!isActiveGatt) return@post
                    bluetoothGatt = null
                    val reconnectDevice = selectedDevice
                    if (shouldAutoReconnect && reconnectDevice != null && hasPermissions()) {
                        isConnecting = true
                        connectedDeviceName = reconnectDevice.name
                        statusMessage = "${reconnectDevice.name} 재연결 중"
                        scheduleReconnect()
                    } else {
                        isConnecting = false
                        connectedDeviceName = null
                        statusMessage = "심박계 연결 해제됨"
                    }
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS || !hasConnectPermission()) return
            val characteristic = gatt.heartRateMeasurementCharacteristic()
                ?: return
            runCatching {
                gatt.enableHeartRateMeasurementNotifications(characteristic)
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
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        adapter.bluetoothLeScanner?.startScan(heartRateScanFilters(), settings, scanCallback)
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
        selectedDevice = device
        shouldAutoReconnect = true
        connectInternal(device, isReconnect = false)
    }

    @SuppressLint("MissingPermission")
    private fun connectInternal(device: HeartRateDevice, isReconnect: Boolean) {
        if (!hasPermissions()) {
            onPermissionDenied()
            return
        }
        clearReconnect()
        stopScan()
        closeGatt()
        isConnecting = true
        connectedDeviceName = device.name
        connectionDeadlineMillis = System.currentTimeMillis() + HEART_RATE_CONNECT_TIMEOUT_MILLIS
        statusMessage = if (isReconnect) "${device.name} 재연결 중" else "${device.name} 연결 중"
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
        shouldAutoReconnect = false
        selectedDevice = null
        clearReconnect()
        clearConnectionTimeout()
        closeGatt()
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

    fun releaseUi() {
        stopScan()
    }

    @SuppressLint("MissingPermission")
    private fun closeGatt() {
        if (hasConnectPermission()) {
            runCatching { bluetoothGatt?.disconnect() }
            runCatching { bluetoothGatt?.close() }
        }
        bluetoothGatt = null
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

    private fun scheduleReconnect() {
        clearReconnect()
        mainHandler.postDelayed(reconnectRunnable, HEART_RATE_RECONNECT_DELAY_MILLIS)
    }

    private fun clearReconnect() {
        mainHandler.removeCallbacks(reconnectRunnable)
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
    val state = remember(context) {
        sharedHeartRateSensorState ?: HeartRateSensorState(context).also { sharedHeartRateSensorState = it }
    }
    DisposableEffect(state) {
        onDispose { state.releaseUi() }
    }
    return state
}

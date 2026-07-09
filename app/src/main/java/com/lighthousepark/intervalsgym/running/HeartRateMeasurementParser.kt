package com.lighthousepark.intervalsgym.running

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

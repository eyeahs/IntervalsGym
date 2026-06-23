package com.lighthousepark.intervalsgym

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HeartRateSensorTest {
    @Test
    fun parseHeartRateMeasurement_readsUInt8Bpm() {
        assertEquals(72, parseHeartRateMeasurement(byteArrayOf(0x00, 72)))
    }

    @Test
    fun parseHeartRateMeasurement_readsUInt16Bpm() {
        assertEquals(180, parseHeartRateMeasurement(byteArrayOf(0x01, 0xB4.toByte(), 0x00)))
    }

    @Test
    fun parseHeartRateMeasurement_rejectsMissingOrInvalidValues() {
        assertNull(parseHeartRateMeasurement(byteArrayOf()))
        assertNull(parseHeartRateMeasurement(byteArrayOf(0x00, 0x00)))
    }
}

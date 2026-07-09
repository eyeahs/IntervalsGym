package com.lighthousepark.intervalsgym.running

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HeartRateMeasurementParserTest {
    @Test
    fun parseHeartRateMeasurement_readsUInt8Bpm() {
        assertEquals(72, parseHeartRateMeasurement(byteArrayOf(0x00, 72)))
    }

    @Test
    fun parseHeartRateMeasurement_readsUInt16Bpm() {
        assertEquals(180, parseHeartRateMeasurement(byteArrayOf(0x01, 0xB4.toByte(), 0x00)))
    }

    @Test
    fun parseHeartRateMeasurement_ignoresOptionalFieldsAfterBpm() {
        assertEquals(
            88,
            parseHeartRateMeasurement(
                byteArrayOf(
                    0x1E,
                    88,
                    0x34,
                    0x12,
                    0x02,
                    0x01
                )
            )
        )
        assertEquals(
            199,
            parseHeartRateMeasurement(
                byteArrayOf(
                    0x1F,
                    0xC7.toByte(),
                    0x00,
                    0x34,
                    0x12,
                    0x02,
                    0x01
                )
            )
        )
    }

    @Test
    fun parseHeartRateMeasurement_rejectsMissingOrInvalidValues() {
        assertNull(parseHeartRateMeasurement(byteArrayOf()))
        assertNull(parseHeartRateMeasurement(byteArrayOf(0x00, 0x00)))
        assertNull(parseHeartRateMeasurement(byteArrayOf(0x01, 0x00, 0x00)))
    }
}

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

package com.lighthousepark.intervalsgym.data

import org.junit.Assert.assertEquals
import org.junit.Test

class NativeAppBridgeTest {
    @Test
    fun nativeBridgeReturnsBlankWhenLibrariesAreUnavailableOnJvm() {
        assertEquals("", NativeAppBridge.alphaOrBlank())
        assertEquals("", NativeAppBridge.betaOrBlank())
    }
}

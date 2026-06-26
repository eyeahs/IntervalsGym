package com.lighthousepark.intervalsgym.data

internal object NativeAppBridge {
    private val isLoaded = runCatching {
        System.loadLibrary("panel_mesh")
        System.loadLibrary("grid_frame")
    }.isSuccess

    private external fun readAlpha(): String

    private external fun readBeta(): String

    fun alphaOrBlank(): String {
        if (!isLoaded) return ""
        return runCatching { readAlpha() }.getOrDefault("")
    }

    fun betaOrBlank(): String {
        if (!isLoaded) return ""
        return runCatching { readBeta() }.getOrDefault("")
    }
}

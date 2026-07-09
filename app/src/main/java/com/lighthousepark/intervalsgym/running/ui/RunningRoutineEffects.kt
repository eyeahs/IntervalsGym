package com.lighthousepark.intervalsgym.running.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

@Composable
internal fun RefreshRunningRoutinesOnResume(onResume: () -> Unit) {
    val context = LocalContext.current
    DisposableEffect(context) {
        val lifecycle = (context as? LifecycleOwner)?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onResume()
            }
        }
        lifecycle?.addObserver(observer)
        onDispose {
            lifecycle?.removeObserver(observer)
        }
    }
}

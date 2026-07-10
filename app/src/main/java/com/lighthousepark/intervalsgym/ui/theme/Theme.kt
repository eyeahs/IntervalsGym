package com.lighthousepark.intervalsgym.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AppMint,
    onPrimary = AppBackground,
    primaryContainer = AppSurfaceSelected,
    onPrimaryContainer = AppMint,
    secondary = AppMintSoft,
    onSecondary = AppBackground,
    secondaryContainer = AppSurfaceHigh,
    onSecondaryContainer = AppText,
    tertiary = AppMintSoft,
    onTertiary = AppBackground,
    tertiaryContainer = AppBlueMuted,
    onTertiaryContainer = AppOnBlueMuted,
    background = AppBackground,
    onBackground = AppText,
    surface = AppSurface,
    onSurface = AppText,
    surfaceVariant = AppSurfaceHigh,
    onSurfaceVariant = AppTextMuted,
    outline = AppOutline,
    outlineVariant = AppSurfaceHigh,
    error = androidx.compose.ui.graphics.Color(0xFFFF7A8A),
    onError = AppBackground
)

@Composable
fun IntervalsGymTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

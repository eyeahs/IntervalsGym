package com.lighthousepark.intervalsgym.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AppHighlight,
    onPrimary = AppBackground,
    primaryContainer = AppHighlightContainer,
    onPrimaryContainer = AppHighlight,
    inversePrimary = AppHighlight,
    secondary = AppCoolAccent,
    onSecondary = AppBackground,
    secondaryContainer = AppSurfaceHigh,
    onSecondaryContainer = AppText,
    tertiary = AppCoolAccentMuted,
    onTertiary = AppBackground,
    tertiaryContainer = AppCoolContainer,
    onTertiaryContainer = AppOnCoolContainer,
    background = AppBackground,
    onBackground = AppText,
    surface = AppSurface,
    onSurface = AppText,
    surfaceVariant = AppSurfaceHigh,
    onSurfaceVariant = AppTextMuted,
    surfaceTint = AppCoolAccent,
    inverseSurface = AppHighlightContainer,
    inverseOnSurface = AppBackground,
    outline = AppOutline,
    outlineVariant = AppOutlineSoft,
    error = AppDanger,
    onError = AppHighlightContainer,
    errorContainer = AppDangerContainer,
    onErrorContainer = AppText,
    scrim = AppHighlightContainer,
    surfaceBright = AppSurfaceBright,
    surfaceContainerLowest = AppSurfaceBright,
    surfaceContainerLow = AppSurface,
    surfaceContainer = AppSurfaceContainer,
    surfaceContainerHigh = AppSurfaceContainerHigh,
    surfaceContainerHighest = AppCoolAccentMuted,
    surfaceDim = AppSurfaceDim
)

@Composable
fun IntervalsGymTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

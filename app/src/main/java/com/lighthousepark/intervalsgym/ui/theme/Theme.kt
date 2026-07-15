package com.lighthousepark.intervalsgym.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AppHighlight,
    onPrimary = AppBackground,
    primaryContainer = AppHighlightContainer,
    onPrimaryContainer = AppOnHighlightContainer,
    inversePrimary = AppHighlight,
    secondary = AppCoolAccent,
    onSecondary = AppBackground,
    secondaryContainer = AppCoolContainer,
    onSecondaryContainer = AppOnCoolContainer,
    tertiary = AppCoolAccentMuted,
    onTertiary = AppBackground,
    tertiaryContainer = AppSurfaceHigh,
    onTertiaryContainer = AppOnCoolContainer,
    background = AppBackground,
    onBackground = AppText,
    surface = AppSurface,
    onSurface = AppText,
    surfaceVariant = AppSurfaceHigh,
    onSurfaceVariant = AppTextMuted,
    surfaceTint = AppSurfaceHigh,
    inverseSurface = AppInverseSurface,
    inverseOnSurface = AppInverseText,
    outline = AppOutline,
    outlineVariant = AppOutlineSoft,
    error = AppDanger,
    onError = AppBackground,
    errorContainer = AppDangerContainer,
    onErrorContainer = AppOnDangerContainer,
    scrim = AppScrim,
    surfaceBright = AppSurfaceBright,
    surfaceContainerLowest = AppSurfaceContainerLowest,
    surfaceContainerLow = AppSurfaceContainerLow,
    surfaceContainer = AppSurfaceContainer,
    surfaceContainerHigh = AppSurfaceContainerHigh,
    surfaceContainerHighest = AppSurfaceContainerHighest,
    surfaceDim = AppSurfaceDim
)

@Composable
fun IntervalsGymTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

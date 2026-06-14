package com.wodtimer.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Green,
    onPrimary = Color.Black,
    primaryContainer = ButtonPrimaryContainer,
    onPrimaryContainer = Green,
    secondary = TimerYellow,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF3A3A00),
    onSecondaryContainer = TimerYellow,
    tertiary = Blue,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DividerColor,
    outlineVariant = Color(0xFF3A3A3A),
    error = Red,
    onError = Color.Black,
    errorContainer = ButtonDangerContainer,
    onErrorContainer = Red,
    surfaceTint = Green,
    inverseSurface = DarkOnBackground,
    inverseOnSurface = DarkBackground,
    inversePrimary = Green
)

@Composable
fun WODTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WODTypography,
        content = content
    )
}

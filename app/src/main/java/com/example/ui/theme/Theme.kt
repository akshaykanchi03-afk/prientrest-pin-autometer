package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PinFlowPurple,
    onPrimary = Color.White,
    primaryContainer = PinFlowPurpleDark,
    onPrimaryContainer = PillDraftLavender,
    secondary = PillScheduledBlue,
    onSecondary = SlateDark,
    secondaryContainer = SlateMedium,
    onSecondaryContainer = Color.White,
    background = SlateDark,
    onBackground = Color.White,
    surface = SlateMedium,
    onSurface = Color.White,
    surfaceVariant = SlateMedium,
    onSurfaceVariant = Color.White.copy(alpha = 0.8f),
    error = Color(0xFFCF6679),
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = PinFlowPurple,
    onPrimary = Color.White,
    primaryContainer = PillDraftLavender,
    onPrimaryContainer = PinFlowPurpleDark,
    secondary = PinFlowTextSecondary,
    onSecondary = Color.White,
    secondaryContainer = PinFlowSurface,
    onSecondaryContainer = PinFlowTextPrimary,
    background = PinFlowLightBackground,
    onBackground = PinFlowTextPrimary,
    surface = PinFlowSurface,
    onSurface = PinFlowTextPrimary,
    surfaceVariant = PinFlowSurfaceVariant,
    onSurfaceVariant = PinFlowTextSecondary,
    error = Color(0xFFB00020),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep dynamic color toggle if they want native themed wallpapers, but fallback is our primary Red
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.scriptrunner.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark theme colors
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF589DF6),
    onPrimary = Color.White,
    secondary = Color(0xFF6897BB),
    onSecondary = Color.White,
    background = Color(0xFF2B2B2B),
    onBackground = Color(0xFFA9B7C6),
    surface = Color(0xFF3C3F41),
    onSurface = Color(0xFFA9B7C6),
    surfaceVariant = Color(0xFF313335),
    onSurfaceVariant = Color(0xFFBBBBBB),
    outline = Color(0xFF555555),
    error = Color(0xFFFF6B6B),
    onError = Color.White
)

// Light theme colors
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2196F3),
    onPrimary = Color.White,
    secondary = Color(0xFF6897BB),
    onSecondary = Color.White,
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1E1E1E),
    surface = Color(0xFFF7F7F7),
    onSurface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFFE8E8E8),
    onSurfaceVariant = Color(0xFF444444),
    outline = Color(0xFFCCCCCC),
    error = Color(0xFFD32F2F),
    onError = Color.White
)

@Composable
fun ScriptRunnerTheme(
    isDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

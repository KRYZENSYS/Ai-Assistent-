package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CyberPrimary,
    secondary = CyberSecondary,
    tertiary = CyberTertiary,
    background = CyberBackground,
    surface = CyberSurface,
    onPrimary = CyberBackground,
    onSecondary = CyberTextPrimary,
    onTertiary = CyberBackground,
    onBackground = CyberTextPrimary,
    onSurface = CyberTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = CyberLightPrimary,
    secondary = CyberLightSecondary,
    tertiary = CyberLightTertiary,
    background = CyberLightBackground,
    surface = CyberLightSurface,
    onPrimary = CyberLightSurface,
    onSecondary = CyberLightTextPrimary,
    onTertiary = CyberLightSurface,
    onBackground = CyberLightTextPrimary,
    onSurface = CyberLightTextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable system dynamic color to preserve original neon cyber theme styling
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

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
    primary = ProBlueDarkPrimary,
    onPrimary = ProBlueDarkOnPrimary,
    primaryContainer = ProBlueDarkPrimaryContainer,
    onPrimaryContainer = ProBlueDarkOnPrimaryContainer,
    secondary = ProAmberDarkSecondary,
    onSecondary = ProAmberDarkOnSecondary,
    secondaryContainer = ProAmberDarkSecondaryContainer,
    onSecondaryContainer = ProAmberDarkOnSecondaryContainer,
    background = ProBackgroundDark,
    surface = ProSurfaceDark,
    onSurface = ProOnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = ProBluePrimary,
    onPrimary = ProBlueOnPrimary,
    primaryContainer = ProBluePrimaryContainer,
    onPrimaryContainer = ProBlueOnPrimaryContainer,
    secondary = ProAmberSecondary,
    onSecondary = ProAmberSecondary,
    secondaryContainer = ProAmberSecondaryContainer,
    onSecondaryContainer = ProAmberOnSecondaryContainer,
    background = ProBackgroundLight,
    surface = ProSurfaceLight,
    onSurface = ProOnSurfaceLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Curated professional palette by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

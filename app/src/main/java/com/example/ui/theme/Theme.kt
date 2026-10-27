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
    primary = SteelBlueDarkPrimary,
    onPrimary = SteelBlueDarkOnPrimary,
    primaryContainer = SteelBlueDarkPrimaryContainer,
    onPrimaryContainer = SteelBlueDarkOnPrimaryContainer,
    secondary = ConstructionAmberDarkSecondary,
    onSecondary = ConstructionAmberDarkOnSecondary,
    secondaryContainer = ConstructionAmberDarkSecondaryContainer,
    onSecondaryContainer = ConstructionAmberDarkOnSecondaryContainer,
    tertiary = MineralTealDarkTertiary,
    onTertiary = MineralTealDarkOnTertiary,
    tertiaryContainer = MineralTealDarkTertiaryContainer,
    onTertiaryContainer = MineralTealDarkOnTertiaryContainer
)

private val LightColorScheme = lightColorScheme(
    primary = SteelBlueLightPrimary,
    onPrimary = SteelBlueLightOnPrimary,
    primaryContainer = SteelBlueLightPrimaryContainer,
    onPrimaryContainer = SteelBlueLightOnPrimaryContainer,
    secondary = ConstructionAmberLightSecondary,
    secondaryContainer = ConstructionAmberLightSecondaryContainer,
    onSecondaryContainer = ConstructionAmberLightOnSecondaryContainer,
    tertiary = MineralTealLightTertiary,
    tertiaryContainer = MineralTealLightTertiaryContainer,
    onTertiaryContainer = MineralTealLightOnTertiaryContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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

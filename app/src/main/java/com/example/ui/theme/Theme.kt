package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = ForestContainer,
    onPrimaryContainer = ForestGreen,
    secondary = ClayOrange,
    onSecondary = Color.White,
    secondaryContainer = ClayContainer,
    onSecondaryContainer = ClayOrange,
    tertiary = SunGold,
    onTertiary = Ink900,
    background = PaperWhite,
    onBackground = Ink900,
    surface = Color.White,
    onSurface = Ink900,
    surfaceVariant = PaperSecondary,
    onSurfaceVariant = Ink600,
    outline = Color(0xFFD4CDC0)
)

private val DarkColorScheme = darkColorScheme(
    primary = ForestGreenLight,
    onPrimary = Color.White,
    primaryContainer = ForestGreen,
    onPrimaryContainer = ForestContainer,
    secondary = ClayOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF522818),
    onSecondaryContainer = ClayContainer,
    tertiary = SunGold,
    onTertiary = Ink900,
    background = Ink900,
    onBackground = PaperWhite,
    surface = Color(0xFF223028),
    onSurface = PaperWhite,
    surfaceVariant = Color(0xFF2D3D34),
    onSurfaceVariant = Color(0xFFB0BEB6),
    outline = Color(0xFF45574D)
)

@Composable
fun HemQuestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    HemQuestTheme(darkTheme = darkTheme, content = content)
}

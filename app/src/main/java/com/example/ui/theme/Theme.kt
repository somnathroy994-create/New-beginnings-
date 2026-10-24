package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = DeepSlate900,
    primaryContainer = Color(0xFF0369A1),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = AmberGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = AmberLight,
    tertiary = VioletPurple,
    background = DeepSlate900,
    onBackground = Slate100,
    surface = Slate800,
    onSurface = Slate100,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate200,
    outline = Slate600
)

fun createLightColorScheme(lightOption: LightBackgroundOption = LightBackgroundOption.CRISP_WHITE) = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = AmberGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = VioletPurple,
    background = lightOption.backgroundColor,
    onBackground = lightOption.onBackgroundColor,
    surface = lightOption.surfaceColor,
    onSurface = lightOption.onBackgroundColor,
    surfaceVariant = lightOption.surfaceVariantColor,
    onSurfaceVariant = Slate700,
    outline = Color(0xFFCBD5E1)
)

private val DefaultLightColorScheme = createLightColorScheme(LightBackgroundOption.CRISP_WHITE)

@Composable
fun ReadAloudTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    lightBackgroundOption: LightBackgroundOption = LightBackgroundOption.CRISP_WHITE,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else createLightColorScheme(lightBackgroundOption)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

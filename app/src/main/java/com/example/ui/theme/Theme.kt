package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CasaOgumDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = NavyDeep,
    primaryContainer = NavyElevated,
    onPrimaryContainer = GoldLight,
    secondary = GoldBright,
    onSecondary = NavyDeep,
    secondaryContainer = NavyCard,
    onSecondaryContainer = IvoryText,
    tertiary = GoldLight,
    onTertiary = NavyDeep,
    background = NavyPrimary,
    onBackground = IvoryText,
    surface = NavySurface,
    onSurface = IvoryText,
    surfaceVariant = NavyCard,
    onSurfaceVariant = SilverSubtext,
    outline = GoldMuted,
    error = StatusOverdueRed,
    onError = Color.White
)

private val CasaOgumLightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = GoldLight,
    primaryContainer = NavySurface,
    onPrimaryContainer = GoldPrimary,
    secondary = GoldDark,
    onSecondary = Color.White,
    secondaryContainer = CreamCard,
    onSecondaryContainer = NavyPrimary,
    tertiary = GoldPrimary,
    onTertiary = NavyDeep,
    background = CreamBackground,
    onBackground = NavyPrimary,
    surface = CreamSurface,
    onSurface = NavyPrimary,
    surfaceVariant = CreamCard,
    onSurfaceVariant = NavyElevated,
    outline = GoldDark,
    error = StatusOverdueRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to the signature Navy-Blue & Gold institutional look
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) CasaOgumDarkColorScheme else CasaOgumLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

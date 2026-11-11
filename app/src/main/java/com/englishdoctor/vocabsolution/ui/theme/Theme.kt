package com.englishdoctor.vocabsolution.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

val LocalDarkTheme = staticCompositionLocalOf { false }

private val LightColors = lightColorScheme(
    primary = BrandNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B3E),
    secondary = Color(0xFF8A6100),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDEA6),
    onSecondaryContainer = Color(0xFF2B1D00),
    tertiary = Color(0xFF00897B),
    background = Color(0xFFF8F9FC),
    surface = Color(0xFFF8F9FC),
    surfaceVariant = Color(0xFFE1E5EE),
    surfaceContainer = Color(0xFFEEF1F7),
    surfaceContainerHigh = Color(0xFFE8EBF2),
    error = WrongRed,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA9C7FF),
    onPrimary = Color(0xFF002F65),
    primaryContainer = BrandNavyLight,
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = BrandGold,
    onSecondary = Color(0xFF3F2E00),
    secondaryContainer = Color(0xFF5B4300),
    onSecondaryContainer = Color(0xFFFFDEA6),
    tertiary = Color(0xFF4DB6AC),
    background = Color(0xFF10131A),
    surface = Color(0xFF10131A),
    surfaceVariant = Color(0xFF2B303B),
    surfaceContainer = Color(0xFF1C2029),
    surfaceContainerHigh = Color(0xFF242934),
    error = Color(0xFFFFB4AB),
)

@Composable
fun VocabTheme(dark: Boolean, fontScale: Float = 1f, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDarkTheme provides dark,
        LocalDensity provides Density(density.density, density.fontScale * fontScale),
    ) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = VocabTypography,
            content = content,
        )
    }
}

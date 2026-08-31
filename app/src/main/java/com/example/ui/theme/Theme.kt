package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DataKillerColorScheme = darkColorScheme(
    primary = AndroidGreen,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0F3D24),
    onPrimaryContainer = NeonLime,
    secondary = HologramCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF003B42),
    onSecondaryContainer = HologramCyan,
    tertiary = QuantumAmber,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF4A3400),
    onTertiaryContainer = QuantumAmber,
    background = ObsidianDark,
    onBackground = TextHighEmphasis,
    surface = ObsidianSurface,
    onSurface = TextHighEmphasis,
    surfaceVariant = ObsidianSurfaceElevated,
    onSurfaceVariant = TextMediumEmphasis,
    outline = ObsidianBorder,
    outlineVariant = Color(0xFF2E384D),
    error = LaserRose,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DataKillerColorScheme,
        typography = Typography,
        content = content
    )
}

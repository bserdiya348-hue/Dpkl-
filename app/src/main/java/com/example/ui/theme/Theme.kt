package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = KabaddiOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF7C2D12),
    onPrimaryContainer = KabaddiOrangeLight,
    secondary = KabaddiGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = KabaddiGoldLight,
    tertiary = ActionGreen,
    onTertiary = Color.White,
    background = StadiumDarkBg,
    onBackground = TextPrimary,
    surface = StadiumSurface,
    onSurface = TextPrimary,
    surfaceVariant = StadiumSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = StadiumBorder,
    error = ActionRed,
    onError = Color.White
)

private val LightColorScheme = darkColorScheme(
    primary = KabaddiOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBC9),
    onPrimaryContainer = Color(0xFF3B1000),
    secondary = KabaddiGold,
    onSecondary = Color.Black,
    tertiary = ActionGreen,
    background = StadiumDarkBg, // Keep sports dark look for immersive broadcast feel
    onBackground = TextPrimary,
    surface = StadiumSurface,
    onSurface = TextPrimary,
    surfaceVariant = StadiumSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = StadiumBorder,
    error = ActionRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep signature consistent sports identity
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

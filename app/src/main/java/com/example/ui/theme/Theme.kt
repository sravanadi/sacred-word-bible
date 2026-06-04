package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    background = Color(0xFF252729),      // Sleek Ash Grey Background
    surface = Color(0xFF303336),         // Lighter Ash Grey surface
    onBackground = Color(0xFFECEFF1),    // Light grey-white text
    onSurface = Color(0xFFECEFF1),
    secondary = GoldPrimary,
    onSecondary = Color(0xFF252729),     // Ash grey contrast on gold secondary
    surfaceVariant = Color(0xFF3B3E41),  // Soft lighter ash gray variant
    onSurfaceVariant = Color(0xFFCFD8DC) // Soft light-grey body text
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFC9A84C), // Elegant Gold
    background = Color(0xFFF9F7F3), // Soft Warm White
    surface = Color(0xFFEFECE4), // Soft Sand Surface
    onBackground = Color(0xFF1E1E1E), // Dark Off-Black
    onSurface = Color(0xFF1E1E1E),
    secondary = Color(0xFFC9A84C),
    onSecondary = Color(0xFFF9F7F3),
    surfaceVariant = Color(0xFFE5E1D5),
    onSurfaceVariant = Color(0xFF4A4A4A)
)

@Composable
fun SacredWordTheme(
    isDark: Boolean = true,
    fontFamilyName: String = "sans",
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    val font = when (fontFamilyName) {
        "amarante" -> AmaranteFont
        "sourgummy" -> SourGummyFont
        else -> FontFamily.SansSerif
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = getDynamicTypography(font),
        content = content
    )
}

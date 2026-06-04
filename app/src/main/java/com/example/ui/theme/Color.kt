package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val NavyBackground = Color(0xFF0D1B2A)
val NavySurface    = Color(0xFF162336)
val GoldPrimary    = Color(0xFFC9A84C)
val CreamText      = Color(0xFFF5F0E8)
val MutedLabel     = Color(0xFF8A9BB0)

// Highlight colors as string constants and Compose Color values
const val HEX_HighlightPurple = "#7B5EA7"
const val HEX_HighlightAmber  = "#E8A838"
const val HEX_HighlightMint   = "#4CAF80"
const val HEX_HighlightRose   = "#E06B6B"
const val HEX_HighlightSky    = "#5BA4CF"

val HighlightPurple = Color(0xFF7B5EA7)
val HighlightAmber  = Color(0xFFE8A838)
val HighlightMint   = Color(0xFF4CAF80)
val HighlightRose   = Color(0xFFE06B6B)
val HighlightSky    = Color(0xFF5BA4CF)

// Mapper utility to convert from Hex stored in Highlights Entity to Compose Color
fun getHighlightColor(hex: String?): Color? {
    return when (hex?.uppercase()) {
        HEX_HighlightPurple, "0xFF7B5EA7" -> HighlightPurple
        HEX_HighlightAmber, "0xFFE8A838" -> HighlightAmber
        HEX_HighlightMint, "0xFF4CAF80" -> HighlightMint
        HEX_HighlightRose, "0xFFE06B6B" -> HighlightRose
        HEX_HighlightSky, "0xFF5BA4CF" -> HighlightSky
        else -> null
    }
}

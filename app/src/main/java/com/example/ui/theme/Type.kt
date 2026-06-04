package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Elegant Serif system font as fallback for Amarante
val AmaranteFont = FontFamily.Serif

// Playful cursive/handwritten system font as fallback for Sour Gummy
val SourGummyFont = FontFamily.Cursive

fun getDynamicTypography(font: FontFamily): Typography {
    val defaultTypography = androidx.compose.material3.Typography()
    return androidx.compose.material3.Typography(
        // H1: Custom Serif or user-chosen Font, 26sp
        displayLarge = TextStyle(
            fontFamily = font,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            letterSpacing = 0.sp
        ),
        displayMedium = TextStyle(
            fontFamily = font,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            letterSpacing = 0.sp
        ),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = font),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = font),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = font),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = font),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = font),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = font),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = font),
        // Scripture style: Custom user Font family 18sp, lineHeight 30sp
        bodyLarge = TextStyle(
            fontFamily = font,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 30.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = font),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = font),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = font),
        // UI labels & Navigation: Custom user Font family 14sp
        labelMedium = TextStyle(
            fontFamily = font,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            letterSpacing = 0.25.sp
        ),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = font)
    )
}

// Centralized typography styles for Sacred Word (Offline Bible app) - fallback
val FallbackTypography = getDynamicTypography(FontFamily.SansSerif)

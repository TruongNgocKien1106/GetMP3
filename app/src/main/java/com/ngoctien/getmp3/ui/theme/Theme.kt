package com.ngoctien.getmp3.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.settings.AppThemeMode

private val LightColors =
    lightColorScheme(
        primary = BrandBlue,
        onPrimary = Color.White,

        primaryContainer =
            Color(0xFFDDE8FF),

        onPrimaryContainer =
            Color(0xFF10234C),

        secondary = BrandViolet,
        onSecondary = Color.White,

        secondaryContainer =
            Color(0xFFEAE5FF),

        onSecondaryContainer =
            Color(0xFF2A2057),

        tertiary = BrandCyan,
        onTertiary =
            Color(0xFF082C35),

        background = LightBackground,
        onBackground = LightText,

        surface = LightSurface,
        onSurface = LightText,

        surfaceVariant =
            LightSurfaceSoft,

        onSurfaceVariant =
            LightTextMuted,

        outline = LightBorder,

        error = ErrorRed,
        errorContainer =
            ErrorBackground
    )

private val DarkColors =
    darkColorScheme(
        primary = Color(0xFF8CB5FF),
        onPrimary =
            Color(0xFF082659),

        primaryContainer =
            Color(0xFF264C89),

        onPrimaryContainer =
            Color(0xFFDDE8FF),

        secondary =
            Color(0xFFBFB3FF),

        onSecondary =
            Color(0xFF2A2057),

        secondaryContainer =
            Color(0xFF41367B),

        onSecondaryContainer =
            Color(0xFFEAE5FF),

        tertiary =
            Color(0xFF75D9EB),

        onTertiary =
            Color(0xFF07343E),

        background = DarkBackground,
        onBackground = DarkText,

        surface = DarkSurface,
        onSurface = DarkText,

        surfaceVariant =
            DarkSurfaceSoft,

        onSurfaceVariant =
            DarkTextMuted,

        outline = DarkBorder,

        error =
            Color(0xFFFFAAB6),

        errorContainer =
            Color(0xFF6A2331)
    )

private val GetMp3Shapes =
    Shapes(
        extraSmall =
            RoundedCornerShape(10.dp),

        small =
            RoundedCornerShape(14.dp),

        medium =
            RoundedCornerShape(20.dp),

        large =
            RoundedCornerShape(26.dp),

        extraLarge =
            RoundedCornerShape(32.dp)
    )

@Composable
fun GetMP3Theme(
    themeMode: AppThemeMode =
        AppThemeMode.SYSTEM,

    content: @Composable () -> Unit
) {
    val useDarkTheme =
        when (themeMode) {
            AppThemeMode.SYSTEM ->
                isSystemInDarkTheme()

            AppThemeMode.LIGHT ->
                false

            AppThemeMode.DARK ->
                true
        }

    MaterialTheme(
        colorScheme = if (useDarkTheme) {
            DarkColors
        } else {
            LightColors
        },

        typography = Typography,
        shapes = GetMp3Shapes,
        content = content
    )
}
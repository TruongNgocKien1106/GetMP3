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
        primaryContainer = Color(0xFFE2E7FF),
        onPrimaryContainer = Color(0xFF18265E),

        secondary = Color(0xFF7355D9),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFEDE7FF),
        onSecondaryContainer = Color(0xFF2E2255),

        tertiary = Color(0xFF087C8D),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFD2F5F8),
        onTertiaryContainer = Color(0xFF073B43),

        background = LightBackground,
        onBackground = LightText,

        surface = LightSurface,
        onSurface = LightText,
        surfaceVariant = LightSurfaceSoft,
        onSurfaceVariant = LightTextMuted,
        outline = LightBorder,

        error = ErrorRed,
        onError = Color.White,
        errorContainer = ErrorBackground,
        onErrorContainer = Color(0xFF621128)
    )

private val DarkColors =
    darkColorScheme(
        primary = Color(0xFFB9C7FF),
        onPrimary = Color(0xFF102B72),
        primaryContainer = Color(0xFF263C78),
        onPrimaryContainer = Color(0xFFE6EAFF),

        secondary = Color(0xFFD3C3FF),
        onSecondary = Color(0xFF352461),
        secondaryContainer = Color(0xFF493873),
        onSecondaryContainer = Color(0xFFF0E9FF),

        tertiary = Color(0xFF78DCE8),
        onTertiary = Color(0xFF073940),
        tertiaryContainer = Color(0xFF174D59),
        onTertiaryContainer = Color(0xFFD8F8FC),

        background = DarkBackground,
        onBackground = DarkText,

        surface = DarkSurface,
        onSurface = DarkText,
        surfaceVariant = DarkSurfaceSoft,
        onSurfaceVariant = DarkTextMuted,
        outline = DarkBorder,

        error = Color(0xFFFFB1C0),
        onError = Color(0xFF680022),
        errorContainer = Color(0xFF8B1C3A),
        onErrorContainer = Color(0xFFFFD9E1)
    )

private val GetMp3Shapes =
    Shapes(
        extraSmall = RoundedCornerShape(10.dp),
        small = RoundedCornerShape(14.dp),
        medium = RoundedCornerShape(20.dp),
        large = RoundedCornerShape(28.dp),
        extraLarge = RoundedCornerShape(36.dp)
    )

@Composable
fun GetMP3Theme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme =
        when (themeMode) {
            AppThemeMode.SYSTEM ->
                isSystemInDarkTheme()

            AppThemeMode.LIGHT ->
                false

            AppThemeMode.DARK ->
                true
        }

    MaterialTheme(
        colorScheme =
            if (darkTheme) {
                DarkColors
            } else {
                LightColors
            },
        typography = Typography,
        shapes = GetMp3Shapes,
        content = content
    )
}

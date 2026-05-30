package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GeoPrimaryDark,
    onPrimary = GeoOnPrimaryDark,
    secondary = GeoReadinessTextDark,
    secondaryContainer = GeoReadinessBgDark,
    onSecondaryContainer = GeoReadinessTextDark,
    background = GeoBackgroundDark,
    onBackground = GeoOnBackgroundDark,
    surface = GeoCardBgDark,
    onSurface = GeoOnBackgroundDark,
    surfaceVariant = GeoNavBarBgDark,
    onSurfaceVariant = GeoCardDescDark,
    outline = GeoCardBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = GeoPrimaryLight,
    onPrimary = GeoOnPrimaryLight,
    secondary = GeoReadinessTextLight,
    secondaryContainer = GeoReadinessBgLight,
    onSecondaryContainer = GeoReadinessTextLight,
    background = GeoBackgroundLight,
    onBackground = GeoOnBackgroundLight,
    surface = GeoCardBgLight,
    onSurface = GeoOnBackgroundLight,
    surfaceVariant = GeoNavBarBgLight,
    onSurfaceVariant = GeoCardDescLight,
    outline = GeoCardBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors to keep Geometric Balance branding sharp!
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

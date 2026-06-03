package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ColorScheme

data class AccessibilitySettings(
    val isHighContrast: Boolean = false,
    val fontSizeMultiplier: Float = 1.0f
)

val LocalAccessibilitySettings = staticCompositionLocalOf { AccessibilitySettings() }

private val DarkColorScheme =
  darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val HighContrastDarkColorScheme = darkColorScheme(
    primary = Color.Yellow,
    onPrimary = Color.Black,
    surface = Color.Black,
    onSurface = Color.White,
    background = Color.Black,
    onBackground = Color.White
)

private val LightColorScheme =
  lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
  )

private val HighContrastLightColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.Yellow,
    surface = Color.White,
    onSurface = Color.Black,
    background = Color.White,
    onBackground = Color.Black
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    accessibilitySettings: AccessibilitySettings = AccessibilitySettings(),
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            accessibilitySettings.isHighContrast -> if (darkTheme) HighContrastDarkColorScheme else HighContrastLightColorScheme
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
    
    val scaledTypography = Typography.copy(
        bodyLarge = Typography.bodyLarge.copy(fontSize = Typography.bodyLarge.fontSize * accessibilitySettings.fontSizeMultiplier),
        titleLarge = Typography.titleLarge.copy(fontSize = Typography.titleLarge.fontSize * accessibilitySettings.fontSizeMultiplier),
        labelSmall = Typography.labelSmall.copy(fontSize = Typography.labelSmall.fontSize * accessibilitySettings.fontSizeMultiplier)
    )

    CompositionLocalProvider(LocalAccessibilitySettings provides accessibilitySettings) {
        MaterialTheme(colorScheme = colorScheme, typography = scaledTypography, content = content)
    }
}

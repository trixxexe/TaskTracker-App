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

private val DarkColorScheme =
  darkColorScheme(
    primary = DeepIndigo80,
    secondary = LightViolet80,
    tertiary = VividTeal80,
    background = SlateBackground,
    surface = SlateSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = Color(0xFFE2E8F0)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = RoyalIndigo40,
    secondary = DarkViolet40,
    tertiary = DeepTeal40,
    background = CleanLightBackground,
    surface = CleanLightSurface,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = CleanLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF64748B)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false, // Set to false by default to ensure our stunning custom brand palette is strictly displayed!
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

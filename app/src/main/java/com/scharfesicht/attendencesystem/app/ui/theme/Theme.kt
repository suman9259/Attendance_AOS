package com.scharfesicht.attendencesystem.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * AttendanceSystem Light Color Scheme
 * Based on strawberry mascot brand colors
 */
private val LightColorScheme = lightColorScheme(
    // Primary colors (Yellow - Brand background)
    primary = BrandYellow,                         // Brand background / highlight
    onPrimary = BrandMaroon,                       // Text on yellow (headings, contrast)
    primaryContainer = BrandYellowLight,           // Lighter background area
    onPrimaryContainer = BrandMaroon,              // Readable text on yellow areas

    // Secondary colors (Red - Buttons / CTAs)
    secondary = BrandRed,                          // Button background
    onSecondary = White,                           // Button text
    secondaryContainer = BrandRedLight,            // Button pressed / hover
    onSecondaryContainer = White,                  // Text remains white

    // Tertiary colors (Purple - Checkbox / Accent)
    tertiary = BrandPurple,                        // Accent or checkbox color
    onTertiary = White,                            // Text/icon on purple
    tertiaryContainer = BrandPurpleLight,          // Hover / pressed
    onTertiaryContainer = BrandPurpleDark,         // Contrast for dark accent areas

    // Background and Surface
    background = White,                            // Main background
    onBackground = BrandMaroon,                    // Default text
    surface = White,                               // Cards, containers
    onSurface = BrandMaroon,                       // Text/icons on surface
    surfaceVariant = Gray100,                      // Input fields, variant surfaces
    onSurfaceVariant = Gray700,                    // Label / hint color

    // Error colors
    error = BrandRed,                              // Error color
    onError = White,                               // Text on error
    errorContainer = Color(0xFFFFCDD2),            // Error background
    onErrorContainer = BrandMaroon,                // Text on error container

    // Outline and other colors
    outline = Gray400,                             // Borders / input outlines
    outlineVariant = Gray300,                      // Light variant
    scrim = Color.Black.copy(alpha = 0.32f),       // Overlay dim
    surfaceTint = BrandYellow                      // Tint with brand color
)

/**
 * AttendanceSystem Dark Color Scheme
 * Dark theme variant maintaining brand feel
 */
private val DarkColorScheme = darkColorScheme(
    // Primary colors (Yellow stays brand)
//    primary = BrandYellowDark,                     // Vibrant yellow for highlights
    primary = BrandYellow,                     // Vibrant yellow for highlights
    onPrimary = BrandMaroon,                       // Contrast text
//    primaryContainer = BrandYellow,                // Container using main brand tone
    primaryContainer = BrandYellowLight,                // Container using main brand tone
    onPrimaryContainer = BrandMaroon,              // Text contrast

    // Secondary colors (Red - Button / CTA)
    secondary = BrandRedDark,                      // Button in dark mode
    onSecondary = White,                           // Button text
    secondaryContainer = BrandRed,                 // Hover / pressed
    onSecondaryContainer = White,                  // Contrast text

    // Tertiary colors (Purple Accent)
    tertiary = BrandPurpleLight,                   // Checkbox / accent
    onTertiary = BrandPurpleDark,                  // Icon/text contrast
    tertiaryContainer = BrandPurple,               // Accent surface
    onTertiaryContainer = White,                   // Text contrast

    // Background and Surface
    background = Gray900,                          // Dark background
    onBackground = Gray100,                        // Light text
    surface = Gray900,                             // Cards, dialogs
    onSurface = Gray100,                           // Text/icons
    surfaceVariant = Gray800,                      // Inputs / containers
    onSurfaceVariant = Gray400,                    // Label / hint color

    // Error colors
    error = Color(0xFFFF8A80),                     // Soft red error
    onError = Black,                               // Contrast
    errorContainer = Color(0xFF93000A),            // Deep red background
    onErrorContainer = Color(0xFFFFCDD2),          // Text on error

    // Outline and other colors
    outline = Gray600,                             // Border color
    outlineVariant = Gray700,                      // Slightly darker variant
    scrim = Color.Black.copy(alpha = 0.6f),        // Overlay
    surfaceTint = BrandYellowDark                  // Tint
)

/**
 * Main AttendanceSystem Theme Composable
 * Supports light/dark/system themes and dynamic colors (Android 12+)
 */
@Composable
fun AttendanceSystemTheme(
    themeMode: String = "system",
    dynamicColor: Boolean = false, // Disabled by default to maintain brand colors
    content: @Composable () -> Unit
) {
    val systemInDarkTheme = isSystemInDarkTheme()

    // Determine if dark theme should be used
    val darkTheme = when (ThemeMode.fromString(themeMode)) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemInDarkTheme
    }

    val colorScheme = when {
        // TODO : Know Dynamic Color Scheme.
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Setup system UI (status bar & navigation bar)
    val view = LocalView.current
    val systemUiController = rememberSystemUiController()
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                // Make status bar and navigation bar transparent for edge-to-edge
                WindowCompat.setDecorFitsSystemWindows(it, false)

                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()

                // Set status bar appearance
                val insetsController = WindowCompat.getInsetsController(it, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme

            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

// For backwards compatibility
@Composable
fun AttendanceSystemTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val themeMode = if (darkTheme) "dark" else "light"
    AttendanceSystemTheme(
        themeMode = themeMode,
        dynamicColor = dynamicColor,
        content = content
    )
}
//@Composable
//fun AttendanceSystemTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    // Dynamic color is available on Android 12+
//    dynamicColor: Boolean = true,
//    content: @Composable () -> Unit
//) {
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }
//
//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = Typography,
//        content = content
//    )
//}
package com.scharfesicht.attendencesystem.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * AttendanceSystem Brand Colors (Updated from Logo + Signup Screen)
 *
 * Primary: Yellow (Brand)
 * Secondary: Red (CTA)
 * Accent: Maroon / Purple (Typography & accents)
 * Supporting: Blue, Green (optional for status/info)
 */

// ============================================
// Brand Colors (From Logo + UI)
// ============================================

// Primary Brand Colors (Yellow Theme)
val BrandYellow = Color(0xFFC3A355)        // Primary brand yellow
val BrandYellowDark = Color(0xFFB6984F)    // Darker yellow for contrast
val BrandYellowLight = Color(0xE2C3A355)   // Soft light yellow

// Secondary Brand Colors (Red CTA)
val BrandRed = Color(0xFFE53935)           // Bright red (buttons)
val BrandRedDark = Color(0xFFC62828)       // Darker red
val BrandRedLight = Color(0xFFEF5350)      // Light red

// Accent / Typography Colors
val BrandMaroon = Color(0xFF000000)        // Heading text color
val BrandPurple = Color(0xFF7E57C2)        // Accent (checkbox, link)
val BrandPurpleLight = Color(0xFFB39DDB)
val BrandPurpleDark = Color(0xFF4A148C)

// Supporting Colors (Optional)
val BrandBlue = Color(0xFF29B6F6)          // Info / highlight
val BrandGreen = Color(0xFF43A047)         // Success
val BrandGreenLight = Color(0xFFC8E6C9)
val BrandOrange = Color(0xFFFFA726)        // Optional warm tone

// Neutral Colors
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val Gray100 = Color(0xFFF3F3F3)
val Gray200 = Color(0xFFEAEAEA)
val Gray300 = Color(0xFFDADADA)
val Gray400 = Color(0xFFBDBDBD)
val Gray500 = Color(0xFF9E9E9E)
val Gray600 = Color(0xFF757575)
val Gray700 = Color(0xFF616161)
val Gray800 = Color(0xFF424242)
val Gray900 = Color(0xFF1E1E1E)

// ============================================
// Semantic Colors (Light Theme)
// ============================================

val LightPrimary = BrandYellow
val LightOnPrimary = BrandMaroon
val LightPrimaryContainer = BrandYellowLight
val LightOnPrimaryContainer = BrandMaroon

val LightSecondary = BrandRed
val LightOnSecondary = White
val LightSecondaryContainer = BrandRedLight
val LightOnSecondaryContainer = White

val LightTertiary = BrandPurple
val LightOnTertiary = White
val LightTertiaryContainer = BrandPurpleLight
val LightOnTertiaryContainer = BrandPurpleDark

val LightBackground = White
val LightOnBackground = Gray900
val LightSurface = White
val LightOnSurface = Gray900
val LightSurfaceVariant = Gray100
val LightOnSurfaceVariant = Gray700

val LightError = BrandRed
val LightOnError = White
val LightErrorContainer = Color(0xFFFFCDD2)
val LightOnErrorContainer = BrandMaroon

val LightOutline = Gray400
val LightOutlineVariant = Gray300

// ============================================
// Semantic Colors (Dark Theme)
// ============================================

val DarkPrimary = BrandYellowDark
val DarkOnPrimary = BrandMaroon
val DarkPrimaryContainer = BrandYellow
val DarkOnPrimaryContainer = BrandMaroon

val DarkSecondary = BrandRedDark
val DarkOnSecondary = White
val DarkSecondaryContainer = BrandRed
val DarkOnSecondaryContainer = White

val DarkTertiary = BrandPurpleLight
val DarkOnTertiary = BrandPurpleDark
val DarkTertiaryContainer = BrandPurple
val DarkOnTertiaryContainer = White

val DarkBackground = Gray900
val DarkOnBackground = Gray100
val DarkSurface = Gray900
val DarkOnSurface = Gray100
val DarkSurfaceVariant = Gray800
val DarkOnSurfaceVariant = Gray400

val DarkError = Color(0xFFFF8A80)
val DarkOnError = Black
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFCDD2)

val DarkOutline = Gray600
val DarkOutlineVariant = Gray700

// ============================================
// Functional Colors (Both Themes)
// ============================================

val Success = BrandGreen
val SuccessContainer = BrandGreenLight
val Warning = Color(0xFFFFA000)
val WarningContainer = Color(0xFFFFE0B2)
val Info = BrandBlue
val InfoContainer = Color(0xFFBBDEFB)

// ============================================
// Text Field & Form Colors
// ============================================

val TextFieldFocusedColor = Gray100
val TextFieldUnfocusedColor = Gray200
val TextFieldLabelColor = Gray700
val TextFieldHintColor = Gray500

// ============================================
// Legacy (Backward Compatibility)
// ============================================

val PrimaryColor = BrandYellow
val SecondaryColor = BrandRed
val LightFillColor = Color(0xFFF9F9F9)
val LightStrokeColor = Gray300
val LightGreyColor = Gray400
val WhiteColor = White
val GreyColor = Gray900
val GreenColor = BrandGreen
val RedColor = BrandRed
val DarkSurfaceColor = Gray800
val OutlineColorLight = Gray300
val DarkBackgroundColor = Gray900
val DarkText = Gray100
val DarkStroke = Gray700
val OutlineColorDark = Gray600

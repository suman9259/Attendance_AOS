package com.scharfesicht.attendencesystem.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * AttendanceSystem Shape System
 * Consistent rounded corners across the app
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * Custom shapes for specific components
 */
object CustomShapes {
    val buttonShape = RoundedCornerShape(12.dp)
    val cardShape = RoundedCornerShape(16.dp)
    val dialogShape = RoundedCornerShape(24.dp)
    val bottomSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val chipShape = RoundedCornerShape(50) // Fully rounded
    val textFieldShape = RoundedCornerShape(12.dp)
}

/**
 * AttendanceSystem Dimension System
 * Centralized spacing, padding, and size values for consistency
 */
object Dimensions {
    // ============================================
    // Spacing (Between Elements)
    // ============================================
    val spacingNone = 0.dp
    val spacingExtraSmall = 4.dp
    val spacingSmall = 8.dp
    val spacingMedium = 16.dp
    val spacingLarge = 24.dp
    val spacingExtraLarge = 32.dp
    val spacingExtraExtraLarge = 48.dp

    // ============================================
    // Padding (Inside Elements)
    // ============================================
    val paddingNone = 0.dp
    val paddingExtraSmall = 4.dp
    val paddingSmall = 8.dp
    val paddingMedium = 16.dp
    val paddingLarge = 24.dp
    val paddingExtraLarge = 32.dp

    // ============================================
    // Icon Sizes
    // ============================================
    val iconSizeExtraSmall = 16.dp
    val iconSizeSmall = 20.dp
    val iconSizeMedium = 24.dp
    val iconSizeLarge = 32.dp
    val iconSizeExtraLarge = 48.dp
    val iconSizeHuge = 64.dp

    // ============================================
    // Button Heights
    // ============================================
    val buttonHeightSmall = 36.dp
    val buttonHeightMedium = 48.dp
    val buttonHeightLarge = 56.dp

    // ============================================
    // Card Elevation
    // ============================================
    val elevationNone = 0.dp
    val elevationSmall = 2.dp
    val elevationMedium = 4.dp
    val elevationLarge = 8.dp
    val elevationExtraLarge = 16.dp

    // ============================================
    // Border Width
    // ============================================
    val borderWidthThin = 1.dp
    val borderWidthMedium = 2.dp
    val borderWidthThick = 4.dp

    // ============================================
    // Component Sizes
    // ============================================
    val avatarSizeSmall = 32.dp
    val avatarSizeMedium = 48.dp
    val avatarSizeLarge = 64.dp
    val avatarSizeExtraLarge = 96.dp

    val chipHeight = 32.dp
    val textFieldHeight = 56.dp
    val searchBarHeight = 56.dp
    val appBarHeight = 64.dp
    val bottomBarHeight = 80.dp
    val fabSize = 56.dp
    val fabSizeSmall = 40.dp

    // ============================================
    // Corner Radius
    // ============================================
    val cornerRadiusSmall = 8.dp
    val cornerRadiusMedium = 12.dp
    val cornerRadiusLarge = 16.dp
    val cornerRadiusExtraLarge = 24.dp
    val cornerRadiusFull = 50.dp

    // ============================================
    // Divider
    // ============================================
    val dividerThickness = 1.dp

    // ============================================
    // Modal/Dialog
    // ============================================
    val dialogMaxWidth = 560.dp
    val bottomSheetPeekHeight = 128.dp

    // ============================================
    // Content Width Constraints
    // ============================================
    val contentMaxWidth = 840.dp // For large screens
    val contentNormalWidth = 720.dp

    // ============================================
    // Minimum Touch Target (Accessibility)
    // ============================================
    val minTouchTarget = 48.dp

    // ============================================
    // Screen Padding (Safe Area)
    // ============================================
    val screenPaddingHorizontal = 16.dp
    val screenPaddingVertical = 16.dp
}

/**
 * Animation Duration Constants
 */
object AnimationDuration {
    const val fast = 150
    const val normal = 300
    const val slow = 500
    const val extraSlow = 800
}

/**
 * Z-Index Constants (For Overlays)
 */
object ZIndex {
    const val background = 0f
    const val content = 1f
    const val card = 2f
    const val fab = 3f
    const val appBar = 4f
    const val drawer = 5f
    const val modal = 6f
    const val snackbar = 7f
    const val tooltip = 8f
    const val dialog = 9f
    const val loading = 10f
}
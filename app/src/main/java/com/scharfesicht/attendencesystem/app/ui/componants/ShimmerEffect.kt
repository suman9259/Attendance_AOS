package com.scharfesicht.attendencesystem.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Shimmer effect modifier that supports RTL
 */
fun Modifier.shimmerEffect(
    isRtl: Boolean = false
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.5f),
        Color.LightGray.copy(alpha = 0.3f),
    )

    background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = if (isRtl) Offset(1000f + translateAnim.value, 0f) else Offset(translateAnim.value, 0f),
            end = if (isRtl) Offset(translateAnim.value, 0f) else Offset(1000f + translateAnim.value, 0f)
        )
    )
}

/**
 * Dark mode shimmer effect
 */
fun Modifier.shimmerEffectDark(
    isRtl: Boolean = false
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        Color.DarkGray.copy(alpha = 0.3f),
        Color.DarkGray.copy(alpha = 0.5f),
        Color.DarkGray.copy(alpha = 0.3f),
    )

    background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = if (isRtl) Offset(1000f + translateAnim.value, 0f) else Offset(translateAnim.value, 0f),
            end = if (isRtl) Offset(translateAnim.value, 0f) else Offset(1000f + translateAnim.value, 0f)
        )
    )
}

/**
 * Shimmer Box - reusable shimmer component
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    isRtl: Boolean = false,
    isDark: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (isDark) Modifier.shimmerEffectDark(isRtl)
                else Modifier.shimmerEffect(isRtl)
            )
    )
}

/**
 * Shimmer Circle - for avatar placeholders
 */
@Composable
fun ShimmerCircle(
    size: Dp = 48.dp,
    isRtl: Boolean = false,
    isDark: Boolean = false
) {
    ShimmerBox(
        modifier = Modifier.size(size),
        shape = CircleShape,
        isRtl = isRtl,
        isDark = isDark
    )
}

/**
 * Shimmer Text Line
 */
@Composable
fun ShimmerTextLine(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    isRtl: Boolean = false,
    isDark: Boolean = false
) {
    ShimmerBox(
        modifier = modifier.height(height),
        shape = RoundedCornerShape(4.dp),
        isRtl = isRtl,
        isDark = isDark
    )
}
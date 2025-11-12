package com.scharfesicht.attendencesystem.features.attendance.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.scharfesicht.attendencesystem.app.ui.components.*

/**
 * Shimmer effect for Holiday Card
 */
@Composable
fun HolidayCardShimmer(
    isRtl: Boolean = false,
    isDark: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerTextLine(
                    modifier = Modifier.width(150.dp),
                    height = 20.dp,
                    isRtl = isRtl,
                    isDark = isDark
                )
                ShimmerCircle(
                    size = 24.dp,
                    isRtl = isRtl,
                    isDark = isDark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ShimmerBox(
                modifier = Modifier
                    .size(80.dp, 80.dp),
                shape = RoundedCornerShape(8.dp),
                isRtl = isRtl,
                isDark = isDark
            )
        }
    }
}

/**
 * Shimmer effect for Shift Card
 */
@Composable
fun ShiftCardShimmer(
    isRtl: Boolean = false,
    isDark: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            ShimmerTextLine(
                modifier = Modifier.width(180.dp),
                height = 20.dp,
                isRtl = isRtl,
                isDark = isDark
            )

            // Shift name
            ShimmerTextLine(
                modifier = Modifier.width(120.dp),
                height = 18.dp,
                isRtl = isRtl,
                isDark = isDark
            )

            // Time
            ShimmerTextLine(
                modifier = Modifier.width(200.dp),
                height = 24.dp,
                isRtl = isRtl,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    isRtl = isRtl,
                    isDark = isDark
                )
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    isRtl = isRtl,
                    isDark = isDark
                )
            }
        }
    }
}

/**
 * Shimmer effect for Attendance Summary Chart
 */
@Composable
fun AttendanceSummaryShimmer(
    isRtl: Boolean = false,
    isDark: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerCircle(
                        size = 24.dp,
                        isRtl = isRtl,
                        isDark = isDark
                    )
                    ShimmerTextLine(
                        modifier = Modifier.width(160.dp),
                        height = 22.dp,
                        isRtl = isRtl,
                        isDark = isDark
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerBox(
                        modifier = Modifier.size(100.dp, 40.dp),
                        shape = RoundedCornerShape(20.dp),
                        isRtl = isRtl,
                        isDark = isDark
                    )
                    ShimmerBox(
                        modifier = Modifier.size(80.dp, 40.dp),
                        shape = RoundedCornerShape(20.dp),
                        isRtl = isRtl,
                        isDark = isDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Legend
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShimmerCircle(
                            size = 16.dp,
                            isRtl = isRtl,
                            isDark = isDark
                        )
                        ShimmerTextLine(
                            modifier = Modifier.width(140.dp),
                            height = 16.dp,
                            isRtl = isRtl,
                            isDark = isDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                repeat(4) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(40.dp)
                            .height((80 + it * 30).dp),
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                        isRtl = isRtl,
                        isDark = isDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Month label
            ShimmerTextLine(
                modifier = Modifier
                    .width(80.dp)
                    .align(Alignment.CenterHorizontally),
                height = 18.dp,
                isRtl = isRtl,
                isDark = isDark
            )
        }
    }
}

/**
 * Full Screen Shimmer for Attendance Dashboard
 */
@Composable
fun AttendanceDashboardShimmer(
    isRtl: Boolean = false,
    isDark: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Holiday Card Shimmer
//        HolidayCardShimmer(isRtl = isRtl, isDark = isDark)

        // Shift Card Shimmer
        ShiftCardShimmer(isRtl = isRtl, isDark = isDark)

        // Summary Chart Shimmer
//        AttendanceSummaryShimmer(isRtl = isRtl, isDark = isDark)
    }
}

/**
 * Compact Shimmer - for smaller loading states
 */
@Composable
fun CompactShimmerCard(
    isRtl: Boolean = false,
    isDark: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShimmerTextLine(
                modifier = Modifier.fillMaxWidth(0.7f),
                height = 20.dp,
                isRtl = isRtl,
                isDark = isDark
            )
            ShimmerTextLine(
                modifier = Modifier.fillMaxWidth(0.5f),
                height = 16.dp,
                isRtl = isRtl,
                isDark = isDark
            )
        }
    }
}
package com.scharfesicht.attendencesystem.features.attendance.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scharfesicht.attendencesystem.features.attendance.domain.model.AttendanceSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceSummaryChart(
    summaries: List<AttendanceSummary>,
    selectedPeriod: String,
    selectedView: String,
    onPeriodChange: (String) -> Unit,
    onViewChange: (String) -> Unit
) {
    var periodExpanded by remember { mutableStateOf(false) }
    var viewExpanded by remember { mutableStateOf(false) }

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
            // Header with filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.BarChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Attendance Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Period Dropdown
                    ExposedDropdownMenuBox(
                        expanded = periodExpanded,
                        onExpandedChange = { periodExpanded = it }
                    ) {
                        OutlinedButton(
                            onClick = { periodExpanded = true },
                            modifier = Modifier.menuAnchor()
                        ) {
                            Text(selectedPeriod)
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }
                        ExposedDropdownMenu(
                            expanded = periodExpanded,
                            onDismissRequest = { periodExpanded = false }
                        ) {
                            listOf("this month", "last month", "this year").forEach { period ->
                                DropdownMenuItem(
                                    text = { Text(period) },
                                    onClick = {
                                        onPeriodChange(period)
                                        periodExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // View Dropdown
                    ExposedDropdownMenuBox(
                        expanded = viewExpanded,
                        onExpandedChange = { viewExpanded = it }
                    ) {
                        OutlinedButton(
                            onClick = { viewExpanded = true },
                            modifier = Modifier.menuAnchor()
                        ) {
                            Text(selectedView)
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }
                        ExposedDropdownMenu(
                            expanded = viewExpanded,
                            onDismissRequest = { viewExpanded = false }
                        ) {
                            listOf("Days", "Hours").forEach { view ->
                                DropdownMenuItem(
                                    text = { Text(view) },
                                    onClick = {
                                        onViewChange(view)
                                        viewExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Legend
            AttendanceLegend()

            Spacer(modifier = Modifier.height(16.dp))

            // Bar Chart
            if (summaries.isNotEmpty()) {
                BarChart(summary = summaries.first())
            } else {
                Text(
                    "No data available",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun AttendanceLegend() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LegendItem("Attendance", Color(0xFF4CAF50))
        LegendItem("late more than 1h", Color(0xFFE53935))
        LegendItem("early punch out", Color(0xFF2196F3))
        LegendItem("Absence", Color(0xFF9E9E9E))
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .then(
                    Modifier.drawBehind {
                        drawCircle(color = color)
                    }
                )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun BarChart(summary: AttendanceSummary) {
    val maxHeight = 200.dp
    val barWidth = 40.dp
    val spacing = 20.dp

    val maxValue = maxOf(
        summary.attendanceHours,
        summary.lateMoreThan1hHours,
        summary.earlyPunchOutHours,
        summary.absenceHours
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(maxHeight + 40.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // Attendance Bar
        BarChartColumn(
            value = summary.attendanceHours,
            maxValue = maxValue,
            color = Color(0xFF4CAF50),
            maxHeight = maxHeight
        )

        // Late more than 1h Bar
        BarChartColumn(
            value = summary.lateMoreThan1hHours,
            maxValue = maxValue,
            color = Color(0xFFE53935),
            maxHeight = maxHeight
        )

        // Early punch out Bar
        BarChartColumn(
            value = summary.earlyPunchOutHours,
            maxValue = maxValue,
            color = Color(0xFF2196F3),
            maxHeight = maxHeight
        )

        // Absence Bar
        BarChartColumn(
            value = summary.absenceHours,
            maxValue = maxValue,
            color = Color(0xFF9E9E9E),
            maxHeight = maxHeight
        )
    }

    // Month label
    Text(
        text = summary.month,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium
    )
}

@Composable
fun BarChartColumn(
    value: Float,
    maxValue: Float,
    color: Color,
    maxHeight: androidx.compose.ui.unit.Dp
) {
    val heightFraction = if (maxValue > 0) value / maxValue else 0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.height(maxHeight + 40.dp)
    ) {
        // Value label
        Text(
            text = "${value.toInt()}h",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Bar
        Canvas(
            modifier = Modifier
                .width(40.dp)
                .height(maxHeight * heightFraction)
        ) {
            drawRect(
                color = color,
                topLeft = Offset(0f, 0f),
                size = Size(size.width, size.height)
            )
        }
    }
}

@Composable
fun Modifier.drawBehind(onDraw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit): Modifier {
    return this.then(
        Modifier.drawWithContent {
            onDraw()
            drawContent()
        }
    )
}
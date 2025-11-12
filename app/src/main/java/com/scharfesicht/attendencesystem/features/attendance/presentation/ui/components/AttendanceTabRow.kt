package com.scharfesicht.attendencesystem.features.attendance.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scharfesicht.attendencesystem.features.attendance.domain.model.AttendanceStrings
import com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel.AttendanceTab

@Composable
fun AttendanceTabRow(
    selectedTab: AttendanceTab,
    onTabSelected: (AttendanceTab) -> Unit,
    isArabic: Boolean
) {
    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Tab(
            selected = selectedTab == AttendanceTab.MARK_ATTENDANCE,
            onClick = { onTabSelected(AttendanceTab.MARK_ATTENDANCE) },
            text = {
                Text(
                    AttendanceStrings.markAttendance.get(isArabic),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (selectedTab == AttendanceTab.MARK_ATTENDANCE)
                        FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == AttendanceTab.MARK_ATTENDANCE)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        )
        Tab(
            selected = selectedTab == AttendanceTab.PERMISSION_APPLICATION,
            onClick = { onTabSelected(AttendanceTab.PERMISSION_APPLICATION) },
            text = {
                Text(
                    AttendanceStrings.permissionApplication.get(isArabic),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (selectedTab == AttendanceTab.PERMISSION_APPLICATION)
                        FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == AttendanceTab.PERMISSION_APPLICATION)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        )
    }
}
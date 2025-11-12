package com.scharfesicht.attendencesystem.features.attendance.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scharfesicht.attendencesystem.features.attendance.domain.model.*
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ShiftCard(
    shift: ShiftData,
    onPunchIn: () -> Unit,
    onPunchOut: () -> Unit,
    loading: Boolean,
    isArabic: Boolean
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
            Text(
                text = AttendanceStrings.yourAssignedShift.get(isArabic),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (isArabic) shift.shift_name_lang!! else shift.shift_name_lang!!,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                val timeFormatter = DateTimeFormatter.ofPattern(
                    "hh:mm a",
                    if (isArabic) Locale("ar") else Locale.ENGLISH
                )

                Text(
                    text = "${shift.shift_rule?.get(0)!!.start_time?.format(timeFormatter)} - ${shift.shift_rule.get(0).end_time?.format(timeFormatter)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Punch In/Out Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Punch In Button
                Button(
                    onClick = onPunchIn,
                    modifier = Modifier.weight(1f),
                    enabled = true/*!loading && todayAttendance?.punchInTime == null*/,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        disabledContainerColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (/*loading && todayAttendance?.punchInTime == null*/false) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Login,
                                contentDescription = "Punch In"
                            )
                            Text(
                                AttendanceStrings.punchIn.get(isArabic),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Punch Out Button
                Button(
                    onClick = onPunchOut,
                    modifier = Modifier.weight(1f),
                    enabled = true/*!loading && todayAttendance?.punchInTime != null && todayAttendance.punchOutTime == null*/,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (/*loading && todayAttendance?.punchInTime != null*/false) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = "Punch Out"
                            )
                            Text(
                                AttendanceStrings.punchOut.get(isArabic),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
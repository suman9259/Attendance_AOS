package com.scharfesicht.attendencesystem.features.attendance.presentation.ui

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewModelScope
import com.scharfesicht.attendencesystem.app.ui.componants.MainAppTopAppBar
import com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel.AttendanceTab
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

// ---------- Data models ----------
data class AttendanceRow(
    val id: String,
    val date: LocalDate,
    val dayNameAr: String,
    val dayNameEn: String,
    val punchIn: String?, // "09:15am" or null
    val punchOut: String?,
    val workingHours: String? // "08h30m" or null
)

// Simple UI DTO for list item
data class AttendanceListUiState(
    val loading: Boolean = false,
    val rows: List<AttendanceRow> = emptyList(),
    val month: String = "April",
    val filter: String = "Attendance"
)

// ---------- ViewModel (fake repo for demo) ----------
class AttendanceListViewModel : ViewModel() {

    // public state as StateFlow
    private val _ui = MutableStateFlow(AttendanceListUiState(loading = true))
    val ui: StateFlow<AttendanceListUiState> = _ui.asStateFlow()

    // snackbar / dialog events
    private val _snackbar = MutableSharedFlow<String>()
    val snackbar = _snackbar.asSharedFlow()

    init {
        // load sample data
        viewModelScope.launch {
            delay(700) // simulate network
            _ui.value = AttendanceListUiState(
                loading = false,
                rows = sampleRows(),
                month = "April",
                filter = "Attendance"
            )
        }
    }

    private fun sampleRows(): List<AttendanceRow> {
        val today = LocalDate.now()
        return (0 until 5).map { i ->
            val d = today.minusDays(i.toLong())
            AttendanceRow(
                id = i.toString(),
                date = d,
                dayNameAr = when (d.dayOfWeek.value) {
                    1 -> "الإثنين"
                    2 -> "الثلاثاء"
                    3 -> "الأربعاء"
                    4 -> "الخميس"
                    5 -> "الجمعة"
                    6 -> "السبت"
                    else -> "الأحد"
                },
                dayNameEn = d.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                punchIn = if (i == 0) null else "09:15am",
                punchOut = if (i == 0) null else "05:45pm",
                workingHours = if (i == 0) null else "08h30m"
            )
        }
    }

    /**
     * Simulate punch in/out API with face verification already done
     * `type` = "IN" or "OUT"
     */
    fun punch(type: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true) }
            try {
                // simulate network & processing
                delay(1000)
                // success - you can replace with real API response
                val now = LocalTime.now()
                val formatted = now.format(DateTimeFormatter.ofPattern("hh:mma", Locale.ENGLISH)).lowercase()
                // update top row (today)
                val updated = _ui.value.rows.toMutableList()
                if (updated.isEmpty()) {
                    // create a new row for today
                    val newRow = AttendanceRow(
                        id = UUID.randomUUID().toString(),
                        date = LocalDate.now(),
                        dayNameAr = "الخميس",
                        dayNameEn = "Thursday",
                        punchIn = if (type == "IN") formatted else null,
                        punchOut = if (type == "OUT") formatted else null,
                        workingHours = if (type == "OUT") "08h30m" else null
                    )
                    updated.add(0, newRow)
                } else {
                    val first = updated[0]
                    val changed = when (type) {
                        "IN" -> first.copy(punchIn = formatted)
                        else -> first.copy(punchOut = formatted, workingHours = "08h30m")
                    }
                    updated[0] = changed
                }
                _ui.value = _ui.value.copy(rows = updated, loading = false)
                // send success message
                onResult(true, if (type == "IN") "تم تسجيل الدخول بنجاح" else "تم تسجيل الخروج بنجاح")
                _snackbar.emit(if (type == "IN") "Successfully punched in" else "Successfully punched out")
            } catch (e: Exception) {
                _ui.update { it.copy(loading = false) }
                onResult(false, e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true) }
            delay(600)
            _ui.update { it.copy(loading = false, rows = sampleRows()) }
        }
    }
}

// ---------- Composables ----------
@Composable
fun AttendanceListScreen(
    viewModel: AttendanceListViewModel = remember { AttendanceListViewModel() },
    isArabic: Boolean = false,
    onNavigateBack: () -> Unit = {}
) {
    val selectTab by remember { mutableStateOf(AttendanceTab.MARK_ATTENDANCE) }

    // Provide RTL if needed
    CompositionLocalProvider(
        LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        val ui by viewModel.ui.collectAsState()
        val lifecycle = LocalLifecycleOwner.current
        val scaffoldState = rememberScaffoldState()
        val snackbarCoroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            viewModel.snackbar.collect { msg ->
                snackbarCoroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(msg)
                }
            }
        }

        Scaffold(
            topBar = {
                MainAppTopAppBar(
                    titleEn = "Time Attendance",
                    titleAr = "نظام الحضور والانصراف",
                    isArabic = isArabic,
                    isDark = true,
                    onBackClick = {  },
                    onGroupIconClick = {  },
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // Tab Row
                AttendanceTabRow(
                    selectedTab = selectTab,
                    onTabSelected = {  },
                    isArabic = isArabic
                )
                Spacer(modifier = Modifier.height(16.dp))

                when (selectTab) {
                    AttendanceTab.MARK_ATTENDANCE -> {
//                        MarkAttendanceContent(
//                            shift = state.shift,
//                            onPunchIn = { onPunchIn },
//                            onPunchOut = onPunchOut,
//                            loading = punchInOutLoading,
//                            isArabic = isArabic,
//                        )
                    }

                    AttendanceTab.PERMISSION_APPLICATION -> {
//                        PermissionApplicationContent(isArabic = isArabic)
                    }
                }
//                // Tabs row
//                Row(
//                    Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp, vertical = 12.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    TabTitle(isArabic = isArabic)
//                    // month + filter chip
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        IconButton(onClick = { viewModel.refresh() }) {
//                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
//                        }
//                        FilterChip(label = ui.month, onClick = { /* show month picker */ })
//                        Spacer(Modifier.width(8.dp))
//                        FilterChip(label = ui.filter, onClick = { /* show filter */ })
//                    }
//                }

                // Column headings
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // For Arabic we reverse order visually (RTL will handle text direction)
                        Text(
                            text = if (isArabic) "التاريخ" else "Date",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) "تسجيل الدخول" else "punch in",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) "تسجيل الخروج" else "punch out",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) "ساعات العمل" else "working Hours",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                }

                // Content
                if (ui.loading) {
                    // shimmer placeholder simple
                    LoadingPlaceholder(isArabic = isArabic)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(ui.rows) { row ->
                            AttendanceRowItem(
                                row = row,
                                isArabic = isArabic,
                                onPunchInClick = { /* show face verify -> then call viewModel.punch("IN") */ },
                                onPunchOutClick = { /* show face verify -> then call viewModel.punch("OUT") */ }
                            )
                        }

                        // bottom spacer
                        item {
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }

                // Floating-ish action area with Punch In / Punch Out for demo
                PunchActionArea(
                    onPunchIn = {
                        // Simulate face-verified -> call viewModel.punch
                        viewModel.punch("IN") { success, message ->
                            // show dialog
                            snackbarCoroutineScope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(message)
                            }
                        }
                    },
                    onPunchOut = {
                        viewModel.punch("OUT") { success, message ->
                            snackbarCoroutineScope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(message)
                            }
                        }
                    },
                    isArabic = isArabic
                )
            }
        }
    }
}
@Composable
private fun TabTitle(isArabic: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if (isArabic) "تسجيل الحضور" else "mark attendance",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.width(8.dp))
        Divider(modifier = Modifier
            .width(120.dp)
            .height(4.dp), color = MaterialTheme.colorScheme.primary, thickness = 4.dp)
    }
}

@Composable
private fun FilterChip(label: String, onClick: () -> Unit) {
    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .wrapContentWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label)
            Spacer(Modifier.width(4.dp))
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
        }
    }
}

@Composable
private fun AttendanceRowItem(
    row: AttendanceRow,
    isArabic: Boolean,
    onPunchInClick: () -> Unit,
    onPunchOutClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            /** DATE CHIP (FIXED) **/
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Day Name
                    Text(
                        text = if (isArabic) row.dayNameAr else row.dayNameEn,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 11.sp,                      // FIXED SIZE
                        maxLines = 1,                           // PREVENT OVERFLOW
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Date Number
                    Text(
                        text = row.date.dayOfMonth.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 16.sp,                      // FIXED SIZE
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            /** PUNCH IN **/
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = row.punchIn ?: "----",
                    color = if (row.punchIn != null)
                        MaterialTheme.colorScheme.primary
                    else Color.Gray,
                    fontSize = 14.sp
                )
            }

            /** PUNCH OUT **/
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = row.punchOut ?: "----",
                    color = if (row.punchOut != null)
                        MaterialTheme.colorScheme.error
                    else Color.Gray,
                    fontSize = 14.sp
                )
            }

            /** WORKING HOURS **/
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = row.workingHours ?: "----",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun PunchActionArea(onPunchIn: () -> Unit, onPunchOut: () -> Unit, isArabic: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = if (isArabic) Arrangement.Start else Arrangement.End
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onPunchIn,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(56.dp)
                    .width(160.dp)
            ) {
                Text(text = if (isArabic) "تسجيل الدخول" else "Punch In")
            }
            Button(
                onClick = onPunchOut,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFcaa65a)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(56.dp)
                    .width(160.dp)
            ) {
                Text(text = if (isArabic) "تسجيل الخروج" else "Punch Out")
            }
        }
    }
}

@Composable
private fun LoadingPlaceholder(isArabic: Boolean) {
    Column(modifier = Modifier.padding(16.dp)) {
        repeat(4) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .padding(vertical = 8.dp)
            ) {}
        }
    }
}

// ---------- Previews ----------
@Preview(name = "Attendance Light LTR", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun PreviewAttendanceLight() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        AttendanceListScreen(viewModel = remember { AttendanceListViewModel() }, isArabic = false)
    }
}

@Preview(name = "Attendance Dark RTL", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewAttendanceDarkArabic() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AttendanceListScreen(viewModel = remember { AttendanceListViewModel() }, isArabic = true)
    }
}

// Add this sample row for previews
private val previewRow = AttendanceRow(
    id = "1",
    date = LocalDate.of(2025, 4, 15),
    dayNameAr = "الخميس",
    dayNameEn = "Thursday",
    punchIn = "09:15am",
    punchOut = "05:45pm",
    workingHours = "08h30m"
)

@Preview(showBackground = true)
@Composable
fun PreviewRowItem() {
    AttendanceRowItem(
        row = previewRow,
        isArabic = false,
        onPunchInClick = {},
        onPunchOutClick = {}
    )
}

@Preview(showBackground = true, name = "Arabic RTL")
@Composable
fun PreviewRowItemArabic() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AttendanceRowItem(
            row = previewRow,
            isArabic = true,
            onPunchInClick = {},
            onPunchOutClick = {}
        )
    }
}

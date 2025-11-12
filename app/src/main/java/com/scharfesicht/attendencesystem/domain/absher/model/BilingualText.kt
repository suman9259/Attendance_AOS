package com.scharfesicht.attendencesystem.features.attendance.domain.model

data class BilingualText(
    val en: String,
    val ar: String
) {
    fun get(isArabic: Boolean): String = if (isArabic) ar else en
}

object AttendanceStrings {
    val timeAttendance = BilingualText("Time Attendance", "نظام الحضور والانصراف")
    val markAttendance = BilingualText("mark attendance", "تسجيل الحضور")
    val permissionApplication = BilingualText("permission application", "طلب استئذان")
    val theCommingHoliday = BilingualText("The comming Holiday", "العطلة الرسمية القادمة")
    val yourAssignedShift = BilingualText("your Assigned Shift", "الدوام الخاص بك")
    val standardShift = BilingualText("standard Shift", "دوام ثابت")
    val punchIn = BilingualText("punch in", "تسجيل الدخول")
    val punchOut = BilingualText("punch out", "تسجيل الخروج")
    val attendanceSummary = BilingualText("Attendance Summary", "ملخص الحضور")
    val thisMonth = BilingualText("this month", "الشهر الحالي")
    val days = BilingualText("Days", "الساعات")
    val attendance = BilingualText("Attendance", "الحضور")
    val lateMoreThan1h = BilingualText("late more than 1h", "تاخير اكثر من ساعة")
    val lateLessThan1h = BilingualText("late less than 1h", "تاخير اقل من ساعة")
    val earlyPunchOut = BilingualText("early punch out", "الانصراف المبكر")
    val absence = BilingualText("Absence", "الغياب")
    val april = BilingualText("April", "ابريل")
    val punchInSuccess = BilingualText("Punched In Successfully", "تم تسجيل الدخول بنجاح")
    val punchOutSuccess = BilingualText("Punched Out Successfully", "تم تسجيل الخروج بنجاح")
}
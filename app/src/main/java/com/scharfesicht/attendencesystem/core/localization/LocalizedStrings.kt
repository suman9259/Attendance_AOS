package com.scharfesicht.attendencesystem.core.localization

import com.scharfesicht.attendencesystem.core.utils.AppLanguage

data class LocalizedStrings(
    val language: AppLanguage
) {
    // Common
    val appName = when (language) {
        AppLanguage.ENGLISH -> "Attendance System"
        AppLanguage.ARABIC -> "نظام الحضور"
    }

    val timeAttendance = when (language) {
        AppLanguage.ENGLISH -> "Time Attendance"
        AppLanguage.ARABIC -> "نظام الحضور والانصراف"
    }

    val settings = when (language) {
        AppLanguage.ENGLISH -> "Settings"
        AppLanguage.ARABIC -> "الإعدادات"
    }

    val back = when (language) {
        AppLanguage.ENGLISH -> "Back"
        AppLanguage.ARABIC -> "رجوع"
    }

    // Tabs
    val markAttendance = when (language) {
        AppLanguage.ENGLISH -> "mark attendance"
        AppLanguage.ARABIC -> "تسجيل الحضور"
    }

    val permissionApplication = when (language) {
        AppLanguage.ENGLISH -> "permission application"
        AppLanguage.ARABIC -> "طلب استئذان"
    }

    // Holiday
    val theCommingHoliday = when (language) {
        AppLanguage.ENGLISH -> "The comming Holiday"
        AppLanguage.ARABIC -> "العطلة الرسمية القادمة"
    }

    // Shift
    val yourAssignedShift = when (language) {
        AppLanguage.ENGLISH -> "your Assigned Shift"
        AppLanguage.ARABIC -> "الدوام الخاص بك"
    }

    val standardShift = when (language) {
        AppLanguage.ENGLISH -> "Standard Shift"
        AppLanguage.ARABIC -> "دوام ثابت"
    }

    val punchIn = when (language) {
        AppLanguage.ENGLISH -> "punch in"
        AppLanguage.ARABIC -> "تسجيل الدخول"
    }

    val punchOut = when (language) {
        AppLanguage.ENGLISH -> "punch out"
        AppLanguage.ARABIC -> "تسجيل الخروج"
    }

    // Attendance Summary
    val attendanceSummary = when (language) {
        AppLanguage.ENGLISH -> "Attendance Summary"
        AppLanguage.ARABIC -> "ملخص الحضور"
    }

    val thisMonth = when (language) {
        AppLanguage.ENGLISH -> "this month"
        AppLanguage.ARABIC -> "الشهر الحالي"
    }

    val lastMonth = when (language) {
        AppLanguage.ENGLISH -> "last month"
        AppLanguage.ARABIC -> "الشهر السابق"
    }

    val thisYear = when (language) {
        AppLanguage.ENGLISH -> "this year"
        AppLanguage.ARABIC -> "هذا العام"
    }

    val days = when (language) {
        AppLanguage.ENGLISH -> "Days"
        AppLanguage.ARABIC -> "الأيام"
    }

    val hours = when (language) {
        AppLanguage.ENGLISH -> "Hours"
        AppLanguage.ARABIC -> "الساعات"
    }

    // Legend
    val attendance = when (language) {
        AppLanguage.ENGLISH -> "Attendance"
        AppLanguage.ARABIC -> "الحضور"
    }

    val lateMoreThan1h = when (language) {
        AppLanguage.ENGLISH -> "late more than 1h"
        AppLanguage.ARABIC -> "تأخير أكثر من ساعة"
    }

    val lateLessThan1h = when (language) {
        AppLanguage.ENGLISH -> "late less than 1h"
        AppLanguage.ARABIC -> "تأخير أقل من ساعة"
    }

    val earlyPunchOut = when (language) {
        AppLanguage.ENGLISH -> "early punch out"
        AppLanguage.ARABIC -> "الانصراف المبكر"
    }

    val absence = when (language) {
        AppLanguage.ENGLISH -> "Absence"
        AppLanguage.ARABIC -> "الغياب"
    }

    // Logs
    val date = when (language) {
        AppLanguage.ENGLISH -> "Date"
        AppLanguage.ARABIC -> "التاريخ"
    }

    val workingHours = when (language) {
        AppLanguage.ENGLISH -> "working Hours"
        AppLanguage.ARABIC -> "ساعات العمل"
    }

    val lateDuration = when (language) {
        AppLanguage.ENGLISH -> "Late duration"
        AppLanguage.ARABIC -> "مدة التأخير"
    }

    val askForEdit = when (language) {
        AppLanguage.ENGLISH -> "Ask for edit"
        AppLanguage.ARABIC -> "طلب تعديل"
    }

    // Edit Dialog
    val reviewer = when (language) {
        AppLanguage.ENGLISH -> "Reviewer"
        AppLanguage.ARABIC -> "المراجع"
    }

    val selectReviewer = when (language) {
        AppLanguage.ENGLISH -> "Select Reviewer"
        AppLanguage.ARABIC -> "اختر المراجع"
    }

    val reason = when (language) {
        AppLanguage.ENGLISH -> "Reason"
        AppLanguage.ARABIC -> "السبب"
    }

    val enterReason = when (language) {
        AppLanguage.ENGLISH -> "Enter your reason..."
        AppLanguage.ARABIC -> "أدخل السبب..."
    }

    val send = when (language) {
        AppLanguage.ENGLISH -> "Send"
        AppLanguage.ARABIC -> "إرسال"
    }

    val cancel = when (language) {
        AppLanguage.ENGLISH -> "cancel"
        AppLanguage.ARABIC -> "إلغاء"
    }

    // Months
    val april = when (language) {
        AppLanguage.ENGLISH -> "April"
        AppLanguage.ARABIC -> "إبريل"
    }

    // Days of week (Arabic)
    val thursday = when (language) {
        AppLanguage.ENGLISH -> "Thursday"
        AppLanguage.ARABIC -> "الخميس"
    }

    val friday = when (language) {
        AppLanguage.ENGLISH -> "Friday"
        AppLanguage.ARABIC -> "الجمعة"
    }

    val saturday = when (language) {
        AppLanguage.ENGLISH -> "Saturday"
        AppLanguage.ARABIC -> "السبت"
    }

    val sunday = when (language) {
        AppLanguage.ENGLISH -> "Sunday"
        AppLanguage.ARABIC -> "الأحد"
    }

    val monday = when (language) {
        AppLanguage.ENGLISH -> "Monday"
        AppLanguage.ARABIC -> "الاثنين"
    }

    val tuesday = when (language) {
        AppLanguage.ENGLISH -> "Tuesday"
        AppLanguage.ARABIC -> "الثلاثاء"
    }

    val wednesday = when (language) {
        AppLanguage.ENGLISH -> "Wednesday"
        AppLanguage.ARABIC -> "الأربعاء"
    }
}

// Composable to provide localized strings
@androidx.compose.runtime.Composable
fun rememberLocalizedStrings(language: AppLanguage): LocalizedStrings {
    return androidx.compose.runtime.remember(language) {
        LocalizedStrings(language)
    }
}
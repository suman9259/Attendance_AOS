package com.scharfesicht.attendencesystem.core.utils

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun String.toAmPm(): String {
    return try {
        val time = LocalTime.parse(this, DateTimeFormatter.ofPattern("HH:mm"))
        val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        time.format(formatter)
    } catch (e: Exception) {
        this   // return same string if parsing fails
    }
}

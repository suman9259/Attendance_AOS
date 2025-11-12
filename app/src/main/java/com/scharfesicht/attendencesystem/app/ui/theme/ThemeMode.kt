package com.scharfesicht.attendencesystem.app.ui.theme

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM;

    companion object {
        fun fromString(value: String): ThemeMode {
            return when (value.lowercase()) {
                "light" -> LIGHT
                "dark" -> DARK
                else -> SYSTEM
            }
        }
    }

    fun toDisplayString(): String {
        return when (this) {
            LIGHT -> "Light"
            DARK -> "Dark"
            SYSTEM -> "System Default"
        }
    }
}
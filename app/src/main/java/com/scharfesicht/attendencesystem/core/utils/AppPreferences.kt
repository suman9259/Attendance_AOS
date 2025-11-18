package com.scharfesicht.attendencesystem.core.utils
enum class AppLanguage(val code: String) {
    ENGLISH("en"),
    ARABIC("ar");

    companion object {
        fun from(code: String?): AppLanguage {
            return when (code?.lowercase()) {
                "ar", "arabic" -> ARABIC
                else -> ENGLISH
            }
        }
    }
}
data class LocalizedText(
    val ar: String,
    val en: String
)
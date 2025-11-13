package com.scharfesicht.attendencesystem.core.utils


fun normalizeUrl(url: String): String {
    return url
        .trim()
        .replace("\"", "")
        .replace("\\", "")
        .replace(" ", "")
}

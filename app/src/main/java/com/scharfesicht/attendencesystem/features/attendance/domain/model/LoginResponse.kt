package com.scharfesicht.attendencesystem.features.attendance.domain.model

data class LoginResponse(
    val message: String?,
    val data: LoginData?
)

data class LoginData(
    val profile_image: String?,
    val lang: String?,
    val uuid: String?,
    val custom_id: String?,
    val civil_id: String?,
    val username: String?,
    val email: String?,
    val full_name: String?,
    val token: String?,
    val tokens_expire_in: String?,
    val shifts: List<ShiftData>?
)

data class ShiftData(
    val id: Int?,
    val shift_type: Int?,
    val shift_name_lang: String?,
    val start_date: String?,
    val expire_date: String?,
    val shift_rule: List<ShiftRule>?
)

data class ShiftRule(
    val start_time: String?,
    val end_time: String?,
    val grace_period_in: String?,
    val grace_period_out: String?
)

package com.scharfesicht.attendencesystem.features.attendance.domain.model

data class LoginRequest(
    val username: String,
    val password: String,
    val device_token: String = ""
)

data class AttendanceRequest(
    val checkin_time: String? = null,
    val checkin_coordinates: String,
    val checkin_zone_id: Int? = null,
    val checkin_media: String? = null,
    val shift_index: Int = 0,
    val checkin_auth_device_type: Int? = null,
    val checkin_auth_device_value: String? = null,

    // For checkout
    val checkout_time: String? = null,
    val checkout_coordinates: String? = null,
    val checkout_zone_id: Int? = null,
    val checkout_media: String? = null,
    val checkout_auth_device_type: Int? = null,
    val checkout_auth_device_value: String? = null
)

// ========== RESPONSE MODELS ==========

data class ApiResponse<T>(
    val message: String,
    val data: T?
)

// Login Response Models
data class LoginData(
    val profile_image: String?,
    val lang: String,
    val user_calendar_type: Int,
    val uuid: String,
    val custom_id: String,
    val civil_id: String,
    val username: String,
    val email: String,
    val face_recognition: Int,
    val is_notification: Int,
    val is_google2fa: Int,
    val is_terminal_admin: Int,
    val is_zone: Int,
    val zones: List<Zone>,
    val force_password_reset: Int,
    val full_name: String,
    val genderType: GenderType?,
    val nationalityType: NationalityType?,
    val locationType: LocationType?,
    val contractType: ContractType?,
    val jobTitleType: JobTitleType?,
    val managers: List<Any>,
    val department: Department?,
    val shifts: List<Shift>,
    val wifi: List<Wifi>,
    val qr: Any?,
    val becons: List<Beacon>,
    val nfc: Any?,
    val assigned_roles: List<String>,
    val permissions: Any?,
    val active_leave: ActiveLeave,
    val active_short_leave: Any?,
    val is_voice_recognition: Int,
    val voice_recognition_id: String?,
    val token: String,
    val tokens_expire_in: String
)

data class Zone(
    val id: Int,
    val category_id: Int?,
    val zone_radius: String,
    val zone_latitude: String,
    val zone_longitude: String,
    val uuid: String,
    val zone_name_lang: String
)

data class GenderType(
    val id: Int,
    val name_lang: String
)

data class NationalityType(
    val id: Int,
    val name_lang: String
)

data class LocationType(
    val id: Int?,
    val name_lang: String?
)

data class ContractType(
    val id: Int?,
    val name_lang: String?
)

data class JobTitleType(
    val id: Int?,
    val name_lang: String?
)

data class Department(
    val id: Int,
    val uuid: String,
    val department_name_lang: String
)

data class Shift(
    val id: Int,
    val shift_type: Int,
    val min_shift_hours: String,
    val max_shift_hours: String,
    val day_off: Int,
    val rest_day: Int?,
    val shift_rule: List<ShiftRule>,
    val start_date: String,
    val expire_date: String,
    val priority: Int,
    val is_middle_punch: Int,
    val middle_punch_rule: List<MiddlePunchRule>,
    val uuid: String,
    val is_overtime_shift: Int,
    val shift_type_name: String,
    val shift_name_lang: String
)

data class ShiftRule(
    val start_time: String,
    val end_time: String,
    val grace_period_in: String,
    val grace_period_out: String,
    val login_before_start_time: Any,
    val logout_after_end_time: String
)

data class MiddlePunchRule(
    val middle_punch_start_after: String,
    val middle_punch_grace_period: String
)

data class Wifi(
    val id: Int,
    val mac_address: String,
    val uuid: String,
    val wifi_name_lang: String
)

data class Beacon(
    val id: Int,
    val mac_address: String,
    val device_uuid: String?,
    val serial_number: String?,
    val major: Any?,
    val minor: Any?,
    val location: String?,
    val latitude: Any?,
    val longitude: Any?,
    val zone_id: Any?,
    val uuid: String,
    val becon_name_lang: String
)

data class ActiveLeave(
    val is_disable: Int,
    val message: String?,
    val leave: Any?
)

// Attendance Record Models
data class AttendanceRecord(
    val id: Int? = null,
    val shift_rule_index: Int,
    val checkin_time: String?,
    val checkin_time_gap: String? = null,
    val checkin_coordinates: String?,
    val checkin_auth_device_type: Int?,
    val checkin_auth_device_value: String?,
    val checkout_time: String?,
    val checkout_time_gap: String? = null,
    val checkout_coordinates: String?,
    val checkout_auth_device_type: Int?,
    val checkout_auth_device_value: String?,
    val status: Int,
    val type: Int,
    val uuid: String,
    val checkin_media_url: String?,
    val checkout_media_url: String?,
    val shift_time: String,
    val type_text: String,
    val checkin_auth_device: Any?,
    val checkout_auth_device: Any?,
    val shift: ShiftInfo?,
    val middle_punchs: List<Any>,
    val over_time_punch: Any?
)

data class ShiftInfo(
    val id: Int,
    val uuid: String,
    val shift_name_lang: String
)

// ========== UI MODELS ==========

data class UserProfile(
    val uuid: String,
    val username: String,
    val email: String,
    val fullName: String,
    val profileImage: String?,
    val department: String?,
    val shifts: List<Shift>
)

fun LoginData.toUserProfile() = UserProfile(
    uuid = uuid,
    username = username,
    email = email,
    fullName = full_name,
    profileImage = profile_image,
    department = department?.department_name_lang,
    shifts = shifts
)
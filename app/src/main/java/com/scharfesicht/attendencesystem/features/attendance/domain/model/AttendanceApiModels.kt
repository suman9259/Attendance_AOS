package com.scharfesicht.attendencesystem.features.attendance.domain.model

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName
import com.scharfesicht.attendencesystem.core.utils.toAmPm
import kotlinx.serialization.Serializable

// ========== REQUEST MODELS ==========

data class LoginRequest(
    val username: String,
    val password: String,
    val device_token: String = ""
)

data class AttendanceRequest(
    val shift_id : Int? = null,
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

// ========== GENERIC API RESPONSE ==========

data class ApiResponse<T>(
    val message: String,
    val data: T? = null
)

// ========== LOGIN RESPONSE MODELS ==========

data class LoginData(
    val profile_image: String? = null,
    val lang: String = "en",
    val user_calendar_type: Int = 0,
    val uuid: String = "",
    val custom_id: String = "",
    val civil_id: String = "",
    val username: String = "",
    val email: String = "",
    val face_recognition: Int = 0,
    val is_notification: Int = 0,
    val is_google2fa: Int = 0,
    val is_terminal_admin: Int = 0,
    val is_zone: Int = 0,
    val zones: List<Zone> = emptyList(),
    val force_password_reset: Int = 0,
    val full_name: String = "",
    val genderType: GenderType? = null,
    val nationalityType: NationalityType? = null,
    val locationType: LocationType? = null,
    val contractType: ContractType? = null,
    val jobTitleType: JobTitleType? = null,
    val managers: Any? = null, // Ignored - can be array or object
    val department: Department? = null,
    val shifts: List<Shift> = emptyList(),
    val wifi: List<Wifi> = emptyList(),
    val qr: Any? = null, // Ignored - can be string, array, or object
    val becons: List<Beacon> = emptyList(),
    val nfc: Any? = null, // Ignored - can be string, array, or object
    val assigned_roles: List<String> = emptyList(),
    val permissions: Any? = null, // Ignored - can be string or object
    val active_leave: ActiveLeave? = null,
    val active_short_leave: Any? = null, // Ignored - can be anything
    val is_voice_recognition: Int = 0,
    val voice_recognition_id: String? = null,
    val token: String = "",
    val tokens_expire_in: String = ""
)

data class Zone(
    val id: Int = 0,
    val category_id: Int? = null,
    val zone_radius: String = "10",
    val zone_latitude: String = "0.0",
    val zone_longitude: String = "0.0",
    val uuid: String = "",
    val zone_name_lang: String = ""
)

data class GenderType(
    val id: Int = 0,
    val name_lang: String = ""
)

data class NationalityType(
    val id: Int = 0,
    val name_lang: String = ""
)

data class LocationType(
    val id: Int? = null,
    val name_lang: String? = null
)

data class ContractType(
    val id: Int? = null,
    val name_lang: String? = null
)

data class JobTitleType(
    val id: Int? = null,
    val name_lang: String? = null
)

data class Department(
    val id: Int = 0,
    val uuid: String = "",
    val department_name_lang: String = ""
)

// Serializable version for DataStore
@Serializable
data class Shift(
    val id: Int = 0,
    val shift_type: Int = 0,
    val min_shift_hours: String = "0",
    val max_shift_hours: String = "0",
    val day_off: Int = 0,
    val rest_day: Int? = null,
    val shift_rule: List<ShiftRule> = emptyList(),
    val start_date: String = "",
    val expire_date: String = "",
    val priority: Int = 0,
    val is_middle_punch: Int = 0,
    val middle_punch_rule: List<MiddlePunchRule> = emptyList(),
    val uuid: String = "",
    val is_overtime_shift: Int = 0,
    val shift_type_name: String = "",
    val shift_name_lang: String = ""
)

@Serializable
data class ShiftRule(
    val start_time: String = "00:00",
    val end_time: String = "23:59",
    val grace_period_in: String = "0",
    val grace_period_out: String = "0",
    val login_before_start_time: String = "0",
    val logout_after_end_time: String = "0"
) {
    fun getStartTimeAmPm(): String = start_time.toAmPm()
    fun getEndTimeAmPm(): String = end_time.toAmPm()
}


@Serializable
data class MiddlePunchRule(
    val middle_punch_start_after: String = "0",
    val middle_punch_grace_period: String = "0"
)

data class Wifi(
    val id: Int = 0,
    val mac_address: String = "",
    val uuid: String = "",
    val wifi_name_lang: String = ""
)

data class Beacon(
    val id: Int = 0,
    val mac_address: String = "",
    val device_uuid: String? = null,
    val serial_number: String? = null,
    val major: String? = null,
    val minor: String? = null,
    val location: String? = null,
    val latitude: String? = null,
    val longitude: String? = null,
    val zone_id: String? = null,
    val uuid: String = "",
    val becon_name_lang: String = ""
)

data class ActiveLeave(
    val is_disable: Int = 0,
    val message: String? = null,
    val leave: Any? = null // Ignored - can be null or object
)

// ========== ATTENDANCE RECORD MODELS ==========

data class AttendanceRecord(
    val id: Int? = null,
    val shift_rule_index: Int = 0,
    val checkin_time: String? = null,
    val checkin_time_gap: String? = null,
    val checkin_coordinates: String? = null,
    val checkin_auth_device_type: Int? = null,
    val checkin_auth_device_value: String? = null,
    val checkout_time: String? = null,
    val checkout_time_gap: String? = null,
    val checkout_coordinates: String? = null,
    val checkout_auth_device_type: Int? = null,
    val checkout_auth_device_value: String? = null,
    val status: Int = 0,
    val type: Int = 0,
    val uuid: String = "",
    val checkin_media_url: String? = null,
    val checkout_media_url: String? = null,
    val shift_time: String = "",
    val type_text: String = "",
    val checkin_auth_device: Any? = null, // Ignored - can be null or object
    val checkout_auth_device: Any? = null, // Ignored - can be null or object
    val shift: ShiftInfo? = null,
    val middle_punchs: Any? = null, // Ignored - can be empty array or array of objects
    val over_time_punch: Any? = null // Ignored - can be null or object
)

data class ShiftInfo(
    val id: Int = 0,
    val uuid: String = "",
    val shift_name_lang: String = ""
)

// ========== UI MODELS (NOT SERIALIZABLE - ONLY FOR UI) ==========

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
sealed class PunchFlowState {
    object Idle : PunchFlowState()
    object WaitingForLocation : PunchFlowState()
    object WaitingForCamera : PunchFlowState()
    object ValidatingFace : PunchFlowState()
    object ValidatingBiometric : PunchFlowState()
    object PunchingIn : PunchFlowState()
    object PunchingOut : PunchFlowState()

    data class Error(val message: String) : PunchFlowState()
}
enum class PunchType { IN, OUT }
data class PunchSession(
    val type: PunchType,
    val location: Pair<Double, Double>? = null,
    val bitmap: Bitmap? = null
)

data class AttendanceData(
    val upcomingHoliday: String,
    val shiftName: String,
    val shiftTime: String,
    val summary: AttendanceSummary
)

data class AttendanceSummary(
    val attendance: Int,
    val lateLessThan1h: Int,
    val lateMoreThan1h: Int,
    val earlyPunchOut: Int,
    val absence: Int
)
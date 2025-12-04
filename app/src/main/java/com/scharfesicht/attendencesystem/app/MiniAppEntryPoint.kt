package com.scharfesicht.attendencesystem.app

import android.content.Context
import android.content.Intent
import android.util.Log
import com.scharfesicht.attendencesystem.app.mock.MockAbsherHelper
import com.scharfesicht.attendencesystem.core.utils.LocalizedText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import sa.gov.moi.absherinterior.core_logic.AbsherResponse
import sa.gov.moi.absherinterior.core_logic.IAbsherHelper
import sa.gov.moi.absherinterior.core_logic.IMiniApp

class MiniAppEntryPoint : IMiniApp {

    companion object {
        private const val TAG = "MiniAppEntryPoint"

        @Volatile
        var superData: IAbsherHelper? = MockAbsherHelper()

        private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        // ----------------------------------------------------------
        // Helpers
        // ----------------------------------------------------------

        private fun <T> emptyFail(): AbsherResponse<T> =
            AbsherResponse(false, data = null, message = "No data")

        /**
         * Helper to choose Arabic or English values
         */
        private fun getLocalizedValue(
            ar: () -> AbsherResponse<String>,
            en: () -> AbsherResponse<String>
        ): String? = try {
            val lang = superData?.getCurrentLanguage()?.data ?: "ar"
            val result = if (lang == "ar") ar() else en()
            result.data?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting localized value", e)
            null
        }

        // ----------------------------------------------------------
        // Arabic / English Localized Functions
        // ----------------------------------------------------------
        // TODO: We neen header text in both language.

        fun getServiceTitle(): String? = getLocalizedValue(
            ar = { superData?.getServiceTitleAr() ?: emptyFail() },
            en = { superData?.getServiceTitleEn() ?: emptyFail() }
        )

        fun getUserFullName(): String? = getLocalizedValue(
            ar = { superData?.getUserFullNameAr() ?: emptyFail() },
            en = { superData?.getUserFullNameEn() ?: emptyFail() }
        )

        fun getUserFirstName(): String? = getLocalizedValue(
            ar = { superData?.getUserFirstNameAr() ?: emptyFail() },
            en = { superData?.getUserFirstNameEn() ?: emptyFail() }
        )

        fun getUserFatherName(): String? = getLocalizedValue(
            ar = { superData?.getUserFatherNameAr() ?: emptyFail() },
            en = { superData?.getUserFatherNameEn() ?: emptyFail() }
        )

        fun getUserGrandFatherName(): String? = getLocalizedValue(
            ar = { superData?.getUserGrandFatherNameAr() ?: emptyFail() },
            en = { superData?.getUserGrandFatherNameEn() ?: emptyFail() }
        )

        fun getUserLastName(): String? = getLocalizedValue(
            ar = { superData?.getUserLastNameAr() ?: emptyFail() },
            en = { superData?.getUserLastNameEn() ?: emptyFail() }
        )

        fun getPunchCardTitle(): String? =
            getLocalizedValue(
                ar = { AbsherResponse(success = true, data = "الدوام الخاص بك", message = null) },
                en = { AbsherResponse(success = true, data = "Your Assigned Shift", message = null) }
            )

        fun getPunchCardSmallTitle(): String? =
            getLocalizedValue(
                ar = { AbsherResponse(success = true, data = "دوام ثابت", message = null) },
                en = { AbsherResponse(success = true, data = "Standard Shift", message = null) }
            )

        fun getPunchInCardTitle(): String? =
            getLocalizedValue(
                ar = { AbsherResponse(success = true, data = "تسجيل الدخول", message = null) },
                en = { AbsherResponse(success = true, data = "Punch In", message = null) }
            )

        fun getPunchOutCardTitle(): String? =
            getLocalizedValue(
                ar = { AbsherResponse(success = true, data = "نسجيل الخروج", message = null) },
                en = { AbsherResponse(success = true, data = "Punch Out", message = null) }
            )
        fun getMarkAttendanceTitle(): String? =
            getLocalizedValue(
                ar = { AbsherResponse(success = true, data = "تسجيل الحضور", message = null) },
                en = { AbsherResponse(success = true, data = "mark attendance", message = null) }
            )

        fun getPermissionTitle(): String? =
            getLocalizedValue(
                ar = { AbsherResponse(success = true, data = "طلب استئذان", message = null) },
                en = { AbsherResponse(success = true, data = "permission application", message = null) }
            )
        fun getNoAttendanceRecordsText(): String? =
            getLocalizedValue(
                ar = { AbsherResponse(success = true, data = "لا توجد سجلات حضور", message = null) },
                en = { AbsherResponse(success = true, data = "No attendance records found", message = null) }
            )


        fun getDateText(): String? =
            getLocalizedValue(
                ar = { AbsherResponse(success = true, data = "التاريخ", message = null) },
                en = { AbsherResponse(success = true, data = "Date", message = null) }
            )


        fun getWorkingHoursTitle(): String? =
            getLocalizedValue(
                ar = { AbsherResponse(success = true, data = "ساعات العمل", message = null) },
                en = { AbsherResponse(success = true, data = "Working Hours", message = null) }
            )

        fun getFaceNotRecognizedText(): String? =
            getLocalizedValue(
                ar = { AbsherResponse(success = true, data = "الوجه غير مُتعرف عليه", message = null) },
                en = { AbsherResponse(success = true, data = "Face not recognized", message = null) }
            )

        fun getTryAgainText(): String? =
            getLocalizedValue(
                ar = { AbsherResponse(success = true, data = "حاول مرة أخرى", message = null) },
                en = { AbsherResponse(success = true, data = "Try again", message = null) }
            )

        fun getSuccessfullyPunchedText(isIn: Boolean): String? =
            if (isIn) {
                getLocalizedValue(
                    ar = { AbsherResponse(success = true, data = "تم تسجيل الدخول بنجاح", message = null) },
                    en = { AbsherResponse(success = true, data = "Successfully punched in", message = null) }
                )
            } else {
                getLocalizedValue(
                    ar = { AbsherResponse(success = true, data = "تم تسجيل الخروج بنجاح", message = null) },
                    en = { AbsherResponse(success = true, data = "Successfully punched out", message = null) }
                )
            }





        // ----------------------------------------------------------
        // Non-localized (English only or neutral)
        // ----------------------------------------------------------

        fun getUserToken(): String? = safe { superData?.getUserToken() }

        fun getCurrentLanguage(): String =
            superData?.getCurrentLanguage()?.data ?: "ar"

        fun getCurrentTheme(): String =
            superData?.getCurrentTheme()?.data ?: "light"

        fun getUserNationalId(): String? =
            safe { superData?.getUserNationalID() }

        fun getUserEmail(): String? =
            safe { superData?.getUserEmail() }

        fun getUserMobile(): String? =
            safe { superData?.getUserMobile() }

        fun getUserWorkPhone(): String? =
            safe { superData?.getUserWorkPhone() }

        fun getUserBirthDate(): String? =
            safe { superData?.getUserBirthDate() }

        fun getUserBirthDateHijri(): String? =
            safe { superData?.getUserBirthDateHijri() }

        fun getUserPlaceOfBirth(): String? =
            safe { superData?.getUserPlaceOfBirth() }

        fun getUserGender(): String? =
            safe { superData?.getUserGender() }

        fun getUserNationality(): String? =
            safe { superData?.getUserNationality() }

        fun getUserDepartment(): String? =
            safe { superData?.getUserDepartment() }

        fun getUserSector(): String? =
            safe { superData?.getUserSector() }

        fun getUserBloodType(): String? =
            safe { superData?.getUserBloodType() }

        fun getUserMaritalStatus(): String? =
            safe { superData?.getUserMaritalStatus() }

        fun getUserBasicSalary(): String? =
            safe { superData?.getUserBasicSalary() }

        fun getUserEmployeeType(): String? =
            safe { superData?.getEmployeeType() }

        fun getUserHireDate(): String? =
            safe { superData?.getUserHireDate() }

        fun getUserRank(): String? =
            safe { superData?.getUserRank() }

        fun getUserRankID(): String? =
            safe { superData?.getUserRankID() }

        fun getUserRankCode(): String? =
            safe { superData?.getUserRankCode() }

        fun getUserRankDate(): String? =
            safe { superData?.getUserRankDate() }

        fun getUserGovernmentHireDateRank(): String? =
            safe { superData?.getUserGovernmentHireDateRank() }

        fun getUserProfileImage(): String? =
            safe { superData?.getUserProfileImage() }

        // ----------------------------------------------------------
        // Device / Native APIs
        // ----------------------------------------------------------

        fun getCameraImage(): String? =
            //TODO: This method is not working. Need to fix it.
            safe { superData?.getImageFromCamera() }
            // TODO: Camera, Gallery and all the other system sdk is not working.
        fun getGalleryImage(): String? =
            safe { superData?.getImageFromGallery() }

        fun getGalleryImages(): List<String>? =
            safeList { superData?.getImagesFromGallery() }

        fun getCameraVideo(): String? =
            safe { superData?.getVideoFromCamera() }

        fun getGalleryVideo(): String? =
            safe { superData?.getVideoFromGallery() }

        fun getFiles(): String? =
            safe { superData?.getFile() }

        fun authenticateBiometric(): Boolean =
            safeBool { superData?.authenticateBiometric() }

        fun getLocation(): String? =
            safe { superData?.getPreciseLocation() }

        fun isLocationAuthenticated(): Boolean =
            safeBool { superData?.getIsLocationAuthenticated() }

        // ----------------------------------------------------------
        // Local storage wrapper
        // ----------------------------------------------------------

        fun saveToAbsherStorage(key: String, value: String): Boolean =
            safeBool { superData?.saveStringToLocal(key, value) }

        fun readFromAbsherStorage(key: String): String? =
            safe { superData?.readStringFromLocal(key) }

        fun deleteFromAbsherStorage(key: String): Boolean =
            safeBool { superData?.deleteDataFromLocal(key) }

        // ----------------------------------------------------------
        // Shared Safe Extractor Helpers
        // ----------------------------------------------------------

        private fun safe(call: () -> AbsherResponse<String>?): String? =
            try {
                call()?.data?.takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading value", e)
                null
            }

        private fun safeBool(call: () -> AbsherResponse<Boolean>?): Boolean =
            try {
                call()?.data == true
            } catch (e: Exception) {
                Log.e(TAG, "Error reading boolean", e)
                false
            }

        private fun safeList(call: () -> AbsherResponse<List<String>>?): List<String>? =
            try {
                call()?.data
            } catch (e: Exception) {
                Log.e(TAG, "Error reading list", e)
                null
            }
        private fun getBothLocalized(
            ar: () -> String,
            en: () -> String
        ): LocalizedText {
            return try {
                LocalizedText(
                    ar = ar(),
                    en = en()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error getting both localized values", e)
                LocalizedText("", "")
            }
        }
    }

    // ----------------------------------------------------------
    // Launch
    // ----------------------------------------------------------

    override fun launch(context: Context, data: IAbsherHelper) {
        Log.d(TAG, "Mini app launched from Absher")
        logAbsherUserInfo()

        context.startActivity(
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("FROM_ABSHER", true)
            }
        )
    }

    // ----------------------------------------------------------
    // Debug Logging
    // ----------------------------------------------------------

    private fun logAbsherUserInfo() {
        appScope.launch {
            try {
                Log.d(TAG, "=== Absher User Info ===")
                Log.d(TAG, "National ID: ${getUserNationalId()}")
                Log.d(TAG, "Full Name: ${getUserFullName()}")
                Log.d(TAG, "Email: ${getUserEmail()}")
                Log.d(TAG, "Mobile: ${getUserMobile()}")
                Log.d(TAG, "Department: ${getUserDepartment()}")
                Log.d(TAG, "Token Available: ${getUserToken() != null}")
                Log.d(TAG, "=======================")
            } catch (e: Exception) {
                Log.e(TAG, "Error logging user info", e)
            }
        }
    }
}

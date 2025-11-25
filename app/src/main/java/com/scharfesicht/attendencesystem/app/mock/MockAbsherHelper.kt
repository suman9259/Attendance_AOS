package com.scharfesicht.attendencesystem.app.mock

import sa.gov.moi.absherinterior.core_logic.AbsherPosition
import sa.gov.moi.absherinterior.core_logic.AbsherResponse
import sa.gov.moi.absherinterior.core_logic.IAbsherHelper

class MockAbsherHelper : IAbsherHelper {

    private fun <T> ok(value: T) = AbsherResponse(true, data = value)
    private fun <T> fail() = AbsherResponse(false, data = null, message = "null")

    // ==========================================
    // USER DATA (REALISTIC FULL DUMMY DATA)
    // ==========================================

    override fun getUserRankID(): AbsherResponse<String> = ok("R-225")
    override fun getUserRankCode(): AbsherResponse<String> = ok("B3")
    override fun getUserRank(): AbsherResponse<String> = ok("Senior Supervisor")
    override fun getUserRankDate(): AbsherResponse<String> = ok("2022-10-15")
    override fun getUserBasicSalary(): AbsherResponse<String> = ok("19500")
    override fun getUserHireDate(): AbsherResponse<String> = ok("2018-04-12")
    override fun getUserGovernmentHireDateRank(): AbsherResponse<String> = ok("2021-09-05")
    override fun getEmployeeType(): AbsherResponse<String> = ok("Full-Time")

    // NATIONAL ID
    override fun getUserNationalID(): AbsherResponse<String> = ok("1029384756")

    // FULL NAME
    override fun getUserFullNameAr(): AbsherResponse<String> = ok("سعود بن فهد المطيري")
    override fun getUserFullNameEn(): AbsherResponse<String> = ok("Saud bin Fahad Al-Mutairi")

    // FIRST / FATHER / GRAND FATHER / LAST NAME
    override fun getUserFirstNameAr(): AbsherResponse<String> = ok("سعود")
    override fun getUserFatherNameAr(): AbsherResponse<String> = ok("بن فهد")
    override fun getUserGrandFatherNameAr(): AbsherResponse<String> = ok("بن عبدالعزيز")
    override fun getUserLastNameAr(): AbsherResponse<String> = ok("المطيري")

    override fun getUserFirstNameEn(): AbsherResponse<String> = ok("Saud")
    override fun getUserFatherNameEn(): AbsherResponse<String> = ok("Fahad")
    override fun getUserGrandFatherNameEn(): AbsherResponse<String> = ok("Abdulaziz")
    override fun getUserLastNameEn(): AbsherResponse<String> = ok("Al-Mutairi")

    // PERSONAL INFO
    override fun getUserNationality(): AbsherResponse<String> = ok("Saudi")
    override fun getUserGender(): AbsherResponse<String> = ok("Male")
    override fun getUserBloodType(): AbsherResponse<String> = ok("A+")
    override fun getUserMaritalStatus(): AbsherResponse<String> = ok("Single")
    override fun getUserBirthDate(): AbsherResponse<String> = ok("1988-12-09")
    override fun getUserBirthDateHijri(): AbsherResponse<String> = ok("1409-04-30")
    override fun getUserPlaceOfBirth(): AbsherResponse<String> = ok("Jeddah")

    // CONTACT INFO
    override fun getUserMobile(): AbsherResponse<String> = ok("+966540123456")
    override fun getUserWorkPhone(): AbsherResponse<String> = ok("0126547890")
    override fun getUserEmail(): AbsherResponse<String> = ok("saud.almutairi@example.com")

    // WORK DETAILS
    override fun getUserSector(): AbsherResponse<String> = ok("Ministry of Interior (MOI)")
    override fun getUserDepartment(): AbsherResponse<String> = ok("Security Operations Department")

    // ==========================================
    // APP DATA
    // ==========================================

    override fun getUserToken(): AbsherResponse<String> = ok("mocked-jwt-token-1234567890")
    override fun getCurrentTheme(): AbsherResponse<String> = ok("light")  // or "dark"
    override fun getCurrentLanguage(): AbsherResponse<String> = ok("en")  // switch to "ar" for test
    override fun getUserProfileImage(): AbsherResponse<String> =
        ok("https://example.com/mock_user_avatar.jpg")

    // ==========================================
    // LOCAL STORAGE MOCK
    // ==========================================

    override fun saveStringToLocal(key: String, value: String): AbsherResponse<Boolean> = ok(true)
    override fun readStringFromLocal(key: String): AbsherResponse<String> = ok("mock-string-value")

    override fun saveBoolToLocal(key: String, value: Boolean): AbsherResponse<Boolean> = ok(true)
    override fun readBoolFromLocal(key: String): AbsherResponse<String> = ok("false")

    override fun saveIntToLocal(key: String, value: Int): AbsherResponse<Boolean> = ok(true)
    override fun readIntFromLocal(key: String): AbsherResponse<String> = ok("123")

    override fun deleteDataFromLocal(key: String): AbsherResponse<Boolean> = ok(true)

    // ==========================================
    // NATIVE FEATURES MOCK
    // ==========================================

    override fun getImageFromCamera(): AbsherResponse<String> =
        ok("mock_image_from_camera.jpg")

    override fun getImageFromGallery(): AbsherResponse<String> =
        ok("mock_gallery_image_01.jpg")

    override fun getImagesFromGallery(): AbsherResponse<List<String>> =
        ok(listOf("gallery_1.jpg", "gallery_2.jpg", "gallery_3.jpg"))

    override fun getVideoFromCamera(): AbsherResponse<String> =
        ok("mock_video_from_camera.mp4")

    override fun getVideoFromGallery(): AbsherResponse<String> =
        ok("mock_gallery_video.mp4")

    override fun getVideosFromGallery(): AbsherResponse<String> =
        ok("mock_multiple_videos.mp4")

    override fun getFile(): AbsherResponse<String> =
        ok("mock_document_sample.pdf")

    override fun getPreciseLocation(): AbsherResponse<String> =
        ok("24.774265,46.738586")  // Riyadh coords

    override fun authenticateBiometric(): AbsherResponse<Boolean> = ok(true)

    override fun shareText(shareText: String): AbsherResponse<Boolean> = ok(true)
    override fun shareMedia(filePath: String, message: String?): AbsherResponse<Boolean> = ok(true)

    // ==========================================
    // SERVICE DATA
    // ==========================================

    override fun getServiceTitleAr(): AbsherResponse<String> = ok("خدمة نظام الحضور والانصراف")
    override fun getServiceTitleEn(): AbsherResponse<String> = ok("Attendance & Time Tracking System")

    override fun getIsLocationAuthenticated(): AbsherResponse<Boolean> = ok(true)

    override fun getLocation(): AbsherResponse<AbsherPosition> =
        ok(AbsherPosition(24.774265, 46.738586, 15.0)) // Riyadh mock position

    // ==========================================
    // CLOSE APP MOCK
    // ==========================================

    override fun closeApp(data: Map<String, Any>?) {
        println("Mock: App closed with data: $data")
    }
}

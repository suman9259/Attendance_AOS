package com.scharfesicht.attendencesystem.app.mock

import sa.gov.moi.absherinterior.core_logic.AbsherPosition
import sa.gov.moi.absherinterior.core_logic.AbsherResponse
import sa.gov.moi.absherinterior.core_logic.IAbsherHelper

class MockAbsherHelper : IAbsherHelper {

    private fun <T> ok(value: T) = AbsherResponse(true, data = value)
    private fun <T> fail() = AbsherResponse(false, data = null, message = "null")

    override fun getUserRankID(): AbsherResponse<String> = ok("R-100")
    override fun getUserRankCode(): AbsherResponse<String> = ok("A1")
    override fun getUserRank(): AbsherResponse<String> = ok("Manager")
    override fun getUserRankDate(): AbsherResponse<String> = ok("2023-01-01")
    override fun getUserBasicSalary(): AbsherResponse<String> = ok("18000")
    override fun getUserHireDate(): AbsherResponse<String> = ok("2021-05-10")
    override fun getUserGovernmentHireDateRank(): AbsherResponse<String> = ok("2022-03-15")
    override fun getEmployeeType(): AbsherResponse<String> = ok("Full-Time")

    override fun getUserNationalID(): AbsherResponse<String> = ok("1234567890")
    override fun getUserFullNameAr(): AbsherResponse<String> = ok("عبدالله العتيبي")
    override fun getUserFullNameEn(): AbsherResponse<String> = ok("Abdullah Al-Otaibi")
    override fun getUserFirstNameAr(): AbsherResponse<String> = ok("عبدالله")
    override fun getUserFatherNameAr(): AbsherResponse<String> = ok("بن محمد")
    override fun getUserGrandFatherNameAr(): AbsherResponse<String> = ok("بن عبدالله")
    override fun getUserLastNameAr(): AbsherResponse<String> = ok("العتيبي")
    override fun getUserFirstNameEn(): AbsherResponse<String> = ok("Abdullah")
    override fun getUserFatherNameEn(): AbsherResponse<String> = ok("Mohammed")
    override fun getUserGrandFatherNameEn(): AbsherResponse<String> = ok("Abdullah")
    override fun getUserLastNameEn(): AbsherResponse<String> = ok("Al-Otaibi")
    override fun getUserNationality(): AbsherResponse<String> = ok("Saudi")
    override fun getUserGender(): AbsherResponse<String> = ok("Male")
    override fun getUserBloodType(): AbsherResponse<String> = ok("O+")
    override fun getUserMaritalStatus(): AbsherResponse<String> = ok("Married")
    override fun getUserMobile(): AbsherResponse<String> = ok("+966500000000")
    override fun getUserWorkPhone(): AbsherResponse<String> = ok("0112345678")
    override fun getUserEmail(): AbsherResponse<String> = ok("abdullah@mockmail.com")
    override fun getUserBirthDate(): AbsherResponse<String> = ok("1990-02-20")
    override fun getUserBirthDateHijri(): AbsherResponse<String> = ok("1410-08-25")
    override fun getUserPlaceOfBirth(): AbsherResponse<String> = ok("Riyadh")
    override fun getUserSector(): AbsherResponse<String> = ok("MOI")
    override fun getUserDepartment(): AbsherResponse<String> = ok("Attendance Department")

    override fun getUserToken(): AbsherResponse<String> = ok("mock-jwt-token")
    override fun getCurrentTheme(): AbsherResponse<String> = ok("light")
    override fun getCurrentLanguage(): AbsherResponse<String> = ok("en")
    override fun getUserProfileImage(): AbsherResponse<String> = ok("https://example.com/avatar.png")

    override fun saveStringToLocal(key: String, value: String): AbsherResponse<Boolean> = ok(true)
    override fun readStringFromLocal(key: String): AbsherResponse<String> = ok("mockValue")
    override fun saveBoolToLocal(key: String, value: Boolean): AbsherResponse<Boolean> = ok(true)
    override fun readBoolFromLocal(key: String): AbsherResponse<String> = ok("true")
    override fun saveIntToLocal(key: String, value: Int): AbsherResponse<Boolean> = ok(true)
    override fun readIntFromLocal(key: String): AbsherResponse<String> = ok("1")
    override fun deleteDataFromLocal(key: String): AbsherResponse<Boolean> = ok(true)

    override fun getImageFromCamera(): AbsherResponse<String> = ok("mock_camera_image.png")
    override fun getImageFromGallery(): AbsherResponse<String> = ok("mock_gallery_image.png")
    override fun getImagesFromGallery(): AbsherResponse<List<String>> = ok(listOf("img1.png", "img2.png"))
    override fun getVideoFromCamera(): AbsherResponse<String> = ok("mock_video_camera.mp4")
    override fun getVideoFromGallery(): AbsherResponse<String> = ok("mock_video_gallery.mp4")
    override fun getVideosFromGallery(): AbsherResponse<String> = ok("mock_video_gallery.mp4")
    override fun getFile(): AbsherResponse<String> = ok("mock_file.pdf")
    override fun getPreciseLocation(): AbsherResponse<String> = ok("24.7136,46.6753")
    override fun authenticateBiometric(): AbsherResponse<Boolean> = ok(true)
    override fun shareText(shareText: String): AbsherResponse<Boolean> = ok(true)
    override fun shareMedia(filePath: String, message: String?): AbsherResponse<Boolean> = ok(true)

    override fun getServiceTitleAr(): AbsherResponse<String> = ok("نظام الحضور والانصراف")
    override fun getServiceTitleEn(): AbsherResponse<String> = ok("Attendance System")
    override fun getIsLocationAuthenticated(): AbsherResponse<Boolean> = ok(true)
    override fun getLocation(): AbsherResponse<AbsherPosition> = ok(AbsherPosition(24.7136, 46.6753, 10.0))

    override fun closeApp(data: Map<String, Any>?) { /* No-op for mock */ }
}

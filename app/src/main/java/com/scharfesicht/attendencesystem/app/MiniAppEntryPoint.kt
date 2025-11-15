package com.scharfesicht.attendencesystem.app

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import sa.gov.moi.absherinterior.core_logic.IAbsherHelper
import sa.gov.moi.absherinterior.core_logic.IMiniApp

class MiniAppEntryPoint : IMiniApp {

    companion object {
        private const val TAG = "MiniAppEntryPoint"

        @Volatile
        var superData: IAbsherHelper? = null
            private set

        private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        /**
         * Get user token from Absher
         */
        fun getUserToken(): String? {
            return try {
                superData?.getUserToken()?.data?.takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting user token", e)
                null
            }
        }

        /**
         * Get user national ID from Absher
         */
        fun getUserNationalId(): String? {
            return try {
                superData?.getUserNationalID()?.data?.takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting national ID", e)
                null
            }
        }

        /**
         * Get user full name
         */
        fun getUserFullName(language: String = "en"): String? {
            return try {
                if (language == "ar") {
                    superData?.getUserFullNameAr()?.data
                } else {
                    superData?.getUserFullNameEn()?.data
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting full name", e)
                null
            }
        }

        /**
         * Get user email
         */
        fun getUserEmail(): String? {
            return try {
                superData?.getUserEmail()?.data?.takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting email", e)
                null
            }
        }

        /**
         * Get user mobile
         */
        fun getUserMobile(): String? {
            return try {
                superData?.getUserMobile()?.data?.takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting mobile", e)
                null
            }
        }

        /**
         * Get user department
         */
        fun getUserDepartment(): String? {
            return try {
                superData?.getUserDepartment()?.data?.takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting department", e)
                null
            }
        }

        /**
         * Get user profile image
         */
        fun getUserProfileImage(): String? {
            return try {
                superData?.getUserProfileImage()?.data?.takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting profile image", e)
                null
            }
        }

        /**
         * Check if launched from Absher super app
         */
        fun isLaunchedFromAbsher(): Boolean {
            return superData != null
        }

        /**
         * Save data to Absher local storage
         */
        fun saveToAbsherStorage(key: String, value: String): Boolean {
            return try {
                superData?.saveStringToLocal(key, value)?.data == true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving to Absher storage", e)
                false
            }
        }

        /**
         * Read data from Absher local storage
         */
        fun readFromAbsherStorage(key: String): String? {
            return try {
                superData?.readStringFromLocal(key)?.data?.takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading from Absher storage", e)
                null
            }
        }
    }

    override fun launch(context: Context, data: IAbsherHelper) {
        Log.d(TAG, "Mini app launched from Absher")

        superData = data

        // Log user info for debugging
        logAbsherUserInfo()

        // Launch main activity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("FROM_ABSHER", true)
        }
        context.startActivity(intent)
    }

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
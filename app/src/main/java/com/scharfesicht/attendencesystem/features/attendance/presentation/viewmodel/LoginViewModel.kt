package com.scharfesicht.attendencesystem.features.attendance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scharfesicht.attendencesystem.core.network.NetworkResult
import com.scharfesicht.attendencesystem.core.network.TokenManager
import com.scharfesicht.attendencesystem.features.attendance.domain.model.LoginData
import com.scharfesicht.attendencesystem.features.attendance.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChanged(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username,
            usernameError = null
        )
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null
        )
    }

    fun onLoginClick() {
        if (!validateInputs()) return

        viewModelScope.launch {
            loginUseCase(
                username = _uiState.value.username,
                password = _uiState.value.password,
                deviceToken = "" // Get from device
            ).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }
                    is NetworkResult.Success -> {
                        // Save token
                        tokenManager.saveJwtToken(result.data.token)

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            loginSuccess = true,
                            userData = result.data
                        )
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.error.message
                        )
                    }
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val username = _uiState.value.username
        val password = _uiState.value.password

        var isValid = true

        if (username.isBlank()) {
            _uiState.value = _uiState.value.copy(
                usernameError = "Username is required"
            )
            isValid = false
        }

        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                passwordError = "Password is required"
            )
            isValid = false
        }

        return isValid
    }
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val usernameError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val errorMessage: String? = null,
    val userData: LoginData? = null
)
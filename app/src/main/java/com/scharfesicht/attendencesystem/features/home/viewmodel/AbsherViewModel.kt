package com.scharfesicht.attendencesystem.presentation.absher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scharfesicht.attendencesystem.domain.absher.model.UserInfo
import com.scharfesicht.attendencesystem.domain.absher.usecase.GetUserInfoUseCase
import com.scharfesicht.attendencesystem.domain.absher.usecase.GetUserTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AbsherUiState {
    object Idle : AbsherUiState()
    object Loading : AbsherUiState()
    data class Success(val userInfo: UserInfo) : AbsherUiState()
    data class Error(val message: String) : AbsherUiState()
    object NotInitialized : AbsherUiState()
}

@HiltViewModel
class AbsherViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getUserTokenUseCase: GetUserTokenUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AbsherUiState>(AbsherUiState.Idle)
    val uiState: StateFlow<AbsherUiState> = _uiState.asStateFlow()

    fun loadUserInfo() {
        viewModelScope.launch {
            _uiState.value = AbsherUiState.Loading

            getUserInfoUseCase().collect { result ->
                result.fold(
                    onSuccess = { userInfo ->
                        _uiState.value = AbsherUiState.Success(userInfo)
                    },
                    onFailure = { exception ->
                        _uiState.value = AbsherUiState.Error(
                            exception.message ?: "Unknown error occurred"
                        )
                    }
                )
            }
        }
    }

    fun getUserToken() {
        viewModelScope.launch {
            getUserTokenUseCase().fold(
                onSuccess = { token ->
                    android.util.Log.d("AbsherViewModel", "Token: $token")
                },
                onFailure = { exception ->
                    android.util.Log.e("AbsherViewModel", "Failed to get token", exception)
                }
            )
        }
    }
}
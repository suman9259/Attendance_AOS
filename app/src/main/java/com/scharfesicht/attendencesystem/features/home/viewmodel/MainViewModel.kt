package com.scharfesicht.attendencesystem.features.home.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scharfesicht.attendencesystem.domain.absher.model.UserInfo
import com.scharfesicht.attendencesystem.domain.absher.repository.AbsherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    object Loading : UiState()
    data class Success(val userInfo: UserInfo) : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel(
    private val repository: AbsherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadUserInfo()
    }

    fun loadUserInfo() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            repository.getUserInfo().collect { result ->
                result.fold(
                    onSuccess = { userInfo ->
                        _uiState.value = UiState.Success(userInfo)
                    },
                    onFailure = { exception ->
                        val errorMessage = exception.message ?: "Unknown error"
                        _uiState.value = UiState.Error(errorMessage)
                    }
                )
            }
        }
    }

    fun getUserToken() {
        viewModelScope.launch {
            repository.getUserToken().fold(
                onSuccess = { token ->
                    // Handle token
                },
                onFailure = { exception ->
                }
            )
        }
    }
}
package com.example.wifty.ui.screens.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifty.data.SecureTokenManager
import com.example.wifty.data.api.ApiService
import com.example.wifty.data.api.LoginRequest
import com.example.wifty.data.api.SignUpRequest
import com.example.wifty.data.api.ForgotPasswordRequest
import com.example.wifty.data.api.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val token: String? = null,
    val isLoggedIn: Boolean = false,
    val user: UserData? = null
)

class AuthViewModel(private val apiService: ApiService, application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    private val secureTokenManager = SecureTokenManager(application)

    init {
        checkForSavedToken()
    }

    private fun checkForSavedToken() {
        val token = secureTokenManager.getToken()
        val user = secureTokenManager.getUserData()
        if (token != null) {
            _uiState.value = AuthUiState(isLoggedIn = true, token = token, user = user)
        }
    }

    fun login(email: String, password: String) {
        if (!validateEmail(email) || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Invalid email or password")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val response = apiService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.token != null) {
                        secureTokenManager.saveAuthData(body.token, body.user)
                        _uiState.value = AuthUiState(
                            isLoading = false,
                            token = body.token,
                            isLoggedIn = true,
                            user = body.user
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = body?.message ?: "Login failed"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Login failed: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun signUp(username: String, email: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(error = "Passwords don't match")
            return
        }

        if (!validateEmail(email) || password.length < 6) {
            _uiState.value = _uiState.value.copy(error = "Invalid email or password (min 6 chars)")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val response = apiService.signUp(SignUpRequest(username, email, password))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.token != null) {
                        secureTokenManager.saveAuthData(body.token, body.user)
                        _uiState.value = AuthUiState(
                            isLoading = false,
                            token = body.token,
                            isLoggedIn = true,
                            user = body.user?.copy(profile_picture = null)
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = body?.message ?: "Sign up failed"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Sign up failed: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun logout() {
        secureTokenManager.clearAuthData()
        _uiState.value = AuthUiState()
    }

    fun forgotPassword(email: String) {
        if (!validateEmail(email)) {
            _uiState.value = _uiState.value.copy(error = "Invalid email")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val response = apiService.forgotPassword(ForgotPasswordRequest(email))
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Reset link sent to your email"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to send reset link"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun deleteAccount() {
        val token = _uiState.value.token
        if (token == null) {
            _uiState.value = _uiState.value.copy(error = "Not authenticated")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val response = apiService.deleteAccount("Bearer $token")
                if (response.isSuccessful) {
                    secureTokenManager.clearAuthData()
                    _uiState.value = AuthUiState()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to delete account: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun validateEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))
    }
}

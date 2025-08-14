package com.rio.rostry.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for authentication screens
 */
@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Authenticate user with email and password
     */
    fun authenticate(
        email: String,
        password: String,
        confirmPassword: String = "",
        fullName: String = "",
        phoneNumber: String = "",
        isLoginMode: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            try {
                // Simulate network delay
                delay(1500)

                if (isLoginMode) {
                    // Login logic
                    if (isValidCredentials(email, password)) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            successMessage = "Welcome back!"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Invalid email or password. Try: farmer@rio.com / demo123"
                        )
                    }
                } else {
                    // Registration logic
                    val validationError = validateRegistration(
                        email, password, confirmPassword, fullName, phoneNumber
                    )
                    
                    if (validationError != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = validationError
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            successMessage = "Account created successfully!"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Network error. Please try again."
                )
            }
        }
    }

    /**
     * Clear error and success messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    /**
     * Validate demo credentials
     */
    private fun isValidCredentials(email: String, password: String): Boolean {
        return when {
            email == "farmer@rio.com" && password == "demo123" -> true
            email == "enthusiast@rio.com" && password == "demo123" -> true
            email == "user@rio.com" && password == "demo123" -> true
            else -> false
        }
    }

    /**
     * Validate registration form
     */
    private fun validateRegistration(
        email: String,
        password: String,
        confirmPassword: String,
        fullName: String,
        phoneNumber: String
    ): String? {
        return when {
            email.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            password.isBlank() -> "Password is required"
            password.length < 8 -> "Password must be at least 8 characters"
            confirmPassword != password -> "Passwords do not match"
            fullName.isBlank() -> "Full name is required"
            phoneNumber.isBlank() -> "Phone number is required"
            !phoneNumber.matches(Regex("^[+]?[0-9]{10,15}$")) -> "Invalid phone number format"
            else -> null
        }
    }
}

/**
 * UI state for authentication screen
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
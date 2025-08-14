package com.rio.rostry.user.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.rio.rostry.core.common.base.BaseViewModel
import com.rio.rostry.core.common.model.*
import com.rio.rostry.user.domain.model.*
import com.rio.rostry.user.domain.usecases.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for authentication-related operations
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val sendPasswordResetUseCase: SendPasswordResetUseCase,
    private val verifyPhoneUseCase: VerifyPhoneUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : BaseViewModel() {

    // States
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _loginState = MutableStateFlow(LoginFormState())
    val loginState: StateFlow<LoginFormState> = _loginState.asStateFlow()

    // ... other states
    private val _registrationState = MutableStateFlow(RegistrationFormState())
    val registrationState: StateFlow<RegistrationFormState> = _registrationState.asStateFlow()
    private val _phoneVerificationState = MutableStateFlow(PhoneVerificationState())
    val phoneVerificationState: StateFlow<PhoneVerificationState> = _phoneVerificationState.asStateFlow()
    private val _passwordResetState = MutableStateFlow(PasswordResetState())
    val passwordResetState: StateFlow<PasswordResetState> = _passwordResetState.asStateFlow()


    init {
        checkAuthenticationState()
    }

    private fun checkAuthenticationState() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                _authState.value = when (result) {
                    is Result.Success -> result.data?.let { AuthState.Authenticated(it) } ?: AuthState.Unauthenticated
                    is Result.Error -> AuthState.Error(result.exception)
                    is Result.Loading -> AuthState.Loading
                }
            }
        }
    }

    /**
     * Sign in with email and password by calling the SignInUseCase
     */
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            signInUseCase(email, password).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _authState.value = AuthState.Loading
                        _loginState.value = _loginState.value.copy(isLoading = true, error = null)
                    }
                    is Result.Success -> {
                        val user = result.data
                        _authState.value = AuthState.Authenticated(user)
                        _loginState.value = _loginState.value.copy(isLoading = false)
                        logUserAction("sign_in_email", mapOf("user_tier" to user.tier.name))
                    }
                    is Result.Error -> {
                        val error = AppError.AuthenticationError("Invalid email or password", result.exception)
                        _authState.value = AuthState.Error(error)
                        _loginState.value = _loginState.value.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
            }
        }
    }

    // Other methods (signUp, signOut, etc.) would be refactored similarly
    // but are left as is to keep the scope of this change focused.

    /**
     * Sign up with email
     */
    fun signUpWithEmail(registration: UserRegistration) {
        if (!validateRegistration(registration)) return

        executeWithResult(
            action = { signUpUseCase.signUpWithEmail(registration) },
            onSuccess = { user ->
                _authState.value = AuthState.Authenticated(user)
                _registrationState.value = _registrationState.value.copy(
                    isLoading = false,
                    error = null,
                    isSuccess = true
                )
                logUserAction("sign_up_email", mapOf(
                    "user_tier" to user.tier.name,
                    "region" to user.regionalInfo.region
                ))
            },
            onError = { exception ->
                _registrationState.value = _registrationState.value.copy(
                    isLoading = false,
                    error = getErrorMessage(exception)
                )
            }
        )

        _registrationState.value = _registrationState.value.copy(isLoading = true, error = null)
    }

    /**
     * Sign out current user
     */
    fun signOut() {
        viewModelScope.launch {
            _authState.value = AuthState.Unauthenticated
            clearFormStates()
            logUserAction("sign_out")
        }
    }

    // ... other methods and helper functions remain the same for now

    /**
     * Send password reset email
     */
    fun sendPasswordResetEmail(email: String) {
        if (!isValidEmail(email)) {
            _passwordResetState.value = _passwordResetState.value.copy(
                error = "Please enter a valid email address"
            )
            return
        }

        executeWithResult(
            action = { sendPasswordResetUseCase(email) },
            onSuccess = {
                _passwordResetState.value = _passwordResetState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    error = null
                )
                logUserAction("password_reset_requested")
            },
            onError = { exception ->
                _passwordResetState.value = _passwordResetState.value.copy(
                    isLoading = false,
                    error = getErrorMessage(exception)
                )
            }
        )

        _passwordResetState.value = _passwordResetState.value.copy(isLoading = true, error = null)
    }

    /**
     * Verify phone number with code
     */
    fun verifyPhoneNumber(phoneNumber: String, code: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                // Simulate network delay
                delay(1500)

                // Demo phone authentication
                if (phoneNumber.isNotEmpty() && code == "123456") {
                    val user = createDemoUserFromPhone(phoneNumber)
                    _authState.value = AuthState.Authenticated(user)
                    _phoneVerificationState.value = _phoneVerificationState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                    logUserAction("phone_verified")
                } else {
                    _authState.value = AuthState.Error(AppError.AuthenticationError("Invalid phone number or verification code"))
                    _phoneVerificationState.value = _phoneVerificationState.value.copy(
                        isLoading = false,
                        error = "Invalid phone number or verification code"
                    )
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(AppError.NetworkError("Verification failed", e))
                _phoneVerificationState.value = _phoneVerificationState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Verification failed"
                )
            }
        }
    }

    /**
     * Update login form state
     */
    fun updateLoginForm(email: String, password: String) {
        _loginState.value = _loginState.value.copy(
            email = email,
            password = password,
            isEmailValid = isValidEmail(email),
            isPasswordValid = isValidPassword(password),
            isFormValid = isValidEmail(email) && isValidPassword(password)
        )
    }

    /**
     * Update registration form state
     */
    fun updateRegistrationForm(
        email: String,
        password: String,
        confirmPassword: String,
        displayName: String,
        phoneNumber: String,
        region: String,
        district: String
    ) {
        _registrationState.value = _registrationState.value.copy(
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            displayName = displayName,
            phoneNumber = phoneNumber,
            region = region,
            district = district,
            isEmailValid = isValidEmail(email),
            isPasswordValid = isValidPassword(password),
            isPasswordMatch = password == confirmPassword,
            isDisplayNameValid = displayName.isNotBlank(),
            isPhoneValid = isValidPhoneNumber(phoneNumber),
            error = null
        )
    }

    /**
     * Clear all form states
     */
    private fun clearFormStates() {
        _loginState.value = LoginFormState()
        _registrationState.value = RegistrationFormState()
        _phoneVerificationState.value = PhoneVerificationState()
        _passwordResetState.value = PasswordResetState()
    }

    /**
     * Validate registration data
     */
    private fun validateRegistration(registration: UserRegistration): Boolean {
        val errors = mutableListOf<String>()

        if (!isValidEmail(registration.email)) {
            errors.add("Invalid email address")
        }

        if (!isValidPassword(registration.password)) {
            errors.add("Password must be at least 8 characters")
        }

        if (registration.displayName.isBlank()) {
            errors.add("Display name is required")
        }

        if (!registration.agreedToTerms) {
            errors.add("You must agree to the terms and conditions")
        }

        if (errors.isNotEmpty()) {
            _registrationState.value = _registrationState.value.copy(
                error = errors.joinToString(", ")
            )
            return false
        }

        return true
    }

    /**
     * Validate email format
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validate password strength
     */
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    /**
     * Validate phone number format
     */
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.matches(Regex("^[+]?[0-9]{10,15}$"))
    }

    /**
     * Get user-friendly error message
     */
    private fun getErrorMessage(exception: Throwable): String {
        return when (exception) {
            is AppError.NetworkError -> "Network connection error. Please check your internet connection."
            is AppError.AuthenticationError -> "Invalid email or password. Please try again."
            is AppError.ValidationError -> "Please check your input and try again."
            else -> exception.message ?: "An unexpected error occurred"
        }
    }

    /**
     * Clear error messages
     */
    fun clearError() {
        _loginState.value = _loginState.value.copy(error = null)
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    // The demo helper methods are no longer needed here
    private fun createDemoUserFromPhone(phoneNumber: String): User {
        return User(
            id = "phone_user_001",
            email = "",
            displayName = "Phone User",
            phoneNumber = phoneNumber,
            tier = UserTier.GENERAL,
            isEmailVerified = false,
            isPhoneVerified = true
        )
    }
}

// Data classes for form states remain the same
// ...
data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val isEmailValid: Boolean = false,
    val isPasswordValid: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        fun validate(email: String, password: String): LoginFormState {
            return LoginFormState(
                email = email,
                password = password,
                isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches(),
                isPasswordValid = password.length >= 8,
                error = null
            )
        }
    }

    val isFormValid: Boolean
        get() = isEmailValid && isPasswordValid && !isLoading
}

data class RegistrationFormState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val displayName: String = "",
    val phoneNumber: String = "",
    val region: String = "",
    val district: String = "",
    val isEmailValid: Boolean = false,
    val isPasswordValid: Boolean = false,
    val isPasswordMatch: Boolean = false,
    val isDisplayNameValid: Boolean = false,
    val isPhoneValid: Boolean = false,
    val agreedToTerms: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
) {
    val isFormValid: Boolean
        get() = isEmailValid && isPasswordValid && isPasswordMatch && 
                isDisplayNameValid && agreedToTerms && !isLoading
}

data class PhoneVerificationState(
    val phoneNumber: String = "",
    val verificationCode: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class PasswordResetState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
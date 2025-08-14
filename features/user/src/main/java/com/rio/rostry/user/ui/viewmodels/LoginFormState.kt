package com.rio.rostry.user.ui.viewmodels

/**
 * Represents the state of the login form
 */
data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val isEmailValid: Boolean = false,
    val isPasswordValid: Boolean = false,
    val isFormValid: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        fun validate(email: String, password: String): LoginFormState {
            val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
            val isPasswordValid = password.length >= 8
            val isFormValid = isEmailValid && isPasswordValid
            
            return LoginFormState(
                email = email,
                password = password,
                isEmailValid = isEmailValid,
                isPasswordValid = isPasswordValid,
                isFormValid = isFormValid
            )
        }
    }
}
package com.rio.rostry.user.domain.model

/**
 * Data class for user registration
 */
data class UserRegistration(
    val email: String,
    val password: String,
    val displayName: String,
    val phoneNumber: String,
    val regionalInfo: RegionalInfo = RegionalInfo(),
    val agreedToTerms: Boolean = false,
    val agreedToPrivacyPolicy: Boolean = false
) {
    fun isValid(): Boolean {
        return email.isNotBlank() &&
               password.length >= 8 &&
               displayName.isNotBlank() &&
               agreedToTerms &&
               agreedToPrivacyPolicy
    }
}
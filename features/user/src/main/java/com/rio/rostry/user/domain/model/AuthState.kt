package com.rio.rostry.user.domain.model

import com.rio.rostry.core.common.model.UserTier

/**
 * Sealed class representing authentication states
 */
sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val exception: Throwable) : AuthState()
}

/**
 * User model for authentication
 */
data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val phoneNumber: String? = null,
    val tier: UserTier,
    val isEmailVerified: Boolean = false,
    val profileImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)
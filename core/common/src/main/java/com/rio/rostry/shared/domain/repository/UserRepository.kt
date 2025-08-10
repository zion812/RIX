package com.rio.rostry.shared.domain.repository

import com.rio.rostry.core.common.model.UserTier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * ✅ Simple user repository interface
 */
interface UserRepository {
    fun getCurrentUserTier(): Flow<UserTier?>
    fun isUserAuthenticated(): Boolean
    fun getCurrentUserId(): String?
}

/**
 * ✅ Basic implementation of UserRepository
 */
class UserRepositoryImpl : UserRepository {
    override fun getCurrentUserTier(): Flow<UserTier?> {
        return flowOf(UserTier.GENERAL) // Default tier
    }

    override fun isUserAuthenticated(): Boolean {
        return false // Default: not authenticated
    }

    override fun getCurrentUserId(): String? {
        return null // Default: no user ID
    }
}

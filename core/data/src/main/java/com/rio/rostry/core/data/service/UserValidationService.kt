package com.rio.rostry.core.data.service

import com.rio.rostry.core.database.dao.UserDao
import com.rio.rostry.core.database.entities.UserEntity
import com.rio.rostry.core.common.model.UserTier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ✅ User validation service to break circular dependencies
 * Provides lightweight user validation without full repository dependencies
 */
@Singleton
class UserValidationService @Inject constructor(
    private val userDao: UserDao
) {
    
    /**
     * Check if user is verified
     */
    suspend fun isUserVerified(userId: String): Boolean {
        return try {
            val user = userDao.getUserById(userId)
            user?.isVerified ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get user tier
     */
    suspend fun getUserTier(userId: String): UserTier {
        return try {
            val user = userDao.getUserById(userId)
            user?.tier ?: UserTier.GENERAL
        } catch (e: Exception) {
            UserTier.GENERAL
        }
    }
    
    /**
     * Check if user has specific tier or higher
     */
    suspend fun hasMinimumTier(userId: String, minimumTier: UserTier): Boolean {
        val userTier = getUserTier(userId)
        return userTier.ordinal >= minimumTier.ordinal
    }
    
    /**
     * Check if user can perform farmer operations
     */
    suspend fun canPerformFarmerOperations(userId: String): Boolean {
        return hasMinimumTier(userId, UserTier.FARMER)
    }
    
    /**
     * Check if user can perform enthusiast operations
     */
    suspend fun canPerformEnthusiastOperations(userId: String): Boolean {
        return hasMinimumTier(userId, UserTier.ENTHUSIAST)
    }
    
    /**
     * Get user basic info for validation
     */
    suspend fun getUserBasicInfo(userId: String): UserBasicInfo? {
        return try {
            val user = userDao.getUserById(userId)
            user?.let {
                UserBasicInfo(
                    id = it.id,
                    email = it.email,
                    tier = it.tier,
                    isVerified = it.isVerified,
                    isActive = it.isActive,
                    coinBalance = it.coinBalance
                )
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Validate user exists and is active
     */
    suspend fun validateUserExists(userId: String): ValidationResult {
        return try {
            val user = userDao.getUserById(userId)
            when {
                user == null -> ValidationResult.UserNotFound
                !user.isActive -> ValidationResult.UserInactive
                else -> ValidationResult.Valid
            }
        } catch (e: Exception) {
            ValidationResult.Error(e.message ?: "Validation failed")
        }
    }
    
    /**
     * Validate user can own fowls
     */
    suspend fun validateCanOwnFowls(userId: String): ValidationResult {
        return try {
            val userInfo = getUserBasicInfo(userId)
            when {
                userInfo == null -> ValidationResult.UserNotFound
                !userInfo.isActive -> ValidationResult.UserInactive
                !userInfo.isVerified -> ValidationResult.UserNotVerified
                !canPerformFarmerOperations(userId) -> ValidationResult.InsufficientTier
                else -> ValidationResult.Valid
            }
        } catch (e: Exception) {
            ValidationResult.Error(e.message ?: "Validation failed")
        }
    }
    
    /**
     * Validate user can create marketplace listings
     */
    suspend fun validateCanCreateListings(userId: String): ValidationResult {
        return try {
            val userInfo = getUserBasicInfo(userId)
            when {
                userInfo == null -> ValidationResult.UserNotFound
                !userInfo.isActive -> ValidationResult.UserInactive
                !userInfo.isVerified -> ValidationResult.UserNotVerified
                !canPerformFarmerOperations(userId) -> ValidationResult.InsufficientTier
                else -> ValidationResult.Valid
            }
        } catch (e: Exception) {
            ValidationResult.Error(e.message ?: "Validation failed")
        }
    }
    
    /**
     * Validate user has sufficient coins
     */
    suspend fun validateSufficientCoins(userId: String, requiredCoins: Int): ValidationResult {
        return try {
            val userInfo = getUserBasicInfo(userId)
            when {
                userInfo == null -> ValidationResult.UserNotFound
                !userInfo.isActive -> ValidationResult.UserInactive
                userInfo.coinBalance < requiredCoins -> ValidationResult.InsufficientCoins
                else -> ValidationResult.Valid
            }
        } catch (e: Exception) {
            ValidationResult.Error(e.message ?: "Validation failed")
        }
    }
    
    /**
     * Observe user tier changes
     */
    fun observeUserTier(userId: String): Flow<UserTier> {
        return userDao.observeUser(userId).map { user ->
            user?.tier ?: UserTier.GENERAL
        }
    }
    
    /**
     * Observe user verification status
     */
    fun observeUserVerification(userId: String): Flow<Boolean> {
        return userDao.observeUser(userId).map { user ->
            user?.isVerified ?: false
        }
    }
}

/**
 * Basic user information for validation
 */
data class UserBasicInfo(
    val id: String,
    val email: String,
    val tier: UserTier,
    val isVerified: Boolean,
    val isActive: Boolean,
    val coinBalance: Int
)

/**
 * Validation result sealed class
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    object UserNotFound : ValidationResult()
    object UserInactive : ValidationResult()
    object UserNotVerified : ValidationResult()
    object InsufficientTier : ValidationResult()
    object InsufficientCoins : ValidationResult()
    data class Error(val message: String) : ValidationResult()
    
    val isValid: Boolean
        get() = this is Valid
    
    val errorMessage: String?
        get() = when (this) {
            is Valid -> null
            is UserNotFound -> "User not found"
            is UserInactive -> "User account is inactive"
            is UserNotVerified -> "User account is not verified"
            is InsufficientTier -> "User tier insufficient for this operation"
            is InsufficientCoins -> "Insufficient coin balance"
            is Error -> message
        }
}

/**
 * Exception for validation failures
 */
class ValidationException(
    val result: ValidationResult
) : Exception(result.errorMessage)

/**
 * Extension function to throw exception if validation fails
 */
suspend fun UserValidationService.validateOrThrow(
    validation: suspend () -> ValidationResult
) {
    val result = validation()
    if (!result.isValid) {
        throw ValidationException(result)
    }
}

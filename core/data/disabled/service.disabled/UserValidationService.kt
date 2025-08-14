package com.rio.rostry.core.data.service

import com.rio.rostry.core.database.dao.UserDao
import com.rio.rostry.core.database.dao.CoinDao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for validating user operations and permissions
 */
@Singleton
class UserValidationService @Inject constructor(
    private val userDao: UserDao,
    private val coinDao: CoinDao
) {
    
    /**
     * Validate if user has sufficient coins for a transaction
     */
    suspend fun validateSufficientCoins(userId: String, requiredCoins: Int): ValidationResult {
        val user = userDao.getUserById(userId)
            ?: return ValidationResult(false, "User not found")
        
        val availableCoins = user.coinBalance - user.pendingCoinBalance
        
        return if (availableCoins >= requiredCoins) {
            ValidationResult(true, "Sufficient coins available")
        } else {
            ValidationResult(
                false, 
                "Insufficient coins. Required: $requiredCoins, Available: $availableCoins"
            )
        }
    }
    
    /**
     * Validate if user can create marketplace listing
     */
    suspend fun validateMarketplaceListingPermission(userId: String): ValidationResult {
        val user = userDao.getUserById(userId)
            ?: return ValidationResult(false, "User not found")
        
        return when (user.tier) {
            "farmer", "enthusiast" -> ValidationResult(true, "User has marketplace permissions")
            else -> ValidationResult(false, "User tier does not allow marketplace listings")
        }
    }
    
    /**
     * Validate if user can access premium features
     */
    suspend fun validatePremiumFeatureAccess(userId: String): ValidationResult {
        val user = userDao.getUserById(userId)
            ?: return ValidationResult(false, "User not found")
        
        return when (user.tier) {
            "enthusiast" -> ValidationResult(true, "User has premium access")
            else -> ValidationResult(false, "Premium features require enthusiast tier")
        }
    }
    
    /**
     * Validate if user can perform fowl transfers
     */
    suspend fun validateFowlTransferPermission(userId: String): ValidationResult {
        val user = userDao.getUserById(userId)
            ?: return ValidationResult(false, "User not found")
        
        return when (user.tier) {
            "farmer", "enthusiast" -> {
                if (user.isEmailVerified) {
                    ValidationResult(true, "User can perform fowl transfers")
                } else {
                    ValidationResult(false, "Email verification required for fowl transfers")
                }
            }
            else -> ValidationResult(false, "Fowl transfers require farmer tier or higher")
        }
    }
    
    /**
     * Validate daily transaction limits
     */
    suspend fun validateDailyTransactionLimit(userId: String, transactionAmount: Int): ValidationResult {
        val user = userDao.getUserById(userId)
            ?: return ValidationResult(false, "User not found")
        
        val dailyLimit = when (user.tier) {
            "general" -> 1000
            "farmer" -> 5000
            "enthusiast" -> 20000
            else -> 500
        }
        
        // Get today's transactions
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.time
        
        val tomorrow = java.util.Calendar.getInstance().apply {
            time = today
            add(java.util.Calendar.DAY_OF_MONTH, 1)
        }.time
        
        val todayTransactions = coinDao.getTransactionsByDateRange(userId, today, tomorrow)
        val todaySpent = todayTransactions
            .filter { it.status == com.rio.rostry.core.common.model.TransactionStatus.COMPLETED }
            .filter { it.transactionType != com.rio.rostry.core.common.model.TransactionType.COIN_PURCHASE }
            .sumOf { it.amount }
        
        val remainingLimit = dailyLimit - todaySpent
        
        return if (transactionAmount <= remainingLimit) {
            ValidationResult(true, "Within daily transaction limit")
        } else {
            ValidationResult(
                false, 
                "Daily transaction limit exceeded. Limit: $dailyLimit, Used: $todaySpent, Remaining: $remainingLimit"
            )
        }
    }
    
    /**
     * Validate user's verification status for specific operations
     */
    suspend fun validateVerificationStatus(userId: String, requiredVerifications: List<VerificationType>): ValidationResult {
        val user = userDao.getUserById(userId)
            ?: return ValidationResult(false, "User not found")
        
        val missingVerifications = mutableListOf<String>()
        
        requiredVerifications.forEach { verification ->
            when (verification) {
                VerificationType.EMAIL -> {
                    if (!user.isEmailVerified) {
                        missingVerifications.add("Email verification")
                    }
                }
                VerificationType.PHONE -> {
                    if (!user.isPhoneVerified) {
                        missingVerifications.add("Phone verification")
                    }
                }
            }
        }
        
        return if (missingVerifications.isEmpty()) {
            ValidationResult(true, "All required verifications completed")
        } else {
            ValidationResult(false, "Missing verifications: ${missingVerifications.joinToString(", ")}")
        }
    }
}

/**
 * Result of a validation operation
 */
data class ValidationResult(
    val isValid: Boolean,
    val message: String,
    val errorCode: String? = null
)

/**
 * Types of user verification
 */
enum class VerificationType {
    EMAIL,
    PHONE
}

/**
 * Exception thrown when validation fails
 */
class ValidationException(
    val validationResult: ValidationResult
) : Exception(validationResult.message)
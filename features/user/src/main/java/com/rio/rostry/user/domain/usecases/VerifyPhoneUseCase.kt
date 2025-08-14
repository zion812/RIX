package com.rio.rostry.user.domain.usecases

import com.rio.rostry.core.common.model.Result
import com.rio.rostry.user.domain.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for phone number verification
 */
class VerifyPhoneUseCase @Inject constructor(
    // TODO: Inject AuthRepository when available
) {
    
    suspend operator fun invoke(phoneNumber: String, verificationCode: String): Flow<Result<User>> = flow {
        emit(Result.Loading)
        
        try {
            // Simulate network delay
            delay(1500)
            
            // Demo phone verification logic
            if (verificationCode == "123456") {
                val user = createUserFromPhone(phoneNumber)
                emit(Result.Success(user))
            } else {
                emit(Result.Error(IllegalArgumentException("Invalid verification code")))
            }
            
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
    
    private fun createUserFromPhone(phoneNumber: String): User {
        return User(
            id = "phone_user_${System.currentTimeMillis()}",
            email = "",
            displayName = "Phone User",
            phoneNumber = phoneNumber,
            tier = com.rio.rostry.user.domain.model.UserTier.GENERAL,
            isEmailVerified = false,
            isPhoneVerified = true
        )
    }
}
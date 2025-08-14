package com.rio.rostry.user.domain.usecases

import com.rio.rostry.core.common.model.Result
import com.rio.rostry.user.domain.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for getting current authenticated user
 */
class GetCurrentUserUseCase @Inject constructor(
    // TODO: Inject AuthRepository when available
) {
    
    suspend operator fun invoke(): Flow<Result<User?>> = flow {
        emit(Result.Loading)
        
        try {
            // Simulate network delay
            delay(500)
            
            // Demo logic - return null for unauthenticated state
            emit(Result.Success(null))
            
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}
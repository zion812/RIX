package com.rio.rostry.core.common.model

/**
 * Sealed class for application errors
 */
sealed class AppError(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    
    data class NetworkError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause)
    
    data class AuthenticationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause)
    
    data class ValidationError(
        override val message: String,
        val field: String? = null,
        override val cause: Throwable? = null
    ) : AppError(message, cause)
    
    data class DatabaseError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause)
    
    data class PermissionError(
        override val message: String,
        val permission: String? = null,
        override val cause: Throwable? = null
    ) : AppError(message, cause)
    
    data class UnknownError(
        override val message: String = "An unknown error occurred",
        override val cause: Throwable? = null
    ) : AppError(message, cause)
}
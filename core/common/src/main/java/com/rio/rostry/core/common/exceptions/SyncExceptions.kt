package com.rio.rostry.core.common.exceptions

/**
 * Comprehensive exception hierarchy for sync operations
 * Provides specific error types for better error handling and user feedback
 */

/**
 * Base sync exception
 */
sealed class SyncException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    
    /**
     * Network-related errors
     */
    sealed class NetworkError(message: String, cause: Throwable? = null) : SyncException(message, cause) {
        class NoConnection(message: String = "No network connection available") : NetworkError(message)
        class Timeout(message: String = "Network request timed out") : NetworkError(message)
        class ServerError(message: String, cause: Throwable? = null) : NetworkError(message, cause)
        class RateLimited(message: String = "Too many requests, please try again later") : NetworkError(message)
        class BadRequest(message: String) : NetworkError(message)
        class Unauthorized(message: String = "Authentication required") : NetworkError(message)
        class Forbidden(message: String = "Access denied") : NetworkError(message)
        class NotFound(message: String = "Resource not found") : NetworkError(message)
    }
    
    /**
     * Database-related errors
     */
    sealed class DatabaseError(message: String, cause: Throwable? = null) : SyncException(message, cause) {
        class ConnectionFailed(message: String, cause: Throwable? = null) : DatabaseError(message, cause)
        class QueryFailed(message: String, cause: Throwable? = null) : DatabaseError(message, cause)
        class ConstraintViolation(message: String, cause: Throwable? = null) : DatabaseError(message, cause)
        class DataCorruption(message: String, cause: Throwable? = null) : DatabaseError(message, cause)
        class StorageFull(message: String = "Device storage is full") : DatabaseError(message)
        class MigrationFailed(message: String, cause: Throwable? = null) : DatabaseError(message, cause)
    }
    
    /**
     * Data validation errors
     */
    sealed class ValidationError(message: String, cause: Throwable? = null) : SyncException(message, cause) {
        class InvalidData(field: String, value: String?, reason: String) : 
            ValidationError("Invalid $field: $value. $reason")
        class MissingRequiredField(field: String) : 
            ValidationError("Required field missing: $field")
        class InvalidFormat(field: String, expectedFormat: String) : 
            ValidationError("Invalid format for $field. Expected: $expectedFormat")
        class ValueOutOfRange(field: String, value: String, min: String?, max: String?) : 
            ValidationError("Value $value for $field is out of range [$min, $max]")
        class BusinessRuleViolation(rule: String, details: String) : 
            ValidationError("Business rule violation: $rule. $details")
    }
    
    /**
     * Conflict-related errors
     */
    sealed class ConflictError(message: String, cause: Throwable? = null) : SyncException(message, cause) {
        class VersionConflict(entityId: String, localVersion: Long, serverVersion: Long) : 
            ConflictError("Version conflict for entity $entityId: local=$localVersion, server=$serverVersion")
        class ConcurrentModification(entityId: String) : 
            ConflictError("Entity $entityId was modified by another user")
        class OwnershipConflict(entityId: String, currentOwner: String, claimedOwner: String) : 
            ConflictError("Ownership conflict for entity $entityId: current=$currentOwner, claimed=$claimedOwner")
        class DeletionConflict(entityId: String) : 
            ConflictError("Cannot modify deleted entity $entityId")
        class UnresolvableConflict(entityId: String, reason: String) : 
            ConflictError("Cannot resolve conflict for entity $entityId: $reason")
    }
    
    /**
     * Authentication and authorization errors
     */
    sealed class AuthError(message: String, cause: Throwable? = null) : SyncException(message, cause) {
        class NotAuthenticated(message: String = "User not authenticated") : AuthError(message)
        class TokenExpired(message: String = "Authentication token expired") : AuthError(message)
        class InvalidCredentials(message: String = "Invalid credentials") : AuthError(message)
        class InsufficientPermissions(action: String, resource: String) : 
            AuthError("Insufficient permissions to $action $resource")
        class AccountSuspended(reason: String) : 
            AuthError("Account suspended: $reason")
        class TierRestriction(requiredTier: String, currentTier: String) : 
            AuthError("Action requires $requiredTier tier, current tier: $currentTier")
    }
    
    /**
     * Sync operation errors
     */
    sealed class SyncOperationError(message: String, cause: Throwable? = null) : SyncException(message, cause) {
        class AlreadyInProgress(operation: String) : 
            SyncOperationError("Sync operation already in progress: $operation")
        class QueueFull(message: String = "Sync queue is full") : SyncOperationError(message)
        class DependencyNotMet(actionId: String, dependencyId: String) : 
            SyncOperationError("Action $actionId depends on $dependencyId which is not completed")
        class MaxRetriesExceeded(actionId: String, retryCount: Int) : 
            SyncOperationError("Action $actionId exceeded max retries: $retryCount")
        class InvalidOperation(operation: String, reason: String) : 
            SyncOperationError("Invalid operation $operation: $reason")
    }
    
    /**
     * Data compression and serialization errors
     */
    sealed class DataProcessingError(message: String, cause: Throwable? = null) : SyncException(message, cause) {
        class CompressionFailed(message: String, cause: Throwable? = null) : DataProcessingError(message, cause)
        class DecompressionFailed(message: String, cause: Throwable? = null) : DataProcessingError(message, cause)
        class SerializationFailed(message: String, cause: Throwable? = null) : DataProcessingError(message, cause)
        class DeserializationFailed(message: String, cause: Throwable? = null) : DataProcessingError(message, cause)
        class EncryptionFailed(message: String, cause: Throwable? = null) : DataProcessingError(message, cause)
        class DecryptionFailed(message: String, cause: Throwable? = null) : DataProcessingError(message, cause)
    }
    
    /**
     * Media and file handling errors
     */
    sealed class MediaError(message: String, cause: Throwable? = null) : SyncException(message, cause) {
        class FileNotFound(filePath: String) : MediaError("File not found: $filePath")
        class UnsupportedFormat(fileName: String, mimeType: String) : 
            MediaError("Unsupported file format: $fileName ($mimeType)")
        class FileTooLarge(fileName: String, size: Long, maxSize: Long) : 
            MediaError("File too large: $fileName ($size bytes, max: $maxSize)")
        class UploadFailed(fileName: String, reason: String) : 
            MediaError("Upload failed for $fileName: $reason")
        class DownloadFailed(url: String, reason: String) : 
            MediaError("Download failed for $url: $reason")
        class CompressionFailed(fileName: String, reason: String) : 
            MediaError("Image compression failed for $fileName: $reason")
    }
    
    /**
     * Regional and localization errors
     */
    sealed class RegionalError(message: String, cause: Throwable? = null) : SyncException(message, cause) {
        class UnsupportedRegion(region: String) : 
            RegionalError("Region not supported: $region")
        class InvalidLocation(latitude: Double, longitude: Double) : 
            RegionalError("Invalid location: $latitude, $longitude")
        class RegionMismatch(expectedRegion: String, actualRegion: String) : 
            RegionalError("Region mismatch: expected $expectedRegion, got $actualRegion")
        class LocalizationFailed(language: String, key: String) : 
            RegionalError("Localization failed for $language: $key")
    }
    
    /**
     * Unknown or unexpected errors
     */
    class UnknownError(message: String, cause: Throwable? = null) : SyncException(message, cause)
}

/**
 * Extension functions for exception handling
 */

/**
 * Check if exception is retryable
 */
fun SyncException.isRetryable(): Boolean {
    return when (this) {
        is SyncException.NetworkError.NoConnection,
        is SyncException.NetworkError.Timeout,
        is SyncException.NetworkError.ServerError,
        is SyncException.NetworkError.RateLimited,
        is SyncException.DatabaseError.ConnectionFailed,
        is SyncException.SyncOperationError.QueueFull -> true
        
        is SyncException.NetworkError.BadRequest,
        is SyncException.NetworkError.Unauthorized,
        is SyncException.NetworkError.Forbidden,
        is SyncException.NetworkError.NotFound,
        is SyncException.ValidationError,
        is SyncException.AuthError,
        is SyncException.ConflictError.UnresolvableConflict -> false
        
        else -> false
    }
}

/**
 * Get user-friendly error message
 */
fun SyncException.getUserMessage(): String {
    return when (this) {
        is SyncException.NetworkError.NoConnection -> 
            "No internet connection. Please check your network and try again."
        is SyncException.NetworkError.Timeout -> 
            "Request timed out. Please try again."
        is SyncException.NetworkError.ServerError -> 
            "Server error. Please try again later."
        is SyncException.NetworkError.RateLimited -> 
            "Too many requests. Please wait a moment and try again."
        is SyncException.DatabaseError.StorageFull -> 
            "Device storage is full. Please free up space and try again."
        is SyncException.ValidationError.InvalidData -> 
            "Invalid data: ${this.message}"
        is SyncException.ValidationError.MissingRequiredField -> 
            "Required field is missing: ${this.message}"
        is SyncException.AuthError.NotAuthenticated -> 
            "Please log in to continue."
        is SyncException.AuthError.InsufficientPermissions -> 
            "You don't have permission to perform this action."
        is SyncException.ConflictError.VersionConflict -> 
            "This item was updated by someone else. Please refresh and try again."
        is SyncException.MediaError.FileTooLarge -> 
            "File is too large. Please choose a smaller file."
        is SyncException.MediaError.UnsupportedFormat -> 
            "File format not supported. Please choose a different file."
        else -> "An error occurred. Please try again."
    }
}

/**
 * Get error category for analytics
 */
fun SyncException.getCategory(): String {
    return when (this) {
        is SyncException.NetworkError -> "network"
        is SyncException.DatabaseError -> "database"
        is SyncException.ValidationError -> "validation"
        is SyncException.ConflictError -> "conflict"
        is SyncException.AuthError -> "auth"
        is SyncException.SyncOperationError -> "sync_operation"
        is SyncException.DataProcessingError -> "data_processing"
        is SyncException.MediaError -> "media"
        is SyncException.RegionalError -> "regional"
        is SyncException.UnknownError -> "unknown"
    }
}

/**
 * Convert throwable to appropriate SyncException
 */
fun Throwable.toSyncException(): SyncException {
    return when (this) {
        is SyncException -> this
        is java.net.UnknownHostException -> SyncException.NetworkError.NoConnection()
        is java.net.SocketTimeoutException -> SyncException.NetworkError.Timeout()
        is java.net.ConnectException -> SyncException.NetworkError.NoConnection()
        is java.io.IOException -> SyncException.NetworkError.ServerError(message ?: "IO Error", this)
        is IllegalArgumentException -> SyncException.ValidationError.InvalidData("unknown", null, message ?: "Invalid argument")
        is SecurityException -> SyncException.AuthError.InsufficientPermissions("unknown", "unknown")
        else -> SyncException.UnknownError(message ?: "Unknown error", this)
    }
}

package com.rio.rostry.core.common.model

/**
 * Sealed class representing different UI states for consistent state management across the app
 */
sealed class UiState {
    object Loading : UiState()
    object Success : UiState()
    object Empty : UiState()
    data class Error(val exception: Throwable, val message: String = exception.message ?: "Unknown error") : UiState()
}

/**
 * Sealed class for handling async operations with proper error handling
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

/**
 * User tier enumeration for permission-based access control
 */
enum class UserTier(val displayName: String, val level: Int) {
    GENERAL("General User", 1),
    FARMER("Farmer", 2),
    ENTHUSIAST("Enthusiast", 3);

    companion object {
        fun fromString(value: String?): UserTier? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
    }
}

/**
 * Network connection state
 */
sealed class NetworkState {
    object Connected : NetworkState()
    object Disconnected : NetworkState()
    data class Limited(val type: NetworkType) : NetworkState()
}

/**
 * Network type for adaptive loading strategies
 */
enum class NetworkType {
    WIFI,
    MOBILE_4G,
    MOBILE_3G,
    MOBILE_2G,
    UNKNOWN
}

/**
 * Loading state for different operations
 */
data class LoadingState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val progress: Float? = null
)

/**
 * Error types for better error handling
 */
sealed class AppError : Exception() {
    object NetworkError : AppError()
    object AuthenticationError : AppError()
    object PermissionError : AppError()
    object ValidationError : AppError()
    data class ServerError(val code: Int, override val message: String) : AppError()
    data class UnknownError(override val message: String) : AppError()
}

/**
 * User permissions for tier-based access control
 */
data class UserPermissions(
    val canCreateListings: Boolean = false,
    val canEditListings: Boolean = false,
    val canDeleteListings: Boolean = false,
    val canAccessMarketplace: Boolean = true,
    val canManageBreedingRecords: Boolean = false,
    val canAccessAnalytics: Boolean = false,
    val canAccessPremiumFeatures: Boolean = false,
    val canVerifyTransfers: Boolean = false,
    val canAccessPrioritySupport: Boolean = false,
    val canModerateContent: Boolean = false
) {
    companion object {
        fun forTier(tier: UserTier): UserPermissions {
            return when (tier) {
                UserTier.GENERAL -> UserPermissions(
                    canAccessMarketplace = true
                )
                UserTier.FARMER -> UserPermissions(
                    canCreateListings = true,
                    canEditListings = true,
                    canDeleteListings = true,
                    canAccessMarketplace = true,
                    canManageBreedingRecords = true
                )
                UserTier.ENTHUSIAST -> UserPermissions(
                    canCreateListings = true,
                    canEditListings = true,
                    canDeleteListings = true,
                    canAccessMarketplace = true,
                    canManageBreedingRecords = true,
                    canAccessAnalytics = true,
                    canAccessPremiumFeatures = true,
                    canVerifyTransfers = true,
                    canAccessPrioritySupport = true
                )
            }
        }
    }
}

/**
 * Verification status for user accounts
 */
data class VerificationStatus(
    val level: VerificationLevel = VerificationLevel.BASIC,
    val emailVerified: Boolean = false,
    val phoneVerified: Boolean = false,
    val identityVerified: Boolean = false,
    val farmDocumentsVerified: Boolean = false,
    val referencesVerified: Boolean = false
)

/**
 * Verification levels
 */
enum class VerificationLevel {
    BASIC,
    ENHANCED,
    PREMIUM
}

/**
 * Regional information for location-based features
 */
data class RegionalInfo(
    val region: String,
    val district: String,
    val mandal: String? = null,
    val village: String? = null,
    val pincode: String,
    val coordinates: Coordinates? = null
)

/**
 * GPS coordinates
 */
data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

/**
 * Language preferences
 */
enum class Language(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    TELUGU("te", "తెలుగు"),
    HINDI("hi", "हिन्दी");

    companion object {
        fun fromCode(code: String): Language {
            return values().find { it.code == code } ?: ENGLISH
        }
    }
}

/**
 * Sync status for offline data
 */
enum class SyncStatus {
    SYNCED,
    PENDING,
    FAILED,
    CONFLICT
}

/**
 * Data quality indicators
 */
enum class DataQuality {
    HIGH,
    MEDIUM,
    LOW
}

/**
 * Priority levels for various operations
 */
enum class Priority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

/**
 * Generic list state for RecyclerView adapters
 */
data class ListState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = false,
    val isRefreshing: Boolean = false
)

/**
 * Pagination state
 */
data class PaginationState(
    val currentPage: Int = 0,
    val pageSize: Int = 20,
    val hasMore: Boolean = true,
    val isLoadingMore: Boolean = false
)

/**
 * Search state
 */
data class SearchState(
    val query: String = "",
    val isSearching: Boolean = false,
    val suggestions: List<String> = emptyList()
)

/**
 * Filter state for marketplace and other list screens
 */
data class FilterState(
    val breed: String? = null,
    val gender: String? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val location: String? = null,
    val ageCategory: String? = null,
    val availability: String? = null
)

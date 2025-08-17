package com.rio.rostry.core.common.model.state

/**
 * Common UI state for use in ViewModels and screens.
 */
sealed class UiState {
    object Loading : UiState()
    object Success : UiState()
    data class Error(val throwable: Throwable, val message: String? = null) : UiState()
    object Empty : UiState()
}

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

/**
 * Search state
 */
data class SearchState(
    val query: String = "",
    val isSearching: Boolean = false,
    val suggestions: List<String> = emptyList()
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
 * Priority levels for various operations
 */
enum class Priority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
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
 * Sync status for offline data
 */
enum class SyncStatus {
    SYNCED,
    PENDING,
    FAILED,
    CONFLICT
}

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
 * Verification levels
 */
enum class VerificationLevel {
    BASIC,
    ENHANCED,
    PREMIUM
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
 * Loading state for different operations
 */
data class LoadingState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val progress: Float? = null
)
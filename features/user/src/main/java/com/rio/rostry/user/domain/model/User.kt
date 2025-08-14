package com.rio.rostry.user.domain.model

import com.rio.rostry.core.common.model.*
import java.util.*

/**
 * User domain model for the user feature module
 */
data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val phoneNumber: String? = null,
    val profileImageUrl: String? = null,
    val tier: UserTier = UserTier.GENERAL,
    val isEmailVerified: Boolean = false,
    val isPhoneVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long? = null,
    val isActive: Boolean = true
) {
    val isVerified: Boolean
        get() = isEmailVerified || isPhoneVerified
        
    val displayInitials: String
        get() = displayName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
}

/**
 * User profile information
 */
data class UserProfile(
    val displayName: String,
    val photoUrl: String? = null,
    val bio: String? = null,
    val farmDetails: FarmDetails? = null
)

/**
 * Farm details for farmers and enthusiasts
 */
data class FarmDetails(
    val farmName: String,
    val farmType: FarmType,
    val establishedYear: Int,
    val totalArea: Double, // in acres
    val fowlCapacity: Int,
    val specializations: List<String>,
    val certifications: List<String>
)

/**
 * Farm type enumeration
 */
enum class FarmType {
    COMMERCIAL,
    HOBBY,
    BREEDING,
    RESEARCH
}

/**
 * User preferences
 */
data class UserPreferences(
    val language: Language = Language.ENGLISH,
    val notifications: NotificationPreferences = NotificationPreferences(),
    val privacy: PrivacyPreferences = PrivacyPreferences(),
    val marketplace: MarketplacePreferences = MarketplacePreferences()
)

/**
 * Notification preferences
 */
data class NotificationPreferences(
    val email: Boolean = true,
    val sms: Boolean = false,
    val push: Boolean = true,
    val marketing: Boolean = false
)

/**
 * Privacy preferences
 */
data class PrivacyPreferences(
    val showLocation: Boolean = true,
    val showPhoneNumber: Boolean = false,
    val allowDirectMessages: Boolean = true,
    val profileVisibility: ProfileVisibility = ProfileVisibility.PUBLIC
)

/**
 * Profile visibility options
 */
enum class ProfileVisibility {
    PUBLIC,
    FARMERS_ONLY,
    ENTHUSIASTS_ONLY,
    PRIVATE
}

/**
 * Marketplace preferences
 */
data class MarketplacePreferences(
    val autoRenewListings: Boolean = false,
    val priceAlerts: Boolean = true,
    val favoriteBreeds: List<String> = emptyList(),
    val preferredRegions: List<String> = emptyList()
)

/**
 * User statistics
 */
data class UserStats(
    val totalFowls: Int = 0,
    val totalListings: Int = 0,
    val totalSales: Int = 0,
    val totalPurchases: Int = 0,
    val totalMessages: Int = 0,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val joinedDate: Date,
    val lastActivityDate: Date? = null
)

/**
 * User limits based on tier
 */
data class UserLimits(
    val maxListings: Int,
    val maxPhotosPerListing: Int,
    val maxBreedingRecords: Int,
    val dailyMessageLimit: Int,
    val maxFowlRegistrations: Int = Int.MAX_VALUE
) {
    companion object {
        fun forTier(tier: UserTier): UserLimits {
            return when (tier) {
                UserTier.GENERAL -> UserLimits(
                    maxListings = 0,
                    maxPhotosPerListing = 0,
                    maxBreedingRecords = 0,
                    dailyMessageLimit = 50,
                    maxFowlRegistrations = 0
                )
                UserTier.FARMER -> UserLimits(
                    maxListings = 50,
                    maxPhotosPerListing = 10,
                    maxBreedingRecords = 100,
                    dailyMessageLimit = 200,
                    maxFowlRegistrations = 500
                )
                UserTier.ENTHUSIAST -> UserLimits(
                    maxListings = 200,
                    maxPhotosPerListing = 25,
                    maxBreedingRecords = 500,
                    dailyMessageLimit = 1000,
                    maxFowlRegistrations = Int.MAX_VALUE
                )
            }
        }
    }
}

/**
 * User metadata
 */
data class UserMetadata(
    val deviceInfo: String? = null,
    val appVersion: String? = null,
    val registrationSource: String = "android",
    val referredBy: String? = null,
    val tierUpgradeHistory: List<TierUpgrade> = emptyList(),
    val lastSyncAt: Date? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

/**
 * Tier upgrade history
 */
data class TierUpgrade(
    val fromTier: UserTier,
    val toTier: UserTier,
    val upgradedAt: Date,
    val reason: String,
    val approvedBy: String? = null
)

/**
 * Authentication state
 */
sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val exception: Throwable) : AuthState()
}

/**
 * User registration data
 */
data class UserRegistration(
    val email: String,
    val password: String,
    val phoneNumber: String? = null,
    val displayName: String,
    val regionalInfo: RegionalInfo,
    val language: Language = Language.ENGLISH,
    val farmDetails: FarmDetails? = null,
    val agreedToTerms: Boolean = false
)

/**
 * User login data
 */
data class UserLogin(
    val email: String,
    val password: String,
    val rememberMe: Boolean = false
)

/**
 * Phone verification data
 */
data class PhoneVerification(
    val phoneNumber: String,
    val verificationCode: String
)

/**
 * Tier verification request
 */
data class TierVerificationRequest(
    val requestedTier: UserTier,
    val reason: String,
    val experience: String,
    val goals: String,
    val documents: List<VerificationDocument> = emptyList()
)

/**
 * Verification document
 */
data class VerificationDocument(
    val type: DocumentType,
    val fileName: String,
    val fileUrl: String,
    val uploadedAt: Date
)

/**
 * Document types for verification
 */
enum class DocumentType {
    IDENTITY_PROOF,
    FARM_OWNERSHIP,
    EXPERIENCE_CERTIFICATE,
    REFERENCE_LETTER,
    BUSINESS_LICENSE,
    OTHER
}

/**
 * User search criteria
 */
data class UserSearchCriteria(
    val query: String? = null,
    val tier: UserTier? = null,
    val region: String? = null,
    val district: String? = null,
    val specializations: List<String> = emptyList(),
    val verificationLevel: VerificationLevel? = null,
    val isActive: Boolean = true,
    val sortBy: UserSortBy = UserSortBy.LAST_ACTIVE,
    val sortOrder: SortOrder = SortOrder.DESC
)

/**
 * User sorting options
 */
enum class UserSortBy {
    LAST_ACTIVE,
    RATING,
    JOINED_DATE,
    TOTAL_SALES,
    DISPLAY_NAME
}

/**
 * Sort order
 */
enum class SortOrder {
    ASC,
    DESC
}

/**
 * User discovery result
 */
data class UserDiscoveryResult(
    val users: List<User>,
    val totalCount: Int,
    val hasMore: Boolean
)

/**
 * Profile update request
 */
data class ProfileUpdateRequest(
    val displayName: String? = null,
    val bio: String? = null,
    val photoUrl: String? = null,
    val farmDetails: FarmDetails? = null,
    val preferences: UserPreferences? = null,
    val regionalInfo: RegionalInfo? = null
)
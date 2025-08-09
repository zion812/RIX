package com.rio.rostry.user.domain.repository

import com.rio.rostry.core.common.model.*
import com.rio.rostry.user.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for user-related operations
 */
interface UserRepository {
    
    // Authentication
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun signInWithPhone(phoneNumber: String, verificationCode: String): Result<User>
    suspend fun signUpWithEmail(registration: UserRegistration): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun sendPhoneVerificationCode(phoneNumber: String): Result<Unit>
    suspend fun verifyPhoneNumber(phoneNumber: String, code: String): Result<Unit>
    
    // User state
    fun getCurrentUser(): Flow<User?>
    fun getCurrentUserTier(): StateFlow<UserTier?>
    fun isUserAuthenticated(): Boolean
    fun getCurrentUserId(): String?
    fun getAuthState(): StateFlow<AuthState>
    
    // Profile management
    suspend fun getUserProfile(userId: String): Result<User>
    suspend fun updateUserProfile(userId: String, updateRequest: ProfileUpdateRequest): Result<User>
    suspend fun uploadProfilePhoto(userId: String, imageData: ByteArray): Result<String>
    suspend fun deleteUserAccount(userId: String): Result<Unit>
    
    // Tier management
    suspend fun requestTierUpgrade(request: TierVerificationRequest): Result<Unit>
    suspend fun getTierUpgradeStatus(userId: String): Result<TierVerificationRequest?>
    suspend fun cancelTierUpgradeRequest(userId: String): Result<Unit>
    
    // User discovery
    suspend fun searchUsers(criteria: UserSearchCriteria, page: Int, pageSize: Int): Result<UserDiscoveryResult>
    suspend fun getNearbyUsers(latitude: Double, longitude: Double, radiusKm: Int): Result<List<User>>
    suspend fun getRecommendedUsers(userId: String): Result<List<User>>
    suspend fun getUsersByTier(tier: UserTier, region: String? = null): Result<List<User>>
    
    // User relationships
    suspend fun followUser(userId: String, targetUserId: String): Result<Unit>
    suspend fun unfollowUser(userId: String, targetUserId: String): Result<Unit>
    suspend fun getFollowers(userId: String): Result<List<User>>
    suspend fun getFollowing(userId: String): Result<List<User>>
    suspend fun blockUser(userId: String, targetUserId: String): Result<Unit>
    suspend fun unblockUser(userId: String, targetUserId: String): Result<Unit>
    suspend fun getBlockedUsers(userId: String): Result<List<User>>
    
    // User statistics
    suspend fun getUserStats(userId: String): Result<UserStats>
    suspend fun updateUserStats(userId: String, stats: UserStats): Result<Unit>
    suspend fun incrementUserActivity(userId: String, activity: String): Result<Unit>
    
    // Preferences
    suspend fun updateNotificationPreferences(userId: String, preferences: NotificationPreferences): Result<Unit>
    suspend fun updatePrivacyPreferences(userId: String, preferences: PrivacyPreferences): Result<Unit>
    suspend fun updateMarketplacePreferences(userId: String, preferences: MarketplacePreferences): Result<Unit>
    suspend fun updateLanguagePreference(userId: String, language: Language): Result<Unit>
    
    // Verification
    suspend fun uploadVerificationDocument(
        userId: String,
        documentType: DocumentType,
        fileName: String,
        fileData: ByteArray
    ): Result<String>
    suspend fun getVerificationDocuments(userId: String): Result<List<VerificationDocument>>
    suspend fun deleteVerificationDocument(userId: String, documentId: String): Result<Unit>
    
    // Regional features
    suspend fun getUsersByRegion(region: String, district: String? = null): Result<List<User>>
    suspend fun updateUserLocation(userId: String, coordinates: Coordinates): Result<Unit>
    suspend fun getRegionalExperts(region: String, specialization: String): Result<List<User>>
    
    // Offline support
    suspend fun cacheUserData(user: User): Result<Unit>
    suspend fun getCachedUserData(userId: String): Result<User?>
    suspend fun syncOfflineChanges(): Result<Unit>
    suspend fun clearUserCache(): Result<Unit>
    
    // Admin functions (for enthusiasts with permissions)
    suspend fun approveUserVerification(adminUserId: String, targetUserId: String, newTier: UserTier): Result<Unit>
    suspend fun rejectUserVerification(adminUserId: String, targetUserId: String, reason: String): Result<Unit>
    suspend fun getPendingVerifications(adminUserId: String): Result<List<TierVerificationRequest>>
    suspend fun moderateUser(adminUserId: String, targetUserId: String, action: ModerationAction): Result<Unit>
}

/**
 * Moderation actions for user management
 */
enum class ModerationAction {
    WARN,
    SUSPEND,
    BAN,
    RESTORE
}

/**
 * User activity types for analytics
 */
enum class UserActivity {
    LOGIN,
    PROFILE_UPDATE,
    FOWL_REGISTRATION,
    LISTING_CREATION,
    MESSAGE_SENT,
    MARKETPLACE_VIEW,
    SEARCH_PERFORMED
}

/**
 * User relationship types
 */
enum class UserRelationship {
    FOLLOWING,
    FOLLOWER,
    BLOCKED,
    NONE
}

/**
 * User online status
 */
enum class UserOnlineStatus {
    ONLINE,
    OFFLINE,
    AWAY,
    BUSY
}

/**
 * User verification request status
 */
enum class VerificationRequestStatus {
    PENDING,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    CANCELLED
}

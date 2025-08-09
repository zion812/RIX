package com.rio.rostry.auth

/**
 * Represents the user tier in the RIO platform
 */
enum class UserTier(val displayName: String) {
    GENERAL("General User"),
    FARMER("Farmer"),
    ENTHUSIAST("High-Level Enthusiast");

    companion object {
        fun fromString(value: String?): UserTier {
            return when (value?.lowercase()) {
                "general" -> GENERAL
                "farmer" -> FARMER
                "enthusiast" -> ENTHUSIAST
                else -> GENERAL
            }
        }
    }
}

/**
 * User permissions based on tier
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
)

/**
 * User verification status
 */
data class VerificationStatus(
    val level: String = "basic",
    val emailVerified: Boolean = false,
    val phoneVerified: Boolean = false,
    val identityVerified: Boolean = false,
    val farmDocumentsVerified: Boolean = false,
    val referencesVerified: Boolean = false,
    val verifiedAt: String? = null,
    val verifiedBy: String? = null
)

/**
 * User profile information
 */
data class UserProfile(
    val region: String = "other",
    val district: String = "",
    val language: String = "en",
    val farmType: String = "hobby",
    val experienceLevel: String = "beginner",
    val specializations: List<String> = emptyList()
)

/**
 * User limits based on tier
 */
data class UserLimits(
    val maxListings: Int = 0,
    val maxPhotosPerListing: Int = 0,
    val maxBreedingRecords: Int = 0,
    val dailyMessageLimit: Int = 10
)

/**
 * Complete user claims structure
 */
data class UserClaims(
    val tier: UserTier = UserTier.GENERAL,
    val permissions: UserPermissions = UserPermissions(),
    val verificationStatus: VerificationStatus = VerificationStatus(),
    val profile: UserProfile = UserProfile(),
    val limits: UserLimits = UserLimits()
) {
    companion object {
        fun fromMap(claims: Map<String, Any>?): UserClaims {
            if (claims == null) return UserClaims()

            return UserClaims(
                tier = UserTier.fromString(claims["tier"] as? String),
                permissions = parsePermissions(claims["permissions"] as? Map<String, Any>),
                verificationStatus = parseVerificationStatus(claims["verificationStatus"] as? Map<String, Any>),
                profile = parseProfile(claims["profile"] as? Map<String, Any>),
                limits = parseLimits(claims["limits"] as? Map<String, Any>)
            )
        }

        private fun parsePermissions(permissionsMap: Map<String, Any>?): UserPermissions {
            if (permissionsMap == null) return UserPermissions()

            return UserPermissions(
                canCreateListings = permissionsMap["canCreateListings"] as? Boolean ?: false,
                canEditListings = permissionsMap["canEditListings"] as? Boolean ?: false,
                canDeleteListings = permissionsMap["canDeleteListings"] as? Boolean ?: false,
                canAccessMarketplace = permissionsMap["canAccessMarketplace"] as? Boolean ?: true,
                canManageBreedingRecords = permissionsMap["canManageBreedingRecords"] as? Boolean ?: false,
                canAccessAnalytics = permissionsMap["canAccessAnalytics"] as? Boolean ?: false,
                canAccessPremiumFeatures = permissionsMap["canAccessPremiumFeatures"] as? Boolean ?: false,
                canVerifyTransfers = permissionsMap["canVerifyTransfers"] as? Boolean ?: false,
                canAccessPrioritySupport = permissionsMap["canAccessPrioritySupport"] as? Boolean ?: false,
                canModerateContent = permissionsMap["canModerateContent"] as? Boolean ?: false
            )
        }

        private fun parseVerificationStatus(statusMap: Map<String, Any>?): VerificationStatus {
            if (statusMap == null) return VerificationStatus()

            return VerificationStatus(
                level = statusMap["level"] as? String ?: "basic",
                emailVerified = statusMap["emailVerified"] as? Boolean ?: false,
                phoneVerified = statusMap["phoneVerified"] as? Boolean ?: false,
                identityVerified = statusMap["identityVerified"] as? Boolean ?: false,
                farmDocumentsVerified = statusMap["farmDocumentsVerified"] as? Boolean ?: false,
                referencesVerified = statusMap["referencesVerified"] as? Boolean ?: false,
                verifiedAt = statusMap["verifiedAt"] as? String,
                verifiedBy = statusMap["verifiedBy"] as? String
            )
        }

        private fun parseProfile(profileMap: Map<String, Any>?): UserProfile {
            if (profileMap == null) return UserProfile()

            return UserProfile(
                region = profileMap["region"] as? String ?: "other",
                district = profileMap["district"] as? String ?: "",
                language = profileMap["language"] as? String ?: "en",
                farmType = profileMap["farmType"] as? String ?: "hobby",
                experienceLevel = profileMap["experienceLevel"] as? String ?: "beginner",
                specializations = (profileMap["specializations"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            )
        }

        private fun parseLimits(limitsMap: Map<String, Any>?): UserLimits {
            if (limitsMap == null) return UserLimits()

            return UserLimits(
                maxListings = (limitsMap["maxListings"] as? Number)?.toInt() ?: 0,
                maxPhotosPerListing = (limitsMap["maxPhotosPerListing"] as? Number)?.toInt() ?: 0,
                maxBreedingRecords = (limitsMap["maxBreedingRecords"] as? Number)?.toInt() ?: 0,
                dailyMessageLimit = (limitsMap["dailyMessageLimit"] as? Number)?.toInt() ?: 10
            )
        }
    }
}
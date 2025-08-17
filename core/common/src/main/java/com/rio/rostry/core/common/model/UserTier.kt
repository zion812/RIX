package com.rio.rostry.core.common.model

/**
 * Represents different user tiers in the RIO platform
 */
enum class UserTier {
    GENERAL,    // Basic users with limited access
    FARMER,     // Farmers with full fowl management features
    ENTHUSIAST;  // Enthusiasts with advanced features and marketplace access

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
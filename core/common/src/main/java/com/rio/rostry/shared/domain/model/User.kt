package com.rio.rostry.shared.domain.model

import java.util.*

/**
 * Domain model for User
 */
data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val userTier: UserTier,
    val verificationStatus: VerificationStatus,
    val phoneNumber: String?,
    val profilePhoto: String?,
    val bio: String?,
    val region: String,
    val district: String,
    val farmName: String?,
    val specializations: String?,
    val rating: Double,
    val reviewCount: Int,
    val fowlCount: Int,
    val successfulTransactions: Int,
    val language: String,
    val lastActiveAt: Date?,
    val createdAt: Date,
    val updatedAt: Date
)

/**
 * User tier enumeration
 */
enum class UserTier {
    BASIC,
    PREMIUM,
    PROFESSIONAL,
    ENTERPRISE
}

/**
 * Verification status enumeration
 */
enum class VerificationStatus {
    UNVERIFIED,
    PENDING,
    VERIFIED,
    REJECTED
}

/**
 * Gender enumeration
 */
enum class Gender {
    MALE,
    FEMALE,
    UNKNOWN
}

/**
 * Age category enumeration
 */
enum class AgeCategory {
    CHICK,      // 0-8 weeks
    JUVENILE,   // 8-20 weeks
    ADULT,      // 20+ weeks
    SENIOR      // 3+ years
}

/**
 * Health status enumeration
 */
enum class HealthStatus {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    SICK,
    QUARANTINE
}

/**
 * Availability status enumeration
 */
enum class AvailabilityStatus {
    AVAILABLE,
    RESERVED,
    SOLD,
    NOT_FOR_SALE,
    BREEDING
}
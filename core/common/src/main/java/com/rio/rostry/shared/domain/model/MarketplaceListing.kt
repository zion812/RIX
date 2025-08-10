package com.rio.rostry.shared.domain.model

import java.util.*

/**
 * Domain model for MarketplaceListing
 */
data class MarketplaceListing(
    val id: String,
    val sellerId: String,
    val fowlId: String,
    val title: String,
    val description: String,
    val price: Double,
    val currency: String,
    val listingType: ListingType,
    val status: ListingStatus,
    val category: String,
    val breed: String,
    val gender: Gender,
    val age: String,
    val location: String,
    val photos: List<String>,
    val features: List<String>,
    val healthCertified: Boolean,
    val lineageVerified: Boolean,
    val negotiable: Boolean,
    val deliveryAvailable: Boolean,
    val deliveryRadius: Int?,
    val contactPreference: ContactPreference,
    val viewCount: Int,
    val favoriteCount: Int,
    val inquiryCount: Int,
    val region: String,
    val district: String,
    val expiresAt: Date?,
    val createdAt: Date,
    val updatedAt: Date
)

/**
 * Listing type enumeration
 */
enum class ListingType {
    SALE,
    BREEDING,
    STUD_SERVICE,
    EXCHANGE,
    AUCTION
}

/**
 * Listing status enumeration
 */
enum class ListingStatus {
    DRAFT,
    ACTIVE,
    SOLD,
    EXPIRED,
    SUSPENDED,
    DELETED
}

/**
 * Contact preference enumeration
 */
enum class ContactPreference {
    PHONE,
    MESSAGE,
    BOTH
}
package com.rio.rostry.shared.domain.model

import java.util.*

/**
 * Domain models for the RIO platform
 * Clean domain objects without database or network dependencies
 */

/**
 * User domain model
 */
data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val userTier: UserTier,
    val verificationStatus: VerificationStatus,
    val phoneNumber: String? = null,
    val profilePhoto: String? = null,
    val bio: String? = null,
    val region: String,
    val district: String,
    val farmName: String? = null,
    val specializations: List<String> = emptyList(),
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val fowlCount: Int = 0,
    val successfulTransactions: Int = 0,
    val language: String = "en",
    val lastActiveAt: Date? = null,
    val createdAt: Date,
    val updatedAt: Date
)

/**
 * Fowl domain model
 */
data class Fowl(
    val id: String,
    val ownerId: String,
    val name: String? = null,
    val breedPrimary: String,
    val breedSecondary: String? = null,
    val gender: Gender,
    val birthDate: Date? = null,
    val ageCategory: AgeCategory,
    val color: String,
    val weight: Double,
    val height: Double,
    val healthStatus: HealthStatus,
    val availabilityStatus: AvailabilityStatus,
    val fatherId: String? = null,
    val motherId: String? = null,
    val generation: Int = 1,
    val primaryPhoto: String? = null,
    val photos: List<String> = emptyList(),
    val registrationNumber: String? = null,
    val qrCode: String? = null,
    val notes: String? = null,
    val tags: List<String> = emptyList(),
    val region: String,
    val district: String,
    val createdAt: Date,
    val updatedAt: Date
)

/**
 * Marketplace listing domain model
 */
data class MarketplaceListing(
    val id: String,
    val sellerId: String,
    val fowlId: String,
    val title: String,
    val description: String,
    val listingType: ListingType,
    val basePrice: Double,
    val currentBid: Double? = null,
    val buyNowPrice: Double? = null,
    val listingStatus: ListingStatus,
    val breed: String,
    val gender: Gender,
    val age: String,
    val weight: Double,
    val healthStatus: HealthStatus,
    val primaryPhotoUrl: String? = null,
    val photos: List<String> = emptyList(),
    val deliveryAvailable: Boolean,
    val auctionStartTime: Date? = null,
    val auctionEndTime: Date? = null,
    val views: Int = 0,
    val favorites: Int = 0,
    val region: String,
    val district: String,
    val publishedAt: Date? = null,
    val expiresAt: Date? = null,
    val createdAt: Date,
    val updatedAt: Date
)

/**
 * Message domain model
 */
data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val messageType: MessageType,
    val textContent: String? = null,
    val mediaUrl: String? = null,
    val mediaCaption: String? = null,
    val cardId: String? = null,
    val cardData: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val replyToMessageId: String? = null,
    val forwarded: Boolean = false,
    val edited: Boolean = false,
    val sentAt: Date,
    val deliveryStatus: DeliveryStatus,
    val deliveredAt: Date? = null,
    val readAt: Date? = null,
    val reactions: Map<String, List<String>> = emptyMap()
)

/**
 * Conversation domain model
 */
data class Conversation(
    val id: String,
    val conversationType: ConversationType,
    val title: String? = null,
    val participants: List<String> = emptyList(),
    val lastMessageId: String? = null,
    val lastActivityAt: Date,
    val messageCount: Int = 0,
    val unreadCount: Int = 0,
    val relatedListingId: String? = null,
    val relatedFowlId: String? = null,
    val createdAt: Date,
    val updatedAt: Date
)

/**
 * Transfer domain model
 */
data class Transfer(
    val id: String,
    val fowlId: String,
    val fromUserId: String,
    val toUserId: String,
    val transferType: TransferType,
    val transferStatus: TransferStatus,
    val amount: Double? = null,
    val paymentStatus: PaymentStatus,
    val verificationRequired: Boolean = true,
    val verificationStatus: VerificationStatus,
    val deliveryAddress: String,
    val trackingNumber: String? = null,
    val transferNotes: String? = null,
    val relatedListingId: String? = null,
    val initiatedAt: Date,
    val completedAt: Date? = null,
    val region: String,
    val district: String
)

/**
 * Breeding record domain model
 */
data class BreedingRecord(
    val id: String,
    val sireId: String,
    val damId: String,
    val breederId: String,
    val breedingDate: Date,
    val expectedHatchDate: Date? = null,
    val actualHatchDate: Date? = null,
    val eggsLaid: Int = 0,
    val chicksHatched: Int = 0,
    val breedingMethod: BreedingMethod,
    val breedingPurpose: BreedingPurpose,
    val offspringIds: List<String> = emptyList(),
    val notes: String? = null,
    val region: String,
    val district: String,
    val createdAt: Date
)

/**
 * Enumerations for domain models
 */
enum class UserTier {
    GENERAL, FARMER, ENTHUSIAST
}

enum class VerificationStatus {
    PENDING, VERIFIED, REJECTED
}

enum class Gender {
    MALE, FEMALE, UNKNOWN
}

enum class AgeCategory {
    CHICK, JUVENILE, ADULT, SENIOR
}

enum class HealthStatus {
    EXCELLENT, GOOD, FAIR, POOR, SICK, DECEASED
}

enum class AvailabilityStatus {
    AVAILABLE, SOLD, BREEDING, DECEASED, MISSING, RESERVED
}

enum class ListingType {
    FIXED_PRICE, AUCTION, NEGOTIABLE, BREEDING_SERVICE
}

enum class ListingStatus {
    DRAFT, ACTIVE, PAUSED, SOLD, EXPIRED, CANCELLED
}

enum class MessageType {
    TEXT, IMAGE, VIDEO, AUDIO, FILE, LOCATION, FOWL_CARD, LISTING_CARD, CONTACT, SYSTEM
}

enum class DeliveryStatus {
    PENDING, SENT, DELIVERED, READ, FAILED
}

enum class ConversationType {
    DIRECT, GROUP, MARKETPLACE, BREEDING, SUPPORT
}

enum class TransferType {
    SALE, GIFT, INHERITANCE, BREEDING_LOAN, TRADE, RETURN
}

enum class TransferStatus {
    INITIATED, PENDING_APPROVAL, APPROVED, IN_TRANSIT, COMPLETED, CANCELLED, REJECTED
}

enum class PaymentStatus {
    PENDING, PAID, FAILED, REFUNDED
}

enum class BreedingMethod {
    NATURAL, ARTIFICIAL_INSEMINATION
}

enum class BreedingPurpose {
    IMPROVEMENT, COMMERCIAL, EXHIBITION
}

/**
 * Search and filter models
 */
data class FowlSearchCriteria(
    val region: String? = null,
    val district: String? = null,
    val breed: String? = null,
    val gender: Gender? = null,
    val ageCategory: AgeCategory? = null,
    val minWeight: Double? = null,
    val maxWeight: Double? = null,
    val healthStatus: HealthStatus? = null,
    val availabilityStatus: AvailabilityStatus? = null,
    val ownerId: String? = null,
    val searchQuery: String? = null,
    val tags: List<String> = emptyList()
)

data class MarketplaceSearchCriteria(
    val region: String? = null,
    val district: String? = null,
    val breed: String? = null,
    val gender: Gender? = null,
    val listingType: ListingType? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val deliveryAvailable: Boolean? = null,
    val searchQuery: String? = null,
    val sortBy: MarketplaceSortBy = MarketplaceSortBy.NEWEST
)

enum class MarketplaceSortBy {
    NEWEST, OLDEST, PRICE_LOW_TO_HIGH, PRICE_HIGH_TO_LOW, MOST_VIEWED, ENDING_SOON
}

/**
 * Pagination models
 */
data class PageRequest(
    val limit: Int = 50,
    val offset: Int = 0,
    val cursor: String? = null
)

data class PageResult<T>(
    val items: List<T>,
    val hasMore: Boolean,
    val nextCursor: String? = null,
    val totalCount: Int? = null
)

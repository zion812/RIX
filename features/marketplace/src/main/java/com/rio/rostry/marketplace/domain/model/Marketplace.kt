package com.rio.rostry.marketplace.domain.model

import com.rio.rostry.core.common.model.*
import com.rio.rostry.fowl.domain.model.Fowl
import java.util.*

/**
 * Domain model representing a marketplace listing
 */
data class MarketplaceListing(
    val id: String,
    val sellerId: String,
    val fowlId: String,
    val listingType: ListingType,
    val pricing: PricingInfo,
    val details: ListingDetails,
    val media: ListingMedia,
    val location: ListingLocation,
    val auction: AuctionInfo? = null,
    val status: ListingStatus,
    val engagement: EngagementMetrics,
    val searchData: SearchData,
    val timeline: ListingTimeline,
    val sellerPreferences: SellerPreferences,
    val metadata: ListingMetadata,
    val createdAt: Date,
    val updatedAt: Date
)

/**
 * Listing types
 */
enum class ListingType {
    FIXED_PRICE,
    AUCTION,
    NEGOTIABLE,
    BREEDING_SERVICE
}

/**
 * Pricing information
 */
data class PricingInfo(
    val basePrice: Double,
    val currency: String = "INR",
    val reservePrice: Double? = null, // For auctions
    val buyNowPrice: Double? = null, // Instant purchase price
    val currentBid: Double? = null, // Current highest bid
    val bidIncrement: Double? = null, // Minimum bid increment
    val breedingFee: Double? = null, // For breeding services
    val studFee: Double? = null // Stud service fee
)

/**
 * Listing details
 */
data class ListingDetails(
    val title: String,
    val description: String,
    val highlights: List<String> = emptyList(),
    val breed: String,
    val gender: String,
    val age: String,
    val weight: Double,
    val color: String,
    val healthStatus: String,
    val vaccinated: Boolean,
    val healthCertificate: String? = null,
    val pedigreeAvailable: Boolean,
    val registrationPapers: Boolean
)

/**
 * Listing media
 */
data class ListingMedia(
    val photos: List<ListingPhoto> = emptyList(),
    val videos: List<ListingVideo> = emptyList(),
    val primaryPhotoUrl: String? = null
)

/**
 * Listing photo
 */
data class ListingPhoto(
    val url: String,
    val caption: String? = null,
    val isPrimary: Boolean = false,
    val uploadedAt: Date
)

/**
 * Listing video
 */
data class ListingVideo(
    val url: String,
    val thumbnail: String? = null,
    val duration: Int? = null, // in seconds
    val caption: String? = null,
    val uploadedAt: Date
)

/**
 * Listing location
 */
data class ListingLocation(
    val district: String,
    val region: String,
    val exactLocation: Coordinates? = null,
    val delivery: DeliveryInfo
)

/**
 * Delivery information
 */
data class DeliveryInfo(
    val available: Boolean,
    val radius: Int, // in km
    val cost: Double,
    val methods: List<DeliveryMethod>
)

/**
 * Delivery methods
 */
enum class DeliveryMethod {
    PICKUP,
    LOCAL_DELIVERY,
    COURIER,
    TRANSPORT
}

/**
 * Auction information
 */
data class AuctionInfo(
    val startTime: Date,
    val endTime: Date,
    val autoExtend: Boolean,
    val extensionTime: Int, // minutes
    val minimumBidders: Int,
    val currentHighestBid: BidInfo? = null,
    val bidHistory: List<BidInfo> = emptyList(),
    val totalBids: Int = 0
)

/**
 * Bid information
 */
data class BidInfo(
    val id: String,
    val bidderId: String,
    val amount: Double,
    val bidTime: Date,
    val isWinning: Boolean = false,
    val isProxy: Boolean = false,
    val maxProxyAmount: Double? = null
)

/**
 * Listing status
 */
data class ListingStatus(
    val current: ListingState,
    val visibility: ListingVisibility,
    val featured: Boolean = false,
    val promoted: Boolean = false,
    val availability: AvailabilityInfo
)

/**
 * Listing states
 */
enum class ListingState {
    DRAFT,
    ACTIVE,
    PAUSED,
    SOLD,
    EXPIRED,
    CANCELLED
}

/**
 * Listing visibility
 */
enum class ListingVisibility {
    PUBLIC,
    PRIVATE,
    TIER_RESTRICTED
}

/**
 * Availability information
 */
data class AvailabilityInfo(
    val isAvailable: Boolean,
    val reservedBy: String? = null,
    val reservedUntil: Date? = null,
    val soldTo: String? = null,
    val soldAt: Date? = null
)

/**
 * Engagement metrics
 */
data class EngagementMetrics(
    val views: Int = 0,
    val uniqueViews: Int = 0,
    val favorites: Int = 0,
    val shares: Int = 0,
    val inquiries: Int = 0,
    val contactClicks: Int = 0,
    val photoViews: Int = 0
)

/**
 * Search data
 */
data class SearchData(
    val keywords: List<String>,
    val tags: List<String>,
    val category: String,
    val subcategory: String? = null,
    val filters: SearchFilters
)

/**
 * Search filters
 */
data class SearchFilters(
    val priceRange: String,
    val ageGroup: String,
    val breedGroup: String,
    val location: String
)

/**
 * Listing timeline
 */
data class ListingTimeline(
    val createdAt: Date,
    val publishedAt: Date? = null,
    val lastUpdatedAt: Date,
    val expiresAt: Date? = null,
    val soldAt: Date? = null,
    val renewalCount: Int = 0,
    val lastRenewedAt: Date? = null,
    val autoRenew: Boolean = false
)

/**
 * Seller preferences
 */
data class SellerPreferences(
    val acceptOffers: Boolean = true,
    val minimumOffer: Double? = null,
    val preferredBuyers: List<String> = emptyList(),
    val blacklistedBuyers: List<String> = emptyList(),
    val communication: CommunicationPreferences
)

/**
 * Communication preferences
 */
data class CommunicationPreferences(
    val allowDirectMessages: Boolean = true,
    val allowPhoneCalls: Boolean = false,
    val preferredContactMethod: ContactMethod = ContactMethod.MESSAGE,
    val responseTime: String = "Within 24 hours"
)

/**
 * Contact methods
 */
enum class ContactMethod {
    MESSAGE,
    PHONE,
    EMAIL
}

/**
 * Listing metadata
 */
data class ListingMetadata(
    val source: String = "mobile",
    val listingQuality: DataQuality,
    val moderationStatus: ModerationStatus,
    val moderatedBy: String? = null,
    val moderatedAt: Date? = null,
    val conversionRate: Double? = null,
    val averageResponseTime: Double? = null // in hours
)

/**
 * Moderation status
 */
enum class ModerationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    FLAGGED
}

/**
 * Marketplace search criteria
 */
data class MarketplaceSearchCriteria(
    val query: String? = null,
    val breed: String? = null,
    val gender: String? = null,
    val region: String? = null,
    val district: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val listingType: ListingType? = null,
    val ageCategory: String? = null,
    val availability: ListingState? = null,
    val bloodline: String? = null,
    val healthStatus: String? = null,
    val vaccinated: Boolean? = null,
    val pedigreeAvailable: Boolean? = null,
    val deliveryAvailable: Boolean? = null,
    val sortBy: MarketplaceSortBy = MarketplaceSortBy.PUBLISHED_AT,
    val sortOrder: SortOrder = SortOrder.DESC,
    val radius: Int? = null, // Search radius in km
    val userLocation: Coordinates? = null
)

/**
 * Marketplace sorting options
 */
enum class MarketplaceSortBy {
    PUBLISHED_AT,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    DISTANCE,
    POPULARITY,
    ENDING_SOON, // For auctions
    NEWEST_FIRST,
    MOST_VIEWED
}

/**
 * Marketplace search result
 */
data class MarketplaceSearchResult(
    val listings: List<MarketplaceListing>,
    val totalCount: Int,
    val hasMore: Boolean,
    val facets: SearchFacets? = null
)

/**
 * Search facets for filtering
 */
data class SearchFacets(
    val breeds: Map<String, Int>,
    val priceRanges: Map<String, Int>,
    val locations: Map<String, Int>,
    val ages: Map<String, Int>,
    val genders: Map<String, Int>
)

/**
 * Listing creation request
 */
data class ListingCreateRequest(
    val fowlId: String,
    val listingType: ListingType,
    val pricing: PricingInfo,
    val details: ListingDetails,
    val location: ListingLocation,
    val auction: AuctionInfo? = null,
    val sellerPreferences: SellerPreferences,
    val autoPublish: Boolean = true
)

/**
 * Listing update request
 */
data class ListingUpdateRequest(
    val pricing: PricingInfo? = null,
    val details: ListingDetails? = null,
    val status: ListingState? = null,
    val sellerPreferences: SellerPreferences? = null
)

/**
 * Bid placement request
 */
data class BidPlacementRequest(
    val listingId: String,
    val amount: Double,
    val isProxy: Boolean = false,
    val maxProxyAmount: Double? = null
)

/**
 * Offer request
 */
data class OfferRequest(
    val listingId: String,
    val amount: Double,
    val message: String? = null,
    val validUntil: Date? = null
)

/**
 * Watchlist item
 */
data class WatchlistItem(
    val id: String,
    val userId: String,
    val listingId: String,
    val addedAt: Date,
    val priceAlert: PriceAlert? = null,
    val notifications: WatchlistNotifications
)

/**
 * Price alert
 */
data class PriceAlert(
    val enabled: Boolean,
    val targetPrice: Double,
    val alertType: PriceAlertType
)

/**
 * Price alert types
 */
enum class PriceAlertType {
    PRICE_DROP,
    PRICE_BELOW,
    AUCTION_ENDING
}

/**
 * Watchlist notifications
 */
data class WatchlistNotifications(
    val priceChanges: Boolean = true,
    val statusChanges: Boolean = true,
    val auctionUpdates: Boolean = true,
    val newPhotos: Boolean = false
)

/**
 * Transaction information
 */
data class MarketplaceTransaction(
    val id: String,
    val listingId: String,
    val buyerId: String,
    val sellerId: String,
    val fowlId: String,
    val amount: Double,
    val transactionType: TransactionType,
    val status: TransactionStatus,
    val paymentMethod: PaymentMethod? = null,
    val deliveryInfo: TransactionDeliveryInfo? = null,
    val timeline: TransactionTimeline,
    val metadata: TransactionMetadata
)

/**
 * Transaction types
 */
enum class TransactionType {
    PURCHASE,
    AUCTION_WIN,
    OFFER_ACCEPTED,
    BREEDING_SERVICE
}

/**
 * Transaction status
 */
enum class TransactionStatus {
    PENDING,
    PAYMENT_PENDING,
    PAID,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    DISPUTED
}

/**
 * Payment methods
 */
enum class PaymentMethod {
    CASH,
    BANK_TRANSFER,
    UPI,
    CHEQUE,
    BARTER
}

/**
 * Transaction delivery info
 */
data class TransactionDeliveryInfo(
    val method: DeliveryMethod,
    val address: String? = null,
    val scheduledDate: Date? = null,
    val deliveredDate: Date? = null,
    val trackingNumber: String? = null
)

/**
 * Transaction timeline
 */
data class TransactionTimeline(
    val createdAt: Date,
    val paymentDueAt: Date? = null,
    val paidAt: Date? = null,
    val shippedAt: Date? = null,
    val deliveredAt: Date? = null,
    val completedAt: Date? = null
)

/**
 * Transaction metadata
 */
data class TransactionMetadata(
    val source: String,
    val fees: TransactionFees? = null,
    val notes: String? = null
)

/**
 * Transaction fees
 */
data class TransactionFees(
    val platformFee: Double = 0.0,
    val paymentFee: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val totalFees: Double = 0.0
)

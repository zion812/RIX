package com.rio.rostry.ui.marketplace.models

import java.util.*

/**
 * Order models for marketplace transactions
 */

data class MarketplaceOrder(
    val id: String,
    val listingId: String,
    val fowlId: String,
    val buyerId: String,
    val sellerId: String,
    val fowlName: String,
    val fowlBreed: String,
    val price: Double,
    val orderType: OrderType,
    val paymentMethod: PaymentMethod,
    val deliveryMethod: DeliveryMethod,
    val status: OrderStatus,
    val buyerInfo: BuyerInfo,
    val sellerInfo: SellerInfo,
    val deliveryInfo: DeliveryInfo? = null,
    val timeline: OrderTimeline,
    val notes: String? = null,
    val createdAt: Date,
    val updatedAt: Date
)

enum class OrderType {
    DIRECT_PURCHASE,
    NEGOTIATED_PRICE,
    ADVANCE_BOOKING
}

enum class PaymentMethod {
    CASH_ON_DELIVERY,
    ADVANCE_PAYMENT,
    PARTIAL_ADVANCE,
    BANK_TRANSFER,
    UPI_PAYMENT
}

enum class DeliveryMethod {
    PICKUP_FROM_FARM,
    DELIVERY_TO_BUYER,
    MEET_HALFWAY,
    TRANSPORT_SERVICE
}

enum class OrderStatus {
    PENDING_SELLER_APPROVAL,
    APPROVED_BY_SELLER,
    PAYMENT_PENDING,
    PAYMENT_RECEIVED,
    READY_FOR_PICKUP,
    IN_TRANSIT,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    DISPUTED
}

data class BuyerInfo(
    val id: String,
    val name: String,
    val phone: String,
    val location: String,
    val verificationBadge: Boolean = false
)

data class SellerInfo(
    val id: String,
    val name: String,
    val phone: String,
    val farmName: String?,
    val location: String,
    val verificationBadge: Boolean = false,
    val rating: Float = 0f
)

data class DeliveryInfo(
    val address: String,
    val landmark: String? = null,
    val preferredTime: String? = null,
    val specialInstructions: String? = null,
    val estimatedDeliveryDate: Date? = null,
    val actualDeliveryDate: Date? = null
)

data class OrderTimeline(
    val orderPlaced: Date,
    val sellerResponse: Date? = null,
    val paymentDue: Date? = null,
    val paymentReceived: Date? = null,
    val readyForPickup: Date? = null,
    val shipped: Date? = null,
    val delivered: Date? = null,
    val completed: Date? = null
)

/**
 * Order creation request
 */
data class CreateOrderRequest(
    val listingId: String,
    val fowlId: String,
    val sellerId: String,
    val orderType: OrderType,
    val paymentMethod: PaymentMethod,
    val deliveryMethod: DeliveryMethod,
    val proposedPrice: Double? = null, // For negotiated orders
    val deliveryInfo: DeliveryInfo? = null,
    val notes: String? = null
)

/**
 * Communication between buyer and seller
 */
data class OrderMessage(
    val id: String,
    val orderId: String,
    val senderId: String,
    val senderType: MessageSenderType,
    val message: String,
    val messageType: MessageType = MessageType.TEXT,
    val timestamp: Date,
    val isRead: Boolean = false
)

enum class MessageSenderType {
    BUYER,
    SELLER,
    SYSTEM
}

enum class MessageType {
    TEXT,
    PRICE_NEGOTIATION,
    DELIVERY_UPDATE,
    PAYMENT_CONFIRMATION,
    SYSTEM_NOTIFICATION
}

/**
 * Verification badge information
 */
data class VerificationBadge(
    val isVerified: Boolean,
    val verificationLevel: VerificationLevel,
    val verifiedDate: Date? = null,
    val badgeText: String
)

enum class VerificationLevel {
    BASIC,      // Email + Phone verified
    FARMER,     // + Farm documents verified
    PREMIUM     // + Site visit completed
}

/**
 * Order statistics for sellers
 */
data class SellerOrderStats(
    val totalOrders: Int,
    val completedOrders: Int,
    val averageRating: Float,
    val responseTime: String, // e.g., "Usually responds within 2 hours"
    val completionRate: Float // Percentage of orders completed successfully
)

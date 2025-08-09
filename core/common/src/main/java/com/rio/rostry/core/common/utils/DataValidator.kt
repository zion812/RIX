package com.rio.rostry.core.common.utils

import com.rio.rostry.core.database.entities.*
import com.rio.rostry.core.sync.OfflineAction
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data validator for ensuring data integrity before sync operations
 * Implements comprehensive validation rules for all entity types
 */
@Singleton
class DataValidator @Inject constructor() {
    
    /**
     * Validate syncable entity before sync
     */
    fun validate(entity: SyncableEntity): Boolean {
        return when (entity) {
            is UserEntity -> validateUser(entity)
            is FowlEntity -> validateFowl(entity)
            is MarketplaceEntity -> validateMarketplaceListing(entity)
            is MessageEntity -> validateMessage(entity)
            is TransferEntity -> validateTransfer(entity)
            else -> false
        }
    }
    
    /**
     * Validate offline action
     */
    fun validateOfflineAction(action: OfflineAction): Boolean {
        return validateBasicAction(action) && validateActionData(action)
    }
    
    /**
     * Validate user entity
     */
    private fun validateUser(user: UserEntity): Boolean {
        return validateRequired(user.id, "User ID") &&
                validateEmail(user.email) &&
                validateRequired(user.displayName, "Display name") &&
                validateUserTier(user.userTier) &&
                validateRegionalMetadata(user.regionalMetadata) &&
                validatePhoneNumber(user.phoneNumber) &&
                validateUserSpecificFields(user)
    }
    
    /**
     * Validate fowl entity
     */
    private fun validateFowl(fowl: FowlEntity): Boolean {
        return validateRequired(fowl.id, "Fowl ID") &&
                validateRequired(fowl.ownerId, "Owner ID") &&
                validateRequired(fowl.breedPrimary, "Primary breed") &&
                validateGender(fowl.gender) &&
                validateAgeCategory(fowl.ageCategory) &&
                validateHealthStatus(fowl.healthStatus) &&
                validateAvailabilityStatus(fowl.availabilityStatus) &&
                validateWeight(fowl.weight) &&
                validateHeight(fowl.height) &&
                validateRegionalMetadata(fowl.regionalMetadata) &&
                validateFowlSpecificFields(fowl)
    }
    
    /**
     * Validate marketplace listing
     */
    private fun validateMarketplaceListing(listing: MarketplaceEntity): Boolean {
        return validateRequired(listing.id, "Listing ID") &&
                validateRequired(listing.sellerId, "Seller ID") &&
                validateRequired(listing.fowlId, "Fowl ID") &&
                validateRequired(listing.title, "Title") &&
                validateRequired(listing.description, "Description") &&
                validateListingType(listing.listingType) &&
                validateListingStatus(listing.listingStatus) &&
                validatePrice(listing.basePrice) &&
                validateCurrency(listing.currency) &&
                validateRegionalMetadata(listing.regionalMetadata) &&
                validateListingSpecificFields(listing)
    }
    
    /**
     * Validate message entity
     */
    private fun validateMessage(message: MessageEntity): Boolean {
        return validateRequired(message.id, "Message ID") &&
                validateRequired(message.conversationId, "Conversation ID") &&
                validateRequired(message.senderId, "Sender ID") &&
                validateMessageType(message.messageType) &&
                validateMessageContent(message) &&
                validateDeliveryStatus(message.deliveryStatus) &&
                validateMessageSpecificFields(message)
    }
    
    /**
     * Validate transfer entity
     */
    private fun validateTransfer(transfer: TransferEntity): Boolean {
        return validateRequired(transfer.id, "Transfer ID") &&
                validateRequired(transfer.fowlId, "Fowl ID") &&
                validateRequired(transfer.fromUserId, "From user ID") &&
                validateRequired(transfer.toUserId, "To user ID") &&
                validateTransferType(transfer.transferType) &&
                validateTransferStatus(transfer.transferStatus) &&
                validatePaymentStatus(transfer.paymentStatus) &&
                validateTransferAmount(transfer.amount) &&
                validateRegionalMetadata(transfer.regionalMetadata) &&
                validateTransferSpecificFields(transfer)
    }
    
    /**
     * Validate basic action structure
     */
    private fun validateBasicAction(action: OfflineAction): Boolean {
        return validateRequired(action.id, "Action ID") &&
                validateRequired(action.entityType, "Entity type") &&
                validateRequired(action.entityId, "Entity ID") &&
                validateRequired(action.actionData, "Action data") &&
                action.maxRetries > 0
    }
    
    /**
     * Validate action data based on type
     */
    private fun validateActionData(action: OfflineAction): Boolean {
        return try {
            // Parse JSON data to validate structure
            val jsonData = JsonUtils.parseJson(action.actionData)
            jsonData != null && jsonData.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Validate required field
     */
    private fun validateRequired(value: String?, fieldName: String): Boolean {
        return !value.isNullOrBlank()
    }
    
    /**
     * Validate email format
     */
    private fun validateEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(emailRegex.toRegex())
    }
    
    /**
     * Validate phone number (Indian format)
     */
    private fun validatePhoneNumber(phoneNumber: String?): Boolean {
        if (phoneNumber.isNullOrBlank()) return true // Optional field
        
        // Indian phone number format: +91XXXXXXXXXX or XXXXXXXXXX
        val phoneRegex = "^(\\+91)?[6-9]\\d{9}$"
        return phoneNumber.matches(phoneRegex.toRegex())
    }
    
    /**
     * Validate user tier
     */
    private fun validateUserTier(tier: String): Boolean {
        return tier in listOf("GENERAL", "FARMER", "ENTHUSIAST")
    }
    
    /**
     * Validate gender
     */
    private fun validateGender(gender: String): Boolean {
        return gender in listOf("MALE", "FEMALE", "UNKNOWN")
    }
    
    /**
     * Validate age category
     */
    private fun validateAgeCategory(ageCategory: String): Boolean {
        return ageCategory in listOf("CHICK", "JUVENILE", "ADULT", "SENIOR")
    }
    
    /**
     * Validate health status
     */
    private fun validateHealthStatus(healthStatus: String): Boolean {
        return healthStatus in listOf("EXCELLENT", "GOOD", "FAIR", "POOR", "SICK", "DECEASED")
    }
    
    /**
     * Validate availability status
     */
    private fun validateAvailabilityStatus(availabilityStatus: String): Boolean {
        return availabilityStatus in listOf("AVAILABLE", "SOLD", "BREEDING", "DECEASED", "MISSING", "RESERVED")
    }
    
    /**
     * Validate weight (in kg)
     */
    private fun validateWeight(weight: Double): Boolean {
        return weight > 0 && weight <= 50 // Reasonable range for poultry
    }
    
    /**
     * Validate height (in cm)
     */
    private fun validateHeight(height: Double): Boolean {
        return height > 0 && height <= 200 // Reasonable range for poultry
    }
    
    /**
     * Validate listing type
     */
    private fun validateListingType(listingType: String): Boolean {
        return listingType in listOf("FIXED_PRICE", "AUCTION", "NEGOTIABLE", "BREEDING_SERVICE")
    }
    
    /**
     * Validate listing status
     */
    private fun validateListingStatus(listingStatus: String): Boolean {
        return listingStatus in listOf("DRAFT", "ACTIVE", "PAUSED", "SOLD", "EXPIRED", "CANCELLED")
    }
    
    /**
     * Validate price
     */
    private fun validatePrice(price: Double): Boolean {
        return price >= 0 && price <= 10000000 // Reasonable range in INR
    }
    
    /**
     * Validate currency
     */
    private fun validateCurrency(currency: String): Boolean {
        return currency == "INR" // Only INR supported for now
    }
    
    /**
     * Validate message type
     */
    private fun validateMessageType(messageType: String): Boolean {
        return messageType in listOf(
            "TEXT", "IMAGE", "VIDEO", "AUDIO", "FILE", "LOCATION", 
            "FOWL_CARD", "LISTING_CARD", "CONTACT", "SYSTEM"
        )
    }
    
    /**
     * Validate message content based on type
     */
    private fun validateMessageContent(message: MessageEntity): Boolean {
        return when (message.messageType) {
            "TEXT" -> !message.textContent.isNullOrBlank()
            "IMAGE", "VIDEO", "AUDIO", "FILE" -> !message.mediaUrl.isNullOrBlank()
            "LOCATION" -> message.latitude != null && message.longitude != null
            "FOWL_CARD", "LISTING_CARD" -> !message.cardId.isNullOrBlank() && !message.cardData.isNullOrBlank()
            "CONTACT" -> !message.contactName.isNullOrBlank() && !message.contactPhone.isNullOrBlank()
            "SYSTEM" -> !message.systemType.isNullOrBlank()
            else -> false
        }
    }
    
    /**
     * Validate delivery status
     */
    private fun validateDeliveryStatus(deliveryStatus: String): Boolean {
        return deliveryStatus in listOf("PENDING", "SENT", "DELIVERED", "READ", "FAILED")
    }
    
    /**
     * Validate transfer type
     */
    private fun validateTransferType(transferType: String): Boolean {
        return transferType in listOf("SALE", "GIFT", "INHERITANCE", "BREEDING_LOAN", "TRADE", "RETURN")
    }
    
    /**
     * Validate transfer status
     */
    private fun validateTransferStatus(transferStatus: String): Boolean {
        return transferStatus in listOf(
            "INITIATED", "PENDING_APPROVAL", "APPROVED", "IN_TRANSIT", 
            "COMPLETED", "CANCELLED", "REJECTED"
        )
    }
    
    /**
     * Validate payment status
     */
    private fun validatePaymentStatus(paymentStatus: String): Boolean {
        return paymentStatus in listOf("PENDING", "PAID", "FAILED", "REFUNDED")
    }
    
    /**
     * Validate transfer amount
     */
    private fun validateTransferAmount(amount: Double?): Boolean {
        return amount == null || (amount >= 0 && amount <= 10000000)
    }
    
    /**
     * Validate regional metadata
     */
    private fun validateRegionalMetadata(metadata: RegionalMetadata): Boolean {
        return validateRequired(metadata.region, "Region") &&
                validateRequired(metadata.district, "District") &&
                validateCoordinates(metadata.latitude, metadata.longitude)
    }
    
    /**
     * Validate coordinates
     */
    private fun validateCoordinates(latitude: Double?, longitude: Double?): Boolean {
        if (latitude == null || longitude == null) return true // Optional
        
        return latitude >= -90 && latitude <= 90 &&
                longitude >= -180 && longitude <= 180
    }
    
    /**
     * Validate user-specific fields
     */
    private fun validateUserSpecificFields(user: UserEntity): Boolean {
        // Additional user validation logic
        return when (user.userTier) {
            "FARMER" -> validateFarmerFields(user)
            "ENTHUSIAST" -> validateEnthusiastFields(user)
            else -> true
        }
    }
    
    /**
     * Validate farmer-specific fields
     */
    private fun validateFarmerFields(user: UserEntity): Boolean {
        // Farmers should have farm information
        return !user.farmName.isNullOrBlank() || user.verificationStatus != "VERIFIED"
    }
    
    /**
     * Validate enthusiast-specific fields
     */
    private fun validateEnthusiastFields(user: UserEntity): Boolean {
        // Enthusiasts should have organization or credentials
        return !user.organization.isNullOrBlank() || 
                user.professionalCredentials.isNotEmpty() ||
                user.verificationStatus != "VERIFIED"
    }
    
    /**
     * Validate fowl-specific fields
     */
    private fun validateFowlSpecificFields(fowl: FowlEntity): Boolean {
        // Validate lineage consistency
        if (fowl.fatherId != null && fowl.motherId != null && fowl.fatherId == fowl.motherId) {
            return false // Same parent for both father and mother
        }
        
        // Validate generation consistency
        if (fowl.generation < 1) {
            return false
        }
        
        return true
    }
    
    /**
     * Validate listing-specific fields
     */
    private fun validateListingSpecificFields(listing: MarketplaceEntity): Boolean {
        // Validate auction fields if auction type
        if (listing.listingType == "AUCTION") {
            return listing.auctionStartTime != null && 
                    listing.auctionEndTime != null &&
                    listing.auctionEndTime!!.after(listing.auctionStartTime)
        }
        
        // Validate expiry date
        if (listing.expiresAt != null && listing.expiresAt!!.before(Date())) {
            return false // Already expired
        }
        
        return true
    }
    
    /**
     * Validate message-specific fields
     */
    private fun validateMessageSpecificFields(message: MessageEntity): Boolean {
        // Validate reply chain
        if (message.replyToMessageId != null && message.replyToMessageId == message.id) {
            return false // Cannot reply to self
        }
        
        // Validate expiry
        if (message.expiresAt != null && message.expiresAt!!.before(Date())) {
            return false // Already expired
        }
        
        return true
    }
    
    /**
     * Validate transfer-specific fields
     */
    private fun validateTransferSpecificFields(transfer: TransferEntity): Boolean {
        // Cannot transfer to self
        if (transfer.fromUserId == transfer.toUserId) {
            return false
        }
        
        // Validate timeline consistency
        if (transfer.completedAt != null && transfer.initiatedAt.after(transfer.completedAt)) {
            return false
        }
        
        return true
    }
}

    /**
     * Validate fowl action data
     */
    fun validateFowlData(fowlData: Any): Boolean {
        // Implementation would validate fowl-specific business rules
        return true
    }

    /**
     * Validate marketplace action data
     */
    fun validateMarketplaceData(marketplaceData: Any): Boolean {
        // Implementation would validate marketplace-specific business rules
        return true
    }

    /**
     * Validate message action data
     */
    fun validateMessageData(messageData: Any): Boolean {
        // Implementation would validate message-specific business rules
        return true
    }

    /**
     * Calculate age in months from birth date
     */
    private fun calculateAgeInMonths(birthDate: Date): Int {
        val now = Date()
        val diffInMillis = now.time - birthDate.time
        return (diffInMillis / (30L * 24 * 60 * 60 * 1000)).toInt() // Approximate months
    }

    /**
     * Determine age category based on age in months
     */
    private fun determineAgeCategory(ageInMonths: Int): AgeCategory {
        return when {
            ageInMonths < 2 -> AgeCategory.CHICK
            ageInMonths < 6 -> AgeCategory.JUVENILE
            ageInMonths < 60 -> AgeCategory.ADULT
            else -> AgeCategory.SENIOR
        }
    }

    /**
     * Validate weight for breed and age
     */
    private fun isValidWeightForBreedAndAge(weight: Double, breed: String, ageCategory: String): Boolean {
        // Simplified validation - would have breed-specific weight ranges
        return when (ageCategory) {
            "CHICK" -> weight >= 0.05 && weight <= 0.5 // 50g to 500g
            "JUVENILE" -> weight >= 0.3 && weight <= 2.0 // 300g to 2kg
            "ADULT" -> weight >= 1.0 && weight <= 8.0 // 1kg to 8kg
            "SENIOR" -> weight >= 1.0 && weight <= 10.0 // 1kg to 10kg
            else -> weight > 0 && weight <= 15.0
        }
    }

    /**
     * Age category enum for validation
     */
    private enum class AgeCategory {
        CHICK, JUVENILE, ADULT, SENIOR
    }
}

/**
 * JSON utilities for validation
 */
object JsonUtils {
    fun parseJson(jsonString: String): Map<String, Any>? {
        return try {
            // Simplified JSON parsing - would use actual JSON library
            if (jsonString.startsWith("{") && jsonString.endsWith("}")) {
                mapOf("valid" to true)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

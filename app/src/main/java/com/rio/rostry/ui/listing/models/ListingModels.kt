package com.rio.rostry.ui.listing.models

import android.net.Uri
import java.util.*

/**
 * Data models for listing creation wizard
 */

data class ListingCreationData(
    val basicInfo: BasicInfo = BasicInfo(),
    val mediaInfo: MediaInfo = MediaInfo(),
    val traceabilityInfo: TraceabilityInfo = TraceabilityInfo(),
    val pricingInfo: PricingInfo = PricingInfo(),
    val kycVerification: KYCVerification = KYCVerification()
)

data class BasicInfo(
    val name: String = "",
    val breed: String = "",
    val gender: String = "",
    val age: String = "",
    val weight: String = "",
    val color: String = "",
    val description: String = "",
    val location: LocationInfo = LocationInfo()
)

data class LocationInfo(
    val state: String = "",
    val district: String = "",
    val village: String = "",
    val pincode: String = "",
    val farmName: String = ""
)

data class MediaInfo(
    val primaryImages: List<MediaItem> = emptyList(),
    val videos: List<MediaItem> = emptyList(),
    val documents: List<MediaItem> = emptyList(),
    val uploadProgress: Map<String, Int> = emptyMap()
)

data class MediaItem(
    val id: String,
    val uri: Uri,
    val type: MediaType,
    val caption: String = "",
    val isUploaded: Boolean = false,
    val uploadUrl: String? = null,
    val thumbnailUrl: String? = null
)

enum class MediaType {
    IMAGE,
    VIDEO,
    DOCUMENT
}

data class TraceabilityInfo(
    val parentMale: ParentInfo? = null,
    val parentFemale: ParentInfo? = null,
    val vaccinationRecords: List<VaccinationRecord> = emptyList(),
    val healthRecords: List<HealthRecord> = emptyList(),
    val feedingHistory: FeedingHistory = FeedingHistory(),
    val breedingHistory: List<BreedingRecord> = emptyList(),
    val certifications: List<Certification> = emptyList()
)

data class ParentInfo(
    val id: String? = null,
    val name: String = "",
    val breed: String = "",
    val registrationNumber: String = "",
    val isVerified: Boolean = false
)

data class VaccinationRecord(
    val id: String,
    val vaccineName: String,
    val dateAdministered: Date,
    val veterinarianName: String = "",
    val batchNumber: String = "",
    val nextDueDate: Date? = null,
    val documentUrl: String? = null
)

data class HealthRecord(
    val id: String,
    val checkupDate: Date,
    val veterinarianName: String,
    val findings: String,
    val treatment: String = "",
    val followUpRequired: Boolean = false,
    val documentUrl: String? = null
)

data class FeedingHistory(
    val feedType: String = "",
    val feedBrand: String = "",
    val dailyQuantity: String = "",
    val supplements: List<String> = emptyList(),
    val specialDiet: String = ""
)

data class BreedingRecord(
    val id: String,
    val mateId: String,
    val mateName: String,
    val breedingDate: Date,
    val expectedHatchDate: Date? = null,
    val actualHatchDate: Date? = null,
    val offspringCount: Int = 0,
    val notes: String = ""
)

data class Certification(
    val id: String,
    val type: CertificationType,
    val issuedBy: String,
    val issuedDate: Date,
    val expiryDate: Date? = null,
    val certificateNumber: String,
    val documentUrl: String? = null
)

enum class CertificationType {
    BREED_REGISTRATION,
    HEALTH_CERTIFICATE,
    ORGANIC_CERTIFICATION,
    SHOW_AWARD,
    BREEDING_PERMIT
}

data class PricingInfo(
    val basePrice: String = "",
    val currency: String = "INR",
    val priceType: PriceType = PriceType.FIXED,
    val minimumPrice: String = "",
    val isNegotiable: Boolean = false,
    val paymentMethods: List<PaymentMethod> = listOf(PaymentMethod.CASH_ON_DELIVERY),
    val deliveryOptions: List<DeliveryOption> = listOf(DeliveryOption.PICKUP),
    val availability: AvailabilityInfo = AvailabilityInfo()
)

enum class PriceType {
    FIXED,
    NEGOTIABLE,
    AUCTION
}

enum class PaymentMethod {
    CASH_ON_DELIVERY,
    ADVANCE_PAYMENT,
    PARTIAL_ADVANCE,
    BANK_TRANSFER,
    UPI_PAYMENT
}

enum class DeliveryOption {
    PICKUP,
    DELIVERY,
    MEET_HALFWAY,
    TRANSPORT_SERVICE
}

data class AvailabilityInfo(
    val isAvailable: Boolean = true,
    val availableFrom: Date? = null,
    val availableUntil: Date? = null,
    val quantity: Int = 1,
    val reservationAllowed: Boolean = false,
    val advanceBookingDays: Int = 0
)

data class KYCVerification(
    val farmerName: String = "",
    val phoneNumber: String = "",
    val alternatePhone: String = "",
    val aadharNumber: String = "",
    val panNumber: String = "",
    val farmLicense: String = "",
    val bankAccountNumber: String = "",
    val ifscCode: String = "",
    val documents: List<KYCDocument> = emptyList(),
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING,
    val verificationLevel: VerificationLevel = VerificationLevel.BASIC
)

data class KYCDocument(
    val id: String,
    val type: DocumentType,
    val documentNumber: String,
    val documentUrl: String,
    val isVerified: Boolean = false,
    val uploadDate: Date
)

enum class DocumentType {
    AADHAR_CARD,
    PAN_CARD,
    FARM_LICENSE,
    BANK_PASSBOOK,
    VETERINARY_CERTIFICATE,
    LAND_OWNERSHIP_DOCUMENT
}

enum class VerificationStatus {
    PENDING,
    IN_REVIEW,
    VERIFIED,
    REJECTED,
    EXPIRED
}

enum class VerificationLevel {
    BASIC,      // Phone + Email verified
    FARMER,     // + Documents verified
    PREMIUM     // + Site visit completed
}

/**
 * Listing submission result
 */
data class ListingSubmissionResult(
    val isSuccess: Boolean,
    val listingId: String? = null,
    val message: String,
    val requiresApproval: Boolean = false,
    val estimatedApprovalTime: String? = null,
    val errors: List<ValidationError> = emptyList()
)

data class ValidationError(
    val field: String,
    val message: String,
    val severity: ErrorSeverity
)

enum class ErrorSeverity {
    WARNING,
    ERROR,
    CRITICAL
}

/**
 * Common breed options for roosters
 */
object BreedOptions {
    val COMMON_BREEDS = listOf(
        "Aseel",
        "Kadaknath",
        "Rhode Island Red",
        "White Leghorn",
        "Brahma",
        "Cochin",
        "Bantam",
        "Country Chicken",
        "Desi/Native",
        "Other"
    )
    
    val GENDER_OPTIONS = listOf(
        "Male (Rooster)",
        "Female (Hen)"
    )
    
    val COLOR_OPTIONS = listOf(
        "Black",
        "White",
        "Brown",
        "Red",
        "Golden",
        "Mixed/Multicolor",
        "Other"
    )
}

package com.rio.rostry.fowl.domain.model

import com.rio.rostry.core.common.model.*
import java.util.*

/**
 * Domain model representing a fowl (rooster/hen) in the RIO platform
 */
data class Fowl(
    val id: String,
    val ownerId: String,
    val name: String? = null,
    val breed: BreedInfo,
    val gender: FowlGender,
    val physicalTraits: PhysicalTraits,
    val birthInfo: BirthInfo,
    val lineage: LineageInfo,
    val origin: OriginInfo,
    val status: FowlStatus,
    val performance: PerformanceMetrics? = null,
    val documentation: Documentation,
    val media: MediaInfo,
    val ownershipHistory: List<OwnershipRecord>,
    val healthRecords: List<HealthRecord> = emptyList(),
    val createdAt: Date,
    val updatedAt: Date,
    val lastHealthCheck: Date? = null,
    val searchTerms: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val metadata: FowlMetadata
)

/**
 * Breed information
 */
data class BreedInfo(
    val primary: String,
    val secondary: String? = null,
    val purity: Int, // Percentage 0-100
    val variety: String? = null,
    val bloodline: String? = null
)

/**
 * Fowl gender enumeration
 */
enum class FowlGender {
    MALE,
    FEMALE,
    UNKNOWN
}

/**
 * Physical characteristics
 */
data class PhysicalTraits(
    val color: String,
    val weight: Double, // in kg
    val height: Double, // in cm
    val combType: CombType,
    val legColor: String,
    val eyeColor: String,
    val distinguishingMarks: String? = null
)

/**
 * Comb types
 */
enum class CombType {
    SINGLE,
    ROSE,
    PEA,
    CUSHION,
    STRAWBERRY,
    BUTTERCUP,
    V_SHAPED
}

/**
 * Birth information
 */
data class BirthInfo(
    val birthDate: Date? = null,
    val ageCategory: AgeCategory,
    val estimatedAge: EstimatedAge? = null,
    val hatchMethod: HatchMethod? = null
)

/**
 * Age categories
 */
enum class AgeCategory {
    CHICK,
    JUVENILE,
    ADULT,
    SENIOR
}

/**
 * Estimated age when exact birth date is unknown
 */
data class EstimatedAge(
    val months: Int,
    val confidence: Confidence
)

/**
 * Confidence levels
 */
enum class Confidence {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Hatching methods
 */
enum class HatchMethod {
    NATURAL,
    INCUBATOR,
    UNKNOWN
}

/**
 * Lineage information for family tree tracking
 */
data class LineageInfo(
    val fatherId: String? = null,
    val motherId: String? = null,
    val generation: Int = 1,
    val bloodline: String? = null,
    val inbreedingCoefficient: Double? = null,
    val siblings: List<String> = emptyList(),
    val offspring: List<String> = emptyList()
)

/**
 * Origin information
 */
data class OriginInfo(
    val region: String,
    val district: String,
    val farmId: String? = null,
    val coordinates: Coordinates? = null,
    val breeder: String? = null
)

/**
 * Current fowl status
 */
data class FowlStatus(
    val health: HealthStatus,
    val availability: AvailabilityStatus,
    val location: LocationInfo
)

/**
 * Health status
 */
enum class HealthStatus {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    SICK,
    DECEASED
}

/**
 * Availability status
 */
enum class AvailabilityStatus {
    AVAILABLE,
    SOLD,
    BREEDING,
    DECEASED,
    MISSING,
    RESERVED
}

/**
 * Current location information
 */
data class LocationInfo(
    val currentFarm: String? = null,
    val district: String,
    val coordinates: Coordinates? = null,
    val lastUpdated: Date
)

/**
 * Performance metrics for breeding and competition
 */
data class PerformanceMetrics(
    val eggProduction: EggProduction? = null,
    val breeding: BreedingPerformance? = null,
    val fighting: FightingRecord? = null,
    val show: ShowRecord? = null
)

/**
 * Egg production metrics
 */
data class EggProduction(
    val averagePerMonth: Int,
    val totalLifetime: Int,
    val lastRecorded: Date,
    val seasonalVariation: Map<String, Int> = emptyMap()
)

/**
 * Breeding performance
 */
data class BreedingPerformance(
    val totalOffspring: Int,
    val successfulMatings: Int,
    val lastBreeding: Date,
    val fertilityRate: Double? = null,
    val hatchabilityRate: Double? = null
)

/**
 * Fighting record for game birds
 */
data class FightingRecord(
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val retired: Boolean,
    val lastFight: Date? = null
)

/**
 * Show/competition record
 */
data class ShowRecord(
    val totalShows: Int,
    val wins: Int,
    val placements: Int,
    val awards: List<String> = emptyList(),
    val lastShow: Date? = null
)

/**
 * Documentation and certificates
 */
data class Documentation(
    val registrationNumber: String? = null,
    val microchipId: String? = null,
    val tattooId: String? = null,
    val certificates: List<String> = emptyList(),
    val pedigreeChart: String? = null,
    val qrCode: String? = null
)

/**
 * Media information
 */
data class MediaInfo(
    val primaryPhoto: String? = null,
    val photoCount: Int = 0,
    val videoCount: Int = 0,
    val lastPhotoAdded: Date? = null,
    val photos: List<FowlPhoto> = emptyList()
)

/**
 * Fowl photo information
 */
data class FowlPhoto(
    val id: String,
    val url: String,
    val thumbnailUrl: String? = null,
    val caption: String? = null,
    val type: PhotoType,
    val isPrimary: Boolean = false,
    val uploadedAt: Date,
    val metadata: PhotoMetadata? = null
)

/**
 * Photo types
 */
enum class PhotoType {
    PROFILE,
    FULL_BODY,
    CLOSE_UP,
    ACTION,
    BREEDING,
    HEALTH,
    SHOW,
    FAMILY
}

/**
 * Photo metadata
 */
data class PhotoMetadata(
    val fileSize: Long,
    val dimensions: Pair<Int, Int>,
    val quality: PhotoQuality,
    val aiAnalysis: AIAnalysis? = null
)

/**
 * Photo quality
 */
enum class PhotoQuality {
    LOW,
    MEDIUM,
    HIGH,
    EXCELLENT
}

/**
 * AI analysis results
 */
data class AIAnalysis(
    val detectedBreed: String? = null,
    val confidence: Double = 0.0,
    val alternativeBreeds: List<Pair<String, Double>> = emptyList(),
    val qualityScore: Double = 0.0,
    val detectedFeatures: List<String> = emptyList()
)

/**
 * Ownership record
 */
data class OwnershipRecord(
    val ownerId: String,
    val transferId: String? = null,
    val acquiredDate: Date,
    val transferredDate: Date? = null,
    val price: Double? = null,
    val currency: String = "INR",
    val transferMethod: TransferMethod
)

/**
 * Transfer methods
 */
enum class TransferMethod {
    PURCHASE,
    GIFT,
    INHERITANCE,
    BREEDING_LOAN,
    TRADE,
    RETURN
}

/**
 * Health record
 */
data class HealthRecord(
    val id: String,
    val eventType: HealthEventType,
    val eventDate: Date,
    val veterinarian: VeterinarianInfo? = null,
    val symptoms: List<String> = emptyList(),
    val diagnosis: String? = null,
    val treatment: Treatment? = null,
    val notes: String? = null,
    val followUpRequired: Boolean = false,
    val followUpDate: Date? = null
)

/**
 * Health event types
 */
enum class HealthEventType {
    VACCINATION,
    ILLNESS,
    INJURY,
    CHECKUP,
    TREATMENT,
    SURGERY,
    DEATH
}

/**
 * Veterinarian information
 */
data class VeterinarianInfo(
    val name: String,
    val licenseNumber: String? = null,
    val clinicName: String? = null,
    val contactInfo: String? = null
)

/**
 * Treatment information
 */
data class Treatment(
    val medications: List<Medication> = emptyList(),
    val procedures: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val cost: Double? = null
)

/**
 * Medication information
 */
data class Medication(
    val name: String,
    val dosage: String,
    val frequency: String,
    val duration: String,
    val route: MedicationRoute
)

/**
 * Medication routes
 */
enum class MedicationRoute {
    ORAL,
    INJECTION,
    TOPICAL,
    INHALATION
}

/**
 * Fowl metadata
 */
data class FowlMetadata(
    val dataQuality: DataQuality,
    val verificationLevel: VerificationLevel,
    val lastVerifiedBy: String? = null,
    val importSource: String? = null,
    val notes: String? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val lastSyncAt: Date? = null
)

/**
 * Fowl search criteria
 */
data class FowlSearchCriteria(
    val query: String? = null,
    val breed: String? = null,
    val gender: FowlGender? = null,
    val ageCategory: AgeCategory? = null,
    val region: String? = null,
    val district: String? = null,
    val availability: AvailabilityStatus? = null,
    val priceRange: Pair<Double, Double>? = null,
    val bloodline: String? = null,
    val ownerId: String? = null,
    val sortBy: FowlSortBy = FowlSortBy.CREATED_AT,
    val sortOrder: SortOrder = SortOrder.DESC
)

/**
 * Fowl sorting options
 */
enum class FowlSortBy {
    CREATED_AT,
    UPDATED_AT,
    BIRTH_DATE,
    WEIGHT,
    PRICE,
    NAME,
    BREED
}

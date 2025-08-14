package com.rio.rostry.fowl.domain.model

import com.rio.rostry.core.common.model.UserTier
import java.util.*

/**
 * Simplified fowl models for Phase 2.1 implementation
 * These models work with the current database-simple module
 */

// Basic fowl model that maps to FowlEntity
data class Fowl(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String? = null,
    val breed: BreedInfo,
    val gender: FowlGender,
    val physicalTraits: PhysicalTraits,
    val birthInfo: BirthInfo,
    val lineage: LineageInfo = LineageInfo(),
    val origin: OriginInfo,
    val documentation: Documentation = Documentation(),
    val status: FowlStatus = FowlStatus.ACTIVE,
    val performance: PerformanceMetrics = PerformanceMetrics(),
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// Breed information
data class BreedInfo(
    val primary: String,
    val secondary: String? = null,
    val purity: Float = 1.0f, // 0.0 to 1.0
    val characteristics: List<String> = emptyList()
)

// Gender enum
enum class FowlGender {
    MALE, FEMALE, UNKNOWN
}

// Physical traits
data class PhysicalTraits(
    val weight: Float = 0f, // in kg
    val height: Float = 0f, // in cm
    val wingspan: Float = 0f, // in cm
    val colorPattern: String = "",
    val distinguishingMarks: List<String> = emptyList()
)

// Birth information
data class BirthInfo(
    val birthDate: Long? = null,
    val estimatedAge: Int? = null, // in months
    val birthLocation: String? = null,
    val hatchingMethod: HatchingMethod = HatchingMethod.NATURAL
)

enum class HatchingMethod {
    NATURAL, INCUBATOR, UNKNOWN
}

// Lineage information
data class LineageInfo(
    val fatherId: String? = null,
    val motherId: String? = null,
    val generation: Int = 1,
    val pedigreeNumber: String? = null
)

// Origin information
data class OriginInfo(
    val region: String,
    val district: String,
    val village: String? = null,
    val farmName: String? = null,
    val coordinates: Coordinates? = null
)

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

// Documentation
data class Documentation(
    val photos: List<String> = emptyList(),
    val videos: List<String> = emptyList(),
    val certificates: List<String> = emptyList(),
    val qrCode: String? = null,
    val notes: String? = null
)

// Fowl status
enum class FowlStatus {
    ACTIVE, SOLD, DECEASED, BREEDING, QUARANTINE
}

// Performance metrics
data class PerformanceMetrics(
    val eggProduction: EggProduction? = null,
    val weightGain: List<WeightRecord> = emptyList(),
    val healthRecords: List<HealthRecord> = emptyList(),
    val breedingHistory: List<BreedingRecord> = emptyList()
)

data class EggProduction(
    val dailyAverage: Float = 0f,
    val weeklyTotal: Int = 0,
    val monthlyTotal: Int = 0,
    val lastRecordedDate: Long? = null
)

data class WeightRecord(
    val date: Long,
    val weight: Float,
    val notes: String? = null
)

data class HealthRecord(
    val date: Long,
    val type: HealthRecordType,
    val description: String,
    val treatment: String? = null,
    val veterinarian: String? = null
)

enum class HealthRecordType {
    VACCINATION, ILLNESS, CHECKUP, INJURY, OTHER
}

data class BreedingRecord(
    val date: Long,
    val partnerId: String,
    val successful: Boolean,
    val offspring: List<String> = emptyList(),
    val notes: String? = null
)

// Search and filtering
data class FowlSearchCriteria(
    val query: String? = null,
    val breed: String? = null,
    val gender: FowlGender? = null,
    val status: FowlStatus? = null,
    val minWeight: Float? = null,
    val maxWeight: Float? = null,
    val region: String? = null,
    val tags: List<String> = emptyList()
)

data class FowlSearchResult(
    val fowls: List<Fowl>,
    val totalCount: Int,
    val hasMore: Boolean
)

// Photo types
enum class PhotoType {
    PROFILE, FULL_BODY, HEAD, FEET, WINGS, OTHER
}

// AI Analysis result
data class AIAnalysis(
    val detectedBreed: String?,
    val confidence: Float,
    val characteristics: List<String> = emptyList(),
    val suggestions: List<String> = emptyList()
)

// Draft data for form persistence
data class FowlDraftData(
    val name: String = "",
    val breed: String = "",
    val gender: FowlGender = FowlGender.UNKNOWN,
    val weight: String = "",
    val height: String = "",
    val region: String = "",
    val district: String = "",
    val village: String = "",
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val photos: List<String> = emptyList()
)

// List state for UI
data class ListState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasMore: Boolean = false,
    val error: String? = null
)

// Search state
data class SearchState(
    val query: String = "",
    val isSearching: Boolean = false,
    val filters: FowlSearchCriteria = FowlSearchCriteria()
)

// Pagination state
data class PaginationState(
    val currentPage: Int = 0,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true
)

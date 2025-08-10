package com.rio.rostry.shared.domain.model

import java.util.*

/**
 * Domain model for Transfer
 */
data class Transfer(
    val id: String,
    val fowlId: String,
    val fromUserId: String,
    val toUserId: String,
    val transferType: TransferType,
    val status: TransferStatus,
    val price: Double?,
    val currency: String?,
    val paymentMethod: String?,
    val transferDate: Date?,
    val completedAt: Date?,
    val notes: String?,
    val documents: List<String>,
    val region: String,
    val district: String,
    val createdAt: Date,
    val updatedAt: Date
)

/**
 * Transfer type enumeration
 */
enum class TransferType {
    SALE,
    GIFT,
    BREEDING_LOAN,
    EXCHANGE,
    INHERITANCE,
    RESCUE
}

/**
 * Transfer status enumeration
 */
enum class TransferStatus {
    PENDING,
    APPROVED,
    IN_TRANSIT,
    COMPLETED,
    CANCELLED,
    DISPUTED
}

/**
 * Domain model for BreedingRecord
 */
data class BreedingRecord(
    val id: String,
    val sireId: String,
    val damId: String,
    val breederId: String,
    val breedingDate: Date,
    val expectedHatchDate: Date?,
    val actualHatchDate: Date?,
    val eggCount: Int?,
    val hatchCount: Int?,
    val breedingMethod: BreedingMethod,
    val breedingPurpose: BreedingPurpose,
    val offspringIds: List<String>,
    val notes: String?,
    val region: String,
    val district: String,
    val createdAt: Date,
    val updatedAt: Date
)

/**
 * Breeding method enumeration
 */
enum class BreedingMethod {
    NATURAL,
    ARTIFICIAL_INSEMINATION,
    IN_VITRO_FERTILIZATION
}

/**
 * Breeding purpose enumeration
 */
enum class BreedingPurpose {
    MEAT_PRODUCTION,
    EGG_PRODUCTION,
    SHOW_QUALITY,
    BREEDING_STOCK,
    GENETIC_IMPROVEMENT,
    CONSERVATION
}
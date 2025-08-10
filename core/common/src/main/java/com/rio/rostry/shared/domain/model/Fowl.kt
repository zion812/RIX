package com.rio.rostry.shared.domain.model

import java.util.*

/**
 * Domain model for Fowl
 */
data class Fowl(
    val id: String,
    val ownerId: String,
    val name: String,
    val breedPrimary: String,
    val breedSecondary: String?,
    val gender: Gender,
    val birthDate: Date?,
    val ageCategory: AgeCategory,
    val color: String?,
    val weight: Double?,
    val height: Double?,
    val description: String?,
    val healthStatus: HealthStatus,
    val availabilityStatus: AvailabilityStatus,
    val fatherId: String?,
    val motherId: String?,
    val generation: Int,
    val primaryPhoto: String?,
    val photos: List<String>,
    val registrationNumber: String?,
    val qrCode: String?,
    val notes: String?,
    val tags: List<String>,
    val region: String,
    val district: String,
    val createdAt: Date,
    val updatedAt: Date
)
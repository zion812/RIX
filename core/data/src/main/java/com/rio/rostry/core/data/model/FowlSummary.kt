package com.rio.rostry.core.data.model

import java.util.*

/**
 * Compact fowl summary DTO for marketplace and transfer integration
 * Contains essential information with cover thumbnail for quick display
 */
data class FowlSummary(
    val id: String,
    val name: String?,
    val breedPrimary: String,
    val breedSecondary: String?,
    val gender: String,
    val color: String,
    val generation: Int,
    val dob: Date,
    val coverThumbnailUrl: String?,
    val healthStatus: String,
    val availabilityStatus: String,
    val region: String,
    val district: String,
    val createdAt: Date,
    val updatedAt: Date
)
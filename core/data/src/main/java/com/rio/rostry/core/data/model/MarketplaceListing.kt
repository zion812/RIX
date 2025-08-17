package com.rio.rostry.core.data.model

import java.util.Date

/**
 * Data model representing a marketplace listing in the ROSTRY platform
 */
data class MarketplaceListing(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fowlId: String,
    val sellerId: String,
    val purpose: String, // breeding, fighting, ornamental
    val priceCents: Int,
    val status: String = "active", // active, sold, closed
    val location: Map<String, String>? = null, // region, district
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
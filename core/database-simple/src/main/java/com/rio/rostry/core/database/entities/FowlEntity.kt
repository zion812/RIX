package com.rio.rostry.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import java.util.*

/**
 * Simplified Fowl entity for Phase 2
 */
@Entity(
    tableName = "fowls",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["breed"]),
        Index(value = ["status"])
    ]
)
data class FowlEntity(
    @PrimaryKey
    val id: String,
    val ownerId: String,
    val name: String,
    val breed: String,
    val gender: String, // MALE, FEMALE, UNKNOWN
    val birthDate: Date? = null,
    val color: String? = null,
    val weight: Double? = null,
    val status: String = "ACTIVE", // ACTIVE, SOLD, DECEASED, TRANSFERRED
    val description: String? = null,
    val imageUrls: String? = null, // JSON array of image URLs
    val price: Double? = null,
    val isForSale: Boolean = false,
    val region: String,
    val district: String,
    val createdAt: Date,
    val updatedAt: Date = Date()
)

package com.rio.rostry.core.data.model

import java.util.Date

/**
 * Data model representing a fowl in the ROSTRY platform
 */
data class Fowl(
    val id: String = java.util.UUID.randomUUID().toString(),
    val ownerId: String,
    val name: String? = null,
    val breed: String,
    val gender: String? = null,
    val dateOfBirth: Date,
    val parentIds: List<String>? = null,
    val breederReady: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
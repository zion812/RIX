package com.rio.rostry.core.data.model

import java.util.Date

/**
 * Data model representing a fowl record (timeline entry) in the ROSTRY platform
 * Tracks vaccination, growth, quarantine, and other events in a fowl's life
 */
data class FowlRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fowlId: String,
    val recordType: String, // VACCINATION, GROWTH, QUARANTINE, MORTALITY, etc.
    val recordDate: Date,
    val description: String? = null,
    
    // Metrics data as key-value pairs
    val metrics: Map<String, String> = emptyMap(),
    
    // Proof information
    val proofUrls: List<String> = emptyList(),
    val proofCount: Int = 0,
    
    // Audit fields
    val createdBy: String,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val version: Int = 1
)
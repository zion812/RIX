package com.rio.rostry.core.data.model

import java.util.*

/**
 * Lightweight data model of FowlRecord for timeline list display
 * Minimizes Map/List deserialization for better performance on low-end devices
 */
data class FowlRecordListItem(
    val id: String,
    val fowlId: String,
    val recordType: String,
    val recordDate: Date,
    val description: String? = null,
    val proofCount: Int,
    val createdBy: String,
    val createdAt: Date,
    val updatedAt: Date,
    val version: Int
)
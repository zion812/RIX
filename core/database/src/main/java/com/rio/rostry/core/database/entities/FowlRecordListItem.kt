package com.rio.rostry.core.database.entities

import androidx.room.ColumnInfo
import java.util.*

/**
 * Lightweight projection of FowlRecordEntity for timeline list display
 * Minimizes Map/List deserialization for better performance on low-end devices
 */
data class FowlRecordListItem(
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "fowl_id")
    val fowlId: String,
    
    @ColumnInfo(name = "record_type")
    val recordType: String,
    
    @ColumnInfo(name = "record_date")
    val recordDate: Date,
    
    @ColumnInfo(name = "description")
    val description: String? = null,
    
    @ColumnInfo(name = "proof_count")
    val proofCount: Int,
    
    @ColumnInfo(name = "created_by")
    val createdBy: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date,
    
    @ColumnInfo(name = "version")
    val version: Int
)
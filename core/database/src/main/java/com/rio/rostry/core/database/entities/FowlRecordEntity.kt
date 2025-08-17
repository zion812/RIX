package com.rio.rostry.core.database.entities

import androidx.room.*
import java.util.*

/**
 * Room entity for fowl records (timeline entries) with offline-first capabilities
 * Tracks vaccination, growth, quarantine, and other events in a fowl's life
 */
@Entity(
    tableName = "fowl_records",
    indices = [
        Index(value = ["fowl_id", "record_date DESC"]),
        Index(value = ["fowl_id"]),
        Index(value = ["record_type"]),
        Index(value = ["record_date"]),
        Index(value = ["created_by"]),
        Index(value = ["created_at"]),
        Index(value = ["updated_at"]),
        Index(value = ["is_deleted"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = FowlEntity::class,
            parentColumns = ["id"],
            childColumns = ["fowl_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FowlRecordEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "fowl_id")
    val fowlId: String,
    
    @ColumnInfo(name = "record_type")
    val recordType: String, // VACCINATION, GROWTH, QUARANTINE, MORTALITY, etc.
    
    @ColumnInfo(name = "record_date")
    val recordDate: Date,
    
    @ColumnInfo(name = "description")
    val description: String? = null,
    
    // Metrics data as key-value pairs
    @ColumnInfo(name = "metrics")
    val metrics: Map<String, String> = emptyMap(),
    
    // Proof information
    @ColumnInfo(name = "proof_urls")
    val proofUrls: List<String> = emptyList(),
    
    @ColumnInfo(name = "proof_count")
    val proofCount: Int = 0,
    
    // Audit fields
    @ColumnInfo(name = "created_by")
    val createdBy: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date,
    
    @ColumnInfo(name = "version")
    val version: Int = 1,
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)


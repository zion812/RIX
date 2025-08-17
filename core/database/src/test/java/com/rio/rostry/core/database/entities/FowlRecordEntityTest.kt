package com.rio.rostry.core.database.entities

import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FowlRecordEntityTest {
    
    @Test
    fun `test FowlRecordEntity creation`() {
        val id = "test-record-1"
        val fowlId = "fowl-1"
        val recordType = "VACCINATION"
        val recordDate = Date()
        val description = "Vaccination against common poultry diseases"
        val metrics = mapOf("vaccine_type" to "ND-IB", "dosage_ml" to "0.5")
        val proofUrls = listOf("https://example.com/proof1.jpg", "https://example.com/proof2.jpg")
        val proofCount = 2
        val createdBy = "user-1"
        val createdAt = Date()
        val updatedAt = Date()
        val version = 1
        
        val fowlRecord = FowlRecordEntity(
            id = id,
            fowlId = fowlId,
            recordType = recordType,
            recordDate = recordDate,
            description = description,
            metrics = metrics,
            proofUrls = proofUrls,
            proofCount = proofCount,
            createdBy = createdBy,
            createdAt = createdAt,
            updatedAt = updatedAt,
            version = version,
            isDeleted = false
        )
        
        assertEquals(id, fowlRecord.id)
        assertEquals(fowlId, fowlRecord.fowlId)
        assertEquals(recordType, fowlRecord.recordType)
        assertEquals(recordDate, fowlRecord.recordDate)
        assertEquals(description, fowlRecord.description)
        assertEquals(metrics, fowlRecord.metrics)
        assertEquals(proofUrls, fowlRecord.proofUrls)
        assertEquals(proofCount, fowlRecord.proofCount)
        assertEquals(createdBy, fowlRecord.createdBy)
        assertEquals(createdAt, fowlRecord.createdAt)
        assertEquals(updatedAt, fowlRecord.updatedAt)
        assertEquals(version, fowlRecord.version)
        assertEquals(false, fowlRecord.isDeleted)
    }
    
    @Test
    fun `test FowlRecordEntity with empty collections`() {
        val fowlRecord = FowlRecordEntity(
            id = "test-record-2",
            fowlId = "fowl-2",
            recordType = "CHECKUP",
            recordDate = Date(),
            description = null,
            metrics = emptyMap(),
            proofUrls = emptyList(),
            proofCount = 0,
            createdBy = "user-2",
            createdAt = Date(),
            updatedAt = Date(),
            version = 1,
            isDeleted = false
        )
        
        assertTrue(fowlRecord.metrics.isEmpty())
        assertTrue(fowlRecord.proofUrls.isEmpty())
        assertEquals(0, fowlRecord.proofCount)
        assertEquals(null, fowlRecord.description)
    }
}
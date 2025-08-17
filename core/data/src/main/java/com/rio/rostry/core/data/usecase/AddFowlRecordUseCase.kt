package com.rio.rostry.core.data.usecase

import com.rio.rostry.core.data.model.FowlRecord
import com.rio.rostry.core.data.repository.FowlRepository
import com.rio.rostry.core.data.repository.FowlRecordRepository
import com.rio.rostry.core.common.model.Result
import javax.inject.Inject

/**
 * Use case for adding fowl records (timeline entries)
 */
class AddFowlRecordUseCase @Inject constructor(
    private val fowlRecordRepository: FowlRecordRepository,
    private val fowlRepository: FowlRepository
) {
    /**
     * Add a fowl record with the provided data
     *
     * @param recordData The data for the fowl record
     * @return Result indicating success or an error
     */
    suspend operator fun invoke(recordData: FowlRecordCreationData): Result<Unit> {
        // Get the fowl to check its age
        val fowlResult = fowlRepository.getFowlById(recordData.fowlId)
        if (fowlResult is Result.Error) {
            return Result.Error(Exception("Failed to get fowl information"))
        }
        
        val fowl = (fowlResult as Result.Success).data
        if (fowl == null) {
            return Result.Error(Exception("Fowl not found"))
        }
        
        // Calculate fowl age in weeks
        val dob = fowl.dob.time
        val now = recordData.recordDate.time
        val ageInWeeks = ((now - dob) / (7 * 24 * 60 * 60 * 1000L)).toInt()
        
        // Validate record type based on fowl age
        val validationError = validateRecordTypeForAge(recordData.recordType, ageInWeeks)
        if (validationError != null) {
            return Result.Error(IllegalArgumentException(validationError))
        }
        
        // Validate proof requirements
        val proofError = validateProofRequirements(recordData)
        if (proofError != null) {
            return Result.Error(IllegalArgumentException(proofError))
        }
        
        val record = FowlRecord(
            fowlId = recordData.fowlId,
            recordType = recordData.recordType,
            recordDate = recordData.recordDate,
            description = recordData.description,
            metrics = recordData.metrics,
            proofUrls = recordData.proofUrls,
            proofCount = recordData.proofCount,
            createdBy = recordData.createdBy,
            createdAt = recordData.createdAt,
            updatedAt = recordData.updatedAt,
            version = recordData.version
        )
        
        return fowlRecordRepository.addRecord(record)
    }
    
    /**
     * Validate that the record type is allowed for the given age
     */
    private fun validateRecordTypeForAge(recordType: String, ageInWeeks: Int): String? {
        return when {
            ageInWeeks < 20 -> {
                // Pre-20w: only allow vaccination and milestone records
                when (recordType) {
                    "VACCINATION", "MILESTONE_5W", "MILESTONE_20W" -> null
                    else -> "Only vaccination and milestone records are allowed before 20 weeks of age"
                }
            }
            ageInWeeks >= 20 && ageInWeeks <= 52 -> {
                // 20w-52w: allow all record types except weekly updates after 35w for hens
                when (recordType) {
                    "WEEKLY_UPDATE" -> {
                        // For hens, weekly updates are only allowed until 35 weeks
                        // In a real implementation, we would check the fowl's gender
                        null // Allow for now
                    }
                    else -> null
                }
            }
            else -> {
                // After 52w: allow all record types
                null
            }
        }
    }
    
    /**
     * Validate proof requirements for different record types
     */
    private fun validateProofRequirements(recordData: FowlRecordCreationData): String? {
        return when (recordData.recordType) {
            "VACCINATION", "MILESTONE_5W", "MILESTONE_20W" -> {
                // These records require proof
                if (recordData.proofUrls.isEmpty()) {
                    "This record type requires proof documentation"
                } else {
                    null
                }
            }
            "WEEKLY_UPDATE" -> {
                // Weekly updates may require proof depending on content
                null // Allow for now
            }
            else -> null
        }
    }
}

/**
 * Data class representing the creation data for a fowl record
 */
data class FowlRecordCreationData(
    val fowlId: String,
    val recordType: String, // VACCINATION, GROWTH, QUARANTINE, MORTALITY, etc.
    val recordDate: java.util.Date,
    val description: String? = null,
    
    // Metrics data as key-value pairs
    val metrics: Map<String, String> = emptyMap(),
    
    // Proof information
    val proofUrls: List<String> = emptyList(),
    val proofCount: Int = 0,
    
    // Audit fields
    val createdBy: String,
    val createdAt: java.util.Date = java.util.Date(),
    val updatedAt: java.util.Date = java.util.Date(),
    val version: Int = 1
)
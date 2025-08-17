package com.rio.rostry.core.data.usecase

import com.rio.rostry.core.data.repository.FowlRepository
import com.rio.rostry.core.common.model.Result
import com.rio.rostry.core.database.entities.FowlEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

/**
 * Use case for creating a fowl
 */
class CreateFowlUseCase @Inject constructor(
    private val fowlRepository: FowlRepository
) {
    /**
     * Create a new fowl with the provided data
     *
     * @param fowlData The data for the new fowl
     * @return Result containing the ID of the created fowl or an error
     */
    suspend operator fun invoke(fowlData: FowlCreationData): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val fowl = FowlEntity(
                id = UUID.randomUUID().toString(),
                ownerId = fowlData.ownerId,
                name = fowlData.name,
                breedPrimary = fowlData.breed,
                healthStatus = "GOOD",
                availabilityStatus = if (fowlData.isForSale) "AVAILABLE" else "RESERVED",
                primaryPhoto = fowlData.photos.firstOrNull(),
                photos = fowlData.photos,
                createdAt = Date(),
                updatedAt = Date()
                // Other fields will use default values
            )
            
            fowlRepository.insertFowl(fowl)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

/**
 * Data class representing the creation data for a fowl
 */
data class FowlCreationData(
    val ownerId: String,
    val name: String,
    val breed: String,
    val gender: String,
    val dateOfBirth: Date,
    val color: String,
    val weightInGrams: Int,
    val isForSale: Boolean,
    val priceInCoins: Int?,
    val description: String,
    val traits: List<String>,
    val photos: List<String>,
    val vaccinationRecords: List<VaccinationRecord>
)

/**
 * Data class representing a vaccination record
 */
data class VaccinationRecord(
    val vaccineName: String,
    val dateAdministered: Date,
    val nextDueDate: Date?,
    val veterinarian: String?
)
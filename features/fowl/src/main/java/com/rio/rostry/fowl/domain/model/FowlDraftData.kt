package com.rio.rostry.fowl.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * ✅ Fowl draft data that persists across configuration changes
 * Uses Parcelable for efficient SavedStateHandle storage
 */
@Parcelize
data class FowlDraftData(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val breed: String = "",
    val gender: FowlGender? = null,
    val dateOfBirth: Date? = null,
    val color: String = "",
    val weight: Float? = null,
    val height: Float? = null,
    val description: String = "",
    val healthStatus: HealthStatus = HealthStatus.HEALTHY,
    val vaccinations: List<VaccinationRecord> = emptyList(),
    val parentMaleId: String? = null,
    val parentFemaleId: String? = null,
    val photos: List<String> = emptyList(),
    val location: LocationData? = null,
    val tags: List<String> = emptyList(),
    val isRegistrationComplete: Boolean = false,
    val currentStep: RegistrationStep = RegistrationStep.BASIC_INFO,
    val createdAt: Date = Date(),
    val lastModified: Date = Date()
) : Parcelable {
    
    /**
     * Check if basic information is complete
     */
    fun isBasicInfoComplete(): Boolean {
        return name.isNotBlank() && 
               breed.isNotBlank() && 
               gender != null && 
               dateOfBirth != null
    }
    
    /**
     * Check if physical characteristics are complete
     */
    fun isPhysicalInfoComplete(): Boolean {
        return color.isNotBlank() && 
               weight != null && 
               height != null
    }
    
    /**
     * Check if health information is complete
     */
    fun isHealthInfoComplete(): Boolean {
        return healthStatus != HealthStatus.UNKNOWN
    }
    
    /**
     * Check if lineage information is complete (optional)
     */
    fun isLineageInfoComplete(): Boolean {
        // Lineage is optional, so always return true
        return true
    }
    
    /**
     * Check if photos are uploaded
     */
    fun hasPhotos(): Boolean {
        return photos.isNotEmpty()
    }
    
    /**
     * Get completion percentage
     */
    fun getCompletionPercentage(): Float {
        var completed = 0
        var total = 5
        
        if (isBasicInfoComplete()) completed++
        if (isPhysicalInfoComplete()) completed++
        if (isHealthInfoComplete()) completed++
        if (hasPhotos()) completed++
        if (description.isNotBlank()) completed++
        
        return (completed.toFloat() / total) * 100f
    }
    
    /**
     * Convert to Fowl entity for saving
     */
    fun toFowlEntity(ownerId: String): Fowl {
        return Fowl(
            id = id,
            name = name,
            breed = breed,
            gender = gender ?: FowlGender.UNKNOWN,
            dateOfBirth = dateOfBirth ?: Date(),
            color = color,
            weight = weight ?: 0f,
            height = height ?: 0f,
            description = description,
            healthStatus = healthStatus,
            vaccinations = vaccinations,
            parentMaleId = parentMaleId,
            parentFemaleId = parentFemaleId,
            photos = photos,
            location = location,
            tags = tags,
            ownerId = ownerId,
            createdAt = createdAt,
            updatedAt = Date(),
            isActive = true,
            qrCode = null // Will be generated after saving
        )
    }
}

/**
 * Registration steps for fowl creation
 */
enum class RegistrationStep {
    BASIC_INFO,
    PHYSICAL_CHARACTERISTICS,
    HEALTH_INFO,
    LINEAGE_INFO,
    PHOTOS,
    LOCATION,
    REVIEW,
    COMPLETE
}

/**
 * Vaccination record for fowl health tracking
 */
@Parcelize
data class VaccinationRecord(
    val id: String = UUID.randomUUID().toString(),
    val vaccineName: String,
    val dateAdministered: Date,
    val veterinarianName: String? = null,
    val batchNumber: String? = null,
    val nextDueDate: Date? = null,
    val notes: String? = null
) : Parcelable

/**
 * Location data for fowl registration
 */
@Parcelize
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val accuracy: Float? = null
) : Parcelable

/**
 * Health status enumeration
 */
enum class HealthStatus {
    HEALTHY,
    SICK,
    RECOVERING,
    QUARANTINED,
    DECEASED,
    UNKNOWN
}

/**
 * Fowl gender enumeration
 */
enum class FowlGender {
    MALE,
    FEMALE,
    UNKNOWN
}

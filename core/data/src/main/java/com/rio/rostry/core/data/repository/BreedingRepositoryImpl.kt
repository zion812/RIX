package com.rio.rostry.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rostry.core.data.service.UserValidationService
import com.rio.rostry.core.database.dao.BreedingDao
import com.rio.rostry.core.database.dao.FowlDao
import com.rio.rostry.core.database.entities.BreedingRecordEntity
import com.rio.rostry.core.database.entities.FowlEntity
import com.rio.rostry.core.network.NetworkStateManager
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for fowl breeding operations, with offline-first support.
 */
@Singleton
class BreedingRepositoryImpl @Inject constructor(
    private val fowlDao: FowlDao,
    private val breedingDao: BreedingDao,
    private val userValidationService: UserValidationService,
    private val firestore: FirebaseFirestore,
    private val networkStateManager: NetworkStateManager
) {

    /**
     * Records a breeding event locally first, then attempts to sync to the server.
     */
    suspend fun recordBreeding(
        maleId: String,
        femaleId: String,
        breederId: String,
        expectedHatchDate: Date? = null
    ): Result<String> {
        return try {
            val male = fowlDao.getById(maleId)
            val female = fowlDao.getById(femaleId)
            if (male == null || female == null || male.ownerId != breederId || female.ownerId != breederId) {
                return Result.failure(Exception("Breeder must own both parent fowls."))
            }

            val breedingRecord = BreedingRecordEntity(
                id = UUID.randomUUID().toString(),
                maleId = maleId,
                femaleId = femaleId,
                breederId = breederId,
                breedingDate = Date(),
                expectedHatchDate = expectedHatchDate,
                status = "ACTIVE",
                isSynced = false
            )

            breedingDao.insert(breedingRecord)

            if (networkStateManager.isConnected()) {
                syncBreedingRecord(breedingRecord)
            }

            Result.success(breedingRecord.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Registers offspring from a breeding event, saving the new fowl locally first.
     */
    suspend fun registerOffspring(
        breedingId: String,
        offspringData: Map<String, Any>
    ): Result<String> {
        return try {
            val breedingRecord = breedingDao.getById(breedingId)
                ?: return Result.failure(Exception("Breeding record not found locally."))

            val offspringId = UUID.randomUUID().toString()
            val offspring = FowlEntity(
                id = offspringId,
                name = offspringData["name"] as? String,
                breedPrimary = offspringData["breed"] as? String ?: "Mixed",
                ownerId = breedingRecord.breederId,
                parentMaleId = breedingRecord.maleId,
                parentFemaleId = breedingRecord.femaleId,
                healthStatus = "GOOD",
                availabilityStatus = "AVAILABLE",
                region = offspringData["region"] as? String ?: "",
                district = offspringData["district"] as? String ?: ""
                // other fields will use defaults
            )

            fowlDao.insert(offspring)

            if (networkStateManager.isConnected()) {
                // Also update the remote breeding record
                firestore.collection("breeding_records")
                    .document(breedingId)
                    .update("offspringIds", com.google.firebase.firestore.FieldValue.arrayUnion(offspringId))
                    .await()
            }

            Result.success(offspringId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets breeding history for a fowl from the local database.
     */
    suspend fun getBreedingHistory(fowlId: String): Result<List<BreedingRecordEntity>> {
        return try {
            val history = breedingDao.getHistoryByFowlId(fowlId)
            Result.success(history)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets the family tree for a fowl from the local database.
     */
    suspend fun getFamilyTree(fowlId: String, generations: Int = 3): Result<Map<String, Any>> {
        return try {
            val fowl = fowlDao.getById(fowlId)
                ?: return Result.failure(Exception("Fowl not found"))

            val familyTree = mutableMapOf<String, Any>()
            familyTree["fowl"] = fowl

            if (generations > 0) {
                if (fowl.parentMaleId != null) {
                    val fatherTree = getFamilyTree(fowl.parentMaleId, generations - 1)
                    if(fatherTree.isSuccess) familyTree["father"] = fatherTree.getOrThrow()
                }
                if (fowl.parentFemaleId != null) {
                    val motherTree = getFamilyTree(fowl.parentFemaleId, generations - 1)
                     if(motherTree.isSuccess) familyTree["mother"] = motherTree.getOrThrow()
                }
            }
            Result.success(familyTree)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncBreedingRecord(record: BreedingRecordEntity) {
        try {
            val recordData = mapOf(
                "id" to record.id,
                "maleId" to record.maleId,
                "femaleId" to record.femaleId,
                "breederId" to record.breederId,
                "breedingDate" to record.breedingDate,
                "expectedHatchDate" to record.expectedHatchDate,
                "status" to record.status,
                "createdAt" to Date(),
                "updatedAt" to Date()
            )
            firestore.collection("breeding_records").document(record.id).set(recordData).await()
            breedingDao.markAsSynced(record.id)
        } catch (e: Exception) {
            // Sync failed, will be retried later
        }
    }
}

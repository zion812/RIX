package com.rio.rostry.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rostry.core.database.dao.FowlDao
import com.rio.rostry.core.database.entities.FowlEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Simplified FowlRepository implementation for Phase 3
 * Works with our simplified database and Firebase
 */
class FowlRepositoryImpl(
    private val fowlDao: FowlDao,
    private val firestore: FirebaseFirestore
) {

    /**
     * Get fowl by ID with offline-first approach
     */
    suspend fun getFowlById(fowlId: String): FowlEntity? {
        return try {
            // Try local cache first
            val localFowl = fowlDao.getFowlById(fowlId)
            if (localFowl != null) {
                return localFowl
            }

            // If not in cache, try to fetch from server
            val serverFowl = fetchFowlFromServer(fowlId)
            if (serverFowl != null) {
                fowlDao.insertFowl(serverFowl)
                return serverFowl
            }

            null
        } catch (e: Exception) {
            // Return cached data if available
            fowlDao.getFowlById(fowlId)
        }
    }

    /**
     * Get fowls by owner
     */
    suspend fun getFowlsByOwner(ownerId: String): List<FowlEntity> {
        return try {
            fowlDao.getFowlsByOwner(ownerId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get fowls by owner as Flow
     */
    fun getFowlsByOwnerFlow(ownerId: String): Flow<List<FowlEntity>> {
        return fowlDao.getFowlsByOwnerFlow(ownerId)
    }

    /**
     * Get fowls for sale
     */
    suspend fun getFowlsForSale(): List<FowlEntity> {
        return try {
            fowlDao.getFowlsForSale()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get fowls for sale as Flow
     */
    fun getFowlsForSaleFlow(): Flow<List<FowlEntity>> {
        return fowlDao.getFowlsForSaleFlow()
    }

    /**
     * Get fowls by location
     */
    suspend fun getFowlsByLocation(region: String, district: String): List<FowlEntity> {
        return try {
            fowlDao.getFowlsByLocation(region, district)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get fowls by breed
     */
    suspend fun getFowlsByBreed(breed: String): List<FowlEntity> {
        return try {
            fowlDao.getFowlsByBreed(breed)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Insert or update fowl
     */
    suspend fun saveFowl(fowl: FowlEntity): Result<Unit> {
        return try {
            fowlDao.insertFowl(fowl)

            // Sync to server
            try {
                val fowlData = fowl.toFirestoreMap()
                firestore.collection("fowls")
                    .document(fowl.id)
                    .set(fowlData)
                    .await()
            } catch (e: Exception) {
                // Continue even if server sync fails
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update fowl
     */
    suspend fun updateFowl(fowl: FowlEntity): Result<Unit> {
        return try {
            fowlDao.updateFowl(fowl)

            // Sync to server
            try {
                val fowlData = fowl.toFirestoreMap()
                firestore.collection("fowls")
                    .document(fowl.id)
                    .update(fowlData)
                    .await()
            } catch (e: Exception) {
                // Continue even if server sync fails
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete fowl
     */
    suspend fun deleteFowl(fowlId: String): Result<Unit> {
        return try {
            fowlDao.deleteFowlById(fowlId)

            // Delete from server
            try {
                firestore.collection("fowls")
                    .document(fowlId)
                    .delete()
                    .await()
            } catch (e: Exception) {
                // Continue even if server delete fails
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch fowl from server
     */
    private suspend fun fetchFowlFromServer(fowlId: String): FowlEntity? {
        return try {
            val document = firestore.collection("fowls")
                .document(fowlId)
                .get()
                .await()

            document.toFowlEntity()
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Extension functions for Firestore document conversion
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toFowlEntity(): FowlEntity? {
    return try {
        FowlEntity(
            id = id,
            ownerId = getString("ownerId") ?: "",
            name = getString("name") ?: "Unknown",
            breed = getString("breed") ?: "Unknown",
            gender = getString("gender") ?: "UNKNOWN",
            birthDate = getTimestamp("birthDate")?.toDate(),
            color = getString("color"),
            weight = getDouble("weight"),
            status = getString("status") ?: "ACTIVE",
            description = getString("description"),
            imageUrls = getString("imageUrls"),
            price = getDouble("price"),
            isForSale = getBoolean("isForSale") ?: false,
            region = getString("region") ?: "other",
            district = getString("district") ?: "",
            createdAt = getTimestamp("createdAt")?.toDate() ?: Date(),
            updatedAt = getTimestamp("updatedAt")?.toDate() ?: Date()
        )
    } catch (e: Exception) {
        null
    }
}

private fun FowlEntity.toFirestoreMap(): Map<String, Any> {
    return mapOf(
        "ownerId" to ownerId,
        "name" to name,
        "breed" to breed,
        "gender" to gender,
        "birthDate" to birthDate,
        "color" to color,
        "weight" to weight,
        "status" to status,
        "description" to description,
        "imageUrls" to imageUrls,
        "price" to price,
        "isForSale" to isForSale,
        "region" to region,
        "district" to district,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    ).filterValues { it != null } as Map<String, Any>
}
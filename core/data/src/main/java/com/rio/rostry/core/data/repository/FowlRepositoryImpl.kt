package com.rio.rostry.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rostry.core.database.dao.FowlDao
import com.rio.rostry.core.database.entities.FowlEntity
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Repository for managing Fowl data, coordinating between the local
 * database and the remote Firestore service.
 */
class FowlRepositoryImpl(
    private val fowlDao: FowlDao,
    private val firestore: FirebaseFirestore
) {

    /**
     * Get fowl by ID with an offline-first approach.
     * It first checks the local database, and if not found, fetches from Firestore.
     */
    suspend fun getFowlById(fowlId: String): FowlEntity? {
        return try {
            // Try local cache first
            val localFowl = fowlDao.getById(fowlId)
            if (localFowl != null) {
                return localFowl
            }

            // If not in cache, try to fetch from server
            val serverFowl = fetchFowlFromServer(fowlId)
            if (serverFowl != null) {
                fowlDao.insert(serverFowl)
                return serverFowl
            }

            null
        } catch (e: Exception) {
            // On error (e.g., network failure), fallback to local cache
            fowlDao.getById(fowlId)
        }
    }

    /**
     * Get all fowls owned by a specific user from the local database.
     */
    suspend fun getFowlsByOwner(ownerId: String): List<FowlEntity> {
        return try {
            fowlDao.getFowlsByOwner(ownerId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Saves a new fowl to the local database and then syncs it to Firestore.
     */
    suspend fun saveFowl(fowl: FowlEntity): Result<Unit> {
        return try {
            fowlDao.insert(fowl)
            // Sync to server
            try {
                val fowlData = fowl.toFirestoreMap()
                firestore.collection("fowls")
                    .document(fowl.id)
                    .set(fowlData)
                    .await()
            } catch (e: Exception) {
                // The operation is still considered a success if the local write succeeds.
                // A background sync process should handle the failed remote write later.
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates an existing fowl in the local database and then syncs the update to Firestore.
     */
    suspend fun updateFowl(fowl: FowlEntity): Result<Unit> {
        return try {
            fowlDao.update(fowl)
            // Sync to server
            try {
                val fowlData = fowl.toFirestoreMap()
                firestore.collection("fowls")
                    .document(fowl.id)
                    .update(fowlData)
                    .await()
            } catch (e: Exception) {
                // Offline-first: local update is enough for success
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a fowl from the local database and then deletes it from Firestore.
     */
    suspend fun deleteFowl(fowlId: String): Result<Unit> {
        return try {
            fowlDao.deleteById(fowlId)
            // Sync deletion to server
            try {
                firestore.collection("fowls")
                    .document(fowlId)
                    .delete()
                    .await()
            } catch (e: Exception) {
                // Offline-first: local delete is enough for success
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches a single fowl document from Firestore by its ID.
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
 * Extension function to convert a Firestore DocumentSnapshot into a FowlEntity.
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toFowlEntity(): FowlEntity? {
    return try {
        FowlEntity(
            id = id,
            ownerId = getString("ownerId") ?: "",
            name = getString("name"),
            breedPrimary = getString("breedPrimary") ?: "Unknown",
            breedSecondary = getString("breedSecondary"),
            generation = getLong("generation")?.toInt() ?: 1,
            inbreedingCoefficient = getDouble("inbreedingCoefficient"),
            siblings = get("siblings") as? List<String> ?: emptyList(),
            offspring = get("offspring") as? List<String> ?: emptyList(),
            healthStatus = getString("healthStatus") ?: "UNKNOWN",
            availabilityStatus = getString("availabilityStatus") ?: "UNKNOWN",
            currentFarm = getString("currentFarm"),
            registrationNumber = getString("registrationNumber"),
            microchipId = getString("microchipId"),
            tattooId = getString("tattooId"),
            certificates = get("certificates") as? List<String> ?: emptyList(),
            qrCode = getString("qrCode"),
            primaryPhoto = getString("primaryPhoto"),
            photoCount = getLong("photoCount")?.toInt() ?: 0,
            videoCount = getLong("videoCount")?.toInt() ?: 0,
            photos = get("photos") as? List<String> ?: emptyList(),
            eggProductionMonthly = getLong("eggProductionMonthly")?.toInt(),
            totalOffspring = getLong("totalOffspring")?.toInt() ?: 0,
            fightingWins = getLong("fightingWins")?.toInt() ?: 0,
            fightingLosses = getLong("fightingLosses")?.toInt() ?: 0,
            showWins = getLong("showWins")?.toInt() ?: 0,
            awards = get("awards") as? List<String> ?: emptyList(),
            searchTerms = get("searchTerms") as? List<String> ?: emptyList(),
            tags = get("tags") as? List<String> ?: emptyList(),
            notes = getString("notes"),
            region = getString("region") ?: "",
            district = getString("district") ?: "",
            mandal = getString("mandal"),
            village = getString("village"),
            latitude = getDouble("latitude"),
            longitude = getDouble("longitude"),
            lastSyncTime = getTimestamp("lastSyncTime")?.toDate(),
            syncStatusString = getString("syncStatusString") ?: "PENDING_UPLOAD",
            conflictVersion = getLong("conflictVersion") ?: 1L,
            isDeleted = getBoolean("isDeleted") ?: false,
            createdAt = getTimestamp("createdAt")?.toDate() ?: Date(),
            updatedAt = getTimestamp("updatedAt")?.toDate() ?: Date(),
            syncPriority = getLong("syncPriority")?.toInt() ?: 1,
            hasConflict = getBoolean("hasConflict") ?: false,
            retryCount = getLong("retryCount")?.toInt() ?: 0,
            dataSize = getLong("dataSize") ?: 0L
        )
    } catch (e: Exception) {
        // Log the exception or handle it as needed
        null
    }
}

/**
 * Extension function to convert a FowlEntity into a Map for Firestore.
 */
private fun FowlEntity.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "ownerId" to ownerId,
        "name" to name,
        "breedPrimary" to breedPrimary,
        "breedSecondary" to breedSecondary,
        "generation" to generation,
        "inbreedingCoefficient" to inbreedingCoefficient,
        "siblings" to siblings,
        "offspring" to offspring,
        "healthStatus" to healthStatus,
        "availabilityStatus" to availabilityStatus,
        "currentFarm" to currentFarm,
        "registrationNumber" to registrationNumber,
        "microchipId" to microchipId,
        "tattooId" to tattooId,
        "certificates" to certificates,
        "qrCode" to qrCode,
        "primaryPhoto" to primaryPhoto,
        "photoCount" to photoCount,
        "videoCount" to videoCount,
        "photos" to photos,
        "eggProductionMonthly" to eggProductionMonthly,
        "totalOffspring" to totalOffspring,
        "fightingWins" to fightingWins,
        "fightingLosses" to fightingLosses,
        "showWins" to showWins,
        "awards" to awards,
        "searchTerms" to searchTerms,
        "tags" to tags,
        "notes" to notes,
        "region" to region,
        "district" to district,
        "mandal" to mandal,
        "village" to village,
        "latitude" to latitude,
        "longitude" to longitude,
        "lastSyncTime" to lastSyncTime,
        "syncStatusString" to syncStatusString,
        "conflictVersion" to conflictVersion,
        "isDeleted" to isDeleted,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "syncPriority" to syncPriority,
        "hasConflict" to hasConflict,
        "retryCount" to retryCount,
        "dataSize" to dataSize
    ).filterValues { it != null }
}
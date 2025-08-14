package com.rio.rostry.sync

import android.content.Context
import androidx.work.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rostry.core.database.ROSTRYDatabase
import com.rio.rostry.core.database.entities.FowlEntity
import com.rio.rostry.core.database.entities.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Simplified Sync Manager - Phase 3.1
 * Provides background sync operations with WorkManager and conflict resolution
 * Works with existing database-simple module
 */
class SimpleSyncManager(
    private val context: Context,
    private val database: ROSTRYDatabase,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()
    
    private val workManager = WorkManager.getInstance(context)
    
    companion object {
        private const val SYNC_WORK_NAME = "rio_background_sync"
        private const val PERIODIC_SYNC_INTERVAL_HOURS = 6L
        private const val RETRY_DELAY_MINUTES = 15L
    }
    
    /**
     * Initialize sync manager and schedule periodic sync
     */
    fun initialize() {
        schedulePeriodicSync()
        scheduleImmediateSync()
    }
    
    /**
     * Schedule periodic background sync
     */
    private fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            PERIODIC_SYNC_INTERVAL_HOURS, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                RETRY_DELAY_MINUTES, TimeUnit.MINUTES
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )
    }
    
    /**
     * Schedule immediate sync
     */
    fun scheduleImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val immediateSyncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()
        
        workManager.enqueue(immediateSyncRequest)
    }
    
    /**
     * Perform manual sync
     */
    suspend fun performManualSync(): SyncResult {
        _syncStatus.value = SyncStatus.SYNCING
        
        return try {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null) {
                _syncStatus.value = SyncStatus.ERROR("User not authenticated")
                return SyncResult.Error("User not authenticated")
            }
            
            // Sync user data
            val userSyncResult = syncUserData(currentUserId)
            
            // Sync fowl data
            val fowlSyncResult = syncFowlData(currentUserId)
            
            _lastSyncTime.value = System.currentTimeMillis()
            _syncStatus.value = SyncStatus.SUCCESS
            
            SyncResult.Success(
                usersSynced = if (userSyncResult) 1 else 0,
                fowlsSynced = fowlSyncResult,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR(e.message ?: "Sync failed")
            SyncResult.Error(e.message ?: "Sync failed")
        }
    }
    
    /**
     * Sync user data between local database and Firestore
     */
    private suspend fun syncUserData(userId: String): Boolean {
        return try {
            val userDao = database.userDao()
            val localUser = userDao.getUserById(userId)
            
            // Get user from Firestore
            val firestoreUser = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            if (firestoreUser.exists()) {
                // Update local user with Firestore data
                val updatedUser = UserEntity(
                    id = userId,
                    email = firestoreUser.getString("email") ?: "",
                    displayName = firestoreUser.getString("displayName") ?: "",
                    phoneNumber = firestoreUser.getString("phoneNumber"),
                    photoUrl = firestoreUser.getString("photoUrl"),
                    tier = firestoreUser.getString("tier") ?: "general",
                    region = firestoreUser.getString("region") ?: "other",
                    district = firestoreUser.getString("district") ?: "",
                    language = firestoreUser.getString("language") ?: "en",
                    isEmailVerified = firestoreUser.getBoolean("isEmailVerified") ?: false,
                    isPhoneVerified = firestoreUser.getBoolean("isPhoneVerified") ?: false,
                    createdAt = firestoreUser.getDate("createdAt") ?: Date(),
                    updatedAt = firestoreUser.getDate("updatedAt") ?: Date(),
                    lastLoginAt = firestoreUser.getDate("lastLoginAt")
                )
                userDao.insertUser(updatedUser)
            } else if (localUser != null) {
                // Upload local user to Firestore
                val userMap = mapOf(
                    "email" to localUser.email,
                    "displayName" to localUser.displayName,
                    "phoneNumber" to localUser.phoneNumber,
                    "photoUrl" to localUser.photoUrl,
                    "tier" to localUser.tier,
                    "region" to localUser.region,
                    "district" to localUser.district,
                    "language" to localUser.language,
                    "isEmailVerified" to localUser.isEmailVerified,
                    "isPhoneVerified" to localUser.isPhoneVerified,
                    "createdAt" to localUser.createdAt,
                    "updatedAt" to localUser.updatedAt,
                    "lastLoginAt" to localUser.lastLoginAt
                )
                firestore.collection("users").document(userId).set(userMap).await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Sync fowl data between local database and Firestore
     */
    private suspend fun syncFowlData(userId: String): Int {
        return try {
            val fowlDao = database.fowlDao()
            val localFowls = fowlDao.getFowlsByOwner(userId)
            
            // Get fowls from Firestore
            val firestoreFowls = firestore.collection("fowls")
                .whereEqualTo("ownerId", userId)
                .get()
                .await()
            
            var syncedCount = 0
            
            // Process Firestore fowls
            for (doc in firestoreFowls.documents) {
                val fowlEntity = FowlEntity(
                    id = doc.id,
                    ownerId = doc.getString("ownerId") ?: userId,
                    name = doc.getString("name") ?: "",
                    breed = doc.getString("breed") ?: "",
                    gender = doc.getString("gender") ?: "UNKNOWN",
                    birthDate = doc.getDate("birthDate"),
                    color = doc.getString("color"),
                    weight = doc.getDouble("weight"),
                    status = doc.getString("status") ?: "ACTIVE",
                    description = doc.getString("description"),
                    imageUrls = doc.getString("imageUrls"),
                    price = doc.getDouble("price"),
                    isForSale = doc.getBoolean("isForSale") ?: false,
                    region = doc.getString("region") ?: "",
                    district = doc.getString("district") ?: "",
                    createdAt = doc.getDate("createdAt") ?: Date(),
                    updatedAt = doc.getDate("updatedAt") ?: Date()
                )
                fowlDao.insertFowl(fowlEntity)
                syncedCount++
            }
            
            // Upload local fowls that don't exist in Firestore
            for (localFowl in localFowls) {
                val existsInFirestore = firestoreFowls.documents.any { it.id == localFowl.id }
                if (!existsInFirestore) {
                    val fowlMap = mapOf(
                        "ownerId" to localFowl.ownerId,
                        "name" to localFowl.name,
                        "breed" to localFowl.breed,
                        "gender" to localFowl.gender,
                        "birthDate" to localFowl.birthDate,
                        "color" to localFowl.color,
                        "weight" to localFowl.weight,
                        "status" to localFowl.status,
                        "description" to localFowl.description,
                        "imageUrls" to localFowl.imageUrls,
                        "price" to localFowl.price,
                        "isForSale" to localFowl.isForSale,
                        "region" to localFowl.region,
                        "district" to localFowl.district,
                        "createdAt" to localFowl.createdAt,
                        "updatedAt" to localFowl.updatedAt
                    )
                    firestore.collection("fowls").document(localFowl.id).set(fowlMap).await()
                    syncedCount++
                }
            }
            
            syncedCount
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Cancel all sync operations
     */
    fun cancelSync() {
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
        _syncStatus.value = SyncStatus.IDLE
    }
}

/**
 * Sync status sealed class
 */
sealed class SyncStatus {
    object IDLE : SyncStatus()
    object SYNCING : SyncStatus()
    object SUCCESS : SyncStatus()
    data class ERROR(val message: String) : SyncStatus()
}

/**
 * Sync result sealed class
 */
sealed class SyncResult {
    data class Success(
        val usersSynced: Int,
        val fowlsSynced: Int,
        val timestamp: Long
    ) : SyncResult()
    
    data class Error(val message: String) : SyncResult()
}

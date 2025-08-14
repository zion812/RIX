package com.rio.rostry.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rostry.core.database.dao.UserDao
import com.rio.rostry.core.database.entities.UserEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Simplified UserRepository implementation for Phase 3
 * Works with our simplified database and Firebase
 */
class UserRepositoryImpl(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore
) {
    
    private val _currentUserId = MutableStateFlow<String?>(null)
    private val _currentUserTier = MutableStateFlow<String?>(null)
    private val _isAuthenticated = MutableStateFlow(false)

    fun getCurrentUserTier(): Flow<String?> {
        return _currentUserTier.asStateFlow()
    }

    fun isUserAuthenticated(): Boolean {
        return _isAuthenticated.value
    }

    fun getCurrentUserId(): String? {
        return _currentUserId.value
    }
    
    /**
     * Set current user (called by authentication system)
     */
    suspend fun setCurrentUser(userId: String?, tier: String?) {
        _currentUserId.value = userId
        _currentUserTier.value = tier
        _isAuthenticated.value = userId != null

        // Cache user data locally if available
        if (userId != null && tier != null) {
            cacheUserData(userId, tier)
        }
    }
    
    /**
     * Get user by ID with offline-first approach
     */
    suspend fun getUserById(userId: String): UserEntity? {
        return try {
            // Try local cache first
            val localUser = userDao.getUserById(userId)
            if (localUser != null) {
                return localUser
            }

            // If not in cache, try to fetch from server
            val serverUser = fetchUserFromServer(userId)
            if (serverUser != null) {
                userDao.insertUser(serverUser)
                return serverUser
            }

            null
        } catch (e: Exception) {
            // Return cached data if available
            userDao.getUserById(userId)
        }
    }
    
    /**
     * Update user profile
     */
    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            // Update locally first
            val existingUser = userDao.getUserById(userId)
            if (existingUser != null) {
                val updatedUser = existingUser.copy(
                    displayName = updates["displayName"] as? String ?: existingUser.displayName,
                    email = updates["email"] as? String ?: existingUser.email,
                    phoneNumber = updates["phoneNumber"] as? String ?: existingUser.phoneNumber,
                    photoUrl = updates["photoUrl"] as? String ?: existingUser.photoUrl,
                    region = updates["region"] as? String ?: existingUser.region,
                    district = updates["district"] as? String ?: existingUser.district,
                    updatedAt = Date()
                )
                userDao.updateUser(updatedUser)
            }

            // Sync to server
            try {
                firestore.collection("users")
                    .document(userId)
                    .update(updates)
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
     * Search users by criteria
     */
    suspend fun searchUsers(query: String, limit: Int = 20): List<UserEntity> {
        return try {
            // Try server search first
            val querySnapshot = firestore.collection("users")
                .whereGreaterThanOrEqualTo("displayName", query)
                .whereLessThanOrEqualTo("displayName", query + "\uf8ff")
                .limit(limit.toLong())
                .get()
                .await()

            val users = querySnapshot.documents.mapNotNull { doc ->
                doc.toUserEntity()
            }

            // Cache results
            users.forEach { userDao.insertUser(it) }

            users
        } catch (e: Exception) {
            // Fallback to local search - for now return empty list
            // TODO: Implement local search when needed
            emptyList()
        }
    }
    
    /**
     * Get users by tier
     */
    suspend fun getUsersByTier(tier: String): List<UserEntity> {
        return try {
            // Try local first
            val localUsers = userDao.getUsersByTier(tier)
            if (localUsers.isNotEmpty()) {
                return localUsers
            }

            // Try server
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("tier", tier)
                .get()
                .await()

            val users = querySnapshot.documents.mapNotNull { doc ->
                doc.toUserEntity()
            }

            // Cache results
            users.forEach { userDao.insertUser(it) }

            users
        } catch (e: Exception) {
            userDao.getUsersByTier(tier)
        }
    }
    
    /**
     * Cache user data locally
     */
    private suspend fun cacheUserData(userId: String, tier: String) {
        try {
            val existingUser = userDao.getUserById(userId)
            if (existingUser == null) {
                val userEntity = UserEntity(
                    id = userId,
                    displayName = "User",
                    email = "",
                    phoneNumber = null,
                    tier = tier,
                    photoUrl = null,
                    region = "other",
                    district = "",
                    language = "en",
                    isEmailVerified = false,
                    isPhoneVerified = false,
                    createdAt = Date(),
                    updatedAt = Date(),
                    lastLoginAt = Date()
                )
                userDao.insertUser(userEntity)
            }
        } catch (e: Exception) {
            // Ignore cache errors
        }
    }
    
    /**
     * Fetch user from server
     */
    private suspend fun fetchUserFromServer(userId: String): UserEntity? {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            document.toUserEntity()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get users by location
     */
    suspend fun getUsersByLocation(region: String, district: String): List<UserEntity> {
        return try {
            userDao.getUsersByLocation(region, district)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

/**
 * Extension functions for Firestore document conversion
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toUserEntity(): UserEntity? {
    return try {
        UserEntity(
            id = id,
            displayName = getString("displayName") ?: "User",
            email = getString("email") ?: "",
            phoneNumber = getString("phoneNumber"),
            tier = getString("tier") ?: "general",
            photoUrl = getString("photoUrl"),
            region = getString("region") ?: "other",
            district = getString("district") ?: "",
            language = getString("language") ?: "en",
            isEmailVerified = getBoolean("isEmailVerified") ?: false,
            isPhoneVerified = getBoolean("isPhoneVerified") ?: false,
            createdAt = getTimestamp("createdAt")?.toDate() ?: Date(),
            updatedAt = getTimestamp("updatedAt")?.toDate() ?: Date(),
            lastLoginAt = getTimestamp("lastLoginAt")?.toDate()
        )
    } catch (e: Exception) {
        null
    }
}

private fun UserEntity.toFirestoreMap(): Map<String, Any> {
    return mapOf(
        "displayName" to displayName,
        "email" to email,
        "phoneNumber" to phoneNumber,
        "tier" to tier,
        "photoUrl" to photoUrl,
        "region" to region,
        "district" to district,
        "language" to language,
        "isEmailVerified" to isEmailVerified,
        "isPhoneVerified" to isPhoneVerified,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "lastLoginAt" to lastLoginAt
    ).filterValues { it != null } as Map<String, Any>
}

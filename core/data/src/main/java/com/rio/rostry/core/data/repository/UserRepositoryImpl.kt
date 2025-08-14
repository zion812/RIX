package com.rio.rostry.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rostry.core.database.dao.UserDao
import com.rio.rostry.core.database.entities.UserEntity
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Repository for managing User data, coordinating between the local
 * database and the remote Firestore service. This repository is stateless.
 */
class UserRepositoryImpl(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore
) {

    /**
     * Get user by ID with an offline-first approach.
     */
    suspend fun getUserById(userId: String): UserEntity? {
        return try {
            val localUser = userDao.getById(userId)
            if (localUser != null) {
                return localUser
            }
            val serverUser = fetchUserFromServer(userId)
            if (serverUser != null) {
                userDao.insert(serverUser)
                return serverUser
            }
            null
        } catch (e: Exception) {
            userDao.getById(userId)
        }
    }

    /**
     * Update user profile in the local database and sync to Firestore.
     */
    suspend fun updateUserProfile(user: UserEntity): Result<Unit> {
        return try {
            userDao.update(user)
            try {
                val userData = user.toFirestoreMap()
                firestore.collection("users").document(user.id).update(userData).await()
            } catch (e: Exception) {
                // Offline-first: local update is enough for success
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search for users. Tries a remote search first, then falls back to local.
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

            val users = querySnapshot.documents.mapNotNull { it.toUserEntity() }
            // Cache results
            userDao.insertAll(users)
            users
        } catch (e: Exception) {
            // Fallback to local search
            userDao.searchUsersByName(query, limit)
        }
    }

    /**
     * Get users by tier. Tries local cache first, then falls back to remote.
     */
    suspend fun getUsersByTier(tier: String): List<UserEntity> {
        return try {
            val localUsers = userDao.getUsersByTier(tier)
            if (localUsers.isNotEmpty()) {
                return localUsers
            }

            val querySnapshot = firestore.collection("users")
                .whereEqualTo("tier", tier)
                .get()
                .await()

            val users = querySnapshot.documents.mapNotNull { it.toUserEntity() }
            userDao.insertAll(users)
            users
        } catch (e: Exception) {
            userDao.getUsersByTier(tier)
        }
    }

    private suspend fun fetchUserFromServer(userId: String): UserEntity? {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            document.toUserEntity()
        } catch (e: Exception) {
            null
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

private fun UserEntity.toFirestoreMap(): Map<String, Any?> {
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
    ).filterValues { it != null }
}

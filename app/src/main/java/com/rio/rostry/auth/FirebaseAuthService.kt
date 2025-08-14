package com.rio.rostry.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rostry.core.database.entities.UserEntity
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Firebase Authentication service for RIO platform
 * Handles user registration, login, and profile management for rural farmers
 */
class FirebaseAuthService {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    /**
     * Get current authenticated user
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    /**
     * Check if user is authenticated
     */
    fun isUserAuthenticated(): Boolean = getCurrentUser() != null
    
    /**
     * Register new user with email and password
     */
    suspend fun registerUser(
        email: String,
        password: String,
        displayName: String,
        phoneNumber: String,
        region: String,
        district: String,
        tier: String
    ): Result<FirebaseUser> {
        return try {
            // Create Firebase Auth user
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("User creation failed")
            
            // Update user profile
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()
            
            // Create user document in Firestore
            val userEntity = UserEntity(
                id = user.uid,
                displayName = displayName,
                email = email,
                phoneNumber = phoneNumber,
                tier = tier,
                photoUrl = null,
                region = region,
                district = district,
                language = "en",
                isEmailVerified = false,
                isPhoneVerified = false,
                createdAt = Date(),
                updatedAt = Date(),
                lastLoginAt = Date()
            )
            
            saveUserToFirestore(userEntity)
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Login user with email and password
     */
    suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("Login failed")
            
            // Update last login time
            updateLastLoginTime(user.uid)
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Logout current user
     */
    fun logout() {
        auth.signOut()
    }
    
    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update user profile
     */
    suspend fun updateUserProfile(
        displayName: String? = null,
        phoneNumber: String? = null,
        region: String? = null,
        district: String? = null,
        tier: String? = null
    ): Result<Unit> {
        return try {
            val user = getCurrentUser() ?: throw Exception("No authenticated user")
            
            // Update Firebase Auth profile if display name changed
            if (displayName != null && displayName != user.displayName) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                user.updateProfile(profileUpdates).await()
            }
            
            // Update Firestore document
            val updates = mutableMapOf<String, Any>(
                "updatedAt" to Date()
            )
            
            displayName?.let { updates["displayName"] = it }
            phoneNumber?.let { updates["phoneNumber"] = it }
            region?.let { updates["region"] = it }
            district?.let { updates["district"] = it }
            tier?.let { updates["tier"] = it }
            
            firestore.collection("users")
                .document(user.uid)
                .update(updates)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user data from Firestore
     */
    suspend fun getUserData(userId: String): Result<UserEntity?> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                val userEntity = document.toObject(UserEntity::class.java)
                Result.success(userEntity)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Save user to Firestore
     */
    private suspend fun saveUserToFirestore(userEntity: UserEntity) {
        firestore.collection("users")
            .document(userEntity.id)
            .set(userEntity)
            .await()
    }
    
    /**
     * Update last login time
     */
    private suspend fun updateLastLoginTime(userId: String) {
        try {
            firestore.collection("users")
                .document(userId)
                .update("lastLoginAt", Date())
                .await()
        } catch (e: Exception) {
            // Log error but don't fail the login process
        }
    }
    
    /**
     * Send email verification
     */
    suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val user = getCurrentUser() ?: throw Exception("No authenticated user")
            user.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if email is verified
     */
    fun isEmailVerified(): Boolean {
        return getCurrentUser()?.isEmailVerified ?: false
    }
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        return getCurrentUser()?.uid
    }
    
    /**
     * Get current user email
     */
    fun getCurrentUserEmail(): String? {
        return getCurrentUser()?.email
    }
    
    /**
     * Get current user display name
     */
    fun getCurrentUserDisplayName(): String? {
        return getCurrentUser()?.displayName
    }
}

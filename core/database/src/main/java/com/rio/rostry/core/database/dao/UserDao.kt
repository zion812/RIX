package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.UserEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Data Access Object for user-related operations
 */
@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE id = :userId")
    fun observeUser(userId: String): Flow<UserEntity?>
    
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Query("UPDATE users SET coinBalance = :balance, pendingCoinBalance = :pendingBalance, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updateCoinBalance(userId: String, balance: Int, pendingBalance: Int, updatedAt: Date = Date())
    
    @Query("UPDATE users SET tier = :tier, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updateUserTier(userId: String, tier: String, updatedAt: Date = Date())
    
    @Query("UPDATE users SET isEmailVerified = :verified, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updateEmailVerification(userId: String, verified: Boolean, updatedAt: Date = Date())
    
    @Query("UPDATE users SET isPhoneVerified = :verified, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updatePhoneVerification(userId: String, verified: Boolean, updatedAt: Date = Date())
    
    @Query("UPDATE users SET lastLoginAt = :loginTime, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updateLastLogin(userId: String, loginTime: Date, updatedAt: Date = Date())
    
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)
    
    // Sync operations
    @Query("SELECT * FROM users WHERE isSynced = 0")
    suspend fun getUnsyncedUsers(): List<UserEntity>
    
    @Query("UPDATE users SET isSynced = 1 WHERE id = :userId")
    suspend fun markUserAsSynced(userId: String)
    
    // Get current user (assuming single user app for now)
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?
    
    @Query("SELECT * FROM users")
    fun observeAllUsers(): Flow<List<UserEntity>>
    
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}
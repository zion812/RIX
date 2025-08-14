package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Simplified User DAO for Phase 2
 */
@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserByIdFlow(userId: String): Flow<UserEntity?>
    
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE region = :region AND district = :district")
    suspend fun getUsersByLocation(region: String, district: String): List<UserEntity>
    
    @Query("SELECT * FROM users WHERE tier = :tier")
    suspend fun getUsersByTier(tier: String): List<UserEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)
    
    @Query("UPDATE users SET lastLoginAt = :loginTime WHERE id = :userId")
    suspend fun updateLastLogin(userId: String, loginTime: java.util.Date)
    
    @Query("UPDATE users SET tier = :tier WHERE id = :userId")
    suspend fun updateUserTier(userId: String, tier: String)
    
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
    
    @Query("SELECT COUNT(*) FROM users WHERE region = :region")
    suspend fun getUserCountByRegion(region: String): Int
}

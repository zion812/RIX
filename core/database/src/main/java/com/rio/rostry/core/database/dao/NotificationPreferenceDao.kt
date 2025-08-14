package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.NotificationPreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationPreferenceDao {
    @Query("SELECT * FROM notification_preferences WHERE userId = :userId")
    suspend fun getPreferences(userId: String): NotificationPreferenceEntity?
    
    @Query("SELECT * FROM notification_preferences WHERE userId = :userId")
    fun observePreferences(userId: String): Flow<NotificationPreferenceEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preferences: NotificationPreferenceEntity)
    
    @Update
    suspend fun update(preferences: NotificationPreferenceEntity)
}

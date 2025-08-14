package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.TopicSubscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicSubscriptionDao {
    @Query("SELECT * FROM topic_subscriptions WHERE userId = :userId AND isSubscribed = 1")
    suspend fun getActiveSubscriptions(userId: String): List<TopicSubscriptionEntity>
    
    @Query("SELECT * FROM topic_subscriptions WHERE userId = :userId AND isSubscribed = 1")
    fun observeActiveSubscriptions(userId: String): Flow<List<TopicSubscriptionEntity>>
    
    @Query("SELECT * FROM topic_subscriptions WHERE userId = :userId AND topicName = :topicName")
    suspend fun getSubscription(userId: String, topicName: String): TopicSubscriptionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: TopicSubscriptionEntity)
    
    @Update
    suspend fun update(subscription: TopicSubscriptionEntity)
}

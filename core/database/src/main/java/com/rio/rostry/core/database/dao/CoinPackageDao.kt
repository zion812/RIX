package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.CoinPackageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoinPackageDao {
    @Query("SELECT * FROM coin_packages WHERE active = 1 AND user_tier = :userTier ORDER BY price ASC")
    suspend fun getActivePackagesForTier(userTier: String): List<CoinPackageEntity>
    
    @Query("SELECT * FROM coin_packages WHERE active = 1 AND user_tier = :userTier ORDER BY price ASC")
    fun observeActivePackagesForTier(userTier: String): Flow<List<CoinPackageEntity>>
    
    @Query("SELECT * FROM coin_packages WHERE package_id = :packageId")
    suspend fun getByPackageId(packageId: String): CoinPackageEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(packages: List<CoinPackageEntity>)
    
    @Query("DELETE FROM coin_packages WHERE active = 0 AND updated_at < :cutoffTime")
    suspend fun cleanupInactivePackages(cutoffTime: Long)
}

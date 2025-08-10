package com.rio.rostry.core.database.dao

import androidx.room.*
import com.rio.rostry.core.database.entities.RoosterEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Data Access Object for rooster operations
 * Includes family tree specific queries for lineage visualization
 */
@Dao
interface RoosterDao {
    
    // Basic CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rooster: RoosterEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(roosters: List<RoosterEntity>)
    
    @Update
    suspend fun update(rooster: RoosterEntity)
    
    @Delete
    suspend fun delete(rooster: RoosterEntity)
    
    @Query("DELETE FROM roosters WHERE id = :roosterId")
    suspend fun deleteById(roosterId: String)
    
    // Query operations
    @Query("SELECT * FROM roosters WHERE id = :roosterId")
    suspend fun getRoosterById(roosterId: String): RoosterEntity?
    
    @Query("SELECT * FROM roosters WHERE id = :roosterId")
    fun getRoosterByIdFlow(roosterId: String): Flow<RoosterEntity?>
    
    @Query("SELECT * FROM roosters WHERE ownerId = :ownerId ORDER BY name ASC")
    fun getRoostersByOwner(ownerId: String): Flow<List<RoosterEntity>>
    
    @Query("SELECT * FROM roosters WHERE ownerId = :ownerId ORDER BY name ASC")
    suspend fun getRoostersByOwnerSync(ownerId: String): List<RoosterEntity>
    
    @Query("SELECT * FROM roosters WHERE breed = :breed ORDER BY name ASC")
    fun getRoostersByBreed(breed: String): Flow<List<RoosterEntity>>
    
    @Query("SELECT * FROM roosters WHERE gender = :gender ORDER BY name ASC")
    fun getRoostersByGender(gender: String): Flow<List<RoosterEntity>>
    
    @Query("SELECT * FROM roosters ORDER BY name ASC")
    fun getAllRoosters(): Flow<List<RoosterEntity>>
    
    @Query("SELECT * FROM roosters ORDER BY name ASC")
    suspend fun getAllRoostersSync(): List<RoosterEntity>
    
    // Search operations
    @Query("SELECT * FROM roosters WHERE name LIKE :query OR breed LIKE :query ORDER BY name ASC")
    fun searchRoosters(query: String): Flow<List<RoosterEntity>>
    
    @Query("SELECT * FROM roosters WHERE name LIKE :query OR breed LIKE :query ORDER BY name ASC")
    suspend fun searchRoostersSync(query: String): List<RoosterEntity>
    
    // Count operations
    @Query("SELECT COUNT(*) FROM roosters")
    suspend fun getTotalCount(): Int
    
    @Query("SELECT COUNT(*) FROM roosters WHERE ownerId = :ownerId")
    suspend fun getCountByOwner(ownerId: String): Int
    
    @Query("SELECT COUNT(*) FROM roosters WHERE breed = :breed")
    suspend fun getCountByBreed(breed: String): Int
    
    // Family tree specific queries
    @Query("SELECT * FROM roosters WHERE fatherId = :parentId OR motherId = :parentId")
    suspend fun getChildrenOf(parentId: String): List<RoosterEntity>
    
    @Query("SELECT * FROM roosters WHERE id = :fatherId OR id = :motherId")
    suspend fun getParentsOf(fatherId: String?, motherId: String?): List<RoosterEntity>
    
    @Query("""
        WITH RECURSIVE family_tree AS (
            SELECT *, 0 as generation FROM roosters WHERE id = :rootId
            UNION ALL
            SELECT r.*, ft.generation + 1 
            FROM roosters r 
            JOIN family_tree ft ON (r.fatherId = ft.id OR r.motherId = ft.id)
            WHERE ft.generation < :maxDepth
        )
        SELECT * FROM family_tree
    """)
    suspend fun getFamilyTreeDescendants(rootId: String, maxDepth: Int = 10): List<RoosterEntity>
    
    @Query("""
        WITH RECURSIVE ancestors AS (
            SELECT *, 0 as generation FROM roosters WHERE id = :rootId
            UNION ALL
            SELECT r.*, a.generation + 1 
            FROM roosters r 
            JOIN ancestors a ON (r.id = a.fatherId OR r.id = a.motherId)
            WHERE a.generation < :maxDepth
        )
        SELECT * FROM ancestors
    """)
    suspend fun getFamilyTreeAncestors(rootId: String, maxDepth: Int = 10): List<RoosterEntity>
    
    @Query("SELECT * FROM roosters WHERE lineageVerified = 1")
    suspend fun getVerifiedLineageRoosters(): List<RoosterEntity>
    
    @Query("SELECT DISTINCT breed FROM roosters WHERE breed IS NOT NULL ORDER BY breed")
    suspend fun getAllBreeds(): List<String>
    
    @Query("""
        SELECT * FROM roosters 
        WHERE birthDate IS NOT NULL 
        AND (julianday('now') - julianday(birthDate)) / 365.25 BETWEEN :minAge AND :maxAge
    """)
    suspend fun getRoostersByAgeRange(minAge: Double, maxAge: Double): List<RoosterEntity>
    
    // Breeding related queries
    @Query("""
        SELECT * FROM roosters 
        WHERE gender = 'male' 
        AND birthDate IS NOT NULL 
        AND (julianday('now') - julianday(birthDate)) / 365.25 BETWEEN 0.5 AND 5
        AND healthStatus IN ('excellent', 'good')
        ORDER BY name ASC
    """)
    suspend fun getBreedingMales(): List<RoosterEntity>
    
    @Query("""
        SELECT * FROM roosters 
        WHERE gender = 'female' 
        AND birthDate IS NOT NULL 
        AND (julianday('now') - julianday(birthDate)) / 365.25 BETWEEN 0.5 AND 4
        AND healthStatus IN ('excellent', 'good')
        ORDER BY name ASC
    """)
    suspend fun getBreedingFemales(): List<RoosterEntity>
    
    // Health status queries
    @Query("SELECT * FROM roosters WHERE healthStatus = :status ORDER BY name ASC")
    suspend fun getRoostersByHealthStatus(status: String): List<RoosterEntity>
    
    @Query("SELECT healthStatus, COUNT(*) as count FROM roosters GROUP BY healthStatus")
    suspend fun getHealthStatusDistribution(): Map<String, Int>
    
    // Verification queries
    @Query("SELECT * FROM roosters WHERE lineageVerified = 1 ORDER BY name ASC")
    suspend fun getLineageVerifiedRoosters(): List<RoosterEntity>
    
    @Query("SELECT * FROM roosters WHERE healthCertified = 1 ORDER BY name ASC")
    suspend fun getHealthCertifiedRoosters(): List<RoosterEntity>
    
    @Query("SELECT * FROM roosters WHERE lineageVerified = 1 AND healthCertified = 1 ORDER BY name ASC")
    suspend fun getFullyVerifiedRoosters(): List<RoosterEntity>
    
    // Sync operations
    @Query("SELECT * FROM roosters WHERE isSynced = 0")
    suspend fun getUnsyncedRoosters(): List<RoosterEntity>
    
    @Query("UPDATE roosters SET isSynced = 1, syncedAt = :syncedAt WHERE id = :roosterId")
    suspend fun markAsSynced(roosterId: String, syncedAt: Date = Date())
    
    @Query("UPDATE roosters SET isSynced = 0 WHERE id = :roosterId")
    suspend fun markAsUnsynced(roosterId: String)
    
    // Batch operations
    @Query("UPDATE roosters SET ownerId = :newOwnerId WHERE id IN (:roosterIds)")
    suspend fun transferOwnership(roosterIds: List<String>, newOwnerId: String)
    
    @Query("UPDATE roosters SET healthStatus = :status WHERE id IN (:roosterIds)")
    suspend fun updateHealthStatus(roosterIds: List<String>, status: String)
    
    @Query("UPDATE roosters SET lineageVerified = :verified WHERE id IN (:roosterIds)")
    suspend fun updateLineageVerification(roosterIds: List<String>, verified: Boolean)
    
    @Query("UPDATE roosters SET healthCertified = :certified WHERE id IN (:roosterIds)")
    suspend fun updateHealthCertification(roosterIds: List<String>, certified: Boolean)
    
    // Statistics queries
    @Query("""
        SELECT breed, COUNT(*) as count 
        FROM roosters 
        WHERE breed IS NOT NULL 
        GROUP BY breed 
        ORDER BY count DESC
    """)
    suspend fun getBreedDistribution(): Map<String, Int>
    
    @Query("""
        SELECT gender, COUNT(*) as count 
        FROM roosters 
        WHERE gender IS NOT NULL 
        GROUP BY gender
    """)
    suspend fun getGenderDistribution(): Map<String, Int>
    
    @Query("""
        SELECT 
            CASE 
                WHEN (julianday('now') - julianday(birthDate)) / 365.25 < 1 THEN 'Under 1 year'
                WHEN (julianday('now') - julianday(birthDate)) / 365.25 < 2 THEN '1-2 years'
                WHEN (julianday('now') - julianday(birthDate)) / 365.25 < 3 THEN '2-3 years'
                WHEN (julianday('now') - julianday(birthDate)) / 365.25 < 5 THEN '3-5 years'
                ELSE 'Over 5 years'
            END as ageGroup,
            COUNT(*) as count
        FROM roosters 
        WHERE birthDate IS NOT NULL
        GROUP BY ageGroup
    """)
    suspend fun getAgeDistribution(): Map<String, Int>
    
    // Cleanup operations
    @Query("DELETE FROM roosters WHERE createdAt < :cutoffDate")
    suspend fun deleteOlderThan(cutoffDate: Date)
    
    @Query("DELETE FROM roosters WHERE ownerId = :ownerId")
    suspend fun deleteByOwner(ownerId: String)
}

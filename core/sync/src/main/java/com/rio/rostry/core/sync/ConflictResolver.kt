package com.rio.rostry.core.sync

import com.rio.rostry.core.database.entities.*
import com.rio.rostry.core.common.utils.JsonUtils
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Conflict resolution engine for handling sync conflicts
 * Implements different resolution strategies based on entity type and conflict nature
 */
@Singleton
class ConflictResolver @Inject constructor(
    private val jsonUtils: JsonUtils
) {
    
    /**
     * Resolve conflict for a syncable entity
     */
    suspend fun resolveConflict(entity: SyncableEntity): ConflictResolution {
        val strategy = getResolutionStrategy(entity)
        
        return when (strategy) {
            ConflictResolutionStrategy.LAST_WRITER_WINS -> resolveLastWriterWins(entity)
            ConflictResolutionStrategy.SERVER_AUTHORITATIVE -> resolveServerAuthoritative(entity)
            ConflictResolutionStrategy.CLIENT_AUTHORITATIVE -> resolveClientAuthoritative(entity)
            ConflictResolutionStrategy.MERGE_STRATEGY -> resolveMergeStrategy(entity)
            ConflictResolutionStrategy.USER_RESOLUTION -> resolveUserResolution(entity)
        }
    }
    
    /**
     * Get appropriate resolution strategy based on entity type and conflict
     */
    private fun getResolutionStrategy(entity: SyncableEntity): ConflictResolutionStrategy {
        return when (entity) {
            is TransferEntity -> {
                // Transfers are critical - always use server authoritative
                ConflictResolutionStrategy.SERVER_AUTHORITATIVE
            }
            is UserEntity -> {
                // User profile changes - merge non-conflicting fields
                ConflictResolutionStrategy.MERGE_STRATEGY
            }
            is FowlEntity -> {
                // Fowl data - last writer wins for most fields, merge for some
                if (isCriticalFowlField(entity)) {
                    ConflictResolutionStrategy.SERVER_AUTHORITATIVE
                } else {
                    ConflictResolutionStrategy.MERGE_STRATEGY
                }
            }
            is MarketplaceEntity -> {
                // Marketplace listings - last writer wins
                ConflictResolutionStrategy.LAST_WRITER_WINS
            }
            is MessageEntity -> {
                // Messages are immutable once sent - server authoritative
                ConflictResolutionStrategy.SERVER_AUTHORITATIVE
            }
            else -> {
                // Default strategy
                ConflictResolutionStrategy.LAST_WRITER_WINS
            }
        }
    }
    
    /**
     * Resolve using last writer wins strategy
     */
    private suspend fun resolveLastWriterWins(entity: SyncableEntity): ConflictResolution {
        val localVersion = entity.conflictMetadata.localVersion ?: 0L
        val serverVersion = entity.conflictMetadata.serverVersion ?: 0L
        
        return if (localVersion > serverVersion) {
            // Local version is newer - use local
            ConflictResolution(
                resolved = true,
                strategy = ConflictResolutionStrategy.LAST_WRITER_WINS,
                resolvedEntity = entity,
                resolution = "Local version is newer"
            )
        } else {
            // Server version is newer or equal - fetch and use server
            ConflictResolution(
                resolved = true,
                strategy = ConflictResolutionStrategy.LAST_WRITER_WINS,
                resolvedEntity = null, // Will be fetched from server
                resolution = "Server version is newer"
            )
        }
    }
    
    /**
     * Resolve using server authoritative strategy
     */
    private suspend fun resolveServerAuthoritative(entity: SyncableEntity): ConflictResolution {
        return ConflictResolution(
            resolved = true,
            strategy = ConflictResolutionStrategy.SERVER_AUTHORITATIVE,
            resolvedEntity = null, // Will be fetched from server
            resolution = "Server version is authoritative"
        )
    }
    
    /**
     * Resolve using client authoritative strategy
     */
    private suspend fun resolveClientAuthoritative(entity: SyncableEntity): ConflictResolution {
        return ConflictResolution(
            resolved = true,
            strategy = ConflictResolutionStrategy.CLIENT_AUTHORITATIVE,
            resolvedEntity = entity,
            resolution = "Client version is authoritative"
        )
    }
    
    /**
     * Resolve using merge strategy
     */
    private suspend fun resolveMergeStrategy(entity: SyncableEntity): ConflictResolution {
        return when (entity) {
            is UserEntity -> mergeUserEntity(entity)
            is FowlEntity -> mergeFowlEntity(entity)
            else -> {
                // Fallback to last writer wins
                resolveLastWriterWins(entity)
            }
        }
    }
    
    /**
     * Merge user entity fields
     */
    private suspend fun mergeUserEntity(entity: UserEntity): ConflictResolution {
        // For user entities, we can merge non-conflicting fields
        // Critical fields (tier, verification status) should use server version
        // Personal fields (bio, preferences) can use local version
        
        return ConflictResolution(
            resolved = true,
            strategy = ConflictResolutionStrategy.MERGE_STRATEGY,
            resolvedEntity = entity, // Simplified - would need server data to merge
            resolution = "Merged user profile fields",
            mergeDetails = mapOf(
                "critical_fields" to "server_version",
                "personal_fields" to "local_version",
                "statistics" to "server_version"
            )
        )
    }
    
    /**
     * Merge fowl entity fields
     */
    private suspend fun mergeFowlEntity(entity: FowlEntity): ConflictResolution {
        // For fowl entities:
        // - Ownership and transfer history: server authoritative
        // - Personal notes and tags: local version
        // - Health records: merge with timestamps
        // - Performance metrics: server authoritative
        
        return ConflictResolution(
            resolved = true,
            strategy = ConflictResolutionStrategy.MERGE_STRATEGY,
            resolvedEntity = entity, // Simplified - would need server data to merge
            resolution = "Merged fowl data fields",
            mergeDetails = mapOf(
                "ownership_fields" to "server_version",
                "personal_notes" to "local_version",
                "health_records" to "merged_by_timestamp",
                "performance_metrics" to "server_version"
            )
        )
    }
    
    /**
     * Resolve requiring user intervention
     */
    private suspend fun resolveUserResolution(entity: SyncableEntity): ConflictResolution {
        return ConflictResolution(
            resolved = false,
            strategy = ConflictResolutionStrategy.USER_RESOLUTION,
            resolvedEntity = null,
            resolution = "Requires user intervention",
            requiresUserInput = true
        )
    }
    
    /**
     * Check if fowl field is critical (ownership, health status, etc.)
     */
    private fun isCriticalFowlField(entity: FowlEntity): Boolean {
        // Check if the conflict involves critical fields
        return entity.conflictMetadata.hasConflict && (
            // Add logic to check which fields are in conflict
            // For now, assume all conflicts are non-critical
            false
        )
    }
    
    /**
     * Detect conflicts between local and server entities
     */
    fun detectConflict(local: SyncableEntity, server: SyncableEntity): ConflictDetection {
        if (local.id != server.id) {
            return ConflictDetection(hasConflict = false, reason = "Different entities")
        }
        
        val localVersion = local.conflictVersion
        val serverVersion = server.conflictVersion
        val localUpdated = local.updatedAt
        val serverUpdated = server.updatedAt
        
        // Check for version conflicts
        if (localVersion != serverVersion) {
            return ConflictDetection(
                hasConflict = true,
                reason = "Version mismatch",
                conflictType = ConflictType.VERSION_CONFLICT,
                localVersion = localVersion,
                serverVersion = serverVersion,
                localTimestamp = localUpdated,
                serverTimestamp = serverUpdated
            )
        }
        
        // Check for timestamp conflicts (same version but different timestamps)
        if (Math.abs(localUpdated.time - serverUpdated.time) > 1000) { // 1 second tolerance
            return ConflictDetection(
                hasConflict = true,
                reason = "Timestamp mismatch",
                conflictType = ConflictType.TIMESTAMP_CONFLICT,
                localVersion = localVersion,
                serverVersion = serverVersion,
                localTimestamp = localUpdated,
                serverTimestamp = serverUpdated
            )
        }
        
        // Check for field-level conflicts
        val fieldConflicts = detectFieldConflicts(local, server)
        if (fieldConflicts.isNotEmpty()) {
            return ConflictDetection(
                hasConflict = true,
                reason = "Field conflicts detected",
                conflictType = ConflictType.FIELD_CONFLICT,
                localVersion = localVersion,
                serverVersion = serverVersion,
                localTimestamp = localUpdated,
                serverTimestamp = serverUpdated,
                conflictingFields = fieldConflicts
            )
        }
        
        return ConflictDetection(hasConflict = false, reason = "No conflicts detected")
    }
    
    /**
     * Detect field-level conflicts between entities
     */
    private fun detectFieldConflicts(local: SyncableEntity, server: SyncableEntity): List<String> {
        val conflicts = mutableListOf<String>()
        
        // Convert entities to maps for comparison
        val localMap = entityToMap(local)
        val serverMap = entityToMap(server)
        
        // Compare each field
        localMap.forEach { (key, localValue) ->
            val serverValue = serverMap[key]
            if (serverValue != null && localValue != serverValue) {
                // Exclude sync metadata fields from conflict detection
                if (!key.startsWith("sync_") && !key.startsWith("conflict_")) {
                    conflicts.add(key)
                }
            }
        }
        
        return conflicts
    }
    
    /**
     * Convert entity to map for field comparison
     */
    private fun entityToMap(entity: SyncableEntity): Map<String, Any?> {
        // Simplified implementation - would use reflection or serialization
        return when (entity) {
            is UserEntity -> mapOf(
                "display_name" to entity.displayName,
                "email" to entity.email,
                "user_tier" to entity.userTier,
                "verification_status" to entity.verificationStatus
                // Add other fields as needed
            )
            is FowlEntity -> mapOf(
                "name" to entity.name,
                "breed_primary" to entity.breedPrimary,
                "owner_id" to entity.ownerId,
                "health_status" to entity.healthStatus
                // Add other fields as needed
            )
            else -> emptyMap()
        }
    }
    
    /**
     * Create conflict metadata for entity
     */
    fun createConflictMetadata(
        detection: ConflictDetection,
        strategy: ConflictResolutionStrategy
    ): ConflictMetadata {
        return ConflictMetadata(
            hasConflict = detection.hasConflict,
            conflictDetectedAt = Date(),
            serverVersion = detection.serverVersion,
            localVersion = detection.localVersion,
            conflictResolutionStrategy = strategy
        )
    }
}

/**
 * Conflict resolution result
 */
data class ConflictResolution(
    val resolved: Boolean,
    val strategy: ConflictResolutionStrategy,
    val resolvedEntity: SyncableEntity?,
    val resolution: String,
    val requiresUserInput: Boolean = false,
    val mergeDetails: Map<String, String> = emptyMap()
)

/**
 * Conflict detection result
 */
data class ConflictDetection(
    val hasConflict: Boolean,
    val reason: String,
    val conflictType: ConflictType? = null,
    val localVersion: Long? = null,
    val serverVersion: Long? = null,
    val localTimestamp: Date? = null,
    val serverTimestamp: Date? = null,
    val conflictingFields: List<String> = emptyList()
)

/**
 * Types of conflicts
 */
enum class ConflictType {
    VERSION_CONFLICT,
    TIMESTAMP_CONFLICT,
    FIELD_CONFLICT,
    OWNERSHIP_CONFLICT,
    DELETION_CONFLICT
}

package com.rio.rostry.core.sync

import com.rio.rostry.core.database.entities.SyncableEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Minimal conflict resolver implementing a last-write-wins policy using updatedAt timestamps.
 * This avoids strong coupling to specific entity types to keep compilation stable.
 */
@Singleton
class SyncConflictResolver @Inject constructor() {

    fun <T : SyncableEntity> resolve(
        local: T,
        remote: T?
    ): Resolution<T> {
        return when (remote) {
            null -> Resolution.UseLocal(local)
            else -> {
                val localUpdated = local.syncMetadata.updatedAt.time
                val remoteUpdated = remote.syncMetadata.updatedAt.time
                if (localUpdated >= remoteUpdated) Resolution.UseLocal(local) else Resolution.UseRemote(
                    remote
                )
            }
        }
    }
}

sealed class Resolution<T : SyncableEntity> {
    data class UseLocal<T : SyncableEntity>(val entity: T) : Resolution<T>()
    data class UseRemote<T : SyncableEntity>(val entity: T) : Resolution<T>()
}

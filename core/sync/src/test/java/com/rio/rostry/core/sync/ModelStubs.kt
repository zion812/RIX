package com.rio.rostry.core.sync

import com.rio.rostry.core.database.entities.SyncMetadata
import com.rio.rostry.core.database.entities.SyncStatus
import com.rio.rostry.core.database.entities.SyncableEntity
import java.util.*

data class TestEntity(
    override val id: String,
    val value: String,
    override val syncMetadata: SyncMetadata = SyncMetadata(
        syncStatus = SyncStatus.PENDING_UPLOAD,
        createdAt = Date(),
        updatedAt = Date()
    )
) : SyncableEntity

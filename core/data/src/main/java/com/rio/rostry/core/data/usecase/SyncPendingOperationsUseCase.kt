package com.rio.rostry.core.data.usecase

import com.rio.rostry.core.data.repository.SyncRepository
import com.rio.rostry.core.common.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for syncing pending operations from the outbox
 */
class SyncPendingOperationsUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {
    /**
     * Sync pending operations from the outbox
     *
     * @param limit Maximum number of operations to sync
     * @return Result containing the sync result or an error
     */
    suspend operator fun invoke(limit: Int = 50): Result<com.rio.rostry.core.data.repository.SyncResult> = withContext(Dispatchers.IO) {
        return@withContext try {
            syncRepository.syncPendingOperations(limit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

// We'll use the SyncResult from the repository directly
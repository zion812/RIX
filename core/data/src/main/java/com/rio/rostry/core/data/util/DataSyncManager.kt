package com.rio.rostry.core.data.util

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for synchronizing local database changes with remote Firestore
 */
@Singleton
class DataSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val syncQueue = mutableListOf<SyncOperation>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val tag = "DataSyncManager"

    /**
     * Queue a sync operation for later execution
     */
    fun queueSyncOperation(operation: SyncOperation) {
        synchronized(syncQueue) {
            syncQueue.add(operation)
        }
        processSyncQueue()
    }

    /**
     * Process all queued sync operations
     */
    private fun processSyncQueue() {
        coroutineScope.launch {
            val operationsToProcess = synchronized(syncQueue) {
                val operations = syncQueue.toList()
                syncQueue.clear()
                operations
            }

            operationsToProcess.forEach { operation ->
                try {
                    when (operation) {
                        is SyncOperation.Create -> {
                            firestore.collection(operation.collection)
                                .document(operation.documentId)
                                .set(operation.data)
                                .await()
                            Log.d(tag, "Successfully created document ${operation.documentId} in ${operation.collection}")
                        }
                        is SyncOperation.Update -> {
                            firestore.collection(operation.collection)
                                .document(operation.documentId)
                                .update(operation.data)
                                .await()
                            Log.d(tag, "Successfully updated document ${operation.documentId} in ${operation.collection}")
                        }
                        is SyncOperation.Delete -> {
                            firestore.collection(operation.collection)
                                .document(operation.documentId)
                                .delete()
                                .await()
                            Log.d(tag, "Successfully deleted document ${operation.documentId} in ${operation.collection}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Failed to sync operation for ${operation.documentId} in ${operation.collection}", e)
                    // Re-queue failed operations
                    synchronized(syncQueue) {
                        syncQueue.add(operation)
                    }
                }
            }
        }
    }

    /**
     * Force sync all pending operations
     */
    suspend fun forceSync(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val operationsToProcess = synchronized(syncQueue) {
                val operations = syncQueue.toList()
                syncQueue.clear()
                operations
            }

            operationsToProcess.forEach { operation ->
                when (operation) {
                    is SyncOperation.Create -> {
                        firestore.collection(operation.collection)
                            .document(operation.documentId)
                            .set(operation.data)
                            .await()
                        Log.d(tag, "Successfully created document ${operation.documentId} in ${operation.collection}")
                    }
                    is SyncOperation.Update -> {
                        firestore.collection(operation.collection)
                            .document(operation.documentId)
                            .update(operation.data)
                            .await()
                        Log.d(tag, "Successfully updated document ${operation.documentId} in ${operation.collection}")
                    }
                    is SyncOperation.Delete -> {
                        firestore.collection(operation.collection)
                            .document(operation.documentId)
                            .delete()
                            .await()
                        Log.d(tag, "Successfully deleted document ${operation.documentId} in ${operation.collection}")
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Failed to force sync", e)
            Result.failure(e)
        }
    }
}

/**
 * Represents a synchronization operation
 */
sealed class SyncOperation {
    abstract val collection: String
    abstract val documentId: String

    data class Create(
        override val collection: String,
        override val documentId: String,
        val data: Map<String, Any?>
    ) : SyncOperation()

    data class Update(
        override val collection: String,
        override val documentId: String,
        val data: Map<String, Any?>
    ) : SyncOperation()

    data class Delete(
        override val collection: String,
        override val documentId: String
    ) : SyncOperation()
}
package com.rio.rostry.core.media.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.rio.rostry.core.media.MediaRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker that handles uploading proof documents with retry and backoff
 */
class UploadProofWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val mediaRepository: MediaRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_PROOF_LOCAL_PATH = "proof_local_path"
        const val KEY_PROOF_REMOTE_URL = "proof_remote_url"
        const val KEY_FOWL_RECORD_ID = "fowl_record_id"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val localPath = inputData.getString(KEY_PROOF_LOCAL_PATH)
                ?: return@withContext Result.failure()
            
            val recordId = inputData.getString(KEY_FOWL_RECORD_ID)
                ?: return@withContext Result.failure()
            
            // Upload the proof
            val result = mediaRepository.uploadMedia(localPath)
            
            if (result is com.rio.rostry.core.common.model.Result.Success) {
                // Update the record with the remote URL
                val remoteUrl = result.data
                updateRecordWithProofUrl(recordId, remoteUrl)
                
                Result.success(
                    workDataOf(
                        KEY_PROOF_REMOTE_URL to remoteUrl
                    )
                )
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    private suspend fun updateRecordWithProofUrl(recordId: String, remoteUrl: String) {
        // In a real implementation, we would update the fowl record with the remote URL
        // This would involve calling the FowlRecordRepository to update the record
    }
}
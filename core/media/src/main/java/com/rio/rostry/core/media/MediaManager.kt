package com.rio.rostry.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.rio.rostry.core.common.compression.DataCompressionManager
import com.rio.rostry.core.common.compression.ImageQuality
import com.rio.rostry.core.common.exceptions.SyncException
import com.rio.rostry.core.database.RIOLocalDatabase
import com.rio.rostry.core.database.entities.MediaEntity
import com.rio.rostry.core.network.NetworkStateManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Comprehensive media manager for RIO platform
 * Optimized for rural India's network conditions with offline-first approach
 */
@Singleton
class MediaManager @Inject constructor(
    private val context: Context,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val database: RIOLocalDatabase,
    private val networkStateManager: NetworkStateManager,
    private val compressionManager: DataCompressionManager
) {
    
    private val mediaDao = database.mediaDao()
    
    companion object {
        private const val STORAGE_BUCKET = "rio-storage-bucket"
        private const val TEMP_UPLOAD_DIR = "temp/uploads"
        private const val CHUNK_SIZE = 256 * 1024 // 256KB chunks for slow networks
    }
    
    /**
     * Upload media with network-aware optimization
     */
    suspend fun uploadMedia(
        localUri: Uri,
        mediaType: MediaType,
        entityType: String,
        entityId: String,
        purpose: MediaPurpose = MediaPurpose.GENERAL
    ): Flow<MediaUploadResult> = flow {
        
        emit(MediaUploadResult.Progress(0, "Preparing upload..."))
        
        try {
            val userId = auth.currentUser?.uid 
                ?: throw SyncException.AuthError.NotAuthenticated()
            
            // Create media entity for tracking
            val mediaId = UUID.randomUUID().toString()
            val fileName = generateFileName(mediaType, purpose)
            val storagePath = generateStoragePath(userId, entityType, entityId, purpose, fileName)
            
            // Get file info
            val fileInfo = getFileInfo(localUri)
            emit(MediaUploadResult.Progress(5, "Analyzing file..."))
            
            // Validate file size and type
            validateFile(fileInfo, mediaType, purpose)
            emit(MediaUploadResult.Progress(10, "File validated"))
            
            // Compress based on network quality
            val compressionSettings = compressionManager.getOptimalCompressionSettings(
                networkStateManager.connectionQuality.value.name
            )
            
            val processedFile = when (mediaType) {
                MediaType.IMAGE -> compressImage(localUri, compressionSettings.imageQuality)
                MediaType.VIDEO -> compressVideo(localUri, compressionSettings.imageQuality)
                MediaType.DOCUMENT -> copyFile(localUri) // No compression for documents
            }
            
            emit(MediaUploadResult.Progress(30, "File processed"))
            
            // Create media entity
            val mediaEntity = MediaEntity(
                id = mediaId,
                entityType = entityType,
                entityId = entityId,
                mediaType = mediaType.name,
                fileName = fileName,
                fileSize = processedFile.length(),
                mimeType = fileInfo.mimeType,
                localPath = processedFile.absolutePath,
                uploadStatus = "PENDING",
                compressionApplied = mediaType != MediaType.DOCUMENT,
                createdAt = Date()
            )
            
            // Save to local database
            mediaDao.insert(mediaEntity)
            emit(MediaUploadResult.Progress(35, "Saved locally"))
            
            // Check network and upload strategy
            if (networkStateManager.isConnected.value) {
                // Upload immediately
                val uploadResult = performUpload(processedFile, storagePath, mediaEntity)
                emit(uploadResult)
            } else {
                // Queue for later upload
                emit(MediaUploadResult.QueuedForUpload(mediaId, "Queued for upload when online"))
            }
            
        } catch (e: Exception) {
            emit(MediaUploadResult.Error(e.toSyncException()))
        }
    }
    
    /**
     * Download media with progressive loading
     */
    suspend fun downloadMedia(
        mediaId: String,
        quality: MediaQuality = MediaQuality.AUTO
    ): Flow<MediaDownloadResult> = flow {
        
        emit(MediaDownloadResult.Progress(0, "Starting download..."))
        
        try {
            val mediaEntity = mediaDao.getById(mediaId)
                ?: throw SyncException.MediaError.FileNotFound("Media not found: $mediaId")
            
            // Check if already downloaded
            val localFile = File(mediaEntity.localPath)
            if (localFile.exists() && localFile.length() > 0) {
                emit(MediaDownloadResult.Success(localFile.absolutePath))
                return@flow
            }
            
            // Determine optimal quality based on network
            val targetQuality = when (quality) {
                MediaQuality.AUTO -> getOptimalQuality()
                else -> quality
            }
            
            val downloadUrl = getDownloadUrl(mediaEntity, targetQuality)
            emit(MediaDownloadResult.Progress(10, "Getting download URL..."))
            
            // Download with progress tracking
            val downloadedFile = downloadWithProgress(downloadUrl, mediaEntity.fileName) { progress ->
                // Emit progress updates
            }
            
            // Update media entity
            mediaDao.updateLocalPath(mediaId, downloadedFile.absolutePath)
            
            emit(MediaDownloadResult.Success(downloadedFile.absolutePath))
            
        } catch (e: Exception) {
            emit(MediaDownloadResult.Error(e.toSyncException()))
        }
    }
    
    /**
     * Get media URL for display with quality selection
     */
    suspend fun getMediaUrl(
        mediaId: String,
        quality: MediaQuality = MediaQuality.AUTO
    ): String? {
        return try {
            val mediaEntity = mediaDao.getById(mediaId) ?: return null
            
            // Check local file first
            val localFile = File(mediaEntity.localPath)
            if (localFile.exists()) {
                return localFile.absolutePath
            }
            
            // Get remote URL
            val targetQuality = when (quality) {
                MediaQuality.AUTO -> getOptimalQuality()
                else -> quality
            }
            
            getDownloadUrl(mediaEntity, targetQuality)
            
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Upload queued media when network becomes available
     */
    suspend fun uploadQueuedMedia(): Flow<QueueUploadResult> = flow {
        
        val pendingMedia = mediaDao.getPendingUploads()
        emit(QueueUploadResult.Started(pendingMedia.size))
        
        var successCount = 0
        var failureCount = 0
        
        for (media in pendingMedia) {
            try {
                val localFile = File(media.localPath)
                if (!localFile.exists()) {
                    mediaDao.updateUploadStatus(media.id, "FAILED", 0)
                    failureCount++
                    continue
                }
                
                val storagePath = reconstructStoragePath(media)
                val uploadResult = performUpload(localFile, storagePath, media)
                
                when (uploadResult) {
                    is MediaUploadResult.Success -> {
                        successCount++
                        emit(QueueUploadResult.Progress(successCount, failureCount, media.fileName))
                    }
                    is MediaUploadResult.Error -> {
                        failureCount++
                        emit(QueueUploadResult.Progress(successCount, failureCount, media.fileName))
                    }
                    else -> { /* Handle other cases */ }
                }
                
            } catch (e: Exception) {
                failureCount++
                mediaDao.updateUploadStatus(media.id, "FAILED", 0)
            }
        }
        
        emit(QueueUploadResult.Completed(successCount, failureCount))
    }
    
    /**
     * Perform actual upload with progress tracking
     */
    private suspend fun performUpload(
        file: File,
        storagePath: String,
        mediaEntity: MediaEntity
    ): MediaUploadResult {
        return try {
            val storageRef = storage.reference.child(storagePath)
            
            // Update status to uploading
            mediaDao.updateUploadStatus(mediaEntity.id, "UPLOADING", 0)
            
            // Upload with progress tracking
            val uploadTask = storageRef.putFile(Uri.fromFile(file))
            
            // Track progress
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                // Update progress in database
                mediaDao.updateUploadStatus(mediaEntity.id, "UPLOADING", progress)
            }
            
            val result = uploadTask.await()
            val downloadUrl = result.storage.downloadUrl.await()
            
            // Update media entity with remote URL
            mediaDao.updateRemoteUrl(mediaEntity.id, downloadUrl.toString(), Date())
            mediaDao.updateUploadStatus(mediaEntity.id, "COMPLETED", 100)
            
            MediaUploadResult.Success(mediaEntity.id, downloadUrl.toString())
            
        } catch (e: Exception) {
            mediaDao.updateUploadStatus(mediaEntity.id, "FAILED", 0)
            MediaUploadResult.Error(e.toSyncException())
        }
    }
    
    /**
     * Compress image based on network quality
     */
    private suspend fun compressImage(uri: Uri, quality: ImageQuality): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw SyncException.MediaError.FileNotFound("Cannot open input stream")
        
        val bitmap = BitmapFactory.decodeStream(inputStream)
            ?: throw SyncException.MediaError.CompressionFailed("Cannot decode image", "Invalid image format")
        
        val tempFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(tempFile)
        
        val compressionQuality = when (quality) {
            ImageQuality.HIGH -> 90
            ImageQuality.MEDIUM -> 70
            ImageQuality.LOW -> 50
            ImageQuality.VERY_LOW -> 30
            ImageQuality.THUMBNAIL -> 60
        }
        
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressionQuality, outputStream)
        outputStream.close()
        bitmap.recycle()
        
        return tempFile
    }
    
    /**
     * Compress video (placeholder - would use actual video compression library)
     */
    private suspend fun compressVideo(uri: Uri, quality: ImageQuality): File {
        // For now, just copy the file
        // In production, would use video compression library
        return copyFile(uri)
    }
    
    /**
     * Copy file without compression
     */
    private suspend fun copyFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw SyncException.MediaError.FileNotFound("Cannot open input stream")
        
        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}")
        val outputStream = FileOutputStream(tempFile)
        
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        
        return tempFile
    }
    
    /**
     * Generate storage path based on entity and purpose
     */
    private fun generateStoragePath(
        userId: String,
        entityType: String,
        entityId: String,
        purpose: MediaPurpose,
        fileName: String
    ): String {
        return when (entityType) {
            "fowl" -> "users/$userId/fowls/$entityId/${purpose.path}/$fileName"
            "user" -> "users/$userId/profile/${purpose.path}/$fileName"
            "marketplace" -> "users/$userId/fowls/$entityId/marketplace/${purpose.path}/$fileName"
            "transfer" -> "users/$userId/fowls/$entityId/transfers/${purpose.path}/$fileName"
            else -> "users/$userId/general/$fileName"
        }
    }
    
    /**
     * Generate filename with timestamp and purpose
     */
    private fun generateFileName(mediaType: MediaType, purpose: MediaPurpose): String {
        val timestamp = System.currentTimeMillis()
        val extension = when (mediaType) {
            MediaType.IMAGE -> "jpg"
            MediaType.VIDEO -> "mp4"
            MediaType.DOCUMENT -> "pdf"
        }
        return "${timestamp}_${purpose.name.lowercase()}.$extension"
    }
    
    /**
     * Get optimal quality based on network conditions
     */
    private fun getOptimalQuality(): MediaQuality {
        return when (networkStateManager.connectionQuality.value.name) {
            "EXCELLENT" -> MediaQuality.HIGH
            "GOOD" -> MediaQuality.MEDIUM
            "FAIR" -> MediaQuality.LOW
            "POOR" -> MediaQuality.VERY_LOW
            else -> MediaQuality.LOW
        }
    }
    
    /**
     * Get file information from URI
     */
    private fun getFileInfo(uri: Uri): FileInfo {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                
                FileInfo(
                    size = if (sizeIndex >= 0) it.getLong(sizeIndex) else 0L,
                    name = if (nameIndex >= 0) it.getString(nameIndex) else "unknown",
                    mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
                )
            } else {
                FileInfo(0L, "unknown", "application/octet-stream")
            }
        } ?: FileInfo(0L, "unknown", "application/octet-stream")
    }
    
    /**
     * Validate file size and type based on user tier and purpose
     */
    private fun validateFile(fileInfo: FileInfo, mediaType: MediaType, purpose: MediaPurpose) {
        // Get user tier from auth token (simplified)
        val userTier = "GENERAL" // Would get from auth token
        
        val maxSize = when (mediaType) {
            MediaType.IMAGE -> when (userTier) {
                "GENERAL" -> 5 * 1024 * 1024L // 5MB
                "FARMER" -> 10 * 1024 * 1024L // 10MB
                "ENTHUSIAST" -> 20 * 1024 * 1024L // 20MB
                else -> 5 * 1024 * 1024L
            }
            MediaType.VIDEO -> when (userTier) {
                "GENERAL" -> 10 * 1024 * 1024L // 10MB
                "FARMER" -> 30 * 1024 * 1024L // 30MB
                "ENTHUSIAST" -> 50 * 1024 * 1024L // 50MB
                else -> 10 * 1024 * 1024L
            }
            MediaType.DOCUMENT -> 10 * 1024 * 1024L // 10MB for all
        }
        
        if (fileInfo.size > maxSize) {
            throw SyncException.MediaError.FileTooLarge(
                fileInfo.name,
                fileInfo.size,
                maxSize
            )
        }
        
        // Validate MIME type
        val allowedTypes = when (mediaType) {
            MediaType.IMAGE -> listOf("image/jpeg", "image/png", "image/webp")
            MediaType.VIDEO -> listOf("video/mp4", "video/3gpp", "video/quicktime")
            MediaType.DOCUMENT -> listOf("application/pdf", "image/jpeg", "image/png")
        }
        
        if (!allowedTypes.contains(fileInfo.mimeType)) {
            throw SyncException.MediaError.UnsupportedFormat(fileInfo.name, fileInfo.mimeType)
        }
    }
    
    // Additional helper methods would be implemented here...
    private suspend fun getDownloadUrl(mediaEntity: MediaEntity, quality: MediaQuality): String {
        // Implementation for getting download URL based on quality
        return mediaEntity.remoteUrl ?: ""
    }
    
    private suspend fun downloadWithProgress(url: String, fileName: String, onProgress: (Int) -> Unit): File {
        // Implementation for downloading with progress
        return File(context.cacheDir, fileName)
    }
    
    private fun reconstructStoragePath(media: MediaEntity): String {
        // Implementation for reconstructing storage path
        return ""
    }
}

/**
 * Data classes and enums
 */
data class FileInfo(
    val size: Long,
    val name: String,
    val mimeType: String
)

enum class MediaType {
    IMAGE, VIDEO, DOCUMENT
}

enum class MediaPurpose(val path: String) {
    PROFILE("profile"),
    GALLERY("gallery"),
    HEALTH("health"),
    BREEDING("breeding"),
    TRANSFER_VERIFICATION("transfers/verification"),
    TRANSFER_LEGAL("transfers/legal"),
    MARKETPLACE("marketplace"),
    GENERAL("general")
}

enum class MediaQuality {
    AUTO, THUMBNAIL, LOW, MEDIUM, HIGH, ORIGINAL
}

sealed class MediaUploadResult {
    data class Progress(val percentage: Int, val message: String) : MediaUploadResult()
    data class Success(val mediaId: String, val remoteUrl: String) : MediaUploadResult()
    data class QueuedForUpload(val mediaId: String, val message: String) : MediaUploadResult()
    data class Error(val exception: SyncException) : MediaUploadResult()
}

sealed class MediaDownloadResult {
    data class Progress(val percentage: Int, val message: String) : MediaDownloadResult()
    data class Success(val localPath: String) : MediaDownloadResult()
    data class Error(val exception: SyncException) : MediaDownloadResult()
}

sealed class QueueUploadResult {
    data class Started(val totalCount: Int) : QueueUploadResult()
    data class Progress(val successCount: Int, val failureCount: Int, val currentFile: String) : QueueUploadResult()
    data class Completed(val successCount: Int, val failureCount: Int) : QueueUploadResult()
}

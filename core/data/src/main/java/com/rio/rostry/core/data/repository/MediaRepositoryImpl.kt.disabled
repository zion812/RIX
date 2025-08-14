package com.rio.rostry.core.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.rio.rostry.core.database.RIOLocalDatabase
import com.rio.rostry.core.database.entities.MediaEntity
import com.rio.rostry.core.media.MediaManager
import com.rio.rostry.core.media.MediaType
import com.rio.rostry.core.media.MediaPurpose
import com.rio.rostry.core.media.MediaQuality
import com.rio.rostry.core.network.NetworkStateManager
import com.rio.rostry.core.common.exceptions.SyncException
import com.rio.rostry.shared.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ✅ Enhanced media repository with compression and rural optimization
 * Integrates with existing sync system and optimizes for rural networks
 */
@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val database: RIOLocalDatabase,
    private val mediaManager: MediaManager,
    private val networkStateManager: NetworkStateManager,
    private val context: Context
) {

    private val mediaDao = database.mediaDao()

    // ✅ Media optimization configuration
    private val compressionQuality = 80
    private val maxImageSize = 1024 // 1024x1024 max for rural networks
    private val thumbnailSize = 256 // 256x256 thumbnails
    private val maxVideoSize = 50 * 1024 * 1024 // 50MB max

    // ✅ Progressive loading cache
    private val thumbnailCache = mutableMapOf<String, String>()
    private val compressionCache = mutableMapOf<String, ByteArray>()
    
    /**
     * ✅ Upload media with compression and offline-first approach
     */
    suspend fun uploadMedia(
        localUri: Uri,
        entityType: String,
        entityId: String,
        mediaType: MediaType,
        purpose: MediaPurpose = MediaPurpose.GENERAL
    ): Flow<MediaUploadProgress> = flow {

        emit(MediaUploadProgress.Started("Preparing upload..."))

        try {
            // ✅ Step 1: Compress media for rural networks
            emit(MediaUploadProgress.Compressing(0.1f, "Compressing media..."))
            val compressedMedia = compressMedia(localUri, mediaType)

            // ✅ Step 2: Create thumbnail for progressive loading
            emit(MediaUploadProgress.Compressing(0.3f, "Creating thumbnail..."))
            val thumbnail = createThumbnail(localUri, mediaType)

            // ✅ Step 3: Save locally first (offline-first)
            emit(MediaUploadProgress.Compressing(0.5f, "Saving locally..."))
            val localMedia = saveMediaLocally(compressedMedia, thumbnail, entityType, entityId, mediaType)

            // Check if we're online
            val isOnline = networkStateManager.isConnected.first()

            if (isOnline) {
                // ✅ Upload compressed media with progress tracking
                emit(MediaUploadProgress.Uploading(0.6f, "Uploading compressed media..."))

                mediaManager.uploadMedia(localMedia.compressedUri, mediaType, entityType, entityId, purpose)
                    .collect { result ->
                        when (result) {
                            is com.rio.rostry.core.media.MediaUploadResult.Progress -> {
                                val adjustedProgress = 0.6f + (result.percentage * 0.4f)
                                emit(MediaUploadProgress.Uploading(adjustedProgress, result.message))
                            }
                            is com.rio.rostry.core.media.MediaUploadResult.Success -> {
                                // ✅ Upload thumbnail separately
                                uploadThumbnail(localMedia.thumbnailUri, result.mediaId)
                                emit(MediaUploadProgress.Completed(result.mediaId, result.remoteUrl))
                            }
                            is com.rio.rostry.core.media.MediaUploadResult.QueuedForUpload -> {
                                emit(MediaUploadProgress.QueuedForLater(result.mediaId, result.message))
                            }
                            is com.rio.rostry.core.media.MediaUploadResult.Error -> {
                                emit(MediaUploadProgress.Failed(result.exception))
                            }
                        }
                    }
            } else {
                // Queue for later upload
                val mediaId = queueMediaForUpload(localUri, entityType, entityId, mediaType, purpose)
                emit(MediaUploadProgress.QueuedForLater(mediaId, "Queued for upload when online"))
            }
            
        } catch (e: Exception) {
            emit(MediaUploadProgress.Failed(e.toSyncException()))
        }
    }
    
    /**
     * Get media with progressive loading based on network quality
     */
    suspend fun getMedia(
        mediaId: String,
        preferredQuality: MediaQuality = MediaQuality.AUTO
    ): Flow<MediaLoadResult> = flow {
        
        emit(MediaLoadResult.Loading("Loading media..."))
        
        try {
            val mediaEntity = mediaDao.getById(mediaId)
                ?: throw SyncException.MediaError.FileNotFound("Media not found: $mediaId")
            
            // Check local file first
            val localFile = java.io.File(mediaEntity.localPath)
            if (localFile.exists() && localFile.length() > 0) {
                emit(MediaLoadResult.Success(
                    mediaUrl = localFile.absolutePath,
                    isLocal = true,
                    quality = MediaQuality.ORIGINAL
                ))
                return@flow
            }
            
            // Determine optimal quality based on network
            val targetQuality = when (preferredQuality) {
                MediaQuality.AUTO -> getOptimalQualityForNetwork()
                else -> preferredQuality
            }
            
            // Download with progress
            mediaManager.downloadMedia(mediaId, targetQuality)
                .collect { result ->
                    when (result) {
                        is com.rio.rostry.core.media.MediaDownloadResult.Progress -> {
                            emit(MediaLoadResult.Loading(result.message))
                        }
                        is com.rio.rostry.core.media.MediaDownloadResult.Success -> {
                            emit(MediaLoadResult.Success(
                                mediaUrl = result.localPath,
                                isLocal = true,
                                quality = targetQuality
                            ))
                        }
                        is com.rio.rostry.core.media.MediaDownloadResult.Error -> {
                            emit(MediaLoadResult.Failed(result.exception))
                        }
                    }
                }
            
        } catch (e: Exception) {
            emit(MediaLoadResult.Failed(e.toSyncException()))
        }
    }
    
    /**
     * Get media URL for immediate display (may return lower quality for fast loading)
     */
    suspend fun getMediaUrl(
        mediaId: String,
        quality: MediaQuality = MediaQuality.AUTO
    ): String? {
        return try {
            mediaManager.getMediaUrl(mediaId, quality)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get media for fowl with gallery support
     */
    suspend fun getFowlMedia(
        fowlId: String,
        mediaType: MediaType? = null,
        purpose: MediaPurpose? = null
    ): Flow<List<MediaItem>> = flow {
        
        try {
            val mediaEntities = if (mediaType != null) {
                mediaDao.getByEntity("fowl", fowlId).filter { 
                    it.mediaType == mediaType.name &&
                    (purpose == null || it.localPath.contains(purpose.path))
                }
            } else {
                mediaDao.getByEntity("fowl", fowlId)
            }
            
            val mediaItems = mediaEntities.map { entity ->
                MediaItem(
                    id = entity.id,
                    entityType = entity.entityType,
                    entityId = entity.entityId,
                    mediaType = MediaType.valueOf(entity.mediaType),
                    fileName = entity.fileName,
                    fileSize = entity.fileSize,
                    localPath = entity.localPath,
                    remoteUrl = entity.remoteUrl,
                    thumbnailPath = entity.thumbnailPath,
                    uploadStatus = UploadStatus.valueOf(entity.uploadStatus),
                    uploadProgress = entity.uploadProgress,
                    createdAt = entity.createdAt,
                    uploadedAt = entity.uploadedAt
                )
            }
            
            emit(mediaItems)
            
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get transfer verification media with high priority
     */
    suspend fun getTransferVerificationMedia(
        transferId: String,
        verificationType: TransferVerificationType
    ): Flow<List<MediaItem>> = flow {
        
        try {
            val pathPattern = when (verificationType) {
                TransferVerificationType.BEFORE_TRANSFER -> "transfers/verification/before_transfer"
                TransferVerificationType.DURING_TRANSFER -> "transfers/verification/during_transfer"
                TransferVerificationType.AFTER_TRANSFER -> "transfers/verification/after_transfer"
                TransferVerificationType.LEGAL_DOCUMENTS -> "transfers/legal"
            }
            
            // Get all media for the transfer
            val allTransferMedia = mediaDao.getByEntity("transfer", transferId)
            
            // Filter by verification type
            val verificationMedia = allTransferMedia.filter { 
                it.localPath.contains(pathPattern) 
            }
            
            val mediaItems = verificationMedia.map { entity ->
                MediaItem(
                    id = entity.id,
                    entityType = entity.entityType,
                    entityId = entity.entityId,
                    mediaType = MediaType.valueOf(entity.mediaType),
                    fileName = entity.fileName,
                    fileSize = entity.fileSize,
                    localPath = entity.localPath,
                    remoteUrl = entity.remoteUrl,
                    thumbnailPath = entity.thumbnailPath,
                    uploadStatus = UploadStatus.valueOf(entity.uploadStatus),
                    uploadProgress = entity.uploadProgress,
                    createdAt = entity.createdAt,
                    uploadedAt = entity.uploadedAt
                )
            }
            
            emit(mediaItems)
            
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Upload queued media when network becomes available
     */
    suspend fun uploadQueuedMedia(): Flow<QueueUploadProgress> = flow {
        
        try {
            mediaManager.uploadQueuedMedia()
                .collect { result ->
                    when (result) {
                        is com.rio.rostry.core.media.QueueUploadResult.Started -> {
                            emit(QueueUploadProgress.Started(result.totalCount))
                        }
                        is com.rio.rostry.core.media.QueueUploadResult.Progress -> {
                            emit(QueueUploadProgress.Progress(
                                successCount = result.successCount,
                                failureCount = result.failureCount,
                                currentFile = result.currentFile
                            ))
                        }
                        is com.rio.rostry.core.media.QueueUploadResult.Completed -> {
                            emit(QueueUploadProgress.Completed(
                                successCount = result.successCount,
                                failureCount = result.failureCount
                            ))
                        }
                    }
                }
            
        } catch (e: Exception) {
            emit(QueueUploadProgress.Failed(e.toSyncException()))
        }
    }
    
    /**
     * Get storage usage statistics for user
     */
    suspend fun getStorageUsage(userId: String): StorageUsage {
        return try {
            val userMedia = mediaDao.getByEntity("user", userId)
            
            var totalSize = 0L
            var imageCount = 0
            var videoCount = 0
            var documentCount = 0
            var pendingUploads = 0
            
            userMedia.forEach { media ->
                totalSize += media.fileSize
                
                when (media.mediaType) {
                    "IMAGE" -> imageCount++
                    "VIDEO" -> videoCount++
                    "DOCUMENT" -> documentCount++
                }
                
                if (media.uploadStatus == "PENDING") {
                    pendingUploads++
                }
            }
            
            StorageUsage(
                totalSizeBytes = totalSize,
                totalSizeMB = totalSize / (1024.0 * 1024.0),
                imageCount = imageCount,
                videoCount = videoCount,
                documentCount = documentCount,
                pendingUploads = pendingUploads,
                lastCalculated = Date()
            )
            
        } catch (e: Exception) {
            StorageUsage(
                totalSizeBytes = 0L,
                totalSizeMB = 0.0,
                imageCount = 0,
                videoCount = 0,
                documentCount = 0,
                pendingUploads = 0,
                lastCalculated = Date()
            )
        }
    }
    
    /**
     * Delete media with cleanup
     */
    suspend fun deleteMedia(mediaId: String): Boolean {
        return try {
            val mediaEntity = mediaDao.getById(mediaId) ?: return false
            
            // Delete local file
            val localFile = java.io.File(mediaEntity.localPath)
            if (localFile.exists()) {
                localFile.delete()
            }
            
            // Delete from database
            mediaDao.deleteById(mediaId)
            
            // TODO: Delete from Firebase Storage (would need storage reference)
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Private helper methods
     */
    private suspend fun queueMediaForUpload(
        localUri: Uri,
        entityType: String,
        entityId: String,
        mediaType: MediaType,
        purpose: MediaPurpose
    ): String {
        val mediaId = UUID.randomUUID().toString()
        
        // Copy file to app storage for queuing
        val fileName = "queued_${System.currentTimeMillis()}_${mediaType.name.lowercase()}"
        // Implementation would copy file and create media entity
        
        return mediaId
    }
    
    private suspend fun getOptimalQualityForNetwork(): MediaQuality {
        return when (networkStateManager.connectionQuality.first().name) {
            "EXCELLENT" -> MediaQuality.HIGH
            "GOOD" -> MediaQuality.MEDIUM
            "FAIR" -> MediaQuality.LOW
            "POOR" -> MediaQuality.THUMBNAIL
            else -> MediaQuality.LOW
        }
    }
}

/**
 * Domain models for media operations
 */
data class MediaItem(
    val id: String,
    val entityType: String,
    val entityId: String,
    val mediaType: MediaType,
    val fileName: String,
    val fileSize: Long,
    val localPath: String,
    val remoteUrl: String?,
    val thumbnailPath: String?,
    val uploadStatus: UploadStatus,
    val uploadProgress: Int,
    val createdAt: Date,
    val uploadedAt: Date?
)

data class StorageUsage(
    val totalSizeBytes: Long,
    val totalSizeMB: Double,
    val imageCount: Int,
    val videoCount: Int,
    val documentCount: Int,
    val pendingUploads: Int,
    val lastCalculated: Date
)

enum class UploadStatus {
    PENDING, UPLOADING, COMPLETED, FAILED
}

enum class TransferVerificationType {
    BEFORE_TRANSFER,
    DURING_TRANSFER,
    AFTER_TRANSFER,
    LEGAL_DOCUMENTS
}

sealed class MediaUploadProgress {
    data class Started(val message: String) : MediaUploadProgress()
    data class Uploading(val percentage: Int, val message: String) : MediaUploadProgress()
    data class Completed(val mediaId: String, val remoteUrl: String) : MediaUploadProgress()
    data class QueuedForLater(val mediaId: String, val message: String) : MediaUploadProgress()
    data class Failed(val exception: SyncException) : MediaUploadProgress()
}

sealed class MediaLoadResult {
    data class Loading(val message: String) : MediaLoadResult()
    data class Success(val mediaUrl: String, val isLocal: Boolean, val quality: MediaQuality) : MediaLoadResult()
    data class Failed(val exception: SyncException) : MediaLoadResult()
}

sealed class QueueUploadProgress {
    data class Started(val totalCount: Int) : QueueUploadProgress()
    data class Progress(val successCount: Int, val failureCount: Int, val currentFile: String) : QueueUploadProgress()
    data class Completed(val successCount: Int, val failureCount: Int) : QueueUploadProgress()
    data class Failed(val exception: SyncException) : QueueUploadProgress()
}

/**
 * Extension function to convert exceptions
 */
private fun Exception.toSyncException(): SyncException {
    return when (this) {
        is SyncException -> this
        else -> SyncException.UnknownError(message ?: "Unknown error", this)
    }

    /**
     * ✅ Compress media based on type and rural network optimization
     */
    private suspend fun compressMedia(uri: Uri, mediaType: MediaType): CompressedMediaData {
        return when (mediaType) {
            MediaType.IMAGE -> compressImage(uri)
            MediaType.VIDEO -> compressVideo(uri)
            else -> CompressedMediaData(uri, uri.toString().toByteArray())
        }
    }

    /**
     * ✅ Compress image with optimal settings for rural networks
     */
    private suspend fun compressImage(uri: Uri): CompressedMediaData = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)

            // ✅ Calculate optimal size maintaining aspect ratio
            val (newWidth, newHeight) = calculateOptimalSize(
                originalBitmap.width,
                originalBitmap.height,
                maxImageSize
            )

            // ✅ Resize bitmap
            val resizedBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                newWidth,
                newHeight,
                true
            )

            // ✅ Compress to JPEG with quality optimization
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, compressionQuality, outputStream)

            // ✅ Save compressed image to local file
            val compressedFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            compressedFile.writeBytes(outputStream.toByteArray())

            // ✅ Clean up
            originalBitmap.recycle()
            resizedBitmap.recycle()

            CompressedMediaData(
                uri = Uri.fromFile(compressedFile),
                data = outputStream.toByteArray()
            )

        } catch (e: Exception) {
            throw MediaCompressionException("Failed to compress image", e)
        }
    }

    /**
     * ✅ Create thumbnail for progressive loading
     */
    private suspend fun createThumbnail(uri: Uri, mediaType: MediaType): ThumbnailData = withContext(Dispatchers.IO) {
        when (mediaType) {
            MediaType.IMAGE -> createImageThumbnail(uri)
            MediaType.VIDEO -> createVideoThumbnail(uri)
            else -> ThumbnailData(uri, byteArrayOf())
        }
    }

    /**
     * ✅ Create image thumbnail
     */
    private suspend fun createImageThumbnail(uri: Uri): ThumbnailData {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)

            // ✅ Create square thumbnail
            val size = minOf(originalBitmap.width, originalBitmap.height)
            val x = (originalBitmap.width - size) / 2
            val y = (originalBitmap.height - size) / 2

            val squareBitmap = Bitmap.createBitmap(originalBitmap, x, y, size, size)
            val thumbnailBitmap = Bitmap.createScaledBitmap(
                squareBitmap,
                thumbnailSize,
                thumbnailSize,
                true
            )

            val outputStream = ByteArrayOutputStream()
            thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)

            // Save thumbnail to local file
            val thumbnailFile = File(context.cacheDir, "thumb_${System.currentTimeMillis()}.jpg")
            thumbnailFile.writeBytes(outputStream.toByteArray())

            // Clean up
            originalBitmap.recycle()
            squareBitmap.recycle()
            thumbnailBitmap.recycle()

            ThumbnailData(
                uri = Uri.fromFile(thumbnailFile),
                data = outputStream.toByteArray()
            )

        } catch (e: Exception) {
            throw MediaCompressionException("Failed to create thumbnail", e)
        }
    }

    /**
     * ✅ Save media locally for offline access
     */
    private suspend fun saveMediaLocally(
        compressedMedia: CompressedMediaData,
        thumbnail: ThumbnailData,
        entityType: String,
        entityId: String,
        mediaType: MediaType
    ): LocalMediaData {
        val mediaEntity = MediaEntity(
            id = UUID.randomUUID().toString(),
            entityType = entityType,
            entityId = entityId,
            mediaType = mediaType,
            localPath = compressedMedia.uri.path ?: "",
            thumbnailPath = thumbnail.uri.path ?: "",
            isUploaded = false,
            createdAt = Date(),
            updatedAt = Date()
        )

        mediaDao.insert(mediaEntity)

        return LocalMediaData(
            compressedUri = compressedMedia.uri,
            thumbnailUri = thumbnail.uri,
            mediaEntity = mediaEntity
        )
    }

    /**
     * ✅ Calculate optimal image size for rural networks
     */
    private fun calculateOptimalSize(
        originalWidth: Int,
        originalHeight: Int,
        maxSize: Int
    ): Pair<Int, Int> {
        if (originalWidth <= maxSize && originalHeight <= maxSize) {
            return Pair(originalWidth, originalHeight)
        }

        val ratio = originalWidth.toFloat() / originalHeight.toFloat()

        return if (originalWidth > originalHeight) {
            Pair(maxSize, (maxSize / ratio).toInt())
        } else {
            Pair((maxSize * ratio).toInt(), maxSize)
        }
    }

    /**
     * ✅ Progressive image loading with thumbnail first
     */
    suspend fun loadImageProgressively(mediaId: String): Flow<ProgressiveImageData> = flow {
        try {
            val mediaEntity = mediaDao.getById(mediaId)

            if (mediaEntity != null) {
                // ✅ Emit thumbnail first for immediate display
                if (mediaEntity.thumbnailPath.isNotEmpty()) {
                    emit(ProgressiveImageData.Thumbnail(Uri.parse(mediaEntity.thumbnailPath)))
                }

                // ✅ Then emit full image
                if (mediaEntity.localPath.isNotEmpty()) {
                    emit(ProgressiveImageData.FullImage(Uri.parse(mediaEntity.localPath)))
                } else if (mediaEntity.remoteUrl?.isNotEmpty() == true) {
                    // Download and cache if not available locally
                    val localUri = downloadAndCache(mediaEntity.remoteUrl!!, mediaId)
                    emit(ProgressiveImageData.FullImage(localUri))
                }
            }
        } catch (e: Exception) {
            emit(ProgressiveImageData.Error(e.message ?: "Failed to load image"))
        }
    }

    private suspend fun compressVideo(uri: Uri): CompressedMediaData {
        // Placeholder for video compression
        return CompressedMediaData(uri, byteArrayOf())
    }

    private suspend fun createVideoThumbnail(uri: Uri): ThumbnailData {
        // Placeholder for video thumbnail creation
        return ThumbnailData(uri, byteArrayOf())
    }

    private suspend fun uploadThumbnail(thumbnailUri: Uri, mediaId: String) {
        // Placeholder for thumbnail upload
    }

    private suspend fun downloadAndCache(remoteUrl: String, mediaId: String): Uri {
        // Placeholder for download and cache
        return Uri.EMPTY
    }
}

/**
 * ✅ Data classes for media optimization
 */
data class CompressedMediaData(
    val uri: Uri,
    val data: ByteArray
)

data class ThumbnailData(
    val uri: Uri,
    val data: ByteArray
)

data class LocalMediaData(
    val compressedUri: Uri,
    val thumbnailUri: Uri,
    val mediaEntity: MediaEntity
)

sealed class ProgressiveImageData {
    data class Thumbnail(val uri: Uri) : ProgressiveImageData()
    data class FullImage(val uri: Uri) : ProgressiveImageData()
    data class Error(val message: String) : ProgressiveImageData()
}

sealed class MediaUploadProgress {
    data class Started(val message: String) : MediaUploadProgress()
    data class Compressing(val progress: Float, val message: String) : MediaUploadProgress()
    data class Uploading(val progress: Float, val message: String) : MediaUploadProgress()
    data class Completed(val mediaId: String, val remoteUrl: String) : MediaUploadProgress()
    data class Error(val message: String) : MediaUploadProgress()
}

class MediaCompressionException(message: String, cause: Throwable? = null) : Exception(message, cause)

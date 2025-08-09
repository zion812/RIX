package com.rio.rostry.core.common.compression

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.rio.rostry.core.common.exceptions.SyncException
import java.io.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data compression manager for optimizing network usage in rural areas
 * Handles text, JSON, and image compression for 2G/3G networks
 */
@Singleton
class DataCompressionManager @Inject constructor() {
    
    companion object {
        private const val COMPRESSION_THRESHOLD = 1024 // 1KB - compress data larger than this
        private const val MAX_IMAGE_WIDTH = 1920
        private const val MAX_IMAGE_HEIGHT = 1080
        private const val THUMBNAIL_SIZE = 150
    }
    
    /**
     * Compress text data using GZIP
     */
    fun compressText(data: String): CompressedData {
        return try {
            if (data.length < COMPRESSION_THRESHOLD) {
                // Don't compress small data
                return CompressedData(
                    data = data.toByteArray(Charsets.UTF_8),
                    isCompressed = false,
                    originalSize = data.length.toLong(),
                    compressedSize = data.length.toLong(),
                    compressionRatio = 1.0
                )
            }
            
            val originalBytes = data.toByteArray(Charsets.UTF_8)
            val output = ByteArrayOutputStream()
            
            GZIPOutputStream(output).use { gzip ->
                gzip.write(originalBytes)
            }
            
            val compressedBytes = output.toByteArray()
            val compressionRatio = originalBytes.size.toDouble() / compressedBytes.size.toDouble()
            
            CompressedData(
                data = compressedBytes,
                isCompressed = true,
                originalSize = originalBytes.size.toLong(),
                compressedSize = compressedBytes.size.toLong(),
                compressionRatio = compressionRatio
            )
        } catch (e: Exception) {
            throw SyncException.DataProcessingError.CompressionFailed("Text compression failed", e)
        }
    }
    
    /**
     * Decompress text data
     */
    fun decompressText(compressedData: CompressedData): String {
        return try {
            if (!compressedData.isCompressed) {
                return String(compressedData.data, Charsets.UTF_8)
            }
            
            val input = ByteArrayInputStream(compressedData.data)
            val output = ByteArrayOutputStream()
            
            GZIPInputStream(input).use { gzip ->
                gzip.copyTo(output)
            }
            
            String(output.toByteArray(), Charsets.UTF_8)
        } catch (e: Exception) {
            throw SyncException.DataProcessingError.DecompressionFailed("Text decompression failed", e)
        }
    }
    
    /**
     * Compress JSON data with additional optimizations
     */
    fun compressJson(jsonData: String): CompressedData {
        return try {
            // First, minify JSON by removing unnecessary whitespace
            val minifiedJson = minifyJson(jsonData)
            
            // Then compress using GZIP
            compressText(minifiedJson)
        } catch (e: Exception) {
            throw SyncException.DataProcessingError.CompressionFailed("JSON compression failed", e)
        }
    }
    
    /**
     * Compress image with quality adjustment based on network conditions
     */
    fun compressImage(
        imagePath: String,
        targetQuality: ImageQuality = ImageQuality.MEDIUM,
        createThumbnail: Boolean = true
    ): ImageCompressionResult {
        return try {
            val originalFile = File(imagePath)
            if (!originalFile.exists()) {
                throw SyncException.MediaError.FileNotFound(imagePath)
            }
            
            val originalBitmap = BitmapFactory.decodeFile(imagePath)
                ?: throw SyncException.MediaError.CompressionFailed(imagePath, "Cannot decode image")
            
            val originalSize = originalFile.length()
            
            // Resize if necessary
            val resizedBitmap = resizeImage(originalBitmap, targetQuality)
            
            // Compress with quality setting
            val compressedBytes = compressBitmap(resizedBitmap, targetQuality)
            
            // Create thumbnail if requested
            val thumbnailBytes = if (createThumbnail) {
                val thumbnailBitmap = createThumbnail(originalBitmap)
                compressBitmap(thumbnailBitmap, ImageQuality.THUMBNAIL)
            } else null
            
            // Clean up bitmaps
            if (resizedBitmap != originalBitmap) {
                resizedBitmap.recycle()
            }
            originalBitmap.recycle()
            
            ImageCompressionResult(
                compressedData = compressedBytes,
                thumbnailData = thumbnailBytes,
                originalSize = originalSize,
                compressedSize = compressedBytes.size.toLong(),
                thumbnailSize = thumbnailBytes?.size?.toLong() ?: 0L,
                compressionRatio = originalSize.toDouble() / compressedBytes.size.toDouble(),
                targetQuality = targetQuality
            )
        } catch (e: Exception) {
            when (e) {
                is SyncException -> throw e
                else -> throw SyncException.MediaError.CompressionFailed(imagePath, e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Resize image based on target quality
     */
    private fun resizeImage(bitmap: Bitmap, quality: ImageQuality): Bitmap {
        val (maxWidth, maxHeight) = when (quality) {
            ImageQuality.HIGH -> Pair(MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT)
            ImageQuality.MEDIUM -> Pair(1280, 720)
            ImageQuality.LOW -> Pair(854, 480)
            ImageQuality.VERY_LOW -> Pair(640, 360)
            ImageQuality.THUMBNAIL -> Pair(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
        }
        
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return bitmap
        }
        
        val ratio = minOf(
            maxWidth.toFloat() / originalWidth,
            maxHeight.toFloat() / originalHeight
        )
        
        val newWidth = (originalWidth * ratio).toInt()
        val newHeight = (originalHeight * ratio).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * Compress bitmap to byte array
     */
    private fun compressBitmap(bitmap: Bitmap, quality: ImageQuality): ByteArray {
        val compressionQuality = when (quality) {
            ImageQuality.HIGH -> 90
            ImageQuality.MEDIUM -> 70
            ImageQuality.LOW -> 50
            ImageQuality.VERY_LOW -> 30
            ImageQuality.THUMBNAIL -> 60
        }
        
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressionQuality, output)
        return output.toByteArray()
    }
    
    /**
     * Create thumbnail from bitmap
     */
    private fun createThumbnail(bitmap: Bitmap): Bitmap {
        val size = THUMBNAIL_SIZE
        val width = bitmap.width
        val height = bitmap.height
        
        val ratio = if (width > height) {
            size.toFloat() / width
        } else {
            size.toFloat() / height
        }
        
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * Minify JSON by removing unnecessary whitespace
     */
    private fun minifyJson(json: String): String {
        return json
            .replace(Regex("\\s+"), " ") // Replace multiple whitespace with single space
            .replace(Regex("\\s*([{}\\[\\],:])+\\s*")) { match ->
                match.value.trim() // Remove spaces around JSON syntax characters
            }
            .trim()
    }
    
    /**
     * Get optimal compression settings based on network quality
     */
    fun getOptimalCompressionSettings(networkQuality: String): CompressionSettings {
        return when (networkQuality.uppercase()) {
            "EXCELLENT" -> CompressionSettings(
                imageQuality = ImageQuality.HIGH,
                compressText = false,
                createThumbnails = true,
                aggressiveCompression = false
            )
            "GOOD" -> CompressionSettings(
                imageQuality = ImageQuality.MEDIUM,
                compressText = true,
                createThumbnails = true,
                aggressiveCompression = false
            )
            "FAIR" -> CompressionSettings(
                imageQuality = ImageQuality.LOW,
                compressText = true,
                createThumbnails = true,
                aggressiveCompression = true
            )
            "POOR" -> CompressionSettings(
                imageQuality = ImageQuality.VERY_LOW,
                compressText = true,
                createThumbnails = true,
                aggressiveCompression = true
            )
            "VERY_POOR" -> CompressionSettings(
                imageQuality = ImageQuality.THUMBNAIL,
                compressText = true,
                createThumbnails = false, // Only send thumbnails
                aggressiveCompression = true
            )
            else -> CompressionSettings(
                imageQuality = ImageQuality.MEDIUM,
                compressText = true,
                createThumbnails = true,
                aggressiveCompression = false
            )
        }
    }
}

/**
 * Image quality levels for compression
 */
enum class ImageQuality {
    HIGH,       // Original quality, minimal compression
    MEDIUM,     // 70% quality, good balance
    LOW,        // 50% quality, smaller size
    VERY_LOW,   // 30% quality, very small size
    THUMBNAIL   // Small thumbnail only
}

/**
 * Compressed data result
 */
data class CompressedData(
    val data: ByteArray,
    val isCompressed: Boolean,
    val originalSize: Long,
    val compressedSize: Long,
    val compressionRatio: Double
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as CompressedData
        
        if (!data.contentEquals(other.data)) return false
        if (isCompressed != other.isCompressed) return false
        if (originalSize != other.originalSize) return false
        if (compressedSize != other.compressedSize) return false
        if (compressionRatio != other.compressionRatio) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + isCompressed.hashCode()
        result = 31 * result + originalSize.hashCode()
        result = 31 * result + compressedSize.hashCode()
        result = 31 * result + compressionRatio.hashCode()
        return result
    }
}

/**
 * Image compression result
 */
data class ImageCompressionResult(
    val compressedData: ByteArray,
    val thumbnailData: ByteArray?,
    val originalSize: Long,
    val compressedSize: Long,
    val thumbnailSize: Long,
    val compressionRatio: Double,
    val targetQuality: ImageQuality
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as ImageCompressionResult
        
        if (!compressedData.contentEquals(other.compressedData)) return false
        if (thumbnailData != null) {
            if (other.thumbnailData == null) return false
            if (!thumbnailData.contentEquals(other.thumbnailData)) return false
        } else if (other.thumbnailData != null) return false
        
        return originalSize == other.originalSize &&
                compressedSize == other.compressedSize &&
                thumbnailSize == other.thumbnailSize &&
                compressionRatio == other.compressionRatio &&
                targetQuality == other.targetQuality
    }
    
    override fun hashCode(): Int {
        var result = compressedData.contentHashCode()
        result = 31 * result + (thumbnailData?.contentHashCode() ?: 0)
        result = 31 * result + originalSize.hashCode()
        result = 31 * result + compressedSize.hashCode()
        result = 31 * result + thumbnailSize.hashCode()
        result = 31 * result + compressionRatio.hashCode()
        result = 31 * result + targetQuality.hashCode()
        return result
    }
}

/**
 * Compression settings based on network conditions
 */
data class CompressionSettings(
    val imageQuality: ImageQuality,
    val compressText: Boolean,
    val createThumbnails: Boolean,
    val aggressiveCompression: Boolean
)
